package com.example.zhixin.tally;

import android.widget.Toast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Created by ZhiXin on 2019/2/28.
 */

public class CalendarHelper {
    private static Calendar calendar = Calendar.getInstance();

    public static Calendar getCalendar(String string, String format) throws ParseException {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat(format);
        calendar.setTime(dateFormat.parse(string));
        return calendar;
    }

    public static String getDateString(String format) {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat(format);
        return dateFormat.format(calendar.getTime());
    }

    public static String getDateString(Calendar calendar, String format) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(format);
        return dateFormat.format(calendar.getTime());
    }
}
