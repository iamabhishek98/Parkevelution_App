package com.example.parkevolution1;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.mxn.soul.flowingdrawer_core.ElasticDrawer;
import com.mxn.soul.flowingdrawer_core.FlowingDrawer;

public class MainActivity extends AppCompatActivity {

    final private int TOTAL_NUM_HDB = 2114; //following hdb_carparks4_1
    final private int TOTAL_NUM_MALL = 376; //following malls2
    /**
     * This is the supposedly starting location the user wants
     * */
    private static LatLonCoordinate latLonCoordinate;

    /**
     *  The real current location of the user
     * */
    private static LatLonCoordinate startingLatLonCoordinate;

    /**
     * The coordinate of the carpark that the user has selected from the ListView in home fragment
     * */
    private static LatLonCoordinate selectedLatLonCoordinate;

    public static LatLonCoordinate getSelectedLatLonCoordinate() {
        return selectedLatLonCoordinate;
    }

    public static void setSelectedLatLonCoordinate(LatLonCoordinate selectedLatLonCoordinate) {
        MainActivity.selectedLatLonCoordinate = selectedLatLonCoordinate;
    }

    public static LatLonCoordinate getStartingLatLonCoordinate() {
        return startingLatLonCoordinate;
    }

    public static void setStartingLatLonCoordinate(LatLonCoordinate startingLatLonCoordinate) {
        MainActivity.startingLatLonCoordinate = startingLatLonCoordinate;
    }

    /**
     * Starting location for travel
     * */


    private int state; // the current displaying fragment
    public void setState(int state){this.state = state;}
    public ParkedHere getParkedHere() {
        return parkedHere;
    }

    public void setParkedHere(ParkedHere parkedHere) {
        this.parkedHere = parkedHere;
    }

    /**
     *  0 -> Home
     *  1 -> Detail (could be any detail fragment)
     *  2 -> Favourite Carpark
     *  3 -> Park here
     * */

    private ParkedHere parkedHere;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //it will load up dummy fragment which can be used as a very short temporary splash screen if u will...
        setContentView(R.layout.activity_main);
        state = 0;


        double default_lat = 1.2906, default_long = 103.8530;
        //update all the lat longs if they are null to prevent any errors
        if(startingLatLonCoordinate == null){
            startingLatLonCoordinate = new LatLonCoordinate(default_lat, default_long);
        }

        if(latLonCoordinate == null){
            latLonCoordinate = new LatLonCoordinate(default_lat, default_long);
        }

        if(selectedLatLonCoordinate == null){
            selectedLatLonCoordinate = new LatLonCoordinate(default_lat, default_long);
        }

        //initialise the sharedpreferences for the favourite carparks
        SharedPreferences hdbPrefs = getSharedPreferences("HDBFavouritePrefs", MODE_PRIVATE);
        String favHDBList = hdbPrefs.getString("favouriteHDBString", null);
        if(favHDBList == null){
            StringBuilder results = new StringBuilder("");
            //create this and put it inside Shared Prefs
            for(int i=0; i<TOTAL_NUM_HDB; i++){
                results.append("FALSE,");
            }
            //dump the results into sharedpreferences
            SharedPreferences.Editor editor = getSharedPreferences("HDBFavouritePrefs", MODE_PRIVATE).edit();
            editor.putString("favouriteHDBString", results.toString());
            editor.apply();
        }

