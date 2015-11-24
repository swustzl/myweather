package com.swustzl.myweather.util;

public interface HttpResponseListener {
    void onFinish(String response);
    void onError(Exception e);

}
