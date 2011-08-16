package com.tommasocodella.androdmtp.configurationapp;

import com.tommasocodella.androdmtp.R;
import android.app.TabActivity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.widget.TabHost;

public class ConfigurationApp extends TabActivity {
	
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		
		Resources res = getResources(); // Resource object to get Drawables
		TabHost tabHost = getTabHost();  // The activity TabHost
		TabHost.TabSpec spec;  // Resusable TabSpec for each tab
		Intent intent;  // Reusable Intent for each tab
		
		// Create an Intent to launch an Activity for the tab (to be reused)
		intent = new Intent().setClass(this, AndroDMTPStatus.class);
				
		// Initialize a TabSpec for each tab and add it to the TabHost
		spec = tabHost.newTabSpec("status").setIndicator("Status", res.getDrawable(R.drawable.ic_tab_info)).setContent(intent);
		tabHost.addTab(spec);
		
		// Create an Intent to launch an Activity for the tab (to be reused)
		intent = new Intent().setClass(this, GPSSettings.class);
		
		// Initialize a TabSpec for each tab and add it to the TabHost
		spec = tabHost.newTabSpec("gps").setIndicator("GPS", res.getDrawable(R.drawable.ic_tab_gps)).setContent(intent);
		tabHost.addTab(spec);
		
		// Do the same for the other tabs
		intent = new Intent().setClass(this, ServerSettings.class);
		spec = tabHost.newTabSpec("server").setIndicator("SERVER", res.getDrawable(R.drawable.ic_tab_server)).setContent(intent);
		tabHost.addTab(spec);

		intent = new Intent().setClass(this, ComunicationSettings.class);
		spec = tabHost.newTabSpec("communication").setIndicator("COM", res.getDrawable(R.drawable.ic_tab_com)).setContent(intent);
		tabHost.addTab(spec);
		
		tabHost.setCurrentTab(0);
	}
	
	@Override
    protected void onStop() {
        super.onStop();
        finish();
    }
}
