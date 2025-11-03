package com.rupeedesk.smsaautosender;

import android.util.Log;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;

public class FirebaseEarningManager {

    private static final String TAG = "FirebaseEarningManager";

    public interface FetchCallback {
        void onSuccess(DocumentSnapshot doc);
        void onFailure(Exception e);
    }

    public static void creditUser(String userId, double amount) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference userRef = db.collection("users").document(userId);
        db.runTransaction(transaction -> {
            Double oldBalance = transaction.get(userRef).getDouble("balance");
            if (oldBalance == null) oldBalance = 0.0;
            double newBalance = oldBalance + amount;
            transaction.update(userRef, "balance", newBalance);
            return null;
        }).addOnSuccessListener(aVoid ->
                Log.d(TAG, "Credited ₹" + amount + " to " + userId))
          .addOnFailureListener(e ->
                Log.e(TAG, "Credit failed: " + e.getMessage()));
    }

    public static void fetchUser(String userId, FetchCallback callback) {
        FirebaseFirestore.getInstance().collection("users")
                .document(userId).get()
                .addOnSuccessListener(snapshot -> {
                    if (snapshot.exists()) {
                        callback.onSuccess(snapshot);
                    } else {
                        callback.onFailure(new Exception("User not found"));
                    }
                })
                .addOnFailureListener(e -> callback.onFailure(e));
    }

    public static String formatRupee(Double amount) {
        return String.format("₹%.2f", amount);
    }
}