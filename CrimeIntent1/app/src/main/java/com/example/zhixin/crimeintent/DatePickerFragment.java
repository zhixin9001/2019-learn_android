package com.example.zhixin.crimeintent;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.DatePicker;

/**
 * Created by ZhiXin on 2018/11/16.
 */

public class DatePickerFragment extends DialogFragment {
    private  static  final String ARG_DATE="date";
    private DatePicker mDatePicker;



    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View v= LayoutInflater.from(getActivity())
                .inflate(R.layout.dialog_date,null);
        return  new AlertDialog.Builder(getActivity())
                .setView(v)
                .setTitle(R.string.date_picker_title)
                .setPositiveButton(android.R.string.ok,null)
                .create();
    }
}
