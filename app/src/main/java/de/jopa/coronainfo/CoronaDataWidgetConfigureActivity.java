package de.jopa.coronainfo;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Toast;

import java.util.ArrayList;

import de.jopa.coronainfo.databinding.CoronaDataWidgetConfigureBinding;

/**
 * The configuration screen for the {@link CoronaDataWidget CoronaDataWidget} AppWidget.
 */
public class CoronaDataWidgetConfigureActivity extends Activity {

    private static final String PREFS_NAME = "de.jopa.coronainfo.CoronaDataWidget";
    private static final String PREF_PREFIX_KEY = "widget_";
    int mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;

    public CoronaDataWidgetConfigureActivity() {
        super();
    }

    // Write the prefix to the SharedPreferences object for this widget
    static void saveAdmUnitId(Context context, int appWidgetId, String admUnitId) {
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_NAME, 0).edit();
        prefs.putString(PREF_PREFIX_KEY + appWidgetId, admUnitId);
        prefs.apply();
    }

    // Read the prefix from the SharedPreferences object for this widget.
    // If there is no preference saved, get the default from a resource
    static String loadAdmUnitId(Context context, int appWidgetId) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
        String admUnitId = prefs.getString(PREF_PREFIX_KEY + appWidgetId, null);
        if (admUnitId != null) {
            return admUnitId;
        } else {
            return "0";
        }
    }

    static void deleteAdmUnitId(Context context, int appWidgetId) {
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_NAME, 0).edit();
        prefs.remove(PREF_PREFIX_KEY + appWidgetId);
        prefs.apply();
    }

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        // Set the result to CANCELED.  This will cause the widget host to cancel
        // out of the widget placement if the user presses the back button.
        setResult(RESULT_CANCELED);

        de.jopa.coronainfo.databinding.CoronaDataWidgetConfigureBinding binding = CoronaDataWidgetConfigureBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Context context = CoronaDataWidgetConfigureActivity.this;

        CoronaData cD = new CoronaData(context);
        ArrayList<String> admUnitNames = new ArrayList<>(cD.getAdmUnitNames());

        ListView listView = findViewById(R.id.listView);
        ArrayAdapter<String> listViewArrayAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, admUnitNames);
        listView.setAdapter(listViewArrayAdapter);
        listView.setOnItemClickListener((parent, view, position, id) -> {
            String admUnitId = cD.getAdmUnitId(listViewArrayAdapter.getItem(position));
            saveAdmUnitId(context, mAppWidgetId, admUnitId);
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            CoronaDataWidget.updateAppWidget(context, appWidgetManager, mAppWidgetId);

            // Make sure we pass back the original appWidgetId
            Intent resultValue = new Intent();
            resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
            setResult(RESULT_OK, resultValue);
            finish();
        });

        SearchView searchView = findViewById(R.id.searchView);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if(admUnitNames.contains(query)){
                    listViewArrayAdapter.getFilter().filter(query);
                }else{
                    Toast.makeText(context, R.string.noRegionFound,Toast.LENGTH_LONG).show();
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

        // Find the widget id from the intent.
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            mAppWidgetId = extras.getInt(
                    AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
        }

        // If this activity was started with an intent without an app widget ID, finish with an error.
        if (mAppWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish();
        }
    }
}