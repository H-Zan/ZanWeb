package com.zan.webviewdemo;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.google.gson.Gson;
import com.zan.webviewdemo.downloader.DefaultDownloadImpl;
import com.zan.webviewdemo.util.ToastUtil;
import com.zan.webviewdemo.util.Utils;
import com.zan.webviewdemo.util.ZanL;
import com.zan.webviewdemo.webbase.PermissionInterceptor;
import com.zan.webviewdemo.wedgit.webview.NestedScrollWebView;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class MainActivity extends AppCompatActivity implements NestedScrollWebView.OnScrollChangeListener {

    private NestedScrollWebView mWebView;

    private LinearLayout mClAll;
    //    private CoordinatorLayout mClAll;
    private LinearLayout mLlToolbar;

    private AlertDialog mAlertDialog;
    private ImageView mImgBack;
    private ImageView mImgForward;
    private EditText mEtUrl;
    private RelativeLayout mRlBottomBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        loadUrl(C.URL.BING);
//        loadUrl(C.URL.ALI_PAY);
    }

    private void initView() {
        mWebView = findViewById(R.id.wbv_main_ac);
//        mWebView = new NestedScrollWebView(this);
        mClAll = findViewById(R.id.cl_all);
//        mClAll = findViewById(R.id.cl_all);
        mLlToolbar = findViewById(R.id.ll_toolbar);
        mImgBack = findViewById(R.id.img_back);
        mImgForward = findViewById(R.id.img_forward);
        mEtUrl = findViewById(R.id.et_url);
        mRlBottomBar = findViewById(R.id.rl_bottom_bar);
        mWebView.setOnScrollChangeListener(this);
        initWebView();
    }

    private void initWebView() {

        /*
        CoordinatorLayout.LayoutParams lp = new CoordinatorLayout.LayoutParams(-1, -1);
        lp.setBehavior(new AppBarLayout.ScrollingViewBehavior());
        mClAll.addView(mWebView,lp);
        mLlToolbar.bringToFront();
        */

//        mWebView.bringToFront();
//        mLlToolbar.bringToFront();

        WebSettings settings = mWebView.getSettings();
        settings.setUseWideViewPort(true); //将图片调整到适合webview的大小
        settings.setLoadWithOverviewMode(true); // 缩放至屏幕的大小
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            settings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        }
        /*// 打开页面时， 自适应屏幕
        settings.setUseWideViewPort(true); //将图片调整到适合webview的大小
		settings.setLoadWithOverviewMode(true); // 缩放至屏幕的大小
		settings.setDefaultTextEncodingName("utf-8");
		settings.setAppCacheEnabled(true);
		*/
        settings.setJavaScriptEnabled(true);
        settings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);

        settings.setDomStorageEnabled(true);//打开DOM存储API
//        settings.setUseWideViewPort(true);
//        settings.setLoadWithOverviewMode(true);
//        settings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.NORMAL);

        mWebView.setWebViewClient(new MyWebViewClient());
        mWebView.setWebChromeClient(new MyWebChromeClient());
