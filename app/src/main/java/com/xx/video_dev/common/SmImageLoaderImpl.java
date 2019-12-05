package com.xx.video_dev.common;


import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.load.resource.gif.GifDrawable;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.transition.Transition;
import com.mrkj.lib.net.impl.RxAsyncHandler;
import com.xx.lib.db.entity.SmContextWrap;
import com.xx.module.common.imageload.BitmapUtil;
import com.xx.module.common.imageload.IImageLoader;
import com.xx.module.common.imageload.ImageLoaderListener;
import com.xx.module.common.imageload.glide.GlideLoadProgressListener;
import com.xx.module.common.imageload.glide.ProgressContentLengthInputStream;
import com.xx.video_dev.R;
import com.xx.video_dev.common.glide.GlideBlurTransformation;
import com.xx.video_dev.common.glide.GlideCircleTransform;
import com.xx.video_dev.common.glide.GlideRoundTransform;
import com.xx.video_dev.common.glide.MaskTransform;
import com.xx.video_dev.common.glide.SimpleTransformation;

/**
 * 全局图片加载
 *
 * 2018/01/17
 */
public class SmImageLoaderImpl implements IImageLoader {

    private static SmImageLoaderImpl imageLoader = null;
    private boolean transitionAnimation = true;

    private RequestOptions getNormalOptions(ImageView view) {
        RequestOptions normalOptions = new RequestOptions().placeholder(R.drawable.icon_default_round)
                .error(R.drawable.icon_default_round);
        if (view == null) {
            return normalOptions.transform(new SimpleTransformation())
                    .override(Target.SIZE_ORIGINAL)
                    .skipMemoryCache(true);
        }
        boolean skipMemory = Build.VERSION.SDK_INT < Build.VERSION_CODES.M;
        ViewGroup.LayoutParams lp = view.getLayoutParams();
        if (lp == null || lp.height < 0 || lp.width < 0) {
            return normalOptions.transform(new SimpleTransformation())
                    .override(Target.SIZE_ORIGINAL)
                    .skipMemoryCache(skipMemory);
        } else {
            return normalOptions.transform(new SimpleTransformation())
                    .override(lp.width, lp.height)
                    .skipMemoryCache(skipMemory);
        }
    }


    private RequestOptions getNormalCircleOptions(Context context, ImageView view) {
        ViewGroup.LayoutParams lp = view.getLayoutParams();
        boolean skipMemory = Build.VERSION.SDK_INT < Build.VERSION_CODES.M;
        RequestOptions normalCircleOptions = new RequestOptions().placeholder(R.drawable.icon_default_vertical)
                .error(R.drawable.icon_default_vertical);
        if (lp.height < 0 || lp.width < 0) {
            return normalCircleOptions
                    .transform(new GlideCircleTransform(context, 0, 0))
                    .override(Target.SIZE_ORIGINAL)
                    .skipMemoryCache(skipMemory);
        } else {
            return normalCircleOptions
                    .transform(new GlideCircleTransform(context, 0, 0))
                    .override(lp.width, lp.height)
                    .skipMemoryCache(skipMemory);
        }
    }

    private SmImageLoaderImpl() {
    }

    public static SmImageLoaderImpl getInstance() {
        if (imageLoader == null) {
            imageLoader = new SmImageLoaderImpl();
        }
        return imageLoader;
    }

