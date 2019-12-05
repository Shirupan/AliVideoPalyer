package com.xx.app.dependendy.service;

public interface BackgroundJob {

    void doJob();

    void cancel();

    void stop();


}