//        mWebView.setWebViewClient(mWebViewClient);
//        mWebView.setWebChromeClient(mWebChromeClient);
//        mWebView.setFocusable(false);
        mWebView.setDownloadListener(DefaultDownloadImpl
                .create(this,
                        mWebView,
                        null,
                        null,
                        mPermissionInterceptor));
    }

    private String TAG = "mainac";
    protected PermissionInterceptor mPermissionInterceptor = new PermissionInterceptor() {

        /**
         * PermissionInterceptor 能达到 url1 允许授权， url2 拒绝授权的效果。
         * AgentWeb 是用自己的权限机制的 ，true 该Url对应页面请求定位权限拦截 ，false 默认允许。
         * @param url
         * @param permissions
         * @param action
         * @return
         */
        @Override
        public boolean intercept(String url, String[] permissions, String action) {
            Gson mGson = new Gson();
            Log.i(TAG, "mUrl:" + url + "  permission:" + mGson.toJson(permissions) + " action:" + action);
            return false;
        }
    };

    private void loadUrl(String url) {
        if (mWebView != null) {
            mWebView.loadUrl(url);
        }
    }

    private void reLoad() {
        if (mWebView != null) {
            mWebView.reload();
        }
    }

    private void goBack() {
        if (mWebView != null && mWebView.canGoBack()) {
            mWebView.goBack();
        }
    }

    private void goForward() {
        if (mWebView != null && mWebView.canGoForward()) {
            mWebView.goForward();
        }
    }

    public void onBottomBarClick(View view) {
        switch (view.getId()) {
            case R.id.img_back:
                goBack();
                break;
            case R.id.img_forward:
                goForward();
                break;
            case R.id.img_refresh:
                if (!Utils.isMultiClick()) {
                    reLoad();
                }
                break;

            case R.id.img_out:
                if (!Utils.isMultiClick() && mWebView != null) {
                    openBrowser(mWebView.getUrl());
                }
                break;
            case R.id.img_close:
//                mWebView.stopLoading();
                showDialog();
                break;
        }
    }

    //webview滑动回调
    @Override
    public void onPageEnd(int l, int t, int oldl, int oldt) {
//        ToastUtil.showAboveView("onPageEnd",mRlBottomBar);
    }

    @Override
    public void onPageTop(int l, int t, int oldl, int oldt) {
//        ToastUtil.showAboveView("onPageTop",mRlBottomBar);

    }

    @Override
    public void onScrollChanged(int l, int t, int oldl, int oldt) {

    }

    public void goWeb(View view) {
        String url = mEtUrl.getText().toString().trim();
        if (!"".equals(url)) {
            if (isUrl(url)) {
                loadUrl(url);
            } else {
                ToastUtil.showAboveView("请输入正确的网址!", mRlBottomBar);
            }
        } else {
            ToastUtil.showAboveView("请输入网址!", mRlBottomBar);
        }
    }

    /**
     * 验证字符串是否为网址
     */
    public static boolean isUrl(String url) {
        String regex = "^(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]";
        Pattern patt = Pattern.compile(regex);
        Matcher matcher = patt.matcher(url);
        return matcher.matches();
    }

    //webview
    private class MyWebViewClient extends WebViewClient {
        boolean loadingFinished = true;
        boolean redirect = false;
        boolean isError = false;
        boolean isSuccess = false;

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String urlNewString) {
            if (!loadingFinished) {
                redirect = true;
            }
            try {
                if (urlNewString.startsWith("weixin://") //微信
                        || urlNewString.startsWith("alipays://") //支付宝
                        || urlNewString.startsWith("mailto://") //邮件
                        || urlNewString.startsWith("tel://")//电话
                        || urlNewString.startsWith("dianping://")//大众点评
                    //其他自定义的scheme
                        ) {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(urlNewString));
                    startActivity(intent);
                    return true;
                }
            } catch (Exception e) { //防止crash (如果手机上没有安装处理某个scheme开头的url的APP, 会导致crash)
                return true;//没有安装该app时，返回true，表示拦截自定义链接，但不跳转，避免弹出上面的错误页面
            }

            loadingFinished = false;
            view.loadUrl(urlNewString);
            return true;
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
            loadingFinished = false;
            //SHOW LOADING IF IT ISNT ALREADY VISIBLE
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            if (!redirect) {
                loadingFinished = true;
            }

            if (!isError) {
                ZanL.e("webview", "成功1");
                if (loadingFinished && !redirect) {
                    //HIDE LOADING IT HAS FINISHED
//                    onWebFinished(true);
                    //回调成功后的相关操作
                    ZanL.e("webview", "成功2");
                } else {
                    redirect = false;
                }
                isSuccess = true;
            }
            isError = false;

            if (mWebView != null && mWebView.canGoBack()) {
                mImgBack.setEnabled(true);
            } else {
                mImgBack.setEnabled(false);
            }
            if (mWebView != null && mWebView.canGoForward()) {
                mImgForward.setEnabled(true);
            } else {
                mImgForward.setEnabled(false);
            }
            setEtUrlSelection(url);
        }

        @Override
        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
            super.onReceivedError(view, errorCode, description, failingUrl);
            isError = true;
            isSuccess = false;
            //回调失败的相关操作
