/**
 * Copyright 2012 Novoda Ltd
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.xx.module.common.imageload;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.NestedScrollView;
import android.text.TextUtils;
import android.util.Base64;
import android.view.View;

import com.mrkj.lib.common.util.AppUtil;
import com.mrkj.lib.common.util.GetPathFromUri4kitkat;
import com.mrkj.lib.common.util.SmLogger;
import com.mrkj.lib.net.NetLib;
import com.mrkj.lib.net.base.OkHttpUtil;
import com.mrkj.lib.net.impl.RxAsyncHandler;
import com.xx.lib.db.exception.ReturnJsonCodeException;

import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.exceptions.Exceptions;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


/**
 * 图片处理工具类
 *
 * @author someone
 */
public class BitmapUtil {

    private static final int BUFFER_SIZE = 64 * 1024;
    public static final int MAX_SIZE = 1024;

    public static Bitmap getBitmapFromView(View view) {
        if (view == null) {
            return null;
        }
        Bitmap result;
        try {
            view.setDrawingCacheEnabled(true);
            view.buildDrawingCache();
            Bitmap screen = Bitmap.createBitmap(view.getDrawingCache());
            view.setDrawingCacheEnabled(false);
            result = screen;
        } catch (Exception e) {
            SmLogger.i("view转图像：" + e.getMessage());
            return null;
        }
        return result;
    }

