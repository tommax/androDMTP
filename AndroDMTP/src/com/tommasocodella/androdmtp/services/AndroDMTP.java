package com.tommasocodella.androdmtp.services;

import com.tommasocodella.androdmtp.gps.AndroDMTPLocationListener;
import com.tommasocodella.androdmtp.gps.GPSUtils;
import com.tommasocodella.androdmtp.opendmtp.client.base.GPSModules;
import com.tommasocodella.androdmtp.opendmtp.client.base.Packet;
import com.tommasocodella.androdmtp.opendmtp.client.base.PacketQueue;
import com.tommasocodella.androdmtp.opendmtp.client.base.PersistentStorage;
import com.tommasocodella.androdmtp.opendmtp.client.base.Props;
import com.tommasocodella.androdmtp.opendmtp.client.base.Protocol;
import com.tommasocodella.androdmtp.opendmtp.client.modules.MotionModule;
import com.tommasocodella.androdmtp.opendmtp.client.modules.OdometerModule;
import com.tommasocodella.androdmtp.opendmtp.codes.StatusCodes;
import com.tommasocodella.androdmtp.opendmtp.util.CThread;
import com.tommasocodella.androdmtp.opendmtp.util.DateTime;
import com.tommasocodella.androdmtp.opendmtp.util.FletcherChecksum;
import com.tommasocodella.androdmtp.opendmtp.util.GeoEvent;
import com.tommasocodella.androdmtp.opendmtp.util.Log;
import com.tommasocodella.androdmtp.opendmtp.util.StringTools;

public class AndroDMTP implements Runnable, Props.SavePropsCallBack{

	//	Constant  declaration
	
    private static final boolean FREE_ACCESS        = true;
    private static final String DMTP				= "DMTP-";
    private static final String DMTP_ACCESS			= "2560797743,192.168.1.3,31000,opendmtp,moto"; //"DMTP-Access";
    private static final String COMM_HOST			= "localhost";
    private static final String COMM_PORT			= "0";
    private static final String ACCOUNT_ID			= "opendmtp";
    private static final String DEVICE_ID			= "mobile";
    private static final String UNIQUE_ID			= "";
    private static final String GPS_RATE			= "2";         // seconds
    private static final String GPS_ACCURACY		= "200";        // meters
    private static final String GPS_MINSPEED		= "0.5";       // kph [7.3 mph]
    private static final int    MOTION_START_TYPE	= 0;
    private static final String MOTION_START_METERS	= "150.0";      // meters
    private static final String MOTION_START_KPH	= "16.1";       // kph [10 mph]
    private static final String MOTION_INMOTION		= "120";        // seconds
    private static final String MOTION_STOP			= "210";        // seconds
    private static final String MOTION_DORMANT		= "1800";       // seconds
    private static final String LOG_NAME			= "MAIN";
    public  static final String COPYRIGHT			= "Copyright 2007-2009, GeoTelematic Solutions, Inc.";    
    public  static final String RELEASE_VERSION		= "1.2.2";
    private static final String DMTP_NAME			= "OpenDMTP";
    private static final String DMTP_TYPE			= "J2ME";
    public  static final String DMTP_VERSION		= DMTP_NAME + "_" + DMTP_TYPE + "." + RELEASE_VERSION;    
    public  static final String TITLE				= "MotoDMTP";
    private static final boolean ENABLE_EVENTS		= true;
    private static final long TIME_SAMPLE_INTERVAL	= 2L; // seconds
    private static final long STANDARD_LOOP_DELAY	= 2000L; // millis
    private static final long FAST_LOOP_DELAY		= 20L; // millis
    private static final long LOOP_DELAY_INCREMENT	= 30L; // millis
    
    //	Variable declaration
    
