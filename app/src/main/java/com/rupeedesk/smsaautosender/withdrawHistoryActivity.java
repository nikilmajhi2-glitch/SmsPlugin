package com.rupeedesk.smsaautosender;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.firestore.*;
import java.util.ArrayList;
import java.util.List;

public class WithdrawHistoryActivity extends AppCompatActivity {
    private ListView listView;
    private ArrayAdapter<String> adapter;
    private List<String> list = new ArrayList<>();
    private String userId;

    @Override
    protected void onCreate(Bundle s) {
        super.onCreate(s);
        setContentView(R.layout.activity_withdraw_history);
        listView = findViewById(R.id.lvWithdraws);
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, list);
        listView.setAdapter(adapter);
        userId = getSharedPreferences("rupeedesk_prefs", MODE_PRIVATE).getString("current_user_id", null);
        loadHistory();
    }

    private void loadHistory() {
        FirebaseFirestore.getInstance().collection("withdrawals")
                .whereEqualTo("userId", userId)
                .orderBy("requestedAt", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(q -> {
                    list.clear();
                    for (DocumentSnapshot d : q) {
                        Double amount = d.getDouble("amount");
                        String status = d.getString("status");
                        list.add("₹" + (amount == null ? "0" : amount) + " — " + (status == null ? "pending" : status));
                    }
                    adapter.notifyDataSetChanged();
                });
    }
}