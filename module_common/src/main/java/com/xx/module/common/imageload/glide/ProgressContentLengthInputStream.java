package com.xx.module.common.imageload.glide;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.ArrayMap;
import android.text.TextUtils;
import android.util.Log;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author
 * @date 2018/8/24 0024
 */
public class ProgressContentLengthInputStream extends FilterInputStream {
    private static final String TAG = "ContentLengthStream";
    private static final int UNKNOWN = -1;

    private final long contentLength;
    private int readSoFar;
    private int mProgress=1;
    private String mUrl;

    @NonNull
    public static InputStream obtain(String url, @NonNull InputStream other,
                                     @Nullable String contentLengthHeader) {
        return obtain(url, other, parseContentLength(contentLengthHeader));
    }

    @NonNull
    public static InputStream obtain(String url, @NonNull InputStream other, long contentLength) {
        return new ProgressContentLengthInputStream(url, other, contentLength);
    }

    private static int parseContentLength(@Nullable String contentLengthHeader) {
        int result = UNKNOWN;
        if (!TextUtils.isEmpty(contentLengthHeader)) {
            try {
                result = Integer.parseInt(contentLengthHeader);
            } catch (NumberFormatException e) {
                if (Log.isLoggable(TAG, Log.DEBUG)) {
                    Log.d(TAG, "failed to parse content length header: " + contentLengthHeader, e);
                }
            }
        }
        return result;
    }

    private ProgressContentLengthInputStream(String url, @NonNull InputStream in, long contentLength) {
        super(in);
        this.mUrl = url;
        this.contentLength = contentLength;
    }

    @Override
    public synchronized int available() throws IOException {
        return (int) Math.max(contentLength - readSoFar, in.available());
    }

    @Override
    public synchronized int read() throws IOException {
        int value = super.read();
        checkReadSoFarOrThrow(value >= 0 ? 1 : -1);
        return value;
    }

    @Override
    public int read(byte[] buffer) throws IOException {
        return read(buffer, 0, buffer.length);
    }

    @Override
    public synchronized int read(byte[] buffer, int byteOffset, int byteCount)
            throws IOException {
        return checkReadSoFarOrThrow(super.read(buffer, byteOffset, byteCount));
    }

    private int checkReadSoFarOrThrow(int read) throws IOException {
        if (read >= 0) {
            readSoFar += read;
        } else if (contentLength - readSoFar > 0) {
            throw new IOException("Failed to read all expected data"
                    + ", expected: " + contentLength
                    + ", but read: " + readSoFar);
        }
        if (this.mUrl != null) {
            List<GlideLoadProgressListener> list = LISTENER_MAP.get(mUrl);
            if (list != null) {
                int progress = (int) (readSoFar * 100 / contentLength);
                if (mProgress < progress) {
                    mProgress = progress;
                    for (GlideLoadProgressListener listener : list) {
                        listener.onProgress(progress);
                    }
                }
                if (progress >= 100) {
                    LISTENER_MAP.remove(mUrl);
                }
            }
        }
        return read;
    }


    private final static Map<String, List<GlideLoadProgressListener>> LISTENER_MAP = new ArrayMap<>();

    public static void registerProgressListener(String url, GlideLoadProgressListener listener) {
        synchronized (LISTENER_MAP) {
            List<GlideLoadProgressListener> list = LISTENER_MAP.get(url);
            if (list == null) {
                list = new ArrayList<>();
            }
            if (!list.contains(listener)) {
                list.add(listener);
            }
            LISTENER_MAP.put(url, list);
        }
    }

    public static void unRegisterProgressListener(String url, GlideLoadProgressListener listener) {
        synchronized (LISTENER_MAP) {
            List<GlideLoadProgressListener> list = LISTENER_MAP.get(url);
            if (list == null) {
                return;
            }
            if (list.contains(listener)) {
                list.remove(listener);
            }
        }
    }
}
