package de.jopa.coronainfo;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import java.util.Calendar;

public class AlarmHandler {
    private Context context;

    public AlarmHandler(Context context) {
        this.context = context;
    }

    @SuppressLint("ShortAlarm")
    public void setAlarmManager() {
        Intent intent = new Intent(context, CoronaDataUpdateReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 2, intent, PendingIntent.FLAG_IMMUTABLE);
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        long triggerAfter = Calendar.getInstance().getTimeInMillis();
        long triggerEvery = 60 * 1000 * 15; //15 minutes

        am.setRepeating(AlarmManager.RTC_WAKEUP, triggerAfter, triggerEvery, pendingIntent);
    }

    public void cancelAlarmManager() {
        Intent intent = new Intent(context, CoronaDataUpdateReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 2, intent, PendingIntent.FLAG_IMMUTABLE);
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        am.cancel(pendingIntent);
    }
}
