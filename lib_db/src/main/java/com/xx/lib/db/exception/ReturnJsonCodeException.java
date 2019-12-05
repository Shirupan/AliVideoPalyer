package com.xx.lib.db.exception;

import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.xx.lib.db.entity.ReturnJson;

import java.io.IOException;

/**
 * @author
 * 网络请求之后ReturnJson中code=0时候的异常类
 * @date 2016/11/16
 */

public class ReturnJsonCodeException extends IOException {

    public ReturnJsonCodeException(String s) {
        super(s);
        mJson = new ReturnJson();
        mJson.setCode(0);
        mJson.setContent(s);
    }

    public ReturnJsonCodeException(String s, int code) {
        super(s);
        mJson = new ReturnJson();
        mJson.setCode(code);
        mJson.setContent(s);
    }

    private ReturnJson mJson;

    public ReturnJsonCodeException(ReturnJson returnJson) {
        super(TextUtils.isEmpty(returnJson.getMsg()) ? returnJson.getContent() : returnJson.getMsg());
        mJson = returnJson;
    }

    @Nullable
    public ReturnJson getReturnJson() {
        return mJson;
    }
}
