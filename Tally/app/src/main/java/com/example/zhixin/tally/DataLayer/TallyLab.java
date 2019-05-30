package com.example.zhixin.tally.DataLayer;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.zhixin.tally.CalendarHelper;
import com.example.zhixin.tally.DataLayer.TallyDbSchema.TallyTable;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

/**
 * Created by ZhiXin on 2019/2/18.
 */

public class TallyLab {
    private static TallyLab sTallyLab;
    private Context mContext;
    private SQLiteDatabase mDatabase;

    private TallyLab(Context context) {
        mContext = context.getApplicationContext();
        mDatabase = new TallyBaseHelper(mContext)
                .getWritableDatabase();

    }

    public static TallyLab get(Context context) {
        if (sTallyLab == null) {
            sTallyLab = new TallyLab(context);
        }
        return sTallyLab;
    }

    public void addTally(Tally tally) {
        ContentValues values = getContentValues(tally);
        mDatabase.insert(TallyTable.NAME, null, values);
    }

    public void updateTally(Tally tally) {
        String uuidStr = tally.getId().toString();
        ContentValues values = getContentValues(tally);
        mDatabase.update(TallyTable.NAME, values,
                TallyTable.Cols.UUID + "=?",
                new String[]{uuidStr});

    }

    public void deleteTally(UUID tallyId) {
        mDatabase.delete(TallyTable.NAME,
                TallyTable.Cols.UUID + " =?",
                new String[]{tallyId.toString()});
    }

    public void deleteTally(String startDate, String endDate) {

        mDatabase.delete(TallyTable.NAME,
                String.format("%s between date('%s') and date('%s')", TallyTable.Cols.DATE, startDate, endDate), null);
    }

    public Tally getTally(UUID tallyId) {
        TallyCursorWrapper cursorWrapper = queryTallies("uuid=?", new String[]{tallyId.toString()});
        Tally tally = null;
        try {
            cursorWrapper.moveToFirst();
            if (!cursorWrapper.isAfterLast()) {
                tally = cursorWrapper.getTally();
            }
        } finally {
            cursorWrapper.close();
        }
        return tally;
    }

    public List<Tally> getTallies(String startDate, String endDate) {
        List<Tally> tallies = new ArrayList<>();
        TallyCursorWrapper cursorWrapper = queryTallies(String.format("%s between date('%s') and date('%s')", TallyTable.Cols.DATE, startDate, endDate),
                null);
        try {
            cursorWrapper.moveToFirst();
            while (!cursorWrapper.isAfterLast()) {
                tallies.add(cursorWrapper.getTally());
                cursorWrapper.moveToNext();
            }
        } finally {
            cursorWrapper.close();
        }
        return tallies;
    }

    public boolean importOneYearTallies(List<Tally> tallies) {
        boolean result = false;
        try {
            mDatabase.beginTransaction();
            Calendar calendar = CalendarHelper.getCalendar(tallies.get(0).getDate(), "yyyy-MM-dd");
            String startDate = getStartOfYear(calendar);
            String endDate = getEndOfYear(calendar);
            deleteTally(startDate, endDate);
            for (Tally tally : tallies) {
                this.addTally(tally);
            }
            mDatabase.setTransactionSuccessful();
            result = true;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            mDatabase.endTransaction();
        }
        return result;
    }

    private TallyCursorWrapper queryTallies(String whereClause, String[] whereArgs) {
        Cursor cursor = mDatabase.query(TallyTable.NAME,
                null,
                whereClause,
                whereArgs,
                null,
                null,
                TallyTable.Cols.DATE + " desc");
        return new TallyCursorWrapper(cursor);
    }

    public double getExpend(Calendar calendar) {
        return getExpendOrIncome(calendar, false);
    }

    public double getIncome(Calendar calendar) {
        return getExpendOrIncome(calendar, true);
    }

