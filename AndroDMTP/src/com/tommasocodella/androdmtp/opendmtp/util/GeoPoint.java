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
//  Container for a GPS location (latitude/longitude), as well as support for
//  distance calculations between points.
// ----------------------------------------------------------------------------
// Change History:
//  2006/03/26  Martin D. Flynn
//     -Initial release
//  2006/11/03  Nam Nguyen
//     -Include JavaDocs
//  2007/01/28  Martin D. Flynn
//     -Changed 'toString()' format to "<fixtime>,<latitude>,<longitude>" to 
//      comply with the protocol. (format was "<latitude>/<longitude>")
//  2007/??/??  Martin D. Flynn
//     -Added new latitude/longitude format methods to allow specifying the number
//      of desired decimal points in the resulting string.
// ----------------------------------------------------------------------------
package com.tommasocodella.androdmtp.opendmtp.util;


/**
* Contains information for a GPS location and support for calculating distance between points.
*/
public class GeoPoint
{

    // ------------------------------------------------------------------------

    private static final String LOG_NAME  = "GPT";

    // ------------------------------------------------------------------------

    /**
    * 'true' if the distance calculation is to use the 'Haversine' formula
    */
    private static boolean UseHaversineDistanceFormula = true;

    // ------------------------------------------------------------------------
    
    /**
    * contains a very small value represent acceptable error
    */
    protected static final double EPSILON           = 1.0E-7; 

    /**
    * The maximum acceptable latitude.
    */
    public  static final double MAX_LATITUDE        = 90.0;

    /**
    * The minimum acceptable latitude.
    */
    public  static final double MIN_LATITUDE        = -90.0;
    
    /**
    * The maximum acceptable longitude.
    */
    public  static final double MAX_LONGITUDE       = 180.0;

    /**
    * The minimum acceptable longitude.
    */
    public  static final double MIN_LONGITUDE       = -180.0;

    /**
    * The 'toString' field separator symbol
    */
    public  static final String PointSeparator      = ",";
    
    // ------------------------------------------------------------------------
    // References:
    //   http://www.jqjacobs.net/astro/geodesy.html
    //   http://www.boeing-727.com/Data/fly%20odds/distance.html
    //   http://mathforum.org/library/drmath/view/51785.html
    //   http://mathforum.org/library/drmath/view/52070.html
    
    /**
    * Holds value Math.PI.
    */
    public  static final double PI                           = Math.PI;

    /**
    * Holds value PI / 2.0.
    */
    public  static final double PI_div2                      = PI / 2.0;

    /**
    * Holds value PI / 4.0.
    */
    public  static final double PI_div4                      = PI / 4.0;

    /**
    * Represents the number of radians in a circle. Holds value PI / 180.0.
    */
    public  static final double RADIANS                      = PI / 180.0;

    /**
    * Earth's equatorial radius in kilometers.
    */
    public  static final double EARTH_EQUATORIAL_RADIUS_KM   = 6378.1370;   // Km: a

    /**
    * Earth's polar radius in kilometers.
    */
    public  static final double EARTH_POLAR_RADIUS_KM        = 6356.752314; // Km: b

    /**
    * Earth's mean radius in kilometers.
    */
    public  static final double EARTH_MEAN_RADIUS_KM         = 6371.0088;   // Km: (2a + b)/3 
    
    /**
    * The number of feet in one mile.
    */
    public  static final double FEET_PER_MILE                = 5280.0;

    /**
    * The number of miles in one kilometer.
    */
    public  static final double MILES_PER_KILOMETER          = 0.621371192;

    /**
    * The number of kilometers in one mile.
    */
    public  static final double KILOMETERS_PER_MILE          = 1.0 / MILES_PER_KILOMETER; // 1.609344

    /**
    * The number of meters in one foot.
    */
    public  static final double METERS_PER_FOOT              = 0.3048;

    /**
    * The number of feet in one meter.
    */
    public  static final double FEET_PER_METER               = 1.0 / METERS_PER_FOOT;     // 3.280839895;

