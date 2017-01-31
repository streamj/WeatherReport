package com.example.stream.weatherreport.db;

import org.litepal.crud.DataSupport;

/**
 * Created by StReaM on 1/31/2017.
 */

public class Province extends DataSupport {
    private int id; // primary key
    private String provinceName;
    private int provinceCode;

    public int getId() {
        return id;
    }

    public String getProvinceName() {
        return provinceName;
    }

    public int getProvinceCode() {
        return provinceCode;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setProvinceCode(int provinceCode) {
        this.provinceCode = provinceCode;
    }

    public void setProvinceName(String provinceName) {
        this.provinceName = provinceName;
    }
}
