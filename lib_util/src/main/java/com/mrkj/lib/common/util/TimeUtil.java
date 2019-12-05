package com.mrkj.lib.common.util;

import android.text.TextUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by Administrator on 2016-05-04.
 */
public class TimeUtil {

    private static final long SECONDED_FROM_MILLIS = 1000;
    private static final long MINUTE_FROM_MILLIS = 60 * SECONDED_FROM_MILLIS;
    private static final long HOUR_FROM_MILLIS = 60 * MINUTE_FROM_MILLIS;
    private static final long DAY_FROM_MILLIS = 24 * HOUR_FROM_MILLIS;

    private static SimpleDateFormat formatter;
    private static SimpleDateFormat formatter2;
    private static SimpleDateFormat formatter3;

    static {
        formatter = (SimpleDateFormat) SimpleDateFormat.getDateInstance();
        formatter.applyPattern("yyyy-MM-dd HH:mm:ss");
        formatter2 = (SimpleDateFormat) SimpleDateFormat.getDateInstance();
        formatter2.applyPattern("yyyy.MM.dd HH:mm:ss");
        formatter3 = (SimpleDateFormat) SimpleDateFormat.getDateInstance();
        formatter3.applyPattern("yyyy-MM-dd");
    }

    public static String getTimeForNow(String date) {
        return getTimeForNow(date, null);
    }

    /**
     * @param date
     * @param temple 输出正常时间的显示格式
     * @return 与现在的时间差（如：2小时前）
     */
    public static String getTimeForNow(String date, String temple) {
        if (date == null) {
            return null;
        }
        boolean isOnlyYYYYMMDD = false;
        Date msgDate;
        Date nowDate = new Date(System.currentTimeMillis());
        if (TextUtils.isEmpty(temple)) {
            try {
                msgDate = formatter.parse(date);
                isOnlyYYYYMMDD = false;
            } catch (ParseException e) {
                //  e.printStackTrace();
                try {
                    msgDate = formatter3.parse(date);
                    isOnlyYYYYMMDD = true;
                } catch (ParseException e2) {
                    isOnlyYYYYMMDD = false;
                    return date;
                }
            }
        } else {
            SimpleDateFormat dateFormat = (SimpleDateFormat) SimpleDateFormat.getDateInstance();
            dateFormat.applyPattern(temple);
            try {
                msgDate = dateFormat.parse(date);
            } catch (ParseException e2) {
                return date;
            }
        }

        Calendar cEnd = Calendar.getInstance();
        Calendar cNow = Calendar.getInstance();

        cEnd.setTime(msgDate);
        cNow.setTime(nowDate);
        //手机上的时间，时区可能不是东八区，但是服务器给的时间是东八区，所以要转换一下
        setTimeToGMT8(cNow);
        int hour_cEnd = cEnd.get(Calendar.HOUR_OF_DAY);
        String hhMM = numberFormat(hour_cEnd) + ":" + numberFormat(cEnd.get(Calendar.MINUTE));
        long dtime = nowDate.getTime() - msgDate.getTime();
        Calendar cDtime = Calendar.getInstance();
        cDtime.setTimeInMillis(dtime);

        int dyear = cEnd.get(Calendar.YEAR) % 4 == 0 ? 366 : 365;
        long dy = dtime / (DAY_FROM_MILLIS * dyear);
        if (dy > 0) {
            return dy + "年前";
        }
        long dm = dtime / (30 * DAY_FROM_MILLIS);
        if (dm > 0) {
            return dm + "月前";
        }
        long dd = (dtime / DAY_FROM_MILLIS);
        if (isOnlyYYYYMMDD) {
            if (dd == 0) {
                return "今天";
            } else if (dd == 1) {
                return "昨天";
            } else {
                return dd + "天前";
            }
        } else {
            if (dd > 0) {
                return dd + "天前 " + hhMM;
            }
            long dhour = dtime / HOUR_FROM_MILLIS;
            if (dhour > 0) {
                return dhour + "小时前";
            }
            long dmin = dtime / MINUTE_FROM_MILLIS;
            if (dmin > 0) {
                return dmin + "分钟前";
            } else {
                return "刚刚";
            }
        }

    }

    private static String numberFormat(int number) {
        if (number < 10 && number >= 0) {
            return "0" + number;
        } else {
            return "" + number;
        }
    }

    private static void setTimeToGMT8(Calendar cNow) {
        //东八区和当前时区的时差
        int offSet = TimeZone.getTimeZone("GMT+8").getRawOffset() - cNow.getTimeZone().getRawOffset();
        cNow.setTime(new Date(cNow.getTime().getTime() + offSet));

    }


