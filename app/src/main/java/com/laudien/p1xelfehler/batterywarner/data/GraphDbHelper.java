package com.laudien.p1xelfehler.batterywarner.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import static com.laudien.p1xelfehler.batterywarner.data.GraphContract.GraphEntry;

public class GraphDbHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "ChargeCurve.db";
    private static final int DATABASE_VERSION = 1;

    public GraphDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(String.format("CREATE TABLE %s (%s INTEGER,%s INTEGER,%s INTEGER);",
                GraphEntry.TABLE_NAME,
                GraphEntry.COLUMN_TIME,
                GraphEntry.COLUMN_PERCENTAGE,
                GraphEntry.COLUMN_TEMPERATURE
        ));
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + GraphEntry.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }
}
