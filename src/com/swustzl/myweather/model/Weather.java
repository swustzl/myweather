package com.swustzl.myweather.model;

public class Weather {
   
    private int id;
    private String cityName;
    private String weatherTemp;
    private String weatherDesp;
    private String wholeWeatherTemp;
    private String wholeWeatherDesp;
    private String windDirection;
    private String windTemp;
    private String publishTime;
    private String weatherCode;
    private String despImg;
    private String wholeDespImg;
    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }
    public String getCityName() {
        return cityName;
    }
    public void setCityName(String cityName) {
        this.cityName = cityName;
    }
    public String getWeatherTemp() {
        return weatherTemp;
    }
    public void setWeatherTemp(String weatherTemp) {
        this.weatherTemp = weatherTemp;
    }
    public String getWeatherDesp() {
        return weatherDesp;
    }
    public void setWeatherDesp(String weatherDesp) {
        this.weatherDesp = weatherDesp;
    }
    public String getWholeWeatherTemp() {
        return wholeWeatherTemp;
    }
    public void setWholeWeatherTemp(String wholeWeatherTemp) {
        this.wholeWeatherTemp = wholeWeatherTemp;
    }
    public String getWholeWeatherDesp() {
        return wholeWeatherDesp;
    }
    public void setWholeWeatherDesp(String wholeWeatherDesp) {
        this.wholeWeatherDesp = wholeWeatherDesp;
    }
    public String getWindDirection() {
        return windDirection;
    }
    public void setWindDirection(String windDirection) {
        this.windDirection = windDirection;
    }
    public String getWindTemp() {
        return windTemp;
    }
    public void setWindTemp(String windTemp) {
        this.windTemp = windTemp;
    }
    public String getPublishTime() {
        return publishTime;
    }
    public void setPublishTime(String publishTime) {
        this.publishTime = publishTime;
    }
    public String getWeatherCode() {
        return weatherCode;
    }
    public void setWeatherCode(String weatherCode) {
        this.weatherCode = weatherCode;
    }
    public String getDespImg() {
        return despImg;
    }
    public void setDespImg(String despImg) {
        this.despImg = despImg;
    }
    public String getWholeDespImg() {
        return wholeDespImg;
    }
    public void setWholeDespImg(String wholeDespImg) {
        this.wholeDespImg = wholeDespImg;
    }

}
