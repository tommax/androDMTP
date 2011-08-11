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
//  This class handles the parsing and encoding for client and server packets.
// ----------------------------------------------------------------------------
// Change History:
//  2006/03/26  Martin D. Flynn
//      Initial release
//  2006/11/03  Joshua Stupplebeen
//      Include JavaDocs
// ----------------------------------------------------------------------------
package com.tommasocodella.androdmtp.opendmtp.client.base;

import com.tommasocodella.androdmtp.opendmtp.codes.Encoding;
import com.tommasocodella.androdmtp.opendmtp.codes.ServerErrors;
import com.tommasocodella.androdmtp.opendmtp.util.Base64;
import com.tommasocodella.androdmtp.opendmtp.util.GeoEvent;
import com.tommasocodella.androdmtp.opendmtp.util.Payload;
import com.tommasocodella.androdmtp.opendmtp.util.StringTools;

/**
* Implements the basic packet structure and methods necessary to handle the parsing and encoding
* for client and server packets. These handlers include predefined payload templates, encoding
* standards, and checksum methods to ensure packet integrity. The packet data is defined down to
* the byte level and encoded for either binary or ASCII tranmission.
*/
public class Packet
{

    // ------------------------------------------------------------------------
    
    /**
    * Minimum header length.
    */
    public static final int     MIN_HEADER_LENGTH           = 3;

    /**
    * Maximum payload length.
    */
    public static final int     MAX_PAYLOAD_LENGTH          = Payload.MAX_PAYLOAD_LENGTH;

    /**
    * Standard hexidecimal header for encoded DMTP packets.
    */
    public static final int     HEADER_BASIC                = 0xE0;
    // The value of 'HEADER_BASIC' must NOT be one of the following values:
    //    0x0A - This is the newline character and may be used to separate ASCII packets
    //    0x0D - This is the carriage-return character used to separate ASCII packets
    //    0x23 - This is reserved for encrypted ASCII encoded packets.
    //    0x24 - This is the start of an ASCII encoded packed ("$")

    // ------------------------------------------------------------------------

    // this value is used to indicate that 'all' sent event packets are to be acknowledged
    public static final long    SEQUENCE_ALL                = -1L; // 0xFFFFFFFFL;

    // ------------------------------------------------------------------------
    // Packet priority

    public static final int     PRIORITY_NONE               = 0; 
    public static final int     PRIORITY_LOW                = 1;    // generally sent via Simplex only (GPRS)
    public static final int     PRIORITY_NORMAL             = 2;    // generally sent via Duplex  only (GPRS)
    public static final int     PRIORITY_HIGH               = 3;    // generally sent via Duplex  only (GPRS, then Satellite)

    // ------------------------------------------------------------------------
    // Client originated packets 
    
    // dialog packets
    public static final int     PKT_CLIENT_EOB_DONE         = 0x00;    // End of block/transmission, "no more to say"
    public static final int     PKT_CLIENT_EOB_MORE         = 0x01;    // End of block/transmission, "I have more to say"
    
    // identification packets
    public static final int     PKT_CLIENT_UNIQUE_ID        = 0x11;    // Unique identifier
    public static final int     PKT_CLIENT_ACCOUNT_ID       = 0x12;    // Account identifier
    public static final int     PKT_CLIENT_DEVICE_ID        = 0x13;   // Device identifier

    // standard fixed format event packets
    public static final int     PKT_CLIENT_FIXED_FMT_STD    = 0x30;    // Standard GPS
    public static final int     PKT_CLIENT_FIXED_FMT_HIGH   = 0x31;    // High Resolution GPS
    public static final int     PKT_CLIENT_FIXED_FMT_F      = 0x3F;    // Reserved

    // DMTP service provider format event packets
    public static final int     PKT_CLIENT_DMTSP_FMT_0      = 0x50;    //
    public static final int     PKT_CLIENT_DMTSP_FMT_1      = 0x51;    //
    public static final int     PKT_CLIENT_DMTSP_FMT_2      = 0x52;    //
    public static final int     PKT_CLIENT_DMTSP_FMT_3      = 0x53;    //
    public static final int     PKT_CLIENT_DMTSP_FMT_4      = 0x54;    //
    public static final int     PKT_CLIENT_DMTSP_FMT_5      = 0x55;    //
    public static final int     PKT_CLIENT_DMTSP_FMT_6      = 0x56;    //
    public static final int     PKT_CLIENT_DMTSP_FMT_7      = 0x57;    //
    public static final int     PKT_CLIENT_DMTSP_FMT_8      = 0x58;    //
    public static final int     PKT_CLIENT_DMTSP_FMT_9      = 0x59;    //
    public static final int     PKT_CLIENT_DMTSP_FMT_A      = 0x5A;    //
    public static final int     PKT_CLIENT_DMTSP_FMT_B      = 0x5B;    //
    public static final int     PKT_CLIENT_DMTSP_FMT_C      = 0x5C;    //
    public static final int     PKT_CLIENT_DMTSP_FMT_D      = 0x5D;    //
    public static final int     PKT_CLIENT_DMTSP_FMT_E      = 0x5E;    //
    public static final int     PKT_CLIENT_DMTSP_FMT_F      = 0x5F;    //

