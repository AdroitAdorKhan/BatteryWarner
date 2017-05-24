package com.laudien.p1xelfehler.batterywarner

import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.Menu
import com.laudien.p1xelfehler.batterywarner.AppInfoHelper.IS_PRO

/**
 * Super class for all activities in the app. It applies the theme,
 * initializes the toolbar and sets its title.
 */
abstract class BaseActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        if (sharedPreferences.getBoolean(getString(R.string.pref_dark_theme_enabled), resources.getBoolean(R.bool.pref_dark_theme_enabled_default))) {
            setTheme(R.style.DarkTheme)
        }
    }

    /**
     * Sets the toolbar title to the proper app name depending on if it is the pro version or not.
     */
    protected fun setToolbarTitle() {
        val toolbar = findViewById(R.id.toolbar) as Toolbar
        toolbar.title = getString(R.string.app_name) + if (IS_PRO) " Pro" else ""
        setSupportActionBar(toolbar)
    }

    /**
     * Method to easily change the title of the toolbar.

     * @param title Title to apply to the toolbar.
     */
    protected fun setToolbarTitle(title: String) {
        val toolbar = findViewById(R.id.toolbar) as Toolbar
        toolbar.title = title
        setSupportActionBar(toolbar)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        if (parentActivityIntent != null) {
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
        }
        return super.onCreateOptionsMenu(menu)
    }
}
