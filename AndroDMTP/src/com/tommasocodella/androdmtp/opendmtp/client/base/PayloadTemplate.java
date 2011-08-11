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
//  This class maintains the structure of 'custom event packets'.  The client
//  can define it's own custom event packet structure using this class, and 
//  send this template to the server so that the server can understand how to
//  parse these custome event packets.
// ----------------------------------------------------------------------------
// Change History:
//  2006/03/26  Martin D. Flynn
//      Initial release
//  2006/11/03  Kiet Huynh
//      Include JavaDocs
// ----------------------------------------------------------------------------
package com.tommasocodella.androdmtp.opendmtp.client.base;

import com.tommasocodella.androdmtp.opendmtp.util.GeoPoint;
import com.tommasocodella.androdmtp.opendmtp.util.Payload;
import com.tommasocodella.androdmtp.opendmtp.util.StringTools;

/**
* A template for encoding and decoding a packet. A template includes the type of the packet and all the
* fields associated with the packet.
*/
public class PayloadTemplate
{

    // ------------------------------------------------------------------------

    public  static final int PRIMITIVE_MASK             = 0x00F0;
    public  static final int PRIMITIVE_LONG             = 0x0010;
    public  static final int PRIMITIVE_GPS              = 0x0030;
    public  static final int PRIMITIVE_STRING           = 0x0040;
    public  static final int PRIMITIVE_BINARY           = 0x0050;

    // ------------------------------------------------------------------------
 
    public  static final int FIELD_STATUS_CODE          = 0x01;
    public  static final int FIELD_TIMESTAMP            = 0x02;
    public  static final int FIELD_INDEX                = 0x03;
    
    public  static final int FIELD_SEQUENCE             = 0x04; // %1u 0 to 255                 %2u 0 to 65535

    public  static final int FIELD_GPS_POINT            = 0x06; // %6g                          %8g
    public  static final int FIELD_GPS_AGE              = 0x07; // %2u 0 to 65535 sec
    public  static final int FIELD_SPEED                = 0x08; // %1u 0 to 255 kph             %2u 0.0 to 655.3 kph
    public  static final int FIELD_HEADING              = 0x09; // %1u 1.412 deg un.            %2u 0.00 to 360.00 deg
    public  static final int FIELD_ALTITUDE             = 0x0A; // %2i -32767 to +32767 m       %3i -838860.7 to +838860.7 m
    public  static final int FIELD_DISTANCE             = 0x0B; // %3u 0 to 16777216 km         %4u 0.0 to 429496729.5 km
    public  static final int FIELD_ODOMETER             = 0x0C; // %3u 0 to 16777216 km         %4u 0.0 to 429496729.5 km

 // Misc fields                                                 // Low                          High
    public  static final int FIELD_GEOFENCE_ID          = 0x0E; // %4u 0x00000000 to 0xFFFFFFFF
    public  static final int FIELD_TOP_SPEED            = 0x0F; // %1u 0 to 255 kph             %2u 0.0 to 655.3 kph

    public  static final int FIELD_STRING               = 0x11; // %*s may contain only 'A'..'Z', 'a'..'z, '0'..'9', '-', '.'
    public  static final int FIELD_STRING_PAD           = 0x12; // %*s may contain only 'A'..'Z', 'a'..'z, '0'..'9', '-', '.'

    public  static final int FIELD_ENTITY               = 0x15; // %*s may contain only 'A'..'Z', 'a'..'z, '0'..'9', '-', '.'
    public  static final int FIELD_ENTITY_PAD           = 0x16; // %*s may contain only 'A'..'Z', 'a'..'z, '0'..'9', '-', '.'

    public  static final int FIELD_BINARY               = 0x1A; // %*b  

 // I/O fields                                                  // Low                          High
    public  static final int FIELD_INPUT_ID             = 0x21; // %4u 0x00000000 to 0xFFFFFFFF
    public  static final int FIELD_INPUT_STATE          = 0x22; // %4u 0x00000000 to 0xFFFFFFFF
    public  static final int FIELD_OUTPUT_ID            = 0x24; // %4u 0x00000000 to 0xFFFFFFFF
    public  static final int FIELD_OUTPUT_STATE         = 0x25; // %4u 0x00000000 to 0xFFFFFFFF
    public  static final int FIELD_ELAPSED_TIME         = 0x27; // %3u 0 to 16777216 sec        %4u 0.000 to 4294967.295 sec
    public  static final int FIELD_COUNTER              = 0x28; // %4u 0 to 4294967295
    
