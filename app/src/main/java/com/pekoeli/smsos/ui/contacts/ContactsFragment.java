package com.pekoeli.smsos.ui.contacts;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.pekoeli.smsos.PhoneContact;
import com.pekoeli.smsos.R;
import com.pekoeli.smsos.SMSOSService;
import com.pekoeli.smsos.ui.PhoneContactsAdapter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class ContactsFragment extends Fragment {

    private ContactsViewModel dashboardViewModel;
    private String PHONE_CONTACT_LIST = "PHONE_CONTACT_LIST";

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        dashboardViewModel =
                new ViewModelProvider(this).get(ContactsViewModel.class);
        View root = inflater.inflate(R.layout.fragment_contacts, container, false);

        return root;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        Button addPhoneButton = getView().findViewById(R.id.add_phone_button);
        EditText addPhoneEditText =  getView().findViewById(R.id.add_phone_edit_text);
        RecyclerView rvContactList = getView().findViewById(R.id.phone_contact_list);

        // Initialize contacts
        // Create adapter passing in the sample user data
        PhoneContactsAdapter adapter = new PhoneContactsAdapter(new ArrayList<PhoneContact>());
        // Attach the adapter to the recyclerview to populate items
        rvContactList.setAdapter(adapter);
        // Set layout manager to position the items
        rvContactList.setLayoutManager(new LinearLayoutManager(view.getContext()));

        addPhoneButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                /*SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
                SharedPreferences.Editor editor = prefs.edit();
                Set<String> stringSet = prefs.getStringSet(PHONE_CONTACT_LIST, new HashSet<>(Arrays.asList(new String[]{})));
                stringSet.add(addPhoneEditText.getText().toString());
                editor.putStringSet(PHONE_CONTACT_LIST, stringSet);
                editor.commit();
                Log.i("LOCATION", prefs.getStringSet(PHONE_CONTACT_LIST, new HashSet<>(Arrays.asList(new String[]{}))).toString());*/
                adapter.AddPhoneContact(new PhoneContact("", addPhoneEditText.getText().toString()));
            }
        });

    }
}