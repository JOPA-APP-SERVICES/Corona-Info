package de.jopa.coronainfo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class CoronaDataUpdateOnBoot extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            AlarmHandler alarmHandler = new AlarmHandler(context);
            alarmHandler.setAlarmManager();
        }
    }
}