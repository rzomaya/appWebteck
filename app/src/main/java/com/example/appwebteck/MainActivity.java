package com.example.appwebteck;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.room.Room;
import androidx.work.OneTimeWorkRequest;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import android.Manifest;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.provider.Settings;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.webkit.ValueCallback;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.appwebteck.AppPermissions.PermissionViewModel;
import com.example.appwebteck.AppPermissions.Permissions;
import com.example.appwebteck.Helper.BackgroundSyncServes;
import com.example.appwebteck.Helper.Updater;
import com.example.appwebteck.Helper.UpdaterViewModel;
import com.example.appwebteck.LocalDB.AppDatabase;
import com.example.appwebteck.Location.LocationService;
import com.example.appwebteck.ui.main.MainFragment;
import com.example.appwebteck.ui.main.MainViewModel;
import com.google.android.material.progressindicator.LinearProgressIndicator;

public class MainActivity extends AppCompatActivity {
    String TAG = "MainActivity";
    private MainViewModel mViewModel;
    private PermissionViewModel pViewModel;

    private UpdaterViewModel updaterViewModel;
    Intent serviceIntent;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mViewModel = new ViewModelProvider(this).get(MainViewModel.class);
        pViewModel = new ViewModelProvider(this).get(PermissionViewModel.class);
        updaterViewModel = new ViewModelProvider(this).get(UpdaterViewModel.class);

        Room.databaseBuilder(getApplicationContext(),
                AppDatabase.class, "appWebteckDatabase").build();

        PeriodicWorkRequest  periodicWorkRequest = new PeriodicWorkRequest.Builder(BackgroundSyncServes.class, 15, java.util.concurrent.TimeUnit.MINUTES).build();
        WorkManager.getInstance(this).enqueue(periodicWorkRequest);

        updaterViewModel.getUpdate().observe(this, aBoolean -> {
            if(aBoolean){
                updaterViewModel.setUpdate(false);
                final Dialog dialog = new Dialog(this);
                dialog.setContentView(R.layout.update_dialog);
                dialog.setCancelable(false);
                dialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
                dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
                TextView tvMessage = dialog.findViewById(R.id.tv_message);
                LinearProgressIndicator progressBar = dialog.findViewById(R.id.progressBar);
                Button btnCancel = dialog.findViewById(R.id.btn_cancel);

                if(Boolean.TRUE.equals(updaterViewModel.getForceUpdate().getValue())){
                        btnCancel.setVisibility(View.GONE);
                }

                dialog.show();
                dialog.findViewById(R.id.btn_update).setOnClickListener(v -> {

                     tvMessage.setText(R.string.updating);
                    new Updater(this,updaterViewModel).update();

                    updaterViewModel.getProgress().observe(this, integer -> {
                        tvMessage.setText("Downloading "+integer+"%");
                        progressBar.setVisibility(View.VISIBLE);
                        progressBar.setProgress(integer);
                        if (integer < 0) {
                            progressBar.animate().alpha(0).setDuration(1000);
                        }

                    });


                });

                updaterViewModel.getIsDownloaded().observe(this, aBoolean1 -> {
                    if(aBoolean1){
                       dialog.dismiss();
                    }
                });

                updaterViewModel.getError().observe(this, s -> {
                    tvMessage.setText(s);
                    progressBar.setVisibility(View.GONE);
                    dialog.findViewById(R.id.btn_update).setVisibility(View.VISIBLE);
                });

                dialog.findViewById(R.id.btn_cancel).setOnClickListener(v -> {
                    dialog.dismiss();
                });

            }
        });


         serviceIntent = new Intent(this, LocationService.class);
           if(LocationService.shouldTrack() && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
               this.startService(serviceIntent);
           }

        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
        builder.detectFileUriExposure();

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, MainFragment.newInstance())
                    .commitNow();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case Permissions.PERMISSIONS_REQUEST_CODE_LOCATION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED  ) {
                    pViewModel.setLocationPermission(true);
                    this.startService(serviceIntent);
                } else {
                    pViewModel.setLocationPermission(false);
                    Toast.makeText(this, "Location denied", Toast.LENGTH_SHORT).show();
                }
                return;

                case Permissions.PERMISSIONS_REQUEST_CODE_CAMERA:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    pViewModel.setCameraPermission(true);
                } else {
                    pViewModel.setCameraPermission(false);
                    Toast.makeText(this, "Camera denied", Toast.LENGTH_SHORT).show();
                }
                return;

                case Permissions.PERMISSIONS_REQUEST_CODE_STORAGE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    pViewModel.setStoragePermission(true);
                } else {
                    pViewModel.setStoragePermission(false);
                    Toast.makeText(this, "Storage denied", Toast.LENGTH_SHORT).show();
                }
                return;

              default:
                  Toast.makeText(this, "requestCode = "+requestCode, Toast.LENGTH_SHORT).show();
        }

    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        ValueCallback<Uri[]> filePathCallback = mViewModel.getFilePathCallback().getValue();

        if (requestCode == 100) {
            if (resultCode == RESULT_OK) {
                // Check if file path callback is null
                if (filePathCallback != null) {
                    // Check if the data contains the image
                    if (data != null && data.getData() != null) {
                        // Get the URI of the selected file
                        Uri result = data.getData();
                        filePathCallback.onReceiveValue(new Uri[]{result});
                        Log.i(TAG, "onActivityResult: " + "data.getData() ");
                    } else if (data != null && data.getExtras() != null) {
                        // Handle the image from extras if needed
                        Bundle extras = data.getExtras();
                        Bitmap imageBitmap = (Bitmap) extras.get("data");
                        Log.d(TAG, "onActivityResult: look -> " + mViewModel.getUri().getValue());
                        Uri uri = FileManger.saveBitmapToFile(this, imageBitmap);
                        filePathCallback.onReceiveValue(new Uri[]{uri});

                    } else {

                        filePathCallback.onReceiveValue(new Uri[]{mViewModel.getUri().getValue()});
                    }
                    mViewModel.setFilePathCallback(null);
                } else {
                    Log.i(TAG, "onActivityResult: " + "filePathCallback is null");
                }
            } else {
                // Check if file path callback is null
                if (filePathCallback != null) {
                    filePathCallback.onReceiveValue(null);
                    Log.i(TAG, "onActivityResult: "+"filePathCallback is not null and result is not ok");
                } else {
                    Log.i(TAG, "onActivityResult: " + "null result");
                }
            }
        }
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // Check whether the key event is the Back button and if there's history.
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            mViewModel.setGoBack(true);
            return true;
        }
        // If it isn't the Back button or there's no web page history, bubble up to
        // the default system behavior. Probably exit the activity.
        return super.onKeyDown(keyCode, event);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(LocationService.shouldTrack()){
            this.stopService(serviceIntent);
        }
    }

}