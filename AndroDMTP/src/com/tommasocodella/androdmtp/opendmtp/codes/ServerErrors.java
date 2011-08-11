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
//  OpenDMTP protocol server error constants.
// ----------------------------------------------------------------------------
// Change History:
//  2006/03/26  Martin D. Flynn
//      Initial release
//  2006/04/09  Martin D. Flynn
//      Added 'NAK_ACCOUNT_ERROR' and 'NAK_DEVICE_ERROR'
//  2006/11/03  Robert Puckett
//      Include JavaDocs
// ----------------------------------------------------------------------------
package com.tommasocodella.androdmtp.opendmtp.codes;

/**
* Contains OpenDMTP protocol server error constants.
*/
public interface ServerErrors
{
    
// ----------------------------------------------------------------------------
// No error:

    /**
    * Contains a constant signifying no error.
    */
    public static final int NAK_OK                          = 0x0000;

// ----------------------------------------------------------------------------
// Account/Device errors:

    public static final int NAK_ID_INVALID                  = 0xF011;
    // Description:
    //      Invalid unique id
    // Payload:
    //      0:2 - This error code
    //      2:1 - Packet header causing error
    //      3:1 - Packet type causing error
    // Notes:
    //      Sent to client when an invalid/unrecognized unique id was specified

    public static final int NAK_ACCOUNT_INVALID             = 0xF021;
    // Description:
    //      Invalid/missing account id
    // Payload:
    //      0:2 - This error code
    //      2:1 - Packet header causing error
    //      3:1 - Packet type causing error
    // Notes:
    //      Sent to client when account id is not recognized

    public static final int NAK_ACCOUNT_INACTIVE            = 0xF022;
    // Description:
    //      Account has expired, or has become inactive
    // Payload:
    //      0:2 - This error code
    //      2:1 - Packet header causing error
    //      3:1 - Packet type causing error
    // Notes:
    //      Sent to client when account id is no longer active

    public static final int NAK_ACCOUNT_ERROR               = 0xF023;
    // Description:
    //      A server error occured while loading Account
    // Payload:
    //      0:2 - This error code
    //      2:1 - Packet header causing error
    //      3:1 - Packet type causing error
    // Notes:
    //      Sent to client when server encountered an error on the Account

    public static final int NAK_DEVICE_INVALID              = 0xF031;
    // Description:
    //      Invalid/missing device id
    // Payload:
    //      0:2 - This error code
    //      2:1 - Packet header causing error
    //      3:1 - Packet type causing error
    // Notes:
    //      Sent to client when device id is not recognized

    public static final int NAK_DEVICE_INACTIVE             = 0xF032;
    // Description:
    //      Device has expired, or has become inactive
    // Payload:
    //      0:2 - This error code
    //      2:1 - Packet header causing error
    //      3:1 - Packet type causing error
    // Notes:
    //      Sent to client when this device id is no longer active

    public static final int NAK_DEVICE_ERROR               = 0xF033;
    // Description:
    //      A server error occured while loading Device
    // Payload:
    //      0:2 - This error code
    //      2:1 - Packet header causing error
    //      3:1 - Packet type causing error
    // Notes:
    //      Sent to client when server encountered an error on the Device

    public static final int NAK_EXCESSIVE_CONNECTIONS       = 0xF041;
    // Description:
    //       Excessive connections
    // Payload:
    //      0:2 - This error code
    //      2:1 - Packet header causing error
    //      3:1 - Packet type causing error
    // Notes:
    //      Sent to client when too many connections have been made in the alotted time.

// ----------------------------------------------------------------------------
// Packet errors:

    public static final int NAK_PACKET_HEADER               = 0xF111;
    // Description:
    //      Invalid/Unsupported packet header
    // Payload:
    //      0:2 - This error code
    //      2:1 - Packet header causing error
    //      3:1 - Packet type causing error
    // Notes:
    //      Sent to client when the packet header is not recognized.

    public static final int NAK_PACKET_TYPE                 = 0xF112;
    // Description:
    //      Invalid/Unsupported packet type
    // Payload:
    //      0:2 - This error code
    //      2:1 - Packet header causing error
    //      3:1 - Packet type causing error
    // Notes:
    //      Sent to client when the packet type is not recognized.

    public static final int NAK_PACKET_LENGTH               = 0xF113;
    // Description:
    //      Invalid packet length
    // Payload:
    //      0:2 - This error code
    //      2:1 - Packet header causing error
    //      3:1 - Packet type causing error
    // Notes:
    //      Sent to client when the packet length is invalid.

    public static final int NAK_PACKET_PAYLOAD              = 0xF114;
    // Description:
    //      Invalid packet payload
    // Payload:
    //      0:2 - This error code
    //      2:1 - Packet header causing error
    //      3:1 - Packet type causing error
    // Notes:
    //      Sent to client when the packet payload is invalid (in the case where the
    //      server is able to perform such validation).

