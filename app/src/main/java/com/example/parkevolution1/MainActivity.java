package com.example.parkevolution1;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.mxn.soul.flowingdrawer_core.ElasticDrawer;
import com.mxn.soul.flowingdrawer_core.FlowingDrawer;

public class MainActivity extends AppCompatActivity {


    private static LatLonCoordinate latLonCoordinate;

    public static LatLonCoordinate getStartingLatLonCoordinate() {
        return startingLatLonCoordinate;
    }

    public static void setStartingLatLonCoordinate(LatLonCoordinate startingLatLonCoordinate) {
        MainActivity.startingLatLonCoordinate = startingLatLonCoordinate;
    }

    /**
     * Starting location for travel
     * */

    private static LatLonCoordinate startingLatLonCoordinate;
    private int state; // the current displaying fragment
    /**
     *  0 -> Home
     *  1 -> Detail (could be any detail fragment)
     *  2 -> Favourite Carpark
     *  3 -> Park here
     * */


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //it will load up dummy fragment which can be used as a very short temporary splash screen if u will...
        setContentView(R.layout.activity_main);
        state = 0;
    }


    @Override
    protected void onStart() {
        super.onStart();

        //open home fragment
        state = 0;
        Main_Fragment main_fragment = new Main_Fragment();
        getSupportFragmentManager().beginTransaction()
                .setTransition(FragmentTransaction.TRANSIT_ENTER_MASK)
                .replace(R.id.main_fragment, main_fragment)
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
                    //open back home fragment
                    ParkedHereFragment parkedHereFragment = new ParkedHereFragment();
                    getSupportFragmentManager().beginTransaction()
                            .setTransition(FragmentTransaction.TRANSIT_ENTER_MASK)
                            .replace(R.id.main_fragment, parkedHereFragment)
                            //.addToBackStack(null)
                            .commit();
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

    public static LatLonCoordinate getLatLonCoordinate() {
        return latLonCoordinate;
    }

    public static void setLatLonCoordinate(LatLonCoordinate latLonCoordinate) {
        MainActivity.latLonCoordinate = latLonCoordinate;
    }
}
