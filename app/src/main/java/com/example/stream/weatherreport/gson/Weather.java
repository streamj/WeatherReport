package com.example.stream.weatherreport.gson;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by StReaM on 1/31/2017.
 */

public class Weather {
    @SerializedName("status")
    public String mStatus;
    @SerializedName("basic")
    public Basic mBasic;
    @SerializedName("aqi")
    public AQI mAQI;
    @SerializedName("now")
    public Now mNow;
    @SerializedName("suggestion")
    public Suggestion mSuggestion;

    @SerializedName("daily_forecast")
    public List<DailyForcast> mDailyForcasts;

    @SerializedName("hourly_forecast")
    public List<HourlyForecast> mHourlyForecasts;
}
