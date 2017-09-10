package com.laudien.p1xelfehler.batterywarner.services;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;

import com.laudien.p1xelfehler.batterywarner.R;
import com.laudien.p1xelfehler.batterywarner.helper.ServiceHelper;

/**
 * An IntentService called by the notification that enables the charging again.
 * If the device is not rooted anymore, the notification with the id
 * ID_NOT_ROOTED will be triggered.
 * It stops itself after it finished (like every IntentService does!).
 */
public class ResumeChargingButtonService extends IntentService {
    /**
     * 'Enable Usb Charging' clicked.
     */
    public static final String ACTION_ENABLE_USB_CHARGING = "enableUsbCharging";
    /**
     * 'Resume Charging' clicked.
     */
    public static final String ACTION_RESUME_CHARGING = "resumeCharging";

    public ResumeChargingButtonService() {
        super(null);
    }

    public ResumeChargingButtonService(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        if (intent != null && intent.getAction() != null) {
            Intent backgroundServiceIntent = new Intent(getApplicationContext(), BackgroundService.class);
            switch (intent.getAction()) {
                case ACTION_ENABLE_USB_CHARGING: // 'Enable USB charging' button
                    // change 'USB charging disabled' to false
                    SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                    sharedPreferences.edit()
                            .putBoolean(getString(R.string.pref_usb_charging_disabled), false)
                            .apply();
                case ACTION_RESUME_CHARGING: // 'Resume charging' button
                    backgroundServiceIntent.setAction(BackgroundService.ACTION_ENABLE_CHARGING);
                    break;
                default:
                    throw new RuntimeException("Unknown action!");
            }
            // resume charging using Background service
            ServiceHelper.startService(getApplicationContext(), backgroundServiceIntent);
        }
    }
}
