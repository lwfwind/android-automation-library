package com.qa.automation.android.util;

import android.content.Context;
import android.graphics.Point;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Environment;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;

import java.io.File;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;

/**
 * 需要在application中初始化
 */
public class DeviceUtil {
    /**
     * The constant TAG.
     */
    public static final String TAG = "DeviceUtil";
    private static TelephonyManager telephonyManager;
    private static ConnectivityManager connectivityManager;
    private static WindowManager windowManager;
    private static Context mContext;

    /**
     * Init.
     *
     * @param context the context
     */
    public static void init(Context context) {
        if (context == null) {
            Log.w(TAG, "mContext==null when initialize DeviceUtil");
            return;
        }
        mContext = context.getApplicationContext();
        if (telephonyManager == null) {
            telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        }
        if (connectivityManager == null) {
            connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        }
        if (windowManager == null) {
            windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        }

    }


    /**
     * 获取设备ID
     *
     * @return 设备ID device id
     */
    public static String getDeviceId() {
        String IMEI = telephonyManager.getDeviceId();
        if (TextUtils.isEmpty(IMEI)) {
            IMEI = Settings.Secure.getString(mContext.getContentResolver(), Settings.Secure.ANDROID_ID);
        }
        return IMEI;
    }

    /**
     * 获取运营商名称
     *
     * @return network operator name
     */
    public static String getNetworkOperatorName() {
        return telephonyManager.getNetworkOperatorName();
    }

    /**
     * 获取电话状态
     *
     * @return CALL_STATE_IDLE 无任何状态时 CALL_STATE_OFFHOOK 接起电话时 CALL_STATE_RINGING 电话进来时
     */
    public static int getCallState() {
        return telephonyManager.getCallState();
    }

    /**
     * 获取设备型号
     *
     * @return device mode
     */
    public static String getDeviceMode() {
        return Build.MODEL;
    }

    /**
     * 获取android系统版本号
     *
     * @return device version code
     */
    public static String getDeviceVersionCode() {
        return Build.VERSION.RELEASE;
    }

    /**
     * 当前网络状态
     *
     * @return NETWORK_WIFI = 100; NETWORK_TYPE_UNKNOWN = 0; NETWORK_TYPE_GPRS = 1; NETWORK_TYPE_EDGE = 2; NETWORK_TYPE_UMTS = 3; NETWORK_TYPE_CDMA = 4; NETWORK_TYPE_EVDO_0 = 5; NETWORK_TYPE_EVDO_A = 6; NETWORK_TYPE_1xRTT = 7; NETWORK_TYPE_HSDPA = 8; NETWORK_TYPE_HSUPA = 9; NETWORK_TYPE_HSPA = 10; NETWORK_TYPE_IDEN = 11; NETWORK_TYPE_EVDO_B = 12; LTE NETWORK_TYPE_LTE = 13; NETWORK_TYPE_EHRPD = 14; NETWORK_TYPE_HSPAP = 15; GSM NETWORK_TYPE_GSM = 16;
     */
    public static int getNetworkType() {
        return isWifiConnected() ? 100 : telephonyManager.getNetworkType();
    }

    /**
     * 获取网络的具体类型
     * 0:未知或断网
     * 1:Wi-Fi
     * 2:移动网络
     *
     * @return network detail type
     */
    public static int getNetworkDetailType() {
        if (getNetworkType() == 0) {
            return 0;
        } else if (getNetworkType() == 100) {
            return 1;
        } else {
            return 2;
        }
    }

    /**
     * 检测wifi是否连接
     *
     * @return boolean boolean
     */
    public static boolean isWifiConnected() {
        NetworkInfo ni = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        return ni.isConnected();
    }

    /**
     * 获取显示屏信息
     *
     * @return device display
     */
    public static Display getDeviceDisplay() {
        return windowManager.getDefaultDisplay();
    }

    /**
     * Gets device width.
     *
     * @return the device width
     */
    public static int getDeviceWidth() {
        Point p = new Point();
        windowManager.getDefaultDisplay().getSize(p);
        return p.x;
    }

