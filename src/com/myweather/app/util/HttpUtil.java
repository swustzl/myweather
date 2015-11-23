package com.myweather.app.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import android.text.TextUtils;

/**
 * http相关工具类
 * */
public class HttpUtil {
    
    /**
     * 发送http请求
     * */
    public static void sendHttpRequest(String address, HttpResponseListener listener){
        HttpRequestThread thread = new HttpRequestThread(address,listener);
        thread.start();
    }
    
    /**
     * 发送http请求
     * */
    public static void saveImage(final String address){
        LogUtil.w("imageurl", address);
        new Thread(new Runnable(){

            @Override
            public void run() {
                HttpURLConnection connection = null;
                try{
                    URL url = new URL(address);
                    connection = (HttpURLConnection)url.openConnection();
                    connection.setRequestMethod("GET");
                    connection.setConnectTimeout(8000);
                    connection.setReadTimeout(8000);
                    InputStream in = connection.getInputStream();
                    
                    File file = new File("/res/drawable-hdpi/1.png");
                    OutputStream out = new FileOutputStream(file);
                    byte[] b = new byte[1024];
                    int len = 0;
                    while ((len = in.read(b)) != -1) {
                       out.write(b, 0, len);
                    }
                    in.close();
                    out.close();
                    
                }catch(Exception e){
                    
                } finally {
                    if(connection != null){
                        connection.disconnect();
                    }
                }     
                
            }}).start();
    }
    
    /**
     * 封装URL
     * @param code  要查询当前类型信息所需要的 码 ，查询省信息时，不需要code 可为null
     * @param type 
     *            所要查询的信息类型 ：
     *            province 省 ，city 市 ， county 县 ，weatherCode 天气码， weatherInfo 天气信息
     * @return url 根据封装好的URL*/
    public static String  encapsulationUrl(String code, String type){
        String url = null;
        if(!TextUtils.isEmpty(type)){
            if("weatherInfo".equals(type)){
                //url = "http://www.weather.com.cn/data/cityinfo/" + code + ".html";
                url = "http://m.weather.com.cn/mweather/"+ code +".shtml";
            }
            else if ("province".equals(type)){
                url = "http://www.weather.com.cn/data/list3/city.xml";
            }
            else {
                url = "http://www.weather.com.cn/data/list3/city" + code + ".xml";
            }
        }
        
        LogUtil.i("urlInfo", "code:{"+code+"}");
        LogUtil.i("urlInfo", "type:{"+type+"}");
        LogUtil.i("urlInfo", "url:{"+url+"}");
        return url;
        
    }

}
