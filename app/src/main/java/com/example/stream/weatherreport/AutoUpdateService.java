package com.example.stream.weatherreport;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.os.SystemClock;
import android.preference.PreferenceManager;

import com.example.stream.weatherreport.gson.Weather;
import com.example.stream.weatherreport.util.WeatherHelper;
import com.example.stream.weatherreport.util.HttpUtil;

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
    public int onStartCommand(Intent intent, int flags, int startId) {
        updateWeather();
        updateBingPic();
        AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
        // 8小时自动更新一次
        int hour = 8 * 3600 * 1000;
        long triggerAtTime = SystemClock.elapsedRealtime() + hour;
        Intent newIntent = new  Intent(this, AutoUpdateService.class);
        PendingIntent pi = PendingIntent.getService(this, 0, newIntent, 0);
        am.cancel(pi);
        am.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAtTime, pi);

        return super.onStartCommand(intent, flags, startId);
    }

    private void updateWeather() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String weatherString = prefs.getString(WeatherActivity.WEATHER, null);
        if (weatherString != null) {
            Weather weather = WeatherHelper.onWeatherResponse(weatherString);
            String weatherId = weather.mBasic.weatherId;
            String weatherUrl = "https://free-api.heweather.com/v5/weather?city=" + weatherId +
                    "&key=" + "6cb14caab7ed4550ac62689c2bea387d";
            HttpUtil.sendOkHttpRequest(weatherUrl, new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    e.printStackTrace();
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String responseString = response.body().string();
                    Weather weather = WeatherHelper.onWeatherResponse(responseString);
                    if (weather != null && weather.mStatus.equals("ok")) {
                        SharedPreferences.Editor editor = PreferenceManager
                                .getDefaultSharedPreferences(AutoUpdateService.this)
                                .edit();
                        editor.putString(WeatherActivity.WEATHER, responseString);
                        editor.apply();
                    }
                }
            });
        }
    }

    private void updateBingPic() {
        String requestUrl = "http://guolin.tech/api/bing_pic";
        HttpUtil.sendOkHttpRequest(requestUrl, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String bingpicUrl = response.body().string();
                SharedPreferences.Editor editor = PreferenceManager
                        .getDefaultSharedPreferences(AutoUpdateService.this)
                        .edit();
                editor.putString(WeatherActivity.BING_PIC, bingpicUrl);
                editor.apply();
            }
        });
    }
}
