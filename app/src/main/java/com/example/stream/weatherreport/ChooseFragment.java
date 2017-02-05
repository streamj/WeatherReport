package com.example.stream.weatherreport;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.stream.weatherreport.db.City;
import com.example.stream.weatherreport.db.County;
import com.example.stream.weatherreport.db.Province;
import com.example.stream.weatherreport.model.SortModel;
import com.example.stream.weatherreport.util.CharacterParser;
import com.example.stream.weatherreport.util.Helper;
import com.example.stream.weatherreport.util.HttpUtil;
import com.example.stream.weatherreport.util.PinyinComparator;
import com.example.stream.weatherreport.util.SortAdapter;

import org.litepal.crud.DataSupport;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    //    private ArrayAdapter<String> mAdapter;
    private List<String> dataList = new ArrayList<>();

    private Sidebar sideBar;
    /**
     * 显示字母的TextView
     */
    private TextView dialog;
    private SortAdapter adapter;
    private EditText mClearEditText;

    /**
     * 汉字转换成拼音的类
     */
    private CharacterParser characterParser;
    private List<SortModel> SourceDateList;
    /**
     * 根据拼音来排列ListView里面的数据类
     */
    private PinyinComparator pinyinComparator;

    private List<Province> mProvinceList;

    private List<City> mCityList;
    private List<County> mCounties;
    // 因为排序之后，顺序和原先实体类 list 不同，而从 adapter 里面根据点击 position 返回的 又是 String
    // 所以用 Map 来保存是实体类和 String 对应关系
    private Map<String, City> cityMap = new HashMap<>();
    private Map<String, Province> provinceMap = new HashMap<>();
    private Map<String, County> countyMap = new HashMap<>();

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
        //实例化汉字转拼音类
        characterParser = CharacterParser.getInstance();

        pinyinComparator = new PinyinComparator();

        sideBar = (Sidebar) view.findViewById(R.id.sidrbar);
        dialog = (TextView) view.findViewById(R.id.dialog);
        sideBar.setTextView(dialog);

        //设置右侧触摸监听
        sideBar.setOnTouchingLetterChangedListener(new Sidebar.OnTouchingLetterChangedListener() {

            @Override
            public void onTouchingLetterChanged(String s) {
                //该字母首次出现的位置
                int position = adapter.getPositionForSection(s.charAt(0));
                if (position != -1) {
                    mListView.setSelection(position);
                }

            }
        });
        SourceDateList = new ArrayList<>();
        adapter = new SortAdapter(getActivity(), SourceDateList);
