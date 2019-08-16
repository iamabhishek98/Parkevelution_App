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
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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

public class RecommendedFragment extends Fragment {

    public SVY21Coordinate currentSVY21Location;
    private List<CarPark> carParks = new ArrayList<>();
    private CarPark[] cpArray = new CarPark[50];

    public RecommendedFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_recommended, container, false);

    }

    private ListView listView;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        //setting the currentSVY21Location for testing purposes
        LatLonCoordinate testCoordinate = new LatLonCoordinate(1.344261, 103.720750);
        currentSVY21Location = testCoordinate.asSVY21();

        mQueue = Volley.newRequestQueue(getContext());

        LatLonCoordinate realCoordinate = MainActivity.getLatLonCoordinate();
        if(realCoordinate != null){
            currentSVY21Location = realCoordinate.asSVY21();
            Log.v("Location_test", "Real coordinate for PRICE is obtained");
        }

        listView = getView().findViewById(R.id.reccListView);

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

    //Abhishek Constants -> 10 cents per 50 metres
    final private double price_threshold = 0.1;
    final private double distance_threshold = 50;

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

        //setting the price for these 50 carparks
        setHourlyPriceForSelectedCarparks(cpArray);

        //setting the availability if info is avail
        setAvailabilityInformation(cpArray);

        //calculate recommendability index for each carpark
        setReccIndex(cpArray);

        //insert Abhishek's sorting algorithm for recommended carpark

        /**
         * Sorting method 0
         * */

        /**
         * Due to closed_val in the setHourlyPrice method, carparks that are closed will automatically be filtered to be displayed last :)
         *
         * */

        Arrays.sort(cpArray, new Comparator<CarPark>() {
            @Override
            public int compare(CarPark c1, CarPark c2) {
                if(c1.getReccIndex() > c2.getReccIndex()){
                    return 1;
                } else if(c1.getReccIndex() < c2.getReccIndex()){
                    return -1;
                } else {
                    return 0;
                }
            }
        });


        //printer function to print out the indices of all the sorted carpark
        for(int i=0; i <cpArray.length; i++){
            Log.v("recc-index", "carpark recc index: "+cpArray[i].getReccIndex());
        }

//        Arrays.sort(cpArray, new Comparator<CarPark>() {
//            @Override
//            public int compare(CarPark carPark, CarPark t1) {
//                if(carPark.getDist() - t1.getDist() <= 0 && carPark.getHourly_price() <= t1.getHourly_price()){
//                    //nearer and cheaper
//                    return -1; //obviously don't swap
//                } else if(carPark.getDist() - t1.getDist() > 0 && carPark.getHourly_price() > t1.getHourly_price()){
//                    //further and more expensive
//                    return 1; //obviously swap
//                } else if(carPark.getDist() - t1.getDist() <=0 && carPark.getHourly_price() > t1.getHourly_price() ){
//                        //nearer but more expensive -> compare the opportunity cost first
//                        double dist_diff = Math.abs(carPark.getDist() - t1.getDist());
//                        if(/*dist_diff/distance_threshold * price_threshold*/ (Math.pow(Math.E, dist_diff/100)) * price_threshold+ t1.getHourly_price() < carPark.getHourly_price()){
//                            return 1; //swap
//                        } else if(/*dist_diff/distance_threshold * price_threshold*/ (Math.pow(Math.E, dist_diff/100)) * price_threshold + t1.getHourly_price() > carPark.getHourly_price()){
//                            return -1; //don't swap. The cheaper price isn't worth it
//                        } else {
//                            return 0;
//                        }
//                    } else if(carPark.getDist() - t1.getDist() > 0 && carPark.getHourly_price() < t1.getHourly_price()){
//                        //further but cheaper
//                        double dist_diff = Math.abs(carPark.getDist() - t1.getDist());
//                        if(/*dist_diff/distance_threshold * price_threshold*/ (Math.pow(Math.E, dist_diff/100)) * price_threshold + carPark.getHourly_price() < t1.getHourly_price()){
//                            return 1; //swap
//                        } else if(/*dist_diff/distance_threshold * price_threshold*/ (Math.pow(Math.E, dist_diff/100)) * price_threshold + carPark.getHourly_price() > t1.getHourly_price()){
//                            return -1; //don't swap. The cheaper price isn't worth it
//                        } else {
//                            return 0;
//                        }
//                    } else {
//                        return 0;
//                    }
//            }
//        });





        /**
         *  Sorting method I
         * */


