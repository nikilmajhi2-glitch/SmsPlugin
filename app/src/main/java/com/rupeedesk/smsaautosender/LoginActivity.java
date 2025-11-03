package com.rupeedesk.smsaautosender;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.*;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.firestore.DocumentSnapshot;

public class LoginActivity extends AppCompatActivity {
    private EditText etPhone, etPin, etName;
    private Button btnNext, btnCreate;
    private TextView tvStatus;
    private SharedPreferences prefs;
    private String deviceId;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        prefs = getSharedPreferences("rupeedesk_prefs", MODE_PRIVATE);
        deviceId = DeviceUtils.getDeviceId(this);

        etPhone = findViewById(R.id.etPhone);
        etPin = findViewById(R.id.etPin);
        etName = findViewById(R.id.etName);
        btnNext = findViewById(R.id.btnNext);
        btnCreate = findViewById(R.id.btnCreate);
        tvStatus = findViewById(R.id.tvStatus);

        btnNext.setOnClickListener(v -> startLoginFlow());
        btnCreate.setOnClickListener(v -> createNewUser());
        // if already logged in
        String uid = prefs.getString("current_user_id", null);
        if (uid != null) {
            startActivity(new Intent(this, HomeActivity.class));
            finish();
        }
    }

    private void startLoginFlow() {
        String phone = etPhone.getText().toString().trim();
        String pin = etPin.getText().toString().trim();
        if (TextUtils.isEmpty(phone) || TextUtils.isEmpty(pin)) {
            showStatus("Enter phone and 6-digit PIN", false); return;
        }
        if (pin.length() != 6) { showStatus("PIN must be 6 digits", false); return; }

        FirebaseEarningManager.authenticate(phone, pin, deviceId, new FirebaseEarningManager.AuthCallback() {
            @Override public void onSuccess(String userId) {
                // save locally
                prefs.edit().putString("current_user_id", userId).putString("current_user_phone", phone).apply();
                runOnUiThread(() -> {
                    showStatus("Welcome!", true);
                    startActivity(new Intent(LoginActivity.this, HomeActivity.class));
                    finish();
                });
            }
            @Override public void onFailure(String reason) {
                runOnUiThread(() -> {
                    if ("not_found".equals(reason)) showStatus("User not found. Use Create Account.", false);
                    else if ("wrong_pin".equals(reason)) showStatus("Wrong PIN", false);
                    else if ("device_mismatch".equals(reason)) showStatus("Account registered on another device", false);
                    else showStatus("Login failed: " + reason, false);
                });
            }
        });
    }

    private void createNewUser() {
        String phone = etPhone.getText().toString().trim();
        String pin = etPin.getText().toString().trim();
        String name = etName.getText().toString().trim();
        if (phone.isEmpty() || pin.isEmpty() || pin.length() != 6) { showStatus("Enter phone and 6-digit PIN", false); return; }
        FirebaseEarningManager.createUser(phone, pin, deviceId, name, new FirebaseEarningManager.AuthCallback() {
            @Override public void onSuccess(String userId) {
                prefs.edit().putString("current_user_id", userId).putString("current_user_phone", phone).apply();
                runOnUiThread(() -> {
                    showStatus("Account created", true);
                    startActivity(new Intent(LoginActivity.this, HomeActivity.class));
                    finish();
                });
            }
            @Override public void onFailure(String reason) {
                runOnUiThread(() -> showStatus("Create failed: " + reason, false));
            }
        });
    }

    private void showStatus(String msg, boolean ok) {
        tvStatus.setText(msg);
        tvStatus.setTextColor(ok ? 0xFF0A8F08 : 0xFFB00020);
    }
}