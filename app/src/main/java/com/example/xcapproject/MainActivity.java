package com.example.xcapproject;

import androidx.appcompat.app.AppCompatActivity;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;

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
    private List<String> getApplications() {
        List<String> result = new ArrayList<>();

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

    private Map<Integer, List<AndroidPermissions>> getPermissions(String applicationPackageName) {
        Map<Integer, List<AndroidPermissions>> result = new HashMap<>();
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
                    List<AndroidPermissions> alreadyInMap = result.get(group);
                    alreadyInMap.add(permission);
                    result.put(group, alreadyInMap);
                } else {
                    List<AndroidPermissions> list = new ArrayList<>();
                    list.add(permission);
                    result.put(group, list);
                }
            } catch (IllegalArgumentException e) {
                continue;
            }
        }

        return result;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.packageManager = getPackageManager();

        List<String> apps = getApplications();
        for (String app : apps) {
            Log.d(TAG, app);
            Map<Integer, List<AndroidPermissions>>  map = getPermissions(app);
            Log.d(TAG, map.toString());
        }

    }
}