    private static AndroDMTP 			DMTP_Main 				= null;
    private AndroDMTPLocationListener	locationListener		= null;
    private PersistentStorage   		propsStore				= null;
    private boolean             		startupInit				= false;
    private CThread						mainLoopThread			= null;
    private Protocol            		protocol				= null;
    private GPSUtils         			gpsUtils	 			= null;
    private GeoEvent            		gpsEvent				= null;
    private GPSModules          		gpsModules				= null;
    private boolean             		sentInitializedEvent	= false;
    private int                 		pendingPing				= StatusCodes.STATUS_NONE;
    private long                		gpsStaleTimer			= 0L;
    private long                		lastGPSAcquisitionTimer = 0L;
    private GeoEvent           			lastValidGPSFix			= new GeoEvent();
    private long                		loopDelayMS				= STANDARD_LOOP_DELAY;
    //private TimeModules         		timeModules 			= null;
    //private long                		lastTimeEventTimer 		= 0L;
    
    //	Getters & Setters section
    
    public static String getTitle(){
        return TITLE;
    }
    
    //	Constructor section
    
    public AndroDMTP(AndroDMTPLocationListener locationListener){
        super();
        DMTP_Main = this;
        this.locationListener = locationListener;
        
    }
    
    /**
    * Returns the singleton instance of this application.
    * @return current instance.
    */
    public static AndroDMTP getInstance(AndroDMTPLocationListener locationListener){
        if (DMTP_Main == null) {
            DMTP_Main = new AndroDMTP(locationListener);
        }
        return DMTP_Main;
    }
    
