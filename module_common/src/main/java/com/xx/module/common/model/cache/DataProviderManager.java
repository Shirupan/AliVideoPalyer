package com.xx.module.common.model.cache;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v4.util.ArrayMap;
import android.text.TextUtils;

import com.mrkj.lib.common.util.AppUtil;
import com.mrkj.lib.common.util.SmLogger;
import com.mrkj.lib.net.impl.RxAsyncHandler;
import com.mrkj.lib.net.retrofit.RetrofitManager;
import com.orhanobut.logger.Logger;
import com.xx.lib.db.entity.ReturnBeanJson;
import com.xx.lib.db.exception.SmCacheException;

import java.io.File;
import java.io.FileFilter;
import java.lang.ref.SoftReference;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.ObservableSource;
import io.reactivex.functions.Function;
import io.rx_cache2.internal.RxCache;
import io.victoralbertos.jolyglot.GsonSpeaker;

public class DataProviderManager {
    public static String NO_CACHE = "rx_cache_no_cache";
    public static String NO_NET = "rx_cache_empty_net";
    protected static File cacheFileDir;
    private final static Map<String, SoftReference<BaseCacheProvider>> PROVIDERS = new ArrayMap<>();
    private static Context mContext;

    public static <T extends BaseCacheProvider> T get(Class<T> classProvider) {
        SoftReference<BaseCacheProvider> reference;
        synchronized (PROVIDERS) {
            reference = PROVIDERS.get(classProvider.getName());
        }
        if (reference == null || reference.get() == null) {
            try {
                Constructor<T> constructor = classProvider.getConstructor();
                T data = constructor.newInstance();
                data.setCacheInterface(init(data.getCacheClass()));
                reference = new SoftReference(data);
                put(classProvider, reference);
                return data;
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
            throw new RuntimeException("classProvider need no params constructor");
        } else {
            return (T) reference.get();
        }

    }

    private static <T> void put(Class<T> classProvider, SoftReference<BaseCacheProvider> reference) {
        synchronized (PROVIDERS) {
            PROVIDERS.put(classProvider.getName(), reference);
        }
    }

    protected static File getCacheFileDir() {
        return cacheFileDir;
    }

    private static void createCacheFile() {
        if (mContext == null) {
            throw new NullPointerException("Please init DataProviderManager.");
        }
        if (mContext.getExternalCacheDir() == null) {
            return;
        }
        String path = mContext.getExternalCacheDir().getPath();
        cacheFileDir = new File(path, "rxCache");
        boolean created;
        if (!cacheFileDir.exists()) {
            created = cacheFileDir.mkdirs();
        } else {
            created = true;
        }
        if (!created) {
            cacheFileDir = null;
        }
    }

    public static void init(Context context) {
        mContext = context.getApplicationContext();
    }


    private static <KEY extends Cache> KEY init(Class<KEY> classInterface) {
        if (cacheFileDir == null) {
            synchronized (BaseCacheProvider.class) {
                if (cacheFileDir == null || !cacheFileDir.exists() || !cacheFileDir.isDirectory()) {
                    if (AppUtil.getSDFreeSize() >= 500) {
                        createCacheFile();
                    }
                }
            }
        }
        KEY data = null;
        if (cacheFileDir != null && cacheFileDir.exists()) {
            data = new RxCache.Builder()
                    .persistence(cacheFileDir, new GsonSpeaker())
                    .using(classInterface);
        }
        return data;
    }


    private static List<File> cacheFiles = new ArrayList<>();

    /**
     * 取得所有缓存的总大小
     *
     * @param c
     * @return Map对象有两个key：“size”大小，“type”单位
     */
    public static Map<String, String> getAllFilesSize(Context c) {
        //先清空容器
        cacheFiles.clear();
        Map<String, String> map = new HashMap<>();
        long size = 0;
        //缓存文件
        size += getCacheFilesSize(c);

        double kb, mb = 0, gb;
        kb = (double) (size / 1024);
        DecimalFormat df = new DecimalFormat("#.##");
        String sizeStr;
        String type;
        if (kb > 100) {
            mb = kb / 1024;
        }

        if (mb > 1024) {
            gb = mb / 1024;
            sizeStr = df.format(gb);
            type = "GB";
        } else {
            sizeStr = df.format(mb);
            type = "MB";
        }
        map.put("size", sizeStr);
        map.put("type", type);

        return map;
    }

    private static long getCacheFilesSize(Context c) {
        long size = 0;
        //手机内部空间
        File dir = c.getCacheDir();
        size += getFilesSize(dir);

        //外部储存空间
        dir = c.getExternalCacheDir();
        size += getFilesSize(dir);
        //sm文件夹

        return size;
    }

    private static long getFilesSize(File dir) {
        long size = 0;
        File[] files;
        if (dir == null) {
            return 0;
        }
        files = dir.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                if (pathname.getPath().contains("shared_prefs")) {
                    return false;
                }
                if (pathname.getPath().contains("databases")) {
                    return false;
                }
                return !pathname.getPath().endsWith(".dex");
            }
        });

        for (File f : files) {
            if (f.isDirectory()) {
                size += getFilesSize(f);
            } else {
                cacheFiles.add(f);
                size += f.length();
            }
        }
        return size;
    }

    public static void deleteDirFiles(Context context, final String dirPath, final Runnable callback) {
        if (TextUtils.isEmpty(dirPath)) {
            return;
        }
        new RxAsyncHandler<Boolean>() {
            @Override
            public Boolean doSomethingBackground() {
                File file = new File(dirPath);
                if (file.exists()) {
                    return delete(file);
                }
                return false;
            }

            @Override
            public void onNext(Boolean data) {
                if (callback != null) {
                    callback.run();
                }
            }
        }.execute();
    }

    private static boolean delete(File dir) {
        if (dir.isFile()) {
            return dir.delete();
        } else {
            File[] list = dir.listFiles();
            boolean delete = false;
            for (File temp : list) {
                delete = delete(temp);
            }
            return delete;
        }

    }

    public static void deleteFiles(final Context context, final Runnable callback) {
        new RxAsyncHandler<Boolean>(context) {
            @Override
            public Boolean doSomethingBackground() {
                getAllFilesSize(context);
                try {
                    for (File file : cacheFiles) {
                        file.delete();
                    }
                    SmLogger.d("Cache is cleaned over.");
                    cacheFiles.clear();
                } catch (NullPointerException e) {
                    cacheFiles = null;
                    SmLogger.d("Cache files is null.");
                }
                return true;
            }


            @Override
            public void onNext(Boolean data) {
                if (callback != null) {
                    callback.run();
                }
            }
        }.execute();
    }

    public static class Builder<T extends DataProviderManager.BaseCacheProvider> {
        private Observable<String> netObservable;
        private Observable<String> cacheObservable;
        private ICacheObservable<T> iCacheObservable;
        private boolean needCache;
        private T mProvider;

        public Builder(Class<T> provider) {
            mProvider = DataProviderManager.get(provider);
        }

        public Builder() {
        }

        public Builder<T> takeProvider(Class<T> provider) {
            mProvider = DataProviderManager.get(provider);
            return this;
        }

        /**
         * 数据来源
         *
         * @param observable
         * @return
         */
        public Builder<T> data(Observable<String> observable) {
            netObservable = observable;
            if (netObservable != null) {
                netObservable = netObservable.compose(RetrofitManager.<String>rxTransformer());
            }
            return this;
        }

        /**
         * 缓存来源
         *
         * @param observable
         * @return
         */
        public Builder<T> cache(ICacheObservable<T> observable) {
            iCacheObservable = observable;
            return this;
        }

        /**
         * 是否取本地缓存来显示
         *
         * @param loadCache
         * @return
         */
        public Builder<T> useCache(boolean loadCache) {
            needCache = loadCache;
            return this;
        }

        public Observable<String> build() {
            if (getCacheFileDir() == null) {
                if (netObservable != null) {
                    return netObservable;
                } else {
                    return Observable.error(new SmCacheException(NO_CACHE));
                }
            }
            //取disk缓存
            if (iCacheObservable != null && needCache) {
                cacheObservable = iCacheObservable
                        .onNetObservable(mProvider, null)
                        .compose(RetrofitManager.<String>rxTransformer());

            }
            //网络请求，并且添加到disk缓存中
            if (netObservable != null && iCacheObservable != null) {
                netObservable = iCacheObservable
                        .onNetObservable(mProvider, netObservable)
                        .map(new Function<String, String>() {
                            @Override
                            public String apply(String s) {
                                SmLogger.i("Data from net and save cache");
                                return s;
                            }
                        });
            }
            if (cacheObservable != null) {
                cacheObservable = cacheObservable.map(new Function<String, String>() {
                    @Override
                    public String apply(String s) throws Exception {
                        Logger.i("Data from cache");
                        return s;
                    }
                }).onErrorResumeNext(new Function<Throwable, ObservableSource<? extends String>>() {
                    @Override
                    public ObservableSource<? extends String> apply(Throwable throwable) {
                        return Observable.create(new ObservableOnSubscribe<String>() {
                            @Override
                            public void subscribe(ObservableEmitter<String> emitter) {
                                //这里有个坑(1)
                                //concat操作符连接多个数据源的时候，前一个数据源调用onComplete()方法，才会发射下一个数据源数据。
                                //另外，当onError调用之后(不管是任何抛出方式)，手动调用onComplete()方法也无效?
                                // emitter.onError(new SmCacheException(NO_CACHE));

                                //这里有个坑(2)
                                //onErrorResumeNext与onExceptionResumeNext的区别
                                //onExceptionResumeNext只接受Exception类型错误。Throwable类型错误还是会丢给onError方法回调
                                SmLogger.d("Data from cache EMPTY!!");
                                emitter.onComplete();
                            }
                        });
                    }
                });
                return Observable.concatDelayError(Arrays.asList(cacheObservable, netObservable));
            } else {
                return netObservable;
            }
        }

    }

    /**
     * @param <P>
     */
    public interface ICacheObservable<P extends DataProviderManager.BaseCacheProvider> {
        /**
         * 取缓存或者存缓存，根据{@param net}，若null则取缓存，否则是存缓存。并且都返回数据
         *
         * @param provider
         * @param net
         * @return 返回数据发射，可能为error（即数据为null且缓存为null）
         */
        Observable<String> onNetObservable(P provider, @Nullable Observable<String> net);

    }

    public interface IEvictDynamicHandler {
        Observable<String> cache(Observable<String> source, boolean save);
    }

    public static abstract class BaseCacheProvider<C extends Cache> {
        private C cacheInterface;

        protected void setCacheInterface(C provider) {
            cacheInterface = provider;
        }

        @Nullable
        public C getCacheInterface() {
            return cacheInterface;
        }

        /**
         * RxCache所使用的接口class
         *
         * @return
         */
        protected abstract Class<C> getCacheClass();

        protected Observable<String> checkNull(Observable<String> data, final IEvictDynamicHandler hanlder) {
            if (data == null) {
                return hanlder.cache(Observable.<String>error(new SmCacheException(NO_NET)), false);
            } else {
                return data.flatMap(new Function<String, ObservableSource<String>>() {
                    @Override
                    public ObservableSource<String> apply(String s) {
                        try {
                            ReturnBeanJson content = RetrofitManager.dispatchTransformer(s, ReturnBeanJson.class);
                            return hanlder.cache(Observable.just(s), content.getCode() != 0);
                        } catch (Exception e) {
                            SmLogger.i(e.getMessage());
                            //以防止有时候服务器传回无用数据
                            if (TextUtils.isEmpty(s) || !(s.trim().startsWith("{") || !s.trim().startsWith("["))) {
                                return hanlder.cache(Observable.<String>error(new SmCacheException("Error data from server")), false);
                            } else {
                                return hanlder.cache(Observable.just(s), true);
                            }
                        }
                    }
                }).onErrorResumeNext(new Function<Throwable, ObservableSource<? extends String>>() {
                    @Override
                    public ObservableSource<? extends String> apply(Throwable throwable) throws Exception {
                        return hanlder.cache(Observable.<String>error(throwable), false);
                    }
                });
            }
        }


    }


}
