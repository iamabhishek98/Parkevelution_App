package com.example.parkevolution1;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.akexorcist.googledirection.DirectionCallback;
import com.akexorcist.googledirection.GoogleDirection;
import com.akexorcist.googledirection.constant.RequestResult;
import com.akexorcist.googledirection.constant.TransportMode;
import com.akexorcist.googledirection.model.Direction;
import com.akexorcist.googledirection.model.Info;
import com.akexorcist.googledirection.model.Leg;
import com.akexorcist.googledirection.model.Route;
import com.akexorcist.googledirection.util.DirectionConverter;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.DefaultValueFormatter;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.StringTokenizer;

import static android.content.Context.MODE_PRIVATE;

public class ProximityDetailFragment extends Fragment implements OnMapReadyCallback, GoogleApiClient.OnConnectionFailedListener {

    private class LotInfo {
        private String lot_num;

        public String getLot_num() {
            return lot_num;
        }

        public void setLot_num(String lot_num) {
            this.lot_num = lot_num;
        }

        private String lot_type;
        private int total_lots;
        private int avail_lots;

        public String getLot_type() {
            return lot_type;
        }

        public void setLot_type(String lot_type) {
            this.lot_type = lot_type;
        }

        public int getTotal_lots() {
            return total_lots;
        }

        public void setTotal_lots(int total_lots) {
            this.total_lots = total_lots;
        }

        public int getAvail_lots() {
            return avail_lots;
        }

        public void setAvail_lots(int avail_lots) {
            this.avail_lots = avail_lots;
        }
    }

    //Details of the carpark
    private String carpark_name, carpark_address;
    private double latitude, longitude;
    private LatLonCoordinate currentPosition;
    private String duration;
    private String data_cat;
    private String hdb_car_parking_price;
    private String hdb_motorcycle_parking_price;
    private String hdb_carpark_id;
    private String availability_url = "https://api.data.gov.sg/v1/transport/carpark-availability";
    private String mall_parking_rate;

    private int total_cplots;

    //Information fields for favourite carpark
    private int fileLineNum;
    private boolean isFavourite;
    private boolean isCarparkHDB;
    private String totalFavouriteString;
    final private int TOTAL_NUM_HDB = 2114; //following hdb_carparks4_1
    final private int TOTAL_NUM_MALL = 376; //following malls2

    //Trends and Graph
    private LineChart lineChart;

    ListView listView;


    public ProximityDetailFragment() {
        // Required empty public constructor
    }

    private Bundle bundle;
    private RequestQueue mQueue;

    private GoogleMap map;

    private ArrayList<JSONObject> dataObj;

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        map.getUiSettings().setMyLocationButtonEnabled(false);
        map.getUiSettings().setMapToolbarEnabled(false);
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

        map.setMyLocationEnabled(false);
        map.clear();
        LatLng latLng = new LatLng(latitude, longitude);
        //map.animateCamera(CameraUpdateFactory.newLatLng(latLng));
        //map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15)); //change this number between 2-21 to control the zoom of the map