//        mAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, dataList);
        mListView.setAdapter(adapter);
        mClearEditText = (EditText) view.findViewById(R.id.filter_edit);

        //根据输入框输入值的改变来过滤搜索
        mClearEditText.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //当输入框里面的值为空，更新为原来的列表，否则为过滤数据列表
                filterData(s.toString());
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {

            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                if (currentLevel == LEVEL_PROV) {
                    String provName = ((SortModel) adapter.getItem(position)).getName();
                    selectedProv = provinceMap.get(provName);
                    queryCities();
                } else if (currentLevel == LEVEL_CITY) {
                    String cityName = ((SortModel) adapter.getItem(position)).getName();
                    selectedCity = cityMap.get(cityName);
                    queryCounties();
                } else if (currentLevel == LEVEL_COUNTY) {
                    String countyName = ((SortModel) adapter.getItem(position)).getName();
                    String weatherId = countyMap.get(countyName).getWeatherId();
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

    private void queryProvinces() {
        mTextView.setText("中国");
        mButton.setVisibility(View.GONE);
        mProvinceList = DataSupport.findAll(Province.class);
        if (mProvinceList.size() > 0) {
            dataList.clear();
            for (Province prov : mProvinceList) {
                String provName = prov.getProvinceName();
                dataList.add(provName);
                // 保存 String 和实体类的映射
                provinceMap.put(provName, prov);
            }

            SourceDateList = filledData(dataList.toArray(new String[dataList.size()]));
            // 根据a-z进行排序源数据
            Collections.sort(SourceDateList, pinyinComparator);
            adapter.updateListView(SourceDateList);
            currentLevel = LEVEL_PROV;
        } else {
            queryFromServer(defaultAddress, PROV_ARG);
        }
    }

    private void queryCities() {
        mClearEditText.setText("");
        mTextView.setText(selectedProv.getProvinceName());
        mButton.setVisibility(View.VISIBLE);
        synchronized (City.class) {
            mCityList = DataSupport
                    .where("provinceid=? ", String.valueOf(selectedProv.getId()))
                    .find(City.class);
        }
        if (mCityList.size() > 0) {
            dataList.clear();
            for (City city : mCityList) {
                String cityName = city.getCityName();
                dataList.add(cityName);
                // 保存 String 和实体类的映射
                cityMap.put(cityName, city);
            }
            SourceDateList = filledData(dataList.toArray(new String[dataList.size()]));
            // 根据a-z进行排序源数据
            Collections.sort(SourceDateList, pinyinComparator);
            adapter.updateListView(SourceDateList);
            currentLevel = LEVEL_CITY;
        } else {
            int provCode = selectedProv.getProvinceCode();
            queryFromServer(defaultAddress + "/" + provCode, CITY_ARG);
        }
    }

    private void queryCounties() {
        mClearEditText.setText("");
        mTextView.setText(selectedCity.getCityName());
        mButton.setVisibility(View.VISIBLE);
        synchronized (County.class) {
            mCounties = DataSupport
                    .where("cityid=? ", String.valueOf(selectedCity.getId()))
                    .find(County.class);
        }
        if (mCounties.size() > 0) {
            dataList.clear();
            for (County county : mCounties) {
                String countyName = county.getCountyName();
                dataList.add(countyName);
                // 保存 String 和实体类的映射
                countyMap.put(countyName, county);
            }
            SourceDateList = filledData(dataList.toArray(new String[dataList.size()]));
            // 根据a-z进行排序源数据
            Collections.sort(SourceDateList, pinyinComparator);
            adapter.updateListView(SourceDateList);
            currentLevel = LEVEL_COUNTY;
        } else {
            int provCode = selectedProv.getProvinceCode();
            int cityCode = selectedCity.getCityCode();
            String addresss = defaultAddress + "/" + provCode + "/" + cityCode;
//            Log.d("DEBUG", addresss);
            queryFromServer(defaultAddress + "/" + provCode + "/" + cityCode, COUNTY_ARG);
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
                    result = Helper.onCountyReponse(responseString, selectedCity.getId());
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

    /**
     * 为ListView填充数据
     *
     * @param date
     * @return
     */
    private List<SortModel> filledData(String[] date) {
        List<SortModel> mSortList = new ArrayList<SortModel>();

        for (int i = 0; i < date.length; i++) {
            SortModel sortModel = new SortModel();
//            Log.d("DEBUG", date[i]);
            sortModel.setName(date[i]);
            //汉字转换成拼音
            String pinyin = characterParser.getSelling(date[i]);
            String sortString = pinyin.substring(0, 1).toUpperCase();

            // 正则表达式，判断首字母是否是英文字母
            if (sortString.matches("[A-Z]")) {
                sortModel.setSortLetters(sortString.toUpperCase());
            } else {
                sortModel.setSortLetters("#");
            }

            mSortList.add(sortModel);
        }
//        Log.d("DEBUG", mSortList.toString());
        return mSortList;
    }

    /**
     * 根据输入框中的值来过滤数据并更新ListView
     *
     * @param filterStr
     */
    private void filterData(String filterStr) {
        List<SortModel> filterDateList = new ArrayList<SortModel>();

        if (TextUtils.isEmpty(filterStr)) {
            filterDateList = SourceDateList;
        } else {
            filterDateList.clear();
            for (SortModel sortModel : SourceDateList) {
                String name = sortModel.getName();
                if (name.toUpperCase().contains(filterStr.toUpperCase())
                        || characterParser.getSelling(name).toUpperCase()
                        .startsWith(filterStr.toUpperCase())) {
                    filterDateList.add(sortModel);
                }
            }
        }
        // 根据a-z进行排序
        Collections.sort(filterDateList, pinyinComparator);
        adapter.updateListView(filterDateList);
    }
}
