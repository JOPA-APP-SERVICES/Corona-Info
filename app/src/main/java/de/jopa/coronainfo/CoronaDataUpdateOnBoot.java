package de.jopa.coronainfo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class CoronaDataUpdateOnBoot extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        AlarmHandler alarmHandler = new AlarmHandler(context);
        alarmHandler.cancelAlarmManager();
        alarmHandler.setAlarmManager();
    }
}