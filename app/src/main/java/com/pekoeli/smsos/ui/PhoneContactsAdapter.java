package com.pekoeli.smsos.ui;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.pekoeli.smsos.PhoneContact;
import com.pekoeli.smsos.R;

import java.util.Calendar;
import java.util.List;

public class PhoneContactsAdapter extends RecyclerView.Adapter<PhoneContactsAdapter.ViewHolder> {

    private List<PhoneContact> phoneContactsList;
    private SharedPreferences prefs;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final ImageButton deleteContactButton;
        private final TextView nameTextView;
        private final TextView phoneTextView;
        private final Button sendInstructionsIndividualButton;
        private final CheckBox emergencyContactCheckbox;

        public ViewHolder(View view) {
            super(view);
            deleteContactButton = view.findViewById(R.id.delete_contact_button);
            nameTextView = view.findViewById(R.id.phone_contact_name_text);
            phoneTextView = view.findViewById(R.id.phone_contact_phone_text);
            sendInstructionsIndividualButton = view.findViewById(R.id.send_instructions_individual_button);
            emergencyContactCheckbox = view.findViewById(R.id.emergency_contact_checkbox);
        }
    }

    public PhoneContactsAdapter(List<PhoneContact> dataSet) {
         phoneContactsList = dataSet;
    }
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.contact_list_item, viewGroup, false);

        prefs = PreferenceManager.getDefaultSharedPreferences(view.getContext());
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {
        viewHolder.nameTextView.setText(phoneContactsList.get(position).getName());
        viewHolder.phoneTextView.setText(phoneContactsList.get(position).getPhone());
        viewHolder.emergencyContactCheckbox.setChecked(phoneContactsList.get(position).isEmergencyContact());
        viewHolder.deleteContactButton.setOnClickListener(v -> {
            PhoneContact removeItem = phoneContactsList.get(position);
            phoneContactsList.remove(position);
            notifyItemRemoved(position);
            UpdateSharedPreferences();
        });
        viewHolder.emergencyContactCheckbox.setOnClickListener(v -> {
            phoneContactsList.get(position).toggleIsEmergencyContact();
            UpdateSharedPreferences();
        });
        // Send instructions to a contact
        viewHolder.sendInstructionsIndividualButton.setOnClickListener(v -> {
            viewHolder.sendInstructionsIndividualButton.setEnabled(false);
            viewHolder.sendInstructionsIndividualButton.setText("Instructions Sent");
            SmsManager smsManager = SmsManager.getDefault();
            String textMessage = phoneContactsList.get(position).getName() + ", " + prefs.getString("NAME", "") + " has added you to their contact list for location tracking. If they have the tracker enabled, you can use the following commands:";

            smsManager.sendTextMessage(phoneContactsList.get(position).getPhone(), null, textMessage, null, null);
            textMessage = "\"Location\" - Get " + prefs.getString("NAME", "") + "'s current location";
            textMessage += "\n\"Location track\" - Track/disable tracking of " + prefs.getString("NAME", "") + "'s location at " + String.valueOf(prefs.getInt("TRACKING_INTERVAL", 30)) + " second intervals";
            smsManager.sendTextMessage(phoneContactsList.get(position).getPhone(), null, textMessage, null, null);
        });
    }

    @Override
    public int getItemCount() {
        if (phoneContactsList != null)
        {
            return phoneContactsList.size();
        }
        return 0;
    }

    public void AddPhoneContact(PhoneContact contact)
    {
        phoneContactsList.add(contact);
        notifyDataSetChanged();
    }

    public void UpdateSharedPreferences()
    {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("PHONE_CONTACT_LIST", new Gson().toJson(phoneContactsList));
        editor.commit();
    }

    public List<PhoneContact> GetPhoneContacts()
    {
        return phoneContactsList;
    }

}