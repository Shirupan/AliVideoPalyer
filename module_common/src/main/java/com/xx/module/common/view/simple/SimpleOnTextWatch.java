package com.xx.module.common.view.simple;

import android.text.Editable;
import android.text.TextWatcher;

/**
 * @author someone
 * @date 2019-05-29
 */
public abstract class SimpleOnTextWatch implements TextWatcher {
    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {

    }
}