    /**
    * The number of feet in one kilometer.
    */
    public  static final double FEET_PER_KILOMETER           = FEET_PER_METER * 1000.0;   // 3280.84

    /**
    * The number of nautical miles in one kilometer.
    */
    public  static final double NAUTICAL_MILES_PER_KILOMETER = 0.539956803;

    /**
    * The number of kilometers in one nautical mile.
    */
    public  static final double KILOMETERS_PER_NAUTICAL_MILE = 1.0 / NAUTICAL_MILES_PER_KILOMETER;

    /**
    * The number of meters in one mile.
    */
    public  static final double METERS_PER_MILE              = METERS_PER_FOOT * FEET_PER_MILE; // 1609.344

    // ------------------------------------------------------------------------

    /**
    * Returns absolute value of a number.
    * @param val Input number.
    * @return absolute value of the input number.
    */
    private static double ABS(double val)   { return (val >= 0.0)? val : -val; }

    /**
    * Returns rounded value of a number.
    * @param val Input number.
    * @return rounded value of the input number.
    */
    private static long   ROUND(double val) { return (val >= 0.0)? (long)(val + 0.5) : (long)(val - 0.5); }

    /**
    * Returns square root of a number.
    * @param X Input number.
    * @return square root of the input number.
    */
    private static double SQRT(double X)    { return Math.sqrt(X); }

    /**
    * Returns cosine value of a number.
    * @param X Input number.
    * @return cos of the input number.
    */
    private static double COS(double X)     { return Math.cos(X); }

    /**
    * Returns sine value of a number.
    * @param X Input number.
    * @return sin of the input number.
    */
    private static double SIN(double X)     { return Math.sin(X); }

    // Coefficients are #5077 from Hart & Cheney. (19.56D)
    // Computer Approximations by Hart, Cheney, Lawson, Maehly, Mesztenyi, Rice, Thacer, 
    // Witzgall, published by Robert E. Krieger Publishing, QA297.C64 1978, ISBN 0-88275-642-7.
    private static final double p4  =  0.161536412982230228262e2;
    private static final double p3  =  0.26842548195503973794141e3;
    private static final double p2  =  0.11530293515404850115428136e4;
    private static final double p1  =  0.178040631643319697105464587e4;
    private static final double p0  =  0.89678597403663861959987488e3;
    private static final double q4  =  0.5895697050844462222791e2;
    private static final double q3  =  0.536265374031215315104235e3;
    private static final double q2  =  0.16667838148816337184521798e4;
    private static final double q1  =  0.207933497444540981287275926e4;
    private static final double q0  =  0.89678597403663861962481162e3;
    
    /**
    * Returns arc sine of a number.
    * @param X Input number.
    * @return arc sine of the input number.
    */
    private static double ASIN(double X)    { 
        //return Math.asin(X); // not in CLDC 1.1
        if ((X <= 1.0) && (X >= -1.0)) {
            return ATAN2(X, SQRT(1.0 - (X * X)));
        } else {
            return Double.NaN;
        }
    }

    /**
    * Returns arc cosine of a number.
    * @param X Input number.
    * @return arc cosine of the input number.
    */
    private static double ACOS(double X)    { 
        //return Math.acos(X); // not in CLDC 1.1
        if ((X <= 1.0) && (X >= -1.0)) {
            return PI_div2 - ASIN(X);
        } else {
            return Double.NaN;
        }
    }
    
