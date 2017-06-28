package com.zyl.amapdemo.core;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.zyl.amapdemo.utils.AppUtils;

import java.util.ArrayList;

/**
 * Created by zyl on 2017/6/27.
 * Activity基类
 */

public class BaseActivity extends AppCompatActivity {

    private static final String TAG = "BaseActivity";
    protected Context mContext;
    private PermissionResultListener permissionResultListener;
    //权限请求的code
    private static final int MY_PERMISSION_REQUEST_CODE = 0x001;
    //手动跳转到设置界面的code
    private static final int SETTING_REQUEST_CODE = 0x002;
    //申请的权限
    private ArrayList<String> requestPermissions;
    //是否需要强退的标记
    private boolean needForceFinish;
    //必须权限数组,没有则app被强退
    protected static final ArrayList<String> FORCE_REQUIRE_PERMISSIONS = new ArrayList<String>(){
        {
            add(Manifest.permission.ACCESS_COARSE_LOCATION);
            add(Manifest.permission.INTERNET);
            add(Manifest.permission.READ_EXTERNAL_STORAGE);
            add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            add(Manifest.permission.ACCESS_FINE_LOCATION);
            add(Manifest.permission.CALL_PHONE);
            add(Manifest.permission.READ_CALENDAR);
            add(Manifest.permission.CAMERA);
            add(Manifest.permission.READ_SMS);
        }

    };


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //没有actionbar
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        //取消横屏
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        //输入法弹出的时候不顶起布局
        //如果我们不设置"adjust..."的属性，对于没有滚动控件的布局来说，采用的是adjustPan方式，
        // 而对于有滚动控件的布局，则是采用的adjustResize方式。
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN |
                WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        mContext = this;
    }

    /**
     * 请求权限
     * @param permissions 需要请求的权限数组
     * @param needFinish 未授权是否需要强退app
     * @param listener 请求权限操作的回调
     */
    protected void requestPermission(ArrayList<String> permissions, boolean needFinish, PermissionResultListener
            listener) {
        if(listener == null) {
            throw new IllegalArgumentException("PermissionResultListener should not be null");
        }
        if(permissions == null || permissions.size() == 0) {
            Log.w(TAG,"permissions should not be null or empty!");
            return;
        }
        Log.d("requirePermissions == " ,permissions.toString());
        needForceFinish = needFinish;
        permissionResultListener = listener;
        requestPermissions = permissions;
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            //当前系统版本大于或等于6.0,对于特殊权限,除了要在清单文件中声明, 还需要向用户申请
            //检查每一个所需权限是否已经授权
           String[] nonSelfPermissions =  checkEachSelfPermission(permissions.toArray(new String[]{}));
            if(nonSelfPermissions.length > 0) {
                //还有未授权的权限 , 请求授权
                requestEachPermissions(nonSelfPermissions);
            } else {
                permissionResultListener.onPermissionGranted();
            }
        } else {
            //反之,只需要在清单文件中声明就可以了
            permissionResultListener.onPermissionGranted();
        }

    }

    /**
     * 检查传入的每一个权限是否都已经授权了
     * @param permissions 权限数组
     * @return 返回未授权的权限数组
     */
    private String[] checkEachSelfPermission(String[] permissions) {
        ArrayList<String> permissionsList = new ArrayList<>();
        for (String permission : permissions) {
            if(ActivityCompat.checkSelfPermission(this,permission) != PackageManager.PERMISSION_GRANTED){
                //当前权限没有授权, 加入到未授权组中去
                permissionsList.add(permission);
            }
        }
        return permissionsList.toArray(new String[permissionsList.size()]);
    }

    /**
     * 对未通过授权的权限
     * @param nonSelfPermissions 检测到的未授权的权限数组
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    private void requestEachPermissions(String[] nonSelfPermissions) {
        if (shouldShowRequestPermissionRationale(nonSelfPermissions)) {
            //之前已拒绝过,需要重新提示
            showPermissionRationaleDialog(nonSelfPermissions);
        }else {
            //不需要提示,直接申请
            ActivityCompat.requestPermissions(this,nonSelfPermissions,MY_PERMISSION_REQUEST_CODE);
        }

    }

    /**
     * 判断每一个未授权的权限,是否需要重新提示用户授权
     * @param permissions 未授权的权限数组
     * @return true: 需要提示  false: 不需要提示
     */
    private boolean shouldShowRequestPermissionRationale(String[] permissions) {
        for (String permission : permissions) {
            if(ActivityCompat.shouldShowRequestPermissionRationale(this,permission)) {
                return true;
            }
        }
        return false;
    }


    /**
     * 显示重新请求授权对话框
     * @param nonSelfPermission 未授权的权限
     */
    private void showPermissionRationaleDialog(final String[] nonSelfPermission) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("温馨提示:")
                .setMessage("为了应用可以正常使用,请您点击确认申请权限")
                .setPositiveButton("确认", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                        ActivityCompat.requestPermissions(BaseActivity.this,nonSelfPermission,MY_PERMISSION_REQUEST_CODE);
                    }
                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                        if(needForceFinish) {
                            Toast.makeText(mContext, "拒绝开启该权限,App将无法使用", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    }
                })
                .setCancelable(false)
                .show();
    }

    /**
     * 申请权限结果的回调
     * @param requestCode 申请权限时传入的请求码
     * @param permissions The requested permissions
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        for (int i=0 ; i<permissions.length; i ++) {
            Log.d("申请结果 : " , permissions[i] + " -- " +grantResults[i]);
        }
   /*     if (requestCode == MY_PERMISSION_REQUEST_CODE) {
            //获取请求的权限列表中被拒绝的部分
            ArrayList<String> deniedPermissions = new ArrayList<>();
            for (String permission : permissions) {
                if(ActivityCompat.checkSelfPermission(mContext,permission) != PackageManager.PERMISSION_GRANTED) {
                    deniedPermissions.add(permission);
                }
            }
            //判断被拒绝的权限中,是否包含必须具备的权限
            if(deniedPermissions.size() > 0) {
                ArrayList<String> forceRequiredPermissionDenied = checkForceRequiredPermissionDenied(
                        FORCE_REQUIRE_PERMISSIONS,deniedPermissions);
                if(forceRequiredPermissionDenied != null && forceRequiredPermissionDenied.size() > 0) {
                    //有必须的权限被拒绝了
                    if (needForceFinish) {
                        //显示手动打开系要权限的dialog
                        showForcePermissionsSettingDialog();
                    } else {
                        permissionResultListener.onPermissionDenied();
                    }
                } else {
                    //没有必要的权限被拒绝,可以进首页
                    permissionResultListener.onPermissionGranted();
                }
            }

        }*/
    }

    private void showForcePermissionsSettingDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("温馨提示")
                .setMessage("必要的权限被拒绝了,App将无法正常使用")
                .setPositiveButton("去设置", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        AppUtils.getAppDetailsSettings(mContext,SETTING_REQUEST_CODE);
                    }
                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                        if (needForceFinish) {
                            AppUtils.restart(mContext);
                        }
                    }
                })
                .setCancelable(false)
                .show();
    }

    private ArrayList<String> checkForceRequiredPermissionDenied(ArrayList<String> forceRequirePermissions,
                                                                 ArrayList<String> deniedPermissions) {
        ArrayList<String> forceRequirePermissionDenied = new ArrayList<>();
        for (String forceRequirePermission : forceRequirePermissions) {
            if(deniedPermissions.contains(forceRequirePermission)) {
                forceRequirePermissionDenied.add(forceRequirePermission);
            }
        }
        return forceRequirePermissionDenied;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //如果需要跳转系统设置页后返回自动再次检查和执行业务 如果不需要则不需要重写onActivityResult
        if (requestCode == SETTING_REQUEST_CODE) {
            requestPermission(requestPermissions,needForceFinish,permissionResultListener);
        }
    }
}
