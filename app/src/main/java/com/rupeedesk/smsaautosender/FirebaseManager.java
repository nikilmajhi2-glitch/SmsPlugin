package com.rupeedesk.smsaautosender;

import android.util.Log;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

public class FirebaseEarningManager {

    public interface AuthCallback {
        void onSuccess(String userId);
        void onFailure(String reason);
    }

    public interface FetchCallback {
        void onSuccess(DocumentSnapshot doc);
        void onFailure(Exception e);
    }

    private static final String TAG = "FirebaseEarningManager";
    public static final FirebaseFirestore db = FirebaseFirestore.getInstance();

    // ✅ Create new user
    public static void createUser(String phone, String pin, String deviceId, String name, AuthCallback callback) {
        Map<String, Object> data = new HashMap<>();
        data.put("phone", phone);
        data.put("pin", pin);
        data.put("deviceId", deviceId);
        data.put("name", name);
        data.put("balance", 0.0);

        db.collection("users")
                .document(deviceId)
                .set(data)
                .addOnSuccessListener(aVoid -> callback.onSuccess(deviceId))
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    // ✅ Authenticate user
    public static void authenticate(String phone, String pin, String deviceId, AuthCallback callback) {
        db.collection("users")
                .document(deviceId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()
                            && pin.equals(doc.getString("pin"))
                            && phone.equals(doc.getString("phone"))) {
                        callback.onSuccess(doc.getId());
                    } else {
                        callback.onFailure("Invalid credentials or device.");
                    }
                })
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    // ✅ Fetch user (updated with success & failure)
    public static void fetchUser(String userId, FetchCallback callback) {
        db.collection("users")
                .document(userId)
                .get()
                .addOnSuccessListener(callback::onSuccess)
                .addOnFailureListener(callback::onFailure);
    }

    // ✅ Credit user
    public static void creditUser(String userId, double amount) {
        db.collection("users")
                .document(userId)
                .update("balance", com.google.firebase.firestore.FieldValue.increment(amount))
                .addOnSuccessListener(aVoid -> Log.d(TAG, "💰 Credited ₹" + amount))
                .addOnFailureListener(e -> Log.e(TAG, "❌ Credit failed", e));
    }

    // ✅ Request withdraw
    public static void requestWithdraw(String userId, double amount, Runnable onSuccess, Runnable onFailure) {
        Map<String, Object> data = new HashMap<>();
        data.put("userId", userId);
        data.put("amount", amount);
        data.put("timestamp", System.currentTimeMillis());

        db.collection("withdrawRequests")
                .add(data)
                .addOnSuccessListener(doc -> onSuccess.run())
                .addOnFailureListener(e -> onFailure.run());
    }

    // ✅ Format rupee
    public static String formatRupee(double amt) {
        return "₹" + new DecimalFormat("#,##0.00").format(amt);
    }
}