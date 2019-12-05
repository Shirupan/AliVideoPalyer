package com.mrkj.lib.common.util;

import android.support.annotation.Nullable;
import android.support.v4.util.ArrayMap;
import android.text.TextUtils;

import java.lang.ref.SoftReference;
import java.util.LinkedList;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PatternPool {

    private static Map<String, SoftReference<Pattern>> pool;
    private static final int MATCHER_MAX_COUNT = 5;
    private final static LinkedList<Entity> marcherLink = new LinkedList<>();

    /**
     * Just cache the {@link Pattern#CASE_INSENSITIVE} type
     *
     * @param str
     * @return
     */
    public synchronized static Pattern getPattern(String str) {
        if (pool == null) {
            pool = new ArrayMap<>();
        }
        Pattern result;
        SoftReference<Pattern> value = pool.get(str);
        if (value == null || value.get() == null) {
            result = Pattern.compile(str, Pattern.CASE_INSENSITIVE);
            value = new SoftReference<>(result);
            pool.put(str, value);
        } else {
            result = value.get();
        }
        return result;
    }

    @Nullable
    public static Matcher getMatcher(String pattern) {
        Matcher result = null;
        Entity resultEntity = null;
        synchronized (marcherLink) {
            if (!marcherLink.isEmpty()) {
                for (Entity entity : marcherLink) {
                    if (TextUtils.equals(entity.pattern, pattern)) {
                        if (entity.matcher != null && entity.matcher.get() != null) {
                            result = entity.matcher.get();
                        }
                        resultEntity = entity;
                        break;
                    }
                }
                if (resultEntity != null) {
                    marcherLink.remove(resultEntity);
                }
            }
        }
        if (result != null) {
            result.reset();
        }
        return result;
    }

    public static void putMatcher(String pattern, Matcher matcher) {
        if (TextUtils.isEmpty(pattern) || matcher == null) {
            return;
        }
        synchronized (marcherLink) {
            checkAndRemoveOldEntity();
            matcher.reset();
            Entity entity = new Entity();
            entity.matcher = new SoftReference<>(matcher);
            entity.pattern = pattern;
            marcherLink.push(entity);
        }
    }

    private static void checkAndRemoveOldEntity() {
        boolean need = marcherLink.size() >= MATCHER_MAX_COUNT;
        while (need) {
            marcherLink.pollLast();
            need = marcherLink.size() >= MATCHER_MAX_COUNT;
        }
    }


    static class Entity {
        String pattern;
        SoftReference<Matcher> matcher;
    }
}
