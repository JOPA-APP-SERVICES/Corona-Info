package de.jopa.coronainfo;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.os.StrictMode;

import androidx.annotation.NonNull;

import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;
import java.util.Set;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class CoronaData{
    public Context context;
    public JSONObject coronaData = new JSONObject();

    public CoronaData(Context context) {
        this.context = context;
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
    }

    public void loadDataForAdmUnitId(String admUnitId) throws IOException{
        OkHttpClient client = new OkHttpClient();

        HttpUrl.Builder urlBuilder = Objects.requireNonNull(HttpUrl.parse("https://services7.arcgis.com/mOBPykOjAyBO2ZKk/arcgis/rest/services/rki_key_data_v/FeatureServer/0/query")).newBuilder();
        urlBuilder.addQueryParameter("user-agent", "Corona-Info/JOPA/1.0");
        urlBuilder.addQueryParameter("where", "AdmUnitId=" + admUnitId);
        urlBuilder.addQueryParameter("outFields", "Inz7T, AnzFallNeu, AnzTodesfallNeu, AnzFall, AnzTodesfall");
        urlBuilder.addQueryParameter("f", "json");
        urlBuilder.addQueryParameter("cacheHint", "true");

        String url = urlBuilder.build().toString();

        Request request = new Request.Builder()
                .url(url)
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull final Response response) throws IOException {
                if (response.isSuccessful()) {
                    String resp = Objects.requireNonNull(response.body()).string();
                    JSONObject data = new JSONObject();
                    try {
                        data = new JSONObject(new JSONObject(new JSONArray(new JSONObject(resp).get("features").toString()).get(0).toString()).get("attributes").toString());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    FileOutputStream fos = null;
                    try {
                        fos = context.openFileOutput(admUnitId + ".json", MODE_PRIVATE);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                    try {
                        if (fos != null) {
                            fos.write(data.toString().getBytes(StandardCharsets.UTF_8));
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    try {
                        if (fos != null) {
                            fos.close();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }
    public void loadData() {
        coronaData = new JSONObject();
        ArrayList<String> admUnitNames = new ArrayList<>(this.getAdmUnitNames());
        for (String admUnitName: admUnitNames) {
            try {
                loadDataForAdmUnitId(getAdmUnitId(admUnitName));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public String[] getDataForAdmUnitId(String admUnitd) throws IOException, JSONException {
        FileInputStream fis = null;
        try {
            fis = context.openFileInput(admUnitd + ".json");
        } catch (FileNotFoundException e) {
            CoronaData cD = new CoronaData(context);
            cD.loadData();
        }
        InputStreamReader isr = new InputStreamReader(fis);
        BufferedReader br = new BufferedReader(isr);
        StringBuilder sb = new StringBuilder();
        String text;

        while ((text = br.readLine()) != null) {
            sb.append(text).append("\n");
        }
        JSONObject data = new JSONObject(sb.toString());

        String name = "";
        for (String admUnitName: getAdmUnitNames()) {
            if (getAdmUnitId(admUnitName).equals(admUnitd)) {
                name = admUnitName;
            }
        }

        String infos = getString(R.string.Cd_7day) + data.get("Inz7T") + "\n" + getString(R.string.Cd_NewCases) + data.get("AnzFallNeu") + "\n" + getString(R.string.Cd_NewDeaths) + data.get("AnzTodesfallNeu") + "\n" + getString(R.string.Cd_Cases) + data.get("AnzFall") + "\n" + getString(R.string.Cd_Deaths) + data.get("AnzTodesfall");
        return new String[]{name, infos};
    }
    public String getAdmUnitId(String admUnitName) {
        JSONObject data = new JSONObject();
        try {
            InputStream stream = context.getAssets().open("AdmUnitIds.json");

            int size = stream.available();
            byte[] buffer = new byte[size];
            stream.read(buffer);
            stream.close();
            data = new JSONObject(new String(buffer));
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }

        try {
            return data.get(admUnitName).toString();
        } catch (JSONException e) {
            return "0";
        }
    }
    public Set<String> getAdmUnitNames() {
        JSONObject data = new JSONObject();
        try {
            InputStream stream = context.getAssets().open("AdmUnitIds.json");

            int size = stream.available();
            byte[] buffer = new byte[size];
            stream.read(buffer);
            stream.close();
            data = new JSONObject(new String(buffer));
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
        return new Gson().fromJson(data.toString(), HashMap.class).keySet();
    }

    public String getString(int id) {
        return context.getResources().getString(id);
    }
}