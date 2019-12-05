package com.xx.lib.db.entity;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

/**
 * @author someone
 * @date 2019-05-28
 */
@Entity
public class UserSystem {

    @PrimaryKey
    private Long id;
    /**
     * createtime : null
     * nickname : 444
     * openid :
     * password : 1111
     * phone : 11111
     * photourl :
     * token : fghdgh
     * uid : 100001
     */

    private long createtime;
    private String nickname;
    private String openid;
    private String password;
    private String phone;
    private String photourl;
    private String token;
    private String xingpush;

    private long uid;

    public long getCreatetime() {
        return createtime;
    }

    public void setCreatetime(long createtime) {
        this.createtime = createtime;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getOpenid() {
        return openid;
    }

    public void setOpenid(String openid) {
        this.openid = openid;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getPhotourl() {
        return photourl;
    }

    public void setPhotourl(String photourl) {
        this.photourl = photourl;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public long getUid() {
        return uid;
    }

    public void setUid(long uid) {
        this.uid = uid;
    }

    public Long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getXingpush() {
        return xingpush;
    }

    public void setXingpush(String xingpush) {
        this.xingpush = xingpush;
    }
}
