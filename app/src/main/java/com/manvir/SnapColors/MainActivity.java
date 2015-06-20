package com.manvir.SnapColors;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.startapp.android.publish.StartAppAd;
import com.startapp.android.publish.StartAppSDK;

import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity {
    private StartAppAd startAppAd = new StartAppAd(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //For ad network
        StartAppSDK.init(this, "101601243", "201780741", true);
        startAppAd.showAd();

        setContentView(R.layout.activity_main);
        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.container, new Settings()).commit();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        //For ad network
        startAppAd.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        //For ad network
        startAppAd.onPause();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        MenuItem versionMenuItem = menu.findItem(R.id.action_version);
        versionMenuItem.setTitle("Version: " + BuildConfig.VERSION_NAME);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_version:
                try {
                    Toast.makeText(getApplicationContext(), "Checking for update.", Toast.LENGTH_SHORT).show();
                    if (new SnapColorsUpdater.updateAv().execute(getPackageManager()).get()) {
                        Toast.makeText(getApplicationContext(), "New update available open Xposed and update.", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(getApplicationContext(), "Already have the latest version.", Toast.LENGTH_LONG).show();
                    }
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(), "Update check failed!", Toast.LENGTH_LONG).show();
                }
                break;
            case R.id.action_twitter:
                Intent twitterIntent = new Intent(Intent.ACTION_VIEW);
                twitterIntent.setData(Uri.parse("https://twitter.com/iphone4life4"));
                startActivity(twitterIntent);
                break;
            case R.id.action_donate:
                startActivity(new Intent(getApplication(), DonateActivity.class));
                break;
            case R.id.action_help:
                Intent xdaIntent = new Intent(Intent.ACTION_VIEW);
                xdaIntent.setData(Uri.parse("http://forum.xda-developers.com/xposed/modules/mod-snapcolors-add-colors-to-snapchat-t2716416"));
                startActivity(xdaIntent);
                break;
            case R.id.action_about:
                startActivity(new Intent(getApplicationContext(), AboutActivity.class));
                break;
        }
        return id == R.id.action_settings || super.onOptionsItemSelected(item);
    }
}