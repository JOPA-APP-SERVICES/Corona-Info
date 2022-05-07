package de.jopa.coronainfo;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.widget.RemoteViews;
import java.io.File;

/**
 * Implementation of App Widget functionality.
 * App Widget Configuration implemented in {@link VaccinationCardWidgetConfigureActivity VaccinationCardWidgetConfigureActivity}
 */
public class VaccinationCardWidget extends AppWidgetProvider {

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.vaccination_card_widget);

        String path = Environment.getExternalStorageDirectory() + "/.CoronaInfo/VaccinationCards/" + appWidgetId + ".png";

        Bitmap bitmap = BitmapFactory.decodeFile(path);
        remoteViews.setImageViewBitmap(R.id.imageView, bitmap);
        PendingIntent openCoronaApp = VaccinationCardWidgetConfigureActivity.openCoronaApp(context);
        if (openCoronaApp != null) {
            remoteViews.setOnClickPendingIntent(R.id.imageView, openCoronaApp);
        }

        appWidgetManager.updateAppWidget(appWidgetId, remoteViews);
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
        File card = new File("storage/emulated/0/.CoronaInfo/VaccinationCards", appWidgetIds[0] + ".png");
        card.delete();
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