//            onWebFinished(false);
            ToastUtil.showNormal("网页飞了！");
            ZanL.e("网页飞了", description);
        }
        /*@Override
        public void onPageFinished(WebView view, String artiUrl) {
			super.onPageFinished(view, artiUrl);
			onWebFinished();
		}*/
    }

    private void setEtUrlSelection(String url) {
        if (url != null && !"".equals(url)) {
            mEtUrl.setText(url);
            mEtUrl.setSelection(url.length());
        }
    }

    private class MyWebChromeClient extends WebChromeClient {
        @Override
        public void onProgressChanged(WebView view, int newProgress) {
            super.onProgressChanged(view, newProgress);
        }

    }

//    private WebViewClient mWebViewClient = new WebViewClient() {
//        @Override
//        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
//            return super.shouldOverrideUrlLoading(view, request);
//        }
//
//        @Override
//        public void onPageStarted(WebView view, String url, Bitmap favicon) {
//            //do you  work
//            Log.i("Info", "BaseWebActivity onPageStarted");
//        }
//    };
//    private WebChromeClient mWebChromeClient = new WebChromeClient() {
//        @Override
//        public void onProgressChanged(WebView view, int newProgress) {
//            //do you work
////            Log.i("Info","onProgress:"+newProgress);
//        }
//
//        @Override
//        public void onReceivedTitle(WebView view, String title) {
//            super.onReceivedTitle(view, title);
//            /*if (mTitleTextView != null) {
//                mTitleTextView.setText(title);
//            }*/
//        }
//    };

    @Override
    public void onBackPressed() {
        // super.onBackPressed();
        webBack();
    }

    private void webBack() {
        try {
            if (mWebView != null && mWebView.canGoBack()) {
                mWebView.goBack();
            } else {
                showDialog();
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    private void clearWebView() {
        if (mWebView != null) {

            mWebView.stopLoading();
            mWebView.clearCache(false);
            mWebView.clearHistory();
            mWebView.freeMemory();
            mClAll.removeView(mWebView);
            mWebView.removeAllViews();
            mWebView.destroy();
            mWebView = null;
        }
    }

    private void showDialog() {

        if (mAlertDialog == null) {
            mAlertDialog = new AlertDialog.Builder(this)
                    .setMessage("您确定要退出吗?")
                    .setNegativeButton("再逛逛", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (mAlertDialog != null) {
                                mAlertDialog.dismiss();
                            }
                        }
                    })
                    .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            if (mAlertDialog != null) {
                                mAlertDialog.dismiss();
                                mAlertDialog = null;
                            }
                            closeApp();
                        }
                    }).create();
        }
        mAlertDialog.show();
        mAlertDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.parseColor("#c1c1c1"));
        mAlertDialog.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(Color.parseColor("#6EC8FE"));
    }

    private void closeApp() {
        clearWebView();
        finish();
    }

    /**
     * 打开浏览器
     *
     * @param targetUrl 外部浏览器打开的地址
     */
    private void openBrowser(String targetUrl) {
        if (TextUtils.isEmpty(targetUrl) || targetUrl.startsWith("file://")) {
            ToastUtil.showNormal("该链接无法使用浏览器打开~");
            return;
        }
        Intent intent = new Intent();
        intent.setAction("android.intent.action.VIEW");
        Uri mUri = Uri.parse(targetUrl);
        intent.setData(mUri);
        startActivity(intent);
    }

}
