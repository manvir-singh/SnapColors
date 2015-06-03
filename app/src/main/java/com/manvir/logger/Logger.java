package com.manvir.logger;

import android.util.Log;

import com.manvir.SnapColors.BuildConfig;

import java.util.Arrays;

import de.robv.android.xposed.XposedBridge;

public class Logger {
    static boolean showLogs = BuildConfig.DEBUG;
    static String TAG = "SnapColors";

    public static void log(Object msg) {
        if (!showLogs)
            return;

        if (msg == null) msg = "NULL";
        if (msg.getClass().isArray()) //noinspection ConstantConditions
            msg = Arrays.toString((Object[]) msg);
        try {
            XposedBridge.log(TAG + ": " + String.valueOf(msg));
        } catch (NoClassDefFoundError e) {
            Log.d(TAG, String.valueOf(msg));
        }
    }
}
