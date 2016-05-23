package com.zzg.logservice.api;

import com.google.gson.JsonObject;

/**
 * @author Abelzzg
 * @version V1.0
 * @Description: ${todo}
 * @date ${date} ${time}
 */
public interface ILogMessageDirectorService {

    void buildMessageFromLogDataCache(int title, JsonObject jsonObject);
}
