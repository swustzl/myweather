package com.myweather.app.database;

import java.util.ArrayList;
import java.util.List;

import com.myweather.app.model.City;
import com.myweather.app.model.County;
import com.myweather.app.model.Province;
import com.myweather.app.model.Weather;
import com.myweather.app.util.LogUtil;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

/**
 * 操作数据库的单例类
 * */
public class WeatherDataBase {
    public static final String DB_NAME = "myweather";
    public static final int VERSION = 1;
    private static WeatherDataBase weatherDataBase;
    private SQLiteDatabase sDatabase;
    private WeatherDataBase(Context context){
        WeatherSQLiteOpenHelper dbHelper = new WeatherSQLiteOpenHelper(context, DB_NAME, null, VERSION);
        sDatabase = dbHelper.getWritableDatabase(); 
    }
    
    public synchronized static WeatherDataBase getInstance(Context context){
        if(weatherDataBase == null){
            weatherDataBase = new WeatherDataBase(context);
        }
        return weatherDataBase;       
    }
    
    /**
     * 存储省级数据
     * */
    public void saveProvince(Province province){
        if(province == null) return;
        ContentValues values = new ContentValues();
        values.put("province_name", province.getProvinceName());
        values.put("province_code", province.getProvinceCode());
        sDatabase.insert("province", null, values);
    }
    
    /**
     * 获取省级数据
     * */
    public List<Province> findProvince(){
        List<Province> provinceList = new ArrayList<Province>();
        Cursor cursor = sDatabase.query("province", null, null, null, null, null, null);
        if(cursor.moveToFirst()){
            do{
                Province province = new Province();
                province.setId(cursor.getInt(cursor.getColumnIndex("id")));
                province.setProvinceName(cursor.getString(cursor.getColumnIndex("province_name")));
                province.setProvinceCode(cursor.getString(cursor.getColumnIndex("province_code")));
                provinceList.add(province);
            }while(cursor.moveToNext());
        }
        if(cursor != null) {cursor.close();}
        return provinceList;
    }
    
    /**
     * 存储市级数据
     * */
    public void saveCity(City city){
        ContentValues values = new ContentValues();
        values.put("city_name", city.getCityName());
        values.put("city_code", city.getCityCode());
        values.put("province_id", city.getProvinceId());
        sDatabase.insert("city", null, values);
    }
    
    /**
     * 获取市级数据
     * */
    public List<City> findCity(Integer provinceId){
        List<City> cityList = new ArrayList<City>();
        Cursor cursor = sDatabase.rawQuery("select * from city where province_id = ?", new String[]{String.valueOf(provinceId)});
        if(cursor.moveToFirst()){
            do{
                City city = new City();
                city.setCityCode(cursor.getString(cursor.getColumnIndex("city_code")));
                city.setCityName(cursor.getString(cursor.getColumnIndex("city_name")));
                city.setId(cursor.getInt(cursor.getColumnIndex("id")));
                city.setProvinceId(cursor.getInt(cursor.getColumnIndex("province_id")));
                cityList.add(city);
            }while(cursor.moveToNext());
        }
        if(cursor != null){cursor.close();}
        return cityList;
    }
    
    /**
     * 存储县级数据
     * */
    public void saveCounty(County county){
        ContentValues values = new ContentValues();
        values.put("county_name", county.getCountyName());
        values.put("county_code", county.getCountyCode());
        values.put("city_id", county.getCityId());
        sDatabase.insert("county", null, values);
    }
    
    /**
     * 获取县级数据
     * */
    public List<County> findCounty(Integer cityId){
        List<County> countyList = new ArrayList<County>();
        Cursor cursor = sDatabase.rawQuery("select * from county where city_id = ?", new String[]{String.valueOf(cityId)});
        if(cursor.moveToFirst()){
            do{
                County county = new County();
                county.setCountyCode(cursor.getString(cursor.getColumnIndex("county_code")));
                county.setCountyName(cursor.getString(cursor.getColumnIndex("county_name")));
                county.setId(cursor.getInt(cursor.getColumnIndex("id")));
                county.setCityId(cursor.getInt(cursor.getColumnIndex("city_id")));
                countyList.add(county);
            }while(cursor.moveToNext());
        }
        if(cursor != null){cursor.close();}
        return countyList;
    }
    
