package com.mrkj.lib.net.retrofit;


import com.google.gson.reflect.TypeToken;
import com.mrkj.lib.common.util.SmLogger;
import com.mrkj.lib.net.base.OkHttpUtil;
import com.xx.base.GsonSingleton;
import com.xx.lib.db.entity.ReturnBeanJson;
import com.xx.lib.db.entity.ReturnJson;
import com.xx.lib.db.entity.ReturnResponse;
import com.xx.lib.db.exception.ReturnJsonCodeException;
import com.trello.rxlifecycle2.android.ActivityEvent;
import com.trello.rxlifecycle2.android.FragmentEvent;
import com.trello.rxlifecycle2.components.support.RxAppCompatActivity;
import com.trello.rxlifecycle2.components.support.RxFragment;

import java.lang.reflect.Type;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.ObservableTransformer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

/**
 * @Function 该类功能：构建Retrofit统一对象
 * @Author
 * @Date 2017/3/17
 */

public class RetrofitManager {
    private static String BASE_URL;
    private static Retrofit retrofit;


    public static void init(String baseUrl) {
        retrofit = null;
        BASE_URL = baseUrl;
    }

    /**
     * @return Retrofit接口实例化
     */
    public static <T> T createApi(Class<T> type) {
        if (retrofit == null) {
            synchronized (RetrofitManager.class) {
                if (retrofit == null) {
                    initRetrofit();
                }
            }
        }
        return retrofit.create(type);
    }


