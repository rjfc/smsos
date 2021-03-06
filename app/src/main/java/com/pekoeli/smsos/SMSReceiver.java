package com.pekoeli.smsos;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.preference.PreferenceManager;
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
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class SMSReceiver extends BroadcastReceiver {
    private FusedLocationProviderClient fusedLocationClient;
    private FusedLocationProviderClient fusedLocationClientStream;
    private LocationRequest locationRequest;
    private LocationRequest locationRequestStream;
    private String senderPhoneNumber;
    private String PHONE_CONTACT_LIST = "PHONE_CONTACT_LIST";
    private String NAME_SETTING = "UNNAMED";
    private List<String> trackingPhoneNumbers = new ArrayList<>();
    private LocationCallback locationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
        for (Location location : locationResult.getLocations()) {
            String latitude = String.valueOf(location.getLatitude());
            String longitude = String.valueOf(location.getLongitude());
            fusedLocationClient.removeLocationUpdates(locationCallback);
            SmsManager smsManager = SmsManager.getDefault();
            String textMessage = NAME_SETTING + "'s Location";
            textMessage += "\n----------------------------";
            textMessage += "\nGoogle Maps Link: " + "http://maps.google.com/maps?f=q&q=" + latitude + "," + longitude;
            textMessage += "\nTime: " + Calendar.getInstance().getTime();
            smsManager.sendTextMessage(senderPhoneNumber, null, textMessage, null, null);
        }
        }
    };


    private LocationCallback locationCallbackStream = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            for (Location location : locationResult.getLocations()) {
                String latitude = String.valueOf(location.getLatitude());
                String longitude = String.valueOf(location.getLongitude());
                 SmsManager smsManager = SmsManager.getDefault();
                String textMessage = NAME_SETTING + "'s Location";
                textMessage += "\n----------------------------";
                textMessage += "\nGoogle Maps Link: " + "http://maps.google.com/maps?f=q&q=" + latitude + "," + longitude;
                textMessage += "\nTime: " + Calendar.getInstance().getTime();
                for (int i = 0; i < trackingPhoneNumbers.size(); i++)
                {
                    smsManager.sendTextMessage(trackingPhoneNumbers.get(i), null, textMessage, null, null);
                }
            }
        }
    };

    public void onReceive(Context context, Intent intent) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        Bundle myBundle = intent.getExtras();
        SmsMessage[] messages = null;
        String commandPrefix = prefs.getString("COMMAND_PREFIX", "location");
        boolean needsToBeContact = prefs.getBoolean("NEEDS_TO_BE_CONTACT", true);
        NAME_SETTING = prefs.getString("NAME", "UNNAMED");
        if (myBundle != null) {
            Object[] pdus = (Object[]) myBundle.get("pdus");
            messages = new SmsMessage[pdus.length];

            for (int i = 0; i < messages.length; i++) {
                //
                messages[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
                senderPhoneNumber = messages[i].getOriginatingAddress();
             List<PhoneContact> defaultPhoneContacts;
                boolean isInWhitelist = true;
                if (prefs.contains(PHONE_CONTACT_LIST) && !prefs.getString(PHONE_CONTACT_LIST, "").equals("[]") && needsToBeContact)
                {
                    isInWhitelist = false;
                    defaultPhoneContacts = new Gson().fromJson(prefs.getString(PHONE_CONTACT_LIST, ""), new TypeToken<ArrayList<PhoneContact>>(){}.getType());
                    for (int j = 0; j < defaultPhoneContacts.size(); j++) {
                        if (senderPhoneNumber.replaceAll("^+1", "").replaceAll("[^\\p{Digit}]", "").equals(defaultPhoneContacts.get(j).getPhone().replaceAll("^+1", "").replaceAll("[^\\p{Digit}]", "")))
                        {
                            isInWhitelist = true;
                        }
                    }

                }
                if (isInWhitelist && messages[i].getMessageBody().trim().toLowerCase().equals(commandPrefix)) {

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
                    fusedLocationClient = LocationServices.getFusedLocationProviderClient(context);
                    fusedLocationClient.requestLocationUpdates
                            (
                                    locationRequest,
                                    locationCallback,
                                    null
                            );
                }
                else if (isInWhitelist && messages[i].getMessageBody().trim().toLowerCase().equals(commandPrefix + " track"))
                {
                    String trimmedPhoneNumber = senderPhoneNumber.replaceAll("[^\\p{Digit}]", "");
                    if (trackingPhoneNumbers.contains(trimmedPhoneNumber))
                    {
                        trackingPhoneNumbers.remove(trimmedPhoneNumber);
                    }
                    else
                    {
                        trackingPhoneNumbers.add(trimmedPhoneNumber);
                    }
                    if (trackingPhoneNumbers.size() == 1)
                    {
                        locationRequestStream = new LocationRequest();
                        // TODO: settings:
                        // TODO: for stream interval
                        // TODO: disable whitelist
                        // TODO: name
                        // TODO: command prefix for location
                        locationRequestStream.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
                        locationRequestStream.setInterval(prefs.getInt("TRACKING_INTERVAL", 30) * 1000);
                        locationRequestStream.setFastestInterval(prefs.getInt("TRACKING_INTERVAL", 30) * 1000);
                        locationRequestStream.setSmallestDisplacement(1);
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
                        fusedLocationClientStream = LocationServices.getFusedLocationProviderClient(context);
                        fusedLocationClientStream.requestLocationUpdates
                                (
                                        locationRequestStream,
                                        locationCallbackStream,
                                        null
                                );
                    }
                    else if (trackingPhoneNumbers.size() == 0)
                    {
                       fusedLocationClientStream.removeLocationUpdates(locationCallbackStream);

                    }
                }
            }
        }
    }

    public void SetTrackingPhoneNumbers(List<String> newList)
    {
        trackingPhoneNumbers = newList;
    }
}