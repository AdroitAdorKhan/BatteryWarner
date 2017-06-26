package com.laudien.p1xelfehler.batterywarner.helper;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.BatteryManager;
import android.support.annotation.RequiresApi;
import android.util.Log;

import com.laudien.p1xelfehler.batterywarner.R;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Locale;

import static android.content.Context.MODE_PRIVATE;
import static android.os.BatteryManager.BATTERY_HEALTH_COLD;
import static android.os.BatteryManager.BATTERY_HEALTH_DEAD;
import static android.os.BatteryManager.BATTERY_HEALTH_GOOD;
import static android.os.BatteryManager.BATTERY_HEALTH_OVERHEAT;
import static android.os.BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE;
import static android.os.BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE;
import static android.os.BatteryManager.EXTRA_HEALTH;
import static android.os.BatteryManager.EXTRA_LEVEL;
import static android.os.BatteryManager.EXTRA_PLUGGED;
import static android.os.BatteryManager.EXTRA_TECHNOLOGY;
import static android.os.BatteryManager.EXTRA_TEMPERATURE;
import static android.os.BatteryManager.EXTRA_VOLTAGE;
import static android.os.Build.VERSION.SDK_INT;
import static android.os.Build.VERSION_CODES.LOLLIPOP;

/**
 * Helper class for everything about battery status information.
 */
public class BatteryHelper {
    private static BatteryData batteryData;

    /**
     * Returns existing or creates new BatteryData object with filling in the data and returns it.
     *
     * @param batteryStatus Intent that is provided by a receiver with the action ACTION_BATTERY_CHANGED.
     * @param context       An instance of the Context class.
     * @return The singleton object of BatteryData.
     */
    public static BatteryData getBatteryData(Intent batteryStatus, Context context) {
        if (batteryData == null) {
            batteryData = new BatteryData(batteryStatus, context);
        }
        return batteryData;
    }

    private static String getHealthString(Context context, int health) {
        switch (health) {
            case BATTERY_HEALTH_COLD:
                return context.getString(R.string.health_cold);
            case BATTERY_HEALTH_DEAD:
                return context.getString(R.string.health_dead);
            case BATTERY_HEALTH_GOOD:
                return context.getString(R.string.health_good);
            case BATTERY_HEALTH_OVER_VOLTAGE:
                return context.getString(R.string.health_overvoltage);
            case BATTERY_HEALTH_OVERHEAT:
                return context.getString(R.string.health_overheat);
            case BATTERY_HEALTH_UNSPECIFIED_FAILURE:
                return context.getString(R.string.health_unspecified_failure);
            default:
                return context.getString(R.string.health_unknown);
        }
    }

    /**
     * Reads the information if the device is currently charging out of the given intent.
     *
     * @param batteryStatus Intent that is provided by a receiver with the action ACTION_BATTERY_CHANGED.
     * @return Returns true if the device is charging, false if not.
     */
    public static boolean isCharging(Intent batteryStatus) {
        return batteryStatus.getIntExtra(EXTRA_PLUGGED, -1) != 0;
    }

    /**
     * Reads the temperature out of the given intent and calculates it to the correct format.
     *
     * @param batteryStatus Intent that is provided by a receiver with the action ACTION_BATTERY_CHANGED.
     * @return Returns the temperature in the correct format as double.
     */
    private static double getTemperature(Intent batteryStatus) {
        return (double) batteryStatus.getIntExtra(EXTRA_TEMPERATURE, -1) / 10;
    }

    /**
     * Reads the voltage out of the given intent and calculates it to the correct format.
     *
     * @param batteryStatus Intent that is provided by a receiver with the action ACTION_BATTERY_CHANGED.
     * @return Returns the voltage in the correct format as double.
     */
    private static double getVoltage(Intent batteryStatus) {
        return (double) batteryStatus.getIntExtra(EXTRA_VOLTAGE, -1) / 1000;
    }