    //	Main app code section
    
    
    /**
    * Starts application. Initializes all properties and starts GPS module as well.
    */
    public void startApp(){
        if (!this.startupInit) {
            //	A new execution of AndroDMTP is started so all params must be setted
            Log.info(LOG_NAME, "AndroDTMP service started");
            this.startupInit = true;

            //	Properties init
            try {
                Props.initProps(this);
            } catch (Throwable th) {
                Log.error(LOG_NAME, "Property init error", th);
            }

            //	Main properties init
            long accessCheckSum = 0xFFFFL;
            long calcCheckSum   = 0xFFFFL;
            try {
                // DMTP-Access: Access,Host,Port,Account,Device,Unique
                // DMTP-Access: 2560797743,data.example.com,31000,opendmtp,moto
                // DMTP-Access: 123456FFFF,data.example.com,31000,opendmtp,moto
                String acc[] = StringTools.parseString(DMTP_ACCESS,',');
                accessCheckSum = StringTools.parseHexLong(((acc.length>0)?acc[0]:""),accessCheckSum);
                String H = (acc.length > 1)? acc[1] : COMM_HOST;
                String P = (acc.length > 2)? acc[2] : COMM_PORT;
                String A = (acc.length > 3)? acc[3] : ACCOUNT_ID;
                String D = (acc.length > 4)? acc[4] : DEVICE_ID;
                String U = (acc.length > 5)? acc[5] : UNIQUE_ID;
                Props.initFromString(Props.PROP_COMM_DMTP_HOST  , H, true);
                Props.initFromString(Props.PROP_COMM_DMTP_PORT  , P, true);
                Props.initFromString(Props.PROP_STATE_ACCOUNT_ID, A, true);
                Props.initFromString(Props.PROP_STATE_DEVICE_ID , D, true);
                Props.initFromString(Props.PROP_STATE_UNIQUE_ID , U, true);
            } catch (Throwable th) {
                Log.error(LOG_NAME, "Main config init error", th);
            }

            //	Custom properties (set as default)
            try{
                Props.initFromString(Props.PROP_CFG_GPS_PORT            	, "rfcm"             , true); // bluetooth
                Props.initFromString(Props.PROP_CFG_GPS_BPS             	, "4800"             , true);
                Props.initFromString(Props.PROP_GPS_SAMPLE_RATE         	, GPS_RATE           , true); // seconds
                Props.initFromString(Props.PROP_GPS_ACCURACY            	, GPS_ACCURACY       , true); // meters
                Props.initFromString(Props.PROP_GPS_MIN_SPEED           	, GPS_MINSPEED       , true); // kph
                Props.initFromString(Props.PROP_COMM_MAX_CONNECTIONS    	, "30,30,30"         , true);
                Props.initFromString(Props.PROP_COMM_MIN_XMIT_DELAY     	, "5"               , true); // seconds
                Props.initFromString(Props.PROP_COMM_MIN_XMIT_RATE      	, "60"               , true); // seconds
                
                if(MOTION_START_TYPE == 0){
                	Props.initFromString(Props.PROP_MOTION_START_TYPE       , "0"                , true); // 0=kph
                	Props.initFromString(Props.PROP_MOTION_START            , MOTION_START_KPH   , true); // kph
                }else{
                	Props.initFromString(Props.PROP_MOTION_START_TYPE       , "1"                , true); // 1=meters
                	Props.initFromString(Props.PROP_MOTION_START            , MOTION_START_METERS, true); // meters
                }
                
                Props.initFromString(Props.PROP_MOTION_IN_MOTION        	, MOTION_INMOTION    , true); // seconds
                Props.initFromString(Props.PROP_MOTION_STOP             	, MOTION_STOP        , true); // seconds
                Props.initFromString(Props.PROP_MOTION_DORMANT_INTRVL   	, MOTION_DORMANT     , true); // seconds
                Props.initFromString(Props.PROP_MOTION_DORMANT_COUNT    	, "0"                , true); // count
                Props.initFromString(Props.PROP_MOTION_EXCESS_SPEED     	, "0.0"              , true); // kph
            }catch (Throwable th){
                Log.error(LOG_NAME, "Custom property init error", th);
            }

            //	Load properties from persistent storage - not implemented -
            /*
	            this.propsStore = new PersistentStorage("OpenDMTP");
	            try {
	                Props.loadFromStore(this.propsStore);
	            } catch (Throwable th) {
	                Log.error(LOG_NAME, "PersistentStorage init error", th);
	            }
            */
            
            //	Access validation
            //	- not implemented - only free access granted
            boolean accessOK;
            if (FREE_ACCESS) {
                accessOK = true;
            } else {
                //StringBuffer csKey = new StringBuffer();
                try {
                    accessCheckSum = ~((accessCheckSum >> 16) ^ accessCheckSum) & calcCheckSum;
                    FletcherChecksum fc = new FletcherChecksum();
                    fc.runningChecksum((Props.getString(Props.PROP_COMM_DMTP_HOST  ,"")+"|").getBytes());
                    fc.runningChecksum((Props.getString(Props.PROP_COMM_DMTP_PORT  ,"")+"|").getBytes());
                    //fc.runningChecksum((Props.getString(Props.PROP_STATE_ACCOUNT_ID,"")+"|").getBytes());
                    //fc.runningChecksum((Props.getString(Props.PROP_STATE_DEVICE_ID ,"")+"|").getBytes());
                    //fc.runningChecksum((Props.getString(Props.PROP_STATE_UNIQUE_ID ,"")+"|").getBytes());
                    calcCheckSum = ~fc.getChecksumAsInt() & calcCheckSum;
                    //csKey.append(Props.getString(Props.PROP_STATE_ACCOUNT_ID,"")+"|");
                    //csKey.append(Props.getString(Props.PROP_STATE_DEVICE_ID ,"")+"|");
                    //csKey.append(Props.getString(Props.PROP_STATE_UNIQUE_ID ,"")+"|");
                    //csKey.append(Props.getString(Props.PROP_COMM_DMTP_HOST  ,"")+"|");
                    //csKey.append(Props.getString(Props.PROP_COMM_DMTP_PORT  ,"")+"|");
                } catch (Throwable th) {
                    calcCheckSum = 0x1234;
                }
                accessOK = (((accessCheckSum ^ ~calcCheckSum) & 0xFFFFL) == 0x0000L);
            }

            //	Header settings
            String firmware = Props.getString(Props.PROP_STATE_FIRMWARE,"");
            String serial   = Props.getString(Props.PROP_STATE_SERIAL ,"");
            Log.println(LOG_NAME, "OpenDMTP protocol reference implementation.");
            Log.println(LOG_NAME, "Title  : " + AndroDMTP.getTitle());
            Log.println(LOG_NAME, "Version: " + DMTP_VERSION + " [" + serial + "]");
            Log.println(LOG_NAME, COPYRIGHT);                        
            
            //	Create first empty GeoEvent
            try{
            	GPSUtils.init();
            	this.gpsUtils = GPSUtils.getInstance();
                this.gpsEvent = new GeoEvent();
            }catch (Throwable th){
                Log.error(LOG_NAME, "GPS init error", th);
            }

            //	Protocol handler init
            try{
                this.protocol = Protocol.createInstance(new TransportImpl());
            }catch (Throwable th){
                Log.error(LOG_NAME, "Protocol init error", th);
                accessOK = false;
            }

            //	GPS event module init
            try {
                this.gpsModules = new GPSModules();
                if (accessOK) {
                    PacketQueue pq = this.protocol.getEventQueue();
                    this.gpsModules.addModule(new OdometerModule(pq));
                    this.gpsModules.addModule(new MotionModule(pq));
                    // here other modules
                }
            } catch (Throwable th) {
                Log.error(LOG_NAME, "GPS modules init error", th);
                accessOK = false;
            }

            // Time event module
            /*
	            try{
	                this.timeModules = new TimeModules();
	                this.timeModules.addModule(new TimeModule(this.protocol.getEventQueue()));
	                // add other modules here
	            }catch (Throwable th){
	                Log.error(LOG_NAME, "Time modules init error", th);
	            }
            */

            //	Main loop thread
            try {
                this.mainLoopThread = new CThread("AndroDMTP", this);
            } catch (Throwable th) {
                Log.error(LOG_NAME, "Loop init error", th);
                accessOK = false;
            }

            //	Start thread
            if (accessOK && (FREE_ACCESS || (((accessCheckSum ^ ~calcCheckSum) & 0xFFFFL) == 0x0000L))) {
                Log.debug(LOG_NAME, "Starting threads ...");
                CThread.startThreads();
            } else {
                // no threads started
            }
          
            //	Running
            if (accessOK) {
                Log.setMessage(1, "Running ...");
            } else {
                Log.setMessage(0, "Access Error!"); // + StringTools.toHexString(accessCheckSum, 16));
                Log.setMessage(1, "Access Error!"); // + StringTools.toHexString(~calcCheckSum & 0xFFFF, 16) + " " + csKey);
            }
        }else{
            //	Already initialized
            Log.info(LOG_NAME, "AndroDTMP service RESTARTED");
            Log.setMessage(1, "Restarting");

            // Restart threads
            CThread.startThreads();
        }
    }
    
