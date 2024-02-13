package com.example.appwebteck.AppPermissions;

import static androidx.core.app.ActivityCompat.requestPermissions;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;

import com.example.appwebteck.R;
import com.example.appwebteck.ui.main.MainViewModel;
import com.google.android.material.switchmaterial.SwitchMaterial;

import java.util.zip.Inflater;

public class Permissions {
    private static final String TAG = "Permissions";
    public static final int PERMISSIONS_REQUEST_CODE_LOCATION = 110;
    public static final int PERMISSIONS_REQUEST_CODE_CAMERA = 111;
    public static final int PERMISSIONS_REQUEST_CODE_STORAGE = 112;


    Context context;
    Activity activity;
    PermissionViewModel pViewModel;
    public Permissions(Context context, Activity activity, PermissionViewModel pViewModel) {
        this.context = context;
        this.activity = activity;
        this.pViewModel = pViewModel;
    }

    public void showPopUp() {

        SharedPreferences sharedPref = context.getSharedPreferences("appWebTeck", Context.MODE_PRIVATE);
        boolean deactivateGps = sharedPref.getBoolean("DeactivateGps", false);


        Dialog popupView = new Dialog(context);
        popupView.setContentView(R.layout.popup_permissions);
        popupView.setCancelable(false);
        popupView.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
        popupView.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        popupView.show();


        SwitchMaterial switchLocation = popupView.findViewById(R.id.switchLocation);
        SwitchMaterial switchCamera = popupView.findViewById(R.id.switchCamera);
        SwitchMaterial switchStorage = popupView.findViewById(R.id.switchStorage);
        TextView error = popupView.findViewById(R.id.error);

        Button ok = popupView.findViewById(R.id.ok);

        pViewModel.setLocationPermission(checkLocationPermission());
        pViewModel.setCameraPermission(checkCameraPermission());
        pViewModel.setStoragePermission(checkStoragePermission());


        pViewModel.getLocationPermission().observe((LifecycleOwner) activity, switchLocation::setChecked);
        pViewModel.getCameraPermission().observe((LifecycleOwner) activity, switchCamera::setChecked);
        pViewModel.getStoragePermission().observe((LifecycleOwner) activity, switchStorage::setChecked);


        switchLocation.setOnClickListener(v -> {
            if (switchLocation.isChecked()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    requestAppPermission(new String[]{
                            Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.ACCESS_FINE_LOCATION,
                    }, PERMISSIONS_REQUEST_CODE_LOCATION);
                }
            } else {
                removeAppPermission();
            }
        });

        switchCamera.setOnClickListener(v -> {
            if (switchCamera.isChecked()) {
                requestAppPermission(new String[]{
                        Manifest.permission.CAMERA,
                        Manifest.permission.RECORD_AUDIO,
                }, PERMISSIONS_REQUEST_CODE_CAMERA);
            } else {
                removeAppPermission();
            }
        });

        switchStorage.setOnClickListener(v -> {
            if (switchStorage.isChecked()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    requestAppPermission(new String[]{
                            Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.READ_EXTERNAL_STORAGE,
                    }, PERMISSIONS_REQUEST_CODE_STORAGE);
                }
            } else {
                removeAppPermission();
            }
        });

        ok.setOnClickListener(v -> {
            if (checkCameraPermission() && checkStoragePermission()) {
                if(deactivateGps){
                   popupView.dismiss();
                }else if (checkLocationPermission() ){
                    popupView.dismiss();
                }else{
                    error.setVisibility(View.VISIBLE);
                }
            }else{
                error.setVisibility(View.VISIBLE);
            }

        });

    }

    private boolean checkCameraPermission( ) {
        return ContextCompat.checkSelfPermission(this.context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
    }
    private boolean checkLocationPermission() {
        return ContextCompat.checkSelfPermission(this.context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private boolean checkStoragePermission() {
        return ContextCompat.checkSelfPermission(this.context, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }



    private void requestAppPermission(String[] permissions, int requestCode) {
        if (ContextCompat.checkSelfPermission(this.context, permissions[0]) == PackageManager.PERMISSION_GRANTED) {
            return;
        }
        requestPermissions(activity,
                permissions,
                requestCode);
    }




    public void removeAppPermission() {
       //open settings to remove permission
        Intent intent = new Intent();
        intent.setAction(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.setData(android.net.Uri.parse("package:" + context.getPackageName()));
        context.startActivity(intent);
    }


    public boolean checkAllPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                return ContextCompat.checkSelfPermission(this.context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                        ContextCompat.checkSelfPermission(this.context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                        ContextCompat.checkSelfPermission(this.context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED &&
                        ContextCompat.checkSelfPermission(this.context, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                        ContextCompat.checkSelfPermission(this.context, Manifest.permission.WAKE_LOCK) == PackageManager.PERMISSION_GRANTED &&
                        ContextCompat.checkSelfPermission(this.context, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
            }
        }
        return false;
    }

}
