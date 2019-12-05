package com.xx.module.common.model;


import android.text.TextUtils;

import com.xx.lib.db.entity.ReturnJson;
import com.xx.lib.db.exception.ReturnJsonCodeException;
import com.xx.lib.db.exception.SmCacheException;
import com.xx.module.common.model.entity.SmError;

import java.io.IOException;
import java.net.ConnectException;
import java.net.UnknownHostException;

import io.reactivex.exceptions.CompositeException;
import retrofit2.HttpException;

/**
 * @author
 * @date 2017/10/25
 */

public class SmErrorHandler {
    public static SmError catchTheErrorSmError(Throwable t) {
        SmError error = null;
        if (t instanceof ConnectException || t instanceof UnknownHostException || t instanceof HttpException) {
            error = SmError.ERROR_NO_CONNECT;
            if (t instanceof HttpException) {
                error.customCode = ((HttpException) t).code();
            }
        } else if (t instanceof SmCacheException) {
            error = SmError.ERROR_CACHE;
        } else if (t instanceof ReturnJsonCodeException) {
            error = SmError.ERROR_CUSTOM;
            if (((ReturnJsonCodeException) t).getReturnJson() != null) {
                ReturnJson json = ((ReturnJsonCodeException) t).getReturnJson();
                if (TextUtils.isEmpty(json.getMsg())) {
                    error.message = json.getContent();
                } else {
                    error.message = json.getMsg();
                }
                error.customCode = json.getCode();
            } else {
                error.message = t.getMessage();
            }
        } else if (t instanceof IOException) {
            error = SmError.ERROR_NETWORK;
        } else if (t instanceof CompositeException) {
            for (Throwable exception : ((CompositeException) t).getExceptions()) {
                if (!(exception instanceof SmCacheException)) {
                    error = SmError.ERROR_CUSTOM;
                    error.message = exception.getMessage();
                    break;
                }
            }
        } else {
            error = SmError.ERROR_CUSTOM;
            error.message = t.getMessage();
        }
        return error;
    }

}
