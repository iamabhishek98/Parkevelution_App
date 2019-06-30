package com.example.parkevolution1;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
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
import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class AvailabilityFragment extends Fragment {

    private String availability_url = "https://api.data.gov.sg/v1/transport/carpark-availability";
    private RequestQueue mQueue;
    public static SVY21Coordinate currentSVY21Location;
    private CarPark[] cpArray = new CarPark[50];
    private List<CarPark> carParks = new ArrayList<>();

    public AvailabilityFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_availability, container, false);
    }

    //Button getJsonButton;
    //TextView txtJson;
    private ListView listView;

    private MyAvailabilityAdapter myAvailabilityAdapter;
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        availCarParks.clear();
        LatLonCoordinate testCoordinate = new LatLonCoordinate(1.344261, 103.720750);
        currentSVY21Location = testCoordinate.asSVY21();
        LatLonCoordinate realCoordinate = MainActivity.getLatLonCoordinate();
        if(realCoordinate != null){
            currentSVY21Location = realCoordinate.asSVY21();
            Log.v("Location_test", "Real coordinate for proximity is obtained");
        }

        listView = getView().findViewById(R.id.availabilityListView);

        myAvailabilityAdapter = new MyAvailabilityAdapter(getContext(), availCarParks);
        listView.setAdapter(myAvailabilityAdapter); //update later in data method

        if (currentSVY21Location != null) {
            calculateNearByCarparks();
        }
        //getJsonButton = getView().findViewById(R.id.getJsonButton);
        //txtJson = getView().findViewById(R.id.txtJson);

        mQueue = Volley.newRequestQueue(getContext());
       /*
        getJsonButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                jsonParse();
            }
        });*/
        jsonParse();
    }


    private void calculateNearByCarparks() {
        readCarParkData();
        calcCarparkDist();
        Log.v("Samuel", "Success-1");
    }

    private void calcCarparkDist() {
        //setting the square hypotenuse distance for each carparks in the entre array list -> O(n)
        for (CarPark carPark : carParks) {
            carPark.setDist(Math.pow(carPark.getX_coord() - currentSVY21Location.getEasting(), 2) + Math.pow(carPark.getY_coord() - currentSVY21Location.getNorthing(), 2));
        }

        //displays the arraylist sorted by distance
        Collections.sort(carParks, new Comparator<CarPark>() {
            @Override
            public int compare(CarPark cp1, CarPark cp2) {
                return (cp1.getDist() > cp2.getDist() ? 1 : (cp1.getDist() < cp2.getDist() ? -1 : 0));
            }
        });

        //transfer this data into the array
        for (int i = 0; i < 50; i++) {
            carParks.get(i).setDataCategory(CarPark.DataCategory.AVAILABILITY);
            cpArray[i] = carParks.get(i);
        }
    }

    private void readCarParkData() {
        carParks.clear();
        InputStream is = getActivity().getResources().openRawResource(R.raw.hdb_carparks1);
        BufferedReader reader = new BufferedReader(
                new InputStreamReader(is, Charset.forName("UTF-8"))
        );
        String line = "";
        try {
            while ((line = reader.readLine()) != null) {
                //split by ","
                String[] tokens = line.split(",");

                //Read the data
                CarPark carPark = new CarPark();
                carPark.setName(tokens[0]);
                carPark.setAddress(tokens[3]);
                try {
                    //Log.v("x-cord", tokens[1]);
                    carPark.setX_coord(Double.parseDouble(tokens[1]));
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                    carPark.setX_coord(0);
                }
                try {
                    //Log.v("y-cord", tokens[2]);
                    carPark.setY_coord(Double.parseDouble(tokens[2]));
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                    carPark.setY_coord(0);
                }


                carPark.setDist(1000000); //initialize all distance to 100000 first
                carParks.add(carPark);
            }
        } catch (IOException e) {
            Log.wtf("MyActivity", "Error reading data file on line" + line, e);
            e.printStackTrace();
        }


    }

    ArrayList<AvailCarPark> availCarParks = new ArrayList<>();
    ArrayList<AvailCarPark> allAvailCarParks = new ArrayList<>();

    private void jsonParse() {
        Log.v("Samuel", "Success-2");
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, availability_url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        //empty the current arraylist of availCarParks
                        availCarParks.clear();
                        //parsing happens here
                        try {
                            Log.v("Samuel", "Success-3");
                            JSONArray jsonArrayItems = response.getJSONArray("items");
                            JSONObject jsonObjectMain = jsonArrayItems.getJSONObject(0);
                            JSONArray jsonArrayCarpark_Data = jsonObjectMain.getJSONArray("carpark_data");
                            for (int i = 0; i < jsonArrayCarpark_Data.length(); i++) {
                                AvailCarPark availCarPark1 = new AvailCarPark();
                                JSONObject jsonObjectCarpark = jsonArrayCarpark_Data.getJSONObject(i);
                                JSONArray jsonArrayInfo = jsonObjectCarpark.getJSONArray("carpark_info");
                                JSONObject jsonObjectInfo = jsonArrayInfo.getJSONObject(0);

                                availCarPark1.setName(jsonObjectCarpark.getString("carpark_number"));
                                availCarPark1.setAvailLots(jsonObjectInfo.getInt("lots_available"));
                                availCarPark1.setTotalLots(jsonObjectInfo.getInt("total_lots"));
                                availCarPark1.setLotType(jsonObjectInfo.getString("lot_type"));
                                allAvailCarParks.add(availCarPark1);
                            }

                            for (int i = 0; i < cpArray.length; i++) {
                                String name = cpArray[i].getName();
                                int max = allAvailCarParks.size();
                                for (int j = 0; j < max; j++) {
                                    AvailCarPark availCarPark = allAvailCarParks.get(j);
                                    if (name.equals(availCarPark.getName())) {
                                        availCarPark.setAddress(cpArray[i].getAddress());
                                        availCarPark.setX_coordSVY21(cpArray[i].getX_coord());
                                        availCarPark.setY_coordSVY21(cpArray[i].getY_coord());
                                        availCarParks.add(availCarPark);
                                    }
                                }
                            }

                            //call function to display the list of data in the listview
                            for (int i = 0; i < availCarParks.size(); i++) {
                                Log.v("AvailCarparks", availCarParks.get(i).toString());
                            }
                            Log.v("Samuel", "Success + availCarparks size:" + availCarParks.size());
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        listView.invalidateViews();
                        myAvailabilityAdapter.notifyDataSetChanged();
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

    /*
    ProgressDialog pd; //this is only available from API 28 so find a way to make it available to older APIs

    private class JsonTask extends AsyncTask<String, String, String>{

        protected void onPreExecute(){
            super.onPreExecute();
            pd = new ProgressDialog(getContext());
            pd.setMessage("Please wait");
            pd.setCancelable(false);
            pd.show();
        }

        protected String doInBackground(String... params){
            HttpURLConnection connection = null;
            BufferedReader reader = null;

            try{
                URL url = new URL(params[0]);
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();

                InputStream stream = connection.getInputStream();
                reader = new BufferedReader(new InputStreamReader(stream));

                StringBuffer buffer = new StringBuffer();
                String line = "";

                while((line = reader.readLine()) != null){
                    buffer.append(line + "\n");
                    Log.d("Response: ", "> "+line); //here you will get the whole response
                }

                return buffer.toString();
            } catch(MalformedURLException e){
                e.printStackTrace();
            } catch(IOException e){
                e.printStackTrace();
            } finally{
                if(connection != null){
                    connection.disconnect();
                }
                try{
                    if(reader != null){
                        reader.close();
                    }
                } catch (IOException e){
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result){
            super.onPostExecute(result);
            if(pd.isShowing()){
                pd.dismiss();
            }
            txtJson.setText(result);
        }
    }
    */

    class AvailCarPark {
        private String name;
        private String address;
        private double x_coordSVY21, y_coordSVY21;

        public String getAddress() {
            return address;
        }

        public void setAddress(String address) {
            this.address = address;
        }

        public double getX_coordSVY21() {
            return x_coordSVY21;
        }

        public void setX_coordSVY21(double x_coordSVY21) {
            this.x_coordSVY21 = x_coordSVY21;
        }

        public double getY_coordSVY21() {
            return y_coordSVY21;
        }

        public void setY_coordSVY21(double y_coordSVY21) {
            this.y_coordSVY21 = y_coordSVY21;
        }

        private int totalLots, availLots;
        private String lotType;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getTotalLots() {
            return totalLots;
        }

        public void setTotalLots(int totalLots) {
            this.totalLots = totalLots;
        }

        public int getAvailLots() {
            return availLots;
        }

        public void setAvailLots(int availLots) {
            this.availLots = availLots;
        }

        public String getLotType() {
            return lotType;
        }

        public void setLotType(String lotType) {
            this.lotType = lotType;
        }

        @Override
        public String toString() {
            return "Carpark name: " + this.name
                    + " lot type: " + this.lotType
                    + " carpark total lots: " + this.totalLots
                    + " available lots: " + this.availLots;
        }

    }

    class MyAvailabilityAdapter extends ArrayAdapter<AvailCarPark> {
        Context context;
        AvailCarPark availCarPark;

        public MyAvailabilityAdapter(Context c, ArrayList<AvailCarPark> carParks) {
            super(c, 0, carParks);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final AvailCarPark carPark = getItem(position);

            if (convertView == null)
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.row_availability, parent, false);

            TextView tvName = convertView.findViewById(R.id.availability_name);
            TextView tvAvailability = convertView.findViewById(R.id.availability_value);
            tvName.setText(carPark.getAddress());
            tvAvailability.setText(carPark.getLotType() + ": " + carPark.getAvailLots() + "/" + carPark.getTotalLots());

            ImageView arrow = convertView.findViewById(R.id.availability_arrow);
            View mainV = convertView.findViewById(R.id.mainV_availability);

            mainV.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    LatLonCoordinate latLonCoordinateCP = (new SVY21Coordinate(carPark.getY_coordSVY21(), carPark.getX_coordSVY21())).asLatLon();
                    LatLng latLng = new LatLng(latLonCoordinateCP.getLatitude(), latLonCoordinateCP.getLongitude());
                    Main_Fragment.addMarkerToMap(latLng, carPark.getName());
                }
            });

            arrow.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    AvailaibilityDetailFragment availaibilityDetailFragment = new AvailaibilityDetailFragment();
                    Bundle bundle = new Bundle();
                    //pass in data here
                    Geocoder geocoder;
                    List<Address> addresses;
                    //all the Carpark DataCategory will be availability in this case
                    bundle.putString("carpark-id", carPark.getName());
                    bundle.putString("carpark-name", carPark.getAddress());
                    bundle.putString("carpark-lot-type", carPark.getLotType());
                    bundle.putInt("carpark-total-lots", carPark.getTotalLots());
                    bundle.putInt("carpark-avail-lots", carPark.getAvailLots());
                    //Address data
                    geocoder = new Geocoder(getActivity(), Locale.getDefault());
                    try{
                        LatLonCoordinate latLonCoordinateCp = (new SVY21Coordinate(carPark.getY_coordSVY21(), carPark.getX_coordSVY21())).asLatLon();
                        addresses = geocoder.getFromLocation(latLonCoordinateCp.getLatitude(), latLonCoordinateCp.getLongitude(), 1);
                        bundle.putString("carpark-address", addresses.get(0).getAddressLine(0));
                        bundle.putDouble("x-coord", latLonCoordinateCp.getLatitude());
                        bundle.putDouble("y-coord", latLonCoordinateCp.getLongitude());
                        bundle.putString("data-cat", "AVAILABILITY");
                    } catch(IOException e){
                        e.printStackTrace();
                        Log.v("Location_result_avail", "Get address for the carpark list view isnt't working");
                    }

                    availaibilityDetailFragment.setArguments(bundle);
                    getActivity().getSupportFragmentManager().beginTransaction()
                            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                            .replace(R.id.main_fragment, availaibilityDetailFragment)
                            .addToBackStack(null)
                            .commit();
                }
            });

            return convertView;
        }
    }
}
