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
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

public class ServerSettings extends Activity {
	
	private EditText serverAddr 	= null;
	private EditText serverPort 	= null;
	private EditText serverAccount 	= null;
	private EditText serverDevice 	= null;
	private Button applyButton		= null;
	
	protected Messenger mService 			= null;
	protected boolean mBound 				= false;
	protected Intent androDMTPService 	= null;
	
	private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            // This is called when the connection with the service has been
            // established, giving us the object we can use to
            // interact with the service.  We are communicating with the
            // service using a Messenger, so here we get a client-side
            // representation of that from the raw IBinder object.
        	System.out.println("attached");
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
	}
	
	private class applyListener implements OnClickListener{
		Message msg = null;
		
		@Override
		public void onClick(View v) {
			//ProgressDialog saveData = ProgressDialog.show(getApplicationContext(),"","Saving. Please wait...",true);
			
			getApplicationContext().bindService(androDMTPService, mConnection, Context.BIND_AUTO_CREATE);
			if(mBound){
				msg = Message.obtain(null, AndroDMTPMainService.MSG_SET_SRVADDR, 0, 0, serverAddr.getText().toString());
				try{
					mService.send(msg);
				}catch (Exception e) {
					
				}
				
				msg = Message.obtain(null, AndroDMTPMainService.MSG_SET_SRVPORT, 0, 0, serverPort.getText().toString());
				try{
					mService.send(msg);
				}catch (Exception e) {
					
				}
				
				msg = Message.obtain(null, AndroDMTPMainService.MSG_SET_SRVACCOUNT, 0, 0, serverAccount.getText().toString());
				try{
					mService.send(msg);
				}catch (Exception e) {
					
				}
				
				msg = Message.obtain(null, AndroDMTPMainService.MSG_SET_SRVDEVICE, 0, 0, serverDevice.getText().toString());
				try{
					mService.send(msg);
				}catch (Exception e) {
					
				}
				
				msg = Message.obtain(null, AndroDMTPMainService.MSG_SET_SRVUNIQUE, 0, 0, "");
				try{
					mService.send(msg);
				}catch (Exception e) {
					
				}
				
				msg = Message.obtain(null, AndroDMTPMainService.MSG_SET_SRVACCESS, 0, 0, "2560797743");
				try{
					mService.send(msg);
				}catch (Exception e) {
					
				}
				
				getApplicationContext().unbindService(mConnection);
			}
		}
		
	}
}
