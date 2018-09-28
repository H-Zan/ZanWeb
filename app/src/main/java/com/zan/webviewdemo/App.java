package com.zan.webviewdemo;

import android.app.Application;
import android.content.Context;

/**
 * Created by zan on 2018/2/28.
 */

public class App extends Application {

    private static App mApp = null;

    public static App get() {
        return mApp;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mApp = this;
    }

    public Context getContext() {
        return getApplicationContext();
    }
}
