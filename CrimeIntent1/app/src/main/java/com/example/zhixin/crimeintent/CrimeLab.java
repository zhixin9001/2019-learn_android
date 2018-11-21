package com.example.zhixin.crimeintent;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by ZhiXin on 2018/11/3.
 */

public class CrimeLab {
    private static CrimeLab sCrimeLab;
    private List<Crime> mCrimes;

    public static CrimeLab get(Context context) {
        if (sCrimeLab == null) {
            sCrimeLab = new CrimeLab(context);
        }
        return sCrimeLab;
    }

    public void addCrime(Crime c){
        mCrimes.add(c);
    }

    private CrimeLab(Context context) {
        mCrimes = new ArrayList<>();
        // for (int i = 0; i < 100; i++) {
        //     Crime crime = new Crime();
        //     crime.setTitle("Crime #" + i);
        //     crime.setSolved(i % 2 == 0);
        //     mCrimes.add(crime);
        // }
    }

    public List<Crime> getCrimes() {
        return mCrimes;
    }

    public Crime getCrime(UUID id) {
        for (Crime crime : mCrimes) {
            if (crime.getId().equals(id)) {
                return crime;
            }
        }
        return null;
    }
}

public class CrimeDbSchema{
    public static final class CrimeTable{
        public static final String NAME="crimes";

        public static final class Cols{
            public static final String UUID="uuid";
            public static final String TITLE="title";
            public static final String DATE="date";
            public static final String SOLVED="solved";
        }
    }
}



