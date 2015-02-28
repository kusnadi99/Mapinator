package com.ceid.sespiros.mapinator;

import android.app.DialogFragment;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.JsonObjectParser;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.Key;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.prefs.Preferences;

import static com.google.android.gms.maps.model.BitmapDescriptorFactory.*;
public class MainActivity extends FragmentActivity
        implements directionInfo.DirectionDialogListener {

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    private DialogFragment editDialog;
    private DialogFragment dialog;
    private DialogFragment directionDialog;
    directionInfo.DirectionsResult directionsResult;
    MarkerDbHelper mDbHelper;
    SQLiteDatabase db;
    boolean addEnabled = false;
    LocationManager mLocationManager;
    Location mLocation;
    String mAddress;
    EditText edit;

    private float[] markerColours = {HUE_RED,HUE_GREEN,HUE_BLUE,HUE_AZURE};

    HttpResponse httpResponse;

    static final HttpTransport HTTP_TRANSPORT = AndroidHttp.newCompatibleTransport();
    static final JsonFactory JSON_FACTORY = new JacksonFactory();
    HttpRequestFactory requestFactory;
    SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Get a handle on the database maybe needed in resume etc
        mDbHelper = new MarkerDbHelper(this);
        db = mDbHelper.getReadableDatabase();

        setUpMapIfNeeded();

        mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 30,
                100, mLocationListener);

        requestFactory = HTTP_TRANSPORT.createRequestFactory(new HttpRequestInitializer() {
            @Override
            public void initialize(HttpRequest request) {
                request.setParser(new JsonObjectParser(JSON_FACTORY));
            }
        });

        prefs = PreferenceManager.getDefaultSharedPreferences(this);

    }

    // The dialog fragment receives a reference to this Activity through the
    // Fragment.onAttach() callback, which it uses to call the following methods
    // defined by the directionInfo.DirectionDialogListener interface
    @Override
    public void onDialogLocationClick(DialogFragment dialog, EditText edit, ImageButton btn) {
        // Pass the editText to global in order to change text in async getaddresstask
        this.edit = edit;
        // User touched the dialog's positive button
        if (edit.getText().toString().isEmpty()) {
            if (mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                mLocation = mLocationManager.getLastKnownLocation(mLocationManager.getAllProviders().get(0));
                getAddress(btn, mLocation);
            } else {
                Toast toast = Toast.makeText(getApplicationContext(), "Turn on Location", Toast.LENGTH_LONG);
                toast.show();
            }
        } else {
            mAddress = edit.getText().toString();
            getLocation(btn, mAddress);
        }
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
            case R.id.action_recv:
                directionDialog = directionInfo.newInstance();
                directionDialog.show(getFragmentManager(), "edit");
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
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMapToolbarEnabled(false);
        }
    }

    /**
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
                .title(title).snippet(desc + "\nClick to edit")
                .icon(BitmapDescriptorFactory.defaultMarker(markerColours[category.intValue()])));

        info.showInfoWindow();
    }

    private class GetLocationTask extends
            AsyncTask<String, Void, Address> {
        Context mContext;

        public GetLocationTask(Context context) {
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
        protected Address doInBackground(String... params) {
            Geocoder geocoder = new Geocoder(mContext, Locale.getDefault());
            // Get the current location from the input parameter list
            String addr = params[0];
            Address address = new Address(Locale.ENGLISH);
            // Create a list to contain the result address
            List<Address> addresses = null;
            try {
                /*
                 * Return 1 address.
                 */
                addresses = geocoder.getFromLocationName(addr, 1);
            } catch (IOException e1) {
                Log.e("LocationSampleActivity",
                        "IO Exception in getFromLocationName()");
                e1.printStackTrace();
            } catch (IllegalArgumentException e2) {
                // Error message to post in the log
                String errorString = "Illegal arguments " + addr
                        + " passed to address service";
                Log.e("LocationSampleActivity", errorString);
                e2.printStackTrace();
            }
            // If the reverse geocode returned an address
            if (addresses != null && addresses.size() > 0) {
                // Get the first address
                address = addresses.get(0);
                return address;
            } else
                return address;
        }
        /**
         * A method that's called once doInBackground() completes. Turn
         * off the indeterminate activity indicator and set
         * the text of the UI element that shows the address. If the
         * lookup failed, display the error message.
         */
        @Override
        protected void onPostExecute(Address address) {
            // Display the results of the lookup.
            //searchTweets(address);
        }
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
            if (edit != null)
                edit.setText(address);
        }
    }

    public void getLocation(View v, String mAddress) {
        /*
         * Reverse geocoding is long-running and synchronous.
         * Run it on a background thread.
         * Pass the current location to the background task.
         * When the task finishes,
         * onPostExecute() displays the address.
        */
        (new GetLocationTask(this)).execute(mAddress);
    }

    /**
     * The "Get Address" button in the UI is defined with
     * android:onClick="getAddress". The method is invoked whenever the
     * user clicks the button.
     *
     * @param v The view object associated with this method,
     * in this case a Button.
     */
    public void getAddress(View v, Location mLocation) {
        // Ensure that a Geocoder services is available
        if (Build.VERSION.SDK_INT >=
                Build.VERSION_CODES.GINGERBREAD
                &&
                Geocoder.isPresent()) {
            /*
             * Reverse geocoding is long-running and synchronous.
             * Run it on a background thread.
             * Pass the current location to the background task.
             * When the task finishes,
             * onPostExecute() displays the address.
             */
            LatLng latlng = new LatLng(mLocation.getLatitude(), mLocation.getLongitude());
            (new GetAddressTask(this)).execute(latlng);
        }
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


    private final LocationListener mLocationListener = new LocationListener() {

        @Override
        public void onLocationChanged(final Location location) {
            mLocation = location;
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    };

    private class DirectionsFetcher extends AsyncTask<String, Integer, String> {
        protected String doInBackground(String... urls) {
            try {
                HttpRequestFactory requestFactory = HTTP_TRANSPORT.createRequestFactory(new HttpRequestInitializer() {
                    @Override
                    public void initialize(HttpRequest request) {
                        request.setParser(new JsonObjectParser(JSON_FACTORY));
                    }
                });

                GenericUrl url = new GenericUrl("https://maps.googleapis.com/maps/api/directions/json");
                url.put("origin", urls[0]);
                url.put("destination", urls[1]);
                url.put("mode", urls[2]);
                url.put("key", getResources().getText(R.string.google_maps_key));

                url.put("language", prefs.getString("lang", "en"));
                url.put("units", prefs.getString("units", "km"));

                HttpRequest request = requestFactory.buildGetRequest(url);
                httpResponse = request.execute();
                directionsResult = httpResponse.parseAs(directionInfo.DirectionsResult.class);

            } catch (Exception ex) {
                ex.printStackTrace();
            }
            return null;
        }

        protected void onProgressUpdate(Integer... progress) {
        }

        protected void onPostExecute(String result) {
            directionInfo.Route route = directionsResult.routes.get(0);
            List<directionInfo.Leg> legs = route.legs;
            directionInfo.Leg leg;
            ArrayList<String> instructions = new ArrayList<String>();
            ArrayList<LatLng> latLngs = new ArrayList<LatLng>();

            Iterator<directionInfo.Leg> LegIterator = legs.iterator();
            while (LegIterator.hasNext()) {
                leg = LegIterator.next();
                Iterator<directionInfo.Step> StepIterator = leg.steps.iterator();
                while (StepIterator.hasNext()) {
                    directionInfo.Step step = StepIterator.next();
                    instructions.add(step.instruction);
                    instructions.add("");
                }
            }

            Intent intent = new Intent();
            intent.setClass(MainActivity.this, DirectionsActivity.class);

            String encodedPoints = route.overviewPolyLine.points;
            intent.putExtra("encodedPoints", encodedPoints);
            intent.putExtra("instructions", instructions);
            intent.putExtra("latLngs", latLngs);

            startActivityForResult(intent, 0);
        }
    }

    public void getDirections(String origin, String destination, String mode) {
        (new DirectionsFetcher()).execute(origin, destination, mode);
    }
}
