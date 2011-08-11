package com.tommasocodella.androdmtp.configurationapp;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

public class ServerSettings extends Activity {
	
	 @Override
	 public void onCreate(Bundle savedInstanceState){
		 super.onCreate(savedInstanceState);
		 TextView textview = new TextView(this);
		 textview.setText("This is the server tab");
		 setContentView(textview);
	 }
}
