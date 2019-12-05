package com.mrkj.lib.push;

import android.app.Instrumentation;
import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.tencent.android.tpush.XGPushBaseReceiver;
import com.tencent.android.tpush.XGPushClickedResult;
import com.tencent.android.tpush.XGPushRegisterResult;
import com.tencent.android.tpush.XGPushShowedResult;
import com.tencent.android.tpush.XGPushTextMessage;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class SmXingePushReceiver extends XGPushBaseReceiver {
    @Override
    public void onRegisterResult(Context context, int i, XGPushRegisterResult xgPushRegisterResult) {
        Log.d("xinge", "onRegisterResult()");
        //注册回调
        // 第三方应用需要将CID上传到第三方服务器，并且将当前用户帐号和CID进行关联，以便日后通过用户帐号查找CID进行消息推送
        Map<String, String> params = new HashMap<>();
        params.put(SmPushManager.KEY_WHAT, SmPushManager.WHAT_BIND_START);
        params.put(SmPushManager.KEY_USER_ID, xgPushRegisterResult.getToken());
        SmPushManager.pushMessage(context, params);
    }

    @Override
    public void onUnregisterResult(Context context, int i) {
        //反注册回调
        Map<String, String> params = new HashMap<>();
        params.put(SmPushManager.KEY_WHAT, SmPushManager.WHAT_BIND_START);
        params.put(SmPushManager.KEY_USER_ID, "");
        SmPushManager.pushMessage(context, params);
    }

    @Override
    public void onSetTagResult(Context context, int i, String s) {
        //设置标签回调
    }

    @Override
    public void onDeleteTagResult(Context context, int i, String s) {
        //删除标签回调
    }

    @Override
    public void onTextMessage(Context context, XGPushTextMessage xgPushTextMessage) {
        Log.d("xinge", "onTextMessage()");
        //应用内消息的回调（消息不展示到通知栏）
    }

    /**
     * 小米通道支持抵达回调，不支持点击回调，支持透传
     * 华为通道不支持抵达回调，支持点击回调（需要自定义参数），支持透传（但忽略自定义参数）
     * 魅族通道支持抵达回调，支持点击回调，不支持透传
     *
     * @param context
     * @param xgPushClickedResult
     */
    @Override
    public void onNotifactionClickedResult(Context context, XGPushClickedResult xgPushClickedResult) {
        Log.d("xinge", "onNotifactionClickedResult()");

    }

    /**
     * 小米通道支持抵达回调，不支持点击回调，支持透传
     * 华为通道不支持抵达回调，支持点击回调（需要自定义参数），支持透传（但忽略自定义参数）
     * 魅族通道支持抵达回调，支持点击回调，不支持透传
     *
     * @param context
     * @param xgPushShowedResult
     */
    @Override
    public void onNotifactionShowedResult(Context context, XGPushShowedResult xgPushShowedResult) {
        // 通知被展示触发的回调，可以在此保存APP收到的通知
        Log.d("xinge", "onNotifactionShowedResult()");
        Map<String, String> params = new HashMap<>();
        params.put(SmPushManager.KEY_WHAT, SmPushManager.WHAT_MESSAGE_START);
        SmPushManager.pushMessage(context, params);
    }
}
