package com.example.appwebteck;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.widget.Toast;
import com.example.appwebteck.ui.main.MainViewModel;
import com.google.gson.Gson;
import java.io.IOException;
import java.sql.Blob;
import java.util.HashMap;
import java.util.Timer;


public class WebAppInterface {
    Context mContext;
    MyLifecycleObserver mObserver;
    WebView webView;
    MainViewModel mViewModel;
    private final String TAG = "WebAppInterface";

    /** Instantiate the interface and set the context */
    public WebAppInterface(Context c, MyLifecycleObserver mObserver, WebView webView, MainViewModel mViewModel) {
        mContext = c;
        this.mObserver = mObserver;
        this.webView = webView;
        this.mViewModel = mViewModel;

    }

    /** Show a toast from the web page */
    @JavascriptInterface
    public String showToast(String toast) {

        Toast.makeText(mContext, toast, Toast.LENGTH_SHORT).show();
        return "nice toast";
    }

    @JavascriptInterface
    public void getLocation() {
        com.example.appwebteck.Location location = new Location(mContext, mViewModel);
        location.getLastLocation();
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
