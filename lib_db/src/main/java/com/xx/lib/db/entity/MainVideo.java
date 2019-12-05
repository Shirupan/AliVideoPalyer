package com.xx.lib.db.entity;

/**
 * @author someone
 * @date 2019-05-30
 */
public class MainVideo {


    /**
     * username : 神奇的皮皮虾
     * userimg : null
     * vid : 2
     * videourl : https://v.qq.com/iframe/player.html?vid=b05309jjec2&tiny=0&auto=0
     * videotitle : 成龙这部电影被吐槽成烂片 却狂赚8亿
     * videomsg : null
     * videolength : 222
     * coverurl : http://puui.qpic.cn/qqvideo_ori/0/b05309jjec2_228_128/0
     * typeid : 1
     * clicks : 15
     * sharenum : 6
     * praise : 3
     * ispush : 0
     * sharephoto : http://puui.qpic.cn/qqvideo_ori/0/b05309jjec2_228_128/0
     * shareurl : https://v.qq.com/iframe/player.html?vid=b05309jjec2&tiny=0&auto=0
     * sharemsg : 成龙这部电影被吐槽成烂片 却狂赚8亿
     * createtime : 1559119940000
     * uid : 100004
     */

    private String username;
    private String userimg;
    private int vid;
    private String videourl;
    private String videotitle;
    private String videomsg;
    private String videolength;
    private String coverurl;
    //竖图
    private String imgurl2;
    private int typeid;
    private int clicks;
    private int sharenum;
    private int praise;
    private String pushtime;
    /**
     * 预留字段
     */
    private int ispush;
    private String sharephoto;
    private String shareurl;
    private String sharemsg;
    private long createtime;
    private String timestr;
    private int uid;
    /**
     * 是否已被该用户赞 0否  1是
     */
    private int ispraise;
    /**
     * 是否已被该用户收藏 0否  1是
     */
    private int iscollection;
    /**
     * 是否是广告item
     */
    private int isadv;
    //本地字段
    private int errorCode = -1;
    private String errorMsg;

    public long getCreatetime() {
        return createtime;
    }

    public void setCreatetime(long createtime) {
        this.createtime = createtime;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUserimg() {
        return userimg;
    }

    public void setUserimg(String userimg) {
        this.userimg = userimg;
    }

    public int getVid() {
        return vid;
    }

    public void setVid(int vid) {
        this.vid = vid;
    }

    public String getVideourl() {
        return videourl;
    }

    public void setVideourl(String videourl) {
        this.videourl = videourl;
    }

    public String getVideotitle() {
        return videotitle;
    }

    public void setVideotitle(String videotitle) {
        this.videotitle = videotitle;
    }

    public String getVideomsg() {
        return videomsg;
    }

    public void setVideomsg(String videomsg) {
        this.videomsg = videomsg;
    }

    public String getVideolength() {
        return videolength;
    }

    public void setVideolength(String videolength) {
        this.videolength = videolength;
    }

    public String getCoverurl() {
        return coverurl;
    }

    public void setCoverurl(String coverurl) {
        this.coverurl = coverurl;
    }

    public int getTypeid() {
        return typeid;
    }

    public void setTypeid(int typeid) {
        this.typeid = typeid;
    }

    public int getClicks() {
        return clicks;
    }

    public void setClicks(int clicks) {
        this.clicks = clicks;
    }

    public int getSharenum() {
        return sharenum;
    }

    public void setSharenum(int sharenum) {
        this.sharenum = sharenum;
    }

    public int getPraise() {
        return praise;
    }

    public void setPraise(int praise) {
        this.praise = praise;
    }

    public int getIspush() {
        return ispush;
    }

    public void setIspush(int ispush) {
        this.ispush = ispush;
    }

    public String getSharephoto() {
        return sharephoto;
    }

    public void setSharephoto(String sharephoto) {
        this.sharephoto = sharephoto;
    }

    public String getShareurl() {
        return shareurl;
    }

    public void setShareurl(String shareurl) {
        this.shareurl = shareurl;
    }

    public String getSharemsg() {
        return sharemsg;
    }

    public void setSharemsg(String sharemsg) {
        this.sharemsg = sharemsg;
    }


    public int getUid() {
        return uid;
    }

    public void setUid(int uid) {
        this.uid = uid;
    }


    public int getIsadv() {
        return isadv;
    }

    public void setIsadv(int isadv) {
        this.isadv = isadv;
    }

    public int getIspraise() {
        return ispraise;
    }

    public void setIspraise(int ispraise) {
        this.ispraise = ispraise;
    }

    public int getIscollection() {
        return iscollection;
    }

    public void setIscollection(int iscollection) {
        this.iscollection = iscollection;
    }

    public String getTimestr() {
        return timestr;
    }

    public void setTimestr(String timestr) {
        this.timestr = timestr;
    }

    public int getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }

    public String getPushtime() {
        return pushtime;
    }

    public void setPushtime(String pushtime) {
        this.pushtime = pushtime;
    }

    public String getImgurl2() {
        return imgurl2;
    }

    public void setImgurl2(String imgurl2) {
        this.imgurl2 = imgurl2;
    }
}
