package com.devansh.xcapproject;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;


import androidx.appcompat.app.AppCompatActivity;

import static com.devansh.xcapproject.MainActivity.APP_DATA;

public class UserIdCollector extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_id);

        Button button = (Button) findViewById(R.id.userIdButton);
        final EditText editText = (EditText) findViewById(R.id.editText);
        final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        if (sharedPreferences.contains("XCAP_UNIQUE_ID")) {
            String id = sharedPreferences.getString("XCAP_UNIQUE_ID", "");
            editText.setText(id);
        }
        button.setOnClickListener(new View.OnClickListener() {
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
