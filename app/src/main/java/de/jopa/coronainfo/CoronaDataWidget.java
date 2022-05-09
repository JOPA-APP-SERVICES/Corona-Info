package de.jopa.coronainfo;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.widget.RemoteViews;

import org.json.JSONException;

import java.io.IOException;

/**
 * Implementation of App Widget functionality.
 * App Widget Configuration implemented in {@link CoronaDataWidgetConfigureActivity CoronaDataWidgetConfigureActivity}
 */
public class CoronaDataWidget extends AppWidgetProvider {

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {

        String admUnitId = CoronaDataWidgetConfigureActivity.loadAdmUnitId(context, appWidgetId);
        CoronaData cD = new CoronaData(context);
        String[] data = new String[2];
        try {
            data = cD.getDataForAdmUnitId(admUnitId);
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.corona_data_widget);
        views.setTextViewText(R.id.textViewName, data[0]);
        views.setTextViewText(R.id.textViewInfos, data[1]);

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        // When the user deletes the widget, delete the preference associated with it.
        CoronaDataWidgetConfigureActivity.deleteAdmUnitId(context, appWidgetIds[0]);
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }
}