//        Arrays.sort(cpArray, new Comparator<CarPark>() {
//            @Override
//            public int compare(CarPark carPark, CarPark t1) {
//                //check for availability first
//                if(carPark.getDataCategory() == CarPark.DataCategory.HDB && t1.getDataCategory() == CarPark.DataCategory.HDB && carPark.getAvail_lots() == 0 && t1.getAvail_lots() == 0){
//                        //both have 0 availabile lots -> sort by distance
//                        if(carPark.getDist() > t1.getDist()){
//                            return 1;
//                        } else if(carPark.getDist() < t1.getDist()){
//                            return -1;
//                        } else {
//                            return 0;
//                        }
//                } else {
//                    if(carPark.getDataCategory() == CarPark.DataCategory.HDB && t1.getDataCategory() == CarPark.DataCategory.SHOPPING_MALL && carPark.getAvail_lots() == 0){
//                        //put shopping mall in front
//                        return 1;
//                    } else if(carPark.getDataCategory() == CarPark.DataCategory.SHOPPING_MALL && t1.getDataCategory() == CarPark.DataCategory.HDB && t1.getAvail_lots() == 0){
//                        //retain shopping mall in front
//                        return -1;
//                    } else if(carPark.getDist() - t1.getDist() <= 0 && carPark.getHourly_price() <= t1.getHourly_price()){
//                        //nearer and cheaper
//                        return -1; //obviously don't swap
//                    } else if(carPark.getDist() - t1.getDist() > 0 && carPark.getHourly_price() > t1.getHourly_price()){
//                        //further and more expensive
//                        return 1; //obviously swap
//                    } else if(carPark.getDist() - t1.getDist() <=0 && carPark.getHourly_price() > t1.getHourly_price() ){
//                        //nearer but more expensive -> compare the opportunity cost first
//                        double dist_diff = Math.abs(carPark.getDist() - t1.getDist());
//                        if(/*dist_diff/distance_threshold * price_threshold*/ (Math.pow(Math.E, dist_diff/100)) * price_threshold+ t1.getHourly_price() < carPark.getHourly_price()){
//                            return 1; //swap
//                        } else if(/*dist_diff/distance_threshold * price_threshold*/ (Math.pow(Math.E, dist_diff/100)) * price_threshold + t1.getHourly_price() > carPark.getHourly_price()){
//                            return -1; //don't swap. The cheaper price isn't worth it
//                        } else {
//                            return 0;
//                        }
//                    } else if(carPark.getDist() - t1.getDist() >= 0 && carPark.getHourly_price() < t1.getHourly_price()){
//                        //further but cheaper
//                        double dist_diff = Math.abs(carPark.getDist() - t1.getDist());
//                        if(/*dist_diff/distance_threshold * price_threshold*/ (Math.pow(Math.E, dist_diff/100)) * price_threshold + carPark.getHourly_price() < t1.getHourly_price()){
//                            return 1; //swap
//                        } else if(/*dist_diff/distance_threshold * price_threshold*/ (Math.pow(Math.E, dist_diff/100)) * price_threshold + carPark.getHourly_price() > t1.getHourly_price()){
//                            return -1; //don't swap. The cheaper price isn't worth it
//                        } else {
//                            return 0;
//                        }
//                    } else {
//                        return 0;
//                    }
//                }
//            }
//        });


        /**
         * Sorting method II
         * */
        /*
        Arrays.sort(cpArray, new Comparator<CarPark>() {
            @Override
            public int compare(CarPark carPark, CarPark t1) {
                if(carPark.getReccIndex() < t1.getReccIndex()){
                    return 1;
                } else if (carPark.getReccIndex() > t1.getReccIndex()){
                    return -1;
                } else {
                    return 0;
                }
            }
        });
        */


        /*
        Arrays.sort(cpArray, new Comparator<CarPark>() {
            @Override
            public int compare(CarPark carPark, CarPark t1) {
                if((Math.abs(carPark.getHourly_price()) <= price_threshold) && (Math.abs(carPark.getDist() - t1.getDist())) < distance_threshold){
                    if(carPark.getDataCategory() == CarPark.DataCategory.HDB && t1.getDataCategory() == CarPark.DataCategory.HDB){
                        if(carPark.getAvail_lots() <= t1.getAvail_lots()){
                            return 1;
                        } else {
                            return -1;
                        }
                    } else {
                        if(carPark.getDist() <= t1.getDist()){
                            return -1;
                        } else {
                            return 1;
                        }
                    }
                } else if((Math.abs(carPark.getHourly_price() - t1.getHourly_price()) <= price_threshold) && (Math.abs(carPark.getDist() - t1.getDist()) >= distance_threshold)){
                    return 1;
                } else if((Math.abs(carPark.getHourly_price() - t1.getHourly_price()) > price_threshold)){
                    if(carPark.getHourly_price() < t1.getHourly_price()) return -1;
                    else return 1;
                } else {
                    return 0;
                }
            }
        });
        */
        //update the list view
        MyReccAdapter myReccAdapter = new MyReccAdapter(getContext(), cpArray);
        listView.setAdapter(myReccAdapter);
    }


    private void setReccIndex(CarPark[] carparks){

        for(int i=0; i<carparks.length; i++){
            double index_c1;
            index_c1 = (Math.pow(Math.E, carparks[i].getDist()/100)) * price_threshold + carparks[i].getHourly_price();
            /*
            if(carparks[i].getDataCategory() == CarPark.DataCategory.HDB){
                if(carparks[i].getAvail_lots() == 0){
                    index_c1 += 1000000; //infinity
                } else {
                    index_c1 += 1/carparks[i].getAvail_lots();
                }

            }
            */
            carparks[i].setReccIndex(index_c1);
        }

        /*
        for(int i=0; i<carparks.length; i++){
            double avail;
            double dist;
            double price;

            if(carparks[i].getDataCategory() == CarPark.DataCategory.HDB){
                //get absolute availability of that carpark
                avail = carparks[i].getAvail_lots();
            } else {
                //set the availability of that carpark to be 1 (DEFAULT VAL)
                avail = 1;
            }

            dist = carparks[i].getDist();
            price = carparks[i].getHourly_price();

            //setting the index of the carpark
            carparks[i].setReccIndex(avail/(price*dist));
        }*/
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
                                currCp.setHourly_price(Double.parseDouble(currCp.getMall_saturday_24rate()));
                            }
                        } else {
                            String[] timeComponents = str.split(":");
                            double currHr = Double.parseDouble(timeComponents[0]);
                            double currMin = Double.parseDouble(timeComponents[1]);
                            double currAbhiTime = currHr*100+currMin;

                            String[] tokens = currCp.getMall_saturday_rates().split(";");
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
                                    currCp.setHourly_price(closed_val);
                                }

                            }
                        } else {
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
    private String availability_url = "https://api.data.gov.sg/v1/transport/carpark-availability";
    private RequestQueue mQueue;

    ArrayList<CarPark> allAvailCarParks = new ArrayList<>();

    private void setAvailabilityInformation(final CarPark[] cpArray){
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, availability_url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        //parsing happens here
                        try {
                            Log.v("Samuel", "Success-3");
                            JSONArray jsonArrayItems = response.getJSONArray("items");
                            JSONObject jsonObjectMain = jsonArrayItems.getJSONObject(0);
                            JSONArray jsonArrayCarpark_Data = jsonObjectMain.getJSONArray("carpark_data");
                            for (int i = 0; i < jsonArrayCarpark_Data.length(); i++) {
                                CarPark availCarPark1 = new CarPark();
                                JSONObject jsonObjectCarpark = jsonArrayCarpark_Data.getJSONObject(i);
                                JSONArray jsonArrayInfo = jsonObjectCarpark.getJSONArray("carpark_info");
                                JSONObject jsonObjectInfo = jsonArrayInfo.getJSONObject(0);

                                availCarPark1.setName(jsonObjectCarpark.getString("carpark_number"));
                                availCarPark1.setAvail_lots(jsonObjectInfo.getInt("lots_available"));
                                availCarPark1.setTotal_lots(jsonObjectInfo.getInt("total_lots"));
                                availCarPark1.setLot_type(jsonObjectInfo.getString("lot_type"));
                                allAvailCarParks.add(availCarPark1);
                            }

                            for (int i = 0; i < cpArray.length; i++) {
                                if(cpArray[i].getDataCategory() == CarPark.DataCategory.SHOPPING_MALL){
                                    //SHOPPING MALL DATA
                                    continue;
                                } else {
                                    //HDB DATA
                                    String name = cpArray[i].getName();
                                    int max = allAvailCarParks.size();
                                    cpArray[i].setAvail_lots(0);
                                    cpArray[i].setTotal_lots(0);
                                    cpArray[i].setLot_type("N");
                                    for (int j = 0; j < max; j++) {
                                        CarPark availCarPark = allAvailCarParks.get(j);
                                        if (name.equals(availCarPark.getName())) {
                                            cpArray[i].setTotal_lots(availCarPark.getTotal_lots());
                                            cpArray[i].setAvail_lots(availCarPark.getAvail_lots());
                                            cpArray[i].setLot_type(availCarPark.getLot_type());
                                            break;
                                        }
                                    }
                                }
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
                Log.v("Samuel", "Error");
            }
        });

        mQueue.add(request);
    }




    class MyReccAdapter extends ArrayAdapter<CarPark> {
        Context context;
        CarPark carPark;

        public MyReccAdapter(Context c, CarPark[] carParks){
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
                            }  else if((carPark.getMall_weekday_24rate()).equals("S")){
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
}