    /**
    * Converts rectangular coordinates (x, y) to polar (r, theta).
    * @param Y y-coordinate
    * @param X x-coordinate
    * @return rectangular coordinate
    */
    private static double ATAN2(double Y, double X) { 
        // return Math.atan2(Y,X);  // not in CLDC 1.1

        // X=0
        if (X == 0.0) {
            if (Y > 0.0) {
                // mid Q1/Q2
                return  PI_div2;
            } else
            if (Y < 0.0) {
                // mid Q3/Q4
                return -PI_div2;
            } else {
                // undefined
                return 0.0;
            }
        }
        
        // X<0
        if (X < 0.0) {
            if (Y >= 0.0) {
                // Q2
                return  (PI - _ATAN( Y / -X)); // Y>=0,X<0 |Y/X|
            } else {
                // Q3
                return -(PI - _ATAN( Y /  X)); // Y<0,X<0 |Y/X|
            }
        }

        // X>0
        if (X > 0.0) {
            // Q1/A4
            //return  ATAN( Y / X);
            if (Y > 0.0) {
                // Q1
                return  _ATAN( Y / X);
            } else {
                // Q4
                return -_ATAN(-Y / X);
            }
        }
        
        /* will never reach here */
        return 0.0;
        
    }

    /**
    * Returns the arc tangent of X.
    * @param X the value whose arc tangent is to be returned.
    * @return the arc tangent of the argument.
    */
    private static double ATAN(double X)
    {
        if (X > 0.0) {
            return _ATAN(X);
        } else {
            return -_ATAN(-X);
        }
    }
    private static double _ATAN(double X)
    {
        if (X < 0.414213562373095048802) { /* tan(PI/8) */
            return _ATANX(X);
        } else
        if (X > 2.414213562373095048802) { /* tan(3*PI/8) */
            return PI_div2 - _ATANX(1.0 / X);
        } else {
            return PI_div4 + _ATANX((X - 1.0) / (X + 1.0));
        }
    }
    private static double _ATANX(double X)
    {
        double XX = X * X;
        return X * ((((p4 * XX + p3) * XX + p2) * XX + p1) * XX + p0) /
                   (((((XX + q4) * XX + q3) * XX + q2) * XX + q1) * XX + q0);
    }

    // ------------------------------------------------------------------------
    
    public  static final int    FORMAT_TYPE_MASK    = 0x0F; // format type mask
    public  static final int    FORMAT_DEC          = 0x01; // decimal format
    public  static final int    FORMAT_DMS          = 0x02; // DMS format
    public  static final int    FORMAT_AXIS_MASK    = 0xF0; // axis mask
    public  static final int    FORMAT_LATITUDE     = 0x10; // latitude
    public  static final int    FORMAT_LONGITUDE    = 0x20; // longitude
    
    public  static final String NORTH_ABBR          = "N";
    public  static final String SOUTH_ABBR          = "S";
    public  static final String EAST_ABBR           = "E";
    public  static final String WEST_ABBR           = "W";
    
    public  static final String NE_ABBR             = NORTH_ABBR + EAST_ABBR;
    public  static final String NW_ABBR             = NORTH_ABBR + WEST_ABBR;
    public  static final String SE_ABBR             = SOUTH_ABBR + EAST_ABBR;
    public  static final String SW_ABBR             = SOUTH_ABBR + WEST_ABBR;

    // ------------------------------------------------------------------------

    /**
    * Returns square of a number.
    * @param X Input number.
    * @return square of the input number.
    */
    private static double SQ(double X) { return X * X; }

    // ------------------------------------------------------------------------

    private double              latitude    = 0.0;
    private double              longitude   = 0.0;
    private long                fixtime     = 0L;

    /**
    * Default constructor.
    */
    public GeoPoint()
    {
    }

    /**
    * Copy constructor
    * @param gp An existing GeoPoint object.
    */
    public GeoPoint(GeoPoint gp)
    {
        this();
        this.setLatitude(gp.getLatitude());
        this.setLongitude(gp.getLongitude());
    }

    /**
    * Creates a new GeoPoint with given latitude and longitude.
    * @param latitude latitude of the point.
    * @param longitude longitude of the point.
    */
    public GeoPoint(double latitude, double longitude)
    {
        this();
        this.setLatitude(latitude);
        this.setLongitude(longitude);
    }

    /**
    * Creates a new GeoPoint with given latitude, longitude and the time.
    * @param latitude latitude of the point.
    * @param longitude longitude of the point.
    * @param fixtime time of the point.
    */
    public GeoPoint(double latitude, double longitude, long fixtime)
    {
        this(latitude, longitude);
        this.setFixtime(fixtime);
    }
 
