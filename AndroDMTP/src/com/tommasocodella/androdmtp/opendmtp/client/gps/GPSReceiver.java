// ----------------------------------------------------------------------------
// Copyright 2006-2008, Martin D. Flynn
// All rights reserved
// ----------------------------------------------------------------------------
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
// 
// http://www.apache.org/licenses/LICENSE-2.0
// 
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//
// ----------------------------------------------------------------------------
// Description:
//  This class parses and manages data read from an NMEA-0183 GPS receiver.
// ----------------------------------------------------------------------------
// Change History:
//  2006/03/26  Martin D. Flynn
//     -Initial release
//  2006/11/03  Robert S. Brewer
//     -Include JavaDocs
//  2006/??/??  Martin D. Flynn
//     -Minor changes made to custom "$DMTP" data format.
//      (used for Motorola phone support)
// ----------------------------------------------------------------------------
package com.tommasocodella.androdmtp.opendmtp.client.gps;

import java.io.InterruptedIOException;

import com.tommasocodella.androdmtp.opendmtp.client.base.Props;
import com.tommasocodella.androdmtp.opendmtp.util.CThread;
import com.tommasocodella.androdmtp.opendmtp.util.DateTime;
import com.tommasocodella.androdmtp.opendmtp.util.GeoEvent;
import com.tommasocodella.androdmtp.opendmtp.util.Log;
import com.tommasocodella.androdmtp.opendmtp.util.StringTools;

