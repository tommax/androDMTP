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
//  This class contains packet parsing exceptions.
// ----------------------------------------------------------------------------
// Change History:
//  2006/03/26  Martin D. Flynn
//      Initial release
//  2006/11/03  Kiet Huynh
//      Include JavaDocs
// ----------------------------------------------------------------------------
package com.tommasocodella.androdmtp.opendmtp.client.base;

import com.tommasocodella.androdmtp.opendmtp.util.StringTools;

/**
* Exception when working with packets.
*/
public class PacketParseException
    extends Exception
{
    // ------------------------------------------------------------------------

    private Packet  packet      = null;
    private int     errorCode   = 0x0000;
    private byte    errorData[] = null;
    private boolean terminate   = false;

    // ------------------------------------------------------------------------

    /**
    * Constructor taking an error code and the Packet which caused this exception
    * @param errCode The error code.
    * @param packet The packet causing this exception
    */
    public PacketParseException(int errCode, Packet packet) 
    {
        this(errCode, packet, null);
    }
    
    /**
    * Constructor taking an error code, Packet, and general error data
    * @param errCode The error code.
    * @param packet The packet causing this exception
    * @param errData The error data.
    */
    public PacketParseException(int errCode, Packet packet, byte errData[]) 
    {
        super();
        this.packet    = packet;
        this.errorCode = errCode;
        this.errorData = errData;
    }
    
    // ------------------------------------------------------------------------

    /**
    * Returns the contained error packet.
    * @return The error packet
    */
    public Packet getPacket()
    {
        return this.packet;
    }

    /**
    * Sets the Packet for which this exception was generated
    * @param pkt The packet causing this exception
    */
    public void setPacket(Packet pkt)
    {
        this.packet = pkt;
    }

    // ------------------------------------------------------------------------

    /**
    * Returns the error code.
    * @return the error code.
    */
    public int getErrorCode()
    {
        return this.errorCode;
    }
    
    /**
    * Returns the error data.
    * @return An array containing error data.
    */
    public byte[] getErrorData() 
    {
        return this.errorData;
    }
    
    /**
    * Creates a client error packet (for transmission to the server)
    * @return The error packet.
    */
    public Packet createClientErrorPacket()
    {
        int errCode    = this.getErrorCode();
        Packet cause   = this.getPacket();
        byte errData[] = this.getErrorData();
        Packet errPkt  = Packet.createClientErrorPacket(errCode, cause);
        if (errData != null) {
            // DO NOT RESET PAYLOAD INDEX!!!
            errPkt.getPayload(false).writeBytes(errData, errData.length);
        }
        return errPkt;
    }
    
    // ------------------------------------------------------------------------

    /**
    * Sets the session terminate flag.
    * This indicates to the protocol handler that this exception was severe enough
    * that the session should be terminated after sending the error packet to the server.
    */
    public void setTerminate()
    {
        this.terminate = true;
    }
    
    /**
    * Returns true if the communication session is to be terminated as a result of this
    * exception.
    * @return True is the session is to be terminated. Otherwise, returns false.
    */
    public boolean terminateSession()
    {
        return this.terminate;
    }
    
    // ------------------------------------------------------------------------

    /**
    * Returns the String representation of this exception
    * @return The String representation of this exception
    */
    public String toString() 
    {
        int errCode = this.getErrorCode();
        StringBuffer sb = new StringBuffer();
        sb.append("[");
        sb.append(StringTools.toHexString(errCode,16));
        sb.append("] ");
        //sb.append(ServerErrors.getErrorDescription(errCode));
        return sb.toString();
    }
    
    // ------------------------------------------------------------------------

}
