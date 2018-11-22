package com.example.zhixin.crimeintent.database;

import android.database.Cursor;
import android.database.CursorWrapper;

import com.example.zhixin.crimeintent.Crime;

import java.util.Date;
import java.util.UUID;

import static com.example.zhixin.crimeintent.database.CrimeDbSchema.*;

/**
 * Created by ZhiXin on 2018/11/22.
 */

public class CrimeCursorWrapper extends CursorWrapper {
    public CrimeCursorWrapper(Cursor cursor) {
        super(cursor);
    }

    public Crime getCrime() {
        String uuidString = getString(getColumnIndex(CrimeTable.Cols.UUID));
        String title = getString(getColumnIndex(CrimeTable.Cols.TITLE));
        long date = getLong(getColumnIndex(CrimeTable.Cols.DATE));
        int isSolved = getInt(getColumnIndex(CrimeTable.Cols.SOLVED));

        Crime crime = new Crime(UUID.fromString(uuidString));
        crime.setTitle(title);
        crime.setDate(new Date(date));
        crime.setSolved(isSolved != 0);
        return  crime;
    }
}
