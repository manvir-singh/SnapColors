package com.manvir.logger;

import android.util.Log;

import com.manvir.SnapColors.BuildConfig;

import de.robv.android.xposed.XposedBridge;

public class Logger {
    static boolean showLogs = BuildConfig.DEBUG;
    static String TAG = "SnapColors";

    public static void log(Object msg){
        if(!showLogs)
            return;

        try {
            XposedBridge.log(TAG+": "+String.valueOf(msg));
        } catch (NoClassDefFoundError e){
            Log.d(TAG, String.valueOf(msg));
        }
    }
}