    // custom format event packets
    public static final int     PKT_CLIENT_CUSTOM_FORMAT_0  = 0x70;    // Custom format data #0
    public static final int     PKT_CLIENT_CUSTOM_FORMAT_1  = 0x71;    // Custom format data #1
    public static final int     PKT_CLIENT_CUSTOM_FORMAT_2  = 0x72;    // Custom format data #2
    public static final int     PKT_CLIENT_CUSTOM_FORMAT_3  = 0x73;    // Custom format data #3
    public static final int     PKT_CLIENT_CUSTOM_FORMAT_4  = 0x74;    // Custom format data #4
    public static final int     PKT_CLIENT_CUSTOM_FORMAT_5  = 0x75;    // Custom format data #5
    public static final int     PKT_CLIENT_CUSTOM_FORMAT_6  = 0x76;    // Custom format data #6
    public static final int     PKT_CLIENT_CUSTOM_FORMAT_7  = 0x77;    // Custom format data #7
    public static final int     PKT_CLIENT_CUSTOM_FORMAT_8  = 0x78;    // Custom format data #8
    public static final int     PKT_CLIENT_CUSTOM_FORMAT_9  = 0x79;    // Custom format data #9
    public static final int     PKT_CLIENT_CUSTOM_FORMAT_A  = 0x7A;    // Custom format data #A
    public static final int     PKT_CLIENT_CUSTOM_FORMAT_B  = 0x7B;    // Custom format data #B
    public static final int     PKT_CLIENT_CUSTOM_FORMAT_C  = 0x7C;    // Custom format data #C
    public static final int     PKT_CLIENT_CUSTOM_FORMAT_D  = 0x7D;    // Custom format data #D
    public static final int     PKT_CLIENT_CUSTOM_FORMAT_E  = 0x7E;    // Custom format data #E
    public static final int     PKT_CLIENT_CUSTOM_FORMAT_F  = 0x7F;    // Custom format data #F

    // Property packet
    public static final int     PKT_CLIENT_PROPERTY_VALUE   = 0xB0;    // Property value
    
    // Custom format packet
    public static final int     PKT_CLIENT_FORMAT_DEF_24    = 0xCF;    // Custom format definition (24 bit field def)

    // Diagnostic/Error packets
    public static final int     PKT_CLIENT_DIAGNOSTIC       = 0xD0;    // Diagnostic codes
    public static final int     PKT_CLIENT_ERROR            = 0xE0;    // Error codes

    // ------------------------------------------------------------------------
    // Server originated packets 
    
    /* End-Of-Block packets */
    public static final int     PKT_SERVER_EOB_DONE         = 0x00;    // ""       : End of transmission, query response
    public static final int     PKT_SERVER_EOB_SPEAK_FREELY = 0x01;    // ""       : End of transmission, speak freely

    // Acknowledge packet
    public static final int     PKT_SERVER_ACK              = 0xA0;    // "%*u"    : Acknowledge

    // Property packets
    public static final int     PKT_SERVER_GET_PROPERTY     = 0xB0;    // "%2u"    : Get property
    public static final int     PKT_SERVER_SET_PROPERTY     = 0xB1;    // "%2u%*b" : Set property

    // File upload packet
    public static final int     PKT_SERVER_FILE_UPLOAD      = 0xC0;    // "%1x%3u%*b" : File upload

    // Error packets
    public static final int     PKT_SERVER_ERROR            = 0xE0;    // "%2u"    : NAK/Error codes
    
    // End-Of-Transmission
    public static final int     PKT_SERVER_EOT              = 0xFF;    // ""       : End transmission (socket will be closed)

    // ------------------------------------------------------------------------
    // custom event payload templates
    
    private static PayloadTemplate ClientCustomEvent_30 = new PayloadTemplate(
        Packet.PKT_CLIENT_FIXED_FMT_STD,
        new PayloadTemplate.Field[] {
            new PayloadTemplate.Field(PayloadTemplate.FIELD_STATUS_CODE , false, 0,  2),
            new PayloadTemplate.Field(PayloadTemplate.FIELD_TIMESTAMP   , false, 0,  4),
            new PayloadTemplate.Field(PayloadTemplate.FIELD_GPS_POINT   , false, 0,  6),
            new PayloadTemplate.Field(PayloadTemplate.FIELD_SPEED       , false, 0,  1),
            new PayloadTemplate.Field(PayloadTemplate.FIELD_HEADING     , false, 0,  1),
            new PayloadTemplate.Field(PayloadTemplate.FIELD_ALTITUDE    , false, 0,  2),
            new PayloadTemplate.Field(PayloadTemplate.FIELD_ODOMETER    , false, 0,  3),
            new PayloadTemplate.Field(PayloadTemplate.FIELD_SEQUENCE    , false, 0,  1)
        }
    );

    private static PayloadTemplate ClientCustomEvent_31 = new PayloadTemplate(
        Packet.PKT_CLIENT_FIXED_FMT_HIGH,
        new PayloadTemplate.Field[] {
            new PayloadTemplate.Field(PayloadTemplate.FIELD_STATUS_CODE , true , 0,  2),
            new PayloadTemplate.Field(PayloadTemplate.FIELD_TIMESTAMP   , true , 0,  4),
            new PayloadTemplate.Field(PayloadTemplate.FIELD_GPS_POINT   , true , 0,  8),
            new PayloadTemplate.Field(PayloadTemplate.FIELD_SPEED       , true , 0,  2),
            new PayloadTemplate.Field(PayloadTemplate.FIELD_HEADING     , true , 0,  2),
            new PayloadTemplate.Field(PayloadTemplate.FIELD_ALTITUDE    , true , 0,  3),
            new PayloadTemplate.Field(PayloadTemplate.FIELD_ODOMETER    , true , 0,  3),
            new PayloadTemplate.Field(PayloadTemplate.FIELD_SEQUENCE    , true , 0,  1)
        }
    );

