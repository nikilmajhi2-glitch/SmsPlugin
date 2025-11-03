package com.rupeedesk.smsaautosender;

import android.content.Context;
import android.provider.Settings;

public class DeviceUtils {
    public static String getDeviceId(Context ctx) {
        try {
            return Settings.Secure.getString(ctx.getContentResolver(), Settings.Secure.ANDROID_ID);
        } catch (Exception e) {
            return "unknown-device";
        }
    }
}