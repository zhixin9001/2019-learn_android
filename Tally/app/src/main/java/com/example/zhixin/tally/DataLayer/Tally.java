package com.example.zhixin.tally.DataLayer;

import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

/**
 * Created by ZhiXin on 2019/2/18.
 */

public class Tally {
    public Tally(UUID id){
        mId=id;
//        mDate=Calendar.getInstance();
    }

    public UUID getId() {
        return mId;
    }

    public void setId(UUID id) {
        mId = id;
    }

    public String getDate() {
        return mDate;
    }

    public void setDate(String date) {
        mDate = date;
    }

    public String getDescription() {
        return mDescription;
    }

    public void setDescription(String description) {
        mDescription = description;
    }

    public Double getAmount() {
        return mAmount;
    }

    public void setAmount(Double amount) {
        mAmount = amount;
    }

    public Integer getCategory() {
        return mCategory;
    }

    public void setCategory(Integer category) {
        mCategory = category;
    }

    private UUID mId;
    private String mDate;
    private String mDescription;
    private Double mAmount;
    private Integer mCategory;
}

