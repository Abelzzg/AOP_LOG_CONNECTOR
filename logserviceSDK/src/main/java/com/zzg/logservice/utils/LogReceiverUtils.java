package com.zzg.logservice.utils;


import android.content.Context;

import com.zzg.logservice.facade.LogReceiverFacade;
import com.zzg.logservice.impl.LogReceiverFacadeImpl;
import com.zzg.logservice.network.CustomHttpUtils;
import com.zzg.logservice.service.LogService;

/**
 * @author Abelzzg
 * @version V1.0
 * @Description: 对外暴露的主工具类
 * @date ${date} ${time}
 */
public class LogReceiverUtils {

    static LogReceiverFacade logReceiveFacade = new LogReceiverFacadeImpl();


    static boolean success = true;

    /**
     * 产生累计事件
     *
     * @param eventName 事件名称
     * @param viewName  view
     */
    public static void generateCountEventlog(String eventName, String viewName) {
        if (success) {
            logReceiveFacade.generateCountEventLog(eventName, viewName);
        }
    }

    /**
     * 产生累计时间事件
     *
     * @param eventName
     * @param viewName
     * @return 返回唯一识别的token
     */
    public static String generateStartPage(String eventName, String viewName) {
        String token = logReceiveFacade.generateStartPage(eventName, viewName);
        return token;
    }

    public static void generateEndPage(String token) {
        logReceiveFacade.generateEndPage(token);
    }

    /**
     * 产生错误日志
     */
    public static void generateErrorEventLog(String viewName, String errorCode, Exception ex) {
        if (success) {
            logReceiveFacade.generateErrorEventLog(viewName, errorCode, ex);
        }
    }

    /**
     * 产生耗时事件
     *
     * @param className
     * @param method
     * @param during
     */
    public static void generateDuringEventlog(String className,String method,String during) {
        if (success) {
            logReceiveFacade.generateDuringEventLog(className, method, during);
        }
    }

    /**
     * 产生行为事件
     *
     * @param eventName
     */
    public static void generateBehaviorEventlog(String eventName) {
        if (success) {
            logReceiveFacade.generateBehaviorEventLog(eventName);
        }
    }

    /**
     * 设置debug模式来收集崩溃日志，默认是true收集
     * @param isDebugMode
     */
    public static void setDebugMode(boolean isDebugMode) {
    	LogService.setDebug(isDebugMode);
        if (success) {
            logReceiveFacade.setDebugMode(isDebugMode);
        }
    }

    /**
     * 注册日志服务
     * 绑定log服务
     * @param appkey
     */
    public static void regeistLogReceiver(String appkey, Context appContext) {
        success = CustomHttpUtils.registLog(appkey);
        if (success) {
            LogService.init(appContext);
            LogService.registerLogService(appkey);
        }
    }

}
