package com.example.zhixin.tally.DataLayer;

/**
 * Created by ZhiXin on 2019/2/23.
 */

public class CategoryArray {
    private static String[] array = new String[]{
            "食",
            "衣",
            "住",
            "行",
            "育儿",
            "健康",
            "教育",
            "话费",
            "社交",
            "其他",
            "收入"};


    public static String[] getArray() {
        return array;
    }

    public static Integer getIndex(String val){
        for(int i=0;i<array.length;i++){
            if(val.equals(array[i])){
                return i;
            }
        }
        return -1;
    }
}
