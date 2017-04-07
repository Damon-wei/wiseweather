package com.wisedeve.wiseweather.util;

import okhttp3.OkHttpClient;
import okhttp3.Request;

/**
 * Created by Damon on 2017/4/7.
 * Description :
 */

public class HttpUtil {
    public static void sendOkHttpRequest(String adress,okhttp3.Callback callback){
        OkHttpClient okHttpClient = new OkHttpClient();
        Request request = new Request.Builder().url(adress).build();
        okHttpClient.newCall(request).enqueue(callback);
    }
}
