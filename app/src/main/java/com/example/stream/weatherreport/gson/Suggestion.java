package com.example.stream.weatherreport.gson;

import com.google.gson.annotations.SerializedName;

/**
 * Created by StReaM on 1/31/2017.
 */

public class Suggestion {
    @SerializedName("air")
    public Air air;

    @SerializedName("comf")
    public Comfort mComfort;

    @SerializedName("cw")
    public CarWash mCarWash;

    @SerializedName("sport")
    public Sport mSport;

    @SerializedName("flu")
    public Flu flu;

    @SerializedName("trav")
    public Traval mTraval;

    @SerializedName("uv")
    public UV uv;

    public class Air {
        public String brf;
        public String txt;
    }

    public class Comfort {
        @SerializedName("brf")
        public String brf;
        @SerializedName("txt")
        public String info;
    }

    public class CarWash {
        @SerializedName("brf")
        public String brf;

        @SerializedName("txt")
        public String info;
    }

    public class Sport {
        @SerializedName("brf")
        public String brf;

        @SerializedName("txt")
        public String info;
    }

    public class Flu {
        @SerializedName("brf")
        public String brf;

        @SerializedName("txt")
        public String info;
    }

    public class Traval {
        @SerializedName("brf")
        public String brf;

        @SerializedName("txt")
        public String info;
    }

    public class UV {
        @SerializedName("brf")
        public String brf;

        @SerializedName("txt")
        public String info;
    }
}
