package com.example.zhixin.nerdlauncher;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by ZhiXin on 2019/1/22.
 */

public class NerdLauncherFragment extends Fragment {
    private RecyclerView mRecyclerView;

    public static NerdLauncherFragment newInstance() {
        return new NerdLauncherFragment();
    }

//    @Nullable
//    @Override
//    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
//        View v = inflater.inflate(R.layout.fragment_nerd_launcher);
//        mRecyclerView = (RecyclerView) v.findViewById(R.id.app_recucler_view);
//        return super.onCreateView(inflater, container, savedInstanceState);
//    }
}
