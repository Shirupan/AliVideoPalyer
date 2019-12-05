package com.mrkj.lib.common.util;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.graphics.Rect;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.provider.Settings;
import android.support.annotation.AttrRes;
import android.support.annotation.ColorRes;
import android.support.annotation.IdRes;
import android.support.annotation.IntegerRes;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.PermissionChecker;
import android.support.v4.util.ArrayMap;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;

import com.mrkj.lib.common.view.SmToast;

import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;


/**
 * Created by Administrator on 2016-04-11.
 */
public class AppUtil {
    public static String device_signid;
    public static String PACKAGE_NAME_QQ = "com.tencent.mobileqq";
    public static String PACKAGE_NAME_WEIXIN = "com.tencent.mm";
    public static String PACKAGE_NAME_WEIBO = "com.sina.weibo";

    /**
     * 获取应用可用的最大内存
     * 返回的是M为单位
     *
     * @param context
     * @return
     */
    public static int getAppLargerMemory(Context context) {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        return activityManager.getLargeMemoryClass();
    }

    /**
     * @param name 该ShareRefrence的名字
     * @param map  保存的键值对
     * @return
     */
    public static boolean saveToSharePreference(Context context, String name, Map<String, String> map) {
        Set<String> keys = map.keySet();
        if (keys.isEmpty()) {
            return false;
        }
        SharedPreferences sp = context.getSharedPreferences(name, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        for (String key : keys) {
            String value = map.get(key);
            if (value != null) {
                editor.putString(key, value);
            }
        }
        editor.apply();
        return true;
    }

    public static Map<String, String> getFromSharePreferences(Context context, String sharePreferencesName, String... keyName) {
        Map<String, String> map = new ArrayMap<>();
        SharedPreferences sp = context.getSharedPreferences(sharePreferencesName, Context.MODE_PRIVATE);
        if (sp != null) {
            for (String aKeyName : keyName) {
                String value = sp.getString(aKeyName, "");
                map.put(aKeyName, value);
            }
        }
        return map;
    }


    /**
     * 获取状态栏高度
     * get
     *
     * @return
     */
    public static int getStatuBarHeight(Context context) {
        return ScreenUtils.getStatusBarHeight(context);
    }

    /**
     * @return 单位MB
     */
    public static float getMaxMemory() {
        long maxMemory = (Runtime.getRuntime().maxMemory() / 1024);
        return maxMemory / 1024;
    }


    /**
     * @param context
     * @param packageName 微信com.tencent.mm
     * @return
     */
    public static boolean isAppInstalled(Context context, String packageName) {
        final PackageManager packageManager = context.getPackageManager();
        List<PackageInfo> pinfo = packageManager.getInstalledPackages(0);
        List<String> pName = new ArrayList<>();
        if (pinfo != null) {
            for (int i = 0; i < pinfo.size(); i++) {
                String pn = pinfo.get(i).packageName;
                pName.add(pn);
            }
        }
        return pName.contains(packageName);
    }

    /**
     * 是否打开了消息通知，针对4.4以上系统有用。4.4以下都是返回true
     *
     * @param context
     * @return
     */
    public static boolean isNotificationEnabled(Context context) {
        NotificationManagerCompat manager = NotificationManagerCompat.from(context);
        return manager.areNotificationsEnabled();
    }

    /**
     * 打开 系统 通知页面
     *
     * @param context
     */
    public static void openApplicationSetting(Context context) {
        Intent intent = new Intent()
                .setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                .setData(Uri.fromParts("package", context.getPackageName(), null));
        try {
            context.startActivity(intent);
        } catch (Exception e) {
            //   SmToast.show(context, "请在设置中打开应用通知管理");
            intent = new Intent(Settings.ACTION_SETTINGS);
            context.startActivity(intent);
        }
    }

    /**
     * 应用外部文件夹目录
     *
     * @return
     */
    public static String getExtraFileDir(Context context) {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            return Environment.getExternalStorageDirectory().getPath() + File.separator + "xx"
                    + File.separator + context.getPackageName();
        }
        return "";
    }

