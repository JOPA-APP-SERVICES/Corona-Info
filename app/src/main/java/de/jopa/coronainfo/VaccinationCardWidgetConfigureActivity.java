package de.jopa.coronainfo;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.util.Base64;
import android.webkit.PermissionRequest;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import java.io.File;
import java.io.FileOutputStream;

import de.jopa.coronainfo.databinding.VaccinationCardWidgetConfigureBinding;

/**
 * The configuration screen for the {@link VaccinationCardWidget VaccinationCardWidget} AppWidget.
 */
public class VaccinationCardWidgetConfigureActivity extends Activity {

    int mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
    final Context context = VaccinationCardWidgetConfigureActivity.this;

    public VaccinationCardWidgetConfigureActivity() {
        super();
    }

    @SuppressLint("SetJavaScriptEnabled")
    @RequiresApi(api = Build.VERSION_CODES.R)
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        // Set the result to CANCELED.  This will cause the widget host to cancel
        // out of the widget placement if the user presses the back button.
        setResult(RESULT_CANCELED);

        de.jopa.coronainfo.databinding.VaccinationCardWidgetConfigureBinding binding = VaccinationCardWidgetConfigureBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        WebView webView = findViewById(R.id.webView);
        webView.clearCache(true);
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setDatabaseEnabled(true);

        webView.loadUrl("https://jopaapps.web.app/apps/impfkartengenerator?coronaInfo=true");
        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onPermissionRequest(final PermissionRequest request) {
                if (ContextCompat.checkSelfPermission(VaccinationCardWidgetConfigureActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(VaccinationCardWidgetConfigureActivity.this, new String[]{Manifest.permission.CAMERA}, 123);
                }
                request.grant(request.getResources());

            }
        });
        webView.setWebViewClient(new WebViewClient());

        webView.setDownloadListener((url, userAgent, contentDisposition, mimetype, contentLength) -> {
            byte[] decodedString = Base64.decode(url.replace("data:image/png;base64,", ""), Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);

            File dir = new File(context.getFilesDir().getPath() + "data/de.jopa.coronainfo/files/VaccinationCards");
            dir.mkdirs();
            File file = new File (dir, mAppWidgetId + ".png");
            if (file.exists ()) file.delete ();
            try {
                FileOutputStream out = new FileOutputStream(file);
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
                out.flush();
                out.close();
            } catch (Exception e) {
                e.printStackTrace();
            }


            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            VaccinationCardWidget.updateAppWidget(context, appWidgetManager, mAppWidgetId);

            // Make sure we pass back the original appWidgetId
            Intent resultValue = new Intent();
            resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
            setResult(RESULT_OK, resultValue);
            finish();
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