package com.xx.module.me.model.entity;

/**
 * @author someone
 * @date 2019-05-31
 */
public class MeMenu {

    /**
     * id : 2
     * type : 0
     * url : null
     * img : null
     * title : 万年历
     */

    private int id;
    private int type;
    private String url;
    private String img;
    private String title;

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

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getImg() {
        return img;
    }

    public void setImg(String img) {
        this.img = img;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
