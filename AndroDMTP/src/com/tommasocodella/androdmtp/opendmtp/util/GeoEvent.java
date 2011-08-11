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
//  Container for GPS events.
// ----------------------------------------------------------------------------
// Change History:
//  2006/03/26  Martin D. Flynn
//      Initial release
//  2006/11/03  Yoshiaki Iinuma
//      Include JavaDocs
// ----------------------------------------------------------------------------
package com.tommasocodella.androdmtp.opendmtp.util;

/**
* Provides an container for GPS events. keeping track of a piece of GPS information in sequence of
* events of each entity.
*/
public class GeoEvent
{

    // ------------------------------------------------------------------------

    public static final long NMEA0183_GPRMC             = 0x00000001L;
    public static final long NMEA0183_GPGGA             = 0x00000002L;
 
    // ------------------------------------------------------------------------

    private int         gpsStatusCode = 0;
    private long        gpsTimestamp  = 0L;
    private long        gpsIndex      = 0L;
    private double      gpsLatitude   = 0.0;
    private double      gpsLongitude  = 0.0;
    private double      gpsSpeedKPH   = 0.0;
    private double      gpsHeading    = 0.0;
    private double      gpsAltitude   = 0.0;
    private double      gpsDistanceKM = 0.0;
    private double      gpsOdometerKM = 0.0;
    private double      gpsHDOP       = 0.0;
    private double      gpsAccuracyM  = 0.0; // meters
    private long        gpsSequence   = 0L;

    /**
    * Default constructor
    */
    public GeoEvent()
    {
        // nothing to do
    }

    // ------------------------------------------------------------------------

    /**
    * Returns the status code of this GPS event.
    * @return the status code of this GPS event.
    */
    public int getStatusCode()
    {
        return this.gpsStatusCode;
    }

    /**
    * Sets the status code of this GPS event.
    * @param code the status code to set
    */
    public void setStatusCode(int code)
    {
        this.gpsStatusCode = code;
    }

    // ------------------------------------------------------------------------

    /**
    * Returns the value of the timestamp of this GPS event.
    * @return the value of the timestamp of this GPS event.
    */
    public long getTimestamp()
    {
        return this.gpsTimestamp;
    }

    /**
    * Sets the specified time to the timestamp of this GPS event.
    * @param time the timestamp value to be set.
    */
    public void setTimestamp(long time)
    {
        this.gpsTimestamp = time;
    }

    // ------------------------------------------------------------------------

    /**
    * Returns the value of the index of this GPS event.
    * @return the value of the index of this GPS event.
    */
    public long getIndex()
    {
        return this.gpsIndex;
    }

    /**
    * Sets the specified value of the index of this GPS event.
    * @param ndx the index value to be set.
    */
    public void setIndex(long ndx)
    {
        this.gpsIndex = ndx;
    }

    // ------------------------------------------------------------------------

    /**
    * Returns the value of the latitude of this GPS event.
    * @return the value of the latitude of this GPS event.
    */
    public double getLatitude()
    {
        return this.gpsLatitude;
    }

    /**
    * Sets the specified value of the latitude of this GPS event.
    * @param lat the value of latitude to be set.
    */
    public void setLatitude(double lat)
    {
        this.gpsLatitude = lat;
    }

    /**
    * Returns the value of the longitude of this GPS event.
    * @return the value of the longitude of this GPS event.
    */
    public double getLongitude()
    {
        return this.gpsLongitude;
    }

    /**
    * Sets the specified value of the longitude of this GPS event.
    * @param lon the value of longitude to be set.
    */
    public void setLongitude(double lon)
    {
        this.gpsLongitude = lon;
    }

    /**
    * Returns the GeoPoint object containing the information about the position and the time of this
    * GPS event.
    * @return the GeoPoint object containing the information about the position and the time of this
    *         GPS event.
    */
    public GeoPoint getGeoPoint()
    {
        return new GeoPoint(this.getLatitude(), this.getLongitude(), this.getTimestamp());
    }
    
    /**
    * Tests if this GPS event has a valid position value.
    * @return true if this GPS event has a valid position value.
    */
    public boolean isValid()
    {
        double lat = this.getLatitude();
        double lon = this.getLongitude();
        return ((lat != 0.0) || (lon != 0.0));
    }
    
