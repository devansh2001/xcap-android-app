package com.example.xcapproject;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
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

import org.json.JSONException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    PackageManager packageManager;
    private static final String TAG = "MyActivity";
    public static final String APP_DATA = "com.example.myfirstapp.MESSAGE";
    public static final String APP_NAME_MAP = "xcap.app.name.map"; // NOT USED
    private final int SPLASH_DISPLAY_LENGTH = 2000;
    private final int EARLIEST_NOTIFICATION_HOUR = 10;
    private final int LATEST_NOTIFICATION_HOUR = 20;
    private final int MIDDLE_NOTIFICATION_HOUR = 15;
    public static final HashMap<String, String> packageNameToAppNameMap = new HashMap<>();;

    private ArrayList<String> getApplications() {
        ArrayList<String> result = new ArrayList<>();

        List<ApplicationInfo> applications = packageManager.getInstalledApplications(
                PackageManager.GET_META_DATA);

        for (ApplicationInfo applicationInfo : applications) {
            String packageName = applicationInfo.packageName;
            String appName = applicationInfo.loadLabel(packageManager).toString();
            result.add(applicationInfo.packageName);
            packageNameToAppNameMap.put(packageName, appName);
        }

        System.out.println("Final Map");
        System.out.println(packageNameToAppNameMap);

        return result;
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel1 = new NotificationChannel(
                    "channel1",
                    "Channel 1",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel1.setDescription("This is channel 1");
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel1);
        }

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
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.packageManager = getPackageManager();

        createNotificationChannel();
//
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
//
        final String isFirstKey = "XCAP_IS_FIRST_IN_DAY";
        boolean isFirst = false;
        if (sharedPreferences.contains(isFirstKey)) {
            Log.d(TAG, "Found String");
            isFirst = sharedPreferences.getBoolean(isFirstKey, false);
            Log.d(TAG, sharedPreferences.getBoolean(isFirstKey, false) + "");
//            SharedPreferences.Editor editor = sharedPreferences.edit();
//            editor.remove(isFirstKey);
//            editor.apply();
        } else {
            Log.d(TAG, "Absent String");
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean(isFirstKey, true);
            editor.apply();
            isFirst = true;
            Log.d(TAG, "Added Boolean : " + true);
        }
//
        Calendar calendar = Calendar.getInstance();
        System.out.println(Calendar.YEAR);
        System.out.println(calendar.getTimeInMillis() + " " + calendar.toString());
//
        if (isFirst) {
            Log.d(TAG, "First in the day");

            int lowerBound = calendar.get(Calendar.HOUR_OF_DAY) + 1;
            int upperBound = LATEST_NOTIFICATION_HOUR - 1;

            System.out.println(lowerBound + " , " + upperBound);

            int hour = (int) Math.floor(lowerBound + Math.random() * (upperBound - lowerBound + 1));
            int minute = (int) Math.random() * 60;

            System.out.println(hour + " : " + minute);

            calendar.set(Calendar.HOUR_OF_DAY, hour);
            calendar.set(Calendar.MINUTE, minute);
            Log.d(TAG, calendar.toString());

            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean(isFirstKey, false);
            editor.apply();
            Log.d(TAG, "Set boolean to false");
        } else {
            Log.d(TAG, "NOT First in the day");
            int lowerBound = EARLIEST_NOTIFICATION_HOUR;
            int upperBound = MIDDLE_NOTIFICATION_HOUR;

            int hour = (int) Math.floor(lowerBound + Math.random() * (upperBound - lowerBound + 1));
            int minute = (int) Math.random() * 60;

            System.out.println(hour + " : " + minute);

            calendar.add(Calendar.DATE, 1);
            calendar.set(Calendar.HOUR_OF_DAY, hour);
            calendar.set(Calendar.MINUTE, minute);
            Log.d(TAG, calendar.toString());

            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean(isFirstKey, true);
            editor.apply();
            Log.d(TAG, "Set boolean to true");
        }

//        Log.d(TAG, calendar.getTimeInMillis() + "");
//        calendar.set(Calendar.HOUR_OF_DAY, 9);
//        calendar.set(Calendar.MINUTE, 39);
//        calendar.set(Calendar.SECOND, 0);
//        Log.d(TAG, calendar.getTimeInMillis() + "");
        Intent intent = new Intent(getApplicationContext(), NotificationReceiver.class);
////        intent.setAction("my_notif");
////
        PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 100, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
//
////        long time = System.currentTimeMillis();
////        long ten = 10 * 1000;
        Log.d(TAG, "Setting alarm for " + calendar.getTimeInMillis() + " - " + calendar.toString());
//        alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
//        System.out.println(alarmManager.getNextAlarmClock().getTriggerTime());
//
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pendingIntent);

        new Handler().postDelayed(new Runnable(){
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void run() {
                /* Create an Intent that will start the Menu-Activity. */
                Intent mainIntent = new Intent(MainActivity.this, AnotherActivity.class);
                System.out.println(MainActivity.this.getPermissionsOfAllApps());
                mainIntent.putExtra(APP_DATA, MainActivity.this.getPermissionsOfAllApps());
//                mainIntent.putExtra(APP_NAME_MAP, packageNameToAppNameMap);
                MainActivity.this.startActivity(mainIntent);
                MainActivity.this.finish();
            }
        }, SPLASH_DISPLAY_LENGTH);
    }

    // NOT USED
    public void send(View view) {
        Intent intent = new Intent(this, AnotherActivity.class);
        intent.putExtra(APP_DATA, this.getPermissionsOfAllApps());
        startActivity(intent);
    }
}