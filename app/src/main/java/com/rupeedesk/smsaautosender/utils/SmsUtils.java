package com.rupeedesk.smsaautosender.utils;

import android.content.Context;
import android.telephony.SmsManager;
import android.widget.Toast;

public class SmsUtils {
    public static boolean sendSms(Context context, String number, String message) {
        try {
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(number, null, message, null, null);
            Toast.makeText(context, "SMS sent to " + number, Toast.LENGTH_SHORT).show();
            return true;
        } catch (Exception e) {
            Toast.makeText(context, "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            return false;
        }
    }
}