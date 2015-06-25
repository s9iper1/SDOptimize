package com.android.sd.optimize;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BootReceiver extends BroadcastReceiver {
	public BootReceiver() {
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO: This method is called when the BroadcastReceiver is receiving
		// an Intent broadcast.
		AlarmManager alarmManager = (AlarmManager) context.getSystemService(Service.ALARM_SERVICE);
		alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME, 0, 60 * 1000, PendingIntent
				.getService(context, 0, new Intent(context, HandlerService.class),
						PendingIntent.FLAG_CANCEL_CURRENT));
	}
}
