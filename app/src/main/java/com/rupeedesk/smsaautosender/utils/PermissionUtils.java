package com.example.smsaautosender.utils;

import android.content.Context;
import android.content.pm.PackageManager;

import androidx.core.content.ContextCompat;

public class PermissionUtils {
    public static boolean hasSendSmsPermission(Context ctx) {
        return ContextCompat.checkSelfPermission(ctx, android.Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED;
    }
}
