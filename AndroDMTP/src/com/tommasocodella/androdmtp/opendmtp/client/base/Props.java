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
//  This class handles the management of properties for the client.
// ----------------------------------------------------------------------------
// Change History:
//  2006/03/26  Martin D. Flynn
//     -Initial release
//  2006/05/07  Martin D. Flynn
//     -Added PROP_CMD_GEOF_ADMIN property.
//     -Added PROP_GEOF_VERSION property.
//     -Added PROP_GEOF_COUNT property.
//  2006/07/02  Martin D. Flynn
//     -Made PROP_STATE_ACCOUNT_ID and PROP_STATE_DEVICE_ID writable
//  2006/11/03  Elayne Man
//     -Include JavaDocs
//  2007/01/28  Martin D. Flynn
//     -Added 'propSetSave' function to override property 'save' attribute
//     -Added several new properties (see 'DMTPProps.java')
//     -Changed the following properties to store odometer values in 1 meter units:
//          PROP_GPS_ACCURACY, PROP_GPS_DISTANCE_DELTA, 
//          PROP_ODOMETER_#_VALUE, PROP_ODOMETER_#_LIMIT
// ----------------------------------------------------------------------------
package com.tommasocodella.androdmtp.opendmtp.client.base;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import com.tommasocodella.androdmtp.opendmtp.codes.ClientErrors;
import com.tommasocodella.androdmtp.opendmtp.codes.DMTPProps;
import com.tommasocodella.androdmtp.opendmtp.util.GeoPoint;
import com.tommasocodella.androdmtp.opendmtp.util.KeyValue;
import com.tommasocodella.androdmtp.opendmtp.util.Log;
import com.tommasocodella.androdmtp.opendmtp.util.Payload;
import com.tommasocodella.androdmtp.opendmtp.util.StringTools;

