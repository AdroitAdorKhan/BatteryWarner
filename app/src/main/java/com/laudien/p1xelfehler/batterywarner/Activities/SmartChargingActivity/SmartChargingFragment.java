package com.laudien.p1xelfehler.batterywarner.Activities.SmartChargingActivity;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceFragment;
import android.preference.TwoStatePreference;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.laudien.p1xelfehler.batterywarner.R;
import com.laudien.p1xelfehler.batterywarner.RootChecker;

public class SmartChargingFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.smart_charging);
        setHasOptionsMenu(true);
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.smart_charging_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_info) {
            openInfoDialog();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(getString(R.string.pref_smart_charging_enabled))) {
            final TwoStatePreference preference = (TwoStatePreference) findPreference(key);
            boolean stopChargingEnabled = sharedPreferences.getBoolean(getString(R.string.pref_stop_charging), getResources().getBoolean(R.bool.pref_stop_charging_default));
            if (!stopChargingEnabled && getActivity() != null) {
                Toast.makeText(getActivity(), "Smart Charging has to be enabled for that!", Toast.LENGTH_SHORT).show();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        preference.setChecked(false);
                    }
                }, getResources().getInteger(R.integer.root_check_switch_back_delay));
            } else {
                RootChecker.handleRootDependingPreference(getActivity(), preference);
            }
        }
    }

    private void openInfoDialog() {
        Context context = getActivity();
        if (context != null) {
            new AlertDialog.Builder(context)
                    .setTitle(R.string.information)
                    .setView(R.layout.fragment_smart_charging)
                    .setCancelable(true)
                    .setPositiveButton(R.string.close, null)
                    .setIcon(R.mipmap.ic_launcher)
                    .create()
                    .show();
        }
    }
}