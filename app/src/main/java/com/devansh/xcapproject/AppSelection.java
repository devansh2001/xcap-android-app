package com.devansh.xcapproject;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.SparseBooleanArray;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class AppSelection extends AppCompatActivity {

    public final String preferencesStringKey = "XCAP_APP_PREFERENCES";

    public void savePreferences(Map<String, Boolean> appPermissions) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(preferencesStringKey, appPermissions.toString());
        editor.apply();
    }

    public void loadPreferences() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
//        if (sharedPreferences.contains(preferencesStringKey)) {
//            Map<String, Boolean> map = (Map<String, Boolean>) sharedPreferences.getString(preferencesStringKey, "");
//        }
        String savedPreferences = "";

        savedPreferences = savedPreferences.substring(1, savedPreferences.length() - 1);
        String[] split = savedPreferences.split(" ");
        Map<String, Boolean> map = new HashMap<>();
        for (String currentAppPermission : split) {
            String[] splitCurrentApp = currentAppPermission.split("=");
            String app = splitCurrentApp[0];
            Boolean permission = new Boolean(splitCurrentApp[1]);
            map.put(app, permission);

        }

    }

    public void makeText(String str) {
        Toast.makeText(this, str, Toast.LENGTH_LONG).show();
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_selection);
        HashMap<String, HashMap<Integer, ArrayList<AndroidPermissions>>> message =
                (HashMap<String, HashMap<Integer, ArrayList<AndroidPermissions>>>)
                        getIntent().getSerializableExtra(MainActivity.APP_DATA);
        System.out.println("App Selection");
        HashMap<String, String> packageNameToAppNameMap = MainActivity.packageNameToAppNameMap;
        System.out.println(message);
//        Map<String, Boolean> applicationPermissions = new HashMap<>();
//
//        applicationPermissions.put("Facebook", true);
//        applicationPermissions.put("Google", true);
//        applicationPermissions.put("WhatsApp", false);
//        applicationPermissions.put("Chrome", true);
//        applicationPermissions.put("SnapChat", true);
//        applicationPermissions.put("Chrome1", true);
//        applicationPermissions.put("SnapChat1", true);
//        applicationPermissions.put("Chrome2", true);
//        applicationPermissions.put("SnapChat2", true);
//        applicationPermissions.put("Chrome3", true);
//        applicationPermissions.put("SnapChat3", true);
//        applicationPermissions.put("SnapChat4", true);
//        applicationPermissions.put("Chrome4", true);
//        applicationPermissions.put("SnapChat5", true);
//
//        System.out.println(applicationPermissions.toString());


        String[] apps = new String[message.size()];
        int i = 0;
        for (String appPackage : message.keySet()) {
            String appName = packageNameToAppNameMap == null ? appPackage : packageNameToAppNameMap.getOrDefault(appPackage, "");
            apps[i++] = appName;
        }
        Arrays.sort(apps);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_multiple_choice, apps);
        final ListView layout = (ListView) findViewById(R.id.listView);
        layout.setAdapter(adapter);
        layout.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

        Button preferencesButton = (Button) findViewById(R.id.preferencesButton);
        preferencesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View e) {
                SparseBooleanArray arr = layout.getCheckedItemPositions();
                if (arr.size() == 0) {
                    makeText("Please select atleast one application from the above list");
                } else {
                    System.out.println("PREFERENCES");
                    System.out.println(arr.toString());
                }
            }
        });

    }
}
