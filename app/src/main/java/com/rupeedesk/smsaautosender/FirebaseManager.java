package com.rupeedesk.smsaautosender;

import android.content.Context;
import android.util.Log;

import com.rupeedesk.smsaautosender.model.SmsItem;
import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

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

    /**
     * Fetches pending sms documents from a collection named 'sms'.
     * This uses a blocking call for simplicity (not recommended on main thread).
     */
    public List<SmsItem> fetchPendingSms() {
        List<SmsItem> out = new ArrayList<>();
        if (db == null) return out;
        try {
            QuerySnapshot snap = db.collection("sms").whereEqualTo("sent", false).get().get();
            for (QueryDocumentSnapshot doc : snap) {
                String to = doc.getString("recipient");
                String msg = doc.getString("message");
                Long ts = doc.getLong("scheduledTime") != null ? doc.getLong("scheduledTime") : 0L;
                out.add(new SmsItem(to, msg, ts, false));
            }
        } catch (InterruptedException | ExecutionException e) {
            Log.e(TAG, "Firestore fetch failed: " + e.getMessage());
        }
        return out;
    }
}
