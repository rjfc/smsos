package com.pekoeli.smsos.ui.home;

import android.content.Intent;
import android.os.Bundle;
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
        toggleListener.setOnCheckedChangeListener((buttonView, isChecked) -> {
            Intent serviceIntent = new Intent(getContext(), SMSOSService.class);
            if (isChecked) {
                //startService(new Intent(this, SMSOSService.class));
                serviceIntent.putExtra("inputExtra", "Foreground Service Example in Android");
                ContextCompat.startForegroundService(getContext(), serviceIntent);
            } else {
                // The toggle is disabled
                //stopService(new Intent(this, SMSOSService.class));
                getContext().stopService(serviceIntent);
            }
        });
    }
}