        SharedPreferences mallsPrefs = getSharedPreferences("MallsFavouritePrefs", MODE_PRIVATE);
        String favMallList = mallsPrefs.getString("favouriteMallString", null);
        if(favMallList == null){
            //create this and put it inside Shared Prefs
            StringBuilder results = new StringBuilder("");
            //create this and put it inside Shared Prefs
            for(int i=0; i<TOTAL_NUM_HDB; i++){
                results.append("FALSE,");
            }
            //dump the results into sharedpreferences
            SharedPreferences.Editor editor = getSharedPreferences("MallsFavouritePrefs", MODE_PRIVATE).edit();
            editor.putString("favouriteMallString", results.toString());
            editor.apply();
        }


    }

    /**
     * Car parked fragment information:
     *      Consists of:
     *          0.
     *          1. Lat, Long of the location parked at
     *          2. Address of the location
     *          3. Description of the location parked at
     * */

    private void initParkedHere(){

        SharedPreferences prefs = getSharedPreferences("ParkedHerePrefs", MODE_PRIVATE);
        boolean isParked = prefs.getBoolean("isParked", false);
        if(isParked) {
            //transfer the data into ParkedHere class
            double parkedLat = prefs.getFloat("pHLat", 0);
            double parkedLong = prefs.getFloat("pHLong", 0);
            String parkedAdd = prefs.getString("pHAddress", "");
            String parkedDescription = prefs.getString("pHDescription", "");
            this.parkedHere = new ParkedHere(parkedLat, parkedLong, parkedAdd, parkedDescription, true);
            Log.v("SharedPrefsCheck", "true");
            //change the nav drawer title accordingly
            TextView tv = findViewById(R.id.park_here_tv);
            tv.setText("Find My Car");
        }  else {
            this.parkedHere = new ParkedHere(0, 0, "", "", false);
            //change the nav drawer title accordingly
            TextView tv = findViewById(R.id.park_here_tv);
            tv.setText("Park My Car");
        }
    }


    @Override
    protected void onStart() {
        super.onStart();
        initParkedHere();

        //open home fragment
        //state = 0;
        Main_Fragment main_fragment = new Main_Fragment();
        getSupportFragmentManager().beginTransaction()
                .setTransition(FragmentTransaction.TRANSIT_ENTER_MASK)
                .replace(R.id.main_fragment, main_fragment, "myMainFragment")
                .addToBackStack(null)
                .commit();

        final FlowingDrawer mDrawer = findViewById(R.id.drawerlayout);
        mDrawer.setTouchMode(ElasticDrawer.TOUCH_MODE_BEZEL);

        View navHome = mDrawer.findViewById(R.id.nav_home);
        View navFavouriteCarpark = mDrawer.findViewById(R.id.nav_favourite_carpark);
        View navParkedHere = mDrawer.findViewById(R.id.nav_parked_here);

        navHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(state != 0){
                    //open back home fragment
                    state = 0;

                    Main_Fragment main_fragment = new Main_Fragment();
                    getSupportFragmentManager().beginTransaction()
                            .setTransition(FragmentTransaction.TRANSIT_ENTER_MASK)
                            .replace(R.id.main_fragment, main_fragment)
                            //.addToBackStack(null)
                            .commit();
                }
                mDrawer.closeMenu();
            }
        });

        navFavouriteCarpark.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(state != 2){
                    state = 2;
                    //open back home fragment
                    FavouriteCarparksFragment favouriteCarparksFragment = new FavouriteCarparksFragment();
                    getSupportFragmentManager().beginTransaction()
                            .setTransition(FragmentTransaction.TRANSIT_ENTER_MASK)
                            .replace(R.id.main_fragment, favouriteCarparksFragment)
                            //.addToBackStack(null)
                            .commit();
                }
                mDrawer.closeMenu();
            }
        });

        navParkedHere.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(state != 3){
                    state =3;
                    //open the correct fragment based on the carpark preference
                    SharedPreferences prefs = getSharedPreferences("ParkedHerePrefs", MODE_PRIVATE);
                    boolean isParked = prefs.getBoolean("isParked", false);
                    if(isParked){
                        FindCarFragment findCarFragment = new FindCarFragment();
                        getSupportFragmentManager().beginTransaction()
                                .setTransition(FragmentTransaction.TRANSIT_ENTER_MASK)
                                .add(R.id.main_fragment, findCarFragment)
                                //.addToBackStack(null)
                                .commit();
                    } else {
                        ParkedHereFragment parkedHereFragment = new ParkedHereFragment();
                        getSupportFragmentManager().beginTransaction()
                                .setTransition(FragmentTransaction.TRANSIT_ENTER_MASK)
                                .add(R.id.main_fragment, parkedHereFragment)
                                //.addToBackStack(null)
                                .commit();
                    }

                }
                mDrawer.closeMenu();
            }
        });

        mDrawer.setOnDrawerStateChangeListener(new ElasticDrawer.OnDrawerStateChangeListener() {
            @Override
            public void onDrawerStateChange(int oldState, int newState) {
                if (newState == ElasticDrawer.STATE_CLOSED) {
                    Log.i("MainActivity", "Drawer STATE_CLOSED");
                }
            }

            @Override
            public void onDrawerSlide(float openRatio, int offsetPixels) {
                Log.i("MainActivity", "openRatio=" + openRatio + " ,offsetPixels=" + offsetPixels);
            }
        });
    }

    public void changeNavDrawerFindCar(){
        SharedPreferences prefs = getSharedPreferences("ParkedHerePrefs", MODE_PRIVATE);
        boolean isParked = prefs.getBoolean("isParked", false);
        if(isParked){
            TextView tv = findViewById(R.id.park_here_tv);
            tv.setText("Find My Car");
        } else {
            TextView tv = findViewById(R.id.park_here_tv);
            tv.setText("Park My Car");
        }
    }



    public static LatLonCoordinate getLatLonCoordinate() {
        return latLonCoordinate;
    }

    public static void setLatLonCoordinate(LatLonCoordinate latLonCoordinate) {
        MainActivity.latLonCoordinate = latLonCoordinate;
    }
    boolean exit = false;
    @Override
    public void onBackPressed() {
        //control the back pressed action from here
        if(state == 0){
            //Home Page -> Exit App
//            DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
//                @Override
//                public void onClick(DialogInterface dialog, int which) {
//                    switch (which){
//                        case DialogInterface.BUTTON_POSITIVE:
//                            exit = true;
//                            onBackPressed();
//                            break;
//
//                        case DialogInterface.BUTTON_NEGATIVE:
//                            //No button clicked
//                            break;
//                    }
//                }
//            };
//
//            AlertDialog.Builder builder = new AlertDialog.Builder(getApplicationContext());
//            builder.setMessage("Are you sure?").setPositiveButton("Yes", dialogClickListener)
//                    .setNegativeButton("No", dialogClickListener).show();

          //  if(exit){
                //Yes button clicked
                this.finish();
                super.onBackPressed();
            //}


        } else if(state == 3 || state ==2 ){
            //open back home fragment
            state = 0;
            Main_Fragment main_fragment = new Main_Fragment();
            getSupportFragmentManager().beginTransaction()
                    .setTransition(FragmentTransaction.TRANSIT_ENTER_MASK)
                    .replace(R.id.main_fragment, main_fragment)
                    //.addToBackStack(null)
                    .commit();
        }
        else {
            state = 0;
            super.onBackPressed();
        }

        /*else {
            //open back home fragment
            state = 0;
            Main_Fragment main_fragment = new Main_Fragment();
            getSupportFragmentManager().beginTransaction()
                    .setTransition(FragmentTransaction.TRANSIT_ENTER_MASK)
                    .replace(R.id.main_fragment, main_fragment)
                    //.addToBackStack(null)
                    .commit();
        }*/
       // super.onBackPressed();
    }
}
