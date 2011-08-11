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
//  OpenDMTP protocol client error constants.
// ----------------------------------------------------------------------------
// Change History:
//  2006/03/26  Martin D. Flynn
//      Initial release
//  2006/11/03  Robert Puckett
//      Include JavaDocs
// ----------------------------------------------------------------------------
package com.tommasocodella.androdmtp.opendmtp.codes;


/**
* Contains OpenDMTP protocol client error constants.
*/
public interface ClientErrors
{

// ----------------------------------------------------------------------------
// Protocol/Packet errors (data provides specifics):

    public static final int ERROR_PACKET_HEADER                 = 0xF111;
    // Description:
    //      Invalid packet header
    // Payload:
    //      0:2 - This error code
    //      2:1 - Packet header causing error (if available)
    //      3:1 - Packet type causing error (if available)
    // Notes:
    //      Sent to server when the packet header is not recognized.
    
    public static final int ERROR_PACKET_TYPE                   = 0xF112;
    // Description:
    //      Invalid packet type
    // Payload:
    //      0:2 - This error code
    //      2:1 - Packet header causing error
    //      3:1 - Packet type causing error
    // Notes:
    //      Sent to server when the packet type is not recognized.

    public static final int ERROR_PACKET_LENGTH                 = 0xF113;
    // Description:
    //      Invalid packet length
    // Payload:
    //      0:2 - This error code
    //      2:1 - Packet header causing error (if available)
    //      3:1 - Packet type causing error (if available)
    // Notes:
    //      Sent to server when the packet length is invalid.

    public static final int ERROR_PACKET_ENCODING               = 0xF114;
    // Description:
    //      Invalid/unsupported packet encoding
    // Payload:
    //      0:2 - This error code
    //      2:1 - Packet header causing error
    //      3:1 - Packet type causing error
    // Notes:
    //      Sent to server when the packet encoding is not supported by the client.

    public static final int ERROR_PACKET_PAYLOAD                = 0xF115;
    // Description:
    //      Invalid packet payload
    // Payload:
    //      0:2 - This error code
    //      2:1 - Packet header causing error
    //      3:1 - Packet type causing error
    // Notes:
    //      Sent to server when the packet payload is invalid.

    public static final int ERROR_PACKET_CHECKSUM               = 0xF116;
    // Description:
    //      Invalid packet checksum (ASCII encoding only)
    // Payload:
    //      0:2 - This error code
    //      2:1 - Packet header causing error
    //      3:1 - Packet type causing error
    // Notes:
    //      Sent to server when the packet checksum appears to be invalid.
    //      This can only occur for ASCII encoded packets.

    public static final int ERROR_PACKET_ACK                    = 0xF117;
    // Description:
    //      Packet ACK sequence invalid
    // Payload:
    //      0:2 - This error code
    //      2:1 - Packet header causing error
    //      3:1 - Packet type causing error
    // Notes:
    //      Sent to server when the packet ACK sequence number was not found
    //      in the list of sent/unacknowledged event packets.

    public static final int ERROR_PROTOCOL_ERROR                = 0xF121;
    // Description:
    //      Protocol error
    // Payload:
    //      0:2 - This error code
    //      2:1 - Packet header causing error
    //      3:1 - Packet type causing error
    // Notes:
    //      Sent to server when the client does not receive an expected
    //      response from the server.

// ----------------------------------------------------------------------------
// Property errors (data provides specifics):

    public static final int ERROR_PROPERTY_READ_ONLY            = 0xF201;
    // Description:
    //      Property is read-only
    // Payload:
    //      0:2 - This error code
    //      2:2 - the id of the read-only property
    // Notes:
    //      Sent to server when an attempt is made to set a read-only property
    
    public static final int ERROR_PROPERTY_WRITE_ONLY           = 0xF202;
    // Description:
    //      Property is write-only
    // Payload:
    //      0:2 - This error code
    //      2:2 - the id of the write-only property
    // Notes:
    //      Sent to server when an attempt is made to read a write-only property
    
    public static final int ERROR_PROPERTY_INVALID_ID           = 0xF211;
    // Description:
    //      Invalid/unsupported property ID
    // Payload:
    //      0:2 - This error code
    //      2:2 - the id of the unrecognized property
    // Notes:
    //      Sent to server when an attempt is made to get/set an unrecognized property id
    
    public static final int ERROR_PROPERTY_INVALID_VALUE        = 0xF212;
    // Description:
    //      Invalid property value
    // Payload:
    //      0:2 - This error code
    //      2:2 - the id of the property which is attempting to be get/set
    // Notes:
    //      Sent to server when a specified value is invalid for the property type
    
    public static final int ERROR_PROPERTY_UNKNOWN_ERROR        = 0xF213;
    // Description:
    //      Invalid property value
    // Payload:
    //      0:2 - This error code
    //      2:2 - the id of the property which has the error
    // Notes:
    //      Sent to server when a specified value is invalid for the property type

// ----------------------------------------------------------------------------
// Command errors (data provides specifics):

