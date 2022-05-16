package de.jopa.coronainfo;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.RemoteViews;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Implementation of App Widget functionality.
 * App Widget Configuration implemented in {@link VaccinationCardWidgetConfigureActivity VaccinationCardWidgetConfigureActivity}
 */
public class VaccinationCardWidget extends AppWidgetProvider {

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.vaccination_card_widget);

        String path = context.getFilesDir().getPath() + "data/de.jopa.coronainfo/files/VaccinationCards/" + appWidgetId + ".png";

        Bitmap bitmap = BitmapFactory.decodeFile(path);
        remoteViews.setImageViewBitmap(R.id.imageView, bitmap);
        PendingIntent openCoronaApp = openCoronaApp(context);
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
        File card = new File(context.getFilesDir().getPath() + "data/de.jopa.coronainfo/files/VaccinationCards", appWidgetIds[0] + ".png");
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

    private static PendingIntent openCoronaApp(Context context) {
        PendingIntent pendingIntent;

        ArrayList<List<String>> apps = new ArrayList<>();
        apps.add(Arrays.asList("de.rki.covpass.app", "de.rki.covpass.app.main.MainActivity"));
        apps.add(Arrays.asList("de.rki.coronawarnapp", "de.rki.coronawarnapp.ui.main.MainActivity"));
        apps.add(Arrays.asList("de.culture4life.luca", "de.culture4life.luca.ui.MainActivity"));
        apps.add(Arrays.asList("eu.greenpassapp.greenpassrk", "eu.greenpassapp.greenpassrk.MainActivity"));
        apps.add(Arrays.asList("fr.gouv.android.stopcovid", "fr.gouv.android.stopcovid.MainActivity"));

        for (List app : apps) {
            try {
                Intent intent = new Intent("android.intent.action.MAIN");
                intent.setComponent(new ComponentName(app.get(0).toString(), app.get(1).toString()));
                pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE);
                return pendingIntent;
            } catch (ActivityNotFoundException e) {
                //pass
            }
        }

        Toast.makeText(context, R.string.noCoronaApp, Toast.LENGTH_SHORT).show();

        Intent intent = new Intent("android.intent.action.MAIN");
        intent.setComponent(new ComponentName("de.jopa.coronainfo", "de.jopa.coronainfo.MainActivity"));
        pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE);
        return pendingIntent;
    }
}