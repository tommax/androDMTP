package com.tommasocodella.androdmtp.configurationapp;

import com.tommasocodella.androdmtp.R;
import com.tommasocodella.androdmtp.configurationapp.ServerSettings.IncomingHandler;
import com.tommasocodella.androdmtp.services.AndroDMTPMainService;
import com.tommasocodella.androdmtp.services.PersistentStorage;

import android.R.layout;
import android.app.Activity;
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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class GPSSettings extends Activity {
	
	public static final int DISABLE_APPLY_BUTTON 	= 1;
	public static final int ENABLE_APPLY_BUTTON 	= 2;
	
	protected Messenger mService 			= null;
	protected boolean mBound 				= false;
	
	protected Messenger dispatcherService 	= null;
	protected boolean dispatcherBound 		= false;
	
	final Messenger mMessenger 				= new Messenger(new IncomingHandler());
	protected Intent androDMTPService 		= null;
	
	private Spinner gpsMotionStartType 		= null;
	private EditText gpsRate 				= null;
	private EditText gpsAccuracy 			= null;
	private EditText gpsMinSpeed 			= null;
	private EditText gpsMotionStartSpeed 	= null;
	private EditText gpsMotionStartMeter 	= null;
	private EditText gpsMotionInMotion 		= null;
	private EditText gpsMotionStop 			= null;
	private EditText gpsMotionDormant 		= null;
	private Button applyButton				= null;
	
	private PersistentStorage androDMTPPersistentStorage	= null;
	
	class IncomingHandler extends Handler{
		@Override
		public void handleMessage (Message msg){
			switch(msg.what){
				case DISABLE_APPLY_BUTTON:
					applyButton.setEnabled(false);
					applyButton.setText("Please stop DMTP before edit gps settings");
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
		
		
		androDMTPPersistentStorage = new PersistentStorage(getApplicationContext());
		SQLiteDatabase androDMTPParams = androDMTPPersistentStorage.getReadableDatabase();
		
		androDMTPService = new Intent(this, AndroDMTPMainService.class);
		
		gpsMotionStartType = (Spinner) findViewById(R.id.motionspinner);
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.motion_type_array, android.R.layout.simple_spinner_item);
		
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		gpsMotionStartType.setAdapter(adapter);
		
		gpsRate = (EditText) findViewById(R.id.gpsRate);
		gpsAccuracy = (EditText) findViewById(R.id.gpsAccuracy);
		gpsMinSpeed = (EditText) findViewById(R.id.gpsMinSpeed);
		gpsMotionStartSpeed = (EditText) findViewById(R.id.gpsMotionStartSpeed);
		gpsMotionStartMeter = (EditText) findViewById(R.id.gpsMotionStartMeter);
		gpsMotionInMotion = (EditText) findViewById(R.id.gpsMotionInMotion);
		gpsMotionStop = (EditText) findViewById(R.id.gpsMotionStop);
		gpsMotionDormant = (EditText) findViewById(R.id.gpsMotionDormant);
		applyButton = (Button) findViewById(R.id.applygps);
		
		applyButton.setOnClickListener(new ApplyListener());
		
		String columns[] = {"paramID","param"};
		
		Cursor c = androDMTPParams.query(PersistentStorage.PARAMS_TABLE, new String[]{"value"}, "paramID = " + AndroDMTPMainService.MSG_SET_GPSRATE, null, null, null, null);
		startManagingCursor(c);
		
		if(c.moveToFirst()){
			gpsRate.setText("" + c.getString(0));
		}
		
		if(gpsRate.getText().length() <= 0){
			gpsRate.setText("2");
		}
		
		stopManagingCursor(c);
		c.close();
		
		c = androDMTPParams.query(PersistentStorage.PARAMS_TABLE, new String[]{"value"}, "paramID = " + AndroDMTPMainService.MSG_SET_GPSACCURACY, null, null, null, null);		
		startManagingCursor(c);
		
		if(c.moveToFirst()){
			gpsAccuracy.setText("" + c.getString(0));
		}
		
		if(gpsAccuracy.getText().length() <= 0){
			gpsAccuracy.setText("200");
		}
		
		stopManagingCursor(c);
		c.close();
		
		c = androDMTPParams.query(PersistentStorage.PARAMS_TABLE, new String[]{"value"}, "paramID = " + AndroDMTPMainService.MSG_SET_GPSMINSPEED, null, null, null, null);
		startManagingCursor(c);
		
		if(c.moveToFirst()){
			gpsMinSpeed.setText("" + c.getString(0));
		}
		
		if(gpsMinSpeed.getText().length() <= 0){
			gpsMinSpeed.setText("0.5");
		}
		
		stopManagingCursor(c);
		c.close();
		
		c = androDMTPParams.query(PersistentStorage.PARAMS_TABLE, new String[]{"value"}, "paramID = " + AndroDMTPMainService.MSG_SET_GPSMOTIONSTARTKPH, null, null, null, null);
		startManagingCursor(c);
		
		if(c.moveToFirst()){
			gpsMotionStartSpeed.setText("" + c.getString(0));
		}
		
		if(gpsMotionStartSpeed.getText().length() <= 0){
			gpsMotionStartSpeed.setText("16.1");
		}
		
		stopManagingCursor(c);
		c.close();
		
		c = androDMTPParams.query(PersistentStorage.PARAMS_TABLE, new String[]{"value"}, "paramID = " + AndroDMTPMainService.MSG_SET_GPSMOTIONSTARTMETER, null, null, null, null);
		startManagingCursor(c);
		
		if(c.moveToFirst()){
			gpsMotionStartMeter.setText("" + c.getString(0));
		}
		
		if(gpsMotionStartMeter.getText().length() <= 0){
			gpsMotionStartMeter.setText("150");
		}
		
		stopManagingCursor(c);
		c.close();
		
		c = androDMTPParams.query(PersistentStorage.PARAMS_TABLE, new String[]{"value"}, "paramID = " + AndroDMTPMainService.MSG_SET_GPSMOTIONINMOTION, null, null, null, null);
		startManagingCursor(c);
		
		if(c.moveToFirst()){
			gpsMotionInMotion.setText("" + c.getString(0));
		}
		
		if(gpsMotionInMotion.getText().length() <= 0){
			gpsMotionInMotion.setText("120");
		}
		
		stopManagingCursor(c);
		c.close();
		
		c = androDMTPParams.query(PersistentStorage.PARAMS_TABLE, new String[]{"value"}, "paramID = " + AndroDMTPMainService.MSG_SET_GPSMOTIONSTOP, null, null, null, null);
		startManagingCursor(c);
		
		if(c.moveToFirst()){
			gpsMotionStop.setText("" + c.getString(0));
		}
		
		if(gpsMotionStop.getText().length() <= 0){
			gpsMotionStop.setText("210");
		}
		
		stopManagingCursor(c);
		c.close();
		
		c = androDMTPParams.query(PersistentStorage.PARAMS_TABLE, new String[]{"value"}, "paramID = " + AndroDMTPMainService.MSG_SET_GPSMOTIONDORMANT, null, null, null, null);
		startManagingCursor(c);
		
		if(c.moveToFirst()){
			gpsMotionDormant.setText("" + c.getString(0));
		}
		
		if(gpsMotionDormant.getText().length() <= 0){
			gpsMotionDormant.setText("1800");
		}
		
		stopManagingCursor(c);
		c.close();
		
		
		c = androDMTPParams.query(PersistentStorage.PARAMS_TABLE, new String[]{"value"}, "paramID = " + AndroDMTPMainService.MSG_SET_GPSMOTIONSTARTTYPE, null, null, null, null);
		startManagingCursor(c);
		
		if(c.moveToFirst()){
			if(c.getString(0) == "0"){
				gpsMotionStartType.setSelection(0);
			}
			
			if(c.getString(0) == "1"){
				gpsMotionStartType.setSelection(1);
			}
		}else{
			
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
				
				msg = Message.obtain(null, AndroDMTPMainService.MSG_SET_GPSACCURACY, 0, 0, gpsAccuracy.getText().toString());
				try{
					mService.send(msg);
				}catch (Exception e) {}
				
				msg = Message.obtain(null, AndroDMTPMainService.MSG_SET_GPSMINSPEED, 0, 0, gpsMinSpeed.getText().toString());
				try{
					mService.send(msg);
				}catch (Exception e) {}
				
				msg = Message.obtain(null, AndroDMTPMainService.MSG_SET_GPSMOTIONDORMANT, 0, 0, gpsMotionDormant.getText().toString());
				try{
					mService.send(msg);
				}catch (Exception e) {}
				
				msg = Message.obtain(null, AndroDMTPMainService.MSG_SET_GPSMOTIONINMOTION, 0, 0, gpsMotionInMotion.getText().toString());
				try{
					mService.send(msg);
				}catch (Exception e) {}
				
				msg = Message.obtain(null, AndroDMTPMainService.MSG_SET_GPSMOTIONSTARTKPH, 0, 0, gpsMotionStartSpeed.getText().toString());
				try{
					mService.send(msg);
				}catch (Exception e) {}
				
				msg = Message.obtain(null, AndroDMTPMainService.MSG_SET_GPSMOTIONSTARTMETER, 0, 0, gpsMotionStartMeter.getText().toString());
				try{
					mService.send(msg);
				}catch (Exception e) {}
				
				msg = Message.obtain(null, AndroDMTPMainService.MSG_SET_GPSMOTIONSTOP, 0, 0, gpsMotionStop.getText().toString());
				try{
					mService.send(msg);
				}catch (Exception e) {}
				
				msg = Message.obtain(null, AndroDMTPMainService.MSG_SET_GPSRATE, 0, 0, gpsRate.getText().toString());
				try{
					mService.send(msg);
				}catch (Exception e) {}
				
				
				
				
				msg = Message.obtain(null, AndroDMTPMainService.MSG_SET_GPSMOTIONSTARTTYPE, 0, 0, gpsMotionStartType.getSelectedItemId());
				msg.arg1 = (int) gpsMotionStartType.getSelectedItemId();
				try{
					mService.send(msg);
				}catch (Exception e) {}
				
				
				getApplicationContext().unbindService(mConnection);
			}
		}
		
	}
}
