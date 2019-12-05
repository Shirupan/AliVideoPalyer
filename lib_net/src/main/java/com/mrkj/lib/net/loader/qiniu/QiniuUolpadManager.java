package com.mrkj.lib.net.loader.qiniu;

import android.support.v4.util.ArrayMap;
import android.text.TextUtils;

import com.mrkj.lib.common.util.FileUtil;
import com.qiniu.android.common.FixedZone;
import com.qiniu.android.http.ResponseInfo;
import com.qiniu.android.storage.Configuration;
import com.qiniu.android.storage.UpCancellationSignal;
import com.qiniu.android.storage.UpCompletionHandler;
import com.qiniu.android.storage.UpProgressHandler;
import com.qiniu.android.storage.UploadManager;
import com.qiniu.android.storage.UploadOptions;

import org.json.JSONObject;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class QiniuUolpadManager {
    private static UploadManager uploadManager;
    private static QiniuUolpadManager qiniuUolpadManager;

    private QiniuUolpadManager() {
        Configuration config = new Configuration.Builder()
                .chunkSize(512 * 1024)        // 分片上传时，每片的大小。 默认256K
                .putThreshhold(1024 * 1024)   // 启用分片上传阀值。默认512K
                .connectTimeout(10)           // 链接超时。默认10秒
                .useHttps(true)               // 是否使用https上传域名
                .responseTimeout(120)          // 服务器响应超时。默认60秒
                // .recorder(recorder)           // recorder分片上传时，已上传片记录器。默认null
                //.recorder(recorder, keyGen)   // keyGen 分片上传时，生成标识符，用于片记录器区分是那个文件的上传记录
                // .zone(FixedZone.zone0)        // 设置区域，指定不同区域的上传域名、备用域名、备用IP。
                .build();
        uploadManager = new UploadManager(config);
    }


    public static QiniuUolpadManager getInstance() {
        if (uploadManager == null) {
            synchronized (UploadManager.class) {
                if (uploadManager == null) {
                    qiniuUolpadManager = new QiniuUolpadManager();
                }
            }
        }
        return qiniuUolpadManager;
    }

    /**
     * 单文件上传
     *
     * @param file
     * @param uploadName 七牛云上的文件名
     * @param params     其他参数
     * @param token      七牛云token
     * @param listener
     * @return
     */
    public void upload(final File file, String uploadName,
                       Map<String, String> params, final String token,
                       final OnUploadListener listener) {
        if (uploadManager == null) {
            getInstance();
        }
        if (uploadName == null) {
            uploadName = FileUtil.getNameFromUrl(file.getPath());
        }
        ArrayMap<String, String> newMaps = null;
        if (params != null) {
            newMaps = new android.support.v4.util.ArrayMap<>();
            Set<String> keys = params.keySet();
            for (String key : keys) {
                //七牛云要求key以x:开头
                String value = params.get(key);
                if (!TextUtils.isEmpty(value)) {
                    if (!key.startsWith("x:")) {
                        key = "x:" + key;
                    }
                    newMaps.put(key, value);
                }
            }
        }
        //取消上传监听
        final UpCancellationSignal cancellationSignal = new UpCancellationSignal() {
            @Override
            public boolean isCancelled() {
                if (listener != null) {
                    return listener.isCancelled();
                }
                return false;
            }
        };
        //上传进度监听
        UpProgressHandler progressHandler1 = new UpProgressHandler() {
            @Override
            public void progress(String key, double percent) {
                if (listener != null) {
                    listener.progress(key, percent);
                }
            }
        };
        //上传完成监听
        UpCompletionHandler completionHandler = new UpCompletionHandler() {
            @Override
            public void complete(String key, ResponseInfo info, JSONObject response) {
                if (listener != null) {
                    listener.complete(key, info, response);
                }
            }
        };
        UploadOptions options = new UploadOptions(newMaps, "application/octet-stream",
                true, progressHandler1, cancellationSignal);
        try {
            uploadManager.put(file, uploadName, token, completionHandler, options);
        } catch (Exception e) {
            if (listener != null) {
                listener.error(e);
            } else {
                e.printStackTrace();
            }
        }

    }


    public void upload(List<FilePath> list, Map<String, String> params, final String token,
                       final OnUploadListener listener) {


    }

    public interface OnUploadListener {

        boolean isCancelled();

        void progress(String name, double percent);

        void complete(String url, ResponseInfo info, JSONObject response);

        void error(Exception e);
    }


    public static class FilePath {
        private String filePath;
        private String fileName;

        public String getFilePath() {
            return filePath;
        }

        public void setFilePath(String filePath) {
            this.filePath = filePath;
        }

        public String getFileName() {
            return fileName;
        }

        public void setFileName(String fileName) {
            this.fileName = fileName;
        }
    }
}