    // ------------------------------------------------------------------------

    /**
    * Validates the current point by checking to see if its coordinates are out of range.
    * @return True if the current point is valid, False otherwise.
    */
    public boolean isValid()
    {
        double latAbs = ABS(this.getLatitude());
        double lonAbs = ABS(this.getLongitude());
        if ((latAbs >= MAX_LATITUDE) || (lonAbs >= MAX_LONGITUDE)) {
            // invalid values
            return false;
        } else
        if ((latAbs <= 0.0002) && (lonAbs <= 0.0002)) {
            // small square off the coast of Africa (Ghana)
            return false;
        } else {
            return true;
        }
    }
    
    // ------------------------------------------------------------------------

    /**
    * Sets latitude of the current point.
    * @param deg Degree
    * @param min Minute
    * @param sec Second
    */
    public void setLatitude(double deg, double min, double sec)
    {
        this.setLatitude(GeoPoint.convertDmsToDec(deg, min, sec));
    }

    /**
    * Sets latitude of the current point.
    * @param lat new Latitude
    */
    public void setLatitude(double lat)
    {
        this.latitude = lat;
    }
    
    /**
    * Returns the latitude of the current point.
    * @return latitude of the current point
    */
    public double getLatitude()
    {
        return this.latitude;
    }
    
    /**
    * Returns the latitude of the current point in radians.
    * @return latitude of the current point in radians
    */
    public double getLatitudeRadians()
    {
        return this.getLatitude() * RADIANS;
    }

    /**
    * Returns the latitude of the current point as a string.
    * @return latitude of the current point as a string
    */
    public String getLatitudeString()
    {
        return formatLatitude(this.getLatitude());
    }

    /**
    * Returns the latitude of the current point as a string.
    * @param dec number of decimals
    * @return latitude of the current point as a string
    */
    public String getLatitudeString(int dec)
    {
        return formatLatitude(this.getLatitude(), dec);
    }

    /**
    * Format the coordinate to a string.
    * @param lat Latitude
    * @return A formatted string.
    */
    public static String formatLatitude(double lat)
    {
        return formatCoord(lat);
    }

    /**
    * Format the coordinate to a string.
    * @param lat Latitude
    * @param dec number of decimals
    * @return A formatted string.
    */
    public static String formatLatitude(double lat, int dec)
    {
        return formatCoord(lat, dec);
    }
    
    // ------------------------------------------------------------------------

    /**
    * Sets longitude of the current point.
    * @param deg Degree
    * @param min Minute
    * @param sec Second
    */
    public void setLongitude(double deg, double min, double sec)
    {
        this.setLongitude(GeoPoint.convertDmsToDec(deg, min, sec));
    }

    /**
    * Sets longitude of the current point.
    * @param lon new longitude
    */
    public void setLongitude(double lon)
    {
        this.longitude = lon;
    }

    /**
    * Returns the longitude of the current point.
    * @return longitude of the current point
    */
    public double getLongitude()
    {
        return this.longitude;
    }
    
    /**
    * Returns the longitude of the current point in radians.
    * @return longitude of the current point in radians
    */
    public double getLongitudeRadians()
    {
        return this.getLongitude() * RADIANS;
    }

    /**
    * Returns the longitude of the current point as a string.
    * @return longitude of the current point as a string
    */
    public String getLongitudeString()
    {
        return formatLongitude(this.getLongitude());
    }

    /**
    * Returns the longitude of the current point as a string.
    * @param dec number of decimals
    * @return longitude of the current point as a string
    */
    public String getLongitudeString(int dec)
    {
        return formatLongitude(this.getLongitude(), dec);
    }

    /**
    * Format the coordinate to a string.
    * @param lon longitude
    * @return A formatted string.
    */
    public static String formatLongitude(double lon)
    {
        return formatCoord(lon);
    }

    /**
    * Format the coordinate to a string.
    * @param lon longitude
    * @param dec number of decimals
    * @return A formatted string.
    */
    public static String formatLongitude(double lon, int dec)
    {
        return formatCoord(lon, dec);
    }
    
