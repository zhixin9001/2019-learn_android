package com.example.zhixin.tally.DataLayer;

/**
 * Created by ZhiXin on 2019/2/21.
 */

public class TallyDbSchema {
    public static final class TallyTable{
        public static final String NAME="tallies";

        public static final class Cols{
            public static final String UUID="uuid";
            public static final String Description="Description";
            public static final String DATE="TallyDate";
            public static final String Amount="Amount";
            public static final String Category="Category";
        }
    }
}
