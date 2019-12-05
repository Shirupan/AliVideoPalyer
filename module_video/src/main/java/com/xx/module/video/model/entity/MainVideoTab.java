package com.xx.module.video.model.entity;

/**
 * @author someone
 * @date 2019-05-30
 */
public class MainVideoTab {

    /**
     * srot : 0
     * typename : 精选
     * tid : 1
     */
    private int id;
    private int srot;
    private String typename;
    private int tid;

    public int getSrot() {
        return srot;
    }

    public void setSrot(int srot) {
        this.srot = srot;
    }

    public String getTypename() {
        return typename;
    }

    public void setTypename(String typename) {
        this.typename = typename;
    }

    public int getTid() {
        return tid;
    }

    public void setTid(int tid) {
        this.tid = tid;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
