package com.zzg.logservice.service;

import com.google.gson.JsonObject;
import com.zzg.logservice.api.ILogMessageBuilderService;
import com.zzg.logservice.api.ILogMessageDirectorService;
import com.zzg.logservice.config.AppConstant;

/**
 * @author Abelzzg
 * @version V1.0
 * @Description: ${todo}
 * @date ${date} ${time}
 */
public class LogMessageDirectorService implements ILogMessageDirectorService {

    ILogMessageBuilderService iLogMessageCacheBuilderService = new LogMessageCacheBuilderService();


    @Override
    public void buildMessageFromLogDataCache(int title, JsonObject jsonObject) {
        switch (title) {
            case AppConstant.COUNT_EVENT:
                iLogMessageCacheBuilderService.buildCountEventLogMessage(jsonObject);
                break;
            case AppConstant.ERROR_EVENT:
                iLogMessageCacheBuilderService.buildErrorEventLogMessage(jsonObject);
                break;
            case AppConstant.BEHAVIOR_EVENT:
                iLogMessageCacheBuilderService.buildBehaviorEventLogMessage(jsonObject);
                break;
            case AppConstant.DURING_EVENT:
                iLogMessageCacheBuilderService.buildDuringEventLogMessage(jsonObject);
                break;
        }
    }

   

}
