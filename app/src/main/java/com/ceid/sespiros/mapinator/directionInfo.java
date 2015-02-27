package com.ceid.sespiros.mapinator;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by sespiros on 6/12/2014.
 */
public class directionInfo extends DialogFragment {
    LatLng latlng;
    String title, desc;
    Long category;
    View layout;

    /**
     * Create a new instance of directionInfo, providing "latlng"
     * as an argument.
     */
    public static directionInfo newInstance(LatLng latlng) {
        directionInfo f = new directionInfo();

        // Supply num input as an argument.
        Bundle args = new Bundle();
        args.putParcelable("latlng", latlng);
        f.setArguments(args);

        return f;
    }

    /**
     * Create a new instance of directionInfo
     * as an argument.
     */
    public static directionInfo newInstance() {
        directionInfo f = new directionInfo();

        return f;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();

        layout = inflater.inflate(R.layout.directions, null);


        builder.setTitle(R.string.dialog_title)
                .setView(layout)
                .setPositiveButton(R.string.okey, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                    }

                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User cancelled the dialog
                    }
                });

        // Create the AlertDialog object and return it
        return builder.create();
    }
}