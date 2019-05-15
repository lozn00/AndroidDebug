package cn.qssq666.androiddebug;

import android.content.Context;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;

public class XposeUtil {

    public static Context getContext() {
        Class<?> aClass = XposedHelpers.findClass("android.app.ActivityThread", ClassLoader.getSystemClassLoader());
        //QSSQUtils.LogHelper("classTheread:" + aClass);
//            android.app.ActivityThread
        Object currentActivityThread = XposedHelpers.callStaticMethod(aClass, "currentActivityThread");//ActivityThread
        if (currentActivityThread == null) {
            return null;
        }
        Object context = XposedHelpers.callMethod(currentActivityThread, "getSystemContext");
        return (Context) context;
    }

    public static Context getApplication() {
        Class<?> activityThreadClass = null;
        try {
            activityThreadClass = Class.forName("android.app.ActivityThread");
        Method currentApplicationMethod = activityThreadClass.getDeclaredMethod("currentApplication");
      return (Context) currentApplicationMethod.invoke(null);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return null;
    }
}