    /**
     * 相册中知命的目录
     *
     * @param context
     * @return
     */
    public static String getAppExtraDCIMDir(Context context) {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getPath() + File.separator + "xx"
                    + File.separator + context.getPackageName();
        }
        return "";
    }

    /**
     * 当前进程是否是主进程
     *
     * @param context
     * @return
     */
    public static boolean checkIsMainProcess(Context context) {
        String processName = getCurProcessName(context);
        String packageName = context.getPackageName();
        return packageName != null && packageName.equals(processName);
    }

    private static String getCurProcessName(Context context) {
        try {
            int pid = android.os.Process.myPid();
            ActivityManager mActivityManager = (ActivityManager) context
                    .getSystemService(Context.ACTIVITY_SERVICE);
            if (mActivityManager != null) {
                for (ActivityManager.RunningAppProcessInfo appProcess : mActivityManager.getRunningAppProcesses()) {
                    if (appProcess.pid == pid) {
                        return appProcess.processName;
                    }
                }

            }
        } catch (Exception e) {
            // ignore
        }
        return null;
    }

    public static void startApp(Context context, String packageName) {
        //同AndroidManifest中主入口Activity一样
        Intent intent = new Intent(Intent.ACTION_MAIN, null);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);

        //得到一个PackageManager的对象
        PackageManager packageManager = context.getApplicationContext().getPackageManager();
        //获取到主入口的Activity集合
        List<ResolveInfo> mlist = packageManager.queryIntentActivities(intent, 0);

        Collections.sort(mlist, new ResolveInfo.DisplayNameComparator(packageManager));

        for (ResolveInfo res : mlist) {
            String pkg = res.activityInfo.packageName;
            String cls = res.activityInfo.name;
            if (pkg.contains(packageName)) {
                ComponentName componentName = new ComponentName(pkg, cls);
                Intent intent1 = new Intent();
                intent1.setComponent(componentName);
                intent1.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                try {
                    context.startActivity(intent1);
                } catch (Exception e) {
                    e.printStackTrace();
                    SmToast.show(context, "未找到相应的应用信息");
                }

            }
        }
    }

    public static void openSystemSetting(Context context) {
        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", context.getPackageName(), null);
        intent.setData(uri);
        try {
            context.startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static synchronized String getIMEI(Context context) {
        if (device_signid != null && !TextUtils.isEmpty(device_signid) && !device_signid.contains("0000000000")) {
            return device_signid;
        }
        if (context == null) {
            return "imei";
        }
        //从本地读取
        SharedPreferences sharedPreferences = context.getSharedPreferences("imei", Context.MODE_PRIVATE);
        device_signid = sharedPreferences.getString("imei", "");
        if (!TextUtils.isEmpty(device_signid) && !device_signid.contains("0000000000")) {
            return device_signid;
        }
        device_signid = null;

        StringBuilder deviceId = new StringBuilder();
        String imei = null;
        TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        int flag;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            flag = context.checkSelfPermission(Manifest.permission.READ_PHONE_STATE);
        } else {
            flag = PermissionChecker.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE);
        }
        //获取imei码
        if (flag == PackageManager.PERMISSION_GRANTED && tm != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                imei = tm.getImei();
                if (TextUtils.isEmpty(imei)) {
                    imei = tm.getMeid();
                }
            } else {
                imei = tm.getDeviceId();
            }
        }
        if (!TextUtils.isEmpty(imei)) {
            deviceId.append("imei");
            deviceId.append(imei);
        }

        if (deviceId.toString().contains("imei")) {
            device_signid = deviceId.toString();
        }
        if (TextUtils.isEmpty(deviceId.toString()) || deviceId.toString().contains("0000000000")) {
            deviceId = new StringBuilder("uuid");
            deviceId.append(UUID.randomUUID());
            device_signid = deviceId.toString();
        }
        //写进本地
        SharedPreferences preferences = context.getSharedPreferences("imei", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("imei", deviceId.toString());
        editor.apply();
        return deviceId.toString();
    }


    public static String getCacheDir(Context context) {
        File dir;

        dir = context.getExternalCacheDir();
        if (dir == null) {
            dir = context.getCacheDir();
        }
        return dir.getPath();
    }

    public static boolean hasExtraStorage(Context context) {
        return Environment.isExternalStorageEmulated();
    }

    /**
     * 外部储存设备中应用数据目录的根目录
     *
     * @param context
     * @return
     */
    public static String getAppExtraStorageRootPath(Context context) {
        if (!hasExtraStorage(context)) {
            return "";
        }
        File dir = new File(getAppCachePath(context));
        return dir.getParent();
    }

    /**
     * 内部储存
     *
     * @param context
     * @return
     */
    public static File getAppRootPath(Context context) {
        return context.getFilesDir();
    }

    /**
     * @return 应用的版本号
     */
    public static int getAppVersionCode(Context context) {
        PackageManager manager = context.getPackageManager();
        String packageName = context.getPackageName();
        PackageInfo info = null;
        try {
            info = manager.getPackageInfo(packageName, 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return 0;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            return (int) info.getLongVersionCode();
        } else {
            return info.versionCode;
        }
    }

    public static String getAppVersionName(Context context) {
        PackageManager manager = context.getPackageManager();
        String name = null;
        try {
            PackageInfo info = manager.getPackageInfo(context.getPackageName(), 0);
            name = info.versionName;

        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return name;
    }


    /**
     * use{@link AppUtil#getNetworkInfoType(Context)}
     *
     * @return 0没有网络，1数据流量，2wifi
     */
    @Deprecated
    public static int getNetWorkConnectType(Context context) {
        ConnectivityManager connectivityManager;
        connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager == null) {
            return 0;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            NetworkInfo network = connectivityManager.getActiveNetworkInfo();
            if (network == null || !network.isAvailable()) {
                return 0;
            } else {
                String name = network.getTypeName();
                if (network.getState() == NetworkInfo.State.CONNECTED) {
                    //更改NetworkStateService的静态变量，之后只要在Activity中进行判断就好了
                    if (TextUtils.equals(name.toLowerCase(), "WIFI".toLowerCase())) {
                        return 2;
                    } else {
                        return 1;
                    }
                }
            }
        } else {
            NetworkInfo[] networks = connectivityManager.getAllNetworkInfo();
            for (NetworkInfo info : networks) {
                if (info.getState() == NetworkInfo.State.CONNECTED) {
                    String name = info.getTypeName();
                    if (info.getState() == NetworkInfo.State.CONNECTED) {
                        //更改NetworkStateService的静态变量，之后只要在Activity中进行判断就好了
                        if (name.equals("WIFI")) {
                            return 2;
                        } else {
                            return 1;
                        }
                    }
                }
            }
        }
        return 0;


    }

    /**
     * @param context
     * @return {@link ConnectivityManager#TYPE_DUMMY} //不可用
     * {@link ConnectivityManager#TYPE_WIFI} //wifi
     * {@link ConnectivityManager#TYPE_MOBILE} //手机流量
     * 等等
     */
    public static int getNetworkInfoType(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager == null) {
            return ConnectivityManager.TYPE_DUMMY;
        }
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if (networkInfo == null) {
            return ConnectivityManager.TYPE_DUMMY;
        }
        return networkInfo.getType();
    }


    public static void closeInputWindow(final View view) {
        InputMethodManager methodManager;
        methodManager = (InputMethodManager) view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (methodManager != null) {
            methodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }


    public static void showSoftInputWindow(View view) {
        view.setFocusable(true);
        view.setFocusableInTouchMode(true);
        view.requestFocus();
        InputMethodManager methodManager;
        methodManager = (InputMethodManager) view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (methodManager != null) {
            methodManager.showSoftInput(view, 0);
        }
    }

    public static boolean isSoftInputShowing(Activity activity) {
        //获取当屏幕内容的高度
        int screenHeight = activity.getWindow().getDecorView().getHeight();
        //获取View可见区域的bottom
        Rect rect = new Rect();
        //DecorView即为activity的顶级view
        activity.getWindow().getDecorView().getWindowVisibleDisplayFrame(rect);
        //考虑到虚拟导航栏的情况（虚拟导航栏情况下：screenHeight = rect.bottom + 虚拟导航栏高度）
        //选取screenHeight*2/3进行判断
        return screenHeight * 2 / 3 > rect.bottom;
    }

    /**
     * @param context
     * @param s       复制的内容
     * @param tip     toast提示信息
     */
    public static void copyToBoard(Context context, String s, String tip) {
        ClipboardManager cmb = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        if (cmb != null) {
            cmb.setPrimaryClip(ClipData.newPlainText(null, s));
            if (TextUtils.isEmpty(tip)) {
                tip = "已复制到粘贴板";
            }
            SmToast.show(context, tip);
        }
    }

    /**
     * @return 屏幕可显示区域高度.若是4.4以上系统，包括状态栏显示区域。4.4以下不包括状态栏
     */
    public static int getScreenHeight(Context context) {
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics dm = new DisplayMetrics();
        if (windowManager != null) {
            windowManager.getDefaultDisplay().getMetrics(dm);
            return dm.heightPixels;
        } else {
            DisplayMetrics dm1 = context.getResources().getDisplayMetrics();
            int height = dm1.heightPixels;
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
                height = height - AppUtil.getStatuBarHeight(context);
            }
            return height;
        }
    }


    /**
     * @return 屏幕宽度
     */
    public static int getScreenWidth(Context context) {
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        return dm.widthPixels;
    }


    /**
     * @return 屏幕分辨率
     */
    public static float getDensity(Context context) {
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        return dm.density;
    }

    /**
     * 根据资源id获取资源字符串名称
     *
     * @param context
     * @param id
     * @return
     */
    public static String getResourceNameFromResId(Context context, @IdRes int id) {
        if (context == null) {
            return "";
        }
        String path;
        try {
            path = context.getResources().getResourceName(id);
        } catch (Exception e) {
            return context.getClass().getSimpleName();
        }
        int index = path.indexOf("/");
        String vName = "";
        if (index != -1) {
            vName = path.substring(index + 1); //取得控件id对应的字符串
        }
        return vName;
    }

    /**
     * 获取meta-data下对应的key的值
     * 注意：取值需要注意，若纯数字值超长，可能会取值错误，请在value加前缀value_
     *
     * @param ctx
     * @param key meta-data 的名称
     * @return
     */
    public static String getAppMetaData(Context ctx, String key) {
        if (ctx == null || TextUtils.isEmpty(key)) {
            return "";
        }
        String resultData = null;
        try {
            PackageManager packageManager = ctx.getPackageManager();
            if (packageManager != null) {
                ApplicationInfo applicationInfo = packageManager.getApplicationInfo(ctx.getPackageName(), PackageManager.GET_META_DATA);
                if (applicationInfo != null) {
                    if (applicationInfo.metaData != null) {
                        Object value = applicationInfo.metaData.get(key);
                        if (value instanceof String) {
                            resultData = ((String) value).replace("value_", "");
                        } else {
                            resultData = String.valueOf(value);
                        }
                    }
                }

            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        return resultData;
    }

    /**
     * 获取外部储存剩余空间(MB)
     *
     * @return
     */
    public static long getSDFreeSize() {
        //取得SD卡文件路径
        File path = Environment.getExternalStorageDirectory();
        StatFs sf = new StatFs(path.getPath());
        //获取单个数据块的大小(Byte)
        long blockSize;
        //空闲的数据块的数量
        long freeBlocks;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR2) {
            blockSize = sf.getBlockSizeLong();
            freeBlocks = sf.getAvailableBlocksLong();
        } else {
            blockSize = sf.getBlockSize();
            freeBlocks = sf.getAvailableBlocks();
        }
        //返回SD卡空闲大小
        //return freeBlocks * blockSize;  //单位Byte
        //return (freeBlocks * blockSize)/1024;   //单位KB
        return (freeBlocks * blockSize) / 1024 / 1024; //单位MB
    }

    /**
     * 获取手机外部储存的总空间（MB）
     *
     * @return
     */
    public static long getSDAllSize() {
        //取得SD卡文件路径
        File path = Environment.getExternalStorageDirectory();
        StatFs sf = new StatFs(path.getPath());
        //获取单个数据块的大小(Byte)
        long blockSize = sf.getBlockSize();
        //获取所有数据块数
        long allBlocks = sf.getBlockCount();
        //返回SD卡大小
        //return allBlocks * blockSize; //单位Byte
        //return (allBlocks * blockSize)/1024; //单位KB
        return (allBlocks * blockSize) / 1024 / 1024; //单位MB
    }


    public static String getAppCachePath(Context context) {
        File dir = context.getExternalCacheDir();
        if (dir == null) {
            dir = context.getCacheDir();
        }
        return dir.getPath();
    }


    private static String getMobileType() {
        return Build.MANUFACTURER;
    }


    /**
     * 进入自启动页面
     * Compatible Mainstream Models 兼容市面主流机型
     *
     * @param context
     */
    public static void openAutoStartSetting(Context context) {
        String mtype = android.os.Build.MODEL; // 手机型号
        Intent intent = new Intent();
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        ComponentName componentName = null;
        if (mtype.startsWith("Redmi") || mtype.startsWith("MI")) {
            componentName = new ComponentName("com.miui.securitycenter", "com.miui.permcenter.autostart.AutoStartManagementActivity");
        } else if (mtype.startsWith("HUAWEI")) {
            componentName = new ComponentName("com.huawei.systemmanager", "com.huawei.systemmanager.startupmgr.ui.StartupNormalAppListActivity");
        } else if (mtype.startsWith("vivo")) {
            componentName = new ComponentName("com.iqoo.secure", "com.iqoo.secure.ui.phoneoptimize.AddWhiteListActivity");
        } else if (mtype.startsWith("ZTE")) {
            componentName = new ComponentName("com.zte.heartyservice", "com.zte.heartyservice.autorun.AppAutoRunManager");
        } else if (mtype.startsWith("F")) {
            componentName = new ComponentName("com.gionee.softmanager", "com.gionee.softmanager.oneclean.AutoStartMrgActivity");
        } else if (mtype.startsWith("oppo")) {
            componentName = new ComponentName("oppo com.coloros.oppoguardelf", "com.coloros.powermanager.fuelgaue.PowerUsageModelActivity");
        }
        intent.setComponent(componentName);
        try {
            context.startActivity(intent);
        } catch (Exception e) {//抛出异常就直接打开设置页面
            openApplicationSetting(context);
        }
    }


    /**
     * 获取是否存在NavigationBar，是否有虚拟按钮
     */
    public static boolean checkDeviceHasNavigationBar(Context context) {
        boolean hasNavigationBar = false;
        Resources rs = context.getResources();
        int id = rs.getIdentifier("config_showNavigationBar", "bool", "android");
        if (id > 0) {
            hasNavigationBar = rs.getBoolean(id);
        }
        try {
            Class systemPropertiesClass = Class.forName("android.os.SystemProperties");
            Method m = systemPropertiesClass.getMethod("get", String.class);
            String navBarOverride = (String) m.invoke(systemPropertiesClass, "qemu.hw.mainkeys");
            if ("1".equals(navBarOverride)) {
                hasNavigationBar = false;
            } else if ("0".equals(navBarOverride)) {
                hasNavigationBar = true;
            }
        } catch (Exception e) {

        }
        return hasNavigationBar;

    }

    /**
     * 获取虚拟按钮ActionBar的高度
     *
     * @param activity activity
     * @return ActionBar高度
     */
    public static int getActionBarHeight(Activity activity) {
        TypedValue tv = new TypedValue();
        if (activity.getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true)) {
            return TypedValue.complexToDimensionPixelSize(tv.data, activity.getResources().getDisplayMetrics());
        }
        return 0;
    }


    /**
     * 获取虚拟按键的高度
     *
     * @param context
     * @return
     */
    public static int getNavigationBarHeight(Context context) {
        int result = 0;
        if (hasNavBar(context)) {
            Resources res = context.getResources();
            int resourceId = res.getIdentifier("navigation_bar_height", "dimen", "android");
            if (resourceId > 0) {
                result = res.getDimensionPixelSize(resourceId);
            }
        }
        return result;
    }

    /**
     * 检查是否存在虚拟按键栏
     *
     * @param context
     * @return
     */
    private static boolean hasNavBar(Context context) {
        Resources res = context.getResources();
        int resourceId = res.getIdentifier("config_showNavigationBar", "bool", "android");
        if (resourceId != 0) {
            boolean hasNav = res.getBoolean(resourceId);
            // check override flag
            String sNavBarOverride = getNavBarOverride();
            if ("1".equals(sNavBarOverride)) {
                hasNav = false;
            } else if ("0".equals(sNavBarOverride)) {
                hasNav = true;
            }
            return hasNav;
        } else { // fallback
            return !ViewConfiguration.get(context).hasPermanentMenuKey();
        }
    }


    /**
     * 判断虚拟按键栏是否重写
     *
     * @return
     */
    private static String getNavBarOverride() {
        String sNavBarOverride = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            try {
                Class c = Class.forName("android.os.SystemProperties");
                Method m = c.getDeclaredMethod("get", String.class);
                m.setAccessible(true);
                sNavBarOverride = (String) m.invoke(null, "qemu.hw.mainkeys");
            } catch (Throwable e) {
            }
        }
        return sNavBarOverride;
    }

    public static void setText2Clipboard(Context context, String msg) {
        ClipboardManager cm = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData data = ClipData.newPlainText(msg, msg);
        cm.setPrimaryClip(data);
    }

    /**
     * 获取自定义属性颜色Id值
     *
     * @param theme
     * @param attr
     * @param defaultResId
     * @return
     */
    @ColorRes
    public static int getThemeColor(Resources.Theme theme, @AttrRes int attr, @ColorRes int defaultResId) {
        TypedValue value = new TypedValue();
        theme.resolveAttribute(attr, value, true);
        if (value.resourceId == 0) {
            return defaultResId;
        } else {
            return value.resourceId;
        }
    }
}


