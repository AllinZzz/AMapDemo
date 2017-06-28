package com.zyl.amapdemo.utils;

import android.content.Intent;
import android.net.Uri;


/**
 * Created by zyl on 2017/6/27.
 */

public class IntentUtils {
    /**
     * 获取App具体设置的意图
     *
     * @param packageName 包名
     * @return intent
     */
    public static Intent getAppDetailsSettingsIntent(String packageName) {
        Intent intent = new Intent("android.settings.APPLICATION_DETAILS_SETTINGS");
        intent.setData(Uri.parse("package:" + packageName));
        return intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    }


}
