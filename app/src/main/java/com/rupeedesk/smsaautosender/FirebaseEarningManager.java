package com.rupeedesk.smsaautosender;

import android.util.Log;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;

public class FirebaseEarningManager {
    private static final String TAG = "FirebaseEarningManager";
    private static final FirebaseFirestore db = FirebaseFirestore.getInstance();

    // ✅ Save earning
    public static void addEarning(String userId, double amount, String reason) {
        if (userId == null) return;

        Map<String, Object> earning = new HashMap<>();
        earning.put("userId", userId);
        earning.put("amount", amount);
        earning.put("reason", reason);
        earning.put("timestamp", System.currentTimeMillis());

        db.collection("earnings")
                .add(earning)
                .addOnSuccessListener(ref -> Log.d(TAG, "Earning added: " + ref.getId()))
                .addOnFailureListener(e -> Log.e(TAG, "Failed to add earning", e));

        // ✅ Also update user's balance
        DocumentReference userRef = db.collection("users").document(userId);
        db.runTransaction(tx -> {
            DocumentReference doc = userRef;
            double oldBalance = 0;
            Double balanceVal = tx.get(doc).getDouble("balance");
            if (balanceVal != null) oldBalance = balanceVal;
            tx.update(doc, "balance", oldBalance + amount);
            return null;
        }).addOnFailureListener(e -> Log.e(TAG, "Balance update failed", e));
    }

    // ✅ Withdraw request
    public static void createWithdrawRequest(String userId, double amount) {
        if (userId == null) return;

        Map<String, Object> request = new HashMap<>();
        request.put("userId", userId);
        request.put("amount", amount);
        request.put("status", "pending");
        request.put("timestamp", System.currentTimeMillis());

        db.collection("withdrawRequests")
                .add(request)
                .addOnSuccessListener(ref -> Log.d(TAG, "Withdraw request added: " + ref.getId()))
                .addOnFailureListener(e -> Log.e(TAG, "Failed to add withdraw request", e));
    }
}