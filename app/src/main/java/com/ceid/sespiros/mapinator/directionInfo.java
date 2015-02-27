package com.ceid.sespiros.mapinator;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by sespiros on 6/12/2014.
 */
public class directionInfo extends DialogFragment {
    LatLng latlng;
    String start, destination;
    View layout;
    ImageButton btn;
    EditText edit;

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

    /* The activity that creates an instance of this dialog fragment must
     * implement this interface in order to receive event callbacks.
     * Each method passes the DialogFragment in case the host needs to query it. */
    public interface NoticeDialogListener {
        public void onDialogLocationClick(DialogFragment dialog, EditText edit, ImageButton btn);
    }

    // Use this instance of the interface to deliver action events
    NoticeDialogListener mListener;

    // Override the Fragment.onAttach() method to instantiate the NoticeDialogListener
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            mListener = (NoticeDialogListener) activity;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(activity.toString()
                    + " must implement NoticeDialogListener");
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();

        layout = inflater.inflate(R.layout.directions, null);

        btn = (ImageButton)layout.findViewById(R.id.button);
        edit = (EditText)layout.findViewById(R.id.editStart);
        btn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mListener.onDialogLocationClick(directionInfo.this, edit, btn);
            }
        });

        edit.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
                if (s.length() != 0)
                    btn.setImageResource(R.drawable.ic_search_grey600_18dp);
                else
                    btn.setImageResource(R.drawable.ic_my_location_grey600_18dp);
            }
            public void beforeTextChanged(CharSequence s, int start, int count, int after){}
            public void onTextChanged(CharSequence s, int start, int before, int count){}
        });

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