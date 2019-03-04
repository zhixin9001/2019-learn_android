package com.example.zhixin.tally;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.zhixin.tally.DataLayer.CategoryArray;
import com.example.zhixin.tally.DataLayer.Tally;
import com.example.zhixin.tally.DataLayer.TallyLab;

import java.util.Calendar;
import java.util.List;

/**
 * Created by ZhiXin on 2019/2/18.
 */

public class MainFragment extends Fragment {
    private RecyclerView mTallyRecyclerView;
    private TallyAdapter mAdapter;
    private static int DETAIL_REQUEST = 1;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);
        mTallyRecyclerView = (RecyclerView) view.findViewById(R.id.tally_recycler_view);
        mTallyRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        updateUI();
        return view;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.main_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.new_tally:
                Intent intent = new Intent(getActivity(), DetailActivity.class);
                startActivityForResult(intent, DETAIL_REQUEST);
                return true;
            case R.id.import_export:
                Intent intentImportExport = new Intent(getActivity(), ImportExportActivity.class);
                startActivity(intentImportExport);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK) {
            return;
        }
        if (requestCode == DETAIL_REQUEST) {
            updateUI();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        updateUI();
    }

    private void updateUI() {
        TallyLab tallyLab = TallyLab.get(getActivity());
        Calendar calendar = Calendar.getInstance();
        String endMonth = TallyLab.getEndOfMonth(calendar);
        calendar.add(Calendar.MONTH, -6);
        String startMonth = TallyLab.getStartOfMonth(calendar);
        List<Tally> tallies = tallyLab.getTallies(startMonth, endMonth);
        if (mAdapter == null) {
            mAdapter = new TallyAdapter(tallies);
            mTallyRecyclerView.setAdapter(mAdapter);
        } else {
            mAdapter.updateTallies(tallies);
            mAdapter.notifyDataSetChanged();
        }

        AppCompatActivity activity = (AppCompatActivity) getActivity();
        activity.getSupportActionBar().setSubtitle(String.format("当月支出: ￥%.2f", tallyLab.getExpend(Calendar.getInstance())));
    }

    private class TallyHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        private Tally mTally;
        private TextView mDescTextView;
        private TextView mAmountTextView;
        private TextView mDateTextView;
        private TextView mCategoryTextView;

        public TallyHolder(LayoutInflater inflater, ViewGroup parent) {
            super(inflater.inflate(R.layout.item_tally, parent, false));
            mDescTextView = (TextView) itemView.findViewById(R.id.tally_desc);
            mAmountTextView = (TextView) itemView.findViewById(R.id.tally_amount);
            mCategoryTextView = (TextView) itemView.findViewById(R.id.tally_category);
            mDateTextView = (TextView) itemView.findViewById(R.id.tally_date);
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
        }

        //
        public void bind(Tally tally) {
            mTally = tally;
            mDescTextView.setText(mTally.getDescription());
            String amount = mTally.getAmount().toString();
            Integer i = mTally.getCategory();
            String date = mTally.getDate();

            mAmountTextView.setText(amount);
            if (i >= 0 && i < CategoryArray.getArray().length) {
                mCategoryTextView.setText(CategoryArray.getArray()[i]);
            } else {
                mCategoryTextView.setText(i.toString());
            }

            mDateTextView.setText(date);
        }

        @Override
        public void onClick(View view) {
            Intent intent = new Intent(getActivity(), DetailActivity.class);
            intent.putExtra(DetailFragment.EXTRA_TALLY_ID, mTally.getId());
            startActivityForResult(intent, DETAIL_REQUEST);
        }

        @Override
        public boolean onLongClick(View view) {
            PopupMenu popupMenu = new PopupMenu(getActivity(), view);
            popupMenu.getMenuInflater().inflate(R.menu.popup, popupMenu.getMenu());
            popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    if (item.getItemId() == R.id.pop_delete) {
                        TallyLab.get(getActivity()).deleteTally(mTally.getId());
                        updateUI();
                        return true;
                    }
                    return false;
                }
            });
            popupMenu.show();
            return true;
        }
    }

    private class TallyAdapter extends RecyclerView.Adapter<TallyHolder> {
        private List<Tally> mTallies;

        public void updateTallies(List<Tally> tallies) {
            mTallies = tallies;
        }

        public TallyAdapter(List<Tally> tallies) {
            mTallies = tallies;
        }

        @Override
        public TallyHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
            return new TallyHolder(layoutInflater, parent);
        }

        @Override
        public void onBindViewHolder(TallyHolder holder, int position) {
            Tally tally = mTallies.get(position);
            holder.bind(tally);
        }

        @Override
        public int getItemCount() {
            return mTallies.size();
        }
    }

}
