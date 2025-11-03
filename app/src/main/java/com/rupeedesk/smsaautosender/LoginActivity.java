package com.rupeedesk.smsaautosender.auth;

import com.google.firebase.firestore.DocumentSnapshot;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.firestore.FirebaseFirestore;
import com.rupeedesk.smsaautosender.HomeActivity;
import com.rupeedesk.smsaautosender.R;
import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {
    private EditText etMobile, etPin;
    private Button btnLogin;
    private FirebaseFirestore db;
    private ProgressDialog dialog;
    private String deviceId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        db = FirebaseFirestore.getInstance();
        etMobile = findViewById(R.id.etMobile);
        etPin = findViewById(R.id.etPin);
        btnLogin = findViewById(R.id.btnLogin);
        dialog = new ProgressDialog(this);
        dialog.setMessage("Please wait...");
        deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

        btnLogin.setOnClickListener(v -> handleLogin());
    }

    private void handleLogin() {
        String mobile = etMobile.getText().toString().trim();
        String pin = etPin.getText().toString().trim();

        if (mobile.isEmpty() || pin.isEmpty()) {
            Toast.makeText(this, "Enter mobile and PIN", Toast.LENGTH_SHORT).show();
            return;
        }

        dialog.show();
        db.collection("users")
                .whereEqualTo("mobile", mobile)
                .get()
                .addOnSuccessListener(query -> {
                    if (query.isEmpty()) {
                        // ✅ Register new user
                        Map<String, Object> user = new HashMap<>();
                        user.put("mobile", mobile);
                        user.put("pin", pin);
                        user.put("deviceId", deviceId);
                        user.put("balance", 0.0);
                        user.put("createdAt", System.currentTimeMillis());

                        db.collection("users")
                                .add(user)
                                .addOnSuccessListener(doc -> {
                                    saveUserId(doc.getId());
                                    goToMain();
                                });
                    } else {
                        // ✅ Existing user
                        DocumentSnapshot doc = query.getDocuments().get(0);
                        String savedPin = doc.getString("pin");
                        String savedDevice = doc.getString("deviceId");

                        if (!pin.equals(savedPin)) {
                            Toast.makeText(this, "Wrong PIN!", Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                        } else if (!savedDevice.equals(deviceId)) {
                            Toast.makeText(this, "This account is locked to another device!", Toast.LENGTH_LONG).show();
                            dialog.dismiss();
                        } else {
                            saveUserId(doc.getId());
                            goToMain();
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    dialog.dismiss();
                    Toast.makeText(this, "Login failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void saveUserId(String id) {
        getSharedPreferences("user", Context.MODE_PRIVATE)
                .edit().putString("userId", id).apply();
    }

    private void goToMain() {
        dialog.dismiss();
        startActivity(new Intent(this, HomeActivity.class));
        finish();
    }
}