    /**
     * Calculates the screen-on percentage with the values in the sharedPreferences.
     * The values were written by the DischargingService.
     *
     * @param context An instance of the Context class.
     * @return The battery percentage loss per hour when the screen is on.
     * Returns 0.0 if there is not enough data yet.
     */
    private static double getScreenOn(Context context) {
        SharedPreferences temporaryPrefs = context.getSharedPreferences(context.getString(R.string.prefs_temporary), MODE_PRIVATE);
        long screenOnTime = temporaryPrefs.getLong(context.getString(R.string.value_time_screen_on), 0);
        int screenOnDrain = temporaryPrefs.getInt(context.getString(R.string.value_drain_screen_on), 0);
        double screenOnTimeInHours = (double) screenOnTime / 3600000;
        double screenOnPercentPerHour = screenOnDrain / screenOnTimeInHours;
        if (screenOnPercentPerHour != 0.0 && !Double.isInfinite(screenOnPercentPerHour) && !Double.isNaN(screenOnPercentPerHour)) {
            return screenOnDrain / screenOnTimeInHours;
        } else {
            return 0.0;
        }
    }

    /**
     * Calculates the screen-off percentage with the values in the sharedPreferences.
     * The values were written by the DischargingService.
     *
     * @param context An instance of the Context class.
     * @return The battery percentage loss per hour when the screen is off.
     * Returns 0.0 if there is not enough data yet.
     */
    private static double getScreenOff(Context context) {
        SharedPreferences temporaryPrefs = context.getSharedPreferences(context.getString(R.string.prefs_temporary), MODE_PRIVATE);
        long screenOffTime = temporaryPrefs.getLong(context.getString(R.string.value_time_screen_off), 0);
        int screenOffDrain = temporaryPrefs.getInt(context.getString(R.string.value_drain_screen_off), 0);
        double screenOffTimeInHours = (double) screenOffTime / 3600000;
        double screenOffPercentPerHour = screenOffDrain / screenOffTimeInHours;
        if (screenOffPercentPerHour != 0.0 && !Double.isInfinite(screenOffPercentPerHour) && !Double.isNaN(screenOffPercentPerHour)) {
            return screenOffPercentPerHour;
        } else {
            return 0.0;
        }
    }

    /**
     * This class holds all the data that can be shown in the BatteryInfoFragment or the info
     * notification (or else where if needed). You can set an OnBatteryValueChangedListener
     * that notifies you when the data was changed with one of the setters. It will only be called
     * if the new data is actually different from the old data. The data can be updated with the
     * update() method.
     * This class is a singleton and is provided by the BatteryHelper class only.
     */
    public static class BatteryData {

        public static final int INDEX_TECHNOLOGY = 0;
        public static final int INDEX_TEMPERATURE = 1;
        public static final int INDEX_HEALTH = 2;
        public static final int INDEX_BATTERY_LEVEL = 3;
        public static final int INDEX_VOLTAGE = 4;
        public static final int INDEX_CURRENT = 5;
        public static final int INDEX_SCREEN_ON = 6;
        public static final int INDEX_SCREEN_OFF = 7;
        private static final int NUMBER_OF_ITEMS = 8;
        private final String[] values = new String[NUMBER_OF_ITEMS];
        private String technology;
        private int health, batteryLevel;
        private long current;
        private double temperature, voltage, screenOn, screenOff;
        private ArrayList<OnBatteryValueChangedListener> listeners;

        private BatteryData(Intent batteryStatus, Context context) {
            update(batteryStatus, context);
        }

