package com.tommasocodella.androdmtp.services;

import android.location.LocationListener;
import android.location.LocationManager;

import com.tommasocodella.androdmtp.TransportImpl;
import com.tommasocodella.androdmtp.gps.AndroDMTPLocationListener;
import com.tommasocodella.androdmtp.opendmtp.client.base.GPSModules;
import com.tommasocodella.androdmtp.opendmtp.client.base.Packet;
import com.tommasocodella.androdmtp.opendmtp.client.base.PacketQueue;
import com.tommasocodella.androdmtp.opendmtp.client.base.PersistentStorage;
import com.tommasocodella.androdmtp.opendmtp.client.base.Props;
import com.tommasocodella.androdmtp.opendmtp.client.base.Protocol;
import com.tommasocodella.androdmtp.opendmtp.client.gps.GPSReceiver;
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

    private static final boolean FREE_ACCESS        = true;
    
    // ------------------------------------------------------------------------
    // Note: In the JAD file, properties names must be specified with '-', instead of '.'
    
    private static final String DMTP_               = "DMTP-";
    private static final String DMTP_ACCESS         = "2560797743,192.168.1.3,31000,opendmtp,moto"; //"DMTP-Access";

    // ------------------------------------------------------------------------

    private static final String COMM_HOST           = "localhost";
    private static final String COMM_PORT           = "0";
    
    private static final String ACCOUNT_ID          = "opendmtp";
    private static final String DEVICE_ID           = "mobile";
    private static final String UNIQUE_ID           = "";

    private static final String GPS_RATE            = "2";         // seconds
    private static final String GPS_ACCURACY        = "200";        // meters
    private static final String GPS_MINSPEED        = "0.5";       // kph [7.3 mph]

    private static final int    MOTION_START_TYPE   = 0;
    private static final String MOTION_START_METERS = "150.0";      // meters
    private static final String MOTION_START_KPH    = "16.1";       // kph [10 mph]
    private static final String MOTION_INMOTION     = "120";        // seconds
    private static final String MOTION_STOP         = "210";        // seconds
    private static final String MOTION_DORMANT      = "1800";       // seconds

    // ------------------------------------------------------------------------

    private static final String LOG_NAME            = "MAIN";

    public  static final String COPYRIGHT           = "Copyright 2007-2009, GeoTelematic Solutions, Inc.";

    // ----------------------------------------------------------------------------
    
    public  static final String RELEASE_VERSION     = "1.2.2";

    private static final String DMTP_NAME           = "OpenDMTP";
    private static final String DMTP_TYPE           = "J2ME";

    public  static final String DMTP_VERSION        = DMTP_NAME + "_" + DMTP_TYPE + "." + RELEASE_VERSION;

    // ------------------------------------------------------------------------
    
    public  static final String TITLE               = "MotoDMTP";
    
    public static String getTitle(){
        return TITLE;
    }

    // ----------------------------------------------------------------------------

    private static final boolean ENABLE_EVENTS      = true;

    // ----------------------------------------------------------------------------

    private static final long TIME_SAMPLE_INTERVAL  =   2L; // seconds

    private static final long STANDARD_LOOP_DELAY   = 2000L; // millis
    private static final long FAST_LOOP_DELAY       =   20L; // millis
    private static final long LOOP_DELAY_INCREMENT  =   30L; // millis

    // ----------------------------------------------------------------------------
    
    private static AndroDMTP  DMTP_Main = null;
    
    private AndroDMTPLocationListener locationListener;
    
    
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

    // ----------------------------------------------------------------------------

    private PersistentStorage   propsStore = null;
    
    private boolean             startupInit = false;
    private CThread             mainLoopThread = null;
    
    private Protocol            protocol = null;
    
    private GPSReceiver         gpsReceiver = null;
    private GeoEvent            gpsEvent = null;
    private GPSModules          gpsModules = null;
  //private TimeModules         timeModules = null;
  
    private boolean             sentInitializedEvent = false;
    private int                 pendingPing = StatusCodes.STATUS_NONE;
    
    private long                gpsStaleTimer = 0L;
    private long                lastGPSAcquisitionTimer = 0L;
  //private long                lastTimeEventTimer = 0L;
    private GeoEvent            lastValidGPSFix = new GeoEvent();
    private long                loopDelayMS = STANDARD_LOOP_DELAY;

    /**
    * Creates and register MIDlet application within the system and associates it with this
    * implementation.
    */
    public AndroDMTP(AndroDMTPLocationListener locationListener){
        super();
        DMTP_Main = this;
        this.locationListener = locationListener;
        
    }

    // ----------------------------------------------------------------------------

    /*private static String GetAppProperty(String key, String dft){
        if (key != null) {
            String val = MainClass.getInstance().getAppProperty(key);
            if (val != null) {
                return val.trim();
            } else {
                return dft;
            }
        } else {
            return dft;
        }
    }*/
    
    // ----------------------------------------------------------------------------
    
    /**
    * Starts application. Initializes all properties and starts GPS module as well.
    * @throws MIDletStateChangeException when error encountered.
    */
    public void startApp(){
        if (!this.startupInit) {
            // new execution ...
            Log.info(LOG_NAME, "vvvvv Application STARTED vvvvv");
            this.startupInit = true;

            /* init properties */
            try {
                Props.initProps(this);
            } catch (Throwable th) {
                Log.error(LOG_NAME, "Property init error", th);
            }

            /* main properties */
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

            /* custom properties (set as default) */
            try {
                Props.initFromString(Props.PROP_CFG_GPS_PORT            , "rfcm"             , true); // bluetooth
                Props.initFromString(Props.PROP_CFG_GPS_BPS             , "4800"             , true);
                Props.initFromString(Props.PROP_GPS_SAMPLE_RATE         , GPS_RATE           , true); // seconds
                Props.initFromString(Props.PROP_GPS_ACCURACY            , GPS_ACCURACY       , true); // meters
                Props.initFromString(Props.PROP_GPS_MIN_SPEED           , GPS_MINSPEED       , true); // kph
                Props.initFromString(Props.PROP_COMM_MAX_CONNECTIONS    , "30,30,30"         , true);
                Props.initFromString(Props.PROP_COMM_MIN_XMIT_DELAY     , "5"               , true); // seconds
                Props.initFromString(Props.PROP_COMM_MIN_XMIT_RATE      , "60"               , true); // seconds
                if (MOTION_START_TYPE == 0) {
                Props.initFromString(Props.PROP_MOTION_START_TYPE       , "0"                , true); // 0=kph
                Props.initFromString(Props.PROP_MOTION_START            , MOTION_START_KPH   , true); // kph
                } else {
                Props.initFromString(Props.PROP_MOTION_START_TYPE       , "1"                , true); // 1=meters
                Props.initFromString(Props.PROP_MOTION_START            , MOTION_START_METERS, true); // meters
                }
                Props.initFromString(Props.PROP_MOTION_IN_MOTION        , MOTION_INMOTION    , true); // seconds
                Props.initFromString(Props.PROP_MOTION_STOP             , MOTION_STOP        , true); // seconds
                Props.initFromString(Props.PROP_MOTION_DORMANT_INTRVL   , MOTION_DORMANT     , true); // seconds
                Props.initFromString(Props.PROP_MOTION_DORMANT_COUNT    , "0"                , true); // count
                Props.initFromString(Props.PROP_MOTION_EXCESS_SPEED     , "0.0"              , true); // kph
            } catch (Throwable th) {
                Log.error(LOG_NAME, "Custom property init error", th);
            }

            /* init from external JAD file properties */
            /*try {
                for (Enumeration kvEnum = Props.getKeyValues(); kvEnum.hasMoreElements();) {
                    KeyValue kv = (KeyValue)kvEnum.nextElement();
                    String userKey = kv.getKeyName().replace('.','-');
                    String userVal = GetAppProperty(DMTP_ + userKey, null);
                    if (userVal != null) {
                        Log.debug(LOG_NAME, "Found User props: '" + userKey + "' ==> '" + userVal + "'");
                        kv.initFromString(userVal, true); // set as default
                    } else {
                        //Log.warn(LOG_NAME, "User prop not found: '" + kv.getKeyName() + "'");
                    }
                }
            } catch (Throwable th) {
                Log.error(LOG_NAME, "JAD property init error", th);
            }*/

            /* init from persistent storage */
            this.propsStore = new PersistentStorage("OpenDMTP");
            try {
                Props.loadFromStore(this.propsStore);
            } catch (Throwable th) {
                Log.error(LOG_NAME, "PersistentStorage init error", th);
            }
            
            /* validate access */
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

            /* header */
            String firmware = Props.getString(Props.PROP_STATE_FIRMWARE,"");
            String serial   = Props.getString(Props.PROP_STATE_SERIAL ,"");
            Log.println(LOG_NAME, "OpenDMTP protocol reference implementation.");
            Log.println(LOG_NAME, "Title  : " + AndroDMTP.getTitle());
            Log.println(LOG_NAME, "Version: " + DMTP_VERSION + " [" + serial + "]");
            Log.println(LOG_NAME, COPYRIGHT);

            /* init display */
            
            
            /* check host:port */
            String hostStr = Props.getString(Props.PROP_COMM_DMTP_HOST,"");
            int    portNum = (int)Props.getLong(Props.PROP_COMM_DMTP_PORT,0,0L);

            /* OnScreen message handler */
            
            
            /* init gps receiver */
            try {
                //GPSReceiver.init(new GPSDeviceImpl(accessOK));
                this.gpsReceiver = GPSReceiver.getInstance();
                this.gpsEvent = new GeoEvent();
            } catch (Throwable th) {
                Log.error(LOG_NAME, "GPS init error", th);
            }

            /* protocol handler */
            try {
                this.protocol = Protocol.createInstance(new TransportImpl());
            } catch (Throwable th) {
                Log.error(LOG_NAME, "Protocol init error", th);
                accessOK = false;
            }

            /* gps event modules */
            try {
                this.gpsModules = new GPSModules();
                if (accessOK) {
                    PacketQueue pq = this.protocol.getEventQueue();
                    this.gpsModules.addModule(new OdometerModule(pq));
                    this.gpsModules.addModule(new MotionModule(pq));
                    // add other modules here
                }
            } catch (Throwable th) {
                Log.error(LOG_NAME, "GPS modules init error", th);
                accessOK = false;
            }

            /* time event modules */
            //try {
            //    this.timeModules = new TimeModules();
            //    this.timeModules.addModule(new TimeModule(this.protocol.getEventQueue()));
            //    // add other modules here
            //} catch (Throwable th) {
            //    Log.error(LOG_NAME, "Time modules init error", th);
            //}

            /* main loop thread */
            try {
                this.mainLoopThread = new CThread("Main", this);
            } catch (Throwable th) {
                Log.error(LOG_NAME, "Loop init error", th);
                accessOK = false;
            }

            /* start threads */
            if (accessOK && (FREE_ACCESS || (((accessCheckSum ^ ~calcCheckSum) & 0xFFFFL) == 0x0000L))) {
                Log.debug(LOG_NAME, "Starting threads ...");
                CThread.startThreads();
            } else {
                // no threads started
            }

            /* update screen with empty GeoEvent */
            
            /* running */
            if (accessOK) {
                Log.setMessage(1, "Running ...");
            } else {
                Log.setMessage(0, "Access Error!"); // + StringTools.toHexString(accessCheckSum, 16));
                Log.setMessage(1, "Access Error!"); // + StringTools.toHexString(~calcCheckSum & 0xFFFF, 16) + " " + csKey);
            }

            /* start as paused */
            // this.pauseApp();
            // super.notifyPaused();

        } else {

            // already initialized
            Log.info(LOG_NAME, "**** Application RESTARTED ...");

            /* message */
            Log.setMessage(1, "Restarting ...");

            /* restart threads */
            CThread.startThreads();

            /* restore display */

            /* message */
            Log.setMessage(1, "");

        }
        
    }
    
    /**
    * Pauses application.
    */
    public void pauseApp(){
        // save current display
        Log.info(LOG_NAME, "**** Application PAUSED ...");
    }
    
    /**
    * Destroys application. During the process all properties saved into storage.
    * @param unconditional if true no exception thrown. If false throws MIDletStateChangeException
    *        exception.
    * @throws MIDletStateChangeException if specified by parameter.
    */
    public void destroyApp(boolean unconditional){
        if (!unconditional) {
            //throw new MIDletStateChangeException();
        }
        CThread.stopThreads();
        this.saveProps();
        Log.info(LOG_NAME, "^^^^^ Application DESTROYED ^^^^^");
    }
    
    // ----------------------------------------------------------------------------
    
    /**
    * Exits from application.
    */
    public void exitApp(){
        // save current display
        try {
            this.destroyApp(true);
        } catch (Exception msce) {
            // this won't occur
        }
    }
    
    // ----------------------------------------------------------------------------

    /**
    * Loads properties from storage.
    */
    public void loadProps(){
        Props.loadFromStore(this.propsStore);
    }

    /**
    * Saves all properties into storage.
    */
    public void saveProps(){
        Props.saveToStore(this.propsStore, false);
    }
    
    // ----------------------------------------------------------------------------

    /**
    * Force an event to be sent as soon as possible
    */
    public void setPendingPing(int statusCode){
        // TODO: should synchronize
        if (this.pendingPing == StatusCodes.STATUS_NONE) {
            this.pendingPing = statusCode;
        }
        this.lastGPSAcquisitionTimer = 0L;
    }
    
    // ----------------------------------------------------------------------------
    // ----------------------------------------------------------------------------
    // ----------------------------------------------------------------------------
    
    /**
    * Runs thread.
    */
    public void run(){
        while (!this.mainLoopThread.shouldStop()) {
            //Log.debug(LOG_NAME, "Main loop iteration ...");
            try {
        
                // -----------------
                // acquire GPS
                boolean didAcquireGPS = false;
                long gpsInterval = Props.getLong(Props.PROP_GPS_SAMPLE_RATE, 0, 15L);
                if (DateTime.isTimerExpired(this.lastGPSAcquisitionTimer,gpsInterval)) {
                	Log.info(LOG_NAME, "TEMPO DI ACQUISIZIONE GPS");
                    didAcquireGPS = this.gpsAcquire();
                    this.lastGPSAcquisitionTimer = DateTime.getTimerSec();
                }
                
                // -----------------
                // time based events
                //if (ENABLE_EVENTS && DateTime.isTimerExpired(this.lastTimeEventTimer, TIME_SAMPLE_INTERVAL)) {
                //    this.timeModules.checkTime(DateTime.getCurrentTimeSec());
                //    this.lastTimeEventTimer = DateTime.getTimerSec();
                //}
        
                // -----------------
                // time to transmit? (we have data and/or minimum times have expired)
                if (ENABLE_EVENTS) {
                    this.protocol.transport();
                }
        
                // -----------------
                // short loop delay
                try { Thread.sleep(this.loopDelayMS); } catch (Throwable t) {/*ignore*/}
                if (this.loopDelayMS < STANDARD_LOOP_DELAY) {
                    // this is used for temporary speed-up/slow-down of the loop delay
                    this.loopDelayMS += LOOP_DELAY_INCREMENT;
                    if (this.loopDelayMS > STANDARD_LOOP_DELAY) {
                        this.loopDelayMS = STANDARD_LOOP_DELAY;
                    }
                }

            } catch (Throwable t) {
                if (this.mainLoopThread.shouldStop()) {
                    break;
                } else {
                    Log.error(LOG_NAME, "RunLoop error", t);
                    try { Thread.sleep(2000L); } catch (Throwable th) {}
                }
            }
            
        } // while (!this.mainLoopThread.shouldStop())
    }

    // ----------------------------------------------------------------------------

    /**
    * Acquire GPS fix.
    */
    
    
    private boolean gpsAcquire(){
        //Log.info(LOG_NAME, "Acquiring GPS fix ...");
        
        /* acquire GPS fix */
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

        //this.gpsEvent.setHeading  (heading);
        
        //GeoEvent gps = this.gpsReceiver.acquire(gpsAcquireTimeoutMS,this.gpsEvent);
        //if ((gps == null) || !gps.isValid()) {
        //    //Log.warn(LOG_NAME, "GPS fix is invalid");
        //} else
        //if (this.lastValidGPSFix.getTimestamp() == gps.getTimestamp()) {
        //    Log.warn(LOG_NAME, "GPS fix timestamp didn't change: " + gps.getTimestamp());
        //}
        
        /* new valid fix? */
        boolean validGPS = false;
        if ((gps != null) && gps.isValid() && (this.lastValidGPSFix.getTimestamp() != gps.getTimestamp())) {
            //Log.setMessage(0, "New GPS Fix [" + (new DateTime()).getTimeString() + "]");
            //Log.info(LOG_NAME, "New GPS fix ...");
            // We've received a new valid GPS fix
            // NOTES: 
            // - PROP_GPS_ACCURACY is already taken into account
            // - It may be desirable to set the current system time to this GPS time.
            double minSpeedKPH = Props.getDouble(Props.PROP_GPS_MIN_SPEED, 0, 7.0);
            gps.checkMinimumSpeed(minSpeedKPH);
            if (ENABLE_EVENTS) {
                // run through standard gps monitors (first)
                this.gpsModules.checkGPS(this.lastValidGPSFix, gps);
                // Send INITIALIZED event
                if (!sentInitializedEvent) {
                    gps.setStatusCode(StatusCodes.STATUS_INITIALIZED);
                    this.protocol.getEventQueue().addEvent(Packet.PRIORITY_NORMAL, gps);
                    sentInitializedEvent = true;
                }
                // Send 'ping' event
                if (this.pendingPing != StatusCodes.STATUS_NONE) {
                    gps.setStatusCode(this.pendingPing);
                    this.protocol.getEventQueue().addEvent(Packet.PRIORITY_NORMAL, gps);
                    this.pendingPing = StatusCodes.STATUS_NONE;
                }
            }
            // save last valid gps fix
            gps.copyTo(this.lastValidGPSFix);
            if (GPSReceiver.isGpsStale()) {
                // GPS was stale, but is no longer stale
                Log.debug(LOG_NAME, "GPS fix is now up to date ...");
                GPSReceiver.setGpsStale(false);
                this.gpsStaleTimer = 0L;
            } else {
                // still not stale 
            }
            //GPSDisplay.getInstance().updateValues(this.lastValidGPSFix);
            validGPS = true;
        } //else       
        /* now stale? */
        /*if (!GPSReceiver.isGpsStale()) {
            // We've not received a valid GPS fix, however the last GPS fix (if any)
            // is not yet considered "stale".
            long gpsExpireInterval = Props.getLong(Props.PROP_GPS_EXPIRATION, 0, 360L);
            if (gpsExpireInterval <= 0L) {
                // The GPS fix is never considered "stale"
            } else
            if (this.lastValidGPSFix.isValid()) {
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
                    GPSReceiver.setGpsStale(true);
                }
            } else 
            if (this.gpsStaleTimer <= 0L) {
                // We've never received a valid GPS fix, and this is our first invalid fix.
                // This is a likely ocurrance when the system has just been powered up,
                // since the GPS receiver may not have had enough time to acquire a fix.
                // Start the GPS expiration timer.  The interval "PROP_GPS_EXPIRATION" should
                // be at least long enough to allow the GPS receiver to make a valid 
                // aquisition after a cold-start.
                this.gpsStaleTimer = DateTime.getTimerSec();
                // If a valid fix is not acquired within the expiration interval, then the
                // GPS receiver will be considered stale.
            } else
            if (DateTime.isTimerExpired(this.gpsStaleTimer,gpsExpireInterval)) {
                // We've never received a valid GPS fix, and now the timer has expired.
                // Likely causes: (most likely to least likely)
                //   1) Device restarted while GPS antenna is obstructed (garage, building, etc.)
                //   2) GPS antenna was never attached.
                //   3) GPS receiver was never attached.
                //   4) GPS receiver serial port was improperly specified.
                // The last 2 can be ruled out by checking to see if we've received anything
                // at all from the GPS receiver, even an invalid (type 'V') record.
                GPSReceiver.setGpsStale(true);
            }
            // is GPS fix now considered "stale"?
            if (GPSReceiver.isGpsStale()) {
                // GPS fix expired, now "stale"
                // Client needs to decide what to do in this case
                // Possible actions:
                //   1) Queue up a ERROR_GPS_EXPIRED error
                Log.setMessage(0, "No GPS (stale)");
                Log.debug(LOG_NAME, "****** GPS fix is expired ... ******");
                // ('protocol.c' now sends this error if GPS fix is stale - see 'gpsIsFixStale')
            } else {
                // not yet stale
                long samples = this.gpsReceiver.getSampleCount_A() + this.gpsReceiver.getSampleCount_V();
                Log.setMessage(0, "No GPS [" + samples + "]");
                //Log.debug(LOG_NAME, "No GPS [" + samples + "]");
            }
        } else {
            // GPS fix is still stale.
            Log.setMessage(0, "No GPS (stale)");
            //Log.debug(LOG_NAME, "****** GPS fix is expired ... ******");
        }*/
        
        /* valid/invalid GPS fix */
        return validGPS;
        
    }    
}