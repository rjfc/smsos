package com.pekoeli.smsos;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;
import android.widget.Toast;

public class SMSReceiver extends BroadcastReceiver
{
    public void onReceive(Context context, Intent intent)
    {
        Bundle myBundle = intent.getExtras();
        SmsMessage [] messages = null;
        String strMessage = "";

        if (myBundle != null)
        {
            Object [] pdus = (Object[]) myBundle.get("pdus");
            messages = new SmsMessage[pdus.length];

            for (int i = 0; i < messages.length; i++)
            {
                //
                messages[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
                // messages[i].getOriginatingAddress() to get the phone that asked
                if (messages[i].getMessageBody().equals("Location"))
                {

                    Log.i("LOCATION", "LOCATION");
                    Toast.makeText(context, "LOCATION WAS ASKED FOR", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
}