package com.xx.module.common.model.util;


import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;
import android.support.v4.util.ArrayMap;
import android.util.Log;

import com.mrkj.lib.common.util.AppUtil;
import com.xx.lib.db.entity.UserSystem;
import com.xx.module.common.UserDataManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.Field;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

public class CrashHandler implements Thread.UncaughtExceptionHandler {
    public static final String TAG = "CrashHandler";
    private String mCrashPath;

    /**
     * 系统默认的UncaughtException处理类
     */
    private Thread.UncaughtExceptionHandler mDefaultHandler;
    /**
     * CrashHandler实例
     */
    private static CrashHandler instance;
    /**
     * 程序的Context对象
     */
    private Context mContext;
    /**
     * 用来存储设备信息和异常信息
     */
    private Map<String, String> infos = new ArrayMap<>();

    private SimpleDateFormat formatter;
    // private DBCommonSession<CrashBean> crashBeanSession;

    /**
     * 保证只有一个CrashHandler实例
     */
    private CrashHandler() {
        formatter = (SimpleDateFormat) DateFormat.getDateInstance();
        formatter.applyPattern("yyyy-MM-dd-HH-mm-ss");

    }

    /**
     * 获取CrashHandler实例 ,单例模式
     */
    public static CrashHandler getInstance() {
        if (instance == null) {
            instance = new CrashHandler();
        }
        return instance;
    }

    /**
     * 初始化
     */
    public void init(Context context) {
        if (mContext == null) {
            mContext = context;
            //获取系统默认的UncaughtException处理器
            mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();
            //设置该CrashHandler为程序的默认处理器
            Thread.setDefaultUncaughtExceptionHandler(this);
        }
    }

    /**
     * 当UncaughtException发生时会转入该函数来处理
     */
    @Override
    public void uncaughtException(Thread thread, Throwable ex) {
        if (!handleException(ex) && mDefaultHandler != null) {
            //如果自己处理失败，交给系统处理异常
            mDefaultHandler.uncaughtException(thread, ex);
        } else {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            android.os.Process.killProcess(android.os.Process.myPid());
            //  System.exit(0);
        }
    }

    /**
     * 自定义错误处理,收集错误信息 发送错误报告等操作均在此完成.
     *
     * @param ex
     * @return true:如果处理了该异常信息;否则返回false.
     */
    private boolean handleException(Throwable ex) {
        if (ex == null) {
            return false;
        }
        //收集设备参数信息
        collectDeviceInfo(mContext);
        //保存日志文件
        saveCatchInfo2File(ex);
        return true;
    }

    /**
     * 收集设备参数信息
     *
     * @param ctx
     */
    public void collectDeviceInfo(Context ctx) {
        try {
            PackageManager pm = ctx.getPackageManager();
            PackageInfo pi = pm.getPackageInfo(ctx.getPackageName(), PackageManager.GET_ACTIVITIES);
            if (pi != null) {
                String versionName = pi.versionName == null ? "null" : pi.versionName;
                String versionCode = pi.versionCode + "";
                infos.put("versionName", versionName);
                infos.put("versionCode", versionCode);
            }
        } catch (PackageManager.NameNotFoundException e) {
            // Log.e(TAG, "an error occured when collect package info", e);
        }
        Field[] fields = Build.class.getDeclaredFields();
        for (Field field : fields) {
            try {
                field.setAccessible(true);
                infos.put(field.getName(), field.get(null).toString());
                //   Log.i(TAG, field.getName() + " : " + field.get(null));
            } catch (Exception e) {
                //  Log.i(TAG, "an error occured when collect crash info", e);
            }
        }
    }

    /**
     * 保存错误信息到文件中
     *
     * @param ex
     * @return 返回文件名称, 便于将文件传送到服务器
     */
    public String saveCatchInfo2File(Throwable ex) {
        try {
            StringBuilder sb = new StringBuilder();
            String mob = null, scode = null, content = null, uid = null;
            for (Map.Entry<String, String> entry : infos.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();
                sb.append(key + "=" + value + "\n");
                if (key.contains("MODEL")) {
                    mob = value;
                }
                if (key.contains("FINGERPRINT")) {
                    scode = value;
                }
            }

            UserSystem system = UserDataManager.getInstance().getUserSystem();
            if (system != null) {
                //  uid = String.valueOf(system.getUserId());
            }
            Writer writer = new StringWriter();
            PrintWriter printWriter = new PrintWriter(writer);
            ex.printStackTrace(printWriter);
            Throwable cause = null;
            try {
                cause = ex.getCause();
            } catch (Exception ignored) {

            }
            while (cause != null) {
                cause.printStackTrace(printWriter);
                cause = cause.getCause();
            }
            printWriter.close();
            String result = writer.toString();
            //以下为保存到本地
            sb.append(result);

            long timestamp = System.currentTimeMillis();
            String time = formatter.format(new Date());
            String fileName = "crash-" + time + "-" + timestamp + ".log";
            if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                mCrashPath = AppUtil.getExtraFileDir(mContext) + File.separator + "log";
                File dir = new File(mCrashPath);
                if (!dir.exists()) {
                    dir.mkdirs();
                }
                File file = new File(mCrashPath, fileName);
                if (file.exists()) {
                    file.delete();
                }
                file.createNewFile();
                FileOutputStream fos = new FileOutputStream(file);
                fos.write(sb.toString().getBytes());
                fos.close();
            }
            return fileName;
        } catch (Exception e) {
            Log.e(TAG, "an error occured while writing file...", e);
        }

        return null;
    }

}