    // ------------------------------------------------------------------------
    // packet payload templates
    // overloaded types:
    //  FIELD_STATUS_CODE - numeric hex
    //  FIELD_INDEX       - numeric dec
    //  FIELD_BINARY      - binary
    //  FIELD_STRING      - string

    private static PayloadTemplate ClientTemplate_EndOfBlock_Done = new PayloadTemplate(
        Packet.PKT_CLIENT_EOB_DONE,
        new PayloadTemplate.Field[] {
            new PayloadTemplate.Field(PayloadTemplate.FIELD_STATUS_CODE , false, 0,  2), // checksum
        }
    );

    private static PayloadTemplate ClientTemplate_EndOfBlock_More = new PayloadTemplate(
        Packet.PKT_CLIENT_EOB_MORE,
        new PayloadTemplate.Field[] {
            new PayloadTemplate.Field(PayloadTemplate.FIELD_STATUS_CODE , false, 0,  2), // checksum
        }
    );

    private static PayloadTemplate ClientTemplate_Unique_ID = new PayloadTemplate(
        Packet.PKT_CLIENT_UNIQUE_ID,
        new PayloadTemplate.Field[] {
            new PayloadTemplate.Field(PayloadTemplate.FIELD_BINARY      , false, 0,  6), // unique-id
        }
    );

    private static PayloadTemplate ClientTemplate_Account_ID = new PayloadTemplate(
        Packet.PKT_CLIENT_ACCOUNT_ID,
        new PayloadTemplate.Field[] {
            new PayloadTemplate.Field(PayloadTemplate.FIELD_STRING      , false, 0, 20), // account-id
        }
    );

    private static PayloadTemplate ClientTemplate_Device_ID = new PayloadTemplate(
        Packet.PKT_CLIENT_DEVICE_ID,
        new PayloadTemplate.Field[] {
            new PayloadTemplate.Field(PayloadTemplate.FIELD_STRING      , false, 0, 20), // device-id
        }
    );

    private static PayloadTemplate ClientTemplate_PropertyValue = new PayloadTemplate(
        Packet.PKT_CLIENT_PROPERTY_VALUE,
        new PayloadTemplate.Field[] {
            new PayloadTemplate.Field(PayloadTemplate.FIELD_STATUS_CODE , false, 0,  2), // key
            new PayloadTemplate.Field(PayloadTemplate.FIELD_BINARY      , false, 0,253), // device-id
        }
    );

    private static PayloadTemplate ClientTemplate_CustomDef = new PayloadTemplate(
        Packet.PKT_CLIENT_FORMAT_DEF_24,
        new PayloadTemplate.Field[] {
            new PayloadTemplate.Field(PayloadTemplate.FIELD_STATUS_CODE , false, 0,  1), // key
            new PayloadTemplate.Field(PayloadTemplate.FIELD_INDEX       , false, 0,  1), // count
            new PayloadTemplate.Field(PayloadTemplate.FIELD_BINARY      , false, 0,  3), // field def
        },
        true
    );

    private static PayloadTemplate ClientTemplate_Diagnostic = new PayloadTemplate(
        Packet.PKT_CLIENT_DIAGNOSTIC,
        new PayloadTemplate.Field[] {
            new PayloadTemplate.Field(PayloadTemplate.FIELD_STATUS_CODE , false, 0,  2), // code
            new PayloadTemplate.Field(PayloadTemplate.FIELD_BINARY      , false, 0,253), // data
        }
    );

    private static PayloadTemplate ClientTemplate_Error = new PayloadTemplate(
        Packet.PKT_CLIENT_ERROR,
        new PayloadTemplate.Field[] {
            new PayloadTemplate.Field(PayloadTemplate.FIELD_STATUS_CODE , false, 0,  2), // code
            new PayloadTemplate.Field(PayloadTemplate.FIELD_BINARY      , false, 0,253), // data
        }
    );

    // ------------------------------------------------------------------------
    // Client Payload template table

    private static PayloadTemplate ClientEventPayloadTemplate_table[] = {
        ClientCustomEvent_30,
        ClientCustomEvent_31,
    };

    private static PayloadTemplate ClientStandardPayloadTemplate_table[] = {
        ClientTemplate_EndOfBlock_Done,
        ClientTemplate_EndOfBlock_More,
        ClientTemplate_Unique_ID,
        ClientTemplate_Account_ID,
        ClientTemplate_Device_ID,
        ClientTemplate_PropertyValue,
        ClientTemplate_CustomDef,
        ClientTemplate_Diagnostic,
        ClientTemplate_Error
    };

    /**
    * Returns a Client PayloadTemplate for the specified custom event packet type
    * @param type The type of PayloadTemplate to return
    * @return the payload template instance, or null if not found
    */
    public static PayloadTemplate GetClientPayloadTemplate(int type)
    {
        // These need to be in a hash table
        // first try events
        for (int i = 0; i < ClientEventPayloadTemplate_table.length; i++) {
            if (type == ClientEventPayloadTemplate_table[i].getPacketType()) {
                return ClientEventPayloadTemplate_table[i];
            }
        }
        // then try the others
        for (int i = 0; i < ClientStandardPayloadTemplate_table.length; i++) {
            if (type == ClientStandardPayloadTemplate_table[i].getPacketType()) {
                return ClientStandardPayloadTemplate_table[i];
            }
        }
        return null;
    }

    // ------------------------------------------------------------------------
    // Server Payload template table

    private static PayloadTemplate ServerTemplate_EndOfBlock_Done = new PayloadTemplate(
        Packet.PKT_SERVER_EOB_DONE,
        new PayloadTemplate.Field[0]
    );

