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
//  OpenDMTP protocol property keys.
// ----------------------------------------------------------------------------
// Change History:
//  2006/03/26  Martin D. Flynn
//     -Initial release
//  2006/05/07  Martin D. Flynn
//     -Added PROP_CMD_GEOF_ADMIN property.
//     -Added PROP_GEOF_VERSION property.
//     -Added PROP_GEOF_COUNT property.
//     -GeoCorr property types renamed to PROP_GEOC_...
//     -Relocated PROP_GEOC_ACTIVE_ID property key (was 0xF561)
//     -Relocated PROP_GEOC_VIOLATION_INTRVL property key (was 0xF571)
//     -Relocated PROP_GEOC_VIOLATION_COUNT property key (was 0xF572)
//  2006/11/03  Robert Puckett
//      Include JavaDocs
//  2007/01/28  Martin D. Flynn
//     -Relocated PROP_CFG_GPS_MODEL from 0xEF22 to 0xEF2A.  This was done to
//      allow other serial port configurations for 0xEFX2 through 0xEFX9.
//     -Relocated PROP_COMM_APN_SETTINGS from 0xF3AA to 0xF3AC
//     -Relocated PROP_GEOC_ACTIVE_ID (again) property key (was 0xF571)
//     -Added the following properties: 
//      PROP_CFG_XPORT_BPS, PROP_CFG_GPS_BPS,
//      PROP_GPS_CLOCK_DELTA, PROP_CFG_SERIAL0_BPS, PROP_CFG_SERIAL1_BPS,
//      PROP_COMM_CONNECTION, PROP_COMM_APN_PHONE, PROP_CMD_AUTHORIZE,
//      PROP_STATE_USER_ID and PROP_STATE_USER_TIME, PROP_TEMP_SAMPLE_INTRVL,
//      PROP_TEMP_REPORT_INTRVL, PROP_CMD_SAVE_PROPS, PROP_CMD_RESET,
//      PROP_STATE_DEV_DIAGNOSTIC, PROP_COMM_MIN_SIGNAL, PROP_INPUT_CONFIG_#,
//      PROP_INPUT_STATE, PROP_OUTPUT_CONFIG_#, PROP_ELAPSED_#_VALUE, 
//      PROP_UNDERVOLTAGE_LIMIT, PROP_COMM_ACCESS_PIN, PROP_CFG_SERIAL2_PORT
//      PROP_CFG_SERIAL2_BPS, PROP_CFG_SERIAL2_DEBUG, PROP_CMD_GEOC_ADMIN, 
//      PROP_MOTION_MOVING_INTRVL,
//     -The odometer unit of measurement has chenged from 0.1 meters to 1 meters
//      for the following properties:
//          PROP_GPS_ACCURACY, PROP_GPS_DISTANCE_DELTA, 
//          PROP_ODOMETER_#_VALUE, PROP_ODOMETER_#_LIMIT
//      A 0.1 meter resolution did not provide a high enough top end value that
//      could be represented in 4 bytes.  The change to 1 meter units provides a
//      maximum odometers value increase from about 267K miles (which many vehicles
//      have been able to attain) to about 2.67M miles (we'll beyond the life 
//      expectancey of most vehicles).  And 1 meter resolution is more than enough
//      to provide a very accurate GPS calculated odometer reading (at least well
//      within the accuracy of currently available GPS receivers).
// ----------------------------------------------------------------------------
package com.tommasocodella.androdmtp.opendmtp.codes;

// ----------------------------------------------------------------------------
// Reserved Property IDs: [E0-00 through FF-FF]
// ----------------------------------------------------------------------------
// Properties allow reading state, or setting state, on the device.
// Property attributes typically may be one or more of the following:
//  - Read-Only (cannot be changed by the server)
//  - State (maintains device state, may be read-only by server)
//  - Command (write-only) (invokes an action on the client when set)
//  - Configuration (sets a behaviour attribute)

// ----------------------------------------------------------------------------
// Argument/Value parsing rules:
// 1) For ASCIIZ types, parsing of the ASCII value stops at the first null
//    terminator, or when the end of the data buffer is encountered.  If a null
//    terminator is encountered, then any remaining data after the null may be
//    discarded.
// 2) For numeric/binary types, if the supplied data length is at least the length
//    which is required by the property, then the data is parsed as defined by the 
//    property, and any remaining data may be discarded.
// 3) For numeric types, if the supplied data length is less-than 
//    than specified as required by the property, then the supplied data is right 
//    shifted until the proper property required data length is achieved.  If the 
//    property value is signed, then most significant bit of the supplied data is 
//    extended into the bytes comprising the filler.  If the property defines a
//    multiple numeric element, the supplied length is divided evenly by the number 
//    of required elements (any remainder constitutes an error) and the quotient
//    number of bytes is parsed as described above for each element. 0-length data 
//    payloads will be interpreted as '0' numeric values.

// ----------------------------------------------------------------------------
// Notes:
// 1) The client is free to impose whatever limits it deems necessary on the 
//    property values set by the server.

/**
* Contains OpenDMTP protocol property keys that allow reading state, or setting state, on the
* device. <br>
* Property attributes typically may be one or more of the following: <br> - Read-Only (cannot be
* changed by the server) <br> - State (maintains device state, may be read-only by server) <br> -
* Command (write-only) (invokes an action on the client when set) <br> - Configuration (sets a
* behaviour attribute) <br>
* Reserved Property IDs: [E0-00 through FF-FF] <br>
* <br>
* ---------------------------------------------------------------------------- <br>
* Argument/Value parsing rules: <br>
* 1) For ASCIIZ types, parsing of the ASCII value stops at the first null terminator, or when the
* end of the data buffer is encountered. If a null terminator is encountered, then any remaining
* data after the null may be discarded. <br>
* 2) For numeric/binary types, if the supplied data length is at least the length which is required
* by the property, then the data is parsed as defined by the property, and any remaining data may
* be discarded. <br>
* 3) For numeric types, if the supplied data length is less-than than specified as required by the
* property, then the supplied data is right shifted until the proper property required data length
* is achieved. If the property value is signed, then most significant bit of the supplied data is
* extended into the bytes comprising the filler. If the property defines a multiple numeric
* element, the supplied length is divided evenly by the number of required elements (any remainder
* constitutes an error) and the quotient number of bytes is parsed as described above for each
* element. 0-length data payloads will be interpreted as '0' numeric values.
* <br>
* ---------------------------------------------------------------------------- <br>
* Notes: <br>
* 1) The client is free to impose whatever limits it deems necessary on the property values set by
* the server.
*/
public interface DMTPProps
{

    // ----------------------------------------------------------------------------
    // Reserved platform configuration properties [E000 through EFFF]
    
    // --- Transport media port config
    public static final int PROP_CFG_XPORT_PORT             = 0xEF11;
        // Description: [optional]
        //      [Read-Only] Serial port to which the transport media device is attached
        // Get Value: 
        //      0:X - [ASCIIZ] string representing the serial port name
        // Notes:
        //      - If needed, this value represents the serial port to which the transport
        //      media device is attached.  For instance, if the system is configured for
        //      GPRS data transport, then this value may represent the serial port to 
        //      which the GPRS modem is attached.
        //      - The read-only attribute of this property should be enforced by the client.
        //      to prevent the server from accidentilly changing this value.

    public static final int PROP_CFG_XPORT_BPS              = 0xEF12;
        // Description: [optional]
        //      [Read-Only] Communication speed (BPS) of the attached transport media device
        // Get Value: 
        //      0:4 - [UInt32] speed of serial port in BPS.
        // Notes:
        //      - The read-only attribute of this property should be enforced by the client.
        //      to prevent the server from accidentilly changing this value.

    public static final int PROP_CFG_XPORT_DEBUG            = 0xEF1D;
        // Description: [optional]
        //      [Read-Only] For use when debugging the transport media device
        // Get Value: 
        //      0:1 - [utBool] Non-Zero if debugging this transport media device
        // Notes:
        //      - Used only when debugging this transport media device.
    
    // --- GPS port config
    public static final int PROP_CFG_GPS_PORT               = 0xEF21;
        // Description: [optional]
        //      [Read-Only] Serial port to which the GPS device is attached
        // Get Value: 
        //      0:X - [ASCIIZ] string representing the serial port name
        // Notes:
        //      - The read-only attribute of this property should be enforced by the client.
        //      to prevent the server from accidentilly changing this value.

    public static final int PROP_CFG_GPS_BPS                = 0xEF22;
        // Description: [optional]
        //      [Read-Only] Communication speed (BPS) of the attached GPS device
        // Get Value: 
        //      0:4 - [UInt32] speed of serial port in BPS.
        // Notes:
        //      - For most GPS receivers, this value is typically 4800 bps.
        //      - The read-only attribute of this property should be enforced by the client.
        //      to prevent the server from accidentilly changing this value.

    public static final int PROP_CFG_GPS_MODEL              = 0xEF2A;  // was 0xEF22
        // Description: [optional]
        //      [Read-Only] The name/type of the attached GPS device
        // Get Value: 
        //      0:X - [ASCIIZ] string representing the name/type of the attached GPS device.
        // Notes:
        //      - This value may be used for custom initialization.
        //      - The read-only attribute of this property should be enforced by the client.
        //      to prevent the server from accidentilly changing this value.
    
