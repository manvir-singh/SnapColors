package com.manvir.SnapColors;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.manvir.logger.Logger;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.ExecutionException;

public class SnapColorsUpdater extends Service{
	public SnapColorsUpdater() {
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	
	@Override
	public void onCreate() {
		
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		try {
			if(new updateAv().execute(getPackageManager()).get()){
                Intent myIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://repo.xposed.info/module/com.manvir.SnapColors"));
                PendingIntent pendingIntent = PendingIntent.getActivity(getApplication(), 0, myIntent, 0);
				NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
				NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
				        .setSmallIcon(R.drawable.ic_launcher)
				        .setContentTitle("SnapColors Update Available")
				        .setContentText("A new version of SnapColors is available, please update.")
				        .setOnlyAlertOnce(true)
				        .setTicker("SnapColors Update Available!")
                        .setContentIntent(pendingIntent)
                        .setAutoCancel(true).setStyle(new NotificationCompat.BigTextStyle().bigText("A new version of SnapColors is available, please update."));
				mNotificationManager.notify(0, mBuilder.build());
			}
		} catch (InterruptedException | ExecutionException e) {
			e.printStackTrace();
		}
        return Service.START_NOT_STICKY;
	}
	
	public static class updateAv extends AsyncTask<PackageManager, Void, Boolean>{
		@Override
		protected Boolean doInBackground(PackageManager... packageManager) {
			boolean returnVal = false;
            try {
                Logger.log("Checking for new version.");
                String currentVersion = packageManager[0].getPackageInfo("com.manvir.SnapColors", 0).versionName;
                String updateUrl = "https://programming4life.com/snapcolors/updater.php?version="+currentVersion;
                HttpClient httpclient = new DefaultHttpClient(); // Create HTTP Client
                HttpGet httpget = new HttpGet(updateUrl); // Set the action you want to do
                HttpResponse response = httpclient.execute(httpget); // Executeit
                HttpEntity entity = response.getEntity();
                InputStream is = entity.getContent(); // Create an InputStream with the response
                BufferedReader reader = new BufferedReader(new InputStreamReader(is, "iso-8859-1"), 8);
                StringBuilder sb = new StringBuilder();
                String line = null;
                while ((line = reader.readLine()) != null) // Read line by line
                    sb.append(line + "\n");

                String resString = sb.toString(); // Result is here
                JSONObject jsonObject = new JSONObject(resString);
                is.close(); // Close the stream
                returnVal = jsonObject.getBoolean("shouldUpdate");
            }
            catch (Exception e) {
                e.printStackTrace();
            }
			return returnVal;
		}
	}

}
