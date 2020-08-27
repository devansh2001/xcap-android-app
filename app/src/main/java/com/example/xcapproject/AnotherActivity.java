package com.example.xcapproject;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.PrecomputedText;
import android.util.Log;
import android.webkit.ConsoleMessage;
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
import java.util.UUID;

import org.json.JSONException;
import org.json.JSONObject;

public class AnotherActivity extends AppCompatActivity {

    public String TAG = "AnotherAct";
    public final String URL = "https://xcap-react-app-stg.herokuapp.com";
//    public final String URL = "http://localhost:3000";
    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_another);

        Intent intent = getIntent();
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        HashMap<String, HashMap<Integer, ArrayList<AndroidPermissions>>> message =
                (HashMap<String, HashMap<Integer, ArrayList<AndroidPermissions>>>)
                intent.getSerializableExtra(MainActivity.EXTRA_MESSAGE);
        JSONObject json = new JSONObject();
        int count = 0;
        for (String app : message.keySet()) {
            if (count > 4) {
                continue;
            }
            count++;
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
        view.setWebChromeClient(new WebChromeClient() {
            public boolean onConsoleMessage(ConsoleMessage cm) {
                Log.d("MyApplication", cm.message() + " -- From line "
                        + cm.lineNumber() + " of "
                        + cm.sourceId() );
                return true;
            }
        });
        view.setWebViewClient(new WebViewClient());
        view.getSettings().setJavaScriptEnabled(true);
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