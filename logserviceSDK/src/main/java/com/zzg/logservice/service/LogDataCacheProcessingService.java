package com.zzg.logservice.service;

import com.google.gson.JsonObject;
import com.zzg.logservice.api.ILogDataProcessingService;
import com.zzg.logservice.api.ILogMessageDirectorService;
import com.zzg.logservice.config.AppConstant;
import com.zzg.logservice.utils.Tools;

/**
 * @author Abelzzg
 * @version V1.0
 * @Description: ${todo}
 * @date ${date} ${time}
 */
public class LogDataCacheProcessingService implements ILogDataProcessingService {

    ILogMessageDirectorService iLogMessageDirectorService = new LogMessageDirectorService();

    /**
     * @param eventName
     * @param viewName
     * @param count     忽略此参数
     */
    @Override
    public void processCountEventLogData(String eventName, String viewName, int count) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("type", "num");
        jsonObject.addProperty("name", eventName);
        jsonObject.addProperty("view", viewName);
        jsonObject.addProperty("count", 1);
        iLogMessageDirectorService.buildMessageFromLogDataCache(AppConstant.COUNT_EVENT, jsonObject);
    }

    /**
     * @param eventName
     * @param viewName
     */
    @Override
    public String processDuringStartEventLogData(String eventName, String viewName) {
        String token = Tools.getUNIQUEID();
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("type", "time");
        jsonObject.addProperty("name", eventName);
        jsonObject.addProperty("view", viewName);
        jsonObject.addProperty("token", token);
        jsonObject.addProperty("start", Tools.getCurrentTimeMillis());
        iLogMessageDirectorService.buildMessageFromLogDataCache(AppConstant.COUNT_EVENT, jsonObject);
        return token;
    }

    /**
     * @param token 忽略此参数
     */
    @Override
    public void processDuringEndEventLogData(String token) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("type", "time");
        jsonObject.addProperty("token", token);
        jsonObject.addProperty("end", Tools.getCurrentTimeMillis());
        iLogMessageDirectorService.buildMessageFromLogDataCache(AppConstant.COUNT_EVENT, jsonObject);
    }


    @Override
    public void processBehaviorEventLogData(String viewName) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("view", viewName);
        jsonObject.addProperty("time", Tools.getCurrentTime4Json());
        iLogMessageDirectorService.buildMessageFromLogDataCache(AppConstant.BEHAVIOR_EVENT, jsonObject);
    }

    @Override
    public void processErrorEventLogData(String viewName, String errorCode, Exception ex) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("time", Tools.getCurrentTime4Json());
        jsonObject.addProperty("reason", ex.getClass().getSimpleName());
        jsonObject.addProperty("view", viewName);
        jsonObject.addProperty("error_code", errorCode);
        //对ex对象进行处理
        //只要前三行
        String sOut = "";
        StackTraceElement[] trace = ex.getStackTrace();
        for (int i = 0; i < 3; i++) {
            sOut += "|" + trace[i];
        }
        jsonObject.addProperty("stack_trace", sOut);
        iLogMessageDirectorService.buildMessageFromLogDataCache(AppConstant.ERROR_EVENT, jsonObject);
    }

    @Override
    public void processDuringEventLogData(String className, String method, String during) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("type", "time");
        jsonObject.addProperty("name", method);
        jsonObject.addProperty("view", className);
        jsonObject.addProperty("during", during);

        iLogMessageDirectorService.buildMessageFromLogDataCache(AppConstant.DURING_EVENT, jsonObject);

    }
}
