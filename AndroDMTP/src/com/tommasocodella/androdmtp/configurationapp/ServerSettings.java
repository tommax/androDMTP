package com.tommasocodella.androdmtp.configurationapp;

import com.tommasocodella.androdmtp.R;
import com.tommasocodella.androdmtp.services.AndroDMTPMainService;
import com.tommasocodella.androdmtp.services.PersistentStorage;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
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
import android.widget.SimpleCursorAdapter;
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
	
	private PersistentStorage androDMTPPersistentStorage	= null;
	
	
	class IncomingHandler extends Handler{
		@Override
		public void handleMessage (Message msg){
			switch(msg.what){
				case DISABLE_APPLY_BUTTON:
					applyButton.setEnabled(false);
					applyButton.setText("Please stop DMTP before edit server settings");
					break;
				case ENABLE_APPLY_BUTTON:
					applyButton.setEnabled(true);
					applyButton.setText("   Save settings   ");
					break;
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
		getApplicationContext().bindService(new Intent(this, CommunicationDispatcher.class), dispatcherConnection, Context.BIND_AUTO_CREATE);
	}
	
	
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.dmtpserveropts);
		androDMTPService = new Intent(this, AndroDMTPMainService.class);
		
		androDMTPPersistentStorage = new PersistentStorage(getApplicationContext());
		SQLiteDatabase androDMTPParams = androDMTPPersistentStorage.getReadableDatabase();
		
		serverAddr = (EditText) findViewById(R.id.serveraddr);
		serverPort = (EditText) findViewById(R.id.serverport);
		serverAccount = (EditText) findViewById(R.id.serveraccount);
		serverDevice = (EditText) findViewById(R.id.serverdevice);
		applyButton = (Button) findViewById(R.id.applyserver);
		
		applyButton.setOnClickListener(new ApplyListener());
		
		String columns[] = {"paramID","param"};
		
		Cursor c = androDMTPParams.query(PersistentStorage.PARAMS_TABLE, new String[]{"value"}, "paramID = " + AndroDMTPMainService.MSG_SET_SRVADDR, null, null, null, null);
		startManagingCursor(c);
		
		if(c.moveToFirst()){
			serverAddr.setText("" + c.getString(0));
		}
		
		if(serverAddr.getText().length() <= 0){
			serverAddr.setText("192.168.1.3");
		}
		
		stopManagingCursor(c);
		c.close();
		
		c = androDMTPParams.query(PersistentStorage.PARAMS_TABLE, new String[]{"value"}, "paramID = " + AndroDMTPMainService.MSG_SET_SRVPORT, null, null, null, null);
		startManagingCursor(c);
		
		if(c.moveToFirst()){
			serverPort.setText("" + c.getString(0));
		}
		
		if(serverPort.getText().length() <= 0){
			serverPort.setText("31000");
		}
		
		stopManagingCursor(c);
		c.close();
		
		c = androDMTPParams.query(PersistentStorage.PARAMS_TABLE, new String[]{"value"}, "paramID = " + AndroDMTPMainService.MSG_SET_SRVACCOUNT, null, null, null, null);
		startManagingCursor(c);
		
		if(c.moveToFirst()){
			serverAccount.setText("" + c.getString(0));
		}
		
		if(serverAccount.getText().length() <= 0){
			serverAccount.setText("unknown");
		}
		
		stopManagingCursor(c);
		c.close();
		
		c = androDMTPParams.query(PersistentStorage.PARAMS_TABLE, new String[]{"value"}, "paramID = " + AndroDMTPMainService.MSG_SET_SRVDEVICE, null, null, null, null);
		startManagingCursor(c);
		
		if(c.moveToFirst()){
			serverDevice.setText("" + c.getString(0));
		}
		
		if(serverDevice.getText().length() <= 0){
			serverDevice.setText("unknown device");
		}
		
		stopManagingCursor(c);
		c.close();
		
		connectToDispatcher();
	}
	
	private class ApplyListener implements OnClickListener{
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
