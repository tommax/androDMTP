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
//  This class handles the client-side connection accounting to make sure that
//  the client adheres to the restrictions that may be imposed by the server.
//  Property values used by the client for determining connection restrictions
//  should match those imposed by the server.
// ----------------------------------------------------------------------------
// Change History:
//  2006/03/26  Martin D. Flynn
//      Initial release
//  2006/11/03  Brandon Lee
//      Include JavaDocs
// ----------------------------------------------------------------------------
package com.tommasocodella.androdmtp.opendmtp.client.base;

import com.tommasocodella.androdmtp.opendmtp.client.custom.Constants;
import com.tommasocodella.androdmtp.opendmtp.util.DateTime;

/**
* Accounts for connection specifications like length, mask size, etc. As well as if it
* were over max time, supports duplex, simplex, etc.
*/
public class Accounting
{

    // ----------------------------------------------------------------------------

    public static final long MAX_CONNECTIONS_PER_HOUR    = 60L;
    public static final int  MAX_MASK_SIZE               = 8; // 8 * 30 minutes == 4 hours

    // ----------------------------------------------------------------------------
    
    private static Accounting duplexAccounting  = null;
    
    /**
    * Returns duplex Accounting.
    * @return duplexAccounting the duplex accounting assigned to instance.
    */
    public static Accounting getDuplexAccounting()
    {
        if (duplexAccounting == null) { duplexAccounting = new Accounting(); }
        return duplexAccounting;
    }

    /**
    *  Marks that a duplex connection has been made.
    *  @return true if it was successful.
    */
    public static boolean markDuplexConnection()
    {
        return getDuplexAccounting().markConnection();
    }

    // ----------------------------------------------------------------------------

    private static Accounting simplexAccounting = null;
    
    /**
    * Returns simplex Accounting.
    * @return simplexAccounting the simplex accounting.
    */
    public static Accounting getSimplexAccounting()
    {
        if (simplexAccounting == null) { simplexAccounting = new Accounting(); }
        return simplexAccounting;
    }
    
    /**
    * Mark that a simplex connection has been made.
    * @return boolean true if successful.
    */
    public static boolean markSimplexConnection()
    {
        return getSimplexAccounting().markConnection();
    }

    // ----------------------------------------------------------------------------
    
    /**
    * Return the time of the last connection time.
    * @return lates time of connection of simplex or duplex.
    */
    public static long getLastConnectionTimer()
    {
        long dupConnTime = getDuplexAccounting()._getLastConnectionTimer();
        long simConnTime = getSimplexAccounting()._getLastConnectionTimer();
        long latest = (dupConnTime > simConnTime)? dupConnTime : simConnTime;
        return latest;
    }

    // ----------------------------------------------------------------------------
    
    /**
    *  Return true if quotas are in effect.
    *  @return true if connections never go over max minutes.
    */
    public static boolean hasQuota()
    {
        long maxMinutes = Props.getLong(Props.PROP_COMM_MAX_CONNECTIONS, 2, 60L);
        return (maxMinutes > 0L)? true : false;
    }
    
    // ----------------------------------------------------------------------------
    
    /**
    * Returns true if we are currently under the total allowed connection count.
    * @return true if under totall quota.
    */
    public static boolean isUnderTotalQuota()
    {
        if (Accounting.hasQuota()) {
    
            /* check total connection limit */
            int maxTotConn = (int)Props.getLong(Props.PROP_COMM_MAX_CONNECTIONS, 0, 1L);
            if (maxTotConn <= 0) { return false; } // NO connections allowed
        
            /* count actual connections and compare to limit */
            int simplexConnCount = getSimplexAccounting().countConnections();
            int duplexConnCount  = getDuplexAccounting().countConnections();
            return ((simplexConnCount + duplexConnCount) < maxTotConn);
            
        } else {
            
            return true;
            
        }
    }
    
    // ----------------------------------------------------------------------------

    /**
    * Return true if we are currently under the number of allowed duplex connections.
    * @return true if currently under allowed duplex connections else false.
    */
    public static boolean isUnderDuplexQuota()
    {
        if (!Accounting.supportsDuplex()) {
            
            /* never under duplex quota if duplex connections aren't supported */
            return false;
            
        } else
        if (Accounting.hasQuota()) {
            
            /* check total connection limit */
            int maxTotConn = (int)Props.getLong(Props.PROP_COMM_MAX_CONNECTIONS, 0, 1L); // Simplex
            if (maxTotConn == 0) { return false; } // NO connections allowed
        
            /* check Duplex connection limit */
            int maxDuplexConn = (int)Props.getLong(Props.PROP_COMM_MAX_CONNECTIONS, 1, 1L); // Duplex
            if (maxDuplexConn == 0) { return false; } // NO Duplex connections allowed
            if (maxDuplexConn > maxTotConn) { maxDuplexConn = maxTotConn; } // limit Duplex conn to total
        
            /* count actual connections and compare to limit */
            int duplexConnCount = getDuplexAccounting().countConnections();
            return (duplexConnCount < maxDuplexConn);
        
        } else {
            
            return true;
            
        }
    }

