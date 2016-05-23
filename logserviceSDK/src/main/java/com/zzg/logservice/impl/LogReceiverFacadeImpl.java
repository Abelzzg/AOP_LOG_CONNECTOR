package com.zzg.logservice.impl;

import com.zzg.logservice.facade.LogReceiverFacade;
import com.zzg.logservice.service.LogDataCacheProcessingService;

/**
 * @author Abelzzg
 * @version V1.0
 * @Description: log接收功能实现类
 * @date ${date} ${time}
 */
public class LogReceiverFacadeImpl implements LogReceiverFacade {


    LogDataCacheProcessingService logDataCacheProcessingService = new LogDataCacheProcessingService();

    boolean isDebugMode = true;//debug模式(是否记录错误日志)

    @Override
    public void setDebugMode(boolean isDebugMode) {
        this.isDebugMode = isDebugMode;
    }

    @Override
    public void generateCountEventLog(String eventName, String viewName) {
        logDataCacheProcessingService.processCountEventLogData(eventName, viewName, 0);
    }



    @Override
    public String generateStartPage(String eventName, String viewName) {
        String token = logDataCacheProcessingService.processDuringStartEventLogData(eventName, viewName);
        return token;
    }

    @Override
    public void generateEndPage(String token) {
        logDataCacheProcessingService.processDuringEndEventLogData(token);
    }

    @Override
    public void generateDuringEventLog(String className, String method,String during) {
        logDataCacheProcessingService.processDuringEventLogData(className,method,during);
    }

    @Override
    public void generateBehaviorEventLog(String viewName) {
        logDataCacheProcessingService.processBehaviorEventLogData(viewName);
    }

    @Override
    public void generateErrorEventLog(String viewName, String errorCode, Exception ex) {
        if (isDebugMode) {
            logDataCacheProcessingService.processErrorEventLogData(viewName, errorCode, ex);
        }
    }

}