    /**
     * @param view
     * @param sampleFactor 缩放倍数  大于1为缩小，小于1为放大
     * @return
     */
    public static Bitmap getBitmapFromView(View view, float sampleFactor) {
        if (view == null) {
            return null;
        }
        if (sampleFactor == 0) {
            throw new IllegalStateException("sampleFactor can not be zero");
        }
        final float scale = 1.0f / sampleFactor;

        final int width = view.getWidth();
        final int height = view.getHeight();

        final int downScaledWidth = (int) (width * scale);
        final int downScaledHeight = (int) (height * scale);
        try {
            Bitmap bitmap = ImageLoader.getInstance().get(view.getContext(), downScaledWidth, downScaledHeight, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            if (sampleFactor > 1.0f) {
                canvas.scale(scale, scale);
            }
            view.draw(canvas);
            return bitmap;
        } catch (Exception e) {
            e.printStackTrace();

        }
        return null;
    }

    public static Bitmap getBitmapByView(NestedScrollView scrollView, boolean saveFile) throws OutOfMemoryError {
        int h = 0;
        Bitmap bitmap;
        View drawView = null;
        for (int i = 0; i < scrollView.getChildCount(); i++) {
            View view = scrollView.getChildAt(i);
            drawView = view;
            h += view.getHeight();
        }

        bitmap = ImageLoader.getInstance().get(scrollView.getContext(), scrollView.getWidth(), h, Bitmap.Config.RGB_565);
        final Canvas canvas = new Canvas(bitmap);
        canvas.drawColor(Color.parseColor("#ffffff"));
        if (drawView != null) {
            drawView.draw(canvas);
        } else {
            scrollView.draw(canvas);
        }
        if (saveFile) {
            File dir = new File(AppUtil.getCacheDir(scrollView.getContext()));
            if (!dir.exists()) {
                dir.mkdirs();
            }
            File file = new File(dir, System.currentTimeMillis() + ".jpg");
            if (file.exists()) {
                file.delete();
            }
            try {
                file.createNewFile();
                FileOutputStream out = new FileOutputStream(file);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return bitmap;
    }

    /**
     * 图片去色，返回灰度图片
     *
     * @param bitmap
     * @return
     */
    public static Bitmap toGrayScale(Context context, Bitmap bitmap, boolean recylce) {
        if (bitmap != null) {
            try {
                Bitmap result = ImageLoader.getInstance().get(context, bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
                Canvas canvas = new Canvas(result);
                ColorMatrix cm = new ColorMatrix();
                cm.setSaturation(0);
                ColorMatrixColorFilter f = new ColorMatrixColorFilter(cm);
                Paint paint = new Paint();
                paint.setColorFilter(f);
                canvas.drawBitmap(bitmap, 0, 0, paint);
                if (recylce) {
                    bitmap.recycle();
                }
                return result;
            } catch (Exception e) {
                SmLogger.d(e.getMessage());
                System.gc();
            }
        }
        return bitmap;
    }

    /**
     * compress是对成像质量做处理，并不能真正意义上的减少像素点.
     * 所以这里使用了缩放
     *
     * @param image
     * @param scaleRatio 一般赋值10左右
     * @return
     */
    public static Bitmap bitmapScaled(Bitmap image, float scaleRatio) {
        int width = (int) (image.getWidth() / scaleRatio);
        int height = (int) (image.getHeight() / scaleRatio);
        return Bitmap.createScaledBitmap(image, width, height, true);
    }

    public static Bitmap bitmapScaled(Bitmap image, int width, int height) {
        if (image == null || image.isRecycled()) {
            return null;
        }
        Size size = getScaleSize(image.getWidth(), image.getHeight(), width, height);
        Bitmap bitmap = Bitmap.createScaledBitmap(image, size.width, size.height, true);
        image.recycle();
        if (bitmap.isRecycled()) {
            return null;
        }
        return compressImage(bitmap, null, bitmap.getConfig(), 100);
    }

    private static Size getScaleSize(int width, int height, int scaleW, int scaleH) {
        int max = Math.max(height, width);
        int outHeight, outWidth;
        if (max == height) {
            //高图
            int tempHeight = height <= scaleH ? height : scaleH;
            outWidth = width * tempHeight / height;
            outHeight = tempHeight;
        } else {  //宽图
            int tempWidth = width <= scaleW ? width : scaleW;
            outHeight = height * tempWidth / width;
            outWidth = tempWidth;
        }
        Size size = new Size(outWidth, outHeight);
        return size;
    }

    public static class Size {
        int width;
        int height;

        public Size(int w, int h) {
            width = w;
            height = h;
        }
    }

    public static String bitmapScaled(String originalPath, int width, int height) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(originalPath, options);
        Size size = getScaleSize(options.outWidth, options.outHeight, width, height);
        Bitmap bitmap = null, result = null;
        String finalPath = "";
        try {
            options.inSampleSize = computeSampleSize(options.outWidth, options.outHeight, Math.max(width, height), true);
            options.inJustDecodeBounds = false;
            FileInputStream in = new FileInputStream(originalPath);
            bitmap = BitmapFactory.decodeFileDescriptor(in.getFD(), null, options);
            result = Bitmap.createScaledBitmap(bitmap, size.width, size.height, true);
            String path = AppUtil.getAppCachePath(NetLib.getContext()) + "/images_zip";
            File dir = new File(path);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            int index = originalPath.lastIndexOf("/");
            String name;
            if (index != -1) {
                name = originalPath.substring(index + 1, originalPath.length());
            } else {
                name = String.valueOf(System.currentTimeMillis() + ".jpg");
            }
            finalPath = dir.getPath() + "/" + name;
            FileOutputStream outputStream = new FileOutputStream(finalPath);
            result.compress(Bitmap.CompressFormat.JPEG, 90, outputStream);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (bitmap != null && !bitmap.isRecycled()) {
                bitmap.recycle();
            }
            if (result != null && !result.isRecycled()) {
                result.recycle();
            }
        }
        return finalPath;
    }


    public static Bitmap drawableToBitmap(Context context, Drawable drawable) {
        Bitmap bitmap = ImageLoader.getInstance().get(context, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
        drawable.draw(canvas);
        return bitmap;

    }

    public static void saveBitmapLocal(@NonNull final Context context, @Nullable final Bitmap bitmap, @NotNull final Observer<String> subscriber) {
        if (bitmap == null) {
            subscriber.onError(new ReturnJsonCodeException("图片保存失败"));
            return;
        }
        RxAsyncHandler handler = new RxAsyncHandler<String>() {

            @Override
            public String doSomethingBackground() {
                //本地保存路径
                File dirFile = new File(AppUtil.getAppExtraDCIMDir(context));
                if (!dirFile.exists()) {
                    dirFile.mkdirs();
                }
                FileOutputStream outputStream = null;
                String finalPath;
                try {
                    finalPath = dirFile.getPath() + File.separator + "sm" + System.currentTimeMillis() + ".jpg";
                    outputStream = new FileOutputStream(finalPath);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    finalPath = "";
                }
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream);
                return finalPath;
            }

            @Override
            public void onNext(String data) {
                if (TextUtils.isEmpty(data)) {
                    subscriber.onError(new ReturnJsonCodeException("保存失败"));
                } else {
                    //通知相册刷新
                    notifySystemMediaUpdate(context, data);
                    subscriber.onNext(data);
                }
            }
        };
        handler.execute();
    }

    /**
     * 通知系统相册 图片刷新
     *
     * @param context
     */
    private static void notifySystemMediaUpdate(Context context, String path) {
        /*  String massage = "来自" + context.getString(R.string.app_name) + "的图片";
          MediaStore.Images.Media.insertImage(context.getContentResolver(), path,
                  FileUtil.getNameFromUrl(path), massage);*/
        Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        Uri uri = Uri.fromFile(new File(path));
        intent.setData(uri);
        context.sendBroadcast(intent);
    }

    public Bitmap decodeFile(File f, int width, int height) {
        updateLastModifiedForCache(f);
        int suggestedSize = height;
        if (width > height) {
            suggestedSize = width;
        }
        Bitmap unscaledBitmap = decodeFile(f, suggestedSize);
        if (unscaledBitmap == null) {
            return null;
        }
        return unscaledBitmap;
    }


    private void updateLastModifiedForCache(File f) {
        f.setLastModified(System.currentTimeMillis());
    }

    private Bitmap decodeFile(File f, int suggestedSize) {
        Bitmap bitmap = null;
        FileInputStream fis = null;
        try {
            int scale = evaluateScale(f, suggestedSize);
            final BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = scale;
            options.inTempStorage = new byte[BUFFER_SIZE];
            options.inPurgeable = true;
            fis = new FileInputStream(f);
            bitmap = BitmapFactory.decodeStream(fis, null, options);
        } catch (final Throwable e) {
            // calling gc does not help as is called anyway
            // http://code.google.com/p/android/issues/detail?id=8488#c80
            // System.gc();
        } finally {
            closeSilently(fis);
        }
        return bitmap;
    }

    private int evaluateScale(File f, int suggestedSize) {
        final BitmapFactory.Options o = new BitmapFactory.Options();
        o.inJustDecodeBounds = true;
        decodeFileToPopulateOptions(f, o);
        return calculateScale(suggestedSize, o.outWidth, o.outHeight);
    }

    private void decodeFileToPopulateOptions(File f, final BitmapFactory.Options o) {
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(f);
            BitmapFactory.decodeStream(fis, null, o);
            closeSilently(fis);
        } catch (final Throwable e) {
            // calling gc does not help as is called anyway
            // http://code.google.com/p/android/issues/detail?id=8488#c80
            // System.gc();
        } finally {
            closeSilently(fis);
        }
    }

    private void closeSilently(Closeable c) {
        try {
            if (c != null) {
                c.close();
            }
        } catch (Exception ignored) {
        }
    }

    private int calculateScale(final int requiredSize, int widthTmp, int heightTmp) {
        int scale = 1;
        while (true) {
            if ((widthTmp / 2) < requiredSize || (heightTmp / 2) < requiredSize) {
                break;
            }
            widthTmp /= 2;
            heightTmp /= 2;
            scale *= 2;
        }
        return scale;
    }

    /**
     * 获取android标准的Uri索引
     *
     * @param context
     * @param uri
     * @return
     */
    public static String getFilePathFromUri(Context context, Uri uri) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            return GetPathFromUri4kitkat.getPath(context, uri);
        } else {
            String path = "";
            if (uri.getScheme().equals("file")) {
                path = uri.getPath();
            } else {
                if (!uri.getScheme().equals("content")) {
                    path = "";
                } else {
                    Cursor exifInterface = context.getContentResolver()
                            .query(uri, new String[]{"_data"}, null, null, null);
                    if (exifInterface != null) {
                        exifInterface.moveToFirst();
                        path = exifInterface.getString(0);
                        exifInterface.close();
                    }

                }
            }
            return path;
        }
    }

