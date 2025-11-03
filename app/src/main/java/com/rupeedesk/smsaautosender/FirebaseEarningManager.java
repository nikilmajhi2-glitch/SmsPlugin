package com.rupeedesk.smsaautosender;

import android.util.Log;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class FirebaseEarningManager {
    private static final String TAG = "FirebaseEarningManager";
    public static FirebaseFirestore db = FirebaseFirestore.getInstance();

    // ✅ Create a new user (during signup)
    public static void createUser(String phone, String pin, String deviceId, String name, AuthCallback callback) {
        DocumentReference userRef = db.collection("users").document(phone);
        userRef.get().addOnSuccessListener(doc -> {
            if (doc.exists()) {
                callback.onFailure("User already exists");
            } else {
                Map<String, Object> data = new HashMap<>();
                data.put("phone", phone);
                data.put("pin", pin);
                data.put("deviceId", deviceId);
                data.put("name", name);
                data.put("balance", 0.0);
                data.put("bank", "");
                data.put("joinedAt", FieldValue.serverTimestamp());
                userRef.set(data)
                        .addOnSuccessListener(aVoid -> callback.onSuccess(phone))
                        .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
            }
        });
    }

    // ✅ Login user
    public static void authenticate(String phone, String pin, String deviceId, AuthCallback callback) {
        DocumentReference userRef = db.collection("users").document(phone);
        userRef.get().addOnSuccessListener(doc -> {
            if (doc.exists()) {
                String storedPin = doc.getString("pin");
                String storedDevice = doc.getString("deviceId");

                if (storedPin != null && storedDevice != null &&
                        storedPin.equals(pin) && storedDevice.equals(deviceId)) {
                    callback.onSuccess(phone);
                } else {
                    callback.onFailure("Invalid PIN or Device");
                }
            } else {
                callback.onFailure("User not found");
            }
        }).addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    // ✅ Credit balance
    public static void creditUser(String userId, double amount) {
        db.collection("users").document(userId)
                .update("balance", FieldValue.increment(amount))
                .addOnSuccessListener(aVoid -> Log.d(TAG, "💰 Credited ₹" + amount + " to " + userId))
                .addOnFailureListener(e -> Log.e(TAG, "❌ Failed to credit user", e));
    }

    // ✅ Request withdraw
    public static void requestWithdraw(String userId, double amount, Runnable callback) {
        DocumentReference userRef = db.collection("users").document(userId);
        userRef.get().addOnSuccessListener(doc -> {
            Double bal = doc.getDouble("balance");
            if (bal != null && bal >= amount) {
                db.collection("withdrawals").add(new HashMap<String, Object>() {{
                    put("userId", userId);
                    put("amount", amount);
                    put("status", "pending");
                    put("requestedAt", FieldValue.serverTimestamp());
                }});
                userRef.update("balance", FieldValue.increment(-amount));
                callback.run();
            } else {
                Log.w(TAG, "⚠️ Insufficient balance");
            }
        });
    }

    // ✅ Fetch user info
    public static void fetchUser(String userId, FetchCallback callback) {
        db.collection("users").document(userId).get()
                .addOnSuccessListener(callback::onFetched)
                .addOnFailureListener(e -> Log.e(TAG, "❌ Fetch user failed", e));
    }

    public interface AuthCallback {
        void onSuccess(String userId);
        void onFailure(String reason);
    }

    public interface FetchCallback {
        void onFetched(DocumentSnapshot doc);
    }

    // ✅ Format rupees
    public static String formatRupee(double amt) {
        return "₹" + String.format("%.2f", amt);
    }
}