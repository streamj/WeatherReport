package com.example.stream.weatherreport;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.stream.weatherreport.gson.DailyForcast;
import com.example.stream.weatherreport.gson.Weather;
import com.example.stream.weatherreport.util.Helper;
import com.example.stream.weatherreport.util.HttpUtil;

import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class WeatherActivity extends AppCompatActivity {
    public static final String WEATHER = "weather";
    public static final String WEATHER_ID = "weather_id";
    public static final String BING_PIC = "bing_pic";

    @BindView(R.id.weather_layout)
    ScrollView weatherLayout;

    @BindView(R.id.textview_city)
    TextView titleCity;

    @BindView(R.id.textview_update_time)
    TextView titleUpdateTime;

    @BindView(R.id.now_high_textview)
    TextView nowMaxDegreeText;

    @BindView(R.id.now_low_textview)
    TextView nowLowDegreeText;

    @BindView(R.id.now_forecast_textview)
    TextView nowWeatherInfo;

    @BindView(R.id.now_item_icon)
    ImageView nowWeatherImageIcon;

    @BindView(R.id.now_item_date_textview)
    TextView nowDateText;

    @BindView(R.id.aqi_textview)
    TextView aqiText;

    @BindView(R.id.pm25_textview)
    TextView pm25Text;

    @BindView(R.id.textview_comfort)
    TextView comfortText;

    @BindView(R.id.textview_carwash)
    TextView carWashText;

    @BindView(R.id.textview_sport)
    TextView sportText;

    @BindView(R.id.forecast_layout)
    LinearLayout forecastLayout;

    @BindView(R.id.bing_pic)
    ImageView bingImage;

    @BindView(R.id.swipe_refresh)
    SwipeRefreshLayout mSwipeRefresh;

    @BindView(R.id.nav_button)
    Button navButton;

    @BindView(R.id.draw_layout)
    public DrawerLayout mDrawLayout;

    private static long lastUpdateTime = 0;
    private static boolean firstRequest = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather);

        ButterKnife.bind(this);

        mSwipeRefresh.setColorSchemeResources(R.color.colorPrimary);
        navButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mDrawLayout.openDrawer(GravityCompat.START);
            }
        });

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String weatherString = prefs.getString(WEATHER, null);
        final String weatherId;
        if (weatherString != null) {
            Weather weather = Helper.onWeatherResponse(weatherString);
            weatherId = weather.mBasic.weatherId;
            showWeatherInfo(weather);
        } else {
            weatherId = getIntent().getStringExtra(WEATHER_ID);
            weatherLayout.setVisibility(View.INVISIBLE);
            requestWeather(weatherId);
            if (firstRequest) {
                startActivity(new Intent(this, AutoUpdateService.class));
            }
        }
        mSwipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                long currentTime = System.currentTimeMillis();
                if (lastUpdateTime > 0 && currentTime - lastUpdateTime > 1000 * 1800) {
                    requestWeather(weatherId);
                    lastUpdateTime = currentTime;
                } else if (lastUpdateTime == 0) {
                    requestWeather(weatherId);
                    lastUpdateTime = currentTime;
                } else {
                    Toast.makeText(WeatherActivity.this,
                            "更新过于频繁，请稍后再更新", Toast.LENGTH_SHORT).show();
                    mSwipeRefresh.setRefreshing(false);
                }
            }
        });

        String bingpic = prefs.getString(BING_PIC, null);
        if (bingpic != null) {
            Glide.with(this).load(bingpic).into(bingImage);
        } else {
            loadBingPic();
        }
    }

    private void loadBingPic() {
        String requestUrl = "http://guolin.tech/api/bing_pic";
        HttpUtil.sendOkHttpRequest(requestUrl, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                // the responseString is a bing pic url
                final String responseString = response.body().string();
                SharedPreferences.Editor editor = PreferenceManager
                        .getDefaultSharedPreferences(WeatherActivity.this).edit();
                editor.putString(BING_PIC, responseString);
                editor.apply();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Glide.with(WeatherActivity.this).load(responseString).into(bingImage);
                    }
                });
            }
        });
    }

    private void requestWeather(String weatherId) {
        String weatherUrl = "https://free-api.heweather.com/v5/weather?city=" + weatherId +
                "&key=" + "6cb14caab7ed4550ac62689c2bea387d";
        Log.d("DEBUG", weatherUrl);
        HttpUtil.sendOkHttpRequest(weatherUrl, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        reportError();
                        // 刷新结束，隐藏刷新进度条
                        mSwipeRefresh.setRefreshing(false);
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String responseString = response.body().string();
                Log.d("DEBUG", responseString);
                final Weather weather = Helper.onWeatherResponse(responseString);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (weather != null && (weather.mStatus).equals("ok")) {
                            SharedPreferences.Editor editor = PreferenceManager
                                    .getDefaultSharedPreferences(WeatherActivity.this)
                                    .edit();
                            editor.putString(WEATHER, responseString);
                            editor.apply();
                            showWeatherInfo(weather);
                        } else {
                            reportError();
                        }
                        mSwipeRefresh.setRefreshing(false);
                    }
                });
            }
        });
        loadBingPic();
    }

    private void reportError() {
        Toast.makeText(WeatherActivity.this, "获取天气信息失败", Toast.LENGTH_SHORT).show();
    }

    private void showWeatherInfo(Weather weather) {
        String cityName = weather.mBasic.cityName;
        String updateTime = weather.mBasic.mUpdate.updateTime.split(" ")[1];
        String high = weather.mDailyForcasts.get(0).mTemperature.max;
        String low = weather.mDailyForcasts.get(0).mTemperature.min;
        String weatherInfo = weather.mNow.mMore.info;
        String nowTime = weather.mBasic.mUpdate.updateTime.split(" ")[0];
        titleCity.setText(cityName);
        titleUpdateTime.setText("最后更新：" + updateTime);
        nowMaxDegreeText.setText(high + "°");
        nowLowDegreeText.setText(low + "°");
        nowDateText.setText(nowTime);
        nowWeatherInfo.setText(weatherInfo);
        nowWeatherImageIcon.setImageResource(getWeatherImage(weatherInfo, true));
        forecastLayout.removeAllViews(); // what the fuck?
        for (DailyForcast dailyForcast : weather.mDailyForcasts) {
            View view = getLayoutInflater().inflate(R.layout.forecast_item, forecastLayout, false);
            TextView dateText = (TextView) view.findViewById(R.id.date_textview);
            TextView infoText = (TextView) view.findViewById(R.id.info_textview);
            TextView maxText = (TextView) view.findViewById(R.id.max_textview);
            TextView minText = (TextView) view.findViewById(R.id.min_textview);
            ImageView forecastImage = (ImageView) view.findViewById(R.id.forecast_imageview);

            String date2 = dailyForcast.date.split("-")[2];
            String date1 = dailyForcast.date.split("-")[1];

            dateText.setText(date1 + "-" + date2);

            String weatherCondition = dailyForcast.condition.textDay;
            infoText.setText(weatherCondition);
            int imageResource =  getWeatherImage(weatherCondition, false);
            forecastImage.setImageResource(imageResource);
            maxText.setText(dailyForcast.mTemperature.max);
            minText.setText(dailyForcast.mTemperature.min);

            forecastLayout.addView(view);
        }
        if (weather.mAQI != null) {
            aqiText.setText(weather.mAQI.mAQICity.aqi);
            pm25Text.setText(weather.mAQI.mAQICity.pm25);
        }
        String comfort = "舒适度: " + weather.mSuggestion.mComfort.info;
        String washCar = "洗车指数: " + weather.mSuggestion.mCarWash.info;
        String sport = "运动建议: " + weather.mSuggestion.mSport.info;
        comfortText.setText(comfort);
        carWashText.setText(washCar);
        sportText.setText(sport);
        weatherLayout.setVisibility(View.VISIBLE);
    }

    private int getWeatherImage(String weatherCondition, boolean ifNow) {
        if (!ifNow) {
            if (weatherCondition.equals("小雨")) {
                return R.drawable.ic_light_rain;
            } else if (weatherCondition.equals("多云")) {
                return R.drawable.ic_light_clouds;
            } else if (weatherCondition.equals("阴")) {
                return R.drawable.ic_cloudy;
            } else if (weatherCondition.equals("晴")) {
                return R.drawable.ic_clear;
            } else if (weatherCondition.equals("雾")) {
                return R.drawable.ic_fog;
            } else if (weatherCondition.contains("雪")) {
                return R.drawable.ic_snow;
            } else if (weatherCondition.equals("雨")) {
                return R.drawable.ic_rain;
            } else if (weatherCondition.contains("风")) {
                return R.drawable.ic_storm;
            } else {
                return R.drawable.unknown;
            }
        } else {
            if (weatherCondition.equals("小雨")) {
                return R.drawable.art_light_rain;
            } else if (weatherCondition.equals("多云")) {
                return R.drawable.art_light_clouds;
            } else if (weatherCondition.equals("阴")) {
                return R.drawable.art_clouds;
            } else if (weatherCondition.equals("晴")) {
                return R.drawable.art_clear;
            } else if (weatherCondition.equals("雾")) {
                return R.drawable.art_fog;
            } else if (weatherCondition.contains("雪")) {
                return R.drawable.art_snow;
            } else if (weatherCondition.equals("雨")) {
                return R.drawable.art_rain;
            } else if (weatherCondition.contains("风")) {
                return R.drawable.art_storm;
            } else {
                return R.drawable.unknown;
            }
        }
    }
}
