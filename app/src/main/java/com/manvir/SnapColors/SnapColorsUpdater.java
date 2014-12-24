package com.manvir.SnapColors;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.ExecutionException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

public class SnapColorsUpdater extends Service{
    static String TAG = "SnapColorsUpdater";
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
			if(new updateAv().execute().get()){
                Intent myIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://repo.xposed.info/module/com.manvir.SnapColors"));
                PendingIntent pendingIntent = PendingIntent.getActivity(
                        getApplication(),
                        0,
                        myIntent,
                        0);
				NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
				NotificationCompat.Builder mBuilder =
				        new NotificationCompat.Builder(this)
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
	
	class updateAv extends AsyncTask<Void, Void, Boolean>{
		@Override
		protected Boolean doInBackground(Void... params) {
			String updateUrl = "http://104.236.75.226/snapcolors/version";
			boolean returnVal = false;

            try {
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
                try{
                    Integer.valueOf(resString.replaceAll("\\W", ""));
                    if(!resString.contains(getPackageManager().getPackageInfo(getPackageName(), 0).versionName)){
                        returnVal = true;
                    }
                }catch (NumberFormatException e){
                    returnVal = false;
                }

                is.close(); // Close the stream
            }
            catch (Exception e) {
                e.printStackTrace();
            }
			return returnVal;
		}
	}

}
