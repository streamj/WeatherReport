package com.example.stream.weatherreport.util;

import com.example.stream.weatherreport.model.SortModel;

import java.util.Comparator;

/**
 * Created by StReaM on 2/5/2017.
 */

public class PinyinComparator implements Comparator<SortModel> {
    public int compare(SortModel o1, SortModel o2) {
        //这里主要是用来对ListView里面的数据根据ABCDEFG...来排序
        if (o2.getSortLetters().equals("#")) {
            return -1;
        } else if (o1.getSortLetters().equals("#")) {
            return 1;
        } else {
            return o1.getSortLetters().compareTo(o2.getSortLetters());
        }
    }
}
