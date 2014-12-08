package com.ceid.sespiros.mapinator;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.ceid.sespiros.mapinator.MarkerDbHelper;
import com.ceid.sespiros.mapinator.marker;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

/**
 * Created by sespiros on 6/12/2014.
 */
public class MarkerDbHelper extends SQLiteOpenHelper {
    // If you change the database schema, you must increment the database version.
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "Mapinator.db";
    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + marker.MarkerEntry.TABLE_NAME + " (" +
                    marker.MarkerEntry._ID + " INTEGER PRIMARY KEY," +
                    marker.MarkerEntry.COLUMN_NAME_TITLE + " TEXT," +
                    marker.MarkerEntry.COLUMN_NAME_DESC + " TEXT," +
                    marker.MarkerEntry.COLUMN_NAME_CATEGORY + " TEXT," +
                    marker.MarkerEntry.COLUMN_NAME_LATITUDE + " DECIMAL(10,7)," +
                    marker.MarkerEntry.COLUMN_NAME_LONGITUDE + " DECIMAL(10,7))";

    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + marker.MarkerEntry.TABLE_NAME;

    private static final String SQL_GET_MARKERS =
            "SELECT * FROM " + marker.MarkerEntry.TABLE_NAME;

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

    public Cursor getMarkers(SQLiteDatabase db) {
        String[] allColumns = {
                marker.MarkerEntry._ID,
                marker.MarkerEntry.COLUMN_NAME_TITLE,
                marker.MarkerEntry.COLUMN_NAME_DESC,
                marker.MarkerEntry.COLUMN_NAME_CATEGORY,
                marker.MarkerEntry.COLUMN_NAME_LATITUDE,
                marker.MarkerEntry.COLUMN_NAME_LONGITUDE };

        return db.query(marker.MarkerEntry.TABLE_NAME,
                allColumns,
                null, null, null, null, null);

    }

    /* Currently same as onUpgrade */
    public void deleteAll(SQLiteDatabase db) {
        db.execSQL(SQL_DELETE_ENTRIES);
        onCreate(db);
    }

    public int deleteMarker(SQLiteDatabase db, LatLng latlng) {
        String lat = String.valueOf(latlng.latitude);
        String lng = String.valueOf(latlng.longitude);
        String whereArgs[] = new String[] {lat, lng};
        int count = db.delete(marker.MarkerEntry.TABLE_NAME,
                marker.MarkerEntry.COLUMN_NAME_LATITUDE + "=? AND " + marker.MarkerEntry.COLUMN_NAME_LONGITUDE + "=?", whereArgs);

        return count;
    }
}
