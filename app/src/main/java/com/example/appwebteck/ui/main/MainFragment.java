package com.example.appwebteck.ui.main;

import static androidx.core.app.ActivityCompat.startActivityForResult;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.webkit.ConsoleMessage;
import android.webkit.GeolocationPermissions;
import android.webkit.PermissionRequest;
import android.webkit.ValueCallback;
import android.webkit.WebSettings;
import android.webkit.WebView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.ViewModelProvider;

import com.example.appwebteck.AppPermissions.PermissionViewModel;
import com.example.appwebteck.AppPermissions.Permissions;
import com.example.appwebteck.FileManger;
import com.example.appwebteck.Helper.UpdaterViewModel;
import com.example.appwebteck.MyLifecycleObserver;
import com.example.appwebteck.R;
import com.example.appwebteck.WebAppInterface;

public class MainFragment extends Fragment {
    private MyLifecycleObserver mObserver;
    private final String TAG = "MainFragment";
    MainViewModel mViewModel;

    PermissionViewModel pViewModel;

    UpdaterViewModel updaterViewModel;

    public static ValueCallback<Uri[]> mUploadMessage;

    public static MainFragment newInstance() {
        return new MainFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_main, container, false);
    }
    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mViewModel = new ViewModelProvider(requireActivity()).get(MainViewModel.class);
        pViewModel = new ViewModelProvider(requireActivity()).get(PermissionViewModel.class);
        updaterViewModel = new ViewModelProvider(requireActivity()).get(UpdaterViewModel.class);

        WebView webView = view.findViewById(R.id.webview);
        mObserver = new MyLifecycleObserver(requireActivity().getActivityResultRegistry(), requireActivity(), webView);
        getLifecycle().addObserver(mObserver);
        Permissions permissions = new Permissions(getActivity(), getActivity(), pViewModel);

        SharedPreferences sharedPref = getActivity().getSharedPreferences("appWebTeck", getActivity().MODE_PRIVATE);
        String workerId = sharedPref.getString("workerId", null);
        String pageUrl = sharedPref.getString("pageUrl", "http:192.168.1.100:3000");//https://webappteck.ir-service.com
        if (workerId != null) {
            if(!permissions.checkAllPermission()){
                permissions.showPopUp();
            }
        }
        mViewModel.setUrl(pageUrl);
        webSettings(webView).loadUrl(pageUrl);
        if (pageUrl != null) {
            mViewModel.setUrl(pageUrl);
        }

        pViewModel.getAsk().observe((LifecycleOwner) this, ask -> {
            if(ask){
                if(!permissions.checkAllPermission()){
                    permissions.showPopUp();
                }
            }
        });

    }


    @SuppressLint("SetJavaScriptEnabled")
    private WebView webSettings(WebView mWebView) {
        WebSettings webSettings = mWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setAllowFileAccess(true);
        webSettings.setAllowContentAccess(true);
        webSettings.setSaveFormData(true);
        webSettings.setLoadsImagesAutomatically(true);
        webSettings.setSupportZoom(true);
        webSettings.setGeolocationEnabled(true);
        webSettings.setMediaPlaybackRequiresUserGesture(false);
        webSettings.setNeedInitialFocus(true);
        webSettings.setDatabaseEnabled(true);
        webSettings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
        WebView.setWebContentsDebuggingEnabled(true);
        webSettings.setUseWideViewPort(true);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setSupportMultipleWindows(true);
        webSettings.setBuiltInZoomControls(true);
        webSettings.setDisplayZoomControls(false);
        webSettings.setAllowFileAccessFromFileURLs(true);
        webSettings.setAllowUniversalAccessFromFileURLs(true);
        webSettings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        webSettings.setUserAgentString("Mozilla/5.0 (Linux; Android " + Build.VERSION.RELEASE + "; " + Build.MODEL + " Build/" + Build.ID + ") AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/91.0.4472.124 Mobile Safari/537.36");
        webSettings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
        mWebView.setWebViewClient(new MyWebViewClient());
        mWebView.setWebChromeClient(new MyWebChromeClient(this.getActivity(),mViewModel));
        mWebView.addJavascriptInterface(new WebAppInterface(getActivity(),mObserver,mWebView,mViewModel,updaterViewModel,pViewModel),"Android");
        return mWebView;
    }



}

class MyWebViewClient extends android.webkit.WebViewClient {
    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        Log.d("MyWebViewClient", "shouldOverrideUrlLoading: " + url);
        view.loadUrl(url);
        return true;
    }

    @Override
    public void onPageFinished(WebView view, String url) {
        Log.d("MyWebViewClient", "onPageFinished: " + url);
        super.onPageFinished(view, url);
    }

    @Override
    public void onPageStarted(WebView view, String url, android.graphics.Bitmap favicon) {
        Log.d("MyWebViewClient", "onPageStarted: " + url);
        super.onPageStarted(view, url, favicon);
    }

    @Override
    public void onLoadResource(WebView view, String url) {
        Log.d("MyWebViewClient", "onLoadResource: " + url);
        super.onLoadResource(view, url);
    }

    @Override
    public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
        Log.d("MyWebViewClient", "onReceivedError: " + description);
        super.onReceivedError(view, errorCode, description, failingUrl);
    }

    @Override
    public void onReceivedHttpError(WebView view, android.webkit.WebResourceRequest request, android.webkit.WebResourceResponse errorResponse) {
        Log.d("MyWebViewClient", "onReceivedHttpError: " + errorResponse.getReasonPhrase());
        super.onReceivedHttpError(view, request, errorResponse);
    }


}

