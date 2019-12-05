package com.xx.lib.db.entity;

/**
 * @author someone
 * @date 2019-05-28
 */
public class QiniuTokenJson {
    /**
     * 文件名
     */
    private String key;
    private String token;
    /**
     * 完整路径
     */
    private String url;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
