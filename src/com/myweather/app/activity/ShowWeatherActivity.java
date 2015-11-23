package com.myweather.app.activity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import com.myweather.app.R;
import com.myweather.app.database.WeatherDataBase;
import com.myweather.app.model.Weather;
import com.myweather.app.service.AutoUpdateWeatherService;
import com.myweather.app.util.HttpResponseListener;
import com.myweather.app.util.HttpUtil;
import com.myweather.app.util.LogUtil;
import com.myweather.app.util.ParserUtil;
import com.squareup.picasso.Picasso;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class ShowWeatherActivity extends Activity implements OnClickListener {
    private LinearLayout weatherInfoLayout;
    private TextView cityName;
    private TextView publishInfo;
    private TextView weatherDesp;
    private TextView temp;
    private TextView wholeWeatherDesp;
    private TextView wholeWeatherTemp;
    private TextView windDirection;
    private TextView windTemp;
    private ImageView despImg;
    private ImageView wholeDespImg1;
    private ImageView wholeDespImg2;

    private WeatherDataBase weatherDataBase;

    private Button selectCity;
    private Button refreshWeather;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.weather);
        LinearLayout weatherLayout = (LinearLayout) findViewById(R.id.weather_layout);
        setWeatherBackgroud(weatherLayout);
        weatherDataBase = WeatherDataBase.getInstance(this);
        weatherInfoLayout = (LinearLayout) findViewById(R.id.weather_info_layout);
        RelativeLayout title = (RelativeLayout) findViewById(R.id.title);
        title.getBackground().setAlpha(100);
        cityName = (TextView) findViewById(R.id.city_name);
        publishInfo = (TextView) findViewById(R.id.publish_info);
        weatherDesp = (TextView) findViewById(R.id.weather_desp);
        temp = (TextView) findViewById(R.id.temp);
        wholeWeatherDesp = (TextView) findViewById(R.id.whole_weather_desp);
        wholeWeatherTemp = (TextView) findViewById(R.id.whole_weather_temp);
        windDirection = (TextView) findViewById(R.id.wind_direction);
        windTemp = (TextView) findViewById(R.id.wind_temp);
        despImg = (ImageView) findViewById(R.id.desp_img);
        wholeDespImg1 = (ImageView) findViewById(R.id.whole_desp_img1);
        wholeDespImg2 = (ImageView) findViewById(R.id.whole_desp_img2);
        String weatherCode = getIntent().getStringExtra("weatherCode");
        if (!TextUtils.isEmpty(weatherCode)) {
            SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(this).edit();
            editor.putString("weather_code", weatherCode);
            editor.putBoolean("city_selected", true);
            editor.putBoolean("start_service", true);
            editor.commit();
            Intent intent = new Intent(this, AutoUpdateWeatherService.class);
            startService(intent);
        }
        showWeatherInfo();
        selectCity = (Button) findViewById(R.id.select_city);
        refreshWeather = (Button) findViewById(R.id.refresh_weather);
        selectCity.setOnClickListener(this);
        refreshWeather.setOnClickListener(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        manager.cancel(1);
    }

    /**
     * 设置背景图片 春夏秋冬四种
     */
    private void setWeatherBackgroud(LinearLayout weatherLayout) {
        SimpleDateFormat sdf = new SimpleDateFormat("M", Locale.CHINA);
        Integer month = Integer.valueOf(sdf.format(new Date()));
        if (month > 2 && month <= 5) {
            weatherLayout.setBackgroundResource(R.drawable.spring);
        }
        if (month > 5 && month <= 8) {
            weatherLayout.setBackgroundResource(R.drawable.summer);
        }
        if (month > 8 && month <= 11) {
            weatherLayout.setBackgroundResource(R.drawable.autumn);
        }
        if (month > 11 || month <= 2) {
            weatherLayout.setBackgroundResource(R.drawable.winter);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
        case R.id.select_city:
            Intent intent = new Intent(this, ChooseAreaActivity.class);
            intent.putExtra("from_weather_activity", true);
            startActivity(intent);
            finish();
            break;
        case R.id.refresh_weather:
            publishInfo.setText("同步中...");
            String weatherCode = getWeatherCodeFromPrefs();
            if (!TextUtils.isEmpty(weatherCode)) {
                loadFromServer(weatherCode, "weatherInfo");
            }
            break;
        default:
            break;
        }
    }

    /**
     * 展示SharedPreferences中天气信息数据
     */
    private void showWeatherInfo() {
        String weatherCode = getWeatherCodeFromPrefs();
        List<Weather> list = weatherDataBase.findWeatherByCode(weatherCode);
        LogUtil.i("showWeatherInfo", String.valueOf(list.size()));
        SimpleDateFormat sdf = new SimpleDateFormat("M月d日", Locale.CHINA);
        String date = sdf.format(new Date());
        if (list.size() == 1 && list.get(0).getPublishTime().contains(date)) {
            setShowContent(list.get(0));

        } else {
            loadFromServer(weatherCode, "weatherInfo");
        }

    }

    /**
     * 从sharedPreferences中获取天气码
     */
    private String getWeatherCodeFromPrefs() {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        String weatherCode = pref.getString("weather_code", "");
        return weatherCode;
    }

    /**
     * 设置要展示的天气内容
     */
    private void setShowContent(Weather weather) {
        cityName.setText(weather.getCityName());
        weatherDesp.setText(weather.getWeatherDesp());
        publishInfo.setText(weather.getPublishTime());
        temp.setText(weather.getWeatherTemp());
        wholeWeatherDesp.setText(weather.getWholeWeatherDesp());
        wholeWeatherTemp.setText(weather.getWholeWeatherTemp());
        windDirection.setText(weather.getWindDirection());
        windTemp.setText(weather.getWindTemp());
        Picasso.with(this).load(weather.getDespImg()).into(despImg);
        showWholeDespImg(weather.getWholeDespImg());
        weatherInfoLayout.setVisibility(View.VISIBLE);
        cityName.setVisibility(View.VISIBLE);
    }

    /**
     * 显示全天信息中的天气图片
     */
    private void showWholeDespImg(String wholeDespImg) {
        if (!TextUtils.isEmpty(wholeDespImg)) {
            String img[] = wholeDespImg.split(",");
            if (img.length == 1) {
                Picasso.with(this).load(img[0]).into(wholeDespImg1);
                wholeDespImg1.setVisibility(View.VISIBLE);
                wholeDespImg2.setVisibility(View.INVISIBLE);
            } else if (img.length == 2) {
                Picasso.with(this).load(img[0]).into(wholeDespImg1);
                Picasso.with(this).load(img[1]).into(wholeDespImg2);
                wholeDespImg1.setVisibility(View.VISIBLE);
                wholeDespImg2.setVisibility(View.VISIBLE);
            }
        }

    }

    /**
     * 从服务器加载对应的数据数据
     * 
     * @param code 要查询当前类型信息所需要的 码
     * @param type 所要查询的信息 类型
     * */
    private void loadFromServer(final String code, final String type) {
        HttpUtil.sendHttpRequest(HttpUtil.encapsulationUrl(code, type), new HttpResponseListener() {

            @Override
            public void onFinish(String response) {
                if ("weatherInfo".equals(type)) {
                    ParserUtil.handleWeatherHtmlResponse(weatherDataBase, response, code);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            showWeatherInfo();
                        }
                    });
                }

            }

            @Override
            public void onError(Exception e) {
                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        publishInfo.setText("同步失败！");
                        Toast.makeText(ShowWeatherActivity.this, "同步数据失败！请检查网络连接...", Toast.LENGTH_SHORT).show();
                    }
                });

            }
        });

    }

}
