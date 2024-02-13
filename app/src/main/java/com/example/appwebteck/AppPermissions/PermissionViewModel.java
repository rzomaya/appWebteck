package com.example.appwebteck.AppPermissions;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class PermissionViewModel extends ViewModel {


    public final MutableLiveData<Boolean> Ask = new MutableLiveData<>();

    private final MutableLiveData<Boolean> locationPermission = new MutableLiveData<>();
    private final MutableLiveData<Boolean> cameraPermission = new MutableLiveData<>();
    private final MutableLiveData<Boolean> storagePermission = new MutableLiveData<>();



    public MutableLiveData<Boolean> getLocationPermission() {
        return locationPermission;
    }

    public void setLocationPermission(Boolean locationPermission) {
        this.locationPermission.setValue(locationPermission);
    }

    public MutableLiveData<Boolean> getCameraPermission() {
        return cameraPermission;
    }

    public void setCameraPermission(Boolean cameraPermission) {
        this.cameraPermission.setValue(cameraPermission);
    }

    public MutableLiveData<Boolean> getStoragePermission() {
        return storagePermission;
    }

    public void setStoragePermission(Boolean storagePermission) {
        this.storagePermission.setValue(storagePermission);
    }

    public MutableLiveData<Boolean> getAsk() {
        return Ask;
    }

}
