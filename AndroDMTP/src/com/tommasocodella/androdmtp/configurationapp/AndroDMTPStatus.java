package com.tommasocodella.androdmtp.configurationapp;

import com.tommasocodella.androdmtp.R;
import com.tommasocodella.androdmtp.configurationapp.ServerSettings.IncomingHandler;
import com.tommasocodella.androdmtp.gps.AndroDMTPStatusLocationListener;
import com.tommasocodella.androdmtp.services.AndroDMTPMainService;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class AndroDMTPStatus extends Activity{
	private ImageView statusImageGreen 	= null;
	private ImageView statusImageRed 	= null;
	private ImageView statusImageYellow = null;
	Messenger mService 					= null;
	boolean mBound 						= false;
	static boolean isStarted			= false;
	Intent androDMTPService 			= null;
	
	protected Messenger dispatcherService 	= null;
	protected boolean dispatcherBound 		= false;
	final Messenger mMessenger 				= new Messenger(new IncomingHandler());
	
	class IncomingHandler extends Handler{
		@Override
		public void handleMessage (Message msg){
			switch(msg.what){
				case 1:
					break;
				default:
					super.handleMessage(msg);
					break;
			}
		}
	}
	
	
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
    
    private ServiceConnection dispatcherConnection = new ServiceConnection() {
		
		@Override
		public void onServiceDisconnected(ComponentName name) {
			dispatcherService = null;
            dispatcherBound = false;
		}
		
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			dispatcherService = new Messenger(service);
			dispatcherBound = true;
			Message registrationMessage = Message.obtain(null, CommunicationDispatcher.ACTIVITY_STATUS_REGISTRATION);
			registrationMessage.replyTo = mMessenger;
			try {
				dispatcherService.send(registrationMessage);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
	};
	
	private void connectToDispatcher(){
		getApplicationContext().bindService(new Intent(this, CommunicationDispatcher.class), dispatcherConnection, Context.BIND_AUTO_CREATE);
	}
	
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
        androDMTPService = new Intent(this, AndroDMTPMainService.class);
		getApplicationContext().bindService(androDMTPService, mConnection, Context.BIND_AUTO_CREATE);
		connectToDispatcher();
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
				
				if(dispatcherBound){
					Message disableSave = Message.obtain(null, CommunicationDispatcher.DISABLE_APPLY_BUTTON_SERVER_SETTINGS);
					try {
						dispatcherService.send(disableSave);
					} catch (RemoteException e) {
						e.printStackTrace();
					}
					
					disableSave = Message.obtain(null, CommunicationDispatcher.DISABLE_APPLY_BUTTON_GPS_SETTINGS);
					try {
						dispatcherService.send(disableSave);
					} catch (RemoteException e) {
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
				
				if(dispatcherBound){
					Message disableSave = Message.obtain(null, CommunicationDispatcher.ENABLE_APPLY_BUTTON_SERVER_SETTINGS);
					try {
						dispatcherService.send(disableSave);
					} catch (RemoteException e) {
						e.printStackTrace();
					}
					
					disableSave = Message.obtain(null, CommunicationDispatcher.ENABLE_APPLY_BUTTON_GPS_SETTINGS);
					try {
						dispatcherService.send(disableSave);
					} catch (RemoteException e) {
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
	
}
