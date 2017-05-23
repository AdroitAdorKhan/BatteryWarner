package com.laudien.p1xelfehler.batterywarner.appIntro

import agency.tango.materialintroscreen.SlideFragment
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.laudien.p1xelfehler.batterywarner.AppInfoHelper.IS_PRO
import com.laudien.p1xelfehler.batterywarner.R
import com.laudien.p1xelfehler.batterywarner.helper.ImageHelper

/**
 * A custom slide for the app intro with a battery image.
 * It uses a filter on the battery image to make it green
 * and different title and description for pro and free version of the app.
 */
class BatterySlide : SlideFragment() {

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater!!.inflate(agency.tango.materialintroscreen.R.layout.fragment_slide, container, false)
        val titleTextView = view.findViewById(R.id.txt_title_slide) as TextView
        val descriptionTextView = view.findViewById(R.id.txt_description_slide) as TextView
        // set title and description texts
        titleTextView.setText(if (IS_PRO) R.string.intro_slide_thank_you_title else R.string.intro_slide_1_title)
        descriptionTextView.setText(if (IS_PRO) R.string.intro_slide_thank_you_description else R.string.intro_slide_1_description)
        // image
        val imageView = view.findViewById(R.id.image_slide) as ImageView
        imageView.setImageResource(R.drawable.ic_battery_status_full_white_256dp)
        ImageHelper.setImageColor(context.resources.getColor(R.color.colorBatteryOk), imageView)
        imageView.visibility = VISIBLE
        return view
    }

    override fun backgroundColor(): Int = R.color.colorIntro1

    override fun buttonsColor(): Int = R.color.colorButtons
}
