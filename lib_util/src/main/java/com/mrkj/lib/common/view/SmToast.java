package com.mrkj.lib.common.view;

import android.content.Context;
import android.support.annotation.DrawableRes;
import android.support.annotation.StringRes;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.mrkj.lib.common.util.R;


/**
 * 自定义Toast
 *
 * @author
 * @date 2016/11/15
 */

public class SmToast {

    public static void show(Context context, String message) {
        try {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
        } catch (Exception ec) {

        }
    }

    public static void show(Context context, @StringRes int res) {
        try {
            Toast.makeText(context, res, Toast.LENGTH_SHORT).show();
        } catch (Exception ec) {

        }
    }

    /**
     * @param context
     * @param message
     * @param res     Toast上面的图片
     */
    public static void showToast(Context context, String message, @DrawableRes int res) {
        if (context == null) {
            return;
        }
        //加载Toast布局
        View toastRoot = LayoutInflater.from(context).inflate(R.layout.sm_toast_layout, null);
        //初始化布局控件
        TextView tv = toastRoot.findViewById(R.id.id_tv_loadingmsg);
        ImageView iv = toastRoot.findViewById(R.id.loadingImageView);
        //为控件设置属性
        tv.setText(message);
        if (res != 0) {
            iv.setImageResource(res);
        }
        //Toast的初始化
        Toast toastStart = new Toast(context);
        toastStart.setGravity(Gravity.CENTER, 0, 0);
        toastStart.setDuration(Toast.LENGTH_SHORT);
        toastStart.setView(toastRoot);
        toastStart.show();
    }

    /**
     * @param context
     * @param message
     * @param res     Toast左边的图片
     */
    public static void showToastRight(Context context, String message, @DrawableRes int res, int durition) {
        if (context == null) {
            return;
        }
        //加载Toast布局
        View toastRoot = LayoutInflater.from(context).inflate(R.layout.sm_toast_layout_left, null);
        //初始化布局控件
        TextView tv = toastRoot.findViewById(R.id.id_tv_loadingmsg);
        ImageView iv = toastRoot.findViewById(R.id.loadingImageView);
        //为控件设置属性
        tv.setText(message);
        if (res != 0) {
            iv.setImageResource(res);
        }
        //Toast的初始化
        Toast toastStart = new Toast(context);
        toastStart.setGravity(Gravity.CENTER, 0, 0);
        toastStart.setDuration(durition);
        toastStart.setView(toastRoot);
        toastStart.show();
    }

    public static void showToastRightLong(Context context, String message) {
        showToastRight(context, message, 0, Toast.LENGTH_LONG);
    }

    public static void showToastRight(Context context, String message) {
        showToastRight(context, message, 0, Toast.LENGTH_SHORT);
    }

    public static void showToast(Context context, String message) {
        showToast(context, message, 0);
    }

    /**
     * @param context
     * @param message
     * @param big     Toast上面的大字
     */
    public static void showToast(Context context, CharSequence message, CharSequence big) {
        showToast(context, message, big, Toast.LENGTH_SHORT);
    }

    /**
     * Toast上面是文字不是图片
     *
     * @param context
     * @param message
     * @param big      Toast的大字
     * @param duration
     */
    public static void showToast(Context context, CharSequence message, CharSequence big, int duration) {
        if (context == null) {
            return;
        }
        //加载Toast布局
        View toastRoot = LayoutInflater.from(context).inflate(R.layout.sm_toast_layout, null);
        //初始化布局控件
        TextView tv = toastRoot.findViewById(R.id.id_tv_loadingmsg);
        ImageView iv = toastRoot.findViewById(R.id.loadingImageView);
        iv.setVisibility(View.GONE);
        TextView bigTv = toastRoot.findViewById(R.id.loading_toast_tv);
        bigTv.setVisibility(View.VISIBLE);
        bigTv.setText(big);
        //为控件设置属性
        tv.setText(message);
        //Toast的初始化
        Toast toastStart = new Toast(context);
        toastStart.setGravity(Gravity.CENTER, 0, 0);
        toastStart.setDuration(duration);
        toastStart.setView(toastRoot);
        toastStart.show();
    }


}
