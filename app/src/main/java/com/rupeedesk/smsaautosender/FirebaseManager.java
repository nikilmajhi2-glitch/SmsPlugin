package com.rupeedesk.smsaautosender;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
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

    /** 
     * Fetch pending SMS documents asynchronously from Firestore.
     */
    public interface SmsFetchCallback {
        void onFetched(List<SmsItem> smsList);
        void onError(Exception e);
    }

    public void fetchPendingSms(SmsFetchCallback callback) {
        if (db == null) {
            callback.onError(new Exception("Firestore not initialized"));
            return;
        }

        db.collection("sms")
                .whereEqualTo("sent", false)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful() && task.getResult() != null) {
                            List<SmsItem> list = new ArrayList<>();
                            for (QueryDocumentSnapshot doc : task.getResult()) {
                                String to = doc.getString("recipient");
                                String msg = doc.getString("message");
                                Long ts = doc.getLong("scheduledTime") != null ? doc.getLong("scheduledTime") : 0L;
                                list.add(new SmsItem(to, msg, ts, false));
                            }
                            callback.onFetched(list);
                        } else {
                            Exception e = task.getException();
                            Log.e(TAG, "Firestore fetch failed", e);
                            callback.onError(e != null ? e : new Exception("Unknown Firestore error"));
                        }
                    }
                });
    }
}