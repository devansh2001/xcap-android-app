package com.devansh.xcapproject;

import android.os.Bundle;
import android.util.SparseBooleanArray;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class AppSelection extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_selection);
        Map<String, Boolean> applicationPermissions = new HashMap<>();

        applicationPermissions.put("Facebook", true);
        applicationPermissions.put("Google", true);
        applicationPermissions.put("WhatsApp", false);
        applicationPermissions.put("Chrome", true);
        applicationPermissions.put("SnapChat", true);
        applicationPermissions.put("Chrome1", true);
        applicationPermissions.put("SnapChat1", true);
        applicationPermissions.put("Chrome2", true);
        applicationPermissions.put("SnapChat2", true);
        applicationPermissions.put("Chrome3", true);
        applicationPermissions.put("SnapChat3", true);
        applicationPermissions.put("SnapChat4", true);
        applicationPermissions.put("Chrome4", true);
        applicationPermissions.put("SnapChat5", true);

        String[] apps = new String[applicationPermissions.size()];
        int i = 0;
        for (String appName : applicationPermissions.keySet()) {
            apps[i++] = appName;
        }
        Arrays.sort(apps);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_multiple_choice, apps);
        final ListView layout = (ListView) findViewById(R.id.listView);
        layout.setAdapter(adapter);
        layout.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
//        for (String appName : applicationPermissions.keySet()) {
//            CheckBox btn = new CheckBox(this);
//            if (applicationPermissions.get(appName) == null || !applicationPermissions.get(appName)) {
//                continue;
//            }
//            btn.setText(appName);
//            layout.addView(btn);
//        }

        Button preferencesButton = (Button) findViewById(R.id.preferencesButton);
        preferencesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View e) {
                SparseBooleanArray arr = layout.getCheckedItemPositions();
                System.out.println("PREFERENCES");
                System.out.println(arr.toString());
            }
        });

    }
}
