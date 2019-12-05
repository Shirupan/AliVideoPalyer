package com.xx.lib.db.entity;

/**
 * @author
 * @date 2016-11-07
 */

public class ReturnBeanJson<T> {
    private int code; //0 失败  1成功
    private T content;  //失败信息或者json
    private String msg;  //失败信息


    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public T getContent() {
        return content;
    }

    public void setContent(T content) {
        this.content = content;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

}