    /**
     * Gets device height.
     *
     * @return the device height
     */
    public static int getDeviceHeight() {
        Point p = new Point();
        windowManager.getDefaultDisplay().getSize(p);
        return p.y;
    }

    /**
     * Gets status bar height.
     *
     * @param context the context
     * @return the status bar height
     */
    public static int getStatusBarHeight(Context context) {
        int result = 0;
        int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = context.getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }


    /**
     * Gets external storage directory.
     *
     * @return the external storage directory
     */
    public static File getExternalStorageDirectory() {
        return Environment.getExternalStorageDirectory();
    }

//    /**
//     * 返回APP的外置系统缓存目录，清除缓存时会清除这里的内容
//     * @return
//     */
//    public static File getExternalAppCacheFile(){
//        return mContext.getExternalCacheDir();
//    }

    /**
     * 返回app的外置系统缓存目录，清除缓存时会清除这里的内容
     *
     * @return external app cache path
     */
    public static String getExternalAppCachePath() {
        File fileDir = mContext.getExternalFilesDir("");
        if (fileDir != null) {
            return fileDir.getAbsolutePath();
        }
        return getExternalStorageDirectory().getAbsolutePath() + File.separator + "Android/data/" + AppInfoUtil.getAppPackageName() + File.separator + "files";
    }

    /**
     * 返回我们自定义的缓存目录，不会被清除缓存
     *
     * @return externale primary cache file
     */
    public static File getExternalePrimaryCacheFile() {
        return new File(Environment.getExternalStorageDirectory().getAbsolutePath(), AppInfoUtil.getAppName());
    }

    /**
     * Gets external download directory.
     *
     * @return the external download directory
     */
    public static File getExternalDownloadDirectory() {
        return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
    }


    /**
     * 判断当前设备是否已经root
     *
     * @return int int
     */
    public static int isRoot() {
        int rootStatus = 0;
        try {
            if ((new File("/system/bin/su").exists()) || (new File("/system/xbin/su").exists())) {
                rootStatus = 1;
            }
        } catch (Exception e) {
            Log.w(TAG, "check isRoot error:" + e.getMessage());
        }
        return rootStatus;
    }

    /**
     * Gets ip.
     *
     * @return the ip
     */
    public static String getIp() {
        String res = null;
        if (isWifiConnected()) {
            res = getIpUnderWifi();
        } else {
            res = getIpWithoutWifi();
        }
        return res;
    }

    /**
     * 非wifi状态下获取ip
     *
     * @return ip without wifi
     */
    public static String getIpWithoutWifi() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements(); ) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumeration = intf.getInetAddresses(); en.hasMoreElements(); ) {
                    InetAddress inetAddress = enumeration.nextElement();
                    if (inetAddress.isLoopbackAddress()) {
                        return inetAddress.getHostAddress().toString();
                    }
                }
            }
        } catch (Exception e) {
            Log.w(TAG, "getIpWithoutWifi error:" + e.getMessage());
        }
        return null;
    }

    /**
     * WIFI状态下获取ip
     *
     * @return
     */
    private static String getIpUnderWifi() {
        WifiManager wifiManager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
        int ipAddress = wifiManager.getConnectionInfo().getIpAddress();
        Log.w(TAG, "getIp  isWifiConnected  ipaddress=" + ipAddress);
        return String.format("%d.%d.%d.%d", ipAddress & 0xff, ipAddress >> 8 & 0xff, ipAddress >> 16 & 0xff, ipAddress >> 24 & 0xff);
    }

    /**
     * Gets screen height.
     *
     * @param context the context
     * @return the screen height in pixels
     */
    public static int getScreenHeight(Context context) {
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = windowManager.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        return size.y;
    }

    /**
     * Gets screen width.
     *
     * @param context the context
     * @return the screen width in pixels
     */
    public static int getScreenWidth(Context context) {
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = windowManager.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        return size.x;
    }

}
