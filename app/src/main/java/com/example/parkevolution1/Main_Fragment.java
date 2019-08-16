package com.example.parkevolution1;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.libraries.places.api.Places;

import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class Main_Fragment extends Fragment implements OnMapReadyCallback, GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks {

    static GoogleMap map;
    /**
     * Location the user wants to travel to
     * */
    Location currentLocation;

    /**
     * Location that the user starts off with
     * */
    Location startingLocation;

    SVY21Coordinate currentSVY21Location;

    double default_lat = 1.2906, default_long = 103.8530;
    //location data
    private double loc_lat, loc_long;
    FusedLocationProviderClient fusedLocationProviderClient;
    private static final int REQUEST_CODE = 101;
    //private PlaceAutocompleteAdapter placeAutocompleteAdapter;
    private GoogleApiClient googleApiClient;
    private FusedLocationProviderClient fusedLocationClient;
    static MarkerOptions markerPrimary;

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public android.view.View onCreateView(LayoutInflater inflater, ViewGroup container,
                                          Bundle savedInstanceState) {
        ((MainActivity) getActivity()).setState(0);
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getActivity());
        if (googleApiClient == null) {
            googleApiClient = new GoogleApiClient.Builder(getContext())
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
        //fetchLastLocation();





        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_main, container, false);
    }

    private void fetchLastLocation() {

        //request for location permission if permission isn't given
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE);
            if (MainActivity.getStartingLatLonCoordinate() == null) {
                MainActivity.setStartingLatLonCoordinate(new LatLonCoordinate(default_lat, default_long));
            }

            if (MainActivity.getLatLonCoordinate() == null) {
                MainActivity.setLatLonCoordinate(new LatLonCoordinate(default_lat, default_long));
            }

            if (MainActivity.getSelectedLatLonCoordinate() == null) {
                MainActivity.setSelectedLatLonCoordinate(new LatLonCoordinate(default_lat, default_long));
            }
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

            Location currLoc = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);

            if (currLoc != null) {
                currentLocation = currLoc;

                /**
                 * the user's current location is set in MainActivity
                 * */

                //current location will now get updated
                MainActivity.setStartingLatLonCoordinate(new LatLonCoordinate(currentLocation.getLatitude(), currentLocation.getLongitude()));

                //if the user did not specify any starting location -> starting location will be set to current location
                if (MainActivity.getLatLonCoordinate() == null) {
                    MainActivity.setLatLonCoordinate(new LatLonCoordinate(currentLocation.getLatitude(), currentLocation.getLongitude()));
                }

                startingLocation = currLoc;
                viewPager.invalidate();
                pagerAdapter.notifyDataSetChanged();
                viewPager.getAdapter().notifyDataSetChanged();
                Log.v("Location_test", "Location is updated");

                //Toast.makeText(getContext(), currentLocation.getLatitude() + "" + currentLocation.getLongitude(), Toast.LENGTH_LONG).show();
                if (currentCoord == null) {
                    currentCoord = new LatLonCoordinate(default_lat, default_long);
                }
                currentSVY21Location = convertToSVY21(currentCoord);
                //SupportMapFragment supportMapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
                mapFragment.getMapAsync(Main_Fragment.this);
            } else {
                if (MainActivity.getStartingLatLonCoordinate() == null) {
                    MainActivity.setStartingLatLonCoordinate(new LatLonCoordinate(default_lat, default_long));
                }

                if (MainActivity.getLatLonCoordinate() == null) {
                    MainActivity.setLatLonCoordinate(new LatLonCoordinate(default_lat, default_long));
                }

                if (MainActivity.getSelectedLatLonCoordinate() == null) {
                    MainActivity.setSelectedLatLonCoordinate(new LatLonCoordinate(default_lat, default_long));
                }
                Toast.makeText(getContext(), "Your current location isn't available yet", Toast.LENGTH_LONG).show();
            }



 //           Task<Location> task = fusedLocationProviderClient.getLastLocation();
