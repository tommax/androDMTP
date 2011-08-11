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
//  Fletcher checksums calculations.
// ----------------------------------------------------------------------------
// Change History:
//  2006/03/26  Martin D. Flynn
//      Initial release
//  2006/11/03  Yoshiaki Iinuma
//      Include JavaDocs
// ----------------------------------------------------------------------------
package com.tommasocodella.androdmtp.opendmtp.util;


/**
* Provides Fletcher's checksum calculations.
*/
public class FletcherChecksum
{
    
    // ------------------------------------------------------------------------

    private int C[] = { 0, 0 };
    
    /**
    * Default constructor
    */
    public FletcherChecksum()
    {
        this.reset();
    }
    
    // ------------------------------------------------------------------------

    /**
    * Resets the current checksum value to zero.
    */
    public void reset()
    {
        this.C[0] = 0;
        this.C[1] = 0;
    }
    
    // ------------------------------------------------------------------------

    /**
    * Returns an array containing the current checksum value.
    * @return an array containing the current checksum value.
    */
    public int[] getValues()
    {
        int F[] = new int[2];
        F[0] = C[0] & 0xFF;
        F[1] = C[1] & 0xFF;
        return F;
    }
    
    // ------------------------------------------------------------------------
    
    /**
    * Tests if the computed checksum is valid.
    * @return true if the checksum is valid.
    */
    public boolean isValid()
    {
        byte F[] = this.getChecksum();
        //Print.println("F0=" + (F[0]&0xFF) + ", F1=" + (F[1]&0xFF));
        return (F[0] == 0) && (F[1] == 0);
    }

    /**
    * Returns the byte array containing the checksum value.
    * @return the byte array containing the checksum value.
    */
    public byte[] getChecksum()
    {
        return this.getChecksum(new byte[2], 0);
    }

    /**
    * Returns the checksum value as a big-endian int.
    * @return the checksum value.
    */
    public int getChecksumAsInt()
    {
        byte cs[] = this.getChecksum(new byte[2], 0);
        return ((((int)cs[0] & 0xFF) << 8) | ((int)cs[1] & 0xFF));
    }

    /**
    * Replaces the value at the specified position in the specified byte array with the already
    * computed checksum.
    * @param F the byte array to which the checksum value is stored.
    * @param offset the offset to specify the position at which the checksum is stored in the byte array.
    * @return the byte array to which checksum value was stored.
    */
    public byte[] getChecksum(byte F[], int offset)
    {
        F[offset + 0] = (byte)((C[0] - C[1])      & 0xFF);
        F[offset + 1] = (byte)((C[1] - (C[0]<<1)) & 0xFF);
        return F;
    }

    /**
    * Calculates the checksum of the specified data. The Calculated checksum is kept in this class.
    * @param b the byte array to be added to the checksum.
    */
    public void runningChecksum(byte b[])
    {
        if (b != null) {
            for (int i = 0; i < b.length; i++) {
                C[0] = C[0] + ((int)b[i] & 0xFF);
                C[1] = C[1] + C[0];
            }
        }
    }
    
    // ------------------------------------------------------------------------

}

