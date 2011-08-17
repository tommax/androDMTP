package com.tommasocodella.androdmtp.configurationapp;

import com.tommasocodella.androdmtp.R;
import com.tommasocodella.androdmtp.services.AndroDMTPMainService;

import android.app.Activity;
import android.app.ProgressDialog;
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
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class ServerSettings extends Activity {
	
	public static final int DISABLE_APPLY_BUTTON 	= 1;
	public static final int ENABLE_APPLY_BUTTON 	= 2;
	
	private EditText serverAddr 	= null;
	private EditText serverPort 	= null;
	private EditText serverAccount 	= null;
	private EditText serverDevice 	= null;
	private Button applyButton		= null;
	
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
				case DISABLE_APPLY_BUTTON:
					applyButton.setEnabled(false);
					break;
				case ENABLE_APPLY_BUTTON:
					applyButton.setEnabled(true);
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
		public void onServiceConnected(ComponentName name, IBinder service) {
			dispatcherService = new Messenger(service);
			dispatcherBound = true;
			
			Message registrationMessage = Message.obtain(null, CommunicationDispatcher.ACTIVITY_SERVER_SETTING_REGISTRATION);
			registrationMessage.replyTo = mMessenger;
			try {
				dispatcherService.send(registrationMessage);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
    	
		@Override
		public void onServiceDisconnected(ComponentName name) {
			dispatcherService = null;
            dispatcherBound = false;
		}
		
	};
	
	private void connectToDispatcher(){
		Toast.makeText(getApplicationContext(), "SERVER SETTING: try to connect to dispatcher", Toast.LENGTH_SHORT).show();
		getApplicationContext().bindService(new Intent(this, CommunicationDispatcher.class), dispatcherConnection, Context.BIND_AUTO_CREATE);
	}
	
	
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.dmtpserveropts);
		androDMTPService = new Intent(this, AndroDMTPMainService.class);
		
		serverAddr = (EditText) findViewById(R.id.serveraddr);
		serverPort = (EditText) findViewById(R.id.serverport);
		serverAccount = (EditText) findViewById(R.id.serveraccount);
		serverDevice = (EditText) findViewById(R.id.serverdevice);
		applyButton = (Button) findViewById(R.id.applyserver);
		
		applyButton.setOnClickListener(new applyListener());
		
		serverAddr.setText("192.168.1.3");
		serverPort.setText("31000");
		serverAccount.setText("androdmtp");
		serverDevice.setText("nexus");
		
		connectToDispatcher();
	}
	
	private class applyListener implements OnClickListener{
		Message msg = null;
		
		@Override
		public void onClick(View v) {			
			getApplicationContext().bindService(androDMTPService, mConnection, Context.BIND_AUTO_CREATE);
			if(mBound){
				msg = Message.obtain(null, AndroDMTPMainService.MSG_SET_SRVADDR, 0, 0, serverAddr.getText().toString());
				try{
					mService.send(msg);
				}catch (Exception e) {}
				
				msg = Message.obtain(null, AndroDMTPMainService.MSG_SET_SRVPORT, 0, 0, serverPort.getText().toString());
				try{
					mService.send(msg);
				}catch (Exception e) {}
				
				msg = Message.obtain(null, AndroDMTPMainService.MSG_SET_SRVACCOUNT, 0, 0, serverAccount.getText().toString());
				try{
					mService.send(msg);
				}catch (Exception e) {}
				
				msg = Message.obtain(null, AndroDMTPMainService.MSG_SET_SRVDEVICE, 0, 0, serverDevice.getText().toString());
				try{
					mService.send(msg);
				}catch (Exception e) {}
				
				msg = Message.obtain(null, AndroDMTPMainService.MSG_SET_SRVUNIQUE, 0, 0, "");
				try{
					mService.send(msg);
				}catch (Exception e) {}
				
				msg = Message.obtain(null, AndroDMTPMainService.MSG_SET_SRVACCESS, 0, 0, "2560797743");
				try{
					mService.send(msg);
				}catch (Exception e) {}
				
				getApplicationContext().unbindService(mConnection);
			}
		}
		
	}
}