    // ----------------------------------------------------------------------------
    
    /**
    * Return true if the device is allowed to make duplex connections to the server.
    * @return returns true if device supports duplex.
    */
    public static boolean supportsDuplex()
    {
        int maxEvents = (int)Props.getLong(Props.PROP_COMM_MAX_DUP_EVENTS, 0, 1L);
        if (maxEvents > 0) {
            int maxDuplexConn = (int)Props.getLong(Props.PROP_COMM_MAX_CONNECTIONS, 1, 1L); // Duplex
            return (maxDuplexConn > 0)? true : false;
        } else {
            return false;
        }
    }

    /**
    * Return true if the device is allowed to make simplex connections to the server.
    * @return returns true if device supports simplex connections.
    */
    public static boolean supportsSimplex()
    {
        int maxEvents = (int)Props.getLong(Props.PROP_COMM_MAX_SIM_EVENTS, 0, 1L);
        if (maxEvents > 0) {
            int maxTotalConn  = (int)Props.getLong(Props.PROP_COMM_MAX_CONNECTIONS, 0, 1L); // Total
            int maxDuplexConn = (int)Props.getLong(Props.PROP_COMM_MAX_CONNECTIONS, 1, 1L); // Duplex
            return (maxTotalConn > maxDuplexConn)? true : false;
        } else {
            return false;
        }
    }

    // ----------------------------------------------------------------------------
    
    /**
    * Return true if minimum time between connections has expired.
    * @return returns true if connections have expired.
    */
    public static boolean absoluteDelayExpired()
    {
        long lastConnTimer = Accounting.getLastConnectionTimer();
        long minXmitDelay  = Props.getLong(Props.PROP_COMM_MIN_XMIT_DELAY, 0, DateTime.MinuteSeconds(30));
        if (minXmitDelay < Constants.MIN_XMIT_DELAY) { minXmitDelay = Constants.MIN_XMIT_DELAY; }
        boolean timerExp = DateTime.isTimerExpired(lastConnTimer, minXmitDelay);
        return timerExp;
    }

    /**
    * Return true if the minimum time between connections has expired.
    * Appears to be very simular to absoluteDelayExpired method.
    * @return returns true if min interval has expired.
    */
    public static boolean minIntervalExpired()
    {
        long lastConnTimer = Accounting.getLastConnectionTimer();
        long minXmitInterval = Props.getLong(Props.PROP_COMM_MIN_XMIT_RATE, 0, DateTime.HourSeconds(2));
        if (minXmitInterval < Constants.MIN_XMIT_RATE) { minXmitInterval = Constants.MIN_XMIT_RATE; }
        boolean timerExp = DateTime.isTimerExpired(lastConnTimer, minXmitInterval);
        return timerExp;
    }

    /**
    * Return true if the maximum connection interval has expired. (ie the maximum amount of time
    * elapsing with having made a connectgion to the server)
    * @return true if interval has expired.
    */
    public static boolean maxIntervalExpired()
    {
        // This function specifically checks the last time we've made a Duplex connection
        long lastConnTimer = getDuplexAccounting()._getLastConnectionTimer();
        long maxXmitInterval = Props.getLong(Props.PROP_COMM_MAX_XMIT_RATE, 0, DateTime.HourSeconds(24));
        boolean timerExp = DateTime.isTimerExpired(lastConnTimer, maxXmitInterval);
        return timerExp;
    }

    // ----------------------------------------------------------------------------
    // ----------------------------------------------------------------------------
    // ----------------------------------------------------------------------------

    private long     shiftTime      = 0L;
    private long     lastConnTimer  = 0L;
    private long     mask[]         = null; 

    // ----------------------------------------------------------------------------
    
    /**
    * Default constructor
    */
    public Accounting() 
    {
        this.shiftTime     = 0L;
        this.lastConnTimer = 0L;
        this.mask          = new long[MAX_MASK_SIZE];   // 4 hour max
    }

    // ----------------------------------------------------------------------------
    
