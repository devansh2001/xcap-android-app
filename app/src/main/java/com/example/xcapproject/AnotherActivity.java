package com.example.xcapproject;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.HashMap;

public class AnotherActivity extends AppCompatActivity {

    public String TAG = "AnotherAct";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_another);

        Intent intent = getIntent();
        HashMap<String, HashMap<Integer, ArrayList<AndroidPermissions>>> message =
                (HashMap<String, HashMap<Integer, ArrayList<AndroidPermissions>>>)
                intent.getSerializableExtra(MainActivity.EXTRA_MESSAGE);
        Log.d(TAG, message.toString());

        LinearLayout layout = (LinearLayout) findViewById(R.id.linearLayout);
        for (int i = 0; i < 10; i++) {
            CheckBox btn = new CheckBox(this);
            btn.setText("Work!");
            layout.addView(btn);
        }



    }
}