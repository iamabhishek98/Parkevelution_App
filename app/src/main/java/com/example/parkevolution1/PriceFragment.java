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

import com.google.android.gms.maps.model.LatLng;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;


public class PriceFragment extends Fragment {

    public SVY21Coordinate currentSVY21Location;
    private List<CarPark> carParks = new ArrayList<>();

    //carparks by distance
    private CarPark[] cpArray = new CarPark[50];
    private CarPark[] cpArrayReverse = new CarPark[50];
    private CarPark[] cpArrayOriginal = new CarPark[50];

    //carparks by price
    private CarPark[] cpArrayPrice = new CarPark[50];
    private CarPark[] cpArrayPriceReverse = new CarPark[50];

    public PriceFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_price, container, false);
    }

    private ListView listView;
    private Spinner spinner;
    private static final String[] paths = {"Default", "Cheapest First", "Cheapest Last"};

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        //setting the currentSVY21Location for testing purposes
        LatLonCoordinate testCoordinate = new LatLonCoordinate(1.344261, 103.720750);
        currentSVY21Location = testCoordinate.asSVY21();

        LatLonCoordinate realCoordinate = MainActivity.getLatLonCoordinate();
        if(realCoordinate != null){
            currentSVY21Location = realCoordinate.asSVY21();
            Log.v("Location_test", "Real coordinate for PRICE is obtained");
        }

        listView = getView().findViewById(R.id.priceListView);
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
                        listView.setAdapter(new MyPriceAdapter(getContext(), cpArray));
                        break;
                    case 1:
                        listView.setAdapter(new MyPriceAdapter(getContext(), cpArrayPrice));
                        break;
                    case 2:
                        listView.setAdapter(new MyPriceAdapter(getContext(), cpArrayPriceReverse));
                    default:
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        if(currentSVY21Location != null){
            readCarParkData();
            displayCarparks_price();
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
        InputStream is2 = getActivity().getResources().openRawResource(R.raw.malls3);
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

                //GET OUT ALL THE ABHISHEK DATA
                carPark.setMall_weekday_24rate(tokens[8]);
                carPark.setMall_weekday_rates(tokens[9]);
                carPark.setMall_saturday_24rate(tokens[10]);
                carPark.setMall_saturday_rates(tokens[11]);
                carPark.setMall_sunday_24rate(tokens[12]);
                carPark.setMall_sunday_rates(tokens[13]);


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

    //this method can be called once initially and everytime the current location FAB is pressed
    private void displayCarparks_price(){
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
        }


        /**
         * Here we have to do allocation of the hourly prices for the carparks
         * */


        setHourlyPriceForSelectedCarparks(cpArray);
        //set cpArrayPrice and cpArrayPriceReverse

        cpArrayOriginal = Arrays.copyOf(cpArray, cpArray.length);

        cpArrayPrice = Arrays.copyOf(cpArray, cpArray.length);
        Arrays.sort(cpArrayPrice, new Comparator<CarPark>() {
            @Override
            public int compare(CarPark carPark, CarPark t1) {
                if(carPark.getHourly_price() - t1.getHourly_price() > 0){
                    return 1;
                } else  if(carPark.getHourly_price() - t1.getHourly_price() < 0){
                    return -1;
                } else {
                    return 0;
                }

            }
        });

        cpArrayPriceReverse = Arrays.copyOf(cpArrayPrice, cpArray.length);
        Collections.reverse(Arrays.asList(cpArrayPriceReverse));

        //cpArrayReverse = Arrays.copyOf(cpArray, cpArray.length);
        //Collections.reverse(Arrays.asList(cpArrayReverse));


        //update the list view
        MyPriceAdapter myProximityAdapter = new MyPriceAdapter(getContext(), cpArray);
        listView.setAdapter(myProximityAdapter);


    }

    private void setHourlyPriceForSelectedCarparks(CarPark[] carparks){

        for(int i=0; i<carparks.length; i++){
            CarPark currCp = carparks[i];
            if (currCp.getDataCategory() == CarPark.DataCategory.HDB) {
                if(currCp.getHdb_car_parking_rate().equals("$0.60 per half-hour")){
                    currCp.setHourly_price(1.2);
                } else if(currCp.getHdb_car_parking_rate().equals("$1.20 per half-hour")){
                    currCp.setHourly_price(2.4);
                }
            } else {
                //a shopping mall carpark

                //start
                Calendar calendar = Calendar.getInstance();
                int day = calendar.get(Calendar.DAY_OF_WEEK);
                int closed_val = 1000000;
                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
                String str = sdf.format(new Date());
                Log.v("ABHISHEK BOI", str);
                switch (day) {
                    case Calendar.SUNDAY:
                        if(!(currCp.getMall_sunday_24rate()).equals("")){
                            if((currCp.getMall_sunday_24rate()).equals("F")){
                                currCp.setHourly_price(0);
                            } else if((currCp.getMall_sunday_24rate()).equals("C")){
                                currCp.setHourly_price(closed_val);
                            } else {
                                try{
                                    currCp.setHourly_price(Double.parseDouble(currCp.getMall_sunday_24rate()));
                                } catch(NumberFormatException e){
                                    currCp.setHourly_price(closed_val);
                                    e.printStackTrace();
                                }
                            }
                        } else {
                            String[] timeComponents = str.split(":");
                            double currHr = Double.parseDouble(timeComponents[0]);
                            double currMin = Double.parseDouble(timeComponents[1]);
                            double currAbhiTime = currHr*100+currMin;

                            String[] tokens = currCp.getMall_sunday_rates().split(";");
                            for(String x : tokens){
                                String[] pair = x.split(":");
                                String timeRange  = pair[0];

                                String[] times = timeRange.split("-");


                                //this is my aaaa
                                double timeLow = Double.parseDouble(times[0]);
                                //if(times[1].equals("0000")) times[1] = "2359";
                                //this is my bbbb
                                double timeHigh = Double.parseDouble(times[1]);

                                int a_hr = (int)timeLow/100;
                                int a_min = (int)timeLow%100;

                                int b_hr = (int)timeHigh/100;
                                int b_min = (int) timeHigh%100;

                                boolean abhiCond = (currHr == a_hr && currMin < a_min) || (currHr == b_hr && currMin > b_min);
                                if(b_hr > a_hr){
                                    if(currHr >= a_hr && currHr <= b_hr){
                                        //check minutes also
                                        if(abhiCond) continue;
                                        else{
                                            //abhiPrice = x;
                                            if(pair[1].equals(" F")){
                                                currCp.setHourly_price(0);
                                            } else if(pair[1].equals(" C")){
                                                currCp.setHourly_price(closed_val);
                                            } else {
                                                currCp.setHourly_price(Double.parseDouble(pair[1].substring(1)));
                                            }
                                            break;
                                        }
                                    }
                                } else {
                                    if(currHr >= a_hr && currHr <= 23 || currHr <= b_hr){
                                        if(abhiCond) continue;
                                        else {
                                            //abhiPrice = x;
                                            if(pair[1].equals(" F")){
                                                currCp.setHourly_price(0);
                                            } else if(pair[1].equals(" C")){
                                                currCp.setHourly_price(closed_val);
                                            } else {
                                                currCp.setHourly_price(Double.parseDouble(pair[1].substring(1)));
                                            }
                                            break;
                                        }
                                    }
                                }
                            }
                        }
                        break;
                    case Calendar.SATURDAY:
                        if(!(currCp.getMall_saturday_24rate()).equals("")){
                            if((currCp.getMall_saturday_24rate()).equals("F")){
                                currCp.setHourly_price(0);
                            } else if((currCp.getMall_saturday_24rate()).equals("C")){
                                currCp.setHourly_price(closed_val);
                            } else {
                                try{
                                    currCp.setHourly_price(Double.parseDouble(currCp.getMall_saturday_24rate()));
                                } catch (NumberFormatException e){
                                    e.printStackTrace();
                                    currCp.setHourly_price(closed_val);
                                }
                            }
                        } else {
                            String[] timeComponents = str.split(":");
                            double currHr = Double.parseDouble(timeComponents[0]);
                            double currMin = Double.parseDouble(timeComponents[1]);
                            double currAbhiTime = currHr*100+currMin;

                            String[] tokens = currCp.getMall_saturday_rates().split(";");
                            Log.v("Cp-post-debug", "Name: "+currCp.getName());
                            for(String x : tokens){
                                String[] pair = x.split(":");
                                String timeRange  = pair[0];

                                String[] times = timeRange.split("-");
                                //this is my aaaa
                                double timeLow = Double.parseDouble(times[0]);
                                //if(times[1].equals("0000")) times[1] = "2359";
                                //this is my bbbb
                                double timeHigh = Double.parseDouble(times[1]);

                                int a_hr = (int)timeLow/100;
                                int a_min = (int)timeLow%100;

                                int b_hr = (int)timeHigh/100;
                                int b_min = (int) timeHigh%100;

                                boolean abhiCond = (currHr == a_hr && currMin < a_min) || (currHr == b_hr && currMin > b_min);
                                if(b_hr >= a_hr){
                                    if(currHr >= a_hr && currHr <= b_hr){
                                        //check minutes also
                                        if(abhiCond) continue;
                                        else{
                                            //abhiPrice = x;
                                            if(pair[1].equals(" F")){
                                                currCp.setHourly_price(0);
                                            } else if(pair[1].equals(" C")){
                                                currCp.setHourly_price(closed_val);
                                            } else {
                                                currCp.setHourly_price(Double.parseDouble(pair[1].substring(1)));
                                            }
                                            break;
                                        }
                                    }
                                } else {
                                    if(currHr >= a_hr && currHr <= 23 || currHr <= b_hr){
                                        if(abhiCond) continue;
                                        else {
                                            //abhiPrice = x;
                                            if(pair[1].equals(" F")){
                                                currCp.setHourly_price(0);
                                            } else if(pair[1].equals(" C")){
                                                currCp.setHourly_price(closed_val);
                                            } else {
                                                currCp.setHourly_price(Double.parseDouble(pair[1].substring(1)));
                                            }
                                            break;
                                        }
                                    }
                                }
                            }
                        }
                        break;
                    default: //Weekday
                        if(!(currCp.getMall_weekday_24rate()).equals("")){
                            if((currCp.getMall_weekday_24rate()).equals("F")){
                                currCp.setHourly_price(0);
                            } else if((currCp.getMall_weekday_24rate()).equals("C")){
                                currCp.setHourly_price(closed_val);
                            } else {
                                try{
                                    currCp.setHourly_price(Double.parseDouble(currCp.getMall_weekday_24rate()));
                                } catch(NumberFormatException e){
                                    e.printStackTrace();
                                    currCp.setHourly_price(closed_val);
                                }

                            }
                        } else {
                            Log.v("MyBugProb", currCp.getName());
                            String[] timeComponents = str.split(":");
                            double currHr = Double.parseDouble(timeComponents[0]);
                            double currMin = Double.parseDouble(timeComponents[1]);
                            double currAbhiTime = currHr*100+currMin;

                            String[] tokens = currCp.getMall_weekday_rates().split(";");
                            for(String x : tokens){
                                String[] pair = x.split(":");
                                String timeRange  = pair[0];

                                String[] times = timeRange.split("-");
                                //this is my aaaa
                                double timeLow = Double.parseDouble(times[0]);
                                //if(times[1].equals("0000")) times[1] = "2359";
                                //this is my bbbb
                                double timeHigh = Double.parseDouble(times[1]);

                                int a_hr = (int)timeLow/100;
                                int a_min = (int)timeLow%100;

                                int b_hr = (int)timeHigh/100;
                                int b_min = (int) timeHigh%100;

                                boolean abhiCond = (currHr == a_hr && currMin < a_min) || (currHr == b_hr && currMin > b_min);
                                if(b_hr > a_hr){
                                    if(currHr >= a_hr && currHr <= b_hr){
                                        //check minutes also
                                        if(abhiCond) continue;
                                        else{
                                            //abhiPrice = x;
                                            if(pair[1].equals(" F")){
                                                currCp.setHourly_price(0);
                                            } else if(pair[1].equals(" C")){
                                                currCp.setHourly_price(closed_val);
                                            } else {
                                                currCp.setHourly_price(Double.parseDouble(pair[1].substring(1)));
                                            }
                                            break;
                                        }
                                    }
                                } else {
                                    if(currHr >= a_hr && currHr <= 23 || currHr <= b_hr){
                                        if(abhiCond) continue;
                                        else {
                                            //abhiPrice = x;
                                            if(pair[1].equals(" F")){
                                                currCp.setHourly_price(0);
                                            } else if(pair[1].equals(" C")){
                                                currCp.setHourly_price(closed_val);
                                            } else {
                                                currCp.setHourly_price(Double.parseDouble(pair[1].substring(1)));
                                            }
                                            break;
                                        }
                                    }
                                }
                            }
                        }
                        break;
                }

                //end
            }
            carparks[i] = currCp;
        }


    }

    class MyPriceAdapter extends ArrayAdapter<CarPark> {
        Context context;
        CarPark carPark;

        public MyPriceAdapter(Context c, CarPark[] carParks){
            super(c, 0, carParks);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent){
            //Get the data item for this position
            final CarPark carPark = getItem(position);
            //check if an existing view is being used, otherwise inflate new view
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.row_price, parent, false);
            }

            // Lookup view for data population
            TextView tvName = convertView.findViewById(R.id.price_name);
            TextView tvPrice = convertView.findViewById(R.id.price_value);
            //Populate the data into the template view using the data object
            tvName.setText(carPark.getName());

            //setting the price information
            String priceInfo = "";
            String abhiPrice = "";
            if (carPark.getDataCategory() == CarPark.DataCategory.HDB) {
                abhiPrice += "Car Rate: "+carPark.getHdb_car_parking_rate() + "\n"
                                + "Motorcycle Rate: $0.65/lot";
            } else{
                /******************************
                 * ABHISHEK'S PRICE ALGORITHM
                 *                            GOES HERE
                 *
                 * */
                //shopping mall data
                Calendar calendar = Calendar.getInstance();
                int day = calendar.get(Calendar.DAY_OF_WEEK);

                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
                String str = sdf.format(new Date());
                Log.v("ABHISHEK BOI", str);
                switch (day) {
                    case Calendar.SUNDAY:
                        if(!(carPark.getMall_sunday_24rate()).equals("")){
                            if((carPark.getMall_sunday_24rate()).equals("F")){
                                abhiPrice = "Free for the first hour";
                            } else if((carPark.getMall_sunday_24rate()).equals("C")){
                                abhiPrice = "Closed during this hour";
                            }  else if((carPark.getMall_weekday_24rate()).equals("S")){
                                abhiPrice = "Season Parking";
                            }else if((carPark.getMall_weekday_24rate()).equals("H")) {
                                abhiPrice = "HDB Coupon";
                            } else if((carPark.getMall_weekday_24rate()).equals("T")) {
                                abhiPrice = "Tenant Only";
                            } else {
                                abhiPrice = "Car Rate: $"+carPark.getMall_sunday_24rate()+" for the first 1hr";
                            }
                        } else {
                            String[] timeComponents = str.split(":");
                            double currHr = Double.parseDouble(timeComponents[0]);
                            double currMin = Double.parseDouble(timeComponents[1]);
                            double currAbhiTime = currHr*100+currMin;

                            String[] tokens = carPark.getMall_sunday_rates().split(";");
                            for(String x : tokens){
                                String[] pair = x.split(":");
                                String timeRange  = pair[0];

                                String[] times = timeRange.split("-");
                                Log.v("Samuel errorrr", "Carparkname: "+ carPark.getName());

                                //this is my aaaa
                                double timeLow = Double.parseDouble(times[0]);
                                //if(times[1].equals("0000")) times[1] = "2359";
                                //this is my bbbb
                                double timeHigh = Double.parseDouble(times[1]);

                                int a_hr = (int)timeLow/100;
                                int a_min = (int)timeLow%100;

                                int b_hr = (int)timeHigh/100;
                                int b_min = (int) timeHigh%100;

                                boolean abhiCond = (currHr == a_hr && currMin < a_min) || (currHr == b_hr && currMin > b_min);
                                if(b_hr > a_hr){
                                    if(currHr >= a_hr && currHr <= b_hr){
                                        //check minutes also
                                        if(abhiCond) continue;
                                        else{
                                            //abhiPrice = x;
                                            if(pair[1].equals(" F")){
                                                abhiPrice = "Free for the first hour";
                                            } else if(pair[1].equals(" C")){
                                                abhiPrice = "Closed during this hour";
                                            } else {
                                                abhiPrice = "Car Rate: $"+pair[1].substring(1);
                                                abhiPrice += " for the first hour";
                                            }
                                            break;
                                        }
                                    }
                                } else {
                                    if(currHr >= a_hr && currHr <= 23 || currHr <= b_hr){
                                        if(abhiCond) continue;
                                        else {
                                            //abhiPrice = x;
                                            if(pair[1].equals(" F")){
                                                abhiPrice = "Free for the first hour";
                                            } else if(pair[1].equals(" C")){
                                                abhiPrice = "Closed during this hour";
                                            } else {
                                                abhiPrice = "Car Rate: $"+pair[1].substring(1);
                                                abhiPrice += " for the first hour";
                                            }
                                            break;
                                        }
                                    }
                                }
                            }
                        }
                        break;
                    case Calendar.SATURDAY:
                        if(!(carPark.getMall_saturday_24rate()).equals("")){
                            if((carPark.getMall_saturday_24rate()).equals("F")){
                                abhiPrice = "Free for the first hour";
                            } else if((carPark.getMall_saturday_24rate()).equals("C")){
                                abhiPrice = "Closed during this hour";
                            }  else if((carPark.getMall_weekday_24rate()).equals("S")){
                                abhiPrice = "Season Parking";
                            }else if((carPark.getMall_weekday_24rate()).equals("H")) {
                                abhiPrice = "HDB Coupon";
                            } else if((carPark.getMall_weekday_24rate()).equals("T")) {
                                abhiPrice = "Tenant Only";
                            } else {
                                abhiPrice = "Car Rate: $"+carPark.getMall_saturday_24rate()+" for the first 1hr";
                            }
                        } else {
                            String[] timeComponents = str.split(":");
                            double currHr = Double.parseDouble(timeComponents[0]);
                            double currMin = Double.parseDouble(timeComponents[1]);
                            double currAbhiTime = currHr*100+currMin;

                            String[] tokens = carPark.getMall_saturday_rates().split(";");
                            for(String x : tokens){
                                String[] pair = x.split(":");
                                String timeRange  = pair[0];

                                String[] times = timeRange.split("-");
                                //this is my aaaa
                                double timeLow = Double.parseDouble(times[0]);
                                //if(times[1].equals("0000")) times[1] = "2359";
                                //this is my bbbb
                                double timeHigh = Double.parseDouble(times[1]);

                                int a_hr = (int)timeLow/100;
                                int a_min = (int)timeLow%100;

                                int b_hr = (int)timeHigh/100;
                                int b_min = (int) timeHigh%100;

                                boolean abhiCond = (currHr == a_hr && currMin < a_min) || (currHr == b_hr && currMin > b_min);
                                if(b_hr >= a_hr){
                                    if(currHr >= a_hr && currHr <= b_hr){
                                        //check minutes also
                                        if(abhiCond) continue;
                                        else{
                                            //abhiPrice = x;
                                            if(pair[1].equals(" F")){
                                                abhiPrice = "Free for the first hour";
                                            } else if(pair[1].equals(" C")){
                                                abhiPrice = "Closed during this hour";
                                            } else {
                                                abhiPrice = "Car Rate: $"+pair[1].substring(1);
                                                abhiPrice += " for the first hour";
                                            }
                                            break;
                                        }
                                    }
                                } else {
                                    if(currHr >= a_hr && currHr <= 23 || currHr <= b_hr){
                                        if(abhiCond) continue;
                                        else {
                                            //abhiPrice = x;
                                            if(pair[1].equals(" F")){
                                                abhiPrice = "Free for the first hour";
                                            } else if(pair[1].equals(" C")){
                                                abhiPrice = "Closed during this hour";
                                            } else {
                                                abhiPrice = "Car Rate: $"+pair[1].substring(1);
                                                abhiPrice += " for the first hour";
                                            }
                                            break;
                                        }
                                    }
                                }
                            }
                        }
                        break;
                    default: //Weekday
                        if(!(carPark.getMall_weekday_24rate()).equals("")){
                            if((carPark.getMall_weekday_24rate()).equals("F")){
                                abhiPrice = "Free for the first hour";
                            } else if((carPark.getMall_weekday_24rate()).equals("C")){
                                abhiPrice = "Closed during this hour";
                            } else if((carPark.getMall_weekday_24rate()).equals("S")){
                                abhiPrice = "Season Parking";
                            }else if((carPark.getMall_weekday_24rate()).equals("H")) {
                                abhiPrice = "HDB Coupon";
                            } else if((carPark.getMall_weekday_24rate()).equals("T")) {
                                abhiPrice = "Tenant Only";
                            } else {
                                abhiPrice = "Car Rate: $"+carPark.getMall_weekday_24rate()+" for the first 1hr";
                            }
                        } else {
                            String[] timeComponents = str.split(":");
                            double currHr = Double.parseDouble(timeComponents[0]);
                            double currMin = Double.parseDouble(timeComponents[1]);
                            double currAbhiTime = currHr*100+currMin;

                            String[] tokens = carPark.getMall_weekday_rates().split(";");
                            for(String x : tokens){
                                String[] pair = x.split(":");
                                String timeRange  = pair[0];

                                String[] times = timeRange.split("-");
                                //this is my aaaa
                                double timeLow = Double.parseDouble(times[0]);
                                //if(times[1].equals("0000")) times[1] = "2359";
                                //this is my bbbb
                                double timeHigh = Double.parseDouble(times[1]);

                                int a_hr = (int)timeLow/100;
                                int a_min = (int)timeLow%100;

                                int b_hr = (int)timeHigh/100;
                                int b_min = (int) timeHigh%100;

                                boolean abhiCond = (currHr == a_hr && currMin < a_min) || (currHr == b_hr && currMin > b_min);
                                if(b_hr > a_hr){
                                    if(currHr >= a_hr && currHr <= b_hr){
                                        //check minutes also
                                        if(abhiCond) continue;
                                        else{
                                            //abhiPrice = x;
                                            if(pair[1].equals(" F")){
                                                abhiPrice = "Free for the first hour";
                                            } else if(pair[1].equals(" C")){
                                                abhiPrice = "Closed during this hour";
                                            } else {
                                                abhiPrice = "Car Rate: $"+pair[1].substring(1);
                                                abhiPrice += " for the first hour";
                                            }
                                            break;
                                        }
                                    }
                                } else {
                                    if(currHr >= a_hr && currHr <= 23 || currHr <= b_hr){
                                        if(abhiCond) continue;
                                        else {
                                            //abhiPrice = x;
                                            if(pair[1].equals(" F")){
                                                abhiPrice = "Free for the first hour";
                                            } else if(pair[1].equals(" C")){
                                                abhiPrice = "Closed during this hour";
                                            } else {
                                                abhiPrice = "Car Rate: $"+pair[1].substring(1);
                                                abhiPrice += " for the first hour";
                                            }
                                            break;
                                        }
                                    }
                                }
                            }
                        }
                        break;
                }

            }
            //tvPrice.setText(priceInfo);

            if(abhiPrice.equals("F")){
                abhiPrice = "Free for the first hour!";
            } else if(abhiPrice.equals("C")){
                abhiPrice = "Closed at this hour.";
            }
            if(abhiPrice.equals("")){
                tvPrice.setText("No price information available at this timing");
            } else {
                tvPrice.setText(abhiPrice);
            }

            ImageView arrow = convertView.findViewById(R.id.price_arrow);
            View mainV = convertView.findViewById(R.id.mainV_price);

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
                    PriceDetailFragment priceDetailFragment = new PriceDetailFragment();
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

                    priceDetailFragment.setArguments(bundle);
                    getActivity().getSupportFragmentManager().beginTransaction()
                            .setTransition(FragmentTransaction.TRANSIT_ENTER_MASK)
                            .add(R.id.main_fragment, priceDetailFragment)
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
