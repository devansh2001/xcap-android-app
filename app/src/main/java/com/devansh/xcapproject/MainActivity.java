package com.devansh.xcapproject;

import androidx.annotation.NonNull;
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
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;

import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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

public class MainActivity extends AppCompatActivity {

    public static PackageManager packageManager;
    private static final String TAG = "MyActivity";
    public static final String APP_DATA = "com.devansh.myfirstapp.MESSAGE";
    public static final String APP_NAME_MAP = "xcap.app.name.map"; // NOT USED
    private final int SPLASH_DISPLAY_LENGTH = 2000;
    private final int EARLIEST_NOTIFICATION_HOUR = 10;
    private final int LATEST_NOTIFICATION_HOUR = 20;
    private final int MIDDLE_NOTIFICATION_HOUR = 15;
    public static final HashMap<String, String> packageNameToAppNameMap = new HashMap<>();
    public static final HashMap<String, String> appNameToPackageNameMap = new HashMap<>();
    // Whitelist added from: https://www.t-mobile.com/support/devices/android/samsung-galaxy-s8/pre-installed-apps-samsung-galaxy-s8
    public static final Set<String> whiteList = new HashSet<>(Arrays.asList(
            "amazon",
            "android pay",
            "calculator",
            "calendar",
            "clock",
            "contacts",
            "drive",
            "email",
            "facebook",
            "galaxy apps",
            "gallery",
            "gmail",
            "google chrome",
            "google maps",
            "google play books",
            "google play magazines",
            "google play movies & tv",
            "google play music",
            "google search",
            "google+",
            "hangouts",
            "instagram",
            "internet",//
            "lookout",//
            "messages",
            "my files",//
            "PEN.UP",//
            "phone",
            "photos",
            "s health",
            "s voice",
            "samsung connect",//
            "samsung gear",//
            "samsung milk music",//
            "samsung notes",//
            "samsung pay",//
            "samsung+",//
            "sidesync",//
            "secure folder",//
            "smart manager",//
            "smart remote",//
            "smart switch",//
            "whatsapp",
            "youtube"
    ));

//    final String NOTIFICATION_SERVICE_URL = "https://xcap-notification-service.herokuapp.com";
    final String NOTIFICATION_SERVICE_URL = "https://xcapteam-notification-service.herokuapp.com/";

    // Credits: https://stackoverflow.com/questions/8784505/how-do-i-check-if-an-app-is-a-non-system-app-in-android
    // Try this: https://stackoverflow.com/a/35036644
    boolean isValidApp(ApplicationInfo applicationInfo) {
        if (applicationInfo == null || applicationInfo.name == null || applicationInfo.name.length() == 0) {
            return false;
        }
        if (whiteList.contains(applicationInfo.loadLabel(packageManager).toString().toLowerCase())) {
            return true;
        }
        int mask = ApplicationInfo.FLAG_SYSTEM | ApplicationInfo.FLAG_UPDATED_SYSTEM_APP;
        return (applicationInfo.flags & mask) == 0 && !applicationInfo.packageName.equals("com.devansh.xcapproject");
    }

