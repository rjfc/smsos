package com.pekoeli.smsos.ui;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.pekoeli.smsos.PhoneContact;
import com.pekoeli.smsos.R;

import java.util.List;

public class PhoneContactsAdapter extends RecyclerView.Adapter<PhoneContactsAdapter.ViewHolder> {

    private List<PhoneContact> localDataSet;
    private SharedPreferences prefs;

    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder).
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final ImageButton deleteContactButton;
        private final TextView nameTextView;
        private final TextView phoneTextView;

        public ViewHolder(View view) {
            super(view);
            // Define click listener for the ViewHolder's View
            deleteContactButton = view.findViewById(R.id.delete_contact_button);
            nameTextView = view.findViewById(R.id.phone_contact_name_text);
            phoneTextView = view.findViewById(R.id.phone_contact_phone_text);
        }
    }

    /**
     * Initialize the dataset of the Adapter.
     *
     * @param dataSet String[] containing the data to populate views to be used
     * by RecyclerView.
     */
    public PhoneContactsAdapter(List<PhoneContact> dataSet) {
        Log.i("LOCATION", "d:" + dataSet);
        localDataSet = dataSet;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        // Create a new view, which defines the UI of the list item
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.contact_list_item, viewGroup, false);

        prefs = PreferenceManager.getDefaultSharedPreferences(view.getContext());
        return new ViewHolder(view);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {

        // Get element from your dataset at this position and replace the
        // contents of the view with that element
        viewHolder.nameTextView.setText(localDataSet.get(position).getName());
        viewHolder.phoneTextView.setText(localDataSet.get(position).getPhone());
        viewHolder.deleteContactButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                PhoneContact removeItem = localDataSet.get(position);
                // remove your item from data base
                localDataSet.remove(position);  // remove the item from list
                notifyItemRemoved(position); // notify the adapter about the removed item
                UpdateSharedPreferences();
            }
        });
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        if (localDataSet != null)
        {
            return localDataSet.size();
        }
        return 0;
    }

    public void AddPhoneContact(PhoneContact contact)
    {
        localDataSet.add(contact);
        notifyDataSetChanged();
        //UpdateSharedPreferences();
    }

    public void UpdateSharedPreferences()
    {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("PHONE_CONTACT_LIST", new Gson().toJson(localDataSet));
        editor.commit();
    }

    public List<PhoneContact> GetPhoneContacts()
    {
        return localDataSet;
    }

}