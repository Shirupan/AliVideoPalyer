package com.mrkj.lib.net.loader.entity;

import java.io.File;

public class FileInfo {
    private String fileName;
    private String netPath;
    File file;
    long fileLong;
    long currentLong;
    int state;
    String message;
    private byte[] data;


    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public long getFileLong() {
        return fileLong;
    }

    public void setFileLong(long fileLong) {
        this.fileLong = fileLong;
    }

    public long getCurrentLong() {
        return currentLong;
    }

    public void setCurrentLong(long currentLong) {
        this.currentLong = currentLong;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public byte[] getData() {
        return data;
    }


    public String getFileName() {
        return fileName;
    }

    public String getNetPath() {
        return netPath;
    }

    public void setNetPath(String netPath) {
        this.netPath = netPath;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
}
