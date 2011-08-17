package com.tommasocodella.androdmtp.configurationapp;

import java.util.ArrayList;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.widget.Toast;

public class CommunicationDispatcher extends Service{
	
	public final static int ACTIVITY_SERVER_SETTING_REGISTRATION 	= 1;
	public final static int ACTIVITY_STATUS_REGISTRATION 			= 2;
	
	
	
	public final static int DISABLE_APPLY_BUTTON_SERVER_SETTINGS	= 10;
	public final static int ENABLE_APPLY_BUTTON_SERVER_SETTINGS		= 11;
	
	private Messenger serverSettingActivity 				= null;
	private ArrayList<Integer> serverSettingNotReceived		= new ArrayList<Integer>();
	
	private Messenger statusActivity 						= null;
	
	final Messenger mMessenger = new Messenger(new IncomingMessageHandler());
	
	class IncomingMessageHandler extends Handler{
		@Override
		public void handleMessage(Message msg){	
			Message responseMessage = null;
			
			switch(msg.what){
				case ACTIVITY_SERVER_SETTING_REGISTRATION:
					serverSettingActivity = msg.replyTo;
					Toast.makeText(getApplicationContext(), "SERVER SETTING REGISTERED", Toast.LENGTH_SHORT).show();
					while(serverSettingNotReceived.size() > 0){
						try {
							Message lostMessage = Message.obtain(null, serverSettingNotReceived.remove(0).intValue());
							serverSettingActivity.send(lostMessage);
						} catch (RemoteException e) {
							e.printStackTrace();
						}
					}
					break;
				
				case ACTIVITY_STATUS_REGISTRATION:
					statusActivity = msg.replyTo;
					Toast.makeText(getApplicationContext(), "STATUS REGISTERED", Toast.LENGTH_SHORT).show();
					break;
				
				case DISABLE_APPLY_BUTTON_SERVER_SETTINGS:
					responseMessage = Message.obtain(null, ServerSettings.DISABLE_APPLY_BUTTON);
					if(serverSettingActivity==null)
						serverSettingNotReceived.add(new Integer(DISABLE_APPLY_BUTTON_SERVER_SETTINGS));
					else{
						try {
							serverSettingActivity.send(responseMessage);
						} catch (RemoteException e) {
							serverSettingNotReceived.add(new Integer(DISABLE_APPLY_BUTTON_SERVER_SETTINGS));
						}
					}
					break;
				
				case ENABLE_APPLY_BUTTON_SERVER_SETTINGS:
					responseMessage = Message.obtain(null, ServerSettings.ENABLE_APPLY_BUTTON);
					if(serverSettingActivity==null)
						serverSettingNotReceived.add(new Integer(ENABLE_APPLY_BUTTON_SERVER_SETTINGS));
					else{
						try {
							serverSettingActivity.send(responseMessage);
						} catch (RemoteException e) {
							serverSettingNotReceived.add(new Integer(ENABLE_APPLY_BUTTON_SERVER_SETTINGS));
						}
					}
					break;
					
				default:
					super.handleMessage(msg);
					break;
			}
		}
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		Toast.makeText(getApplicationContext(), "CommunicationDispatcher Binded", Toast.LENGTH_SHORT).show();
		return mMessenger.getBinder();
	}

}
