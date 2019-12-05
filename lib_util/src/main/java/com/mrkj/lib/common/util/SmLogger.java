package com.mrkj.lib.common.util;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.Map;
import java.util.Set;


/**
 * @author someone
 * @date 2019-05-07
 */
public class SmLogger {
    private static boolean DEBUG;
    private static String TAG = "[APP LOG]";

    public static boolean isDEBUG() {
        return DEBUG;
    }

    public static void setBebug(boolean debug) {
        DEBUG = debug;
        /*if (DEBUG) {
            Logger.addLogAdapter(new AndroidLogAdapter());
        } else {
            Logger.clearLogAdapters();
        }*/
    }

    public static void setTag(String tag) {
        TAG = tag;
    }

    public static void v(String message) {
        d(TAG, message);
    }

    public static void v(String tag, String message) {
        if (DEBUG) {
            Log.v(tag, message);
        }
    }

    public static void d(String message) {
        d(TAG, message);
    }

    public static void d(String tag, String message) {
        if (DEBUG) {
            Log.d(tag, message);
        }
    }

    public static void i(String message) {
        d(TAG, message);
    }

    public static void i(String tag, String message) {
        if (DEBUG) {
            Log.i(tag, message);
        }
    }

    public static void w(String message) {
        d(TAG, message);
    }

    public static void w(String tag, String message) {
        if (DEBUG) {
            Log.w(tag, message);
        }
    }

    public static void e(String message) {
        d(TAG, message);
    }

    public static void e(String tag, String message) {
        if (DEBUG) {
            Log.e(tag, message);
        }
    }

    private static Gson mGson = new Gson();

    public static void json(String msg) {
        try {
            Map<String, Object> map = mGson.fromJson(msg, new TypeToken<Map<String, Object>>() {
            }.getType());
            if (map != null) {
                d("----------------------------------------------------------------");
                Set<String> set = map.keySet();
                for (String s : set) {
                    d(s + " : " + map.get(s));
                }
                d("----------------------------------------------------------------");
            } else {
                d(msg);
            }
        } catch (Exception e) {
            i(e.getLocalizedMessage());
            d(msg);
        }
    }
}