    /** 
    * Sets this point to an invalid location (0.0/0.0)
    */
    public void invalidate()
    {
        this.setLatitude(0.0);
        this.setLongitude(0.0);
    }

    // ------------------------------------------------------------------------

    /**
    * Returns the value of the speed of this GPS event in KPH.
    * @return the value of the speed of this GPS event in KPH.
    */
    public double getSpeedKPH()
    {
        return this.gpsSpeedKPH;
    }

    /**
    * Returns the value of the speed of this GPS event in MPH.
    * @return the value of the speed of this GPS event in MPH.
    */
    public double getSpeedMPH()
    {
        return this.gpsSpeedKPH * GeoPoint.MILES_PER_KILOMETER;
    }

    /**
    * Sets the specified value to the speed of this GPS event in KPH.
    * @param kph the speed to be set.
    */
    public void setSpeedKPH(double kph)
    {
        this.gpsSpeedKPH = kph;
    }
    
    /**
    * Checks to see that the speed represented by this GeoEvent is at least equal-to, or greater-than
    * that of the specified speed.  If less that the specified speed, the internal speed and heading
    * are set to '0'.  This is done to mitigate the effects of having a reported GPS speed of say
    * 10 kph, while the device is sitting motionless in a parked car.
    * @param minSpeedKPH the minimum speed to which the speed of this GPS event is required to reach.
    */
    public void checkMinimumSpeed(double minSpeedKPH)
    {
        if (this.getSpeedKPH() < minSpeedKPH) {
            // the reported speed does not meet the minimum requirement
            // mark the speed as 'not moving'
            this.setSpeedKPH(0.0);
            this.setHeading(0.0);
        }
    }

    // ------------------------------------------------------------------------

    /**
    * Returns the value of the direction of this GPS event (in degrees)
    * @return the value of the direction of this GPS event (in degrees)
    */
    public double getHeading()
    {
        return this.gpsHeading;
    }

    /**
    * Sets the specified value to the direction of this GPS event.
    * @param deg the value of the direction to be set.
    */
    public void setHeading(double deg)
    {
        this.gpsHeading = deg;
    }

    // ------------------------------------------------------------------------

    /**
    * Returns the value of the altitude of this GPS event in meters
    * @return the value of the altitude of this GPS event in meters
    */
    public double getAltitude()
    {
        return this.gpsAltitude; // meters
    }

    /**
    * Returns the value of the altitude of this GPS event in feet.
    * @return the value of the altitude of this GPS event in feet.
    */
    public double getAltitudeFeet()
    {
        return this.gpsAltitude * GeoPoint.FEET_PER_METER; // feet
    }

    /**
    * Sets the specified value to the altitude of this GPS event in meters
    * @param meters the value of the altitude to be set.
    */
    public void setAltitude(double meters)
    {
        this.gpsAltitude = meters;
    }

    // ------------------------------------------------------------------------

    /**
    * Returns the value of the distance of this GPS event in km.
    * @return the value of the distance of this GPS event in km.
    */
    public double getDistanceKM()
    {
        return this.gpsDistanceKM;
    }

    /**
    * Sets the specified value to the distance of this GPS event in km.
    * @param km the value of the distance to be set in km.
    */
    public void setDistanceKM(double km)
    {
        this.gpsDistanceKM = km;
    }

    // ------------------------------------------------------------------------

    /**
    * Returns the value of the odometer of this GPS event in km.
    * @return the value of the odometer of this GPS event in km.
    */
    public double getOdometerKM()
    {
        return this.gpsOdometerKM;
    }

    /**
    * Sets the specified value to the odometer of this GPS event in km.
    * @param km the value of the odometer to be set in km.
    */
    public void setOdometerKM(double km)
    {
        this.gpsOdometerKM = km;
    }

    // ------------------------------------------------------------------------

    /**
    * Return the value of the horizontal dilution of precision of this GPS event.
    * @return the value of the horizontal dilution of precision of this GPS event.
    */
    public double getHDOP()
    {
        return this.gpsHDOP;
    }

    /**
    * Sets the value to the horizontal dilution of precision of this GPS event.
    * @param hdop the value of the horizontal dilution of precision to be set.
    */
    public void setHDOP(double hdop)
    {
        this.gpsHDOP = hdop;
    }

