package com.manvir.SnapColors;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

//This activity is never shown it is transparent
public class OpenInSCActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        if (intent.getType().startsWith("image/")) {
            initSC();
            intent.setComponent(new ComponentName("com.snapchat.android", "com.snapchat.android.LandingPageActivity"));
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(this, "Images only!", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    /**
     * We need snapchat to initialize it self with the data we proved it.
     * The only way to do this is to force close snapchat and then let it recreate it self from scratch with our data.
     */
    private void initSC() {
        ((ActivityManager) getSystemService(ACTIVITY_SERVICE)).killBackgroundProcesses("com.snapchat.android");
    }
}
