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
//  This class provides GPS-based checking for generation of motion events,
//  such as start, stop, in-motion, dormant, and excessive speed events.
// ----------------------------------------------------------------------------
// Change History:
//  2006/03/26  Martin D. Flynn
//      Initial release
//  2006/11/03  Robert Puckett
//      Include JavaDocs
// ----------------------------------------------------------------------------
package com.tommasocodella.androdmtp.opendmtp.client.modules;

import com.tommasocodella.androdmtp.opendmtp.client.base.Packet;
import com.tommasocodella.androdmtp.opendmtp.client.base.PacketQueue;
import com.tommasocodella.androdmtp.opendmtp.client.base.Props;
import com.tommasocodella.androdmtp.opendmtp.client.custom.Constants;
import com.tommasocodella.androdmtp.opendmtp.codes.StatusCodes;
import com.tommasocodella.androdmtp.opendmtp.util.DateTime;
import com.tommasocodella.androdmtp.opendmtp.util.GeoEvent;
import com.tommasocodella.androdmtp.opendmtp.client.base.GPSModules;

/**
* Provides GPS-based checking for generation of motion events, such as start, stop, in-motion,
* dormant, and excessive speed events.
*/
public class MotionModule
    implements StatusCodes, GPSModules.Module
{

    // ----------------------------------------------------------------------------
    
    private static final int MOTION_START_PRIORITY      = Packet.PRIORITY_NORMAL;
    private static final int MOTION_STOP_PRIORITY       = Packet.PRIORITY_NORMAL;
    private static final int IN_MOTION_PRIORITY         = Packet.PRIORITY_LOW;
    private static final int DORMANT_PRIORITY           = Packet.PRIORITY_LOW;
    private static final int EXCESS_SPEED_PRIORITY      = Packet.PRIORITY_NORMAL;

    // ------------------------------------------------------------------------

    /**
    * Contains a queue for events where each event includes a priority and GeoEvent.
    */
    private PacketQueue     packetQueue                 = null;
    
    /**
    * Contains the last GeoEvent where motion was detected.
    */
    private GeoEvent        lastMotionFix               = new GeoEvent(); 

    /**
    * Contains the last GeoEvent where the device was stopped.
    */
    private GeoEvent        lastStoppedFix              = new GeoEvent();
    
    /**
    * Contains a boolean flag to indicate if the device is already in motion.
    */
    private boolean         isInMotion                  = false;

    /**
    * Contains a boolean flag to indicate if the speed is above a given threshold specified in
    * property PROP_MOTION_EXCESS_SPEED.
    */
    private boolean         isExceedingSpeed            = false;
    
    /**
    * Contains a timer value used to detect a stopped motion event.
    */
    private long            lastStoppedTimer            = 0L;
    
    /**
    * Contains a timer value used to detect when a new in motion event can be added to the queue.
    */
    private long            lastInMotionMessageTimer    = 0L;
    
    /**
    * Contains a timer value used to detect when a new dormant motion event can be added to the
    * queue.
    */
    private long            lastDormantMessageTimer     = 0L;

    /**
    * Contains a count of the number of sequential dormant motion events sent to the queue.
    */
    private long            dormantCount                = 0L;

    /**
    * Constructs a MotionModule instance with the passed packet queue.
    * @param queue A motion event queue.
    */
    public MotionModule(PacketQueue queue)
    {
        this.packetQueue = queue; // must not be null
    }
    
    // ------------------------------------------------------------------------

    /**
    * Compares the two GeoEvent parameters, identifies motion events, and sets member variables
    * accordingly.
    * @param oldFix The previous GeoEvent
    * @param newFix The new GeoEvent
    */
    public void checkGPS(GeoEvent oldFix, GeoEvent newFix)
    {
        // Note: 'newFix' is the same GeoEvent object that is sent for every 'checkGPS'
        // invocation.  It is just updated with the current GPS information before
        // each call to this method.  As a result, do not expect that you can store
        // the state of this fix simply by assigning this value to another variable.
        // Instead, you will need to use "newFix.copyTo(savedGPS)" in order to save
        // the state of this gps fix.

        // 'newFix' will contain a valid fix, but verify anyway
        if ((newFix == null) || !newFix.isValid()) {
            return;
        }

        /* check motion start/stop */
        //   - send start/stop/in-motion event
        boolean isCurrentlyMoving = false;
        double defMotionStart = Props.getDouble(Props.PROP_MOTION_START, 0, 0.0); // kph/meters
        // Props.PROP_GPS_MIN_SPEED should already be accounted for
        if (defMotionStart > 0.0) {
            // start/in-motion/stop is in effect

            /* first fix */
            if ((this.lastMotionFix == null) || !this.lastMotionFix.isValid()) {
                // first initialization of the 'last' motion fix
                newFix.copyTo(this.lastMotionFix);
            }
            // 'this.lastMotionFix' contains a valid fix at this point

            /* currently moving? */
            isCurrentlyMoving = _checkIsMoving(this.lastMotionFix, newFix, defMotionStart, this.isInMotion);
            if (isCurrentlyMoving) {
                // I am moving
                this.lastStoppedTimer = 0L; // reset stopped time
                this.lastStoppedFix.invalidate();
                newFix.copyTo(this.lastMotionFix); // update:
                if (!this.isInMotion) {
                    // I wasn't moving before, but now I am
                    this.isInMotion = true;
                    this.lastInMotionMessageTimer = DateTime.getTimerSec(); // start "in-motion" timer
                    // send 'start' event
                    this._queueMotionEvent(MOTION_START_PRIORITY, STATUS_MOTION_START, newFix);
                } else {
                    // continued in-motion
                }
            } else {
                // no longer moving
                if (this.isInMotion) {
                    // I was moving, but I'm not anymore
                    if (this.lastStoppedTimer <= 0L) {
                        // start "stopped" timer (first non-moving sample)
                        this.lastStoppedTimer = DateTime.getTimerSec();
                        newFix.copyTo(this.lastStoppedFix);
                        // this will be reset again if we start moving before time expires
                    } else {
                        int defMotionStop = (int)Props.getLong(Props.PROP_MOTION_STOP, 0, 0L); // seconds
                        if (DateTime.isTimerExpired(this.lastStoppedTimer,defMotionStop)) {
                            // time expired, we are now officially NOT moving
                            this.isInMotion = false;
                            newFix.copyTo(this.lastMotionFix); // update:
                            // send 'stop' event
                            int defStopType = (int)Props.getLong(Props.PROP_MOTION_STOP_TYPE, 0, 0L); // 0=after_delay, 1=when_stopped
                            if (defStopType == 0) {
                                this._queueMotionEvent(MOTION_STOP_PRIORITY, STATUS_MOTION_STOP, newFix);
                            } else {
                                this._queueMotionEvent(MOTION_STOP_PRIORITY, STATUS_MOTION_STOP, this.lastStoppedFix);
                            }
                            this.lastStoppedTimer = 0L;
                            this.lastStoppedFix.invalidate();
                        }
                    }
                } else {
                    // still not moving 
                }
            }
            
        } else {
            // this is necessary in case "start/stop" events were turned off while moving
            this.isInMotion = false;
        }
    
        /* check in-motion */
        //   - send "in-motion" events while moving (between start/stop events)
        //   - send "dormant" events while not moving
        if (this.isInMotion) {
            // moving (between start/stop) ['isCurrentlyMoving' may be false]
            long defMotionInterval = Props.getLong(Props.PROP_MOTION_IN_MOTION, 0, 0L);
            if (defMotionInterval > 0L) {
                // In-motion interval has been defined.  We want in-motion events.
                if (defMotionInterval < Constants.MIN_IN_MOTION_INTERVAL) {
                    // in-motion interval is too small, reset to minimum value
                    defMotionInterval = Constants.MIN_IN_MOTION_INTERVAL;
                    Props.setLong(Props.PROP_MOTION_IN_MOTION, 0, defMotionInterval);
                }
                int defStopType = (int)Props.getLong(Props.PROP_MOTION_STOP_TYPE, 0, 0L); // 0=after_delay, 1=when_stopped
                if ((defStopType != 0) && !isCurrentlyMoving) {
                    // 'defStopType' states that in-motion messages are to be generated iff actually moving, 
                    // and were not currently moving.
                } else
                if (DateTime.isTimerExpired(this.lastInMotionMessageTimer,defMotionInterval)) {
                    // we're moving, and the in-motion interval has expired
                    this.lastInMotionMessageTimer = DateTime.getTimerSec();
                    // send in-motion message
                    this._queueMotionEvent(IN_MOTION_PRIORITY, STATUS_MOTION_IN_MOTION, newFix);
                }
            }
            this.lastDormantMessageTimer = 0L;
            this.dormantCount = 0L;
        } else {
            // not moving (outside start/stop)
            // if Props.PROP_MOTION_START is '0', then 'dormant' will always be in effect.
            long defDormantInterval = Props.getLong(Props.PROP_MOTION_DORMANT_INTRVL, 0, 0L);
            if (defDormantInterval > 0L) {
                if (defDormantInterval < Constants.MIN_DORMANT_INTERVAL) { 
                    defDormantInterval = Constants.MIN_DORMANT_INTERVAL;
                    Props.setLong(Props.PROP_MOTION_DORMANT_INTRVL, 0, defDormantInterval);
                }
                long maxDormantCount = Props.getLong(Props.PROP_MOTION_DORMANT_COUNT, 0, 0L);
                if ((maxDormantCount <= 0L) || (this.dormantCount < maxDormantCount)) {
                    if (this.lastDormantMessageTimer <= 0L) {
                        // initialize dormant timer
                        this.lastDormantMessageTimer = DateTime.getTimerSec();
                        this.dormantCount = 0L;
                    } else
                    if (DateTime.isTimerExpired(this.lastDormantMessageTimer,defDormantInterval)) {
                        this.lastDormantMessageTimer = DateTime.getTimerSec();
                        // send dormant message
                        this._queueMotionEvent(DORMANT_PRIORITY, STATUS_MOTION_DORMANT, newFix);
                        this.dormantCount++;
                    }
                }
            }
        }
        
        /* check excessive speed */
        double defMaxSpeed = Props.getDouble(Props.PROP_MOTION_EXCESS_SPEED, 0, 0.0); // kph
        if (defMaxSpeed > 0.0) {
            // maxSpeed is defined
            boolean isCurrentlyExceedingSpeed = (newFix.getSpeedKPH() >= defMaxSpeed)? true : false;
            if (isCurrentlyExceedingSpeed) {
                // I'm currently exceeding maxSpeed
                if (!this.isExceedingSpeed) {
                    // I wasn't exceeding maxSpeed before, but now I am
                    this.isExceedingSpeed = true;
                    this._queueMotionEvent(EXCESS_SPEED_PRIORITY, STATUS_MOTION_EXCESS_SPEED, newFix);
                } else {
                    // I'm still exceeding maxSpeed
                }
            } else {
                // I'm currently not exceeding maxSpeed
                if (this.isExceedingSpeed) {
                    // I was exceeding maxSpeed before, but now I'm not
                    this.isExceedingSpeed = false;
                } else {
                    // I'm still not exceeding maxSpeed
                }
            }
        } else {
            // this is necessary in case "speeding" events were turned off while speeding
            this.isExceedingSpeed = false;
        }
    }

    // ----------------------------------------------------------------------------

    /**
    * Checks if the GeoEvents indicate movement.
    * @param lastFix The last GeoEvent.
    * @param newFix The new GeoEvent.
    * @param defMotionStart Threshold value for determining whether an object is moving or not. If
    *        property PROP_MOTION_START_TYPE is 0, then defMotionStart is kph speed value. If
    *        property PROP_MOTION_START_TYPE is 1, then defMotionStart is meters distance value.
    * @return True if the GPS data indicates that the device is moving, false otherwise.
    */
    private boolean _checkIsMoving(GeoEvent lastFix, GeoEvent newFix, 
        double defMotionStart, boolean isInMotion)
    {
        // 'lastFix' and 'newFix' will always contain a valid fix
        int defStartType = (int)Props.getLong(Props.PROP_MOTION_START_TYPE, 0, 0L); // 0=kph, 1=meters
        // defStartType:
        //   0 - check speed (kph)
        //   1 - check distance (meters)
        
        /* lastFix & newFix have already been tested for validity, but retest anyway. */
        if ((newFix == null) || !newFix.isValid()) {
            // this will never occur
            return false;
        }
        
        switch (defStartType) {
            case 0 : // try a simple speed check
                if (newFix.getSpeedKPH() >= defMotionStart) {
                    return true;
                }
                break;
            case 1 : // distance check
            default: {
                if ((lastFix == null) || !lastFix.isValid()) { // re-validate
                    // this will never occur
                    return true;
                } else
                if (newFix.metersToPoint(lastFix) >= defMotionStart) {
                    if (!isInMotion && !newFix.isAccuracyOK(defMotionStart)) {
                        // we weren't previously moving, so this motion would trigger a start
                        // unacceptable accuracy to trigger a start
                        return false;
                    } else {
                        return true;
                    }
                }
            } break;
        }
            
        /* not moving */
        return false;
        
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
        newFix.setStatusCode(code);
        this.packetQueue.addEvent(priority, newFix);
    }

}
