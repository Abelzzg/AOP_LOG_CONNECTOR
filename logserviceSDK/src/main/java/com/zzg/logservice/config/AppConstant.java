package com.zzg.logservice.config;

import com.zzg.logservice.utils.Tools;

/**
 * 全局常量
 *
 * @author Abelzzg
 */
public class AppConstant {


    public static final String version="1.0";//sdk的版本号
    /**
     * 日志三种时间
     */
    /**
     * 行为时间
     */
    public static final int BEHAVIOR_EVENT = 101;

    /**
     * 统计事件
     */
    public static final int COUNT_EVENT = 102;

    /**
     * 错误事件
     */
    public static final int ERROR_EVENT = 103;

    /**
     * 耗时事件
     */
    public static final int DURING_EVENT = 104;

    /**
     * 服务端地址
     */
    public static final String SERVICE_URL = "http://200.1.1.26:8090/log-parser-http/log/process";

    public static final String logPath = android.os.Environment
            .getDataDirectory().getAbsolutePath() + "/data/" + Tools.getPackageName() + "/yeepay/";

    /**
     * sdcard根目录
     */
    public static final String sdcardRootPath = android.os.Environment
            .getExternalStorageDirectory().getPath();

    /**
     * 机身根目录:DataDirectory/data/包名/files/
     */
    public static final String dataRootPath = android.os.Environment
            .getDataDirectory() + "/data/" + Tools.getPackageName() + "/files/";

    /**
     * log日志统一文件夹
     */
    public static final String logDir = android.os.Environment
            .getDataDirectory().getAbsolutePath() + "/data/" + Tools.getPackageName() + "/yeepay";

    public static final String CRASH_CODE = "Ex999999";
}
