package com.pekoeli.smsos.ui.home;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.pekoeli.smsos.R;
import com.pekoeli.smsos.SMSOSService;

import static androidx.core.content.ContextCompat.getSystemService;

public class HomeFragment extends Fragment {

    private HomeViewModel homeViewModel;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        homeViewModel =
                new ViewModelProvider(this).get(HomeViewModel.class);
        View root = inflater.inflate(R.layout.fragment_home, container, false);

        return root;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        ToggleButton toggleListener = getView().findViewById(R.id.toggle_listener_button);
        TextView serviceStatusText = getView().findViewById(R.id.service_status_text);
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
                //startService(new Intent(this, SMSOSService.class));
                serviceIntent.putExtra("inputExtra", "Your location can be requested");
                ContextCompat.startForegroundService(getContext(), serviceIntent);
                serviceStatusText.setText(getResources().getText(R.string.service_active_text));
            } else {
                // The toggle is disabled
                //stopService(new Intent(this, SMSOSService.class));
                getContext().stopService(serviceIntent);
                serviceStatusText.setText(getResources().getText(R.string.service_inactive_text));
            }
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