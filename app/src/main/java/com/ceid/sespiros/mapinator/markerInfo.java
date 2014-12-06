package com.ceid.sespiros.mapinator;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import com.google.android.gms.maps.model.LatLng;
import com.ceid.sespiros.mapinator.marker;

/**
 * Created by sespiros on 6/12/2014.
 */
public class markerInfo extends DialogFragment {
    SQLiteDatabase db;
    MarkerDbHelper mDbHelper;
    LatLng latlng;
    EditText title, desc;
    Spinner category;
    View layout;

    /**
     * Create a new instance of markerInfo, providing "latlng"
     * as an argument.
     */
    static markerInfo newInstance(LatLng latlng) {
        markerInfo f = new markerInfo();

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

        mDbHelper = new MarkerDbHelper(getActivity());
        latlng = getArguments().getParcelable("latlng");

        builder.setMessage(R.string.dialog_message)
                .setView(layout)
                .setPositiveButton(R.string.okey, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                        // Gets the data repository in write mode
                        db = mDbHelper.getWritableDatabase();

                        title = (EditText) layout.findViewById(R.id.editTitle);
                        desc = (EditText) layout.findViewById(R.id.editDescription);
                        category = (Spinner) layout.findViewById(R.id.spinner);
                        Log.d("debug", desc.getText().toString());
                        // Create a new map of values, where column names are the keys
                        ContentValues values = new ContentValues();
                        values.put(marker.MarkerEntry.COLUMN_NAME_TITLE, title.getText().toString());
                        values.put(marker.MarkerEntry.COLUMN_NAME_DESC, desc.getText().toString());
                        values.put(marker.MarkerEntry.COLUMN_NAME_CATEGORY, category.getSelectedItem().toString());
                        values.put(marker.MarkerEntry.COLUMN_NAME_LATITUDE, latlng.latitude);
                        values.put(marker.MarkerEntry.COLUMN_NAME_LONGITUDE, latlng.longitude);

                        // Insert the new row, returning the primary key value of the new row
                        long newRowId;
                        newRowId = db.insert(
                                marker.MarkerEntry.TABLE_NAME,
                                null,
                                values);
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
