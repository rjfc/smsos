package com.pekoeli.smsos;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.ToggleButton;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        ToggleButton toggleListener = findViewById(R.id.toggle_listener_button);
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
        });
    }
}

