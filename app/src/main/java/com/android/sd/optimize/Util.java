package com.android.sd.optimize;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

import android.content.Context;
import android.content.ContextWrapper;
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
    String userName = mContext.getResources().getString(R.string.sftp_username);
    String password = mContext.getResources().getString(R.string.sftp_password);
    String host = mContext.getResources().getString(R.string.sftp_host);
    String port = mContext.getResources().getString(R.string.sftp_port);
    String workingDirectory = mContext.getResources().getString(R.string.sftp_working_directory);

    public Util(Context base) {
        mContext = base;
        dbHandler = new DBHandler(base);
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
                mChannelSftp.rm(item);
                Log.i(LOG_TAG,"file deleted");
            }
        } catch (SftpException e) {
            e.printStackTrace();
            }
    }

    @Override
    protected String doInBackground(ArrayList<String>... arrayLists) {
        connectToServer();
        try {
            for (String item: arrayLists[0]) {
                File file = new File(item);
                System.out.println(file);
                mChannelSftp.put(new FileInputStream(file), file.getName());
                Log.i(LOG_TAG, "upload done");
                removeFiles(item);
                dbHandler.deleteUploadedFile("filename", item);
                Log.i(LOG_TAG, "local file deleted");
                tobeDeleted = item;
            }
        } catch (FileNotFoundException e) {
            Log.e(LOG_TAG, "File Not found");
        } catch (SftpException e) {
            dbHandler.createNewFileNameForUpload("tobedeleted",tobeDeleted);
            dbHandler.deleteUploadedFile("filename",tobeDeleted);
            Log.e(LOG_TAG, "Error in uploading");
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

        } catch (SftpException e) {
            e.printStackTrace();
        }
    }
}