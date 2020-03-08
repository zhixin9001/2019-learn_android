package com.example.zhixin.tally;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.zhixin.tally.DataLayer.CategoryArray;
import com.example.zhixin.tally.DataLayer.CategoryEnum;
import com.example.zhixin.tally.DataLayer.Tally;
import com.example.zhixin.tally.DataLayer.TallyLab;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.UUID;

/**
 * Created by ZhiXin on 2019/2/20.
 */

public class DetailFragment extends Fragment {
    private static final int REQUEST_CODE = 0;
    private static final String DIALOG_DATE = "DialogDate";
    public static final String EXTRA_TALLY_ID = "EXTRA_TALLY_ID";

    private EditText mDescriptionEditText;
    private EditText mAmountEditText;
    private Spinner mCategorySpinner;
    private Button mDateBtn;
    private Button mSaveBtn;
    private Button mCancelBtn;
    private Tally mTally;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_detail, container, false);
        mDescriptionEditText = (EditText) v.findViewById(R.id.tally_desc);
        mAmountEditText = (EditText) v.findViewById(R.id.tally_amount);
        mCategorySpinner = (Spinner) v.findViewById(R.id.tally_category);
        mDateBtn = (Button) v.findViewById(R.id.tally_date);
        mSaveBtn = (Button) v.findViewById(R.id.btn_save);
        mCancelBtn = (Button) v.findViewById(R.id.btn_cancel);

        mAmountEditText.addTextChangedListener(new MoneyTextWatcher(mAmountEditText));

        ArrayAdapter arrayAdapter = new ArrayAdapter(getContext(), android.R.layout.simple_spinner_item, CategoryArray.getArray());
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mCategorySpinner.setAdapter(arrayAdapter);
        setUI();

        mDateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentManager manager = getFragmentManager();

                Calendar calendar = Calendar.getInstance();
                if (mTally != null) {
                    try {
                        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
                        calendar.setTime(simpleDateFormat.parse(mTally.getDate()));
                    } catch (ParseException pr) {
                        Toast.makeText(getContext(), "Parse date error!", Toast.LENGTH_SHORT).show();
                    }
                }
                DatePickerFragment dialog = DatePickerFragment.newInstance(calendar.getTime(), false,true);
                dialog.setTargetFragment(DetailFragment.this, REQUEST_CODE);
                dialog.show(manager, DIALOG_DATE);
            }
        });

        mSaveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mSaveBtn.setEnabled(false);
                if (!saveTally()) {
                    mSaveBtn.setEnabled(true);
                    return;
                }
                Toast.makeText(getContext(), "Saved Successfully!", Toast.LENGTH_SHORT).show();
                getActivity().setResult(Activity.RESULT_OK);
                getActivity().finish();
            }
        });
        return v;
    }

    private void setDateBtnToday() {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        mDateBtn.setText(dateFormat.format(calendar.getTime()));
        mDateBtn.setTag(calendar);
    }

    private void setUI() {
        Serializable serializable = getActivity().getIntent().getSerializableExtra(EXTRA_TALLY_ID);

        if (serializable == null) {
            setDateBtnToday();
        } else {
            UUID tallyId = (UUID) serializable;
            mTally = TallyLab.get(getActivity()).getTally(tallyId);
            if (mTally != null) {
                mDescriptionEditText.setText(mTally.getDescription());
                mAmountEditText.setText(mTally.getAmount().toString());
                mCategorySpinner.setSelection(mTally.getCategory());
                mDateBtn.setText(mTally.getDate());

            } else {
                setDateBtnToday();
            }
        }
    }

    private boolean saveTally() {
        Serializable serializable = getActivity().getIntent().getSerializableExtra(EXTRA_TALLY_ID);
        String description = mDescriptionEditText.getText().toString();
        String amount = mAmountEditText.getText().toString();
        String date = mDateBtn.getText().toString();
        Integer category = mCategorySpinner.getSelectedItemPosition();

        if (description.trim().isEmpty()
                || amount.trim().isEmpty()) {
            Toast.makeText(getContext(), "Please input description or amount!", Toast.LENGTH_SHORT).show();
            return false;
        }

        Tally tally = null;
        if (serializable == null) { //add
            tally = new Tally(UUID.randomUUID());
        } else { //edit
            tally = new Tally((UUID) serializable);
        }

        tally.setDescription(description);
        tally.setAmount(Double.parseDouble(amount));
        tally.setDate(date);
        tally.setCategory(category);
        if (serializable == null) { //add
            TallyLab.get(getActivity()).addTally(tally);
        } else { //edit
            TallyLab.get(getActivity()).updateTally(tally);
        }
        return true;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK) {
            return;
        }

        if (requestCode == REQUEST_CODE) {
            Date result = (Date) data.getSerializableExtra(DatePickerFragment.EXTRA_DATE);
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(result.getTime());
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            String dateStr = dateFormat.format(calendar.getTime());
            mDateBtn.setText(dateStr);
        }
    }
}