    private static PayloadTemplate ServerTemplate_EndOfBlock_SpeakFreely = new PayloadTemplate(
        Packet.PKT_SERVER_EOB_SPEAK_FREELY,
        new PayloadTemplate.Field[0]
    );

    private static PayloadTemplate ServerTemplate_Ack = new PayloadTemplate(
        Packet.PKT_SERVER_ACK,
        new PayloadTemplate.Field[] {
            new PayloadTemplate.Field(PayloadTemplate.FIELD_STATUS_CODE , false, 0,  4), // sequence
        }
    );

    private static PayloadTemplate ServerTemplate_GetProperty = new PayloadTemplate(
        Packet.PKT_SERVER_GET_PROPERTY,
        new PayloadTemplate.Field[] {
            new PayloadTemplate.Field(PayloadTemplate.FIELD_STATUS_CODE , false, 0,  4), // property
        }
    );

    private static PayloadTemplate ServerTemplate_SetProperty = new PayloadTemplate(
        Packet.PKT_SERVER_SET_PROPERTY,
        new PayloadTemplate.Field[] {
            new PayloadTemplate.Field(PayloadTemplate.FIELD_STATUS_CODE , false, 0,  2), // property
            new PayloadTemplate.Field(PayloadTemplate.FIELD_BINARY      , false, 0,251), // value
        }
    );

    private static PayloadTemplate ServerTemplate_Error = new PayloadTemplate(
        Packet.PKT_SERVER_ERROR,
        new PayloadTemplate.Field[] {
            new PayloadTemplate.Field(PayloadTemplate.FIELD_STATUS_CODE , false, 0,  2), // error
            new PayloadTemplate.Field(PayloadTemplate.FIELD_STATUS_CODE , false, 0,  1), // header
            new PayloadTemplate.Field(PayloadTemplate.FIELD_STATUS_CODE , false, 0,  1), // type
            new PayloadTemplate.Field(PayloadTemplate.FIELD_BINARY      , false, 0,251), // extra
        }
    );

    private static PayloadTemplate ServerTemplate_EndOfTransmission = new PayloadTemplate(
        Packet.PKT_SERVER_EOT,
        new PayloadTemplate.Field[0]
    );

    private static PayloadTemplate ServerStandardPayloadTemplate_table[] = {
        ServerTemplate_EndOfBlock_Done,
        ServerTemplate_Ack,
        ServerTemplate_GetProperty,
        ServerTemplate_SetProperty,
        ServerTemplate_Error,
        ServerTemplate_EndOfTransmission
    };

    /**
    * Returns a Server PayloadTemplate for the specified event packet type
    * @param type The type of PayloadTemplate to return
    * @return the payload template instance, or null if not found
    */
    public static PayloadTemplate GetServerPayloadTemplate(int type)
    {
        // These need to be in a hash table
        for (int i = 0; i < ServerStandardPayloadTemplate_table.length; i++) {
            if (type == ServerStandardPayloadTemplate_table[i].getPacketType()) {
                return ServerStandardPayloadTemplate_table[i];
            }
        }
        return null;
    }

    // ------------------------------------------------------------------------

    /**
    * Creates a packet with no payload.
    * @param type int denoting the packet type.
    * @return Created packet is returned.
    */
    public static Packet createClientPacket(int type)
    {
        return new Packet(true, HEADER_BASIC, type);
    }

    /**
    * Creates a packet with a payload of an array of bytes.
    * @param type int denoting the packet type.
    * @param payload Array of bytes.
    * @return Created packet is returned.
    */
    public static Packet createClientPacket(int type, byte payload[])
    {
        return new Packet(true, HEADER_BASIC, type, payload);
    }

    /**
    * Creates a packet with a payload of a String.
    * @param type int denoting the packet type.
    * @param payload String payload.
    * @return Created packet is returned.
    */
    public static Packet createClientPacket(int type, String payload)
    {
        return new Packet(true, HEADER_BASIC, type, payload);
    }

    /**
    * Creates a packet with a payload of type Payload.
    * @param type int denoting the packet type.
    * @param payload object Payload.
    * @return Created packet is returned.
    */
    public static Packet createClientPacket(int type, Payload payload)
    {
        return new Packet(true, HEADER_BASIC, type, payload);
    }

    /**
    * Creates an error encoded packet.
    * @param errCode int error code.
    * @param causePkt Packet denoting the cause of the error.
    * @return Returns the error encoded packet.
    */
    public static Packet createClientErrorPacket(int errCode, Packet causePkt)
    {
        Payload payload = new Payload();
        payload.writeULong((long)errCode, 2);
        // caller may stil want to write additional arguments to the specific error payload
        return Packet.createClientPacket(PKT_CLIENT_ERROR, payload);
    }