    // ------------------------------------------------------------------------

    /**
    * Sets fixtime of the current point.
    * @param fixtime new fixtime
    */
    public void setFixtime(long fixtime)
    {
        this.fixtime = fixtime;
    }
    
    /**
    * Returns the fixtime of the current point.
    * @return fixtime of the current point
    */
    public long getFixtime()
    {
        return this.fixtime;
    }
    
    // ------------------------------------------------------------------------

    private static final double POW_24  =    16777216.0; // 2^24
    private static final double POW_28  =   268435456.0; // 2^28
    private static final double POW_32  =  4294967296.0; // 2^32

    /**
    * Encodes the GeoPoint into an array of bytes ready to be sent.
    * @param gp GeoPoint to be sent.
    * @param enc Place to write the bytes to.
    * @param ofs Offset.
    * @param len Length
    * @return The encoded array of bytes.
    */
    public static byte[] encodeGeoPoint(GeoPoint gp, byte enc[], int ofs, int len)
    {
        
        /* null/empty bytes */
        if (enc == null) {
            return null;
        }
        
        /* offset/length out-of-range */
        if (len < 0) { len = enc.length; }
        if ((ofs + len) > enc.length) {
            return null;
        }
        
        /* not enough bytes to encode */
        if (len < 6) {
            return null;
        }

        /* lat/lon */
        double lat = gp.getLatitude();
        double lon = gp.getLongitude();
        
        /* standard resolution */
        if ((len >= 6) && (len < 8)) {
            // LL-LL-LL LL-LL-LL
            long rawLat24 = (lat != 0.0)? ROUND((lat -  90.0) * (POW_24 / -180.0)) : 0L;
            long rawLon24 = (lon != 0.0)? ROUND((lon + 180.0) * (POW_24 /  360.0)) : 0L;
            long rawAccum = ((rawLat24 << 24) & 0xFFFFFF000000L) | (rawLon24 & 0xFFFFFFL);
            enc[ofs + 0] = (byte)((rawAccum >> 40) & 0xFF);
            enc[ofs + 1] = (byte)((rawAccum >> 32) & 0xFF);
            enc[ofs + 2] = (byte)((rawAccum >> 24) & 0xFF);
            enc[ofs + 3] = (byte)((rawAccum >> 16) & 0xFF);
            enc[ofs + 4] = (byte)((rawAccum >>  8) & 0xFF);
            enc[ofs + 5] = (byte)((rawAccum      ) & 0xFF);
            return enc;
        } 
        
        /* high resolution */
        if (len >= 8) {
            // LL-LL-LL-LL LL-LL-LL-LL
            long rawLat32 = (lat != 0.0)? ROUND((lat -  90.0) * (POW_32 / -180.0)) : 0L;
            long rawLon32 = (lon != 0.0)? ROUND((lon + 180.0) * (POW_32 /  360.0)) : 0L;
            long rawAccum = ((rawLat32 << 32) & 0xFFFFFFFF00000000L) | (rawLon32 & 0xFFFFFFFFL);
            enc[ofs + 0] = (byte)((rawAccum >> 56) & 0xFF);
            enc[ofs + 1] = (byte)((rawAccum >> 48) & 0xFF);
            enc[ofs + 2] = (byte)((rawAccum >> 40) & 0xFF);
            enc[ofs + 3] = (byte)((rawAccum >> 32) & 0xFF);
            enc[ofs + 4] = (byte)((rawAccum >> 24) & 0xFF);
            enc[ofs + 5] = (byte)((rawAccum >> 16) & 0xFF);
            enc[ofs + 6] = (byte)((rawAccum >>  8) & 0xFF);
            enc[ofs + 7] = (byte)((rawAccum      ) & 0xFF);
            return enc;
        }
       
        /* will never reach here */
        return null;

    }