/**
* This class parses and manages data read from an NMEA-0183 GPS receiver.
*/
public class GPSReceiver
    implements Runnable, CThread.ThreadListener
{
    
    // ------------------------------------------------------------------------

    // custom GPS event record 
    public  static final String  GPS_CUSTOM_RECORD      = "$DMTP";
    
    // ------------------------------------------------------------------------
   
    private static final boolean GPS_EMULATOR           = false;

    // ------------------------------------------------------------------------

    private static final String  LOG_NAME               = "GPS";

    // ------------------------------------------------------------------------
    // Bluetooth GPS device names/pins:
    //    "Pharos iGPS-BT"/12345678
    // ------------------------------------------------------------------------

    public static final String GPS_RECEIVER_UNKOWN      = "";
    public static final String GPS_RECEIVER_GARMIN      = "garmin";
    public static final String GPS_RECEIVER_TRIMBLE     = "trimble";
    public static final String GPS_RECEIVER_UBLOX       = "ublox";
    public static final String GPS_RECEIVER_SIRF        = "sirf";
    public static final String GPS_RECEIVER_PHAROS      = "pharos"; // sirf

    public static final double KILOMETERS_PER_KNOT      = 1.85200000;

    // ------------------------------------------------------------------------

    public static GPSReceiver   DMTP_GPSReceiver        = null;
    
    /**
    * Initializes the singleton GPSReceiver instance, or returns existing instance if
    * already initialized.
    * @param gpsDev Actual GPS device to receive data from.
    * @return The initialized singleton GPSReceiver object.
    */
    public static GPSReceiver init(GPSDevice gpsDev)
    {
        if (DMTP_GPSReceiver == null) {
            DMTP_GPSReceiver = new GPSReceiver(gpsDev);
        }
        return DMTP_GPSReceiver;
    }
    
    /**
    * Returns existing instance if already initialized, or logs error if instance has
    * not been created yet.
    * @return The singleton GPSReceiver object.
    */
    public static GPSReceiver getInstance()
    {
        if (DMTP_GPSReceiver == null) {
            Log.error(LOG_NAME, "GPSReceiver uninitialized!");
        }
        return DMTP_GPSReceiver;
    }

    // ------------------------------------------------------------------------

    private CThread             acquireThread   = null;
    private GPSReadWatchdog     watchdog        = null;

    private Object              gpsLock         = new Object();
    private long                gpsGPRMC_time   = 0L;
    private long                gpsGPGGA_time   = 0L;
    private GeoEvent            gpsEvent        = null;

    private Object              sampleLock      = new Object();
    private long                lastSampleTime  = 0L;
    private long                lastValidTime   = 0L;
    private long                sampleCount_A   = 0L;
    private long                sampleCount_V   = 0L;
    private long                restartCount    = 0L;
    
    private GPSDevice           gpsDevice       = null;

    /**
    * Starts GPSReceiver thread running.
    * @param gpsDev GPS device to collect data from.
    */
    private GPSReceiver(GPSDevice gpsDev)
    {
        try {
            // start GPSReceiver in a separate thread
            this.gpsDevice = gpsDev;
            this.gpsEvent  = new GeoEvent();
            if (this.gpsDevice.runInThread()) {
                this.acquireThread = new CThread("GPS", this); // thread is started later
                this.watchdog      = new GPSReadWatchdog();
            }
        } catch (Throwable t) {
            Log.error(LOG_NAME, "Init error", t);
        }
    }
    
    /**
    * Main method for thread, receiving data from GPS device until told to stop.
    */
    public void run()
    {
        //Log.debug(LOG_NAME, "Starting thread ...");
        // (We don't get here unless we're running in a thread)
        while (!this.acquireThread.shouldStop()) {
            
            /* reset GPS connection */
            this.gpsDevice.closeDevice();
            try { Thread.sleep(3000L); } catch (Throwable t) {}
            
            /* get GPS fix */
            try {
                this._acquire(0L/*no timeout while in a thread*/);
            } catch (SecurityException se) {
                // terminate thread
                break;
            }
            // we only get here is there is an error
            
        }
        this.gpsDevice.closeDevice();
        //Log.debug(LOG_NAME, "Stopping thread ...");
    }

    // ----------------------------------------------------------------------------

    private static boolean  gpsIsStale = false;

    /**
    * Accessor for gspIsStale field, which indicates that it has been too long since the
    * last fix from the GPS device.
    * @return true if the position data is stale, false otherwise.
    */
    public static boolean isGpsStale()
    {
        return GPSReceiver.gpsIsStale;
    }

    /**
    * Setter for GPS staleness flag, which indicates that it has been too long since
    * the last fix from the GPS device.
    * @param stale the new value for the GPS staleness flag
    */
    public static void setGpsStale(boolean stale)
    {
        if (stale && !GPSReceiver.gpsIsStale) {
            Log.warn(LOG_NAME, "GPS receiver has been tagged 'stale'");
        } else 
        if (!stale && GPSReceiver.gpsIsStale) {
            Log.warn(LOG_NAME, "GPS receiver has been tagged 'current'");
        }
        GPSReceiver.gpsIsStale = stale;
    }

    // ------------------------------------------------------------------------

    /**
    * Attempts to acquire a GPS event from the GPS device
    * in either a threaded or non-threaded environment.
    * @param timeoutMS milliseconds to wait for fix before timing out.
    * @param gev Destination for the GPS event data.
    * @return Destination for the GPS event data, or null on error.
    */
    public GeoEvent acquire(long timeoutMS, GeoEvent gev)
    {
        if (this.acquireThread == null) {
            // Non-Threaded environment:
            // Wait up to 'timeoutMS' milliseconds for a GPS fix
            // The value of 'timeoutMS' for non-threaded environments may need to
            // be as high as 90 seconds or more if this module needs to cold-start 
            // the GPS receiver.  Note that the calling thread will be blocked 
            // during this time.
            boolean ok = false;
            try {
                ok = this._acquire(timeoutMS);
            } catch (SecurityException se) {
                ok = false;
            }
            if (ok) {
                return this.getGeoEvent(gev);
            } else {
                // timeout/error
                this.gpsDevice.closeDevice();
                return null;
            }
        } else {
            // Threaded environment:
            // Return the last GPS fix if it is within the specified 'timeoutMS'
            if (timeoutMS <= 0L) {
                // no timeout, just get latest fix
                return this.getGeoEvent(gev);
            } else {
                // return last fix, if it's within the specified timeout
                long timeoutSec = (timeoutMS + 999L) / 1000L;
                if ((this.getLastValidTime() + timeoutSec) >= DateTime.getCurrentTimeSec()) {
                    return this.getGeoEvent(gev);
                } else {
                    return null;
                }
            }
        }
    }

    // ------------------------------------------------------------------------

    /**
    * Does the actual work of acquiring the GPS data from the GPS device.
    * @param timeoutMS milliseconds to wait for fix before timing out.
    * @return true if read a valid record before timeout, otherwise false.
    * @throws SecurityException if the user has denied us access to the Bluetooth 'rfcm'
    * device.
    */
    private static long acquireCount = 0L;
    private boolean _acquire(long timeoutMS)
        throws SecurityException
    {
        
        /* GPS emulator */
        /* *
        if (GPS_EMULATOR) {
            while (!this.acquireThread.shouldStop()) {
                try { Thread.sleep(1000L); } catch (Throwable t) {}
                long fixtime = DateTime.getCurrentTimeSec();
                double lat =  (double)((long)(((double)fixtime * GeoPoint.PI) *  10000.0) %  900000L) / 10000.0;
                double lon = -(double)((long)(((double)fixtime * GeoPoint.PI) * 100000.0) % 1800000L) / 10000.0;
                synchronized (this.gpsLock) {
                    this.gpsGPRMC_time = fixtime;
                    this.gpsEvent.setTimestamp(fixtime);
                    this.gpsEvent.setLatitude (lat);
                    this.gpsEvent.setLongitude(lon);
                    this.gpsEvent.setSpeedKPH (75.0);
                    this.gpsEvent.setHeading  (133.0);
                    this.gpsEvent.setAltitude (720.0);
                }
                synchronized (this.sampleLock) {
                    this.sampleCount_A++;
                    this.lastSampleTime = DateTime.getCurrentTimeSec();
                    this.lastValidTime = fixtime; 
                }
                if (this.acquireThread == null) {
                    // we aren't running in a thread, return now
                    return true;
                }
            }
            // Control never reaches here
        }
        /* */

        /* timeout */
        // Timeouts are ignore if running in a thread
        // A 'timeout' simply means that we've timed-out attempting to read a "valid"
        // GPS record.  It is assumed that if we are connected to the GPS device, it
        // is always sending us GPS data (possibly 'invalid', but still sending us data).
        long timeoutTime = ((timeoutMS > 0L) && (this.acquireThread == null))? 
            (DateTime.getCurrentTimeMillis() + timeoutMS) : 0L;

        /* GPS acquisition loop */
        acquireCount++;
        try {
            
            /* find gps receiver */
            if (!this.gpsDevice.isOpen()) {
                //Log.debug(LOG_NAME, "Open GPS ...");
                Log.setMessage(0, "Open GPS ...");
                boolean didOpen = this.gpsDevice.openDevice();
                if ((this.acquireThread != null) && this.acquireThread.shouldStop()) {
                    throw new GPSException("Thread should stop");
                }
                if (!didOpen) { // still null
                    // GPS receiver was not found
                    Log.debug(LOG_NAME, "GPS open failed ...");
                    Log.setMessage(0, "GPS failed");
                    try { Thread.sleep(4000L); } catch (Throwable t) {}
                    throw new GPSException("GPS Receiver not found");
                }
                //Log.setMessage(0, "... GPS Opened");
            }
        
            /* GPS read loop */
            // Hang here until one of the following occurs:
            // - We get a GPS error
            // - We parse a valid record and we're not running in a thread
            // - A timeout has been specified and we've timed out
            StringBuffer sb = new StringBuffer();
            while (true) {

                /* stop thread? */
                if ((this.acquireThread != null) && this.acquireThread.shouldStop()) {
                    throw new GPSException("Thread should stop");
                }

                /* read line */
                //Log.debug(LOG_NAME, "GPS Read ...");
                Log.setMessage(0, "GPS Acquire ...");
                sb.setLength(0);
                int count = this.gpsDevice.readLine(sb, 15000L);
                //Log.debug(LOG_NAME, sb.toString());
                boolean readValid = this._parseNMEA0183(sb.toString());
                Log.setMessage(0, "");

                /* not in a thread */
                if (this.acquireThread == null) {
                    // not in a thread
                    if (readValid) {
                        // we've read a valid record and we're not running in a thread
                        //Log.setMessage(0, "GPS Valid ...");
                        return true;
                    } else
                    if ((timeoutMS > 0L) && (timeoutTime < DateTime.getCurrentTimeMillis())) {
                        // we haven't read a valid record within the timeout period
                        Log.setMessage(0, "GPS Invalid ...");
                        return false;
                    }
                }

            }
            // Control never reaches here
            
        } catch (GPSException gpse) {
            Log.error(LOG_NAME, gpse.getMessage(), gpse.getException());
            Log.setMessage(0, gpse.getMessage());
        } catch (InterruptedException ie) { // thread interrupted
            Log.error(LOG_NAME, "Thread interrupted");
            Log.setMessage(0, "GPS Interrupted!");
        } catch (InterruptedIOException iioe) { // timeout
            Log.error(LOG_NAME, "Read timeout");
            Log.setMessage(0, "GPS Timeout!");
        } catch (SecurityException se) {
            // We could get a security exception if the user has denied us access
            // to the Bluetooth 'rfcm' device.
            Log.error(LOG_NAME, "Access denied", se);
            Log.setMessage(0, "GPS Denied!");
            this.gpsDevice.closeDevice();
            throw se;
        } catch (Throwable t) {
            Log.error(LOG_NAME, "Error", t);
            Log.setMessage(0, "GPS Error!");
            t.printStackTrace();
        }
        
        /* the only way we get here is if there was a GPS error */
        // reset the gps receiver
        //Log.setMessage(0, "GPS Close ...");
        this.gpsDevice.closeDevice();
        // sleep a few seconds
        try { Thread.sleep(5000L); } catch (Throwable t) {}
        return false;
        
    }

    // ------------------------------------------------------------------------

    /**
    * Copies the class static field GeoEvent into the supplied parameter, and also
    * returns it as a convenience. This method is synchronized for thread safety.
    * @param gev Destination for the GPS event data.
    * @return GPS event data copied into parameter.
    */
    private GeoEvent getGeoEvent(GeoEvent gev)
    {
        synchronized (this.gpsLock) {
            gev = (GeoEvent)this.gpsEvent.copyTo(gev);
        }
        return gev;
    }
    
    // ------------------------------------------------------------------------
    // http://www.scientificcomponent.com/nmea0183.htm
    // http://home.mira.net/~gnb/gps/nmea.html
    // $GPGGA - Global Positioning System Fix Data
    //   $GPGGA,015402.240,0000.0000,N,00000.0000,E,0,00,50.0,0.0,M,18.0,M|0.0,0000*4B
    //   $GPGGA,025425.494,3509.0743,N,11907.6314,W,1,04,2.3,530.3,M,-21.9,M|0.0,0000*4D
    //         ,    1     ,    2    ,3,     4    ,5,6,7 , 8 ,  9 ,10,  11,12,13 , 14
    //      1   UTC time of position HHMMSS
    //      2   current latitude in ddmm.mm format
    //      3   latitude hemisphere ("N" = northern, "S" = southern)
    //      4   current longitude in dddmm.mm format
    //      5   longitude hemisphere ("E" = eastern, "W" = western)
    //      6   (0=no fix, 1=GPS, 2=DGPS, 3=PPS?, 6=dead-reckoning)
    //      7   number of satellites (00-12)
    //      8   Horizontal Dilution of Precision
    //      9   Height above/below mean geoid (above mean sea level, not WGS-84 ellipsoid height)
    //     10   Unit of height, always 'M' meters
    //     11   Geoidal separation (add to #9 to get WGS-84 ellipsoid height)
    //     12   Unit of Geoidal separation (meters)
    //     13   Age of differential GPS
    //     14   Differential reference station ID (always '0000')
    // $GPGLL - Geographic Position, Latitude/Longitude
    //   $GPGLL,36000.0000,N,72000.0000,E,015402.240,V*17
    //   $GPGLL,3209.0943,N,11907.9313,W,025426.493,A*2F
    //   $GPGLL,    1    ,2,     3    ,4,      5   ,6
    //      1    Current latitude
    //      2    North/South
    //      3    Current longitude
    //      4    East/West
    //      5    UTC in hhmmss format
    //      6    A=valid, V=invalid
    // $GPGSA - GPS DOP and Active Satellites
    //   $GPGSA|A|1|||||||||||||50.0|50.0|50.0*05|
    //   $GPGSA|A|3|16|20|13|23|||||||||4.3|2.3|3.7*36|
    // $GPGSV - GPS Satellites in View
    //   $GPGSV|3|1|9|16|35|51|32|4|9|269|0|20|32|177|33|13|62|329|37*4E|
    //   $GPGSV|3|2|9|3|14|113|0|24|5|230|0|8|12|251|0|23|71|71|39*70|
    //   $GPGSV|3|3|9|131|0|0|0*43|
    // $GPRMC - Recommended Minimum Specific GPS/TRANSIT Data
    //   $GPRMC|015402.240|V|36000.0000|N|72000.0000|E|0.000000||200505||*3C|
    //   $GPRMC,025423.494,A,3709.0642,N,11907.8315,W,0.094824,108.52,200505,,*12
    //   $GPRMC,      1   ,2,    3    ,4,     5    ,6, 7      ,   8  ,  9   ,A,B*M
    //   Where:
    //      1   UTC time of position HHMMSS
    //      2   validity of the fix ("A" = valid, "V" = invalid)
    //      3   current latitude in ddmm.mm format
    //      4   latitude hemisphere ("N" = northern, "S" = southern)
    //      5   current longitude in dddmm.mm format
    //      6   longitude hemisphere ("E" = eastern, "W" = western)
    //      7   speed in knots
    //      8   true course in degrees
    //      9   date in DDMMYY format
    //      A   magnetic variation in degrees
    //      B   direction of magnetic variation ("E" = east, "W" = west)
    //      M   checksum
    // $GPVTG - Track Made Good and Ground Speed
    //   $GPVTG,054.7,T,034.4,M,005.5,N,010.2,K*41
    //             1  2    3  4    5  6    7  8
    //      1   True course made good over ground, degrees
    //      2   Magnetic course made good over ground, degrees
    //      3   Ground speed, N=Knots
    //      4   Ground speed, K=Kilometers per hour

    /**
    * Parses provided NMEA-0183 formatted String from GPS device and stores results
    * in class static fields. Field access is synchronized for thread safety.
    * @param data NMEA-0183 formatted String to be parsed.
    * @return true if data is valid, false otherwise.
    */
    private boolean _parseNMEA0183(String rawData)
    {
        
        /* proper header? */
        if ((rawData == null) || !rawData.startsWith("$")) {
            //Log.warn(LOG_NAME, "Rcd does not start with '$'");
            return false;
        }

        /* parse record */
        String fld[] = StringTools.parseString(rawData.toUpperCase(), ',');
        if ((fld == null) || (fld.length < 2)) {
            Log.warn(LOG_NAME, "Invalid number of fields");
            return false;
        }

        /* unexpected sentences */
        if (fld[0].startsWith("$PG") ||    // <-- Garmin sentences
            fld[0].startsWith("$GPGSV")) { // <-- Satellite info sentence
            // reconfigure Garmin device
            //Log.warn(LOG_NAME, "Found satellite sentence '$GPGSV'");
            String receiver = Props.getString(Props.PROP_CFG_GPS_MODEL,"");
            if (receiver.equalsIgnoreCase(GPS_RECEIVER_GARMIN)) {
                // If the serial port is configured for RX-only, then this section should
                // be commented out.  Otherwise the Garmin config strings will be continually
                // sent and may slow down communication with the receiver.
                //this._configGarmin(com);
            } else {
                // don't know how to trim out these sentences
            }
            return false;
        }

        /* valid parsed record indicator */
        boolean validFix_GPRMC = false;
        boolean validFix_GPGGA = false;

        /* special case custom format */
        if (fld[0].startsWith(GPSReceiver.GPS_CUSTOM_RECORD)) {
            //   0        1         2        3         4         5        6        7        8        9      10      11      12    13
            // $DMTP,timestamp,latitude,longitude,accuracy,altitudeM,altUncMM,speedKPH,speedUnc,heading,cellLat,cellLon,#sats,assist
            if (fld.length >= 10) {

                /* parse */
                long   fixtime   = StringTools.parseLong(  fld[1], 0L);     // seconds
                double latitude  = StringTools.parseDouble(fld[2], 0.0);    // degrees
                double longitude = StringTools.parseDouble(fld[3], 0.0);    // degrees
                double accuracyM = StringTools.parseDouble(fld[4], -1.0);   // meters
                double altMeters = StringTools.parseDouble(fld[5], 0.0);    // meters
              //double altUncM   = StringTools.parseDouble(fld[6], 0.0);    // meters
                double speedKPH  = StringTools.parseDouble(fld[7], 0.0);    // kph
              //double speedUnc  = StringTools.parseDouble(fld[8], 0.0);    // kph
                double heading   = StringTools.parseDouble(fld[9], -1.0);   // degrees

                /* horizontal accuracy? */
                // Reject this fix if the accuracy is too far off?
                // If enabled, PROP_GPS_ACCURACY should not be less than about 50.0 meters
                double minAccuracyM = Props.getDouble(Props.PROP_GPS_ACCURACY, 0, 0.0);
                if (minAccuracyM < 15.0) { minAccuracyM = 15.0; } // absolute minimum
                boolean accuracyIsOK = ((minAccuracyM <= 0.0) || (accuracyM < minAccuracyM));

                /* valid lat/lon? */
                if (!accuracyIsOK) {
                    // out of acceptable accuracy range
                    Log.warn(LOG_NAME, "Unacceptable accuracy: " + accuracyM);
                } else
                if ((latitude  >=  90.0) || (latitude  <=  -90.0) ||
                    (longitude >= 180.0) || (longitude <= -180.0)   ) {
                    // lat/lon appears to be invalid!
                    Log.warn(LOG_NAME, "Invalid lat/lon: " + latitude + "/" + longitude);
                } else {
                    //Log.warn(LOG_NAME, "Valid DMTP lat/lon: " + latitude + "/" + longitude);
                    synchronized (this.gpsLock) {
                        this.gpsGPRMC_time = fixtime;
                        this.gpsGPGGA_time = fixtime;
                        this.gpsEvent.setTimestamp(fixtime);
                        this.gpsEvent.setLatitude (latitude);
                        this.gpsEvent.setLongitude(longitude);
                        this.gpsEvent.setAccuracy (accuracyM);
                        this.gpsEvent.setSpeedKPH (speedKPH);
                        this.gpsEvent.setHeading  (heading);
                        this.gpsEvent.setAltitude (altMeters);
                    }
                    validFix_GPRMC = true;
                    validFix_GPGGA = true;
                }

                /* count valid fix */
                synchronized (this.sampleLock) {
                    if (validFix_GPRMC) { 
                        this.sampleCount_A++;
                        this.lastValidTime = fixtime; 
                    } else {
                        this.sampleCount_V++;
                    }
                    this.lastSampleTime = DateTime.getCurrentTimeSec();
                }

                return validFix_GPRMC;
            } else {

                /* count invalid fix */
                synchronized (this.sampleLock) {
                    this.sampleCount_V++;
                    this.lastSampleTime = DateTime.getCurrentTimeSec();
                }

                return false;
            }
        }

        /* proper NMEA-0183 records below this line */
        if (!fld[0].startsWith("$GP")) {
            //Log.warn("GP", "Does not start with '$GP'");
            return false;
        }

        /* test checksum */
        // checksum (XOR sum of all characters between '$' and '*', exclusive)
        if (!this._hasValidChecksum(rawData)) {
            Log.warn(LOG_NAME, "Failed Checksum");
            return false;
        }

        // We cannot assume that the GPRMC will arrive before GPGGA, or visa-versa.
        // so we must handle either case (I've seen both cases).

        // $GPRMC
        if (fld[0].startsWith("$GPRMC")) {
            //Log.warn(LOG_NAME, "Found '$GPRMC'");
            if (fld.length < 9) {
                return false;
            } else
            if (fld[2].equals("A")) {
                // "A" - valid gps fix
                    
                /* parse */
                long   hms       = StringTools.parseLong(fld[1], 0L);
                long   dmy       = StringTools.parseLong(fld[9], 0L);
                long   fixtime   = this._getUTCSeconds(dmy, hms);
                double latitude  = this._parseLatitude (fld[3], fld[4]);
                double longitude = this._parseLongitude(fld[5], fld[6]);
                double knots     = StringTools.parseDouble(fld[7], -1.0);
                double heading   = StringTools.parseDouble(fld[8], -1.0);
                double speedKPH  = (knots >= 0.0)? (knots * KILOMETERS_PER_KNOT) : -1.0;
                
                /* valid lat/lon? */
                if ((latitude  <  90.0) && (latitude  >  -90.0) &&
                    (longitude < 180.0) && (longitude > -180.0)   ) {
                    //Log.warn(LOG_NAME, "Valid GPRMC lat/lon: " + latitude + "/" + longitude);
                    synchronized (this.gpsLock) {
                        this.gpsGPRMC_time = fixtime;
                        this.gpsEvent.setTimestamp(fixtime);
                        this.gpsEvent.setLatitude (latitude);
                        this.gpsEvent.setLongitude(longitude);
                        this.gpsEvent.setSpeedKPH (speedKPH);
                        this.gpsEvent.setHeading  (heading);
                    }
                    validFix_GPRMC = true;
                } else {
                    // We have an valid record, but the lat/lon appears to be invalid!
                    Log.warn(LOG_NAME, "Invalid GPRMC lat/lon");
                }
                
                /* count valid fix */
                synchronized (this.sampleLock) {
                    this.sampleCount_A++;
                    this.lastSampleTime = DateTime.getCurrentTimeSec();
                    if (validFix_GPRMC) { 
                        this.lastValidTime = fixtime; 
                    }
                }

                return validFix_GPRMC;
            } else {
                // "V" - invalid gps fix
                
                /* count invalid fix */
                synchronized (this.sampleLock) {
                    this.sampleCount_V++;
                    this.lastSampleTime = DateTime.getCurrentTimeSec();
                }

                return false;
            }
        }
        
        // $GPGGA
        if (fld[0].startsWith("$GPGGA")) {
            //Log.warn(LOG_NAME, "Found '$GPGGA'");
            if (fld.length < 10) {
                return false;
            } else
            if (!fld[6].equals("0")) {
                // "1" = GPS
                // "2" = DGPS
                
                /* parse */
                long   hms       = StringTools.parseLong(fld[1], 0L);
                long   dmy       = 0L; // we don't know the day
                long   fixtime   = this._getUTCSeconds(dmy, hms);
                double latitude  = this._parseLatitude (fld[2], fld[3]);
                double longitude = this._parseLongitude(fld[4], fld[5]);
                double hdop      = StringTools.parseDouble(fld[8], 0.0);
                int    fixtype   = (int)StringTools.parseLong(fld[6], 1L); // 1=GPS, 2=DGPS, 3=PPS?, ...
                double altitude  = StringTools.parseDouble(fld[9], 0.0);
                
                /* valid lat/lon? */
                if ((latitude  <  90.0) && (latitude  >  -90.0) &&
                    (longitude < 180.0) && (longitude > -180.0)   ) {
                    synchronized (this.gpsLock) {
                        this.gpsGPGGA_time = fixtime;
                        this.gpsEvent.setTimestamp(fixtime);
                        this.gpsEvent.setLatitude (latitude);
                        this.gpsEvent.setLongitude(longitude);
                        this.gpsEvent.setAltitude (altitude);
                        this.gpsEvent.setHDOP     (hdop);
                    }
                    validFix_GPGGA = true;
                } else {
                    // We have an valid record, but the lat/lon appears to be invalid!
                    Log.warn(LOG_NAME, "Invalid GPGGA lat/lon");
                }
                
                /* count valid fix */
                synchronized (this.sampleLock) {
                    if (validFix_GPGGA) {
                        this.lastValidTime = fixtime; 
                    }
                }

                return true;
            } else {
                // no valid fix
                return false;
            }
        }

        /* unrecognized record */
        return false;
    }

    // ------------------------------------------------------------------------

    /**
    * Checks if NMEA-0183 formatted String has valid checksum by calculating the
    * checksum of the payload and comparing that to the received checksum.
    * @param str NMEA-0183 formatted String to be checked.
    * @return true if checksum is valid, false otherwise.
    */
    private boolean _hasValidChecksum(String str)
    {
        int c = str.indexOf("*");
        if (c < 0) {
            // does not contain a checksum char
            return false;
        }
        String chkSum = str.substring(c + 1);
        byte cs[] = StringTools.parseHex(chkSum,null);
        if ((cs == null) || (cs.length != 1)) {
            // invalid checksum hex length
            return false;
        }
        int calcSum = this._calcChecksum(str);
        return (calcSum == ((int)cs[0] & 0xFF));
    }
    
    /**
    * Calculates the checksum for a NMEA-0183 formatted String, to allow it to be
    * compared against the received checksum.
    * @param str NMEA-0183 formatted String to be checksummed.
    * @return Checksum computed from input.
    */
    private int _calcChecksum(String str)
    {
        byte b[] = StringTools.getBytes(str);
        if (b == null) {
            return -1;
        } else {
            int cksum = 0, s = 0;
            if ((b.length > 0) && (b[0] == '$')) { s++; }
            for (; s < b.length; s++) {
                if (b[s] ==  '*') { break; }
                if (b[s] == '\r') { break; }
                if (b[s] == '\n') { break; }
                cksum = (cksum ^ b[s]) & 0xFF;
            }
            return cksum;
        }
    }

    // ----------------------------------------------------------------------------

    /**
    * Computes seconds in UTC time given values from GPS device.
    * @param dmy Date received from GPS in DDMMYY format, where DD is day, MM is month,
    * YY is year.
    * @param hms Time received from GPS in HHMMSS format, where HH is hour, MM is minute,
    * and SS is second.
    * @return Time in UTC seconds.
    */
    private long _getUTCSeconds(long dmy, long hms)
    {
    
        /* time of day [TOD] */
        int    HH  = (int)((hms / 10000L) % 100L);
        int    MM  = (int)((hms / 100L) % 100L);
        int    SS  = (int)(hms % 100L);
        long   TOD = (HH * 3600L) + (MM * 60L) + SS;
    
        /* current UTC day */
        long DAY;
        if (dmy > 0L) {
            int    yy  = (int)(dmy % 100L) + 2000;
            int    mm  = (int)((dmy / 100L) % 100L);
            int    dd  = (int)((dmy / 10000L) % 100L);
            long   yr  = ((long)yy * 1000L) + (long)(((mm - 3) * 1000) / 12);
            DAY        = ((367L * yr + 625L) / 1000L) - (2L * (yr / 1000L))
                         + (yr / 4000L) - (yr / 100000L) + (yr / 400000L)
                         + (long)dd - 719469L;
        } else {
            // we don't have the day, so we need to figure out as close as we can what it should be.
            long   utc = DateTime.getCurrentTimeSec();
            long   tod = utc % DateTime.DaySeconds(1);
            DAY        = utc / DateTime.DaySeconds(1);
            long   dif = (tod >= TOD)? (tod - TOD) : (TOD - tod); // difference should be small (ie. < 1 hour)
            if (dif > DateTime.HourSeconds(12)) { // 12 to 18 hours
                // > 12 hour difference, assume we've crossed a day boundary
                if (tod > TOD) {
                    // tod > TOD likely represents the next day
                    DAY++;
                } else {
                    // tod < TOD likely represents the previous day
                    DAY--;
                }
            }
        }
        
        /* return UTC seconds */
        long sec = DateTime.DaySeconds(DAY) + TOD;
        return sec;
        
    }

    /**
    * Parses latitude given values from GPS device.
    * @param s Latitude String from GPS device in ddmm.mm format.
    * @param d Latitude hemisphere, "N" for northern, "S" for southern.
    * @return Latitude parsed from GPS data, with appropriate sign based on hemisphere or
    * 90.0 if invalid latitude provided.
    */
    private double _parseLatitude(String s, String d)
    {
        double _lat = StringTools.parseDouble(s, 99999.0);
        if (_lat < 99999.0) {
            double lat = (double)((long)_lat / 100L); // _lat is always positive here
            lat += (_lat - (lat * 100.0)) / 60.0;
            return d.equals("S")? -lat : lat;
        } else {
            return 90.0; // invalid latitude
        }
    }
    
    /**
    * Parses longitude given values from GPS device.
    * @param s Longitude String from GPS device in ddmm.mm format.
    * @param d Longitude hemisphere, "E" for eastern, "W" for western.
    * @return Longitude parsed from GPS data, with appropriate sign based on hemisphere or
    * 180.0 if invalid longitude provided.
    */
    private double _parseLongitude(String s, String d)
    {
        double _lon = StringTools.parseDouble(s, 99999.0);
        if (_lon < 99999.0) {
            double lon = (double)((long)_lon / 100L); // _lon is always positive here
            lon += (_lon - (lon * 100.0)) / 60.0;
            return d.equals("W")? -lon : lon;
        } else {
            return 180.0; // invalid longitude
        }
    }

    // ------------------------------------------------------------------------

    /**
    * Returns the time that the last GPS event was returned from the GPS device.
    * This method is synchronized for thread safety.
    * @return Last sample time in seconds from epoch.
    */
    public long getLastSampleTime()
    {
        long time;
        synchronized (this.sampleLock) {
            time = this.lastSampleTime;
        }
        return time;
    }
    
    /**
    * Returns the time that the last valid GPS event was returned from the GPS device,
    * using the time from the GPS device. This method is synchronized for thread safety.
    * @return Last valid sample time in UTC seconds.
    */
    public long getLastValidTime()
    {
        long time;
        synchronized (this.sampleLock) {
            time = this.lastValidTime;
        }
        return time;
    }
    
    /**
    * Returns number of valid GPS samples (called "A" because that is how valid samples
    * are coded in NMEA-0183). This method is synchronized for thread safety.
    * @return Count of valid GPS samples.
    */
    public long getSampleCount_A()
    {
        long count;
        synchronized (this.sampleLock) {
            count = this.sampleCount_A;
        }
        return count;
    }
    
    /**
    * Returns number of invalid GPS samples (called "V" because that is how invalid samples
    * are coded in NMEA-0183). This method is synchronized for thread safety.
    * @return Count of invalid GPS samples.
    */
    public long getSampleCount_V()
    {
        long count;
        synchronized (this.sampleLock) {
            count = this.sampleCount_V;
        }
        return count;
    }
    
    /**
    * Returns restart count.  Currently returns '0', reserved for future use.
    * @return Count of restarts, currently returns '0'.
    */
    public long getRestartCount()
    {
        long count;
        synchronized (this.sampleLock) {
            count = this.restartCount;
        }
        return count;
    }
    
    // ------------------------------------------------------------------------
    // CThread.ThreadListener
    
    /**
    * Listener for initialization before thread starts. Currently, does nothing.
    */
    public void threadWillStart()
    {
        // ignore
    }
    
    /**
    * Listener for finalization when a thread is going to stop, allowing GPS device
    * to be closed.
    */
    public void threadWillStop()
    {
        try {
            Log.warn(LOG_NAME, "Forcing GPS connection closed ...");
            if (this.gpsDevice != null) {
                this.gpsDevice.closeDevice();
            }
        } catch (Throwable t) {
            // ignore
        }
    }
    
    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

    // ------------------------------------------------------------------------
    // if "<DataInputStream>.available()" works, this watchdog is not necessary.
    
    /**
    * Watches the GPS device to ensure that GPS acquisition thread gets interrupted
    * if samples are not being received.  Not needed if if "<DataInputStream>.available()"
    * returns the actual number of bytes ready to read.
    */
    private class GPSReadWatchdog
        extends CThread
        implements Runnable
    {
        
        /**
        * Creates the new watchdog thread.
        */
        public GPSReadWatchdog() {
            super("GPSWatchdog", null); // this);
            // started by CThread
        }
        
        /**
        * Indicates when the watchdog thread should stop, based on whether the acquire
        * thread is still running.
        * @return true when watchdog thread should stop.
        */
        public boolean shouldStop() {
            if (GPSReceiver.this.acquireThread == null) {
                return true;
            } else {
                return super.shouldStop();
            }
        }
        
        /**
        * Does the actual watching of the acquisition thread.
        */
        public void run() {
            
            long gpsServiceTime = 0L;
            boolean resetServiceTime = true;
            while (!this.shouldStop()) {
                
                /* wait until we're talking to the GPS receiver */
                if (!GPSReceiver.this.gpsDevice.isOpen()) {
                    //Log.warn(LOG_NAME+"WD", "No GPS service yet ...");
                    try { Thread.sleep(4000L); } catch (Throwable t) {}
                    resetServiceTime = true;
                    continue;
                }
                if (resetServiceTime) {
                    gpsServiceTime = DateTime.getCurrentTimeSec();
                    resetServiceTime = false;
                    //Log.warn(LOG_NAME+"WD", "GPS service started ...");
                }
            
                /* check timeout */
                long nowTime = DateTime.getCurrentTimeSec();
                long lastSampleTime = GPSReceiver.this.getLastSampleTime();
                if (lastSampleTime == 0L) { lastSampleTime = gpsServiceTime; }
                if ((lastSampleTime + 14L) < nowTime) {
                    Log.warn(LOG_NAME+"WD", "Interrupt!!!");
                    // Note: when using BlueCove, this does NOT interrupt the pending 'read()'
                    if (GPSReceiver.this.acquireThread != null) {
                        GPSReceiver.this.acquireThread.interrupt();
                    }
                }
                try { Thread.sleep(2000L); } catch (Throwable t) {}
            
            }
                
        }
    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

    //public static void main(String argv[])
    //{
    //    Props.initProps(null);
    //    try {
    //        GPSReceiver.init(new GPSDeviceImpl());
    //        GPSReceiver gps = GPSReceiver.getInstance();
    //        CThread.startThreads();
    //    } catch (Throwable t) {
    //        Log.error(LOG_NAME, "Unable to start GPSReceiver", t);
    //    }
    //}
    
}
