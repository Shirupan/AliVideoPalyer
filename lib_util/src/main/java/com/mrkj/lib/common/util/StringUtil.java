package com.mrkj.lib.common.util;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.mrkj.lib.common.view.SmToast;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 字符串处理工具类
 *
 * @author rosen
 * 2014-04-08
 */
public class StringUtil {
    public static final String DEFAULT_DATE_PATTERN = "yyyy-MM-dd";
    public static final String DEFAULT_DATETIME_PATTERN = "yyyy-MM-dd HH:mm:ss";
    public static final String DEFAULT_TIME_PATTERN = "HH:mm:ss";
    public final static String EMPTY = "";

    /**
     * 格式化日期字符串
     *
     * @param date
     * @param pattern
     * @return
     */
    public static String formatDate(Date date, String pattern) {
        SimpleDateFormat format = new SimpleDateFormat(pattern);
        return format.format(date);
    }


    /**
     * 获取当前时间 格式为yyyy-MM-dd 例如2011-07-08
     *
     * @return
     */
    public static String getDate() {
        return formatDate(new Date(), DEFAULT_DATE_PATTERN);
    }


    /**
     * 获取当前时间 格式为yyyy-MM-dd hh:mm:ss 例如2011-11-30 16:06:54
     *
     * @return
     */
    public static String getDateTime() {
        return formatDate(new Date(), DEFAULT_DATETIME_PATTERN);
    }


    public static boolean isEmpty(String str) {
        return str == null || str.length() == 0;
    }

    /**
     * 1. 处理特殊字符 2. 去除后缀名带来的文件浏览器的视图凌乱(特别是图片更需要如此类似处理，否则有的手机打开图库，全是我们的缓存图片)
     *
     * @param url
     * @return
     */
    public static String replaceUrlWithPlus(String url) {
        if (url != null) {
            return url.replaceAll("http://(.)*?/", "")
                    .replaceAll("[.:/,%?&=]", "+").replaceAll("[+]+", "+");
        }
        return null;
    }

    /**
     * 验证手机号码
     *
     * @param mobiles
     * @return 验证成功返回true，验证失败返回false
     */
    public static boolean isMobileNO(String mobiles) {
        if (TextUtils.isEmpty(mobiles)) {
            return false;
        }
        mobiles = mobiles.trim();
        if (!TextUtils.isDigitsOnly(mobiles)) {
            return false;
        }
       /* if (!mobiles.startsWith("1")) {
            return false;
        }*/
        return mobiles.length() > 5;
        /*  Pattern p = Pattern
                .compile("^((13[0-9])|(14[5,7])|(15[^4,\\D])|(17[0-9])|(18[^4,\\D]))\\d{8}$");
        Matcher m = p.matcher(mobiles);
        return m.matches();*/
    }

    public static boolean checkMobilePhoneWithToast(Context context, String phone) {
        if (TextUtils.isEmpty(phone)) {
            SmToast.show(context, "请输入手机号码");
            return false;
        }
        if (!TextUtils.isDigitsOnly(phone)) {
            SmToast.show(context, "请输入有效手机号码");
            return false;
        }
        if (phone.length() < 5) {
            SmToast.show(context, "请输入正确手机号码");
            return false;
        }
        return true;
    }

    public static boolean checkInputNameWithToast(Context context, String name) {
        if (TextUtils.isEmpty(name)) {
            SmToast.show(context, "请输入姓名");
            return false;
        }
        if (TextUtils.isDigitsOnly(name)) {
            SmToast.show(context, "姓名不能为纯数字");
            return false;
        }

        if (name.length() < 2) {
            SmToast.show(context, "请输入2字以上的姓名");
            return false;
        }
        return true;
    }

    /**
     * 验证固定电话号码
     *
     * @param phone 电话号码，格式：国家（地区）电话代码 + 区号（城市代码） + 电话号码，如：+8602085588447
     *              <p><b>国家（地区） 代码 ：</b>标识电话号码的国家（地区）的标准国家（地区）代码。它包含从 0 到 9 的一位或多位数字，
     *              数字之后是空格分隔的国家（地区）代码。</p>
     *              <p><b>区号（城市代码）：</b>这可能包含一个或多个从 0 到 9 的数字，地区或城市代码放在圆括号——
     *              对不使用地区或城市代码的国家（地区），则省略该组件。</p>
     *              <p><b>电话号码：</b>这包含从 0 到 9 的一个或多个数字 </p>
     * @return 验证成功返回true，验证失败返回false
     */
    public static boolean checkPhone(String phone) {
        String regex = "(\\+\\d+)?(\\d{3,4}\\-?)?\\d{7,8}$";
        return Pattern.matches(regex, phone);
    }

