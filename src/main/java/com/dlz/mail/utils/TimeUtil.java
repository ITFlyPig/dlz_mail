package com.dlz.mail.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

public class TimeUtil {

    /*
   * 将时间戳转换为时间
   */
    public static String stampToDate(long time) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH mm ss");
        Date date = new Date(time);
        return simpleDateFormat.format(date);
    }
}
