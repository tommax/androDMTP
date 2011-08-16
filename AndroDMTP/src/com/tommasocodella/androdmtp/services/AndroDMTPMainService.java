package com.tommasocodella.androdmtp.services;

import com.tommasocodella.androdmtp.gps.AndroDMTPLocationListener;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.IBinder;
import android.widget.Toast;

public class AndroDMTPMainService extends Service {
	
	private AndroDMTP dmtp = null;
	
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public void onCreate() {
		Toast.makeText(this, "AndroDMTP instantiated", Toast.LENGTH_SHORT).show();
		
		LocationManager locationManager = (LocationManager)this.getSystemService(Context.LOCATION_SERVICE);
        LocationListener locationListener = new AndroDMTPLocationListener();
        
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
        
        
        dmtp = AndroDMTP.getInstance((AndroDMTPLocationListener) locationListener);
        
        dmtp.startApp();
		
	}
	
	@Override
	public void onDestroy(){
		dmtp.exitApp();
	}
	  
	public int onStartCommand(Intent intent, int flags, int startId) {
		Toast.makeText(this, "service starting", Toast.LENGTH_SHORT).show();
		// If we get killed, after returning from here, restart
		return 0;
	}

}
