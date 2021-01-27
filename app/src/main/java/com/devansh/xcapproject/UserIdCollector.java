package com.devansh.xcapproject;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;


import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import static com.devansh.xcapproject.MainActivity.APP_DATA;

public class UserIdCollector extends AppCompatActivity {
    public Set<String> loadPreferences() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        Set<String> savedAllowedApps = (Set<String>) sharedPreferences.getStringSet(AppSelection.preferencesStringKey, new HashSet<String>());
        return savedAllowedApps;
    }

    public void makeText(String str) {
        Toast.makeText(this, str, Toast.LENGTH_LONG).show();
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public HashMap<String, HashMap<Integer, ArrayList<AndroidPermissions>>> filterApps(Set<String> allowedApps, HashMap<String, HashMap<Integer, ArrayList<AndroidPermissions>>> allApps) {
        HashMap<String, String> packageNameToAppNameMap = MainActivity.packageNameToAppNameMap;
        final HashMap<String, String> appNameToPackageNameMap = MainActivity.appNameToPackageNameMap;

        HashMap<String, HashMap<Integer, ArrayList<AndroidPermissions>>> filtered = new HashMap<>();
        for (String appPackage : allApps.keySet()) {
            String appName = packageNameToAppNameMap.getOrDefault(appPackage, "");
            if (allowedApps.contains(appName)) {
                filtered.put(appPackage, allApps.get(appPackage));
            }
        }

        return filtered;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_id);

        Button startStudyButton = (Button) findViewById(R.id.userIdButton);
        final EditText editText = (EditText) findViewById(R.id.editText);
        final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        if (sharedPreferences.contains("XCAP_UNIQUE_ID")) {
            String id = sharedPreferences.getString("XCAP_UNIQUE_ID", "");
            editText.setText(id);
        }
        startStudyButton.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onClick(View view) {

                String userId = editText.getText().toString();

                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("XCAP_UNIQUE_ID", userId);
                editor.apply();

                System.out.println(userId);

                Set<String> allowedApps = loadPreferences();
                if (allowedApps == null || allowedApps.size() == 0) {
                    makeText("Please allow at least one app to be selected using the 'Change Preferences' button below");
                } else {
                    HashMap<String, HashMap<Integer, ArrayList<AndroidPermissions>>> appData =
                            (HashMap<String, HashMap<Integer, ArrayList<AndroidPermissions>>>)
                                    getIntent().getSerializableExtra(MainActivity.APP_DATA);
                    appData = filterApps(allowedApps, appData);
                    Intent mainIntent = new Intent(UserIdCollector.this, AnotherActivity.class);
                    mainIntent.putExtra(APP_DATA, appData);
                    UserIdCollector.this.startActivity(mainIntent);
                    UserIdCollector.this.finish();
                }
            }
        });


        Button changeAppPreferencesButton = (Button) findViewById(R.id.changePreferencesButton);
        changeAppPreferencesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String userId = editText.getText().toString();

                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("XCAP_UNIQUE_ID", userId);
                editor.apply();

                System.out.println(userId);

                Intent mainIntent = new Intent(UserIdCollector.this, AppSelection.class);
                mainIntent.putExtra(APP_DATA, getIntent().getSerializableExtra(MainActivity.APP_DATA));
                UserIdCollector.this.startActivity(mainIntent);
                UserIdCollector.this.finish();
            }
        });
    }
}