    public  static final int FIELD_SENSOR32_LOW         = 0x31; // %4u 0x00000000 to 0xFFFFFFFF
    public  static final int FIELD_SENSOR32_HIGH        = 0x32; // %4u 0x00000000 to 0xFFFFFFFF
    public  static final int FIELD_SENSOR32_AVER        = 0x33; // %4u 0x00000000 to 0xFFFFFFFF

    public  static final int FIELD_TEMP_LOW             = 0x3A; // %1i -127 to +127 C           %2i -3276.7 to +3276.7 C
    public  static final int FIELD_TEMP_HIGH            = 0x3B; // %1i -127 to +127 C           %2i -3276.7 to +3276.7 C
    public  static final int FIELD_TEMP_AVER            = 0x3C; // %1i -127 to +127 C           %2i -3276.7 to +3276.7 C

 // GPS quality fields                                          // Low                          High
    public  static final int FIELD_GPS_DGPS_UPDATE      = 0x41; // %2u 0 to 65535 sec
    public  static final int FIELD_GPS_HORZ_ACCURACY    = 0x42; // %1u 0 to 255 m               %2u 0.0 to 6553.5 m
    public  static final int FIELD_GPS_VERT_ACCURACY    = 0x43; // %1u 0 to 255 m               %2u 0.0 to 6553.5 m
    public  static final int FIELD_GPS_SATELLITES       = 0x44; // %1u 0 to 12
    public  static final int FIELD_GPS_MAG_VARIATION    = 0x45; // %2i -180.00 to 180.00 deg
    public  static final int FIELD_GPS_QUALITY          = 0x46; // %1u (0=None, 1=GPS, 2=DGPS, ...)
    public  static final int FIELD_GPS_TYPE             = 0x47; // %1u (1=None, 2=2D, 3=3D, ...)
    public  static final int FIELD_GPS_GEOID_HEIGHT     = 0x48; // %1i -128 to +127 m           %2i -3276.7 to +3276.7 m
    public  static final int FIELD_GPS_PDOP             = 0x49; // %1u 0.0 to 25.5              %2u 0.0 to 99.9
    public  static final int FIELD_GPS_HDOP             = 0x4A; // %1u 0.0 to 25.5              %2u 0.0 to 99.9
    public  static final int FIELD_GPS_VDOP             = 0x4B; // %1u 0.0 to 25.5              %2u 0.0 to 99.9

 // OBC/J1708 fields
    public  static final int FIELD_OBC_FAULT_CODE       = 0x50; // %2u
    public  static final int FIELD_OBC_INT_VALUE        = 0x51; // %8u
    public  static final int FIELD_OBC_BIN_VALUE        = 0x52; // %*b (at least 4 bytes)
    public  static final int FIELD_OBC_DISTANCE         = 0x54; // %3u 0 to 16777216 km         %4u 0.0 to 429496729.5 km
    public  static final int FIELD_OBC_ENGINE_HOURS     = 0x56; // %3u 0 to 1677721.6 hours
    public  static final int FIELD_OBC_ENGINE_RPM       = 0x57; // %2u 0 to 65535 rpm
    public  static final int FIELD_OBC_ENGINE_TEMP      = 0x58; // %2i -32767 to +32767 C
    public  static final int FIELD_OBC_OIL_LEVEL        = 0x59; // %1u 0% to 100% percent
    public  static final int FIELD_OBC_FUEL_LEVEL       = 0x5B; // %1u 0% to 100% percent
    public  static final int FIELD_OBC_FUEL_ECONOMY     = 0x5C; // %2u 0.0 to 6553.5 kpg

    // ------------------------------------------------------------------------

    private int     customType  = -1; // undefined
    private Field   fields[]    = null;
    private boolean repeatLast  = false;
    
    /**
    * PayloadTemplate constructor
    * @param type The custom type
    * @param flds An array of Field
    */
    public PayloadTemplate(int type, Field flds[])
    {
        this.customType = type;
        this.fields     = flds;
        this.repeatLast = false;
    }
        
    /**
    * PayloadTemplate constructor
    * @param type The custom type.
    * @param flds An array of Field.
    * @param repeatLast A boolean value indicating whether the last field should be
    *         used as the default field for invalid indexes.
    */
    public PayloadTemplate(int type, Field flds[], boolean repeatLast)
    {
        this.customType = type;
        this.fields     = flds;
        this.repeatLast = repeatLast;
    }

