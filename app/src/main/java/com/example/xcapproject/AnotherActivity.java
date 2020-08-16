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
import java.util.List;
import java.util.Set;

import org.json.JSONException;
import org.json.JSONObject;

public class AnotherActivity extends AppCompatActivity {

    public String TAG = "AnotherAct";
    public final String URL = "https://xcap-react-app-prd.herokuapp.com";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_another);

        Intent intent = getIntent();
        HashMap<String, HashMap<Integer, ArrayList<AndroidPermissions>>> message =
                (HashMap<String, HashMap<Integer, ArrayList<AndroidPermissions>>>)
                intent.getSerializableExtra(MainActivity.EXTRA_MESSAGE);
        JSONObject json = new JSONObject();
        for (String app : message.keySet()) {
            ArrayList<AndroidPermissions> compiledList = new ArrayList<>();
            for (ArrayList<AndroidPermissions> list : message.get(app).values()) {
                compiledList.addAll(list);
            }
            try {
                json.put(app, compiledList);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        try {
            Log.d(TAG, json.toString(4));
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
        view.setWebChromeClient(new WebChromeClient());
        view.setWebViewClient(new WebViewClient());
        view.getSettings().setJavaScriptEnabled(true);
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