package com.xx.module.common.client;

import android.content.Context;

import com.chenenyu.router.Router;
import com.chenenyu.router.template.RouteTable;

import java.lang.ref.WeakReference;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;

public abstract class BaseClient<T> {
    private Context mContext;
    private Class<? extends T> httpModelClz;


    public void init(Context context) {
        if (mContext == null) {
            mContext = context.getApplicationContext();
            Router.handleRouteTable(new RouteTable() {
                @Override
                public void handle(Map<String, Class<?>> map) {
                    injectPageRouter(map);
                }
            });
        }
        Class<? extends T> clz = getModelClass();
        if (httpModelClz == null || !clz.getName().equals(httpModelClz.getName())) {
            httpModelClz = clz;
            getModelClient();
        }
    }

    protected abstract Class<? extends T> getModelClass();

    protected abstract void injectPageRouter(Map<String, Class<?>> map);


    public Context getContext() {
        return mContext;
    }

    private WeakReference<T> dataModel;

    public T getModelClient() {
        if (dataModel == null || dataModel.get() == null) {
            synchronized (this) {
                if (dataModel == null || dataModel.get() == null) {
                    try {
                        Constructor<? extends T> constructor = httpModelClz.getConstructor();
                        Object o = constructor.newInstance();
                        T model = (T) o;
                        dataModel = new WeakReference<>(model);
                    } catch (NoSuchMethodException e) {
                        e.printStackTrace();
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    } catch (InstantiationException e) {
                        e.printStackTrace();
                    } catch (InvocationTargetException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        if (dataModel == null) {
            throw new IllegalStateException("Please init Module");
        }
        return dataModel.get();
    }


}
