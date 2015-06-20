package com.manvir.SnapColors;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.manvir.common.SETTINGS;

import java.util.Calendar;

public class onBooted extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (BuildConfig.DEBUG) return;
        if (context.getSharedPreferences(SETTINGS.NAME, Context.MODE_WORLD_READABLE).getBoolean(SETTINGS.KEYS.checkForVer, SETTINGS.DEFAULTS.checkForVer)) {
            Intent intent1 = new Intent(context, SnapColorsUpdater.class);
            PendingIntent pintent = PendingIntent.getService(context, 0, intent1, 0);
            AlarmManager alarm = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            alarm.setRepeating(AlarmManager.RTC_WAKEUP, Calendar.getInstance().getTimeInMillis(), 86400000, pintent);//
        }
    }
}