package com.example.parkevolution1;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.asksira.dropdownview.DropDownView;
import com.asksira.dropdownview.OnDropDownSelectionListener;
import com.google.android.gms.maps.model.LatLng;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;


public class ProximityFragment extends Fragment {

    public static SVY21Coordinate currentSVY21Location;
    private List<CarPark> carParks = new ArrayList<>();
    private CarPark[] cpArray = new CarPark[50];
    private CarPark[] cpArrayReverse = new CarPark[50];
    private CarPark[] cpArrayOriginal = new CarPark[50];

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_proximity, container, false);
    }

    private ListView listView;
    //private DropDownView dropDownView;
    //private List<String> yourFilterList = Arrays.asList("Nearest to Furthest", "Furthest to Nearest");
    private Spinner spinner;
    private static final String[] paths = {"Nearest to Furthest", "Furthest to Nearest"};
    private boolean ascending = true; //the carparks are arranged in ascending order

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        //setting the currentSVY21Location for testing purposes
        LatLonCoordinate testCoordinate = new LatLonCoordinate(1.344261, 103.720750);
        currentSVY21Location = testCoordinate.asSVY21();

        LatLonCoordinate realCoordinate = MainActivity.getLatLonCoordinate();
        if(realCoordinate != null){
            currentSVY21Location = realCoordinate.asSVY21();
            Log.v("Location_test", "Real coordinate for proximity is obtained");
        }

        listView = getView().findViewById(R.id.proximityListView);
        spinner = getView().findViewById(R.id.spinner);

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_item, paths);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        spinner.setSelection(0, true);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                switch (i){
                    case 0:
                        if(ascending != true){
                            ascending = true;
                            //update the list view
                            cpArray = Arrays.copyOf(cpArrayOriginal, cpArrayOriginal.length);
                            listView.setAdapter(new MyProximityAdapter(getContext(), cpArray));
                        }
                        break;
                    case 1:
                        if(ascending == true){
                            ascending = false;
                            //update the list view
                            cpArray = Arrays.copyOf(cpArrayReverse, cpArrayReverse.length);
                            listView.setAdapter(new MyProximityAdapter(getContext(), cpArray));

                            for(CarPark c: cpArray){
                                Log.v("Reverse-test", c.getAddress());
                            }
                        }
                        break;
                    default:
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });


