package com.manvir.logger;

import android.util.Log;

import com.manvir.SnapColors.BuildConfig;

import de.robv.android.xposed.XposedBridge;

public class Logger {
    static boolean showLogs = BuildConfig.DEBUG;
    static String TAG = "SnapColors";

    public static void log(String msg){
        if(!showLogs)
            return;

        try {
            XposedBridge.log(TAG+": "+msg);
        } catch (NoClassDefFoundError e){
            Log.d(TAG, msg);
        }
    }
}
