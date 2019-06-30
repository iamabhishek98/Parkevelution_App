package com.example.parkevolution1;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

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
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

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

public class AvailaibilityDetailFragment extends Fragment implements OnMapReadyCallback, GoogleApiClient.OnConnectionFailedListener {

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

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        map.getUiSettings().setMyLocationButtonEnabled(false);

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
        map.setMyLocationEnabled(true);
        map.clear();
        LatLng latLng = new LatLng(latitude, longitude);
        //map.animateCamera(CameraUpdateFactory.newLatLng(latLng));
        //map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
        MarkerOptions options = new MarkerOptions().position(latLng).title(carpark_name);
        map.addMarker(options);
        Log.v("Map_check", "marker added");
    }

    private String data_cat;
    private String hdb_car_parking_price;
    private String hdb_motorcycle_parking_price;
    private String hdb_carpark_id;
    private String carpark_lot_type;
    private int carpark_total_lots;
    private int carpark_avail_lots;
    private String carpark_id;


    public AvailaibilityDetailFragment() {
        // Required empty public constructor
    }

    private Bundle bundle;
    private RequestQueue mQueue;

    private GoogleMap map;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        bundle = this.getArguments();
        carpark_id = bundle.getString("carpark-id");
        carpark_name = bundle.getString("carpark-name");
        carpark_address = bundle.getString("carpark-address");
        latitude = bundle.getDouble("x-coord");
        longitude = bundle.getDouble("y-coord");
        data_cat = bundle.getString("data-cat");
        carpark_lot_type = bundle.getString("carpark-lot-type");
        carpark_total_lots = bundle.getInt("carpark-total-lots");
        carpark_avail_lots = bundle.getInt("carpark-avail-lots");
        mQueue = Volley.newRequestQueue(getContext());
        currentPosition = new LatLonCoordinate(MainActivity.getLatLonCoordinate().getLatitude(), MainActivity.getLatLonCoordinate().getLongitude());
        return inflater.inflate(R.layout.fragment_availaibility_detail, container, false);
    }

    private SupportMapFragment mapFragment;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        //setting up the map
        mapFragment = (SupportMapFragment)getChildFragmentManager().findFragmentById(R.id.map_availability_detail);
        mapFragment.getMapAsync(this);

        //setting up the text fields
        TextView carparkNameTv = getView().findViewById(R.id.carpark_name_availability);
        carparkNameTv.setText(carpark_name);

        TextView carparkAddressTv = getView().findViewById(R.id.address_text_availability);
        carparkAddressTv.setText(carpark_address);

        TextView carparkDurationTv = getView().findViewById(R.id.duration_text_availability);
        getDurationInfo(currentPosition, new LatLonCoordinate(latitude, longitude), carparkDurationTv);

        TextView carparkAvailabilityTv = getView().findViewById(R.id.availability_text_availability);
        String availText = "Lot Type: "+carpark_lot_type+"\n"+
                "Availability: "+carpark_avail_lots+"/"+carpark_total_lots;
        carparkAvailabilityTv.setText(availText);

        TextView carParkRateTv = getView().findViewById(R.id.parking_cost_text_availability);
        getRate(carpark_id, carParkRateTv);
        super.onViewCreated(view, savedInstanceState);
    }

    private void getRate(String id, TextView textView){
        InputStream is = getActivity().getResources().openRawResource(R.raw.hdb_carparks4);
        BufferedReader reader = new BufferedReader(
                new InputStreamReader(is, Charset.forName("UTF-8"))
        );
        String line = "";
        try{
            while((line = reader.readLine()) != null){
                //split by ","
                String[] tokens = line.split(",");
                if(tokens[0].equals(id)){
                    //get the carpark rate
                    hdb_car_parking_price = tokens[4];
                    hdb_motorcycle_parking_price="Whole Day\n(7:00am to 10:30pm)\n$0.65 per lot\nWhole Night\n(10:30pm to 7:00am on the following day)\n$0.65 per lot";
                    String parkingRate = "Car rate: \n"+hdb_car_parking_price+"\n\nMotorcycle Rate:\n"+hdb_motorcycle_parking_price;
                    textView.setText(parkingRate);
                }
            }
        }catch(IOException e){
            Log.wtf("MyActivity", "Error reading data file on line" + line, e);
            e.printStackTrace();
        }
    }

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
                            //Info distanceInfo = leg.getDistance();
                            Info durationInfo = leg.getDuration();
                            //String distance = distanceInfo.getText();
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
                            int padding = (int) (width * 0.18); // offset from edges of the map 10% of screen

                            CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, width, height, padding);
                            map.moveCamera(cu);
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

    /*
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
                                Log.v("Detail_proximity", "json id: "+jsonObjectCarpark.getString("carpark_number"));
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
                            }
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
    }*/
}

