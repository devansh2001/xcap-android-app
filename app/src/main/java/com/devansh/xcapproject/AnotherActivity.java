package com.devansh.xcapproject;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.webkit.ConsoleMessage;
import android.webkit.JavascriptInterface;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import org.json.JSONException;
import org.json.JSONObject;

public class AnotherActivity extends AppCompatActivity {

    public String TAG = "AnotherAct";
//    public final String URL = "https://xcap-react-app-prd.herokuapp.com";
    public final String URL = "https://xcapteam-react-app-prd.herokuapp.com/";
//    public final String URL = "http://www.google.com";
    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_another);

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
            } catch (JSONException e) {
                Toast.makeText(this, "Error getting app name", Toast.LENGTH_LONG).show();
                e.printStackTrace();
            }
        }

        try {
            if (sharedPreferences.contains("XCAP_UNIQUE_ID")) {
//                Log.d(TAG, "Found String");
//                Log.d(TAG, sharedPreferences.getString("XCAP_UNIQUE_ID", ""));
                json.put("PARTICIPANT_ID", sharedPreferences.getString("XCAP_UNIQUE_ID", ""));
            } else {
//                Log.d(TAG, "Absent String");
                String uuid = UUID.randomUUID().toString();
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("XCAP_UNIQUE_ID", uuid);
                editor.apply();
                json.put("PARTICIPANT_ID", uuid);
//                Log.d(TAG, "Added String : " + uuid);
            }
        } catch (JSONException e) {
            Toast.makeText(this, "Error getting XCAP_UNIQUE_ID", Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }

        WebView view = (WebView) findViewById(R.id.webView);
        final ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressBar2);
        view.setVisibility(View.GONE);
        view.setWebChromeClient(new WebChromeClient() {
            public boolean onConsoleMessage(ConsoleMessage cm) {
//                Log.d("MyApplication", cm.message() + " -- From line "
//                        + cm.lineNumber() + " of "
//                        + cm.sourceId() );
                return true;
            }

            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                progressBar.setProgress(newProgress);

//                Log.d("MyApplication", newProgress + " is progress");

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
                // System.out.println(url);
                if (url != null && url.contains("mailto")) {
                    Intent bugReportIntent = BugReportUtility.getEmailIntent(getIntent().getStringExtra("USER_ID"));
                    try {
                        Toast.makeText(AnotherActivity.this,
                                "Opening default email app...",
                                Toast.LENGTH_LONG).show();
                        startActivity(bugReportIntent);
                    } catch (Exception e) {
                        Toast.makeText(AnotherActivity.this,
                                "Default email app not found!\nPlease email dponda3@gatech.edu with subject: [XCAP BUG] (your participant ID)",
                                Toast.LENGTH_LONG).show();
                    }
                } else {
                    view.loadUrl(url);
                }
                return true;
            }

            @Override
            public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
//              super.onReceivedSslError(view, handler, error);

//                Log.e(TAG, "onReceivedSslError...");
//                Log.e(TAG, "Error: " + error);
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

//        Log.d(TAG, "Passing JSON");
//        Log.d(TAG, json.toString());
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
//        Log.d(TAG, "GOT CONSTRUCTOR DATA");
//        Log.d(TAG, json.toString());
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