package com.laudien.p1xelfehler.batterywarner.fragments;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.laudien.p1xelfehler.batterywarner.HistoryActivity;
import com.laudien.p1xelfehler.batterywarner.R;
import com.laudien.p1xelfehler.batterywarner.data.GraphContract;
import com.laudien.p1xelfehler.batterywarner.helper.ToastHelper;
import com.laudien.p1xelfehler.batterywarner.services.ChargingService;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.util.Locale;

import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;
import static android.os.BatteryManager.EXTRA_PLUGGED;
import static android.support.annotation.Dimension.SP;
import static android.widget.Toast.LENGTH_SHORT;
import static com.laudien.p1xelfehler.batterywarner.AppInfoHelper.IS_PRO;
import static com.laudien.p1xelfehler.batterywarner.data.GraphContract.DATABASE_HISTORY_PATH;
import static com.laudien.p1xelfehler.batterywarner.data.GraphDbHelper.DATABASE_NAME;
import static java.text.DateFormat.SHORT;

/**
 * A Fragment that shows the latest charging curve.
 * It loads the graphs from the database in the app directory and registers a ContentObserver
 * to refresh automatically with the latest data.
 */
public class GraphFragment extends BasicGraphFragment {

    private static final int REQUEST_SAVE_GRAPH = 10;
    private SharedPreferences sharedPreferences;
    private final BroadcastReceiver chargingStateChangedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            setTimeText();
        }
    };
    private boolean graphEnabled;
    private ContentObserver mContentObserver;

    /**
     * Saves the graph in the app directory to the database directory in the external storage.
     * Can only run outside of the main/ui thread!
     *
     * @param context An instance of the Context class.
     * @return Returns true if the saving process was successful, false if not.
     */
    public static boolean saveGraph(Context context) {
        final String TAG = "GraphSaver";
        Log.d(TAG, "Saving graph...");
        if (IS_PRO) { // pro version
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
            boolean graphEnabled = sharedPreferences.getBoolean(context.getString(R.string.pref_graph_enabled), context.getResources().getBoolean(R.bool.pref_graph_enabled_default));
            if (graphEnabled) { // graph is enabled in settings
                if (ContextCompat.checkSelfPermission(context, WRITE_EXTERNAL_STORAGE) == PERMISSION_GRANTED) {
                    ContentResolver contentResolver = context.getContentResolver();
                    Cursor mCursor = contentResolver.query(
                            GraphContract.GraphEntry.URI_CURRENT_GRAPH,
                            null,
                            null,
                            null,
                            GraphContract.GraphEntry.COLUMN_TIME
                    );
                    if (mCursor != null && mCursor.getCount() > 1) {
                        mCursor.moveToLast();
                        long endTime = mCursor.getLong(mCursor.getColumnIndex(GraphContract.GraphEntry.COLUMN_TIME));
                        mCursor.close();
                        String outputFileDir = String.format(
                                Locale.getDefault(),
                                "%s/%s",
                                DATABASE_HISTORY_PATH,
                                DateFormat.getDateInstance(SHORT)
                                        .format(endTime)
                                        .replace("/", "_")
                        );
                        // rename the file if it already exists
                        File outputFile = new File(outputFileDir);
                        String baseFileDir = outputFileDir;
                        for (byte i = 1; outputFile.exists() && i < 127; i++) {
                            outputFileDir = baseFileDir + " (" + i + ")";
                            outputFile = new File(outputFileDir);
                        }
                        File inputFile = context.getDatabasePath(DATABASE_NAME);
                        try {
                            File directory = new File(DATABASE_HISTORY_PATH);
                            if (!directory.exists()) {
                                if (!directory.mkdirs()) {
                                    return false;
                                }
                            }
                            FileInputStream inputStream = new FileInputStream(inputFile);
                            FileOutputStream outputStream = new FileOutputStream(outputFile, false);
                            byte[] buffer = new byte[1024];
                            while (inputStream.read(buffer) != -1) {
                                outputStream.write(buffer);
                            }
                            outputStream.flush();
                            outputStream.close();
                            inputStream.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                            return false;
                        }
                        Log.d(TAG, "Graph saved!");
                        return true;
                    } else { // no or not enough data
                        Log.d(TAG, "No or not enough data!");
                        return false;
                    }
                } else { // storage permission not granted
                    Log.d(TAG, "Storage permission not granted! Disable auto save...");
                    PreferenceManager.getDefaultSharedPreferences(context).edit()
                            .putBoolean(context.getString(R.string.pref_graph_autosave), false)
                            .apply();
                    return false;
                }
            } else { // graph disabled
                Log.d(TAG, "Graphs are disabled in the settings!");
                return false;
            }
        } else { // free version of the app
            Log.d(TAG, "Not the pro version of the app!");
            return false;
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable final ViewGroup container, @Nullable Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        graphEnabled = sharedPreferences.getBoolean(getString(R.string.pref_graph_enabled), getResources().getBoolean(R.bool.pref_graph_enabled_default));
        View view = super.onCreateView(inflater, container, savedInstanceState);
        if (IS_PRO) {
            if (graphEnabled) {
                switch_percentage.setChecked(
                        sharedPreferences.getBoolean(getString(R.string.pref_checkBox_percent), getResources().getBoolean(R.bool.pref_checkBox_percent_default))
                );
                switch_temp.setChecked(
                        sharedPreferences.getBoolean(getString(R.string.pref_checkBox_temperature), getResources().getBoolean(R.bool.pref_checkBox_temperature_default))
                );
                // register ContentObserver
                mContentObserver = new ContentObserver(new Handler()) {
                    @Override
                    public void onChange(boolean selfChange) {
                        super.onChange(selfChange);
                        graphView.removeAllSeries();
                        loadGraphs();
                    }
                };
                ContentResolver contentResolver = getContext().getContentResolver();
                contentResolver.registerContentObserver(getUri(), false, mContentObserver);
            } else { // graph disabled
                setBigText(getString(R.string.toast_disabled_in_settings), true);
            }
        } else { // free version
            setBigText(getString(R.string.toast_not_pro), true);
        }
        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mContentObserver != null) {
            getContext().getContentResolver().unregisterContentObserver(mContentObserver);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (IS_PRO && graphEnabled) {
            getContext().registerReceiver(chargingStateChangedReceiver, new IntentFilter("android.intent.action.ACTION_POWER_DISCONNECTED"));
            getContext().registerReceiver(chargingStateChangedReceiver, new IntentFilter("android.intent.action.ACTION_POWER_CONNECTED"));
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (graphEnabled) {
            sharedPreferences.edit()
                    .putBoolean(getString(R.string.pref_checkBox_percent), switch_percentage.isChecked())
                    .putBoolean(getString(R.string.pref_checkBox_temperature), switch_temp.isChecked())
                    .apply();
            if (IS_PRO && graphEnabled) {
                getContext().unregisterReceiver(chargingStateChangedReceiver);
            }
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.reload_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (!IS_PRO && id != R.id.menu_open_history && id != R.id.menu_settings) {
            ToastHelper.sendToast(getContext(), R.string.toast_not_pro_short, LENGTH_SHORT);
            return true;
        }
        switch (id) {
            case R.id.menu_reset:
                if (graphEnabled) {
                    if (graph_percentage != null && graph_temperature != null) {
                        showResetDialog();
                    } else {
                        ToastHelper.sendToast(getContext(), R.string.toast_nothing_to_delete, LENGTH_SHORT);
                    }
                } else {
                    ToastHelper.sendToast(getContext(), R.string.toast_disabled_in_settings, LENGTH_SHORT);
                }
                return true;
            case R.id.menu_open_history:
                startActivity(new Intent(getContext(), HistoryActivity.class));
                return true;
            case R.id.menu_save_to_history:
                saveGraph();
                return true;
            case R.id.menu_info:
                showInfo();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults[0] == PERMISSION_GRANTED && requestCode == REQUEST_SAVE_GRAPH) {
            saveGraph(); // restart the saving of the graph
        }
    }

    /**
     * Loads the graphs out of the database from the database file in the app directory.
     *
     * @return Returns an array of the graphs in the database or null if it is not the pro version
     * of the app.
     */
    @Override
    protected Uri getUri() {
        if (IS_PRO) {
            return GraphContract.GraphEntry.URI_CURRENT_GRAPH;
        }
        return null;
    }

    /**
     * Loads the graphs only if it is the pro version of the app. Otherwise it shows the
     * not-pro text under the GraphView.
     */
    @Override
    void loadGraphs() {
        if (IS_PRO) {
            super.loadGraphs();
        } else {
            setBigText(getString(R.string.toast_not_pro), true);
        }
    }

    /**
     * Sets the text under the GraphView depending on if the device is charging, fully charged or
     * not charging. It also shows if there is no or not enough data to show a graph.
     */
    @Override
    void setTimeText() {
        Intent batteryStatus = getContext().registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        if (batteryStatus == null) {
            return;
        }
        boolean isFull = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) == 100;
        int chargingType = batteryStatus.getIntExtra(EXTRA_PLUGGED, -1);
        boolean isCharging = chargingType != 0;
        if (isCharging) { // charging
            boolean isChargingTypeEnabled = ChargingService.isChargingTypeEnabled(getContext(), chargingType, sharedPreferences);
            if (isChargingTypeEnabled) { // charging type enabled
                if (isFull) { // fully charged
                    showDischargingText();
                } else { // not fully charged
                    boolean isDatabaseEmpty = graph_percentage == null || graph_temperature == null;
                    String timeString;
                    if (isDatabaseEmpty) {
                        timeString = InfoObject.getZeroTimeString(getContext());
                    } else {
                        timeString = infoObject.getTimeString(getContext());
                    }
                    setNormalText(String.format(Locale.getDefault(), "%s... (%s)", getString(R.string.charging), timeString));
                }
            } else { // charging type disabled
                setBigText(getString(R.string.toast_charging_type_disabled), false);
            }
        } else { // discharging
            showDischargingText();
        }
    }

    private void showDischargingText() {
        boolean isDatabaseEmpty = graph_percentage == null || graph_temperature == null;
        if (isDatabaseEmpty) { // no data yet (database is empty)
            setBigText(getString(R.string.toast_no_data), true);
        } else { // database is not empty
            boolean hasEnoughData = infoObject.getTimeInMinutes() != 0;
            if (hasEnoughData) { // enough data
                String timeString = infoObject.getTimeString(getContext());
                setNormalText(String.format(Locale.getDefault(), "%s: %s", getString(R.string.info_charging_time), timeString));
            } else { // not enough data
                setBigText(getString(R.string.toast_not_enough_data), true);
            }
        }
    }

    private void setBigText(String disableText, boolean disableCheckBoxes) {
        textView_chargingTime.setText(disableText);
        textView_chargingTime.setTextSize(SP, getResources().getInteger(R.integer.text_size_charging_text_big));
        if (disableCheckBoxes) {
            switch_temp.setEnabled(false);
            switch_percentage.setEnabled(false);
        }
    }

    private void setNormalText(String enableText) {
        textView_chargingTime.setTextSize(SP, getResources().getInteger(R.integer.text_size_charging_text_normal));
        textView_chargingTime.setText(enableText);
        switch_temp.setEnabled(true);
        switch_percentage.setEnabled(true);
    }

    private void saveGraph() {
        if (graphView.getSeries().size() > 0 && graph_percentage.getHighestValueX() > 0) {
            // check for permission
            if (ContextCompat.checkSelfPermission(getContext(), WRITE_EXTERNAL_STORAGE) == PERMISSION_GRANTED) {
                // save graph and show toast
                boolean success = saveGraph(getContext());
                ToastHelper.sendToast(getContext(), success ? R.string.toast_success_saving : R.string.toast_error_saving, LENGTH_SHORT);
            } else { // permission not granted -> ask for permission
                requestPermissions(new String[]{WRITE_EXTERNAL_STORAGE}, REQUEST_SAVE_GRAPH);
            }
        } else { // there is no graph or the graph does not have enough data
            ToastHelper.sendToast(getContext(), R.string.toast_nothing_to_save, LENGTH_SHORT);
        }
    }

    /**
     * Shows the dialog to reset the graphs, meaning that the table in the
     * app directory database will be cleared.
     */
    private void showResetDialog() {
        new AlertDialog.Builder(getContext())
                .setCancelable(true)
                .setIcon(R.mipmap.ic_launcher)
                .setTitle(R.string.dialog_title_are_you_sure)
                .setMessage(R.string.dialog_message_delete_graph)
                .setNegativeButton(R.string.dialog_button_cancel, null)
                .setPositiveButton(R.string.dialog_button_yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        ContentResolver contentResolver = getContext().getContentResolver();
                        contentResolver.delete(getUri(), null, null);
                        ToastHelper.sendToast(getContext(), R.string.toast_success_delete_graph, LENGTH_SHORT);
                    }
                }).create().show();
    }
}
