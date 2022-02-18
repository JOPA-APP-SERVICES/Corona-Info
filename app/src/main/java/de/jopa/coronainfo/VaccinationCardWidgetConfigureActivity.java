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
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import de.jopa.coronainfo.databinding.VaccinationCardWidgetConfigureBinding;

/**
 * The configuration screen for the {@link VaccinationCardWidget VaccinationCardWidget} AppWidget.
 */
public class VaccinationCardWidgetConfigureActivity extends Activity {

    private static final String PREFS_NAME = "de.jopa.coronainfo.VaccinationCardWidget";
    private static final String PREF_PREFIX_KEY = "widget_";
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

    // Write the prefix to the SharedPreferences object for this widget
    static void savePath(Context context, int appWidgetId, String path) {
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_NAME, 0).edit();
        prefs.putString(PREF_PREFIX_KEY + appWidgetId, path);
        prefs.apply();
    }

    // Read the prefix from the SharedPreferences object for this widget.
    // If there is no preference saved, get the default from a resource
    static String loadPath(Context context, int appWidgetId) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
        String path = prefs.getString(PREF_PREFIX_KEY + appWidgetId, null);
        if (path != null) {
            return path;
        } else {
            return "";
        }
    }

    static void deletePath(Context context, int appWidgetId) {
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

    static void openCoronaApp(Context context) {
        context.getApplicationContext();
        try {
            Intent covpass = new Intent("android.intent.action.MAIN");
            covpass.setComponent(new ComponentName(
                    "de.rki.covpass.app",  //This is the package name of another application
                    "de.rki.covpass.app.main.MainActivity"));
            context.startActivity(covpass);
        } catch (ActivityNotFoundException e) {
            try {
                Intent coronaWarn = new Intent("android.intent.action.MAIN");
                coronaWarn.setComponent(new ComponentName(
                        "de.rki.coronawarnapp",  //This is the package name of another application
                        "de.rki.coronawarnapp.ui.launcher.LauncherActivity"));
                context.startActivity(coronaWarn);
            } catch (ActivityNotFoundException e2) {
                try {
                    Intent luca = new Intent("android.intent.action.MAIN");
                    luca.setComponent(new ComponentName(
                            "de.culture4life.luca",  //This is the package name of another application
                            "de.culture4life.luca.ui.splash.SplashActivity"));
                    context.startActivity(luca);
                } catch (ActivityNotFoundException e3) {
                    try {
                        Intent greenPass = new Intent("android.intent.action.MAIN");
                        greenPass.setComponent(new ComponentName(
                                "eu.greenpassapp.greenpassrk",  //This is the package name of another application
                                "eu.greenpassapp.greenpassrk.MainActivity"));
                        context.startActivity(greenPass);
                    } catch (ActivityNotFoundException e4) {
                        Toast.makeText(context, R.string.noCoronaApp, Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }
    }

    private void generateVacCard() throws IOException {
        String lang = Locale.getDefault().getLanguage();

        InputStream stream = context.getAssets().open("VaccinationCards/" + lang + ".svg");
        int size = stream.available();
        byte[] buffer = new byte[size];
        stream.read(buffer);
        stream.close();
        String svg = new String(buffer);

        savePath(context, mAppWidgetId, svg);

        Intent notificationIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://jopaapps.web.app/apps/impfkartengenerator?appWidgetId=" + mAppWidgetId));

        PendingIntent contentIntent = PendingIntent.getActivity(context, 0, notificationIntent, 0);

        createNotificationChannel("Vaccinationcard setup", "");
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "1")
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("Corona Info Impfkartenwidget einrichten")
                .setContentText("Bitte klicken sie hier, um ihr Impfkartenwidget einzurichten")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(contentIntent);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);

// notificationId is a unique int for each notification that you must define
        notificationManager.notify(1, builder.build());
    }

    private void createNotificationChannel(String name, String description) {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel("vacCardSetup", name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
}