    public static final int NAK_PACKET_ENCODING             = 0xF115;
    // Description:
    //      Encoding not supported
    // Payload:
    //      0:2 - This error code
    //      2:1 - Packet header causing error
    //      3:1 - Packet type causing error
    // Notes:
    //      Sent to the client when packet has been received using an encoding
    //      that is not supported by the server.

    public static final int NAK_PACKET_CHECKSUM             = 0xF116;
    // Description:
    //      Invalid packet checksum (ASCII encoding only)
    // Payload:
    //      0:2 - This error code
    //      2:1 - Packet header causing error
    //      3:1 - Packet type causing error
    // Notes:
    //      - Sent to client when the packet checksum appears to be invalid.
    //      This can only occur for ASCII encoded packets.
    //      - This likely indicates a transmission error.  If this is the case
    //      then the server is unable to trust any information contained in the
    //      packet.
  
// ----------------------------------------------------------------------------
// Protocol errors:

    public static final int NAK_BLOCK_CHECKSUM              = 0xF311;
    // Description:
    //      Invalid block checksum
    // Payload:
    //      0:2 - This error code
    //      2:1 - Packet header causing error
    //      3:1 - Packet type causing error
    // Notes:
    //      Sent to client when the block checksum appears to be invalid.

    public static final int NAK_PROTOCOL_ERROR              = 0xF312;
    // Description:
    //      Protocol error
    // Payload:
    //      0:2 - This error code
    //      2:1 - Packet header causing error
    //      3:1 - Packet type causing error
    // Notes:
    //      Sent to client when the server does not receive an expected
    //      response from the client.
  
// ----------------------------------------------------------------------------
// Event packet errors:

    public static final int NAK_FORMAT_DEFINITION_INVALID   = 0xF411;
    // Description:
    //      Custom format definition is invalid
    // Payload:
    //      0:2 - This error code
    //      2:1 - Packet header causing error
    //      3:1 - Packet type causing error
    //      4:1 - Custom format packet type
    // Notes:
    //      - Sent to client when the server has determined that the specified custom
    //      format definition is invalid.  One of the following errors were found:
    //          - The custom format packet is invalid (ie. not within the proper range)
    //          - The specified number of fields was invalid.
    //          - The combined field length is greater than the maximum payload length.
    //          - A specified field type was not recognized.

    public static final int NAK_FORMAT_NOT_SUPPORTED        = 0xF421;
    // Description:
    //      Custom formats are not supported
    // Payload:
    //      0:2 - This error code
    //      2:1 - Packet header causing error
    //      3:1 - Packet type causing error
    // Notes:
    //      Sent to client when the server does not support custom format types,
    //      or if the level of service provide by the DMT service provider does
    //      not allow them.

    public static final int NAK_FORMAT_NOT_RECOGNIZED       = 0xF422;
    // Description:
    //      Custom format not recognized
    // Payload:
    //      0:2 - This error code
    //      2:1 - Packet header causing error
    //      3:1 - Packet type causing error
    // Notes:
    //      - Sent to client when the server does not recognize a custom format type
    //      sent by the client.

    public static final int NAK_EXCESSIVE_EVENTS            = 0xF431;
    // Description:
    //      Excessive events
    // Payload:
    //      0:2 - This error code
    //      2:1 - Packet header causing error
    //      3:1 - Packet type causing error
    //      4:X - Event sequence number (if available)
    // Notes:
    //      - Sent to client when too many events have been sent in the alotted time
    //      based on the current level of service.
    //      - The server should only send this error AFTER it sends any applicable ACK.
    //      - When receiving this error, the client should act on it accordingly by
    //      removing the offending sent event (if present), and pulling back on the 
    //      number of generated events.

    public static final int NAK_DUPLICATE_EVENT             = 0xF432;
    // Description:
    //      Duplicate event found
    // Payload:
    //      0:2 - This error code
    //      2:1 - Packet header causing error
    //      3:1 - Packet type causing error
    //      4:X - Event sequence number (if available)
    // Notes:
    //      - Sent to client when the server has determined that a sent event
    //      already exists in the database.
    //      - This error is typically ignored on the client side since there is usually
    //      little that the client can do about it anyway.  As such, the server may
    //      optionally choose not to return this error to the client, but instead
    //      quietly ignore the duplicate event packet.

    public static final int NAK_EVENT_ERROR                 = 0xF441;
    // Description:
    //      Generic event error detected
    // Payload:
    //      0:2 - This error code
    //      2:1 - Packet header causing error
    //      3:1 - Packet type causing error
    //      4:X - Event sequence number (if available)
    // Notes:
    //      - Sent to client when the server has encountered an error that
    //      prevented this event from being inserted into the database.
    //      - This error is typically ignored on the client side since there is usually
    //      little that the client can do about it anyway.  As such, the server may
    //      optionally choose not to return this error to the client.

// ----------------------------------------------------------------------------
    
}
