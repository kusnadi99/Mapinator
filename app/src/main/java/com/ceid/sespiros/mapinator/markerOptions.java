package com.ceid.sespiros.mapinator;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;

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
    public static markerOptions newInstance(Marker marker) {
        markerOptions f = new markerOptions();

        // Supply num input as an argument.
        Bundle args = new Bundle();
        args.put
        args.putParcelable("marker", marker);
        f.setArguments(args);

        return f;
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
                        ((MainActivity)getActivity()).editMarker(latlng);
                        break;
                    case 2: // receive instructions
                        break;
                    case 3: // share on facebook
                        break;
                }
            }
        });
        // Create the AlertDialog object and return it
        return builder.create();
    }
}