    //	Pauses application.
    public void pauseApp(){
        // save current display
        Log.info(LOG_NAME, "**** Application PAUSED ...");
    }
    
    //	Destroys application. During the process all properties saved into storage.
    public void destroyApp(){
        CThread.stopThreads();
        this.saveProps();
        Log.info(LOG_NAME, "Application DESTROYED");
    }
        
    //	Exits from application.
    public void exitApp(){
        // save current display
        try {
            this.destroyApp();
        } catch (Exception msce) {
            // this won't occur
        }
    }
    
    //	Loads properties from storage.
    public void loadProps(){
        Props.loadFromStore(this.propsStore);
    }

    //	Saves all properties into storage.
    public void saveProps(){
        //Props.saveToStore(this.propsStore, false);
    }
    
    //	Force an event to be sent as soon as possible
    public void setPendingPing(int statusCode){
        // TODO: should synchronize
        if (this.pendingPing == StatusCodes.STATUS_NONE) {
            this.pendingPing = statusCode;
        }
        this.lastGPSAcquisitionTimer = 0L;
    }
    
    //	Tread run method
    public void run(){
        while (!this.mainLoopThread.shouldStop()) {
            try{
            	//	Acquire GPS
                boolean didAcquireGPS = false;
                long gpsInterval = Props.getLong(Props.PROP_GPS_SAMPLE_RATE, 0, 15L);
                if (DateTime.isTimerExpired(this.lastGPSAcquisitionTimer,gpsInterval)) {
                	Log.info(LOG_NAME, "GPS acquisition slot");
                    didAcquireGPS = this.gpsAcquire();
                    this.lastGPSAcquisitionTimer = DateTime.getTimerSec();
                }
                
                //	Time based events
                /*
	                if (ENABLE_EVENTS && DateTime.isTimerExpired(this.lastTimeEventTimer, TIME_SAMPLE_INTERVAL)) {
	                    this.timeModules.checkTime(DateTime.getCurrentTimeSec());
	                    this.lastTimeEventTimer = DateTime.getTimerSec();
	                }
                */
        
                //	Time to transmit? (we have data and/or minimum times have expired)
                if (ENABLE_EVENTS) {
                    this.protocol.transport();
                }
        
                //	Short loop delay
                try { Thread.sleep(this.loopDelayMS); } catch (Throwable t) {/*ignore*/}
                if(this.loopDelayMS < STANDARD_LOOP_DELAY){
                    //	This is used for temporary speed-up/slow-down of the loop delay
                    this.loopDelayMS += LOOP_DELAY_INCREMENT;
                    if(this.loopDelayMS > STANDARD_LOOP_DELAY){
                        this.loopDelayMS = STANDARD_LOOP_DELAY;
                    }
                }

            }catch(Throwable t){
                if(this.mainLoopThread.shouldStop()){
                    break;
                }else{
                    Log.error(LOG_NAME, "RunLoop error", t);
                    try { Thread.sleep(2000L); } catch (Throwable th) {}
                }
            }   
        }
    }

