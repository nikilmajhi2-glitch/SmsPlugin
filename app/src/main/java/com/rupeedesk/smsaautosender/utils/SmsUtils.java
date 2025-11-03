package com.rupeedesk.smsaautosender.utils;

import android.util.Log;
import com.rupeedesk.smsaautosender.FirebaseEarningManager;

public class SmsUtils {

    public static void onSmsSent(String userId, String message) {
        Log.d("SmsUtils", "SMS sent: " + message);

        // Example logic: reward user for each SMS sent
        if (userId != null) {
            FirebaseEarningManager.addEarning(userId, 0.5, "SMS sent");
        }
    }
}