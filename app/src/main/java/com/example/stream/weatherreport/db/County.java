package com.example.stream.weatherreport.db;

import org.litepal.crud.DataSupport;

/**
 * Created by StReaM on 1/31/2017.
 */

public class County extends DataSupport {
    private int id;
    private String countyName;
    private String weather;
    private int cityId;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCountyName() {
        return countyName;
    }

    public void setCountyName(String countyName) {
        this.countyName = countyName;
    }

    public String getWeather() {
        return weather;
    }

    public void setWeather(String weather) {
        this.weather = weather;
    }

    public int getCityId() {
        return cityId;
    }

    public void setCityId(int cityId) {
        this.cityId = cityId;
    }
}
