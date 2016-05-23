package com.zzg.logservice.api;

import com.google.gson.JsonObject;

/**
 * @author Abelzzg
 * @version V1.0
 * @Description: ${todo}
 * @date ${date} ${time}
 */
public interface ILogMessageBuilderService {
    void buildCountEventLogMessage(JsonObject jsonObject);

    void buildBehaviorEventLogMessage(JsonObject jsonObject);

    void buildErrorEventLogMessage(JsonObject jsonObject);

    void buildDeviceInfoLogMessage();

    void buildDuringEventLogMessage(JsonObject jsonObject);
}
