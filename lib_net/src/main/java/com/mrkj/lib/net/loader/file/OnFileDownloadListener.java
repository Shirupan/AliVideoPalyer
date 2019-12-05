package com.mrkj.lib.net.loader.file;

import com.xx.lib.db.entity.ReturnJson;

/**
 * @author
 * @date 2018/8/24 0024
 */
public interface OnFileDownloadListener {
    void onSuccess(ReturnJson result);

    void onProgress(String name, long current, long totalSize);

    void onError(ReturnJson json);
}
