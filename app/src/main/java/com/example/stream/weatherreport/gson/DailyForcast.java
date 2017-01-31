package com.example.stream.weatherreport.gson;

import com.google.gson.annotations.SerializedName;

/**
 * Created by StReaM on 1/31/2017.
 */

public class DailyForcast {
    @SerializedName("astro")
    public Astro astro;

    @SerializedName("cond")
    public WeatherCondition condition;

    @SerializedName("date")
    public String date;

    @SerializedName("hum")
    public String hum;

    @SerializedName("pcpn")
    public String pcpn;

    @SerializedName("pop")
    public String pop;

    @SerializedName("pres")
    public String pres;

    @SerializedName("tmp")
    public Temperature mTemperature;

    @SerializedName("uv")
    public String uv;

    @SerializedName("vis")
    public String vis;

    @SerializedName("wind")
    public Wind wind;


    public class Astro {
        @SerializedName("mr")
        public String mr;
        @SerializedName("ms")
        public String ms;
        @SerializedName("sr")
        public String sr;
        @SerializedName("ss")
        public String ss;
    }

    public class WeatherCondition {
        @SerializedName("code_d")
        public String CodeDay;
        @SerializedName("code_n")
        public String CodeNight;
        @SerializedName("txt_d")
        public String textDay;
        @SerializedName("txt_n")
        public String textNight;
    }


    public class Temperature {
        public String max;
        public String min;
    }

    public class Wind {
        public String deg;
        public String dir;
        public String sc;
        public String spd;
    }

}