    /**
     * 验证邮箱地址
     *
     * @param email
     * @return
     */
    public static boolean isEmail(String email) {
        String str = "^([a-zA-Z0-9]*[-_]?[a-zA-Z0-9]+)*@([a-zA-Z0-9]*[-_]?[a-zA-Z0-9]+)+[\\.][A-Za-z]{2,3}([\\.][A-Za-z]{2})?$";
        Pattern p = PatternPool.getPattern(str);
        Matcher m = p.matcher(email);
        return m.matches();
    }

    /**
     * 验证身份证号码
     *
     * @param idNum
     * @return
     */
    public static boolean isIDNum(String idNum) {
        Pattern p1 = PatternPool.getPattern("^[1-9]\\d{7}((0\\d)|(1[0-2]))(([0|1|2]\\d)|3[0-1])\\d{3}$");
        Pattern p2 = PatternPool.getPattern("^[1-9]\\d{5}[1-9]\\d{3}((0\\d)|(1[0-2]))(([0|1|2]\\d)|3[0-1])((\\d{4})|\\d{3}[A-Z])$");
        Matcher m1 = p1.matcher(idNum);
        Matcher m2 = p2.matcher(idNum);
        return m2.matches() || m1.matches();
    }


    /**
     * 替换掉所有HTML标签
     *
     * @return
     */
    public static String replaceAllHtml(String str) {
        if (str == null) {
            return null;
        }
        String regularExpression = "<[^>]*>";
        return str.replaceAll(regularExpression, "");
    }


    public static boolean isHtml(String content) {
        if (TextUtils.isEmpty(content)) {
            return false;
        }
        return content.contains("</p>") || content.contains("</html>");
    }

    private static Random randGen = null;
    private static char[] numbersAndLetters = null;

    /**
     * 个位数前面补齐0
     *
     * @param i
     * @return
     */
    public static String numberComplement(int i) {
        return String.format(Locale.CHINESE, "%02d", i);
    }

    /**
     * 产生随机字符串
     * 调用此方法randomString(int),int是字符串的长度，即可产生指定长度的随机字符串。
     */
    public static final String randomString(int length) {
        if (length < 1) {
            return null;
        }
        if (randGen == null) {
            randGen = new Random();
            numbersAndLetters = ("0123456789abcdefghijklmnopqrstuvwxyz0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ").toCharArray();
        }
        char[] randBuffer = new char[length];
        for (int i = 0; i < randBuffer.length; i++) {
            randBuffer[i] = numbersAndLetters[randGen.nextInt(71)];
        }
        return new String(randBuffer);
    }

    public static String md5(String string) {
        byte[] hash;
        try {
            hash = MessageDigest.getInstance("MD5").digest(string.getBytes("UTF-8"));
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Huh, MD5 should be supported?", e);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Huh, UTF-8 should be supported?", e);
        }

        StringBuilder hex = new StringBuilder(hash.length * 2);
        for (byte b : hash) {
            if ((b & 0xFF) < 0x10) {
                hex.append("0");
            }
            hex.append(Integer.toHexString(b & 0xFF));
        }

