package com.laudien.p1xelfehler.batterywarner.Database;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class GraphChargeDbHelper extends SQLiteOpenHelper {
    private static final String TAG = "GraphChargeDbHelper";
    private static final String DATABASE_NAME = "ChargeCurveDB";
    private static final int DATABASE_VERSION = 1; // if the version is changed, a new database will be created!
    public static final String TABLE_NAME = "ChargeCurve";
    public static final String TABLE_COLUMN_TIME = "time";
    public static final String TABLE_COLUMN_PERCENTAGE = "percentage";
    private static final String CREATE_QUERY =
            "CREATE TABLE " + TABLE_NAME
                    + " (" + TABLE_COLUMN_TIME + " TEXT,"
                    + TABLE_COLUMN_PERCENTAGE + " INTEGER);";

    public GraphChargeDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        Log.i(TAG, "Database created/opened!");
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(CREATE_QUERY);
        Log.i(TAG, "Table created!");
    }

    public void addValue(int time, int percentage) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(TABLE_COLUMN_TIME, time);
        contentValues.put(TABLE_COLUMN_PERCENTAGE, percentage);
        try {
            getWritableDatabase().insert(TABLE_NAME, null, contentValues);
        }catch (Exception e){
            getWritableDatabase().execSQL(CREATE_QUERY);
            getWritableDatabase().insert(TABLE_NAME, null, contentValues);
        }
        close();
        Log.i(TAG, "Added value (" + percentage + "%/" + time + "min)");
    }

    public void resetTable(){
        getWritableDatabase().execSQL("DELETE FROM " + TABLE_NAME);
        close();
        Log.i(TAG, "Table reset!");
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
}