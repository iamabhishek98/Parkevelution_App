package com.example.parkevolution1;

public class ParkedHere {

    private double parkedLatitude;
    private double parkedLongitude;
    private String parkedAddress;
    private String parkedDescription;
    private boolean parkedBoolean;

    public ParkedHere(double parkedLatitude, double parkedLongitude, String parkedAddress, String parkedDescription, boolean parkedBoolean){
        this.parkedLatitude = parkedLatitude;
        this.parkedLongitude = parkedLongitude;
        this.parkedAddress = parkedAddress;
        this.parkedDescription = parkedDescription;
        this.parkedBoolean = parkedBoolean;
    }

    public double getParkedLatitude() {
        return parkedLatitude;
    }

    public void setParkedLatitude(double parkedLatitude) {
        this.parkedLatitude = parkedLatitude;
    }

    public double getParkedLongitude() {
        return parkedLongitude;
    }

    public void setParkedLongitude(double parkedLongitude) {
        this.parkedLongitude = parkedLongitude;
    }

    public String getParkedAddress() {
        return parkedAddress;
    }

    public void setParkedAddress(String parkedAddress) {
        this.parkedAddress = parkedAddress;
    }

    public String getParkedDescription() {
        return parkedDescription;
    }

    public void setParkedDescription(String parkedDescription) {
        this.parkedDescription = parkedDescription;
    }

    public boolean isParkedBoolean() {
        return parkedBoolean;
    }

    public void setParkedBoolean(boolean parkedBoolean) {
        this.parkedBoolean = parkedBoolean;
    }

}
