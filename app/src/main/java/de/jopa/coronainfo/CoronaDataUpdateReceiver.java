package de.jopa.coronainfo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.util.Log;

public class CoronaDataUpdateReceiver extends BroadcastReceiver {
    Context context;


    @Override
    public void onReceive(Context context, Intent intent) {
        /*this.context = context;
        Log.i("MINT1", "update1");
        if (isConnected()) {
            Log.i("MINT1", "update2");
            CoronaData cD = new CoronaData(context);
            cD.loadData();
        }*/
        Log.i("MINT1", "Service running");

    }

    public boolean isConnected() {
        ConnectivityManager cm = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetwork() != null;
    }
}