    // might work - experimental
    boolean isValidApp2(ApplicationInfo applicationInfo) {
        try {

            PackageInfo pi_app = packageManager.getPackageInfo(applicationInfo.packageName, PackageManager.GET_SIGNATURES);
            PackageInfo pi_sys = packageManager.getPackageInfo("android", PackageManager.GET_SIGNATURES);
            return pi_app == null
                    || pi_app.signatures == null
                    || !pi_sys.signatures[0].equals(pi_app.signatures[0]);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private ArrayList<String> getApplications() {
        ArrayList<String> result = new ArrayList<>();

        List<ApplicationInfo> applications = packageManager.getInstalledApplications(
                PackageManager.GET_META_DATA);
        System.out.println("Checking apps");

        for (ApplicationInfo applicationInfo : applications) {
            String appName = applicationInfo.loadLabel(packageManager).toString();

            if (!isValidApp(applicationInfo)) {
                continue;
            }
            System.out.println(appName);
            System.out.println("Above is not system app ****");
            String packageName = applicationInfo.packageName;
            //String appName = applicationInfo.loadLabel(packageManager).toString();
            result.add(applicationInfo.packageName);
            packageNameToAppNameMap.put(packageName, appName);
            appNameToPackageNameMap.put(appName, packageName);
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

    public String getDateTimeString(Calendar calendar) {
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1;
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
        int seconds = calendar.get(Calendar.SECOND);

        return String.format("%d-%d-%d %d:%d:%d", year, month, day, hour, minute, seconds);

    }

    public String getDateString(Calendar calendar) {
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1;
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        String dateString = String.format("%d-%d-%d", year, month, day);

        System.out.println(dateString);

        return dateString;
    }
    // https://stackoverflow.com/a/2595654
    public String getNotificationTimeInUTC(Calendar calendar) {
        Calendar deepCopy = (Calendar) calendar.clone();
        deepCopy.setTimeZone(TimeZone.getTimeZone("UTC"));
        System.out.println("Updated Notification Time");
        System.out.println(getDateTimeString(deepCopy));
        System.out.println(getDateTimeString(calendar));
        return getDateTimeString(deepCopy);
    }

    public boolean getAndSet(Calendar calendar) {

        final String keyString = "XCAP_LAST_UPDATED_DATE";
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String currentString = "";
        String dateString = getDateString(calendar);

        if (sharedPreferences.contains(keyString)) {
            currentString = sharedPreferences.getString(keyString, "");
        }

        System.out.println("GET AND SET RUN SUMMARY");
        System.out.println(dateString + " was datestring");
        System.out.println(dateString + " was currentString");

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(keyString, dateString);
        editor.apply();

        return currentString.equals(dateString);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.packageManager = getPackageManager();

        System.out.println("THIS IS SCREEN 1");

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


        boolean isSameDay = getAndSet(calendar);
        System.out.println(isSameDay + " isSameDay");

        if (isSameDay) {
            // Means that we need to set an alarm for tomorrow
            int lowerBound = EARLIEST_NOTIFICATION_HOUR;
            int upperBound = MIDDLE_NOTIFICATION_HOUR;

            int hour = (int) Math.floor(lowerBound + Math.random() * (upperBound - lowerBound));
            int minute = (int) (Math.random() * 60);

            System.out.println(hour + " " + minute);

            calendar.add(Calendar.DATE, 1);
            calendar.set(Calendar.HOUR_OF_DAY, hour);
            calendar.set(Calendar.MINUTE, minute);
            calendar.set(Calendar.SECOND, 0);
        } else {
            // Means that we need to set an alarm for today

            int lowerBound = MIDDLE_NOTIFICATION_HOUR;
            int upperBound = LATEST_NOTIFICATION_HOUR;

            int hour = (int) Math.floor(lowerBound + Math.random() * (upperBound - lowerBound));
            int minute = (int) (Math.random() * 60);

            System.out.println(hour + " " + minute);

            calendar.set(Calendar.HOUR_OF_DAY, hour);
            calendar.set(Calendar.MINUTE, minute);
            calendar.set(Calendar.SECOND, 0);

        }


//
//        if (isFirst || Calendar.HOUR_OF_DAY < LATEST_NOTIFICATION_HOUR) {
//            Log.d(TAG, "First in the day");
//
//            int lowerBound = calendar.get(Calendar.HOUR_OF_DAY) + 1;
//            int upperBound = LATEST_NOTIFICATION_HOUR - 1;
//
//            System.out.println(lowerBound + " , " + upperBound);
//
//            int hour = (int) Math.floor(lowerBound + Math.random() * (upperBound - lowerBound + 1));
//            int minute = (int) Math.random() * 60;
//
//            System.out.println(hour + " : " + minute);
//
//            calendar.set(Calendar.HOUR_OF_DAY, hour);
//            calendar.set(Calendar.MINUTE, minute);
//            Log.d(TAG, calendar.toString());
//
//            SharedPreferences.Editor editor = sharedPreferences.edit();
//            editor.putBoolean(isFirstKey, false);
//            editor.apply();
//            Log.d(TAG, "Set boolean to false");
//        } else {
//            Log.d(TAG, "NOT First in the day");
//            int lowerBound = EARLIEST_NOTIFICATION_HOUR;
//            int upperBound = MIDDLE_NOTIFICATION_HOUR;
//
//            int hour = (int) Math.floor(lowerBound + Math.random() * (upperBound - lowerBound + 1));
//            int minute = (int) Math.random() * 60;
//
//            System.out.println(hour + " : " + minute);
//
//            calendar.add(Calendar.DATE, 1);
//            calendar.set(Calendar.HOUR_OF_DAY, hour);
//            calendar.set(Calendar.MINUTE, minute);
//            Log.d(TAG, calendar.toString());
//
//            SharedPreferences.Editor editor = sharedPreferences.edit();
//            editor.putBoolean(isFirstKey, true);
//            editor.apply();
//            Log.d(TAG, "Set boolean to true");
//        }

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
        Log.d(TAG, "Set alarm " + calendar.getTime().toString());
        Toast.makeText(this, calendar.getTime().toString(), Toast.LENGTH_LONG).show();
//        alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
//        System.out.println(alarmManager.getNextAlarmClock().getTriggerTime());
//
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pendingIntent);

        final String[] deviceId = {""};

        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(new OnCompleteListener<String>() {
                    @Override
                    public void onComplete(@NonNull Task<String> task) {
                        if (!task.isSuccessful()) {
                            Log.w("FCMStuff", "Fetching FCM registration token failed", task.getException());
                            return;
                        }

                        // Get new FCM registration token
                        String token = task.getResult();

                        deviceId[0] = token;

                        // Log and toast
//                        String msg = getString(R.string.msg_token_fmt, token);
                        Log.d("FCMStuff", token);
//                        Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
                    }
                });

        final String dateString = getNotificationTimeInUTC(calendar);
        System.out.println(dateString);
        System.out.println("Date String");

        new Handler().postDelayed(new Runnable(){
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void run() {
                // API call here
                // https://trinitytuts.com/get-and-post-request-using-okhttp-in-android-application/
                try {

                    System.out.println("Making call");
                    OkHttpClient client = new OkHttpClient();

                    JSONObject dict = new JSONObject();
                    dict.put("notification_time", dateString);
                    dict.put("device_id", deviceId[0]);

                    RequestBody body = RequestBody.create(MediaType.parse("application/json"),
                            dict.toString());

                    HttpUrl localUrl = HttpUrl.parse(NOTIFICATION_SERVICE_URL + "/schedule-notification");
                    Request request = new Request.Builder()
                            .url(localUrl)
                            .post(body)
                            .header("Accept", "application/json")
                            .header("Content-Type", "application/json")
                            .header("Access-Control-Allow-Origin", "*")
                            .build();

//                    Response response = client.newCall(request).execute();
//                    System.out.println(response.body().string());

                    client.newCall(request).enqueue(new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {
                            String mMessage = e.getMessage().toString();
                            Log.w("failure Response", mMessage);
                            //call.cancel();
                        }

                        @Override
                        public void onResponse(Call call, Response response) throws IOException {

                            String mMessage = response.body().string();
                            Log.e("OKTTP", mMessage);
                        }
                    });

                } catch (Exception e) {
                    e.printStackTrace();
                }
                /* Create an Intent that will start the Menu-Activity. */
                Intent mainIntent = new Intent(MainActivity.this, UserIdCollector.class);
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