package com.example.parkevolution1;


import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import static android.content.Context.MODE_PRIVATE;


/**
 * A simple {@link Fragment} subclass.
 */
public class FindCarFragment extends Fragment implements OnMapReadyCallback, GoogleApiClient.OnConnectionFailedListener {

    static GoogleMap map;
    private SupportMapFragment mapFragment;

    double default_lat = 1.2906, default_long = 103.8530;
    Location currentLocation;
    LatLonCoordinate startingLatLong;


    //Data from shared preferences
    double currLat, currLong;
    String currAddress, currDescription;

    public FindCarFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        //set State
        ((MainActivity)getActivity()).setState(3);

        //get the data from shared preferences
        SharedPreferences prefs = getActivity().getSharedPreferences("ParkedHerePrefs", MODE_PRIVATE);

        //transfer the data into ParkedHere class
        currLat = prefs.getFloat("pHLat", 0);
        currLong = prefs.getFloat("pHLong", 0);
        currAddress = prefs.getString("pHAddress", "");
        currDescription = prefs.getString("pHDescription", "");

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_find_car, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        //Update all the views
        TextView addrView = getView().findViewById(R.id.park_here_addressView);
        TextView descView = getView().findViewById(R.id.park_here_description);
        Button unparkButton = getView().findViewById(R.id.unpark_button);

        addrView.setText(currAddress);
        descView.setText(currDescription);
        unparkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //show the dialogue to confirm that the user wants to unpark
                new AlertDialog.Builder(getContext())
                        .setTitle("Unpark")
                        .setMessage("Once you unpark your car, you will lose this location information. Do you want to proceed?")
                        .setIcon(R.drawable.ic_car)
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                //clear information in sharedpreferences
                                SharedPreferences.Editor editor = getActivity().getSharedPreferences("ParkedHerePrefs", Context.MODE_PRIVATE).edit();
                                editor.putBoolean("isParked", false);
                                editor.apply();

                                //reload this page -> appropriate contentview and layout should be loaded up
                                ((MainActivity)getActivity()).changeNavDrawerFindCar();

                                ParkedHereFragment parkedHereFragment = new ParkedHereFragment();
                                getActivity().getSupportFragmentManager().beginTransaction()
                                        .setTransition(FragmentTransaction.TRANSIT_ENTER_MASK)
                                        .replace(R.id.main_fragment, parkedHereFragment)
                                        //.addToBackStack(null)
                                        .commit();
                            }})
                        .setNegativeButton(android.R.string.no, null).show();
            }
        });

        FloatingActionButton nav_fab = getView().findViewById(R.id.fab_find_car);
        nav_fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String nav_address = "http://maps.google.com/maps?daddr="
                        + currLat +", "
                        + currLong;

                /**
                 * Check if the person has google maps app
                 * Open google maps if the person has google maps
                 * If not open browser
                 * "http://maps.google.com/maps?saddr=51.5, 0.125&daddr=51.5, 0.15"
                 */

                Intent intent = new Intent(android.content.Intent.ACTION_VIEW, Uri.parse(nav_address));
                startActivity(intent);
            }
        });
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        map.getUiSettings().setMyLocationButtonEnabled(false);
        //Map Tool Bar is the default google map's toolbar that opens up the navigation direcitons to that location as well as google maps location on google maps
        /**
         * This is replaced with custom floating action button
         * */
        map.getUiSettings().setMapToolbarEnabled(false);
        LatLng currLatLng = new LatLng(currLat, currLong);
        MarkerOptions markerOptions = new MarkerOptions().position(currLatLng).title(currAddress);
        map.clear();
        map.animateCamera(CameraUpdateFactory.newLatLng(currLatLng));
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(currLatLng, 15)); //change this number between 2-21 to control the zoom of the map
        map.addMarker(markerOptions).setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
    }
}