//            task.addOnSuccessListener(new OnSuccessListener<Location>() {
//                @Override
//                public void onSuccess(Location location) {
//                    if (location != null) {
//                        currentLocation = location;
//
//                        /**
//                         * the user's current location is set in MainActivity
//                         * */
//
//                        //current location will now get updated
//                        MainActivity.setStartingLatLonCoordinate(new LatLonCoordinate(currentLocation.getLatitude(), currentLocation.getLongitude()));
//
//                        //if the user did not specify any starting location -> starting location will be set to current location
//                        if (MainActivity.getLatLonCoordinate() == null) {
//                            MainActivity.setLatLonCoordinate(new LatLonCoordinate(currentLocation.getLatitude(), currentLocation.getLongitude()));
//                        }
//
//                        startingLocation = location;
//                        viewPager.invalidate();
//                        pagerAdapter.notifyDataSetChanged();
//                        viewPager.getAdapter().notifyDataSetChanged();
//                        Log.v("Location_test", "Location is updated");
//
//                        //Toast.makeText(getContext(), currentLocation.getLatitude() + "" + currentLocation.getLongitude(), Toast.LENGTH_LONG).show();
//                        if (currentCoord == null) {
//                            currentCoord = new LatLonCoordinate(default_lat, default_long);
//
//                        }
//                        currentSVY21Location = convertToSVY21(currentCoord);
//                        //SupportMapFragment supportMapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
//                        mapFragment.getMapAsync(Main_Fragment.this);
//                    } else {
//                        if (MainActivity.getStartingLatLonCoordinate() == null) {
//                            MainActivity.setStartingLatLonCoordinate(new LatLonCoordinate(default_lat, default_long));
//                        }
//
//                        if (MainActivity.getLatLonCoordinate() == null) {
//                            MainActivity.setLatLonCoordinate(new LatLonCoordinate(default_lat, default_long));
//                        }
//
//                        if (MainActivity.getSelectedLatLonCoordinate() == null) {
//                            MainActivity.setSelectedLatLonCoordinate(new LatLonCoordinate(default_lat, default_long));
//                        }
//                        Toast.makeText(getContext(), "Last known location isn't retrieved", Toast.LENGTH_LONG).show();
//                    }
//                }
//            });
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    //widgets
    //private AutoCompleteTextView mSearchText;

    List<Place.Field> placeFields = Arrays.asList(Place.Field.ID,
            Place.Field.NAME,
            Place.Field.ADDRESS,
            Place.Field.LAT_LNG);
    AutocompleteSupportFragment places_fragment;
    private PagerAdapter pagerAdapter;
    private ViewPager viewPager;
    private SupportMapFragment mapFragment;
    ArrayList<Fragment> fr_list = new ArrayList<>();
    //Button testBtn;


    FloatingActionButton locFab;
    FloatingActionButton navFab;
    private BottomSheetBehavior sheetBehavior;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        View mView = getView();

        locFab = getView().findViewById(R.id.fab_1);
        locFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                if(compClosed) {
                    isFirstOpened = true;
                    fetchLastLocation();

                    //update the starting location with this data
                    if (MainActivity.getStartingLatLonCoordinate() != null) {
                        LatLonCoordinate t = MainActivity.getStartingLatLonCoordinate();
                        MainActivity.setLatLonCoordinate(new LatLonCoordinate(t.getLatitude(), t.getLongitude()));
                    } else {
                        Log.v("LocationError51", "latlongcoordinate is not updated!");
                    }
                    //update the viewpager
                    viewPager.getAdapter().notifyDataSetChanged();
                }
            }
        });


