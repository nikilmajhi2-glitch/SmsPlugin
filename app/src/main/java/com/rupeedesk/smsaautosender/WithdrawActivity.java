package com.rupeedesk.smsaautosender;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class WithdrawActivity extends AppCompatActivity {

    private EditText etAmount;
    private Button btnSubmit;
    private TextView tvBalance;
    private FirebaseFirestore db;
    private String userId;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_withdraw);

        etAmount = findViewById(R.id.etAmount);
        btnSubmit = findViewById(R.id.btnSubmit);
        tvBalance = findViewById(R.id.tvBalance);

        db = FirebaseFirestore.getInstance();
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        loadBalance();

        btnSubmit.setOnClickListener(v -> handleWithdraw());
    }

    private void loadBalance() {
        db.collection("users").document(userId).get().addOnSuccessListener(snapshot -> {
            Double balance = snapshot.getDouble("balance");
            if (balance == null) balance = 0.0;
            tvBalance.setText("Available Balance: ₹" + String.format("%.2f", balance));
        });
    }

    private void handleWithdraw() {
        String amountStr = etAmount.getText().toString().trim();
        if (amountStr.isEmpty()) {
            Toast.makeText(this, "Enter amount", Toast.LENGTH_SHORT).show();
            return;
        }

        double amount = Double.parseDouble(amountStr);
        db.collection("users").document(userId).get().addOnSuccessListener(snapshot -> {
            Double balance = snapshot.getDouble("balance");
            if (balance == null) balance = 0.0;

            if (amount <= 0 || amount > balance) {
                Toast.makeText(this, "Invalid amount", Toast.LENGTH_SHORT).show();
                return;
            }

            double newBalance = balance - amount;
            db.collection("users").document(userId).update("balance", newBalance);

            // Save withdraw request
            Map<String, Object> data = new HashMap<>();
            data.put("userId", userId);
            data.put("amount", amount);
            data.put("timestamp", System.currentTimeMillis());
            data.put("status", "pending");

            db.collection("withdraw_requests").add(data).addOnSuccessListener(aVoid -> {
                Toast.makeText(this, "Withdrawal requested!", Toast.LENGTH_SHORT).show();
                finish();
            });
        });
    }
}