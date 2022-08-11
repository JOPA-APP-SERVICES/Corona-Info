package de.jopa.coronainfo;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
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
import com.mikepenz.aboutlibraries.LibsBuilder;
import org.json.JSONException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {
    @RequiresApi(api = Build.VERSION_CODES.R)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SharedPreferences prefs = this.getSharedPreferences("de.jopa.coronainfo.CoronaData", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        if (prefs.getBoolean("FirstTime", true)) {
            editor.putBoolean("FirstTime", false).apply();
            Intent intent = new Intent(this, Intro.class);
            startActivity(intent);
        }

        CoronaData cD = new CoronaData(MainActivity.this);
        ArrayList<String> admUnitNames = new ArrayList<>(cD.getAdmUnitNames());

        //AlarmHandler alarmHandler = new AlarmHandler(MainActivity.this);
        //alarmHandler.cancelAlarmManager();
        //alarmHandler.setAlarmManager();

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
                Toast.makeText(this, e.toString(), Toast.LENGTH_SHORT).show();
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
        if (id == R.id.about) {
            builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle(R.string.app_name).setMessage(R.string.aboutText);
            builder.setNegativeButton(R.string.alertDialogCancel, null);
            dialog = builder.create();
            dialog.show();
            return true;
        } else if (id == R.id.opensource) {
            builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle(R.string.app_name);
            builder.setMessage(Html.fromHtml(getString(R.string.openSourceText)));
            builder.setNegativeButton(R.string.alertDialogCancel, null);
            dialog = builder.create();
            dialog.show();

            ((TextView) Objects.requireNonNull(dialog.findViewById(android.R.id.message))).setMovementMethod(LinkMovementMethod.getInstance());

            return true;
        } else if (id == R.id.licences) {
            new LibsBuilder()
                    .withLicenseShown(true)
                    .withVersionShown(true)
                    .withAboutIconShown(true)
                    .withActivityTitle(getString(R.string.licences))
                    .start(MainActivity.this);
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }
}