    public static final int PROP_CFG_GPS_DEBUG              = 0xEF2D;
        // Description: [optional]
        //      [Read-Only] For use when debugging the GPS device
        // Get Value: 
        //      0:1 - [utBool] Non-Zero if debugging this GPS device
        // Notes:
        //      - Used only when debugging this GPS device.
    
    // --- General serial port 0 config
    public static final int PROP_CFG_SERIAL0_PORT           = 0xEF31;
        // Description: [optional]
        //      [Read-Only] General serial port 0 name
        // Get Value: 
        //      0:X - [ASCIIZ] string representing this serial port name
        // Notes:
        //      - The read-only attribute of this property should be enforced by the client.
        //      to prevent the server from accidentilly changing this value.

    public static final int PROP_CFG_SERIAL0_BPS            = 0xEF32;
        // Description: [optional]
        //      [Read-Only] Communication speed (BPS) of the attached serial port 0
        // Get Value: 
        //      0:4 - [UInt32] speed of serial port in BPS.
        // Notes:
        //      - The read-only attribute of this property should be enforced by the client.
        //      to prevent the server from accidentilly changing this value.

    public static final int PROP_CFG_SERIAL0_DEBUG          = 0xEF3D;
        // Description: [optional]
        //      [Read-Only] For use when debugging serial port 0
        // Get Value: 
        //      0:1 - [utBool] Non-Zero if debugging this serial port
        // Notes:
        //      - Used only when debugging this serial port.
    
    // --- General serial port 1 config
    public static final int PROP_CFG_SERIAL1_PORT           = 0xEF41;
        // Description: [optional]
        //      [Read-Only] General serial port 1
        // Get Value: 
        //      0:X - [ASCIIZ] string representing this serial port name
        // Notes:
        //      - The read-only attribute of this property should be enforced by the client.
        //      to prevent the server from accidentilly changing this value.

    public static final int PROP_CFG_SERIAL1_BPS            = 0xEF42;
        // Description: [optional]
        //      [Read-Only] Communication speed (BPS) of the attached serial port 1
        // Get Value: 
        //      0:4 - [UInt32] speed of serial port in BPS.
        // Notes:
        //      - The read-only attribute of this property should be enforced by the client.
        //      to prevent the server from accidentilly changing this value.

    public static final int PROP_CFG_SERIAL1_DEBUG          = 0xEF4D;
        // Description: [optional]
        //      [Read-Only] For use when debugging serial port 1
        // Get Value: 
        //      0:1 - [utBool] Non-Zero if debugging this serial port
        // Notes:
        //      - Used only when debugging this serial port.
    
    // --- General serial port 2 config
    public static final int PROP_CFG_SERIAL2_PORT           = 0xEF51;
        // Description: [optional]
        //      [Read-Only] General serial port 2
        // Get Value: 
        //      0:X - [ASCIIZ] string representing this serial port name
        // Notes:
        //      - The read-only attribute of this property should be enforced by the client.
        //      to prevent the server from accidentilly changing this value.

    public static final int PROP_CFG_SERIAL2_BPS            = 0xEF52;
        // Description: [optional]
        //      [Read-Only] Communication speed (BPS) of the attached serial port 2
        // Get Value: 
        //      0:4 - [UInt32] speed of serial port in BPS.
        // Notes:
        //      - The read-only attribute of this property should be enforced by the client.
        //      to prevent the server from accidentilly changing this value.

    public static final int PROP_CFG_SERIAL2_DEBUG          = 0xEF5D;
        // Description: [optional]
        //      [Read-Only] For use when debugging serial port 2
        // Get Value: 
        //      0:1 - [utBool] Non-Zero if debugging this serial port
        // Notes:
        //      - Used only when debugging this serial port.
    
    // --- General serial port 3 config
    public static final int PROP_CFG_SERIAL3_PORT           = 0xEF61;
        // Description: [optional]
        //      [Read-Only] General serial port 3
        // Get Value: 
        //      0:X - [ASCIIZ] string representing this serial port name
        // Notes:
        //      - The read-only attribute of this property should be enforced by the client.
        //      to prevent the server from accidentilly changing this value.

    public static final int PROP_CFG_SERIAL3_BPS            = 0xEF62;
        // Description: [optional]
        //      [Read-Only] Communication speed (BPS) of the attached serial port 3
        // Get Value: 
        //      0:4 - [UInt32] speed of serial port in BPS.
        // Notes:
        //      - The read-only attribute of this property should be enforced by the client.
        //      to prevent the server from accidentilly changing this value.

    public static final int PROP_CFG_SERIAL3_DEBUG          = 0xEF6D;
        // Description: [optional]
        //      [Read-Only] For use when debugging serial port 3
        // Get Value: 
        //      0:1 - [utBool] Non-Zero if debugging this serial port
        // Notes:
        //      - Used only when debugging this serial port.

    // ----------------------------------------------------------------------------
    // Reserved Command properties (WO = write-only) [F000 through F0FF]

    public static final int PROP_CMD_SAVE_PROPS             = 0xF000;
        // Description:
        //      Command[WO]: Forced property 'save'
        // Set Value:
        //      - none
        // Effect:
        //      Save properties, if changed

    public static final int PROP_CMD_AUTHORIZE              = 0xF002;
        // Description:
        //      Command[WO]: Set device authorization
        // Set Value: 
        //      0:X - [ASCIIZ] user
        //      X:X - [ASCIIZ] password [optional]
        // Special data length rules:
        //      - User name requirements may be defined by the device.
        //      - Password is optional (or as required by the device).
        // Effect: 
        //      Authenticate (per device requirements) and set PROP_STATE_USER_ID.
        // Notes:
        //      - The device may choose to refuse communication until authorized.

    public static final int PROP_CMD_STATUS_EVENT           = 0xF011;
        // Description:
        //      Command[WO]: Generate/Send status event
        // Set Value: 
        //      0:2 - [UInt16] status code of event to generate
        //      2:1 - [UInt8] Index, as needed by client [optional]
        // Special data length rules:
        //      - The server must always send a valid statuCode with this command.
        //      - The index should be included if it is needed.  If a needed index
        //      is missing, it will be assumed that the value should be '0'.
        // Effect: 
        //      Generate the specified event.
        // Notes:
        //      - Client may decide which status codes are supported in this command.
        //      However, at least STATUS_LOCATION must be supported to allow querying
        //      the device about it's current location.
    
    public static final int PROP_CMD_SET_OUTPUT             = 0xF031;
        // Description: [optional]
        //      Command[WO]: Set output
        // Set Value: 
        //      0:1 - [UInt8] Index of output
        //            Valid range 0 to 15.
        //      1:1 - [UInt8] Output state 
        //            Valid values 0=off, 1=on (bit mask 0xFE is reserved)
        //      2:4 - [UInt32] Duration in milliseconds [optional]
        //            Valid range 0 to 4294967295 millis (a '0' value means indefinite)
        //            Client may impose a maximum value.
        // Special data length rules:
        //      - The server must always send a valid index and the output state with this 
        //      command.  The duration need not be sent if the value is '0'.
        //      - The Command implementaion defines the behaviour if the supplied argument 
        //      length is not at least 2 bytes, or if the index is outside the acceptable
        //      range.  The mask 0x01 should be applied to obtain the output state.
        // Effect:
        //      - Set specified output to the specified state for specified duration, then
        //      set the opposite state to that specified.  
        //      - If the duration is zero, or omitted, then the duration is indefinite.
        //      - The client should impose any limits deemed necessary for the type of output.
        //      (ie. When sending a command to perform an action on the device, it is the 
        //      clients responsibility to insure that the output profile will not do damage to 
        //      the device, property, persons, etc.)
        // Notes:
        //      - Support for this command on the client is optional (as in the case where
        //      the client does not support digital outputs).  If not supported by the
        //      client it should return error 0xF311 (invalid/unsupported command).

    public static final int PROP_CMD_RESET                  = 0xF0FF;
        // Description: [optional]
        //      Command[WO]: Reset/Reboot client
        // Set Value: 
        //      0:1 - reset type (0=cold reset, 1=warm reset)
        //      1:X - client defined reset authorization
        // Effect:
        //      - If allowed by the client, this specifies that the client is to perform
        //      its power-on-reset routines.
        // Notes:
        //      - Support for this command on the client is optional.

    // ----------------------------------------------------------------------------
    // Read-Only/State properties:
    
    public static final int PROP_STATE_PROTOCOL             = 0xF100;
        // Description:
        //      [Read-Only] Protocol version
        // Get Value: 
        //      0:1 - [UInt8] Major version id
        //            Valid range 0 to 255
        //      1:1 - [UInt8] Minor version id
        //            Valid range 0 to 255
        //      2:1 - [UInt8] Minor revision [optional]
        //            Valid range 0 to 255
        // Special data length rules:
        //      - The client must always send at least the Major and Minor ids.
        // Notes:
        //      - This value represents the version of the DMTP protocol that this client
        //      has implemented.
        //      - The read-only attribute of this property should be enforced by the client.
    
    public static final int PROP_STATE_FIRMWARE             = 0xF101;
        // Description:
        //      [Read-Only] Firmware version
        // Get Value: 
        //      0:X - [ASCIIZ] string representing the firmware version id
        // Notes:
        //      - This value is defined by the client and represents the version of the firmware.
        //      - The read-only attribute of this property should be enforced by the client.
    
    public static final int PROP_STATE_COPYRIGHT            = 0xF107;
        // Description:
        //      [Read-Only] Copyright
        // Get Value: 
        //      0:X - [ASCIIZ] string representing the copyright string
        // Notes:
        //      - The read-only attribute of this property should be enforced by the client.
    
