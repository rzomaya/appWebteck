package com.example.appwebteck;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.text.BoringLayout;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.widget.Toast;

import com.example.appwebteck.AppPermissions.PermissionViewModel;
import com.example.appwebteck.Helper.UpdaterViewModel;
import com.example.appwebteck.ui.main.MainViewModel;
import com.google.gson.Gson;
import java.io.IOException;
import java.util.HashMap;


public class WebAppInterface {
    Context mContext;
    MyLifecycleObserver mObserver;
    WebView webView;
    MainViewModel mViewModel;

    UpdaterViewModel updaterViewModel;


    PermissionViewModel pViewModel;

    private final String TAG = "WebAppInterface";

    /** Instantiate the interface and set the context */
    public WebAppInterface(Context c, MyLifecycleObserver mObserver, WebView webView, MainViewModel mViewModel, UpdaterViewModel updaterViewModel , PermissionViewModel pViewModel) {
        mContext = c;
        this.mObserver = mObserver;
        this.webView = webView;
        this.mViewModel = mViewModel;
        this.updaterViewModel = updaterViewModel ;
        this.pViewModel = pViewModel;
    }

    /** Show a toast from the web page */
    @JavascriptInterface
    public String showToast(String toast) {
        Toast.makeText(mContext, toast, Toast.LENGTH_LONG).show();
        return "nice toast";
    }
    @JavascriptInterface
    public void setConfig(String workerId,String sessionID,String portUrl, int webAppVersion , boolean DeactivateGps , String language,String pageUrl) {
        SharedPreferences sharedPref = mContext.getSharedPreferences("appWebTeck",Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        if(workerId == null || sessionID == null || portUrl == null || language == null ){
            Log.d(TAG, "setConfig: null");
            return;
        }
        editor.putString("workerId", workerId);
        editor.putString("sessionId", sessionID);
        editor.putString("portUrl", portUrl);
        editor.putInt("webAppVersion", webAppVersion);
        editor.putBoolean("DeactivateGps", DeactivateGps);
        editor.putString("language", language);
        editor.putString("pageUrl", pageUrl);
        editor.apply();
        pViewModel.Ask.postValue(true);
        Log.d(TAG, "setConfig: "+workerId+" "+sessionID+" "+portUrl+" "+webAppVersion+" "+DeactivateGps+" "+language);
    }

    @JavascriptInterface
    public void getLocation() {

    }

    @JavascriptInterface
    public void updateApp(int appVersion, String updateUrl , boolean forceUpdate) {
        //  get the current app version manifest
        int currentVersion =  BuildConfig.VERSION_CODE;
        if (currentVersion < appVersion && updateUrl != null && !updateUrl.isEmpty()) {
            updaterViewModel.url.postValue(updateUrl);
            updaterViewModel.forceUpdate.postValue(forceUpdate);
            updaterViewModel.update.postValue(true);
        }
    }


    @JavascriptInterface
    public void captureImage(String dirName, String imageName) {

        Uri uri = FileManger.createDir(mContext, dirName, imageName);
        mObserver.captureImage(uri);
    }
    @JavascriptInterface
    public void recordVideo(String dirName, String videoName) {
        Uri uri = FileManger.createDir(mContext, dirName, videoName);
        mObserver.recordVideo(uri);
    }
    @JavascriptInterface
    public void selectImage() {
        mObserver.selectImage();
    }
    @JavascriptInterface
    public String loadOrdersPhotos(String dirName,String orderID) {
        HashMap<String,String> photos = FileManger.loadOrdersPhotos(mContext, dirName, orderID);
        return new Gson().toJson(photos);
    }

    @JavascriptInterface
    public String load(String dirName,String fileName) {
        String data = null;
        try {
            data = FileManger.load(mContext, dirName, fileName);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return data;
    }
    @JavascriptInterface
    public void createFile(String dirName,String fileName,String content){
        try {
            FileManger.createFile(mContext,dirName,fileName,content);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @JavascriptInterface
    public void saveFiles(String fileName , String base64){
        try {
            FileManger.saveFiles(mContext,fileName,base64);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
