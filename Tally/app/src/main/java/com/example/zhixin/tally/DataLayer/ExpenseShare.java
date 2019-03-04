package com.example.zhixin.tally.DataLayer;

/**
 * Created by ZhiXin on 2019/2/25.
 */

public class ExpenseShare {
    public CategoryEnum getCategory() {
        return mCategory;
    }

    public void setCategory(CategoryEnum category) {
        mCategory = category;
    }

    public double getShareRate() {
        return shareRate;
    }

    public void setShareRate(double shareRate) {
        this.shareRate = shareRate;
    }

    public double getShareAmount() {
        return shareAmount;
    }

    public void setShareAmount(double shareAmount) {
        this.shareAmount = shareAmount;
    }

    private CategoryEnum mCategory;
    private double shareRate;
    private double shareAmount;
}
