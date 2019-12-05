package com.xx.module.common;

import android.content.Context;
import android.content.Intent;

import com.xx.module.common.router.RouterParams;

/**
 * @author someone
 * @date 2019-05-28
 */
public class BaseConfig {
    public static final String DEFAULT_CHANNEL = "office";

    //   public static String GET_URL_NEW = "http://192.168.1.37:8080/";

    /**
     * 以下内容通过manifest文件获取
     */
    public static String QQ_APP_ID = "";
    public static String QQ_APP_SECRET = "";
    public static String WEIBO_KEY = "";
    public static String WEIBO_SECRET = "";
    public static String WEIBO_URL = "http://www.sina.com";
    public static String WX_APP_ID = "";
    public static String WX_APP_SECRET = "";

    public static final String LongInName_SINA = "xlsm__";// 新浪账号前缀
    public static final String LongInName_TX = "txsm__";// 腾讯账号前缀
    public static final String LongInName_WX = "wxsm__";// 微信账户前缀

    //客服QQ
    public static final String CUSTOM_QQ = "";

    public static final int DEFAULT_SAVOR_SIZE = 1500;

    /**
     * 不能包含特殊字符，可以是汉字，数字和字母
     */
    public static final String ZHENGZE_NICK_NAME = "^[\\u4E00-\\u9FA5A-Za-z0-9_]+$";

    /**
     * 登录用户信息更新发送广播
     *
     * @param context
     */
    public static void sendUserInfoChangeBroadcast(Context context) {
        Intent intent = new Intent(RouterParams.Broadcast.ACTION_USER_INFO_REFRESH);
        context.sendBroadcast(intent);
    }
}
