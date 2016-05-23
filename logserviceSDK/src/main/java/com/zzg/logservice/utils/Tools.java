package com.zzg.logservice.utils;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.graphics.Point;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Debug;
import android.os.Environment;
import android.os.StatFs;
import android.telephony.TelephonyManager;
import android.telephony.cdma.CdmaCellLocation;
import android.telephony.gsm.GsmCellLocation;
import android.text.format.Formatter;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;

import com.google.gson.JsonObject;
import com.zzg.logservice.config.AppConstant;
import com.zzg.logservice.service.LogService;

/**
 * 全局共用的常用方法类
 *
 * @author Abelzzg
 */
public class Tools {

    /**
     * 定义一个全局的Log日志输出，以便日后统一关闭
     */
    public static void log(String message) {
        if (isDebuggable()) {
            log("yeepayAnalytics", message);
        }
    }

    /**
     * 定义一个全局的Log日志输出，以便日后统一关闭
     */
    public static void log(String tag, String message) {
        if (isDebuggable()) {
            Log.e(tag, message);
        }
    }

    /**
     * appkey添加模式 application标签对中添加： <meta-data android:name="YEEPAY_APPKEY"
     * android:value="4f83c5d852701564c0000011" > </meta-data> 得到APP_KEY
     *
     * @return appkey
     */
    public static String getAppkey() {
        String appkey = "";
        try {
            ApplicationInfo appInfo = LogService.globalContext
                    .getPackageManager().getApplicationInfo(
                            LogService.globalContext.getPackageName(),
                            PackageManager.GET_META_DATA);
            if (appInfo.metaData == null)
                return appkey;
            appkey = appInfo.metaData.getString("YEEPAY_APPKEY");
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return appkey;
    }

    /**
     * 获取使用内存大小
     */
    public static String getMemory() {
        int pss = 0;
        ActivityManager myAM = (ActivityManager) LogService.globalContext
                .getSystemService(Context.ACTIVITY_SERVICE);
        String packageName = LogService.globalContext.getPackageName();
        List<ActivityManager.RunningAppProcessInfo> appProcesses = myAM
                .getRunningAppProcesses();
        for (ActivityManager.RunningAppProcessInfo appProcess : appProcesses) {
            if (appProcess.processName.equals(packageName)) {
                int pids[] = {appProcess.pid};
                Debug.MemoryInfo self_mi[] = myAM.getProcessMemoryInfo(pids);
                pss = self_mi[0].getTotalPss();
            }
        }
        return Formatter.formatFileSize(LogService.globalContext, pss * 1024);
    }

    public static String getTotalMemory() {
        String str1 = "/proc/meminfo";// 系统内存信息文件
        String str2;
        String[] arrayOfString;
        long initial_memory = 0;
        try {
            FileReader localFileReader = new FileReader(str1);
            BufferedReader localBufferedReader = new BufferedReader(
                    localFileReader, 8192);
            str2 = localBufferedReader.readLine();// 读取meminfo第一行，系统总内存大小
            arrayOfString = str2.split("\\s+");
            for (String num : arrayOfString) {
                Log.i(str2, num + "\t");
            }
            initial_memory = Integer.valueOf(arrayOfString[1]).intValue() * 1024;// 获得系统总内存，单位是KB，乘以1024转换为Byte
            localBufferedReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Formatter.formatFileSize(LogService.globalContext, initial_memory);// Byte转换为KB或者MB，内存大小规格化
    }

    /**
     * 获取分辨率
     *
     * @return
     */
    public static String getResolution() {
        WindowManager wm = (WindowManager) LogService.globalContext.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;
        int height = size.y;
        return width + "*" + height;
    }

    /**
     * 获取像素密度
     *
     * @return
     */
    public static String getDpi() {
        DisplayMetrics metric = new DisplayMetrics();
        Activity activity = (Activity) (LogService.globalContext);
        activity.getWindowManager().getDefaultDisplay().getMetrics(metric);
        int densityDpi = metric.densityDpi;  // 屏幕密度DPI（120 / 160 / 240）
        return densityDpi + "dpi";
    }

    /**
     * 获取location area code 位置区编码
     *
     * @return
     */
    public static int getLac() {
        try {
            TelephonyManager telephonyManager = (TelephonyManager) LogService.globalContext
                    .getSystemService(Context.TELEPHONY_SERVICE);
            GsmCellLocation location = (GsmCellLocation) telephonyManager
                    .getCellLocation();
            if (location == null) {
                return 0;
            } else {
                int lac = location.getLac();
                return lac;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }

    }

    /**
     * 手机信号覆盖区域的的编号ID 获取cell_id
     *
     * @return
     */
    public static int getCellId() {
        // 中国移动和中国联通获取LAC、CID的方式
        TelephonyManager telephonyManager = (TelephonyManager) LogService.globalContext.getApplicationContext()
                .getSystemService(Context.TELEPHONY_SERVICE);
        try {
            GsmCellLocation location = (GsmCellLocation) telephonyManager
                    .getCellLocation();
            if (location == null) {
                return 0;
            } else {
                int cellId = location.getCid();
                return cellId;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * 获取versionCode（ANDROID版本号）
     */
    public static String getDeviceVersion() {
        String sdkVersion = Build.VERSION.RELEASE;
        return sdkVersion;
    }

    /**
     * 获取目标APK的编译版本
     * @return
     */
    public static String getTargetAndroidVersion() {
        int targetSdkVersion = LogService.globalContext.getApplicationInfo().targetSdkVersion;
        return "android-"+targetSdkVersion + "";
    }

    /**
     * 获得当前使用语言
     *
     * @return
     */
    public static String getLan() {
        String lan = Locale.getDefault().getLanguage();
        return lan;
    }

    /**
     * 保存数据到SD卡
     *
     * @param data
     * @param path
     */
    public static void saveDataToSDCard(byte[] data, String path) {
        DataOutputStream dataOutStream = null;
        try {
            File file = new File(path);
            if (!file.exists()) {
                file.createNewFile();
            }
            dataOutStream = new DataOutputStream(new FileOutputStream(path));
            dataOutStream.write(data);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                dataOutStream.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
    }

    /**
     * 获取软件包名
     *
     * @return
     */
    public static String getPackageName() {
        return LogService.globalContext.getPackageName();
    }

    /**
     * 获取versionCode（ANDROID版本号）
     */
    public static String getVersionCode() {
        String sdkVersion = Build.VERSION.RELEASE;
        return sdkVersion;
    }

    /**
     * 是否为调试模式
     *
     * @return
     */
    public static boolean isDebuggable() {
        try {
            PackageInfo pinfo = LogService.globalContext.getPackageManager()
                    .getPackageInfo(getPackageName(), 0);
            if (pinfo != null) {
                int flags = pinfo.applicationInfo.flags;
                return (flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 获取平台号+版本号+渠道号
     *
     * @return
     */
    public static String getVersionName() {
        try {
            PackageInfo pinfo = LogService.globalContext.getPackageManager()
                    .getPackageInfo(getPackageName(), 0);
            String versionName = pinfo.versionName;
            if (null != versionName) {
                return versionName;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * 获取日志版本号 未完成
     */
    public static String getLogVersion() {
        // 未完成
        return null;

    }

    /**
     * 判断当前网络是否为wifi
     *
     * @return
     */
    public static boolean isWifi() {
        ConnectivityManager connectivityManager = (ConnectivityManager) LogService.globalContext
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetInfo = connectivityManager.getActiveNetworkInfo();
        if (activeNetInfo != null
                && activeNetInfo.getType() == ConnectivityManager.TYPE_WIFI) {
            return true;
        }
        return false;
    }

    /**
     * 获取网络信息
     *
     * @return
     */
    public static String getNetworkType() {
        StringBuffer sInfo = new StringBuffer();
        ConnectivityManager connectivity = (ConnectivityManager) LogService.globalContext
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity != null) {

            NetworkInfo activeNetInfo = connectivity.getActiveNetworkInfo();// 如果不是wifi则为空
            NetworkInfo mobNetInfo = connectivity
                    .getNetworkInfo(ConnectivityManager.TYPE_MOBILE);// 判断是否是手机网络
            // 非手机网络
            if (activeNetInfo != null) {
                sInfo.append(activeNetInfo.getTypeName());
            }
            // 手机网络
            else if (mobNetInfo != null) {
                sInfo.append(mobNetInfo.getSubtypeName());
            }
        }
        return sInfo.toString();
    }

    /**
     * 获取IP
     *
     * @return
     */
    public static String getLocalIPV6IpAddress() {

        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface
                    .getNetworkInterfaces(); en.hasMoreElements(); ) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf
                        .getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress()) {
                        return inetAddress.getHostAddress().toString();
                    }
                }
            }
        } catch (SocketException ex) {
            Log.e("WifiPreference IpAddress", ex.toString());
        }
        return null;
    }

    public static String saveLogHeader() {
        JsonObject jsonGlobal = new JsonObject();
        jsonGlobal.addProperty("app_key", Tools.getLogVersion());// app标示码
        jsonGlobal.addProperty("app_version", Tools.getVersionName());// 版本号
        jsonGlobal.addProperty("device_identifier", Tools.getIMEI());// 设备ID
        jsonGlobal.addProperty("local_ip", Tools.getLocalIPV6IpAddress());// 设备ID

        // 设备信息
        JsonObject device_info = new JsonObject();

        device_info.addProperty("device_model", Tools.getPhoneModel());
        device_info.addProperty("wifi_mac", Tools.getMacAddress());
        jsonGlobal.add("device_info", device_info);

        // 设备环境
        JsonObject device_env = new JsonObject();
        device_env.addProperty("language", Tools.getLan());// 语言
        device_env.addProperty("network", Tools.getNetworkType());// 网络类型
        device_env.addProperty("cell_id", Tools.getCellId());// 手机信号覆盖区域的的编号ID
        device_env.addProperty("lac", Tools.getLac());// 位置区域码
        device_env.addProperty("mcc_mnc", Tools.getIMSI());// 国家码_网络码
        jsonGlobal.add("device_env", device_env);
        return jsonGlobal.toString();
    }

    public static long getLastClickTime() {
        return lastClickTime;
    }

    public static double getLatitude() {
        Location loc = getLoc();
        if (loc != null) {
            return loc.getLatitude();
        } else
            return 0;
    }

    public static double getLongitude() {
        Location loc = getLoc();
        if (loc != null) {
            return loc.getLongitude();
        } else
            return 0;
    }

    /**
     * 判断是否是快速网络
     *
     * @return
     */
    public static boolean isFastMobileNetwork() {
        TelephonyManager telephonyManager = (TelephonyManager) LogService.globalContext
                .getSystemService(Context.TELEPHONY_SERVICE);
        if (isWifi()) {
            return true;
        }
        switch (telephonyManager.getNetworkType()) {
            case TelephonyManager.NETWORK_TYPE_1xRTT:
                return false; // ~ 50-100 kbps
            case TelephonyManager.NETWORK_TYPE_CDMA:
                return false; // ~ 14-64 kbps
            case TelephonyManager.NETWORK_TYPE_EDGE:
                return false; // ~ 50-100 kbps
            case TelephonyManager.NETWORK_TYPE_IDEN:
                return false; // ~25 kbps
            case TelephonyManager.NETWORK_TYPE_UNKNOWN:
                return false;
            case TelephonyManager.NETWORK_TYPE_GPRS:
                return false; // ~ 100 kbps
            case TelephonyManager.NETWORK_TYPE_EVDO_0:
                return true; // ~ 400-1000 kbps
            case TelephonyManager.NETWORK_TYPE_EVDO_A:
                return true; // ~ 600-1400 kbps
            case TelephonyManager.NETWORK_TYPE_HSDPA:
                return true; // ~ 2-14 Mbps
            case TelephonyManager.NETWORK_TYPE_HSPA:
                return true; // ~ 700-1700 kbps
            case TelephonyManager.NETWORK_TYPE_HSUPA:
                return true; // ~ 1-23 Mbps
            case TelephonyManager.NETWORK_TYPE_UMTS:
                return true; // ~ 400-7000 kbps
            case TelephonyManager.NETWORK_TYPE_EHRPD:
                return true; // ~ 1-2 Mbps
            case TelephonyManager.NETWORK_TYPE_EVDO_B:
                return true; // ~ 5 Mbps
            case TelephonyManager.NETWORK_TYPE_HSPAP:
                return true; // ~ 10-20 Mbps
            case TelephonyManager.NETWORK_TYPE_LTE:
                return true; // ~ 10+ Mbps
            default:
                return false;
        }
    }

    /**
     * 获取wifi的mac地址
     *
     * @return
     */
    public static String getMacAddress() {
        try {
            WifiManager wifi = (WifiManager) LogService.globalContext
                    .getSystemService(Context.WIFI_SERVICE);
            WifiInfo info = wifi.getConnectionInfo();
            String mac = info.getMacAddress();
            if (null != mac) {
                return mac;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * 获取硬盘大小
     *
     * @return
     */
    public static String getTotalHardDiskInfo() {
        File root = Environment.getRootDirectory();
        StatFs sf = new StatFs(root.getPath());
        long blockSize = 0;
        long blockCount = 0;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR2) {
            blockSize = sf.getBlockSizeLong();
            blockCount = sf.getBlockSizeLong();
        } else {
            blockSize = sf.getBlockSize();
            blockCount = sf.getBlockCount();
        }
        return ToolsFile.formatFileSize(blockCount * blockSize);
    }

    public static String getLogcat() {
        StringBuffer log = new StringBuffer();
        try {
            ArrayList commandLine = new ArrayList();
            commandLine.add("logcat");
            commandLine.add("-d");//使用该参数可以让logcat获取日志完毕后终止进程
            commandLine.add("-v");
            commandLine.add("time");
            commandLine.add("-f");//如果使用commandLine.add(">");是不会写入文件，必须使用-f的方式
            commandLine.add("/sdcard/log/logcat.txt");
            Process process = Runtime.getRuntime().exec((String[]) commandLine.toArray(new String[commandLine.size()]));
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()), 1024);
            String line = bufferedReader.readLine();
            while (line != null) {
                log.append(line);
                log.append("\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return log.toString();
    }

    /**
     * 获取可用
     *
     * @return
     */
    public static String getAvailHardDiskInfo() {
        File root = Environment.getRootDirectory();
        StatFs sf = new StatFs(root.getPath());
        long blockSize = 0;
        long availCount = 0;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR2) {
            blockSize = sf.getBlockSizeLong();
            availCount = sf.getAvailableBlocksLong();
        } else {
            blockSize = sf.getBlockSize();
            availCount = sf.getAvailableBlocks();
        }
        return ToolsFile.formatFileSize(availCount * blockSize);
    }

    /**
     * 得到当前分辨率 例如 320*480 return
     */
    public static String getPx() {
        // TODO Auto-generated method stub
        DisplayMetrics metrics = new DisplayMetrics();
        WindowManager wm = (WindowManager) LogService.globalContext
                .getSystemService(Context.WINDOW_SERVICE);
        wm.getDefaultDisplay().getMetrics(metrics);
        int heightPixel = metrics.heightPixels;
        int widthPixel = metrics.widthPixels;
        String px = "" + widthPixel + "*" + heightPixel + "";
        return px;
    }

    public static Location getLoc() {
        LocationManager locationManager = (LocationManager) LogService.globalContext
                .getSystemService(Context.LOCATION_SERVICE);
        Location location;
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);// 高精度
        criteria.setAltitudeRequired(false);// 不要求海拔
        criteria.setBearingRequired(false);// 不要求方位
        criteria.setCostAllowed(true);// 允许有花费
        criteria.setPowerRequirement(Criteria.POWER_LOW);// 低功耗
        // 从可用的位置提供器中，匹配以上标准的最佳提供器
        String provider = locationManager.getBestProvider(criteria, true);
        // 获得最后一次变化的位置
        location = locationManager.getLastKnownLocation(provider);
        return location;
    }

    /**
     * json统一时间格式
     *
     * @return
     */
    public static String getCurrentTime4Json() {
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        String current_time = sdf.format(date);
        return current_time;
    }

    /**
     * 获得当前日期和时间 格式 yyyy-MM-dd HH:mm
     */
    public static String getCurrentDateTimeNoSS() {
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        String current_time = sdf.format(date);
        return current_time;
    }

    /**
     * 获得当前日期和时间 格式 yyyy-MM-dd HH:mm:ss
     */
    public static String getCurrentDateTime() {
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String current_time = sdf.format(date);
        return current_time;
    }

    /**
     * 获得当前日期和时间 格式yyyy-MM-dd HH:mm:ss:SS
     */
    public static String getCurrentDateTimeWithSS() {
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SS");
        String current_time = sdf.format(date);
        return current_time;
    }

    /**
     * 获得当前时间HH:mm
     */
    public static String getCurrentTime() {
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
        String current_time = sdf.format(date);
        return current_time;
    }

    /**
     * 获得当前时间mm
     */
    public static String getCurrentTimeMM() {
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("mm");
        String current_time = sdf.format(date);
        return current_time;
    }

    /**
     * 返回当前时间，单位毫秒
     *
     * @return
     */
    public static long getCurrentTimeMillis() {
        return System.currentTimeMillis();
    }

    /**
     * 获得当前日期
     */
    public static String getCurrentDate() {
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String current_time = sdf.format(date);
        return current_time;
    }

    /**
     * 获得天数
     */
    public static int getDayNum(long millisTime) {
        int day = (int) (millisTime / (1000 * 60 * 60 * 24));
        if (millisTime % (1000 * 60 * 60 * 24) != 0) {
            return day + 1;
        }
        return day;
    }

    /**
     * 返回两次的时间差的显示方式
     *
     * @param startTime 开始时间
     * @param nowTime   结束时间
     * @return
     */
    public static String showDifTime(long startTime, long nowTime) {
        String re = "";
        long difftime = nowTime - startTime;
        if (difftime < 0) {
            re = "0秒前";
        } else if (difftime < 60 * 1000) {
            // 小于60s
            re = difftime / 1000 + "秒前";
        } else if (difftime < 60 * 60 * 1000) {
            // 小于60min
            re = (difftime / 1000) / 60 + "分钟前";
        } else {
            Date date_start = new Date(startTime);
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
            String nowDay = formatter.format(new Date(nowTime));
            String yesterDay = formatter.format(new Date(nowTime - 24 * 60 * 60
                    * 1000));
            String startDay = formatter.format(date_start);
            if (startDay.equals(nowDay)) {
                SimpleDateFormat myFormatter = new SimpleDateFormat("HH:mm");
                re = "今天  " + myFormatter.format(date_start);
            } else if (startDay.equals(yesterDay)) {
                SimpleDateFormat myFormatter = new SimpleDateFormat("HH:mm");
                re = "昨天  " + myFormatter.format(date_start);
            } else {
                SimpleDateFormat myFormatter = new SimpleDateFormat(
                        "yyyy-MM-dd HH:mm");
                re = myFormatter.format(date_start);
            }
        }
        return re;
    }

    /**
     * 判断两个时间差
     *
     * @param beforeTime  上一次的时间
     * @param nowTime     本次的时间
     * @param defaultDiff 需要的差距
     * @return
     */
    public static boolean dateDiff(String beforeTime, String nowTime,
                                   long defaultDiff) {
        try {
            SimpleDateFormat formatter = new SimpleDateFormat(
                    "yyyy-MM-dd HH:mm:ss");
            Date date_before = formatter.parse(beforeTime);
            Date date_after = formatter.parse(nowTime);
            long now_time = date_after.getTime();
            long before_time = date_before.getTime();
            long diff = now_time - before_time;
            if (diff - defaultDiff > 0) {
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 计算两个时间差
     *
     * @param beforeTime  上一次的时间
     * @param beforeTime  本次的时间
     * @param defaultDiff 需要的差距
     * @return
     */
    public static boolean timeDiff(String beforeTime, String nowTime,
                                   long defaultDiff) {
        try {
            SimpleDateFormat formatter = new SimpleDateFormat(
                    "yyyy-MM-dd HH:mm:ss");
            Date date_before = formatter.parse(beforeTime);
            Date date_after = formatter.parse(nowTime);
            long now_time = date_after.getTime();
            long before_time = date_before.getTime();
            long diff = now_time - before_time;
            if (diff - defaultDiff > 0) {
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 判断当天时间是否为晚上08:00~22:00
     */
    public static Boolean timePushEffect() {
        String currentTime = Tools.getCurrentTime();
        int index = currentTime.lastIndexOf(":");
        int Hour = Integer.valueOf(currentTime.substring(0, index));
        if (Hour < 22 && Hour >= 8) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 安装APK
     *
     * @param activity
     * @param path     apk文件路径
     */
    public static void installApk(Activity activity, String path) {
        File file = new File(path);
        if (file.exists()) {
            Intent intent = new Intent();
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setAction(android.content.Intent.ACTION_VIEW);
            intent.setDataAndType(Uri.fromFile(file),
                    "application/vnd.android.package-archive");
            activity.startActivity(intent);
        }
    }

    /**
     * 获取当前根路径
     *
     * @return
     */
    public static String getRootPath() {
        String rootPath = "";
        if (Environment.MEDIA_MOUNTED.equals(Environment
                .getExternalStorageState())
                && Environment.getExternalStorageDirectory().canWrite()) {
            rootPath = AppConstant.sdcardRootPath;
        } else {
            rootPath = AppConstant.dataRootPath;
        }
        return rootPath;
    }

    /**
     * dip转成pixel
     *
     * @param dip dip尺寸
     * @return
     */
    public static int dipToPixel(float dip) {
        return (int) (dip
                * LogService.globalContext.getResources().getDisplayMetrics().density + 0.5);
    }

    /**
     * 基站信息
     *
     * @return
     */
    public static String getNetWorkBaseStation() {
        try {
            TelephonyManager tm = (TelephonyManager) LogService.globalContext
                    .getSystemService(Context.TELEPHONY_SERVICE);
            // 获取网络类型
            int type = tm.getNetworkType();
            // 在中国，移动的2G是EGDE，联通的2G为GPRS，电信的2G为CDMA，电信的3G为EVDO
            // NETWORK_TYPE_EVDO_A 中国电信3G
            // NETWORK_TYPE_CDMA 中国电信2G
            if (type == TelephonyManager.NETWORK_TYPE_EVDO_A
                    || type == TelephonyManager.NETWORK_TYPE_CDMA
                    || type == TelephonyManager.NETWORK_TYPE_1xRTT) {
                CdmaCellLocation location = (CdmaCellLocation) tm
                        .getCellLocation();
                if (location != null) {
                    // [31188,575299,1675765,13824,2]
                    int baseStationId = location.getBaseStationId();
                    int a = location.getBaseStationLatitude();
                    int b = location.getBaseStationLongitude();
                    return baseStationId + "_" + a + "_" + b;
                }
            } else {// 其他网络全部使用GsmCellLocation
                GsmCellLocation location = (GsmCellLocation) tm
                        .getCellLocation();
                if (location != null) {
                    int cid = location.getCid();
                    int lac = location.getLac();
                    return cid + "_" + lac;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * 获得手机型号
     *
     * @return
     */
    public static String getPhoneModel() {
        try {
            String phoneVersion = android.os.Build.MODEL;
            if (null != phoneVersion) {
                return phoneVersion;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * 调用系统短信
     *
     * @param activity    Activity自身
     * @param phoneNumber
     * @param body        信息内容
     */
    public static void sendSms(Activity activity, String phoneNumber,
                               String body) {
        try {
            Uri smsToUri = Uri.parse("smsto:" + phoneNumber);// 联系人地址
            Intent intent = new Intent(android.content.Intent.ACTION_SENDTO,
                    smsToUri);
            intent.putExtra("address", phoneNumber);
            intent.putExtra("sms_body", body);// 短信的内容
            activity.startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 获得屏幕的宽度
     *
     * @return
     */
    public static int getScreenWidth() {
        DisplayMetrics dm = new DisplayMetrics();
        dm = LogService.globalContext.getApplicationContext().getResources()
                .getDisplayMetrics();
        return dm.widthPixels;
    }

    /**
     * 获得屏幕的高度
     *
     * @return
     */
    public static int getScreenHeight() {
        DisplayMetrics dm = new DisplayMetrics();
        dm = LogService.globalContext.getApplicationContext().getResources()
                .getDisplayMetrics();
        return dm.heightPixels;
    }

    /**
     * 获得CPU使用率
     */
    public static String getCpuInfo() {
        int cpu = 0;
        try {
            RandomAccessFile reader = new RandomAccessFile("/proc/stat", "r");
            String load = reader.readLine();
            String[] toks = load.split(" ");
            long idle1 = Long.parseLong(toks[5]);
            long cpu1 = Long.parseLong(toks[2]) + Long.parseLong(toks[3])
                    + Long.parseLong(toks[4]) + Long.parseLong(toks[6])
                    + Long.parseLong(toks[7]) + Long.parseLong(toks[8]);
            try {
                Thread.sleep(500);
            } catch (Exception e) {
                e.printStackTrace();
            }
            reader.seek(0);
            load = reader.readLine();
            reader.close();
            toks = load.split(" ");
            long idle2 = Long.parseLong(toks[5]);
            long cpu2 = Long.parseLong(toks[2]) + Long.parseLong(toks[3])
                    + Long.parseLong(toks[4]) + Long.parseLong(toks[6])
                    + Long.parseLong(toks[7]) + Long.parseLong(toks[8]);
            cpu = (int) (100 * (cpu2 - cpu1) / ((cpu2 + idle2) - (cpu1 + idle1)));
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return cpu + "%";
    }

    /**
     * 获得手机IMEI
     *
     * @return
     */
    public static String getIMEI() {
        try {
            TelephonyManager tm = (TelephonyManager) LogService.globalContext
                    .getSystemService(Context.TELEPHONY_SERVICE);
            String imei = tm.getDeviceId();
            if (null != imei) {
                return imei;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * 获得手机IMSI
     *
     * @return
     */
    public static String getIMSI() {
        try {
            TelephonyManager tm = (TelephonyManager) LogService.globalContext
                    .getSystemService(Context.TELEPHONY_SERVICE);
            String imsi = tm.getNetworkOperator();
            if (null != imsi) {
                return imsi;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * 获取sdcard或data的剩余空间
     */
    public static long getSdcardFreeSize(String rootPath) {
        // 取得sdcard文件路径
        StatFs statFs = new StatFs(rootPath);
        // 获取block的SIZE
        long blocSize = statFs.getBlockSize();
        // 可使用的Block的数量
        long availaBlock = statFs.getAvailableBlocks();
        // 剩余空间大小
        long freeSize = availaBlock * blocSize;
        return freeSize;
    }

    /**
     * 获得系统版本号
     *
     * @return
     */
    public static String getSDK() {
        try {
            String release = android.os.Build.VERSION.RELEASE;
            if (null != release) {
                return release;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * 判断当前是否符合桌面显示的对话框
     *
     * @param context
     * @return
     */
    public static boolean pushDeskFlag(Context context) {
        boolean deskFlag = false;
        String taskNameTop = "";
        ActivityManager activityManager = (ActivityManager) context
                .getSystemService(Context.ACTIVITY_SERVICE);
        List<RunningTaskInfo> tasksInfo = activityManager.getRunningTasks(100);
        if (tasksInfo.size() > 0) {
            taskNameTop = tasksInfo.get(0).topActivity.getPackageName();
        } else {
            return true;
        }
        for (int i = 0; i < tasksInfo.size(); i++) {
            if (context.getPackageName().equals(
                    tasksInfo.get(i).topActivity.getPackageName())) {
                return false;
            }
        }
        List<String> names = getAllTheLauncher(context);
        for (int i = 0; i < names.size(); i++) {
            if (names.get(i).equals(taskNameTop)) {
                deskFlag = true;
            }
        }
        return deskFlag;
    }

    /**
     * 获取所有的launcher信息
     *
     * @param context
     * @return
     */
    private static List<String> getAllTheLauncher(Context context) {
        List<String> names = null;
        PackageManager pkgMgt = context.getPackageManager();
        Intent it = new Intent(Intent.ACTION_MAIN);
        it.addCategory(Intent.CATEGORY_HOME);
        List<ResolveInfo> ra = pkgMgt.queryIntentActivities(it, 0);
        if (ra.size() != 0) {
            names = new ArrayList<String>();
        }
        for (int i = 0; i < ra.size(); i++) {
            String packageName = ra.get(i).activityInfo.packageName;
            names.add(packageName);
        }
        return names;
    }

    /**
     * 判断手机是否有发送短信权限
     *
     * @param context
     * @return
     */
    public static boolean isUseSendSMSPermission(Context context) {
        try {
            PackageInfo pInfo = context.getPackageManager().getPackageInfo(
                    context.getPackageName(), PackageManager.GET_PERMISSIONS);
            String[] permissions = pInfo.requestedPermissions;
            for (String s : permissions) {
                if (s.trim().equals(android.Manifest.permission.SEND_SMS))
                    return true;
            }
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 判断字符串是否为数字
     *
     * @param str
     * @return
     */
    public static boolean isNumeric(String str) {
        if (str.matches("\\d*")) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * MD5加密
     */
    public static String getMD5Str(String str) {
        MessageDigest messageDigest = null;
        try {
            messageDigest = MessageDigest.getInstance("MD5");
            messageDigest.reset();
            messageDigest.update(str.getBytes("UTF-8"));
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        byte[] byteArray = messageDigest.digest();
        StringBuffer md5StrBuff = new StringBuffer();
        for (int i = 0; i < byteArray.length; i++) {
            if (Integer.toHexString(0xFF & byteArray[i]).length() == 1) {
                md5StrBuff.append("0").append(
                        Integer.toHexString(0xFF & byteArray[i]));
            } else {
                md5StrBuff.append(Integer.toHexString(0xFF & byteArray[i]));
            }
        }
        return md5StrBuff.substring(0, md5StrBuff.length()).toString();
    }

    /**
     * 获取随机数
     *
     * @return
     */
    public static String getRandomNumber() {
        return new DecimalFormat("0000000000").format(new Random()
                .nextInt(1000000000));
    }

    /**
     * 上次点击的时间
     */
    private static long lastClickTime;

    /**
     * 按钮是不是连续被按下
     *
     * @return
     */
    public static boolean isFastDoubleClick(int timeDifference) {
        long time = System.currentTimeMillis();
        long timeD = time - lastClickTime;
        if (0 < timeD && timeD < timeDifference) {
            return true;
        }
        lastClickTime = time;
        return false;
    }

    /**
     * 截取并按规则组合字符串
     *
     * @return
     */
    public static String subAndCombinationString(String str, int subLength,
                                                 boolean isReduction) {
        if (isReduction) {
            String str1 = str.substring(0, subLength);
            String str2 = str.replace(str1, "");
            String result = str2 + str1;
            return result;
        } else {
            String temp = str.substring(0, str.length() - subLength);
            String str1 = temp.substring(0, subLength);
            String str2 = temp.replace(str1, "");
            String str3 = str.replace(temp, "");
            String result = str3 + str1 + str2;
            return result;
        }
    }

    /**
     * 得到唯一id识别开始结束时间
     *
     * @return
     */
    public static String getUNIQUEID() {
        return GenerateSequenceUtil.generateSequenceNo();
    }

    public static String getUserId() {
        //TODO

        return "UID20140909111";
    }
}
