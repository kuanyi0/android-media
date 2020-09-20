package com.yikuan.androidmedia.app;

import android.app.Application;

import com.yikuan.androidcommon.AndroidCommon;

/**
 * @author yikuan
 * @date 2020/09/17
 */
public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        AndroidCommon.init(this);
    }
}