    public static final int PROP_STATE_SERIAL               = 0xF110;
        // Description: [optional]
        //      [Read-Only] Serial number
        // Get Value: 
        //      0:X - [ASCIIZ] string representing the serial number (may be same as device id)
        // Special data length rules:
        //      - The maximum length of the serial number is 20 ASCII characters.
        // Notes:
        //      - This value is defined by the client.  If the client does not have a serial
        //      number, then this value should be the same as the device-id.
        //      - The terminating null ('0') need not be included in the payload
        //      - The read-only attribute of this property should be enforced by the client.
    
    public static final int PROP_STATE_UNIQUE_ID            = 0xF112;
        // Description:
        //      [Read-Only] Unique ID
        // Value:
        //      0:X - [UInt8] Unique code provided by the DMT service provider
        // Special data length rules:
        //      - The client must send a 0-length field if the unique id has not been defined.
        // Notes:
        //      - This unique id is provided by the DMT service provider and uniquely
        //      identifies the device.  The value should be at least 4 bytes in length, but
        //      not more than 20 bytes.
        //      - The read-only attribute of this property should be enforced by the client.
    
    public static final int PROP_STATE_ACCOUNT_ID           = 0xF114;
        // Description:
        //      [Read-Only] Account ID
        // Value:
        //      0:X - [ASCIIZ] account ID recognized by the DMT service provider
        // Special data length rules:
        //      - The length of the property payload should be at least the length of the
        //      number of characters required to represent the account id.
        //      - The minimum length of this id is defined by the DMT service provider.
        //      - The maximum length of this id is 20 ASCII characters.
        // Notes:
        //      - This account id is provided by the DMT service provider and uniquely
        //      identifies the owner of the account.
        //      - The terminating null ('0') need not be included in the payload
        //      - The read-only attribute of this property should be enforced by the client.
    
    public static final int PROP_STATE_DEVICE_ID            = 0xF115;
        // Description:
        //      [Read-Only] Device ID
        // Value:
        //      0:X - [ASCIIZ] device ID recognized by the DMT service provider
        // Special data length rules:
        //      - The length of the property payload should be at least the length of the
        //      number of characters required to represent the device id.
        //      - The minimum length of this id is defined by the DMT service provider.
        //      - The maximum length of this id is 20 ASCII characters.
        // Notes:
        //      - This device id is provided by account owner and is registered to the DMT
        //      service provider.  This device id uniquely identifies the device within the
        //      account id.
        //      - The terminating null ('0') need not be included in the payload
        //      - The read-only attribute of this property should be enforced by the client.

    public static final int PROP_STATE_USER_ID              = 0xF117;
        // Description:
        //      [Optional Read-Only] User ID
        // Value:
        //      0:X - [ASCIIZ] user ID recognized by the DMT service provider
        // Special data length rules:
        //      - The length of the property payload should be at least the length of the
        //      number of characters required to represent the user id.
        //      - The minimum length of this id is defined by the DMT service provider.
        //      - The maximum length of this id is 20 ASCII characters.
        // Notes:
        //      - This user id is provided by account owner and is registered to the DMT
        //      service provider.  This user id may be used as-needed by the device to send
        //      as field information in an event.
        //      - The terminating null ('0') need not be included in the payload
        //      - The read-only attribute of this property may be enforced by the client.

    public static final int PROP_STATE_USER_TIME            = 0xF118;
        // Description:
        //      [Read-Only] User ID time
        // Value:
        //      0:4 - [UInt32] Number of seconds since midnight Jan 1, 1970 GMT
        // Special data length rules:
        //      - This timestamp should be initialized automatically at the time the
        //      PROP_STATE_USER_ID value was set.
        //      - The client must either respond with an exact 4 byte value, or may send
        //      a 0-length value indicating to the server that the client cannot
        //      provide the user-time.
        // Notes:
        //      - The read-only attribute of this property may be enforced by the client.

    public static final int PROP_STATE_TIME                 = 0xF121;
        // Description: [optional]
        //      [Read-Only] Current time of device
        // Value:
        //      0:4 - [UInt32] Number of seconds since midnight Jan 1, 1970 GMT
        //            Valid value is defined by the current time
        // Special data length rules:
        //      - The client must either respond with an exact 4 byte value, or may send
        //      a 0-length value indicating to the server that the client cannot
        //      provide the system time.
        // Note:
        //      - Implementation of this property is optional.  However,
        //      if the client cannot, or does not wish to support this feature, it
        //      should at least return a 0-length value.
        //      - Typically this property is read-only, however this is enforced by
        //      the client, not the server.  The client may allow this value to be set
        //      if necessary.
    
    public static final int PROP_STATE_GPS                  = 0xF123;
        // Description: [optional]
        //      [Read-Only] Latest (current) GPS fix
        // Value:
        //    10/14-byte length
        //      0:4 - [UInt32] GPS fix time
        //            Valid value is defined by the current time
        //      4:3 - [UInt24] standard-resolution encoded latitude
        //            See "Encoding the GPS Latitude/Longitude"
        //      7:3 - [UInt24] standard-resolution encoded longitude
        //            See "Encoding the GPS Latitude/Longitude"
        //     10:4 - [UInt32] optional odometer value in meters
        //    or
        //    12/16-byte length
        //      0:4 - [UInt32] GPS fix time
        //            Valid value is defined by the current time
        //      4:4 - [UInt32] high-resolution encoded latitude
        //            See "Encoding the GPS Latitude/Longitude"
        //      8:4 - [UInt32] high-resolution encoded longitude
        //            See "Encoding the GPS Latitude/Longitude"
        //     12:4 - [UInt32] optional odometer value in meters
        //    or
        //      0:0 - this property is not supported
        // Special data length rules:
        //      - The client must respond with either a 10/14-byte or 12/16-byte length.
        //      Or the client may respond with a 0-length value if this feature cannot be
        //      supported.
        // Notes:
        //      - Depending on the degree of accuracy that the client wishes to provide,
        //      the client may return either a 6-byte, or 8-byte, encoded Lat/Lon.
        //      If the client cannot, or does not wish to support this feature, it
        //      should at least return a 0-length value.
        //      - This property should be considered read-only.
        //      - Server note: The server will infer from the length of the data payload
        //      which type of encoding is used.  A data payload of 10/14 bytes will indicate
        //      a standard-resolution encoding, and a length of 12/16 bytes will indicating
        //      a high-resolution encoding.  If the data payload is 0-length, the server
        //      will assume that this property feature is not supported by this client.
        
    public static final int PROP_STATE_GPS_DIAGNOSTIC       = 0xF124;
        // Description: [optional]
        //      [Read-Only] Latest (current) GPS diagnostic information
        // Value:
        //      0:4 - [UInt32] Last GPS sample time
        //      4:4 - [UInt32] Last GPS valid fix time
        //      8:4 - [UInt32] Number of valid GPS fixes since reboot
        //     12:4 - [UInt32] Number of invalid GPS fixes since reboot
        //     16:4 - [UInt32] Number of forced GPS restarts
        // Notes:
        //      - This property is used by the client to provide diagnostic information
        //      regarding the current health of the GPS module.
    
    public static final int PROP_STATE_QUEUED_EVENTS        = 0xF131;
        // Description: [optional]
        //      [Read-Only] Event counts (queue for transmission, and total)
        // Value:
        //      0:4 - [UInt32] Number of queued, un-acknowledged, events
        //      0:4 - [UInt32] Total number of events generated (since last reboot)

    public static final int PROP_STATE_DEV_DIAGNOSTIC       = 0xF141;
        // Description: [optional]
        //      [Read-Only] Device diagnostics
        // Value:
        //      0:4 - [UInt32] The device reset count (either manual or forced)
        //      4:4 - [UInt32] reserved
        //      8:4 - [UInt32] reserved
        //     12:4 - [UInt32] reserved
        //     16:4 - [UInt32] reserved

    // ----------------------------------------------------------------------------
    // Communication protocol properties:
    
    public static final int PROP_COMM_SPEAK_FIRST           = 0xF303;
        // Description:
        //      If 'true', client is expected to initiate the conversation with the server.
        // Value: 
        //      0:1 - [utBool] Non-Zero if client is expected to initiate conversation
        // Special data length rules:
        //      - The client must send the 1 required byte
        //      - The server must send the 1 required byte
        // Notes:
        //      - This value may be read-only on the client.
    
    public static final int PROP_COMM_FIRST_BRIEF           = 0xF305;
        // Description:
        //      If 'true', client must send only ID and EOB packets on first packet block.  It 
        //      must not include any other packets.
        // Value: 
        //      0:1 - [utBool] Non-Zero if client is to send only ID and EOB packets on first packet 
        //            block.
        // Special data length rules:
        //      - The client must send the 1 required byte
        //      - The server must send the 1 required byte
        // Notes:
        //      - This value may be read-only on the client.
    