    /**
    * Extracts GeoPoint from an array of bytes.
    * @param enc Array of bytes holding the GeoPoint.
    * @param ofs Offset.
    * @param len length
    * @return The extracted GeoPoint.
    */
    public static GeoPoint decodeGeoPoint(byte enc[], int ofs, int len)
    {
        
        /* null/empty bytes */
        if (enc == null) {
            return null;
        }
        
        /* offset/length out-of-range */
        if (len < 0) { len = enc.length; }
        if ((ofs + len) > enc.length) {
            return null;
        }
        
        /* not enough bytes to decode */
        if (len < 6) {
            return null;
        }
        
        /* standard resolution */
        if ((len >= 6) && (len < 8)) {
            // LL-LL-LL LL-LL-LL
            long rawLat24 = (((long)enc[ofs+0] & 0xFF) << 16) | (((long)enc[ofs+1] & 0xFF) << 8) | ((long)enc[ofs+2] & 0xFF);
            long rawLon24 = (((long)enc[ofs+3] & 0xFF) << 16) | (((long)enc[ofs+4] & 0xFF) << 8) | ((long)enc[ofs+5] & 0xFF);
            double lat = (rawLat24 != 0L)? (((double)rawLat24 * (-180.0 / POW_24)) +  90.0) : 0.0;
            double lon = (rawLon24 != 0L)? (((double)rawLon24 * ( 360.0 / POW_24)) - 180.0) : 0.0;
            //Log.println(LOG_NAME, "Decoded Lat/Lon: " + lat + "/" + lon);
            return new GeoPoint(lat, lon);
        }
        
        /* high resolution */
        if (len >= 8) {
            // LL-LL-LL-LL LL-LL-LL-LL
            long rawLat32 = (((long)enc[ofs+0] & 0xFF) << 24) | (((long)enc[ofs+1] & 0xFF) << 16) | (((long)enc[ofs+2] & 0xFF) << 8) | ((long)enc[ofs+3] & 0xFF);
            long rawLon32 = (((long)enc[ofs+4] & 0xFF) << 24) | (((long)enc[ofs+5] & 0xFF) << 16) | (((long)enc[ofs+6] & 0xFF) << 8) | ((long)enc[ofs+7] & 0xFF);
            double lat = (rawLat32 != 0L)? (((double)rawLat32 * (-180.0 / POW_32)) +  90.0) : 0.0;
            double lon = (rawLon32 != 0L)? (((double)rawLon32 * ( 360.0 / POW_32)) - 180.0) : 0.0;
            return new GeoPoint(lat, lon);
        }
        
        /* will never reach here */
        return null;
        
    }
    
    // ------------------------------------------------------------------------

    /**
    * Computes the distance in radians from current point to a specified point.
    * @param dest Destination point.
    * @return distance in radians from the current point to the point given as argument.
    */
    public double radiansToPoint(GeoPoint dest)
    {
        // References:
        //   http://www.boeing-727.com/Data/fly%20odds/distance.html
        // Flat plane approximations:
        //   http://mathforum.org/library/drmath/view/51833.html
        //   http://mathforum.org/library/drmath/view/62720.html
        if (dest == null) {
            // you pass in 'null', you deserver what you get
            return Double.NaN;
        } else
        if (this.equals(dest)) {
            // If the points are equals, the radians would be NaN
            return 0.0;
        } else {
            try {
                double lat1 = this.getLatitudeRadians(), lon1 = this.getLongitudeRadians();
                double lat2 = dest.getLatitudeRadians(), lon2 = dest.getLongitudeRadians();
                double rad  = 0.0;
                if (UseHaversineDistanceFormula) {
                    // Haversine formula:
                    // "The Haversine formula may be more accurate for small distances"
                    // See: http://www.census.gov/cgi-bin/geo/gisfaq?Q5.1
                    //      http://mathforum.org/library/drmath/view/51879.html
                    // Also, use of the Haversine formula is about twice as fast as the Law of Cosines
                    double dlat = lat2 - lat1;
                    double dlon = lon2 - lon1;
                    double a    = SQ(SIN(dlat/2.0)) + (COS(lat1) * COS(lat2) * SQ(SIN(dlon/2.0)));
                    rad = 2.0 * ATAN2(SQRT(a), SQRT(1.0 - a));
                } else {
                    // Law of Cosines for Spherical Trigonometry:
                    // Per http://www.census.gov/cgi-bin/geo/gisfaq?Q5.1 this method isn't recommended:
                    //  "Although this formula is mathematically exact, it is unreliable for
                    //   small distances because the inverse cosine is ill-conditioned."
                    // Note: this problem appears to be less of an issue in Java.  The amount of error
                    // between Law-of-Cosine and Haversine formulas appears small even when calculating
                    // distance aven as low as 1.5 meters.
                    double dlon = lon2 - lon1;
                    rad = ACOS((SIN(lat1) * SIN(lat2)) + (COS(lat1) * COS(lat2) * COS(dlon)));
                }
                
                return rad;
                
            } catch (Throwable t) {
                
                return Double.NaN;
                
            }
        }
    }

