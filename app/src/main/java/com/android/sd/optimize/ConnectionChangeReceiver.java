package com.android.sd.optimize;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class ConnectionChangeReceiver extends BroadcastReceiver {

    private String LOG_TAG = AppGlobals.getLogTag(getClass());
    DBHandler dbHandler;

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(LOG_TAG, "receiver is UP.....");
        Helpers helpers = new Helpers(context);
        dbHandler = new DBHandler(context);
        new Thread(helpers).start();
    }
}

