package com.myweather.app.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

public class WeatherSQLiteOpenHelper extends SQLiteOpenHelper{
    
    private static final String CREATE_PROVINCE = "create table province("
            + "id integer primary key autoincrement,"
            + "province_name text,"
            + "province_code text)";

    private static final String CREATE_CITY = "create table city("
            + "id integer primary key autoincrement,"
            + "city_name text,"
            + "city_code text,"
            + "province_id integer)";
    
    private static final String CREATE_COUNTY = "create table county("
            + "id integer primary key autoincrement,"
            + "county_name text,"
            + "county_code text,"
            + "city_id integer)";
    private static final String CREATE_COUNTY_WEATHER_REL = "create table county_weather_rel("
            + "id integer primary key autoincrement,"
            + "weather_code text,"
            + "county_id integer)";
    private static final String CREATE_WEATHER = "create table weather("
            + "id integer primary key autoincrement,"
            + "city_name text,"
            + "weather_temp text,"
            + "weather_desp text,"
            + "desp_img text,"
            + "whole_weather_temp text,"
            + "whole_weather_desp text,"
            + "whole_desp_img text,"
            + "wind_direction text,"
            + "wind_temp text,"
            + "publish_time text,"
            + "weather_code text)";
    
    public WeatherSQLiteOpenHelper(Context context, String name, CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_PROVINCE);
        db.execSQL(CREATE_CITY);
        db.execSQL(CREATE_COUNTY);
        db.execSQL(CREATE_COUNTY_WEATHER_REL);
        db.execSQL(CREATE_WEATHER);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // TODO Auto-generated method stub
        
    }

}
