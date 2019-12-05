package com.mrkj.lib.common.permission;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.PermissionChecker;
import android.support.v4.util.ArrayMap;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.ForegroundColorSpan;

import com.mrkj.lib.common.util.ScreenUtils;
import com.mrkj.lib.common.view.SmToast;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by fhs on 2016-08-12.
 */
public class PermissionUtil {

    /**
     * 为了适配API23，即Android M 在清单文件中配置use permissions后，还要在程序运行的时候进行申请。
     * <p/>
     * ***整个权限的申请与处理的过程是这样的：
     * *****1.进入主Activity，首先申请所有的权限；
     * *****2.用户对权限进行授权，有2种情况：
     * ********1).用户Allow了权限，则表示该权限已经被授权，无须其它操作；
     * ********2).用户Deny了权限，则下次启动Activity会再次弹出系统的Permisssions申请授权对话框。
     * *****3.如果用户Deny了权限，那么下次再次进入Activity，会再次申请权限，这次的权限对话框上，会有一个选项“dont ask me again”：
     * ********1).如果用户勾选了“dont ask me again”的checkbox，下次启动时就必须自己写Dialog或者Snackbar引导用户到应用设置里面去手动授予权限；
     * ********2).如果用户未勾选上面的选项，若选择了Allow，则表示该权限已经被授权，无须其它操作；
     * ********3).如果用户未勾选上面的选项，若选择了Deny，则下次启动Activity会再次弹出系统的Permisssions申请授权对话框。
     */


    // 状态码、标志位
    public static final int REQUEST_STATUS_CODE = 0x001;
    public static final int REQUEST_PERMISSION_SETTING = 0x002;


    private static String[] permissionList;
    private static List<String> grantedPermissionList = new ArrayList<>();
    private static List<String> unGrantedPermissionList = new ArrayList<>();
    private static Runnable allowRunnalbe, unAllowRunnale;
    private static OnPermissionRequestCallback onPermissionRequestCallback;
    private static boolean showMessageDialog;

    private static Context mBaseContext;
    private static Activity startActivity;

    public static void init(Context context) {
        if (context != null) {
            mBaseContext = context.getApplicationContext();
        }
    }