    public static final int PROP_COMM_MAX_CONNECTIONS       = 0xF311;
        // Description:
        //      Maximum number of allowed connections per time period
        // Value: 
        //      0:1 - [UInt8] Maximum total connections per time period (Duplex + Simplex)
        //            Valid range: 0 to 255 connections (0 means NO connections)
        //      1:1 - [UInt8] Maximum Duplex connections per time period
        //            Valid range: 0 to 255 connections (0 means NO Duplex connections)
        //      2:1 - [UInt8] Number of minutes over which the above limits apply
        //            Valid range: 0 to 240 minutes 
        //            (Rounded to floor 30 minute interval.  Max 240 minutes.)
        // Special data length rules:
        //      - The client should send the 3 required bytes
        //      - The server must send the 3 required bytes
        // Notes:
        //      - These values should match those provided by the level of service
        //      granted by the DMT service provider.
        //      - The number of total connections should always be >= the number of Duplex connections
        //      - The number of Duplex connection should be set to '0' if all messages are to
        //      transmitted via Simplex (eg. UDP).
    
    public static final int PROP_COMM_MIN_XMIT_DELAY        = 0xF312;
        // Description:
        //      Absolute minimum time delay (seconds) between transmit intervals
        // Value: 
        //      0:2 - [UInt16] Minimum time in seconds between transmissions
        //            Valid range: 0 to 65535 seconds (0 means no minimum)
        // Special data length rules:
        //      - The client should respond with at least the minimum length that can accurately
        //      represent the value.
        //      - The server must always send at least 1 byte.
        // Notes:
        //      - The device must never transmit more often than the interval specified by this 
        //      property (even for critical events).
    
    public static final int PROP_COMM_MIN_XMIT_RATE         = 0xF313;
        // Description:
        //      Minimum data transmit interval (seconds)
        // Value: 
        //      0:4 - [UInt32] Minimum time in seconds between transmissions of non-critical events.
        //            Valid range: 0 to 4294967295 seconds (the client may impose limits)
        // Special data length rules:
        //      - The client should respond with at least the minimum length that can accurately
        //      represent the value.
        //      - The server must always send at least 1 byte.
        // Notes:
        //      - For non-critical events, the device should never transmit more often than the 
        //      interval specified by this property.
        
    public static final int PROP_COMM_MAX_XMIT_RATE         = 0xF315;
        // Description:
        //      Maximum data transmit interval
        // Value: 
        //      0:4 - [UInt32] Maximum time in seconds between transmissions
        //            Valid range: 0 to 4294967295 seconds (the client may impose limits)
        // Special data length rules:
        //      - The client should respond with at least the minimum length that can accurately
        //      represent the value.
        //      - The server must always send at least 1 byte.
        // Notes:
        //      - If this amount of time passes without any data trasnmission, initiate a
        //      non-data transmission to see if the server wishes to send the client any
        //      information or reconfiguration.
        //      - This value should never be less than PROP_COMM_MIN_XMIT_RATE
        
    public static final int PROP_COMM_MAX_DUP_EVENTS        = 0xF317;
        // Description:
        //      Maximum events to send per block (Duplex connections)
        // Value: 
        //      0:1 - [UInt8] Maximum number of events to send per acknowledge block (1 to 255)
        //            Valid range: 1 to 255 events (the client/server may impose limits)
        // Special data length rules:
        //      - The client must alway send a 1 byte value.
        //      - The server must always send a 1 byte value.
        // Notes:
        //      - This value should be at least 1, but should not be greater than 128.  The
        //      server may refuse the data if greater than 128.
        
    public static final int PROP_COMM_MAX_SIM_EVENTS        = 0xF318;
        // Description:
        //      Maximum events to send per Simplex transmission
        // Value: 
        //      0:1 - [UInt8] Maximum number of events to send per Simplex transmission (1 to 255)
        //            Valid range: 1 to 255 events (the client may impose limits)
        // Special data length rules:
        //      - The client must alway send a 1 byte value.
        //      - The server must always send a 1 byte value.
        // Notes:
        //      - This value should be at least 1, but should not be greater than 16.  Since
        //      Simplex transmissions may not guarantee delivery (UDP does not), making this 
        //      value larger may result in a more significant data loss should a particular 
        //      message be lost.
        //      - Since Simplex transmissions may not guarantee delivery, only low priority, 
        //      non-critical messages should be sent via Simplex.

    // ----------------------------------------------------------------------------
    // Communication connection properties:

    public static final int PROP_COMM_SETTINGS              = 0xF3A0;
        // Description:
        //      Communication settings - as defined by device
        // Value: 
        //      0:X - [ASCIIZ] Device defined communication settings.
        // Special data length rules:
        //      - A 0-length value indicates that no communication settings are available.
        // Notes:
        //      - The format of the value payload is defined by the client device.
        //      - The terminating null ('0') need not be included in the payload
        //      - The client may choose to make this read-only.
    
    public static final int PROP_COMM_DMTP_HOST             = 0xF3A1;
        // Description: [optional]
        //      Communication settings host
        // Value: 
        //      0:X - [ASCIIZ] host name or ip address, identifier
        // Special data length rules:
        //      - A 0-length value indicates that no host name is available.
        // Notes:
        //      - This value must be supplied by your DMT service provider.
        //      - The terminating null ('0') need not be included in the payload
        //      - The client may choose to make this read-only.
    
    public static final int PROP_COMM_DMTP_PORT             = 0xF3A2;
        // Description: [optional]
        //      Communication settings port
        // Value: 
        //      0:2 - [UInt16] host port number for Duplex/Simplex communications.
        // Special data length rules:
        //      - The length must be large enough to accurately represent the port.
        //      - A 0-length value indicates that no port is specified.
        // Notes:
        //      - This value must be supplied by your DMT service provider.
        //      - The client may choose to make this read-only.
    
    public static final int PROP_COMM_DNS_1                 = 0xF3A3;
        // Description: [optional]
        //      Communication settings DNS 1 
        // Value: 
        //      0:X - [ASCIIZ] DNS ip address (primary)
        // Special data length rules:
        //      - A 0-length value indicates that DNS-1 is not available.
        // Notes:
        //      - This value must be supplied by your GSM/GPRS airtime service provider.
        //      - The terminating null ('0') need not be included in the payload
        //      - The client may choose to make this read-only.
    
    public static final int PROP_COMM_DNS_2                 = 0xF3A4;
        // Description: [optional]
        //      Communication settings DNS 2 
        // Value: 
        //      0:X - [ASCIIZ] DNS ip address (secondary)
        // Special data length rules:
        //      - A 0-length value indicates that DNS-2 is not available.
        // Notes:
        //      - This value must be supplied by your GSM/GPRS airtime service provider.
        //      - The terminating null ('0') need not be included in the payload
        //      - The client may choose to make this read-only.

    public static final int PROP_COMM_CONNECTION            = 0xF3A5;
        // Description: [optional]
        //      Connection name (WindowsCE)
        // Value: 
        //      0:X - [ASCIIZ] connection name as required by the client device.
        // Special data length rules:
        //      - A 0-length value indicates that the connection name is not available.
        // Notes:
        //      - The format of this ASCIIZ value is defined by the client device.
        //      - The terminating null ('0') need not be included in the payload

    public static final int PROP_COMM_APN_NAME              = 0xF3A6;
        // Description: [optional]
        //      Communication settings APN name
        // Value: 
        //      0:X - [ASCIIZ] communication settings as required by the client device.
        // Special data length rules:
        //      - A 0-length value indicates that the APN name is not available.
        // Notes:
        //      - The format of this ASCIIZ value is defined by the client device.
        //      - This value must be supplied by your GSM/GPRS airtime service provider.
        //      - The terminating null ('0') need not be included in the payload
    
    public static final int PROP_COMM_APN_SERVER            = 0xF3A7;
        // Description: [optional]
        //      Communication settings APN server
        // Value: 
        //      0:X - [ASCIIZ] communication settings as required by the client device.
        // Special data length rules:
        //      - A 0-length value indicates that the APN server is not available.
        // Notes:
        //      - The format of this ASCIIZ value is defined by the client device.
        //      - This value must be supplied by your GSM/GPRS airtime service provider.
        //      - The terminating null ('0') need not be included in the payload
    
    public static final int PROP_COMM_APN_USER              = 0xF3A8;
        // Description: [optional]
        //      Communication settings APN user
        // Value: 
        //      0:X - [ASCIIZ] communication settings as required by the client device.
        // Special data length rules:
        //      - A 0-length value indicates that the APN user is not available.
        // Notes:
        //      - The format of this ASCIIZ value is defined by the client device.
        //      - This value must be supplied by your GSM/GPRS airtime service provider.
        //      - The terminating null ('0') need not be included in the payload
    
    public static final int PROP_COMM_APN_PASSWORD          = 0xF3A9;
        // Description: [optional]
        //      Communication settings APN password
        // Value: 
        //      0:X - [ASCIIZ] communication settings as required by the client device.
        // Special data length rules:
        //      - A 0-length value indicates that the APN password is not available.
        // Notes:
        //      - The format of this ASCIIZ value is defined by the client device.
        //      - This value must be supplied by your GSM/GPRS airtime service provider.
        //      - The terminating null ('0') need not be included in the payload

    public static final int PROP_COMM_APN_PHONE             = 0xF3AA;
        // Description: [optional]
        //      Communication settings APN phone number
        // Value: 
        //      0:X - [ASCIIZ] APN phone number as required by the client device.
        // Special data length rules:
        //      - A 0-length value indicates that the phone number is not available.
        // Notes:
        //      - The format of this ASCIIZ value is defined by the client device.
        //      - The terminating null ('0') need not be included in the payload

    public static final int PROP_COMM_APN_SETTINGS          = 0xF3AC; // was 0xF3AA
        // Description: [optional]
        //      General communication settings
        // Value: 
        //      0:X - [ASCIIZ] communication settings as required by the client device.
        // Special data length rules:
        //      - A 0-length value indicates that the APN settings are not available.
        // Notes:
        //      - The format of this ASCIIZ value is defined by the client device.
        //      - This value must be supplied by your GSM/GPRS airtime service provider.
        //      - The terminating null ('0') need not be included in the payload

