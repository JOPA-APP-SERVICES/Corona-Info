package de.jopa.coronainfo;

import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Toast;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import de.jopa.coronainfo.databinding.VaccinationCardWidgetConfigureBinding;

/**
 * The configuration screen for the {@link VaccinationCardWidget VaccinationCardWidget} AppWidget.
 */
public class VaccinationCardWidgetConfigureActivity extends Activity {

    int mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
    final Context context = VaccinationCardWidgetConfigureActivity.this;
    View.OnClickListener mOnClickListener = v -> {
        try {
            generateVacCard();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // It is the responsibility of the configuration activity to update the app widget
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        VaccinationCardWidget.updateAppWidget(context, appWidgetManager, mAppWidgetId);

        // Make sure we pass back the original appWidgetId
        Intent resultValue = new Intent();
        resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
        setResult(RESULT_OK, resultValue);
        finish();
    };

    public VaccinationCardWidgetConfigureActivity() {
        super();
    }

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        // Set the result to CANCELED.  This will cause the widget host to cancel
        // out of the widget placement if the user presses the back button.
        setResult(RESULT_CANCELED);

        de.jopa.coronainfo.databinding.VaccinationCardWidgetConfigureBinding binding = VaccinationCardWidgetConfigureBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.addButton.setOnClickListener(mOnClickListener);

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

    static PendingIntent openCoronaApp(Context context) {
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

    private void generateVacCard() throws IOException {
        String lang = getResources().getConfiguration().locale.getLanguage();
        File dir = new File(Environment.getExternalStorageDirectory() + "/.CoronaInfo/VaccinationCards/");
        if (!dir.exists()) {
            dir.mkdirs();
        }

        File file = new File(Environment.getExternalStorageDirectory() + "/.CoronaInfo/VaccinationCards", mAppWidgetId + ".png");
        if (!file.exists()) {
            file.createNewFile();
        }

            try{
                java.io.InputStream asset = getAssets().open("VaccinationCards/" + lang + ".png");
                java.io.FileOutputStream output = new java.io.FileOutputStream(file);
                final byte[] buffer = new byte[asset.available()];
                int size;
                while ((size = asset.read(buffer)) != -1) {
                    output.write(buffer, 0, size);
                }
                asset.close();
                output.close();
            } catch (java.io.IOException e){
                Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
            }

        Intent notificationIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://jopaapps.web.app/apps/impfkartengenerator?appWidgetId=" + mAppWidgetId));

        PendingIntent contentIntent = PendingIntent.getActivity(context, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE);

        createNotificationChannel(getString(R.string.notificationChannelTitle), "");
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "vacCardSetup")
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(getString(R.string.vaccinationCardSetupNotificationTitle))
                .setContentText(getString(R.string.vaccinationCardSetupNotificationBodyStartShort))
                .setStyle(new NotificationCompat.BigTextStyle().bigText(getString(R.string.vaccinationCardSetupNotificationBodyStart)))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(contentIntent);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);

        notificationManager.notify(1, builder.build());
    }

    private void createNotificationChannel(String name, String description) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel("cardSetup", name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
}