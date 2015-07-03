package com.android.sd.optimize;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class ConnectionChangeReceiver extends BroadcastReceiver {

    private String LOG_TAG = AppGlobals.getLogTag(getClass());

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(LOG_TAG, "receiver is UP.....");
        Intent uploadIntent = new Intent(context, Util.class);
        context.startService(uploadIntent);
    }
}

