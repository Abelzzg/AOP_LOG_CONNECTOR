package com.zzg.logservice.api;

/**
 * @author Abelzzg
 * @version V1.0
 * @Description: ${todo}
 * @date ${date} ${time}
 */
public interface ILogDataProcessingService {

    void processCountEventLogData(String eventName, String viewName, int count);

    String processDuringStartEventLogData(String eventName, String viewName);

    void processDuringEndEventLogData(String token);

    void processBehaviorEventLogData(String viewName);

    void processErrorEventLogData(String viewName, String errorCode, Exception ex);

    void processDuringEventLogData(String className, String method, String during);
}
