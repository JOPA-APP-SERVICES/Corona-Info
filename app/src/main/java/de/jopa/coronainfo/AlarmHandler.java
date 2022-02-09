package de.jopa.coronainfo;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

public class AlarmHandler {
    private Context context;

    public AlarmHandler(Context context) {
        this.context = context;
    }

    public void setAlarmManager() {
        Intent intent = new Intent(context, CoronaDataUpdateService.class);
        PendingIntent sender = PendingIntent.getBroadcast(context, 2, intent, 0);
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        long triggerAfter = 0;
        long triggerEvery = 60 * 60 * 1000; //One hour
        am.setRepeating(AlarmManager.RTC_WAKEUP, triggerAfter, triggerEvery, sender);
    }

    public void cancelAlarmManager() {
        Intent intent = new Intent(context, CoronaDataUpdateService.class);
        PendingIntent sender = PendingIntent.getBroadcast(context, 2, intent, 0);
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        am.cancel(sender);
    }
}