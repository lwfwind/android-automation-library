package com.qa.automation.android.util;

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.TrafficStats;


/**
 * The type App infos util.
 */
public class AppInfoUtil {

    /**
     * The constant CHANNEL_KXP.
     */
    public static final String CHANNEL_KXP = "kuaxvepai";

    private static Context mContext;

    /**
     * Init.
     *
     * @param contxt the contxt
     */
    public static void init(Context contxt) {
        mContext = contxt;
    }


    /**
     * Gets app package name.
     *
     * @return the app package name
     */
    public static String getAppPackageName() {
        return mContext.getPackageName();
    }

    /**
     * get version name
     *
     * @return current version name
     */
    public static String getAPPVersion() {
        try {
            return mContext.getPackageManager().getPackageInfo(mContext.getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return "";
        }
    }


    /**
     * Gets app name.
     *
     * @return the app name
     */
    public static String getAppName() {
        return mContext.getApplicationInfo().loadLabel(mContext.getPackageManager()).toString();
    }


    /**
     * Gets record audio permission.
     *
     * @return the record audio permission
     */
    public static boolean getRecordAudioPermission() {
        return PackageManager.PERMISSION_GRANTED == mContext.getPackageManager().checkPermission("android.permission.RECORD_AUDIO", getAppPackageName());
    }

    /**
     * 获取手机启动后本应用总接受数据大小KB
     *
     * @return total rx byte
     */
    public static long getTotalRxByte() {
        long totalBytes = TrafficStats.getUidRxBytes(mContext.getApplicationInfo().uid) / 1024;//转为KB
        return totalBytes == TrafficStats.UNSUPPORTED ? 0 : totalBytes;
    }

    /**
     * 获取手机启动后本应用总上行数据大小KB
     *
     * @return total tx byte
     */
    public static long getTotalTxByte() {
        long totalBytes = TrafficStats.getUidTxBytes(mContext.getApplicationInfo().uid) / 1024;//转为KB
        return totalBytes == TrafficStats.UNSUPPORTED ? 0 : totalBytes;
    }


    /**
     * 获取app发布的渠道名称
     *
     * @return channel name
     */
    public static String getChannelName() {
        String resultData = "";
        if (mContext == null) {
            return resultData;
        }
        try {
            PackageManager packageManager = mContext.getPackageManager();
            if (packageManager == null) {
                return resultData;
            }
            ApplicationInfo applicationInfo = packageManager.getApplicationInfo(mContext.getPackageName(), PackageManager.GET_META_DATA);
            if (applicationInfo == null || applicationInfo.metaData == null) {
                return resultData;
            }
            resultData = applicationInfo.metaData.getString("UMENG_CHANNEL");
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (resultData == null) {
            resultData = "";
        }
        return resultData;
    }

    /**
     * Gets cur process name.
     *
     * @return the cur process name
     */
    public static String getCurProcessName() {
        int pid = android.os.Process.myPid();
        ActivityManager mActivityManager = (ActivityManager) mContext
                .getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningAppProcessInfo appProcess : mActivityManager
                .getRunningAppProcesses()) {
            if (appProcess.pid == pid) {
                return appProcess.processName;
            }
        }
        return null;
    }

    /**
     * Gets uid.
     *
     * @return the uid
     */
    public static int getUid() {
        return mContext.getApplicationInfo().uid;
    }
}
