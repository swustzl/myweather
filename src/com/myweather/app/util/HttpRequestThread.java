package com.myweather.app.util;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class HttpRequestThread extends Thread{
    
    private String address;
    private HttpResponseListener listener;
    public HttpRequestThread(String address,HttpResponseListener listener){
        this.address = address;
        this.listener = listener;
    }
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
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            StringBuilder response = new StringBuilder();
            String line = reader.readLine();
            while(line != null){
                response.append(line);
                line = reader.readLine();
            }
            if(listener != null){
                listener.onFinish(response.toString());
            }
        }catch(Exception e){
            if(listener != null){
                listener.onError(e);
            }
        } finally {
            if(connection != null){
                connection.disconnect();
            }
        }     
    }

}
