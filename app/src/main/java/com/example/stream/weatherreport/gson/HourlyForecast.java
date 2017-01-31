package com.example.stream.weatherreport.gson;

import com.google.gson.annotations.SerializedName;

/**
 * Created by StReaM on 2/1/2017.
 */

public class HourlyForecast {
    @SerializedName("cond")
    public WeatherCondition condition;

    public class WeatherCondition {
        @SerializedName("code")
        public String Code;
        @SerializedName("txt")
        public String text;
    }
    @SerializedName("date")
    public String date;

    public String hum;
    public String pop;
    public String pres;
    public String tmp;

    @SerializedName("wind")
    public Wind wind;

    public class Wind {
        @SerializedName("deg")
        public String degree;

        @SerializedName("dir")
        public String direction;

        @SerializedName("sc")
        public String strength;
        @SerializedName("spd")
        public String speed;
    }

}
