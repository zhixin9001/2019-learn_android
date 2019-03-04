package com.example.zhixin.photogallery;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by ZhiXin on 2019/2/17.
 */

public abstract class VisibleFragment extends Fragment{
    private static final String TAG = "VisibleFragment";

    @Override
    public void onStart() {
        super.onStart();
        IntentFilter filter = new IntentFilter(PollService.ACTION_SHOW_NORIFICATION);
        getActivity().registerReceiver(mOnShowNotification, filter,PollService.PERM_PRIVATE,null);
    }

    @Override
    public void onStop() {
        super.onStop();
        getActivity().unregisterReceiver(mOnShowNotification);
    }

    private BroadcastReceiver mOnShowNotification = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
//            Toast.makeText(getActivity(),
//                    "Got a broadcast:" + intent.getAction(),
//                    Toast.LENGTH_LONG)
//                    .show();
            Log.i(TAG,"canceling notification");
            setResultCode(Activity.RESULT_CANCELED);

        }
    };
}

