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
//  This class handles encoding and decoding of packet payloads.
// ----------------------------------------------------------------------------
// Change History:
//  2006/03/26  Martin D. Flynn
//      Initial release
//  2006/11/03  Guanghong Yang
//      Include JavaDocs
// ----------------------------------------------------------------------------
package com.tommasocodella.androdmtp.opendmtp.util;


/**
* Handles encoding and decoding of packet payloads. It provides utilities to transfer between
* common data types and input/output stream.
*/
public class Payload
{

    // ------------------------------------------------------------------------

    /** Maximum length of packet payloads. */
    public static final int     MAX_PAYLOAD_LENGTH = 255;

    // ------------------------------------------------------------------------

    private byte        payload[] = null;
    private int         size = 0;
    private int         index = 0;

    /**
    * Initializes the instance by setting size and index to 0 and creates a new byte[] with maximum
    * length.
    */
    public Payload()
    {
        // configure for creating a new packet (data destination)
        this.payload = new byte[MAX_PAYLOAD_LENGTH];
        this.size    = 0; // no 'size' yet
        this.index   = 0; // start at index '0' for writing
    }
    
    /**
    * Initializes the instance by setting the payload with an already converted byte[].
    * @param b The payload byte[].
    */
    public Payload(byte b[])
    {
        this(b, 0, ((b != null)? b.length : 0));
    }
    
    /**
    * Initializes the instance with a String.
    * @param s The String to be set into the payload.
    */
    public Payload(String s)
    {
        this();
        if (s != null) {
            this.writeString(s, s.length());
        }
    }
    
    /**
    * Initializes the instance by setting the payload with a part of an already converted byte[].
    * @param b The payload byte array
    * @param ofs Offset into the byte array
    * @param len Length of the bytes to be parsed
    */
    public Payload(byte b[], int ofs, int len)
    {
        // (data source)
        if ((b == null) || (ofs >= b.length)) {
            this.payload = new byte[0];
            this.size    = 0;
            this.index   = 0;
        } else
        if ((ofs == 0) && (b.length == len)) {
            this.payload = b;
            this.size    = b.length;
            this.index   = 0;
        } else {
            if (len > (b.length - ofs)) { len = b.length - ofs; }
            this.payload = new byte[len];
            System.arraycopy(b, ofs, this.payload, 0, len);
            this.size    = len;
            this.index   = 0;
        }
    }
    
    // ------------------------------------------------------------------------

    /**
    * Returns the size of the payload. For writing, it is total bytes written; for reading, it is
    * total bytes available.
    * @return Size of the payload.
    */
    public int getSize()
    {
        // Write: total bytes written
        // Read : total bytes available
        return this.size;
    }
    
    /**
    * Returns the available size of the payload. For writing, it is the ramaining bytes available for
    * writing; for reading, it is the ramaining bytes available for reading.
    * @return The available size of the payload.
    */
    public int getAvail()
    {
        // Write: remaining bytes available for writing
        // Read : remaining bytes available for reading
        return (this.size - this.index);
    }
    
    /**
    * Returns the current index. For writing, it is the number of bytes written; for reading, it is
    * the number of bytes read.
    * @return The current index.
    */
    public int getIndex()
    {
        // Write: number of bytes written
        // Read : number of bytes read
        return this.index;
    }

    // ------------------------------------------------------------------------

    /**
    * Returns the byte[] of the payload.
    * @return The byte[] of the payload.
    */
    private byte[] _getBytes()
    {
        return this.payload;
    }
    
    /**
    * Returns the content of the payload.
    * @return The byte[] of the payload.
    */
    public byte[] getBytes()
    {
        // return the full payload (regardless of the state of 'this.index')
        byte b[] = this._getBytes();
        if (this.size == b.length) {
            return b;
        } else {
            byte n[] = new byte[this.size];
            System.arraycopy(b, 0, n, 0, this.size);
            return n;
        }
    }
    
    // ------------------------------------------------------------------------

    /**
    * Resets the index of the payload. This enables reading and writing from the beginning.
    */
    public void resetIndex()
    {
        // this makes Payload a data source
        this.resetIndex(0);
    }

    /**
    * Resets the index of the payload to the indicated position. This enables random reading and
    * writing.
    * @param ndx The position to start reading or writing.
    */
    public void resetIndex(int ndx)
    {
        this.index = (ndx <= 0)? 0 : ndx;
    }
    
    /**
    * Checks if the payload has enough available space.
    * @param length The length of data to be added to the payload.
    * @return True if the payload has enough available space for the length of data, or false
    *         otherwise.
    */
    public boolean isValidLength(int length)
    {
        return ((this.index + length) <= this.size);
    }
    // ------------------------------------------------------------------------
    
