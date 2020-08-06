package com.example.xcapproject;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

public class AnotherActivity extends AppCompatActivity {

    public String TAG = "AnotherAct";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_another);

        Intent intent = getIntent();
        HashMap<String, HashMap<Integer, ArrayList<AndroidPermissions>>> message =
                (HashMap<String, HashMap<Integer, ArrayList<AndroidPermissions>>>)
                intent.getSerializableExtra(MainActivity.EXTRA_MESSAGE);
        Log.d(TAG, message.toString());

//        LinearLayout layout = (LinearLayout) findViewById(R.id.linearLayout);
//        for (int i = 0; i < 10; i++) {
//            CheckBox btn = new CheckBox(this);
//            btn.setText("Work!");
//            layout.addView(btn);
//        }

        WebView view = (WebView) findViewById(R.id.webView);
        view.setWebChromeClient(new WebChromeClient());
        view.setWebViewClient(new WebViewClient());
        view.getSettings().setJavaScriptEnabled(true);
        view.addJavascriptInterface(new WebAppInterface(this, message.keySet()), "Android");
        view.loadUrl("http://localhost:3000");



    }
}

class WebAppInterface {
    Context mContext;
    Set<String> set;
    public String TAG = "AnotherAct";
    /** Instantiate the interface and set the context */
    WebAppInterface(Context c, Set<String> set) {
        mContext = c;
        this.set = set;
        Log.d(TAG, "GOT CONSTRUCTOR DATA");
        Log.d(TAG, set.toString());
    }

    /** Show a toast from the web page */
    @JavascriptInterface
    public void showToast(String toast) {
        Toast.makeText(mContext, toast, Toast.LENGTH_SHORT).show();
    }

    @JavascriptInterface
    public String showKeySet() {
        return set.toString();
    }

}