package com.xx.lib.db.entity;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

/**
 * @author someone
 * @date 2019-06-14
 */
@Entity
public class UserSetting {
    @PrimaryKey
    private Integer id;

    /**
     * 是否消息推送
     * 0是  1否
     */
    private int notify;
    /**
     * 是否wifi自动播放
     * 0是  1否
     */
    private int wifiAutoPlay;

    private String token;


    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public int getNotify() {
        return notify;
    }

    public void setNotify(int notify) {
        this.notify = notify;
    }

    public int getWifiAutoPlay() {
        return wifiAutoPlay;
    }

    public void setWifiAutoPlay(int wifiAutoPlay) {
        this.wifiAutoPlay = wifiAutoPlay;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
