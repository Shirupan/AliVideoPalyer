package com.xx.module.common.view.base;

import java.util.ArrayList;
import java.util.List;

/**
 * @author someone
 * @date 2019/4/1
 */
public interface ITakePhotoView {

    TakePhotoHandler getTakePhotoHandler();

    void onGetPhoto(List<String> data);

    void onModifyPhoto(ArrayList<String> list);
}
