package com.example.appwebteck;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

import com.example.appwebteck.LocalDB.AppDatabase;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.BufferedWriter;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Sync {

    private static final String TAG = "SyncClass";
    private static final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final static int CONNECTION_TIMEOUT = 15000;
    private static Context mContext;

    public Sync(Context mContext) {
        this.mContext = mContext;
    }


    public static void sendJsonData(String json, long id) {
        executorService.execute(() -> {
            try {

                SharedPreferences sharedPref = mContext.getSharedPreferences("appWebTeck",Context.MODE_PRIVATE);
                String serverUrl = sharedPref.getString("portUrl","https://appteck.mpsservice.net/webApp_V1.0/MPSMobilePort.php");

                URL url = new URL(serverUrl);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

                urlConnection.setRequestMethod("POST");
                urlConnection.setRequestProperty("Charset", "UTF-8");
                urlConnection.setDoInput(true);
                urlConnection.setDoOutput(true);
                urlConnection.setConnectTimeout(CONNECTION_TIMEOUT);

                OutputStream os = urlConnection.getOutputStream();
                BufferedWriter writer = new BufferedWriter(new java.io.OutputStreamWriter(os, "UTF-8"));
                writer.write(json);
                writer.flush();
                writer.close();
                os.close();
                urlConnection.connect();
                int responseCode = urlConnection.getResponseCode();

                InputStream inputStream = urlConnection.getInputStream();
                byte[] buffer = new byte[1024];
                int actuallyRead;
                StringBuilder stringBuilder = new StringBuilder();
                while ((actuallyRead = inputStream.read(buffer)) != -1) {
                    stringBuilder.append(new String(buffer, 0, actuallyRead));
                }
                String response = stringBuilder.toString();
                Log.d(TAG, "myResponse: "+response);
                AppDatabase db = AppDatabase.getINSTANCE(mContext);

                //TODO: check if result code is 0 in case of backend error
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    db.locationDao().updateSyncedLocation((int) id);
                    db.locationDao().deleteLocation((int) id);
                    Log.d(TAG, "sendJsonData: success");
                  //  FileManger.logThis("Location sent" + response);
                } else {
                    Log.e(TAG, "sendJsonData: failed");
                }

            } catch (Exception e) {
                Log.e(TAG, "Error in background task: " + e.getMessage());

            }
        });
    }
}

