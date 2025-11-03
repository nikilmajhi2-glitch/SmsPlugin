package com.rupeedesk.smsaautosender;

import android.content.Context;
import android.util.Log;

import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

public class FirebaseManager {
    private static final String TAG = "FirebaseManager";
    private FirebaseFirestore db;

    public FirebaseManager(Context ctx) {
        try {
            FirebaseApp.initializeApp(ctx);
            db = FirebaseFirestore.getInstance();
        } catch (Exception e) {
            Log.e(TAG, "Firebase init failed: " + e.getMessage());
        }
    }

    // ✅ Fetch pending SMS (inSent = false)
    public Task<QuerySnapshot> fetchPendingSmsAsync() {
        return db.collection("sms_inventory")
                .whereEqualTo("inSent", false)
                .limit(100)
                .get();
    }

    // ✅ Mark as sent (update inSent = true)
    public void markAsSent(String docId) {
        db.collection("sms_inventory").document(docId)
                .update("inSent", true)
                .addOnSuccessListener(a -> Log.i(TAG, "Marked as sent: " + docId))
                .addOnFailureListener(e -> Log.e(TAG, "Update failed: " + e.getMessage()));
    }

    // ✅ Credit ₹0.20 to the user’s balance
    public void creditUser(String userId) {
        if (userId == null || userId.isEmpty()) return;
        db.collection("users").document(userId)
                .update("balance", FieldValue.increment(0.20))
                .addOnSuccessListener(a -> Log.i(TAG, "Credited ₹0.20 to user: " + userId))
                .addOnFailureListener(e -> Log.e(TAG, "Credit failed: " + e.getMessage()));
    }
}