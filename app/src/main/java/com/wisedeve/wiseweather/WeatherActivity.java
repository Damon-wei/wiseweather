package com.wisedeve.wiseweather;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.wisedeve.wiseweather.gson.Weather;
import com.wisedeve.wiseweather.service.AutoUpdateService;
import com.wisedeve.wiseweather.util.HttpUtil;
import com.wisedeve.wiseweather.util.Utility;

import java.io.IOException;
import java.text.SimpleDateFormat;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

import static android.media.CamcorderProfile.get;
import static com.wisedeve.wiseweather.R.layout.forecast;

public class WeatherActivity extends AppCompatActivity {

    public DrawerLayout drawerLayout;
    private ImageView navHome;
    public SwipeRefreshLayout swipeRefresh;
    private ImageView bingPicImg;
    private ScrollView weatherLayout;
    private TextView titleCityText;
    private TextView titleUpdateTime;
    private TextView degreeText;
    private TextView weatherInfoText;
    private LinearLayout forecastLayout;
    private TextView aqiText;
    private TextView pm25Text;
    private TextView comfortText;
    private TextView carWashText;
    private TextView sportText;
    private String nowTime;
    private String today;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= 21) {
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }
        setContentView(R.layout.activity_weather);

        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        navHome = (ImageView) findViewById(R.id.iv_home);
        swipeRefresh = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh);
        swipeRefresh.setColorSchemeResources(android.R.color.holo_purple, android.R.color.holo_blue_bright, android.R.color.holo_orange_light,
                android.R.color.holo_red_light);
        bingPicImg = (ImageView) findViewById(R.id.iv_bing_pic);
        weatherLayout = (ScrollView) findViewById(R.id.weather_layout);
        titleCityText = (TextView) findViewById(R.id.tv_title_city);
        titleUpdateTime = (TextView) findViewById(R.id.tv_title_update_time);
        degreeText = (TextView) findViewById(R.id.tv_degree);
        weatherInfoText = (TextView) findViewById(R.id.tv_weather_info);
        forecastLayout = (LinearLayout) findViewById(R.id.ll_forecast_layout);
        aqiText = (TextView) findViewById(R.id.tv_aqi);
        pm25Text = (TextView) findViewById(R.id.tv_pm25);
        comfortText = (TextView) findViewById(R.id.tv_comfort);
        carWashText = (TextView) findViewById(R.id.tv_car_wash);
        sportText = (TextView) findViewById(R.id.tv_sport);

        navHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String weatherStr = sharedPreferences.getString("weather", null);
        String weatherId;
        if (weatherStr != null) {
            Weather weather = Utility.handleWeatherResponse(weatherStr);
            weatherId = weather.getHeWeather5().get(0).getBasic().getId();
            showWeatherInfo(weather);
        }else {
            weatherId = getIntent().getStringExtra("weather_id");
            weatherLayout.setVisibility(View.INVISIBLE);
            requestWeather(weatherId);
        }
        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                requestWeather(weatherId);
            }
        });
        nowTime = new SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss").format(System.currentTimeMillis());
        today = nowTime.split(" ")[0];
        String bingPic = sharedPreferences.getString(today, null);
        if (bingPic != null) {
            Glide.with(this).load(bingPic).into(bingPicImg);
        }else {
            loadBingPic();
        }

    }

    /**
     * 加载Bing每日一图
     */
    private void loadBingPic() {
        String requestBingPic = "http://guolin.tech/api/bing_pic";
        HttpUtil.sendOkHttpRequest(requestBingPic, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String bingPic = response.body().string();
                SharedPreferences.Editor edit = PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
                String nowTime = new SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss").format(System.currentTimeMillis());
                String today = nowTime.split(" ")[0];
                edit.putString(today,bingPic);
                edit.apply();
                runOnUiThread(()->{
                    Glide.with(WeatherActivity.this).load(bingPic).into(bingPicImg);
                });
            }
        });
    }

    /**
     * 根据天气id获取天气信息
     * @param weatherId
     */
    public void requestWeather(String weatherId) {
        String weatherUrl = "https://free-api.heweather.com/v5/weather?city="+ weatherId +"&key=fb6a8c92b1db4c7abefb949a177b61fe";
        HttpUtil.sendOkHttpRequest(weatherUrl, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        swipeRefresh.setRefreshing(false);
                        Toast.makeText(WeatherActivity.this,"获取天气信息失败",Toast.LENGTH_SHORT).show();
                    }
                });
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseText = response.body().string();
                Weather weather = Utility.handleWeatherResponse(responseText);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (weather != null && "ok".equals(weather.getHeWeather5().get(0).getStatus())) {
                            SharedPreferences.Editor editor = PreferenceManager.
                                    getDefaultSharedPreferences(WeatherActivity.this)
                                    .edit();
                            editor.putString("weather",responseText);
                            editor.apply();
                            showWeatherInfo(weather);
                        }else {
                            Toast.makeText(WeatherActivity.this,"获取天气信息失败",Toast.LENGTH_SHORT).show();
                        }
                        swipeRefresh.setRefreshing(false);
                    }
                });
            }
        });
    }

    /**
     * 处理并展示Weather实体类中的数据
     * @param weather
     */
    private void showWeatherInfo(Weather weather) {
        Intent intent = new Intent(this, AutoUpdateService.class);
        startService(intent);
        String city = weather.getHeWeather5().get(0).getBasic().getCity();
        String updateTime = weather.getHeWeather5().get(0).getBasic().getUpdate().getLoc();
        String degree = weather.getHeWeather5().get(0).getNow().getTmp() + "℃";
        String weatherInfo = weather.getHeWeather5().get(0).getNow().getCond().getTxt();
        titleCityText.setText(city);
        titleUpdateTime.setText(updateTime.split(" ")[1]);
        degreeText.setText(degree);
        weatherInfoText.setText(weatherInfo);
        forecastLayout.removeAllViews();
        for (Weather.HeWeather5Bean.DailyForecastBean forecast: weather.getHeWeather5().get(0).getDaily_forecast()) {
            View view = LayoutInflater.from(this).inflate(R.layout.forecast_item, forecastLayout, false);
            TextView date = (TextView) view.findViewById(R.id.tv_date);
            TextView info = (TextView) view.findViewById(R.id.tv_info);
            TextView max = (TextView) view.findViewById(R.id.tv_max);
            TextView min = (TextView) view.findViewById(R.id.tv_min);
            date.setText(forecast.getDate());
            info.setText(forecast.getCond().getTxt_d());
            max.setText(forecast.getTmp().getMax());
            min.setText(forecast.getTmp().getMin());
            forecastLayout.addView(view);
        }
        aqiText.setText(weather.getHeWeather5().get(0).getAqi().getCity().getAqi());
        pm25Text.setText(weather.getHeWeather5().get(0).getAqi().getCity().getPm25());
        comfortText.setText("舒适度：" + weather.getHeWeather5().get(0).getSuggestion().getComf().getTxt());
        carWashText.setText("洗车指数：" + weather.getHeWeather5().get(0).getSuggestion().getCw().getTxt());
        sportText.setText("运动建议：" + weather.getHeWeather5().get(0).getSuggestion().getSport().getTxt());
        weatherLayout.setVisibility(View.VISIBLE);
    }
}