    private boolean checkNotNull(SmContextWrap contextWrap) {
        if (contextWrap == null) {
            return false;
        } else if (contextWrap.getFragment() != null) {
            return contextWrap.getFragment().isAdded();
        } else if (contextWrap.getActivity() != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                return !contextWrap.getActivity().isDestroyed();
            } else {
                return !contextWrap.getActivity().isFinishing();
            }
        } else {
            if (contextWrap.getContext() != null && contextWrap.getContext() instanceof Activity) {
                Activity activity = (Activity) contextWrap.getContext();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                    return !activity.isDestroyed();
                } else {
                    return !activity.isFinishing();
                }
            } else {
                return false;
            }
        }
    }


    private boolean checkNotNull(SmContextWrap contextWrap, ImageView view) {
        if (contextWrap == null || view == null) {
            return false;
        } else if (contextWrap.getFragment() != null) {
            return contextWrap.getFragment().isAdded();
        } else if (contextWrap.getActivity() != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                return !contextWrap.getActivity().isDestroyed();
            } else {
                return !contextWrap.getActivity().isFinishing();
            }
        } else {
            if (contextWrap.getContext() != null && contextWrap.getContext() instanceof Activity) {
                Activity activity = (Activity) contextWrap.getContext();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                    return !activity.isDestroyed();
                } else {
                    return !activity.isFinishing();
                }
            }
            return contextWrap.getContext() != null;
        }
    }

    private GlideRequests getRequest(SmContextWrap contextWrap) {
        if (contextWrap.getFragment() != null) {
            if (contextWrap.getFragment().getActivity() == null) {
                return null;
            }
            return GlideApp.with(contextWrap.getFragment());
        } else if (contextWrap.getActivity() != null) {
            return GlideApp.with(contextWrap.getActivity());
        } else {
            return GlideApp.with(contextWrap.getContext());
        }
    }

    @Override
    public void loadCircle(final SmContextWrap contextWrap, final String url, final ImageView imageView, final int defaultResId) {
        if (!checkNotNull(contextWrap, imageView)) {
            return;
        }
        final GlideRequests requests = getRequest(contextWrap);
        if (requests == null) {
            return;
        }
        if (!checkTag(imageView, url)) {
            requests.clear(imageView);
        }
        imageView.post(new Runnable() {
            @Override
            public void run() {
                GlideRequest<Drawable> request = requests.load(url)
                        .apply(getNormalCircleOptions(contextWrap.getContext(), imageView)
                                .placeholder(defaultResId).error(defaultResId)
                                .transform(new GlideCircleTransform(contextWrap.getContext(), 0, 0)));
                request = globalSetting(request);
                request.into(imageView);
            }
        });
    }

    private GlideRequest<Drawable> globalSetting(GlideRequest<Drawable> request) {
        if (transitionAnimation) {
            request = request.transition(DrawableTransitionOptions.withCrossFade());
        }
        return request;
    }

    @Override
    public void loadCircleResource(Context context, int resId, ImageView view) {
        SmContextWrap contextWrap = SmContextWrap.obtain(context);
        if (!checkNotNull(contextWrap, view)) {
            return;
        }
        GlideRequests requests = getRequest(contextWrap);
        if (requests == null) {
            return;
        }
        GlideRequest<Drawable> request = requests.load(resId)
                .apply(getNormalCircleOptions(contextWrap.getContext(), view)
                        .transform(new GlideCircleTransform(contextWrap.getContext(), 0, 0)));
        request = globalSetting(request);
        request.into(view);
    }

    @Override
    public void loadResource(Context context, int resId, @org.jetbrains.annotations.Nullable ImageView imageView) {
        SmContextWrap contextWrap = SmContextWrap.obtain(context);
        if (!checkNotNull(contextWrap, imageView)) {
            return;
        }
        GlideRequests requests = getRequest(contextWrap);
        if (requests == null) {
            return;
        }
        GlideRequest<Drawable> request = requests.load(resId)
                .apply(getNormalOptions(imageView).placeholder(0).error(0));
        request = globalSetting(request);
        request.into(imageView);
    }

    @Override
    public void clear(Context context) {
        Glide.get(context).clearMemory();
    }

    @Override
    public Bitmap get(Context context, int width, int height, Bitmap.Config config) {
        return Glide.get(context).getBitmapPool().get(width, height, config);
    }

    @Override
    public void loadResource(final Context context, final int resId, final int w, final int h,
                             @org.jetbrains.annotations.Nullable final ImageLoaderListener<Drawable> listener) {
        if (listener == null) {
            return;
        }
        new RxAsyncHandler<Bitmap>(context) {
            @Override
            public Bitmap doSomethingBackground() {
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true;
                BitmapFactory.decodeResource(context.getResources(), resId, options);
                int height = options.outHeight;
                int width = options.outWidth;
                if (h > 0 && w > 0) {
                    //限定图片大小
                    options.inSampleSize = computeSampleSize(width, height, Math.max(w, h), true);
                }
                options.inJustDecodeBounds = false;
                return BitmapFactory.decodeResource(context.getResources(), resId, options);
            }

            @Override
            public void onNext(Bitmap data) {
                listener.onSuccess(new BitmapDrawable(context.getResources(), data));
            }

            @Override
            public void onError(Throwable e) {
                super.onError(e);
                listener.onLoadFailed();
            }
        }.execute();

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
    private static int computeSampleSize(final int w, final int h, final double maxSize, final boolean saveMemory) {
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

    @Override
    public void loadCircleBound(SmContextWrap contextWrap, String url, ImageView imageview, int colorId,
                                int widthDp, int defaultResId) {
        if (!checkNotNull(contextWrap, imageview)) {
            return;
        }
        GlideRequests requests = getRequest(contextWrap);
        if (requests == null) {
            return;
        }
        if (!checkTag(imageview, url)) {
            requests.clear(imageview);
        }
        GlideRequest<Drawable> request = requests.load(url)
                .apply(getNormalCircleOptions(contextWrap.getContext(), imageview)
                        .placeholder(defaultResId).error(defaultResId)
                        .transform(new GlideCircleTransform(contextWrap.getContext(),
                                ContextCompat.getColor(contextWrap.getContext(), colorId), widthDp)));
        request = globalSetting(request);
        request.into(imageview);
    }

    @Override
    public void loadCircleBoundResource(Context context, int resId, ImageView imageview, int colorId, int widthDp) {
        SmContextWrap contextWrap = SmContextWrap.obtain(context);
        if (!checkNotNull(contextWrap, imageview)) {
            return;
        }
        GlideRequests requests = getRequest(contextWrap);
        if (requests == null) {
            return;
        }
        GlideRequest<Drawable> request = requests.load(resId)
                .apply(getNormalCircleOptions(contextWrap.getContext(), imageview)
                        .transform(new GlideCircleTransform(contextWrap.getContext(),
                                ContextCompat.getColor(contextWrap.getContext(), colorId), widthDp)));
        request = globalSetting(request);
        request.into(imageview);
    }

    @Override
    public void load(SmContextWrap contextWrap, final String url, final int defaultid, final ImageView imageview) {
        if (!checkNotNull(contextWrap, imageview)) {
            return;
        }
        final GlideRequests requests = getRequest(contextWrap);
        if (requests == null) {
            return;
        }
        if (!checkTag(imageview, url)) {
            requests.clear(imageview);
            imageview.post(new Runnable() {
                @Override
                public void run() {
                    GlideRequest<Drawable> request = requests.load(url)
                            .apply(getNormalOptions(imageview).placeholder(defaultid).error(defaultid));
                    request = globalSetting(request);
                    request.into(imageview);
                }
            });
        }
    }


    @Override
    public void loadGif(SmContextWrap contextWrap, final String url, int defaultResId, @Nullable final ImageLoaderListener<Drawable> listener) {
        if (listener == null || !checkNotNull(contextWrap)) {
            return;
        }
        GlideRequests requests = getRequest(contextWrap);
        if (requests == null) {
            return;
        }
        if (!checkTag(listener.getView(), url) && listener.getView() != null) {
            requests.clear(listener.getView());
        }
        final GlideLoadProgressListener progressListener = new GlideLoadProgressListener() {
            @Override
            public void onProgress(int progress) {
                listener.onProgress(progress);
            }
        };
        registerProgressListener(url, progressListener);
        requests.asGif()
                .load(url)
                .apply(getNormalOptions(listener.getView()).placeholder(defaultResId).error(defaultResId))
                .into(new SimpleTarget<GifDrawable>() {
                    @Override
                    public void onResourceReady(@NonNull GifDrawable resource, @Nullable Transition<? super GifDrawable> transition) {
                        unRegisterProgressListener(url, progressListener);
                        listener.onSuccess(resource);
                    }

                    @Override
                    public void onLoadFailed(@Nullable Drawable errorDrawable) {
                        super.onLoadFailed(errorDrawable);
                        unRegisterProgressListener(url, progressListener);
                        listener.onLoadFailed();
                    }
                });
    }

    @Override
    public void loadGif(SmContextWrap contextWrap, final String url, final int defaultResId, @Nullable final ImageView imageView) {
        if (!checkNotNull(contextWrap, imageView)) {
            return;
        }
        final GlideRequests requests = getRequest(contextWrap);
        if (requests == null) {
            return;
        }
        if (!checkTag(imageView, url)) {
            requests.clear(imageView);
        }
        imageView.post(new Runnable() {
            @Override
            public void run() {
                requests.asGif()
                        .load(url)
                        .apply(getNormalOptions(imageView).placeholder(defaultResId).error(defaultResId))
                        .into(imageView);
            }
        });
    }

    @Override
    public void loadGifUri(SmContextWrap contextWrap, final Uri uri, final ImageView imageView, final int defaultResId) {
        if (!checkNotNull(contextWrap, imageView)) {
            return;
        }
        final GlideRequests requests = getRequest(contextWrap);
        if (requests == null) {
            return;
        }
        imageView.post(new Runnable() {
            @Override
            public void run() {
                requests.asGif()
                        .load(uri)
                        .apply(getNormalOptions(imageView).placeholder(defaultResId).error(defaultResId))
                        .into(imageView);
            }
        });
    }


    private void load(SmContextWrap contextWrap, String url, final boolean clearImageView, final ImageLoaderListener<Drawable> listener) {
        if (listener == null || !checkNotNull(contextWrap)) {
            if (listener != null) {
                listener.onLoadFailed();
            }
            return;
        }
        GlideRequests requests = getRequest(contextWrap);
        if (requests == null) {
            return;
        }
        GlideRequest<Drawable> request = requests.load(url);
        if (clearImageView) {
            request = request.apply(getNormalOptions(listener.getView()).placeholder(0).error(0));
        } else if (listener.getView() != null) {
            Drawable d = listener.getView().getDrawable();
            request = request.apply(getNormalOptions(listener.getView()).placeholder(d).error(d));
        }

        request = globalSetting(request);
        request.into(new SimpleTarget<Drawable>() {
            @Override
            public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                listener.onSuccess(resource);
            }

            @Override
            public void onLoadFailed(@Nullable Drawable errorDrawable) {
                super.onLoadFailed(errorDrawable);
                listener.onLoadFailed();
            }
        });
    }


    @Override
    public void loadWithThumb(final SmContextWrap contextWrap, final String url, String thumb,
                              @NonNull final ImageLoaderListener<Drawable> listener) {
        if (!checkNotNull(contextWrap)) {
            listener.onLoadFailed();
            return;
        }
        GlideRequests requests = getRequest(contextWrap);
        if (requests == null) {
            return;
        }
        final GlideLoadProgressListener progressListener = new GlideLoadProgressListener() {
            @Override
            public void onProgress(int progress) {
                listener.onProgress(progress);
            }
        };
        registerProgressListener(url, progressListener);
        GlideRequest<Drawable> request = requests.load(url);
        request = globalSetting(request);
        request.load(url)
                .thumbnail(GlideApp.with(contextWrap.getContext()).load(thumb))
                .into(new SimpleTarget<Drawable>() {
                    @Override
                    public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                        unRegisterProgressListener(url, progressListener);
                        listener.onSuccess(resource);
                    }

                    @Override
                    public void onLoadFailed(@Nullable Drawable errorDrawable) {
                        super.onLoadFailed(errorDrawable);
                        unRegisterProgressListener(url, progressListener);
                        listener.onLoadFailed();

                    }
                });
    }

    @Override
    public void loadBlur(final SmContextWrap contextWrap, final String url, int radius, final int placeholder, final ImageView imageview) {
        if (!checkNotNull(contextWrap, imageview)) {
            return;
        }
        final GlideRequests requests = getRequest(contextWrap);
        if (requests == null) {
            return;
        }
        if (!checkTag(imageview, url)) {
            requests.clear(imageview);
        }
        if (radius == -1) {
            radius = 14;
        }
        final int finalRadius = radius;
        imageview.post(new Runnable() {
            @Override
            public void run() {
                GlideRequest<Drawable> request = requests.load(url)
                        .apply(getNormalOptions(imageview).placeholder(placeholder).error(placeholder))
                        .transform(new GlideBlurTransformation(contextWrap.getContext(), finalRadius,
                                3, false));
                request = globalSetting(request);
                request.into(imageview);
            }
        });

    }

    @Override
    public void load(SmContextWrap contextWrap, String url, int placeHolder, final ImageLoaderListener<Drawable> listener) {
        RequestManager manager = getRequestManager(contextWrap);
        if (!checkTag(listener.getView(), url) && listener.getView() != null) {
            manager.clear(listener.getView());
        }
        manager.load(url)
                .apply(getNormalOptions(listener.getView()).placeholder(placeHolder).error(placeHolder))
                .into(new SimpleTarget<Drawable>() {
                    @Override
                    public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                        listener.onSuccess(resource);
                    }

                    @Override
                    public void onLoadFailed(@Nullable Drawable errorDrawable) {
                        super.onLoadFailed(errorDrawable);
                        listener.onLoadFailed();
                    }
                });
    }

    @Override
    public void loadUri(final SmContextWrap contextWrap, final Uri uri, final ImageView imageview, final int defaultid) {
        if (!checkNotNull(contextWrap, imageview)) {
            return;
        }
        final GlideRequests requests = getRequest(contextWrap);
        if (requests == null) {
            return;
        }
        if (!checkTag(imageview, uri.getEncodedSchemeSpecificPart())) {
            requests.clear(imageview);
        }
        imageview.post(new Runnable() {
            @Override
            public void run() {
                GlideRequest<Drawable> request = requests.load(uri)
                        .apply(getNormalOptions(imageview).placeholder(defaultid).error(defaultid));
                request = globalSetting(request);
                request.into(imageview);
            }
        });
    }


    @Override
    public void loadUri(SmContextWrap contextWrap, Uri uri, int defaultid, final ImageLoaderListener<Drawable> listener) {
        if (!checkNotNull(contextWrap)) {
            if (listener != null) {
                listener.onLoadFailed();
            }
            return;
        }
        GlideRequests requests = getRequest(contextWrap);
        if (requests == null) {
            return;
        }
        GlideRequest<Drawable> request = requests.load(uri)
                .apply(getNormalOptions(listener.getView()).placeholder(defaultid).error(defaultid));
        request = globalSetting(request);
        request.into(new SimpleTarget<Drawable>() {
            @Override
            public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                listener.onSuccess(resource);
            }

            @Override
            public void onLoadFailed(@Nullable Drawable errorDrawable) {
                super.onLoadFailed(errorDrawable);
                listener.onLoadFailed();
            }
        });
    }


    @Override
    public void loadRound(SmContextWrap contextWrap, final String url, final ImageView imageview, final float radius, final int placeholder) {
        if (!checkNotNull(contextWrap, imageview)) {
            return;
        }
        final GlideRequests requests = getRequest(contextWrap);
        if (requests == null) {
            return;
        }
        if (!checkTag(imageview, url)) {
            requests.clear(imageview);
        }
        imageview.post(new Runnable() {
            @Override
            public void run() {
                GlideRequest<Drawable> request = requests.load(url)
                        .apply(getNormalOptions(imageview)
                                .placeholder(placeholder)
                                .error(placeholder)
                                .transform(new GlideRoundTransform(radius)));
                request = globalSetting(request);
                request.into(imageview);
            }
        });

    }

    @Override
    public void loadRound(SmContextWrap contextWrap, final String url, final ImageView imageview, final boolean admin, final float radius, final int placeholder) {
        if (!checkNotNull(contextWrap, imageview)) {
            return;
        }
        final GlideRequests requests = getRequest(contextWrap);
        if (requests == null) {
            return;
        }
        if (!checkTag(imageview, url)) {
            requests.clear(imageview);
        }
        imageview.post(new Runnable() {
            @Override
            public void run() {
                GlideRequest<Drawable> request = requests.load(url)
                        .apply(getNormalOptions(imageview)
                                .placeholder(placeholder)
                                .error(placeholder)
                                .transform(new GlideRoundTransform(radius)));
                request = globalSetting(request);
                if (!admin) {
                    request = request.dontAnimate();
                }
                request.into(imageview);
            }
        });
    }


    @Override
    public void loadTextView(final SmContextWrap contextWrap, final String tag, final TextView view, final ImageView imageView, final boolean isBlur) {
        if (!checkNotNull(contextWrap, imageView)) {
            return;
        }
        if (view == null) {
            return;
        }
        view.postDelayed(new Runnable() {
            @Override
            public void run() {
                final GlideRequests request = getRequest(contextWrap);
                if (request == null) {
                    return;
                }
                if (!checkTag(imageView, tag)) {
                    request.clear(imageView);
                }
                Drawable drawable = imageView.getDrawable();
                if (drawable == null || (drawable instanceof BitmapDrawable && ((BitmapDrawable) drawable).getBitmap() == null)) {
                    new RxAsyncHandler<Bitmap>() {
                        @Override
                        public Bitmap doSomethingBackground() {
                            return BitmapUtil.getBitmapFromView(view, 2);
                        }

                        @Override
                        public void onNext(Bitmap data) {
                            RequestOptions requestOptions = getNormalOptions(imageView).placeholder(0).error(0);
                            if (isBlur) {
                                int wR = (int) Math.ceil((double) data.getWidth() / 400);
                                int hR = (int) Math.ceil((double) data.getHeight() / 600);
                                int max = Math.max(wR, hR);
                                if (max <= 1) {
                                    max = 1;
                                }
                                requestOptions = requestOptions.transform(new GlideBlurTransformation(view.getContext(),
                                        10, max, true));
                            }
                            request.load(data)
                                    .apply(requestOptions)
                                    .into(imageView);
                        }
                    }.execute();
                }
            }
        }, 10);

    }

    @Override
    public void loadMask(SmContextWrap contextWrap, String url, int maskId, final ImageLoaderListener<Drawable> listener) {
        GlideRequests requests = getRequest(contextWrap);
        if (requests == null) {
            return;
        }
        GlideRequest<Drawable> request = requests.load(url)
                .apply(getNormalOptions(listener.getView())
                        .transform(new MaskTransform(contextWrap.getContext(), maskId)));
        request = globalSetting(request);
        request.into(new CustomTarget<Drawable>() {
            @Override
            public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                listener.onSuccess(resource);
            }

            @Override
            public void onLoadCleared(@Nullable Drawable placeholder) {

            }

            @Override
            public void onLoadFailed(@Nullable Drawable errorDrawable) {
                super.onLoadFailed(errorDrawable);
                listener.onLoadFailed();
            }
        });
    }

    @Override
    public void loadBlur(SmContextWrap contextWrap, String url, int radius, int placeholder,
                         final ImageLoaderListener<Drawable> listener) {
        if (contextWrap == null || listener == null) {
            if (listener != null) {
                listener.onLoadFailed();
            }
            return;
        }
        RequestManager manager = getRequestManager(contextWrap);
        if (!checkTag(listener.getView(), url) && listener.getView() != null) {
            manager.clear(listener.getView());
        }
        manager.load(url)
                .apply(getNormalOptions(listener.getView()).placeholder(placeholder).error(placeholder)
                        .transform(new GlideBlurTransformation(contextWrap.getContext(),
                                radius, 3, false)))
                .into(new SimpleTarget<Drawable>() {
                    @Override
                    public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                        listener.onSuccess(resource);
                    }

                    @Override
                    public void onLoadFailed(@Nullable Drawable errorDrawable) {
                        super.onLoadFailed(errorDrawable);
                        listener.onLoadFailed();
                    }
                });
    }


    @Override
    public void loadBlur(SmContextWrap contextWrap, Bitmap bitmap, int radius, int placeholder, final ImageLoaderListener<Drawable> listener) {
        if (contextWrap == null || listener == null) {
            if (listener != null) {
                listener.onLoadFailed();
            }
            return;
        }
        RequestManager manager = getRequestManager(contextWrap);
        manager.load(bitmap)
                .apply(getNormalOptions(listener.getView()).placeholder(placeholder).error(placeholder)
                        .transform(new GlideBlurTransformation(contextWrap.getContext(), radius,
                                3, false)))
                .into(new SimpleTarget<Drawable>() {
                    @Override
                    public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                        listener.onSuccess(resource);
                    }

                    @Override
                    public void onLoadFailed(@Nullable Drawable errorDrawable) {
                        super.onLoadFailed(errorDrawable);
                        listener.onLoadFailed();
                    }
                });
    }

    private RequestManager getRequestManager(SmContextWrap contextWrap) {
        RequestManager manager;
        if (contextWrap.getFragment() != null) {
            manager = GlideApp.with(contextWrap.getFragment());
        } else {
            manager = GlideApp.with(contextWrap.getActivity());
        }
        return manager;
    }


    @Override
    public void pause(SmContextWrap contextWrap) {
        GlideRequests requests = getRequest(contextWrap);
        if (requests != null) {
            requests.pauseRequests();
        }
    }

    @Override
    public void resume(SmContextWrap contextWrap) {
        GlideRequests requests = getRequest(contextWrap);
        if (requests != null) {
            requests.resumeRequests();
        }
    }

    @Override
    public void loadThumbnail(Context context, Drawable placeholder, ImageView imageView, Uri uri, int resize) {
        GlideApp.with(context)
                .asBitmap()
                .load(uri)
                .placeholder(placeholder)
                .override(resize, resize)
                .centerCrop()
                .into(imageView);
    }


    /**
     * @param imageview
     * @param url
     * @return tag匹配，返回true
     */
    @Override
    public boolean checkTag(View imageview, String url) {
        if (imageview == null) {
            return false;
        }
        Object tag = imageview.getTag(R.id.imageloader_url);
        if (tag instanceof String && TextUtils.equals(url, (String) tag)) {
            return true;
        } else {
            imageview.setTag(R.id.imageloader_url, url);
            if (imageview instanceof ImageView) {
                ((ImageView) imageview).setImageBitmap(null);
            }
            return false;
        }
    }


    @Override
    public void loadAnimatedGifThumbnail(Context context, int resize, Drawable placeholder, ImageView imageView, Uri uri) {
        GlideApp.with(context)
                .asBitmap()
                .load(uri)
                .placeholder(placeholder)
                .override(resize, resize)
                .centerCrop()
                .into(imageView);
    }

    @Override
    public void loadImage(Context context, int resizeX, int resizeY, ImageView imageView, Uri uri) {
        GlideApp.with(context)
                .load(uri)
                .apply(getNormalOptions(imageView).override(resizeX, resizeY).priority(Priority.HIGH))
                .into(imageView);
    }

    @Override
    public void loadAnimatedGifImage(Context context, int resizeX, int resizeY, ImageView imageView, Uri uri) {
        GlideApp.with(context)
                .asGif()
                .load(uri)
                .apply(getNormalOptions(imageView).override(resizeX, resizeY).priority(Priority.HIGH))
                .into(imageView);
    }


    public void registerProgressListener(String url, GlideLoadProgressListener loadProgressListener) {
        ProgressContentLengthInputStream.registerProgressListener(url, loadProgressListener);
    }

    public void unRegisterProgressListener(String url, GlideLoadProgressListener loadProgressListener) {
        ProgressContentLengthInputStream.unRegisterProgressListener(url, loadProgressListener);
    }
}
