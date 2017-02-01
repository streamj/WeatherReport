package com.example.stream.weatherreport;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        if (prefs.getString(WeatherActivity.WEATHER, null) != null) {
//            Log.d("DEBUG OF DEBUG", prefs.getString(WeatherActivity.WEATHER, null));
            // 之后只要缓存过，都是从这里进入 Activity 的，那么 intent 当然没有东西了
            Intent intent = new Intent(this, WeatherActivity.class);
            startActivity(intent);
            finish();
        }
//        Log.d("DEBUG", "CAN YOU SEE ME");
    }

}
