package com.zzg.logservice.service;

import android.app.ActivityManager;
import android.content.Context;
import android.content.IntentFilter;
import android.text.TextUtils;
import android.util.Log;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.zzg.logservice.config.AppConstant;
import com.zzg.logservice.exception.CustomException;
import com.zzg.logservice.network.CustomHttpUtils;
import com.zzg.logservice.utils.Tools;
import com.zzg.logservice.utils.ToolsFile;

import java.io.IOException;
import java.util.List;

/**
 * Author Abelzzg
 */
public class LogService {

    private static boolean debug = true;// 是否记录崩溃日志
    public static final String CONNECTIVITY_CHANGE_ACTION = "android.net.conn.CONNECTIVITY_CHANGE";
    /**
     * 全局的一个JsonObject 日志内容
     */
    private static JsonObject jsonGlobal = new JsonObject();
    private static JsonObject jsonRealtimeGlobal = new JsonObject();
    private static String APPKEY = "";
    /**
     * 全局上下文
     */
    public static Context globalContext;

    public static String filePath;
    private static String serviceUrl;

    private static boolean start = true;
    private static String logDir;
    public static String logName;
    private static CustomException customException;

    /**
     * 统计服务初始化
     */
    public static void init(Context context) {
        globalContext = context;
        registerDateTransReceiver();
        // 产生新的文件，与之前的文件不冲突
        logName = "log.json" + Tools.getCurrentTime4Json();
        filePath = AppConstant.logPath + logName;
        serviceUrl = AppConstant.SERVICE_URL;
        logDir = AppConstant.logDir;
        sendLog();
        // 起线程监控是否挂起
        checkForeOrBack();
        // 如果开启debug模式就调用抓崩溃日志
        if (debug) {
            customException = CustomException.getInstance();
            customException.init(LogService.globalContext);
        }
        // 产生一个新文件和全局json对象
        initJsonGloblal();
        saveLogHeader();
        // sendLog();
    }

    private static void initJsonGloblal() {
        // 产生一个新文件和全局json对象
        String json = ToolsFile.readFile(filePath);
        JsonParser jsonParser = new JsonParser();
        jsonGlobal = (JsonObject) jsonParser.parse(json);
    }

