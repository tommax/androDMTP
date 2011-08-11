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
//  This class manages the transport layer for the OpenDMTP protocol.
// ----------------------------------------------------------------------------
// Change History:
//  2006/03/31  Martin D. Flynn
//      Initial release
//  2006/11/03  Elayne Man
//      Include JavaDocs
// ----------------------------------------------------------------------------
package com.tommasocodella.androdmtp.opendmtp.client.base;

/**
* The Transport interface.
*/
public interface Transport
{

    /**
    * Returns true if the transport is open.
    * @return True if transport is open
    */
    public boolean isOpen();

    /**
    * Opens the transport medium.
    * @param xportType The transport type
    * @return true, if successful
    */
    public boolean open(int xportType);

    /**
    * Closes the transport medium.
    * @param sendUDP True if sending UDP packets
    * @return true, if successful
    */
    public boolean close(boolean sendUDP);

    /**
    * Read a single packet as a byte array
    * @return the single packet read
    */
    public byte[] readPacket();

    /**
    * Write a single packet to the transport media
    * @param b packet to be written
    * @return the length written
    */
    public int writePacket(byte b[]);

}
