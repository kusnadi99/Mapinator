package com.ceid.sespiros.mapinator;

import android.app.DialogFragment;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import android.support.v4.app.FragmentActivity;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import static com.google.android.gms.maps.model.BitmapDescriptorFactory.*;
public class MainActivity extends FragmentActivity {

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    private DialogFragment editDialog;
    private DialogFragment dialog;
    MarkerDbHelper mDbHelper;
    SQLiteDatabase db;
    boolean addEnabled = false;

    private float[] markerColours = {HUE_RED,HUE_GREEN,HUE_BLUE,HUE_AZURE};
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
                return true;
            case R.id.action_settings:
                Intent intent = new Intent();
                intent.setClass(MainActivity.this, SetPreferenceActivity.class);
                startActivityForResult(intent, 0);
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
                dialog = markerOptions.newInstance(marker.getPosition());
                dialog.show(getFragmentManager(), "options");
            }
        });

        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latlng) {
                getAddress(latlng);
            }
        });
    }

    public void getAddress(LatLng point) {
        /*
         * Reverse geocoding is long-running and synchronous.
         * Run it on a background thread.
         * Pass the current location to the background task.
         * When the task finishes,
         * onPostExecute() displays the address.
         */

        (new GetAddressTask(this)).execute(point);
    }

    private class GetAddressTask extends AsyncTask<LatLng, Void, String> {
        Context mContext;

        public GetAddressTask(Context context) {
            super();
            mContext = context;
        }

        /**
         * Get a Geocoder instance, get the latitude and longitude
         * look up the address, and return it
         *
         * @return A string containing the address of the current
         * location, or an empty string if no address can be found,
         * or an error message
         * @params params One or more Location objects
         */
        @Override
        protected String doInBackground(LatLng... params) {
            Geocoder geocoder = new Geocoder(mContext, Locale.getDefault());

            // Get the current location from the input parameter list
            // Create a list to contain the result address
            List<Address> addresses = null;
            try {
                addresses = geocoder.getFromLocation(params[0].latitude, params[0].longitude, 1);
            } catch (IOException e1) {
                Log.e("LocationSampleActivity", "IO Exception in getFromLocation()");
                e1.printStackTrace();
                return ("IO Exception trying to get address");
            } catch (IllegalArgumentException e2) {
                // Error message to post in the log
                String errorString = "Illegal arguments " + Double.toString(params[0].latitude) +
                        " , " + Double.toString(params[0].longitude) + " passed to address service";
                Log.e("LocationSampleActivity", errorString);
                e2.printStackTrace();
                return errorString;
            }
            // If the reverse geocode returned an address
            if (addresses != null && addresses.size() > 0) {
                // Get the first address
                Address address = addresses.get(0);
                /*
                 * Format the first line of address (if available),
                 * city, and country name.
                 */
                String addressText = String.format(
                        "%s, %s, %s",
                        address.getMaxAddressLineIndex() > 0 ? address.getAddressLine(0) : "",
                        address.getLocality(),
                        address.getCountryName());
                // Return the text
                return addressText;
            } else {
                return "No address found";
            }
        }

        @Override
        protected void onPostExecute(String address) {
            // Display the results of the lookup.
            Toast.makeText(getApplicationContext(), address, Toast.LENGTH_SHORT).show();
        }
    }

    private MarkerOptions markerToMarkerOptions(Cursor c) {
        marker mark = new marker();
        LatLng latlng = new LatLng(c.getDouble(4), c.getDouble(5));
        Log.d("DEBUG",latlng.toString());
        String title = c.getString(1);
        String description = c.getString(2);
        int category = c.getInt(3);
        String snippet = title + description + category + "Click to edit";

        mark.setTitle(title);
        mark.setDescription(description);
        mark.setCategory(category);
        mark.setCoordinates(latlng);
        mark.setCoordinates(latlng);

        return new MarkerOptions().position(latlng).title(title).snippet(snippet).icon(BitmapDescriptorFactory.defaultMarker(markerColours[category]));
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
            editDialog = markerInfo.newInstance(latlng);
            editDialog.show(getFragmentManager(), "edit");
            addEnabled = false;
        }
    }

    void editCurrentMarker(LatLng latlng) {
        editDialog = markerInfo.newInstance(latlng);
        editDialog.show(getFragmentManager(), "edit");
    }

    void deleteMarker(LatLng latlng) {
        if( mDbHelper.deleteMarker(db, latlng) > 0) {
            Toast toast = Toast.makeText(getApplicationContext(), "Marker deleted successfully", Toast.LENGTH_SHORT);
            toast.show();
            mMap.clear();
            showMarkers();
        }
    }

    void doPositiveClick(String title, String desc, Long category, LatLng latlng) {
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
                .title(title).snippet(desc+getResources().getStringArray(R.array.categories_array)[category.intValue()]+"Click to edit")
                .icon(BitmapDescriptorFactory.defaultMarker(markerColours[category.intValue()])));

        info.showInfoWindow();
    }

}