    public static final int PROP_COMM_MIN_SIGNAL            = 0xF3AD;
        // Description: [optional]
        //      Communication settings minimum signal strength
        // Value: 
        //      0:1 - [UInt8] Minimum signal strength required to establish connection
        // Notes:
        //      - This is typically a value between 0 and 31 inclusive.  The client
        //        may use this value to compare against the signal strength returned
        //        from a "AT+CSQ" comment sent to the modem.

    public static final int PROP_COMM_ACCESS_PIN            = 0xF3AF;
        // Description: [optional]
        //      Access PIN/Password
        // Value:
        //      0:8 - [UInt8(8)] 8-byte access pin code
        // Special data length rules:
        //      - If the client wishes to keep this value a secret, it may choose to always 
        //        return a 0-length property payload.
        // Notes:
        //      - This value may be used for any access control purpose the client deems 
        //        necessary.

    // ----------------------------------------------------------------------------
    // Packet/Data format properties:

    public static final int PROP_COMM_CUSTOM_FORMATS        = 0xF3C0;
        // Description: [optional]
        //      True if server supports custom formats for this client
        // Value: 
        //      0:1 - 1 if server supports custom formats, 0 otherwise.
        // Special data length rules:
        //      - A 0-length value indicates that custom formats are not supported.
        // Notes:
        //      - This is a hint to whether or not the DMT service provider will support
        //        custom formats from this device.
        
    public static final int PROP_COMM_ENCODINGS             = 0xF3C1;
        // Description: [optional]
        //      Mask indicating the encodings supported by the server.
        // Value: 
        //      0:1 - Bitmask indicating supported encodings
        //          0x01 - Binary (always true)
        //          0x02 - Ascii Base64 (always true)
        //          0x04 - Ascii Hex (always true)
        //          0x08 - Ascii CSV (server support is optional)
        //          0xF0 - reserved
        // Special data length rules:
        //      - All servers must support Binary, Ascii Hex, and Ascii Base64.
        //      - Server support for CSV is optional.
        // Notes:
        //      - This is a hint to whether or not the DMT service provider will support
        //        the specified encoding for this device.
        //      - Since Binay, Hex, and Base64 must be supported by the DMT service
        //        provider, this essentially indicate whether the encoding CSV is supported
        //        by the DMT service provider.
    
    public static final int PROP_COMM_BYTES_READ            = 0xF3F1;
        // Description: [optional]
        //      Number of bytes read by client
        // Value: 
        //      0:4 - [UInt32] read byte count
        //            Valid range: 0 to 4294967295
        // Notes:
        //      - This is for information purposes only and the client is not required
        //      to implement this property.
        
    public static final int PROP_COMM_BYTES_WRITTEN         = 0xF3F2;
        // Description: [optional]
        //      Number of bytes written by client
        // Value: 
        //      0:4 - [UInt32] write byte count
        //            Valid range: 0 to 4294967295
        // Notes:
        //      - This is for information purposes only and the client is not required
        //      to implement this property.
    
    // ----------------------------------------------------------------------------
    // GPS config properties:
    
    public static final int PROP_GPS_SAMPLE_RATE            = 0xF511;
        // Description:
        //      GPS sample interval
        // Value: 
        //      0:2 - [UInt16] Number of seconds between GPS sampling
        //            Valid range: 1 to 65535 seconds (the effect of 0 is defined by the client)
        // Special data length rules:
        //      - The client/server must send at least 1 byte.
        // Notes:
        //      - This value represent the amount of time to wait between GPS  
        //      location acquisition and analysis.  This value is typically a
        //      short amount of time, somewhere between 5 and 30 seconds
    
    public static final int PROP_GPS_ACQUIRE_WAIT           = 0xF512;
        // Description:
        //      Amount of time to block when waiting for a current GPS fix
        // Value: 
        //      0:2 - [UInt16] Number of milliseconds to block
        //            Valid range: 1 to 65535 milliseconds 
        //            The effect of 0 is defined by the client, but would typically mean
        //            that last valid fix should be immediately used.
        // Special data length rules:
        //      - The client/server must send at least 1 byte.
        // Notes:
        //      - This value respresent the amount of time to block waiting for
        //      a valid current GPS fix.
        //      - This value should be in the range of 0 to 5000 milliseconds.
        //      - '0' is defined by the client, but typically means that last valid fix should 
        //      be immediately used.
        
    public static final int PROP_GPS_EXPIRATION             = 0xF513;
        // Description:
        //      GPS Expiration
        // Value: 
        //      0:2 - [UInt16] Number of seconds after which the GPS fix is considered stale
        //            Valid range: 1 to 65535 sec (0 means this feature is disabled)
        // Special data length rules:
        //      - The value should be at least 1 byte.
        // Notes:
        //      - The behavior of the client when a GPS fix has expired is unspecified.
        //      The client may wish to send a diagnostic/error message to the server.
    
    public static final int PROP_GPS_CLOCK_DELTA            = 0xF515;
        // Description:
        //      Update system clock if difference exceeds value [optional]
        // Value: 
        //      0:2 - [UInt16] Number of seconds that the system clock must be out of sync
        //            with the GPS clock in order to force a time update of the system clock.
        // Special data length rules:
        //      - The value should be at least 1 byte.
        // Notes:
        //      - If non-zero, the client should check the GPS clock against the system clock
        //      and update the system clock if the delta is greater that (or equal to) this
        //      delta value.
        //      - A value of zero is an indicator to the client that the system clock should
        //      not be sync'ed to the GPS clock.
        //      - This is an optional feature.  The client may wish to never update the
        //      system clock, always update the system clock based on some fixed delta, or 
        //      choose to update the system clock based on this property value.

    public static final int PROP_GPS_ACCURACY               = 0xF521;
        // Description:
        //      GPS Accuracy threshold [optional]
        // Value: 
        //      0:2 - [UInt16] GPS accuracy threshold in meters.
        //            Valid range: 0 to 65535 meters (0 means the feature is not supported)
        // Special data length rules:
        //      - The value should be 2 bytes.  A 0-length value indicates that the feature 
        //      is not supported.
        // Notes:
        //      - A GPS fix will be rejected if it's accuracy falls outside this threshold.
        //      For example, if the value is set to 800 meters and the accuracy of a given
        //      GPS fix is determined to be 1000 meters, then the GPS fix should be rejected
        //      and another GPS fix should be acquired.
        //      - Support for this property is optional (not all clients may have the
        //      ability to determine the accuracy of a GPS fix).  The client may return the
        //      error DIAG_PROPERTY_INVALID_ID if it cannot support this property.
    
    public static final int PROP_GPS_MIN_SPEED              = 0xF522;
        // Description:
        //      GPS Minimum speed
        // Value: 
        //      0:2 - [UInt16] Minimum GPS speed in 0.1 KPH units 
        //            Valid range: 0.0 to 6553.5 kph (0.0 means no minimum speed)
        // Special data length rules:
        //      - A 0-length value indicates no minimum speed
        // Notes:
        //      - GPS reported speed values less-than, or equals-to, this value will be
        //      considered stopped and will be reported in location events as 0 KPH.
        //      - The purpose of this property is to adjust for inaccuracies in some
        //      GPS modules which can report a significant speed value, even when the
        //      device is not moving.
        
    public static final int PROP_GPS_DISTANCE_DELTA         = 0xF531;
        // Description:
        //      Distance delta
        // Value: 
        //      0:4 - [UInt32] The minimum distance that the device has to move (in 1 meter
        //            units) for distance (ie. odometer) accumulation
        //            Valid range: 1 to 4294967295 meters (however, the client may impose 
        //            a minimum)
        // Special data length rules:
        //      - The effect of a 0-length value is defined by the client.
        // Notes:
        //      - The device must move this number of meters before a distance accumulation
        //      (ie. odometer) is performed.  The new GPS fix will then be stored in
        //      PROP_ODOMETER_0_GPS
        //      - This value should be larger than the accuracy capability of the GPS module.
        //      Setting this value too low (eg. 20 meters) may cause the device to accumulate
        //      distance even though the device isn't moving.  The value should not be less 
        //      than the value specified for PROP_GPS_ACCURACY.  For non-WAAS enabled GPS
        //      modules, this value probably should not be less that 500 meters.  For WAAS
        //      enabled modules, this value could probably be around 200 meters.  Experiment
        //      with this and check the results for yourself.
        //      - This value effects all accumulated odometer values.
    
    // ----------------------------------------------------------------------------
    // Geofence properties:

