package com.mrkj.module.sms;

import android.app.Activity;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;

import com.mrkj.lib.common.view.SmToast;
import com.mrkj.lib.net.tool.ExceptionUtl;

import org.json.JSONObject;

import java.util.List;
import java.util.Map;

import cn.smssdk.EventHandler;
import cn.smssdk.SMSSDK;

/**
 * 短信验证回调
 * Created by someone on 2017/4/7.
 */

public abstract class SMSEventHandler extends EventHandler {
    private Activity activity;
    public static final String ZONE = "zone";
    public static final String RULE = "rule";
    private List<Map<String, String>> supportedCountries;


    public SMSEventHandler(Activity activity) {
        this.activity = activity;
    }

    @Override
    public void beforeEvent(int i, Object o) {
        super.beforeEvent(i, o);
    }

    @Override
    public void afterEvent(int event, final int result, final Object data) {
        if (event == SMSSDK.EVENT_GET_VERIFICATION_CODE || event == SMSSDK.EVENT_GET_VOICE_VERIFICATION_CODE) {
            /* 请求验证码(即验证码短信已经发送)后的执行动作 */
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (data instanceof Boolean) {
                        boolean smart = (boolean) data;
                        onSendComplete();
                        if (smart) {
                            SmToast.show(activity, "已通过验证码云验证");
                            onSubmitPass();
                        }
                    } else {
                        if (result == SMSSDK.RESULT_COMPLETE) {
                            onSendComplete();
                        } else {
                            doOnError(data);
                        }
                    }
                }
            });

        } else if (event == SMSSDK.EVENT_SUBMIT_VERIFICATION_CODE) {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    /*提交验证码后的执行动作 */
                    if (data instanceof Boolean) {
                        boolean smart = (boolean) data;
                        if (smart) {
                            SmToast.show(activity, "已通过验证码云验证");
                            onSubmitPass();
                        } else {
                            doOnError(new Exception("{detail:\"验证失败\",status:500}"));
                        }
                    } else if (result == SMSSDK.RESULT_COMPLETE) {
                        onSubmitPass();
                    } else {
                        doOnError(data);
                    }
                }
            });
        } else if (event == SMSSDK.EVENT_GET_SUPPORTED_COUNTRIES) {
            /* 获取短信验证码支持的国家区号和规则 */
            try {
                supportedCountries = (List<Map<String, String>>) data;
                onSupportedCountiesResult(supportedCountries);
            } catch (Exception e) {

            }

        }
    }

    public List<Map<String, String>> getSupportedCountries() {
        return supportedCountries;
    }

    public abstract void onSendComplete();

    protected abstract void onSubmitPass();

    protected void onSupportedCountiesResult(List<Map<String, String>> supportedCountries) {

    }

    public void onError(boolean canSend) {
    }

    private void doOnError(Object data) {
        try {
            boolean canSend = false;
            Throwable throwable = (Throwable) data;
            throwable.printStackTrace();
            JSONObject object = new JSONObject(throwable.getMessage());
            String des = object.optString("detail");//错误描述
            int status = object.optInt("status");//错误代码
            if (status > 0 && !TextUtils.isEmpty(des)) {
                String message;
                switch (status) {
                    case 400:
                        message = "请求失败，\n请稍后重试";
                        break;
                    case 457:
                        message = "手机号码格式错误，\n请填写正确手机号码";
                        canSend = true;
                        break;
                    case 461:
                    case 604:
                    case 602:
                        message = "不支持该地区发送验证码。\n请确认地区编号是否正确，建议优先使用中国大陆(86)手机号码";
                        canSend = true;
                        break;
                    case 465:
                    case 463:
                    case 476:
                    case 477:
                    case 478:
                        message = "该手机号码当日发送验证码次数超限，\n请明日再试";
                        canSend = true;
                        break;
                    case 464:
                        message = "您使用的手机每天发送次数超限，\n请明日再试或更换手机尝试";
                        canSend = true;
                        break;
                    case 466:
                        message = "校验的验证码为空，\n请输入正确验证码";
                        break;
                    case 467:
                        message = "5分钟内校验错误超过3次，验证码失效。\n请重新获取验证码";
                        break;
                    case 468:
                        message = "您提交校验的验证码错误。\n请稍后重试";
                        break;
                    case 462:
                    case 472:
                        message = "请求验证过于频繁，\n请稍后重试";
                        break;
                    default:
                        message = "请求失败，\n请稍后重试";
                        break;

                }
                message = message + "。" + status;
                if (activity != null && !activity.isFinishing()) {
                    new AlertDialog.Builder(activity)
                            .setMessage(message)
                            .setItems(new String[]{"知道了"}, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            }).show();
                }
                onError(canSend);
            }
        } catch (Exception e) {
            //do something
            if (activity != null && !activity.isFinishing()) {
                new AlertDialog.Builder(activity)
                        .setMessage("验证码处理失败，请稍后重试。\n" + ExceptionUtl.catchTheError(e))
                        .setItems(new String[]{"知道了"}, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        }).show();
                onError(true);
            }

        }
    }


}
