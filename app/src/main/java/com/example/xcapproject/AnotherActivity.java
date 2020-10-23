package com.example.xcapproject;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.PrecomputedText;
import android.util.Log;
import android.view.View;
import android.webkit.ConsoleMessage;
import android.webkit.JavascriptInterface;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.json.JSONException;
import org.json.JSONObject;

public class AnotherActivity extends AppCompatActivity {

    public String TAG = "AnotherAct";
    public final String URL = "https://xcap-react-app-prd.herokuapp.com";
//    public final String URL = "http://www.google.com";
    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_another);
        System.out.println("THIS IS SCREEN 2");
        Intent intent = getIntent();
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        HashMap<String, HashMap<Integer, ArrayList<AndroidPermissions>>> message =
                (HashMap<String, HashMap<Integer, ArrayList<AndroidPermissions>>>)
                intent.getSerializableExtra(MainActivity.APP_DATA);

        HashMap<String, String> packageNameToAppNameMap = MainActivity.packageNameToAppNameMap;
        JSONObject json = new JSONObject();
        for (String app : message.keySet()) {
            ArrayList<AndroidPermissions> compiledList = new ArrayList<>();
            for (ArrayList<AndroidPermissions> list : message.get(app).values()) {
                compiledList.addAll(list);
            }
            try {
                String appName = packageNameToAppNameMap == null ? app : packageNameToAppNameMap.getOrDefault(app, "");
                json.put(appName, compiledList);
                System.out.println(app);
                System.out.println(compiledList);
                System.out.println("*****");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        try {
            Log.d(TAG, json.toString(4));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        try {
            if (sharedPreferences.contains("XCAP_UNIQUE_ID")) {
                Log.d(TAG, "Found String");
                Log.d(TAG, sharedPreferences.getString("XCAP_UNIQUE_ID", ""));
                json.put("PARTICIPANT_ID", sharedPreferences.getString("XCAP_UNIQUE_ID", ""));
            } else {
                Log.d(TAG, "Absent String");
                String uuid = UUID.randomUUID().toString();
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("XCAP_UNIQUE_ID", uuid);
                editor.apply();
                json.put("PARTICIPANT_ID", uuid);
                Log.d(TAG, "Added String : " + uuid);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

//        try {
//            Log.d(TAG, new JSONObject(message).toString(4));
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }

//        LinearLayout layout = (LinearLayout) findViewById(R.id.linearLayout);
//        for (int i = 0; i < 10; i++) {
//            CheckBox btn = new CheckBox(this);
//            btn.setText("Work!");
//            layout.addView(btn);
//        }

        WebView view = (WebView) findViewById(R.id.webView);
        final ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressBar2);
        view.setVisibility(View.GONE);
        view.setWebChromeClient(new WebChromeClient() {
            public boolean onConsoleMessage(ConsoleMessage cm) {
                Log.d("MyApplication", cm.message() + " -- From line "
                        + cm.lineNumber() + " of "
                        + cm.sourceId() );
                return true;
            }

            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                progressBar.setProgress(newProgress);

                Log.d("MyApplication", newProgress + " is progress");

                super.onProgressChanged(view, newProgress);

                if (newProgress >= 90) {
                    view.setVisibility(View.VISIBLE);
                    progressBar.setVisibility(View.INVISIBLE);
                }
            }
        });
        view.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url)
            {
                //do whatever you want with url
                System.out.println(url);
                System.out.println("THIS IS MY URL HAHA");
                view.loadUrl(url);
                return true;
            }

            @Override
            public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
//              super.onReceivedSslError(view, handler, error);

                Log.e(TAG, "onReceivedSslError...");
                Log.e(TAG, "Error: " + error);
                handler.proceed();
            }
        });

        view.getSettings().setJavaScriptEnabled(true);
        view.getSettings().setDomStorageEnabled(true);
        view.getSettings().setLoadsImagesAutomatically(true);
        view.getSettings().setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        view.getSettings().setDatabaseEnabled(true);
        view.getSettings().setAppCacheEnabled(true);
        view.getSettings().setMinimumFontSize(1);
        view.getSettings().setMinimumLogicalFontSize(1);
        view.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);

        Log.d(TAG, "Passing JSON");
        Log.d(TAG, json.toString());
        view.addJavascriptInterface(new WebAppInterface(this, json), "Android");
        view.loadUrl(this.URL);



    }
}

class WebAppInterface {
    Context mContext;
    JSONObject json;
    public String TAG = "AnotherAct";
    /** Instantiate the interface and set the context */
    WebAppInterface(Context c, JSONObject json) {
        mContext = c;
        this.json = json;
        Log.d(TAG, "GOT CONSTRUCTOR DATA");
        Log.d(TAG, json.toString());
    }

    /** Show a toast from the web page */
    @JavascriptInterface
    public void showToast(String toast) {
        Toast.makeText(mContext, toast, Toast.LENGTH_SHORT).show();
    }

    @JavascriptInterface
    public String showKeySet() {
        return json.toString();
    }

}