    public static String getMonth(int mon) {
        switch (mon) {
            case 0:
                return "Jan";
            case 1:
                return "Feb";
            case 2:
                return "Mar";
            case 3:
                return "Apr";
            case 4:
                return "May";
            case 5:
                return "Jun";
            case 6:
                return "Jul";
            case 7:
                return "Aug";
            case 8:
                return "Sep";
            case 9:
                return "Oct";
            case 10:
                return "Nov";
            case 11:
                return "Dev";
        }
        return null;
    }

    public static boolean checkToday(String createtime) {
        Date date;
        try {
            date = formatter.parse(createtime);
        } catch (ParseException e) {
            e.printStackTrace();
            return false;
        }

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        Calendar nowC = Calendar.getInstance();
        nowC.setTime(new Date(System.currentTimeMillis()));
        return year == nowC.get(Calendar.YEAR)
                && month == nowC.get(Calendar.MONTH)
                && day == nowC.get(Calendar.DAY_OF_MONTH);
    }


    private static SimpleDateFormat formatterNoTime;
    private static SimpleDateFormat formatterNoTime2;

    static {
        formatterNoTime = (SimpleDateFormat) SimpleDateFormat.getDateInstance();
        formatterNoTime.applyPattern("yyyy-MM-dd");
        formatterNoTime2 = (SimpleDateFormat) SimpleDateFormat.getDateInstance();
        formatterNoTime2.applyPattern("yyyy.MM.dd");
    }

    /**
     * 支持 yyyy.MM.dd 和yyyy-MM-dd格式时间转换成millsLong
     *
     * @param timeStr
     * @param dayStart 取值当天起始还是结束
     * @return
     */
    public static long getDateInMills(String timeStr, boolean dayStart) {
        Date date = null;
        try {
            date = formatterNoTime.parse(timeStr);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        if (date == null) {
            try {
                date = formatterNoTime2.parse(timeStr);
            } catch (ParseException e1) {
                e1.printStackTrace();
            }
        }
        if (date != null) {
            if (!dayStart) {
                return date.getTime() + 24 * 60 * 60 * 1000 - 1;
            }
            return date.getTime();
        } else {
            return 0;
        }

    }

    /**
     * 以周一为第一天计算
     *
     * @param position
     * @return
     */
    public static String getdayOfWeek(int position) {
        String result = "";
        switch (position) {
            case 2:
                result = "周一";
                break;
            case 3:
                result = "周二";
                break;
            case 4:
                result = "周三";
                break;
            case 5:
                result = "周四";
                break;
            case 6:
                result = "周五";
                break;
            case 7:
                result = "周六";
                break;
            case 1:
                result = "周日";
                break;
            default:
                result = "--";
        }
        return result;
    }

    /**
     * 根据时间长度格式化成 00:00:00 时间格式
     *
     * @param timelong 秒
     * @return
     */
    public static String getFormatTime(long timelong) {
        if (timelong == 0) {
            return "";
        }
        String time = "";
        int hour = (int) (timelong / 60 / 60);
        if (hour != 0) {
            time = StringUtil.autoGenericCode(hour + "") + ":";
            timelong -= hour * 60 * 60;
        }
        int min = (int) (timelong / 60);
        if (min != 0) {
            time = time + StringUtil.autoGenericCode(min + "") + ":";
            timelong -= hour * 60;
        } else {
            time = "00:";
        }
        int seconded = (int) (timelong);
        time = time + StringUtil.autoGenericCode(seconded + "");
        return time;

    }

    /**
     * 视频时间长度
     *
     * @param seconded
     * @return
     */
    public static String getVideoFormatTime(int seconded) {
        if (seconded == 0) {
            return "00:00";
        }
        int hour = seconded / (60 * 60);
        int min = (seconded - (hour * 60 * 60)) / 60;
        int lastSeconded = seconded - (hour * 60 * 60) - min * 60;
        StringBuilder sb = new StringBuilder();
        if (hour > 0) {
            sb.append(autoGenericCode(hour + ""));
            sb.append(":");
        }
        if (min > 0) {
            sb.append(autoGenericCode(min + ""));
            sb.append(":");
        } else {
            sb.append("00:");
        }
        sb.append(autoGenericCode(lastSeconded + ""));
        return sb.toString();
    }

    public static String autoGenericCode(String code) {
        return autoGenericCode(code, 2);
    }

    public static String autoGenericCode(String code, int num) {
        String result = "";
        result = String.format("%0" + num + "d", StringUtil.integerValueOf(code, 0));
        return result;
    }

    /**
     * 重置日历为当天开始时间
     *
     * @param calendar
     */
    public static void calendarFromDayBegin(Calendar calendar) {
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
    }
}