    /**
    * Computes the distance in kilometers from current point to a specified point.
    * @param gp Destination point.
    * @return distance in kilometers from the current point to the point given as argument.
    */
    public double kilometersToPoint(GeoPoint gp)
    {
        double radians = this.radiansToPoint(gp);
        return !Double.isNaN(radians)? (EARTH_MEAN_RADIUS_KM * radians) : Double.NaN;
    }

    /**
    * Computes the distance in meters from current point to a specified point.
    * @param gp Destination point.
    * @return distance in meters from the current point to the point given as argument.
    */
    public double metersToPoint(GeoPoint gp)
    {
        double radians = this.radiansToPoint(gp);
        return !Double.isNaN(radians)? ((EARTH_MEAN_RADIUS_KM * 1000.0) * radians) : Double.NaN;
    }

    // ------------------------------------------------------------------------

    /**
    * Converts radius to delta lat/long.
    * [experimental, not currently in use]
    * @param radiusMeters radiusMeters
    * @return a geopoint
    */
    public GeoPoint getRadiusDeltaPoint(double radiusMeters)
    {
        double a = EARTH_EQUATORIAL_RADIUS_KM * 1000.0;
        double b = EARTH_POLAR_RADIUS_KM * 1000.0;
        double lat = this.getLatitudeRadians();
        // r(T) = (a^2) / sqrt((a^2)*(cos(T)^2) + (b^2)*(sin(T)^2))
        double r = SQ(a) / SQRT((SQ(a) * SQ(COS(lat))) + (SQ(b) * SQ(SIN(lat))));
        // dlat = (180 * R) / (PI * r);
        double dlat = (180.0 * radiusMeters) / (PI * r);
        // dlon = dlat / cos(lat);
        double dlon = dlat / COS(lat);
        return new GeoPoint(dlat, dlon);
    }
    
    // ------------------------------------------------------------------------

    /**
    * Checks to see if the distance from current point to a specified point is under some value.
    * @param deltaKilometers value to compare distance to.
    * @param gp GeoPoint to calculate distance to.
    * @return True if the distance is under the given value, False otherwise
    */
    public boolean isNearby(GeoPoint gp, double deltaKilometers)
    {
        return (this.kilometersToPoint(gp) <= deltaKilometers);
    }

    // ------------------------------------------------------------------------
    
    private static String DIRECTION[] = { NORTH_ABBR, NE_ABBR, EAST_ABBR, SE_ABBR, SOUTH_ABBR, SW_ABBR, WEST_ABBR, NW_ABBR };
    
    /**
    * Returns the heading abbreviation (based on 8 compass points)
    * @param heading heading
    * @return the heading string
    */
    public static String GetHeadingString(double heading)
    {
        if (!Double.isNaN(heading) && (heading >= 0.0)) {
            int h = (int)ROUND(heading / 45.0) % 8;
            return DIRECTION[(h > 7)? 0 : h];
        } else {
            return "";
        }
    }

