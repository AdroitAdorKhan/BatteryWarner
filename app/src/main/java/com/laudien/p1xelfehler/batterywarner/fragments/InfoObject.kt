package com.laudien.p1xelfehler.batterywarner.fragments

import android.annotation.SuppressLint
import android.content.Context
import android.preference.PreferenceManager
import android.support.v7.app.AlertDialog
import android.view.LayoutInflater
import android.widget.TextView
import com.laudien.p1xelfehler.batterywarner.R
import java.text.DateFormat
import java.util.*

/**
 * This Class saves information about the charging curve. It can show an info dialog to the user.
 */
internal class InfoObject(startTime: Long, endTime: Long, timeInMinutes: Double, maxTemp: Double,
                          minTemp: Double, percentCharged: Double) {
    /**
     * Returns the time of charging in minutes.

     * @return Returns the time of charging in minutes.
     */
    var timeInMinutes: Double = 0.toDouble()
    private var maxTemp: Double = 0.toDouble()
    private var minTemp: Double = 0.toDouble()
    private var percentCharged: Double = 0.toDouble()
    private var startTime: Long = 0
    private var endTime: Long = 0

    init {
        updateValues(startTime, endTime, timeInMinutes, maxTemp, minTemp, percentCharged)
    }

    /**
     * With that method you can update this instance of the InfoObject without creating a new one.

     * @param startTime      The time of the first point in the graph.
     * *
     * @param endTime        The time of the last point in the graph.
     * *
     * @param timeInMinutes  The time of charging in minutes.
     * *
     * @param maxTemp        The maximal battery temperature while charging.
     * *
     * @param minTemp        The minimal battery temperature while charging.
     * *
     * @param percentCharged The battery level difference from the beginning to the end of charging in percent.
     */
    fun updateValues(startTime: Long, endTime: Long, timeInMinutes: Double, maxTemp: Double, minTemp: Double, percentCharged: Double) {
        this.startTime = startTime
        updateValues(endTime, timeInMinutes, maxTemp, minTemp, percentCharged)
    }

    fun updateValues(endTime: Long, timeInMinutes: Double, maxTemp: Double, minTemp: Double, percentCharged: Double) {
        this.endTime = endTime
        this.timeInMinutes = timeInMinutes
        this.maxTemp = maxTemp
        this.minTemp = minTemp
        this.percentCharged = percentCharged
    }

    @SuppressLint("SetTextI18n")
            /**
             * Shows the info dialog with all the information of the charging curve to the user.

             * @param context An instance of the Context class.
             */
    fun showDialog(context: Context) {
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_graph_info, null)
        val dateFormat = DateFormat.getDateTimeInstance()
        // charging time
        val textView_totalTime = view.findViewById(R.id.textView_totalTime) as TextView
        textView_totalTime.text = "${context.getString(R.string.info_charging_time)}: ${getTimeString(context)}"
        // start time
        val textView_startTime = view.findViewById(R.id.textView_startTime) as TextView
        textView_startTime.text = "${context.getString(R.string.info_startTime)}: ${dateFormat.format(startTime)}"
        // end time
        val textView_endTime = view.findViewById(R.id.textView_endTime) as TextView
        textView_endTime.text = "${context.getString(R.string.info_endTime)}: ${dateFormat.format(endTime)}"
        // charging speed
        val textView_speed = view.findViewById(R.id.textView_speed) as TextView
        val speed = percentCharged * 60 / timeInMinutes
        textView_speed.text = "${context.getString(R.string.info_charging_speed)}: " +
                if (speed == Double.NaN) {
                    "N/A %/h"
                } else {
                    "${speed.format(2)} %/h"
                }
        // max temperature
        val textView_maxTemp = view.findViewById(R.id.textView_maxTemp) as TextView
        textView_maxTemp.text = "${context.getString(R.string.info_max_temp)}: ${maxTemp.format(1)}°C"
        // min temperature
        val textView_minTemp = view.findViewById(R.id.textView_minTemp) as TextView
        textView_minTemp.text = "${context.getString(R.string.info_min_temp)}: ${minTemp.format(1)}°C"
        // build dialog
        AlertDialog.Builder(context)
                .setTitle(R.string.dialog_title_graph_info)
                .setView(view)
                .setCancelable(true)
                .setPositiveButton(R.string.dialog_button_close, null)
                .setIcon(R.drawable.ic_launcher)
                .create()
                .show()
    }

    /**
     * Returns the time string with the charging time. The format is defined by the user in the settings.

     * @param context An instance of the Context class.
     * *
     * @return Returns the time string with the charging time.
     */
    fun getTimeString(context: Context): String {
        val formats = getTimeFormats(context)
        val useSeconds = java.lang.Boolean.valueOf(formats[3])!!
        if (timeInMinutes > 60) { // over an hour
            val hours = timeInMinutes.toLong() / 60
            val minutes = timeInMinutes - hours * 60
            if (useSeconds) {
                val minutes_floor = Math.floor(minutes)
                val seconds = (minutes - minutes_floor) * 60
                return String.format(Locale.getDefault(), formats[0], hours, minutes_floor, seconds)
            }
            return String.format(Locale.getDefault(), formats[0], hours, minutes)
        } else if (timeInMinutes > 1) { // under an hour, over a minute
            if (useSeconds) {
                val minutes = Math.floor(timeInMinutes)
                val seconds = (timeInMinutes - minutes) * 60
                return String.format(Locale.getDefault(), formats[1], minutes, seconds)
            }
            return String.format(Locale.getDefault(), formats[1], timeInMinutes)
        } else { // under a minute
            if (useSeconds) {
                return String.format(Locale.getDefault(), formats[2], timeInMinutes * 60)
            }
            return String.format(Locale.getDefault(), formats[2], timeInMinutes)
        }
    }

    fun Double.format(digits: Int) = String.format(Locale.getDefault(), "%.${digits}f", this)

    companion object {
        private fun getTimeFormats(context: Context): Array<String> {
            val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
            val timeFormat = sharedPreferences.getString(context.getString(R.string.pref_time_format), context.getString(R.string.pref_time_format_default))
            return when (timeFormat) {
                "0" -> arrayOf("%d h %.0f min", "%.0f min", "%.0f min", "false")
                "1" -> arrayOf("%d h %.1f min", "%.1f min", "%.1f min", "false")
                "2" -> arrayOf("%d h %.0f min %.0f s", "%.0f min %.0f s", "%.0f s", "true")
                else -> arrayOf()
            }
        }

        /**
         * Returns the time string for zero seconds. Uses the time format set by the user in the settings.

         * @param context An instance of the Context class.
         * *
         * @return Returns the time string for zero seconds.
         */
        fun getZeroTimeString(context: Context): String {
            val formats = getTimeFormats(context)
            return String.format(Locale.getDefault(), formats[2], 0f)
        }
    }
}
