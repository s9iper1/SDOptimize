package com.android.sd.optimize;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.io.File;
import java.io.IOException;

public class DeleteDataReceiver extends BroadcastReceiver {

    private final String LOG_TAG = AppGlobals.getLogTag(getClass());

    @Override
    public void onReceive(Context context, Intent intent) {
        DBHandler dbHandler = new DBHandler(context);
        String fileName = intent.getStringExtra("FILE_NAME");
        dbHandler.deleteUploadedFile("filename", fileName);
        Log.i(LOG_TAG, "Delete from DB");
        removeFiles(fileName);
        Log.i(LOG_TAG, "local file deleted");
    }

    void removeFiles(String path) {
        File file = new File(path);
        if (file.exists()) {
            String deleteCmd = "rm -r " + path;
            Runtime runtime = Runtime.getRuntime();
            try {
                runtime.exec(deleteCmd);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
