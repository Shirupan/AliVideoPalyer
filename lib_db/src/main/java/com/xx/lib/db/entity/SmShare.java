package com.xx.lib.db.entity;

import android.graphics.Bitmap;

import java.io.Serializable;

/**
 * @author
 * @date 2016/11/15
 */

public class SmShare implements Serializable {
    private Integer id; //序列

    private String url; //分享的url地址

    private String content; //分享的文本内容

    private String imgurl;// 分享的图片url
    /**
     * 0视频分享  1资讯分享  2网页分享
     */
    private Integer kind;  //哪个入口请求的分享

    private Bitmap shareBitmap;  //如果是要分享缓存在中的图片（截屏分享）
    /**
     * 截屏分享时候 填入的title
     */
    private String title;

    /**
     * 截屏分享时候填入的副标题（一般为测试中用户选择的答案）
     */
    private String tips;
    /**
     * 1 微信好友  2 微信朋友圈
     * 3 qq好友   4  qq空间
     * 5 新浪微博
     */
    private String sharetype; //格式： 1#2#4
    /**
     * 上面转化成数组
     */
    private Integer[] shareType;

    /**
     * 0 没有活动 1 有活动 .后续或许会拓展，1，2，3，4，5 这样
     */
    private int isactivity;
    /**
     * 分享内容的id
     */
    private int qid;
    /**
     * 分享记录类型
     */
    private int shareMode;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getImgurl() {
        return imgurl;
    }

    public void setImgurl(String imgurl) {
        this.imgurl = imgurl;
    }

    public Integer getKind() {
        return kind;
    }

    public void setKind(Integer kind) {
        this.kind = kind;
    }

    public String getSharetype() {
        return sharetype;
    }

    public void setSharetype(String sharetype) {
        this.sharetype = sharetype;
    }

    public int getIsactivity() {
        return isactivity;
    }

    public void setIsactivity(int isactivity) {
        this.isactivity = isactivity;
    }


    public Bitmap getShareBitmap() {
        return shareBitmap;
    }


    public void setShareBitmap(Bitmap shareBitmap) {
        this.shareBitmap = shareBitmap;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }


    public String getTips() {
        return tips;
    }

    public void setTips(String tips) {
        this.tips = tips;
    }

    public int getQid() {
        return qid;
    }

    public void setQid(int qid) {
        this.qid = qid;
    }

    public void setShareMode(int shareMode) {
        this.shareMode = shareMode;
    }

    public int getShareMode() {
        return shareMode;
    }

    public Integer[] getShareType() {
        return shareType;
    }

    public void setShareType(Integer[] shareType) {
        this.shareType = shareType;
    }
}
