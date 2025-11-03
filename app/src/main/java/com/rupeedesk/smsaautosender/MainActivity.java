package com.rupeedesk.smsaautosender;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.smsaautosender.utils.PermissionUtils;
import com.google.firebase.FirebaseApp;

public class MainActivity extends AppCompatActivity {

    private TextView statusText;
    private Button startBtn;
    private ActivityResultLauncher<String[]> permissionLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        statusText = findViewById(R.id.statusText);
        startBtn = findViewById(R.id.startBtn);

        // Initialize Firebase (if google-services.json is present)
        try {
            FirebaseApp.initializeApp(this);
        } catch (Exception e) {
            // ignore - app may not have firebase configured
        }

        permissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestMultiplePermissions(),
                result -> {
                    Boolean granted = result.get(Manifest.permission.SEND_SMS);
                    if (granted != null && granted) {
                        statusText.setText("Permission granted. You can start the SMS service.");
                    } else {
                        statusText.setText("Send SMS permission denied.");
                    }
                }
        );

        startBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (PermissionUtils.hasSendSmsPermission(MainActivity.this)) {
                    startSmsService();
                } else {
                    permissionLauncher.launch(new String[]{Manifest.permission.SEND_SMS});
                }
            }
        });
    }

    private void startSmsService() {
        statusText.setText("Starting SMS service...");
        Intent i = new Intent(this, SmsService.class);
        ContextCompat.startForegroundService(this, i);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}
