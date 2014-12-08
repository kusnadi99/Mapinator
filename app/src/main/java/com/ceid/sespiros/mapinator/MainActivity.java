package com.ceid.sespiros.mapinator;

import android.app.DialogFragment;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import android.support.v4.app.FragmentActivity;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class MainActivity extends FragmentActivity {

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    private DialogFragment editDialog;
    private DialogFragment dialog;
    MarkerDbHelper mDbHelper;
    SQLiteDatabase db;
    boolean addEnabled = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Get a handle on the database maybe needed in resume etc
        mDbHelper = new MarkerDbHelper(this);
        db = mDbHelper.getReadableDatabase();

        setUpMapIfNeeded();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        switch (id) {
            case R.id.action_showmarkers:
                showMarkers();
                return true;
            case R.id.action_add:
                addEnabled = true;
                Toast toast = Toast.makeText(getApplicationContext(), "Click to add a marker", Toast.LENGTH_LONG);
                toast.show();
                return true;
            case R.id.action_clear:
                mMap.clear();
                mDbHelper.deleteAll(db);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Sets up the map if it is possible to do so (i.e., the Google Play services APK is correctly
     * installed) and the map has not already been instantiated.. This will ensure that we only ever
     * call {@link #setUpMap()} once when {@link #mMap} is not null.
     * <p/>
     * If it isn't installed {@link SupportMapFragment} (and
     * {@link com.google.android.gms.maps.MapView MapView}) will show a prompt for the user to
     * install/update the Google Play services APK on their device.
     * <p/>
     * A user can return to this FragmentActivity after following the prompt and correctly
     * installing/updating/enabling the Google Play services. Since the FragmentActivity may not
     * have been completely destroyed during this process (it is likely that it would only be
     * stopped or paused), {@link #onCreate(Bundle)} may not be called again so we should call this
     * method in {@link #onResume()} to guarantee that it will be called.
     */
    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                setUpMap();
            }
        }
    }

    /**
     * This is where we can add markers or lines, add listeners or move the camera. In this case, we
     * just add a marker near Africa.
     * <p/>
     * This should only be called once and when we are sure that {@link #mMap} is not null.
     */
    private void setUpMap() {
        // When click spawn an edit dialog and create a new marker
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latlng) {
                editMarker(latlng);
            }
        });

        mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                dialog = markerOptions.newInstance(marker);
                dialog.show(getFragmentManager(), "options");
            }
        });
    }

    private MarkerOptions markerToMarkerOptions(Cursor c) {
        marker mark = new marker();
        LatLng latlng = new LatLng(c.getDouble(4), c.getDouble(5));
        Log.d("DEBUG",latlng.toString());
        String title = c.getString(1);
        String description = c.getString(2);
        String category = c.getString(3);
        String snippet = title + description + category + "Click to edit";

        mark.setTitle(title);
        mark.setDescription(description);
        mark.setCategory(category);
        mark.setCoordinates(latlng);

        return new MarkerOptions().position(latlng).title(title).snippet(snippet);
    }

    private void showMarkers() {
        Cursor c;
        c = mDbHelper.getMarkers(db);

        c.moveToFirst();
        while(!c.isAfterLast()) {
            mMap.addMarker(markerToMarkerOptions(c));
            c.moveToNext();
        }
    }

    void editMarker(LatLng latlng) {
        if (addEnabled) {
            editDialog = MarkerInfo.newInstance(latlng);
            editDialog.show(getFragmentManager(), "edit");
            addEnabled = false;
        }
    }

    void deleteMarker(LatLng latlng) {
        if( mDbHelper.deleteMarker(db, latlng) > 0) {
            Toast toast = Toast.makeText(getApplicationContext(), "Marker deleted successfully", Toast.LENGTH_SHORT);
            toast.show();
        }
    }

    void doPositiveClick(String title, String desc, String category, LatLng latlng) {
        // Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();
        values.put(marker.MarkerEntry.COLUMN_NAME_TITLE, title);
        values.put(marker.MarkerEntry.COLUMN_NAME_DESC, desc);
        values.put(marker.MarkerEntry.COLUMN_NAME_CATEGORY, category);
        values.put(marker.MarkerEntry.COLUMN_NAME_LATITUDE, latlng.latitude);
        values.put(marker.MarkerEntry.COLUMN_NAME_LONGITUDE, latlng.longitude);

        // Insert the new row, returning the primary key value of the new row
        long newRowId;
        newRowId = db.insert(
                marker.MarkerEntry.TABLE_NAME,
                null,
                values);

        Marker info = mMap.addMarker(new MarkerOptions().position(latlng)
                .title(title).snippet(desc+category+"Click to edit"));

        info.showInfoWindow();
    }

}