    /**
    * Converts a part of byte[] to a long value. Returns the default value if something went wrong.
    * @param data The byte[] to be converted.
    * @param ofs The offset into the byte[];
    * @param len The length of the part of byte[] to be converted.
    * @param signed True if the long value is signed or false if it is unsigned.
    * @param dft The default value.
    * @return A long value converted from a part of byte[].
    */
    private static long _decodeLong(byte data[], int ofs, int len, boolean signed, long dft)
    {
        // Big-Endian order
        // { 0x01, 0x02, 0x03 } -> 0x010203
        if ((data != null) && (data.length >= (ofs + len))) {
            long n = (signed && ((data[ofs] & 0x80) != 0))? -1L : 0L;
            for (int i = ofs; i < ofs + len; i++) {
                n = (n << 8) | ((long)data[i] & 0xFF); 
            }
            return n;
        } else {
            return dft;
        }
    }

    /**
    * Reads the payload for a certain length and parse it to a long value. If the length is longer
    * than the available length, returns the default value.
    * @param length The length of bytes to be read.
    * @param dft The default value.
    * @return A long value read from the stream.
    */
    public long readLong(int length, long dft)
    {
        int maxLen = ((this.index + length) <= this.size)? length : (this.size - this.index);
        if (maxLen <= 0) {
            // nothing to read
            return dft;
        } else {
            byte b[] = this._getBytes();
            long val = _decodeLong(b, this.index, maxLen, true, dft);
            this.index += maxLen;
            return val;
        }
    }

    /**
    * Reads the payload for a certain length and parse it to a long value. The default value is set
    * as 0.
    * @param length The length of bytes to be read.
    * @return A long value read from the stream.
    * @see #readLong(int, long).
    */
    public long readLong(int length)
    {
        return this.readLong(length, 0L);
    }
    
    // ------------------------------------------------------------------------

    /**
    * Reads the payload for a certain length and parse it to a unsigned long value. If the length is
    * longer than the available length, returns the default value.
    * @param length The length of bytes to be read.
    * @param dft The default value.
    * @return A unsigned long value read from the stream.
    */
    public long readULong(int length, long dft)
    {
        int maxLen = ((this.index + length) <= this.size)? length : (this.size - this.index);
        if (maxLen <= 0) {
            // nothing to read
            return dft;
        } else {
            byte b[] = this._getBytes();
            long val = _decodeLong(b, this.index, maxLen, false, dft);
            this.index += maxLen;
            return val;
        }
    }

    /**
    * Reads the payload for a certain length and parse it to a unsigned long value. The default value
    * is set as 0.
    * @param length The length of bytes to be read.
    * @return A unsigned long value read from the stream.
    * @see #readULong(int, long).
    */
    public long readULong(int length)
    {
        return this.readULong(length, 0L);
    }
    
    // ------------------------------------------------------------------------

    /**
    * Reads the payload for a certain length and parse it to a byte[]. If the length is longer than
    * the available length, returns an empty byte[].
    * @param length The length of bytes to be read.
    * @return A byte[] read from the stream.
    */
    public byte[] readBytes(int length)
    {
        int maxLen = ((this.index + length) <= this.size)? length : (this.size - this.index);
        if (maxLen <= 0) {
            // no room left
            return new byte[0];
        } else {
            byte n[] = new byte[maxLen];
            System.arraycopy(this._getBytes(), this.index, n, 0, maxLen);
            this.index += maxLen;
            return n;
        }
    }
    
    // ------------------------------------------------------------------------

    /**
    * Reads the payload for a certain length and parse it to a String. If the length is longer than
    * the available length, returns an empty String.
    * @param length The length of bytes to be read.
    * @return A String read from the stream.
    */
    public String readString(int length)
    {
        int maxLen = ((this.index + length) <= this.size)? length : (this.size - this.index);
        if (maxLen <= 0) {
            // no room left
            return "";
        } else {
            int m;
            byte b[] = this._getBytes();
            for (m = 0; (m < maxLen) && ((this.index + m) < this.size) && (b[this.index + m] != 0); m++);
            String s = StringTools.toStringValue(b, this.index, m);
            this.index += m;
            if (m < maxLen) { this.index++; }
            return s;
        }
    }
    
    // ------------------------------------------------------------------------

    /**
    * Reads the payload for a certain length and parse it to a String. If the length is longer than
    * the available length, returns a new GeoPoint and sets the index to the end.
    * @param length The length of bytes to be read.
    * @return A <tt>GeoPoint</tt> read from the stream.
    */
    public GeoPoint readGPS(int length)
    {
        int maxLen = ((this.index + length) <= this.size)? length : (this.size - this.index);
        if (maxLen < 6) {
            // not enough bytes to decode GeoPoint
            GeoPoint gp = new GeoPoint();
            if (maxLen > 0) { this.index += maxLen; }
            return gp;
        } else
        if (length < 8) {
            // 6 <= len < 8
            GeoPoint gp = GeoPoint.decodeGeoPoint(this._getBytes(), this.index, length);
            this.index += maxLen; // 6
            return gp;
        } else {
            // 8 <= len
            GeoPoint gp = GeoPoint.decodeGeoPoint(this._getBytes(), this.index, length);
            this.index += maxLen; // 8
            return gp;
        }
    }

