package com.xx.module.common.router;

/**
 * @author someone
 * @date 2019-05-28
 */
public class RouterParams {
    public static class GlobalBroadCast {

        /**
         * 未读消息数
         */
        public static final String PUSH_MESSAGE = "com.mrkj.global.PUSH_MESSAGE";

    }

    public static class VideoView {
        public static final String DATA = "video_data";

        public static final String VID = "video_id";

        public static final String VIEW_TYPE = "view_type";

        public static final String VIDEO_LIST = "video_data";

        public static final String VIDEO_LIST_POSITION = "video_position";
    }

    public static class WebView {
        public static final String TITLE = "title";
        public static final String URL = "url";
        public static final String SHARE_KIND = "shareKind";
        public static final String SHARE_IMAGE = "shareImage";
        public static final String SHARE_URL = "shareUrl";
        public static final String SHARE_CONTENT = "shareContent";
        public static final String SHARE_TITLE = "shareTitle";
    }

    public static class LoginView {

        public static final String PASSWORD_TYPE = "password_type";
        /**
         * 修改密码
         */
        public static final String PASSWORD_TYPE_CHANGE = "0";
        /**
         * 注册提交密码
         */
        public static final String PASSWORD_TYPE_REGISTER = "1";

        public static final String PHONE_NUM = "login_phone_number";
    }

    public static class Broadcast {
        public static final String ACTION_USER_INFO_REFRESH = "broadcast.action.user.info.refresh";
    }

    public static class NewsView {

        public static final String SID = "news_sid";
    }
}
