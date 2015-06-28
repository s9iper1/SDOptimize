package com.android.sd.optimize;

import android.app.Application;
import android.content.Context;

public class AppGlobals extends Application {

    private static final String LOG_TAG = "SDO";
    private static Context sContext;

    @Override
    public void onCreate() {
        super.onCreate();
        sContext = getApplicationContext();
    }

    static String getLogTag(Class aClass) {
        return LOG_TAG + "/" + aClass.getSimpleName();
    }

    static Context getContext() {
        return sContext;
    }
}
