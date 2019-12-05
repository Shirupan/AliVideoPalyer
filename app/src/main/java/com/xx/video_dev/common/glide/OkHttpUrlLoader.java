package com.xx.video_dev.common.glide;


import android.support.annotation.NonNull;
import android.util.Log;

import com.bumptech.glide.Priority;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.HttpException;
import com.bumptech.glide.load.Options;
import com.bumptech.glide.load.data.DataFetcher;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.ModelLoader;
import com.bumptech.glide.load.model.ModelLoaderFactory;
import com.bumptech.glide.load.model.MultiModelLoaderFactory;
import com.xx.module.common.imageload.glide.ProgressContentLengthInputStream;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * 接管Glide 4.0中url加载，增加进度条
 *
 * @author
 * @date 2018/8/24 0024
 */
public class OkHttpUrlLoader implements ModelLoader<GlideUrl, InputStream> {
    private OkHttpClient client;

    public OkHttpUrlLoader(OkHttpClient client) {
        this.client = client;
    }

    @Override
    public boolean handles(@NonNull GlideUrl url) {
        return true;
    }

    @Override
    public LoadData<InputStream> buildLoadData(@NonNull GlideUrl model, int width, int height, @NonNull Options options) {
        //返回LoadData对象，泛型为InputStream
        return new LoadData<>(model, new OkHttpStreamFetcher(this.client, model));
    }

    public static class Factory implements ModelLoaderFactory<GlideUrl, InputStream> {
        private OkHttpClient client;


        public Factory(OkHttpClient client) {
            this.client = client;
        }

        @Override
        @NonNull
        public ModelLoader<GlideUrl, InputStream> build(MultiModelLoaderFactory multiFactory) {
            return new OkHttpUrlLoader(this.client);
        }

        @Override
        public void teardown() {
        }
    }

    public class OkHttpStreamFetcher implements DataFetcher<InputStream> {
        private static final String TAG = "OkHttpFetcher";
        private final OkHttpClient client;
        private final GlideUrl url;
        InputStream stream;
        ResponseBody responseBody;
        private volatile Call call;

        OkHttpStreamFetcher(OkHttpClient client, GlideUrl url) {
            this.client = client;
            this.url = url;
        }

        @Override
        public void loadData(@NonNull Priority priority, @NonNull final DataCallback<? super InputStream> callback) {
            Request.Builder requestBuilder = (new Request.Builder()).url(this.url.toStringUrl());

            for (Object o : this.url.getHeaders().entrySet()) {
                Map.Entry headerEntry = (Map.Entry) o;
                String key = (String) headerEntry.getKey();
                requestBuilder.addHeader(key, (String) headerEntry.getValue());
            }

            final Request request1 = requestBuilder.build();
            this.call = this.client.newCall(request1);
            this.call.enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    if (Log.isLoggable("OkHttpFetcher", Log.DEBUG)) {
                        Log.d("OkHttpFetcher", "OkHttp failed to obtain result", e);
                    }
                    callback.onLoadFailed(e);
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    OkHttpStreamFetcher.this.responseBody = response.body();
                    if (response.isSuccessful()) {
                        long contentLength = OkHttpStreamFetcher.this.responseBody.contentLength();
                        OkHttpStreamFetcher.this.stream = ProgressContentLengthInputStream.obtain(url.toStringUrl(), OkHttpStreamFetcher.this.responseBody.byteStream(), contentLength);
                        callback.onDataReady(OkHttpStreamFetcher.this.stream);
                    } else {
                        callback.onLoadFailed(new HttpException(response.message(), response.code()));
                    }

                }
            });
        }

        @Override
        public void cleanup() {
            try {
                if (this.stream != null) {
                    this.stream.close();
                }
            } catch (IOException var2) {

            }

            if (this.responseBody != null) {
                this.responseBody.close();
            }

        }

        @Override
        public void cancel() {
            Call local = this.call;
            if (local != null) {
                local.cancel();
            }

        }

        @Override
        @NonNull
        public Class<InputStream> getDataClass() {
            return InputStream.class;
        }

        @Override
        @NonNull
        public DataSource getDataSource() {
            return DataSource.REMOTE;
        }
    }

}
