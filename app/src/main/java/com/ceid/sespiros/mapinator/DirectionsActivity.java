package com.ceid.sespiros.mapinator;

import android.graphics.Color;
import android.graphics.Path;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.PolyUtil;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class DirectionsActivity extends FragmentActivity {

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_directions);
        setUpMapIfNeeded();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
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
        String encodedPoints = (String)getIntent().getStringExtra("encodedPoints");
        ArrayList<String> instructions = (ArrayList<String>)getIntent().getStringArrayListExtra("instructions");
        List<LatLng> latLngs = PolyUtil.decode(encodedPoints);
        int step = 0, skip = 0;

        //map options
        mMap.getUiSettings().setMapToolbarEnabled(false);
        //mMap.getUiSettings().setRotateGesturesEnabled(true);

        // Instantiates a new Polyline object and adds points to define a rectangle
        PolylineOptions rectOptions = new PolylineOptions()
                .color(Color.CYAN)
                .geodesic(true);
        LatLng latlng = null;
        String instr = null;

        Iterator<LatLng> latLngIterator = latLngs.iterator();
        Iterator<String> instrIterator = instructions.iterator();
        Log.d("SIZE of latlngs", Integer.toString(latLngs.size()));
        Log.d("SIZE of instructions", Integer.toString(latLngs.size()));
        while (latLngIterator.hasNext() && instrIterator.hasNext()) {
            latlng = latLngIterator.next();
            instr = instrIterator.next();
            if (skip%2 == 0) {
                mMap.addMarker(new MarkerOptions().position(latlng).title("Step " + Integer.toString(step)).snippet(Html.fromHtml(instr).toString()));
                step++;
            }
            rectOptions.add(latlng);
            skip++;
        }
        mMap.addMarker(new MarkerOptions().position(latlng).title("Step " + Integer.toString(step)).snippet("End"));

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLngs.get(0), 17));
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(latLngs.get(0))      // Sets the center of the map to Mountain View
                .zoom(17)                   // Sets the zoom
                .bearing(90)                // Sets the orientation of the camera to east
                .tilt(30)                   // Sets the tilt of the camera to 30 degrees
                .build();                   // Creates a CameraPosition from the builder
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

        // Get back the mutable Polyline
        Polyline polyline = mMap.addPolyline(rectOptions);
    }
}
