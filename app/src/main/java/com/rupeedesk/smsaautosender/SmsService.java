package com.rupeedesk.smsaautosender;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.telephony.SmsManager;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.rupeedesk.smsaautosender.model.SmsItem;
import com.rupeedesk.smsaautosender.FirebaseManager;

import java.util.ArrayList;
import java.util.List;

public class SmsService extends Service {
    private static final String TAG = "SmsService";
    private static final String CHANNEL_ID = "sms_service_channel";

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
        Notification n = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("SmsAutoSender")
                .setContentText("Service running")
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .build();
        startForeground(1, n);

        // In a real app you'd fetch from Firestore using FirebaseManager.
        // For demo we use a small hardcoded list or try to fetch if Firebase configured.
        List<SmsItem> messages = new ArrayList<>();
        try {
            FirebaseManager fm = new FirebaseManager(getApplicationContext());
            List<SmsItem> remote = fm.fetchPendingSms();
            if (remote != null && !remote.isEmpty()) {
                messages.addAll(remote);
            }
        } catch (Exception e) {
            Log.i(TAG, "Firebase not configured or fetch failed: " + e.getMessage());
        }

        // Fallback demo messages
        if (messages.isEmpty()) {
            SmsItem s = new SmsItem("+10000000000", "Hello from SmsAutoSender (demo)", 0L, false);
            messages.add(s);
        }

        sendAll(messages);
        stopSelf();
    }

    private void sendAll(List<SmsItem> list) {
        SmsManager smsManager = SmsManager.getDefault();
        for (SmsItem item : list) {
            try {
                smsManager.sendTextMessage(item.getRecipient(), null, item.getMessage(), null, null);
                Log.i(TAG, "Sent SMS to " + item.getRecipient());
            } catch (Exception e) {
                Log.e(TAG, "Failed to send SMS to " + item.getRecipient() + ": " + e.getMessage());
            }
        }
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "SMS Service Channel",
                    NotificationManager.IMPORTANCE_LOW
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) manager.createNotificationChannel(serviceChannel);
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
