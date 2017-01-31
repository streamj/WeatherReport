package com.example.stream.weatherreport.gson;

import com.google.gson.annotations.SerializedName;

/**
 * Created by StReaM on 1/31/2017.
 */

public class Basic {
    @SerializedName("city")
    public String cityName;

    @SerializedName("cnty")
    public String country;

    @SerializedName("id")
    public String weatherId;

    @SerializedName("lat")
    public String latitude;

    @SerializedName("lon")
    public String longitude;

    @SerializedName("update")
    public Update mUpdate;

    public class Update {
        @SerializedName("loc")
        public String updateTime;

        @SerializedName("utc")
        public String updateTimeUTC;
    }
}
