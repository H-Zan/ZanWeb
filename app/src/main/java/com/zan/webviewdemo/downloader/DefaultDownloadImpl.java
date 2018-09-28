
package com.zan.webviewdemo.downloader;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.webkit.WebView;


import com.zan.webviewdemo.App;
import com.zan.webviewdemo.R;
import com.zan.webviewdemo.util.ToastUtil;
import com.zan.webviewdemo.util.ZanL;
import com.zan.webviewdemo.webbase.Action;
import com.zan.webviewdemo.webbase.ActionActivity;
import com.zan.webviewdemo.webbase.AgentWebPermissions;
import com.zan.webviewdemo.webbase.AgentWebUtils;
import com.zan.webviewdemo.webbase.PermissionInterceptor;

import java.io.File;
import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DefaultDownloadImpl implements android.webkit.DownloadListener {
    /**
     * Application Context
     */
    private Context mContext;
    /**
     * 通知ID，默认从1开始
     */
    private volatile static AtomicInteger NOTICATION_ID = new AtomicInteger(1);
    /**
     * 下载监听，DownloadListener#onStart 下载的时候触发，DownloadListener#result下载结束的时候触发
     * 4.0.0 每一次下载都会触发这两个方法，4.0.0以下只有触发下载才会回调这两个方法。
     */
    private DownloadListener mDownloadListener;
    /**
     * Activity
     */
    private WeakReference<Activity> mActivityWeakReference = null;
    /**
     * TAG 用于打印，标识
     */
    private static final String TAG = DefaultDownloadImpl.class.getSimpleName();
    /**
     * 权限拦截
     */
    private PermissionInterceptor mPermissionListener = null;
    /**
     * 当前下载链接
     */
    private String mUrl;
    /**
     * mContentDisposition ，提取文件名 ，如果ContentDisposition不指定文件名，则从url中提取文件名
     */
    private String mContentDisposition;
    /**
     * 文件大小
     */
    private long mContentLength;
    /**
     * 文件类型
     */
    private String mMimetype;
    /**
     * AbsAgentWebUIController
     */
//    private WeakReference<AbsAgentWebUIController> mAgentWebUIController;
    /**
     * ExtraServiceImpl
     */
    private ExtraServiceImpl mExtraServiceImpl;
    /**
     * UA
     */
    private String mUserAgent;
    /**
     * ExtraServiceImpl
     */
    private ExtraServiceImpl mCloneExtraServiceImpl = null;
    /**
     * 进度回调
     */
    private volatile DownloadingListener mDownloadingListener;
    /**
     * 根据p3c，预编译正则，提升性能。
     */
    private Pattern mPattern = Pattern.compile(".*filename=(.*)");

    DefaultDownloadImpl(ExtraServiceImpl extraServiceImpl) {
        if (!extraServiceImpl.mIsCloneObject) {
            this.bind(extraServiceImpl);
            this.mExtraServiceImpl = extraServiceImpl;
        } else {
            this.mCloneExtraServiceImpl = extraServiceImpl;
        }
    }

    private void bind(ExtraServiceImpl extraServiceImpl) {
        this.mActivityWeakReference = new WeakReference<Activity>(extraServiceImpl.mActivity);
        this.mContext = extraServiceImpl.mActivity.getApplicationContext();
        this.mDownloadListener = extraServiceImpl.mDownloadListener;
        this.mDownloadingListener = extraServiceImpl.mDownloadingListener;
        this.mPermissionListener = extraServiceImpl.mPermissionInterceptor;
//        this.mAgentWebUIController = new WeakReference<AbsAgentWebUIController>(AgentWebUtils.getAgentWebUIControllerByWebView(extraServiceImpl.mWebView));
    }


    @Override
    public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimetype, long contentLength) {
        onDownloadStartInternal(url, userAgent, contentDisposition, mimetype, contentLength, null);
    }


    private synchronized void onDownloadStartInternal(String url, String userAgent, String contentDisposition, String mimetype, long contentLength, ExtraServiceImpl extraServiceImpl) {

        if (null == mActivityWeakReference.get() || mActivityWeakReference.get().isFinishing()) {
            return;
        }
        if (null != this.mPermissionListener) {
            if (this.mPermissionListener.intercept(url, AgentWebPermissions.STORAGE, "download")) {
                return;
            }
        }

        ExtraServiceImpl mCloneExtraServiceImpl = null;
        if (null == extraServiceImpl) {
            try {
                mCloneExtraServiceImpl = (ExtraServiceImpl) this.mExtraServiceImpl.clone();
            } catch (CloneNotSupportedException ignore) {
                if (ZanL.isDebug()) {
                    ignore.printStackTrace();
                }
                ZanL.i(TAG, " clone object failure !!! ");
                return;
            }
        } else {
            mCloneExtraServiceImpl = extraServiceImpl;
        }
        mCloneExtraServiceImpl
                .setUrl(this.mUrl = url)
                .setMimetype(this.mMimetype = mimetype)
                .setContentDisposition(this.mContentDisposition = contentDisposition)
                .setContentLength(this.mContentLength = contentLength)
                .setUserAgent(this.mUserAgent = userAgent);
        this.mCloneExtraServiceImpl = mCloneExtraServiceImpl;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            List<String> mList = null;
            if ((mList = checkNeedPermission()).isEmpty()) {
                preDownload();
            } else {
                Action mAction = Action.createPermissionsAction(mList.toArray(new String[]{}));
                ActionActivity.setPermissionListener(getPermissionListener());
                ActionActivity.start(mActivityWeakReference.get(), mAction);
            }
        } else {
            preDownload();
        }
    }

    private ActionActivity.PermissionListener getPermissionListener() {
        return new ActionActivity.PermissionListener() {
            @Override
            public void onRequestPermissionsResult(@NonNull String[] permissions, @NonNull int[] grantResults, Bundle extras) {
                if (checkNeedPermission().isEmpty()) {
                    preDownload();
                } else {
                    /*if (null != mAgentWebUIController.get()) {
                        mAgentWebUIController
                                .get()
                                .onPermissionsDeny(
                                        checkNeedPermission().
                                                toArray(new String[]{}),
                                        AgentWebPermissions.ACTION_STORAGE, "Download");
                    }*/
                    ToastUtil.showNormal("储存权限获取失败~");
                    ZanL.e(TAG, "储存权限获取失败~");
                }

            }
        };
    }

    private List<String> checkNeedPermission() {
        List<String> deniedPermissions = new ArrayList<>();
        if (!AgentWebUtils.hasPermission(mActivityWeakReference.get(), AgentWebPermissions.STORAGE)) {
            deniedPermissions.addAll(Arrays.asList(AgentWebPermissions.STORAGE));
        }
        return deniedPermissions;
    }

    private void preDownload() {

        // true 表示用户取消了该下载事件。
        if (null != this.mDownloadListener
                && this.mDownloadListener
                .onStart(this.mUrl,
                        this.mUserAgent,
                        this.mContentDisposition,
                        this.mMimetype,
                        this.mContentLength,
                        this.mCloneExtraServiceImpl)) {
            return;
        }
        File mFile = getFile(mContentDisposition, mUrl);
        // File 创建文件失败
        if (null == mFile) {
            ZanL.e(TAG, "新建文件失败");
            return;
        }
        if (mFile.exists()) {
            ZanL.e(TAG, "文件已存在1 mFilelength:" + mFile.length() + ",mContentLength:" + mContentLength);

        }
        //mFile.length()=0?
        if (mFile.exists() && mFile.length() >= mContentLength && mContentLength > 0) {
//        if (mFile.exists()) {
            ZanL.e(TAG, "文件已存在");

            // true 表示用户处理了下载完成后续的通知用户事件
            if (null != this.mDownloadListener && this.mDownloadListener.onResult(mFile.getAbsolutePath(), mUrl, null)) {
                return;
            }

            Intent mIntent = AgentWebUtils.getCommonFileIntentCompat(mContext, mFile);
            try {
//                mContext.getPackageManager().resolveActivity(mIntent)
                if (null != mIntent) {
                    if (!(mContext instanceof Activity)) {
                        mIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    }
                    mContext.startActivity(mIntent);
                }
                return;
            } catch (Throwable throwable) {
                ToastUtil.showNormal("没有找到可以打开文件的应用");
                if (ZanL.isDebug()) {
                    throwable.printStackTrace();
                }
                return;
            }

        }

        // 该链接是否正在下载
        if (ExecuteTasksMap.getInstance().contains(mUrl) || ExecuteTasksMap.getInstance().contains(mFile.getAbsolutePath())) {
            /*if (mAgentWebUIController.get() != null) {
                mAgentWebUIController.get().onShowMessage(
                        mActivityWeakReference.get()
                                .getString(R.string.agentweb_download_task_has_been_exist),
                        TAG.concat("|preDownload"));
            }*/
            ToastUtil.showNormal(App.get().getContext().getString(R.string.agentweb_download_task_has_been_exist));
            return;
        }


        // 移动数据
        if (!this.mCloneExtraServiceImpl.isForceDownload() &&
                AgentWebUtils.checkNetworkType(mContext) > 1) {

            showDialog(mFile);
            return;
        }
        performDownload(mFile);
    }

    private void forceDownload(final File file) {

        this.mCloneExtraServiceImpl.setForceDownload(true);
        performDownload(file);
    }

    private void showDialog(final File file) {

        Activity mActivity;
        if (null == (mActivity = mActivityWeakReference.get()) || mActivity.isFinishing()) {
            return;
        }
        onForceDownloadAlert(mActivity, createCallback(file));
    }

    private void onForceDownloadAlert(Activity ac, final Handler.Callback callback) {
        Activity mActivity;
        if ((mActivity = ac) == null || mActivity.isFinishing()) {
            return;
        }

        AlertDialog mAlertDialog = null;
        mAlertDialog = new AlertDialog.Builder(mActivity)
                .setTitle(mActivity.getString(R.string.agentweb_tips))
                .setMessage(mActivity.getString(R.string.agentweb_honeycomblow))
                .setNegativeButton(mActivity.getString(R.string.agentweb_download), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (dialog != null) {
                            dialog.dismiss();
                        }
                        if (callback != null) {
                            callback.handleMessage(Message.obtain());
                        }
                    }
                })//
                .setPositiveButton(mActivity.getString(R.string.agentweb_cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        if (dialog != null) {
                            dialog.dismiss();
                        }
                    }
                }).create();

        mAlertDialog.show();
    }

    private Handler.Callback createCallback(final File file) {
        return new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                forceDownload(file);
                return true;
            }
        };
    }

    private void performDownload(File file) {

        try {

            ExecuteTasksMap.getInstance().addTask(mUrl, file.getAbsolutePath());
            /*if (null != mAgentWebUIController.get()) {
                mAgentWebUIController.get()
                        .onShowMessage(mActivityWeakReference.get().getString(R.string.agentweb_coming_soon_download) + ":" + file.getName(), TAG.concat("|performDownload"));
            }*/

            ToastUtil.showNormal(mActivityWeakReference.get().getString(R.string.agentweb_coming_soon_download) + ":" + file.getName());

            DownloadTask mDownloadTask = new DownloadTask(NOTICATION_ID.incrementAndGet(),
                    this.mDownloadListenerAdapter,
                    mContext, file,
                    this.mCloneExtraServiceImpl);
            new Downloader().download(mDownloadTask);
            this.mUrl = null;
            this.mContentDisposition = null;
            this.mContentLength = -1;
            this.mMimetype = null;
            this.mUserAgent = null;

        } catch (Throwable ignore) {
            if (ZanL.isDebug()) {
                ignore.printStackTrace();
            }
        }

    }


    private File getFile(String contentDisposition, String url) {

        String fileName = "";
        try {
            fileName = getFileNameByContentDisposition(contentDisposition);
            if (TextUtils.isEmpty(fileName) && !TextUtils.isEmpty(url)) {
                Uri mUri = Uri.parse(url);
                fileName = mUri.getPath().substring(mUri.getPath().lastIndexOf('/') + 1);
            }
            if (!TextUtils.isEmpty(fileName) && fileName.length() > 64) {
                fileName = fileName.substring(fileName.length() - 64, fileName.length());
            }
            if (TextUtils.isEmpty(fileName)) {
                fileName = AgentWebUtils.md5(url);
            }
            if (fileName.contains("\"")) {
                fileName = fileName.replace("\"", "");
            }
            return AgentWebUtils.createFileByName(mContext, fileName, !this.mCloneExtraServiceImpl.isOpenBreakPointDownload());
        } catch (Throwable e) {
            if (ZanL.isDebug()) {
                ZanL.i(TAG, "fileName:" + fileName);
                e.printStackTrace();
            }
        }

        return null;
    }

    private String getFileNameByContentDisposition(String contentDisposition) {
        if (TextUtils.isEmpty(contentDisposition)) {
            return "";
        }
        Matcher m = mPattern.matcher(contentDisposition.toLowerCase());
        if (m.find()) {
            return m.group(1);
        } else {
            return "";
        }
    }

    private DownloadListenerAdapter mDownloadListenerAdapter = new DownloadListenerAdapter() {
        @Override
        public void onProgress(String url, long downloaded, long length, long useTime) {
            if (null != mDownloadingListener) {
                synchronized (mDownloadingListener) {
                    if (null != mDownloadingListener) {
                        mDownloadingListener.onProgress(url, downloaded, length, useTime);
                    }
                }
            }
        }

        @Override
        public void onBindService(String url, DownloadingService downloadingService) {
            if (null != mDownloadingListener) {
                synchronized (mDownloadingListener) {
                    mDownloadingListener.onBindService(url, downloadingService);
                }
            }

        }

        @Override
        public void onUnbindService(String url, DownloadingService downloadingService) {
            if (null != mDownloadingListener) {
                synchronized (mDownloadingListener) {
                    mDownloadingListener.onUnbindService(url, downloadingService);
                }
            }
        }

        @Override
        public boolean onResult(String path, String url, Throwable e) {
            ExecuteTasksMap.getInstance().removeTask(path);
            return null != mDownloadListener && mDownloadListener.onResult(path, url, e);
        }
    };


    /**
     * 静态缓存当前正在下载的任务 mUrl
     * i -> mUrl
     * i+1 -> path
     */
    static class ExecuteTasksMap extends ReentrantReadWriteLock {

        private LinkedList<String> mTasks = null;
        private static volatile ExecuteTasksMap sInstance = null;

        private ExecuteTasksMap() {
            super(false);
            mTasks = new LinkedList();
        }


        static ExecuteTasksMap getInstance() {

            if (null == sInstance) {
                synchronized (ExecuteTasksMap.class) {
                    if (null == sInstance) {
                        sInstance = new ExecuteTasksMap();
                    }
                }
            }
            return sInstance;
        }

        void removeTask(String path) {

            try {
                writeLock().lock();
                int position = -1;
                if ((position = mTasks.indexOf(path)) == -1) {
                    return;
                }
                mTasks.remove(position);
                mTasks.remove(position - 1);
            } finally {
                writeLock().unlock();
            }

        }

        void addTask(String url, String path) {

            try {
                writeLock().lock();
                mTasks.add(url);
                mTasks.add(path);
            } finally {
                writeLock().unlock();
            }
        }

        // 加锁读
        boolean contains(String url) {

            try {
                readLock().lock();
                return mTasks.contains(url);
            } finally {
                readLock().unlock();
            }

        }
    }


    public static DefaultDownloadImpl create(@NonNull Activity activity,
                                             @NonNull WebView webView,
                                             @Nullable DownloadListener downloadListener,
                                             @NonNull DownloadingListener downloadingListener,
                                             @Nullable PermissionInterceptor permissionInterceptor) {
        return new ExtraServiceImpl()
                .setActivity(activity)
                .setWebView(webView)
                .setDownloadListener(downloadListener)
                .setPermissionInterceptor(permissionInterceptor)
                .setDownloadingListener(downloadingListener)
                .create();
    }

    public static class ExtraServiceImpl extends AgentWebDownloader.ExtraService implements Cloneable, Serializable {
        private transient Activity mActivity;
        private boolean mIsForceDownload = false;
        private boolean mEnableIndicator = true;
        private transient DownloadListener mDownloadListener;
        private transient PermissionInterceptor mPermissionInterceptor;
        private boolean mIsParallelDownload = true;
        private transient WebView mWebView;
        protected int mIcon = R.drawable.ic_file_download_black_24dp;
        private DefaultDownloadImpl mDefaultDownload;
        protected String mUrl;
        protected String mUserAgent;
        protected String mContentDisposition;
        protected String mMimetype;
        protected long mContentLength;
        private boolean mIsCloneObject = false;
        private transient DownloadingListener mDownloadingListener;

        public ExtraServiceImpl setDownloadingListener(DownloadingListener downloadingListener) {
            this.mDownloadingListener = downloadingListener;
            return this;
        }

        @Override
        public boolean isForceDownload() {
            return mIsForceDownload;
        }

//        public static final int PENDDING = 1001;
//        public static final int DOWNLOADING = 1002;
//        public static final int FINISH = 1003;
//        public static final int ERROR = 1004;
//        private AtomicInteger state = new AtomicInteger(PENDDING);

        @Override
        public String getUrl() {
            return mUrl;
        }

        @Override
        protected ExtraServiceImpl setUrl(String url) {
            this.mUrl = url;
            return this;
        }

        @Override
        public String getUserAgent() {
            return mUserAgent;
        }

        @Override
        protected ExtraServiceImpl setUserAgent(String userAgent) {
            this.mUserAgent = userAgent;
            return this;
        }


        @Override
        public String getContentDisposition() {
            return mContentDisposition;
        }

        @Override
        protected ExtraServiceImpl setContentDisposition(String contentDisposition) {
            this.mContentDisposition = contentDisposition;
            return this;
        }

        @Override
        @DrawableRes
        public int getIcon() {
            return mIcon;
        }

        @Override
        public String getMimetype() {
            return mMimetype;
        }

        @Override
        protected ExtraServiceImpl setMimetype(String mimetype) {
            this.mMimetype = mimetype;
            return this;
        }

        @Override
        public long getContentLength() {
            return mContentLength;
        }

        @Override
        protected ExtraServiceImpl setContentLength(long contentLength) {
            this.mContentLength = contentLength;
            return this;
        }


        ExtraServiceImpl setActivity(Activity activity) {
            mActivity = activity;
            return this;
        }


        @Override
        public ExtraServiceImpl setForceDownload(boolean force) {
            mIsForceDownload = force;
            return this;
        }

        @Override
        public ExtraServiceImpl setEnableIndicator(boolean enableIndicator) {
            this.mEnableIndicator = enableIndicator;
            return this;
        }

        ExtraServiceImpl setDownloadListener(DownloadListener downloadListeners) {
            this.mDownloadListener = downloadListeners;
            return this;
        }

        ExtraServiceImpl setPermissionInterceptor(PermissionInterceptor permissionInterceptor) {
            mPermissionInterceptor = permissionInterceptor;
            return this;
        }

        @Override
        public ExtraServiceImpl setIcon(@DrawableRes int icon) {
            this.mIcon = icon;
            return this;
        }

        @Override
        public ExtraServiceImpl setParallelDownload(boolean parallelDownload) {
            mIsParallelDownload = parallelDownload;
            return this;
        }

        @Override
        public ExtraServiceImpl setopenbreakpointdownload(boolean openBreakPointDownload) {
            mIsOpenBreakPointDownload = openBreakPointDownload;
            return this;
        }

        ExtraServiceImpl setWebView(WebView webView) {
            this.mWebView = webView;
            return this;
        }


        @Override
        protected ExtraServiceImpl clone() throws CloneNotSupportedException {
            ExtraServiceImpl mExtraServiceImpl = (ExtraServiceImpl) super.clone();
            mExtraServiceImpl.mIsCloneObject = true;
            mExtraServiceImpl.mActivity = null;
            mExtraServiceImpl.mDownloadListener = null;
            mExtraServiceImpl.mPermissionInterceptor = null;
            mExtraServiceImpl.mWebView = null;
            return mExtraServiceImpl;
        }

        DefaultDownloadImpl create() {
            return this.mDefaultDownload = new DefaultDownloadImpl(this);
        }

        @Override
        public synchronized void performReDownload() {

            ZanL.i(TAG, "performReDownload:" + mDefaultDownload);
            if (null != this.mDefaultDownload) {
                this.mDefaultDownload
                        .onDownloadStartInternal(
                                getUrl(),
                                getUserAgent(),
                                getContentDisposition(),
                                getMimetype(),
                                getContentLength(), this);
            }
        }

    }


}
