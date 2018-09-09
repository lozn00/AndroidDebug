package cn.qssq666.androiddebug;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.util.Log;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

public class Main implements IXposedHookLoadPackage {
    private static final String TAG = "DebugMain";

    public void handleLoadPackage(LoadPackageParam lpparam) throws Throwable {

        if(lpparam.packageName.equals(" com.baidu.BaiduMap")){
            return;
        }
        try {


            Class<?> aClass = XposedHelpers.findClass("com.android.server.pm.PackageManagerService", lpparam.classLoader);

            XposedBridge.hookAllMethods(aClass, "getPackageInfo", new XC_MethodHook() {
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    PackageInfo packageInfo = (PackageInfo) param.getResult();
                    if (packageInfo != null) {
                        ApplicationInfo appinfo = packageInfo.applicationInfo;
                        int flags = appinfo.flags;
                        Log.i(TAG, "Load App : " + appinfo.packageName);
                        Log.i(TAG, "==== After Hook ====");
                        if ((flags & 32768) == 0) {
                            flags |= 32768;
                        }
                        if ((flags & 2) == 0) {
                            flags |= 2;
                        }
                        appinfo.flags = flags;
                        param.setResult(packageInfo);
                        Log.i(TAG, "flags = " + flags);
                        Main.isDebugable(appinfo);
                        Main.isBackup(appinfo);
                    }
                }
            });
        } catch (Exception e) {

        } catch (Error e) {

        }

    }

    public static boolean isDebugable(ApplicationInfo info) {
        try {
            if ((info.flags & 2) != 0) {
                Log.i(TAG, "Open Debugable");
                return true;
            }
        } catch (Exception e) {
        }
        Log.i(TAG, "Close Debugable");
        return false;
    }

    public static boolean isBackup(ApplicationInfo info) {
        try {
            if ((info.flags & 32768) != 0) {
                Log.i(TAG, "Open Backup");
                return true;
            }
        } catch (Exception e) {
        }
        Log.i(TAG, "Close Backup");
        return false;
    }
}