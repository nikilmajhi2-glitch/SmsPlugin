package com.rupeedesk.smsaautosender.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.firestore.FirebaseFirestore;
import com.rupeedesk.smsaautosender.R;
import com.rupeedesk.smsaautosender.auth.LoginActivity;
import com.rupeedesk.smsaautosender.WithdrawActivity;
import com.rupeedesk.smsaautosender.WithdrawHistoryActivity;

public class ProfileActivity extends AppCompatActivity {
    private TextView tvMobile, tvBalance;
    private Button btnWithdraw, btnHistory, btnLogout;
    private FirebaseFirestore db;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        tvMobile = findViewById(R.id.tvMobile);
        tvBalance = findViewById(R.id.tvBalance);
        btnWithdraw = findViewById(R.id.btnWithdraw);
        btnHistory = findViewById(R.id.btnHistory);
        btnLogout = findViewById(R.id.btnLogout);
        db = FirebaseFirestore.getInstance();

        userId = getSharedPreferences("user", Context.MODE_PRIVATE).getString("userId", null);
        loadProfile();

        btnWithdraw.setOnClickListener(v -> startActivity(new Intent(this, WithdrawActivity.class)));
        btnHistory.setOnClickListener(v -> startActivity(new Intent(this, WithdrawHistoryActivity.class)));
        btnLogout.setOnClickListener(v -> logout());
    }

    private void loadProfile() {
        if (userId == null) return;
        db.collection("users").document(userId)
                .get()
                .addOnSuccessListener(doc -> {
                    tvMobile.setText("Mobile: " + doc.getString("mobile"));
                    Double bal = doc.getDouble("balance");
                    tvBalance.setText("Balance: ₹" + (bal == null ? 0 : bal));
                });
    }

    private void logout() {
        getSharedPreferences("user", Context.MODE_PRIVATE).edit().clear().apply();
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }
}