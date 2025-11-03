package com.rupeedesk.smsaautosender;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.firestore.DocumentSnapshot;

public class HomeActivity extends AppCompatActivity {
    private TextView tvBalance, tvStatus;
    private Button btnStart, btnProfile, btnLogout;
    private SharedPreferences prefs;
    private String userId;

    @Override
    protected void onCreate(Bundle s) {
        super.onCreate(s);
        setContentView(R.layout.activity_home);

        prefs = getSharedPreferences("rupeedesk_prefs", MODE_PRIVATE);
        userId = prefs.getString("current_user_id", null);

        tvBalance = findViewById(R.id.tvBalance);
        tvStatus = findViewById(R.id.tvStatus);
        btnStart = findViewById(R.id.btnStart);
        btnProfile = findViewById(R.id.btnProfile);
        btnLogout = findViewById(R.id.btnLogout);

        btnProfile.setOnClickListener(v -> startActivity(new Intent(this, ProfileActivity.class)));
        btnLogout.setOnClickListener(v -> {
            prefs.edit().remove("current_user_id").remove("current_user_phone").apply();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });

        btnStart.setOnClickListener(v -> {
            Intent it = new Intent(this, SmsService.class);
            startService(it);
            tvStatus.setText("Service started — running in background");
        });

        refreshBalance();
    }

    private void refreshBalance() {
        if (userId == null) return;

        FirebaseEarningManager.fetchUser(userId, new FirebaseEarningManager.FetchCallback() {
            @Override
            public void onSuccess(DocumentSnapshot doc) {
                runOnUiThread(() -> {
                    if (doc.exists()) {
                        Double b = doc.getDouble("balance");
                        if (b == null) b = 0.0;
                        tvBalance.setText(FirebaseEarningManager.formatRupee(b));
                    }
                });
            }

            @Override
            public void onFailure(Exception e) {
                runOnUiThread(() -> tvBalance.setText("Error"));
            }
        });
    }
}