    // ------------------------------------------------------------------------

    /**
    * Returns the type of the packet.
    * @return The packet type
    */
    public int getPacketType()
    {
        return this.customType;
    }
    
    /**
    * Returns the Field at the specified index.
    * @param ndx The index of the desired field.
    * @return The Field at the specified index.
    */
    public Field getField(int ndx)
    {
        if ((ndx >= 0) && (this.fields != null) && (this.fields.length > 0)) {
            if (ndx < this.fields.length) {
                return this.fields[ndx];
            } else
            if (this.repeatLast) {
                return this.fields[this.fields.length - 1];
            }
        }
        return null;
    }
    
    /**
    * Returns an array of Fields for this PayloadTemplate.
    * @return A array of Fields for this PayloadTemplate
    */
    public Field[] getFields()
    {
        if (this.fields == null) { this.fields = new Field[0]; }
        return this.fields;
    }
    
    /**
    * Returns the 'repeatLast' value for this PayloadTemplate.
    * @return The 'repeatLast' boolean value
    */
    public boolean getRepeatLast()
    {
        return this.repeatLast;
    }
    
    // ------------------------------------------------------------------------

    /**
    * Returns a Payload containing the PKT_CLIENT_FORMAT_DEF_24 template packet type.
    * @return The template Payload.
    */
    public Payload getPayload()
    {
        Payload p = new Payload();
        Field fld[] = this.getFields();
        
        /* start with type and field count */
        p.writeULong(this.getPacketType(), 1);
        p.writeULong(fld.length, 1);
        
        /* write fields */
        for (int i = 0; i < fld.length; i++) {
            p.writeULong(fld[i].getMask(), 3);
        }
        
        /* return payload */
        return p;
        
    }
    
    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

    private static final char FIELD_VALUE_SEPARATOR = '|';

    /**
    * Defines a single field of a packet.
    */
    public static class Field
    {
        private boolean hiRes   = false;
        private int     fldType = -1;
        private int     fldNdx  = 0;
        private int     fldLen  = 0;
        
        /**
        * Field constructor.
        * @param type The type of the field.
        * @param hiRes The hiRes value.
        * @param index The index of the field.
        * @param length The length of the field.
        */
        public Field(int type, boolean hiRes, int index, int length) {
            this.fldType = type;
            this.hiRes   = hiRes;
            this.fldNdx  = index;
            this.fldLen  = length;
        }
        
        /**
        * Field constructor.
        * @param mask Field definition mask
        */
        public Field(long mask) {
            // Mask Layout:
            //   23:1  HiRes    (1 bit)     0x800000
            //   16:7  Type     (0..63)     0x7F0000
            //    8:8  Index    (0..15)     0x00FF00
            //    0:8  Length   (0..15)     0x0000FF
            this.fldType = (int)(mask >> 16) & 0x7F;
            this.hiRes   = ((mask & 0x800000) != 0);
            this.fldNdx  = (int)(mask >>  8) & 0xFF;
            this.fldLen  = (int)mask & 0xFF;
        }
        
        /**
        * Field constructor.
        * @param s The input string of format "type|[H|L]|index|length"
        */
        public Field(String s) {
            // "<type>|[H|L]|<index>|<length>"
            String f[] = StringTools.parseString(s,FIELD_VALUE_SEPARATOR);
            this.fldType = (f.length > 0)? (int)StringTools.parseLong(f[1],-1L) : -1;
            this.hiRes   = (f.length > 1)? f[0].equalsIgnoreCase("H") : false;
            this.fldNdx  = (f.length > 2)? (int)StringTools.parseLong(f[2], 0L) :  0;
            this.fldLen  = (f.length > 3)? (int)StringTools.parseLong(f[3], 0L) :  0;
        }
        
        /**
        * Returns the Field definition mask
        * @return The mask.
        */
        public long getMask() {
            long mask = 0L;
            if (this.hiRes) { mask |= 0x800000; }
            mask |= (this.fldType << 16) & 0x7F0000;
            mask |= (this.fldNdx  <<  8) & 0x00FF00;
            mask |= (this.fldLen  <<  0) & 0x0000FF;
            return mask;
        }

        /**
        * Returns the type of this Field.
        * @return this field type
        */
        public int getType() {
            return this.fldType;
        }
        
