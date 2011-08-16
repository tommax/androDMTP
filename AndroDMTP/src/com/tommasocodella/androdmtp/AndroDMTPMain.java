package com.tommasocodella.androdmtp;

import android.app.Activity;
import android.content.Context;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

import com.tommasocodella.androdmtp.gps.AndroDMTPLocationListener;
import com.tommasocodella.androdmtp.opendmtp.client.base.GPSModules;
import com.tommasocodella.androdmtp.opendmtp.client.base.PersistentStorage;
import com.tommasocodella.androdmtp.opendmtp.client.base.Props;
import com.tommasocodella.androdmtp.opendmtp.client.base.Protocol;
import com.tommasocodella.androdmtp.opendmtp.client.gps.GPSReceiver;
import com.tommasocodella.androdmtp.opendmtp.util.CThread;
import com.tommasocodella.androdmtp.opendmtp.util.FletcherChecksum;
import com.tommasocodella.androdmtp.opendmtp.util.GeoEvent;
import com.tommasocodella.androdmtp.opendmtp.util.Log;
import com.tommasocodella.androdmtp.opendmtp.util.StringTools;

public class AndroDMTPMain extends Activity{

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        
        
        /*
         * LocationManager locationManager = (LocationManager)this.getSystemService(Context.LOCATION_SERVICE);
        LocationListener locationListener = new AndroDMTPLocationListener();
        
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
        
        MainClass m = MainClass.getInstance((AndroDMTPLocationListener) locationListener);
        
        m.startApp();*/
        
        /*Intent intent = new Intent(this, AndroDMTPMainService.class);
        startService(intent);*/
    }
}