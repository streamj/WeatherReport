package com.example.stream.weatherreport.gson;

import com.google.gson.annotations.SerializedName;

/**
 * Created by StReaM on 1/31/2017.
 */

public class Now {
    @SerializedName("cond")
    public More mMore;

    public String fl;

    public String hum;

    public String pcpn;

    public String pres;

    @SerializedName("tmp")
    public String temperature;

    public String vis;

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

    public class More {
        @SerializedName("code")
        public String code;

        @SerializedName("txt")
        public String info;

    }
}
