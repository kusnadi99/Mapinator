package com.ceid.sespiros.mapinator;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.widget.EditText;
import android.widget.ImageButton;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

/**
 * Created by sespiros on 6/12/2014.
 */
public class markerOptions extends DialogFragment {

    /**
     * Create a new instance of markerInfo, providing "title"
     * as an argument.
     */
    public static markerOptions newInstance(LatLng latlng) {
        markerOptions f = new markerOptions();

        // Supply num input as an argument.
        Bundle args = new Bundle();
        args.putParcelable("latlng", latlng);
        f.setArguments(args);

        return f;
    }

    /* The activity that creates an instance of this dialog fragment must
     * implement this interface in order to receive event callbacks.
     * Each method passes the DialogFragment in case the host needs to query it. */
    public interface DirectionDialogMarkerListener {
        public void onDialogRecvClick(DialogFragment dialog, LatLng latlng);
    }

    /* The activity that creates an instance of this dialog fragment must
 * implement this interface in order to receive event callbacks.
 * Each method passes the DialogFragment in case the host needs to query it. */
    public interface TwitterListener {
        public void onTweet(DialogFragment dialog, LatLng latlng);
    }

    // Use this instance of the interface to deliver action events
    DirectionDialogMarkerListener mListener;
    TwitterListener mListener2;

    // Override the Fragment.onAttach() method to instantiate the DirectionDialogListener
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the DirectionDialogListener so we can send events to the host
            mListener = (DirectionDialogMarkerListener) activity;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(activity.toString()
                    + " must implement DirectionDialogMarkerListener");
        }
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the TwitterListener so we can send events to the host
            mListener2 = (TwitterListener) activity;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(activity.toString()
                    + " must implement TwitterListener");
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();

        builder.setItems(R.array.options_array, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                LatLng latlng = getArguments().getParcelable("latlng");

                switch (which) {
                    case 0: // delete
                        ((MainActivity)getActivity()).deleteMarker(latlng);
                        break;
                    case 1: // edit
                        ((MainActivity)getActivity()).editCurrentMarker(latlng);
                        break;
                    case 2: // receive instructions
                        mListener.onDialogRecvClick(markerOptions.this, latlng);
                        break;
                    case 3: // share on twitter
                        mListener2.onTweet(markerOptions.this, latlng);
                        break;
                }
            }
        });
        // Create the AlertDialog object and return it
        return builder.create();
    }
}
