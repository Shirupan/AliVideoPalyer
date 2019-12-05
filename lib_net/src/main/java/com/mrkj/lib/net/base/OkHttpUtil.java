package com.mrkj.lib.net.base;

import android.text.TextUtils;
import android.util.Log;

import com.mrkj.lib.net.NetLib;
import com.mrkj.lib.net.impl.RxAsyncHandler;
import com.mrkj.lib.net.impl.RxMainThreadScheduler;
import com.mrkj.lib.net.loader.qiniu.QiniuUolpadManager;
import com.mrkj.lib.net.retrofit.RetrofitManager;
import com.qiniu.android.http.ResponseInfo;
import com.xx.lib.db.entity.QiniuTokenJson;
import com.xx.lib.db.exception.ReturnJsonCodeException;

import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import io.reactivex.exceptions.Exceptions;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;


/**
 * 网络请求调用OKHttp
 *
 * @author
 */

public class OkHttpUtil {
    private static OkHttpClient client;

    public static OkHttpClient getOkHttpClient() {
        if (client == null) {
            synchronized (OkHttpClient.class) {
                if (client == null) {
                    //内容log
                    if (NetLib.DEBUG) {
                        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
                        interceptor.setLevel(HttpLoggingInterceptor.Level.HEADERS);
                        client = new OkHttpClient.Builder()
                                .connectTimeout(20, TimeUnit.SECONDS)
                                .readTimeout(1, TimeUnit.MINUTES)
                                // .retryOnConnectionFailure(true)
                                .addInterceptor(interceptor)
                                .build();
                    } else {
                        client = new OkHttpClient.Builder()
                                .connectTimeout(20, TimeUnit.SECONDS)
                                .readTimeout(1, TimeUnit.MINUTES)
                                //.retryOnConnectionFailure(true)
                                .build();
                    }

                }
            }
        }
        return client;
    }


    /**
     * GET方式的同步请求
     *
     * @param url ,tag也设置为url（取消请求时候用到）
     * @return
     */
    public static Response executeGET(String url) throws IOException {
        OkHttpClient client = getOkHttpClient();
        Request request = new Request.Builder()
                .url(url)
                .tag(url)
                .get()
                .build();
        Response response = null;
        response = client.newCall(request).execute();
        return response;
    }

    /**
     * GET方式的异步请求
     *
     * @param url
     * @param callback OKHttp提供的回调方法并不是在主线程上。请使用UICallback回调。
     */
    public static Call executeGET(String url, Callback callback) {
        OkHttpClient client = getOkHttpClient();
        Request request = new Request.Builder()
                .url(url)
                .get()
                .tag(url)
                .build();
        Call call = client.newCall(request);
        call.enqueue(callback);
        return call;
    }

    public static Call executeGET(String url, Map<String, String> headers, Callback callback) {
        OkHttpClient client = getOkHttpClient();
        Request.Builder builder = new Request.Builder()
                .url(url);
        Set<String> keys = headers.keySet();
        for (String key : keys) {
            String value = headers.get(key);
            if (!TextUtils.isEmpty(value)) {
                builder.addHeader(key, value);
            }
        }
        Request request = builder
                .get()
                .tag(url)
                .build();
        Call call = client.newCall(request);
        call.enqueue(callback);
        return call;
    }

    /**
     * @param url
     * @param requestBody
     * @param callback    OKHttp提供的回调方法并不是在主线程上。请使用UICallback回调。
     * @return 如果想取消请求，可以使用Call.cancel()方法
     */
    public static Call executePost(String url, RequestBody requestBody, Callback callback) {
        OkHttpClient client = getOkHttpClient();
        Request request = new Request.Builder()
                .url(url)
                .tag(url)
                .post(requestBody)
                .build();
        Call call = client.newCall(request);
        call.enqueue(callback);
        return call;
    }

    public static Response executePost(String url, RequestBody requestBody) throws IOException {
        OkHttpClient client = getOkHttpClient();
        Request request = new Request.Builder()
                .url(url)
                .tag(url)
                .post(requestBody)
                .build();
        Call call = client.newCall(request);
        return call.execute();

    }

    public static byte[] httpPost(String url, String entity) {
        if (url == null || url.length() == 0) {
            Log.e("okhttp", "httpPost, url is null");
            return null;
        }
        MediaType MEDIA_TYPE_MARKDOWN
                = MediaType.parse("text/x-markdown; charset=utf-8");
        Request request = new Request.Builder()
                .url(url)
                .tag(url)
                .addHeader("Accept", "application/json")
                .addHeader("Content-type", "application/json")
                .post(RequestBody.create(MEDIA_TYPE_MARKDOWN, entity))
                .build();
        try {
            if (client != null) {
                okhttp3.Response response = client.newCall(request).execute();
                byte[] buffer = response.body().bytes();
                response.close();
                return buffer;
            } else {
                return null;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;

    }


    public static void upLoadFile(String actionUrl, String filePath, Callback callback) {

        //创建File
        File file = new File(filePath);
        //创建RequestBody
        RequestBody body = RequestBody.create(MediaType.parse("application/octet-stream"), file);
        //创建Request
        final Request request = new Request.Builder().url(actionUrl).post(body).build();
        final Call call = getOkHttpClient().newBuilder()
                .writeTimeout(50, TimeUnit.SECONDS)
                .build()
                .newCall(request);
        call.enqueue(callback);
    }


    public static void uploadQiniuFile(final String file, final Map<String, String> paramsMap,
                                       final QiniuUolpadManager.OnUploadListener listener) {
        int index = file.lastIndexOf(".");
        String docformat = "";
        if (index >= 0) {
            docformat = file.substring(index + 1);
        }
        String url = "http://api.ddznzj.com/qiniutoken/?docformat=" + docformat;

        OkHttpUtil.executeGET(url, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, final IOException e) {
                new RxMainThreadScheduler() {
                    @Override
                    public void onNext(Integer data) {
                        if (listener != null) {
                            listener.error(e);
                        }
                    }
                }.execute();
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull final Response response) throws IOException {
                new RxAsyncHandler<QiniuTokenJson>() {
                    @Override
                    public QiniuTokenJson doSomethingBackground() {
                        try {
                            return RetrofitManager.dispatchTransformer(response.body().string(), QiniuTokenJson.class);
                        } catch (Exception e) {
                            e.printStackTrace();
                            throw Exceptions.propagate(e);
                        }
                    }

                    @Override
                    public void onNext(QiniuTokenJson data) {
                        uploadToQiniu(data, listener);
                    }
                }.execute();
            }

            private void uploadToQiniu(final QiniuTokenJson json, final QiniuUolpadManager.OnUploadListener listener) {
                QiniuUolpadManager.getInstance().upload(new File(file), json.getKey(), paramsMap, json.getToken(),
                        new QiniuUolpadManager.OnUploadListener() {
                            @Override
                            public boolean isCancelled() {
                                return listener.isCancelled();
                            }

                            @Override
                            public void progress(String name, double percent) {
                                if (listener != null) {
                                    listener.progress(name, percent);
                                }
                            }

                            @Override
                            public void complete(String name, ResponseInfo info, JSONObject response) {
                                if (info.statusCode != 200) {
                                    error(new ReturnJsonCodeException(info.error));
                                } else {
                                    if (listener != null) {
                                        listener.complete(json.getUrl(), info, response);
                                    }
                                }


                            }

                            @Override
                            public void error(Exception e) {
                                if (listener != null) {
                                    listener.error(e);
                                }
                            }
                        });
            }
        });
    }

}
