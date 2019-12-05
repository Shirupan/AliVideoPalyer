package com.mrkj.module.sms;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.Looper;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.jakewharton.rxbinding2.widget.RxTextView;
import com.jakewharton.rxbinding2.widget.TextViewAfterTextChangeEvent;
import com.mob.MobSDK;
import com.mrkj.lib.common.util.AppUtil;
import com.mrkj.lib.common.util.StringUtil;
import com.mrkj.lib.common.view.SmToast;
import com.mrkj.lib.net.loader.file.SmNetProgressDialog;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;

/**
 * @author
 * @date 2017/9/12
 */

public class NumberCodeManager {
    private Dialog loadingDialog;
    private static List<Map<String, String>> supportedCountries;

    private NumberCodeManager() {
    }

    public static NumberCodeManager getInstance() {
        return SingleTon.manager;
    }


    private static class SingleTon {
        static NumberCodeManager manager = new NumberCodeManager();
    }


    private static Map<Integer, EventHandler> handlerMap = new android.support.v4.util.ArrayMap<>();


    public void register(final Activity context, View codeView, final OnSubmitCallback callback) {
        registerSMSCodeButton(codeView);
        if (handlerMap.get(context.hashCode()) != null) {
            return;
        }
        EventHandler eventHandler = new EventHandler() {
            @Override
            protected void onSendComplete(String phone, String zone, Integer code) {
                if (loadingDialog != null && loadingDialog.isShowing()) {
                    loadingDialog.dismiss();
                    loadingDialog = null;
                }
                // 请求验证码后，跳转到验证码填写页面
                SmToast.showToast(context, context.getString(R.string.sms_virificaition_code_sent));
                startToCountDownSMS();
                if (callback != null) {
                    callback.onSendComplete();
                }
            }

            @Override
            protected void onSubmitPass(String phone, String zone) {
                if (loadingDialog != null && loadingDialog.isShowing()) {
                    loadingDialog.dismiss();
                    loadingDialog = null;
                }
                resetCountDownSMS();
                if (callback != null) {
                    callback.onSubmitPass();
                }
            }

            @Override
            public void onError(Throwable e) {
                if (loadingDialog != null && loadingDialog.isShowing()) {
                    loadingDialog.dismiss();
                    loadingDialog = null;
                }
                if (!(e instanceof NumberErrorException)) {
                    resetCountDownSMS();
                }
                if (callback != null) {
                    callback.onError(e);
                }
            }
        };
        handlerMap.put(context.hashCode(), eventHandler);
        eventHandler.onRegister();
    }


    /**
     * 短信验证码
     *
     * @param context
     * @param zoneCode
     * @param phone
     */
    public void getVerificationCode(Context context, String zoneCode, String phone) {
        loadingDialog = new SmNetProgressDialog.Builder(context).show();
        if (judgePhoneNum(context, phone)) {
            if (supportedCountries != null) {
                for (Map<String, String> map : supportedCountries) {
                    String zonecode = map.get(SMSEventHandler.ZONE);
                    if (!TextUtils.isEmpty(zonecode) && TextUtils.equals(zonecode, zoneCode)) {
                        String rule = map.get(SMSEventHandler.RULE);
                        if (!TextUtils.isEmpty(rule)) {
                            if (Pattern.matches(rule, phone)) {
                                // 获取验证码
                                NumberCodeHttp.getNumberCode(phone, zoneCode, onNumberCodeCallback);
                                // SMSSDK.getVerificationCode(zoneCode, phone);
                            } else {
                                SmToast.showToastRight(context, "手机号码有误");
                                resetCountDownSMS();
                                loadingDialog.dismiss();
                            }
                        } else {
                            // 获取验证码
                            NumberCodeHttp.getNumberCode(phone, zoneCode, onNumberCodeCallback);
                            // SMSSDK.getVerificationCode(zoneCode, phone);
                        }
                        return;
                    }
                }
                SmToast.showToastRight(context, "不支持该地区手机号");
                resetCountDownSMS();
                loadingDialog.dismiss();
                loadingDialog = null;
            } else {
                // 获取验证码
                NumberCodeHttp.getNumberCode(phone, zoneCode, onNumberCodeCallback);
                // SMSSDK.getVerificationCode(zoneCode, phone);
            }
        } else {
            loadingDialog.dismiss();
            loadingDialog = null;
        }
    }