    // ------------------------------------------------------------------------

    /**
    * Return the value of the horizontal accuracy in meters, or 0.0 if the
    * accuracy is not supported or unknown.
    * @return the value of the horizontal accuracy in meters.
    */
    public double getAccuracy()
    {
        return this.getAccuracyMeters();
    }

    /**
    * Return the value of the horizontal accuracy in meters, or 0.0 if the
    * accuracy is not supported or unknown.
    * @return the value of the horizontal accuracy in meters.
    */
    public double getAccuracyMeters()
    {
        return this.gpsAccuracyM;
    }

    /**
    * Returns the value of the altitude of this GPS event in feet.
    * @return the value of the altitude of this GPS event in feet.
    */
    public double getAccuracyFeet()
    {
        if (this.gpsAccuracyM >= 0.0) {
            return this.gpsAccuracyM * GeoPoint.FEET_PER_METER; // feet
        } else {
            return -1.0;
        }
    }

    /**
    * Sets the value of the horizontal accuracy in meters
    * @param accMeters the value of the horizontal accuracy in meters
    */
    public void setAccuracy(double accMeters)
    {
        this.gpsAccuracyM = accMeters;
    }
    
    /** 
    * Return true if this accuracy is acceptable
    * @param minAccuracyM minimum acceptable horzontal accuracy in meters
    * @return true if the accuracy is acceptable, false otherwise
    */
    public boolean isAccuracyOK(double minAccuracyM)
    {
        if (minAccuracyM <= 0.0) {
            // minimum accuracy is not to be considered
            return true;
        } else {
            if (minAccuracyM < 15.0) { minAccuracyM = 15.0; } // absolute minimum
            return (this.getAccuracyMeters() < minAccuracyM);
        }
    }

    // ------------------------------------------------------------------------

    /**
    * Return the value of the sequence of this GPS event.
    * @return the value of the sequence of this GPS event.
    */
    public long getSequence()
    {
        return this.gpsSequence;
    }

    /**
    * Sets the value to the sequence of this GPS event.
    * @param seq the value of the sequence to be set.
    */
    public void setSequence(long seq)
    {
        this.gpsSequence = seq;
    }

    // ------------------------------------------------------------------------

    public double metersToPoint(GeoEvent gev)
    {
        if (gev != null) {
            GeoPoint thisGP  = this.getGeoPoint();
            GeoPoint otherGP = gev.getGeoPoint();
            return thisGP.metersToPoint(otherGP);
        } else {
            return -1.0;
        }
    }
    
    // ------------------------------------------------------------------------

    /**
    * Copy the contents of this GPS event to the other.
    * @param gev the GeoEvent object to which the contents of this GPS event are copied.
    * @return the GeoEvent object to which the contents of this GPS event are copied. If the passed
    *         GeoEvent is null, a new instance is created.
    */
    public GeoEvent copyTo(GeoEvent gev)
    {
        if (gev == null) { gev = new GeoEvent(); }
        gev.setStatusCode(this.getStatusCode());
        gev.setTimestamp (this.getTimestamp());
        gev.setIndex     (this.getIndex());
        gev.setLatitude  (this.getLatitude());
        gev.setLongitude (this.getLongitude());
        gev.setAccuracy  (this.getAccuracy());
        gev.setSpeedKPH  (this.getSpeedKPH());
        gev.setHeading   (this.getHeading());
        gev.setAltitude  (this.getAltitude());
        gev.setDistanceKM(this.getDistanceKM());
        gev.setOdometerKM(this.getOdometerKM());
        gev.setHDOP      (this.getHDOP());
        gev.setSequence  (this.getSequence());
        return gev;
    }

    /**
    * Copy the contents of the specified GPS event to this event
    * @param gev the GeoEvent object from which the contents of this GPS event are copied.
    * @return this GeoEvent object.
    */
    public GeoEvent copyFrom(GeoEvent gev)
    {
        if (gev != null) {
            // copy 'gev' to this event
            gev.copyTo(this);
            return this;
        } else {
            // this GeoEvent is left as-is
            return this;
        }
    }

    // ------------------------------------------------------------------------
    
}
