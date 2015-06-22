package com.android.sd.optimize;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class HandlerService extends Service {
	public HandlerService() {
	}

	@Override
	public void onCreate() {
		if (!BackService.isRunning) {
			startService(new Intent(this, BackService.class));
			stopSelf();
		}
	}

	@Override
	public IBinder onBind(Intent intent) {
		// TODO: Return the communication channel to the service.
		throw new UnsupportedOperationException("Not yet implemented");
	}
}
