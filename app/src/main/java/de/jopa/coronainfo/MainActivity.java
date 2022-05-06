package de.jopa.coronainfo;

import static android.Manifest.permission.MANAGE_EXTERNAL_STORAGE;
import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import android.app.NotificationManager;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import com.github.kaiwinter.androidremotenotifications.RemoteNotifications;
import com.github.kaiwinter.androidremotenotifications.model.UpdatePolicy;
import org.json.JSONException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.FileChannel;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    @RequiresApi(api = Build.VERSION_CODES.R)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        requestPermissions(new String[]{WRITE_EXTERNAL_STORAGE,READ_EXTERNAL_STORAGE, MANAGE_EXTERNAL_STORAGE}, 1);

        try {
            if ("/coronainfo/addVaccinationCard".equals(getIntent().getData().getPath())) {
                int appWidgetId = Integer.parseInt(getIntent().getData().getQueryParameter("appWidgetId"));
                String name = getIntent().getData().getQueryParameter("name");

                File dir = new File(Environment.getExternalStorageDirectory() + "/.CoronaInfo/VaccinationCards/");
                if (!dir.exists()) {
                    dir.mkdirs();
                }

                File src = new File(Environment.getExternalStorageDirectory() + "/Download", name + ".png");
                File dst = new File("storage/emulated/0/.CoronaInfo/VaccinationCards", appWidgetId + ".png");

                FileInputStream inStream;
                try {
                    inStream = new FileInputStream(src);
                    FileOutputStream outStream = new FileOutputStream(dst);
                    FileChannel inChannel = inStream.getChannel();
                    FileChannel outChannel = outStream.getChannel();
                    inChannel.transferTo(0, inChannel.size(), outChannel);
                    inStream.close();
                    outStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                src.delete();

                AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
                VaccinationCardWidget.updateAppWidget(this, appWidgetManager, appWidgetId);

                NotificationManager notificationManager1 = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                notificationManager1.cancel(1);

                NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "vacCardSetup")
                        .setSmallIcon(R.drawable.ic_launcher_foreground)
                        .setContentTitle(getString(R.string.vaccinationCardSetupNotificationTitle))
                        .setContentText(getString(R.string.vaccinationCardSetupNotificationBodySuccessShort))
                        .setStyle(new NotificationCompat.BigTextStyle().bigText(getString(R.string.vaccinationCardSetupNotificationBodySuccess)))
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT);

                NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);

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
        cD.loadData();
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
        if (id == R.id.licences) {
            builder = new AlertDialog.Builder(MainActivity.this);
            builder.setMessage("OkHttp3 - Square\n" +
                    "org.json - JSON\ngson - Google\nandroid-remote-notifications - kaiwinter").setTitle(R.string.app_name);
            builder.setNegativeButton(R.string.alertDialogCancel, (dialog12, id12) -> dialog12.cancel());
            dialog = builder.create();
            dialog.show();
            return true;
        } else if (id == R.id.about) {
            builder = new AlertDialog.Builder(MainActivity.this);
            builder.setMessage(R.string.aboutText).setTitle(R.string.app_name);
            builder.setNegativeButton(R.string.alertDialogCancel, (dialog1, id1) -> dialog1.cancel());
            dialog = builder.create();
            dialog.show();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }
}