package com.zzg.logservice.broadcastreceiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import com.zzg.logservice.service.LogService;
import com.zzg.logservice.utils.Tools;

/**
 * @author Abelzzg
 * @version V1.0
 * @Description: ${todo}
 * @date ${date} ${time}
 */
public class ConnectionChangeReceiver extends BroadcastReceiver {

    public static final String CONNECTIVITY_CHANGE_ACTION = "android.net.conn.CONNECTIVITY_CHANGE";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if(TextUtils.equals(action, CONNECTIVITY_CHANGE_ACTION)&& Tools.isFastMobileNetwork()){//网络变化的时候会发送通知
            LogService.sendLog();
        }
    }
}
