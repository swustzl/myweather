package com.swustzl.myweather.activity;

import java.util.ArrayList;
import java.util.List;

import com.swustzl.myweather.R;
import com.swustzl.myweather.database.WeatherDataBase;
import com.swustzl.myweather.model.City;
import com.swustzl.myweather.model.County;
import com.swustzl.myweather.model.Province;
import com.swustzl.myweather.util.HttpResponseListener;
import com.swustzl.myweather.util.HttpUtil;
import com.swustzl.myweather.util.LogUtil;
import com.swustzl.myweather.util.ParserUtil;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class ChooseAreaActivity extends Activity {
    /**
     * 显示省所代表的级别
     */
    private static final int LEVEL_PROVINCE = 0;
    /**
     * 显示市所代表的级别
     */
    private static final int LEVEL_CITY = 1;
    /**
     * 显示县所代表的级别
     */
    private static final int LEVEL_COUNTY = 2;
    /**
     * 当前显示的省市县级别
     */
    private int currentLevel;

    private TextView title;
    private ListView areaList;
    private ArrayAdapter<String> adapter;
    /**
     * ListView中需要显示的数据list
     */
    private List<String> dataList = new ArrayList<String>();
    private WeatherDataBase weatherDataBase;

    private List<Province> provinceList;
    private List<City> cityList;
    private List<County> countyList;
    /**
     * ListView中所选中的省
     */
    private Province province;
    /**
     * ListView中所选中的市
     */
    private City city;
    /**
     * ListView中所选中的县
     */
    private County county;

    private ProgressDialog progressDialog;

    private Boolean isFromWeatherActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        isFromWeatherActivity = getIntent().getBooleanExtra("from_weather_activity", false);
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        if (pref.getBoolean("city_selected", false) && !isFromWeatherActivity) {
            Intent intent = new Intent(this, ShowWeatherActivity.class);
            startActivity(intent);
            finish();
            return;
        }
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.choose_area);
        weatherDataBase = WeatherDataBase.getInstance(this);
        title = (TextView) findViewById(R.id.title);
        areaList = (ListView) findViewById(R.id.area_list);
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, dataList);
        areaList.setAdapter(adapter);
        areaList.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (currentLevel == LEVEL_PROVINCE) {
                    province = provinceList.get(position);
                    selectCity();
                } else if (currentLevel == LEVEL_CITY) {
                    city = cityList.get(position);
                    selectCounty();
                } else if (currentLevel == LEVEL_COUNTY) {
                    county = countyList.get(position);
                    selectWeatherCode();
                }
            }
        });
        selectProvince();
    }

    /**
     * 查询天气码，并启动展示天气活动，先从数据库中查找，找不到时，加载远程服务器中数据
     */
    protected void selectWeatherCode() {
        String weatherCode = weatherDataBase.findWeatherCodeByCountyId(county.getId());
        if (!TextUtils.isEmpty(weatherCode)) {
            Intent intent = new Intent(ChooseAreaActivity.this, ShowWeatherActivity.class);
            intent.putExtra("weatherCode", weatherCode);
            startActivity(intent);
            finish();
        } else {
            loadFromServer(county.getCountyCode(), "weatherCode");
        }
    }

    /**
     * 查询省级数据，先从数据库中查找，找不到时，加载远程服务器中数据
     */
    private void selectProvince() {
        provinceList = weatherDataBase.findProvince();
        if (provinceList != null && provinceList.size() > 0) {
            dataList.clear();
            for (int i = 0; i < provinceList.size(); i++) {
                dataList.add(provinceList.get(i).getProvinceName());
            }
            adapter.notifyDataSetChanged();
            areaList.setSelection(0);
            title.setText("中国");
            currentLevel = LEVEL_PROVINCE;
        } else {
            if (provinceList.size() == 0) {
                LogUtil.i("provinceList", "size==0");
            }
            loadFromServer(null, "province");
        }
    }

    /**
     * 查询市级数据，先从数据库中查找，找不到时，加载远程服务器中数据
     */
    private void selectCity() {
        cityList = weatherDataBase.findCity(province.getId());
        if (cityList != null && cityList.size() > 0) {
            dataList.clear();
            for (int i = 0; i < cityList.size(); i++) {
                dataList.add(cityList.get(i).getCityName());
            }
            adapter.notifyDataSetChanged();
            areaList.setSelection(0);
            title.setText(province.getProvinceName());
            currentLevel = LEVEL_CITY;
        } else {
            if (cityList.size() == 0) {
                LogUtil.i("cityList", "size==0");
            }
            loadFromServer(province.getProvinceCode(), "city");
        }
    }

    /**
     * 查询县级数据，先从数据库中查找，找不到时，加载远程服务器中数据
     */
    private void selectCounty() {
        countyList = weatherDataBase.findCounty(city.getId());
        if (countyList != null && countyList.size() > 0) {
            dataList.clear();
            for (int i = 0; i < countyList.size(); i++) {
                dataList.add(countyList.get(i).getCountyName());
            }
            adapter.notifyDataSetChanged();
            areaList.setSelection(0);
            title.setText(city.getCityName());
            currentLevel = LEVEL_COUNTY;
        } else {
            if (countyList.size() == 0) {
                LogUtil.i("countyList", "size==0");
            }
            loadFromServer(city.getCityCode(), "county");
        }
    }

    /**
     * 从服务器加载数据
     * 
     * @param code 要查询当前类型信息所需要的 码 ，查询省信息时，不需要code 可为null
     * @param type 所要查询的信息类型
     * */
    private void loadFromServer(String code, final String type) {
        showProgress();
        HttpUtil.sendHttpRequest(HttpUtil.encapsulationUrl(code, type), new HttpResponseListener() {

            @Override
            public void onFinish(String response) {
                Boolean result = false;
                if ("province".equals(type)) {
                    result = ParserUtil.handleProvincesResponse(weatherDataBase, response);
                } else if ("city".equals(type)) {
                    result = ParserUtil.handleCitiesResponse(weatherDataBase, response, province.getId());
                } else if ("county".equals(type)) {
                    result = ParserUtil.handleCountiesResponse(weatherDataBase, response, city.getId());
                } else if ("weatherCode".equals(type)) {
                    result = ParserUtil.handleCountyWeatherRelResponse(weatherDataBase, response, county.getId());
                }
                if (result) {
                    runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
                            LogUtil.i("choose", "success");
                            closeProgress();
                            if ("province".equals(type)) {
                                selectProvince();
                            } else if ("city".equals(type)) {
                                selectCity();
                            } else if ("county".equals(type)) {
                                selectCounty();
                            } else if ("weatherCode".equals(type)) {
                                selectWeatherCode();
                            }
                        }
                    });
                } else {
                    LogUtil.i("choose", "error");
                }
            }

            @Override
            public void onError(Exception e) {
                LogUtil.e("loadArea", e.toString());
                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        closeProgress();
                        Toast.makeText(ChooseAreaActivity.this, "加载失败", Toast.LENGTH_SHORT).show();
                        ;
                    }
                });
            }
        });
    }

    /**
     * 关闭进度框
     */
    protected void closeProgress() {
        if (progressDialog != null) {
            progressDialog.dismiss();
        }

    }

    /**
     * 打开进度框
     */
    private void showProgress() {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(this);
            progressDialog.setMessage("加载中...");
            progressDialog.setCanceledOnTouchOutside(false);
        }
        progressDialog.show();
    }

    @Override
    public void onBackPressed() {
        if (currentLevel == LEVEL_COUNTY) {
            selectCity();
        } else if (currentLevel == LEVEL_CITY) {
            selectProvince();
        } else {
            if (isFromWeatherActivity) {
                Intent intent = new Intent(this, ShowWeatherActivity.class);
                startActivity(intent);
            }
            finish();
        }
    }

}
