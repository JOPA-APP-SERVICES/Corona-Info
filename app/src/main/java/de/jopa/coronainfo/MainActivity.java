package de.jopa.coronainfo;

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

import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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
                        "org.json - JSON\ngson - Google").setTitle(R.string.app_name);
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