//        dropDownView = getView().findViewById(R.id.dropdownview);
//        dropDownView.setDropDownListItem(yourFilterList);
//        dropDownView.setOnSelectionListener(new OnDropDownSelectionListener() {
//            @Override
//            public void onItemSelected(DropDownView dropDownView, int position) {
//                Toast.makeText(getContext(), "Selected one: "+position, Toast.LENGTH_LONG).show();
//            }
//        });

        if(currentSVY21Location != null){
            readCarParkData();
            displayCarparks_distance();
        }

        super.onViewCreated(view, savedInstanceState);
    }

    private void readCarParkData(){
        carParks.clear();
        InputStream is = getActivity().getResources().openRawResource(R.raw.hdb_carparks4_1);
        BufferedReader reader = new BufferedReader(
                new InputStreamReader(is, Charset.forName("UTF-8"))
        );
        String line = "";
        int lineCounterHDB = 0;
        try{
            while((line = reader.readLine()) != null){
                //split by ","
                String[] tokens = line.split(",");

                //Read the data
                CarPark carPark = new CarPark();
                carPark.setId(tokens[1]);
                carPark.setName(tokens[4]);
                carPark.setAddress(tokens[4]);
                carPark.setHdb_car_parking_rate(tokens[5]);
                //carPark.setHdb_motorcycle_parking_rate(tokens[5]);
                try{
                    Log.v("x-cord", tokens[2]);
                    carPark.setX_coord(Double.parseDouble(tokens[2]));
                } catch(NumberFormatException e){
                    e.printStackTrace();
                    carPark.setX_coord(0);
                }
                try{
                    Log.v("y-cord", tokens[3]);
                    carPark.setY_coord(Double.parseDouble(tokens[3]));
                }
                catch(NumberFormatException e){
                    e.printStackTrace();
                    carPark.setY_coord(0);
                }

                String favBool = tokens[0];
                if(favBool.equals("TRUE")){
                    carPark.setIsFavourite(true);
                } else {
                    carPark.setIsFavourite(false);
                }
                carPark.setFileLineNum(lineCounterHDB);
                carPark.setIsHDB(true);
                carPark.setDist(1000000); //initialize all distance to 100000 first
                carPark.setDataCategory(CarPark.DataCategory.HDB);
                carParks.add(carPark);
                lineCounterHDB++;
            }
        }catch(IOException e){
            Log.wtf("MyActivity", "Error reading data file on line" + line, e);
            e.printStackTrace();
        }

        //Reading data from the malls file
        InputStream is2 = getActivity().getResources().openRawResource(R.raw.malls2);
        BufferedReader reader2 = new BufferedReader(
                new InputStreamReader(is2, Charset.forName("UTF-8"))
        );

        int lineCounterMalls = 0;
        try{
            while((line = reader2.readLine()) != null){
                //split by ","
                String[] tokens = line.split(",");

                //Read the data
                CarPark carPark =  new CarPark();
                carPark.setName(tokens[1]);
                carPark.setAddress(tokens[1]);
                try{
                    SVY21Coordinate svy21Coordinate = new LatLonCoordinate(
                            Double.parseDouble(tokens[2]),
                            Double.parseDouble(tokens[3])
                    ).asSVY21();
                    carPark.setX_coord(svy21Coordinate.getEasting());
                    carPark.setY_coord(svy21Coordinate.getNorthing());
                } catch (NumberFormatException e){
                    e.printStackTrace();
                    Log.v("Mall_Data", "Error in reading in coords "+
                            tokens[2] + " " + tokens[3]);
                }
                carPark.setMall_weekday_rate1(tokens[4]);
                carPark.setMall_weekday_rate2(tokens[5]);
                carPark.setMall_sat_rate(tokens[6]);
                carPark.setMall_sun_rate(tokens[7]);
                carPark.setDist(1000000); //initialize all distance to that first
                carPark.setDataCategory(CarPark.DataCategory.SHOPPING_MALL);

                String favBool = tokens[0];
                if(favBool.equals("TRUE")){
                    carPark.setIsFavourite(true);
                } else {
                    carPark.setIsFavourite(false);
                }
                carPark.setIsHDB(false);
                carPark.setFileLineNum(lineCounterMalls);
                carParks.add(carPark);
                lineCounterMalls++;
            }
        } catch(IOException e){
            Log.d("Mall_Data", "Error reading data on file" + line, e);
            e.printStackTrace();
        }
    }

    MyProximityAdapter myProximityAdapter;
    //this method can be called once initially and everytime the current location FAB is pressed
    private void displayCarparks_distance(){
        //setting the square hypotenuse distance for each carparks in the entre array list -> O(n)
        for(CarPark carPark: carParks){
            carPark.setDist(Math.pow(carPark.getX_coord() - currentSVY21Location.getEasting(), 2) + Math.pow(carPark.getY_coord() - currentSVY21Location.getNorthing(), 2));
        }

        Log.v("Proximity_Data", "size of carparks proximity array: "+carParks.size());
        //displays the arraylist sorted by distance
        Collections.sort(carParks, new Comparator<CarPark>() {
            @Override
            public int compare(CarPark cp1, CarPark cp2) {
                return (cp1.getDist() > cp2.getDist() ? 1 : (cp1.getDist() < cp2.getDist() ?  -1 : 0));
            }
        });

        Location locationCurrent = new Location("Current");

        locationCurrent.setLatitude(currentSVY21Location.asLatLon().getLatitude());
        locationCurrent.setLongitude(currentSVY21Location.asLatLon().getLongitude());

        //locationCurrent.setLatitude(MainActivity.getLatLonCoordinate().getLatitude());
        //locationCurrent.setLongitude(MainActivity.getLatLonCoordinate().getLongitude());
        //transfer this data into the array
        for(int i=0; i<50; i++){
            cpArray[i] = carParks.get(i);
            //add in the distance data
            Location locationPoint = new Location("point");
            LatLonCoordinate pointCoord = (new SVY21Coordinate(cpArray[i].getY_coord(), cpArray[i].getX_coord())).asLatLon();
            locationPoint.setLatitude(pointCoord.getLatitude());
            locationPoint.setLongitude(pointCoord.getLongitude());
            double dist = locationCurrent.distanceTo(locationPoint);
            double rounded_dist = Math.round(dist*10)/10.0;
            cpArray[i].setDist(rounded_dist);
            //cpArray[i].setDist(getDuration(currentSVY21Location.asLatLon(), pointCoord));
        }

        cpArrayReverse = Arrays.copyOf(cpArray, cpArray.length);
        Collections.reverse(Arrays.asList(cpArrayReverse));
        cpArrayOriginal = Arrays.copyOf(cpArray, cpArray.length);

        //update the list view
        myProximityAdapter = new MyProximityAdapter(getContext(), cpArray);
        listView.setAdapter(myProximityAdapter);
    }

    class MyProximityAdapter extends ArrayAdapter<CarPark>{
        Context context;
        CarPark carPark;

        public MyProximityAdapter(Context c, CarPark[] carParks){
            super(c, 0, carParks);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent){
            //Get the data item for this position
            final CarPark carPark = getItem(position);
            //check if an existing view is being used, otherwise inflate new view
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.row_proximity, parent, false);
            }

            // Lookup view for data population
            TextView tvName = convertView.findViewById(R.id.proxomity_name);
            TextView tvProximity = convertView.findViewById(R.id.proximity_value);
            //Populate the data into the template view using the data object
            tvName.setText(carPark.getName());
            tvProximity.setText(Double.toString(carPark.getDist())+"m");
            ImageView arrow = convertView.findViewById(R.id.proximity_arrow);
            View mainV = convertView.findViewById(R.id.mainV_proximity);

            mainV.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    LatLonCoordinate latLonCoordinateCP = (new SVY21Coordinate(carPark.getY_coord(), carPark.getX_coord())).asLatLon();

                    //the selected latLong will get updated to the location of the selected carpark
                    ((MainActivity)getActivity()).setSelectedLatLonCoordinate(latLonCoordinateCP);

                    LatLng latLng = new LatLng(latLonCoordinateCP.getLatitude(), latLonCoordinateCP.getLongitude());
                    Main_Fragment.addMarkerToMap(latLng, carPark.getName());
                }
            });
            arrow.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ProximityDetailFragment proximityDetailFragment = new ProximityDetailFragment();
                    Bundle bundle = new Bundle();

                    //setting the selected location in MainActivity
                    LatLonCoordinate latLonCoordinateCP1 = (new SVY21Coordinate(carPark.getY_coord(), carPark.getX_coord())).asLatLon();
                    ((MainActivity)getActivity()).setSelectedLatLonCoordinate(latLonCoordinateCP1);

                    //pass in the data here
                    Geocoder geocoder;
                    List<Address> addresses;
                    switch(carPark.getDataCategory()){
                        case HDB:
                            bundle.putString("carpark-name", carPark.getName());
                            //Address data
                            geocoder = new Geocoder(getActivity(), Locale.getDefault());
                            try {
                                LatLonCoordinate latLonCoordinateCP = (new SVY21Coordinate(carPark.getY_coord(), carPark.getX_coord())).asLatLon();
                                addresses = geocoder.getFromLocation(latLonCoordinateCP.getLatitude(), latLonCoordinateCP.getLongitude(), 1);
                                bundle.putString("carpark-address", addresses.get(0).getAddressLine(0));
                                bundle.putDouble("x-coord", latLonCoordinateCP.getLatitude());
                                bundle.putDouble("y-coord", latLonCoordinateCP.getLongitude());
                                bundle.putString("data-cat", "HDB");
                                bundle.putString("hdb_car_parking_rate", carPark.getHdb_car_parking_rate());
                                //bundle.putString("hdb_motorcycle_parking_rate", carPark.getHdb_motorcycle_parking_rate());
                                bundle.putString("carpark-id", carPark.getId());


                                //Information for favourite carpark
                                bundle.putInt("fileLineNum", carPark.getFileLineNum());
                                bundle.putBoolean("isFavourite", carPark.getIsFavourite());
                                bundle.putBoolean("isHDB", carPark.getIsHDB());


                            } catch (IOException e) {
                                e.printStackTrace();
                                Log.v("Location_result", "Get address for the carpark list view isn't working");
                            }
                            break;
                        case SHOPPING_MALL:
                            bundle.putString("carpark-name", carPark.getName());
                            //Address data
                            geocoder = new Geocoder(getActivity(), Locale.getDefault());
                            try {
                                LatLonCoordinate latLonCoordinateCP = (new SVY21Coordinate(carPark.getY_coord(), carPark.getX_coord())).asLatLon();
                                addresses = geocoder.getFromLocation(latLonCoordinateCP.getLatitude(), latLonCoordinateCP.getLongitude(), 1);
                                bundle.putString("carpark-address", addresses.get(0).getAddressLine(0));
                                bundle.putDouble("x-coord", latLonCoordinateCP.getLatitude());
                                bundle.putDouble("y-coord", latLonCoordinateCP.getLongitude());
                                bundle.putString("data-cat", "SHOPPING_MALL");
                                bundle.putString("shopping-weekday1", carPark.getMall_weekday_rate1());
                                bundle.putString("shopping-weekday2", carPark.getMall_weekday_rate2());
                                bundle.putString("shopping-sat", carPark.getMall_sat_rate());
                                bundle.putString("shopping-sun", carPark.getMall_sun_rate());


                                //Information for favourite carpark
                                bundle.putInt("fileLineNum", carPark.getFileLineNum());
                                bundle.putBoolean("isFavourite", carPark.getIsFavourite());
                                bundle.putBoolean("isHDB", carPark.getIsHDB());

                            } catch (IOException e) {
                                e.printStackTrace();
                                Log.v("Location_result", "Get address for the carpark list view isn't working");
                            }
                            break;
                        case AVAILABILITY:
                            //code
                            break;
                        default:
                                break;
                    }
                    proximityDetailFragment.setArguments(bundle);
                    getActivity().getSupportFragmentManager().beginTransaction()
                            .setTransition(FragmentTransaction.TRANSIT_ENTER_MASK)
                            .add(R.id.main_fragment, proximityDetailFragment)
                            .addToBackStack(null)
                            .commit();
                }
            });
            return convertView;
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        spinner.setSelection(0);
    }
}
