package com.ceid.sespiros.mapinator;

import android.provider.BaseColumns;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by sespiros on 6/12/2014.
 */
public class marker {
    private long id;
    private String title, description;
    private int category;
    private double latitude, longitude;

    // To prevent someone from accidentally instantiating the contract class,
    // give it an empty constructor.
    public marker() {}

    /* Inner class that defines the table contents */
    public static abstract class MarkerEntry implements BaseColumns {
        public static final String TABLE_NAME = "markers";
        public static final String COLUMN_NAME_TITLE = "title";
        public static final String COLUMN_NAME_DESC = "description";
        public static final String COLUMN_NAME_CATEGORY = "category";
        public static final String COLUMN_NAME_LATITUDE = "latitude";
        public static final String COLUMN_NAME_LONGITUDE = "longitude";

    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getCategory() {
        return category;
    }

    public void setCategory(int category) {
        this.category = category;
    }

    public void setCoordinates(LatLng latlng) {
        this.latitude = latlng.latitude;
        this.longitude = latlng.longitude;
    }
}
