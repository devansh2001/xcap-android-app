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
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class AppSelection extends AppCompatActivity {

    public final String preferencesStringKey = "XCAP_APP_PREFERENCES";

    public void savePreferences(Set<String> allowedApps) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putStringSet(preferencesStringKey, allowedApps);
        editor.apply();
        editor.commit();
    }

    public Set<String> loadPreferences() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        Set<String> savedAllowedApps = (Set<String>) sharedPreferences.getStringSet(preferencesStringKey, new HashSet<String>());
        return savedAllowedApps;
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
        loadPreferences();
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


        final String[] apps = new String[message.size()];
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
                    makeText("Please select at least one application from the above list");
                } else {
                    System.out.println("PREFERENCES");
                    Set<String> selectedApps = new HashSet<>();
                    for (int i = 0; i < apps.length; i++) {
                        if (arr.get(i, false)) {
                            selectedApps.add(apps[i]);
                        }
                    }
                    savePreferences(selectedApps);
                }
            }
        });

    }
}
