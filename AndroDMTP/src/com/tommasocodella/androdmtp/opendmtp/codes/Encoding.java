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
//  OpenDMTP protocol packet encoding constants.
// ----------------------------------------------------------------------------
// Change History:
//  2006/03/26  Martin D. Flynn
//      Initial release
//  2006/11/03  Robert Puckett
//      Include JavaDocs
// ----------------------------------------------------------------------------
package com.tommasocodella.androdmtp.opendmtp.codes;


/**
* Contains OpenDMTP protocol packet encoding constants.
*/
public class Encoding
{
    
    // ------------------------------------------------------------------------
    
    /**
    * Contains a character signifying ASCII encoding.
    */
    public static final byte    AsciiEncodingChar           = '$';

    /**
    * Contains a character signifying the ASCII Checksum.
    */
    public static final byte    AsciiChecksumChar           = '*';

    /**
    * Contains a character signifying ASCII end of line.
    */
    public static final byte    AsciiEndOfLineChar          = '\r';

    // ------------------------------------------------------------------------

    /**
    * Contains a character signifying Base64 encoding.
    */
    public static final byte    ENCODING_BASE64_CHAR        = '='; 

    /**
    * Contains a character signifying HEX encoding.
    */
    public static final byte    ENCODING_HEX_CHAR           = ':';

    /**
    * Contains a character signifying CSV encoding.
    */
    public static final byte    ENCODING_CSV_CHAR           = ',';

    // ------------------------------------------------------------------------
    // Packet encoding
    
    /**
    * Contains a constant checksum mask used in packet encoding.
    */
    public static final int     CHECKSUM_MASK               = 0x8000;

    /**
    * Contains a constant encoding mask used in packet encoding.
    */
    public static final int     ENCODING_MASK               = 0x00FF;

    /**
    * Contains a constant representing the supported binary encoding.
    */
    public static final int     SUPPORTED_ENCODING_BINARY   = 0x0001;

    /**
    * Contains a constant representing the supported base64 encoding.
    */
    public static final int     SUPPORTED_ENCODING_BASE64   = 0x0002;

    /**
    * Contains a constant representing the supported hex encoding.
    */
    public static final int     SUPPORTED_ENCODING_HEX      = 0x0004;

    /**
    * Contains a constant representing the supported CSV encoding.
    */
    public static final int     SUPPORTED_ENCODING_CSV      = 0x0008;

    /**
    * Contains a constant representing the requirement of a support server for certain encoding
    * types.
    */
    public static final int     SUPPORT_SERVER_REQUIRED     = SUPPORTED_ENCODING_BINARY | SUPPORTED_ENCODING_BASE64 | SUPPORTED_ENCODING_HEX;

    /**
    * Contains a constant representing an unknown encoding.
    */
    public static final int     ENCODING_UNKNOWN            = 0x0000;     // unknown ASCII encoding

    /**
    * Contains a constant representing binary encoding.
    */
    public static final int     ENCODING_BINARY             = SUPPORTED_ENCODING_BINARY;

    /**
    * Contains a constant representing base64 encoding.
    */
    public static final int     ENCODING_BASE64             = SUPPORTED_ENCODING_BASE64;

    /**
    * Contains a checksum value for base64 encoding.
    */
    public static final int     ENCODING_BASE64_CKSUM       = SUPPORTED_ENCODING_BASE64 | CHECKSUM_MASK;

    /**
    * Contains a constant representing hex encoding.
    */
    public static final int     ENCODING_HEX                = SUPPORTED_ENCODING_HEX;

    /**
    * Contains a checksum value for hex encoding.
    */
    public static final int     ENCODING_HEX_CKSUM          = SUPPORTED_ENCODING_HEX | CHECKSUM_MASK;

    /**
    * Contains a constant representing CSV encoding.
    */
    public static final int     ENCODING_CSV                = SUPPORTED_ENCODING_CSV;

    /**
    * Contains a checksum value for CSV encoding.
    */
    public static final int     ENCODING_CSV_CKSUM          = SUPPORTED_ENCODING_CSV | CHECKSUM_MASK;
    
    /**
    * Checks if the encoding is binary.
    * @param encoding Encoding to be checked.
    * @return True if encoding is binary, false otherwise.
    */
    public static boolean IsEncodingBinary(int encoding) { 
        return (encoding == ENCODING_BINARY);
    }

    /**
    * Checks if the encoding is ASCII.
    * @param encoding Encoding to be checked.
    * @return True if the encoding is ASCII, false otherwise.
    */
    public static boolean IsEncodingAscii(int encoding) { 
        return ((encoding & ENCODING_MASK) > ENCODING_BINARY);
    }

    /**
    * Checks if the encoding is checksum.
    * @param encoding Encoding to be checked.
    * @return True if the encoding is checksum, false otherwise.
    */
    public static boolean IsEncodingChecksum(int encoding) { 
        return ((encoding & CHECKSUM_MASK) != 0);
    }

    /**
    * Determines if the server is required for the encoding type specified.
    * @param encoding The encoding type.
    * @return True if the server is required, false otherwise.
    */
    public static boolean IsServerRequired(int encoding) {
        return ((encoding & SUPPORT_SERVER_REQUIRED) != 0);
    }

    // ------------------------------------------------------------------------

}
