package com.xx.module.common.imageload.photo;

import android.net.Uri;

/**
 * @author
 * @date 2018/1/18 0018
 */

public class PhotoImage {
    private String originalPath;
    private String compressPath;
    private PhotoImage.FromType fromType;
    private boolean cropped;
    private boolean compressed;

    public static PhotoImage obtain(String path, PhotoImage.FromType fromType) {
        return new PhotoImage(path, fromType);
    }

    public static PhotoImage obtain(Uri uri, PhotoImage.FromType fromType) {
        return new PhotoImage(uri, fromType);
    }

    private PhotoImage(String path, PhotoImage.FromType fromType) {
        this.originalPath = path;
        this.fromType = fromType;
    }

    private PhotoImage(Uri uri, PhotoImage.FromType fromType) {
        this.originalPath = uri.getPath();
        this.fromType = fromType;
    }

    public String getOriginalPath() {
        return originalPath;
    }

    public void setOriginalPath(String originalPath) {
        this.originalPath = originalPath;
    }

    public String getCompressPath() {
        return compressPath;
    }

    public void setCompressPath(String compressPath) {
        this.compressPath = compressPath;
    }

    public PhotoImage.FromType getFromType() {
        return fromType;
    }

    public void setFromType(PhotoImage.FromType fromType) {
        this.fromType = fromType;
    }

    public boolean isCropped() {
        return cropped;
    }

    public void setCropped(boolean cropped) {
        this.cropped = cropped;
    }

    public boolean isCompressed() {
        return compressed;
    }

    public void setCompressed(boolean compressed) {
        this.compressed = compressed;
    }

    public enum FromType {
        CAMERA, OTHER
    }
}
