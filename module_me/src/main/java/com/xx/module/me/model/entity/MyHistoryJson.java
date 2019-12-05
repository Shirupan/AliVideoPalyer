package com.xx.module.me.model.entity;

import com.xx.lib.db.entity.MainVideo;
import com.xx.lib.db.entity.NewsJson;

/**
 * @author someone
 * @date 2019-06-12
 */
public class MyHistoryJson {

    /**
     * id : 1
     * type : 0
     * userdata : null
     * timestr : 2019-05-30 17:51:57
     * newsdata : null
     * videodata : {"clicks":0,"coverurl":"http://puui.qpic.cn/qqvideo_ori/0/b05309jjec2_228_128/0","createtime":{"date":29,"day":3,"hours":16,"minutes":52,"month":4,"seconds":20,"time":1559119940000,"timezoneOffset":-480,"year":119},"ispush":0,"praise":3,"sharemsg":"成龙这部电影被吐槽成烂片 却狂赚8亿","sharenum":6,"sharephoto":"http://puui.qpic.cn/qqvideo_ori/0/b05309jjec2_228_128/0","shareurl":"https://v.qq.com/iframe/player.html?vid=b05309jjec2&tiny=0&auto=0","timestr":"","typeid":1,"uid":100004,"userimg":"","username":"","vid":2,"videolength":222,"videomsg":"","videotitle":"成龙这部电影被吐槽成烂片 却狂赚8亿","videourl":"https://v.qq.com/iframe/player.html?vid=b05309jjec2&tiny=0&auto=0"}
     * sid : null
     * uid : 100004
     * vid : 2
     * createtime : 1559209917000
     * num : 1
     */

    private int id;
    private int type;
    private String timestr;
    private NewsJson newsdata;
    private MainVideo videodata;
    private Integer sid;
    private int uid;
    private Integer vid;
    private long createtime;
    private int num;


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }


    public String getTimestr() {
        return timestr;
    }

    public void setTimestr(String timestr) {
        this.timestr = timestr;
    }

    public NewsJson getNewsdata() {
        return newsdata;
    }

    public void setNewsdata(NewsJson newsdata) {
        this.newsdata = newsdata;
    }

    public MainVideo getVideodata() {
        return videodata;
    }

    public void setVideodata(MainVideo videodata) {
        this.videodata = videodata;
    }

    public Integer getSid() {
        return sid;
    }

    public void setSid(Integer sid) {
        this.sid = sid;
    }

    public int getUid() {
        return uid;
    }

    public void setUid(int uid) {
        this.uid = uid;
    }

    public Integer getVid() {
        return vid;
    }

    public void setVid(Integer vid) {
        this.vid = vid;
    }

    public long getCreatetime() {
        return createtime;
    }

    public void setCreatetime(long createtime) {
        this.createtime = createtime;
    }

    public int getNum() {
        return num;
    }

    public void setNum(int num) {
        this.num = num;
    }


}
