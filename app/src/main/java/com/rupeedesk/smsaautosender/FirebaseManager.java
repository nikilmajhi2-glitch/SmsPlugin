package com.rupeedesk.smsaautosender;

import android.content.Context;
import android.util.Log;

import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.rupeedesk.smsaautosender.model.SmsItem;

import java.util.ArrayList;
import java.util.List;

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

    /** Load up to 100 pending SMS */
    public Task<QuerySnapshot> fetchPendingSmsAsync() {
        if (db == null) return null;
        return db.collection("sms")
                .whereEqualTo("sent", false)
                .limit(100)
                .get();
    }

    /** Delete successfully sent SMS */
    public void deleteSms(String docId) {
        if (db == null) return;
        db.collection("sms").document(docId)
                .delete()
                .addOnSuccessListener(aVoid -> Log.i(TAG, "Deleted SMS doc: " + docId))
                .addOnFailureListener(e -> Log.e(TAG, "Delete failed: " + e.getMessage()));
    }

    /** Add ₹0.20 credit to user’s balance */
    public void creditUser(String userId) {
        if (db == null) return;
        db.collection("users").document(userId)
                .get()
                .addOnSuccessListener(doc -> {
                    double balance = 0;
                    if (doc.exists() && doc.getDouble("balance") != null)
                        balance = doc.getDouble("balance");
                    balance += 0.20;
                    db.collection("users").document(userId)
                            .update("balance", balance)
                            .addOnSuccessListener(aVoid -> Log.i(TAG, "Credited ₹0.20 to user " + userId))
                            .addOnFailureListener(e -> Log.e(TAG, "Credit update failed: " + e.getMessage()));
                });
    }
}