package com.ceid.sespiros.mapinator;

import android.provider.BaseColumns;

/**
 * Created by sespiros on 6/12/2014.
 */
public class Marker {
    // To prevent someone from accidentally instantiating the contract class,
    // give it an empty constructor.
    public Marker() {}

    /* Inner class that defines the table contents */
    public static abstract class MarkerEntry implements BaseColumns {
        public static final String TABLE_NAME = "markers";
        public static final String COLUMN_NAME_LATITUDE = "latitude";
        public static final String COLUMN_NAME_LONGITUDE = "longitude";
    }
}
