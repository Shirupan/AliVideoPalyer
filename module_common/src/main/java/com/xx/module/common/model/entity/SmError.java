package com.xx.module.common.model.entity;


import android.content.Context;
import android.support.annotation.StringRes;

import com.xx.module.common.R;

/**
 * @author someone
 * @date 2019/4/1
 */
public enum SmError {

    ERROR_NO_LOGIN(101, R.string.sm_error_no_login),
    ERROR_NO_MONEY(102, R.string.sm_error_money_not_enough),
    ERROR_CACHE(103, R.string.sm_error_cache),
    ERROR_CUSTOM(104, 0),
    ERROR_NO_CONNECT(501, R.string.sm_error_network_connect),
    ERROR_NETWORK(502, R.string.sm_error_network),
    ERROR_GET_DATA_FAILED(503, R.string.sm_error_network);


    public int errorCode;
    public int msgRes;
    /**
     * 先判断msgRes，当msgRes！=0的情况下才使用该字段
     */
    public String message;
    /**
     * 服务器返回的code
     */
    public int customCode;


    SmError(int code, @StringRes int message) {
        errorCode = code;
        this.msgRes = message;
    }


    public String getMessage(Context context) {
        return msgRes == 0 ? message : context.getString(msgRes);
    }
}
