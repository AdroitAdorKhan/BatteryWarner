package com.laudien.p1xelfehler.batterywarner

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.preference.PreferenceManager
import android.support.design.widget.TabLayout
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.support.v4.view.ViewPager
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast.LENGTH_SHORT
import com.laudien.p1xelfehler.batterywarner.appIntro.IntroActivity
import com.laudien.p1xelfehler.batterywarner.fragments.GraphFragment
import com.laudien.p1xelfehler.batterywarner.fragments.MainPageFragment
import com.laudien.p1xelfehler.batterywarner.helper.ToastHelper

/**
 * The main activity that is shown to the user after opening the app if the intro is already finished.
 * It also checks if both apps are installed and sends a broadcast or disables app functionality
 * depending on if this is the pro version or not. It tells the user to uninstall the free version.
 */
class MainActivity : BaseActivity() {
    private var backPressed = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        val firstStart = sharedPreferences.getBoolean(getString(R.string.pref_first_start), resources.getBoolean(R.bool.pref_first_start_default))
        if (firstStart) {
            startActivity(Intent(this, IntroActivity::class.java))
        }
        setContentView(R.layout.activity_main)
        setToolbarTitle()
        val viewPager = findViewById(R.id.viewPager) as ViewPager
        if (viewPager != null) { // phones only
            viewPager.adapter = ViewPagerAdapter(supportFragmentManager)
            val tabLayout = findViewById(R.id.tab_layout) as TabLayout
            tabLayout.setupWithViewPager(viewPager)
        }
    }

    override fun onBackPressed() {
        if (!backPressed) {
            ToastHelper.sendToast(this, R.string.toast_click_to_exit, LENGTH_SHORT)
            backPressed = true
            Handler().postDelayed({ backPressed = false }, 3000)
        } else {
            finishAffinity()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.settings_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.menu_settings) {
            startActivity(Intent(this, SettingsActivity::class.java))
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private inner class ViewPagerAdapter internal constructor(fm: FragmentManager) : FragmentPagerAdapter(fm) {

        override fun getCount(): Int = 2

        override fun getItem(position: Int): Fragment?
                = when (position) {
            0 -> MainPageFragment()
            1 -> GraphFragment()
            else -> null
        }

        override fun getPageTitle(position: Int): CharSequence?
                = when (position) {
            0 -> getString(R.string.title_main_page)
            1 -> getString(R.string.title_stats)
            else -> null
        }
    }
}
