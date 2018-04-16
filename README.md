## AndFix的基本介绍
AndFix，全称是Android hot-fix。是阿里开源的一个Android热补丁框架，允许APP在不重新发布版本的情况下修复线上的bug。
## 使用AndFix完成线上Bug修复
##### 常用API
- 初始化API
```
patchManager = new PatchManager(context);
patchManager.init(appversion);//current version
```
- 装载patch文件
`patchManager.loadPatch();`
这里注意，要尽早加载patch，最好在Application.onCreate();方法中调用
- Add patch,
`patchManager.addPatch(path);//path of the patch file that was downloaded`
patch文件被下载下来后，通过addPatch()添加到patchManager中，才能生效。

##### AndFix简单封装
- Creater AndFixPatchManager 用来管理AndFix的api
整个app中只有需要有一个AndFixPatchManager管理类就可以了，所以采用单例模式(双重校验锁)。
之后再application中调用就可以了。
```
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
```
##### 创建一个带bug的apk

首先build一个有bug的 bug apk安装到手机
解决bug后，build一个新的 new apk
首先定义一个patch文件补丁安装的路径，并初始化。
```
    private static final String file_end = ".apatch";
    private String mPatchDir;
    mPatchDir = getExternalCacheDir().getAbsolutePath()+"/apatch";
```
模拟bug的产生：创建一个按钮的点击事件
由于error并没有初始化，打印的时候，是会报空指针的。
```
public void createBug(View view){
        String error = null;
        Log.e("createbug",error);
    }
```
创建一个修复按钮
```
public void fixBug(View view){
        AndFixPatchManager.getInstance().addPatch(getPatchName());
}
```
然后我们去构建一个带Bug的apk，并安装到手机上

##### patch生成
在官网上下载 apkpatch-1.0.3.zip ，根据系统运行其中的.bat文件或者.sh文件。
![](https://upload-images.jianshu.io/upload_images/11184437-f6620dbd7157cb91.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

- apkpatch命令及参数详解
apkpatch -f 用来生成patch文件
-f  新apk的文件路径
-t 旧apk的文件路径
-o 输出apatch的文件路径
apkpatch -m 合并多个patch文件为一个
- 使用apkpatch命令生成apatch包
./apkpatch.sh -f new.apk -t bug.apk -o outputs/ -k youdo .jks -p xxxxx -a xxxxxx -e xxxxxxx

##### patch安装
将apatch文件push到手机,，然后点击修复bug的按钮即可。

- 实际工作中可以使用下面俩个方法来完成热修复
1 应用启动的时候，在 onCreate() 方法中获取友盟的在线参数来判断当前的应用版本是否有补丁需要下载，有则通过ThinDonloadManager来下载到SD下并且通过使用AndFix来加载到应用中。
2 使用极光推送消息到该应用的版本需要下载补丁，如果应用收到了消息后，应用判断当前的版本是否需要下载补丁。如果应用没有收到消息的通知，则下次启动App的时候，获取友盟在线参数来判断是否需要下载补丁。

## AndFix简单封装
- 具体步骤：
1 发现Bug 生成apatch
2 将apatch下发到用户手机存储系统
3 利用AndFix完成patch安装，解决Bug
- 具体实现思路
- 
![](https://upload-images.jianshu.io/upload_images/11184437-6cfae8bc18645508.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

1 首先调用AndFix模块的`checkHasPatch()`向服务器检查当前是否有新的patch
2 服务器会返回一个实体对象`return PatchUpdateInfo`,里面包含相关的Patch信息
3 调用`hasNewPatch()`来判断是否有新的Patch
4 如果没有Patch，直接`stopSeif()`
5 如果有新的Patch，调用`downLoadPatch()`，`startDownload`下载Patch文件
6 下载完成后，把服务器`return PatchFile`，返回的Patch文件保存到手机存储。
7 最后调用`AndFixPatchManager `来安装补丁文件

- 具体编码
首先创建一个AndFixService服务，要把整个模块放在一个Service中，在后台默默的运行。
完成文件目录的构造
```
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
```
通过`onStartCommand`发送handler信息请求更新
```
 @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mHandler.sendEmptyMessage(UPDATE_PATCH);
        return START_NOT_STICKY;
    }
```
检查是否有新的apatch文件
```
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
```
如果有更新，则完成下载，调用api安装补丁`AndFixPatchManager.getInstance().addPatch(mPatchFile);`
```
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
```