    /**
     * 存储县级id和对应的天气码数据
     * */
    public void saveCountyWeatherRel(Integer countyId, String weatherCode){
        ContentValues values = new ContentValues();
        values.put("weather_code", weatherCode);
        values.put("county_id", countyId);
        sDatabase.insert("county_weather_rel", null, values);
    }
    
    /**
     * 根据县级id获取对应的天气码
     * */
    public String findWeatherCodeByCountyId(Integer countyId){
        String weatherCode = null;
        Cursor cursor = sDatabase.rawQuery("select * from county_weather_rel where county_id = ? limit 1", new String[]{String.valueOf(countyId)});
        if(cursor.moveToFirst()){
            weatherCode = cursor.getString(cursor.getColumnIndex("weather_code"));
        }
        if(cursor != null){cursor.close();}
        return weatherCode;
    }
    
    /**
     * 存储天气数据
     * */
    public void saveWeather(Weather weather){
        ContentValues values = new ContentValues();
        values.put("city_name", weather.getCityName());
        values.put("weather_desp", weather.getWeatherDesp());
        values.put("weather_temp", weather.getWeatherTemp());
        values.put("whole_weather_desp", weather.getWholeWeatherDesp());
        values.put("whole_weather_temp", weather.getWholeWeatherTemp());
        values.put("wind_direction", weather.getWindDirection());
        values.put("wind_temp", weather.getWindTemp());
        values.put("publish_time", weather.getPublishTime());
        values.put("desp_img", weather.getDespImg());
        values.put("whole_desp_img", weather.getWholeDespImg());
        if(findWeatherCountByCode(weather.getWeatherCode()) > 0){
            sDatabase.update("weather", values, "weather_code = ?", new String[]{weather.getWeatherCode()});
        }
        else{
            values.put("weather_code", weather.getWeatherCode());
            sDatabase.insert("weather", null, values);
        }
       
    }
    
    /**
     * 根据天气码获取对应的天气条数
     * */
    public Integer findWeatherCountByCode(String weatherCode){
        LogUtil.i("findWeatherCountByCode", weatherCode);
        Cursor cursor = sDatabase.rawQuery("select count(*) as size from weather where weather_code = ?", new String[]{weatherCode});
        int size = 0;
        if(cursor.moveToFirst()){
            size = cursor.getInt(cursor.getColumnIndex("size"));
        }
        if(cursor != null){cursor.close();}
        return size;
    }
    
    /**
     * 根据天气码获取对应的天气
     * */
    public List<Weather> findWeatherByCode(String weatherCode){
        List<Weather> list = new ArrayList<Weather>();
        Cursor cursor = sDatabase.rawQuery("select * from weather where weather_code = ?", new String[]{weatherCode});
        
        if(cursor.moveToFirst()){
            do{
                Weather weather = new Weather();
                weather.setId(cursor.getInt(cursor.getColumnIndex("id")));
                weather.setCityName(cursor.getString(cursor.getColumnIndex("city_name")));
                weather.setPublishTime(cursor.getString(cursor.getColumnIndex("publish_time")));
                weather.setWeatherCode(cursor.getString(cursor.getColumnIndex("weather_code")));
                weather.setWeatherDesp(cursor.getString(cursor.getColumnIndex("weather_desp")));
                weather.setWeatherTemp(cursor.getString(cursor.getColumnIndex("weather_temp")));
                weather.setWholeWeatherDesp(cursor.getString(cursor.getColumnIndex("whole_weather_desp")));
                weather.setWholeWeatherTemp(cursor.getString(cursor.getColumnIndex("whole_weather_temp")));
                weather.setWindDirection(cursor.getString(cursor.getColumnIndex("wind_direction")));
                weather.setWindTemp(cursor.getString(cursor.getColumnIndex("wind_temp")));
                weather.setDespImg(cursor.getString(cursor.getColumnIndex("desp_img")));
                weather.setWholeDespImg(cursor.getString(cursor.getColumnIndex("whole_desp_img")));
                list.add(weather);
            }while(cursor.moveToNext());
        }
        if(cursor != null){cursor.close();}
        return list;
    }


}