    /**
    * Constructs the actual contents of the packet according to predefined payload templates. Uses a
    * case statement switching between the packet type field to populate the various template data
    * fields in the payload.
    * @param event GeoEvent contains all GPS data.
    * @param template PayloadTemplate formats the contents of the packet.
    * @return Created packet is returned.
    */
    public static Packet createClientEventPacket(GeoEvent event, PayloadTemplate template)
    {
        
        /* default default template */
        if (template == null) {
            template = ClientCustomEvent_30; 
        }
        
        /* create packet */
        Packet pkt = Packet.createClientPacket(template.getPacketType());

        /* cache sequence number */
        long sequence = SEQUENCE_ALL;

        /* populate payload */
        Payload payload = pkt.getPayload(false);
        for (int i = 0;; i++) {
            PayloadTemplate.Field field = template.getField(i);
            if (field == null) { break; }
            int type      = field.getType();
            boolean hiRes = field.isHiRes();
            int ndx       = field.getIndex();
            int length    = field.getLength();
            switch (type) {
                case PayloadTemplate.FIELD_STATUS_CODE  : // %2u
                    payload.writeULong(event.getStatusCode(), length);
                    break;
                case PayloadTemplate.FIELD_TIMESTAMP    : // %4u
                    payload.writeULong(event.getTimestamp(), length);
                    break;
                case PayloadTemplate.FIELD_INDEX        : // %4u 0 to 4294967295
                    payload.writeULong(event.getIndex(), length);
                    break;
                case PayloadTemplate.FIELD_GPS_POINT    : // %6g                          %8g
                    payload.writeGPS(event.getGeoPoint(), length);
                    break;
                case PayloadTemplate.FIELD_SPEED        : // %1u 0 to 255 kph             %2u 0.0 to 6553.5 kph
                    if (hiRes) {
                        long sp = (long)((event.getSpeedKPH() * 10.0) + 0.5);
                        payload.writeULong(sp, length);
                    } else {
                        long sp = (long)(event.getSpeedKPH() + 0.5);
                        if ((length == 1) && (sp > 255)) { sp = 255; }
                        payload.writeULong(sp, length);
                    }
                    break;
                case PayloadTemplate.FIELD_HEADING      : // %1u 1.412 deg un.            %2u 0.00 to 360.00 deg
                    if (hiRes) {
                        long hd = (long)((event.getHeading() * 100.0) + 0.5);
                        payload.writeULong(hd, length);
                    } else {
                        long hd = (long)((event.getHeading() * 255.0/360.0) + 0.5);
                        payload.writeULong(hd, length);
                    }
                    break;
                case PayloadTemplate.FIELD_ALTITUDE     : // %2i -32767 to +32767 m       %3i -838860.7 to +838860.7 m
                    if (hiRes) {
                        long alt = (long)((event.getAltitude() * 10.0) + 0.5);
                        payload.writeULong(alt, length);
                    } else {
                        long alt = (long)(event.getAltitude() + 0.5);
                        payload.writeULong(alt, length);
                    }
                    break;
                case PayloadTemplate.FIELD_DISTANCE     : // %3u 0 to 16777216 km         %3u 0.0 to 1677721.6 km
                    if (hiRes) {
                        long ds = (long)((event.getDistanceKM() * 10.0) + 0.5);
                        payload.writeULong(ds, length);
                    } else {
                        long ds = (long)(event.getDistanceKM() + 0.5);
                        payload.writeULong(ds, length);
                    }
                    break;
                case PayloadTemplate.FIELD_ODOMETER     : // %3u 0 to 16777216 km         %3u 0.0 to 1677721.6 km
                    if (hiRes) {
                        long ds = (long)((event.getOdometerKM() * 10.0) + 0.5);
                        payload.writeULong(ds, length);
                    } else {
                        long ds = (long)(event.getOdometerKM() + 0.5);
                        payload.writeULong(ds, length);
                    }
                    break;
                case PayloadTemplate.FIELD_SEQUENCE     : // %1u 0 to 255
                    sequence = (Packet.eventSequence++) & ((1L << (length * 8)) - 1L);
                    payload.writeULong(sequence, length);
                    break;
                // other fields may be needed for other PayloadTemplates
            }
        }
        
        /* set packet sequence */
        // will be 'SEQUENCE_ALL', if not specified as a field
        pkt.setEventSequence(sequence);

        return pkt;
    }

    // ------------------------------------------------------------------------

    /**
    * Calculates the packet's checksum utilizing the bitwise XOR (^) to shift the bits according to
    * the array of bytes passed.
    * @param b Array of bytes.
    * @return Returns the checksum as an int.
    */
    private static int CalcChecksum(byte b[])
    {
        if (b == null) {
            return -1;
        } else {
            int cksum = 0, s = 0;
            if ((b.length > 0) && (b[0] == Encoding.AsciiEncodingChar)) { s++; }
            for (; s < b.length; s++) {
                if (b[s] == Encoding.AsciiChecksumChar ) { break; }
                if (b[s] == Encoding.AsciiEndOfLineChar) { break; }
                cksum = (cksum ^ b[s]) & 0xFF;
            }
            return cksum;
        }
    }
    
    // ------------------------------------------------------------------------
    
    private static long eventSequence = 0L;
    
    private int      encoding           = Encoding.ENCODING_BINARY;
    private boolean  hasAsciiChecksum   = false;
    private boolean  isClient           = true;
    private int      header             = 0;
    private int      type               = 0;
    private Payload  payload            = null;
    private boolean  isSent             = false;
    private int      priority           = PRIORITY_NORMAL;
    private long     sequence           = 0L;
    
    /**
    * Empty Packet constructor for creating either client or server packets
    * @param isClient True if the packet originates from the client.
    * @param header header of the packet.
    */
    public Packet(boolean isClient, int header)
    {
        this(isClient, header, 0x00, new Payload());
        // it is assumed that the caller will fill in the type later!
    }

    /**
    * Typed Packet constructor consisting of the empty packet adding an int type.
    * @param isClient True if the packet originates from the client.
    * @param header header of the packet.
    * @param type determines the template used to format the payload.
    */
    public Packet(boolean isClient, int header, int type)
    {
        this(isClient, header, type, new Payload());
    }

