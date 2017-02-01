package com.example.stream.weatherreport;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
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

    @BindView(R.id.aqi_quality)
    TextView aqiQuality;

    @BindView(R.id.aqi_textview)
    TextView aqiText;

    @BindView(R.id.pm25_textview)
    TextView pm25Text;

    @BindView(R.id.aqi_suggestion_text)
    TextView aqiSuggestion;

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


    @BindView(R.id.nav_view)
    NavigationView navigation;

    private static long lastUpdateTime = 0;
    private static boolean firstRequest = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather);

        ButterKnife.bind(this);

        setUpNavButton();
        setUpNavigationView();

        // 事先设定的 weatherId
        String selectedWeatherId = getIntent().getStringExtra(WEATHER_ID);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        // 缓存在设置里的 weatherJSON
        String weatherString = prefs.getString(WEATHER, null);


        final String weatherId;

        // 缓存过 JSON
        if (weatherString != null) {
            // 先用缓存的 json 获取 weatherId
//            Weather weather = Helper.onWeatherResponse(weatherString);
//            weatherId = weather.mBasic.weatherId;

            // 从 ChooseActivity 重新选择城市之后进的，有 intent 值
            if (selectedWeatherId != null) {
                weatherLayout.setVisibility(View.INVISIBLE);
                weatherId = selectedWeatherId;
                requestWeather(weatherId);
            } else {
                // 从 mainActivity 进的, 没有 intent 值,说明没重新选择城市，那么直接用缓存信息显示
                Weather weather = Helper.onWeatherResponse(weatherString);
                weatherId = weather.mBasic.weatherId;
                showWeatherInfo(weather);
            }

        } else {
            // 没有缓存过，说明是第一次
            weatherId = selectedWeatherId;
            weatherLayout.setVisibility(View.INVISIBLE);
            requestWeather(weatherId);
            //todo 把 service 搞成单例
//            if (firstRequest) {
//                startActivity(new Intent(this, AutoUpdateService.class));
//            }
        }
        setUpSwipeRefresh(weatherId);


        String bingpic = prefs.getString(BING_PIC, null);
        if (bingpic != null) {
            Glide.with(this).load(bingpic).into(bingImage);
        } else {
            loadBingPic();
        }
    }

    private void setUpNavigationView() {
        navigation.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            //用于辨别此前是否已有选中条目
            MenuItem preMenuItem;

            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                //首先将选中条目变为选中状态 即checked为true,后关闭Drawer，以前选中的Item需要变为未选中状态
                if (preMenuItem != null) {
                    preMenuItem.setChecked(false);
                }
                menuItem.setChecked(true);
                mDrawLayout.closeDrawers();
                preMenuItem = menuItem;
                switch (menuItem.getItemId()) {
                    case R.id.navigation_sub_item1:
                        startActivity(new Intent(WeatherActivity.this, ChooseActivity.class));
                        finish();
                        break;
                    case R.id.navigation_sub_item2:
                        Toast.makeText(WeatherActivity.this, "Unimplemented", Toast.LENGTH_SHORT).show();
                        break;

                }
                return true;
            }
        });
    }

    private void setUpNavButton() {
        navButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mDrawLayout.openDrawer(GravityCompat.START);
            }
        });
    }

    private void setUpSwipeRefresh(final String weatherId) {
        mSwipeRefresh.setColorSchemeResources(R.color.colorPrimary);
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
//        Log.d("DEBUG", weatherUrl);
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
//                Log.d("DEBUG", responseString);
                final Weather weather = Helper.onWeatherResponse(responseString);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (weather != null && (weather.mStatus).equals("ok")) {
                            // 请求成功，缓存并显示
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
            int imageResource = getWeatherImage(weatherCondition, false);
            forecastImage.setImageResource(imageResource);
            maxText.setText(dailyForcast.mTemperature.max);
            minText.setText(dailyForcast.mTemperature.min);

            forecastLayout.addView(view);
        }
        if (weather.mAQI != null) {
            aqiQuality.setText(weather.mAQI.mAQICity.qlty);
            aqiText.setText(weather.mAQI.mAQICity.aqi);
            pm25Text.setText(weather.mAQI.mAQICity.pm25);
            String aqisu = weather.mSuggestion.air.txt;
            aqiSuggestion.setText(aqisu);

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
