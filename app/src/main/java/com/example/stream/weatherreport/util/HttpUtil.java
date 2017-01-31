package com.example.stream.weatherreport.util;

import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;

/**
 * Created by StReaM on 1/31/2017.
 */

public class HttpUtil {

    private static OkHttpClient client = new OkHttpClient();

    public static void sendOkHttpRequest(String address, Callback callback) {
        Request request = new Request.Builder().url(address).build();
        //  异步
        client.newCall(request).enqueue(callback);
    }
}
