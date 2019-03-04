package com.example.zhixin.photogallery;

import android.content.Context;
import android.preference.PreferenceManager;

/**
 * Created by ZhiXin on 2019/2/12.
 */

public class QueryPreferences {
    private static final String PRED_SEARCH_QUERY="searchQuery";
    private static final String PRED_LAST_RESULT_ID="lastResultId";
    private static final String PRED_IS_ALARM_ON="isAlarmOn";

    public static String getStoredQuery(Context context){
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getString(PRED_SEARCH_QUERY,null);
    }

    public  static void setStoredQuery(Context context,String query){
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putString(PRED_SEARCH_QUERY,query)
                .apply();
    }

    public static String getLastResultId(Context context){
        return  PreferenceManager.getDefaultSharedPreferences(context)
                .getString(PRED_LAST_RESULT_ID,null);
    }

    public  static void  setLastResultId(Context context,String lastResultId){
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putString(PRED_LAST_RESULT_ID,lastResultId)
                .apply();
    }

    public static boolean isAlarmOn(Context context){
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(PRED_IS_ALARM_ON,false);
    }

    public static void setAlarmOn(Context context,boolean isOn){
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putBoolean(PRED_IS_ALARM_ON,isOn)
                .apply();
    }
}