/**
* Client Property manager
*/
public class Props
    implements DMTPProps
{

    // ------------------------------------------------------------------------

    // this value should mirror the release version of the reference implementation
    // (grep for "RELEASE_VERSION" in all projects)
    public static final String RELEASE_VERSION          = "1.2.2";

    // This value should reflect the current version of the protocol document
    public static final String PROTOCOL_VERSION         = "0,2,2"; // must have comma separator!

    // ------------------------------------------------------------------------

    private static final String LOG_NAME                = "PROPS";
    
    // ------------------------------------------------------------------------
    
    private static final String DFT_FIRMWARE_VERSION    = "DMTP_J_" + RELEASE_VERSION;

    private static final String DFT_COPYRIGHT           = "";

    private static final String DFT_SERIAL_NUMBER       = "";
    private static final String DFT_UNIQUE_ID           = "0x000000000000";
    private static final String DFT_ACCOUNT_ID          = "";
    private static final String DFT_DEVICE_ID           = "";

    private static final String DFT_COMM_SETTINGS       = "";
    private static final String DFT_COMM_HOST           = "127.0.0.1";
    private static final String DFT_COMM_PORT           = "31000";
    
    private static final String DFT_ACCESS_PIN          = "0x3132333435363738"; // "12345678"

    // The following are typically not used on a J2ME capable phone as the phone 
    // itself manages the GPRS connection
    private static final String DFT_COMM_DNS_1          = "192.168.1.3";
    private static final String DFT_COMM_DNS_2          = "192.168.1.3";
    private static final String DFT_COMM_CONNECTION     = "";
    private static final String DFT_COMM_APN_NAME       = "";
    private static final String DFT_COMM_APN_SERVER     = "";
    private static final String DFT_COMM_APN_USER       = "";
    private static final String DFT_COMM_APN_PASSWORD   = "";
    private static final String DFT_COMM_APN_PHONE      = "";  // "*99***1#"
    private static final String DFT_COMM_APN_SETTINGS   = "";  // APN settings which are not handled in the above

    // ------------------------------------------------------------------------

    public  static final int    ERROR_INVALID_KEY       = 1;
    public  static final int    ERROR_INVALID_TYPE      = 2;
    public  static final int    ERROR_INVALID_LENGTH    = 3;
    public  static final int    ERROR_READ_ONLY         = 11;
    public  static final int    ERROR_WRITE_ONLY        = 12;
    public  static final int    ERROR_COMMAND_INVALID   = 22;
    public  static final int    ERROR_COMMAND_ERROR     = 23;

    // ------------------------------------------------------------------------

    public  static final int    MAX_ID_SIZE             = 20;  // Account/Device/Serial

    // ------------------------------------------------------------------------
    
    private static final int RO   = KeyValue.READONLY;
    private static final int WO   = KeyValue.WRITEONLY;
    private static final int SAVE = KeyValue.SAVE;

    private static KeyValue kvProps[] = {
    //           KeyCode                      Name               Type                Attr       Ndx  Init

    // --- local serial port configuration
    new KeyValue(PROP_CFG_XPORT_PORT        , "cfg.xpo.port"   , KeyValue.STRING   , RO       ,  1,  ""     ),
    new KeyValue(PROP_CFG_XPORT_BPS         , "cfg.xpo.bps"    , KeyValue.UINT32   , RO       ,  1,  ""     ),
    new KeyValue(PROP_CFG_XPORT_DEBUG       , "cfg.xpo.debug"  , KeyValue.BOOLEAN  , RO       ,  1,  "false"),
    new KeyValue(PROP_CFG_GPS_PORT          , "cfg.gps.port"   , KeyValue.STRING   , RO|SAVE  ,  1,  "rfcm" ),
    new KeyValue(PROP_CFG_GPS_BPS           , "cfg.gps.bps"    , KeyValue.UINT32   , RO|SAVE  ,  1,  "4800" ),
    new KeyValue(PROP_CFG_GPS_MODEL         , "cfg.gps.model"  , KeyValue.STRING   , RO       ,  1,  ""     ),
    new KeyValue(PROP_CFG_GPS_DEBUG         , "cfg.gps.debug"  , KeyValue.BOOLEAN  , RO       ,  1,  "false"),
  //new KeyValue(PROP_CFG_SERIAL0_PORT      , "cfg.sp0.port"   , KeyValue.STRING   , RO       ,  1,  ""     ),
  //new KeyValue(PROP_CFG_SERIAL0_BPS       , "cfg.sp0.bps"    , KeyValue.UINT32   , RO       ,  1,  ""     ),
  //new KeyValue(PROP_CFG_SERIAL0_DEBUG     , "cfg.sp0.debug"  , KeyValue.BOOLEAN  , RO       ,  1,  "false"),
  //new KeyValue(PROP_CFG_SERIAL1_PORT      , "cfg.sp1.port"   , KeyValue.STRING   , RO       ,  1,  ""     ),
  //new KeyValue(PROP_CFG_SERIAL1_BPS       , "cfg.sp1.bps"    , KeyValue.UINT32   , RO       ,  1,  ""     ),
  //new KeyValue(PROP_CFG_SERIAL1_DEBUG     , "cfg.sp1.debug"  , KeyValue.BOOLEAN  , RO       ,  1,  "false"),
  //new KeyValue(PROP_CFG_SERIAL2_PORT      , "cfg.sp2.port"   , KeyValue.STRING   , RO       ,  1,  ""     ),
  //new KeyValue(PROP_CFG_SERIAL2_BPS       , "cfg.sp2.bps"    , KeyValue.UINT32   , RO       ,  1,  ""     ),
  //new KeyValue(PROP_CFG_SERIAL2_DEBUG     , "cfg.sp2.debug"  , KeyValue.BOOLEAN  , RO       ,  1,  "false"),
  //new KeyValue(PROP_CFG_SERIAL3_PORT      , "cfg.sp3.port"   , KeyValue.STRING   , RO       ,  1,  ""     ),
  //new KeyValue(PROP_CFG_SERIAL3_BPS       , "cfg.sp3.bps"    , KeyValue.UINT32   , RO       ,  1,  ""     ),
  //new KeyValue(PROP_CFG_SERIAL3_DEBUG     , "cfg.sp3.debug"  , KeyValue.BOOLEAN  , RO       ,  1,  "false"),

    // --- commands
    new KeyValue(PROP_CMD_SAVE_PROPS        , "cmd.saveprops"  , KeyValue.COMMAND  , WO       ,  1,  null   ),
    new KeyValue(PROP_CMD_AUTHORIZE         , "cmd.auth"       , KeyValue.COMMAND  , WO       ,  1,  null   ),
    new KeyValue(PROP_CMD_STATUS_EVENT      , "cmd.status"     , KeyValue.COMMAND  , WO       ,  1,  null   ),
  //new KeyValue(PROP_CMD_SET_OUTPUT        , "cmd.output"     , KeyValue.COMMAND  , WO       ,  1,  null   ),
    new KeyValue(PROP_CMD_RESET             , "cmd.reset"      , KeyValue.COMMAND  , WO       ,  1,  null   ),

    // --- retained state properties
    new KeyValue(PROP_STATE_PROTOCOL        , "sta.proto"      , KeyValue.UINT8    , RO       ,  3,  PROTOCOL_VERSION ),
    new KeyValue(PROP_STATE_FIRMWARE        , "sta.firm"       , KeyValue.STRING   , RO       ,  1,  DFT_FIRMWARE_VERSION ),
    new KeyValue(PROP_STATE_COPYRIGHT       , "sta.copyright"  , KeyValue.STRING   , RO       ,  1,  DFT_COPYRIGHT ),
    new KeyValue(PROP_STATE_SERIAL          , "sta.serial"     , KeyValue.STRING   , RO       ,  1,  DFT_SERIAL_NUMBER ),
    new KeyValue(PROP_STATE_UNIQUE_ID       , "sta.unique"     , KeyValue.BINARY   , RO       ,  1,  DFT_UNIQUE_ID ),
    new KeyValue(PROP_STATE_ACCOUNT_ID      , "sta.account"    , KeyValue.STRING   ,    SAVE  ,  1,  DFT_ACCOUNT_ID ), // allow write
    new KeyValue(PROP_STATE_DEVICE_ID       , "sta.device"     , KeyValue.STRING   ,    SAVE  ,  1,  DFT_DEVICE_ID ),  // allow write
    new KeyValue(PROP_STATE_USER_ID         , "sta.user"       , KeyValue.STRING   ,    SAVE  ,  1,  "" ),  // allow write
    new KeyValue(PROP_STATE_USER_TIME       , "sta.user.time"  , KeyValue.UINT32   , RO|SAVE  ,  1,  "0"    ),
    new KeyValue(PROP_STATE_TIME            , "sta.time"       , KeyValue.UINT32   , RO       ,  1,  "0"    ),
    new KeyValue(PROP_STATE_GPS             , "sta.gpsloc"     , KeyValue.GPS      , RO|SAVE  ,  1,  "0"    ), 
    new KeyValue(PROP_STATE_GPS_DIAGNOSTIC  , "sta.gpsdiag"    , KeyValue.UINT32   , RO       ,  5,  "0,0,0,0,0" ), 
    new KeyValue(PROP_STATE_QUEUED_EVENTS   , "sta.evtqueue"   , KeyValue.UINT32   , RO       ,  2,  "0,0"    ), 
    new KeyValue(PROP_STATE_DEV_DIAGNOSTIC  , "sta.devdiag"    , KeyValue.UINT32   , RO|SAVE  ,  5,  "0,0,0,0,0" ), 

    // --- Communication protocol properties
    new KeyValue(PROP_COMM_SPEAK_FIRST      , "com.first"      , KeyValue.BOOLEAN  ,    SAVE  ,  1,  "true" ),
    new KeyValue(PROP_COMM_FIRST_BRIEF      , "com.brief"      , KeyValue.BOOLEAN  ,    SAVE  ,  1,  "false"),
    new KeyValue(PROP_COMM_MAX_CONNECTIONS  , "com.maxconn"    , KeyValue.UINT8    ,    SAVE  ,  3,  "4,1,120" ), // total/duplex/minutes
    new KeyValue(PROP_COMM_MIN_XMIT_DELAY   , "com.mindelay"   , KeyValue.UINT16   ,    SAVE  ,  1,  "1800" ),
    new KeyValue(PROP_COMM_MIN_XMIT_RATE    , "com.minrate"    , KeyValue.UINT32   ,    SAVE  ,  1,  "1800" ),
    new KeyValue(PROP_COMM_MAX_XMIT_RATE    , "com.maxrate"    , KeyValue.UINT32   ,    SAVE  ,  1,  "3600" ),
    new KeyValue(PROP_COMM_MAX_DUP_EVENTS   , "com.maxduplex"  , KeyValue.UINT8    ,    SAVE  ,  1,  "12"   ),
    new KeyValue(PROP_COMM_MAX_SIM_EVENTS   , "com.maxsimplex" , KeyValue.UINT8    ,    SAVE  ,  1,  "4"    ),

    // --- Communication connection properties
    new KeyValue(PROP_COMM_SETTINGS         , "com.settings"   , KeyValue.STRING   ,    SAVE  ,  1,  DFT_COMM_SETTINGS ),
    new KeyValue(PROP_COMM_DMTP_HOST        , "com.dmtp.host"  , KeyValue.STRING   ,    SAVE  ,  1,  DFT_COMM_HOST ),
    new KeyValue(PROP_COMM_DMTP_PORT        , "com.dmtp.port"  , KeyValue.UINT16   ,    SAVE  ,  1,  DFT_COMM_PORT ),
    new KeyValue(PROP_COMM_DNS_1            , "com.dns1"       , KeyValue.STRING   ,    SAVE  ,  1,  DFT_COMM_DNS_1 ),
    new KeyValue(PROP_COMM_DNS_2            , "com.dns2"       , KeyValue.STRING   ,    SAVE  ,  1,  DFT_COMM_DNS_2 ),
    new KeyValue(PROP_COMM_CONNECTION       , "com.connection" , KeyValue.STRING   ,    SAVE  ,  1,  DFT_COMM_CONNECTION ),
    new KeyValue(PROP_COMM_APN_NAME         , "com.apnname"    , KeyValue.STRING   ,    SAVE  ,  1,  DFT_COMM_APN_NAME ),
    new KeyValue(PROP_COMM_APN_SERVER       , "com.apnserv"    , KeyValue.STRING   ,    SAVE  ,  1,  DFT_COMM_APN_SERVER ),
    new KeyValue(PROP_COMM_APN_USER         , "com.apnuser"    , KeyValue.STRING   ,    SAVE  ,  1,  DFT_COMM_APN_USER ),
    new KeyValue(PROP_COMM_APN_PASSWORD     , "com.apnpass"    , KeyValue.STRING   ,    SAVE  ,  1,  DFT_COMM_APN_PASSWORD ),
    new KeyValue(PROP_COMM_APN_PHONE        , "com.apnphone"   , KeyValue.STRING   ,    SAVE  ,  1,  DFT_COMM_APN_PHONE ),
    new KeyValue(PROP_COMM_APN_SETTINGS     , "com.apnsett"    , KeyValue.STRING   ,    SAVE  ,  1,  DFT_COMM_APN_SETTINGS ),
    new KeyValue(PROP_COMM_MIN_SIGNAL       , "com.minsignal"  , KeyValue.UINT8    ,    SAVE  ,  1,  "7" ),
    new KeyValue(PROP_COMM_ACCESS_PIN       , "com.pin"        , KeyValue.BINARY   ,    SAVE  ,  8,  DFT_ACCESS_PIN ),

    // --- Packet/Data format properties
    new KeyValue(PROP_COMM_CUSTOM_FORMATS   , "com.custfmt"    , KeyValue.UINT8    ,    SAVE  ,  1,  "0"    ),
    new KeyValue(PROP_COMM_ENCODINGS        , "com.encodng"    , KeyValue.UINT8    ,    SAVE  ,  1,  "7"    ),
    new KeyValue(PROP_COMM_BYTES_READ       , "com.rdcnt"      , KeyValue.UINT32   ,    SAVE  ,  1,  "0"    ),
    new KeyValue(PROP_COMM_BYTES_WRITTEN    , "com.wrcnt"      , KeyValue.UINT32   ,    SAVE  ,  1,  "0"    ),

    // --- GPS properties
    new KeyValue(PROP_GPS_SAMPLE_RATE       , "gps.smprate"    , KeyValue.UINT16   ,    SAVE  ,  1,  "8"    ),
    new KeyValue(PROP_GPS_ACQUIRE_WAIT      , "gps.aquwait"    , KeyValue.UINT16   ,    SAVE  ,  1,  "0"    ),
    new KeyValue(PROP_GPS_EXPIRATION        , "gps.expire"     , KeyValue.UINT16   ,    SAVE  ,  1,  "0"    ),
    new KeyValue(PROP_GPS_CLOCK_DELTA       , "gps.updclock"   , KeyValue.BOOLEAN  ,    SAVE  ,  1,  "0"    ),
    new KeyValue(PROP_GPS_ACCURACY          , "gps.accuracy"   , KeyValue.UINT16   ,    SAVE  ,  1,  "0"    ),
    new KeyValue(PROP_GPS_MIN_SPEED         , "gps.minspd"     , KeyValue.UDEC16   ,    SAVE  ,  1,  "8.0"  ),
    new KeyValue(PROP_GPS_DISTANCE_DELTA    , "gps.dstdelt"    , KeyValue.UINT32   ,    SAVE  ,  1,  "500"  ),

    // --- Geofence properties
    new KeyValue(PROP_CMD_GEOF_ADMIN        , "geo.admin"      , KeyValue.COMMAND  , WO       ,  1,  null   ),
    new KeyValue(PROP_GEOF_COUNT            , "geo.count"      , KeyValue.UINT16   , RO       ,  1,  "0"    ),
    new KeyValue(PROP_GEOF_VERSION          , "geo.version"    , KeyValue.STRING   ,    SAVE  ,  1,  ""     ),
    new KeyValue(PROP_GEOF_ARRIVE_DELAY     , "geo.arr.delay"  , KeyValue.UINT32   ,    SAVE  ,  1,  "30"   ), 
    new KeyValue(PROP_GEOF_DEPART_DELAY     , "geo.dep.delay"  , KeyValue.UINT32   ,    SAVE  ,  1,  "10"   ), 
    new KeyValue(PROP_GEOF_CURRENT          , "geo.current"    , KeyValue.UINT32   ,    SAVE  ,  1,  "0"    ), 

    // --- GeoCorr properties
  //new KeyValue(PROP_GEOC_VIOLATION_INTRVL , "gco.vio.rate"   , KeyValue.UINT16   ,    SAVE  ,  1,  "120"  ), 
  //new KeyValue(PROP_GEOC_VIOLATION_COUNT  , "gco.vio.cnt"    , KeyValue.UINT16   ,    SAVE  ,  1,  "0"    ), 
  //new KeyValue(PROP_GEOC_ACTIVE_ID        , "gco.active"     , KeyValue.UINT32   ,    SAVE  ,  1,  "0"    ), 

    // --- motion properties
    new KeyValue(PROP_MOTION_START_TYPE     , "mot.start.type" , KeyValue.UINT8    ,    SAVE  ,  1,  "0"    ), // speed 
    new KeyValue(PROP_MOTION_START          , "mot.start"      , KeyValue.UDEC16   ,    SAVE  ,  1,  "0.0"  ), // kph
    new KeyValue(PROP_MOTION_IN_MOTION      , "mot.inmotion"   , KeyValue.UINT16   ,    SAVE  ,  1,  "0"    ), // seconds
    new KeyValue(PROP_MOTION_STOP           , "mot.stop"       , KeyValue.UINT16   ,    SAVE  ,  1,  "600"  ), // seconds
    new KeyValue(PROP_MOTION_STOP_TYPE      , "mot.stop.type"  , KeyValue.UINT8    ,    SAVE  ,  1,  "0"    ),
    new KeyValue(PROP_MOTION_DORMANT_INTRVL , "mot.dorm.rate"  , KeyValue.UINT32   ,    SAVE  ,  1,  "0"    ), // seconds
    new KeyValue(PROP_MOTION_DORMANT_COUNT  , "mot.dorm.cnt"   , KeyValue.UINT16   ,    SAVE  ,  1,  "1"    ),
    new KeyValue(PROP_MOTION_EXCESS_SPEED   , "mot.exspeed"    , KeyValue.UDEC16   ,    SAVE  ,  1,  "0.0"  ), // kph
  //new KeyValue(PROP_MOTION_MOVING_INTRVL  , "mot.dorm.rate"  , KeyValue.UINT16   ,    SAVE  ,  1,  "0"    ), // seconds

    // --- odometer properties
    new KeyValue(PROP_ODOMETER_0_VALUE      , "odo.0.value"    , KeyValue.UINT32   ,    SAVE  ,  1,  "0"    ),
    new KeyValue(PROP_ODOMETER_1_VALUE      , "odo.1.value"    , KeyValue.UINT32   ,    SAVE  ,  1,  "0"    ),
    new KeyValue(PROP_ODOMETER_2_VALUE      , "odo.2.value"    , KeyValue.UINT32   ,    SAVE  ,  1,  "0"    ),
    new KeyValue(PROP_ODOMETER_0_LIMIT      , "odo.0.limit"    , KeyValue.UINT32   ,    SAVE  ,  1,  "0"    ),
    new KeyValue(PROP_ODOMETER_1_LIMIT      , "odo.1.limit"    , KeyValue.UINT32   ,    SAVE  ,  1,  "0"    ),
    new KeyValue(PROP_ODOMETER_2_LIMIT      , "odo.2.limit"    , KeyValue.UINT32   ,    SAVE  ,  1,  "0"    ),
    new KeyValue(PROP_ODOMETER_0_GPS        , "odo.0.gps"      , KeyValue.GPS      , RO|SAVE  ,  1,  "0,0,0"),
    new KeyValue(PROP_ODOMETER_1_GPS        , "odo.1.gps"      , KeyValue.GPS      , RO|SAVE  ,  1,  "0,0,0"),
    new KeyValue(PROP_ODOMETER_2_GPS        , "odo.2.gps"      , KeyValue.GPS      , RO|SAVE  ,  1,  "0,0,0"),

    // --- Elapsed time properties
    // omitted

    // --- Generic sensor properties
    // omitted

    // --- Temperature properties
    // omitted

    };

    // ------------------------------------------------------------------------

    public interface SavePropsCallBack
    {
        public void saveProps();
    }

    // ------------------------------------------------------------------------

    private static Vector               kvOrd       = null;
    private static Hashtable            nameMap     = null;
    private static Hashtable            codeMap     = null;
    private static SavePropsCallBack    saveProps   = null;
    
    /**
    * Private constructor, no instances are allowed.
    */
    private Props()
    {
        // no instance allowed
    }
    
    /**
    * Initialize the Properties
    */
    public static void initProps(SavePropsCallBack saveProps)
    {
        //Log.info(LOG_NAME, "Initilizing Properties ...");
        for (int i = 0; i < kvProps.length; i++) {
            Props.putKeyValue(kvProps[i]);
        }
        Props.saveProps = saveProps;
    }

    // ------------------------------------------------------------------------

    /**
    *** Save properties
    **/
    public static boolean saveProps()
    {
        if (Props.saveProps != null) {
            Props.saveProps.saveProps();
            return true;
        } else {
            return false;
        }
    }

    // ------------------------------------------------------------------------

    /**
    * Load from Auxiliary Store.
    * @param auxStore the Auxiliary Store value.
    * @return if it has successfully completed.
    */
    public static boolean loadFromStore(AuxiliaryStore auxStore)
    {
        Vector v = (auxStore != null)? auxStore.readData() : null;
        if (v != null) {
            for (Enumeration e = v.elements(); e.hasMoreElements();) {
                String keyEqualsValue = (String)e.nextElement();
                Props.initFromString(keyEqualsValue); // parse "key=value"
            }
            return true;
        } else {
            return false;
        }
    }
    
    // ------------------------------------------------------------------------

    /**
    * Adds a key value to this Property container
    * @param kv The key value.
    */
    public static void putKeyValue(KeyValue kv)
    {
        if (Props.kvOrd   == null) { Props.kvOrd   = new Vector(); }
        if (Props.nameMap == null) { Props.nameMap = new Hashtable(); }
        if (Props.codeMap == null) { Props.codeMap = new Hashtable(); }
        Props.kvOrd.addElement(kv);
        Props.nameMap.put(kv.getKeyName(), kv);
        Props.codeMap.put(new Integer(kv.getKeyCode()), kv);
    }

    /**
    * Resets all properties value to their default values.
    */
    public static void resetToDefault()
    {
        for (Enumeration i = Props.getKeyValues(); i.hasMoreElements();) {
            KeyValue kv = (KeyValue)i.nextElement();
            kv.resetToDefault();
        }
    }
        
    // ------------------------------------------------------------------------

    /**
    * Returns an enumeration of key values
    * @return The key value enumeration
    */
    public static Enumeration getKeyValues()
    {
        return Props.kvOrd.elements();
    }
    
    // ------------------------------------------------------------------------

    /**
    * Returns the key value for a given property key code.
    * @param code The property key code.
    * @return The Key value.
    */
    public static KeyValue getKeyValue(int code)
    {
        if (Props.codeMap != null) {
            return (KeyValue)Props.codeMap.get(new Integer(code));
        } else {
            Log.error(LOG_NAME, "Not initialized!!");
            return null;
        }
    }

    /**
    * Returns the key value for a given property key name.
    * @param name The property key name.
    * @return the Key value.
    */
    public static KeyValue getKeyValue(String name)
    {
        if (name == null) {
            return null;
        } else
        if (name.startsWith("0x") || name.startsWith("0X")) {
            return Props.getKeyValue((int)StringTools.parseHexLong(name, 0x0000L));
        } else
        if (Props.nameMap != null) {
            return (KeyValue)Props.nameMap.get(name);
        } else {
            Log.error(LOG_NAME, "Not initialized!!");
            return null;
        }
    }

    // ------------------------------------------------------------------------
    
    /**
    * Initializes a key value from a string.
    * @param key The key value.
    * @param val The string value.
    * @param setAsDefault The default value
    * @return If it has successfully been initialized.
    */
    public static boolean initFromString(int key, String val, boolean setAsDefault)
    {
        KeyValue kv = Props.getKeyValue(key);
        return (kv != null)? kv.initFromString(val,setAsDefault) : false;
    }

    /**
    * Initializes a key from a string.
    * @param key The key value.
    * @param val The string value.
    * @return If it has successfully been initialized.
    */
    public static boolean initFromString(int key, String val)
    {
        KeyValue kv = Props.getKeyValue(key);
        return (kv != null)? kv.initFromString(val) : false;
    }
    
    /**
    * Initializes a key from a string.
    * @param key The key value.
    * @param val The string value.
    * @return If it has successfully been initialized.
    */
    public static boolean initFromString(String key, String val)
    {
        KeyValue kv = Props.getKeyValue(key);
        return (kv != null)? kv.initFromString(val) : false;
    }
   
    /**
    * Initializes a key from a string.
    * @param keyEqualsVal The string value.
    * @return If it has successfully been initialized.
    */
    public static boolean initFromString(String keyEqualsVal)
    {
        // Parse/init "key=value"
        String keyVal[] = KeyValue.parseKeyValue(keyEqualsVal);
        if ((keyVal != null) && (keyVal.length == 2) && (keyVal[0] != null)) {
            return Props.initFromString(keyVal[0], keyVal[1]);
        } else {
            return false;
        }
    }

    // ------------------------------------------------------------------------

    /**
    * Sets the specified property key code to 'changed'
    * @param key The property key code
    * @return If the key 'changed' state has successfully been set.
    */
    public static boolean setChanged(int key)
    {
        KeyValue kv = Props.getKeyValue(key);
        if (kv != null) {
            kv.setChanged();
            return true;
        } else {
            return false;
        }
    }

    /**
    * Sets the specified property key name to 'changed'
    * @param key The property key name
    * @return If the key 'changed' state has successfully been set.
    */
    public static boolean setChanged(String key)
    {
        KeyValue kv = Props.getKeyValue(key);
        if (kv != null) {
            kv.setChanged();
            return true;
        } else {
            return false;
        }
    }

    /**
    * Clears the 'changed' state for the specified property key code
    * @param key The property key code
    * @return If the key 'changed' state has successfully been cleared.
    */
    public static boolean clearChanged(int key)
    {
        KeyValue kv = Props.getKeyValue(key);
        if (kv != null) {
            kv.clearChanged();
            return true;
        } else {
            return false;
        }
    }

    /**
    * Clears the 'changed' state for the specified property key name
    * @param key The property key name
    * @return If the key 'changed' state has successfully been cleared.
    */
    public static boolean clearChanged(String key)
    {
        KeyValue kv = Props.getKeyValue(key);
        if (kv != null) {
            kv.clearChanged();
            return true;
        } else {
            return false;
        }
    }

    // ------------------------------------------------------------------------

    /**
    * Sets the 'read-only' state of the specified property key code
    * @param key The property key code
    * @param readOnly The read only state.
    * @return true if this property key was found, false otherwise
    */
    public static boolean setReadOnly(int key, boolean readOnly)
    {
        KeyValue kv = Props.getKeyValue(key);
        if (kv != null) { 
            kv.setReadOnly(readOnly);
            return true;
        } else {
            return false;
        }
    }

    // ------------------------------------------------------------------------

    /**
    * Sets the change listener for the specified property key
    * @param key The key code.
    * @param changeListener The ChangeListener value.
    * @return true if this property key was found, false otherwise
    */
    public static boolean setChangeListener(int key, KeyValue.ChangeListener changeListener)
    {
        KeyValue kv = Props.getKeyValue(key);
        if (kv != null) { 
            kv.setChangeListener(changeListener);
            return true;
        } else {
            return false;
        }
    }

    // ------------------------------------------------------------------------

    /**
    * Sets the command handler for the specified property key
    * @param key The key code
    * @param cmd The Command handler
    * @return true if it has successfully been set.
    */
    public static boolean setCommandHandler(int key, KeyValue.CommandHandler cmd)
    {
        KeyValue kv = Props.getKeyValue(key);
        if (kv != null) { 
            return kv.setCommandHandler(cmd);
        } else {
            return false;
        }
    }

    // ------------------------------------------------------------------------

    /**
    * Returns a packet payload containing the property value
    * @param key The key code.
    * @return The payload value.
    * @throws ClientErrorException If the an error occurs
    */
    public static Payload getPayload(int key)
        throws ClientErrorException
    {
        // return the specified property in a Payload
        
        /* get KeyValue */
        KeyValue kv = Props.getKeyValue(key);
        if (kv == null) {
            throw new ClientErrorException(ClientErrors.ERROR_PROPERTY_INVALID_ID);
        } else
        if (kv.isWriteOnly()) {
            throw new ClientErrorException(ClientErrors.ERROR_PROPERTY_WRITE_ONLY);
        }
        
        /* return payload */
        Payload p = kv.getPayload(-1);
        if (p == null) {
            throw new ClientErrorException(ClientErrors.ERROR_PROPERTY_UNKNOWN_ERROR);
        }
        return p;
        
    }

    /**
    * Sets the value of the specified property key to the data found in the specified payload.
    * @param key The key code.
    * @param payload The payload value
    * @throws ClientErrorException If an error occurs
    */
    public static void setPayload(int key, Payload payload)
        throws ClientErrorException
    {
        // parse property values from the specified Payload
        
        /* get KeyValue */
        KeyValue kv = Props.getKeyValue(key);
        if (kv == null) {
            throw new ClientErrorException(ClientErrors.ERROR_PROPERTY_INVALID_ID);
        } else
        if (kv.isReadOnly()) {
            throw new ClientErrorException(ClientErrors.ERROR_PROPERTY_READ_ONLY);
        }
        
        /* Command? */
        if (kv.isCommand()) {
            byte b[] = payload.readBytes(payload.getAvail());
            KeyValue.CommandHandler ch = kv.getCommandHandler();
            if (ch != null) {
                int cmdErr = ch.command(key, -1, b);
                if (cmdErr > 0) {
                    throw new ClientErrorException(ClientErrors.ERROR_COMMAND_ERROR, cmdErr);
                }
            } else {
                throw new ClientErrorException(ClientErrors.ERROR_COMMAND_INVALID, 0);
            }
        }
        
        /* non-command */
        kv.setPayload(payload);

    }
    
    // ------------------------------------------------------------------------

    /**
    * Returns a byte array containing the value of the specified property key
    * @param key The key code
    * @param dft The default value
    * @return The byte array
    */
    public static byte[] getByteArray(int key, byte dft[])
    {
        KeyValue kv = Props.getKeyValue(key);
        if (kv != null) { 
            return kv.getByteArray(dft);
        } else {
            return dft;
        }
    }

    /**
    * Sets the specified property key to the value specified by the byte array
    * @param key The key code
    * @param val the byte array
    * @return true, if the value was successfully set
    */
    public static boolean setByteArray(int key, byte val[])
    {
        KeyValue kv = Props.getKeyValue(key);
        if (kv != null) { 
            return kv.setByteArray(val);
        } else {
            return false;
        }
    }

    // ------------------------------------------------------------------------

    /**
    * Returns value of the specified property key as a boolean
    * @param key The key code
    * @param ndx The value index
    * @param dft The default value
    * @return The boolean value of the property key
    */
    public static boolean getBoolean(int key, int ndx, boolean dft)
    {
        KeyValue kv = Props.getKeyValue(key);
        if (kv != null) { 
            return kv.getBoolean(ndx, dft);
        } else {
            return dft;
        }
    }
    
    /**
    * Sets the value of the specified property key as a boolean
    * @param key The key code.
    * @param ndx The value index
    * @param val The boolean value.
    * @return If the value has been sucessfully set.
    */
    public static boolean setBoolean(int key, int ndx, boolean val)
    {
        KeyValue kv = Props.getKeyValue(key);
        if (kv != null) { 
            return kv.setBoolean(val, ndx);
        } else {
            return false;
        }
    }

    // ------------------------------------------------------------------------

    /**
    * Returns value of the specified property key as a long
    * @param key The key code
    * @param ndx The value index
    * @param dft The default value
    * @return The long value of the property key
    */
    public static long getLong(int key, int ndx, long dft)
    {
        KeyValue kv = Props.getKeyValue(key);
        if (kv != null) { 
            return kv.getLong(ndx, dft);
        } else {
            return dft;
        }
    }

    /**
    * Sets the value of the specified property key as a long
    * @param key The key code.
    * @param ndx The value index
    * @param val The long value.
    * @return If the value has been sucessfully set.
    */
    public static boolean setLong(int key, int ndx, long val)
    {
        KeyValue kv = Props.getKeyValue(key);
        if (kv != null) { 
            return kv.setLong(val, ndx);
        } else {
            return false;
        }
    }
    
    /**
    * Adds the specified long value to the specified property key
    * @param key The key code
    * @param ndx The value index
    * @param val The long value to add
    * @return The resulting long value
    */
    public static long addLong(int key, int ndx, long val)
    {
        long n = Props.getLong(key, ndx, 0L) + val;
        return Props.setLong(key, ndx, n)? n : 0L;
    }

    // ------------------------------------------------------------------------

    /**
    * Returns value of the specified property key as a double
    * @param key The key code
    * @param ndx The value index
    * @param dft The default value
    * @return The double value of the property key
    */
    public static double getDouble(int key, int ndx, double dft)
    {
        KeyValue kv = Props.getKeyValue(key);
        if (kv != null) { 
            return kv.getDouble(ndx, dft);
        } else {
            return dft;
        }
    }

    /**
    * Sets the value of the specified property key as a double
    * @param key The key code.
    * @param ndx The value index
    * @param val The double value.
    * @return If the value has been sucessfully set.
    */
    public static boolean setDouble(int key, int ndx, double val)
    {
        KeyValue kv = Props.getKeyValue(key);
        if (kv != null) { 
            return kv.setDouble(val, ndx);
        } else {
            return false;
        }
    }

    /**
    * Adds the specified double value to the specified property key
    * @param key The key code
    * @param ndx The value index
    * @param val The double value to add
    * @return The resulting double value
    */
    public static double addDouble(int key, int ndx, double val)
    {
        double n = Props.getDouble(key, ndx, 0.0) + val;
        return Props.setDouble(key, ndx, n)? n : 0.0;
    }

    // ------------------------------------------------------------------------

    /**
    * Returns value of the specified property key as a string
    * @param key The key code
    * @param dft The default value
    * @return The string value of the property key
    */
    public static String getString(int key, String dft)
    {
        KeyValue kv = Props.getKeyValue(key);
        if (kv != null) { 
            return kv.getString(dft);
        } else {
            return dft;
        }
    }

    /**
    * Sets the value of the specified property key as a string
    * @param key The key code.
    * @param val The string value.
    * @return If the value has been sucessfully set.
    */
    public static boolean setString(int key, String val)
    {
        KeyValue kv = Props.getKeyValue(key);
        if (kv != null) { 
            return kv.setString(val);
        } else {
            return false;
        }
    }

    // ------------------------------------------------------------------------

    /**
    * Returns value of the specified property key as a GeoPoint
    * @param key The key code
    * @param dft The default value
    * @return The GeoPoint value of the property key
    */
    public static GeoPoint getGeoPoint(int key, GeoPoint dft)
    {
        KeyValue kv = Props.getKeyValue(key);
        if (kv != null) { 
            return kv.getGeoPoint(dft);
        } else {
            return dft;
        }
    }
    
    /**
    * Sets the value of the specified property key as a GeoPoint
    * @param key The key code.
    * @param val The GeoPoint value.
    * @return If the value has been sucessfully set.
    */
    public static boolean setGeoPoint(int key, GeoPoint val)
    {
        KeyValue kv = Props.getKeyValue(key);
        if (kv != null) { 
            return kv.setGeoPoint(val);
        } else {
            return false;
        }
    }

    // ------------------------------------------------------------------------

    /**
    * Returns true if any key value has changed.
    * @return True if a key value has changed, false otherwise
    */
    public static boolean hasChanged()
    {
        for (Enumeration i = Props.getKeyValues(); i.hasMoreElements();) {
            KeyValue kv = (KeyValue)i.nextElement();
            if (kv.hasChanged()) {
                return true;
            }
        }
        return false;
    }
    
    /**
    * Clears the changed state for all property keys
    */
    public static void clearChanged()
    {
        for (Enumeration i = Props.getKeyValues(); i.hasMoreElements();) {
            KeyValue kv = (KeyValue)i.nextElement();
            kv.clearChanged();
        }
    }
    
    /**
    * Saves the current property state to auxiliary storage
    * @param auxStore The auxiliary store
    * @param all true to write all property values, false to only write non-default values.
    * @return if successfully saved.
    */
    public static boolean saveToStore(AuxiliaryStore auxStore, boolean all)
    {
        if (Props.hasChanged()) {
            // Include only 'savable' properties
            Log.debug(LOG_NAME, "Saving properties ...");
            Vector v = new Vector();
            for (Enumeration i = Props.getKeyValues(); i.hasMoreElements();) {
                KeyValue kv = (KeyValue)i.nextElement();
                if (kv.isSave() && !kv.isDefault()) {
                    String kvStr = kv.toString();
                    Log.debug(LOG_NAME, "   " + kvStr);
                    v.addElement(kvStr);
                }
            }
            auxStore.writeData(v);
            Props.clearChanged();
            return true;
        } else {
            return false;
        }
    }
    
    /**
    * Prints the key values.
    * @param all true to print all property values, false to only print non-default values.
    */
    public static void print(boolean all)
    {
        // Include everything except commands
        StringBuffer sb = new StringBuffer();
        for (Enumeration i = Props.getKeyValues(); i.hasMoreElements();) {
            KeyValue kv = (KeyValue)i.nextElement();
            if (!kv.isCommand()) {
                sb.append(kv.toString());
                sb.append("\n");
            }
        }
        System.out.println(sb.toString());
    }
     
    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
    * Interface for the Auxiliary Store.
    */
    public interface AuxiliaryStore
    {
        /**
        * Writes the property values contained in the Vector to auxiliary storage
        * @param rcds the vector data
        * @return true if completed successfully
        */
        public boolean writeData(Vector rcds);
        /**
        * Reads property data from auxiliary storage
        * @return the Vector data
        */
        public Vector readData();
    }
    
    // ------------------------------------------------------------------------
    
}

