package com.xx.lib.db.entity;

/**
 * @author someone
 * @date 2019-06-14
 */

public class SplashAdvert {

    /**
     * id : 6
     * content : 往者不可谏，来着犹可追。”对于2018年你会有哪些期许呢？新的一年里，你在学习、工作、人际、事业以及感情上都会有哪些起伏波动？整体运程对你的生活有哪些帮扶和阻滞？2018年流年运程由专业的大师团队打造的一款产品，让你在2018年迅速地找到新的方向和突破口。
     * restime : 5
     * weburl : https://hy.yixueqm.com/zhiming/index.php/home-lnyc2019-index
     * imgurl : https://www.yixueqm.com/zhiming/Public/images/mingli_shouye/lnys_banner.png
     * title : 流年运势
     * ispush : 1
     * clicks : 76354
     * createtime : 1559119407000
     */

    private int id;
    private String content;
    private int restime;
    private String weburl;
    private String imgurl;
    private String title;
    private int ispush;
    private int clicks;
    private long createtime;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public int getRestime() {
        return restime;
    }

    public void setRestime(int restime) {
        this.restime = restime;
    }

    public String getWeburl() {
        return weburl;
    }

    public void setWeburl(String weburl) {
        this.weburl = weburl;
    }

    public String getImgurl() {
        return imgurl;
    }

    public void setImgurl(String imgurl) {
        this.imgurl = imgurl;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getIspush() {
        return ispush;
    }

    public void setIspush(int ispush) {
        this.ispush = ispush;
    }

    public int getClicks() {
        return clicks;
    }

    public void setClicks(int clicks) {
        this.clicks = clicks;
    }

    public long getCreatetime() {
        return createtime;
    }

    public void setCreatetime(long createtime) {
        this.createtime = createtime;
    }
}
