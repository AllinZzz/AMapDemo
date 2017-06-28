package com.zyl.amapdemo.core;

/**
 * Created by zyl on 2017/6/27.
 * 授权结果监听
 */

public interface PermissionResultListener {

    void onPermissionGranted();

    void onPermissionDenied();
}
