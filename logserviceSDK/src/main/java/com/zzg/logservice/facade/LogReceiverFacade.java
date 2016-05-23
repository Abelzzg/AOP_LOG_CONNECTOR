package com.zzg.logservice.facade;

/**
 * @author Abelzzg
 * @version V1.0
 * @Description: ${todo}
 * @date ${date} ${time}
 */
public interface LogReceiverFacade {

    void setDebugMode(boolean isDebugMode);

    void generateCountEventLog(String eventName, String viewName);

    String generateStartPage(String eventName, String viewName);

    void generateEndPage(String token);


    void generateBehaviorEventLog(String eventName);

    void generateErrorEventLog(String viewName, String errorCode, Exception ex);

    void generateDuringEventLog(String className, String method, String during);
}
