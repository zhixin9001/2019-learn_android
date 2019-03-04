package com.example.zhixin.tally.DataLayer;

import android.database.Cursor;
import android.database.CursorWrapper;

import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

import static com.example.zhixin.tally.DataLayer.TallyDbSchema.*;

/**
 * Created by ZhiXin on 2019/2/21.
 */

public class TallyCursorWrapper extends CursorWrapper {
    public TallyCursorWrapper(Cursor cursor) {
        super(cursor);
    }

    public Tally getTally() {
        String uuidStr = getString(getColumnIndex(TallyTable.Cols.UUID));
        String description = getString(getColumnIndex(TallyTable.Cols.Description));
        String date = getString(getColumnIndex(TallyTable.Cols.DATE));
        double amount = getDouble(getColumnIndex(TallyTable.Cols.Amount));
        Integer category = getInt(getColumnIndex(TallyTable.Cols.Category));

        Tally tally = new Tally(UUID.fromString(uuidStr));
        tally.setDescription(description);
        tally.setAmount(amount);
        tally.setCategory(category);

        tally.setDate(date);

        return tally;
    }
}
