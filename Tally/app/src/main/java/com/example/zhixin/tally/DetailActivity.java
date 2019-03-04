package com.example.zhixin.tally;

import android.support.v4.app.Fragment;

/**
 * Created by ZhiXin on 2019/2/20.
 */

public class DetailActivity extends SingleFragmentActivity {
    @Override
    protected Fragment createFragment() {
        return new DetailFragment();
    }
}
