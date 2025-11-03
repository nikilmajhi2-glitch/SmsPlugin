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

import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.rupeedesk.smsaautosender.model.SmsItem;

import java.util.ArrayList;
import java.util.List;

public class SmsService extends Service {
    private static final String TAG = "SmsService";
    private static final String CHANNEL_ID = "sms_service_channel";
    private FirebaseManager fm;

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
        Notification n = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("SmsAutoSender")
                .setContentText("Sending messages...")
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .build();
        startForeground(1, n);

        fm = new FirebaseManager(getApplicationContext());
        loadAndSend();
    }

    private void loadAndSend() {
        fm.fetchPendingSmsAsync().addOnSuccessListener(this::processMessages)
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to fetch SMS: " + e.getMessage());
                    stopSelf();
                });
    }

    private void processMessages(QuerySnapshot snap) {
        if (snap == null || snap.isEmpty()) {
            Log.i(TAG, "No pending SMS to send");
            stopSelf();
            return;
        }

        List<SmsItem> messages = new ArrayList<>();
        for (QueryDocumentSnapshot doc : snap) {
            String id = doc.getId();
            String to = doc.getString("recipient");
            String msg = doc.getString("message");
            String userId = doc.getString("userId"); // optional
            messages.add(new SmsItem(to, msg, 0L, false));
            sendSms(id, to, msg, userId);
        }
    }

    private void sendSms(String docId, String to, String msg, String userId) {
        SmsManager smsManager = SmsManager.getDefault();
        try {
            smsManager.sendTextMessage(to, null, msg, null, null);
            Log.i(TAG, "Sent SMS to " + to);

            // Delete message after send
            fm.deleteSms(docId);

            // Credit ₹0.20 to sender
            if (userId != null && !userId.isEmpty()) {
                fm.creditUser(userId);
            }

        } catch (Exception e) {
            Log.e(TAG, "Failed to send SMS: " + e.getMessage());
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