package com.laudien.p1xelfehler.batterywarner

import android.content.pm.PackageManager
import android.os.Bundle
import android.support.v7.widget.Toolbar
import com.laudien.p1xelfehler.batterywarner.preferences.SettingsFragment

/**
 * An Activity that is the frame for the SettingsFragment. It shows the version name of the app
 * in the toolbar subtitle.
 */
class SettingsActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.frame_layout)
        val toolbar = findViewById(R.id.toolbar) as Toolbar
        toolbar.title = getString(R.string.title_preferences)
        try { // put version code in subtitle of the toolbar
            val pInfo = packageManager.getPackageInfo(packageName, 0)
            toolbar.subtitle = pInfo.versionName
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }
        setSupportActionBar(toolbar)
        // replace container layout with SettingsFragment
        fragmentManager.beginTransaction().replace(R.id.container_layout, SettingsFragment()).commit()
    }

    override fun onBackPressed() {
        onNavigateUp() // to enable the theme if changed
    }
}
