package com.example.appwebteck.Helper;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelStoreOwner;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.appwebteck.BuildConfig;
import com.example.appwebteck.MainActivity;

import java.io.File;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

public class Updater {
    private static final String TAG = "Updater";
    //private static final String serverUrl = "https://appteck.mpsservice.net/releases/latest.apk";
    private static final String APK_FILE = "appWebTeck.apk";

    private UpdaterViewModel updaterViewModel;
    private Context mContext;


    public Updater(Context mContext, UpdaterViewModel updaterViewModel) {
        this.mContext = mContext;
        this.updaterViewModel = updaterViewModel;
    }

    public void update() {
        Executor executor = Executors.newSingleThreadExecutor();

        executor.execute(() -> {
            try {
                URL url = new URL(updaterViewModel.getUrl().getValue());
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.setDoInput(true);
                urlConnection.setDoOutput(true);
                urlConnection.connect();
                String parentName = "APKs";
                String extStorageDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).toString();
                File parent = new File(extStorageDirectory, "/appWebTeck/"+parentName);
                String PATH = parent.getAbsolutePath();

                java.io.File file = new java.io.File(PATH);
                if (!file.exists()){
                    file.mkdirs();
                }
                java.io.File outputFile = new java.io.File(file, APK_FILE);
                java.io.FileOutputStream fos = new java.io.FileOutputStream(outputFile);
                java.io.InputStream is = urlConnection.getInputStream();
                byte[] buffer = new byte[1024];
                int total_size = urlConnection.getContentLength();
                int downloaded = 0;
                int len1 = 0;
                int per = 0;
                while ((len1 = is.read(buffer)) != -1) {
                    fos.write(buffer, 0, len1);
                    downloaded += len1;
                    per = (int) (downloaded * 100 / total_size);
                    updaterViewModel.progress.postValue(per);
                }

                fos.close();
                is.close();

                updaterViewModel.isDownloaded.postValue(true);
                Uri uri = FileProvider.getUriForFile(mContext, BuildConfig.APPLICATION_ID + ".provider",new File(PATH+APK_FILE));
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setDataAndType(uri, "application/vnd.android.package-archive");
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                mContext.startActivity(intent);
            } catch (Exception e) {
                e.printStackTrace();
                // Handle errors appropriately
                Log.e(TAG, "update:failed ", e);
                updaterViewModel.Error.postValue(""+e.getMessage());
            }
        });

    }
}
