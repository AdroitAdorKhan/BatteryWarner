package com.laudien.p1xelfehler.batterywarner.Activities.SettingsActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.laudien.p1xelfehler.batterywarner.BatteryAlarmManager;
import com.laudien.p1xelfehler.batterywarner.Contract;
import com.laudien.p1xelfehler.batterywarner.GraphDbHelper;
import com.laudien.p1xelfehler.batterywarner.R;
import com.laudien.p1xelfehler.batterywarner.Services.ChargingService;

import java.util.Calendar;

import static android.app.Activity.RESULT_OK;

public class SettingsFragment extends Fragment implements CompoundButton.OnCheckedChangeListener, View.OnClickListener, SeekBar.OnSeekBarChangeListener {

    public Uri sound;
    //private static final String TAG = "SettingsFragment";
    private SharedPreferences sharedPreferences;
    private CheckBox checkBox_usb, checkBox_ac, checkBox_wireless, checkBox_lowBattery,
            checkBox_highBattery, checkBox_chargeCurve;
    private SeekBar seekBar_lowBattery, seekBar_highBattery;
    private TextView textView_lowBattery, textView_highBattery;
    private Switch switch_darkTheme;

    public static Uri getNotificationSound(Context context) {
        String uri = PreferenceManager.getDefaultSharedPreferences(context)
                .getString(context.getString(R.string.pref_sound_uri), "");
        if (!uri.equals(""))
            return Uri.parse(uri); // saved URI
        else // default URI
            return RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);
        setHasOptionsMenu(true);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());

        textView_lowBattery = (TextView) view.findViewById(R.id.textView_lowBattery);
        textView_highBattery = (TextView) view.findViewById(R.id.textView_highBattery);
        seekBar_lowBattery = (SeekBar) view.findViewById(R.id.seekBar_lowBattery);
        seekBar_highBattery = (SeekBar) view.findViewById(R.id.seekBar_highBattery);
        checkBox_usb = (CheckBox) view.findViewById(R.id.checkBox_usb);
        checkBox_ac = (CheckBox) view.findViewById(R.id.checkBox_ac);
        checkBox_wireless = (CheckBox) view.findViewById(R.id.checkBox_wireless);
        checkBox_lowBattery = (CheckBox) view.findViewById(R.id.checkBox_lowBattery);
        checkBox_highBattery = (CheckBox) view.findViewById(R.id.checkBox_highBattery);
        switch_darkTheme = (Switch) view.findViewById(R.id.switch_darkTheme);

        checkBox_highBattery.setOnCheckedChangeListener(this);
        checkBox_highBattery.setChecked(sharedPreferences.getBoolean(getString(R.string.pref_warning_high_enabled), true));
        seekBar_lowBattery.setOnSeekBarChangeListener(this);
        seekBar_highBattery.setOnSeekBarChangeListener(this);
        checkBox_usb.setOnCheckedChangeListener(this);
        checkBox_usb.setChecked(sharedPreferences.getBoolean(getString(R.string.pref_usb_enabled), true));
        checkBox_ac.setOnCheckedChangeListener(this);
        checkBox_ac.setChecked(sharedPreferences.getBoolean(getString(R.string.pref_ac_enabled), true));
        checkBox_wireless.setOnCheckedChangeListener(this);
        checkBox_wireless.setChecked(sharedPreferences.getBoolean(getString(R.string.pref_wireless_enabled), true));
        checkBox_lowBattery.setOnCheckedChangeListener(this);
        checkBox_lowBattery.setChecked(sharedPreferences.getBoolean(getString(R.string.pref_warning_low_enabled), true));
        seekBar_lowBattery.setProgress(sharedPreferences.getInt(getString(R.string.pref_warning_low), Contract.DEF_WARNING_LOW));
        seekBar_highBattery.setProgress(sharedPreferences.getInt(getString(R.string.pref_warning_high), Contract.DEF_WARNING_HIGH));
        switch_darkTheme.setOnCheckedChangeListener(this);
        switch_darkTheme.setChecked(sharedPreferences.getBoolean(getString(R.string.pref_dark_theme_enabled), false));
        checkBox_chargeCurve = (CheckBox) view.findViewById(R.id.checkBox_chargeCurve);

        textView_lowBattery.setText(getString(R.string.low_battery_warning) + " " + seekBar_lowBattery.getProgress() + "%");
        textView_highBattery.setText(getString(R.string.high_battery_warning) + " " + seekBar_highBattery.getProgress() + "%");

        Button btn_sound = (Button) view.findViewById(R.id.button_sound);
        btn_sound.setOnClickListener(this);

        // notification sound
        sound = getNotificationSound(getContext());

        if (Contract.IS_PRO) {
            checkBox_chargeCurve.setOnCheckedChangeListener(this);
            checkBox_chargeCurve.setChecked(sharedPreferences.getBoolean(getString(R.string.pref_graph_enabled), true));
        } else {
            checkBox_chargeCurve.setEnabled(false);
            checkBox_chargeCurve.setChecked(false);
            TextView textView_stats = (TextView) view.findViewById(R.id.textView_stats);
            textView_stats.setText(getString(R.string.stats) + " (" + getString(R.string.pro_only_short) + ")");
        }

        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.settings_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_done:
                saveAll();
                Toast.makeText(getActivity().getApplicationContext(), getString(R.string.settings_saved), Toast.LENGTH_SHORT).show();
                getActivity().onNavigateUp();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
        switch (compoundButton.getId()) {
            case R.id.checkBox_lowBattery:
                seekBar_lowBattery.setEnabled(checked);
                break;
            case R.id.checkBox_highBattery:
                checkBox_ac.setEnabled(checked);
                checkBox_usb.setEnabled(checked);
                checkBox_wireless.setEnabled(checked);
                checkBox_ac.setChecked(checked);
                checkBox_usb.setChecked(checked);
                checkBox_wireless.setChecked(checked);
                seekBar_highBattery.setEnabled(checked);
                break;
        }
        if (!checkBox_ac.isChecked() && !checkBox_usb.isChecked() && !checkBox_wireless.isChecked())
            checkBox_highBattery.setChecked(false);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.button_sound:
                Intent intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, getString(R.string.btn_notification) + ":");
                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, true);
                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true);
                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_DEFAULT_URI,
                        RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_NOTIFICATION);
                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, sound);
                startActivityForResult(intent, Contract.PICK_SOUND_REQUEST);
                break;
        }
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
        int state = seekBar.getProgress();
        switch (seekBar.getId()) {
            case R.id.seekBar_lowBattery:
                if (state > Contract.WARNING_LOW_MAX)
                    seekBar.setProgress(Contract.WARNING_LOW_MAX);
                else if (state < Contract.WARNING_LOW_MIN)
                    seekBar.setProgress(Contract.WARNING_LOW_MIN);
                else
                    textView_lowBattery.setText(getString(R.string.low_battery_warning) + " " + state + "%");
                break;
            case R.id.seekBar_highBattery:
                if (state < Contract.WARNING_HIGH_MIN)
                    seekBar.setProgress(Contract.WARNING_HIGH_MIN);
                else
                    textView_highBattery.setText(getString(R.string.high_battery_warning) + " " + state + "%");
                break;
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        /*String logText = null;
        switch (seekBar.getId()) {
            case R.id.seekBar_lowBattery:
                logText = "Low Battery percentage changed to " + seekBar.getProgress() + "%";
                break;
            case R.id.seekBar_highBattery:
                logText = "High Battery percentage changed to " + seekBar.getProgress() + "%";
                break;
        }
        if (logText != null)
            Log.i(TAG, logText);*/
    }

    public void saveAll() {
        // reset graph database if it was checked/unchecked
        if (checkBox_chargeCurve.isChecked() != sharedPreferences.getBoolean(getString(R.string.pref_graph_enabled), true)) {
            GraphDbHelper dbHelper = GraphDbHelper.getInstance(getContext());
            dbHelper.resetTable();
            sharedPreferences.edit()
                    .putLong(getString(R.string.pref_graph_time), Calendar.getInstance().getTimeInMillis())
                    .putInt(getString(R.string.pref_last_percentage), -1)
                    .apply(); // reset time
        }

        Context context = getContext();
        BatteryAlarmManager batteryAlarmManager = BatteryAlarmManager.getInstance(context);

        // notify if warning low was changed
        if (seekBar_lowBattery.getProgress() != sharedPreferences.getInt(getString(R.string.pref_warning_low), Contract.DEF_WARNING_LOW)) {
            //batteryAlarmManager.notifyWarningLowChanged(seekBar_lowBattery.getProgress());
        }

        // notify if warning high was changed
        if (seekBar_highBattery.getProgress() != sharedPreferences.getInt(getString(R.string.pref_warning_high), Contract.DEF_WARNING_HIGH)) {
            //batteryAlarmManager.notifyWarningHighChanged(seekBar_highBattery.getProgress());
        }

        // save the settings
        sharedPreferences.edit()
                .putBoolean(getString(R.string.pref_usb_enabled), checkBox_usb.isChecked())
                .putBoolean(getString(R.string.pref_ac_enabled), checkBox_ac.isChecked())
                .putBoolean(getString(R.string.pref_wireless_enabled), checkBox_wireless.isChecked())
                .putBoolean(getString(R.string.pref_warning_low_enabled), checkBox_lowBattery.isChecked())
                .putBoolean(getString(R.string.pref_warning_high_enabled), checkBox_highBattery.isChecked())
                .putInt(getString(R.string.pref_warning_low), seekBar_lowBattery.getProgress())
                .putInt(getString(R.string.pref_warning_high), seekBar_highBattery.getProgress())
                .putString(getString(R.string.pref_sound_uri), sound.toString())
                .putBoolean(getString(R.string.pref_graph_enabled), checkBox_chargeCurve.isChecked())
                .putBoolean(getString(R.string.pref_dark_theme_enabled), switch_darkTheme.isChecked())
                .apply();

        // notify if necessary and enabled
        batteryAlarmManager.checkAndNotify(context);

        // restart discharging alarm and charging service
        batteryAlarmManager.cancelDischargingAlarm(context);
        batteryAlarmManager.setDischargingAlarm(context);
        context.startService(new Intent(context, ChargingService.class));
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK) return;
        switch (requestCode) {
            case Contract.PICK_SOUND_REQUEST: // notification sound picker
                sound = data.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
        }
    }
}