    /**
     * 注册广播接收者
     */
    private static void registerDateTransReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(CONNECTIVITY_CHANGE_ACTION);
        filter.setPriority(1000);
//		globalContext.registerReceiver(new ConnectionChangeReceiver(), filter);
    }

    /**
     * 统计记录
     *
     * @param jsonObject
     */
    public static String saveCountEvent(JsonObject jsonObject) {
        String key = "count_event";
        initJsonGloblal();
        // 找到统计时间json对象
        JsonArray count_event = new JsonArray();// Count_event下得数组
        if (!jsonObject.isJsonNull()) {
            // 如果jsonGlobal有count_event
            if (jsonGlobal.get(key) != null) {
                count_event = jsonGlobal.getAsJsonArray(key);
                // 如果是时间统计事件
                if (jsonObject.get("type").getAsString().equals("time")) {
                    count_event.add(jsonObject);
                } else {
                    int countnum = 0;//记录type="num"的数量

                    for (int i = 0; i < count_event.size(); i++) {


                        JsonObject countJson = count_event.get(i)
                                .getAsJsonObject();
                        // name字段是否匹配getAsStri
                        // 如果是数值型统计事件
                        if (countJson.get("type").getAsString().equals("num")
                                && jsonObject.get("type").getAsString()
                                .equals("num")) {
                            countnum += 1;
                            // 如果有一样名称的统计事件
                            if (countJson.get("name") != null
                                    && countJson.get("name").equals(
                                    jsonObject.get("name"))) {
                                int count = countJson.get("count").getAsInt();
                                countJson.remove("count");
                                countJson.addProperty("count", count += 1);
                            } else {// 如果没有，则加入新的统计事件
                                jsonObject.addProperty("count", 1);
                                count_event.add(jsonObject);
                            }
                        }
                    }
                    if (countnum == 0) {
                        jsonObject.addProperty("count", 1);
                        count_event.add(jsonObject);
                    }
                }
                writeFile(jsonGlobal);
            }
            // 如果没有count_event事件
            else {
                // count_event中加入jsonObject
                count_event.add(jsonObject);
                jsonGlobal.add(key, count_event);
                writeFile(jsonGlobal);
            }
        }
        return jsonObject.toString();
    }

    /**
     * 记录行为内容
     *
     * @param jsonObject 行为名称
     */
    public static String saveBehaviorLog(JsonObject jsonObject) {
        // 如果key存在，就在这个key对应的array中加入一个jsonObject
        String key = "behavior_trace";
        initJsonGloblal();
        JsonArray behavior = new JsonArray();// behavior_trace下得数组
        if (!jsonObject.isJsonNull()) {
            // 如果jsonGlobal有behavior_trace
            if (jsonGlobal.get(key) != null) {
                // 得到一个array
                behavior = jsonGlobal.getAsJsonArray(key);
                // 再得到一个array
                for (int i = 0; i < behavior.size(); i++) {
                    if (behavior.get(i).getAsJsonObject().get("trace") != null) {
                        JsonArray trace = behavior.get(i).getAsJsonObject()
                                .get("trace").getAsJsonArray();
                        // 如果behavior_trace下的数组中有trace为开头的jsonObject
                        if (trace != null) {
                            trace.add(jsonObject);
                        }
                    }
                    // 如果behavior_trace下的数组中没有trace为开头的jsonObject
                    else {
                        JsonArray jsonArray = new JsonArray();
                        jsonArray.add(jsonObject);
                        JsonObject js = new JsonObject();
                        js.add("trace", jsonObject);
                        behavior.add(js);
                    }
                }
            }
            // 如果没有behavior_trace
            else {
                // 生成一个trace对应数组的jsonObject
                JsonArray jsonArray = new JsonArray();
                jsonArray.add(jsonObject);
                JsonObject js = new JsonObject();
                js.add("trace", jsonArray);
                behavior.add(js);
                jsonGlobal.add(key, behavior);
            }
        }
        writeFile(jsonGlobal);
        return jsonObject.toString();
    }

    /**
     * 记录错误日志
     */
    public static String saveErrorLog(JsonObject jsonObject) {
        String result = collectLog("error_event", jsonObject);
        return result;
    }

    // 记录时间的临时array
    static JsonArray jsonArray = new JsonArray();

    /**
     * 处理during开始
     *
     * @param startJsonObject
     */
    public static void saveStartLog(JsonObject startJsonObject) {
        jsonArray.add(startJsonObject);
    }

    /**
     * 处理during开始
     *
     * @param endJsonObject
     */
    public static void saveEndLog(JsonObject endJsonObject) {
        for (int i = 0; i < jsonArray.size(); i++) {
            JsonObject start = (JsonObject) jsonArray.get(i);
            // 判断token相同，事件名称相同

            if (start.get("token").equals(endJsonObject.get("token"))) {
                // 计算时间差
                long during = endJsonObject.get("end").getAsLong()
                        - start.get("start").getAsLong();
                during = during / 1000;
                start.remove("start");
                start.remove("token");
                start.addProperty("during", during);
                // 往jsonGlobal里插入during jsonObject
                saveCountEvent(start);
                jsonArray.remove(i);
            }
        }
    }


    public static boolean registerLogService(String appkey) {
        if (!TextUtils.isEmpty(appkey)) {
            APPKEY = appkey;
        } else {
            APPKEY = Tools.getAppkey();
        }
        String json = ToolsFile.readFile(filePath);
        JsonParser jsonParser = new JsonParser();
        jsonGlobal = (JsonObject) jsonParser.parse(json);
        if (TextUtils.isEmpty(appkey)) {
            appkey = Tools.getAppkey();
        } else {
            jsonGlobal.addProperty("app_key", appkey);
            jsonRealtimeGlobal.addProperty("app_key", appkey);
        }
        writeFile(jsonGlobal);
        return false;
    }

    public static void setDebug(boolean isDebug) {
        debug = isDebug;
    }

    public static String collectLog(String key, JsonObject jsonObject) {
        // 如果key存在，就在这个key对应的array中加入一个jsonObject
        JsonArray jsonArray = new JsonArray();
        if (!jsonObject.isJsonNull()) {
            if (jsonGlobal.get(key) != null) {
                jsonArray = jsonGlobal.getAsJsonArray(key);
                jsonArray.add(jsonObject);
                jsonGlobal.add(key, jsonArray);
            } else {
                jsonArray.add(jsonObject);
                jsonGlobal.add(key, jsonArray);
            }
        }
        writeFile(jsonGlobal);
        return jsonGlobal.toString();
    }

    /**
     * 获取日志内容
     *
     * @return
     */
    public static String getLog() {
        String file = ToolsFile.readFile(filePath);
        return file;
    }

    public static void writeFile(JsonObject jsonObject) {
        // 往文件里写
        try {
            ToolsFile.writeFile(filePath, jsonObject.toString());
        } catch (IOException e) {
            // TODO Auto-generated catch block
            System.out.print("写入文件异常........" + filePath);
            e.printStackTrace();
        }
    }

    /**
     * 发送日志，发送之前的日志
     */
    public synchronized static void sendLog() {
        // 如果不在高速网络状态下，直接返回不发送
        if (!Tools.isFastMobileNetwork()) {
            Log.i("com.yeepay.logservice", "网络异常，请检查网络状态");
            return;
        } else {
            // 搜索文件夹下所有文件带log的文件
            JsonArray jsonArray = ToolsFile.readAllLog(logDir);
            // 监测jsonArray
            jsonArray = checkLog(jsonArray);

            if (jsonArray == null || jsonArray.isJsonNull()
                    || jsonArray.size() == 0) {
                return;
            } else {
                // 发送到服务器
                CustomHttpUtils customHttpUtils = CustomHttpUtils.getInstance();
                customHttpUtils.uploadString(jsonArray.toString(), serviceUrl);
            }
        }
    }

    private static JsonArray checkLog(JsonArray jsonArray) {
        // 监测behavior
        if (jsonArray != null) {
            for (int i = 0; i < jsonArray.size(); i++) {
                JsonObject json = jsonArray.get(i).getAsJsonObject();
                if (json.get("behavior_trace") != null) {
                    JsonArray behavior_trace = json.get("behavior_trace")
                            .getAsJsonArray();
                    for (int j = 0; j < behavior_trace.size(); j++) {
                        JsonObject behaviorJ = (JsonObject) behavior_trace
                                .get(j);
                        if (behaviorJ.get("trace") != null) {
                            JsonArray trace = behaviorJ.get("trace")
                                    .getAsJsonArray();
                            if (trace.size() == 1) {
                                json.remove("behavior_trace");
                            }
                        }
                    }
                }
            }
        }
        return jsonArray;
    }

    /**
     * 标记程序是否被挂起
     */
    private static boolean foreground = false;
    private static boolean background = false;

    /**
     * 监听程序是否后台挂起
     */
    private static void checkForeOrBack() {

        new Thread() {
            public void run() {
                int time = 0;
                while (true) {
                    ActivityManager myAM = (ActivityManager) LogService.globalContext
                            .getSystemService(Context.ACTIVITY_SERVICE);
                    String packageName = LogService.globalContext
                            .getApplicationContext().getPackageName();
                    List<ActivityManager.RunningAppProcessInfo> appProcesses = myAM
                            .getRunningAppProcesses();
                    for (ActivityManager.RunningAppProcessInfo appProcess : appProcesses) {
                        if (appProcess.processName.equals(packageName)) {
                            if (appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                                foreground = true;
                                if (foreground && background) {
                                    background = false;
                                    if (time == 1) {
                                        LogService.sendLog();
                                        time -= 1;
                                    }
                                }
                            } else {
                                background = true;
                                if (foreground && background) {
                                    if (start) {
                                        start = false;
                                    } else {
                                        time += 1;
                                        // 挂起操作
                                        JsonObject jsonObject = new JsonObject();
                                        jsonObject.addProperty("view", "挂起状态");
                                        jsonObject.addProperty("time",
                                                Tools.getCurrentTimeMillis());
                                        // LogService.saveBehaviorLog(jsonObject);
                                        // 发送日志文件
                                    }
                                    foreground = false;
                                }
                            }
                        }
                    }
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }.start();
    }

    /**
     * 设备及安装软件信息 往jsonGlobal中加入Header信息
     *
     * @return 头信息
     */
    public static String saveLogHeader() {
        String json = ToolsFile.readFile(filePath);
        JsonParser jsonParser = new JsonParser();
        jsonGlobal = (JsonObject) jsonParser.parse(json);
        jsonGlobal.addProperty("upload_time", Tools.getCurrentTime4Json());// 上传时间
        jsonGlobal.addProperty("app_key", APPKEY);// app标示码
        jsonGlobal.addProperty("app_version", Tools.getVersionName());// 版本号
        jsonGlobal.addProperty("device_id", Tools.getIMEI());// 设备ID
        jsonGlobal.addProperty("device_model", Tools.getPhoneModel());

        jsonGlobal.addProperty("device_android_version", Tools.getDeviceVersion());// 设备ID
        jsonGlobal.addProperty("target_android_version", Tools.getTargetAndroidVersion());//目标apk的编译版本

        if (!TextUtils.isEmpty(Tools.getIMSI()))
            jsonGlobal.addProperty("mcc_mnc", Tools.getIMSI());// 国家码_网络码

        jsonGlobal.addProperty("screen_resolution", Tools.getResolution());
//        jsonGlobal.addProperty("dpi", Tools.getDpi());
//        jsonGlobal.addProperty("wifi_mac", !TextUtils.isEmpty(Tools.getMacAddress()) ? Tools.getMacAddress() : null);
        if (TextUtils.isEmpty(Tools.getNetworkType())) {
            jsonGlobal.addProperty("network", Tools.getNetworkType());// 网络类型
        }


        jsonGlobal.addProperty("use_memory", Tools.getMemory());// 使用的内存
        jsonGlobal.addProperty("totle_memory", Tools.getTotalMemory());// 总内存
        jsonGlobal.addProperty("cpu_used", Tools.getCpuInfo());// CPU使用率

//        if (Tools.getCellId() != 0) {
//            jsonGlobal.addProperty("cell_id", Tools.getCellId());// 手机信号覆盖区域的的编号ID
//        }
//        jsonGlobal.addProperty("language", Tools.getLan());// 语言
        if (Tools.getLac() != 0) {
            jsonGlobal.addProperty("lac", Tools.getLac());// 位置区域码
        }

        if (TextUtils.isEmpty(Tools.getUserId())) {
            jsonGlobal.addProperty("user_id", Tools.getUserId());// CPU使用率
        }

        if (Tools.getLocalIPV6IpAddress() != null) {
            try {
                String Ipv6 = Tools.getLocalIPV6IpAddress();
                Log.i("com.yeepay.logservice", Ipv6);
                String ipv6 = Ipv6;
                jsonGlobal.addProperty("ipv6", ipv6);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        jsonGlobal.addProperty("sdk_version", "1.0");// 版本号


        writeFile(jsonGlobal);
        return jsonGlobal.toString();
    }

    /**
     * 拼装实时发送的头文件
     *
     * @return
     */
    public static String saveRealtimeLogHeader() {
        jsonRealtimeGlobal.addProperty("app_key", APPKEY);// app标示码
        jsonRealtimeGlobal.addProperty("app_version", Tools.getVersionName());// 版本号
        jsonRealtimeGlobal.addProperty("device_id", Tools.getIMEI());// 设备ID
        jsonRealtimeGlobal.addProperty("upload_time",
                Tools.getCurrentTime4Json());// 设备ID
        // jsonRealtimeGlobal.addProperty("os_version", Tools.getVersionCode());
        return null;
    }

    public static void saveDuringEventLog(JsonObject jsonObject) {
        saveCountEvent(jsonObject);
    }
}