    public static final int PROP_CMD_GEOF_ADMIN             = 0xF542;
        // Description: [optional]
        //      Command[WO]: GeoZone admin
        // Set Value:
        //      0:1 - Admin command type
        //            0x10 Add GeoZone list to table
        //            0x20 Remove specified GeoZone(terminal) ID from table
        //            0x30 Save GeoZone table to predefined location.
        //   0x10: Add GeoZone list to table
        //      1:2 - Zone-ID (currently limited to a 2 byte value)
        //      3:2 - bits 0:3  type
        //            bits 3:13 radius (meters)
        //      5:6 - Encoded Latitude/Longitude #1
        //     11:6 - Encoded Latitude/Longitude #2
        //     The above template may be repeated up to 15 times per packet.
        //   0x20: Remove specified GeoZone(terminal) ID from table
        //      1:2 - Zone-ID
        //     If 0xFFFF is specified for the Zone-ID, then all points will be removed
        //   0x30: Save GeoZone table to predefined location.
        //      X:X - Payload will be ignored
        // Notes:
        //      - The the method used for storing GeoZones/Geofences may necessarily
        //      be very dependent on the client device on which OpenDMTP resides.
        //      The above represents a standard extension for those client devices
        //      that are able to support this type of GeoZone/Geofence format.  If
        //      the client device is unable to support this format and wishes to 
        //      implement its own custom method for GeoZone/Geofence detection, then
        //      it should always respond with the COMMAND_FEATURE_NOT_SUPPORTED if 
        //      this command property is called.

    public static final int PROP_GEOF_COUNT                 = 0xF547;
        // Description: [optional]
        //      [Read-Only] GeoZone table entry count
        // Get Value: 
        //      0:2 - Number GeoZone entries in table

    public static final int PROP_GEOF_VERSION               = 0xF548;
        // Description: [optional]
        //      GeoZone table version
        // Get/Set Value: 
        //      0:X - [ASCIIZ] string representing server defined geofence version
        // Special data length rules:
        //      - The maximum length of the version string is 20 characters.

    public static final int PROP_GEOF_ARRIVE_DELAY          = 0xF54A;
        // Description: [optional]
        //      Geofence arrival delay in seconds
        // Value: 
        //      0:2 - [UInt16] Number of seconds that the device must be in a geofence
        //            before is is considered "arrived".
        // Notes:
        //      - This property prevents devices being marked as 'arrived' when they
        //      only passing through.
    
    public static final int PROP_GEOF_DEPART_DELAY          = 0xF54D;
        // Description: [optional]
        //      Geofence departure delay in seconds
        // Value: 
        //      0:2 - [UInt16] Number of seconds that the device must be outside a geofence
        //            before is is considered "departed".
        // Notes:
        //      - The value for this property is generally small and prevents devices being 
        //      marked as 'departed' when they only left briefly.  This is generally only
        //      necessary to prevent oddball bouncing GPS locations from causing multiple
        //      improper arrival/departure messages.
    
    public static final int PROP_GEOF_CURRENT               = 0xF551;
        // Description: [optional]
        //      Geofence ID in which the device is sitting
        // Value: 
        //      0:4 - [UInt32] Geofence ID
        //            Valid range: 0x00000000 to 0xFFFFFFFF (0 means not in a geofence).
        // Notes:
        //      - This value should generally be set by the device itself at it enters or
        //      leave pre-defined geofenced areas.
    
    // ----------------------------------------------------------------------------
    // GeoCorr properties:

    public static final int PROP_CMD_GEOC_ADMIN             = 0xF562;
        // Description: [optional]
        //      Command[WO]: GeoCorridor admin

    public static final int PROP_GEOC_ACTIVE_ID             = 0xF567;
        // Description: [optional]
        //      The active Geofence ID
        // Value: 
        //      0:4 - [UInt32] Active Geofence ID
        //            Valid range: 0x00000000 to 0xFFFFFFFF (0 means no geofence is active).
        // Note:
        //      - This is typically used for identifying an active alarm state geofence.
        //      - This value may be set by the device itself as it determines necesary
        //      as it enters or leaves predefined geofenced areas.

    public static final int PROP_GEOC_VIOLATION_INTRVL      = 0xF56A;
        // Description: [optional]
        //      During a sustained geofence violation, the number of seconds between geofence
        //      violation events.
        // Value: 
        //      0:2 - [UInt16] Number of seconds between geofence violation events.  
        //            Valid range: 30 to 65535 seconds.
        // Notes:
        //      - This value represents the interval (in seconds) between geofence violation
        //      events during a sustained geofence violation.
    
    public static final int PROP_GEOC_VIOLATION_COUNT       = 0xF56D;
        // Description:
        //      Maximum number of geofence violation messages to send
        // Value: 
        //      0:2 - [UInt16] Maximum number of violation messages to send during a geofence
        //            violation.
        //            Valid range: 1 to 65535 events (0 indefinite)
        // Special data length rules:
        //      - A 0-length value indicates that the violation message count is indefinite.
        // Notes:
        //      - This value represents the number of geofence violation events that should
        //      be sent once the device has determined that a geofence violation has occurred.

    // ----------------------------------------------------------------------------
    // Motion properties:
    
    public static final int PROP_MOTION_START_TYPE          = 0xF711;
        // Description:
        //      Motion start type
        // Value: 
        //      0:1 - [UInt8] Motion start type
        //            Valid values: 0=kph, 1=meters moved, 2 to 255 are reserved.
        // Special data length rules:
        //      - A 0-length value implies a motion start type of '0' (KPH).
        // Notes:
        //      - This property defines the meaning of the value for the property 
        //      PROP_MOTION_START.  If this value is '0', then motion-start is defined
        //      if KPH.  If this value is '1', then motion-start is defined in the
        //      number of meters moved.
    
    public static final int PROP_MOTION_START               = 0xF712;
        // Description:
        //      Motion start definition
        // Value: 
        //      0:2 - [UInt16] Definition of start of motion in 0.1 KPH/Meters units
        //            Valid range: 0.1 to 6553.5 kph/meters (0.0 means this feature is inactive)
        // Special data length rules:
        //      - The 0-length value implies that motion start has not yet been defined.
        // Notes:
        //      - A value of 0 means that stop/stop motion events are not currently enabled.
        //      - Whether this value is interpreted as KPH or Meters depends on the value
        //      of the property PROP_MOTION_START_TYPE.
    
    public static final int PROP_MOTION_IN_MOTION           = 0xF713;
        // Description:
        //      In-motion interval
        // Value: 
        //      0:2 - [UInt16] Number of seconds between in-motion events
        //            Valid range: 0 to 65535 seconds (0 means this feature is inactive)
        // Special data length rules:
        //      - A 0-length value implies that the in-motion interval is not defined.
        // Notes:
        //      - A value of 0 means that no in-motion events are to be generated.
    
    public static final int PROP_MOTION_STOP                = 0xF714;
        // Description:
        //      Motion stop definition
        // Value: 
        //      0:2 - [UInt16] Definition of end of motion in number of seconds (1 to 65535 sec)
        // Special data length rules:
        //      none
    
    public static final int PROP_MOTION_STOP_TYPE           = 0xF715;
        // Description:
        //      Motion stop type
        // Value: 
        //      0:1 - [UInt8] Motion start type
        //            Valid values: 0=after_delay, 1=when_stopped, 2 to 255 are reserved.
        // Special data length rules:
        //      - A 0-length value implies a motion start type of '0' (after_delay).
        // Notes:
        //      - This property defines the effect of the value for the property 
        //      PROP_MOTION_STOP.  If this value is '0', then the stop-motion event will be
        //      generated with a timestamp at the time the PROP_MOTION_STOP timer has expired.
        //      Also in-motion messated will be generated on a scheduled interval until the
        //      stop-motion event is generated.  If this value is '1', then the timestamp of
        //      the generated stop-motion event will be the time that the vehicle actually
        //      stopped (the stop-motion event is delayed until the stopped timer is expired).
        //      Also, in-motion event will only be generated if the vehicle is actually in
        //      motion at the time the in-motion event is to be generated.
    
    public static final int PROP_MOTION_DORMANT_INTRVL      = 0xF716;
        // Description:
        //      Dormant interval
        // Value: 
        //      0:4 - [UInt32] Number of seconds between dormant events (1 to 4294967295 sec)
        //            Valid range: 0 to 4294967296 seconds (0 means no dormant messages)
        // Special data length rules:
        //      - A 0-length value indicates that dormant events are disabled.
        // Notes:
        //      - This value represents the interval (in seconds) between dormant events
        //      once the device has determined that it is no longer moving.  The number of
        //      dormant messages sent is defined by the property PROP_MOTION_DORMANT_COUNT.
    
    public static final int PROP_MOTION_DORMANT_COUNT       = 0xF717;
        // Description:
        //      Maximum number of dormant messages to send
        // Value: 
        //      0:2 - [UInt16] Maximum number of dormant messages to send during dormancy
        //            Valid range: 0 to 65535 events (0 indefinite)
        // Special data length rules:
        //      - A 0-length value indicates that the dormant message count is indefinite.
        // Notes:
        //      - This value represents the number of dormant messages that should be
        //      sent once the device has determined that it is no longer moving.
        //      Typically, this value is 0 (indefinite), however it may be desirable to
        //      have a limited number of dormant messages sent by the client.
    
    public static final int PROP_MOTION_EXCESS_SPEED        = 0xF721;  // Excess speed (0.1 kph)
        // Description:
        //      Excess speed
        // Value: 
        //      0:2 - [UInt16] Definition of excess speed in 0.1 KPH units
        //            Valid range: 0.1 to 6553.5 KPH (0 means this feature is disabled)
        // Special data length rules:
        //      - A 0-length value indicates that this feature is disabled
        // Notes:
        //      - An excess speed event will be generated if the current speed exceeds this 
        //      value

