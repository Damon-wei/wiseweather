package com.wisedeve.wiseweather.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.annotation.IntDef;

import com.wisedeve.wiseweather.gson.Weather;
import com.wisedeve.wiseweather.util.HttpUtil;
import com.wisedeve.wiseweather.util.Utility;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class AutoUpdateService extends Service {
    public AutoUpdateService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent,int flags, int startId) {
        updateWeather();
        AlarmManager manager = (AlarmManager) getSystemService(ALARM_SERVICE);
        int anHour = 3 * 60 * 60 * 1000;
        long triggerAtTime = SystemClock.elapsedRealtimeNanos() + anHour;
        Intent i = new Intent(this,AutoUpdateService.class);
        PendingIntent pendingIntent = PendingIntent.getService(this, 0, i, 0);
        manager.cancel(pendingIntent);
        manager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP,triggerAtTime,pendingIntent);
        return super.onStartCommand(intent, flags, startId);
    }

    /**
     * 更新天气信息
     */
    private void updateWeather() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String weatherStr = preferences.getString("weather", null);
        if (weatherStr != null) {
            Weather weather = Utility.handleWeatherResponse(weatherStr);
            String weatherId = weather.getHeWeather5().get(0).getBasic().getId();
            String weatherUrl = "https://free-api.heweather.com/v5/weather?city="+ weatherId +"&key=fb6a8c92b1db4c7abefb949a177b61fe";
            HttpUtil.sendOkHttpRequest(weatherUrl, new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {

                }
                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String responseText = response.body().string();
                    Weather weather1 = Utility.handleWeatherResponse(responseText);
                    if (weather1 != null && "ok".equals(weather1.getHeWeather5().get(0).getStatus())) {
                        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(AutoUpdateService.this).edit();
                        editor.putString("weather",responseText);
                        editor.apply();
                    }
                }
            });
        }


    }
}