    /**
    * Return the length of the connection time accounting mask in 30 minute intervals. ie. a
    * return value of 6 indicates 6-30 minute intervals, or a total of 3 hours.
    * @return newMaskLen the mask length.
    */
    public int getMaskLen()
    {
        /* return the length of the connection time accounting mask in 30 minute
        ** intervals.  ie. a return value of 6 indicates 6-30 minute intervals, or
        ** a total of 3 hours */
        int newMaskLen = 2;
        int maxMinutes = (int)Props.getLong(Props.PROP_COMM_MAX_CONNECTIONS, 2, 60L);
        int len = (maxMinutes + 15) / 30; // round to nearest half-hour
        if (len < 1) {
            newMaskLen = 1;
        } else
        if (len > MAX_MASK_SIZE) {
            newMaskLen = MAX_MASK_SIZE;
        } else {
            newMaskLen = len;
        }
        return newMaskLen;
    }

    // ----------------------------------------------------------------------------
    
    /**
    * Does calculations for shiftTime, assigns then calls shiftMinutes with minutes.
    * @return int the shifted minutes.
    */
    public int shift() 
    {
   
        /* check elapsed time */
        long nowTime   = DateTime.getCurrentTimeSec();
        long deltaTime = nowTime  - this.shiftTime;
        long minutes   = deltaTime / 60L;
        if (minutes <= 0L) {
            // not enough time has passed since last shift
            return 0;
        }
        this.shiftTime = nowTime - (deltaTime % 60L);
        
        /* shift */
        return this.shiftMinutes(minutes);
        
    }
    
    /**
    * Returns the shifted minutes if enough time has passed. Clears mask if max time passed.
    * @param minutes a long of minutes passed.
    * @return integer with shift.
    */
    private int shiftMinutes(long minutes)
    {
    
        /* has enough time elapsed? */
        if (minutes <= 0L) {
            // nothing to shift
            return 0;
        }

        /* maximum possible minutes have elapsed */
        int maskLen = this.getMaskLen();
        if (minutes >= ((long)maskLen * 30L)) {
            // enough time has passed to reset everything
            this.clearMask();
            return maskLen;
        }
    
        /* shift */
        while (minutes > 0L) {
            long maxMin = (minutes <= 30L)? minutes : 30L; // shift a maximum of 30 minutes at a time
            long carry = 0L;
            for (int i = 0; i < maskLen; i++) {
                long c = (this.mask[i] >> (30L - maxMin)) & ((1L << maxMin) - 1L);
                this.mask[i] = ((this.mask[i] << maxMin) | carry) & 0x3FFFFFFFL;
                carry = c;
            }
            minutes -= maxMin;
        }
        return maskLen; // did shift

    }

    // ----------------------------------------------------------------------------

    /**
    * Returns the last connection timer
    * @return the last connection timer.
    */
    private long _getLastConnectionTimer()
    {
        return this.lastConnTimer;
    }
    
    /**
    * Mark that a connection has occured in the mask.
    * @return boolean true if successful.
    */
    private boolean markConnection()
    {
        
        /* save last connection time */
        this.lastConnTimer = DateTime.getTimerSec();
        
        /* mark this connection in the mask */
        this.shift();
        
        // we assume here that 'maskLen' is at-least '1'
        if ((this.mask[0] & 1L) != 0L) {
            // Already set.
            // This indicates that more than one transmission has occurred in the same minute.
            // The absolute minimum time delay is apparently not working.
            return false;
        } else {
            this.mask[0] |= 1L;
            return true;
        }
        
    }

    // ----------------------------------------------------------------------------
    
    /**
    * Return the number of connections made in the masked time interval .
    * @return count then number of connections.
    */
    public int countConnections()
    {
        int i, count = 0;
        int maskLen = this.shift();
        for (i = 0; i < maskLen; i++) {
            long bc = this.mask[i];
            bc -= (bc & 0xAAAAAAAAL) >> 1; // <-- the counting ocurs here, the rest is addition
            bc  = (bc & 0x33333333L) + ((bc >> 2) & 0x33333333L); // add with carry
            bc  = (bc + (bc >>  4)) & 0x0F0F0F0FL; // add, no carry
            bc  = (bc + (bc >>  8)) & 0x00FF00FFL; // */ bc += bc >>  8;  // add, no carry
            bc  = (bc + (bc >> 16)) & 0x000000FFL; // */ bc += bc >> 16;  // add, no carry
            count += bc & 0xFF;
        }
        return count;
    }

    // ----------------------------------------------------------------------------

    /**
    * Clears Mask.
    */
    public void clearMask() 
    {
        for (int i = 0; i < this.mask.length; i++) {
            this.mask[i] = 0L;
        }
    }

    // ----------------------------------------------------------------------------
   
}
