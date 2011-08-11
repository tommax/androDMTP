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
//  Date/Time utilities.
// ----------------------------------------------------------------------------
// Change History:
//  2006/03/26  Martin D. Flynn
//      Initial release
//  2006/11/03  Mark Stillwell
//      Include JavaDocs
// ----------------------------------------------------------------------------
package com.tommasocodella.androdmtp.opendmtp.util;

import java.util.Date;
import java.util.Calendar;

/**
* Represents a particular moment in time and provides various date/time and timer utility methods.
*/
public class DateTime
{
     
    // ------------------------------------------------------------------------
    
    /** The number of hours in one day. */
    public static final long HOURS_PER_DAY      = 24L;

    /** The number of seconds in one minute. */
    public static final long SECONDS_PER_MINUTE = 60L;

    /** The number of minutes in one hour. */
    public static final long MINUTES_PER_HOUR   = 60L;

    /** The number of days in one week. */
    public static final long DAYS_PER_WEEK      = 7L;

    /** The number of seconds in one hour. */
    public static final long SECONDS_PER_HOUR   = SECONDS_PER_MINUTE * MINUTES_PER_HOUR;

    /** The number of minutes in one day. */
    public static final long MINUTES_PER_DAY    = HOURS_PER_DAY * MINUTES_PER_HOUR;

    /** The number of seconds in one day. */
    public static final long SECONDS_PER_DAY    = MINUTES_PER_DAY * SECONDS_PER_MINUTE;

    /** The number of minutes in one week. */
    public static final long MINUTES_PER_WEEK   = DAYS_PER_WEEK * MINUTES_PER_DAY;

    /**
    * Returns the number of seconds in the given integer number of days.
    * @param days an integer number of days
    * @return the number of seconds in the given number of days
    */
    public static long DaySeconds(long days)
    {
        return days * SECONDS_PER_DAY;
    }

    /**
    * Returns the number of seconds in the given number of days.
    * @param days a number of days
    * @return the number of seconds in the given number of days
    */
    public static long DaySeconds(double days)
    {
        return (long)((days * (double)SECONDS_PER_DAY) + 0.5);
    }
    
    /**
    * Returns the number of seconds in the given integer number of hours.
    * @param hours an integer number of hours
    * @return the number of seconds in the given number of hours
    */
    public static long HourSeconds(long hours)
    {
        return hours * SECONDS_PER_HOUR;
    }
    
    /**
    * Returns the number of seconds in the given integer number of minutes.
    * @param minutes an integer number of minutes
    * @return the number of seconds in the given number of minutes
    */
    public static long MinuteSeconds(long minutes)
    {
        return minutes * SECONDS_PER_MINUTE;
    }
    
    // ------------------------------------------------------------------------

    /**
    * Returns a string representing the date derived from adding the given number of seconds to the
    * epoch (Jan 1, 1970, midnight GMT).
    * @param timeSec number of seconds past the epoch
    * @return a string representing the date derived by adding timeSec seconds to the epoch
    */
    public static String toString(long timeSec)
    {
        return (new java.util.Date(timeSec * 1000L)).toString();
    }

    /**
    * Returns current number of seconds past the epoch (Jan 1, 1970, midnight GMT).
    * @return current number of seconds past the epoch
    */
    public static long getCurrentTimeSec()
    {
        // Number of seconds since the 'epoch' January 1, 1970, 00:00:00 GMT
        return getCurrentTimeMillis() / 1000L;
    }

    /**
    * Returns current number of milliseconds past the epoch (Jan 1, 1970, midnight GMT).
    * @return current number of milliseconds past the epoch
    */
    public static long getCurrentTimeMillis()
    {
        // Number of milliseconds since the 'epoch' January 1, 1970, 00:00:00 GMT
        return System.currentTimeMillis();
    }

    // ------------------------------------------------------------------------

    private long     timeMillis = 0L; // ms since January 1, 1970, 00:00:00 GMT

    /**
    * Default constructor. Initializes the object to represent the current moment in time.
    */
    public DateTime()
    {
        this.setTimeMillis(getCurrentTimeMillis());
    }
 
    /**
    * Creates a new DateTime object initialized to a moment in time determined by adding the given
    * number of seconds to the epoch (Jan 1, 1970, midnight GMT).
    * @param timeSec number of seconds past the epoch
    */
    public DateTime(long timeSec)
    {
        this.setTimeSec(timeSec);
    }
    
    /**
    * Creates a new DateTime object initialized to the same moment in time as the given object.
    * @param dt a DateTime object to use to initialize a new DateTime object
    */
    public DateTime(DateTime dt)
    {
        this.timeMillis = dt.timeMillis;
    }
    
    // ------------------------------------------------------------------------

    /**
    * Returns a Date initialized to the same Date and Time as this DateTime object.
    * @return a Date object set to the same Date and Time
    */
    public Date getDate()
    {
        return new Date(this.getTimeMillis());
    }

    /**
    * Returns a Calendar initialized to the same Date and Time as this DateTime object.
    * @return a Calendar object set to the same Date and Time
    */
    public Calendar getCalendar()
    {
        Calendar cal = Calendar.getInstance();
        cal.setTime(this.getDate());
        return cal;
    }
    
