package com.android.sd.optimize;

import android.app.Application;

public class AppGlobals extends Application {

    private static final String LOG_TAG = "SDO";

    @Override
    public void onCreate() {
        super.onCreate();
    }

    static String getLogTag(Class aClass) {
        return LOG_TAG + "/" + aClass.getSimpleName();
    }
}
