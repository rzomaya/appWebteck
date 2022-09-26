package com.example.appwebteck;

import android.app.Activity;
import android.net.Uri;
import android.webkit.WebView;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.ActivityResultRegistry;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import java.io.IOException;
import java.net.URLEncoder;



public class MyLifecycleObserver implements DefaultLifecycleObserver {
    private final ActivityResultRegistry mRegistry;
    private ActivityResultLauncher<String> mGetContent;
    private ActivityResultLauncher<Uri> takePicture;
    private ActivityResultLauncher<Uri> recordVideo;
    private Uri fUri;
    private final String TAG = "MyLifecycleObserver";
    private final Activity mContext;
    WebView webView;

    public MyLifecycleObserver(@NonNull ActivityResultRegistry registry, Activity context, WebView webView) {
        mRegistry = registry;
        mContext = context;
        this.webView = webView;
    }

    public void onCreate(@NonNull LifecycleOwner owner) {


        mGetContent = mRegistry.register("key", owner, new ActivityResultContracts.GetContent(),
                uri -> {
                    try {
                        String base64 =  FileManger.toBase64(mContext ,uri);
                        final String retFunction = "imgGotFromAndroid('data:image/png;base64," + URLEncoder.encode(base64, "UTF-8") + "');";
                        webView.evaluateJavascript(retFunction,null);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });

        takePicture = mRegistry.register("keyOne", owner, new ActivityResultContracts.TakePicture(),
                result -> {
                    try {

                            String base64 =FileManger.toBase64(mContext,fUri);
                            final String retFunction = "imgGotFromAndroid('data:image/png;base64," + URLEncoder.encode(base64, "UTF-8") + "');";
                            webView.evaluateJavascript(retFunction,null);

                    } catch (IOException e) {
                        e.printStackTrace();
                    }


                });


        recordVideo = mRegistry.register("keyTwo", owner, new ActivityResultContracts.TakeVideo(),
                        result -> {
                            try {
                                String base64 =FileManger.toBase64(mContext,fUri);
                                final String retFunction = "videoGotFromAndroid('data:video/mp4;base64," + URLEncoder.encode(base64, "UTF-8") + "');";
                                webView.evaluateJavascript(retFunction,null);

                            } catch (IOException e) {
                                e.printStackTrace();
                            }
        });
    }

    public void selectImage() {
        // Open the activity to select an image
        mGetContent.launch("image/*");
    }
    public  void captureImage(Uri uri) {
        // Open the activity to capture an image
        fUri = uri;
        takePicture.launch(uri);
    }

    public  void recordVideo(Uri uri) {
        // Open the activity to record a video
        fUri = uri;
        recordVideo.launch(uri);
    }

}
