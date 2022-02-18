package de.jopa.coronainfo;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Picture;
import android.graphics.drawable.PictureDrawable;
import android.util.Log;
import android.widget.RemoteViews;

import com.pixplicity.sharp.Sharp;
import com.pixplicity.sharp.SharpPicture;

/**
 * Implementation of App Widget functionality.
 * App Widget Configuration implemented in {@link VaccinationCardWidgetConfigureActivity VaccinationCardWidgetConfigureActivity}
 */
public class VaccinationCardWidget extends AppWidgetProvider {

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {

        String path = VaccinationCardWidgetConfigureActivity.loadPath(context, appWidgetId);
        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.vaccination_card_widget);
        //views.setImageViewUri(R.id.imageView, Uri.parse(path));
        Picture picture = Sharp.loadString(path).getSharpPicture().getPicture();
        views.setImageViewBitmap(R.id.imageView, pictureDrawable2Bitmap(picture));

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
        for (int appWidgetId : appWidgetIds) {
            VaccinationCardWidgetConfigureActivity.deletePath(context, appWidgetId);
        }
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }

    protected PendingIntent getPendingSelfIntent(Context context) {
        Intent intent = new Intent(context, getClass());
        intent.setAction("openCoronaApp");
        return PendingIntent.getBroadcast(context, 0, intent, 0);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if ("openCoronaApp".equals(intent.getAction())) {
            // your onClick action is here
            Log.i("openCoronaApp", "Hi");
        }
    }

    private static Bitmap pictureDrawable2Bitmap(Picture picture) {
        PictureDrawable pd = new PictureDrawable(picture);
        Bitmap bitmap = Bitmap.createBitmap(pd.getIntrinsicWidth(), pd.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        canvas.drawPicture(pd.getPicture());
        return bitmap;
    }
}