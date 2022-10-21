package com.example.appwebteck.ui.main;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.ViewModelProvider;

import com.example.appwebteck.Location;
import com.example.appwebteck.MyLifecycleObserver;
import com.example.appwebteck.R;
import com.example.appwebteck.WebAppInterface;

public class MainFragment extends Fragment {
    private MyLifecycleObserver mObserver;
    private final String TAG = "MainFragment";
    MainViewModel mViewModel;
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
         mViewModel = new ViewModelProvider(this).get(MainViewModel.class);
        WebView webView = view.findViewById(R.id.webview);
        mObserver = new MyLifecycleObserver(requireActivity().getActivityResultRegistry(), requireActivity(), webView);
        getLifecycle().addObserver(mObserver);
        mViewModel.setUrl("http://192.168.178.62:3000");

        webSettings(webView).loadUrl(mViewModel.getUrl().getValue());
        Location location = new Location(requireActivity(),mViewModel);
        location.startLocationUpdates();

        mViewModel.getLocation().observe((LifecycleOwner) requireActivity(), location1 -> {
            webView.loadUrl("javascript:locationGotFromAndroid('" + location1 + "')");
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
        webSettings.setAppCacheEnabled(true);
        WebView.setWebContentsDebuggingEnabled(true);
        webSettings.setCacheMode(WebSettings.LOAD_NO_CACHE);
        mWebView.addJavascriptInterface(new WebAppInterface(getActivity(),mObserver,mWebView,mViewModel),"Android");
        return mWebView;
    }


}