class MyWebChromeClient extends android.webkit.WebChromeClient {
    Activity activity;
    String TAG = "MyWebChromeClient";
    int FILECHOOSER_RESULTCODE = 100;


    MainViewModel mViewModel;


    public MyWebChromeClient(Activity activity,  MainViewModel mViewModel) {
        this.activity = activity;
        this.mViewModel = mViewModel;
    }

    @Override
    public boolean onJsAlert(WebView view, String url, String message, android.webkit.JsResult result) {
        Log.d("MyWebChromeClient", "onJsAlert: " + message);
        return super.onJsAlert(view, url, message, result);
    }

    @Override
    public void onGeolocationPermissionsShowPrompt(String origin, GeolocationPermissions.Callback callback) {
        callback.invoke(origin, true, false);
    }

    @Override
    public void onPermissionRequest(PermissionRequest request) {
        Log.d("onPermissionRequest", "onPermissionRequest: "+ request.getOrigin());
        request.grant(request.getResources());
    }

    @Override
    public void onPermissionRequestCanceled(PermissionRequest request) {
        super.onPermissionRequestCanceled(request);
    }



    @Override
    public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, FileChooserParams fileChooserParams) {

        mViewModel.setFilePathCallback(filePathCallback);
        if (fileChooserParams.getAcceptTypes()[0].equals("image/*")) {
            Intent takePictureIntent = TakePictureIntent();
            if (takePictureIntent.resolveActivity(webView.getContext().getPackageManager()) != null) {
                Uri uri = FileManger.createImageFile();
                mViewModel.setUri(uri);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
                startActivityForResult(activity, takePictureIntent, FILECHOOSER_RESULTCODE, null);
            }
        } else if (fileChooserParams.getAcceptTypes()[0].equals("video/*")) {
            Intent takeVideoIntent = TakeVideoIntent();
            if (takeVideoIntent.resolveActivity(webView.getContext().getPackageManager()) != null) {
                Uri uri = FileManger.createVideoFile();
                mViewModel.setUri(uri);
                takeVideoIntent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
                takeVideoIntent.putExtra(MediaStore.EXTRA_DURATION_LIMIT, 60);
                takeVideoIntent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 0);
                startActivityForResult(activity, takeVideoIntent, FILECHOOSER_RESULTCODE, null);
            }
        }  else if (fileChooserParams.getAcceptTypes()[0].equals("*/*")) {
            Intent takeFileIntent = TakeFileIntent();
            if (takeFileIntent.resolveActivity(webView.getContext().getPackageManager()) != null) {
                startActivityForResult(activity, takeFileIntent, FILECHOOSER_RESULTCODE, null);
            }
        }

        return true;
    }

    public Intent TakePictureIntent() {
        return new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
    }

    public Intent TakeVideoIntent() {
        return new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
    }


    public Intent TakeFileIntent() {
        Intent takeFileIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Files.getContentUri("external"));
        takeFileIntent.setType("*/*");
        return takeFileIntent;
    }


    @Override
    public boolean onConsoleMessage(ConsoleMessage cm) {
        Log.d("MyWebChromeClient", cm.message() + " -- From line "
                + cm.lineNumber() + " of "
                + cm.sourceId() );
        return true;
    }

    @Override
    public boolean onJsConfirm(WebView view, String url, String message, android.webkit.JsResult result) {
        Log.d("MyWebChromeClient", "onJsConfirm: " + message);
        return super.onJsConfirm(view, url, message, result);
    }

    @Override
    public boolean onJsPrompt(WebView view, String url, String message, String defaultValue, android.webkit.JsPromptResult result) {
        Log.d("MyWebChromeClient", "onJsPrompt: " + message);
        return super.onJsPrompt(view, url, message, defaultValue, result);
    }

    @Override
    public boolean onJsBeforeUnload(WebView view, String url, String message, android.webkit.JsResult result) {
        Log.d("MyWebChromeClient", "onJsBeforeUnload: " + message);
        return super.onJsBeforeUnload(view, url, message, result);
    }

    @Override
    public void onProgressChanged(WebView view, int newProgress) {
        Log.d("MyWebChromeClient", "onProgressChanged: " + newProgress);
        super.onProgressChanged(view, newProgress);
    }

    @Override
    public void onReceivedTitle(WebView view, String title) {
        Log.d("MyWebChromeClient", "onReceivedTitle: " + title);
        super.onReceivedTitle(view, title);
    }

    @Override
    public View getVideoLoadingProgressView() {
        Log.d("MyWebChromeClient", "getVideoLoadingProgressView: ");
        return super.getVideoLoadingProgressView();
    }

}
