package com.devansh.xcapproject;

import android.content.Context;
import android.content.Intent;
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
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.devansh.xcapproject.MainActivity.APP_DATA;

public class AppSelection extends AppCompatActivity {

    public static final String preferencesStringKey = "XCAP_APP_PREFERENCES";

    public void savePreferences(Set<String> allowedApps) {
        System.out.println(allowedApps);
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
        final HashMap<String, HashMap<Integer, ArrayList<AndroidPermissions>>> message =
                (HashMap<String, HashMap<Integer, ArrayList<AndroidPermissions>>>)
                        getIntent().getSerializableExtra(MainActivity.APP_DATA);
//        System.out.println("App Selection");
        HashMap<String, String> packageNameToAppNameMap = MainActivity.packageNameToAppNameMap;
        final HashMap<String, String> appNameToPackageNameMap = MainActivity.appNameToPackageNameMap;
//        System.out.println(message);
        Set<String> savedApps = loadPreferences();

        final String[] apps = new String[message.size()];
        int i = 0;
        List<Integer> arrayPositions = new ArrayList<>();
        for (String appPackage : message.keySet()) {
            String appName = packageNameToAppNameMap == null ? appPackage : packageNameToAppNameMap.getOrDefault(appPackage, "");
            apps[i++] = appName;
        }
        Arrays.sort(apps);
        i = 0;
        for (String app : apps) {
            if (savedApps.contains(app)) {
                arrayPositions.add(i);
            }
            i++;
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_multiple_choice, apps);
        final ListView layout = (ListView) findViewById(R.id.listView);
        layout.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        layout.setAdapter(adapter);
        for (int pos : arrayPositions) {
            layout.setItemChecked(pos, true);
        }


        Button preferencesButton = (Button) findViewById(R.id.preferencesButton);
        preferencesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View e) {
                SparseBooleanArray arr = layout.getCheckedItemPositions();
                System.out.println("Logging apps...");
                System.out.println(arr);
                if (arr.size() == 0) {
                    makeText("Please select at least one application from the above list");
                } else {
//                    System.out.println("PREFERENCES");
                    System.out.println("Message before");
                    System.out.println(message);
                    Set<String> selectedApps = new HashSet<>();
                    for (int i = 0; i < apps.length; i++) {
                        if (arr.get(i, false)) {
                            selectedApps.add(apps[i]);
                        } else {
                            String packageName = appNameToPackageNameMap.get(apps[i]);
                            message.remove(packageName);
                        }
                    }
                    savePreferences(selectedApps);
                    System.out.println("Message after");
                    System.out.println(message);

                    Intent mainIntent = new Intent(AppSelection.this, AnotherActivity.class);
//                    System.out.println("Sending into WebPage");
//                    System.out.println(message);
                    if (message.size() == 0) {
                        makeText("Please select at least one application from the above list");
                    } else {
                        mainIntent.putExtra(APP_DATA, message);
                        AppSelection.this.startActivity(mainIntent);
                        AppSelection.this.finish();
                    }

                }
            }
        });

        Button bugReport = (Button) findViewById(R.id.bugReportAppSelector);
        bugReport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent bugReportIntent = BugReportUtility.getEmailIntent(getIntent().getStringExtra("USER_ID"));
                try {
                    Toast.makeText(AppSelection.this,
                            "Opening default email app...",
                            Toast.LENGTH_LONG).show();
                    startActivity(bugReportIntent);
                } catch (Exception e) {
                    Toast.makeText(AppSelection.this,
                            "Default email app not found!\nPlease email dponda3@gatech.edu with subject: [XCAP BUG] (your participant ID)",
                            Toast.LENGTH_LONG).show();
                }
            }
        });

    }
}
