package com.example.parkevolution1;


import android.content.Context;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static android.content.Context.MODE_PRIVATE;


/**
 * A simple {@link Fragment} subclass.
 */
public class FavouriteCarparksFragment extends Fragment {

    private EditText filterText;
    private ArrayAdapter<CarPark> listAdapter;

    public FavouriteCarparksFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        //set State
        ((MainActivity)getActivity()).setState(2);

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_favourite_carparks, container, false);
    }

    private ArrayList<CarPark> carParks = new ArrayList<>();
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        filterText = getView().findViewById(R.id.editText);

        ListView itemList = getView().findViewById(R.id.listView);


        //Edit this part so that all the favourite car park names from both HDB list and the Malls list are displayed
        String [] listViewAdapterContent = {"School", "House", "Building", "Food", "Sports", "Dress", "Ring"};

        ArrayList<String> favCarParks = new ArrayList<>();
        carParks.clear();
        //retrieve all the favourite HDB carparks
        /**
         * 1. open the csv file
         * 2. get the string tokens from the shared preference
         * 3. Loop throught the shared preference and csv file at the same time
         *      3.1. If TRUE -> Get out the carpark name and relevant information
         *      3.2 if False -> Do nothing, continue
         * */
        InputStream is = getActivity().getResources().openRawResource(R.raw.hdb_carparks4_1);
        BufferedReader reader = new BufferedReader(
                new InputStreamReader(is, Charset.forName("UTF-8"))
        );

        SharedPreferences favouritePrefs = getActivity().getSharedPreferences("HDBFavouritePrefs", MODE_PRIVATE);
        String totalHDBFavouriteString = favouritePrefs.getString("favouriteHDBString", null);
        String[] currBoolTokens = totalHDBFavouriteString.split(",");

        String line = "";
        int lineNum = 0;
        try{
            while((line = reader.readLine()) != null){
                if(currBoolTokens[lineNum].equals("TRUE")) {
                    //Split by ","
                    String[] tokens = line.split(",");
                    //Read the data
                    CarPark carPark = new CarPark();
                    carPark.setId(tokens[1]);
                    carPark.setName(tokens[4]);

                    //test line
                    favCarParks.add(tokens[4]);


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
                    carPark.setFileLineNum(lineNum);
                    carPark.setIsHDB(true);
                    carPark.setDist(1000000); //initialize all distance to 100000 first
                    carPark.setDataCategory(CarPark.DataCategory.HDB);
                    carParks.add(carPark);
                }
                lineNum++;
            }
        } catch(IOException e){
            e.printStackTrace();
        }

        //retrieve all the favourite Malls carparks

        InputStream is2 = getActivity().getResources().openRawResource(R.raw.malls2);
        BufferedReader reader2 = new BufferedReader(
                new InputStreamReader(is2, Charset.forName("UTF-8"))
        );

        SharedPreferences favouriteMallsPrefs = getActivity().getSharedPreferences("MallsFavouritePrefs", MODE_PRIVATE);
        String totalMallFavouriteString = favouriteMallsPrefs.getString("favouriteMallString", null);
        String[] currMallBoolTokens = totalMallFavouriteString.split(",");

        String line2 = "";
        int lineNum2 = 0;
        try{
            while((line2 = reader2.readLine()) != null) {
                if (currMallBoolTokens[lineNum2].equals("TRUE")) {
                    //split by ","
                    String[] tokens = line2.split(",");

                    //Read the data
                    CarPark carPark =  new CarPark();
                    carPark.setName(tokens[1]);

                    //test line
                    favCarParks.add(tokens[1]);

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
                    carPark.setFileLineNum(lineNum2);
                    carParks.add(carPark);
                }
                lineNum2++;
            }
        } catch(IOException e){
            e.printStackTrace();
        }

        //listAdapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_list_item_1, android.R.id.text1, favCarParks);

        listAdapter = new MyProximityAdapter(getContext(), carParks);
        itemList.setAdapter(listAdapter);

        itemList.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
// make Toast when click
                Toast.makeText(getContext(), "Position " + position, Toast.LENGTH_LONG).show();
            }
        });
        filterText.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                listAdapter.getFilter().filter(s);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    class MyProximityAdapter extends ArrayAdapter<CarPark>{
        Context context;
        CarPark carPark;

        public MyProximityAdapter(Context c, ArrayList<CarPark> carParks){
            super(c, 0, carParks);
        }

        @androidx.annotation.NonNull
        @Override
        public View getView(int position, @androidx.annotation.Nullable View convertView, @androidx.annotation.NonNull ViewGroup parent) {
            final CarPark carPark = getItem(position);
            if(convertView == null){
                convertView = LayoutInflater.from(getContext()).inflate(android.R.layout.simple_list_item_1, parent, false);
            }

            TextView tvName = convertView.findViewById(android.R.id.text1);
            tvName.setText(carPark.getName());

            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    ProximityDetailFragment proximityDetailFragment = new ProximityDetailFragment();
                    Bundle bundle = new Bundle();

                    //pass in the data here
                    Geocoder geocoder;
                    List<Address> addresses;

                    if(carPark.getIsHDB()){
                        //HDB
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
                    } else {
                        //Shopping mall or some other data
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
                    }

                    proximityDetailFragment.setArguments(bundle);
                    getActivity().getSupportFragmentManager().beginTransaction()
                            .setTransition(FragmentTransaction.TRANSIT_ENTER_MASK)
                            .replace(R.id.main_fragment, proximityDetailFragment)
                            .addToBackStack(null)
                            .commit();
                }
            });
            return convertView;
        }
    }
}
