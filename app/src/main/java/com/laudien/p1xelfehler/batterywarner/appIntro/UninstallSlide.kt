package com.laudien.p1xelfehler.batterywarner.appIntro

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button

import com.laudien.p1xelfehler.batterywarner.AppInfoHelper
import com.laudien.p1xelfehler.batterywarner.R

import agency.tango.materialintroscreen.SlideFragment

class UninstallSlide : SlideFragment() {

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater!!.inflate(R.layout.slide_uninstall, container, false)
        view.findViewById(R.id.btn_uninstall).setOnClickListener {
            val uri = Uri.parse("package:" + AppInfoHelper.PACKAGE_NAME_FREE)
            val uninstallIntent = Intent(Intent.ACTION_UNINSTALL_PACKAGE, uri)
            startActivity(uninstallIntent)
        }
        return view
    }

    override fun backgroundColor(): Int = R.color.colorIntro3

    override fun buttonsColor(): Int = R.color.colorButtons

    override fun canMoveFurther(): Boolean {
        try {
            context.packageManager.getPackageInfo(AppInfoHelper.PACKAGE_NAME_FREE, PackageManager.GET_ACTIVITIES)
            return false
        } catch (e: PackageManager.NameNotFoundException) {
            return true
        }

    }
}