//        testBtn = getView().findViewById(R.id.testLocationButton);


        navFab = getView().findViewById(R.id.fab_2);
        navFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(compClosed) {

                    LatLonCoordinate latLonCoordinate = ((MainActivity) getActivity()).getLatLonCoordinate();
                    LatLonCoordinate startingLatLonCoordinate = ((MainActivity) getActivity()).getStartingLatLonCoordinate();
                    /**
                     * Logging for error testing
                     * */
                    Log.v("Navigation_testing", "Starting Latitude: " + startingLatLonCoordinate.getLatitude() + " Starting Longitude: " + startingLatLonCoordinate.getLongitude());
                    Log.v("Navigation_testing", "Starting Latitude: " + latLonCoordinate.getLatitude() + " Starting Longitude: " + startingLatLonCoordinate.getLongitude());
                    // Directions
                    String nav_address = "http://maps.google.com/maps?daddr="
                            + latLonCoordinate.getLatitude() + ", "
                            + latLonCoordinate.getLongitude();

                    /**
                     * Check if the person has google maps app
                     * Open google maps if the person has google maps
                     * If not open browser
                     * "http://maps.google.com/maps?saddr=51.5, 0.125&daddr=51.5, 0.15"
                     */

                    Intent intent = new Intent(android.content.Intent.ACTION_VIEW, Uri.parse(nav_address));
                    startActivity(intent);
                }
            }
        });
        //mSearchText = mView.findViewById(R.id.input_search);

        init();
        setUpPlaceAutoComplete();
        //Viewpager
        viewPager = mView.findViewById(R.id.view_pager);
        sheetBehavior = BottomSheetBehavior.from(viewPager);
        fr_list.add(new ProximityFragment());
        fr_list.add(new PriceFragment());
        fr_list.add(new AvailabilityFragment());
        fr_list.add(new RecommendedFragment());
        pagerAdapter = new FixedTabsPagerAdapter(getActivity().getSupportFragmentManager(), fr_list);

        viewPager.setAdapter(pagerAdapter);
        viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i1) {

            }

            @Override
            public void onPageSelected(int i) {
                pagerAdapter.notifyDataSetChanged();
            }

            @Override
            public void onPageScrollStateChanged(int i) {

            }
        });
        TabLayout tabLayout = mView.findViewById(R.id.tab_layout);
        tabLayout.setupWithViewPager(viewPager);

        //locFab.performClick();

        try {
            viewPager.getAdapter().notifyDataSetChanged();
        } catch (NullPointerException e) {
            e.printStackTrace();
        }

        if (MainActivity.getStartingLatLonCoordinate() == null) {
            MainActivity.setStartingLatLonCoordinate(new LatLonCoordinate(default_lat, default_long));
        }

        if (MainActivity.getLatLonCoordinate() == null) {
            MainActivity.setLatLonCoordinate(new LatLonCoordinate(default_lat, default_long));
        }

        if (MainActivity.getSelectedLatLonCoordinate() == null) {
            MainActivity.setSelectedLatLonCoordinate(new LatLonCoordinate(default_lat, default_long));
        }


        super.onViewCreated(view, savedInstanceState);
    }

    private static final LatLngBounds LAT_LNG_BOUNDS = new LatLngBounds(new LatLng(-40, -168), new LatLng(71, 136));
    final private String apiKey = "AIzaSyCQqUq9JUexPU9MF3c3KYhRUY42sBoiC6w";
    private PlacesClient placesClient;


    private void init() {
        // Initialize Places.
        if (!Places.isInitialized())
            Places.initialize(getActivity(), apiKey);
        // Create a new Places client instance.
        placesClient = Places.createClient(getActivity());
    }

    private void setUpPlaceAutoComplete() {
        places_fragment = (AutocompleteSupportFragment) getChildFragmentManager().findFragmentById(R.id.places_autocomplete_fragment);
        places_fragment.setPlaceFields(placeFields);
        places_fragment.setCountry("SG"); //added this line
        places_fragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(@NonNull Place place) {
                //Toast.makeText(getContext(), ""+place.getName(), Toast.LENGTH_LONG).show();
                double lat_places, long_places;
                lat_places = place.getLatLng().latitude;
                long_places = place.getLatLng().longitude;
                if (currentLocation != null) {
                    try {
                        currentLocation.setLatitude(lat_places);
                        currentLocation.setLongitude(long_places);
                    } catch (NullPointerException e) {
                        e.printStackTrace();
                        Toast.makeText(getContext(), "A Location exception has occurred. Please restart the app", Toast.LENGTH_LONG).show();
                    }

                }

                //((MainActivity)getActivity()).setStartingLatLonCoordinate(new LatLonCoordinate(lat_places, long_places));
                markerPrimary = new MarkerOptions().position(place.getLatLng()).title(place.getName());

                //the starting location will get updated
                MainActivity.setLatLonCoordinate(new LatLonCoordinate(lat_places, long_places));
                map.clear();
                LatLng latLng = new LatLng(lat_places, long_places);
                map.animateCamera(CameraUpdateFactory.newLatLng(latLng));
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
                MarkerOptions options = new MarkerOptions().position(latLng).title(place.getName());
                map.addMarker(options).setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));




                //mapFragment.getMapAsync(Main_Fragment.this);
                //((FragmentCanali) mMyAdapter.fr_list.get(0)).refresh();
                //viewPager.invalidate();
                viewPager.getAdapter().notifyDataSetChanged();
            }

            @Override
            public void onError(@NonNull Status status) {
                Toast.makeText(getContext(), "" + status.getStatusMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private String TAG = "Main_Fragment";

    private LatLonCoordinate currentCoord;
    private boolean isFirstOpened = true; //for the very first marker

    private boolean isCurrLocSel = true;
    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        map.getUiSettings().setMyLocationButtonEnabled(false);

        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }

        //Map Tool Bar is the default google map's toolbar that opens up the navigation direcitons to that location as well as google maps location on google maps
        /**
         * This is replaced with custom floating action button
         * */
        map.getUiSettings().setMapToolbarEnabled(false);


        //currentLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
        if (currentLocation != null) {
            LatLng latLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
            //MarkerOptions markerOptions = new MarkerOptions().position(latLng).title("Here");
            map.clear();

            map.animateCamera(CameraUpdateFactory.newLatLng(latLng));
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15)); //change this number between 2-21 to control the zoom of the map

            //map.addMarker(markerOptions);
            if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            //blue marker to show current location
            map.setMyLocationEnabled(true);
            //add marker on current location
            //if(isFirstOpened){
              //  isFirstOpened =  false;
                MarkerOptions options = new MarkerOptions().position(latLng).title("Here");
                map.addMarker(options).setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));

            //}
            currentCoord = new LatLonCoordinate(latLng.latitude, latLng.longitude);

            //method to update the distances and display them on the fragment
            //viewPager.getAdapter().notifyDataSetChanged();
            viewPager.setAdapter(pagerAdapter);
        } else{
            //animates in on the map showing singapore
            LatLng latLng;
            if(((MainActivity)getActivity()).getStartingLatLonCoordinate() == null){
                latLng = new LatLng(default_lat,  default_long);
            } else {
                LatLonCoordinate latLonCoordinate = ((MainActivity)getActivity()).getStartingLatLonCoordinate();
                latLng = new LatLng(latLonCoordinate.getLatitude(),  latLonCoordinate.getLongitude());
            }

            map.clear();
            map.animateCamera(CameraUpdateFactory.newLatLng(latLng));
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 10));
            map.addMarker(new MarkerOptions().position(latLng).title("Singapore"));
            //map.moveCamera(CameraUpdateFactory.newLatLng(latLng));
            currentCoord = new LatLonCoordinate(latLng.latitude, latLng.longitude);
            viewPager.getAdapter().notifyDataSetChanged();
        }

    }

    boolean compClosed = true;
    boolean compOpen = false;
    @Override
    public void onStart() {
        googleApiClient.connect();

        //getting and setting the peek height
        int window_height = getActivity().getWindowManager().getDefaultDisplay().getHeight();
        int map_height = dpToPx(400);
        sheetBehavior.setPeekHeight(window_height-map_height);
        sheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View view, int i) {
                //Toast.makeText(getContext(), "state: "+i, Toast.LENGTH_LONG).show();

                AlphaAnimation anim = new AlphaAnimation(1.0f, 0.0f);
                anim.setDuration(300);
                anim.setFillAfter(true);
                AlphaAnimation anim2 = new AlphaAnimation(0.0f, 1.0f);
                anim2.setDuration(500);
                anim2.setFillAfter(true);

                anim.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {
                        navFab.show();
                        locFab.show();
                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        navFab.hide();
                        locFab.hide();
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });

                anim2.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {
                        navFab.show();
                        locFab.show();
                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        navFab.show();
                        locFab.show();
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });

                compClosed = true;
                compOpen = false;
                if(i== 3 && !compOpen && compClosed){
                    compClosed = false;
                    compOpen = true;
                    locFab.startAnimation(anim);
                    navFab.startAnimation(anim);

                } else if(i==4 && compClosed && !compOpen){
                    compClosed = true;
                    compOpen = false;
                    locFab.startAnimation(anim2);
                    navFab.startAnimation(anim2);
                }
            }

            @Override
            public void onSlide(@NonNull View view, float v) {

            }
        });


        super.onStart();
    }

    private int dpToPx(int dp){
        float density = getContext().getResources().getDisplayMetrics().density;
        return Math.round(dp*density);
    }

    @Override
    public void onStop() {
        googleApiClient.disconnect();
        super.onStop();
    }



    //    @Override
