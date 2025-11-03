package com.rupeedesk.smsaautosender;

import android.content.Context;
import android.util.Log;

import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.rupeedesk.smsaautosender.model.SmsItem;

import java.util.ArrayList;
import java.util.Date;
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

    /** ✅ Fetch up to 100 unsent SMS messages */
    public List<SmsItem> fetchPendingSms() {
        List<SmsItem> list = new ArrayList<>();
        if (db == null) return list;

        try {
            QuerySnapshot snap = db.collection("sms_inventory")
                    .whereEqualTo("isSent", false)
                    .limit(100)
                    .get()
                    .getResult();

            if (snap != null) {
                for (QueryDocumentSnapshot doc : snap) {
                    String recipient = doc.getString("recipientNumber");
                    String message = doc.getString("messageBody");
                    if (recipient != null && message != null) {
                        list.add(new SmsItem(doc.getId(), recipient, message));
                    }
                }
            }

            Log.i(TAG, "Fetched " + list.size() + " messages from Firestore.");
        } catch (Exception e) {
            Log.e(TAG, "Error fetching messages: " + e.getMessage());
        }
        return list;
    }

    /** ✅ Mark SMS as sent + deduct ₹0.20 credit */
    public void markAsSent(SmsItem item) {
        if (db == null || item == null || item.getId() == null) return;

        DocumentReference docRef = db.collection("sms_inventory").document(item.getId());
        docRef.update(
                "isSent", true,
                "sentAt", new Date(),
                "sentBy", "9767646494"
        ).addOnSuccessListener(a ->
                Log.i(TAG, "Marked message sent: " + item.getId())
        ).addOnFailureListener(e ->
                Log.e(TAG, "Failed to mark sent: " + e.getMessage())
        );

        // Deduct ₹0.20 from user balance (adjust "defaultUser" to actual user doc ID)
        db.collection("users").document("defaultUser")
                .update("balance", FieldValue.increment(-0.20))
                .addOnSuccessListener(a ->
                        Log.i(TAG, "Deducted ₹0.20 credit.")
                ).addOnFailureListener(e ->
                        Log.e(TAG, "Failed to deduct credit: " + e.getMessage())
                );
    }
}