    /**
    * Packet constructor consisting of the typed Packet as well as a byte array payload.
    * @param isClient True if the packet originates from the client.
    * @param header header of the packet.
    * @param type determines the template used to format the payload.
    * @param payload Array of bytes.
    */
    public Packet(boolean isClient, int header, int type, byte payload[])
    {
        this(isClient, header, type, new Payload(payload));
    }

    /**
    * Paccket constructor consisting of the typed Packet as well as a String payload.
    * @param isClient True if the packet originates from the client.
    * @param header header of the packet.
    * @param type determines the template used to format the payload.
    * @param payload String payload.
    */
    public Packet(boolean isClient, int header, int type, String payload)
    {
        this(isClient, header, type, new Payload(payload));
    }

    /**
    * Packet constructor consisting of the typed Packet as well as an instance of the Payload object
    * as the Packet payload.
    * @param isClient True if the packet originates from the client.
    * @param header header of the packet.
    * @param type determines the template used to format the payload.
    * @param payload instantiated payload object.
    */
    public Packet(boolean isClient, int header, int type, Payload payload)
    {
        this.isClient = isClient;
        this.header   = header;
        this.type     = type;
        this.payload  = payload;
    }

    /**
    * Packet constructor consisting of the typed Packet as well as a String containing the payload
    * @param isClient True if the packet originates from the client.
    * @param pkt String payload
    * @throws PacketParseException if parsing error occurs
    */
    public Packet(boolean isClient, String pkt) 
        throws PacketParseException 
    {
        this(isClient, StringTools.getBytes(pkt));
    }

    /**
    * Packet constructor from a raw byte array representing a single packet
    * @param isClient True if the packet originates from the client.
    * @param pkt the incoming packet byte array
    * @throws PacketParseException if parsing error occurs
    */
    public Packet(boolean isClient, byte pkt[]) 
        throws PacketParseException 
    {
        // 'pkt' always contains only a single packet
        this.isClient = isClient;
        this.encoding = Encoding.ENCODING_UNKNOWN;
        if (pkt.length < 3) {
            
            this.header = (pkt.length > 0)? ((int)pkt[0] & 0xFF) : 0x00;
            this.type   = (pkt.length > 1)? ((int)pkt[1] & 0xFF) : 0x00;
            throw new PacketParseException(ServerErrors.NAK_PACKET_LENGTH, this); // errData ok
            
        } else
        if (pkt[0] == Encoding.AsciiEncodingChar) {
            
            /* checksum */
            int pLen = 1; // start with first character after AsciiEndOfLineChar
            int cksumActual = 0, cksumTest = -1;
            this.hasAsciiChecksum = false;
            for (;(pLen < pkt.length) && (pkt[pLen] != Encoding.AsciiEndOfLineChar); pLen++) {
                if (pkt[pLen] == Encoding.AsciiChecksumChar) {
                    this.hasAsciiChecksum = true;
                    String hexCksum = StringTools.toStringValue(pkt, pLen + 1, 2);
                    cksumTest = StringTools.parseHexInt(hexCksum, -1);
                    break;
                }
                cksumActual = (cksumActual ^ pkt[pLen]) & 0xFF;
            }
            // 'pLen' now represents length of actual packet string.
            
            /* string packet */
            String p = StringTools.toStringValue(pkt, 0, pLen);
            
            /* header */
            this.header = (pLen >= 3)? StringTools.parseHexInt(p.substring(1,3), 0x00) : 0x00;
            this.type   = (pLen >= 5)? StringTools.parseHexInt(p.substring(3,5), 0x00) : 0x00;
            if (this.header != HEADER_BASIC) {
                throw new PacketParseException(ServerErrors.NAK_PACKET_HEADER, this); // errData ok
            }

            /* minimum length */
            if (pLen < 5) { // eg. "$E0D1"
                throw new PacketParseException(ServerErrors.NAK_PACKET_LENGTH, this); // errData ok
            }
            
            /* check checksum */
            // wait until header/type are parsed before testing checksum
            if (cksumTest < 0) {
                // record does not contain a checksum
            } else
            if (cksumTest != cksumActual) {
                // If the checksum fails, then we really can't trust any information contained in the 
                // packet, thus we are unable to accurately let the client know specifically which packet
                // had the problem.
                throw new PacketParseException(ServerErrors.NAK_PACKET_CHECKSUM, this); // errData ok
            } else {
                // Checksum is ok
            }

            /* payload encoding */
            int ench = (p.length() >= 6)? p.charAt(5) : -1;
            if ((ench == Encoding.AsciiEndOfLineChar) || (ench < 0)) {
                // encoding not known, assign default
                this.encoding = this.hasAsciiChecksum? Encoding.ENCODING_BASE64_CKSUM : Encoding.ENCODING_BASE64;
                this.payload  = new Payload(new byte[0]);
            } else
            if (ench == Encoding.ENCODING_HEX_CHAR) {
                // Hex
                this.encoding = this.hasAsciiChecksum? Encoding.ENCODING_HEX_CKSUM : Encoding.ENCODING_HEX;
                this.payload  = new Payload(StringTools.parseHex(p.substring(6), new byte[0]));
            } else
            if (ench == Encoding.ENCODING_BASE64_CHAR) {
                // Base64
                this.encoding = this.hasAsciiChecksum? Encoding.ENCODING_BASE64_CKSUM : Encoding.ENCODING_BASE64;
                this.payload  = new Payload(Base64.decode(p.substring(6)));
            } else
            if (ench == Encoding.ENCODING_CSV_CHAR) {
                // CSV
                // unsupported encoding
                throw new PacketParseException(ServerErrors.NAK_PACKET_ENCODING, this); // errData ok
            } else {
                // unrecognized encoding
                throw new PacketParseException(ServerErrors.NAK_PACKET_ENCODING, this); // errData ok
            }
            
        } else
        if (pkt[0] == (byte)HEADER_BASIC) {
            
            /* binary header */
            this.encoding = Encoding.ENCODING_BINARY;
            this.header   = (int)pkt[0] & 0xFF;
            this.type     = (int)pkt[1] & 0xFF;
            
            /* check payload length */
            int len = (int)pkt[2] & 0xFF;
            if (len != pkt.length - 3) {
                throw new PacketParseException(ServerErrors.NAK_PACKET_LENGTH, this); // errData ok
            }
            
            /* payload */
            this.payload  = new Payload(pkt, 3, len);
            
        } else {
            
            this.encoding = Encoding.ENCODING_UNKNOWN;
            this.header   = (int)pkt[0] & 0xFF;
            this.type     = (int)pkt[1] & 0xFF;
            throw new PacketParseException(ServerErrors.NAK_PACKET_HEADER, this); // errData ok

        }

    }

