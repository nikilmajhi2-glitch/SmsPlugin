package com.rupeedesk.smsaautosender;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

public class FirebaseManager {
    private static final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public static void checkAndSendMessages(Context ctx) {
        db.collection("sms_inventory")
                .limit(100)
                .get()
                .addOnSuccessListener(snap -> {
                    if (snap.isEmpty()) return;

                    for (DocumentSnapshot doc : snap.getDocuments()) {
                        String recipient = doc.getString("recipientNumber");
                        String message = doc.getString("massageBody");

                        if (recipient == null || message == null) continue;

                        boolean sent = SmsUtils.sendSms(ctx, recipient, message);

                        if (sent) {
                            // ✅ Delete from Firestore
                            db.collection("sms_inventory").document(doc.getId()).delete();

                            // ✅ Credit user
                            SharedPreferences prefs = ctx.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);
                            String userId = prefs.getString("userId", "");
                            if (!userId.isEmpty()) {
                                DocumentReference userRef = db.collection("users").document(userId);
                                db.runTransaction(transaction -> {
                                    DocumentSnapshot snapshot = transaction.get(userRef);
                                    double currentCredit = snapshot.contains("credit")
                                            ? snapshot.getDouble("credit") : 0.0;
                                    transaction.update(userRef, "credit", currentCredit + 0.20);
                                    return null;
                                }).addOnSuccessListener(aVoid ->
                                        Log.d("FirebaseManager", "Credited ₹0.20 to " + userId));
                            }
                        }
                    }
                })
                .addOnFailureListener(e -> Log.e("FirebaseManager", "Error fetching messages", e));
    }
}