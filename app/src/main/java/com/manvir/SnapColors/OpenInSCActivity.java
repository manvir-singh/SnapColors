package com.manvir.SnapColors;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import com.manvir.common.PACKAGES;
import com.manvir.common.SETTINGS;

import java.io.File;

//This activity is never shown it is transparent
public class OpenInSCActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        boolean isImage = true;
        if (intent.getType().startsWith("video/")) isImage = false;
        if (!isImage) {
            File video = new File(((Uri) intent.getExtras().get(Intent.EXTRA_STREAM)).getPath());
            if (video.length() > 2500000) { //2.50MB
                Toast.makeText(this, "Video file size is to large", Toast.LENGTH_LONG).show();
                return;
            }
        }
        SharedPreferences prefs = getSharedPreferences(SETTINGS.NAME, Context.MODE_WORLD_READABLE);
        if (!isImage && prefs.getBoolean(SETTINGS.KEYS.resizeVideoAlert, SETTINGS.DEFAULTS.resizeVideoAlert)) {
            AlertDialog.Builder resizeVideoAlert = new AlertDialog.Builder(this) {{
                setTitle("Warning");
                setMessage("Video resizing is buggy. Your videos may or may not get resized correctly. (This is a one time warning)");
                setPositiveButton("Okay", (dialog, which) -> {
                    initSC();
                    intent.putExtra("com.manvir.SnapColors.isSnapColors", true);
                    intent.putExtra("com.manvir.SnapColors.isImage", false);
                    intent.setComponent(new ComponentName(PACKAGES.SNAPCHAT, PACKAGES.SNAPCHAT + ".LandingPageActivity"));
                    prefs.edit().putBoolean(SETTINGS.KEYS.resizeVideoAlert, false).apply();
                    startActivity(intent);
                    finish();
                });
            }};
            resizeVideoAlert.show();
        } else if (!isImage) {
            initSC();
            intent.putExtra("com.manvir.SnapColors.isSnapColors", true);
            intent.putExtra("com.manvir.SnapColors.isImage", false);
            intent.setComponent(new ComponentName(PACKAGES.SNAPCHAT, PACKAGES.SNAPCHAT + ".LandingPageActivity"));
            prefs.edit().putBoolean(SETTINGS.KEYS.resizeVideoAlert, false).apply();
            startActivity(intent);
            finish();
        } else {
            initSC();
            intent.putExtra("com.manvir.SnapColors.isSnapColors", true);
            intent.putExtra("com.manvir.SnapColors.isImage", true);
            intent.setComponent(new ComponentName(PACKAGES.SNAPCHAT, PACKAGES.SNAPCHAT + ".LandingPageActivity"));
            startActivity(intent);
            finish();
        }
    }

    /**
     * We need snapchat to initialize it self with the data we proved it.
     * The only way to do this is to force close snapchat and then let it recreate it self from scratch with our data.
     */
    private void initSC() {
        ((ActivityManager) getSystemService(ACTIVITY_SERVICE)).killBackgroundProcesses(PACKAGES.SNAPCHAT);
    }
}