        /**
        * Returns the primitive type of this Field.
        * @return The primitive type.
        */
        public int getPrimitiveType() {
            switch (this.fldType) {
                case FIELD_GPS_POINT:    return PRIMITIVE_GPS;
                case FIELD_STRING:       return PRIMITIVE_STRING;
                case FIELD_BINARY:       return PRIMITIVE_BINARY;
                default:                 return PRIMITIVE_LONG;
            }
        }
        
        /**
        * Returns True if the type is valid.
        * @return True if the type is valid.
        */
        public boolean isValidType() {
            return true;
        }
        
        /**
        * Returns true if the type is represented by a signed number.
        * @return True if the type is represented by a signed number.
        */
        public boolean isSigned() {
            switch (this.fldType) {
                case FIELD_GPS_MAG_VARIATION:
                case FIELD_GPS_GEOID_HEIGHT:
                case FIELD_ALTITUDE:
                case FIELD_TEMP_LOW:
                case FIELD_TEMP_HIGH:
                case FIELD_TEMP_AVER:
                    return true;
                default:
                    return false;
            }
        }
        
        /**
        * Returns true if the type is represented by a hexidecimal number.
        * @return TRUE if the type is represented by a hexidecimal number.
        */
        public boolean isHex() {
            switch (this.fldType) {
                case FIELD_HEADING:
                    return !this.hiRes;
                case FIELD_STATUS_CODE:
                case FIELD_SEQUENCE:
                case FIELD_INPUT_ID:
                case FIELD_INPUT_STATE:
                case FIELD_OUTPUT_ID:
                case FIELD_OUTPUT_STATE:
                case FIELD_GEOFENCE_ID:
                    return true;
                default:
                    return false;
            }
        }

        /**
        * Returns True if this Field is HiRes.
        * @return True if this Field is HiRes.
        */
        public boolean isHiRes() {
            return this.hiRes;
        }
        
        /**
        * Returns the index of the Field.
        * @return The index of the Field.
        */
        public int getIndex() {
            return this.fldNdx;
        }
        
        /**
        * Returns the length of the Field.
        * @return The length of the Field.
        */
        public int getLength() {
            return this.fldLen;
        }
        
        /**
        * Parses a string, per the definition of this field, and writes the appropriate bytes to the specified Payload
        * @param s The string to be parsed.
        * @param sndx index of the string that is to be parsed
        * @param payload The payload into which the parsed value is written
        * @return The next string index
        */
        public int parseString(String s[], int sndx, Payload payload) {
            // NOTE: This should specifically set the index to the proper payload location!!
            int length = this.getLength();
            switch (this.getPrimitiveType()) {
                case PRIMITIVE_GPS: {
                    double lat = StringTools.parseDouble(s[sndx++], 0.0);
                    double lon = (sndx < s.length)? StringTools.parseDouble(s[sndx++], 0.0) : 0.0;
                    payload.writeGPS(new GeoPoint(lat,lon), length);
                    break;
                }
                case PRIMITIVE_STRING: {
                    payload.writeString(s[sndx++], length);
                    break;
                }
                case PRIMITIVE_BINARY: {
                    byte b[] = StringTools.parseHex(s[sndx++], new byte[0]);
                    payload.writeBytes(b, length);
                    break;
                }
                case PRIMITIVE_LONG:
                default: {
                    long val = s[sndx].startsWith("0x")?
                        StringTools.parseHexLong(s[sndx++], 0L) :
                        StringTools.parseLong(s[sndx++], 0L);
                    if (this.isSigned()) {
                        payload.writeLong(val, length);
                    } else {
                        payload.writeULong(val, length);
                    }
                    break;
                }
            }
            return sndx;
        }

        /**
        * Returns the String representation of this Field
        * @return The string representing of this Field.
        */
        public String toString() {
            StringBuffer sb = new StringBuffer();
            sb.append(this.getType());
            sb.append(FIELD_VALUE_SEPARATOR);
            sb.append(this.isHiRes()?"H":"L");
            sb.append(FIELD_VALUE_SEPARATOR);
            sb.append(this.getIndex());
            sb.append(FIELD_VALUE_SEPARATOR);
            sb.append(this.getLength());
            return sb.toString();
        }

        /**
        * Returns true if this Field is equal to the specified Field
        * @param other The object to be compared with this Field
        * @return True if equal, false otherwise
        */
        public boolean equals(Object other) {
            if (other instanceof Field) {
                return this.toString().equals(other.toString());
            } else {
                return false;
            }
        }

    }

}