//    public void onStart() {
//        super.onStart();
//        fetchLastLocation();
//        viewPager.getAdapter().notifyDataSetChanged();
//    }

    private SVY21Coordinate convertToSVY21(LatLonCoordinate latLonCoordinate){
        return latLonCoordinate.asSVY21();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case REQUEST_CODE:
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    fetchLastLocation();
                }
                break;
        }
    }

    public static void addMarkerToMap(LatLng latLng, String name){
        //name = "New Location";
        MarkerOptions options = new MarkerOptions().position(latLng).title(name);
        map.clear();
        if(markerPrimary != null) map.addMarker(markerPrimary).setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
        map.addMarker(options).setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));

        //zoom in on the read location -> set selected location
        LatLng latLonCoordinate = new LatLng(MainActivity.getSelectedLatLonCoordinate().getLatitude(), MainActivity.getSelectedLatLonCoordinate().getLongitude());
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLonCoordinate, 15));
        //map.animateCamera(CameraUpdateFactory.newLatLng(latLonCoordinate));
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        locFab.performClick();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }


    /*---------- Listener class to get coordinates ------------- */
    private class MyLocationListener implements LocationListener {

        @Override
        public void onLocationChanged(Location loc) {
            //editLocation.setText("");
            //pb.setVisibility(View.INVISIBLE);
            Toast.makeText(
                    getContext(),
                    "Location changed: Lat: " + loc.getLatitude() + " Lng: "
                            + loc.getLongitude(), Toast.LENGTH_SHORT).show();
            String longitude = "Longitude: " + loc.getLongitude();
            Log.v(TAG, longitude);
            String latitude = "Latitude: " + loc.getLatitude();
            Log.v(TAG, latitude);

            /*------- To get city name from coordinates -------- */
            String cityName = null;
            Geocoder gcd = new Geocoder(getContext(), Locale.getDefault());
            List<Address> addresses;
            try {
                addresses = gcd.getFromLocation(loc.getLatitude(),
                        loc.getLongitude(), 1);
                if (addresses.size() > 0) {
                    System.out.println(addresses.get(0).getLocality());
                    cityName = addresses.get(0).getLocality();
                }
            }
            catch (IOException e) {
                e.printStackTrace();
            }
            String s = longitude + "\n" + latitude + "\n\nMy Current City is: "
                    + cityName;
            //editLocation.setText(s);

            Toast.makeText(getContext(), s, Toast.LENGTH_LONG).show();
        }

        @Override
        public void onProviderDisabled(String provider) {}

        @Override
        public void onProviderEnabled(String provider) {}

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {}
    }
}