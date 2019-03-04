package com.example.zhixin.tally;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.zhixin.tally.DataLayer.ExpenseShare;
import com.example.zhixin.tally.DataLayer.TallyLab;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;

import org.w3c.dom.Text;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class AnalyseFragment extends Fragment {
    private static final int REQUEST_CODE = 0;
    private static final String DIALOG_DATE = "DialogDate";

    private TextView mMonthExpend;
    private TextView mMonthIncome;
    private TextView mMonthBalance;
    private TextView mSearchMonth;
    private Button mSearchBtn;
    private View view;
    private ListView listView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_analyse, container, false);
        mMonthExpend = (TextView) view.findViewById(R.id.month_expend);
        mMonthIncome = (TextView) view.findViewById(R.id.month_income);
        mMonthBalance = (TextView) view.findViewById(R.id.month_balance);
        mSearchMonth = (TextView) view.findViewById(R.id.text_search_month);
        mSearchBtn = (Button) view.findViewById(R.id.btn_search);
        listView = (ListView) view.findViewById(R.id.share_list_view);

        mSearchMonth.setText(CalendarHelper.getDateString("yyyy-MM"));
        mSearchMonth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentManager manager = getFragmentManager();
                try {
                    Calendar calendar1 = CalendarHelper.getCalendar(mSearchMonth.getText().toString(), "yyyy-MM");
                    DatePickerFragment dialog = DatePickerFragment.newInstance(calendar1.getTime(), true);
                    dialog.setTargetFragment(AnalyseFragment.this, REQUEST_CODE);
                    dialog.show(manager, DIALOG_DATE);
                } catch (ParseException pe) {
                    Toast.makeText(getContext(), "Parse date error!", Toast.LENGTH_SHORT).show();
                }
            }
        });
        mSearchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                search();
            }
        });

//        search();
        return view;
    }

    private void search() {
        showCaseFlow();
        showExpenseShare();
    }

    private void showCaseFlow() {
        try {
            Calendar calendar = CalendarHelper.getCalendar(mSearchMonth.getText().toString(), "yyyy-MM");
            double expend = TallyLab.get(getActivity()).getExpend(calendar);
            double income = TallyLab.get(getActivity()).getIncome(calendar);
            mMonthExpend.setText(String.format("%.2f", expend));
            mMonthIncome.setText(String.format("%.2f", income));
            mMonthBalance.setText(String.format("%.2f", income - expend));
        } catch (ParseException pe) {
            Toast.makeText(getContext(), "Parse date error!", Toast.LENGTH_SHORT).show();
        }
    }

    private void showExpenseShare() {
        try {
            Calendar calendar = CalendarHelper.getCalendar(mSearchMonth.getText().toString(), "yyyy-MM");
            Double total = Double.parseDouble(mMonthExpend.getText().toString());
            List<ExpenseShare> expenseShares = TallyLab.get(getActivity()).getExpenseShare(calendar, total);

            setListView(expenseShares);
            setPieChart(expenseShares);
        } catch (ParseException pe) {
            Toast.makeText(getContext(), "Parse date error!", Toast.LENGTH_SHORT).show();
        }
    }


    private void setListView(List<ExpenseShare> expenseShares) {
        if (listView.getAdapter() == null) {
            ViewAdapter viewAdapter = new ViewAdapter(getActivity(), expenseShares);
            listView.setAdapter(viewAdapter);
        } else {
            ViewAdapter viewAdapter = (ViewAdapter) listView.getAdapter();
            viewAdapter.updateData(expenseShares);
            viewAdapter.notifyDataSetChanged();
        }

    }

    private void setPieChart(List<ExpenseShare> expenseShares) {
        PieChart mPieChart = (PieChart) view.findViewById(R.id.piechart);
        List<PieEntry> entries = new ArrayList<>();

        mPieChart.setUsePercentValues(true);//设置使用百分比（后续有详细介绍）
        mPieChart.setHighlightPerTapEnabled(true);//点击是否放大
        mPieChart.setExtraOffsets(10, 10, 10, 10); //设置边距
        mPieChart.setRotationEnabled(true);//是否可以旋转
        Description description = new Description();
        description.setEnabled(false);
        mPieChart.setDescription(description);

        for (ExpenseShare es : expenseShares) {
            entries.add(new PieEntry((float) es.getShareRate(), es.getCategory().value()));
        }

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setSliceSpace(1f);//设置饼块之间的间隔
        dataSet.setColors(ColorHelper.PIE_COLORS);//设置饼块的颜色
        dataSet.setValueLinePart1OffsetPercentage(80f);//数据连接线距图形片内部边界的距离，为百分数
        dataSet.setValueLinePart1Length(0.3f);
        dataSet.setValueLinePart2Length(0.4f);
        dataSet.setYValuePosition(PieDataSet.ValuePosition.OUTSIDE_SLICE);
        dataSet.setValueLineColor(Color.YELLOW);//设置连接线的颜色

        PieData pieData = new PieData(dataSet);
        pieData.setValueFormatter(new PercentFormatter());
        pieData.setValueTextSize(11f);
        pieData.setValueTextColor(Color.DKGRAY);

        Legend legend = mPieChart.getLegend();
        legend.setEnabled(true);//是否显示图例
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.LEFT);//图例相对于图表横向的位置
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);//图例相对于图表纵向的位置
        legend.setOrientation(Legend.LegendOrientation.HORIZONTAL);//图例显示的方向
        legend.setDrawInside(false);
        legend.setDirection(Legend.LegendDirection.LEFT_TO_RIGHT);
        mPieChart.setData(pieData);
        mPieChart.notifyDataSetChanged();
        mPieChart.invalidate();
    }

    private class ViewHolder {
        private TextView mShareDescTextView;
        private TextView mShareAmountTextView;

        public ViewHolder(View view, ExpenseShare expenseShare) {
            mShareDescTextView = view.findViewById(R.id.share_desc);
            mShareAmountTextView = view.findViewById(R.id.share_amount);
            mShareDescTextView.setText(expenseShare.getCategory().value());
            mShareAmountTextView.setText(String.format("%.2f", expenseShare.getShareAmount()));
        }
    }

    private class ViewAdapter extends BaseAdapter {
        private List<ExpenseShare> expenseShares;
        private Context context;

        public void updateData(List<ExpenseShare> expenseShares) {
            this.expenseShares = expenseShares;
        }

        public ViewAdapter(Context context, List<ExpenseShare> expenseShares) {
            this.context = context;
            this.expenseShares = expenseShares;
        }

        @Override
        public int getCount() {
            return expenseShares.size();
        }

        @Override
        public Object getItem(int i) {
            return expenseShares.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            view = LayoutInflater.from(context).inflate(R.layout.item_share, null);
            ViewHolder viewHolder = new ViewHolder(view, expenseShares.get(i));
            view.setTag(viewHolder);
            return view;
        }
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
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM");
            mSearchMonth.setText(dateFormat.format(calendar.getTime()));
        }
    }
}
