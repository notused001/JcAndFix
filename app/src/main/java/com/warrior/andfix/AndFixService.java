package com.warrior.andfix;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.warrior.beans.BasePatch;
import com.warrior.network.RequestCenter;
import com.warrior.network.listener.DisposeDataListener;
import com.warrior.network.listener.DisposeDownloadListener;

import java.io.File;

/**
 * Created by Jamie
 */

public class AndFixService extends Service {

    private static final int UPDATE_PATCH = 0x02;
    private static final int DOWNLOAD_PATCH = 0x01;
    private static final String FILE_END = ".apatch";
    private static final String TAG = AndFixService.class.getSimpleName();
    private String mPatchFileDir;//patch文件存放路径
    private String mPatchFile;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case UPDATE_PATCH:
                    checkPatchUpdate();
                    break;
                case DOWNLOAD_PATCH:
                    downloadPatch();
                    break;
            }
        }


    };

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        init();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mHandler.sendEmptyMessage(UPDATE_PATCH);
        return START_NOT_STICKY;
    }

    private void init() {
        mPatchFileDir = getExternalCacheDir().getAbsolutePath() + "/apatch/";
        File patchDir = new File(mPatchFileDir);

        try {
            if (patchDir == null || !patchDir.exists()) {
                patchDir.mkdir();
            }
        } catch (Exception e) {
            e.printStackTrace();
            stopSelf();
        }
    }

    private BasePatch mBasePatchInfo;
    private void checkPatchUpdate() {
        RequestCenter.requestPatchUpdateInfo(new DisposeDataListener() {
            @Override
            public void onSuccess(Object responseObj) {
                mBasePatchInfo= (BasePatch) responseObj;
                if(!TextUtils.isEmpty(mBasePatchInfo.data.downloadUrl)){
                    mHandler.sendEmptyMessage(DOWNLOAD_PATCH);
                }else{
                    stopSelf();
                }
            }

            @Override
            public void onFailure(Object reasonObj) {
                stopSelf();
            }
        });
    }

    private void downloadPatch() {
        mPatchFile = mPatchFileDir.concat(String.valueOf(System.currentTimeMillis())).concat(FILE_END);
        RequestCenter.downloadFile(mBasePatchInfo.data.downloadUrl, mPatchFile, new DisposeDownloadListener() {
            @Override
            public void onProgress(int progrss) {

            }

            @Override
            public void onSuccess(Object responseObj) {
                AndFixPatchManager.getInstance().addPatch(mPatchFile);
            }

            @Override
            public void onFailure(Object reasonObj) {
                stopSelf();
            }
        });
    }
}
