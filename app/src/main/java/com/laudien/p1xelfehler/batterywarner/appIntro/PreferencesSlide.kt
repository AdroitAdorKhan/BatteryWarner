package com.laudien.p1xelfehler.batterywarner.appIntro

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.laudien.p1xelfehler.batterywarner.R

import agency.tango.materialintroscreen.SlideFragment

/**
 * A custom slide for the app intro that shows the SettingsFragment.
 */
class PreferencesSlide : SlideFragment() {
    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View?
            = inflater!!.inflate(R.layout.slide_preferences, container, false)

    override fun backgroundColor(): Int = R.color.colorPreferencesSlide

    override fun buttonsColor(): Int = R.color.colorButtons
}