    // ------------------------------------------------------------------------

    /**
    * Returns the moment in time represented by this object as the number of seconds past the epoch
    * (Jan 1, 1970 midnight GMT).
    * @return the moment in time represented by this object as seconds past the epoch
    */
    public long getTimeSec()
    {
        return this.getTimeMillis() / 1000L;
    }

    /**
    * Sets the moment in time represented by this object to the given number of seconds past the
    * epoch (Jan 1, 1970 midnight GMT).
    * @param timeSec a moment in time represented by the number of seconds past the epoch
    */
    public void setTimeSec(long timeSec)
    {
        this.timeMillis = timeSec * 1000L;
    }

    // ------------------------------------------------------------------------

    /**
    * Returns the moment in time represented by this object as the number of milliseconds past the
    * epoch (Jan 1, 1970 midnight GMT).
    * @return the moment in time represented by this object as milliseconds past the epoch
    */
    public long getTimeMillis()
    {
        return this.timeMillis;
    }

    /**
    * Sets the moment in time represented by this object to the given number of milliseconds past the
    * epoch (Jan 1, 1970 midnight GMT).
    * @param timeMillis a moment in time represented by the number of milliseconds past the epoch
    */
    public void setTimeMillis(long timeMillis)
    {
        this.timeMillis = timeMillis;
    }
    
    // ------------------------------------------------------------------------
    
    /**
    * Tests the given object to see if it equals this one.  Equality is defined to mean that the
    * given object is another instance of the DateTime class, set to the same moment in time.
    * @param obj an object to compare with this one
    * @return true if the object equals this one
    */
    public boolean equals(Object obj) 
    {
        if (obj instanceof DateTime) {
            return (this.getTimeMillis() == ((DateTime)obj).getTimeMillis());
        } else {
            return false;
        }
    }
                    
    // ------------------------------------------------------------------------
    
    private static long timerBase = 0L;
    
    /**
    * Sets the timer base to the current time.
    */
    public static void markTimerBase()
    {
        // this could set time startup time to '0' (or near zero) if the system
        // clock hasn't been updated by GPS yet.
        DateTime.timerBase = DateTime.getCurrentTimeSec();
        // We back up one second so that all timer initialized right at the startup of the
        // program will not be zero.
        if (DateTime.timerBase > 0L) { DateTime.timerBase--; }
    }
    
    /**
    * Gets the value of timerBase.
    * @return the value of timerBase
    */
    public static long getTimerBase()
    {
        return DateTime.timerBase;
    }
    
    /**
    * Returns the number of seconds the given value is past when the timer was set.
    * @param timeSec a moment in time represented as seconds past the epoch
    * @return the number of seconds the given time is past when the timer was started
    */
    public static long getTimerSec(long timeSec)
    {
        return (timeSec - DateTime.getTimerBase());
    }

    /**
    * Returns the number of seconds since the timer was set.
    * @return the number of seconds since the timer was set
    */
    public static long getTimerSec()
    {
        return DateTime.getTimerSec(DateTime.getCurrentTimeSec());
    }
    
    /**
    * Returns the result of adding the given number of seconds to the timer base.
    * @param timer a number of seconds
    * @return the result of adding the given number of seconds to the timer base
    */
    public static long getTimeFromTimer(long timer)
    {
        return timer + DateTime.getTimerBase();
    }
    
    /**
    * Returns true if the sum of the operands is less than the number of seconds since the timer was
    * set, or if either of the parameters are less than or equal to zero, false otherwise.
    * @param timerSec a number of seconds
    * @param intervalSec a number of seconds
    * @return true if timersec+intervalSec < current time - timerBase, false otherwise
    */
    public static boolean isTimerExpired(long timerSec, long intervalSec)
    {
    
        /* return true if the timer has not yet been initialized */
        if (timerSec <= 0L) {
            return true;
        }
    
        /* return true if there is no timeout interval */
        if (intervalSec <= 0L) {
            return true;
        }
    
        /* return true if 'intervalSec' has passed */
        long baseTimeSec = DateTime.getTimerSec();
        if ((timerSec + intervalSec) < baseTimeSec) {
            return true;
        }

        /* timer has not expired */
        return false;
        
    }
    
    // ------------------------------------------------------------------------

    /**
    * Returns a string representing the time (but not date) represented by this object.
    * @return a string representing the time (but not date) represented by this object.
    */
    public String getTimeString()
    {
        Calendar cal = this.getCalendar();
        StringBuffer sb = new StringBuffer();
        sb.append(String.valueOf(cal.get(Calendar.HOUR_OF_DAY) + 100).substring(1));
        sb.append(":");
        sb.append(String.valueOf(cal.get(Calendar.MINUTE)      + 100).substring(1));
        sb.append(":");
        sb.append(String.valueOf(cal.get(Calendar.SECOND)      + 100).substring(1));
        return sb.toString();
    }
    
    /**
    * Returns a string representing this DateTime object.
    * @return a String representing the time represented by this object
    */
    public String toString() 
    {
        return this.getDate().toString();
    }
    
}
