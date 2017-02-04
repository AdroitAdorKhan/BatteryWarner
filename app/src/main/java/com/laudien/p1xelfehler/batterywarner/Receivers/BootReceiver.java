package com.laudien.p1xelfehler.batterywarner.Receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.laudien.p1xelfehler.batterywarner.BatteryAlarmManager;
import com.laudien.p1xelfehler.batterywarner.R;
import com.laudien.p1xelfehler.batterywarner.Services.ChargingService;

public class BootReceiver extends BroadcastReceiver {

    //private static final String TAG = "BootReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (!intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) return;
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        if (sharedPreferences.getBoolean(context.getString(R.string.pref_first_start), true))
            return; // return if intro was not finished

        Intent batteryStatus = context.registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        if (batteryStatus == null) {
            return;
        }

        // set already notified to false
        sharedPreferences.edit().putBoolean(context.getString(R.string.pref_already_notified), false).apply();

        boolean isCharging = BatteryAlarmManager.isCharging(batteryStatus);
        BatteryAlarmManager batteryAlarmManager = BatteryAlarmManager.getInstance(context);
        if (isCharging) { // charging
            ChargingService.startService(context); // start charging service if enabled
        } else { // discharging
            batteryAlarmManager.setDischargingAlarm(context); // start discharging alarm if enabled
        }
        batteryAlarmManager.checkAndNotify(context, batteryStatus); // check battery and notify
    }
}
