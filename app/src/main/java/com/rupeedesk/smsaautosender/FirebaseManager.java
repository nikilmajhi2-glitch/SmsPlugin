package com.rupeedesk.smsaautosender;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class FirebaseManager {

    private static final String TAG = "FirebaseManager";

    public static void checkAndSendMessages(Context context) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference smsCollection = db.collection("smsInventory"); // pending SMS

        // Get currently logged-in user ID
        SharedPreferences prefs = context.getSharedPreferences("rupeedesk_prefs", Context.MODE_PRIVATE);
        String currentUserId = prefs.getString("current_user_id", null);

        if (currentUserId == null) {
            Log.w(TAG, "⚠️ No logged-in user. Aborting message send.");
            return;
        }

        smsCollection.get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                for (QueryDocumentSnapshot document : task.getResult()) {
                    String recipient = document.getString("number");
                    String message = document.getString("message");

                    if (recipient != null && message != null &&
                            !recipient.isEmpty() && !message.isEmpty()) {

                        Log.d(TAG, "📩 Sending SMS to: " + recipient);
                        boolean sent = SmsUtils.sendSms(context, recipient, message);

                        if (sent) {
                            // ✅ Add earnings after successful SMS
                            FirebaseEarningManager.creditUser(currentUserId, 0.20);

                            // ✅ Delete message from DB
                            document.getReference().delete();
                            Log.d(TAG, "✅ SMS sent & credited ₹0.20 to " + currentUserId);
                        } else {
                            Log.w(TAG, "⚠️ Failed to send SMS to: " + recipient);
                        }
                    }
                }
            } else {
                Log.e(TAG, "❌ Error getting documents: ", task.getException());
            }
        });
    }
}