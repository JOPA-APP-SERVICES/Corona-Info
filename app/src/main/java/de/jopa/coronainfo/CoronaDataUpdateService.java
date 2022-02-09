package de.jopa.coronainfo;

import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Handler;
import android.os.Looper;

public class CoronaDataUpdateService extends BroadcastReceiver {
    Context context;
    @Override
    public void onReceive(Context context, Intent intent) {
        this.context = context;
        if (isConnected()) {
            CoronaData cD = new CoronaData(context);
            cD.loadData();
        } else {
            while (!(isConnected())) {
                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    CoronaData cD = new CoronaData(context);
                    cD.loadData();
                    AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
                    ComponentName widget = new ComponentName(context, CoronaDataWidget.class);
                    int[] appWidgetIds = appWidgetManager.getAppWidgetIds(widget);
                    appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.textViewInfos);
                }, 1000);
            }
        }
    }

    public boolean isConnected() {
        ConnectivityManager cm = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm.getActiveNetwork().toString() == null) {
            return false;
        }
        return true;
    }
}
