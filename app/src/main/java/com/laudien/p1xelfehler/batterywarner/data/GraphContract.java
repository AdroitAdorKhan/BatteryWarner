package com.laudien.p1xelfehler.batterywarner.data;

import android.net.Uri;
import android.os.Environment;

public class GraphContract {
    public static final String AUTHORITY = "com.laudien.p1xelfehler.batterywarner.database";
    public static final String DATABASE_HISTORY_PATH = Environment.getExternalStorageDirectory() + "/BatteryWarner";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + AUTHORITY);
    public static final String PATH_CURRENT_GRAPH = "current_graph";
    public static final String PATH_GRAPH_HISTORY = "graph_history";

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
