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
//  Base64 encoding/decoding.
// ----------------------------------------------------------------------------
// Change History:
//  2006/03/26  Martin D. Flynn
//      Initial release
//  2006/11/03  Robert Puckett
//      Include JavaDocs
// ----------------------------------------------------------------------------
package com.tommasocodella.androdmtp.opendmtp.util;

/**
* Provides methods for base64 encoding/decoding.
*/
public class Base64
{
    
    // ------------------------------------------------------------------------
    
    /**
    * Contains the base64 character map.
    */
    private static final char Base64Map[] = {
        'A','B','C','D','E','F','G','H','I','J','K','L','M',
        'N','O','P','Q','R','S','T','U','V','W','X','Y','Z',
        'a','b','c','d','e','f','g','h','i','j','k','l','m',
        'n','o','p','q','r','s','t','u','v','w','x','y','z',
        '0','1','2','3','4','5','6','7','8','9','+','/'
    };
    
    /**
    * Contains the base64 pad character.
    */
    private static final char Base64Pad = '=';
    
    /**
    * Calculates the index of the character specified in base64.
    * @param ch A character.
    * @return The index of the character in the base64 map, or 0 for invalid character.
    */
    private static int _indexOf(char ch) 
    {
        for (int i = 0; i < Base64Map.length; i++) {
            if (ch == Base64Map[i]) {
                return i;
            }
        }
        return 0; // invalid character found
    }
    
    // ------------------------------------------------------------------------
    
    /**
    * Encodes the string in base64.
    * @param str A string to be encoded.
    * @return The base64 encoded string.
    */
    public static String encode(String str)
    {
        return (str != null)? Base64.encode(str.getBytes()) : "";
    }
    
    /**
    * Encodes a byte array into base64.
    * @param buff A byte array.
    * @return A string representing the base64 encoding of the byte array buff.
    */
    public static String encode(byte buff[])
    {
        StringBuffer sb = new StringBuffer();
        int len = buff.length;
        
        for (int i = 0; i < len; i += 3) {
            // 00000000 00000000 00000000
            // 10000010 00001000 00100000

            /* place next 3 bytes into register */
            int              reg24  = ((int)buff[i  ] << 16) & 0xFF0000;
            if ((i+1)<len) { reg24 |= ((int)buff[i+1] <<  8) & 0x00FF00; }
            if ((i+2)<len) { reg24 |= ((int)buff[i+2]      ) & 0x0000FF; }
            
            /* encode data 6 bits at a time */
            sb.append(             Base64Map[(reg24 >>> 18) & 0x3F]);
            sb.append(             Base64Map[(reg24 >>> 12) & 0x3F]);
            sb.append(((i+1)<len)? Base64Map[(reg24 >>>  6) & 0x3F] : Base64Pad);
            sb.append(((i+2)<len)? Base64Map[(reg24       ) & 0x3F] : Base64Pad);
            
        }
        
        return sb.toString();
        
    }
    
    // ------------------------------------------------------------------------
    
    /**
    * Decodes a base64 string into a byte array (not tested).
    * @param b64Str A base64 string.
    * @return The byte array corresponding to the base64 string.
    */
    static public byte[] decode(String b64Str)
    {
        // not yet tested
        StringBuffer sb = new StringBuffer();
        int len = b64Str.length();
        while ((len > 0) && (b64Str.charAt(len - 1) == Base64Pad)) { len--; }
        
        /* output buffer length */
        // XX==, XXX=, XXXX, XXXXXX==
        int b = 0, blen = (((len - 1) / 4) * 3) + ((len - 1) % 4);
        if (((len - 1) % 4) == 0) {
            // the encoded Base64 String has an invalid length
            blen++;
        }
        byte buff[] = new byte[blen]; 
        // 1=?0, 2=1, 3=2, 4=3, 5=?3, 6=4, 7=5, 8=6, 9=?6, 10=7
        
        for (int i = 0; i < len; i += 4) {
            
            /* place next 3 bytes into register */
            int              reg24  = (_indexOf(b64Str.charAt(i  )) << 18) & 0xFC0000;
            if ((i+1)<len) { reg24 |= (_indexOf(b64Str.charAt(i+1)) << 12) & 0x03F000; }
            if ((i+2)<len) { reg24 |= (_indexOf(b64Str.charAt(i+2)) <<  6) & 0x000FC0; }
            if ((i+3)<len) { reg24 |= (_indexOf(b64Str.charAt(i+3))      ) & 0x00003F; }

                             buff[b++] = (byte)((reg24 >>> 16) & 0xFF);
            if ((i+2)<len) { buff[b++] = (byte)((reg24 >>>  8) & 0xFF); }
            if ((i+3)<len) { buff[b++] = (byte)((reg24       ) & 0xFF); }

        }
        
        return buff;
        
    }

}
