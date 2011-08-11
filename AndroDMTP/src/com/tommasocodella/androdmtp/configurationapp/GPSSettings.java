package com.tommasocodella.androdmtp.configurationapp;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

public class GPSSettings extends Activity {
	
	 @Override
	 public void onCreate(Bundle savedInstanceState){
		 super.onCreate(savedInstanceState);
		 TextView textview = new TextView(this);
		 textview.setText("This is the gps tab");
		 setContentView(textview);
	 }
}
