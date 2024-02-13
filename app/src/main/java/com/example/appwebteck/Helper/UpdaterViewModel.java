package com.example.appwebteck.Helper;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class UpdaterViewModel extends ViewModel {

    public final MutableLiveData<String> url = new MutableLiveData<>();
    public  final MutableLiveData<Integer> progress = new MutableLiveData<>();

    public  final MutableLiveData<Boolean> update = new MutableLiveData<>();
    public final MutableLiveData<Boolean> isDownloaded = new MutableLiveData<>();

    public final MutableLiveData<Boolean> forceUpdate = new MutableLiveData<>();


    public final MutableLiveData<String> Error = new MutableLiveData<>();
    public MutableLiveData<Integer> getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress.setValue(progress);
    }


    public MutableLiveData<Boolean> getUpdate() {
        if (update.getValue() == null) {
            update.setValue(false);
        }
        return update;
    }

    public void setUpdate(Boolean update) {
        this.update.setValue(update);
    }

    public MutableLiveData<Boolean> getIsDownloaded() {
        if (isDownloaded.getValue() == null) {
            isDownloaded.setValue(false);
        }
        return isDownloaded;
    }
    public void setIsDownloaded(Boolean isDownloading) {
        this.isDownloaded.setValue(isDownloading);
    }


    public MutableLiveData<String> getError() {
        return Error;
    }

    public void setError(String error) {
        this.Error.setValue(error);
    }


    public MutableLiveData<String> getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url.setValue(url);
    }

    public void setForceUpdate(Boolean forceUpdate) {
        this.forceUpdate.setValue(forceUpdate);
    }

    public MutableLiveData<Boolean> getForceUpdate() {
        if (forceUpdate.getValue() == null) {
            forceUpdate.setValue(false);
        }
        return forceUpdate;
    }




}
