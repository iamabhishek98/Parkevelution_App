package com.example.parkevolution1;

public class CarPark {
    private String name;
    private String address;
    private String hdb_car_parking_rate;
    private String mall_weekday_rate1;
    private String mall_weekday_rate2;
    private String mall_sat_rate;
    private String mall_sun_rate;

    public String getMall_weekday_rate1() {
        return mall_weekday_rate1;
    }

    public void setMall_weekday_rate1(String mall_weekday_rate1) {
        this.mall_weekday_rate1 = mall_weekday_rate1;
    }

    public String getMall_weekday_rate2() {
        return mall_weekday_rate2;
    }

    public void setMall_weekday_rate2(String mall_weekday_rate2) {
        this.mall_weekday_rate2 = mall_weekday_rate2;
    }

    public String getMall_sat_rate() {
        return mall_sat_rate;
    }

    public void setMall_sat_rate(String mall_sat_rate) {
        this.mall_sat_rate = mall_sat_rate;
    }

    public String getMall_sun_rate() {
        return mall_sun_rate;
    }

    public void setMall_sun_rate(String mall_sun_rate) {
        this.mall_sun_rate = mall_sun_rate;
    }

    public String getHdb_car_parking_rate() {
        return hdb_car_parking_rate;
    }

    public void setHdb_car_parking_rate(String hdb_car_parking_rate) {
        this.hdb_car_parking_rate = hdb_car_parking_rate;
    }

    public String getHdb_motorcycle_parking_rate() {
        return hdb_motorcycle_parking_rate;
    }

    public void setHdb_motorcycle_parking_rate(String hdb_motorcycle_parking_rate) {
        this.hdb_motorcycle_parking_rate = hdb_motorcycle_parking_rate;
    }

    private String hdb_motorcycle_parking_rate;
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    private String id;
    private double x_coord, y_coord;
    private double dist;
    enum DataCategory{
        HDB,
        SHOPPING_MALL,
        AVAILABILITY
    }
    private DataCategory dataCategory;

    public DataCategory getDataCategory() {
        return dataCategory;
    }

    public void setDataCategory(DataCategory dataCategory) {
        this.dataCategory = dataCategory;
    }

    @Override
    public String toString(){
        return "Carpark Name: " + this.name +
                    " address: " + this.address +
                    "dist: "+this.dist;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public double getX_coord() {
        return x_coord;
    }

    public void setX_coord(double x_coord) {
        this.x_coord = x_coord;
    }

    public double getY_coord() {
        return y_coord;
    }

    public void setY_coord(double y_coord) {
        this.y_coord = y_coord;
    }

    public double getDist() {
        return dist;
    }

    public void setDist(double dist) {
        this.dist = dist;
    }

    //this method does a simple pythagoras theorem to calc the distance and stores the distance in SVY21 format
    public void setDist(double x, double y){
        double x2 = Math.pow(x - this.x_coord, 2);
        double y2 = Math.pow(y - this.y_coord, 2);
        this.dist = Math.sqrt(x2 + y2);
    }
}
