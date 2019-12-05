package com.xx.module.common.client;


import android.support.v4.util.ArrayMap;

import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;

/**
 * @author someone
 * @date 2019-05-13
 */
public class ModuleManager {
    private static Map<Class, BaseClient> clients = new ArrayMap<>();


    public static <T extends BaseClient> T of(@NotNull Class<T> clz) {
        BaseClient result = clients.get(clz);
        if (result == null) {
            result = makeClient(clz);
            clients.put(clz, result);
        }
        return (T) result;
    }


    private static <T extends BaseClient> T makeClient(Class<T> clz) {
        Constructor<?>[] constructors = clz.getConstructors();
        T result = null;
        for (Constructor c : constructors) {
            try {
                result = (T) c.newInstance();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

}