    public static String doFile2Base64(String path) {
        String videoStr = null;
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            FileInputStream fis = new FileInputStream(new File(path));

            byte[] buf = new byte[1024];
            int n;
            while (-1 != (n = fis.read(buf))) {
                baos.write(buf, 0, n);
            }
            videoStr = Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT);
        } catch (Exception e) {
            // TODO: startActivity exception
        }
        return videoStr;
    }

    /**
     * 将图片转换成Base64编码字符串
     *
     * @param path 图片的路径
     * @return
     */
    public static String encodeImage2Base64(String path, int maxSize) {
        //decode to bitmap
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);
        int height = options.outHeight;
        int width = options.outWidth;
        //限定图片大小
        options.inSampleSize = computeSampleSize(width, height, maxSize, true);
        options.inJustDecodeBounds = false;

        try {
            FileInputStream inputStream = new FileInputStream(path);
            Bitmap bitmap = BitmapFactory.decodeFileDescriptor(inputStream.getFD(), null, options);
            return encodeBitmap(bitmap);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    public static Bitmap decodeFile(String path) {
        return decodeFile(path, MAX_SIZE, MAX_SIZE);
    }

    public static Bitmap decodeFile(String path, int maxW, int maxH) {
        BitmapFactory.Options newOpts = new BitmapFactory.Options();
        //开始读入图片，此时把options.inJustDecodeBounds 设回true了
        newOpts.inJustDecodeBounds = true;
        //此时返回bm为空
        BitmapFactory.decodeFile(path, newOpts);
        newOpts.inJustDecodeBounds = false;
        int w = newOpts.outWidth;
        int h = newOpts.outHeight;


        //缩放比。由于是固定比例缩放，只用高或者宽其中一个数据进行计算即可
        //be=1表示不缩放
        int be = 1;
        if (w > h && w > maxW) {
            //如果宽度大的话根据宽度固定大小缩放
            be = (newOpts.outWidth / maxW);
        } else if (w < h && h > maxH) {
            //如果高度高的话根据宽度固定大小缩放
            be = (newOpts.outHeight / maxH);
        }
        if (be <= 0) {
            be = 1;
        }
        //设置缩放比例
        newOpts.inSampleSize = be;
        newOpts.inPreferredConfig = Bitmap.Config.ARGB_8888;
        //重新读入图片，注意此时已经把options.inJustDecodeBounds 设回false了
        FileInputStream in;
        try {
            in = new FileInputStream(path);
            Bitmap bitmap = BitmapFactory.decodeFileDescriptor(in.getFD(), null, newOpts);
            return compressImage(bitmap, path, newOpts.inPreferredConfig, 100);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 图片压缩，并指定不超过多少KB
     *
     * @param source 源文件地址
     * @param maxW
     * @param maxH
     * @param kBSize 文件占用大小，-1为不限制
     * @return
     */
    public static String compressImage(String source, int maxW, int maxH, int kBSize) {
        File sourceFile = new File(source);
        if (!sourceFile.exists() || !sourceFile.isFile()) {
            return source;
        }
        Bitmap b = Bitmap.createScaledBitmap(BitmapFactory.decodeFile(source), maxW, maxH, true);
        if (b.getHeight() <= maxH || b.getWidth() <= maxW) {
            return source;
        }
        if (sourceFile.length() / 1024 <= kBSize) {
            return source;
        }
        return compressImage(b, maxW, maxH, kBSize);
    }

    public static String compressImage(Bitmap source, int maxW, int maxH, int kbSize) {
        Bitmap result;
        if (source.getHeight() <= maxH || source.getWidth() <= maxW) {
            result = source;
        } else {
            result = compressImage(source, "", source.getConfig(), kbSize);
        }
        String path = AppUtil.getAppCachePath(NetLib.getContext()) + "/images_zip";
        File dir = new File(path);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        String name = "temp_view_reveal_bitmap.jpg";
        String finalPath = dir.getPath() + "/" + name;
        File file = new File(finalPath);
        if (file.exists()) {
            file.delete();
        }
        try {
            file.createNewFile();
            FileOutputStream outputStream = new FileOutputStream(file);
            result.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
            return finalPath;
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
    }

    /**
     * 将图片压制到100K
     *
     * @param image
     * @param file  源文件路径，根据图片的信息旋转图片
     * @param maxKb 文件大小上限
     * @return
     */
    private static Bitmap compressImage(Bitmap image, @Nullable String file, Bitmap.Config config, int maxKb) {
        if (image == null || maxKb == -1) {
            return image;
        }
        if (!TextUtils.isEmpty(file)) {
            int angle = readPictureDegree(file);
            if (angle != 0) {
                image = RotateBmp(angle, image);
            }
        }
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 1;
        options.inPreferredConfig = config;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        int quality = 100;
        while (baos.toByteArray().length / 1024 > maxKb) {
            baos.reset();//重置baos即清空baos
            image.compress(Bitmap.CompressFormat.JPEG, quality, baos);
            quality -= 10;
        }
        image.recycle();
        ByteArrayInputStream inputStream = new ByteArrayInputStream(baos.toByteArray());
        Bitmap bitmap = BitmapFactory.decodeStream(inputStream, null, null);
        SmLogger.d("bitmap 压制后大小：" + bitmap.getByteCount());
        return bitmap;
    }

    /**
     * 读取照片exif信息中的旋转角度
     *
     * @param path 照片路径
     * @return角度          
     */

    public static int readPictureDegree(String path) {
        int degree = 0;
        try {
            ExifInterface exifInterface = new ExifInterface(path);
            int orientation = exifInterface.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL);
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    degree = 90;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    degree = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    degree = 270;
                    break;
                default:
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return degree;
    }

    public static Bitmap RotateBmp(int angle, Bitmap bmp) {
        // 下面的方法主要作用是把图片转一个角度，也可以放大缩小等
        Matrix m = new Matrix();
        int width = bmp.getWidth();
        int height = bmp.getHeight();
        m.setRotate(angle);
        bmp = Bitmap.createBitmap(bmp, 0, 0, width, height, m, true);// 从新生成图片
        return bmp;
    }

    public static String encodeBitmap(Bitmap bitmap) {
        if (bitmap == null) {
            return null;
        }
        SmLogger.d("bitmap width: " + bitmap.getWidth() + " height: " + bitmap.getHeight());

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] bytes = baos.toByteArray();

        //base64 encode
        byte[] encode = Base64.encode(bytes, Base64.DEFAULT);
        if (!bitmap.isRecycled()) {
            bitmap.recycle();
        }
        return new String(encode);
    }

    /**
     * @param url
     * @param filePath 本地保存路径。可以为null，表示保存在应用文件目录
     * @param observer
     */
    public static void saveBitmapLocal(final Context context, final String url, String filePath, Observer<String> observer) {
        if (TextUtils.isEmpty(filePath)) {
            filePath = AppUtil.getAppCachePath(NetLib.getContext()) + "/launch";
        }
        if (TextUtils.isEmpty(url)) {
            observer.onError(new ReturnJsonCodeException("没有图片"));
        }
        final String realFilePath = filePath;
        final OkHttpClient client = OkHttpUtil.getOkHttpClient();
        Observable.create(new ObservableOnSubscribe<String>() {
            @Override
            public void subscribe(ObservableEmitter<String> e) throws Exception {
                Request request = new Request.Builder()
                        .url(url)
                        .build();
                InputStream input = null;
                OutputStream out = null;
                //保存到应用目录下
                String name = url.substring(url.lastIndexOf("/") + 1, url.length());
                File imageFile = new File(realFilePath, name);
                if (!imageFile.getParentFile().exists()) {
                    imageFile.getParentFile().mkdirs();
                }
                if (imageFile.exists()) {
                    imageFile.delete();
                }
                imageFile.createNewFile();
                try {
                    Response response = client.newCall(request).execute();
                    input = response.body().byteStream();
                    byte[] data = readStream(input);
                    Bitmap bitmap;
                    if (data != null) {
                        bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                        out = new FileOutputStream(imageFile);
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
                        e.onNext(imageFile.getPath());
                        e.onComplete();
                    } else {
                        throw Exceptions.propagate(new Throwable("图片下载失败"));
                    }
                    notifySystemMediaUpdate(context, imageFile.getPath());
                    response.close();
                } catch (IOException io) {
                    throw Exceptions.propagate(io);
                } finally {
                    if (input != null) {
                        try {
                            input.close();
                        } catch (IOException io) {
                            io.printStackTrace();
                        }
                    }
                    if (out != null) {
                        try {
                            out.close();
                        } catch (IOException io) {
                            io.printStackTrace();
                        }
                    }
                }
            }
        }).subscribeOn(io.reactivex.schedulers.Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(observer);

    }

    /**
     * @param url
     * @param filePath 文件保存的目录
     * @return 返回本地图片地址
     */
    public static String saveBitmapLocal(final String url, String filePath) {
        if (TextUtils.isEmpty(filePath)) {
            filePath = AppUtil.getAppCachePath(NetLib.getContext()) + "/image";
        }
        if (TextUtils.isEmpty(url)) {
            return "";
        }
        final String realFilePath = filePath;
        final OkHttpClient client = OkHttpUtil.getOkHttpClient();
        Request request = new Request.Builder()
                .url(url)
                .build();
        InputStream input = null;
        OutputStream out = null;
        //保存到应用目录下
        String name = url.substring(url.lastIndexOf("/") + 1, url.length());
        File imageFile = new File(realFilePath, name);
        if (!imageFile.getParentFile().exists()) {
            imageFile.getParentFile().mkdirs();
        }
        if (imageFile.exists()) {
            imageFile.delete();
        }

        try {
            boolean create = imageFile.createNewFile();
            if (!create) {
                SmLogger.e("网络图片保存失败，文件创建失败");
                return "";
            }
            Response response = client.newCall(request).execute();
            input = response.body().byteStream();
            out = new FileOutputStream(imageFile);
            byte[] buffer = new byte[1024];
            int index = 0;
            while ((index = input.read(buffer)) != 1) {
                out.write(buffer, 0, index);
            }
            out.flush();
            response.close();
            return imageFile.getPath();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException io) {
                    io.printStackTrace();
                }
            }
            if (out != null) {
                try {
                    out.close();
                } catch (IOException io) {
                    io.printStackTrace();
                }
            }
        }
        return "";
    }

    /*
     * 得到图片字节流 数组大小
     * */
    public static byte[] readStream(InputStream inStream) throws Exception {
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int len;
        while ((len = inStream.read(buffer)) != -1) {
            outStream.write(buffer, 0, len);
        }
        outStream.close();
        inStream.close();
        return outStream.toByteArray();
    }

    public static void base64toFile(String filePath, String base64) throws IOException {
        byte[] bitmapArray;
        bitmapArray = Base64.decode(base64, Base64.DEFAULT);
        File file = new File(filePath);
        if (!file.exists()) {
            File dir = file.getParentFile();
            if (!dir.exists()) {
                dir.mkdirs();
            }
            file.createNewFile();
        }
        FileOutputStream fileOutputStream = new FileOutputStream(file);
        fileOutputStream.write(bitmapArray);
        fileOutputStream.flush();
    }


    /**
     * 这里因为使用的是二次Bitmap编码,所以使用prevPowerOf2往小的值取
     *
     * @param w
     * @param h
     * @param maxSize
     * @param saveMemory 省内存模式:
     *                   <p>
     *                   true: 计算SampleSize时,往上取值尽可能大.则最终图片最长边 尺寸
     *                   小于等于maxSize(小概率会大于maxSize,如输入某些特殊尺寸时,见
     *                   {@link #nextPowerOf2(int)})
     *                   <p>
     *                   false: 计算SampleSize时,往下取值尽可能小.则最终图片最长边 尺寸
     *                   大于等于maxSize(没见过小于maxSize的情况)
     * @return
     */
    @SuppressWarnings("unused")
    public static int computeSampleSize(final int w, final int h, final double maxSize, final boolean saveMemory) {
        final int initialSize = (int) Math.max(w / maxSize, h / maxSize);
        //原来: 这里因为使用的是二次Bitmap编码,所以使用prevPowerOf2往小的值取,这样第一次取到的Bitmap大小肯定大于maxSize
        //现在: 这里因为使用的是二次Bitmap编码[支持缩小和扩大图片],所以使用 nextPowerOf2 取尽量小的值,肯定[小于]maxSize,这时,再将其[扩大]maxSize即可!
        //     经过测试test,最终生成的图片:
        //      小于3000以上的像素原图: 生成的新图文件[几乎等于]使用prevPowerOf2生成的新图片,即两文件MD5值大概率情况下都会相等.
        //      大于3000以上的像素原图: 生成的新图文件大小略小于使用prevPowerOf2生成的新图片,使用BCompare图片对比容差25时,看不出多少差异.
        if (saveMemory) {
            return nextPowerOf2(initialSize);
        }
        return prevPowerOf2(initialSize);
    }

    /**
     * 在不进行二次Bitmap编码大小的情况下,可直接使用此nextPowerOf2获得更高的采样比值,以便缩小为更小的Bitmap
     * 但是可能不太稳定,如原图为1024x768时,生成的目标尺寸也大的,小的都有.
     * // Returns the next power of two.
     * // Returns the input if it is already power of 2.
     * // Throws IllegalArgumentException if the input is <= 0 or
     * // the answer overflows.
     * <p>
     * <p>
     * 最终值往大的取(重采样得的图片尺寸越小)
     *
     * @param n
     * @return
     */
    private static int nextPowerOf2(int n) {
        if (n <= 0 || n > (1 << 30)) {
            return 1;
        }
        n -= 1;
        n |= n >> 16;
        n |= n >> 8;
        n |= n >> 4;
        n |= n >> 2;
        n |= n >> 1;
        return n + 1;
    }

    /**
     * 1.这里追求的目标是:尽可能的接近目标MaxSize的大小,可以大于等于MaxSize大小
     * <p>
     * 2.第二步再将接近MaxSize大小的Bitmap再次真正编码为边长为MaxSize大小的Bitmap!
     * 最终值往小的取(重采样得的图片尺寸越大)
     *
     * @param n
     * @return
     */
    private static int prevPowerOf2(final int n) {
        if (n <= 0) {
            return 1;
        }
        return Integer.highestOneBit(n);
    }


}