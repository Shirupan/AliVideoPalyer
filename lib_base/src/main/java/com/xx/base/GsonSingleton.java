package com.xx.base;/**
 * Created by someone on 2017/2/17.
 */

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.stream.JsonReader;

import java.io.StringReader;
import java.lang.reflect.Type;

/**
 * @Author
 * @Create 2017/2/17
 */
public class GsonSingleton {
    private static Gson gson;
    private static volatile GsonSingleton gsonSingleton;


    private GsonSingleton() {
        gson = new Gson();
    }

    public static GsonSingleton getInstance() {
        if (gsonSingleton == null) {
            synchronized (GsonSingleton.class) {
                if (gsonSingleton == null) {
                    gsonSingleton = new GsonSingleton();
                }
            }
        }
        return gsonSingleton;
    }

    public <T> T fromJson(String json, Class<T> classOfT) {
        if (json == null) {
            return null;
        }
        try {
            //处理有些字符异常
            JsonReader jsonReader = new JsonReader(new StringReader(json));
            jsonReader.setLenient(true);
            return gson.fromJson(jsonReader, classOfT);
        } catch (JsonSyntaxException e) {
            e.printStackTrace();
            return null;
        }
    }


    public <T> T fromJson(String json, Type classOfT) {
        try {
            return gson.fromJson(json, classOfT);
        } catch (JsonSyntaxException e) {
            e.printStackTrace();
            return null;
        }
    }

    public String toJson(Object o) {
        return gson.toJson(o);
    }
}
