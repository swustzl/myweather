package com.myweather.app.util;

public interface HttpResponseListener {
    void onFinish(String response);
    void onError(Exception e);

}