    /**
    * Returns the heading from current point to another point.
    * @param dest Destination point.
    * @return the heading from current point to another point (in degrees)
    */
    public double headingToPoint(GeoPoint dest)
    {
        // Assistance from:
        //   http://mathforum.org/library/drmath/view/55417.html
        //   http://williams.best.vwh.net/avform.htm
        try {              
            double lat1 = this.getLatitudeRadians(), lon1 = this.getLongitudeRadians();
            double lat2 = dest.getLatitudeRadians(), lon2 = dest.getLongitudeRadians();
            double dist = this.radiansToPoint(dest);
            double rad  = ACOS((SIN(lat2) - (SIN(lat1) * COS(dist))) / (SIN(dist) * COS(lat1)));
            if (SIN(lon2 - lon1) < 0) { rad = (2.0 * PI) - rad; }
            double deg  = rad / RADIANS;
            return deg;
        } catch (Throwable t) {
            //Log.errorLOG_NAME, ("headingToPoint", t);
            return 0.0;
        }
    }
    
    // ------------------------------------------------------------------------

    /**
    * Returns string representation of the current GeoPoint.
    * @return string representation of the current GeoPoint.
    */
    public String toString()
    {
        StringBuffer sb = new StringBuffer();
        sb.append(this.getFixtime());
        sb.append(PointSeparator);
        sb.append(this.getLatitudeString());
        sb.append(PointSeparator);
        sb.append(this.getLongitudeString());
        return sb.toString();
    }
    
    /**
    * Compares the current GeoPoint with another.
    * @param other The GeoPoint to be compared to.
    * @return True if points are equal, False otherwise.
    */
    public boolean equals(Object other)
    {
        if (other instanceof GeoPoint) {
            GeoPoint gp = (GeoPoint)other;
            double deltaLat = ABS(gp.getLatitude()  - this.getLatitude() );
            double deltaLon = ABS(gp.getLongitude() - this.getLongitude());
            return ((deltaLat < EPSILON) && (deltaLon < EPSILON));
        } else {
            return false;
        }
    }
    
    // ------------------------------------------------------------------------
    
    /**
    * Converts (degree, minute, second) to decimal number.
    * @param deg Degree
    * @param min Minute
    * @param sec Second
    * @return The converted value
    */
    public static double convertDmsToDec(int deg, int min, int sec)
    {
        return GeoPoint.convertDmsToDec((double)deg, (double)min, (double)sec);
    }
    
    /**
    * Converts (degree, minute, second) to decimal number.
    * @param deg Degree
    * @param min Minute
    * @param sec Second
    * @return The converted value
    */
    public static double convertDmsToDec(double deg, double min, double sec)
    {
        double sign = (deg >= 0.0)? 1.0 : -1.0;
        double d = ABS(deg);
        double m = ABS(min / 60.0);
        double s = ABS(sec / 3600.0);
        return sign * (d + m + s);
    }
    
    // ------------------------------------------------------------------------
    /**
    * Extracts a location represented by value of type double to a string.
    * @param location a location
    * @return A string representing the location given as argument.
    */
    public static String formatCoord(double location)
    {
        return GeoPoint.formatCoord(location, 5);
    }
    
    /**
    * Extracts a location represented by value of type double to a string.
    * @param location a location
    * @param DEC  number of decimals
    * @return The formatted coordinate value
    */
    public static String formatCoord(double location, int DEC)
    {
        //int DEC = 5;
        double DIV = 100000.0, FRAME = 1000.0;

        /* signed? */
        boolean isSgn = false;
        if (location < 0.0) {
            isSgn = true;
            location = -location;
        }
        
        /* round */
        long loc = (long)((location * DIV) + 0.5);
        location = ((double)loc / DIV) + FRAME;
        
        /* convert to string and trim FRAME */
        String v = StringTools.formatDouble(location, DEC);
        String t = v.substring(1);
        if (t.startsWith("00")) {
            t = t.substring(2);
        } else
        if (t.startsWith("0")) {
            t = t.substring(1);
        }
        String d = (isSgn?"-":"") + t;
        
        return d;
    }
    
    // ------------------------------------------------------------------------

}