    private String mPhone;
    private String mZone;
    private Integer mCode;

    public Integer getCode() {
        return mCode;
    }

    private NumberCodeHttp.OnNumberCodeCallback onNumberCodeCallback = new NumberCodeHttp.OnNumberCodeCallback() {
        @Override
        public void onSuccess(String phone, Integer code, String zone) {
            mPhone = phone;
            mZone = zone;
            mCode = code;
            Set<Integer> set = handlerMap.keySet();
            for (Integer key : set) {
                EventHandler handler = handlerMap.get(key);
                if (handler != null) {
                    handler.onSendComplete(mPhone, mZone, mCode);
                }
            }
        }

        @Override
        public void onError(Throwable e) {
            Set<Integer> set = handlerMap.keySet();
            for (Integer key : set) {
                EventHandler handler = handlerMap.get(key);
                if (handler != null) {
                    handler.onError(e);
                }
            }
        }
    };


    public static class EventHandler {
        public void onRegister() {
        }

        protected void onSendComplete(String phone, String zone, Integer code) {


        }

        protected void onSubmitPass(String phone, String zone) {

        }

        public void onError(Throwable e) {

        }

        public void onUnregister() {
        }
    }


    /**
     * 提交验证码
     *
     * @param context
     * @param zoneCode 地区区号
     * @param phone    手机号码
     * @param code     验证码
     */
    public void submitVerificationCode(Context context, String zoneCode, String phone, Integer code) {
        if (judgePhoneNum(context, phone) && code != null) {
            if (supportedCountries != null) {
                for (Map<String, String> map : supportedCountries) {
                    String zonecode = map.get(SMSEventHandler.ZONE);
                    if (!TextUtils.isEmpty(zonecode) && TextUtils.equals(zonecode, zoneCode)) {
                        String rule = map.get(SMSEventHandler.RULE);
                        if (!TextUtils.isEmpty(rule)) {
                            if (Pattern.matches(rule, phone)) {
                                // 提交验证码
                                checkNumberCode(phone, zoneCode, code);
                                //  SMSSDK.submitVerificationCode(zoneCode, phone, code);
                            } else {
                                SmToast.showToastRight(context, "手机号码有误");
                            }
                        } else {
                            // 提交验证码
                            checkNumberCode(phone, zoneCode, code);
                        }
                        return;
                    }
                }
                SmToast.showToastRight(context, "不支持该地区手机号");
            } else {
                // 提交验证码
                checkNumberCode(phone, zoneCode, code);
            }
        } else {
            SmToast.showToastRight(context, "手机号和验证码不能为空");
        }
    }

    private void checkNumberCode(String phone, String zoneCode, Integer code) {
        if (!TextUtils.isEmpty(mPhone) && mPhone.equals(phone)
                && !TextUtils.isEmpty(mZone) && mZone.equals(zoneCode)
                && mCode != null && code != null && mCode.intValue() == code.intValue()) {
            Set<Integer> set = handlerMap.keySet();
            for (Integer key : set) {
                EventHandler handler = handlerMap.get(key);
                if (handler != null) {
                    handler.onSubmitPass(phone, zoneCode);
                }
            }
        } else {
            Set<Integer> set = handlerMap.keySet();
            Throwable e = new NumberErrorException("验证码错误");
            for (Integer key : set) {
                EventHandler handler = handlerMap.get(key);
                if (handler != null) {
                    handler.onError(e);
                }
            }
        }
    }


    private static long lastGetCountriesTime = 0;


    public void unRegister(Activity context, View codeView) {
        if (codeView != null) {
            unRegisterSMSCodeButton(codeView);
        }
        EventHandler eventHandler = handlerMap.get(context.hashCode());
        if (eventHandler != null) {
            eventHandler.onUnregister();
            handlerMap.remove(context.hashCode());
        }
    }

