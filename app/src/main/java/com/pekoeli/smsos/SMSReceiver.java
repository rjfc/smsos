package com.pekoeli.smsos;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

public class SMSReceiver extends BroadcastReceiver {
    private FusedLocationProviderClient fusedLocationClient;
    private LocationRequest locationRequest;
    private String senderPhoneNumber;
    private LocationCallback locationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            for (Location location : locationResult.getLocations()) {
                String latitude = String.valueOf(location.getLatitude());
                String longitude = String.valueOf(location.getLongitude());
                Log.i("LOCATION", latitude + " | " + longitude);
                fusedLocationClient.removeLocationUpdates(locationCallback);
                SmsManager smsManager = SmsManager.getDefault();
                smsManager.sendTextMessage(senderPhoneNumber, null, "LATITUDE: " + latitude + ", LONGITUDE: " + longitude, null, null);
            }
        }
    };

    public void onReceive(Context context, Intent intent) {
        Bundle myBundle = intent.getExtras();
        SmsMessage[] messages = null;
        String strMessage = "";

        if (myBundle != null) {
            Object[] pdus = (Object[]) myBundle.get("pdus");
            messages = new SmsMessage[pdus.length];

            for (int i = 0; i < messages.length; i++) {
                //
                messages[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
                senderPhoneNumber = messages[i].getOriginatingAddress();
                if (messages[i].getMessageBody().equals("Location")) {

                    locationRequest = new LocationRequest();
                    locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
                    locationRequest.setInterval(100);
                    locationRequest.setFastestInterval(100);
                    locationRequest.setSmallestDisplacement(1);
                    //mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 10, mLocationListener);
                    if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        // TODO: Consider calling
                        //    ActivityCompat#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for ActivityCompat#requestPermissions for more details.
                        return;
                    }
                    fusedLocationClient =LocationServices.getFusedLocationProviderClient(context);
                    fusedLocationClient.requestLocationUpdates
                            (
                                    locationRequest,
                                    locationCallback,
                                    null
                            );
                    Log.i("LOCATION", "LOCATION");
                    Toast.makeText(context, "LOCATION WAS ASKED FOR", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
}