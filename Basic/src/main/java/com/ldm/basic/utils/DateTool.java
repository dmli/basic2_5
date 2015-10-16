package com.ldm.basic.utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Created by ldm on 12-4-17.
 * 根据提供当前时间及给定的格式，返回时间字符串
 */
public class DateTool {

    /**
     * 获取当前的时间字符串
     *
     * @param format yy-MM-dd HH
     * @return 字符串形式的当前时间
     */
    public static String getNowDate(String format) {
        return new SimpleDateFormat(format, Locale.US).format(new Date());
    }

    /**
     * 将给定时间转换成字符串
     *
     * @param date   时间戳
     * @param format 格式
     * @return str
     */
    public static String format(long date, String format) {
        return new SimpleDateFormat(format, Locale.CHINESE).format(new Date(date));
    }

    /**
     * 将给定时间转换成字符串
     *
     * @param date   long类型的字符串时间戳
     * @param format 格式
     * @return str
     */
    public static String format(String date, String format) {
        long s1 = 0;
        try {
            s1 = Long.parseLong(date);
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        if (s1 <= 0) {
            return null;
        }
        return format(s1, format);
    }

    /**
     * 根据日期获取年龄
     *
     * @param year 1990
     * @return int;年龄
     */
    public static int getAge(String year) {
        int iAge;
        Calendar cal = Calendar.getInstance();
        int iCurrYear = cal.get(Calendar.YEAR);
        iAge = iCurrYear - Integer.valueOf(year);
        return iAge;
    }

    /**
     * 判断是否为合法的日期时间字符串
     *
     * @param strInput   时间字符串
     * @param dateFormat 格式
     * @return boolean;符合为true,不符合为false
     */
    public static boolean isDate(String strInput, String dateFormat) {
        boolean result = false;
        if (strInput != null) {
            SimpleDateFormat formatter = new SimpleDateFormat(dateFormat, Locale.CHINESE);
            formatter.setLenient(false);
            try {
                formatter.format(formatter.parse(strInput));
                result = true;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    /**
     * 验证小于当前日期 是否有效
     *
     * @param iYear  待验证日期(年)
     * @param iMonth 待验证日期(月 1-12)
     * @param iDate  待验证日期(日)
     * @return 符合为true, 不符合为false
     */
    public static boolean isDate(int iYear, int iMonth, int iDate) {
        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);
        int datePerMonth;
        if (iYear < 1900 || iYear >= year) {
            return false;
        }
        if (iMonth < 1 || iMonth > 12) {
            return false;
        }
        switch (iMonth) {
            case 4:
            case 6:
            case 9:
            case 11:
                datePerMonth = 30;
                break;
            case 2:
                boolean dm = ((iYear % 4 == 0 && iYear % 100 != 0) || (iYear % 400 == 0)) && (iYear > 1900 && iYear < year);
                datePerMonth = dm ? 29 : 28;
                break;
            default:
                datePerMonth = 31;
        }
        return (iDate >= 1) && (iDate <= datePerMonth);
    }

    /**
     * 将数字月份转成对应的大写月份 （返回值不带 月 字样）
     *
     * @param month 月份  例：01 ：一   02：二
     * @return 大写月份  （没有对应月份将返回null）
     */
    public static String monthToUppercase(String month) {
        String result = null;
        if ("1".equals(month) || "01".equals(month)) {
            result = "一";
        } else if ("2".equals(month) || "02".equals(month)) {
            result = "二";
        } else if ("3".equals(month) || "03".equals(month)) {
            result = "三";
        } else if ("4".equals(month) || "04".equals(month)) {
            result = "四";
        } else if ("5".equals(month) || "05".equals(month)) {
            result = "五";
        } else if ("6".equals(month) || "06".equals(month)) {
            result = "六";
        } else if ("7".equals(month) || "07".equals(month)) {
            result = "七";
        } else if ("8".equals(month) || "08".equals(month)) {
            result = "八";
        } else if ("9".equals(month) || "09".equals(month)) {
            result = "九";
        } else if ("10".equals(month)) {
            result = "十";
        } else if ("11".equals(month)) {
            result = "十一";
        } else if ("12".equals(month)) {
            result = "十二";
        }
        return result;
    }

    /**
     * 将数字月份转成对应的大写月份 （返回值不带 月 字样）
     *
     * @param month 月份  例：1 ：一   2：二
     * @return 大写月份  （没有对应月份将返回null）
     */
    public static String monthToUppercase(int month) {
        return monthToUppercase("" + month);
    }
}