    public static void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                                  @NonNull int[] grantResults) {
        if (requestCode == REQUEST_STATUS_CODE) {
            for (int i = 0; i < permissions.length; i++) {
                if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                    if (!grantedPermissionList.contains(permissions[i])) {
                        grantedPermissionList.add(permissions[i]);
                    }
                } else {
                    if (!unGrantedPermissionList.contains(permissions[i])) {
                        unGrantedPermissionList.add(permissions[i]);
                    }
                }
            }
            //有些可能勾选了“不再提醒”，导致授权失败
            if (!unGrantedPermissionList.isEmpty()) {
                if (onPermissionRequestCallback != null
                        && !onPermissionRequestCallback.onShowNeedToSettingDialog(unGrantedPermissionList)) {
                    showNeedUserSetDialog(startActivity, unGrantedPermissionList.toArray(new String[unGrantedPermissionList.size()]));
                } else if (onPermissionRequestCallback == null) {
                    showNeedUserSetDialog(startActivity, unGrantedPermissionList.toArray(new String[unGrantedPermissionList.size()]));
                }
            } else {
                if (allowRunnalbe != null) {
                    allowRunnalbe.run();
                }
                if (onPermissionRequestCallback != null) {
                    onPermissionRequestCallback.onSuccess();
                }
                cleanAllData();
            }
        } else {
            cleanAllData();
        }
    }

    @Deprecated
    public static void checkAndRequestPermissions(final Activity activity, Runnable allow, Runnable unAllow, final String... permissions) {
        checkAndRequestPermissions(activity, allow, unAllow, true, permissions);
    }

    public static void checkAndRequestPermissions(final Activity activity, OnPermissionRequestCallback callback, final String... permissions) {
        checkAndRequestPermissions(activity, true, callback, permissions);
    }

    private static void checkAndRequestPermissions(final Activity activity, boolean showDialog, OnPermissionRequestCallback callback, final String... permissions) {
        startActivity = activity;
        permissionList = permissions;
        onPermissionRequestCallback = callback;
        showMessageDialog = showDialog;
        //权限检查
        for (String permission : permissions) {
            if (selfPermissionGranted(activity, permission)) {
                if (grantedPermissionList.contains(permission)) {
                    continue;
                }
                grantedPermissionList.add(permission);
            } else {
                if (unGrantedPermissionList.contains(permission)) {
                    continue;
                }
                unGrantedPermissionList.add((permission));
            }
        }
        if (unGrantedPermissionList.isEmpty()) {
            if (onPermissionRequestCallback != null) {
                onPermissionRequestCallback.onSuccess();
                cleanAllData();
            }
        } else {  //权限提示并申请
            if (!showMessageDialog) { //用户授权完成之后，再次验证是否已经授权
                if (onPermissionRequestCallback != null) {
                    onPermissionRequestCallback.onFailed();
                    cleanAllData();
                }
                return;
            }
            if (onPermissionRequestCallback != null
                    && !onPermissionRequestCallback.onShowCustomDialog(grantedPermissionList, unGrantedPermissionList)) {
                showRequestDialog(activity, unGrantedPermissionList.toArray(new String[unGrantedPermissionList.size()]));
            } else if (onPermissionRequestCallback == null) {
                showRequestDialog(activity, unGrantedPermissionList.toArray(new String[unGrantedPermissionList.size()]));
            }
            unGrantedPermissionList = new ArrayList<>(); //置空
        }

    }

    public static boolean selfPermissionGranted(Context context, String permission) {
        int targetSdkVersion = 0;
        boolean ret = false;

        try {
            final PackageInfo info = context.getPackageManager().getPackageInfo(
                    context.getPackageName(), 0);
            targetSdkVersion = info.applicationInfo.targetSdkVersion;

        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (targetSdkVersion >= Build.VERSION_CODES.M) {
                ret = context.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED;
            } else {
                ret = PermissionChecker.checkSelfPermission(context, permission) == PermissionChecker.PERMISSION_GRANTED;
            }
        } else {
            return true;
        }

        return ret;
    }

    @Deprecated
    private static void checkAndRequestPermissions(final Activity activity, Runnable allow, Runnable unAllow, boolean showMessage, final String... permissions) {
        permissionList = permissions;
        allowRunnalbe = allow;
        unAllowRunnale = unAllow;
        showMessageDialog = showMessage;
        //权限检查
        for (String permission : permissions) {
            if (selfPermissionGranted(activity, permission)) {
                if (grantedPermissionList.contains(permission)) continue;
                grantedPermissionList.add(permission);
            } else {
                if (unGrantedPermissionList.contains(permission)) continue;
                unGrantedPermissionList.add((permission));
            }
        }
        if (unGrantedPermissionList.isEmpty()) {
            if (allowRunnalbe != null) {
                allowRunnalbe.run();
                cleanAllData();
            }
        } else {  //权限提示并申请
            if (!showMessageDialog) {
                if (unAllowRunnale != null) {
                    unAllowRunnale.run();
                    cleanAllData();
                }
                return;
            }
            showRequestDialog(activity, unGrantedPermissionList.toArray(new String[unGrantedPermissionList.size()]));
            unGrantedPermissionList = new ArrayList<>(); //置空
        }

    }

    private static void showRequestDialog(final Activity activity, final String... permission) {
        Spannable spannable = new SpannableString("权限申请");
        spannable.setSpan(new ForegroundColorSpan(Color.parseColor("#3d5b9b")),
                0, spannable.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        Spannable cancelSp = new SpannableString("取消");
        cancelSp.setSpan(new ForegroundColorSpan(Color.parseColor("#999999")),
                0, cancelSp.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);

        new AlertDialog.Builder(activity)
                .setTitle(spannable)
                .setCancelable(false)
                .setMessage(getPermissionMessage(activity, permission))
                .setPositiveButton("授权", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        Intent intent = new Intent(activity, PermissionActivity.class);
                        intent.putExtra("permissions", permission);
                        activity.startActivity(intent);
                        //    ActivityCompat.requestPermissions(activity, permission, PermissionUtil.REQUEST_STATUS_CODE);
                    }
                })
                .setNegativeButton(cancelSp, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        if (unAllowRunnale != null) {
                            unAllowRunnale.run();
                        }
                        if (onPermissionRequestCallback != null) {
                            onPermissionRequestCallback.onFailed();
                        }
                        cleanAllData();
                    }
                })
                .show();
    }


    /**
     * 弹窗提示授权
     *
     * @param activity
     */
    private static void showNeedUserSetDialog(final Activity activity, String... permissions) {
        Spannable spannable = new SpannableString("手动授权申请");
        spannable.setSpan(new ForegroundColorSpan(Color.parseColor("#3d5b9b")),
                0, spannable.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        Spannable cancelSp = new SpannableString("取消");
        cancelSp.setSpan(new ForegroundColorSpan(Color.parseColor("#999999")),
                0, cancelSp.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        final Runnable cancelRun = new Runnable() {
            @Override
            public void run() {
                if (unAllowRunnale != null) {
                    unAllowRunnale.run();
                }
                if (onPermissionRequestCallback != null) {
                    onPermissionRequestCallback.onFailed();
                }
                cleanAllData();
            }
        };
        if (activity == null) {
            cancelRun.run();
            return;
        }
        new AlertDialog.Builder(activity)
                .setTitle(spannable)
                .setCancelable(false)
                .setMessage(getPermissionMessage(activity, permissions))
                .setPositiveButton("前往设置", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        // 进入App设置页面
                        if (activity != null) {
                            openPermissionSettingActivity(activity);
                        }
                        cleanAllData();
                    }
                })
                .setNegativeButton(cancelSp, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        cancelRun.run();
                    }
                })
                .show();
    }

    private static void cleanAllData() {
        cleanPermissionList();
        allowRunnalbe = null;
        unAllowRunnale = null;
        permissionList = null;
        onPermissionRequestCallback = null;
        startActivity = null;
    }

    private static void cleanPermissionList() {
        grantedPermissionList.clear();
        unGrantedPermissionList.clear();
        showMessageDialog = false;
    }


    public static void onActivityResult(int requestCode, int resultCode, Intent data) {
        //设置页面返回
        if (requestCode == REQUEST_PERMISSION_SETTING) {
            if (permissionList != null) {
                cleanPermissionList();
                if (onPermissionRequestCallback != null) {
                    checkAndRequestPermissions(startActivity, false, onPermissionRequestCallback, permissionList);
                }
                checkAndRequestPermissions(startActivity, allowRunnalbe, unAllowRunnale, false, permissionList);
            }

        }
    }

    /**
     * 关于shouldShowRequestPermissionRationale函数的一点儿注意事项：
     * ***1).应用安装后第一次访问，则直接返回false；
     * ***2).第一次请求权限时，用户Deny了，再次调用shouldShowRequestPermissionRationale()，则返回true；
     * ***3).第二次请求权限时，用户Deny了，并选择了“dont ask me again”的选项时，再次调用shouldShowRequestPermissionRationale()时，返回false；
     * ***4).设备的系统设置中，禁止了应用获取这个权限的授权，则调用shouldShowRequestPermissionRationale()，返回false。
     */
    public static boolean showRationaleUI(Activity activity, String permission) {
        return ActivityCompat.shouldShowRequestPermissionRationale(activity, permission);
    }

    public static boolean hasPermissions(Context context, String... permissions) {
        //权限检查
        for (String permission : permissions) {
            if (selfPermissionGranted(context, permission)) {
                if (grantedPermissionList.contains(permission)) continue;
                grantedPermissionList.add(permission);
            } else {
                if (unGrantedPermissionList.contains(permission)) continue;
                unGrantedPermissionList.add((permission));
            }
        }
        boolean has = unGrantedPermissionList.isEmpty();
        cleanAllData();
        return has;
    }

    public interface OnPermissionRequestCallback {
        boolean onShowCustomDialog(List<String> grantedPermissionList, List<String> unGrantedPermissionList);

        boolean onShowNeedToSettingDialog(List<String> unGrantedPermissionList);

        void onSuccess();

        void onFailed();
    }

    public static abstract class SimpleOnPermissionRequestCallback implements OnPermissionRequestCallback {

        @Override
        public boolean onShowCustomDialog(List<String> grantedPermissionList, List<String> unGrantedPermissionList) {
            return false;
        }

        @Override
        public boolean onShowNeedToSettingDialog(List<String> unGrantedPermissionList) {
            return false;
        }

        @Override
        public void onSuccess() {

        }

        @Override
        public void onFailed() {

        }
    }

    private static CharSequence getPermissionMessage(Context context, String... permissions) {
        StringBuilder sb = new StringBuilder("应用需开启以下权限：\n");
        for (int i = 0; i < permissions.length; i++) {
            String permission = permissions[i];
            String message = permissionMessages.get(permission);
            if (!TextUtils.isEmpty(message)) {
                sb.append("\"").append(message).append("\"");
                permissionMessages.remove(permission);
            } else {
                switch (permission) {
                    case Manifest.permission.READ_EXTERNAL_STORAGE:
                    case Manifest.permission.WRITE_EXTERNAL_STORAGE:
                        sb.append("\"读写手机存储\"");
                        break;
                    case Manifest.permission.INTERNET:
                        sb.append("\"连接网络\"");
                        break;
                    case Manifest.permission.CAMERA:
                        sb.append("\"照相机\"");
                        break;
                    case Manifest.permission.READ_PHONE_STATE:
                        sb.append("\"读取本机识别码\"");
                        break;
                    case Manifest.permission.ACCESS_NETWORK_STATE:
                        sb.append("\"获取网络状态\"");
                        break;
                    case Manifest.permission.ACCESS_WIFI_STATE:
                        sb.append("\"获取WIFI状态\"");
                        break;
                    case Manifest.permission.READ_CONTACTS:
                    case Manifest.permission.WRITE_CONTACTS:
                        sb.append("\"获取手机联系人\"");
                        break;
                    case Manifest.permission.RECORD_AUDIO:
                        sb.append("\"录音功能\"");
                        break;
                    case Manifest.permission.WRITE_APN_SETTINGS:
                        sb.append("[\"访问网络接入方式\"");
                        break;
                    case Manifest.permission.ACCESS_FINE_LOCATION:
                        sb.append("\"设备位置,获取天气信息\"");
                        break;
                    case Manifest.permission.CALL_PHONE:
                        sb.append("\"通话记录\"");
                        break;
                    case Manifest.permission.GET_ACCOUNTS:
                        sb.append("\"访问设置帐号\"");
                        break;

                }
            }
            if (i != permissions.length - 1) {
                sb.append("、").append("\n");
            } else {
                sb.append("，\n以提供更好的服务，同时保护隐私安全。\n");
            }
        }
        //sb.append("我们不会收集您的隐私信息，请给予授权通过。");
        Spannable spannable = new SpannableString(sb);
        spannable.setSpan(new AbsoluteSizeSpan(ScreenUtils.dp2px(context, 15)), 0, sb.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        return spannable;
    }

    public static void openPermissionSettingActivity(Context context) {
        try {
            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            Uri uri = Uri.fromParts("package", context.getPackageName(), null);
            intent.setData(uri);
            context.startActivity(intent);
        } catch (Exception e) {
            SmToast.show(context, "设置打开错误，请手动到设置中授权");
        }
    }

    private static Map<String, String> permissionMessages = new ArrayMap<>();

    /**
     * 权限申请时候，显示给用户的文字信息
     * 采用【用后即清除】模式。即每次需要显示自定义文字都要重新注册文字内容
     *
     * @param permission
     * @param message
     */
    public static void registerPermissionMessage(String permission, String message) {
        permissionMessages.put(permission, message);
    }
}