    public static final int PROP_MOTION_MOVING_INTRVL       = 0xF725;
        // Description: [optional]
        //      'Moving' interval
        // Value: 
        //      0:2 - [UInt16] Number of seconds between 'Moving' events (1 to 65535 sec)
        //            Valid range: 0 to 65535 seconds (0 means no 'Moving' messages)
        // Special data length rules:
        //      - A 0-length value indicates that 'moving' events are disabled.
        // Notes:
        //      - This value represents the minimum interval (in seconds) between 'moving' 
        //      events if the device determines that it is moving.
        //      - 'Moving' events may operate independently of motion start/stop/in-motion
        //      events, and may be generated even if start/stop events are not in use.
        //      - This property is optional.  The client may also decide the special 
        //      conditions under which these events are generated.

    // ----------------------------------------------------------------------------
    // Odometer properties:
        
    public static final int PROP_ODOMETER_0_VALUE           = 0xF770;
    public static final int PROP_ODOMETER_1_VALUE           = 0xF771;
    public static final int PROP_ODOMETER_2_VALUE           = 0xF772;
    public static final int PROP_ODOMETER_3_VALUE           = 0xF773;
    public static final int PROP_ODOMETER_4_VALUE           = 0xF774;
    public static final int PROP_ODOMETER_5_VALUE           = 0xF775;
    public static final int PROP_ODOMETER_6_VALUE           = 0xF776;
    public static final int PROP_ODOMETER_7_VALUE           = 0xF777;
        // Description: [optional]
        //      Device odometer/tripometer (1 meter units)
        // Value:
        //      0:4 - [UInt32] Number of meters that the device has moved since the value
        //            was last reset.
        //            Value range 0 to 4294967295 meters.
        // Special data length rules:
        //      - The client must respond with at least the minimum number of bytes
        //      required to accurately respresent the number of meters travelled.  A
        //      0-length value will considered as the value '0'.
        // Notes:
        //      - PROP_ODOMETER_0_VALUE should be used to represent the number of meters moved
        //      since the device was put into service.  This odometer may be read-only.
        //      - PROP_ODOMETER_1..7 may be used for special 'tripometer' applications.
        
    public static final int PROP_ODOMETER_0_LIMIT           = 0xF780;
    public static final int PROP_ODOMETER_1_LIMIT           = 0xF781;
    public static final int PROP_ODOMETER_2_LIMIT           = 0xF782;
    public static final int PROP_ODOMETER_3_LIMIT           = 0xF783;
    public static final int PROP_ODOMETER_4_LIMIT           = 0xF784;
    public static final int PROP_ODOMETER_5_LIMIT           = 0xF785;
    public static final int PROP_ODOMETER_6_LIMIT           = 0xF786;
    public static final int PROP_ODOMETER_7_LIMIT           = 0xF787;
        // Description: [optional]
        //      Device odometer/tripometer triggered alarm point (1 meter units)
        // Value:
        //      0:4 - [UInt32] Once the client has achieved this number of meters it should
        //            trigger a corresponding STATUS_ODOM_LIMIT_# event.
        //            Value range 1 to 4294967295 meters.
        // Special data length rules:
        //      - The client must respond with at least the minimum number of bytes required 
        //      to accurately respresent the value.
        // Notes:
        //      - A value of '0' indicates that no alarm/event will be generated.
        //      - Once this limit is reached, the client should issue a corresponding
        //      STATUS_ODOM_LIMIT_# event, however, the decision to reset the odometer or
        //      continue to count is left to the client.
    
    public static final int PROP_ODOMETER_0_GPS             = 0xF790;
    public static final int PROP_ODOMETER_1_GPS             = 0xF791;
    public static final int PROP_ODOMETER_2_GPS             = 0xF792;
    public static final int PROP_ODOMETER_3_GPS             = 0xF793;
    public static final int PROP_ODOMETER_4_GPS             = 0xF794;
    public static final int PROP_ODOMETER_5_GPS             = 0xF795;
    public static final int PROP_ODOMETER_6_GPS             = 0xF796;
    public static final int PROP_ODOMETER_7_GPS             = 0xF797;
        // Description: [optional]
        //      Device odometer GPS (point of last odometer GPS fix)
        // Value:
        //    10/14-byte length
        //      0:4 - [UInt32] GPS fix time
        //            Valid value is defined by the current time
        //      4:3 - [UInt24] standard-resolution encoded latitude
        //            See "Encoding the GPS Latitude/Longitude"
        //      7:3 - [UInt24] standard-resolution encoded longitude
        //            See "Encoding the GPS Latitude/Longitude"
        //     10:4 - [UInt32] optional odometer value in meters
        //    or
        //    12/16-byte length
        //      0:4 - [UInt32] GPS fix time
        //            Valid value is defined by the current time
        //      4:4 - [UInt32] high-resolution encoded latitude
        //            See "Encoding the GPS Latitude/Longitude"
        //      8:4 - [UInt32] high-resolution encoded longitude
        //            See "Encoding the GPS Latitude/Longitude"
        //     12:4 - [UInt32] optional odometer value in meters
        // Special data length rules:
        //      - The client must respond with either a 10/14-byte or 12/16-byte length.
        //      - The server must send either a 10/14-byte or 12/16-byte length, depending
        //      on which lat/lon encoding the server has determined that the client supports.
        // Notes:
        //      - These properties may be used by the client to maintain the GPS location
        //      state necessary to accumulate GPS-based odometer information.  This
        //      property only holds a single GPS point, if the client requires a more
        //      general state cache for odometer information, the PROP_ODOMETER_#_STATE
        //      properties should be used.
        //      - Depending on the degree of accuracy that the client wishes to provide,
        //      the client may return either a 6-byte, or 8-byte, encoded Lat/Long.
        //      - These properties should be considered read-only, however this is enforced
        //      by the client, not the server.  The client may allow this value to be set
        //      if necessary.
        //      - Server note: The server will infer from the length of the data payload
        //      which type of encoding is used.  A data payload of 10/14 bytes will indicate
        //      a standard-resolution encoding, and a length of 12/16 bytes will indicating
        //      a high-resolution encoding.

    // ----------------------------------------------------------------------------
    // Digital input/output properties:

    public static final int PROP_INPUT_STATE                = 0xF901;
        // Description: [optional]
        //      Current digital input configuration
        // Value:
        //      0:4 - [UInt32] Mask containing current input state
        //              The least significant bit is input #0
        //              The most significant bit is input #31
        // Special data length rules:
        //      - The client must respond with at least the minimum number of bytes required 
        //      to accurately contain the desired input state value.
        // Notes:
        //      - The client may choose to make this property read-only
    
    public static final int PROP_INPUT_CONFIG_0             = 0xF910;
    public static final int PROP_INPUT_CONFIG_1             = 0xF911;
    public static final int PROP_INPUT_CONFIG_2             = 0xF912;
    public static final int PROP_INPUT_CONFIG_3             = 0xF913;
    public static final int PROP_INPUT_CONFIG_4             = 0xF914;
    public static final int PROP_INPUT_CONFIG_5             = 0xF915;
    public static final int PROP_INPUT_CONFIG_6             = 0xF916;
    public static final int PROP_INPUT_CONFIG_7             = 0xF917;
    public static final int PROP_INPUT_CONFIG_8             = 0xF918;
    public static final int PROP_INPUT_CONFIG_9             = 0xF919;
    public static final int PROP_INPUT_CONFIG_A             = 0xF91A;
    public static final int PROP_INPUT_CONFIG_B             = 0xF91B;
    public static final int PROP_INPUT_CONFIG_C             = 0xF91C;
    public static final int PROP_INPUT_CONFIG_D             = 0xF91D;
    public static final int PROP_INPUT_CONFIG_E             = 0xF91E;
    public static final int PROP_INPUT_CONFIG_F             = 0xF91F;
        // Description: [optional]
        //      Digital input configuration
        // Value:
        //      0:4 - [UInt32] Support mask
        //              0x00000001 - Trigger event when state changes to 'On'
        //              0x00000002 - Trigger event when state changes to 'Off'
        //              0x00000008 - High priority (when used with event generation)
        //              0x00000010 - Start elapse-timer when state changes to 'On'
        //              0x00000020 - Start elapse-timer when state changes to 'Off'
        //              0x00000100 - Trigger output cycle when state changes to 'On'
        //              0x00000200 - Trigger output cycle when state changes to 'Off'
        //      4:4 - [UInt32] Reserved
        // Special data length rules:
        //      - The client must respond with at least the minimum number of bytes required 
        //      to accurately contain the desired mask value.
        // Notes:
        //      - A 'support mask' of '0' indicates that this digital input will be ignored.
        //      - The triggered outputs are determined by client implementation.

    public static final int PROP_OUTPUT_CONFIG_0            = 0xF930;
    public static final int PROP_OUTPUT_CONFIG_1            = 0xF931;
    public static final int PROP_OUTPUT_CONFIG_2            = 0xF932;
    public static final int PROP_OUTPUT_CONFIG_3            = 0xF933;
    public static final int PROP_OUTPUT_CONFIG_4            = 0xF934;
    public static final int PROP_OUTPUT_CONFIG_5            = 0xF935;
    public static final int PROP_OUTPUT_CONFIG_6            = 0xF936;
    public static final int PROP_OUTPUT_CONFIG_7            = 0xF937;
        // Description: [optional]
        //      Digital output configuration
        // Value:
        //      0:4 - [UInt32] Support mask
        //              0x00000001 - Trigger event when output is turned 'On'
        //              0x00000002 - Trigger event when output is turned 'Off'
        //              0x00000008 - High priority (when used with event generation)
        //      4:4 - [UInt32] Maximum 'ON' time (in milliseconds)
        //              A value of '0' indicates that the output should remain on indefinitely.
        // Special data length rules:
        //      - The client must respond with at least the minimum number of bytes required 
        //      to accurately contain the desired mask value.
        //      - While the 'maximum on' time is specified in milliseconds, it may not be
        //      possible for the client to provide that level of granularity.  In this case
        //      the client may choose to round up to the next nearest second if necessary.

