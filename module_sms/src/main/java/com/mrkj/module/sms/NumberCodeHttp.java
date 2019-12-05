package com.mrkj.module.sms;

import android.support.v4.util.ArrayMap;

import com.mrkj.lib.net.retrofit.RetrofitManager;
import com.xx.lib.db.entity.ReturnBeanJson;

import java.util.Map;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import retrofit2.http.GET;
import retrofit2.http.QueryMap;

/**
 * @author someone
 * @date 2019-05-29
 */
public class NumberCodeHttp {
    public static final void getNumberCode(final String phone, final String zoneCode, final OnNumberCodeCallback codeCallback) {
        Map<String, String> params = new ArrayMap<>();
        params.put("phone", phone);
        RetrofitManager.createApi(NumberCodeHttp.Service.class)
                .getNumberCode(params)
                .compose(RetrofitManager.<Integer>rxReturnBeanJsonTransformer(null, Integer.class))
                .subscribe(new Observer<Integer>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(Integer returnJson) {
                        if (codeCallback != null) {
                            codeCallback.onSuccess(phone, returnJson, zoneCode);
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        if (codeCallback != null) {
                            codeCallback.onError(e);
                        }
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }


    interface OnNumberCodeCallback {
        void onSuccess(String phone, Integer code, String zone);

        void onError(Throwable e);
    }


    public interface Service {
        @GET("//")
        Observable<ReturnBeanJson> getNumberCode(@QueryMap Map<String, String> post);
    }

}