    public static void addZoneCodeTextChangeEvents(final EditText codeEt) {
        RxTextView.afterTextChangeEvents(codeEt)
                .subscribe(new Consumer<TextViewAfterTextChangeEvent>() {
                    @Override
                    public void accept(@NonNull TextViewAfterTextChangeEvent textViewAfterTextChangeEvent) throws Exception {
                        if (textViewAfterTextChangeEvent != null && textViewAfterTextChangeEvent.editable() != null) {
                            String s = textViewAfterTextChangeEvent.editable().toString();
                            if (!TextUtils.isEmpty(s) && !s.startsWith("+")) {
                                s = "+" + s;
                                codeEt.setText(s);
                                codeEt.setSelection(codeEt.length());
                            }
                        }
                    }
                });
    }

    public static class SimpleSubmitCallback implements OnSubmitCallback {
        @Override
        public void onSubmitPass() {

        }

        @Override
        public void onSendComplete() {

        }

        @Override
        public void onError(Throwable e) {

        }
    }

    public interface OnSubmitCallback {
        void onSubmitPass();

        void onSendComplete();

        void onError(Throwable e);
    }

    /**
     * 电话号码框验证
     *
     * @return
     */
    public static boolean judgePhoneNum(Context context, String phone) {
        if (phone != null && phone.length() > 0) {
            phone = phone.replaceAll("\\s*", ""); // 多个空白字符：空白字符不一定是空格，还可以能是tab啥的
            if (TextUtils.isEmpty(phone)) {
                SmToast.showToastRight(context, "请输入手机号码");
                return false;
            } else {
                if (StringUtil.isMobileNO(phone)) {
                    return true;
                } else {
                    SmToast.showToastRight(context, "手机号码输入有误");
                    return false;
                }
            }
        } else {
            SmToast.showToastRight(context, "请输入手机号码");
            return false;
        }
    }


    //-------------------------------------验证码倒计时，全局共用----------------------------------------
    private static ArrayList<View> codeButtons = new ArrayList<>();
    private static Disposable countDownDis;

    /**
     * 注册验证码按钮
     *
     * @param button
     */
    private static void registerSMSCodeButton(View button) {
        if (!codeButtons.contains(button)) {
            codeButtons.add(button);
        }
    }

    /**
     * 注销验证码按钮
     *
     * @param button
     */
    public static void unRegisterSMSCodeButton(View button) {
        if (codeButtons.contains(button)) {
            codeButtons.remove(button);
        }
    }

    /**
     * 开始xx秒后获取验证码。如果本身正在倒计时，则沿用
     */
    private static void startToCountDownSMS() {
        if (countDownDis == null || countDownDis.isDisposed()) {
            Observable.interval(0, 1, TimeUnit.SECONDS)
                    .take(120 + 1)
                    .map(new Function<Long, Long>() {
                        @Override
                        public Long apply(@NonNull Long aLong) throws Exception {
                            return 120 - aLong;
                        }
                    })
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Observer<Long>() {
                        @Override
                        public void onSubscribe(Disposable d) {
                            countDownDis = d;
                        }

                        @Override
                        public void onNext(Long aLong) {
                            for (View button : codeButtons) {
                                if (button instanceof TextView) {
                                    ((TextView) button).setText(String.format(Locale.CHINESE, "%1ds", aLong));
                                    button.setEnabled(false);
                                }
                            }
                        }

                        @Override
                        public void onError(Throwable e) {

                        }

                        @Override
                        public void onComplete() {
                            for (View button : codeButtons) {
                                if (button instanceof TextView) {
                                    button.setEnabled(true);
                                    ((TextView) button).setText("发送验证码");
                                }
                            }
                        }
                    });
        }
    }

    /**
     * 是否正在倒计时
     *
     * @return
     */
    public boolean isCountDown() {
        return countDownDis != null && !countDownDis.isDisposed();
    }

    /**
     * 倒计时停止，重置
     */
    private static void resetCountDownSMS() {
        if (countDownDis != null) {
            countDownDis.dispose();
        }
        for (View button : codeButtons) {
            if (button instanceof TextView) {
                ((TextView) button).setText("发送验证码");
                button.setEnabled(true);
            }
        }
    }

    public boolean isMainThread() {
        return Looper.getMainLooper().getThread() == Thread.currentThread();
    }

    public static String getZoneCodeFromEditText(TextView e) {
        String zoneCode = e.getText().toString();
        if (TextUtils.isEmpty(zoneCode)) {
            zoneCode = e.getHint().toString();
        }
        zoneCode = zoneCode.replace("+", "");
        return zoneCode;
    }
}