    // ------------------------------------------------------------------------

    /**
    * Sets the packet's sent field.
    * @param sent boolean value determining if the packet has been sent.
    */
    public void setSent(boolean sent)
    {
        this.isSent = sent;
    }

    /**
    * Gets the packet's sent field.
    * @return The packet's boolean sent field.
    */
    public boolean isSent()
    {
        return this.isSent;
    }

    // ------------------------------------------------------------------------

    /**
    * Sets the packet's priority level.
    * @param pri priority level.
    */
    public void setPriority(int pri)
    {
        this.priority = pri;
    }

    /**
    * Gets the packet's priority level.
    * @return Returns the packet's priority.
    */
    public int getPriority()
    {
        return this.priority;
    }

    // ------------------------------------------------------------------------

    /**
    * Sets the packet's event sequence.
    * @param seq long representing the packet's sequence.
    */
    public void setEventSequence(long seq)
    {
        this.sequence = seq;
    }

    /**
    * Gets the packet's event sequence.
    * @return Returns the packet's event sequence.
    */
    public long getEventSequence()
    {
        return this.sequence;
    }

    // ------------------------------------------------------------------------

    /**
    * Sets the packet's encoding format.
    * @param encoding encoding format.
    */
    public void setEncoding(int encoding)
    {
        this.encoding = encoding;
    }
    
    /**
    * Returns the packet's encoding format.
    * @return the packet's encoding format.
    */
    public int getEncoding()
    {
        return this.encoding;
    }
    
    // ------------------------------------------------------------------------

    /**
    * Returns the packet's header field.
    * @return the packet's header field as an int.
    */
    public int getPacketHeader()
    {
        return this.header;
    }
   
    // ------------------------------------------------------------------------

    /**
    * Sets the packet's type field.
    * @param type Packet's type.
    */
    public void setPacketType(int type)
    {
        this.type = type;
    }

    /**
    * Returns the Packet's type field.
    * @return the packet's type field
    */
    public int getPacketType()
    {
        return this.type;
    }
    
    /**
    * Returns a boolean indicating if the packet is an identifier type.
    * @return a boolean indicating if the packet is an identifier type.
    */
    public boolean isIdentType()
    {
        int t = this.getPacketType();
        return (t == PKT_CLIENT_UNIQUE_ID ) || 
               (t == PKT_CLIENT_ACCOUNT_ID) ||
               (t == PKT_CLIENT_DEVICE_ID );
    }

    /**
    * Returns a boolean indicating if the packet is an event type.
    * @return a boolean indicating if the packet is an event type.
    */
    public boolean isEventType()
    {
        return isEventType(this.getPacketType());
    }
    
    /**
    * Returns true if the specified type represents an event packet
    * @param t The packet type to test
    * @return a boolean indicating if the specified type is an event type.
    */
    public static boolean isEventType(int t)
    {
        if (isFixedEventType(t) || isCustomEventType(t)) {
            return true;
        } else
        if ((t >= PKT_CLIENT_DMTSP_FMT_0) && (t <= PKT_CLIENT_DMTSP_FMT_F)) {
            return true;
        } else {
            return false;
        }
    }
    
    /**
    * Returns true if the specified type represents a fixed event packet type
    * @param t The packet type to test
    * @return a boolean indicating if the packet is a fixed event packet type
    */
    public static boolean isFixedEventType(int t)
    {
        if ((t >= PKT_CLIENT_FIXED_FMT_STD) && (t <= PKT_CLIENT_FIXED_FMT_F)) {
            return true;
        } else {
            return false;
        }
    }
    
    /**
    * Returns true if the specified type represents a custom event packet type
    * @param t The packet type to test
    * @return a boolean indicating if the packet is a custom event packet type
    */
    public static boolean isCustomEventType(int t)
    {
        if ((t >= PKT_CLIENT_CUSTOM_FORMAT_0) && (t <= PKT_CLIENT_CUSTOM_FORMAT_F)) {
            return true;
        } else {
            return false;
        }
    }

    // ------------------------------------------------------------------------

    /**
    * Returns true if the packet contains an ASCII checksum.
    * @return a boolean true if packet contains an ASCII checksum
    */
    public boolean hasAsciiChecksum()
    {
        return this.hasAsciiChecksum;
    }
    
    // ------------------------------------------------------------------------

    /**
    * Returns the packet's length.
    * @return The packet length.
    */
    public int getPacketLength()
    {
        return MIN_HEADER_LENGTH + this.getPayloadLength();
    }

