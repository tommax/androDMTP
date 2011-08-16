package com.tommasocodella.androdmtp.configurationapp;

import com.tommasocodella.androdmtp.R;
import com.tommasocodella.androdmtp.gps.AndroDMTPStatusLocationListener;
import com.tommasocodella.androdmtp.services.AndroDMTPMainService;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class AndroDMTPStatus extends Activity{
	private ImageView statusImageGreen;
	private ImageView statusImageRed;
	private ImageView statusImageYellow;
	Messenger mService = null;
	boolean mBound;
	Intent androDMTPService;
	
	private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            // This is called when the connection with the service has been
            // established, giving us the object we can use to
            // interact with the service.  We are communicating with the
            // service using a Messenger, so here we get a client-side
            // representation of that from the raw IBinder object.
            mService = new Messenger(service);
            mBound = true;
        }

        public void onServiceDisconnected(ComponentName className) {
            // This is called when the connection with the service has been
            // unexpectedly disconnected -- that is, its process crashed.
            mService = null;
            mBound = false;
        }
    };
	
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
		
		
		/*androDMTPService = new Intent(this, AndroDMTPMainService.class);
		startService(androDMTPService);*/
		
		
		
		
		 
		((AndroDMTPStatusLocationListener)locationListener).setLatitudeNow((TextView) findViewById(R.id.latitudeNow));
		((AndroDMTPStatusLocationListener)locationListener).setLongitudeNow((TextView) findViewById(R.id.longitudeNow));
		((AndroDMTPStatusLocationListener)locationListener).setAccuracyNow((TextView) findViewById(R.id.accuracyNow));
		((AndroDMTPStatusLocationListener)locationListener).setAltitudeNow((TextView) findViewById(R.id.altitudeNow));
		((AndroDMTPStatusLocationListener)locationListener).setSpeedNow((TextView) findViewById(R.id.speedNow));
		 
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
	        
	}
	
	@Override
    protected void onStart() {
        super.onStart();


        Toast.makeText(getApplicationContext(), "AndroDMTP send binding", Toast.LENGTH_SHORT).show();
        androDMTPService = new Intent(this, AndroDMTPMainService.class);
		getApplicationContext().bindService(androDMTPService, mConnection, Context.BIND_AUTO_CREATE);
    }
	
	private class StartStopListener implements OnClickListener{
		private Message msg;
		
		@Override
		public void onClick(View v) {
			Button startButton = (Button) findViewById(R.id.startDMTP);
			Button pauseButton = (Button) findViewById(R.id.pauseDMTP);
			Button applyServerSettings = (Button) findViewById(R.id.applyserver);
			
			if(statusImageGreen.getVisibility() == ImageView.INVISIBLE){
				startButton.setText("STOP");
				pauseButton.setEnabled(true);
				if(mBound){
					msg = Message.obtain(null, AndroDMTPMainService.MSG_SAY_START, 0, 0);
					try{
						mService.send(msg);
					}catch (Exception e) {
						e.printStackTrace();
					}
				}
				
				statusImageGreen.setVisibility(ImageView.VISIBLE);
				statusImageRed.setVisibility(ImageView.INVISIBLE);
			}else{
				startButton.setText("START");
				pauseButton.setEnabled(false);
				if(mBound){
					msg = Message.obtain(null, AndroDMTPMainService.MSG_SAY_STOP, 0, 0);
					try{
						mService.send(msg);
					}catch (Exception e) {
						e.printStackTrace();
					}
				}
				
				statusImageGreen.setVisibility(ImageView.INVISIBLE);
				statusImageRed.setVisibility(ImageView.VISIBLE);
			}
		}
		
	}
	
	private class PauseListener implements OnClickListener{
		private Message msg;
		
		@Override
		public void onClick(View v) {
			Button startButton = (Button) findViewById(R.id.startDMTP);
			Button pauseButton = (Button) findViewById(R.id.pauseDMTP);
			if(statusImageYellow.getVisibility() == ImageView.VISIBLE){
				pauseButton.setText("PAUSE");
				startButton.setEnabled(true);
				if(mBound){
					msg = Message.obtain(null, AndroDMTPMainService.MSG_SAY_RESUME, 0, 0);
					try{
						mService.send(msg);
					}catch (Exception e) {
						e.printStackTrace();
					}
				}
				
				statusImageGreen.setVisibility(ImageView.VISIBLE);
				statusImageYellow.setVisibility(ImageView.INVISIBLE);
			}else{
				pauseButton.setText("RESUME");
				startButton.setEnabled(false);
				if(mBound){
					msg = Message.obtain(null, AndroDMTPMainService.MSG_SAY_PAUSE, 0, 0);
					try{
						mService.send(msg);
					}catch (Exception e) {
						e.printStackTrace();
					}
				}
				
				statusImageGreen.setVisibility(ImageView.INVISIBLE);
				statusImageYellow.setVisibility(ImageView.VISIBLE);
			}
		}
	}
	
	@Override
    protected void onStop() {
        super.onStop();
        if (mBound) {
            unbindService(mConnection);
            mBound = false;
        }
    }
}
