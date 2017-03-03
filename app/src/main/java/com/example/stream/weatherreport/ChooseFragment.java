package com.example.stream.weatherreport;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.stream.weatherreport.db.City;
import com.example.stream.weatherreport.db.County;
import com.example.stream.weatherreport.db.Province;
import com.example.stream.weatherreport.model.SortModel;
import com.example.stream.weatherreport.util.CharacterParser;
import com.example.stream.weatherreport.util.NewSortAdapter;
import com.example.stream.weatherreport.util.WeatherHelper;
import com.example.stream.weatherreport.util.HttpUtil;
import com.example.stream.weatherreport.util.PinyinComparator;

import org.litepal.crud.DataSupport;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * Created by StReaM on 1/31/2017.
 */

public class ChooseFragment extends Fragment implements NewSortAdapter.OnItemClickListener {
    public static final int LEVEL_PROV = 0;
    public static final int LEVEL_CITY = 1;
    public static final int LEVEL_COUNTY = 2;
    public static final String PROV_ARG = "province";
    public static final String CITY_ARG = "city";
    public static final String COUNTY_ARG = "county";
    public static final String defaultAddress = "http://guolin.tech/api/china";
    public static final String FAILURE_NOTE = "loading failed";

    private ProgressDialog mProgressDialog;
    @BindView(R.id.toolbar_text_view)
    TextView mTextView;
//    @BindView(R.id.choose_list_view)
//    ListView mListView;

    @BindView(R.id.choose_list_view)
    RecyclerView mRecyclerView;
    //    private ArrayAdapter<String> mAdapter;
    private List<String> dataList = new ArrayList<>();
    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.sidrbar)
    Sidebar sideBar;
    /**
     * 显示字母的TextView
     */
    @BindView(R.id.dialog)
    TextView dialog;

//    private SortAdapter adapter;
    private NewSortAdapter adapter;

    @BindView(R.id.filter_edit)
    EditText mClearEditText;

    /**
     * 汉字转换成拼音的类
     */
    private CharacterParser characterParser;
    private List<SortModel> mSourceDateList;
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

    private  ActionBar mActionBar;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.choose, container, false);
        ButterKnife.bind(this, view);

        initWidgets();
        setUpEvents();
        return view;
    }

    private void initWidgets() {
        setHasOptionsMenu(true);
        AppCompatActivity activity =  (AppCompatActivity) getActivity();
        activity.setSupportActionBar(toolbar);
        mActionBar = activity.getSupportActionBar();
        // 返回键导航的图标设置
        if (mActionBar != null) {
            // 让 toolbar title 消失
            mActionBar.setDisplayShowTitleEnabled(false);
            mActionBar.setHomeAsUpIndicator(R.drawable.ic_arrow_back_white_24dp);
            mActionBar.setDisplayHomeAsUpEnabled(false);
        }

        //实例化汉字转拼音类
        characterParser = CharacterParser.getInstance();
        pinyinComparator = new PinyinComparator();
        sideBar.setTextView(dialog);
        mSourceDateList = new ArrayList<>();
        adapter = new NewSortAdapter(getActivity(), mSourceDateList);
        adapter.setOnItemClickListener(this);
//        adapter = new SortAdapter(getActivity(), mSourceDateList);

//        mAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, dataList);
//        mListView.setAdapter(adapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRecyclerView.setAdapter(adapter);
    }

    private void setUpEvents(){
        //设置右侧触摸监听
        sideBar.setOnTouchingLetterChangedListener(new Sidebar.OnTouchingLetterChangedListener() {

            @Override
            public void onTouchingLetterChanged(String s) {
                //该字母首次出现的位置
                int position = adapter.getPositionForSection(s.charAt(0));
                if (position != -1) {
//                    mListView.setSelection(position);
                    // recyclerView 没有 setSelection, 这个等价
                    mRecyclerView.getLayoutManager().scrollToPosition(position);
                }

            }
        });
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
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

//        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
//                if (currentLevel == LEVEL_PROV) {
//                    String provName = ((SortModel) adapter.getItem(position)).getName();
//                    selectedProv = provinceMap.get(provName);
//                    queryCities();
//                } else if (currentLevel == LEVEL_CITY) {
//                    String cityName = ((SortModel) adapter.getItem(position)).getName();
//                    selectedCity = cityMap.get(cityName);
//                    queryCounties();
//                } else if (currentLevel == LEVEL_COUNTY) {
//                    String countyName = ((SortModel) adapter.getItem(position)).getName();
//                    String weatherId = countyMap.get(countyName).getWeatherId();
//                    // 如果没有缓存，是从这里进去的，那么当然 intent 会有东西了
//                    Intent intent = new Intent(getActivity(), WeatherActivity.class);
//                    intent.putExtra(WeatherActivity.WEATHER_ID, weatherId);
//                    startActivity(intent);
//                    getActivity().finish();
//                }
//            }
//        });
        queryProvinces();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            if (currentLevel == LEVEL_COUNTY) {
                queryCities();
            } else if (currentLevel == LEVEL_CITY) {
                queryProvinces();
            }
        }
        return super.onOptionsItemSelected(item);
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
        mTextView.setText("选择省份");