        /**
         * Updates all the data that is in the batteryStatus intent and the SharedPreferences given.
         *
         * @param batteryStatus Intent that is provided by a receiver with the action ACTION_BATTERY_CHANGED.
         * @param context       An instance of the Context class.
         */
        public void update(Intent batteryStatus, Context context) {
            setTechnology(batteryStatus.getStringExtra(EXTRA_TECHNOLOGY), context);
            setTemperature(BatteryHelper.getTemperature(batteryStatus), context);
            setHealth(batteryStatus.getIntExtra(EXTRA_HEALTH, -1), context);
            setBatteryLevel(batteryStatus.getIntExtra(EXTRA_LEVEL, -1), context);
            setVoltage(BatteryHelper.getVoltage(batteryStatus), context);
            if (SDK_INT >= LOLLIPOP) {
                BatteryManager batteryManager = (BatteryManager) context.getSystemService(Context.BATTERY_SERVICE);
                long current = batteryManager.getLongProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW);
                if (current == 0) {
                    File currentFile = getCurrentFile();
                    if (currentFile != null) {
                        try {
                            Process process = Runtime.getRuntime().exec("cat " + currentFile.getPath());
                            BufferedReader reader = new BufferedReader(
                                    new InputStreamReader(process.getInputStream()));
                            int read;
                            char[] buffer = new char[8];
                            StringBuilder output = new StringBuilder();
                            while ((read = reader.read(buffer)) > 0) {
                                output.append(buffer, 0, read);
                            }
                            reader.close();
                            // Waits for the command to finish.
                            process.waitFor();
                            String outputString = output.toString();
                            try {
                                current = Long.getLong(outputString);
                            } catch (Exception ignored) {
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    } else {
                        Log.d(getClass().getSimpleName(), "No current found!");
                    }
                }
                setCurrent(current, context);
            }
            setScreenOn(BatteryHelper.getScreenOn(context), context);
            setScreenOff(BatteryHelper.getScreenOff(context), context);
        }

        /**
         * Get all the data as String array with correct formats to show to the user.
         * Use the INDEX constants to determine which String is which.
         *
         * @return Returns all data as String array with correct formats to show to the user.
         */
        public String[] getAsArray() {
            return values;
        }

        /**
         * This method does the same as the getAsArray() method, but only returns the data that is
         * enabled to be shown in the info notification.
         * Caution: The indexes are not correct here!
         *
         * @param context           An instance of the Context class.
         * @param sharedPreferences An instance of the SharedPreferences class.
         * @return Returns enabled data as String array with correct formats to show to the user.
         */
        public String[] getEnabledOnly(Context context, SharedPreferences sharedPreferences) {
            boolean[] enabledBooleans = new boolean[NUMBER_OF_ITEMS];
            boolean measureBatteryDrainEnabled = sharedPreferences.getBoolean(context.getString(R.string.pref_measure_battery_drain), context.getResources().getBoolean(R.bool.pref_measure_battery_drain_default));
            enabledBooleans[INDEX_TECHNOLOGY] = sharedPreferences.getBoolean(context.getString(R.string.pref_info_technology), context.getResources().getBoolean(R.bool.pref_info_technology_default));
            enabledBooleans[INDEX_TEMPERATURE] = sharedPreferences.getBoolean(context.getString(R.string.pref_info_temperature), context.getResources().getBoolean(R.bool.pref_info_temperature_default));
            enabledBooleans[INDEX_HEALTH] = sharedPreferences.getBoolean(context.getString(R.string.pref_info_health), context.getResources().getBoolean(R.bool.pref_info_health_default));
            enabledBooleans[INDEX_BATTERY_LEVEL] = sharedPreferences.getBoolean(context.getString(R.string.pref_info_battery_level), context.getResources().getBoolean(R.bool.pref_info_battery_level_default));
            enabledBooleans[INDEX_VOLTAGE] = sharedPreferences.getBoolean(context.getString(R.string.pref_info_voltage), context.getResources().getBoolean(R.bool.pref_info_voltage_default));
            enabledBooleans[INDEX_CURRENT] = sharedPreferences.getBoolean(context.getString(R.string.pref_info_current), context.getResources().getBoolean(R.bool.pref_info_current_default));
            enabledBooleans[INDEX_SCREEN_ON] = measureBatteryDrainEnabled && sharedPreferences.getBoolean(context.getString(R.string.pref_info_screen_on), context.getResources().getBoolean(R.bool.pref_info_screen_on_default));
            enabledBooleans[INDEX_SCREEN_OFF] = measureBatteryDrainEnabled && sharedPreferences.getBoolean(context.getString(R.string.pref_info_screen_off), context.getResources().getBoolean(R.bool.pref_info_screen_off_default));
            // add enabled strings to array
            String[] enabledValues = new String[NUMBER_OF_ITEMS];
            byte count = 0;
            for (byte i = 0; i < NUMBER_OF_ITEMS; i++) {
                if (enabledBooleans[i]) {
                    enabledValues[i] = values[i];
                    count++;
                }
            }
            // remove null values from array
            String[] cleanedValues = new String[count];
            byte j = 0;
            for (String s : enabledValues) {
                if (s != null) {
                    cleanedValues[j++] = s;
                }
            }
            return cleanedValues;
        }

        /**
         * Get a specific value with the given index as correctly formatted String.
         *
         * @param index One of the INDEX attributes that determine which value should be returned.
         * @return Returns the value with the given index as correctly formatted String.
         */
        public String getValueString(int index) {
            return values[index];
        }

// --Commented out by Inspection START (16.06.2017 14:22):
//        /**
//         * Get the value with the given index as object.
//         *
//         * @param index One of the INDEX attributes that determine which value should be returned.
//         * @return Returns the value with the given index as object or null if there is no object with that index.
//         */
//        public Object getValue(int index) {
//            switch (index) {
//                case INDEX_TECHNOLOGY:
//                    return technology;
//                case INDEX_TEMPERATURE:
//                    return temperature;
//                case INDEX_HEALTH:
//                    return health;
//                case INDEX_BATTERY_LEVEL:
//                    return batteryLevel;
//                case INDEX_VOLTAGE:
//                    return voltage;
//                case INDEX_CURRENT:
//                    return current;
//                case INDEX_SCREEN_ON:
//                    return screenOn;
//                case INDEX_SCREEN_OFF:
//                    return screenOff;
//                default:
//                    return null;
//            }
//        }
// --Commented out by Inspection STOP (16.06.2017 14:22)

        private File getCurrentFile() {
            File f;

            // htc desire hd / desire z / inspire?
            f = new File("/sys/class/power_supply/battery/batt_current");
            if (f.exists()) {
                return f;
            }

            // nexus one cyanogenmod
            f = new File("/sys/devices/platform/ds2784-battery/getcurrent");
            if (f.exists()) {
                return f;
            }

            // sony ericsson xperia x1
            f = new File("/sys/devices/platform/i2c-adapter/i2c-0/0-0036/power_supply/ds2746-battery/current_now");
            if (f.exists()) {
                return f;
            }

            // xdandroid
            f = new File("/sys/devices/platform/i2c-adapter/i2c-0/0-0036/power_supply/battery/current_now");
            if (f.exists()) {
                return f;
            }

            // some htc devices
            f = new File("/sys/class/power_supply/battery/batt_current");
            if (f.exists())
                return f;

            // nexus one
            f = new File("/sys/class/power_supply/battery/current_now");
            if (f.exists())
                return f;

            // samsung galaxy vibrant
            f = new File("/sys/class/power_supply/battery/batt_chg_current");
            if (f.exists())
                return f;

            // sony ericsson x10
            f = new File("/sys/class/power_supply/battery/charger_current");
            if (f.exists())
                return f;

            // Nook Color
            f = new File("/sys/class/power_supply/max17042-0/current_now");
            if (f.exists())
                return f;

            return null;
        }

        private void setTechnology(String technology, Context context) {
            if (this.technology == null || !this.technology.equals(technology)) {
                this.technology = technology;
                values[INDEX_TECHNOLOGY] = context.getString(R.string.info_technology) + ": " + technology;
                notifyListeners(INDEX_TECHNOLOGY);
            }
        }

        private void setHealth(int health, Context context) {
            if (this.health != health || values[INDEX_HEALTH] == null) {
                this.health = health;
                values[INDEX_HEALTH] = context.getString(R.string.info_health) + ": " + BatteryHelper.getHealthString(context, health);
                notifyListeners(INDEX_HEALTH);
            }
        }

        private void setBatteryLevel(int batteryLevel, Context context) {
            if (this.batteryLevel != batteryLevel || values[INDEX_BATTERY_LEVEL] == null) {
                this.batteryLevel = batteryLevel;
                values[INDEX_BATTERY_LEVEL] = String.format(context.getString(R.string.info_battery_level) + ": %d%%", batteryLevel);
                notifyListeners(INDEX_BATTERY_LEVEL);
            }
        }

        public int getBatteryLevel() {
            return batteryLevel;
        }

        @RequiresApi(api = LOLLIPOP)
        private void setCurrent(long current, Context context) {
            if (this.current != current || values[INDEX_CURRENT] == null) {
                this.current = current;
                values[INDEX_CURRENT] = String.format(Locale.getDefault(), "%s: %d mA", context.getString(R.string.info_current), current / -1000);
                notifyListeners(INDEX_CURRENT);
            }
        }

        private void setTemperature(double temperature, Context context) {
            if (this.temperature != temperature || values[INDEX_TEMPERATURE] == null) {
                this.temperature = temperature;
                values[INDEX_TEMPERATURE] = String.format(Locale.getDefault(), context.getString(R.string.info_temperature) + ": %.1f °C", temperature);
                notifyListeners(INDEX_TEMPERATURE);
            }
        }

        private void setVoltage(double voltage, Context context) {
            if (this.voltage != voltage || values[INDEX_VOLTAGE] == null) {
                this.voltage = voltage;
                values[INDEX_VOLTAGE] = String.format(Locale.getDefault(), context.getString(R.string.info_voltage) + ": %.3f V", voltage);
                notifyListeners(INDEX_VOLTAGE);
            }
        }

        private void setScreenOn(double screenOn, Context context) {
            if (this.screenOn != screenOn || values[INDEX_SCREEN_ON] == null) {
                this.screenOn = screenOn;
                if (screenOn == 0.0) {
                    values[INDEX_SCREEN_ON] = String.format(Locale.getDefault(), "%s: %s %%/h", context.getString(R.string.info_screen_on), "N/A");
                } else {
                    values[INDEX_SCREEN_ON] = String.format(Locale.getDefault(), "%s: %.2f %%/h", context.getString(R.string.info_screen_on), screenOn);
                }
                notifyListeners(INDEX_SCREEN_ON);
            }
        }

        private void setScreenOff(double screenOff, Context context) {
            if (this.screenOff != screenOff || values[INDEX_SCREEN_OFF] == null) {
                this.screenOff = screenOff;
                if (screenOff == 0.0) {
                    values[INDEX_SCREEN_OFF] = String.format(Locale.getDefault(), "%s: %s %%/h", context.getString(R.string.info_screen_off), "N/A");
                } else {
                    values[INDEX_SCREEN_OFF] = String.format(Locale.getDefault(), "%s: %.2f %%/h", context.getString(R.string.info_screen_off), screenOff);
                }
                notifyListeners(INDEX_SCREEN_OFF);
            }
        }

        public void registerOnBatteryValueChangedListener(OnBatteryValueChangedListener listener) {
            if (listeners == null) {
                listeners = new ArrayList<>(1);
            }
            if (!listeners.contains(listener)) {
                listeners.add(listener);
            } else {
                Log.d(getClass().getSimpleName(), "The given listener is already in the list!");
            }
        }

        public void unregisterOnBatteryValueChangedListener(OnBatteryValueChangedListener listener) {
            if (listeners != null && listener != null && listeners.contains(listener)) {
                listeners.remove(listener);
            } else {
                Log.d(getClass().getSimpleName(), "unregisterOnBatteryValueChangedListener called without any registered listener!");
            }
        }

        private void notifyListeners(int index) {
            if (listeners != null && !listeners.isEmpty()) {
                OnBatteryValueChangedListener listener;
                for (byte i = 0; i < listeners.size(); i++) {
                    listener = listeners.get(i);
                    if (listener != null) {
                        listener.onBatteryValueChanged(index);
                    } else { // remove listener if it is null
                        listeners.remove(i);
                        Log.d(getClass().getSimpleName(), "Forgot to unregister a listener! Listener was removed from the list!");
                    }
                }
            }
        }

        public interface OnBatteryValueChangedListener {
            void onBatteryValueChanged(int index);
        }
    }
}
