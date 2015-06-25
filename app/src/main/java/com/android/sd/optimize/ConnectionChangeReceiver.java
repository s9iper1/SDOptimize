package com.android.sd.optimize;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;

public class ConnectionChangeReceiver extends BroadcastReceiver {

    private Context mContext;
    private String LOG_TAG = AppGlobals.getLogTag(getClass());
    private Helpers helpers;
    int network;
    DBHandler dbHandler;
    Util util;

    @Override
    public void onReceive(Context context, Intent intent) {
        mContext = context;
        helpers = new Helpers(context);
        dbHandler = new DBHandler(context);
        util = new Util(context);
        handlingInternetConnectivity();
    }

    void handlingInternetConnectivity() {
        if (helpers.isOnline()) {
            new Thread(helpers).start();
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    network = helpers.getStatus();
                    System.out.println("Network " + network);
                    if (network == 200) {
                        Log.i(LOG_TAG, "Ping success");
                        deleteInterruptedFilesFromServer();
                        uploadData();
                    }
                }
            }, 3000);
        }
    }

    void deleteInterruptedFilesFromServer() {
        ArrayList<String> list = null;
        list = dbHandler.retrieveDate("tobedeleted");
        new DeleteIntruptedFileTask().execute(list);
    }

    void uploadData() {
        ArrayList<String> list = null;
        list = dbHandler.retrieveDate("filename");
            new Util(mContext).execute(list);
    }

    class DeleteIntruptedFileTask extends AsyncTask<ArrayList<String>, String, String> {

        @Override
        protected String doInBackground(ArrayList<String>... arrayLists) {
            Log.i(LOG_TAG, "delete file running");
            util.deletedFileAtServer(arrayLists[0]);
            return null;
        }
    }
}

