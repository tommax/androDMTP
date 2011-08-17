package com.tommasocodella.androdmtp.configurationapp;

import com.tommasocodella.androdmtp.R;
import com.tommasocodella.androdmtp.configurationapp.ServerSettings.IncomingHandler;

import android.R.layout;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class GPSSettings extends Activity {
	
	protected Messenger mService 			= null;
	protected boolean mBound 				= false;
	
	protected Messenger dispatcherService 	= null;
	protected boolean dispatcherBound 		= false;
	
	final Messenger mMessenger 				= new Messenger(new IncomingHandler());
	protected Intent androDMTPService 		= null;
	
	
	class IncomingHandler extends Handler{
		@Override
		public void handleMessage (Message msg){
			switch(msg.what){
				default:
					super.handleMessage(msg);
					break;
			}
		}
	}
	
	
	private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            mService = new Messenger(service);
            mBound = true;
        }

        public void onServiceDisconnected(ComponentName className) {
            mService = null;
            mBound = false;
        }
    };
    
    private ServiceConnection dispatcherConnection = new ServiceConnection() {
		public void onServiceConnected(ComponentName name, IBinder service) {
			dispatcherService = new Messenger(service);
			dispatcherBound = true;
			
			Message registrationMessage = Message.obtain(null, CommunicationDispatcher.ACTIVITY_GPS_SETTING_REGISTRATION);
			registrationMessage.replyTo = mMessenger;
			try {
				dispatcherService.send(registrationMessage);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
    	
		public void onServiceDisconnected(ComponentName name) {
			dispatcherService = null;
            dispatcherBound = false;
		}
		
	};
	
	private void connectToDispatcher(){
		getApplicationContext().bindService(new Intent(this, CommunicationDispatcher.class), dispatcherConnection, Context.BIND_AUTO_CREATE);
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.dmtpgpsopts);
		
		Spinner motionSpinner = (Spinner) findViewById(R.id.motionspinner);
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.motion_type_array, android.R.layout.simple_spinner_item);
		
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		motionSpinner.setAdapter(adapter);
		
		connectToDispatcher();
	}
}
