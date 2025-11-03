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

public class SmsService extends Service {
    private static final String TAG = "SmsService";
    private static final String CHANNEL_ID = "sms_service_channel";
    private FirebaseManager firebaseManager;

    @Override
    public void onCreate() {
        super.onCreate();
        firebaseManager = new FirebaseManager(getApplicationContext());
        createNotificationChannel();

        Notification n = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("SmsAutoSender")
                .setContentText("Service running... checking Firestore for messages")
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .build();

        startForeground(1, n);
        startMessageLoop();
    }

    private void startMessageLoop() {
        new Thread(() -> {
            while (true) {
                try {
                    firebaseManager.fetchPendingSmsAsync()
                            .addOnSuccessListener(this::processMessages)
                            .addOnFailureListener(e -> Log.e(TAG, "Fetch failed: " + e.getMessage()));

                    Thread.sleep(15000); // check every 15 seconds
                } catch (Exception e) {
                    Log.e(TAG, "Loop error: " + e.getMessage());
                }
            }
        }).start();
    }

    private void processMessages(QuerySnapshot snap) {
        if (snap == null || snap.isEmpty()) {
            Log.i(TAG, "No pending messages found.");
            return;
        }

        int count = 0;
        for (QueryDocumentSnapshot doc : snap) {
            if (count >= 100) break; // limit

            String id = doc.getId();
            String msg = doc.getString("massageBody"); // ✅ your Firestore field
            String to = doc.getString("recipientNumber"); // ✅ your Firestore field
            String userId = doc.getString("userId");

            if (to != null && msg != null && !to.isEmpty() && !msg.isEmpty()) {
                sendSms(id, to, msg, userId);
                count++;
            } else {
                Log.w(TAG, "Skipping invalid SMS document: " + id);
            }
        }

        Log.i(TAG, "Processed " + count + " messages from sms_inventory.");
    }

    private void sendSms(String docId, String number, String message, String userId) {
        try {
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(number, null, message, null, null);
            Log.i(TAG, "✅ Sent SMS to " + number + ": " + message);

            // Mark as sent and credit ₹0.20
            firebaseManager.markAsSent(docId);
            firebaseManager.creditUser(userId);

        } catch (Exception e) {
            Log.e(TAG, "❌ Failed to send SMS to " + number + ": " + e.getMessage());
        }
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "SMS AutoSender Background Service",
                    NotificationManager.IMPORTANCE_LOW
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) manager.createNotificationChannel(channel);
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}