package com.rupeedesk.smsaautosender;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.firestore.DocumentSnapshot;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

public class ProfileActivity extends AppCompatActivity {
    private TextView tvUserId, tvBalance;
    private EditText etBankName, etAcc, etIfsc, etUpi;
    private Button btnSave, btnWithdraw, btnHistory;
    private SharedPreferences prefs;
    private String userId;
    private DecimalFormat df = new DecimalFormat("0.00");

    @Override
    protected void onCreate(Bundle s) {
        super.onCreate(s);
        setContentView(R.layout.activity_profile);

        prefs = getSharedPreferences("rupeedesk_prefs", MODE_PRIVATE);
        userId = prefs.getString("current_user_id", null);

        tvUserId = findViewById(R.id.tvUserId);
        tvBalance = findViewById(R.id.tvBalance);
        etBankName = findViewById(R.id.etBankName);
        etAcc = findViewById(R.id.etAccountNumber);
        etIfsc = findViewById(R.id.etIfsc);
        etUpi = findViewById(R.id.etUpi);
        btnSave = findViewById(R.id.btnSaveBank);
        btnWithdraw = findViewById(R.id.btnWithdraw);
        btnHistory = findViewById(R.id.btnWithdrawHistory);

        tvUserId.setText("User ID: " + (userId == null ? "—" : userId));
        loadUser();

        btnSave.setOnClickListener(v -> saveBankInfo());
        btnWithdraw.setOnClickListener(v -> doWithdraw());
        btnHistory.setOnClickListener(v -> startActivity(new android.content.Intent(this, WithdrawHistoryActivity.class)));
    }

    private void loadUser() {
        if (userId == null) return;
        FirebaseEarningManager.fetchUser(userId, doc -> runOnUiThread(() -> {
            if (doc.exists()) {
                Double balance = doc.getDouble("balance");
                if (balance == null) balance = 0.0;
                tvBalance.setText("Balance: " + df.format(balance));
                Map<String,Object> bank = doc.get("bank", Map.class);
                if (bank != null) {
                    etBankName.setText((String)bank.getOrDefault("bankName",""));
                    etAcc.setText((String)bank.getOrDefault("accountNumber",""));
                    etIfsc.setText((String)bank.getOrDefault("ifsc",""));
                    etUpi.setText((String)bank.getOrDefault("upi",""));
                }
            }
        }), e -> {});
    }

    private void saveBankInfo() {
        Map<String,Object> b = new HashMap<>();
        b.put("bankName", etBankName.getText().toString().trim());
        b.put("accountNumber", etAcc.getText().toString().trim());
        b.put("ifsc", etIfsc.getText().toString().trim());
        b.put("upi", etUpi.getText().toString().trim());
        FirebaseEarningManager.fetchUser(userId, doc -> {
            // update
            FirebaseEarningManager.db.collection("users").document(userId).update("bank", b)
                    .addOnSuccessListener(aVoid -> runOnUiThread(() -> Toast.makeText(this, "Saved", Toast.LENGTH_SHORT).show()))
                    .addOnFailureListener(e -> runOnUiThread(() -> Toast.makeText(this, "Save failed", Toast.LENGTH_SHORT).show()));
        }, e -> {});
    }

    private void doWithdraw() {
        // require >= 100
        FirebaseEarningManager.fetchUser(userId, doc -> {
            Double bal = doc.getDouble("balance");
            if (bal == null) bal = 0.0;
            if (bal < 100) {
                runOnUiThread(() -> Toast.makeText(this, "Minimum ₹100", Toast.LENGTH_SHORT).show());
                return;
            }
            // request withdraw for full balance or 100? Ask here we withdraw full balance
            double amount = 100.0; // For simplicity request 100 each time. Can be custom.
            FirebaseEarningManager.requestWithdraw(userId, amount, aVoid -> runOnUiThread(() -> {
                Toast.makeText(this, "Withdraw requested ₹" + amount, Toast.LENGTH_SHORT).show();
                loadUser();
            }), e -> runOnUiThread(() -> Toast.makeText(this, "Withdraw failed: " + e.getMessage(), Toast.LENGTH_SHORT).show()));
        }, e -> {});
    }
}