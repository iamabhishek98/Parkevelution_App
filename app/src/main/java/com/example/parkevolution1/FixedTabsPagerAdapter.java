package com.example.parkevolution1;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.ArrayList;

public class FixedTabsPagerAdapter extends FragmentPagerAdapter {

    ArrayList<Fragment> fr_list;
    public FixedTabsPagerAdapter(FragmentManager fm, ArrayList<Fragment> fr_list) {
        super(fm);
        this.fr_list = fr_list;
    }

    @Override
    public int getItemPosition(@NonNull Object object) {
        //return super.getItemPosition(object);
        // POSITION_NONE makes it possible to reload the PagerAdapter
        return POSITION_NONE;
    }

    @Override
    public Fragment getItem(int i) {
/*
        switch(i){
            case 0:
                return new ProximityFragment();
            case 1:
                return new PriceFragment();
            case 2:
                return new AvailabilityFragment();
            default:
                return null;
        }
        */return fr_list.get(i);
    }

    @Override
    public int getCount() {
        return 3;
    }


    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        switch(position){
            case 0:
                return "PROXIMITY";
            case 1:
                return "PRICE";
            case 2:
                return "AVAILABILITY";
            default:
                return null;
        }
    }



}
