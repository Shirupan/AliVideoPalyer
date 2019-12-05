package com.xx.module.video.model.entity;

import java.util.List;

/**
 * @author someone
 * @date 2019-05-30
 */
public class VideoTabManagerJson {
    /**
     * 用户已经选择的tab
     */
    private List<MainVideoTab> obj1;
    /**
     * 用户未选择的tab
     */
    private List<MainVideoTab> obj2;

    public List<MainVideoTab> getObj1() {
        return obj1;
    }

    public void setObj1(List<MainVideoTab> obj1) {
        this.obj1 = obj1;
    }

    public List<MainVideoTab> getObj2() {
        return obj2;
    }

    public void setObj2(List<MainVideoTab> obj2) {
        this.obj2 = obj2;
    }
}
