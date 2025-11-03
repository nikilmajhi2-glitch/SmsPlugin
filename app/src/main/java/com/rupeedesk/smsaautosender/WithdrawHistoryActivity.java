package com.rupeedesk.smsaautosender;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;

public class WithdrawHistoryActivity extends AppCompatActivity {

    private ListView listView;
    private ArrayAdapter<String> adapter;
    private ArrayList<String> withdraws = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_withdraw_history); // Ensure this matches your layout file name

        listView = findViewById(R.id.listWithdrawHistory);

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, withdraws);
        listView.setAdapter(adapter);

        loadWithdrawHistory();
    }

    private void loadWithdrawHistory() {
        String userId = getSharedPreferences("rupeedesk_prefs", MODE_PRIVATE)
                .getString("current_user_id", null);
        if (userId == null) return;

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("withdrawRequests")
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(query -> {
                    withdraws.clear();
                    for (QueryDocumentSnapshot doc : query) {
                        double amount = doc.getDouble("amount") != null ? doc.getDouble("amount") : 0.0;
                        String status = doc.getString("status");
                        long ts = doc.getLong("timestamp") != null ? doc.getLong("timestamp") : 0;
                        String date = (ts > 0) ? new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm").format(new java.util.Date(ts)) : "";
                        withdraws.add("₹" + amount + " - " + (status == null ? "Pending" : status) + "  (" + date + ")");
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    withdraws.clear();
                    withdraws.add("Error loading withdraw history");
                    adapter.notifyDataSetChanged();
                });
    }
}