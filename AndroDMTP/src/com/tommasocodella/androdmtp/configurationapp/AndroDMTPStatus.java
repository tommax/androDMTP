package com.tommasocodella.androdmtp.configurationapp;

import com.tommasocodella.androdmtp.R;
import com.tommasocodella.androdmtp.gps.AndroDMTPStatusLocationListener;
import com.tommasocodella.androdmtp.services.AndroDMTPMainService;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class AndroDMTPStatus extends Activity{
	private ImageView statusImageGreen;
	private ImageView statusImageRed;
	private ImageView statusImageYellow;
	protected Intent androDMTPService;
	
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.dmtpstatus);
		 
		Button startButton = (Button) findViewById(R.id.startDMTP);
		startButton.setOnClickListener(new StartStopListener());
		
		Button pauseButton = (Button) findViewById(R.id.pauseDMTP);
		pauseButton.setOnClickListener(new PauseListener());
		 
		statusImageGreen = (ImageView) findViewById(R.id.statusIconGreen);
		statusImageRed = (ImageView) findViewById(R.id.statusIconRed);
		statusImageYellow = (ImageView) findViewById(R.id.statusImageYellow);
		 
		statusImageGreen.setVisibility(ImageView.INVISIBLE);
		statusImageYellow.setVisibility(ImageView.INVISIBLE);
		statusImageRed.setVisibility(ImageView.VISIBLE);
		 	 
		startButton.setText("START");
		pauseButton.setText("PAUSE");
		pauseButton.setEnabled(false);
		 
		 
		 
		LocationManager locationManager = (LocationManager)this.getSystemService(Context.LOCATION_SERVICE);
		LocationListener locationListener = new AndroDMTPStatusLocationListener();
		
		
		androDMTPService = new Intent(this, AndroDMTPMainService.class);
		startService(androDMTPService);
		
		
		 
		((AndroDMTPStatusLocationListener)locationListener).setLatitudeNow((TextView) findViewById(R.id.latitudeNow));
		((AndroDMTPStatusLocationListener)locationListener).setLongitudeNow((TextView) findViewById(R.id.longitudeNow));
		((AndroDMTPStatusLocationListener)locationListener).setAccuracyNow((TextView) findViewById(R.id.accuracyNow));
		((AndroDMTPStatusLocationListener)locationListener).setAltitudeNow((TextView) findViewById(R.id.altitudeNow));
		((AndroDMTPStatusLocationListener)locationListener).setSpeedNow((TextView) findViewById(R.id.speedNow));
		 
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
	        
	 }

	private class StartStopListener implements OnClickListener{
		@Override
		public void onClick(View v) {
			Button startButton = (Button) findViewById(R.id.startDMTP);
			Button pauseButton = (Button) findViewById(R.id.pauseDMTP);
			
			if(statusImageGreen.getVisibility() == ImageView.INVISIBLE){
				startButton.setText("STOP");
				pauseButton.setEnabled(true);

				statusImageGreen.setVisibility(ImageView.VISIBLE);
				statusImageRed.setVisibility(ImageView.INVISIBLE);
			}else{
				startButton.setText("START");
				pauseButton.setEnabled(false);
				statusImageGreen.setVisibility(ImageView.INVISIBLE);
				statusImageRed.setVisibility(ImageView.VISIBLE);
			}
		}
		
	}
	
	private class PauseListener implements OnClickListener{
		@Override
		public void onClick(View v) {
			Button startButton = (Button) findViewById(R.id.startDMTP);
			Button pauseButton = (Button) findViewById(R.id.pauseDMTP);
			if(statusImageYellow.getVisibility() == ImageView.VISIBLE){
				pauseButton.setText("PAUSE");
				startButton.setEnabled(true);
				statusImageGreen.setVisibility(ImageView.VISIBLE);
				statusImageYellow.setVisibility(ImageView.INVISIBLE);
			}else{
				pauseButton.setText("RESUME");
				startButton.setEnabled(false);
				statusImageGreen.setVisibility(ImageView.INVISIBLE);
				statusImageYellow.setVisibility(ImageView.VISIBLE);
			}
		}
		
	}
}
