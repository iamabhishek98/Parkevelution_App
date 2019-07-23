package com.example.parkevolution1;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;

import com.google.android.gms.common.api.Status;
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
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;

import java.util.Arrays;
import java.util.List;

public class Main_Fragment extends Fragment implements OnMapReadyCallback, GoogleApiClient.OnConnectionFailedListener{

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

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getActivity());
        fetchLastLocation();

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_main, container, false);
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
                        MainActivity.setLatLonCoordinate(new LatLonCoordinate(currentLocation.getLatitude(), currentLocation.getLongitude()));
                        MainActivity.setStartingLatLonCoordinate(new LatLonCoordinate(currentLocation.getLatitude(), currentLocation.getLongitude()));
                        startingLocation = location;
                        //viewPager.invalidate();
                        //pagerAdapter.notifyDataSetChanged();
                        viewPager.getAdapter().notifyDataSetChanged();
                        Log.v("Location_test", "Location is updated");

                        //Toast.makeText(getContext(), currentLocation.getLatitude() + "" + currentLocation.getLongitude(), Toast.LENGTH_LONG).show();
                        if(currentCoord == null){
                            currentCoord = new LatLonCoordinate(default_lat, default_long);

                        }
                        currentSVY21Location = convertToSVY21(currentCoord);
                        //SupportMapFragment supportMapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
                        mapFragment.getMapAsync(Main_Fragment.this);
                    }
                }
            });
        }
    }

    private List<WeatherSample> weatherSamples = new ArrayList<>();

    private void readWeatherData() {
        InputStream is = getActivity().getResources().openRawResource(R.raw.data);
        BufferedReader reader = new BufferedReader(
                new InputStreamReader(is, Charset.forName("UTF-8"))
        );
        String line = "";
        try {
            while ((line = reader.readLine()) != null) {

                //split by ","
                String[] tokens = line.split(",");

                //Read the data
                WeatherSample sample = new WeatherSample();
                sample.setMonth(tokens[0]);
                if (tokens[1].length() > 0) {
                    sample.setRainfall(Double.parseDouble(tokens[1]));
                } else {
                    sample.setRainfall(0);
                }
                if (tokens.length >= 3 && tokens[2].length() > 0) {
                    sample.setSunHours(Integer.parseInt(tokens[2]));
                } else {
                    sample.setSunHours(0);
                }
                weatherSamples.add(sample);

                Log.d("MyActivity", "just created" + sample);
            }
        } catch (IOException e) {
            Log.wtf("MyActivity", "Error reading data file on line" + line, e);
            e.printStackTrace();
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
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        View mView = getView();

        FloatingActionButton locFab = getView().findViewById(R.id.fab_1);
        locFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isFirstOpened = true;
                fetchLastLocation();
            }
        });

        FloatingActionButton navFab = getView().findViewById(R.id.fab_2);
        navFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                LatLonCoordinate latLonCoordinate = ((MainActivity)getActivity()).getLatLonCoordinate();
                LatLonCoordinate startingLatLonCoordinate = ((MainActivity) getActivity()).getStartingLatLonCoordinate();
                /**
                 * Logging for error testing
                 * */
                Log.v("Navigation_testing", "Starting Latitude: "+startingLatLonCoordinate.getLatitude() + " Starting Longitude: "+startingLatLonCoordinate.getLongitude());
                Log.v("Navigation_testing", "Starting Latitude: "+latLonCoordinate.getLatitude()+ " Starting Longitude: "+startingLatLonCoordinate.getLongitude());
                // Directions
                String nav_address = "http://maps.google.com/maps?daddr="
                        + latLonCoordinate.getLatitude() +", "
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
        });
        //mSearchText = mView.findViewById(R.id.input_search);

        init();
        setUpPlaceAutoComplete();
        //Viewpager
        viewPager = mView.findViewById(R.id.view_pager);

        fr_list.add(new ProximityFragment());
        fr_list.add(new PriceFragment());
        fr_list.add(new AvailabilityFragment());
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

        super.onViewCreated(view, savedInstanceState);
    }

    private static final LatLngBounds LAT_LNG_BOUNDS = new LatLngBounds(new LatLng(-40,-168), new LatLng(71,136));
    final private String apiKey  = "AIzaSyCQqUq9JUexPU9MF3c3KYhRUY42sBoiC6w";
    private PlacesClient placesClient;


    private void init() {
        // Initialize Places.
        if(!Places.isInitialized())
            Places.initialize(getActivity(), apiKey);
        // Create a new Places client instance.
        placesClient = Places.createClient(getActivity());
    }

    private void setUpPlaceAutoComplete(){
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
                currentLocation.setLatitude(lat_places);
                currentLocation.setLongitude(long_places);
                markerPrimary = new MarkerOptions().position(place.getLatLng()).title(place.getName());
                MainActivity.setLatLonCoordinate(new LatLonCoordinate(lat_places, long_places));
                mapFragment.getMapAsync(Main_Fragment.this);
                //((FragmentCanali) mMyAdapter.fr_list.get(0)).refresh();
                //viewPager.invalidate();
                viewPager.getAdapter().notifyDataSetChanged();
            }

            @Override
            public void onError(@NonNull Status status) {
                Toast.makeText(getContext(), ""+status.getStatusMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private String TAG = "Main_Fragment";

    private LatLonCoordinate currentCoord;
    private boolean isFirstOpened = true; //for the very first marker

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

        } else{
            //animates in on the map showing singapore
            LatLng latLng = new LatLng(default_lat,  default_long);
            map.clear();
            map.animateCamera(CameraUpdateFactory.newLatLng(latLng));
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 10));
            map.addMarker(new MarkerOptions().position(latLng).title("Singapore"));
            //map.moveCamera(CameraUpdateFactory.newLatLng(latLng));
            currentCoord = new LatLonCoordinate(latLng.latitude, latLng.longitude);
        }
    }

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
        if(markerPrimary != null) map.addMarker(markerPrimary);
        map.addMarker(options).setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));

    }

}