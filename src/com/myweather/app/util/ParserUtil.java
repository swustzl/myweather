package com.myweather.app.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import android.text.TextUtils;

import com.myweather.app.database.WeatherDataBase;
import com.myweather.app.model.City;
import com.myweather.app.model.County;
import com.myweather.app.model.Province;
import com.myweather.app.model.Weather;

/**
 * 数据解析工具类
 */
public class ParserUtil {

    /**
     * 处理返回的省级数据
     */
    public static Boolean handleProvincesResponse(WeatherDataBase weatherDataBase, String response) {
        if (!TextUtils.isEmpty(response)) {
            LogUtil.v("response", response);
            String[] provinces = response.split(",");
            for (String p : provinces) {
                Province province = new Province();
                String[] a = p.split("\\|");
                province.setProvinceCode(a[0]);
                province.setProvinceName(a[1]);
                weatherDataBase.saveProvince(province);
            }
            return true;
        }
        return false;
    }

    /**
     * 处理返回的市级数据
     */
    public static Boolean handleCitiesResponse(WeatherDataBase weatherDataBase, String response, Integer provinceId) {
        if (!TextUtils.isEmpty(response)) {
            LogUtil.v("response", response);
            String[] cities = response.split(",");
            for (String s : cities) {
                City city = new City();
                String[] c = s.split("\\|");
                city.setCityCode(c[0]);
                city.setCityName(c[1]);
                city.setProvinceId(provinceId);
                weatherDataBase.saveCity(city);
            }
            return true;
        }
        return false;
    }

    /**
     * 处理返回的县级数据
     */
    public static Boolean handleCountiesResponse(WeatherDataBase weatherDataBase, String response, Integer cityId) {
        if (!TextUtils.isEmpty(response)) {
            LogUtil.v("response", response);
            String[] counties = response.split(",");
            for (String s : counties) {
                County county = new County();
                String[] c = s.split("\\|");
                county.setCountyCode(c[0]);
                county.setCountyName(c[1]);
                county.setCityId(cityId);
                weatherDataBase.saveCounty(county);
            }
            return true;
        }
        return false;
    }

    /**
     * 处理服务器返回的html格式天气数据
     */
    public static void handleWeatherHtmlResponse(WeatherDataBase weatherDataBase, String response, String weatherCode) {
        LogUtil.i("html", response);
        try {
            Weather weather = packagingWeather(response, weatherCode);
            weatherDataBase.saveWeather(weather);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 对weather对象进行填充数据
     */
    private static Weather packagingWeather(String response, String weatherCode) {
        Weather weather = new Weather();
        Document doc = Jsoup.parse(response);
        weather = getWholeWeatherDesp(weather, doc);
        weather.setWholeWeatherTemp(getWholeWeatherTemp(doc));

        Element sk = doc.getElementsByClass("sk").get(0);
        weather.setPublishTime(getPublishTime(sk));
        weather.setCityName(getCityName(sk));
        weather.setWeatherCode(weatherCode);
        weather = getWeatherTempAndImg(weather, sk.getElementsByTag("td"));

        return weather;
    }

    private static Weather getWeatherTempAndImg(Weather weather, Elements tds) {
        String despImg = tds.get(0).getElementsByTag("img").get(0).attr("src");
        String weatherTemp = tds.get(1).text(); // 温度
        weather = getWeatherAndWind(weather, tds.get(2).getElementsByTag("span"));
        weather.setWeatherTemp(weatherTemp);
        weather.setDespImg(despImg);
        return weather;
    }

    private static Weather getWeatherAndWind(Weather weather, Elements spans) {
        String weatherDesp = spans.get(0).text(); // 天气
        String windDirection = spans.get(1).text(); // 风向
        String windTemp = spans.get(2).text(); // 风度
        weather.setWeatherDesp(weatherDesp);
        weather.setWindDirection(windDirection);
        weather.setWindTemp(windTemp);
        return weather;
    }

    private static String getCityName(Element sk) {
        String cityName = sk.getElementsByTag("h1").get(0).getElementsByTag("span").get(0).getElementsByTag("a").get(0)
                .text();
        return cityName;
    }

    private static String getWholeWeatherTemp(Document doc) {
        String wholeWeatherTemp = doc.getElementsByClass("days7").get(0).getElementsByTag("li").get(0)
                .getElementsByTag("span").get(0).text();
        return wholeWeatherTemp;
    }

    private static String getPublishTime(Element sk) {
        SimpleDateFormat sdf = new SimpleDateFormat("H:mm", Locale.CHINA);
        String date = sdf.format(new Date());
        Element h2 = sk.getElementsByTag("h2").get(0);
        String publishTime = h2.text() + date + "更新";
        return publishTime;
    }

    private static Weather getWholeWeatherDesp(Weather weather, Document doc) {
        Elements imgs = doc.getElementsByClass("days7").get(0).getElementsByTag("li").get(0).getElementsByTag("i")
                .get(0).getElementsByTag("img");
        String wholeWeatherDesp;
        String wholeDespImg;
        if (imgs.size() == 1) {
            wholeWeatherDesp = imgs.get(0).attr("alt");
            wholeDespImg = imgs.get(0).attr("src");
        } else {
            String dayDesp = imgs.get(0).attr("alt");
            String dayImg = imgs.get(0).attr("src");
            String nightDesp = imgs.get(1).attr("alt");
            String nightImg = imgs.get(1).attr("src");
            if (dayDesp.equals(nightDesp)) {
                wholeWeatherDesp = dayDesp;
            } else {
                wholeWeatherDesp = dayDesp + "转" + nightDesp;
            }
            wholeDespImg = dayImg + "," + nightImg;
        }
        weather.setWholeWeatherDesp(wholeWeatherDesp);
        weather.setWholeDespImg(wholeDespImg);
        return weather;
    }

    /**
     * 解析县级码和天气码数据
     */
    public static Boolean handleCountyWeatherRelResponse(WeatherDataBase weatherDataBase, String response, int countyId) {
        if (!TextUtils.isEmpty(response)) {
            LogUtil.v("response", response);
            String[] array = response.split("\\|");
            if (array != null && array.length == 2) {
                String weatherCode = array[1];
                weatherDataBase.saveCountyWeatherRel(countyId, weatherCode);
                return true;
            }
        }
        return false;
    }

}
