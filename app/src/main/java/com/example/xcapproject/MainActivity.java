package com.example.xcapproject;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    PackageManager packageManager;
    private static final String TAG = "MyActivity";
    public static final String EXTRA_MESSAGE = "com.example.myfirstapp.MESSAGE";
    private final int SPLASH_DISPLAY_LENGTH = 2000;

    private ArrayList<String> getApplications() {
        ArrayList<String> result = new ArrayList<>();

        List<ApplicationInfo> applications = packageManager.getInstalledApplications(
                PackageManager.GET_META_DATA);

        for (ApplicationInfo applicationInfo : applications) {
            result.add(applicationInfo.packageName);
        }

        return result;
    }

    private String formatPermission(String unformattedPermission) {
        String[] split = unformattedPermission.split("\\.");
        if (split == null || split.length == 0) {
            return "";
        }
        return split[split.length - 1];
    }

    private HashMap<Integer, ArrayList<AndroidPermissions>> getPermissions(String applicationPackageName) {
        HashMap<Integer, ArrayList<AndroidPermissions>> result = new HashMap<>();
        PackageInfo packageInfo;

        try {
            packageInfo = packageManager.getPackageInfo(
                    applicationPackageName,
                    PackageManager.GET_PERMISSIONS);
            if (packageInfo == null) {
                throw new Exception("Cannot find package info for " + applicationPackageName);
            }
        } catch (PackageManager.NameNotFoundException e) {
            Log.d(TAG, "Error while getting permissions for: " + applicationPackageName);
            e.printStackTrace();
            return result;
        } catch (Exception e) {
            Log.d(TAG, e.getMessage() == null ? "" : e.getMessage());
            e.printStackTrace();
            return result;
        }


        String[] unformattedPermissions = packageInfo.requestedPermissions;
        if (unformattedPermissions == null || unformattedPermissions.length == 0) {
            return result;
        }

        for (String unformattedPermission : unformattedPermissions) {
            String formattedPermission = formatPermission(unformattedPermission);

            AndroidPermissions permission;
            try {
                permission = AndroidPermissions.valueOf(formattedPermission);
                int group = permission.getGroup();

                if (result.containsKey(group)) {
                    ArrayList<AndroidPermissions> alreadyInMap = result.get(group);
                    alreadyInMap.add(permission);
                    result.put(group, alreadyInMap);
                } else {
                    ArrayList<AndroidPermissions> list = new ArrayList<>();
                    list.add(permission);
                    result.put(group, list);
                }
            } catch (IllegalArgumentException e) {
                continue;
            }
        }

        return result;
    }

    public HashMap<String, HashMap<Integer, ArrayList<AndroidPermissions>>> getPermissionsOfAllApps() {
        HashMap<String, HashMap<Integer, ArrayList<AndroidPermissions>>> result = new HashMap<>();
        ArrayList<String> apps = getApplications();
        for (String app : apps) {
            Log.d(TAG, app);
            HashMap<Integer, ArrayList<AndroidPermissions>>  map = getPermissions(app);
            result.put(app, map);
            Log.d(TAG, map.toString());
        }
        return result;

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.packageManager = getPackageManager();

        new Handler().postDelayed(new Runnable(){
            @Override
            public void run() {
                /* Create an Intent that will start the Menu-Activity. */
                Intent mainIntent = new Intent(MainActivity.this, AnotherActivity.class);
                System.out.println(MainActivity.this.getPermissionsOfAllApps());
                mainIntent.putExtra(EXTRA_MESSAGE, MainActivity.this.getPermissionsOfAllApps());
                MainActivity.this.startActivity(mainIntent);
                MainActivity.this.finish();
            }
        }, SPLASH_DISPLAY_LENGTH);
    }

    public void send(View view) {
        Intent intent = new Intent(this, AnotherActivity.class);
        intent.putExtra(EXTRA_MESSAGE, this.getPermissionsOfAllApps());
        startActivity(intent);
    }
}