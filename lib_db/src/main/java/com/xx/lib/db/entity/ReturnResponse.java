package com.xx.lib.db.entity;

/**
 * @author
 * @date 2018/9/20 0020
 */
public class ReturnResponse<T> {
    private String response;
    private ReturnJson returnJson;
    private T data;

    public ReturnJson getReturnJson() {
        return returnJson;
    }

    public void setReturnJson(ReturnJson returnJson) {
        this.returnJson = returnJson;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }
}
