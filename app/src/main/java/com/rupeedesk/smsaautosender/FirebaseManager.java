package com.rupeedesk.smsaautosender;

import android.content.Context;
import android.util.Log;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class FirebaseManager {

    private static final String TAG = "FirebaseManager";

    public static void checkAndSendMessages(Context context) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference smsCollection = db.collection("smsInventory"); // ✅ updated name

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
                            // ✅ Deduct credit after send
                            deductCredit();
                            // ✅ Delete message after send
                            document.getReference().delete();
                            Log.d(TAG, "✅ SMS sent successfully and deleted from Firestore.");
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

    private static void deductCredit() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users")
                .document("global_user")
                .update("credits", com.google.firebase.firestore.FieldValue.increment(-0.20))
                .addOnSuccessListener(aVoid -> Log.d(TAG, "💰 Credit deducted successfully"))
                .addOnFailureListener(e -> Log.e(TAG, "❌ Credit deduction failed", e));
    }
}