//        mButton.setVisibility(View.GONE);
        mActionBar.setDisplayHomeAsUpEnabled(false);
        mProvinceList = DataSupport.findAll(Province.class);
        if (mProvinceList.size() > 0) {
            dataList.clear();
            for (Province prov : mProvinceList) {
                String provName = prov.getProvinceName();
                dataList.add(provName);
                // 保存 String 和实体类的映射
                provinceMap.put(provName, prov);
            }

            mSourceDateList = filledData(dataList.toArray(new String[dataList.size()]));
            Log.d("DEBUG", "DATALIST:" + mSourceDateList);
            // 根据a-z进行排序源数据
            Collections.sort(mSourceDateList, pinyinComparator);
            adapter.updateListView(mSourceDateList);
            currentLevel = LEVEL_PROV;
        } else {
            queryFromServer(defaultAddress, PROV_ARG);
        }
    }

    private void queryCities() {
        mClearEditText.setText("");
        mTextView.setText(selectedProv.getProvinceName());
//        mButton.setVisibility(View.VISIBLE);
        mActionBar.setDisplayHomeAsUpEnabled(true);
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
            mSourceDateList = filledData(dataList.toArray(new String[dataList.size()]));
            // 根据a-z进行排序源数据
            Collections.sort(mSourceDateList, pinyinComparator);
            adapter.updateListView(mSourceDateList);
            currentLevel = LEVEL_CITY;
        } else {
            int provCode = selectedProv.getProvinceCode();
            queryFromServer(defaultAddress + "/" + provCode, CITY_ARG);
        }
    }

    private void queryCounties() {
        mClearEditText.setText("");
        mTextView.setText(selectedCity.getCityName());
        mActionBar.setDisplayHomeAsUpEnabled(true);
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
            mSourceDateList = filledData(dataList.toArray(new String[dataList.size()]));
            // 根据a-z进行排序源数据
            Collections.sort(mSourceDateList, pinyinComparator);
            adapter.updateListView(mSourceDateList);
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
                boolean result = false;
                if (type.equals(PROV_ARG)) {
                    result = WeatherHelper.onProvinceResponse(responseString);
                } else if (type.equals(CITY_ARG)) {
                    result = WeatherHelper.onCityResponse(responseString, selectedProv.getId());
                } else if (type.equals(COUNTY_ARG)) {
                    result = WeatherHelper.onCountyReponse(responseString, selectedCity.getId());
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
            filterDateList = mSourceDateList;
        } else {
            filterDateList.clear();
            for (SortModel sortModel : mSourceDateList) {
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

    @Override
    public void onChooserItemClick(int position) {
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
}
