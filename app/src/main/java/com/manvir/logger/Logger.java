package com.manvir.logger;

import android.util.Log;

import de.robv.android.xposed.XposedBridge;

public class Logger {
    String tag;
    boolean showLogs = false;
    public Logger(String tag){
        this.tag = tag;
    }

    public void logLogcat(String msg){
        if(!showLogs)
            return;
        Log.i(tag, msg);
    }

    public void log(String msg){
        if(!showLogs)
            return;
        XposedBridge.log("SnapColors: "+msg);
    }

    public void shouldShowLogs(boolean YesOrNo){
        showLogs = YesOrNo;
    }
}
