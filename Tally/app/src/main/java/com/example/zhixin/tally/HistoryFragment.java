package com.example.zhixin.tally;


import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.zhixin.tally.DataLayer.CategoryArray;
import com.example.zhixin.tally.DataLayer.CategoryEnum;
import com.example.zhixin.tally.DataLayer.CategoryHistoryAmount;
import com.example.zhixin.tally.DataLayer.TallyLab;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;


/**
 * A simple {@link Fragment} subclass.
 */
public class HistoryFragment extends Fragment {
    private static final int START_REQUEST_CODE = 0;
    private static final int END_REQUEST_CODE = 1;
    private static final String DIALOG_DATE = "DialogDate";

    private TextView mStartMonthTextView;
    private TextView mEndMonthTextView;
    private Button mSearchBtn;
    private RecyclerView mHistoryRecyclerView;
    private View mView;
    private HashMap<Float, String> mKeyMonthKV = new HashMap<>();
    private LineChartAdapter mLineChartAdapter;

    public HistoryFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_history, container, false);
        mStartMonthTextView = (TextView) mView.findViewById(R.id.start_month);
        mEndMonthTextView = (TextView) mView.findViewById(R.id.end_month);
        mSearchBtn = (Button) mView.findViewById(R.id.btn_search_history);
        mHistoryRecyclerView = (RecyclerView) mView.findViewById(R.id.history_recycler_view);
        mHistoryRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MONTH, -2);
        mStartMonthTextView.setText(CalendarHelper.getDateString(calendar, "yyyy-MM"));
        mEndMonthTextView.setText(CalendarHelper.getDateString("yyyy-MM"));

        mStartMonthTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentManager manager = getFragmentManager();
                try {
                    Calendar calendar = CalendarHelper.getCalendar(mStartMonthTextView.getText().toString(), "yyyy-MM");
                    DatePickerFragment dialog = DatePickerFragment.newInstance(calendar.getTime(), true);
                    dialog.setTargetFragment(HistoryFragment.this, START_REQUEST_CODE);
                    dialog.show(manager, DIALOG_DATE);
                } catch (ParseException pe) {
                    Toast.makeText(getContext(), "Parse date error!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        mEndMonthTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentManager manager = getFragmentManager();
                try {
                    Calendar calendar = CalendarHelper.getCalendar(mEndMonthTextView.getText().toString(), "yyyy-MM");
                    DatePickerFragment dialog = DatePickerFragment.newInstance(calendar.getTime(), true);
                    dialog.setTargetFragment(HistoryFragment.this, END_REQUEST_CODE);
                    dialog.show(manager, DIALOG_DATE);
                } catch (ParseException pe) {
                    Toast.makeText(getContext(), "Parse date error!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        mSearchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showLineChart();
            }
        });
        return mView;
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK) {
            return;
        }

        if (requestCode == START_REQUEST_CODE) {
            Date result = (Date) data.getSerializableExtra(DatePickerFragment.EXTRA_DATE);
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(result.getTime());
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM");
            mStartMonthTextView.setText(dateFormat.format(calendar.getTime()));
        } else if (requestCode == END_REQUEST_CODE) {
            Date result = (Date) data.getSerializableExtra(DatePickerFragment.EXTRA_DATE);
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(result.getTime());
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM");
            mEndMonthTextView.setText(dateFormat.format(calendar.getTime()));
        }
    }

    private void showLineChart() {
        mKeyMonthKV.clear();
        List<CategoryHistoryAmount> categoryHistoryAmountList = new ArrayList<>();
        try {
            Calendar startCalendar = CalendarHelper.getCalendar(mStartMonthTextView.getText().toString(), "yyyy-MM");
            Calendar endCalendar = CalendarHelper.getCalendar(mEndMonthTextView.getText().toString(), "yyyy-MM");

            CategoryHistoryAmount categoryHistoryAmount = new CategoryHistoryAmount();
            categoryHistoryAmount.setCategoryEnum(CategoryEnum.Income);
            categoryHistoryAmountList.add(categoryHistoryAmount);
            for (int i = 0; i < CategoryArray.getArray().length; i++) {
                if (CategoryArray.getArray()[i].equals(CategoryEnum.Income.value())) {
                    continue;
                }
                categoryHistoryAmount = new CategoryHistoryAmount();
                categoryHistoryAmount.setCategoryEnum(CategoryEnum.getEnumByIndex(i));
                categoryHistoryAmountList.add(categoryHistoryAmount);
            }

            while (startCalendar.get(Calendar.YEAR) < endCalendar.get(Calendar.YEAR)
                    || startCalendar.get(Calendar.MONTH) <= endCalendar.get(Calendar.MONTH)) {
                HashMap<CategoryEnum, Double> categoryAmount = TallyLab.get(getActivity()).getCategoryAmount(startCalendar);
                String month = CalendarHelper.getDateString(startCalendar, "yyyy-MM");
                for (CategoryHistoryAmount categoryHistoryAmount1 : categoryHistoryAmountList) {
                    double amount = 0;
                    if (categoryAmount.containsKey(categoryHistoryAmount1.getCategoryEnum())) {
                        amount = categoryAmount.get(categoryHistoryAmount1.getCategoryEnum());
                    }
                    categoryHistoryAmount1.putMonthAmountKV(month, amount);
                }
                startCalendar.add(Calendar.MONTH, 1);
            }
        } catch (Exception pe) {
            Log.i("tag_export", pe.getMessage());
            pe.printStackTrace();
        }

        if (mLineChartAdapter == null) {
            mLineChartAdapter = new LineChartAdapter(categoryHistoryAmountList);
            mHistoryRecyclerView.setAdapter(mLineChartAdapter);
        } else {
            mLineChartAdapter.updateData(categoryHistoryAmountList);
            mLineChartAdapter.notifyDataSetChanged();
        }
    }

    private class LineChartHolder extends RecyclerView.ViewHolder {
        private CategoryHistoryAmount mCategoryHistoryAmount;
        private LineChart mLineChart;

        public LineChartHolder(LayoutInflater inflater, ViewGroup parent) {
            super(inflater.inflate(R.layout.item_category_history, parent, false));
            mLineChart = (LineChart) itemView.findViewById(R.id.lineChart);
        }

        public void bind(CategoryHistoryAmount categoryHistoryAmount) {
            mCategoryHistoryAmount = categoryHistoryAmount;
            String title = mCategoryHistoryAmount.getCategoryEnum().value();
            TreeMap<String, Double> monthAmountKV = mCategoryHistoryAmount.getMonthAmountKV();
            //line chart
            //显示边界
            mLineChart.setDrawBorders(false);
            //设置数据
            final List<Entry> entries = new ArrayList<>();
            float i = 1;
            Iterator it = monthAmountKV.keySet().iterator();
            while (it.hasNext()) {
                String month = it.next().toString();
                if (!mKeyMonthKV.containsValue(month)) {
                    mKeyMonthKV.put(i, month);
                }
                double amount = monthAmountKV.get(month);
                entries.add(new Entry(i, (float) amount));
                i++;
            }
            //一个LineDataSet就是一条线
            LineDataSet lineDataSet = new LineDataSet(entries, title);
            lineDataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);  //set line mode
            int colorIndex = CategoryArray.getIndex(mCategoryHistoryAmount.getCategoryEnum().value());
            lineDataSet.setColor(ColorHelper.PIE_COLORS[colorIndex]);
            lineDataSet.setLineWidth(1f);
            lineDataSet.setCircleRadius(1f);
            lineDataSet.setValueTextSize(10f);
            lineDataSet.setDrawFilled(true);
            lineDataSet.setFillColor(ColorHelper.PIE_COLORS[colorIndex]);
            LineData data = new LineData(lineDataSet);

            mLineChart.setBackgroundColor(Color.WHITE);//是否显示边界
            mLineChart.setDrawBorders(false);
            mLineChart.setDrawGridBackground(false);//是否展示网格线
            Description description = new Description();
            description.setEnabled(false);
            mLineChart.setDescription(description);

            /***XY轴的设置***/
            XAxis xAxis = mLineChart.getXAxis();
            YAxis leftYAxis = mLineChart.getAxisLeft();
            YAxis rightYaxis = mLineChart.getAxisRight();
            xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
            rightYaxis.setEnabled(false);
            if (entries.size() <= 1) {
                xAxis.setLabelCount(1, false);
            } else {
                xAxis.setLabelCount(entries.size() - 1, false);
            }

            xAxis.setValueFormatter(new IAxisValueFormatter() {
                @Override
                public String getFormattedValue(float value, AxisBase axis) {
                    if (mKeyMonthKV.containsKey(value)) {
                        return mKeyMonthKV.get(value).substring(2);
                    } else {
                        return "";
                    }
                }
            });
            mLineChart.setData(data);
        }
    }

    private class LineChartAdapter extends RecyclerView.Adapter<LineChartHolder> {
        private List<CategoryHistoryAmount> mCategoryHistoryAmountList;

        public void updateData(List<CategoryHistoryAmount> data) {
            mCategoryHistoryAmountList = data;
        }

        public LineChartAdapter(List<CategoryHistoryAmount> data) {
            mCategoryHistoryAmountList = data;
        }

        @Override
        public LineChartHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
            return new LineChartHolder(layoutInflater, parent);
        }

        @Override
        public void onBindViewHolder(LineChartHolder holder, int position) {
            CategoryHistoryAmount categoryHistoryAmount = mCategoryHistoryAmountList.get(position);
            holder.bind(categoryHistoryAmount);
        }

        @Override
        public int getItemCount() {
            return mCategoryHistoryAmountList.size();
        }
    }

}
