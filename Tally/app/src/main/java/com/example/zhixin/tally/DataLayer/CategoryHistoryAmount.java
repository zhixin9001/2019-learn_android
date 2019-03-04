package com.example.zhixin.tally.DataLayer;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * Created by ZhiXin on 2019/3/3.
 */

public class CategoryHistoryAmount {
    public CategoryEnum getCategoryEnum() {
        return mCategoryEnum;
    }

    public void setCategoryEnum(CategoryEnum categoryEnum) {
        mCategoryEnum = categoryEnum;
    }

    public TreeMap<String, Double> getMonthAmountKV() {
        return mMonthAmountKV;
    }

    public void putMonthAmountKV(String key, double value) {
        mMonthAmountKV.put(key, value);
    }

    private CategoryEnum mCategoryEnum;
    private TreeMap<String, Double> mMonthAmountKV = new TreeMap<>();
}

