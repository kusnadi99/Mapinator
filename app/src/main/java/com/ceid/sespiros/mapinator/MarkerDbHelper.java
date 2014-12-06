package com.ceid.sespiros.mapinator;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.ceid.sespiros.mapinator.MarkerDbHelper;

/**
 * Created by sespiros on 6/12/2014.
 */
public class MarkerDbHelper extends SQLiteOpenHelper {
    // If you change the database schema, you must increment the database version.
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "Marker.db";
    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + Marker.MarkerEntry.TABLE_NAME + " (" +
                    Marker.MarkerEntry._ID + " INTEGER PRIMARY KEY," +
                    Marker.MarkerEntry.COLUMN_NAME_LATITUDE + " TEXT," +
                    Marker.MarkerEntry.COLUMN_NAME_LONGITUDE + " TEXT )";

    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + Marker.MarkerEntry.TABLE_NAME;

    public MarkerDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ENTRIES);
    }
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        db.execSQL(SQL_DELETE_ENTRIES);
        onCreate(db);
    }
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }
}
