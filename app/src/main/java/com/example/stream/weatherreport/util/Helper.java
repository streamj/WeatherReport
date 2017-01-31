package com.example.stream.weatherreport.util;

import android.text.TextUtils;
import android.util.Log;

import com.example.stream.weatherreport.db.City;
import com.example.stream.weatherreport.db.County;
import com.example.stream.weatherreport.db.Province;
import com.example.stream.weatherreport.gson.Weather;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by StReaM on 1/31/2017.
 */

public class Helper {
    public static boolean onProvinceResponse(String response) {
        if (!TextUtils.isEmpty(response)) {
            try {
                JSONArray allProvinces = new JSONArray(response);
                for(int i = 0; i < allProvinces.length(); i++) {
                    JSONObject jsonObjectProv = allProvinces.getJSONObject(i);
                    Province province = new Province();
                    province.setProvinceCode(jsonObjectProv.getInt("id"));
                    province.setProvinceName(jsonObjectProv.getString("name"));
                    province.save();
                }
                return true;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public static boolean onCityResponse(String response, int provinceId) {
        if (!TextUtils.isEmpty(response)) {
            try {
                JSONArray allCities = new JSONArray(response);
                for (int i = 0; i < allCities.length(); i++) {
                    JSONObject jsonObjectCity = allCities.getJSONObject(i);
                    City city = new City();
                    city.setCityCode(jsonObjectCity.getInt("id"));
                    city.setCityName(jsonObjectCity.getString("name"));
                    city.setProvinceId(provinceId);
                    city.save();
                }
                return true;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public static boolean onCountyReponse(String response, int cityId) {
        if (!TextUtils.isEmpty(response)) {
            Log.d("DEBUG", response);
            try {
                JSONArray allCounties = new JSONArray(response);
                for (int i = 0; i < allCounties.length(); i++) {
                    JSONObject jsonObjectCounty = allCounties.getJSONObject(i);
                    County county = new County();
                    county.setWeatherId(jsonObjectCounty.getString("weather_id"));
                    county.setCountyName(jsonObjectCounty.getString("name"));
                    county.setCityId(cityId);
                    county.save();
                }
                return true;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public static Weather onWeatherResponse(String response) {
        try {
            JSONObject jsonObject = new JSONObject(response);
            JSONArray jsonArray = jsonObject.getJSONArray("HeWeather5");
            String weatherContent = jsonArray.getJSONObject(0).toString();
            Log.d("DEBUG: ==>", weatherContent);
            return new Gson().fromJson(weatherContent, Weather.class);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

}
