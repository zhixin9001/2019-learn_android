package com.example.zhixin.tally.DataLayer;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.zhixin.tally.DataLayer.TallyDbSchema.TallyTable;

/**
 * Created by ZhiXin on 2019/2/21.
 */

public class TallyBaseHelper extends SQLiteOpenHelper {
    private static final int VERSION = 1;
    private static final String DATABASE_NAME = "tallyBase.db";

    public TallyBaseHelper(Context context){
        super(context,DATABASE_NAME,null,VERSION);
    }
    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL("create table " + TallyTable.NAME + "(" +
                " _id integer primary key autoincrement, " +
                TallyTable.Cols.UUID + ", " +
                TallyTable.Cols.Description + ", " +
                TallyTable.Cols.DATE + ", " +
                TallyTable.Cols.Amount + ","+
                TallyTable.Cols.Category + ")"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
}
