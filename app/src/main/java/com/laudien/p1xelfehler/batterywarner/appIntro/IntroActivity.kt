package com.laudien.p1xelfehler.batterywarner.appIntro

import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.Window
import android.view.WindowManager

import com.laudien.p1xelfehler.batterywarner.AppInfoHelper
import com.laudien.p1xelfehler.batterywarner.MainActivity
import com.laudien.p1xelfehler.batterywarner.R
import com.laudien.p1xelfehler.batterywarner.helper.ToastHelper
import com.laudien.p1xelfehler.batterywarner.receivers.DischargingAlarmReceiver
import com.laudien.p1xelfehler.batterywarner.services.ChargingService
import com.laudien.p1xelfehler.batterywarner.services.DischargingService

import agency.tango.materialintroscreen.MaterialIntroActivity
import agency.tango.materialintroscreen.SlideFragmentBuilder
import android.content.Intent.ACTION_BATTERY_CHANGED

import android.os.BatteryManager.EXTRA_PLUGGED
import android.widget.Toast.LENGTH_SHORT
import com.laudien.p1xelfehler.batterywarner.AppInfoHelper.IS_PRO

/**
 * An Activity that shows the app intro. It shows a different intro for the pro and the free
 * version of the app.
 * After it finished, it starts either the ChargingService, DischargingService or triggers a
 * DischargingAlarm depending on the user settings and starts the MainActivity.
 */
class IntroActivity : MaterialIntroActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN)
        super.onCreate(savedInstanceState)
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false)
        addSlide(BatterySlide()) // first slide
        if (!IS_PRO) { // free version
            // second slide
            addSlide(SlideFragmentBuilder()
                    .backgroundColor(R.color.colorIntro2)
                    .buttonsColor(R.color.colorButtons)
                    .image(R.drawable.batteries)
                    .title(getString(R.string.intro_slide_2_title))
                    .description(getString(R.string.intro_slide_2_description))
                    .build()
            )
            // third slide
            addSlide(SlideFragmentBuilder()
                    .backgroundColor(R.color.colorIntro3)
                    .buttonsColor(R.color.colorButtons)
                    .image(R.drawable.ic_done_white_320dp)
                    .title(getString(R.string.intro_slide_3_title))
                    .description(getString(R.string.intro_slide_3_description))
                    .build()
            )
        } else {
            // uninstall the free app if it is installed
            try {
                packageManager.getPackageInfo(AppInfoHelper.PACKAGE_NAME_FREE, PackageManager.GET_ACTIVITIES)
                addSlide(UninstallSlide())
            } catch (e: PackageManager.NameNotFoundException) { // one of the apps is not installed
            }

        }
        // preference slide
        addSlide(PreferencesSlide())
    }

    override fun onFinish() {
        super.onFinish()
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        sharedPreferences.edit().putBoolean(getString(R.string.pref_first_start), false).apply()
        val batteryStatus = registerReceiver(null, IntentFilter(ACTION_BATTERY_CHANGED))
        val isCharging = batteryStatus.getIntExtra(EXTRA_PLUGGED, -1) != 0
        val dischargingServiceEnabled = sharedPreferences.getBoolean(getString(R.string.pref_discharging_service_enabled), resources.getBoolean(R.bool.pref_discharging_service_enabled_default))
        val infoNotificationEnabled = sharedPreferences.getBoolean(getString(R.string.pref_info_notification_enabled), resources.getBoolean(R.bool.pref_info_notification_enabled_default))
        val warningLowEnabled = sharedPreferences.getBoolean(getString(R.string.pref_warning_low_enabled), resources.getBoolean(R.bool.pref_warning_low_enabled_default))
        if (dischargingServiceEnabled || infoNotificationEnabled) {
            startService(Intent(this, DischargingService::class.java))
        } else if (warningLowEnabled) {
            DischargingAlarmReceiver.cancelDischargingAlarm(this)
            sendBroadcast(Intent(this, DischargingAlarmReceiver::class.java))
        }
        if (isCharging) { // charging -> start ChargingService
            startService(Intent(this, ChargingService::class.java))
        }
        ToastHelper.sendToast(applicationContext, R.string.intro_finish_toast, LENGTH_SHORT)
        startActivity(Intent(this, MainActivity::class.java))
    }
}