    // ------------------------------------------------------------------------
    
    /**
    * Converts a long value to the indicated position of the byte[].
    * @param data The byte[] to be written.
    * @param ofs The offset into the byte[] to begin the writing.
    * @param len The length of the byte[] to write.
    * @param val The long value to be converted.
    * @return The length if succeeds, or 0 if fails.
    */
    private static int _encodeLong(byte data[], int ofs, int len, long val)
    {
        // Big-Endian order
        if ((data != null) && (data.length >= (ofs + len))) {
            long n = val;
            for (int i = (ofs + len - 1); i >= ofs; i--) {
                data[i] = (byte)(n & 0xFF);
                n >>>= 8;
            }
            return len;
        } else {
            return 0;
        }
    }

    /**
    * Writes to the payload a long value with a indicated length.
    * @param val The long value to be written.
    * @param length The length of bytes to be written.
    * @return The length if succeeds, or 0 if fails.
    */
    public int writeLong(long val, int length)
    {
        byte b[] = this._getBytes();
        if (length <= 0) {
            // nothing to write
            return length;
        } else
        if ((this.index + length) > b.length) {
            // no room left
            return 0;
        } else {
            _encodeLong(b, this.index, length, val);
            this.index += length;
            if (this.size < this.index) { this.size = this.index; }
            return length;
        }
    }

    /**
    * Writes to the payload a unsigned long value with a indicated length.
    * @param val The unsigned long value to be written.
    * @param length The length of bytes to be written.
    * @return The length if succeeds, or 0 if fails.
    * @see #writeLong(long, int).
    */
    public int writeULong(long val, int length)
    {
        return this.writeLong(val, length);
    }
    
    // ------------------------------------------------------------------------

    /**
    * Writes to the payload a byte[] with a indicated length.
    * @param n The byte[] to be written.
    * @param length The length of bytes to be written.
    * @return The length if succeeds, or 0 if fails.
    */
    public int writeBytes(byte n[], int length)
    {
        byte b[] = this._getBytes();
        int maxLen = ((this.index + length) <= b.length)? length : (b.length - this.index);
        if ((n == null) || (n.length == 0)) {
            // nothing to write
            return 0;
        } else
        if (maxLen <= 0) {
            // no room left
            return 0;
        } else {
            int m = (n.length < maxLen)? n.length : maxLen;
            System.arraycopy(n, 0, b, this.index, m);
            for (;m < maxLen; m++) { b[m] = 0; }
            this.index += m;
            if (this.size < this.index) { this.size = this.index; }
            return m;
        }
    }

    // ------------------------------------------------------------------------

    /**
    * Writes to the payload a String with a indicated length.
    * @param s The String to be written.
    * @param length The length of bytes to be written.
    * @return The length if succeeds, or 0 if fails.
    */
    public int writeString(String s, int length)
    {
        byte b[] = this._getBytes();
        int maxLen = ((this.index + length) <= b.length)? length : (b.length - this.index);
        if (s == null) {
            // nothing to write
            return 0;
        } else
        if (maxLen <= 0) {
            // no room left
            return 0;
        } else {
            byte n[] = StringTools.getBytes(s);
            int m = (n.length < maxLen)? n.length : maxLen;
            System.arraycopy(n, 0, b, this.index, m);
            this.index += m;
            if (m < maxLen) { 
                b[this.index++] = (byte)0; // terminate string
                m++;
            }
            if (this.size < this.index) { this.size = this.index; }
            return m;
        }
    }

    // ------------------------------------------------------------------------

    /**
    * Writes to the payload a <tt>GeoPoint</tt> with a indicated length.
    * @param gp The <tt>GeoPoint</tt> to be written.
    * @param length The length of bytes to be written.
    * @return The length if succeeds, or 0 if fails.
    */
    public int writeGPS(GeoPoint gp, int length)
    {
        byte b[] = this._getBytes();
        int maxLen = ((this.index + length) <= b.length)? length : (b.length - this.index);
        if (maxLen < 6) {
            // not enough bytes to encode GeoPoint
            return 0;
        } else
        if (length < 8) {
            // 6 <= len < 8
            GeoPoint.encodeGeoPoint(gp, b, this.index, length);
            this.index += 6;
            if (this.size < this.index) { this.size = this.index; }
            return 6;
        } else {
            // 8 <= len
            GeoPoint.encodeGeoPoint(gp, b, this.index, length);
            this.index += 8;
            if (this.size < this.index) { this.size = this.index; }
            return 8;
        }
    }

    // ------------------------------------------------------------------------

    /**
    * Returns the Hex representation of the payload's data.
    * @return A String of the Hex representation of the payload's data.
    */
    public String toString()
    {
        return StringTools.toHexString(this.payload, 0, this.size);
    }
    
    // ------------------------------------------------------------------------
    
}

