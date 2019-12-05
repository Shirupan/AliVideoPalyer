package com.mrkj.lib.net.loader.file;

import android.app.Dialog;
import android.content.Context;

import com.mrkj.lib.net.base.OkHttpUtil;
import com.mrkj.lib.net.impl.IRxHandler;
import com.mrkj.lib.net.impl.RxAsyncHandler;
import com.mrkj.lib.net.impl.RxMainThreadScheduler;
import com.mrkj.lib.net.tool.ExceptionUtl;
import com.xx.lib.db.entity.ReturnJson;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import io.reactivex.annotations.Nullable;
import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Response;

/**
 * @author
 * @date 2018/8/24 0024
 */
public class FileDownloadManager {
    private FileDownloadManager.P p;
    private IRxHandler asyncHandler;
    private Call mCall;

    private FileDownloadManager(FileDownloadManager.P p) {
        this.p = p;
        if (this.p.okHttpClient == null) {
            this.p.okHttpClient = OkHttpUtil.getOkHttpClient();
        }
    }

    public static class Build {
        FileDownloadManager.P p;

        public Build() {
            p = new FileDownloadManager.P();
        }

        public Build(Context context) {
            p = new FileDownloadManager.P();
            p.mContext = context;
        }

        public FileDownloadManager.Build setOnFileDownloadListener(OnFileDownloadListener listener) {
            p.listener = listener;
            return this;
        }


        /**
         * @param url          下载链接
         * @param downloadFile 文件路径
         * @return
         */
        public FileDownloadManager.Build setUrl(String url, String downloadFile) {
            p.url = url;
            p.file = downloadFile;
            return this;
        }

        /**
         * 其他需要上传的参数
         *
         * @param params
         * @return
         */
        public FileDownloadManager.Build setParams(Map<String, String> params) {
            p.params = params;
            return this;
        }

        public FileDownloadManager.Build setProgressDialog(Dialog dialog) {
            p.mDialog = dialog;
            return this;
        }

        public FileDownloadManager.Build setOkHttpClient(OkHttpClient c) {
            p.okHttpClient = c;
            return this;
        }

        public FileDownloadManager excute() {
            FileDownloadManager fileUploadManager = new FileDownloadManager(p);
            fileUploadManager.start();
            return fileUploadManager;
        }
    }

    public void stop() {
        if (asyncHandler != null) {
            asyncHandler.dispose();
            asyncHandler = null;
        }
        if (mCall != null) {
            mCall.cancel();
            mCall = null;
        }
    }

    private void start() {
        asyncHandler = new RxAsyncHandler<ReturnJson>() {
            private long readSoFar;

            @Override
            public void onStart() {
                if (p.mDialog != null) {
                    p.mDialog.show();
                }
            }

            @Override
            public ReturnJson doSomethingBackground() {
                ReturnJson json = new ReturnJson();
                try {
                    Response response = OkHttpUtil.executeGET(p.url);
                    InputStream in = response.body().byteStream();
                    final long totalSize = response.body().contentLength();
                    File file = new File(p.file);
                    if (!file.exists()) {
                        File dir = file.getParentFile();
                        if (!dir.exists()) {
                            dir.mkdirs();
                        }
                    } else {
                        file.delete();
                    }
                    file.createNewFile();
                    FileOutputStream outputStream = new FileOutputStream(file);
                    byte[] bytes = new byte[1024];
                    int length;
                    while ((length = in.read(bytes)) != -1) {
                        readSoFar += length;
                        outputStream.write(bytes, 0, length);
                        new RxMainThreadScheduler() {
                            @Override
                            public void onNext(Integer data) {
                                p.listener.onProgress(p.url, readSoFar, totalSize);
                            }
                        }.execute();
                    }
                    in.close();
                    outputStream.close();
                    json.setCode(1);
                    json.setContent("下载完成");
                } catch (IOException e) {
                    e.printStackTrace();
                    json.setCode(0);
                    json.setContent(ExceptionUtl.catchTheError(e));
                }
                return json;
            }

            @Override
            public void onNext(ReturnJson data) {
                if (p.listener != null) {
                    if (data.getCode() == 1) {
                        p.listener.onSuccess(data);
                    } else {
                        p.listener.onError(data);
                    }
                }
            }

            @Override
            public void onError(Throwable e) {
                super.onError(e);
                ReturnJson json = new ReturnJson();
                json.setCode(0);
                json.setContent(ExceptionUtl.catchTheError(e));
                if (p.listener != null) {
                    p.listener.onError(json);
                }
            }

            @Override
            public void onComplete() {
                if (p.mDialog != null) {
                    p.mDialog.dismiss();
                    p.mDialog = null;
                }
                p.mContext = null;
            }
        }.execute();
    }

    private static class P {
        @Nullable
        Context mContext;
        OnFileDownloadListener listener;
        String file;
        OkHttpClient okHttpClient;
        Map<String, String> params;
        String url;
        Dialog mDialog;
        long totalSize = 0;
        long totalCurrent = 0;
    }


   }
