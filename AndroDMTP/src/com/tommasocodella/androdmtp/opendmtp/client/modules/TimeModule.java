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
//  This class provides time-based checking for generation of scheduled events.
// ----------------------------------------------------------------------------
// Change History:
//  2006/03/26  Martin D. Flynn
//      Initial release
//  2006/11/03  Robert Puckett
//      Include JavaDocs
// ----------------------------------------------------------------------------
package com.tommasocodella.androdmtp.opendmtp.client.modules;

import com.tommasocodella.androdmtp.opendmtp.client.base.Packet;
import com.tommasocodella.androdmtp.opendmtp.client.base.TimeModules;
import com.tommasocodella.androdmtp.opendmtp.client.base.PacketQueue;
import com.tommasocodella.androdmtp.opendmtp.codes.StatusCodes;
import com.tommasocodella.androdmtp.opendmtp.util.DateTime;
import com.tommasocodella.androdmtp.opendmtp.util.GeoEvent;
import com.tommasocodella.androdmtp.opendmtp.util.Log;

/**
* Provides time-based checking for generation of scheduled events.
*/
public class TimeModule
    implements StatusCodes, TimeModules.Module
{

    // ------------------------------------------------------------------------

    private static final String LOG_NAME                = "TIME";

    // ----------------------------------------------------------------------------
    
    private static final int TIME_PRIORITY              = Packet.PRIORITY_NORMAL;

    // ------------------------------------------------------------------------

    /**
    * Contains a queue for events where each event includes a priority and GeoEvent. No sorting on
    * priority is done. <br>
    * Note: <br>
    * Time events have the default new GeoEvent associated with them.
    */
    private PacketQueue     packetQueue                 = null;

    /**
    * Creates an instance of TimeModule that initializes the internal queue.
    * @param queue A queue of events.
    */
    public TimeModule(PacketQueue queue)
    {
        this.packetQueue = queue; // must not be null
    }

    // ------------------------------------------------------------------------

    /**
    * Checks the time passed against the times in the queue. <br>
    * Note:<br>
    * Currently not implemented
    * @param currentTime A time value.
    */
    public void checkTime(long currentTime)
    {
        //this._queueTimeEvent(TIME_PRIORITY, STATUS_ELAPSED_LIMIT_00);
    }

    // ----------------------------------------------------------------------------
    
    /**
    * Adds a time event to the queue.
    * @param priority The priority for the time event.
    * @param code The status code for the time event.
    */
    private void _queueTimeEvent(int priority, int code)
    {
        Log.debug(LOG_NAME, "Queuing Time event ...");
        GeoEvent gev = new GeoEvent();
        gev.setTimestamp(DateTime.getCurrentTimeSec());
        gev.setStatusCode(code);
        this.packetQueue.addEvent(priority, gev);
    }

}
