package com.example.appwebteck.ui.main;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class MainViewModel extends ViewModel {
    private  final MutableLiveData<String> url = new MutableLiveData<>();
    private  final MutableLiveData<String> location = new MutableLiveData<>();

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

}