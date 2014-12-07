package com.ceid.sespiros.mapinator;

import android.app.Activity;
import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.os.Build;

import android.support.v4.app.FragmentActivity;
import android.webkit.WebView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import com.ceid.sespiros.mapinator.markerInfo;
import com.ceid.sespiros.mapinator.marker;

public class MainActivity extends FragmentActivity {

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    private MapFragment mMapFragment;
    private DialogFragment editDialog;
    private DialogFragment dialog;
    MarkerDbHelper mDbHelper;
    SQLiteDatabase db;

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
        /* if press the button */

        /*
        -------------------------------------------------
         */

        // When click spawn an edit dialog and create a new marker
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                editDialog = markerInfo.newInstance(latLng);
                editDialog.show(getFragmentManager(), "edit");

                String title = editDialog.getArguments().getString("Title");
                String desc = editDialog.getArguments().getString("Description");
                String category = editDialog.getArguments().getString("Category");

                Marker info = mMap.addMarker(new MarkerOptions().position(latLng)
                        .title(title).snippet(desc+category+"Click to edit"));

                info.showInfoWindow();
            }
        });

        mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
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

    private void showMarkers(GoogleMap mMap) {
        Cursor c;
        c = mDbHelper.getMarkers(db);

        c.moveToFirst();
        while(!c.isAfterLast()) {
            mMap.addMarker(markerToMarkerOptions(c));
            c.moveToNext();
        }
    }
}