        return hex.toString();
    }


    /**
     * 验证多少到多少个汉字
     *
     * @param str
     * @param start 范围起始
     * @param end   范围结束
     * @return
     */
    public static boolean chinaSimple(String str, int start, int end) {
        if (TextUtils.isEmpty(str) || start < 0 || end > str.length() || start > end) {
            return false;
        }
        // 去除前后的空格
        String strs = str.replace("/(^\\s+)|(\\s+$)/g", "");
        //
        String regEx = "/^[\\u4e00-\\u9fa5]{" + start + ", " + end + "}$/";
        Pattern p = PatternPool.getPattern(regEx);
        return p.matcher(strs).find();
    }

    public static boolean nickNameRegExMatches(String str, int start, int end) {
        if (TextUtils.isEmpty(str) || start < 0
                || start > str.length() || end < str.length() || start > end) {
            return false;
        }
        // 去除前后的空格
        String strs = str.replace("/(^\\s+)|(\\s+$)/g", "");
        Log.d("StringUtil", "strs " + strs);
        //
        // String all  = "^[\\u4E00-\\u9FA5\\uF900-\\uFA2D]{2,10}$";//{2,10}表示字符的长度是2-10
        String all = "^[\\u4E00-\\u9FA5\\uF900-\\uFA2D]{" + start + "," + end + "}$";//{2,10}表示字符的长度是2-10
        Log.d("StringUtil", "all " + all);
        boolean tf = false;
        try {
            Pattern pattern = PatternPool.getPattern(all);
            tf = pattern.matcher(strs).matches();
        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.d("StringUtil", "tf " + tf);
        return tf;
    }


    /**
     * 判断是否有数组数据：不为null、“”、“0”、“null”
     *
     * @return
     */
    public static boolean hasDatas(String content) {
        if (null == content) {
            return false;
        }
        if (content.equals("")) {
            return false;
        }
        if (content.equals("1")) {
            return false;
        }
        if (content.equals("null")) {
            return false;
        }
        if (content.equals("0")) {
            return false;
        }
        return (content.startsWith("[") && content.endsWith("]"));
    }

    /**
     * 将系统emoji表情转换成十进制字符串
     *
     * @param s
     * @return
     */
    public static String converStringWithEmojiToMsg(String s) {
        if (TextUtils.isEmpty(s)) {
            return "";
        }
        int a = 0;
        String aa = "";
        String sss = "";
        int b = 0;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            //判断是否为表情
            if (isEmojiCharacter(c)) {
                b++;
                int i1 = c - 48;//转为ascill
                a += i1;
                if (b % 2 == 0) { //emoji占用两个长度
                    aa = "[e]" + (a + 16419) + "[/e]";
                    a = 0;
                    b = 0;
                    sss = sss + aa;
                }
            } else {
                sss = sss + Character.toString(c);
            }
        }
        return sss;
    }

    /**
     * 过滤emoji
     *
     * @param s
     * @return
     */
    public static String emojiFilter(String s) {
        if (TextUtils.isEmpty(s)) {
            return s;
        }
        int a = 0;
        String aa = "";
        String sss = "";
        int b = 0;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            //判断是否为表情
            if (isEmojiCharacter(c)) {
                b++;
                int i1 = c - 48;//转为ascill
                a += i1;
                if (b % 2 == 0) { //emoji占用两个长度
                    aa = "";
                    a = 0;
                    b = 0;
                    sss = sss + aa;
                }
            } else {
                sss = sss + Character.toString(c);
            }
        }
        return sss;
    }

    /**
     * 判断是不是emoji表情
     *
     * @param codePoint
     * @return
     */
    private static boolean isEmojiCharacter(char codePoint) {
        return !(codePoint == 0x0 || codePoint == 0x9 || codePoint == 0xA || codePoint == 0xD || codePoint >= 0x20 && codePoint <= 0xD7FF || codePoint >= 0xE000 && codePoint <= 0xFFFD);
    }


    public static int integerValueOf(String str, int defaultValue) {
        if (TextUtils.isEmpty(str)) {
            return defaultValue;
        }
        if (!TextUtils.isEmpty(str) && TextUtils.isDigitsOnly(str)) {
            long temp = Long.valueOf(str);
            if (temp < Integer.MAX_VALUE) {
                return (int) temp;
            }
        }
        return defaultValue;
    }

    public static long longValueOf(String str, long defaultValue) {
        if (TextUtils.isEmpty(str)) {
            return defaultValue;
        }
        if (!TextUtils.isEmpty(str) && TextUtils.isDigitsOnly(str)) {
            return Long.valueOf(str);
        }
        return defaultValue;
    }

    public static double doubleValueOf(String str, double defaultValue) {
        if (TextUtils.isEmpty(str)) {
            return defaultValue;
        }
        if (!TextUtils.isEmpty(str) && TextUtils.isDigitsOnly(str)) {
            return Double.valueOf(str);
        }
        return defaultValue;
    }

    public static String ToDBC(String input) {
        char[] c = input.toCharArray();
        for (int i = 0; i < c.length; i++) {
            if (c[i] == 12288) {
                c[i] = (char) 32;
                continue;
            }
            if (c[i] > 65280 && c[i] < 65375) {
                c[i] = (char) (c[i] - 65248);
            }
        }

        return new String(c);

    }

    /**
     * 对字符串处理:将指定位置到指定位置的字符以星号代替
     *
     * @param content 传入的字符串
     * @param begin   开始位置
     * @param end     结束位置
     * @return
     */
    public static String getStarString(String content, int begin, int end) {

        if (begin >= content.length() || begin < 0) {
            return content;
        }
        if (end >= content.length() || end < 0) {
            return content;
        }
        if (begin >= end) {
            return content;
        }
        String starStr = "";
        for (int i = begin; i < end; i++) {
            starStr = starStr + "*";
        }
        return content.substring(0, begin) + starStr + content.substring(end, content.length());

    }

    /**
     * 对字符加星号处理：除前面几位和后面几位外，其他的字符以星号代替
     *
     * @param content  传入的字符串
     * @param frontNum 保留前面字符的位数
     * @param endNum   保留后面字符的位数
     * @return 带星号的字符串
     */

    public static String getStarString2(String content, int frontNum, int endNum) {
        if (frontNum >= content.length() || frontNum < 0) {
            return content;
        }
        if (endNum >= content.length() || endNum < 0) {
            return content;
        }
        if (frontNum + endNum >= content.length()) {
            return content;
        }
        String starStr = "";
        for (int i = 0; i < (content.length() - frontNum - endNum); i++) {
            starStr = starStr + "*";
        }
        return content.substring(0, frontNum) + starStr
                + content.substring(content.length() - endNum, content.length());

    }

    public static String autoGenericCode(String code, int num) {
        String result = "";
        result = String.format("%0" + num + "d", StringUtil.integerValueOf(code, 0));
        return result;
    }

    public static String autoGenericCode(String code) {
        return autoGenericCode(code, 2);
    }

    public static String addZero(int number) {
        if (number >= 0 && number < 10) {
            return "0" + number;
        } else {
            return "" + number;
        }
    }

    /**
     * 解析出url请求的路径，包括页面
     *
     * @param strURL url地址
     * @return url路径
     */
    public static String urlPage(String strURL) {
        String strPage = null;
        String[] arrSplit = null;

        strURL = strURL.trim().toLowerCase();

        arrSplit = strURL.split("[?]");
        if (strURL.length() > 0) {
            if (arrSplit.length > 1) {
                if (arrSplit[0] != null) {
                    strPage = arrSplit[0];
                }
            }
        }

        return strPage;
    }

    /**
     * 去掉url中的路径，留下请求参数部分
     *
     * @param strURL url地址
     * @return url请求参数部分
     */
    private static String truncateUrlPage(String strURL) {
        String strAllParam = null;
        String[] arrSplit = null;

        strURL = strURL.trim();

        arrSplit = strURL.split("[?]");
        if (strURL.length() > 1) {
            if (arrSplit.length > 1) {
                if (arrSplit[1] != null) {
                    strAllParam = arrSplit[1];
                }
            }
        }

        return strAllParam;
    }

    /**
     * 解析出url参数中的键值对
     * 如 "index.jsp?Action=del&id=123"，解析出Action:del,id:123存入map中
     *
     * @param URL url地址
     * @return url请求参数部分
     */
    public static Map<String, String> urlRequest(String URL) {
        Map<String, String> mapRequest;


        String strUrlParam = truncateUrlPage(URL);
        if (strUrlParam == null) {
            return null;
        }
        mapRequest = new HashMap<>();
        String[] arrSplit;
        //每个键值为一组 www.2cto.com
        arrSplit = strUrlParam.split("[&]");
        for (String strSplit : arrSplit) {
            String[] arrSplitEqual = null;
            arrSplitEqual = strSplit.split("[=]");

            //解析出键值
            if (arrSplitEqual.length > 1) {
                //正确解析
                mapRequest.put(arrSplitEqual[0], arrSplitEqual[1]);

            } else {
                if (!TextUtils.equals(arrSplitEqual[0], "")) {
                    //只有参数没有值，不加入
                    mapRequest.put(arrSplitEqual[0], "");
                }
            }
        }
        return mapRequest;
    }

    /**
     * 从Url中获取文件名
     *
     * @param url
     * @return
     */
    public static String getNameFromUrl(String url) {
        if (TextUtils.isEmpty(url)) {
            return "";
        }
        int index = url.lastIndexOf("/");
        if (index == -1) {
            return url;
        }
        return url.substring(index + 1, url.length());
    }

    /**
     * 保留小数点后几位
     *
     * @param data
     * @param number
     * @return
     */
    public static String getDecimalPointString(double data, int number) {
        String format = "%." + number + "f";
        return String.format(Locale.CHINESE, format, data);
    }

    /**
     * 保留两位小数点并且不四舍五入
     *
     * @param data
     * @return
     */
    public static String getDecimalFormat(double data) {
        String format = "%.2f";
        return String.format(format, data);
    }


    private static int HOUR = 60 * 60;

    /**
     * 根据秒数返回 “00：00：00”的时间长度
     *
     * @param seconded
     * @return
     */
    public static String getTimeBySeconded(int seconded) {
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

    public static String getTimeBySeconded(long seconded) {
        if (seconded == 0) {
            return "00:00";
        }
        long hour = seconded / (60 * 60);
        long min = (seconded - (hour * 60 * 60)) / 60;
        long lastSeconded = seconded - (hour * 60 * 60) - min * 60;
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

    /**
     * 个数显示转换
     *
     * @param count
     * @return
     */
    public static String formatCount(Integer count) {
        if (count == null) {
            return "0";
        }
        if (count < 10000) {
            return count + "";
        } else {
            return getDecimalPointString(((double) count) / 10000, 1) + "w";
        }
    }

    /*
     * 中文转unicode编码
     */
    public static String gbEncoding(final String gbString) {
        char[] utfBytes = gbString.toCharArray();
        String unicodeBytes = "";
        for (int i = 0; i < utfBytes.length; i++) {
            String hexB = Integer.toHexString(utfBytes[i]);
            if (hexB.length() <= 2) {
                hexB = "00" + hexB;
            }
            unicodeBytes = unicodeBytes + "\\u" + hexB;
        }
        return unicodeBytes;
    }

    /**
     * 此方法能正确转换包括符号在内的文字信息。其他网上的方法可能会出现 比如818e找不到对应的字符
     *
     * @param theString
     * @return
     */
    public static String decodeUnicode(final String theString) {
        char aChar;
        int len = theString.length();
        StringBuilder outBuffer = new StringBuilder(len);
        for (int x = 0; x < len; ) {
            aChar = theString.charAt(x++);
            if (aChar == '\\') {
                aChar = theString.charAt(x++);
                if (aChar == 'u') {
                    // Read the xxxx
                    int value = 0;
                    for (int i = 0; i < 4; i++) {
                        aChar = theString.charAt(x++);
                        switch (aChar) {
                            case '0':
                            case '1':
                            case '2':
                            case '3':
                            case '4':
                            case '5':
                            case '6':
                            case '7':
                            case '8':
                            case '9':
                                value = (value << 4) + aChar - '0';
                                break;
                            case 'a':
                            case 'b':
                            case 'c':
                            case 'd':
                            case 'e':
                            case 'f':
                                value = (value << 4) + 10 + aChar - 'a';
                                break;
                            case 'A':
                            case 'B':
                            case 'C':
                            case 'D':
                            case 'E':
                            case 'F':
                                value = (value << 4) + 10 + aChar - 'A';
                                break;
                            default:
                                throw new IllegalArgumentException(
                                        "Malformed   \\uxxxx   encoding.");
                        }

                    }
                    outBuffer.append((char) value);
                } else {
                    if (aChar == 't')
                        aChar = '\t';
                    else if (aChar == 'r')
                        aChar = '\r';
                    else if (aChar == 'n')
                        aChar = '\n';
                    else if (aChar == 'f')
                        aChar = '\f';
                    outBuffer.append(aChar);
                }
            } else
                outBuffer.append(aChar);
        }
        return outBuffer.toString();
    }

    /**
     * 字符串等字符分组
     *
     * @param content
     * @param ems
     * @return
     */
    public static List<String> getStringArray(String content, int ems) {
        if (TextUtils.isEmpty(content)) {
            return Collections.emptyList();
        } else {
            List<String> list = new ArrayList<>();
            int start = 0;
            int last = content.length();
            while (start < last) {
                int lastIndex = start + ems;
                if (last < lastIndex) {
                    lastIndex = last;
                }
                String data = content.substring(start, lastIndex);
                list.add(data);
                start = lastIndex ;
            }
            return list;
        }
    }
}
