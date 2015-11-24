package com.swustzl.myweather.model;

public class CountyWeatherRel {
    private int id;
    private String weatherCode;
    private int countyId;
    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }
    public String getWeatherCode() {
        return weatherCode;
    }
    public void setWeatherCode(String weatherCode) {
        this.weatherCode = weatherCode;
    }
    public int getCountyId() {
        return countyId;
    }
    public void setCountyId(int countyId) {
        this.countyId = countyId;
    }

}
