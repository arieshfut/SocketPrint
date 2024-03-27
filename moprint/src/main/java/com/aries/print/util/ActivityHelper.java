package com.aries.print.util;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

/**
 * @author : Crazy.Mo
 */
public class ActivityHelper {
    /**
     *PrintHand 打印ApplicationId
     */
    public static final String APPID_PRINTHAND = "com.dynamixsoftware.printhand";

    public static final String MAIN_PRINTHAND = APPID_PRINTHAND+".ui.ActivityMain";
    /**
     *惠普打印ApplicationId
     */
    public static final String APPID_HP="com.hp.printercontrol";

    public static final String MAIN_HP = APPID_HP+".base.PrinterControlActivity";
    /**
     * 通过应用的包名和对应的Activity全类名启动任意一个Activity（可以跨进程）
     * 如果该Activity非应用入口(入口Activity默认android:exported="true")，则需要再清单文件中添加 android:exported="true"。
     * Service也需要添加android:exported="true"。允许外部应用调用。
     * @param pkg 应用的包名即AppcationId
     * @param cls 要启动的Activity 全类名
     */
    public static void startActivityByComponentName(Context context, String pkg, String cls) {
        ComponentName comp = new ComponentName(pkg,cls);
        Intent intent = new Intent();
        intent.setComponent(comp);
        intent.setAction("android.intent.action.VIEW");
        intent.setAction("android.intent.action.SEND");
        intent.addCategory("android.intent.category.DEFAULT");
        context.startActivity(intent);
    }
}
