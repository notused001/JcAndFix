package com.warrior.andfix;

import android.content.Context;

import com.alipay.euler.andfix.patch.PatchManager;
import com.warrior.util.Utils;

/**
 * Created by Jamie
 */

public class AndFixPatchManager {
    private static AndFixPatchManager mInstance = null;

    private static PatchManager mPatchManager = null;

    public static AndFixPatchManager getInstance() {
        if (mInstance == null) {
            synchronized (AndFixPatchManager.class) {
                if (mInstance == null) {
                    mInstance = new AndFixPatchManager();
                }
            }
        }
        return mInstance;
    }

    //初始化AndFix方法
    public void initPatch(Context context) {
        mPatchManager = new PatchManager(context);
        mPatchManager.init(Utils.getVersionName(context));
        mPatchManager.loadPatch();
    }

    //加载我们的patch文件
    public void addPatch(String path) {
        try {
            if (mPatchManager != null) {
                mPatchManager.addPatch(path);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
