package com.devansh.xcapproject;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;

import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.TimeZone;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class AppDetailsUtility extends AppCompatActivity {

    PackageManager packageManager;
    private static final String TAG = "MyActivity";
//    public static final String APP_DATA = "com.devansh.myfirstapp.MESSAGE";
//    public static final String APP_NAME_MAP = "xcap.app.name.map"; // NOT USED
//    private final int SPLASH_DISPLAY_LENGTH = 2000;
//    private final int EARLIEST_NOTIFICATION_HOUR = 10;
//    private final int LATEST_NOTIFICATION_HOUR = 20;
//    private final int MIDDLE_NOTIFICATION_HOUR = 15;
    public static final HashMap<String, String> packageNameToAppNameMap = new HashMap<>();;

    //    final String NOTIFICATION_SERVICE_URL = "https://xcap-notification-service.herokuapp.com";
//    final String NOTIFICATION_SERVICE_URL = "https://xcapteam-notification-service.herokuapp.com/";

    // Credits: https://stackoverflow.com/questions/8784505/how-do-i-check-if-an-app-is-a-non-system-app-in-android
    boolean isValidApp(ApplicationInfo applicationInfo) {
        int mask = ApplicationInfo.FLAG_SYSTEM | ApplicationInfo.FLAG_UPDATED_SYSTEM_APP;
        return (applicationInfo.flags & mask) == 0 && !applicationInfo.packageName.equals("com.devansh.xcapproject");
    }

    private ArrayList<String> getApplications() {
        ArrayList<String> result = new ArrayList<>();

        List<ApplicationInfo> applications = packageManager.getInstalledApplications(
                PackageManager.GET_META_DATA);

        for (ApplicationInfo applicationInfo : applications) {
            if (!isValidApp(applicationInfo)) {
                continue;
            }
            String packageName = applicationInfo.packageName;
            String appName = applicationInfo.loadLabel(packageManager).toString();
            result.add(applicationInfo.packageName);
            packageNameToAppNameMap.put(packageName, appName);
        }

        System.out.println("Final Map");
        System.out.println(packageNameToAppNameMap);

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

    public void removeAppsWithNoPermissions(
            HashMap<String, HashMap<Integer, ArrayList<AndroidPermissions>>> map
    ) {
        if (map == null) {
            return;
        }

        List<String> toRemoveFromMap = new ArrayList<>();

        for (String key : map.keySet()) {
            HashMap<Integer, ArrayList<AndroidPermissions>> value = map.get(key);
            boolean valid = false;

            for (int permissionId : value.keySet()) {
                ArrayList<AndroidPermissions> permissions = value.get(permissionId);
                if (permissions != null && permissions.size() != 0) {
                    valid = true;
                    break;
                }
            }


            if (!valid) {
                toRemoveFromMap.add(key);
            }
        }

        for (String toRemoveKey : toRemoveFromMap) {
            map.remove(toRemoveKey);
        }
    }

    public HashMap<String, HashMap<Integer, ArrayList<AndroidPermissions>>> getPermissionsOfAllApps() {
//        this.packageManager = MainActivity.packageManager;
//        this.packageManager = this.getPackageManager();
        HashMap<String, HashMap<Integer, ArrayList<AndroidPermissions>>> result = new HashMap<>();
//        packageNameToAppNameMap = new HashMap<>();
        ArrayList<String> apps = getApplications();
        for (String app : apps) {
            Log.d(TAG, app);
            HashMap<Integer, ArrayList<AndroidPermissions>>  map = getPermissions(app);
            result.put(app, map);
            Log.d(TAG, map.toString());
        }

        removeAppsWithNoPermissions(result);
        return result;

    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.packageManager = getPackageManager();
    }
}