    public List<ExpenseShare> getExpenseShare(Calendar calendar, double total) {
        List<ExpenseShare> expenseShares = new ArrayList<>();
        String startDate = getStartOfMonth(calendar);
        String endDate = getEndOfMonth(calendar);
        Integer inCome = CategoryArray.getIndex(CategoryEnum.Income.value());
        double sumRate = 0;

        Cursor cursor = mDatabase
                .rawQuery(String.format("select Category, sum(Amount) from tallies where TallyDate between date('%s') and date('%s') and Category != %s group by Category",
                        startDate,
                        endDate,
                        inCome),
                        null);
        try {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                ExpenseShare expenseShare = new ExpenseShare();
                expenseShare.setCategory(CategoryEnum.getEnumByIndex(cursor.getInt(0)));
                double amount = cursor.getDouble(1);
                expenseShare.setShareAmount(amount);
                if (cursor.isLast()) {
                    expenseShare.setShareRate(1 - sumRate);
                } else {
                    double rate = amount / total;
                    expenseShare.setShareRate(rate);
                    sumRate += rate;
                }
                expenseShares.add(expenseShare);
                cursor.moveToNext();
            }
        } finally {
            cursor.close();
        }
        return expenseShares;
    }

    public HashMap<CategoryEnum, Double> getCategoryAmount(Calendar calendar) {
        HashMap<CategoryEnum, Double> hashMap = new HashMap<>();
        String startDate = getStartOfMonth(calendar);
        String endDate = getEndOfMonth(calendar);
        Cursor cursor = mDatabase
                .rawQuery(String.format("select Category, sum(Amount) from tallies where TallyDate between date('%s') and date('%s') group by Category",
                        startDate,
                        endDate),
                        null);

        try {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                hashMap.put(CategoryEnum.getEnumByIndex(cursor.getInt(0)), cursor.getDouble(1));
                cursor.moveToNext();
            }
        } finally {
            cursor.close();
            return hashMap;
        }
    }

    private double getExpendOrIncome(Calendar calendar, Boolean isIncome) {
        String startDate = getStartOfMonth(calendar);
        String endDate = getEndOfMonth(calendar);
        Integer inCome = CategoryArray.getIndex(CategoryEnum.Income.value());
        String equalStr = isIncome
                ? "="
                : "!=";
        Cursor cursor = mDatabase
                .rawQuery(String.format("select sum(Amount) from tallies where TallyDate between date('%s') and date('%s') and Category %s %s",
                        startDate,
                        endDate,
                        equalStr,
                        inCome),
                        null);
        Double value = null;
        try {
            cursor.moveToFirst();
            if (!cursor.isAfterLast()) {
                value = cursor.getDouble(0);
            }
        } finally {
            cursor.close();
        }
        return value;
    }

    public static String getStartOfMonth(Calendar calendar) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Integer currentYear = calendar.get(Calendar.YEAR);
        Integer currentMonth = calendar.get(Calendar.MONTH);
        calendar.set(currentYear, currentMonth, 1);
        return dateFormat.format(calendar.getTime());
    }

    public static String getEndOfMonth(Calendar calendar) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Integer currentYear = calendar.get(Calendar.YEAR);
        Integer currentMonth = calendar.get(Calendar.MONTH);
        calendar.set(currentYear, currentMonth + 1, 1);
        calendar.add(Calendar.DATE, -1);
        return dateFormat.format(calendar.getTime());
    }

    public static String getStartOfYear(Calendar calendar) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Integer currentYear = calendar.get(Calendar.YEAR);
        calendar.set(currentYear, 1, 1);
        return dateFormat.format(calendar.getTime());
    }

    public static String getEndOfYear(Calendar calendar) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Integer currentYear = calendar.get(Calendar.YEAR);
        calendar.set(currentYear, 12, 31);
        return dateFormat.format(calendar.getTime());
    }

    private static ContentValues getContentValues(Tally tally) {
        ContentValues values = new ContentValues();
        values.put(TallyTable.Cols.UUID, tally.getId().toString());
        values.put(TallyTable.Cols.Description, tally.getDescription());
        values.put(TallyTable.Cols.DATE, tally.getDate().toString());
        values.put(TallyTable.Cols.Category, tally.getCategory());
        values.put(TallyTable.Cols.Amount, tally.getAmount());
        return values;
    }
}
