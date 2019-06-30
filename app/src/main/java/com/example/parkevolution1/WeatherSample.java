package com.example.parkevolution1;

public class WeatherSample {
    private String month;
    private double rainfall;
    private int sunHours;

    public String getMonth() {
        return month;
    }

    public void setMonth(String month) {
        this.month = month;
    }

    public double getRainfall() {
        return rainfall;
    }

    public void setRainfall(double rainfall) {
        this.rainfall = rainfall;
    }

    public int getSunHours() {
        return sunHours;
    }

    public void setSunHours(int sunHours) {
        this.sunHours = sunHours;
    }

    @Override
    public String toString() {
        return "WeatherSample{" +
                "month='" + month + '\'' +
                ", rainfall=" + rainfall +
                ", sunHours=" + sunHours +
                '}';
    }
}
