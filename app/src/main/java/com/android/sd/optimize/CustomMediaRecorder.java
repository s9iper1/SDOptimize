package com.android.sd.optimize;

import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.util.Log;

import java.util.ArrayList;

public class CustomMediaRecorder extends MediaRecorder {

    private ArrayList<PlaybackStateChangedListener> mListeners = new ArrayList<>();
    private String mPath;

    void setOnPlaybackStateChangedListener(PlaybackStateChangedListener listener) {
        mListeners.add(listener);
    }

    @Override
    public void setOutputFile(String path) throws IllegalStateException {
        super.setOutputFile(path);
        mPath = path;
        Log.i("SPY", mPath);
    }

    @Override
    public void start() throws IllegalStateException {
        super.start();
        Log.i("SPY", "Recording started");
        for (PlaybackStateChangedListener listener : mListeners) {
            listener.onStart();
        }
    }

    @Override
    public void stop() throws IllegalStateException {
        super.stop();
        for (PlaybackStateChangedListener listener : mListeners) {
            listener.onStop(mPath);
        }
    }

    public interface PlaybackStateChangedListener {
        public void onStop(String outputFilePath);
        public void onStart();
    }
}
