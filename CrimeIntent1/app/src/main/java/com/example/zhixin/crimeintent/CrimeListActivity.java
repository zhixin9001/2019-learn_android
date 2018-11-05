package com.example.zhixin.crimeintent;

import android.support.v4.app.Fragment;

/**
 * Created by ZhiXin on 2018/11/3.
 */

public class CrimeListActivity extends SingleFragmentActivity {

    @Override
    protected Fragment createFragment() {
        return new CrimeListFragment();
    }
}