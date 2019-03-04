package com.example.zhixin.tally;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.TextView;

public class NavigateActivity extends AppCompatActivity {

    private TextView mTextMessage;
    private FragmentTransaction transaction;
    private FragmentManager fragmentManager;
    private static String SAVED_SELECTED_ITEM = "SAVED_SELECTED_ITEM";
    private int mSelectedItemID;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            fragmentManager = getSupportFragmentManager();
            transaction = fragmentManager.beginTransaction();
            mSelectedItemID = item.getItemId();
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    transaction.replace(R.id.tab_fragment, new MainFragment());
                    transaction.commit();
                    return true;
                case R.id.navigation_dashboard:
                    transaction.replace(R.id.tab_fragment, new AnalyseFragment());
                    transaction.commit();
                    return true;
                case R.id.navigation_notifications:
                    transaction.replace(R.id.tab_fragment, new HistoryFragment());
                    transaction.commit();
                    return true;
                default:
                    transaction.replace(R.id.tab_fragment, new MainFragment());
                    transaction.commit();
                    return true;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigate);

        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        navigation.setSelectedItemId(mSelectedItemID);
//        setDefaultFragment();

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(SAVED_SELECTED_ITEM, mSelectedItemID);
    }

    private void setDefaultFragment() {

        fragmentManager = getSupportFragmentManager();
        transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.tab_fragment, new MainFragment());
        transaction.commit();
    }
}