    private static void initRetrofit() {
        retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(OkHttpUtil.getOkHttpClient())
                .addConverterFactory(GsonConverterFactory.create())
                .addConverterFactory(ScalarsConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build();
    }


    public static <U> ObservableTransformer<U, U> rxTransformer() {
        return new ObservableTransformer<U, U>() {
            @Override
            public ObservableSource<U> apply(Observable<U> upstream) {
                return upstream
                        .subscribeOn(io.reactivex.schedulers.Schedulers.io())
                        .observeOn(io.reactivex.android.schedulers.AndroidSchedulers.mainThread());
            }

        };
    }

    /**
     * 子线程处理后回调主线程.<p>
     * 将String类型数据gson转化成D泛型对象
     *
     * @param type 需要转换的数据类型的type
     * @param <D>  GSON转换后的数据类型
     * @return
     */
    public static <D> ObservableTransformer<String, D> rxTransformer(final Object bindLife, final Type type) {
        return new ObservableTransformer<String, D>() {
            @Override
            public ObservableSource<D> apply(Observable<String> upstream) {
                return upstream
                        .compose(RetrofitManager.<String>getBindUntilEvent(bindLife))
                        .subscribeOn(Schedulers.io())
                        .flatMap(new Function<String, ObservableSource<D>>() {
                            @Override
                            public ObservableSource<D> apply(String s) throws Exception {
                                try {
                                    D data = dispatchTransformer(s, type);
                                    return Observable.just(data);
                                } catch (Exception e) {
                                    return Observable.error(e);
                                }
                            }
                        })
                        .subscribeOn(Schedulers.io())
                        .observeOn(io.reactivex.android.schedulers.AndroidSchedulers.mainThread());
            }

        };
    }

    public static <D> ObservableTransformer<ReturnBeanJson, D> rxReturnBeanJsonTransformer(final Object bindLife, final Type type) {
        return new ObservableTransformer<ReturnBeanJson, D>() {
            @Override
            public ObservableSource<D> apply(Observable<ReturnBeanJson> upstream) {
                return upstream
                        .compose(RetrofitManager.<ReturnBeanJson>getBindUntilEvent(bindLife))
                        .subscribeOn(Schedulers.io())
                        .flatMap(new Function<ReturnBeanJson, ObservableSource<D>>() {
                            @Override
                            public ObservableSource<D> apply(ReturnBeanJson s) throws Exception {
                                try {
                                    D data = dispatchReturnBeanJsonTransformer(s, type);
                                    return Observable.just(data);
                                } catch (Exception e) {
                                    return Observable.error(e);
                                }
                            }
                        })
                        .subscribeOn(Schedulers.io())
                        .observeOn(io.reactivex.android.schedulers.AndroidSchedulers.mainThread());
            }

        };
    }


    private static <T> T dispatchReturnBeanJsonTransformer(ReturnBeanJson json, Type type) throws ReturnJsonCodeException {
        SmLogger.i("--------------------------------------------------------------------------------");
        SmLogger.i(GsonSingleton.getInstance().toJson(json));
        SmLogger.i("--------------------------------------------------------------------------------");
        if (json.getCode() != 1) {
            ReturnJson returnJson = new ReturnJson();
            returnJson.setCode(json.getCode());
            returnJson.setMsg(json.getMsg());
            throw new ReturnJsonCodeException(returnJson);
        }
        String newContent = GsonSingleton.getInstance().toJson(json.getContent());
        SmLogger.i("--------------------------------------------------------------------------------");
        SmLogger.i(GsonSingleton.getInstance().toJson(newContent));
        SmLogger.i("--------------------------------------------------------------------------------");
        if (String.class.toString().equals(type.toString())) {
            return (T) newContent;
        }
        T data = GsonSingleton.getInstance().fromJson(newContent, type);
        if (data != null) {
            return data;
        }
        throw new ReturnJsonCodeException("Error from Server");
    }


    public static <D> ObservableTransformer<D, D> getBindUntilEvent(Object bindLife) {
        ObservableTransformer<D, D> transformer;
        if (bindLife instanceof RxAppCompatActivity) {
            transformer = ((RxAppCompatActivity) bindLife).bindUntilEvent(ActivityEvent.DESTROY);
        } else if (bindLife instanceof RxFragment) {
            transformer = ((RxFragment) bindLife).bindUntilEvent(FragmentEvent.DESTROY_VIEW);
        } else {
            transformer = new ObservableTransformer<D, D>() {
                @Override
                public ObservableSource<D> apply(Observable<D> upstream) {
                    return upstream;
                }
            };
        }
        return transformer;
    }

    public static <U> ObservableTransformer<U, U> rxTransformer(final Object bindLife) {
        return new ObservableTransformer<U, U>() {
            @Override
            public ObservableSource<U> apply(Observable<U> upstream) {
                return upstream
                        .compose(RetrofitManager.<U>getBindUntilEvent(bindLife))
                        .subscribeOn(io.reactivex.schedulers.Schedulers.io())
                        .observeOn(io.reactivex.android.schedulers.AndroidSchedulers.mainThread());
            }

        };
    }

    public static ObservableTransformer<ReturnBeanJson, ReturnBeanJson> rxReturnBeanJsonTransformer(final Object bindLife) {
        return new ObservableTransformer<ReturnBeanJson, ReturnBeanJson>() {
            @Override
            public ObservableSource<ReturnBeanJson> apply(Observable<ReturnBeanJson> upstream) {
                return upstream
                        .compose(RetrofitManager.<ReturnBeanJson>getBindUntilEvent(bindLife))
                        .map(new Function<ReturnBeanJson, ReturnBeanJson>() {
                            @Override
                            public ReturnBeanJson apply(ReturnBeanJson returnBeanJson) throws Exception {
                                if (returnBeanJson.getCode() == 1) {
                                    return returnBeanJson;
                                }
                                ReturnJson returnJson = new ReturnJson();
                                returnJson.setCode(returnBeanJson.getCode());
                                returnJson.setMsg(returnBeanJson.getMsg());
                                throw new ReturnJsonCodeException(returnJson);
                            }
                        })
                        .subscribeOn(io.reactivex.schedulers.Schedulers.io())
                        .observeOn(io.reactivex.android.schedulers.AndroidSchedulers.mainThread());
            }

        };
    }

    /**
     * 同时兼容老版本的
     * （1）直接返回对象格式、
     * （2）ReturnJson只有code和content字段格式、
     * （3）ReturnJson新增tip字段用以表示返回字符串信息格式
     * 支持{@link ReturnResponse}格式
     *
     * @param response
     * @param type
     * @param <T>
     * @return
     * @throws Exception
     */
    public static <T> T dispatchTransformer(String response, Type type) throws Exception {
        ReturnBeanJson beanJson = GsonSingleton.getInstance().fromJson(response, new TypeToken<ReturnBeanJson>() {
        }.getType());
        if (beanJson != null) {
            if (beanJson.getCode() != 1) {
                ReturnJson returnJson = new ReturnJson();
                returnJson.setCode(beanJson.getCode());
                returnJson.setMsg(beanJson.getMsg());
                throw new ReturnJsonCodeException(returnJson);
            }
            if (type.toString().equals(ReturnBeanJson.class.toString())) {
                return (T) beanJson;
            }
            String newContent = GsonSingleton.getInstance().toJson(beanJson.getContent());
            if (String.class.toString().equals(type.toString())) {
                return (T) newContent;
            }
            T data = GsonSingleton.getInstance().fromJson(newContent, type);
            if (data != null) {
                return data;
            }
        }
        throw new ReturnJsonCodeException("Error from Server");
    }

}
