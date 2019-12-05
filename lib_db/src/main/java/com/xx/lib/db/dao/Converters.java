package com.xx.lib.db.dao;

import android.arch.persistence.room.TypeConverter;

import com.google.gson.Gson;
import com.xx.lib.db.entity.CreateTime;

/**
 * @author someone
 * @date 2019-06-03
 */
public class Converters {
    static Gson mGson = new Gson();

    @TypeConverter
    public static CreateTime stringToTime(String json) {
        CreateTime time;
        try {
            time = mGson.fromJson(json, CreateTime.class);
        } catch (Exception e) {
            time = null;
        }
        return time;
    }

    @TypeConverter
    public static String timeToString(CreateTime time) {
        if (time == null) {
            return "";
        } else {
            return mGson.toJson(time);
        }
    }
}
