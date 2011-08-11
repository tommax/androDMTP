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
//  This class is the generic interface for GPS devices that support the
//  NMEA-0183 protocol.
// ----------------------------------------------------------------------------
// Change History:
//  2006/03/26  Martin D. Flynn
//      Initial release
//  2006/11/03  Robert S. Brewer
//      Include JavaDocs
// ----------------------------------------------------------------------------
package com.tommasocodella.androdmtp.opendmtp.client.gps;

import java.io.InputStream;
import java.io.IOException;

/**
* A generic interface for GPS devices that support the NMEA-0183 protocol.
*/
public interface GPSDevice
{

    /**
    * GPS device driver return whether this device should be run in a separate thread
    * @return true if the GPS driver should be run in a thread
    */
    public boolean runInThread();
    
    /**
    * Indicates whether the connection to the GPS device is open or not.
    * @return true if the GPS device is open, false if not.
    */
    public boolean isOpen();

    /**
    * Opens a connection to the GPS device.
    * @return true if the connection could be opened, false otherwise.
    * @throws GPSException if connection could not be opened. Now preferred behavior is to
    * just return false and not throw an exception.
    */
    public boolean openDevice() 
        throws GPSException;

    /**
    * Reads a line of data from the GPS device into a provided buffer, with timeout if
    * the GPS device is unresponsive.
    * @param sb buffer to write GPS data into.
    * @param timeoutMS milliseconds to wait before timing out.
    * @return number of bytes read.
    * @throws GPSException if GPS error encountered.
    * @throws SecurityException if unexpected security problem encountered.
    * @throws IOException if unexpected IO error encountered.
    * @throws InterruptedException if another thread interrupts this thread while waiting for
    * data.
    */
    public int readLine(StringBuffer sb, long timeoutMS) 
        throws GPSException, InterruptedException, IOException, SecurityException;

    /**
    * Closes connection to GPS device.
    */
    public void closeDevice();

}
