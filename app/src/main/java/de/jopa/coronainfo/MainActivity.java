package de.jopa.coronainfo;

import android.app.NotificationManager;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.github.kaiwinter.androidremotenotifications.RemoteNotifications;
import com.github.kaiwinter.androidremotenotifications.model.UpdatePolicy;
import org.json.JSONException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        try {
            if ("/coronainfo/addVaccinationCard".equals(getIntent().getData().getPath())) {
                int appWidgetId = Integer.parseInt(getIntent().getData().getQueryParameter("appWidgetId"));
                String svg = getIntent().getData().getQueryParameter("svg");
                VaccinationCardWidgetConfigureActivity.savePath(MainActivity.this, appWidgetId, svg);

                AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(MainActivity.this);
                ComponentName widget = new ComponentName(MainActivity.this, VaccinationCardWidget.class);
                appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetId, R.id.imageView);

                NotificationManager notificationManager1 = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                notificationManager1.cancel(1);

                NotificationCompat.Builder builder = new NotificationCompat.Builder(MainActivity.this, "1")
                        .setSmallIcon(R.drawable.ic_launcher_foreground)
                        .setContentTitle("Corona Info Impfkartenwidget einrichten")
                        .setContentText("Die Einrichtung ihres Impfkartenwidgets ist abgeschlossen.")
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT);

                NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);

                // notificationId is a unique int for each notification that you must define
                notificationManager.notify(2, builder.build());
            }
        } catch (NullPointerException e) {
            //pass
        }

        try {
            RemoteNotifications.start(MainActivity.this, new URL("https://jopaapi.web.app/coronainfo/notifications.json"), UpdatePolicy.NOW);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        CoronaData cD = new CoronaData(MainActivity.this);
        ArrayList<String> admUnitNames = new ArrayList<>(cD.getAdmUnitNames());

        AlarmHandler alarmHandler = new AlarmHandler(MainActivity.this);
        alarmHandler.cancelAlarmManager();
        alarmHandler.setAlarmManager();

        TextView textViewName = findViewById(R.id.textViewName);
        TextView textViewInfos = findViewById(R.id.textViewInfos);

        ListView listView = findViewById(R.id.listView);
        ArrayAdapter<String> listViewArrayAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, admUnitNames);
        listView.setAdapter(listViewArrayAdapter);
        listView.setOnItemClickListener((parent, view, position, id) -> {
            String admUnitId = cD.getAdmUnitId(listViewArrayAdapter.getItem(position));
            try {
                String[] data = cD.getDataForAdmUnitId(admUnitId);
                textViewName.setText(data[0]);
                textViewInfos.setText(data[1]);
            } catch (IOException e) {
                Toast.makeText(MainActivity.this, R.string.canNotLoadCData, Toast.LENGTH_SHORT).show();
            } catch (JSONException e) {
                Log.i("JSONExc", e.toString());
            }
        });

        SearchView searchView = findViewById(R.id.searchView);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if(admUnitNames.contains(query)){
                    listViewArrayAdapter.getFilter().filter(query);
                }else{
                    Toast.makeText(MainActivity.this, R.string.noRegionFound,Toast.LENGTH_LONG).show();
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String query) {
                for (String admUnitName: admUnitNames) {
                    if(admUnitName.contains(query)){
                        listViewArrayAdapter.getFilter().filter(query);
                    }
                }
                return false;
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        AlertDialog.Builder builder;
        AlertDialog dialog;
        switch (id){
            case R.id.licences:
                builder = new AlertDialog.Builder(MainActivity.this);
                builder.setMessage("OkHttp3 - Square\n" +
                        "org.json - JSON\ngson - Google\nandroid-remote-notifications - kaiwinter").setTitle(R.string.app_name);
                builder.setNegativeButton(R.string.alertDialogCancel, (dialog12, id12) -> dialog12.cancel());
                dialog = builder.create();
                dialog.show();
                return true;
            case R.id.about:
                builder = new AlertDialog.Builder(MainActivity.this);
                builder.setMessage(R.string.aboutText).setTitle(R.string.app_name);
                builder.setNegativeButton(R.string.alertDialogCancel, (dialog1, id1) -> dialog1.cancel());
                dialog = builder.create();
                dialog.show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}