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
            fusedLocationClient.removeLocationUpdates(locationCallback);
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(senderPhoneNumber, null, generateText(location, NAME_SETTING), null, null);
        }
        }
    };

    private LocationCallback locationCallbackStream = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            for (Location location : locationResult.getLocations()) {
                SmsManager smsManager = SmsManager.getDefault();
                for (int i = 0; i < trackingPhoneNumbers.size(); i++)
                {
                    smsManager.sendTextMessage(trackingPhoneNumbers.get(i), null, generateText(location, NAME_SETTING), null, null);
                }
            }
        }
    };

    private String generateText (Location location, String name)
    {
        String latitude = String.valueOf(location.getLatitude());
        String longitude = String.valueOf(location.getLongitude());
        String textMessage = name + "'s Location";
        textMessage += "\n----------------------------";
        textMessage += "\nGoogle Maps Link: " + "http://maps.google.com/maps?f=q&q=" + latitude + "," + longitude;
        textMessage += "\nTime: " + Calendar.getInstance().getTime();
        return textMessage;
    }

    public void sendMessage() {

    }

    public void SetTrackingPhoneNumbers(List<String> newList)
    {
        trackingPhoneNumbers = newList;
    }

    public void onReceive(Context context, Intent intent) {
        Bundle myBundle = intent.getExtras();

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        NAME_SETTING = prefs.getString("NAME", "UNNAMED");
        String commandPrefix = prefs.getString("COMMAND_PREFIX", "location");
        boolean needsToBeContact = prefs.getBoolean("NEEDS_TO_BE_CONTACT", true);

        SmsMessage[] messages = null;

        if (myBundle != null) {
            Object[] pdus = (Object[]) myBundle.get("pdus");
            messages = new SmsMessage[pdus.length];

            for (int i = 0; i < messages.length; i++) {
                messages[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
                senderPhoneNumber = messages[i].getOriginatingAddress();
                List<PhoneContact> phoneContacts;

                // senderPermitted will evaluate to true if (sender does not need to be a contact) OR (sender needs to be a contact AND sender is in contacts list) OR (sender needs to be a contact AND contact list is empty)
                boolean senderPermitted = true;
                if (prefs.contains(PHONE_CONTACT_LIST) && !prefs.getString(PHONE_CONTACT_LIST, "").equals("[]") && needsToBeContact)
                {
                    senderPermitted = false;
                    phoneContacts = new Gson().fromJson(prefs.getString(PHONE_CONTACT_LIST, ""), new TypeToken<ArrayList<PhoneContact>>(){}.getType());
                    for (int j = 0; j < phoneContacts.size(); j++) {
                        if (senderPhoneNumber.replaceAll("^+1", "").replaceAll("[^\\p{Digit}]", "").equals(phoneContacts.get(j).getPhone().replaceAll("^+1", "").replaceAll("[^\\p{Digit}]", "")))
                        {
                            senderPermitted = true;
                        }
                    }

                }

                if (senderPermitted && messages[i].getMessageBody().trim().toLowerCase().equals(commandPrefix)) {
                    // If just the command prefix is sent, then send a single text message of location
                    locationRequest = new LocationRequest();
                    locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
                    locationRequest.setInterval(0);
                    locationRequest.setFastestInterval(0);
                    locationRequest.setSmallestDisplacement(1);

                    if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
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
                else if (senderPermitted && messages[i].getMessageBody().trim().toLowerCase().equals(commandPrefix + " track"))
                {
                    // If command prefix + " track" is sent, then send a stream of location text messages that correspond to the user-set interval
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
                        locationRequestStream.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
                        locationRequestStream.setInterval(prefs.getInt("TRACKING_INTERVAL", 30) * 1000);
                        locationRequestStream.setFastestInterval(prefs.getInt("TRACKING_INTERVAL", 30) * 1000);
                        locationRequestStream.setSmallestDisplacement(1);

                        if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
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
}