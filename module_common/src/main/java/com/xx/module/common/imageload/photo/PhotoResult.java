package com.xx.module.common.imageload.photo;

import java.util.ArrayList;

/**
 * @author
 * @date 2018/1/18 0018
 */

public class PhotoResult {
    private ArrayList<PhotoImage> images;
    private PhotoImage image;

    public static PhotoResult of(PhotoImage image) {
        ArrayList<PhotoImage> images = new ArrayList<>(1);
        images.add(image);
        return new PhotoResult(images);
    }

    public static PhotoResult of(ArrayList<PhotoImage> images) {
        return new PhotoResult(images);
    }

    private PhotoResult(ArrayList<PhotoImage> images) {
        this.images = images;
        if (images != null && !images.isEmpty()) this.image = images.get(0);
    }

    public ArrayList<PhotoImage> getImages() {
        return images;
    }

    public void setImages(ArrayList<PhotoImage> images) {
        this.images = images;
    }

    public PhotoImage getImage() {
        return image;
    }

    public void setImage(PhotoImage image) {
        this.image = image;
    }
}
