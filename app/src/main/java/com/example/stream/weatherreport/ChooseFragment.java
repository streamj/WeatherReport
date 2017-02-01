package com.example.stream.weatherreport;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.stream.weatherreport.db.City;
import com.example.stream.weatherreport.db.County;
import com.example.stream.weatherreport.db.Province;
import com.example.stream.weatherreport.util.Helper;
import com.example.stream.weatherreport.util.HttpUtil;

import org.litepal.crud.DataSupport;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * Created by StReaM on 1/31/2017.
 */

public class ChooseFragment extends Fragment {
    public static final int LEVEL_PROV = 0;
    public static final int LEVEL_CITY = 1;
    public static final int LEVEL_COUNTY = 2;
    public static final String PROV_ARG = "province";
    public static final String CITY_ARG = "city";
    public static final String COUNTY_ARG = "county";
    public static final String defaultAddress = "http://guolin.tech/api/china";
    public static final String FAILURE_NOTE = "loading failed";

    private ProgressDialog mProgressDialog;
    private TextView mTextView;
    private Button mButton;
    private ListView mListView;
    private ArrayAdapter<String> mAdapter;
    private List<String> dataList = new ArrayList<>();

    private List<Province> mProvinceList;
    private List<City> mCityList;
    private List<County> mCounties;

    private Province selectedProv;
    private City selectedCity;
    private int currentLevel;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.choose, container, false);
        mTextView = (TextView) view.findViewById(R.id.choose_text_view);
        mButton = (Button) view.findViewById(R.id.choose_button);
        mListView = (ListView) view.findViewById(R.id.choose_list_view);
        mAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, dataList);
        mListView.setAdapter(mAdapter);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                if (currentLevel == LEVEL_PROV) {
                    selectedProv = mProvinceList.get(position);
                    queryCities();
                } else if (currentLevel == LEVEL_CITY) {
                    selectedCity = mCityList.get(position);
//                    Log.d("DEBUG", "GET HERE");
                    queryCounties();
                } else if (currentLevel == LEVEL_COUNTY) {
                    String weatherId = mCounties.get(position).getWeatherId();
                    // 如果没有缓存，是从这里进去的，那么当然 intent 会有东西了
                    Intent intent = new Intent(getActivity(), WeatherActivity.class);
                    intent.putExtra(WeatherActivity.WEATHER_ID, weatherId);
                    startActivity(intent);
                    getActivity().finish();
                }
            }
        });
        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (currentLevel == LEVEL_COUNTY) {
                    queryCities();
                } else if (currentLevel == LEVEL_CITY) {
                    queryProvinces();
                }
            }
        });
        queryProvinces();
    }

    private void queryProvinces() {
        mTextView.setText("中国");
        mButton.setVisibility(View.GONE);
        mProvinceList = DataSupport.findAll(Province.class);
        if (mProvinceList.size() > 0) {
            dataList.clear();
            for (Province prov: mProvinceList) {
                dataList.add(prov.getProvinceName());
            }
            mAdapter.notifyDataSetChanged();
            mListView.setSelection(0);
            currentLevel = LEVEL_PROV;
        } else {
            queryFromServer(defaultAddress, PROV_ARG);
        }
    }

    private void queryFromServer(String address, final String type) {
        showProgressDialog();
//        Log.d("DEBUG", address);
        HttpUtil.sendOkHttpRequest(address, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        closeProgressDialog();
                        Toast.makeText(getContext(), FAILURE_NOTE, Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseString = response.body().string();
//                Log.d("DEBBUG", responseString);
                boolean result = false;
                if (type.equals(PROV_ARG)) {
                    result = Helper.onProvinceResponse(responseString);
                } else if (type.equals(CITY_ARG)) {
                    result = Helper.onCityResponse(responseString, selectedProv.getId());
                } else if (type.equals(COUNTY_ARG)) {
                    result = Helper.onCountyReponse(responseString,selectedCity.getId());
                }
                if (result) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            closeProgressDialog();
                            if (type.equals(PROV_ARG)) {
                                queryProvinces();
                            } else if (type.equals(CITY_ARG)) {
                                queryCities();
                            } else if (type.equals(COUNTY_ARG)) {
                                queryCounties();
                            }
                        }
                    });
                }
            }
        });
    }

    private void closeProgressDialog() {
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
        }
    }

    private void showProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(getActivity());
            mProgressDialog.setMessage("loading...");
            mProgressDialog.setCanceledOnTouchOutside(false);
        }
        mProgressDialog.show();
    }

    private void queryCities() {
        mTextView.setText(selectedProv.getProvinceName());
        mButton.setVisibility(View.VISIBLE);
        mCityList = DataSupport
                .where("provinceid=? ", String.valueOf(selectedProv.getId()))
                .find(City.class);
        if (mCityList.size() > 0) {
            dataList.clear();
            for (City city : mCityList) {
                dataList.add(city.getCityName());
            }
            mAdapter.notifyDataSetChanged();
            mListView.setSelection(0);
            currentLevel = LEVEL_CITY;
        } else {
            int provCode = selectedProv.getProvinceCode();
            queryFromServer(defaultAddress + "/" +  provCode, CITY_ARG);
        }
    }

    private void queryCounties() {
        mTextView.setText(selectedCity.getCityName());
        mButton.setVisibility(View.VISIBLE);
        mCounties = DataSupport
                .where("cityid=? ", String.valueOf(selectedCity.getId()))
                .find(County.class);
        if (mCounties.size()  > 0 ) {
            dataList.clear();
            for (County county: mCounties) {
                dataList.add(county.getCountyName());
            }
            mAdapter.notifyDataSetChanged();
            mListView.setSelection(0);
            currentLevel = LEVEL_COUNTY;
        } else {
            int provCode = selectedProv.getProvinceCode();
            int cityCode = selectedCity.getCityCode();
            String addresss = defaultAddress + "/" + provCode + "/" + cityCode;
//            Log.d("DEBUG", addresss);
            queryFromServer(defaultAddress +  "/" + provCode + "/" + cityCode, COUNTY_ARG);
        }
    }

}
