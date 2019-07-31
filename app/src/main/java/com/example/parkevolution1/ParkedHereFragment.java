package com.example.parkevolution1;


import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.io.IOException;
import java.util.List;
import java.util.Locale;


/**
 * A simple {@link Fragment} subclass.
 */
public class ParkedHereFragment extends Fragment implements OnMapReadyCallback, GoogleApiClient.OnConnectionFailedListener {

    static GoogleMap map;
    private GoogleApiClient googleApiClient;
    static MarkerOptions markerPrimary;
    private SupportMapFragment mapFragment;
    private static final int REQUEST_CODE = 101;

    double default_lat = 1.2906, default_long = 103.8530;
    Location currentLocation;
    LatLonCoordinate startingLatLong;
    FusedLocationProviderClient fusedLocationProviderClient;

    public ParkedHereFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        //set State
        ((MainActivity)getActivity()).setState(3);

        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getActivity());
        //set the map location as the current location obtained from main activity
        LatLonCoordinate startLoc = ((MainActivity)getActivity()).getStartingLatLonCoordinate();
        if(startLoc == null){
            startingLatLong = new LatLonCoordinate(default_lat, default_long);
        } else {
            startingLatLong = new LatLonCoordinate(startLoc.getLatitude(), startLoc.getLongitude());
        }

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_parked_here, container, false);
    }


    TextView cpNameView;
    EditText descriptionET;
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //map init
        mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        //initialise the textview
        cpNameView = getView().findViewById(R.id.park_here_addressView);
        setCpName(cpNameView);

        //if current location FAB is clicked, then we call fetchLocation() to obtain the real location
        FloatingActionButton locFab = getView().findViewById(R.id.fab_parked_here_curr_loc);
        locFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fetchLastLocation(); //updates the startingLocation and map if possible
                setCpName(cpNameView);
            }
        });

        descriptionET = getView().findViewById(R.id.parked_here_description_et);

        Button parkHere = getView().findViewById(R.id.park_here_button);
        parkHere.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //open a dialog to confirm if the person wants to park his car there
                new AlertDialog.Builder(getContext())
                        .setTitle("Park Here")
                        .setMessage("Click yes to confirm")
                        .setIcon(R.drawable.ic_car)
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                //save the information
                                double currLat = startingLatLong.getLatitude();
                                double currLong = startingLatLong.getLongitude();
                                String currAddress = cpNameView.getText().toString();
                                String currDescription = descriptionET.getText().toString();

                                //put this information inside SharedPreference
                                SharedPreferences.Editor editor = getActivity().getSharedPreferences("ParkedHerePrefs", Context.MODE_PRIVATE).edit();
                                editor.putFloat("pHLat", (float)startingLatLong.getLatitude());
                                editor.putFloat("pHLong", (float)startingLatLong.getLongitude());
                                editor.putString("pHAddress", currAddress);
                                editor.putString("pHDescription", currDescription);
                                editor.putBoolean("isParked", true);
                                editor.apply();

                                //reload this page -> appropriate contentview and layout should be loaded up
                                ((MainActivity)getActivity()).changeNavDrawerFindCar();

                                FindCarFragment findCarFragment = new FindCarFragment();
                                getActivity().getSupportFragmentManager().beginTransaction()
                                        .setTransition(FragmentTransaction.TRANSIT_ENTER_MASK)
                                        .replace(R.id.main_fragment, findCarFragment)
                                        //.addToBackStack(null)
                                        .commit();
                            }})
                        .setNegativeButton(android.R.string.no, null).show();
            }
        });
    }

    private void setCpName(TextView tv){
        tv.setText("Unknown");
        Geocoder geocoder;
        List<Address> addresses;
        geocoder = new Geocoder(getActivity(), Locale.getDefault());
        try{
            addresses = geocoder.getFromLocation(startingLatLong.getLatitude(), startingLatLong.getLongitude(), 1);
            if(addresses.size() != 0){
                tv.setText(addresses.get(0).getAddressLine(0));

            } else {
                tv.setText("Address not available");

            }
        } catch(IOException e){
            e.printStackTrace();
        }
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

        if (currentLocation != null) {
            LatLng latLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
            MarkerOptions markerOptions = new MarkerOptions().position(latLng).title("Here");
            map.clear();

            map.animateCamera(CameraUpdateFactory.newLatLng(latLng));
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15)); //change this number between 2-21 to control the zoom of the map

            //map.addMarker(markerOptions);
            if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            //blue marker to show current location
            map.setMyLocationEnabled(true);

            MarkerOptions options = new MarkerOptions().position(latLng).title("Here");
            map.addMarker(options).setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));

            //}
            //startingLatLong = new LatLonCoordinate(latLng.latitude, latLng.longitude);

            //method to update the distances and display them on the fragment

        } else{
            //animates in on the map showing singapore
            //check if mainActivity got startinglatlon
            //if don't have then set back to default

            LatLng latLng = new LatLng(startingLatLong.getLatitude(),  startingLatLong.getLongitude());
            map.clear();
            map.animateCamera(CameraUpdateFactory.newLatLng(latLng));
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 10));
            map.addMarker(new MarkerOptions().position(latLng).title("Singapore"));
            //map.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        }
    }

    private void fetchLastLocation() {
        //readWeatherData();

        //request for location permission if permission isn't given
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE);
            return;
        }
        //check if location is turned on
        LocationManager lm = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        boolean gps_enabled = false;
        boolean network_enabled = false;

        try {
            gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch (Exception ex) {

        }

        try {
            network_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch (Exception ex) {
        }

        if (!gps_enabled && !network_enabled) {
            // notify user
            new AlertDialog.Builder(getActivity())
                    .setMessage("GPS Not Enabled")
                    .setPositiveButton("Open Location Settings", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                            getActivity().startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                        }
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        } else {
            Task<Location> task = fusedLocationProviderClient.getLastLocation();
            task.addOnSuccessListener(new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    if (location != null) {
                        currentLocation = location;

                        /**
                         * the user's current location is set in MainActivity
                         * */
                        //MainActivity.setLatLonCoordinate(new LatLonCoordinate(currentLocation.getLatitude(), currentLocation.getLongitude()));
                        MainActivity.setStartingLatLonCoordinate(new LatLonCoordinate(currentLocation.getLatitude(), currentLocation.getLongitude()));

                        //update startingLatlong
                        startingLatLong = new LatLonCoordinate(location.getLatitude(), location.getLongitude());

                        //SupportMapFragment supportMapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
                        mapFragment.getMapAsync(ParkedHereFragment.this);
                    } else {
                        if(((MainActivity)getActivity()).getStartingLatLonCoordinate() != null){
                            currentLocation.setLatitude(((MainActivity)getActivity()).getStartingLatLonCoordinate().getLatitude());
                            currentLocation.setLongitude(((MainActivity)getActivity()).getStartingLatLonCoordinate().getLongitude());

                            //update the starting loc also
                            startingLatLong = new LatLonCoordinate(currentLocation.getLatitude(), currentLocation.getLongitude());
                        } else {
                            currentLocation = new Location("");
                            currentLocation.setLatitude(default_lat);
                            currentLocation.setLongitude(default_long);
                        }
                        Toast.makeText(getContext(), "Location error", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }
}