    public static final int ERROR_COMMAND_INVALID               = 0xF311;
    // Description:
    //      The specified command is invalid/unsupported
    // Payload:
    //      0:2 - This error code
    //      2:2 - the id of the invalid command.
    // Notes:
    //      Sent to the server when the client is requested to perform a 
    //      command which it does not support.

    public static final int ERROR_COMMAND_ERROR                 = 0xF321;
    // Description:
    //      The command arguments are invalid, or an execution error was encountered.
    // Payload:
    //      0:2 - This error code
    //      2:2 - the id of the command which had the error
    //      4:2 - returned command error (reason)
    //      6:X - other data which may be useful in diagnosing the error [optional]
    // Notes:
    //      Sent to the server when the executed client command has found an 
    //      error either in the command arguments, or in the execution of the command.

// ----------------------------------------------------------------------------
// Upload errors (data provides specifics):

    public static final int ERROR_UPLOAD_TYPE                   = 0xF401;
    // Description:
    //      Invalid speficied upload type
    // Payload:
    //      0:2 - This error code
    // Notes:
    //      Sent to the server when the upload record type is not recognized.

    public static final int ERROR_UPLOAD_LENGTH                 = 0xF411;
    // Description:
    //      Invalid speficied upload file size (too small/large)
    // Payload:
    //      0:2 - This error code
    // Notes:
    //      Sent to the server when the specified data length is too large/small.

    public static final int ERROR_UPLOAD_OFFSET_OVERLAP         = 0xF412;
    // Description:
    //      Invalid speficied upload file offset
    // Payload:
    //      0:2 - This error code
    // Notes:
    //      Sent to the server when the specified data offset overlaps a previous record.

    public static final int ERROR_UPLOAD_OFFSET_GAP             = 0xF413;
    // Description:
    //      Invalid speficied upload file offset
    // Payload:
    //      0:2 - This error code
    // Notes:
    //      Sent to the server when the specified data offset leaves a gap between this
    //      and the previous record.

    public static final int ERROR_UPLOAD_OFFSET_OVERFLOW        = 0xF414;
    // Description:
    //      Invalid speficied upload file offset
    // Payload:
    //      0:2 - This error code
    // Notes:
    //      Sent to the server when the specified data offset and the length of the
    //      provided data exceeds the previously specified length of the file.

    public static final int ERROR_UPLOAD_FILE_NAME              = 0xF421;
    // Description:
    //      Invalid speficied upload file name
    // Payload:
    //      0:2 - This error code
    // Notes:
    //      Sent to the server when the specified filename is invalid.

    public static final int ERROR_UPLOAD_CHECKSUM               = 0xF431;
    // Description:
    //      Invalid speficied upload checksum
    // Payload:
    //      0:2 - This error code
    // Notes:
    //      Sent to the server when the specified checksum value is invalid.

    public static final int ERROR_UPLOAD_SAVE                   = 0xF441;
    // Description:
    //      Error saving upload file
    // Payload:
    //      0:2 - This error code
    //      2:X - Additional diagnostic information as needed.
    // Notes:
    //      Sent to the server when the client is unable to save the uploaded
    //      file.  Possibly due to some internal client error.

// ----------------------------------------------------------------------------
// GPS errors (data provides specifics):

    public static final int ERROR_GPS_EXPIRED                   = 0xF911;
    // Description:
    //      GPS fix expired (possible antenna problem)
    // Payload:
    //      0:2 - This error code
    //      2:4 - the time of the last valid fix
    // Notes:
    //      Sent to server when the client has determined that a new GPS
    //      fix has not bee acquired in the expected time frame (as specified
    //      by the property PROP_GPS_EXPIRATION).  This typically means that
    //      either the device is not in an area where a GPS fix is possible,
    //      or that there may be a problem with the GPS antenna.

    public static final int ERROR_GPS_FAILURE                   = 0xF912;
    // Description:
    //      Lost communication with GPS module (possible module problem)
    // Payload:
    //      0:2 - This error code
    //      2:4 - the time of the last GPS communication
    //      6:X - anything else that the client deems useful to diagnosing this problem.
    // Notes:
    //      This differs from ERROR_GPS_EXPIRED is that no communication from
    //      GPS module (whether valid, or invalid) has been received in the 
    //      expected time frame (typically 15 to 30 seconds).  This typically
    //      indicates a failure in the GPS module.

// ----------------------------------------------------------------------------
// Internal errors (data provides specifics):

    public static final int ERROR_INTERNAL_ERROR_00             = 0xFE00;
// ...
    public static final int ERROR_INTERNAL_ERROR_0F             = 0xFE0F;
    // Description:
    //      Internal error, as defined by client device
    // Payload:
    //      0:2 - This error code
    //      2:X - payload format is defined by the client.
    // Notes:
    //      These error codes are for use by the client to allow general
    //      error information to be sent to the server for analysis.

// ----------------------------------------------------------------------------

}