        //CARPARK MARKER
        MarkerOptions options = new MarkerOptions().position(latLng).title(carpark_name);
        map.addMarker(options).setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));

        //INITIAL MARKER
        LatLng latLng1 = new LatLng(currentPosition.getLatitude(), currentPosition.getLongitude());
        MarkerOptions options_start = new MarkerOptions().position(latLng1).title("Starting here");
        map.addMarker(options_start).setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));

        Log.v("Map_check", "marker added");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        //set State
        ((MainActivity)getActivity()).setState(1);

        // Inflate the layout for this fragment
        bundle = this.getArguments();
        carpark_name = bundle.getString("carpark-name");
        carpark_address = bundle.getString("carpark-address");
        latitude = bundle.getDouble("x-coord");
        longitude = bundle.getDouble("y-coord");
        data_cat = bundle.getString("data-cat");
        mQueue = Volley.newRequestQueue(getContext());




        //Retrieving out favourite carpark information
        fileLineNum = bundle.getInt("fileLineNum");
        isCarparkHDB = bundle.getBoolean("isHDB");

        currentPosition = new LatLonCoordinate(MainActivity.getStartingLatLonCoordinate().getLatitude(), MainActivity.getStartingLatLonCoordinate().getLongitude());



        //duration = getDurationInfo(currentPosition, new LatLonCoordinate(latitude, longitude));

        //get the  correct shared preference
        if(isCarparkHDB){
            SharedPreferences favouritePrefs = getActivity().getSharedPreferences("HDBFavouritePrefs", MODE_PRIVATE);
            totalFavouriteString = favouritePrefs.getString("favouriteHDBString", null);
        } else {
            SharedPreferences favouritePrefs = getActivity().getSharedPreferences("MallsFavouritePrefs", MODE_PRIVATE);
            totalFavouriteString = favouritePrefs.getString("favouriteMallString", null);
        }
        return inflater.inflate(R.layout.fragment_proximity_detail, container, false);
    }


    private BottomSheetBehavior bottomSheetBehavior;
    private ScrollView scrollView;

    private boolean isHDB;
    private SupportMapFragment mapFragment;
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //setting up the map
        mapFragment = (SupportMapFragment)getChildFragmentManager().findFragmentById(R.id.map_proximity_detail);
        mapFragment.getMapAsync(this);

        scrollView = getView().findViewById(R.id.proximity_scrollview);
        bottomSheetBehavior = BottomSheetBehavior.from(scrollView);

        //setting the heights
        //getting and setting the peek height
        int window_height = getActivity().getWindowManager().getDefaultDisplay().getHeight();
        int map_height = dpToPx(400);
        bottomSheetBehavior.setPeekHeight(window_height-map_height);
        bottomSheetBehavior.setHideable(false);
        //setting up the text fields
        TextView carparkNameTv = getView().findViewById(R.id.carpark_name_proximity);
        carparkNameTv.setText(carpark_name);

        TextView carparkAddressTv = getView().findViewById(R.id.address_text_proximity);
        carparkAddressTv.setText(carpark_address);

        TextView carparkDurationTv = getView().findViewById(R.id.duration_text_proximity);
        getDurationInfo(currentPosition, new LatLonCoordinate(latitude, longitude), carparkDurationTv);
        //carparkDurationTv.setText(duration);

        TextView carparkAvailabilityTv = getView().findViewById(R.id.availability_text_proximity);
        TextView carParkRateTv = getView().findViewById(R.id.parking_cost_text_proximity);
        if(data_cat.equals("HDB")){
            isHDB = true;
            //setting the availability
            hdb_carpark_id = bundle.getString("carpark-id");
            getAvailability(hdb_carpark_id, carparkAvailabilityTv);
            //setting the parking rates
            hdb_car_parking_price = bundle.getString("hdb_car_parking_rate");
            //hdb_motorcycle_parking_price = bundle.getString("hdb_motorcycle_parking_rate");
            hdb_motorcycle_parking_price="Whole Day\n(7:00am to 10:30pm)\n$0.65 per lot\nWhole Night\n(10:30pm to 7:00am on the following day)\n$0.65 per lot";
            String parkingRate = "Car rate: \n"+hdb_car_parking_price+"\n\nMotorcycle Rate:\n"+hdb_motorcycle_parking_price;
            carParkRateTv.setText(parkingRate);
        }else if(data_cat.equals("SHOPPING_MALL")){
            isHDB = false;
            mall_parking_rate = "Weekday Parking Rate 1:\n"+bundle.getString("shopping-weekday1")
            + "\n\nWeekday Parking Rate 2:\n"+bundle.getString("shopping-weekday2")+"\n\nSaturday Parking Rate:\n"
            +bundle.getString("shopping-sat")+"\n\nSunday/Public Hol:\n"+bundle.getString("shopping-sun");
            carParkRateTv.setText(mall_parking_rate);
        }

        //setting the favourite carpark
        final ImageView favStarIcon = getView().findViewById(R.id.star_fav_icon);
        //run through the totalFavouriteString to get to particular index
        String[] tokens = totalFavouriteString.split(",");
        if(tokens[fileLineNum].equals("FALSE")){
            isFavourite = false;
            //load bordered star image
            favStarIcon.setBackgroundResource(R.drawable.ic_star_border);
        } else {
            isFavourite = true;
            //load the full star image
            favStarIcon.setBackgroundResource(R.drawable.ic_star_full_fav);
        }

        favStarIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //check which file this carpark belongs to
                if(isCarparkHDB){
                    if(isFavourite == false){
                        isFavourite = true; //toggle the state
                        //update the sharedpreference
                        SharedPreferences.Editor editor = getActivity().getSharedPreferences("HDBFavouritePrefs", MODE_PRIVATE).edit();
                        String[] curTokens = totalFavouriteString.split(",");
                        StringBuilder newTotalFavouriteString = new StringBuilder("");
                        for(int i=0; i<TOTAL_NUM_HDB; i++){
                            if(i == fileLineNum){
                                newTotalFavouriteString.append("TRUE,");
                            } else {
                                newTotalFavouriteString.append(curTokens[i]+",");
                            }
                        }
                        editor.putString("favouriteHDBString", newTotalFavouriteString.toString());
                        editor.apply();
                        //update the UI
                        favStarIcon.setBackgroundResource(R.drawable.ic_star_full_fav);
                    } else {
                        isFavourite = false; //toggle the state
                        //update the sharedpreference
                        SharedPreferences.Editor editor = getActivity().getSharedPreferences("HDBFavouritePrefs", MODE_PRIVATE).edit();
                        String[] curTokens = totalFavouriteString.split(",");
                        StringBuilder newTotalFavouriteString = new StringBuilder("");
                        for(int i=0; i<TOTAL_NUM_HDB; i++){
                            if(i == fileLineNum){
                                newTotalFavouriteString.append("FALSE,");
                            } else {
                                newTotalFavouriteString.append(curTokens[i]+",");
                            }
                        }
                        editor.putString("favouriteHDBString", newTotalFavouriteString.toString());
                        editor.apply();
                        //update the UI
                        favStarIcon.setBackgroundResource(R.drawable.ic_star_border);
                    }
                } else {
                    if(isFavourite == false){
                        isFavourite = true; //toggle the state
                        //update the sharedpreference
                        SharedPreferences.Editor editor = getActivity().getSharedPreferences("MallsFavouritePrefs", MODE_PRIVATE).edit();
                        String[] curTokens = totalFavouriteString.split(",");
                        StringBuilder newTotalFavouriteString = new StringBuilder("");
                        for(int i=0; i<TOTAL_NUM_MALL; i++){
                            if(i == fileLineNum){
                                newTotalFavouriteString.append("TRUE,");
                            } else {
                                newTotalFavouriteString.append(curTokens[i]+",");
                            }
                        }
                        editor.putString("favouriteMallString", newTotalFavouriteString.toString());
                        editor.apply();
                        //update the UI
                        favStarIcon.setBackgroundResource(R.drawable.ic_star_full_fav);
                    } else {
                        isFavourite = false; //toggle the state
                        //update the sharedpreference
                        SharedPreferences.Editor editor = getActivity().getSharedPreferences("MallsFavouritePrefs", MODE_PRIVATE).edit();
                        String[] curTokens = totalFavouriteString.split(",");
                        StringBuilder newTotalFavouriteString = new StringBuilder("");
                        for(int i=0; i<TOTAL_NUM_MALL; i++){
                            if(i == fileLineNum){
                                newTotalFavouriteString.append("FALSE,");
                            } else {
                                newTotalFavouriteString.append(curTokens[i]+",");
                            }
                        }
                        editor.putString("favouriteMallString", newTotalFavouriteString.toString());
                        editor.apply();
                        //update the UI
                        favStarIcon.setBackgroundResource(R.drawable.ic_star_border);
                    }
                }
            }
        });

        //Getting the line chart
        lineChart = getView().findViewById(R.id.trendGraphProximityFrag);
        lineChart.setVisibility(View.GONE);  //hide the line char initially

        Button moreInfoBtn = getView().findViewById(R.id.more_info_button);
        moreInfoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //make this button disappear
                view.setVisibility(View.GONE);

                View entireView = (getView().findViewById(R.id.histTrendProx));
                //dummyDrawGraph(lineChart, null, entireView);

                //First check if the data belongs to HDB or not
                if(isHDB){
                    //load the spinner
                    getView().findViewById(R.id.avail_progressBar_prox).setVisibility(View.VISIBLE);
                    Toast.makeText(getContext(), "Please wait for 10-20 sec while the data loads.\n\t\t\t\t\t\t\t\t\t\t\t\t\t\tThank you!", Toast.LENGTH_LONG).show();
                    //get all the data
                    dataObj = jsonParseTable1(lineChart, entireView);
                } else {
                    //just display no further information available text
                    getView().findViewById(R.id.no_further_info_prox).setVisibility(View.VISIBLE);
                }
            }
        });


        FloatingActionButton navBtn = getView().findViewById(R.id.fab_2);
        navBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                LatLonCoordinate selectedLatLonCoordinate = ((MainActivity) getActivity()).getSelectedLatLonCoordinate();
                LatLonCoordinate startingLatLonCoordinate = ((MainActivity) getActivity()).getStartingLatLonCoordinate();
                /**
                 * Logging for error testing
                 * */
                //Log.v("Navigation_testing", "Starting Latitude: " + startingLatLonCoordinate.getLatitude() + " Starting Longitude: " + startingLatLonCoordinate.getLongitude());
                //Log.v("Navigation_testing", "Starting Latitude: " + latLonCoordinate.getLatitude() + " Starting Longitude: " + startingLatLonCoordinate.getLongitude());
                // Directions
                String nav_address = "http://maps.google.com/maps?daddr="
                        + selectedLatLonCoordinate.getLatitude() + ", "
                        + selectedLatLonCoordinate.getLongitude();

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

    private void dummyDrawGraph(LineChart lineChart, ArrayList<Integer> dataset, View view){
        view.setVisibility(View.VISIBLE);
        lineChart.setVisibility(View.VISIBLE);
        lineChart.setDragEnabled(true);
        lineChart.setScaleEnabled(true);
        lineChart.getAxisRight().setEnabled(false);
        lineChart.getDescription().setText("Number of Hours Ago");
        lineChart.getDescription().setTextColor(Color.BLACK);
        //lineChart.getDescription().setPosition(-5f, -40f);
        lineChart.getDescription().setTextSize(18f);
        lineChart.getDescription().setYOffset(30f);
        lineChart.getDescription().setEnabled(false);

        //the X-Axis:
        XAxis xAxis = lineChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setTextSize(16f);
        xAxis.setTextColor(Color.BLACK);
        xAxis.setDrawAxisLine(true);
        xAxis.setDrawGridLines(false);
        xAxis.setAxisLineWidth(2f);
        xAxis.setAxisLineColor(Color.BLACK);

        final String[] xAxisCuts = new String[] {"24", "23", "22", "21", "20", "19", "18", "17", "16", "15", "14", "13", "12", "11", "10", "9", "8", "7", "6", "5", "4", "3", "2", "1"};

        //xAxis.setGranularity(1f);
        xAxis.setValueFormatter(new IAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                return xAxisCuts[(int) value];
            }
        });

        //the Y-axis
        YAxis yAxis = lineChart.getAxisLeft();
        yAxis.setTextSize(10f);
        yAxis.setAxisMinimum(0f);
        yAxis.setAxisMaximum(total_cplots);
        yAxis.setTextColor(Color.BLACK);
        yAxis.setAxisLineWidth(2f);
        yAxis.setAxisLineColor(Color.BLACK);

        ArrayList<Entry> yValues = new ArrayList<>();

        //create the datasets
        yValues.add(new Entry(0, 60));
        yValues.add(new Entry(1, 20));
        yValues.add(new Entry(2, 44));
        yValues.add(new Entry(3, 28));
        yValues.add(new Entry(4, 52));
        yValues.add(new Entry(5, 80));
        yValues.add(new Entry(6, 44));
        yValues.add(new Entry(7, 28));
        yValues.add(new Entry(8, 52));
        yValues.add(new Entry(9, 80));


        LineDataSet set1 = new LineDataSet(yValues, "Available Lots data for past 24 hours");
        set1.setFillAlpha(110);
        set1.setColor(Color.BLUE);
        set1.setLineWidth(2f);
        set1.setCircleRadius(3f);
        //set1.setCircleColor(R.color.orange_color);
        set1.setValueTextSize(14f);
        set1.setValueTextColor(Color.BLACK);

        ArrayList<ILineDataSet> dataSets = new ArrayList<>();
        dataSets.add(set1);
        LineData data = new LineData(dataSets);
        lineChart.setData(data);
    }

    private void drawGraph(LineChart lineChart, ArrayList<Integer> dataset, View view){
        view.setVisibility(View.VISIBLE);
        lineChart.setVisibility(View.VISIBLE);
        lineChart.setDragEnabled(true);
        lineChart.setScaleEnabled(true);
        lineChart.getAxisRight().setEnabled(false);
        lineChart.getDescription().setEnabled(false);

        //the X-Axis:
        XAxis xAxis = lineChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setTextSize(16f);
        xAxis.setTextColor(Color.BLACK);
        xAxis.setDrawAxisLine(true);
        xAxis.setDrawGridLines(true);
        xAxis.setAxisLineWidth(2f);
        xAxis.setAxisLineColor(Color.BLACK);

        //final String[] xAxisCuts = new String[] {"24", "23", "22", "21", "20", "19", "18", "17", "16", "15", "14", "13", "12", "11", "10", "9", "8", "7", "6", "5", "4", "3", "2", "1", "0"};
        final String[] xAxisCuts = new String[] {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23", "24"};

        xAxis.setGranularity(1f);
        xAxis.setValueFormatter(new IAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                return xAxisCuts[(int) value];
            }
        });


        //the Y-axis
        YAxis yAxis = lineChart.getAxisLeft();
        yAxis.setTextSize(16f);
        yAxis.setAxisMinimum(0f);
        yAxis.setAxisMaximum(total_cplots + 20);
        yAxis.setTextColor(Color.BLACK);
        yAxis.setAxisLineWidth(2f);
        yAxis.setAxisLineColor(Color.BLACK);


        ArrayList<Entry> yValues = new ArrayList<>();

        //create the datasets
        /*
        int counter = 1;
        for(int i=dataset.size() - 1; i >= 0; i--){
            yValues.add(new Entry(counter, dataset.get(i)));
            counter++;
        }
        */
        for(int i=0; i<dataset.size(); i++){
            int val = dataset.get(i);
            if(val > total_cplots){
                val = total_cplots;
            }
            yValues.add(new Entry(i+1, val));
        }

        LineDataSet set1 = new LineDataSet(yValues, "Available Lots");
        set1.setFillAlpha(110);
        set1.setColor(Color.BLUE);
        set1.setLineWidth(2f);
        set1.setCircleRadius(3f);
        //set1.setCircleColor(R.color.orange_color);
        set1.setValueTextSize(0f);
        /*
        set1.setValueTextSize(14f);
        set1.setValueTextColor(Color.BLACK);*/

        ArrayList<ILineDataSet> dataSets = new ArrayList<>();
        dataSets.add(set1);
        LineData data = new LineData(dataSets);
        lineChart.setData(data);

        initPredictions();
    }


    String date_time = "";
    int mYear;
    int mMonth;
    int mDay;

    int mHour;
    int mMinute;
    String dayOfWeek;
    private void initPredictions(){
        Button predictionBtn = getView().findViewById(R.id.button_choose_time);

        predictionBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                datePicker();
            }
        });
    }

    private void datePicker(){

        // Get Current Date
        final Calendar c = Calendar.getInstance();
        mYear = c.get(Calendar.YEAR);
        mMonth = c.get(Calendar.MONTH);
        mDay = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(),
                new DatePickerDialog.OnDateSetListener() {

                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {

                        date_time = dayOfMonth + "-" + (monthOfYear + 1) + "-" + year;

                        SimpleDateFormat simpledateformat = new SimpleDateFormat("EEEE");
                        Date date = new Date(year, monthOfYear, dayOfMonth-1);
                        dayOfWeek = simpledateformat.format(date);
                        //Toast.makeText(getContext(), dayOfWeek, Toast.LENGTH_LONG).show();
                        //*************Call Time Picker Here ********************
                        timePicker();
                    }
                }, mYear, mMonth, mDay);
        datePickerDialog.show();
    }

    private void timePicker(){
        // Get Current Time
        final Calendar c = Calendar.getInstance();
        mHour = c.get(Calendar.HOUR_OF_DAY);
        mMinute = c.get(Calendar.MINUTE);

        // Launch Time Picker Dialog
        TimePickerDialog timePickerDialog = new TimePickerDialog(getContext(),
                new TimePickerDialog.OnTimeSetListener() {

                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {

                        mHour = hourOfDay;
                        mMinute = minute;
                        date_time += " "+hourOfDay + " "+minute;

                        //go through the dataObj to filter all the relevant information

                        //Toast.makeText(getContext(), mHour+"", Toast.LENGTH_LONG).show();
                        sieveOutPrediction();
                        //et_show_date_time.setText(date_time+" "+hourOfDay + ":" + minute);
                    }
                }, mHour, mMinute, false);
        timePickerDialog.show();
    }

    /**
     * This method will loop through the list of JSON objects and retrieve out the
     * carpark availability by day and hour. The average of all the availability is computed
     * and displayed and the predicted parking lot availability
     * */
    private void sieveOutPrediction(){

        //init
        int counter = 0; //counts the number of obtained rows
        int total_avail_lots = 0; //add the avail lots to this number

        String myData="";
        if(dayOfWeek.equals("Sunday")){
            myData+= "SUN";
        } else if(dayOfWeek.equals("Saturday")){
            myData += "SAT";
        } else if(dayOfWeek.equals("Monday")){
            myData += "MON";
        } else if(dayOfWeek.equals("Tuesday")){
            myData += "TUE";
        } else if(dayOfWeek.equals("Wednesday")){
            myData += "WED";
        } else if(dayOfWeek.equals("Thursday")){
            myData += "THU";
        } else if(dayOfWeek.equals("Friday")) {
            myData += "FRI";
        }

        if(mHour <10){
            myData+= "0"+mHour;
        } else {
            myData += mHour+"";
        }

        for(JSONObject jsonObject: dataObj){
            try{
                String dateFromObj = jsonObject.getString("Time");
                String[] tokens = dateFromObj.split("-");
                String result = tokens[0]+tokens[2].substring(0, 2);

            //    Log.v("Tokenised", "FInal result after tokenizing: "+result);
                if(result.equals(myData)){
                    try{
                        int currResult = jsonObject.getInt(hdb_carpark_id+"_C");
                        counter++;
                        total_avail_lots += currResult;
                    } catch (JSONException e){
                        e.printStackTrace();
                    }
                }
            } catch(JSONException e){
                e.printStackTrace();
            }
        }

        if(counter != 0){
            total_avail_lots /= counter;
        }

        //setting up the UI
        getView().findViewById(R.id.ultimatePredictionUI).setVisibility(View.VISIBLE);
        TextView tv = getView().findViewById(R.id.predictionResultTv);
        tv.setText(total_avail_lots +"\n out of\n"+total_cplots);
        //animate the progress bar
        ProgressBar ultimatePB = getView().findViewById(R.id.ultimateProgressBar);
        setProgressMax(ultimatePB, total_cplots);
        setProgressAnimate(ultimatePB, total_avail_lots);
    }

    private void setProgressMax(ProgressBar pb, int max) {
        pb.setMax(max * 100);
    }

    private void setProgressAnimate(ProgressBar pb, int progressTo)
    {
        ObjectAnimator animation = ObjectAnimator.ofInt(pb, "progress", pb.getProgress(), progressTo * 100);
        animation.setDuration(500);
        animation.setInterpolator(new DecelerateInterpolator());
        animation.start();
    }

    private void setUpAdditionalInfo(ArrayList<JSONObject> dataObj, LineChart lineChart, final View entireView){
        //data has been retrieved, u can hide thre progress bar
        if(getView() != null){
            getView().findViewById(R.id.avail_progressBar_prox).setVisibility(View.GONE);
            if(dataObj.size() == 0){
                //there is no data inside -> hide the line chart and let the user know that there is no additional information available
                getView().findViewById(R.id.no_further_info_prox).setVisibility(View.VISIBLE);
            } else {
                //Load up the information into the linechart and init it
                //get the last 24 data points
                ArrayList<Integer> dataPoints = new ArrayList<>();
                int dataSize = dataObj.size();
                String cpCode = hdb_carpark_id + "_C";
                /**
                 *Takes the last 24 datapoints.
                 *  the first index in the array list will contain the latest data
                 *  -> iterate from the back when plotting the graph
                 * */
                for(int i=dataSize; i > dataSize - 24; i--){
                    try{
                        dataPoints.add(dataObj.get(i-1).getInt(cpCode));
                    } catch(JSONException e){
                        Toast.makeText(getContext(), "No Additional Information is available", Toast.LENGTH_LONG).show();
                    }
                }

                //make the line char appear
                lineChart.setVisibility(View.VISIBLE);

                for(int i=0; i<dataPoints.size(); i++){
                    Log.v("DATA_POINT_RETRIEVED", Integer.toString(dataPoints.get(i)));
                }

                drawGraph(lineChart, dataPoints, entireView);
            }
        } else {
            return;
        }
    }

    private ArrayList<JSONObject> jsonParseTable1(final LineChart lineChart, final View entireView) {
        final ArrayList<JSONObject> resultObj = new ArrayList<>();

        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, "http://carparkdata.hopto.org/avail_1.php", null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        Log.v("Samuel", "Success-2 - testest - event is retu");
                        Log.v("Abhishek", response.toString());
                        //get the first object
                        try {
                            //it is guarenteed that the first object exists
                            JSONObject firstObj = response.getJSONObject(0);
                            String carpark_id = hdb_carpark_id + "_C";
                            boolean hasCp = firstObj.has(carpark_id);
                            if(!hasCp){
                                jsonParseTable2(resultObj, lineChart, entireView);
                            } else {
                                //package the data up and return the data in the form of AL1
                                for(int i=0; i< response.length(); i++){
                                    resultObj.add(response.getJSONObject(i));
                                }
                                setUpAdditionalInfo(dataObj, lineChart, entireView);
                            }
                        } catch (JSONException e) {
                            //some kind of connection or data retrieval error has occurred
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
                if(getView()!= null){
                    getView().findViewById(R.id.avail_progressBar_prox).setVisibility(View.GONE);
                    getView().findViewById(R.id.no_further_info_prox).setVisibility(View.VISIBLE);
                }
                Log.v("Samuel", "Error");
            }
        });
        mQueue.add(request);
        return resultObj;
    }

    private void jsonParseTable2(final ArrayList<JSONObject> resultObj, final LineChart lineChart, final View entireView){
        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, "http://carparkdata.hopto.org/avail_2.php", null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        //get the first object
                        try {
                            //it is guarenteed that the first object exists
                            JSONObject firstObj = response.getJSONObject(0);
                            String carpark_id = hdb_carpark_id + "_C";
                            boolean hasCp = firstObj.has(carpark_id);
                            if(!hasCp){
                                //resultObj will remain as empty -> size 0
                                //setUpAdditionalInfo(dataObj, lineChart);
                                if(getView().findViewById(R.id.avail_progressBar_prox) != null)
                                    getView().findViewById(R.id.avail_progressBar_prox).setVisibility(View.GONE);
                                if(getView().findViewById(R.id.no_further_info_prox) != null)
                                getView().findViewById(R.id.no_further_info_prox).setVisibility(View.VISIBLE);
                            } else {
                                //package the data up and return the data in the form of AL1
                                for(int i=0; i< response.length(); i++){
                                    resultObj.add(response.getJSONObject(i));
                                }
                                setUpAdditionalInfo(dataObj, lineChart, entireView);
                            }
                        } catch (JSONException e) {
                            //some kind of connection or data retrieval error has occurred
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
                if(getView()!= null){
                    getView().findViewById(R.id.avail_progressBar_prox).setVisibility(View.GONE);
                    getView().findViewById(R.id.no_further_info_prox).setVisibility(View.VISIBLE);
                }
                Log.v("Samuel", "Error");
            }
        });
        mQueue.add(request);
    }

    String distance_result;
    private void getDurationInfo(LatLonCoordinate initialPoint, LatLonCoordinate finalPoint, final TextView textView){
        String serverKey = getResources().getString(R.string.google_direction_api_key);
        final LatLng origin = new LatLng(initialPoint.getLatitude(), initialPoint.getLongitude());
        final LatLng destination = new LatLng(finalPoint.getLatitude(), finalPoint.getLongitude());
        //--------------------Using AK Exorcist Google Direction Library-----------
        GoogleDirection.withServerKey(serverKey)
                .from(origin)
                .to(destination)
                .transportMode(TransportMode.DRIVING)
                .execute(new DirectionCallback() {
                    @Override
                    public void onDirectionSuccess(Direction direction, String rawBody) {
                        String status = direction.getStatus();
                        if(status.equals(RequestResult.OK)){
                            String finalResult = "";
                            Route route = direction.getRouteList().get(0);
                            Leg leg = route.getLegList().get(0);
                            Info distanceInfo = leg.getDistance();
                            Info durationInfo = leg.getDuration();
                            distance_result = distanceInfo.getText();

                            double distance_num = Double.parseDouble(distance_result.substring(0, distance_result.length()-2));
                            double distance_cost = (distance_num/11)*2.25;
                            String dist_cost_strDouble = String.format("%.2f", distance_cost);
                            ((TextView)getView().findViewById(R.id.travel_text_proximity)).setText("Travel cost: Around $"+dist_cost_strDouble+"\nTravel Distance: "+distance_result);

                            Log.v("Distance_resuilts", "_________@@@@@@@@DISTANCE: "+distance_result);
                            //tempDuration = (Double.parseDouble(durationInfo.getValue()));
                            String duration_result = durationInfo.getText();
                            int duration_result_d = Integer.parseInt(durationInfo.getValue());
                            finalResult += "Driving time: " + duration_result;
/*                         A HUMBLE ATTEMPT TO SHOW THE ETA at the carpark
                            Calendar calendar = GregorianCalendar.getInstance();
                            calendar.add(Calendar.MINUTE, duration_result_d);
                            finalResult += "\nETA: "+calendar.get(Calendar.HOUR_OF_DAY)+":"+calendar.get(Calendar.MINUTE);*/
                            textView.setText(finalResult);
                           // Toast.makeText(getContext(), "Current Time: "+duration_result, Toast.LENGTH_LONG).show();

                            //-------------Drawing Path--------------\\
                            ArrayList<LatLng> directionPositionList = leg.getDirectionPoint();
                            PolylineOptions polylineOptions = DirectionConverter.createPolyline(getActivity(),
                                    directionPositionList, 5, getResources().getColor(R.color.colorPrimary));
                            map.addPolyline(polylineOptions);

                            //--------------Zooming the map according to marker bounds-------\\
                            LatLngBounds.Builder builder = new LatLngBounds.Builder();
                            builder.include(origin);
                            builder.include(destination);
                            LatLngBounds bounds = builder.build();

                            int width = getResources().getDisplayMetrics().widthPixels;
                            int height = getResources().getDisplayMetrics().heightPixels;
                            int padding = (int) (width *0.20); // offset from edges of the map 10% of screen

                            CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, width, height, padding);
                            map.moveCamera(cu);
                            LatLng latLonCoordinate = new LatLng(MainActivity.getSelectedLatLonCoordinate().getLatitude(), MainActivity.getSelectedLatLonCoordinate().getLongitude());
                            map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLonCoordinate, 15));
                            //map.animateCamera(CameraUpdateFactory.newLatLng(latLonCoordinate));

                            //------------------------------------------------------------------\\

                           Log.v("Location_result", "duration result: "+duration_result);
                        } else if (status.equals(RequestResult.NOT_FOUND)){
                            textView.setText("Travel time \nData Unavailable");
                            Log.v("Location_result", "No routes exist");
                        } else {
                            textView.setText("Travel time \nData Unavailable");
                            Log.v("Location_result", "Main duration method failed: "+ status);
                        }
                    }

                    @Override
                    public void onDirectionFailure(Throwable t) {
                        Log.v("Location_result", "Direction failure");
                    }
                });
        Log.v("Location_result", "But the getDuration() was called");
    }

    private ArrayList<LotInfo> allData = new ArrayList<>();
    private void getAvailability(final String id, final TextView textView){
        //get the availabiliity data

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, availability_url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        //empty the current arraylist of availCarParks
                        allData.clear();
                        //parsing happens here
                        try {
                            Log.v("Samuel", "Success-3");
                            JSONArray jsonArrayItems = response.getJSONArray("items");
                            JSONObject jsonObjectMain = jsonArrayItems.getJSONObject(0);
                            JSONArray jsonArrayCarpark_Data = jsonObjectMain.getJSONArray("carpark_data");
                            //loop through every single data obtained
                            for (int i = 0; i < jsonArrayCarpark_Data.length(); i++) {
                                JSONObject jsonObjectCarpark = jsonArrayCarpark_Data.getJSONObject(i);
                                //Log.v("Detail_proximity", "json id: "+jsonObjectCarpark.getString("carpark_number"));
                                if(jsonObjectCarpark.getString("carpark_number").equals(id)){
                                    JSONArray jsonArrayInfo = jsonObjectCarpark.getJSONArray("carpark_info");
                                    JSONObject jsonObjectInfo = jsonArrayInfo.getJSONObject(0);

                                    LotInfo lotInfo = new LotInfo();
                                    lotInfo.setLot_num(id);
                                    lotInfo.setLot_type(jsonObjectInfo.getString("lot_type"));
                                    lotInfo.setTotal_lots(jsonObjectInfo.getInt("total_lots"));
                                    lotInfo.setAvail_lots(jsonObjectInfo.getInt("lots_available"));

                                    allData.add(lotInfo);
                                }
                            }

                            String availability_string ="";
                            for(LotInfo lotInfo: allData){
                                availability_string += "Type: "+lotInfo.getLot_type()
                                        +"\n"+"Availability: "+lotInfo.getAvail_lots()+"/"+lotInfo.getTotal_lots()+"\n";
                                total_cplots = lotInfo.getTotal_lots();
                            }
                            if(availability_string.equals("")) availability_string = "No availability data available";
                            textView.setText(availability_string);
                            Log.v("Proximity_details", "Success + availCarparks size:" + allData.size());
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


    @Override
    public void onStop() {
        mQueue.cancelAll("hi");
        super.onStop();
    }

    private int dpToPx(int dp){
        float density = getContext().getResources().getDisplayMetrics().density;
        return Math.round(dp*density);
    }
}
