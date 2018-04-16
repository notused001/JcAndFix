package com.warrior.util;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

/**
 * Created by Jamie
 */

public class Utils {

    /**
     * 获取应用程序versionname
     *
     * @param context
     * @return
     */
    public static String getVersionName(Context context) {
        String versionName = "1.0.0";
        try {
            PackageManager pm = context.getPackageManager();
            PackageInfo pi = pm.getPackageInfo(context.getPackageName(), 0);
            versionName = pi.versionName;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return versionName;
    }
}
