package com.laudien.p1xelfehler.batterywarner.data;

import android.content.ContentValues;
import android.net.Uri;
import android.os.Environment;

import com.laudien.p1xelfehler.batterywarner.BuildConfig;

import java.io.File;
import java.io.FileReader;

public class GraphContract {
    public static final String AUTHORITY = BuildConfig.APPLICATION_ID;
    public static final String DATABASE_HISTORY_PATH = Environment.getExternalStorageDirectory() + "/BatteryWarner";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + AUTHORITY);
    public static final String PATH_CURRENT_GRAPH = "current_graph";
    public static final String PATH_GRAPH_HISTORY = "graph_history";

    public static ContentValues buildContentValues(long time, int percentage, int temperature) {
        ContentValues contentValues = new ContentValues(3);
        contentValues.put(GraphContract.GraphEntry.COLUMN_TIME, time);
        contentValues.put(GraphContract.GraphEntry.COLUMN_PERCENTAGE, percentage);
        contentValues.put(GraphContract.GraphEntry.COLUMN_TEMPERATURE, temperature);
        return contentValues;
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static boolean isValidDatabaseFile(File file) {
        try {
            FileReader fileReader = new FileReader(file.getPath());
            char[] buffer = new char[16];
            fileReader.read(buffer, 0, 16); // read first 16 bytes
            fileReader.close();
            String string = String.valueOf(buffer);
            return string.equals("SQLite format 3\u0000");
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static final class GraphEntry {
        public static final Uri URI_CURRENT_GRAPH = BASE_CONTENT_URI.buildUpon().appendPath(PATH_CURRENT_GRAPH).build();
        public static final Uri URI_GRAPH_HISTORY = BASE_CONTENT_URI.buildUpon().appendPath(PATH_GRAPH_HISTORY).build();
        public static final String TABLE_NAME = "ChargeCurve";
        public static final String COLUMN_TIME = "time";
        public static final String COLUMN_PERCENTAGE = "percentage";
        /**
         * The temperature saved as integer. To get the temperature you need to calculate TEMP/10.
         **/
        public static final String COLUMN_TEMPERATURE = "temperature";

        /*
        ChargeCurve
         ---------------------------------
        | time | percentage | temperature |
        | 1234 | 20         | 300         |
        | 5678 | 21         | 315         |
        | 9101 | 22         | 320         |
        | ...  | ...        | ...         |
         ---------------------------------
         */
    }
}
