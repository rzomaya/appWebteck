package com.example.appwebteck.ui.main;

import android.net.Uri;
import android.webkit.ValueCallback;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class MainViewModel extends ViewModel {
    private  final MutableLiveData<String> url = new MutableLiveData<>();
    private  final MutableLiveData<String> location = new MutableLiveData<>();
    private  final MutableLiveData<ValueCallback<Uri[]> > filePathCallback = new MutableLiveData<>();
    private final MutableLiveData<Uri> uri = new MutableLiveData<>();

    private  final MutableLiveData<Boolean> goBack = new MutableLiveData<>();



    public MutableLiveData<String> getUrl() {
        return url;
    }
    public  void setUrl(String url) {
        this.url.setValue(url);
    }

    public  MutableLiveData<String> getLocation() {
        return location;
    }
    public  void setLocation(String location) {
        this.location.setValue(location);
    }

    //set file path callback
    public void setFilePathCallback(ValueCallback<Uri[]> filePathCallback) {
        this.filePathCallback.setValue(filePathCallback);
    }

    //get file path callback
    public MutableLiveData<ValueCallback<Uri[]> > getFilePathCallback() {
        return filePathCallback;
    }


    public MutableLiveData<Uri> getUri() {
        return uri;
    }

    public void setUri(Uri uri) {
        this.uri.setValue(uri);
    }

    public MutableLiveData<Boolean> getGoBack() {
        return goBack;
    }

    public void setGoBack(Boolean goBack) {
        this.goBack.setValue(goBack);
    }



}