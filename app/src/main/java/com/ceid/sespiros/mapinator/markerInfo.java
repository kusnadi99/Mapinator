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

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by sespiros on 6/12/2014.
 */
public class MarkerInfo extends DialogFragment {
    LatLng latlng;
    String title, desc, category;
    View layout;

    /**
     * Create a new instance of markerInfo, providing "latlng"
     * as an argument.
     */
    public static MarkerInfo newInstance(LatLng latlng) {
        MarkerInfo f = new MarkerInfo();

        // Supply num input as an argument.
        Bundle args = new Bundle();
        args.putParcelable("latlng", latlng);
        f.setArguments(args);

        return f;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();

        layout = inflater.inflate(R.layout.info, null);
        Spinner spinner = (Spinner) layout.findViewById(R.id.spinner);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this.getActivity(),
                R.array.categories_array, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinner.setAdapter(adapter);

        builder.setTitle(R.string.dialog_title)
                .setView(layout)
                .setPositiveButton(R.string.okey, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        title = ((EditText) layout.findViewById(R.id.editTitle))
                                .getText().toString();
                        desc = ((EditText) layout.findViewById(R.id.editDescription))
                                .getText().toString();
                        category = ((Spinner) layout.findViewById(R.id.spinner))
                                .getSelectedItem().toString();
                        latlng = getArguments().getParcelable("latlng");

                        ((MainActivity)getActivity()).doPositiveClick(title, desc, category, latlng);
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
