package com.example.parkevolution1;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;

public class MainActivity extends AppCompatActivity {

    private static LatLonCoordinate latLonCoordinate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public static LatLonCoordinate getLatLonCoordinate() {
        return latLonCoordinate;
    }

    public static void setLatLonCoordinate(LatLonCoordinate latLonCoordinate) {
        MainActivity.latLonCoordinate = latLonCoordinate;
    }
}
