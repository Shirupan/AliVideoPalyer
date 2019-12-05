package com.mrkj.lib.net.tool;


import android.text.TextUtils;

import com.xx.lib.db.entity.ReturnJson;
import com.xx.lib.db.exception.ReturnJsonCodeException;
import com.xx.lib.db.exception.SmCacheException;

import java.io.IOException;
import java.net.ConnectException;
import java.net.UnknownHostException;
import java.util.List;

import io.reactivex.exceptions.CompositeException;
import io.rx_cache2.RxCacheException;
import retrofit2.HttpException;

/**
 * @author
 * @date 2017/10/25
 */

public class ExceptionUtl {

    public static String catchTheError(Throwable t) {
        String message = "";
        if (t instanceof ConnectException || t instanceof UnknownHostException) {
            message = "亲，您的网络有点问题";
        } else if (t instanceof RxCacheException) {
            message = "";
        } else if (t instanceof SmCacheException) {
            message = "";
        } else if (t instanceof HttpException) {
            message = "网络繁忙";
        } else if (t instanceof ReturnJsonCodeException) {
            if (((ReturnJsonCodeException) t).getReturnJson() != null) {
                ReturnJson json = ((ReturnJsonCodeException) t).getReturnJson();
                if (TextUtils.isEmpty(json.getMsg())) {
                    message = json.getContent();
                } else {
                    message = json.getMsg();
                }
            } else {
                message = t.getMessage();
            }
        } else if (t instanceof IOException) {
            message = "网络错误";
        } else if (t instanceof CompositeException) {
            List<Throwable> list = ((CompositeException) t).getExceptions();
            for (Throwable e : list) {
                String m = catchTheError(e);
                if (!TextUtils.isEmpty(m)) {
                    message = m;
                    break;
                }
            }
        }
        if (TextUtils.isEmpty(message)) {
            String temp = t.getLocalizedMessage();
            if (!TextUtils.isEmpty(temp)) {
                int index = temp.indexOf(":") + 1;
                if (index >= 0) {
                    message = temp.substring(index);
                } else {
                    message = temp;
                }
            }
        }
        if (TextUtils.isEmpty(message)) {
            message = "呀~发生错误了...";
        }
        return message;
    }


}
