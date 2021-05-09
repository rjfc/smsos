package com.pekoeli.smsos;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ToggleButton;

public class TempActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
       // setContentView(R.layout.activity_temp);


       /* ToggleButton toggleListener = findViewById(R.id.toggle_listener_button);
        toggleListener.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                //startService(new Intent(this, SMSOSService.class));
                Intent serviceIntent = new Intent(this, SMSOSService.class);
                serviceIntent.putExtra("inputExtra", "Foreground Service Example in Android");
                ContextCompat.startForegroundService(this, serviceIntent);
            } else {
                // The toggle is disabled
                //stopService(new Intent(this, SMSOSService.class));
                Intent serviceIntent = new Intent(this, SMSOSService.class);
                stopService(serviceIntent);
            }
        });*/
    }
}

