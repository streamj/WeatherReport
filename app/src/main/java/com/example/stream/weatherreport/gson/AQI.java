package com.example.stream.weatherreport.gson;

import com.google.gson.annotations.SerializedName;

/**
 * Created by StReaM on 1/31/2017.
 */

public class AQI {
    @SerializedName("city")
    public AQICity mAQICity;

    public class AQICity {
        public String aqi;
        public String pm10;
        public String pm25;
        public String qlty;
    }
}
