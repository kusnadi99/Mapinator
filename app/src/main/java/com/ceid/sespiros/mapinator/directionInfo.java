package com.ceid.sespiros.mapinator;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioGroup;

import com.google.android.gms.maps.model.LatLng;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.JsonObjectParser;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.Key;

import java.util.List;

/**
 * Created by sespiros on 6/12/2014.
 */
public class directionInfo extends DialogFragment {
    View layout;
    ImageButton btn;
    EditText edit, edit2;
    RadioGroup means;
    int choice;


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
    public interface DirectionDialogListener {
        public void onDialogLocationClick(DialogFragment dialog, EditText edit, ImageButton btn);
    }

    // Use this instance of the interface to deliver action events
    DirectionDialogListener mListener;

    // Override the Fragment.onAttach() method to instantiate the DirectionDialogListener
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the DirectionDialogListener so we can send events to the host
            mListener = (DirectionDialogListener) activity;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(activity.toString()
                    + " must implement DirectionDialogListener");
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();

        layout = inflater.inflate(R.layout.directions, null);

        btn = (ImageButton) layout.findViewById(R.id.button);
        edit = (EditText) layout.findViewById(R.id.editStart);
        edit2 = (EditText) layout.findViewById(R.id.editDestination);
        means = (RadioGroup) layout.findViewById(R.id.radio);
        choice = means.getCheckedRadioButtonId();
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

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
        });

        builder.setTitle(R.string.dialog2_title)
                .setView(layout)
                .setPositiveButton(R.string.okey, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        String mode;
                        if(choice==R.id.bicycling)
                            mode = "bicycling";
                        else if (choice==R.id.walking)
                            mode = "walking";
                        else
                            mode = "driving";

                        // Call directions API
                        ((MainActivity)getActivity()).getDirections(edit.getText().toString(), edit2.getText().toString(), mode);
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

    public static class DirectionsResult implements Parcelable{
        @Key("routes")
        public List<Route> routes;

        public int describeContents() {
            return 0;
        }

        // write your object's data to the passed-in Parcel
        public void writeToParcel(Parcel out, int flags) {
            out.writeList(routes);
        }

        // this is used to regenerate your object. All Parcelables must have a CREATOR that implements these two methods
        public static final Parcelable.Creator<DirectionsResult> CREATOR = new Parcelable.Creator<DirectionsResult>() {
            public DirectionsResult createFromParcel(Parcel in) {
                return new DirectionsResult(in);
            }

            public DirectionsResult[] newArray(int size) {
                return new DirectionsResult[size];
            }
        };

        // example constructor that takes a Parcel and gives you an object populated with it's values
        private DirectionsResult(Parcel in) {
            in.readList(routes, null);
        }

        public DirectionsResult()
        {}
    }

    public static class Route {
        @Key("overview_polyline")
        public OverviewPolyLine overviewPolyLine;
        @Key("legs")
        public List<Leg> legs;
    }

    public static class OverviewPolyLine {
        @Key("points")
        public String points;
    }

    public static class Leg {
        @Key("steps")
        public List<Step> steps;
    }
    public static class Step {
        @Key("html_instructions")
        public String instruction;
    }
}