    //	Acquire GPS fix.
    
    private boolean gpsAcquire(){
        Log.info(LOG_NAME, "Acquiring GPS fix ...");
        long gpsAcquireTimeoutMS = Props.getLong(Props.PROP_GPS_ACQUIRE_WAIT, 0, 0L);
        
        GeoEvent gps  = new GeoEvent();
        
        Log.info(LOG_NAME, "GET FIX");
        if(((AndroDMTPLocationListener)locationListener).isLocationPresent()){
	        Log.info(LOG_NAME, "FIX:" + ((AndroDMTPLocationListener)locationListener).getLatitude());
	        
	        
	        gps.setLatitude(((AndroDMTPLocationListener)locationListener).getLatitude());
	        gps.setLongitude(((AndroDMTPLocationListener)locationListener).getLongitude());
	        gps.setAccuracy(((AndroDMTPLocationListener)locationListener).getAccuracy());
	        gps.setTimestamp(((AndroDMTPLocationListener)locationListener).getTimestamp());
	        gps.setSpeedKPH(((AndroDMTPLocationListener)locationListener).getSpeed());
	        gps.setAltitude(((AndroDMTPLocationListener)locationListener).getAltitude());
        }else{
        	gps = null;
        }
        
        //	New valid fix?
        boolean validGPS = false;        
        
        if ((gps != null) && gps.isValid() && (this.lastValidGPSFix.getTimestamp() != gps.getTimestamp())) {
        	// A new valid GPS fix is recived
        	// NOTES: 
			// - It may be desirable to set the current system time to this GPS time.
			double minSpeedKPH = Props.getDouble(Props.PROP_GPS_MIN_SPEED, 0, 7.0);
			gps.checkMinimumSpeed(minSpeedKPH);
			if (ENABLE_EVENTS) {
				//	Run through standard gps monitors (first)
				this.gpsModules.checkGPS(this.lastValidGPSFix, gps);
				//	Send INITIALIZED event
				if (!sentInitializedEvent) {
					gps.setStatusCode(StatusCodes.STATUS_INITIALIZED);
					this.protocol.getEventQueue().addEvent(Packet.PRIORITY_NORMAL, gps);
					sentInitializedEvent = true;
				}
				//	Send 'ping' event
				if (this.pendingPing != StatusCodes.STATUS_NONE) {
					gps.setStatusCode(this.pendingPing);
					this.protocol.getEventQueue().addEvent(Packet.PRIORITY_NORMAL, gps);
					this.pendingPing = StatusCodes.STATUS_NONE;
				}
			}
			//	Save last valid gps fix
			gps.copyTo(this.lastValidGPSFix);
			if(GPSUtils.isGpsStale()) {
				//	GPS was stale, but is no longer stale
				Log.debug(LOG_NAME, "GPS fix is now up to date");
				GPSUtils.setGpsStale(false);
				this.gpsStaleTimer = 0L;
			} else {
				// still not stale 
			}
			validGPS = true;
		}
        
        if(!GPSUtils.isGpsStale()){
        	// We've not received a valid GPS fix, however the last GPS fix (if any)
            // is not yet considered "stale".
            long gpsExpireInterval = Props.getLong(Props.PROP_GPS_EXPIRATION, 0, 360L);
            if (gpsExpireInterval <= 0L) {
                // The GPS fix is never considered "stale"
            }else{
	            if(this.lastValidGPSFix.isValid()) {
	                // We have previously received at least 1 valid GPS fix.  Set the timer to
	                // the last valid fix, and compare the age of the fix to the GPS expiration
	                // interval.
	                this.gpsStaleTimer = DateTime.getTimerSec(this.lastValidGPSFix.getTimestamp());
	                if (DateTime.isTimerExpired(this.gpsStaleTimer,gpsExpireInterval)) {
	                    // The timer has expired, we're now "stale"
	                    // Likely causes: (most likely to least likely)
	                    //   1) GPS antenna is obstructed (garage, building, etc.)
	                    //   2) GPS antenna has been disconnected/damaged.
	                    //   3) GPS receiver communication link has been disconnected.
	                    //   4) GPS receiver has become defective.
	                    // The last 2 can be ruled out by checking to see if we've received anything
	                    // at all from the GPS receiver, even an invalid (type 'V') record.
	                	GPSUtils.setGpsStale(true);
	                }
	            }else{
		            if(this.gpsStaleTimer <= 0L){
		                // We've never received a valid GPS fix, and this is our first invalid fix.
		                // This is a likely ocurrance when the system has just been powered up,
		                // since the GPS receiver may not have had enough time to acquire a fix.
		                // Start the GPS expiration timer.  The interval "PROP_GPS_EXPIRATION" should
		                // be at least long enough to allow the GPS receiver to make a valid 
		                // aquisition after a cold-start.
		                this.gpsStaleTimer = DateTime.getTimerSec();
		                // If a valid fix is not acquired within the expiration interval, then the
		                // GPS receiver will be considered stale.
		            }else
		            	if (DateTime.isTimerExpired(this.gpsStaleTimer,gpsExpireInterval)) {
			                // We've never received a valid GPS fix, and now the timer has expired.
			                // Likely causes: (most likely to least likely)
			                //   1) Device restarted while GPS antenna is obstructed (garage, building, etc.)
			                //   2) GPS antenna was never attached.
			                //   3) GPS receiver was never attached.
			                //   4) GPS receiver serial port was improperly specified.
			                // The last 2 can be ruled out by checking to see if we've received anything
			                // at all from the GPS receiver, even an invalid (type 'V') record.
		            		GPSUtils.setGpsStale(true);
			        }
	            }
            }
			        
            //	Is GPS fix now considered "stale"?
            if (GPSUtils.isGpsStale()) {
                // GPS fix expired, now "stale"
                // Client needs to decide what to do in this case
                // Possible actions:
                //   1) Queue up a ERROR_GPS_EXPIRED error
                Log.setMessage(0, "No GPS (stale)");
                Log.debug(LOG_NAME, "****** GPS fix is expired ... ******");
                // ('protocol.c' now sends this error if GPS fix is stale - see 'gpsIsFixStale')
            }else{
                // not yet stale
            }
        }else{
            // GPS fix is still stale.
            Log.setMessage(0, "No GPS (stale)");
            //Log.debug(LOG_NAME, "****** GPS fix is expired ... ******");
        }
        return validGPS;
        
    }    
}