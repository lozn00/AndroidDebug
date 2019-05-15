package cn.qssq666.androiddebug;

import android.app.Application;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.os.Debug;
import android.util.Log;
import android.view.Window;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

public class Main implements IXposedHookLoadPackage {
    private static final String TAG = "DebugMain";

    public static boolean isDebug(PackageInfo packageInfo) {
        try {
            if (packageInfo != null) {
                ApplicationInfo info = packageInfo.applicationInfo;
                return (info.flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0;
            }
        } catch (Exception e) {

        }
        return false;

    }

    public void handleLoadPackage(final LoadPackageParam lpparam) throws Throwable {

        if (lpparam.packageName.equals(" com.baidu.BaiduMap")) {
            return;
        }
        Log.w(TAG,"handleLoadPackage "+lpparam.packageName);
        try {
            trywrap(lpparam);

        } catch (Throwable e) {

        }
        try {

            XposedHelpers.findAndHookMethod(Debug.class, "isDebuggerConnected", new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    super.beforeHookedMethod(param);
                    param.setResult(false);
                }

                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    super.afterHookedMethod(param);
                }
            });

            Class<?> activityThreadClass = null;
            activityThreadClass = Class.forName("android.app.ActivityThread");
            Object currentActivityThread = activityThreadClass.getDeclaredMethod("currentActivityThread", new Class[0]).invoke(null, new Object[0]);
            Field sPackageManagerField = activityThreadClass.getDeclaredField("sPackageManager");
            sPackageManagerField.setAccessible(true);
            Object sPackageManager = sPackageManagerField.get(currentActivityThread);
//        Class<?> iPackageManagerInterface = Class.forName("android.content.pm.IPackageManager");
            Class<?> querySignClass;
            if (sPackageManager == null) {
                querySignClass = Class.forName("android.app.ApplicationPackageManage");
            } else {
                querySignClass = sPackageManager.getClass();
            }
            final Method getPackageInfo = querySignClass.getMethod("getPackageInfo", String.class, int.class, int.class);
            XposedBridge.hookMethod(getPackageInfo, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    super.beforeHookedMethod(param);
                }

                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    super.afterHookedMethod(param);

                    String packageName = (String) param.args[0];
                    try{

                    Context application = XposeUtil.getApplication();
                    if (application == null || application.getPackageName().equals(packageName)) {
                        PackageInfo packageInfo = (PackageInfo) param.getResult();
                        if (packageInfo != null && isDebug(packageInfo)) {
                            int flags = 0;
                            int mask = ApplicationInfo.FLAG_DEBUGGABLE;
                            packageInfo.applicationInfo.flags = (packageInfo.applicationInfo.flags & ~mask) | (flags & mask);
                            Log.w(TAG, packageName + " ApplicationPackageManage 清除debug标记后:" + isDebug(packageInfo) + "<" + packageInfo.applicationInfo.packageName);
                        } else {
//                            Log.w(TAG,"非调试状态。。。。。");
                        }
                    } else {

                    }
                    }catch (Throwable e){
                        Log.e(TAG,"内部崩溃",e);
                    }
                }
            });

            XposedHelpers.findAndHookMethod(Application.class, "attach", Context.class, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    super.beforeHookedMethod(param);
                }

                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    super.afterHookedMethod(param);
                    try{
                    final Context application = XposeUtil.getApplication();
                    final Context context = (Context) param.args[0];
                    Log.w(TAG, "___attach:" + context.getPackageName() + "," + param.thisObject.getClass().getName());

                    XposedHelpers.findAndHookMethod("android.content.ContextWrapper",lpparam.classLoader, "getApplicationInfo", new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                            super.afterHookedMethod(param);
                        }

                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            super.beforeHookedMethod(param);

                            try{

                            Log.w(TAG, "context.getApplicationInfo:" + context.getPackageName());
                            PackageInfo result = (PackageInfo) param.getResult();
                            if (result != null && isDebug(result)) {
                                //&(~flag)
//                                result.applicationInfo.flags &= ~ApplicationInfo.FLAG_DEBUGGABLE;
                                int flags = 0;
                                int mask = ApplicationInfo.FLAG_DEBUGGABLE;
                                result.applicationInfo.flags = (result.applicationInfo.flags & ~mask) | (flags & mask);

                                Log.w(TAG, application.getClass().getName() + " 清除debug标记后:" + isDebug(result) + "<" + result.applicationInfo.packageName);
                            }
                            }catch (Throwable e){
                                Log.e(TAG,"getApplicationInfo内部崩溃",e);
                            }


                        }
                    });
                    }catch (Throwable e){
                        Log.e(TAG,"attach崩溃",e);
                    }

                }
            });

        } catch (Throwable e) {
            Log.w(TAG, "ERROR_" + lpparam.packageName, e);
        }



      /*      XposedHelpers.findAndHookMethod("android.app.ApplicationPackageManager", lpparam.classLoader, "getPackageInfoAsUser", String.class, int.class, int.class, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    super.afterHookedMethod(param);
                }

                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    super.beforeHookedMethod(param);
                    PackageInfo result = (PackageInfo) param.getResult();
                    if (isDebug(result)) {
                        result.applicationInfo.flags |= ApplicationInfo.FLAG_DEBUGGABLE;
                        Log.w(TAG, "清除debug标记后:" + isDebug(result) + "<" + result.applicationInfo.packageName);
                    }

                }
            });
*/

    }

    private void trywrap(LoadPackageParam lpparam) {
        Class<?> aClass = XposedHelpers.findClass("com.android.server.pm.PackageManagerService", lpparam.classLoader);

        XposedBridge.hookAllMethods(aClass, "getPackageInfo", new XC_MethodHook() {
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                PackageInfo packageInfo = (PackageInfo) param.getResult();
                if (packageInfo != null) {
                    ApplicationInfo appinfo = packageInfo.applicationInfo;
                    int flags = appinfo.flags;
                    if (appinfo.packageName.equals("com.aicoin.phone")) {
                        Log.i(TAG, "Load App : " + appinfo.packageName);
//                            Log.i(TAG, "Load App : " + appinfo.packageName + "," + Log.getStackTraceString(new RuntimeException()));
                    } else {
                        Log.i(TAG, "Load App : " + appinfo.packageName);

                    }
                    Log.i(TAG, "==== After Hook ====");
                    if ((flags & ApplicationInfo.FLAG_ALLOW_BACKUP) == 0) {
                        flags |= ApplicationInfo.FLAG_ALLOW_BACKUP;
                    }
                    if ((flags & ApplicationInfo.FLAG_DEBUGGABLE) == 0) {
                        flags |= ApplicationInfo.FLAG_DEBUGGABLE;
                    }
                    appinfo.flags = flags;
                    param.setResult(packageInfo);
                    Log.i(TAG, "flags = " + flags);
                    Main.isDebugable(appinfo);
                    Main.isBackup(appinfo);
                }
            }
        });
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