package com.pekoeli.smsos.ui.settings;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.pekoeli.smsos.PhoneContact;
import com.pekoeli.smsos.R;
import com.pekoeli.smsos.SMSOSService;
import com.pekoeli.smsos.ui.PhoneContactsAdapter;

import java.util.ArrayList;
import java.util.List;

public class SettingsFragment extends Fragment {

    private SettingsViewModel notificationsViewModel;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        notificationsViewModel =
                new ViewModelProvider(this).get(SettingsViewModel.class);
        View root = inflater.inflate(R.layout.fragment_settings, container, false);
        return root;
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        EditText nameSetting = getView().findViewById(R.id.name_setting);
        EditText commandPrefixSetting = getView().findViewById(R.id.command_prefix_setting);
        EditText trackingIntervalSetting = getView().findViewById(R.id.tracking_interval_setting);
        CheckBox needsToBeContactSetting = getView().findViewById(R.id.needs_to_be_contact_setting);

        nameSetting.setText(prefs.getString("NAME", ""));
        commandPrefixSetting.setText(prefs.getString("COMMAND_PREFIX", "Location"));
        trackingIntervalSetting.setText(String.valueOf(prefs.getInt("TRACKING_INTERVAL", 30)));
        needsToBeContactSetting.setChecked(prefs.getBoolean("NEEDS_TO_BE_CONTACT", true));

        nameSetting.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count){
                prefs.edit().putString("NAME", s.toString()).apply();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after){

            }
        });

        commandPrefixSetting.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count){
                prefs.edit().putString("COMMAND_PREFIX", s.toString().trim().toLowerCase()).apply();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after){

            }
        });

        trackingIntervalSetting.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count){
                if (s.toString().equals(""))
                {
                    prefs.edit().putInt("TRACKING_INTERVAL", 1).apply();
                    trackingIntervalSetting.setText("1");
                }
                else
                {

                    prefs.edit().putInt("TRACKING_INTERVAL", Integer.valueOf(s.toString())).apply();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after){

            }
        });

        needsToBeContactSetting.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean("NEEDS_TO_BE_CONTACT", isChecked).apply();
        });
    }
}