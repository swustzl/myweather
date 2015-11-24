package com.swustzl.myweather.service;

import java.util.List;

import com.swustzl.myweather.R;
import com.swustzl.myweather.activity.ShowWeatherActivity;
import com.swustzl.myweather.database.WeatherDataBase;
import com.swustzl.myweather.model.Weather;
import com.swustzl.myweather.receiver.AutoUpdateWeatherReceiver;
import com.swustzl.myweather.util.HttpResponseListener;
import com.swustzl.myweather.util.HttpUtil;
import com.swustzl.myweather.util.ParserUtil;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.os.SystemClock;
import android.preference.PreferenceManager;

public class AutoUpdateWeatherService extends Service {

    private WeatherDataBase weatherDataBase;

    /**
     * 自动更新完成设置的时间间隔
     */
    private static final int NORMAL_TIME = 12 * 60 * 60 * 1000;

    /**
     * 自动更新失败设置重启的时间间隔
     */
    private static final int ABNORMAL_TIME = 60 * 60 * 1000;

    @Override
    public IBinder onBind(Intent intent) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        weatherDataBase = WeatherDataBase.getInstance(this);
        new Thread(new Runnable() {

            @Override
            public void run() {
                SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(AutoUpdateWeatherService.this);
                String weatherCode = pref.getString("weather_code", "");
                Boolean flag = pref.getBoolean("start_service", false);
                if (!flag) {
                    updateWeather(weatherCode);
                } else {
                    setFlagToFalse();
                    setAlarm(NORMAL_TIME);
                }
            }

        }).start();

        return super.onStartCommand(intent, flags, startId);
    }

    /**
     * 设置定时启动自动更新服务的时钟
     */
    private void setAlarm(int time) {
        AlarmManager manager = (AlarmManager) getSystemService(ALARM_SERVICE);
        long triggerAtTime = SystemClock.elapsedRealtime() + time;
        Intent i = new Intent(this, AutoUpdateWeatherReceiver.class);
        PendingIntent pi = PendingIntent.getBroadcast(this, 0, i, 0);
        manager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAtTime, pi);
    }

    protected void updateWeather(final String weatherCode) {

        HttpUtil.sendHttpRequest(HttpUtil.encapsulationUrl(weatherCode, "weatherInfo"), new HttpResponseListener() {

            @Override
            public void onFinish(String response) {
                ParserUtil.handleWeatherHtmlResponse(weatherDataBase, response, weatherCode);
                sendNotification(weatherCode);
                setAlarm(NORMAL_TIME);
            }

            @Override
            public void onError(Exception e) {
                setAlarm(ABNORMAL_TIME);
                e.printStackTrace();
            }
        });
    }

    /**
     * 服务同步数据后发送通知
     */
    private void sendNotification(String weatherCode) {
        List<Weather> list = weatherDataBase.findWeatherByCode(weatherCode);
        if (list != null && list.size() == 1) {
            Weather weather = list.get(0);
            NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            String title = "今日" + weather.getCityName() + "天气   " + weather.getWholeWeatherDesp();
            String con = weather.getWholeWeatherTemp() + "  " + weather.getWindTemp() + "  "
                    + weather.getWindDirection();
            Notification notification = new Notification(R.drawable.th_weather, title, System.currentTimeMillis());
            Intent intent = new Intent(this, ShowWeatherActivity.class);
            PendingIntent pi = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
            notification.setLatestEventInfo(this, title, con, pi);
            notification.defaults = Notification.DEFAULT_ALL;
            manager.notify(1, notification);
        }
    }

    /**
     * 设置选择城市后第一次开启服务的标志位，true第一次开启
     */
    private void setFlagToFalse() {
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(this).edit();
        editor.putBoolean("start_service", false);
        editor.commit();
    }

}
