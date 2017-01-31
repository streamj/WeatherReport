package com.example.stream.weatherreport;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

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

    @BindView(R.id.weather_layout)
    ScrollView weatherLayout;

    @BindView(R.id.textview_city)
    TextView titleCity;

    @BindView(R.id.textview_update_time)
    TextView titleUpdateTime;

    @BindView(R.id.textview_degree)
    TextView degreeText;

    @BindView(R.id.textview_weather_info)
    TextView weatherInfoText;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather);

        ButterKnife.bind(this);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String weatherString = prefs.getString(WEATHER, null);
        if (weatherString != null) {
            Weather weather = Helper.onWeatherResponse(weatherString);
            showWeatherInfo(weather);
        } else {
            String weatherId = getIntent().getStringExtra(WEATHER_ID);
            weatherLayout.setVisibility(View.INVISIBLE);
            requestWeather(weatherId);
        }
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
                    }
                });
            }
        });
    }

    private void reportError() {
        Toast.makeText(WeatherActivity.this, "获取天气信息失败", Toast.LENGTH_SHORT).show();
    }

    private void showWeatherInfo(Weather weather) {
        String cityName = weather.mBasic.cityName;
        String updateTime = weather.mBasic.mUpdate.updateTime.split(" ")[1];
        String degree = weather.mNow.temperature;
        String weatherInfo = weather.mNow.mMore.info;
        titleCity.setText(cityName);
        titleUpdateTime.setText(updateTime);
        degreeText.setText(degree);
        weatherInfoText.setText(weatherInfo);
        forecastLayout.removeAllViews(); // what the fuck?
        for (DailyForcast dailyForcast : weather.mDailyForcasts) {
            View view = getLayoutInflater().inflate(R.layout.forecast_item, forecastLayout, false);
            TextView dateText = (TextView) view.findViewById(R.id.date_textview);
            TextView infoText = (TextView) view.findViewById(R.id.info_textview);
            TextView maxText = (TextView) view.findViewById(R.id.max_textview);
            TextView minText = (TextView) view.findViewById(R.id.min_textview);
            dateText.setText(dailyForcast.date);
            infoText.setText(dailyForcast.condition.textDay);
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
}
