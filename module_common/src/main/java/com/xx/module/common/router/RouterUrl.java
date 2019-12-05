package com.xx.module.common.router;

/**
 * @author someone
 * @date 2019-05-28
 */
public class RouterUrl {

    public static final String SM_SCHEME_TEMP = "xxvideo";
    public static final String SM_SCHEME = SM_SCHEME_TEMP + "://";
    public static final String SM_HOST = "xx.xx.com";

    public static final String ACTIVITY_MAIN = SM_SCHEME + SM_HOST + "/main";

    /**
     * 登陆
     */
    public static final String ACTIVITY_LOGIN_MAIN = SM_SCHEME + SM_HOST + "/login/main";
    public static final String ACTIVITY_PHONE_BIND = SM_SCHEME + SM_HOST + "/login/phone";
    public static final String ACTIVITY_LOGIN_PASSWORD = SM_SCHEME + SM_HOST + "/login/password";


    public static final String ACTIVITY_SETTING = SM_SCHEME + SM_HOST + "/setting";




    public static final String ACTIVITY_IMAGE_PAGE = SM_SCHEME + SM_HOST + "/image/page";

    public static final String ACTIVITY_IMAGE_PREVIEW = SM_SCHEME + SM_HOST + "/image/preview";
    public static final String ACTIVITY_WEB_VIEW = SM_SCHEME + SM_HOST + "/webview";
    /**
     * 视频分类设置
     */
    public static final String ACTIVITY_VIDEO_TAB_MANAGER = SM_SCHEME + SM_HOST + "/video/tab/manager";

    public static final String ACTIVITY_SHORT_VIDEO_MAIN = SM_SCHEME + SM_HOST + "/video/shortvideo/main";

    public static final String ACTIVITY_NEWS_DETAIL = SM_SCHEME + SM_HOST + "/news/detail";


    public static final String ACTIVITY_VIDEO_DETAIL = SM_SCHEME + SM_HOST + "/video/detail";


    public static final String ACTIVITY_ME_INFO_EDIT = SM_SCHEME + SM_HOST + "/me/info/edit";
    public static final String ACTIVITY_ME_HISTORY = SM_SCHEME + SM_HOST + "/me/history";
    public static final String ACTIVITY_ME_PRAISE = SM_SCHEME + SM_HOST + "/me/praise";
    public static final String ACTIVITY_ME_COLLECTION = SM_SCHEME + SM_HOST + "/me/collection";

    public static final String ACTIVITY_ME_EDIT_NICK_NAME = SM_SCHEME + SM_HOST + "/me/info/edit/nick";

}
