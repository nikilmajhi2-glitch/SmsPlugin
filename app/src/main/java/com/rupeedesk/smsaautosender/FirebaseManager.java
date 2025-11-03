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
        CollectionReference smsCollection = db.collection("smsInventory"); // ✅ collection for pending SMS

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
                    String recipient = document.getString("number");   // ✅ updated field name
                    String message = document.getString("message");   // ✅ updated field name

                    if (recipient != null && message != null &&
                        !recipient.isEmpty() && !message.isEmpty()) {

                        Log.d(TAG, "📩 Sending SMS to: " + recipient + " -> " + message);
                        boolean sent = SmsUtils.sendSms(context, recipient, message);

                        if (sent) {
                            // ✅ Credit the user when message sent successfully
                            FirebaseEarningManager.creditUser(currentUserId, 0.20);

                            // ✅ Delete message after send
                            document.getReference().delete();
                            Log.d(TAG, "✅ SMS sent & credited ₹0.20 to user " + currentUserId);
                        } else {
                            Log.w(TAG, "⚠️ Failed to send SMS to: " + recipient);
                        }
                    } else {
                        Log.w(TAG, "⚠️ Invalid message or number in document: " + document.getId());
                    }
                }
            } else {
                Log.e(TAG, "❌ Error getting documents: ", task.getException());
            }
        });
    }
}