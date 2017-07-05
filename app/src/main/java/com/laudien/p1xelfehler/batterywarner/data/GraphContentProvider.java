package com.laudien.p1xelfehler.batterywarner.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public class GraphContentProvider extends ContentProvider {
    private static final int CODE_CURRENT_GRAPH = 100;
    private static final int CODE_GRAPH_HISTORY_WITH_FILE_NAME = 201;
    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private GraphDbHelper mGraphDbHelper;

    private static UriMatcher buildUriMatcher() {
        UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(GraphContract.AUTHORITY, GraphContract.PATH_CURRENT_GRAPH, CODE_CURRENT_GRAPH);
        uriMatcher.addURI(GraphContract.AUTHORITY, GraphContract.PATH_GRAPH_HISTORY + "/*", CODE_GRAPH_HISTORY_WITH_FILE_NAME);
        return uriMatcher;
    }

    @Override
    public boolean onCreate() {
        mGraphDbHelper = new GraphDbHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String orderBy) {
        SQLiteDatabase database;
        Cursor cursor;
        switch (sUriMatcher.match(uri)) {
            case CODE_CURRENT_GRAPH:
                database = mGraphDbHelper.getReadableDatabase();
                break;
            case CODE_GRAPH_HISTORY_WITH_FILE_NAME:
                String fileName = uri.getLastPathSegment();
                String databasePath = GraphContract.DATABASE_HISTORY_PATH + "/" + fileName;
                database = SQLiteDatabase.openDatabase(databasePath, null, SQLiteDatabase.OPEN_READONLY);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        // get cursor from database
        cursor = database.query(
                GraphContract.GraphEntry.TABLE_NAME,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                GraphContract.GraphEntry.COLUMN_TIME
        );
        // notify ContentObservers
        Context context = getContext();
        if (context != null) {
            cursor.setNotificationUri(context.getContentResolver(), uri);
        }
        return cursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues contentValues) {
        Uri returnUri;
        switch (sUriMatcher.match(uri)) {
            case CODE_CURRENT_GRAPH:
                SQLiteDatabase database = mGraphDbHelper.getWritableDatabase();
                long id = database.insert(
                        GraphContract.GraphEntry.TABLE_NAME,
                        null,
                        contentValues
                );
                if (id != -1) {
                    returnUri = ContentUris.withAppendedId(GraphContract.GraphEntry.URI_CURRENT_GRAPH, id);
                } else {
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                }
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        Context context = getContext();
        if (context != null) {
            context.getContentResolver().notifyChange(uri, null);
        }
        return returnUri;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        int rowsRemoved;
        switch (sUriMatcher.match(uri)) {
            case CODE_CURRENT_GRAPH:
                SQLiteDatabase database = mGraphDbHelper.getWritableDatabase();
                rowsRemoved = database.delete(
                        GraphContract.GraphEntry.TABLE_NAME,
                        null,
                        null
                );
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        if (rowsRemoved != 0) {
            Context context = getContext();
            if (context != null) {
                context.getContentResolver().notifyChange(uri, null);
            }
        }
        return rowsRemoved;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues contentValues, @Nullable String s, @Nullable String[] strings) {
        throw new UnsupportedOperationException("Graph points cannot be updated!");
    }
}
