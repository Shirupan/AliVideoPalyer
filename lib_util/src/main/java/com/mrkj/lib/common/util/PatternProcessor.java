package com.mrkj.lib.common.util;

import java.util.regex.Matcher;

public class PatternProcessor<T> {

    private ProcessedListener<T> processedListener;
    private String mRegEx;
    private CharSequence mCharSequence;

    public PatternProcessor(String regEx, CharSequence sequence) {
        mRegEx = regEx;
        mCharSequence = sequence;
    }

    public PatternProcessor(String regEx) {
        mRegEx = regEx;
    }

    public void setProcessedListener(ProcessedListener<T> listener) {
        processedListener = listener;
    }

    public T execute() {
        if (mCharSequence == null) {
            return null;
        }
        Matcher matcher = PatternPool.getMatcher(mRegEx);
        if (matcher == null) {
            matcher = PatternPool.getPattern(mRegEx).matcher(mCharSequence);
        } else {
            matcher.reset(mCharSequence);
        }
        T result = null;
        if (processedListener != null) {
            result = processedListener.onHanded(matcher);
        }
        matcher.reset();
        PatternPool.putMatcher(mRegEx, matcher);
        return result;
    }

    public T execute(CharSequence sequence) {
        mCharSequence = sequence;
        return execute();
    }

    public interface ProcessedListener<T> {
        T onHanded(Matcher matcher);
    }
}