    public static final int PROP_ELAPSED_0_VALUE            = 0xF970;
    public static final int PROP_ELAPSED_1_VALUE            = 0xF971;
    public static final int PROP_ELAPSED_2_VALUE            = 0xF972;
    public static final int PROP_ELAPSED_3_VALUE            = 0xF973;
    public static final int PROP_ELAPSED_4_VALUE            = 0xF974;
    public static final int PROP_ELAPSED_5_VALUE            = 0xF975;
    public static final int PROP_ELAPSED_6_VALUE            = 0xF976;
    public static final int PROP_ELAPSED_7_VALUE            = 0xF977;
        // Description: [optional]
        //      Device elapsed timer values
        // Value:
        //      0:4 - [UInt32] Elapsed timer seconds.
        // Special data length rules:
        //      - The client must respond with at least the minimum number of bytes required 
        //      to accurately respresent the value.

    public static final int PROP_ELAPSED_0_LIMIT            = 0xF980;
    public static final int PROP_ELAPSED_1_LIMIT            = 0xF981;
    public static final int PROP_ELAPSED_2_LIMIT            = 0xF982;
    public static final int PROP_ELAPSED_3_LIMIT            = 0xF983;
    public static final int PROP_ELAPSED_4_LIMIT            = 0xF984;
    public static final int PROP_ELAPSED_5_LIMIT            = 0xF985;
    public static final int PROP_ELAPSED_6_LIMIT            = 0xF986;
    public static final int PROP_ELAPSED_7_LIMIT            = 0xF987;
        // Description: [optional]
        //      Device elapsed timer triggered alarm point
        // Value:
        //      0:4 - [UInt32] MSB of elapsed timer
        //      4:4 - [UInt32] LSB of elapsed timer.  Once the client has achieved this number of
        //            milliseconds it should trigger a corresponding STATUS_ELAPSED_LIMIT_# event. 
        //            Value range 0.000 and up.
        // Special data length rules:
        //      - The client must respond with at least the minimum number of bytes required 
        //      to accurately respresent the value.
        // Notes:
        //      - A value of '0' indicates that no alarm/event will be generated.
        //      - This can be considered a single 64-bit integer, if supported by the client.
        //      - The high order 32-bits is not necessary unless the timer needs to count
        //      beyond 49 days (the limit of a single 32-bit value).
        //      - Once this limit is reached, the client should issue a corresponding
        //      STATUS_ELAPSED_LIMIT_# event, however, the decision to reset the timer or
        //      continue to count is left to the client.
    
    // ----------------------------------------------------------------------------
    // Sensor configuration properties:

    public static final int PROP_UNDERVOLTAGE_LIMIT         = 0xFB01;  // undervoltage limit
        // Description: [optional]
        //      Undervoltage limit
        // Value:
        //      0:4 - [UInt32] Undervoltage limit in millivolts
        // Notes:
        //      - A value of '0' indicates that no undervoltage alarm/event will be generated.
        //      - When the supply voltage falls below this value, the client should issue a
        //      STATUS_LOW_BATTERY event.  The client may decide how often this event is to 
        //      be repeated should the voltage remain below this threshold.

    public static final int PROP_SENSOR_CONFIG_0            = 0xFB10;  // Set sensor 0 config
    public static final int PROP_SENSOR_CONFIG_1            = 0xFB11;  // Set sensor 1 config
    public static final int PROP_SENSOR_CONFIG_2            = 0xFB12;  // Set sensor 2 config
    public static final int PROP_SENSOR_CONFIG_3            = 0xFB13;  // Set sensor 3 config
    public static final int PROP_SENSOR_CONFIG_4            = 0xFB14;  // Set sensor 4 config
    public static final int PROP_SENSOR_CONFIG_5            = 0xFB15;  // Set sensor 5 config
    public static final int PROP_SENSOR_CONFIG_6            = 0xFB16;  // Set sensor 6 config
    public static final int PROP_SENSOR_CONFIG_7            = 0xFB17;  // Set sensor 7 config
        // Description: [optional]
        //      Set sensor # configuration
        // Value:
        //   32-bit type:
        //      0:4 - [UInt32] gain
        //            Valid range: 0 to 4294967295 units (as defined by the client)
        //      4:4 - [UInt32] offset
        //            Valid range: 0 to 4294967295 units (as defined by the client)
        // Special data length rules:
        //      - If the length is less-than 8, then the length must be divisible by 2.
        //      Each gain/offset value will then be composed of the number of bytes
        //      defined by the quotient.
        // Notes:
        //      - The units of this value are defined by the client.
    
    public static final int PROP_SENSOR_RANGE_0             = 0xFB20;  // Set sensor 0 high/low
    public static final int PROP_SENSOR_RANGE_1             = 0xFB21;  // Set sensor 1 high/low
    public static final int PROP_SENSOR_RANGE_2             = 0xFB22;  // Set sensor 2 high/low
    public static final int PROP_SENSOR_RANGE_3             = 0xFB23;  // Set sensor 3 high/low
    public static final int PROP_SENSOR_RANGE_4             = 0xFB24;  // Set sensor 4 high/low
    public static final int PROP_SENSOR_RANGE_5             = 0xFB25;  // Set sensor 5 high/low
    public static final int PROP_SENSOR_RANGE_6             = 0xFB26;  // Set sensor 6 high/low
    public static final int PROP_SENSOR_RANGE_7             = 0xFB27;  // Set sensor 7 high/low
        // Description: [optional]
        //      Set sensor # high/low range
        // Value:
        //   32-bit type:
        //      0:4 - [UInt32] Low range
        //            Valid range: 0 to 4294967295 units (as defined by the client)
        //      4:4 - [UInt32] High range
        //            Valid range: 0 to 4294967295 units (as defined by the client)
        // Special data length rules:
        //      - If the length is less-than 8, then the length must be divisible by 2.
        //      Each low/high range value will then be composed of the number of bytes
        //      defined by the quotient.
        // Notes:
        //      - The units of this range is defined by the client.
    
    // ----------------------------------------------------------------------------
    // Temperature configuration:
    
    public static final int PROP_TEMP_SAMPLE_INTRVL         = 0xFB60;
        // Description: [optional]
        //      Set temperature sampling interval (in seconds)
        // Value:
        //      0:4 - [UInt32] Sample rate in seconds
        //            Valid range: 0 to 4294967295 seconds
        //      4:4 - [UInt32] Port close indicator
        //            If this value is '0', the temperature monitor port will be
        //            left open, non-zero and the port will be closed.
        // Notes:
        //      - If the length is less-than 8, then the length must be divisible by 2.
        //      Each configuration value will then be composed of the number of bytes
        //      defined by the quotient.

    public static final int PROP_TEMP_REPORT_INTRVL         = 0xFB63;
        // Description: [optional]
        //      Set temperature reporting intervals (in seconds)
        // Value:
        //      0:4 - [UInt32] Periodic reporting interval in seconds
        //            Valid range: 0 to 4294967295 seconds
        //      4:4 - [UInt32] Alarm reporting interval in seconds
        //            Valid range: 0 to 4294967295 seconds
        // Notes:
        //      - The periodic reporting interval represents the time between general
        //      temperature events.
        //      - The alarm reporting interval represent the time between temperature 
        //      range alarms while a temperature sensor remains out-of-range.

    public static final int PROP_TEMP_CONFIG_0              = 0xFB70;  // Set temp 0 config
    public static final int PROP_TEMP_CONFIG_1              = 0xFB71;  // Set temp 1 config
    public static final int PROP_TEMP_CONFIG_2              = 0xFB72;  // Set temp 2 config
    public static final int PROP_TEMP_CONFIG_3              = 0xFB73;  // Set temp 3 config
        // Description: [optional]
        //      Set temperature sensor # configuration
        // Value:
        //      0:4 - [UInt32] Config 1
        //            Valid range: 0 to 4294967295 units (as defined by the client)
        //      2:4 - [UInt32] Config 2
        //            Valid range: 0 to 4294967295 units (as defined by the client)
        // Special data length rules:
        //      - If the length is less-than 8, then the length must be divisible by 2.
        //      Each configuration value will then be composed of the number of bytes
        //      defined by the quotient.
    
    public static final int PROP_TEMP_RANGE_0               = 0xFB80;  // Set temp 0 high/low
    public static final int PROP_TEMP_RANGE_1               = 0xFB81;  // Set temp 1 high/low
    public static final int PROP_TEMP_RANGE_2               = 0xFB82;  // Set temp 2 high/low
    public static final int PROP_TEMP_RANGE_3               = 0xFB83;  // Set temp 3 high/low
        // Description: [optional]
        //      Set temperature sensor # high/low range
        // Value:
        //      0:2 - [UInt16] Signed Low  range
        //            Valid range: -3276.7C to +3276.7C
        //      2:2 - [UInt16] Signed High range (-3276.7C to +3276.7C)
        //            Valid range: -3276.7C to +3276.7C
        // Special data length rules:
        //      - If the length is less-than 4, then the length must be divisible by 2.
        //      Each low/high range value will then be composed of the number of bytes
        //      defined by the quotient.

}
