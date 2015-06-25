package com.android.sd.optimize;

import android.app.Application;
import android.content.Context;

public class AppGlobals extends Application {

    private static Context sContext;
    private static final String LOG_TAG = "SDO";
    static final int STOPPED_AFTER_TIME = 101;
    static final int STOPPED_WITH_DIRECT_CALL = 102;

    @Override
    public void onCreate() {
        super.onCreate();
        sContext = getApplicationContext();
    }

    static Context getContext() {
        return sContext;
    }

    static String getLogTag(Class aClass) {
        return LOG_TAG + "/" + aClass.getSimpleName();
    }
}
