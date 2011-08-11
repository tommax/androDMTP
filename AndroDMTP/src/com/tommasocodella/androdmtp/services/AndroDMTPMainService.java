package com.tommasocodella.androdmtp.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.widget.Toast;

public class AndroDMTPMainService extends Service {
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public void onCreate() {
		Toast.makeText(this, "service starting", Toast.LENGTH_SHORT).show();
	}
	  
	public int onStartCommand(Intent intent, int flags, int startId) {
		Toast.makeText(this, "service starting", Toast.LENGTH_SHORT).show();
		// If we get killed, after returning from here, restart
		return 0;
	}

}
