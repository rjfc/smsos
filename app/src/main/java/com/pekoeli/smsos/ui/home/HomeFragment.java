package com.pekoeli.smsos.ui.home;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.telephony.SmsManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.pekoeli.smsos.PhoneContact;
import com.pekoeli.smsos.R;
import com.pekoeli.smsos.SMSOSService;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import static com.pekoeli.smsos.ui.contacts.ContactsFragment.PHONE_CONTACT_LIST;

public class HomeFragment extends Fragment {
    enum SendLocationReasons {
        NONE,
        EMERGENCY,
        CREEPY_PERSON,
        SCARY_PLACE
    }

    private HomeViewModel homeViewModel;
    private SharedPreferences prefs;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationRequest locationRequest;
    private SendLocationReasons sendLocationReason;
    private List<PhoneContact> phoneContacts;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        homeViewModel =
                new ViewModelProvider(this).get(HomeViewModel.class);
        View root = inflater.inflate(R.layout.fragment_home, container, false);
        prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        phoneContacts = new ArrayList<PhoneContact>();
        if (prefs.contains(PHONE_CONTACT_LIST) && !prefs.getString(PHONE_CONTACT_LIST, "").equals(""))
        {
            phoneContacts = new Gson().fromJson(prefs.getString(PHONE_CONTACT_LIST, ""), new TypeToken<ArrayList<PhoneContact>>(){}.getType());
        }
        return root;
    }

    private String generateText (Location location, String name)
    {
        String latitude = String.valueOf(location.getLatitude());
        String longitude = String.valueOf(location.getLongitude());
        String textMessage = name + "'s location";
        textMessage += "\n----------------------------";
        textMessage += "\nGoogle Maps Link: " + "http://maps.google.com/maps?f=q&q=" + latitude + "," + longitude;
        textMessage += "\nTime: " + Calendar.getInstance().getTime();
        return textMessage;
    }

    private LocationCallback locationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            for (Location location : locationResult.getLocations()) {
                fusedLocationClient.removeLocationUpdates(locationCallback);
                SmsManager smsManager = SmsManager.getDefault();
                if (phoneContacts != null) {
                    for (PhoneContact pc: phoneContacts) {
                        if (pc.isEmergencyContact()) {
                            smsManager.sendTextMessage(pc.getPhone(), null, generateText(location, prefs.getString("NAME", "")), null, null);
                        }
                    }
                }
            }
        }
    };

    private void sendLocation() {
        locationRequest = new LocationRequest();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(0);
        locationRequest.setFastestInterval(0);
        locationRequest.setSmallestDisplacement(1);

        if (ActivityCompat.checkSelfPermission(getContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(getContext());
        fusedLocationClient.requestLocationUpdates
                (
                        locationRequest,
                        locationCallback,
                        null
                );
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        Switch toggleListener = getView().findViewById(R.id.toggle_listener_button);
        TextView serviceStatusText = getView().findViewById(R.id.service_status_text);

        AppCompatButton sendLocationButton = getView().findViewById(R.id.send_location_button);
        AppCompatButton emergencyButton = getView().findViewById(R.id.emergency_button);
        AppCompatButton creepyPersonButton = getView().findViewById(R.id.creepy_person_button);
        AppCompatButton scaryPlaceButton = getView().findViewById(R.id.scary_place_button);


        toggleListener.setChecked(isMyServiceRunning(SMSOSService.class));
        if (isMyServiceRunning(SMSOSService.class))
        {
            serviceStatusText.setText(getResources().getText(R.string.service_active_text));
        }
        else
        {
            serviceStatusText.setText(getResources().getText(R.string.service_inactive_text));
        }
        toggleListener.setOnCheckedChangeListener((buttonView, isChecked) -> {
            Intent serviceIntent = new Intent(getContext(), SMSOSService.class);
            if (isChecked) {
                // The toggle is enabled
                serviceIntent.putExtra("inputExtra", "Your location can be requested");
                ContextCompat.startForegroundService(getContext(), serviceIntent);
                serviceStatusText.setText(getResources().getText(R.string.service_active_text));
            } else {
                // The toggle is disabled
                getContext().stopService(serviceIntent);
                serviceStatusText.setText(getResources().getText(R.string.service_inactive_text));
            }
        });

        sendLocationButton.setOnClickListener(v -> {
            SmsManager smsManager = SmsManager.getDefault();
            sendLocationReason = SendLocationReasons.NONE;
            if (phoneContacts != null) {
                for (PhoneContact pc: phoneContacts) {
                    if (pc.isEmergencyContact()) {
                        smsManager.sendTextMessage(pc.getPhone(), null, prefs.getString("NAME", "") + " has sent you their location!", null, null);
                    }
                }
            }
            sendLocation();
        });

        emergencyButton.setOnClickListener(v -> {
            SmsManager smsManager = SmsManager.getDefault();
            sendLocationReason = SendLocationReasons.NONE;
            if (phoneContacts != null) {
                for (PhoneContact pc: phoneContacts) {
                    if (pc.isEmergencyContact()) {
                        smsManager.sendTextMessage(pc.getPhone(), null, prefs.getString("NAME", "").toUpperCase() + " HAS AN EMERGENCY - CALL AND GO TO LOCATION ASAP!", null, null);
                    }
                }
            }
            sendLocation();
        });

        creepyPersonButton.setOnClickListener(v -> {
            SmsManager smsManager = SmsManager.getDefault();
            sendLocationReason = SendLocationReasons.NONE;
            if (phoneContacts != null) {
                for (PhoneContact pc: phoneContacts) {
                    if (pc.isEmergencyContact()) {
                        smsManager.sendTextMessage(pc.getPhone(), null, prefs.getString("NAME", "").toUpperCase() + " SEES A CREEPY PERSON - CALL ASAP", null, null);
                    }
                }
            }
            sendLocation();
        });

        scaryPlaceButton.setOnClickListener(v -> {
            SmsManager smsManager = SmsManager.getDefault();
            sendLocationReason = SendLocationReasons.NONE;
            if (phoneContacts != null) {
                for (PhoneContact pc: phoneContacts) {
                    if (pc.isEmergencyContact()) {
                        smsManager.sendTextMessage(pc.getPhone(), null, prefs.getString("NAME", "").toUpperCase() +  " IS AT A SCARY PLACE - CALL ASAP", null, null);
                    }
                }
            }
            sendLocation();
        });
    }
    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getContext().getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
}