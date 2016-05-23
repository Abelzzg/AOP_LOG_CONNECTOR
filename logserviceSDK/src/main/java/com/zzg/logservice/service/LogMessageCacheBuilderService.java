package com.zzg.logservice.service;

import com.google.gson.JsonObject;
import com.zzg.logservice.api.ILogMessageBuilderService;

/**
 * @author Abelzzg
 * @version V1.0
 * @Description: ${todo}
 * @date ${date} ${time}
 */
public class LogMessageCacheBuilderService implements ILogMessageBuilderService {

    @Override
    public void buildCountEventLogMessage(JsonObject jsonObject) {
        if (jsonObject.get("type").getAsString().equals("time")) {
            if (jsonObject.get("start")!=null) {
                LogService.saveStartLog(jsonObject);
            } else if (jsonObject.get("end")!=null) {
                LogService.saveEndLog(jsonObject);
            }
        } else {
            LogService.saveCountEvent(jsonObject);
        }
    }

    @Override
    public void buildBehaviorEventLogMessage(JsonObject jsonObject) {
        LogService.saveBehaviorLog(jsonObject);
    }

    @Override
    public void buildErrorEventLogMessage(JsonObject jsonObject) {
        LogService.saveErrorLog(jsonObject);
    }

    @Override
    public void buildDeviceInfoLogMessage() {

    }

    @Override
    public void buildDuringEventLogMessage(JsonObject jsonObject) {
        LogService.saveDuringEventLog(jsonObject);
    }
}