    /**
    * Returns the packet's payload length.
    * @return The payload length.
    */
    public int getPayloadLength()
    {
        return this.payload.getSize();
    }
    
    /**
    * Returns a boolean indicating if the packet has a payload.
    * @return true, if the packet has a payload.
    */
    public boolean hasPayload()
    {
        return (this.getPayloadLength() > 0);
    }

    /**
    * Returns the packets payload
    * @return a Payload object representing the packet's payload.
    */
    public Payload getPayload()
    {
        return this.getPayload(false);
    }
    
    /**
    * Returns the packet's payload.
    * @param reset true to clear/reset the data in the packet payload
    * @return Payload object representing the packet's payload.
    */
    public Payload getPayload(boolean reset)
    {
        if (reset) {
            // make Payload a data source
            // or reset to 0 for writing at the beginning
            this.payload.resetIndex();
        }
        return this.payload;
    }
    
    /**
    * Returns the PayloadTemplate used to create the packet payload
    * @return The payload template.
    */
    public PayloadTemplate getPayloadTemplate()
    {
        if (this.isClient) {
            PayloadTemplate plt = GetClientPayloadTemplate(this.type);
            return plt;
        } else {
            return GetServerPayloadTemplate(this.type);
        }
    }
    
    // ------------------------------------------------------------------------
    
    /**
    * Returns a byte array of the specified payload offset/length
    * @param ofs offset.
    * @param len length.
    * @return array of bytes representing the specified section of the payload
    */
    public byte[] getPayloadBytes(int ofs, int len)
    {
        byte b[] = this.getPayload(true).getBytes();
        if (ofs >= b.length) {
            return new byte[0];
        } else {
            if (len > (b.length - ofs)) { len = b.length - ofs; }
            byte n[] = new byte[len];
            System.arraycopy(b, ofs, n, 0, len);
            return n;
        }
    }
    
    // ------------------------------------------------------------------------

    /**
    * Returns the packet as a byte array using the default encoding
    * @return array of bytes representing the encoded packet
    */
    public byte[] encode()
    {
        return this.encode(this.getEncoding());
    }
    
    /**
    * Returns the packet as a byte array using the specified encoding
    * @param encoding specified packet encoding
    * @return the packet as an array of bytes
    */
    public byte[] encode(int encoding)
    {
        byte payload[] = this.getPayload(true).getBytes();
        if (encoding == Encoding.ENCODING_BINARY) {
            int len = payload.length;
            byte pkt[] = new byte[MIN_HEADER_LENGTH + len];
            pkt[0] = (byte)(this.header & 0xFF);
            pkt[1] = (byte)(this.type & 0xFF);
            pkt[2] = (byte)(len & 0xFF);
            System.arraycopy(payload, 0, pkt, 3, len);
            return pkt;
        } else {
            StringBuffer sb = new StringBuffer();
            sb.append(Encoding.AsciiEncodingChar);
            sb.append(StringTools.toHexString((long)this.header & 0xFF, 8));
            sb.append(StringTools.toHexString((long)this.type   & 0xFF, 8));
            if (payload.length > 0) {
                switch (encoding) {
                    case Encoding.ENCODING_CSV_CKSUM   :
                    case Encoding.ENCODING_CSV         : {
                        sb.append("?unsupported_encoding?");
                        break;
                    }
                    case Encoding.ENCODING_BASE64_CKSUM: 
                    case Encoding.ENCODING_BASE64      : {
                        sb.append(Encoding.ENCODING_BASE64_CHAR); 
                        sb.append(Base64.encode(payload));
                        break;
                    }
                    case Encoding.ENCODING_HEX_CKSUM   : 
                    case Encoding.ENCODING_HEX         : {
                        sb.append(Encoding.ENCODING_HEX_CHAR); 
                        StringTools.toHexString(payload, sb);
                        break;
                    }
                    case Encoding.ENCODING_UNKNOWN     :
                    default                   : {
                        sb.append("?unknown_encoding?");
                        break;
                    }
                }
            } else {
                //sb.append(" <No Payload>");
            }
            
            /* add ASCII checksum */
            if (Encoding.IsEncodingChecksum(encoding)) {
                int cksum = CalcChecksum(StringTools.getBytes(sb.toString()));
                if (cksum >= 0) {
                    sb.append(Encoding.AsciiChecksumChar);
                    sb.append(StringTools.toHexString((long)cksum & 0xFF, 8));
                }
            }
            
            /* end of line */
            sb.append(Encoding.AsciiEndOfLineChar);
            return StringTools.getBytes(sb.toString());
            
        }
    }

    // ------------------------------------------------------------------------
    
    /**
    * Returns a String reprentation of the packet using the specified encoding
    * @param encoding specified packet encoding
    * @return String representation of this packet
    */
    public String toString(int encoding)
    {
        byte b[] = this.encode(encoding);
        if ((b != null) && (b.length > 0)) {
            if (b[0] == Encoding.AsciiEncodingChar) {
                int len = (b[b.length - 1] == Encoding.AsciiEndOfLineChar)? (b.length - 1) : b.length;
                return StringTools.toStringValue(b, 0, len);
            } else {
                return "0x" + StringTools.toHexString(b);
            }
        } else {
            return "";
        }
    }
    
    /**
    * Return a String representation of this packet using the default encoding.
    * @return Returns the String representation of this packet
    */
    public String toString()
    {
        return this.toString(this.getEncoding());
    }

    // ------------------------------------------------------------------------

}
