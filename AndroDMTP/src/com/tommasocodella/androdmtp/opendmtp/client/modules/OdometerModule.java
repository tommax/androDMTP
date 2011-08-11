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
//  This class provides GPS-based checking for odometer value accumulation.
// ----------------------------------------------------------------------------
// Change History:
//  2006/??/??  Martin D. Flynn
//     -Initial release
// ----------------------------------------------------------------------------
package com.tommasocodella.androdmtp.opendmtp.client.modules;

import com.tommasocodella.androdmtp.opendmtp.client.base.PacketQueue;
import com.tommasocodella.androdmtp.opendmtp.client.base.GPSModules;
import com.tommasocodella.androdmtp.opendmtp.client.base.Props;
import com.tommasocodella.androdmtp.opendmtp.codes.StatusCodes;
import com.tommasocodella.androdmtp.opendmtp.util.GeoEvent;
import com.tommasocodella.androdmtp.opendmtp.util.GeoPoint;

public class OdometerModule
    implements StatusCodes, GPSModules.Module
{

    // ----------------------------------------------------------------------------

    /**
    * Contains a queue for events where each event includes a priority and GeoEvent.
    */
    private PacketQueue     packetQueue                 = null;
    
    /**
    * Contains the last 'saved' odometer value
    */
    private long            lastSavedOdomKM             = 0L;

    /**
    * Constructs a OdometerModule instance with the passed packet queue.
    * @param queue A motion event queue.
    */
    public OdometerModule(PacketQueue queue)
    {
        this.packetQueue = queue; // must not be null
    }

    // ----------------------------------------------------------------------------

    private void _saveProps()
    {
        Props.saveProps();
    }
    
    // ----------------------------------------------------------------------------

    /**
    * Compares the two GeoEvent parameters, calculating odometer values
    * @param oldFix The previous GeoEvent
    * @param newFix The new GeoEvent
    */
    public void checkGPS(GeoEvent oldFix, GeoEvent newFix)
    {
        GeoPoint newGP = newFix.getGeoPoint(); // includes lat/lon/time
        long odomMeters = Props.getLong(Props.PROP_ODOMETER_0_VALUE, 0, 0L);

        /* compare to last reference point */
        GeoPoint lastGP = Props.getGeoPoint(Props.PROP_ODOMETER_0_GPS, null);
        if ((lastGP != null) && lastGP.isValid()) {
            long minDeltaMeters = Props.getLong(Props.PROP_GPS_DISTANCE_DELTA, 0, 0L);    
            long deltaMeters = (long)newGP.metersToPoint(lastGP);
            if (deltaMeters >= minDeltaMeters) {
                odomMeters = Props.addLong(Props.PROP_ODOMETER_0_VALUE, 0, deltaMeters);
                Props.setGeoPoint(Props.PROP_ODOMETER_0_GPS, newGP);
                if ((this.lastSavedOdomKM > odomMeters) || ((this.lastSavedOdomKM + 50000L) < odomMeters)) {
                    // save if 'lastOdomKM' is invalid (ie. > odomMeters), or if driven at least 50km
                    Props.saveProps();
                    this.lastSavedOdomKM = odomMeters;
                }
                //TODO: may wish to compare against PROP_ODOMETER_0_LIMIT for generating an event
            }
        } else {
            // initialize odometer GPS point
            Props.setLong(Props.PROP_ODOMETER_0_VALUE, 0, odomMeters);
            Props.setGeoPoint(Props.PROP_ODOMETER_0_GPS, newGP);
            Props.saveProps(); // initial save
        }
        
        /* update odometer/distance value in 'newFix' */
        // TODO: this type of update should occur in a central location
        newFix.setDistanceKM((double)odomMeters / 1000.0);
        newFix.setOdometerKM((double)odomMeters / 1000.0);

    }

    // ----------------------------------------------------------------------------
    
    /**
    * Adds a motion event to the queue consisting of a priority, code, and GeoEvent.
    * @param priority The priority associated with the motion event.
    * @param code Represents the motion status.
    * @param newFix The newer GeoEvent.
    */
    private void _queueMotionEvent(int priority, int code, GeoEvent newFix)
    {
        if (newFix != null) {
            newFix.setStatusCode(code);
            this.packetQueue.addEvent(priority, newFix);
        }
    }

}
