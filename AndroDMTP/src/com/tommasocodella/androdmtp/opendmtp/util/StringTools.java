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
//  General String based utilities (parsing, encoding, etc).
// ----------------------------------------------------------------------------
// Change History:
//  2006/03/26  Martin D. Flynn
//      Initial release
//  2006/11/03  Guanghong Yang
//      Include JavaDocs
// ----------------------------------------------------------------------------
package com.tommasocodella.androdmtp.opendmtp.util;

import java.lang.Long;
import java.lang.Double;
import java.lang.Character;
import java.util.Hashtable;
import java.util.Enumeration;
import java.util.Vector;

/**
* Provides general String based utilities (parsing, encoding, etc).
*/
public class StringTools
{

    // ------------------------------------------------------------------------

    private static final String LOG_NAME            = "STR";

    // ------------------------------------------------------------------------

    /** Default charset String. */
    public static final String DEFAULT_CHARSET      = "ISO-8859-1";
    
    private static final int   FILTER_TYPE_Boolean  = 1;
    private static final int   FILTER_TYPE_Byte     = 2;
    private static final int   FILTER_TYPE_Short    = 3;
    private static final int   FILTER_TYPE_Integer  = 4;
    private static final int   FILTER_TYPE_Long     = 5;
    private static final int   FILTER_TYPE_Float    = 6;
    private static final int   FILTER_TYPE_Double   = 7;

    // ------------------------------------------------------------------------

    /**
    * Returns a sequence of bytes of a string, based on the default character set
    * @param s String to be converted.
    * @return byte[] The byte array.
    */
    public static byte[] getBytes(String s)
    {
        if (s != null) {
            try {
                return s.getBytes(DEFAULT_CHARSET);
            } catch (Throwable uce) { // UnsupportedEncodingException
                // will not occur
                Log.error(LOG_NAME, "Charset not found: " + DEFAULT_CHARSET);
                return s.getBytes();
            }
        } else {
            return null;
        }
    }

    /**
    * Converts a long value to a byte[].
    * @param val The long value to be converted.
    * @param bitLen The length of bits of the long value.
    * @return The byte[].
    */
    public static byte[] getBytes(long val, int bitLen)
    {
        int byteLen = (bitLen + 7) / 8;
        byte b[] = new byte[byteLen];
        for (int i = 0; i < byteLen; i++) { 
            b[byteLen - i - 1] = (byte)(val & 0xFF);
            val >>>= 8;
        }
        return b;
    }
    
    // ------------------------------------------------------------------------
    
    /**
    * Creates the string out of a byte array.
    * @param b byte array
    * @return String
    * @see #toStringValue(byte[], int, int).
    */
    public static String toStringValue(byte b[])
    {
        return (b != null)? StringTools.toStringValue(b, 0, b.length) : null;
    }
    
    /**
    * Creates the string out of a byte array.
    * @param b byte array
    * @param ofs offset into byte array
    * @param len length of byte array to convert to a String
    * @return a String
    */
    public static String toStringValue(byte b[], int ofs, int len)
    {
        if (b != null) {
            try {
                return new String(b, ofs, len, DEFAULT_CHARSET);
            } catch (Throwable t) {
                // This should NEVER occur (at least not because of the charset)
                Log.error(LOG_NAME, "Byte=>String conversion error", t);
                return new String(b, ofs, len);
            }
        } else {
            return null; // what goes around ...
        }
    }
     
    // ------------------------------------------------------------------------
    
    /**
    * Converts a string into a double.
    * @param num String representing a numeric value
    * @param dft default value to return in case the parsing failed
    * @return the double value represented by the string argument
    */
    public static double parseDouble(String num, double dft)
    {
        String n = StringTools.filterNumber(num, FILTER_TYPE_Double);
        if (n != null) {
            try {
                return Double.parseDouble(n);
            } catch (NumberFormatException nfe) {
                // Since 'filterNumber' makes sure that only digits are parsed,
                // this likely means that the specified digit string is too large
                // for this required data type.
            }
        }
        return dft;
    }
    
    // ------------------------------------------------------------------------
    
    /**
    * Parses a string value into a long.
    * @param num String representing a numeric value
    * @param dft default value to return in case of parsing failure
    * @return long value or the default
    */
    public static long parseLong(String num, long dft)
    {
        String n = StringTools.filterNumber(num, FILTER_TYPE_Long);
        if (n != null) {
            if (n.toLowerCase().startsWith("0x")) {
                return StringTools.parseHexLong(n, dft);
            } else {
                try {
                    return Long.parseLong(n);
                } catch (NumberFormatException nfe) {
                    // Since 'filterNumber' makes sure that only digits are parsed,
                    // this likely means that the specified digit string is too large
                    // for this required data type.
                }
            }
        }
        return dft;
    }
    
    // ------------------------------------------------------------------------
    
    /**
    * Checks if a char is equivalent to space.
    * @param x The char to be checked.
    * @return True if the char is equivalent to space, or false otherwise.
    */
    private static boolean isWhitespace(char x)
    {
        return (x == ' ') || (x == '\t') || (x == '\r') || (x == '\n');
    }
    
    /**
    * Extracts the leading valid numeric characters from a String.
    * @param val The String to be parsed.
    * @param type The type of number to be extracted.
    * @return The valid numeric value represented as a String
    */
    private static String filterNumber(String val, int type)
    {
             
        /* null string */
        if (val == null) { // null string
            return "";
        }
            
        /* skip initial whitespace */
        int s = 0;
        while ((s < val.length()) && StringTools.isWhitespace(val.charAt(s))) { s++; }
        if (s == val.length()) { // empty string
            return "";
        }
        String v = val; // val.trim();
        int vlen = v.length();
        
        /* hex number */
        boolean hex = false;
        if ((v.length() >= 2) && (v.charAt(s) == '0') && (Character.toLowerCase(v.charAt(s + 1)) == 'x')) {
            hex = true;
        }

        /* skip initial digits */
        int ps, p;
        if (hex) {
            ps = s + 2; // skip ofer "0x";
            p  = ps;
            for (;(p < vlen) && (HEX.indexOf(Character.toUpperCase(v.charAt(p))) >= 0);) { p++; }
        } else {
            ps = (v.charAt(s) == '-')? (s + 1) : s; // negative number?
            p  = ps;
            for (;(p < vlen) && Character.isDigit(v.charAt(p));) { p++; }
        }
        boolean foundDigit = (p > ps);
            
        /* end of digits? */
        String num = null;
        if ((p >= vlen) // pointer beyond end of string
            || (type == FILTER_TYPE_Long)
            || (type == FILTER_TYPE_Integer)
            || (type == FILTER_TYPE_Short)
            || (type == FILTER_TYPE_Byte)
            ) {
            // end of String or Long/Integer/Short/Byte
            num = foundDigit? v.substring(s, p) : null;
        } else
        if (v.charAt(p) != '.') {
            // Double/Float, but doesn't contain decimal point
            num = foundDigit? v.substring(s, p) : null;
        } else {
            // Double/Float, decimal digits
            p++; // skip '.'
            for (ps = p; (p < vlen) && Character.isDigit(v.charAt(p));) { p++; }
            if (p > ps) { foundDigit = true; }
            num = foundDigit? v.substring(s, p) : null;
        }
            
        /* return extracted number */
        return num;
        
    }
    
    // ------------------------------------------------------------------------
    
    /**
    * Parses a string into a boolean value.
    * @param data String
    * @param dft Default value o return
    * @return result boolean value
    */
    public static boolean parseBoolean(String data, boolean dft)
    {
        if (data != null) {
            String v = data.toLowerCase();
            return v.equals("true") || v.equals("yes") || v.equals("on") || v.equals("1");
        }
        return dft;
    }

    // ------------------------------------------------------------------------

    /** String of all possible hex symbols. */
    public static final String HEX = "0123456789ABCDEF";
    
    /**
    * Parses a string into a hexadecimal value represented as a byte array.
    * @param data string
    * @param dft default value to return
    * @return byte array representing a hex value
    */
    public static byte[] parseHex(String data, byte dft[])
    {
        if (data != null) {
            
            /* get data string */
            String d = data.toUpperCase();
            String s = d.startsWith("0X")? d.substring(2) : d;
            
            /* remove any invalid trailing characters */
            for (int i = 0; i < s.length(); i++) {
                if (HEX.indexOf(s.charAt(i)) < 0) {
                    s = s.substring(0, i);
                    break;
                }
            }
            
            /* return default if nothing to parse */
            if (s.equals("")) { 
                return dft; 
            }
            
            /* right justify */
            if ((s.length() & 1) == 1) { s = "0" + s; } // right justified
            
            /* parse data */
            byte rtn[] = new byte[s.length() / 2];
            for (int i = 0; i < s.length(); i += 2) {
                int c1 = HEX.indexOf(s.charAt(i));
                if (c1 < 0) { c1 = 0; /* Invalid Hex char */ }
                int c2 = HEX.indexOf(s.charAt(i+1));
                if (c2 < 0) { c2 = 0; /* Invalid Hex char */ }
                rtn[i/2] = (byte)(((c1 << 4) & 0xF0) | (c2 & 0x0F));
            }
            
            /* return value */
            return rtn;
            
        } else {
            
            return dft;
            
        }
    }
    
    /**
    * Parses a string into a hex value represented as an integer.
    * @param data string
    * @param dft default value to return
    * @return int result hex value
    * @see #parseHexLong(String, long).
    */
    public static int parseHex(String data, int dft)
    {
        return (int)StringTools.parseHexLong(data, (long)dft);
    }
    
    /**
    * Parses a string into a hex value represented as an integer.
    * @param data string
    * @param dft default value to return
    * @return int result hex value
    * @see #parseHexLong(String, long).
    */
    public static int parseHexInt(String data, int dft)
    {
        return (int)StringTools.parseHexLong(data, (long)dft);
    }
    
    /**
    * Parses a string into a hex value represented as a long.
    * @param data string
    * @param dft default value to return
    * @return long result hex value
    */
    public static long parseHex(String data, long dft)
    {
        return StringTools.parseHexLong(data, dft);
    }
    
    /**
    * Parses a string into a hex value represented as a long.
    * @param data string
    * @param dft default value to return
    * @return long result hex value
    */
    public static long parseHexLong(String data, long dft)
    {
        byte b[] = parseHex(data, null);
        if (b != null) {
            long val = 0L;
            for (int i = 0; i < b.length; i++) {
                val = (val << 8) | ((int)b[i] & 0xFF);
            }
            return val;
        } else {
            return dft;
        }
    }
    
    // ------------------------------------------------------------------------
    
    /**
    * Converts entire array of bytes into a string value representing a hex value.
    * @param b byte array
    * @param sb a string buffer to write into
    * @return StringBuffer value
    */
    public static StringBuffer toHexString(byte b, StringBuffer sb)
    {
        if (sb == null) { sb = new StringBuffer(); }
        sb.append(HEX.charAt((b >> 4) & 0xF));
        sb.append(HEX.charAt(b & 0xF));
        return sb;
    }
    
    /**
    * Converts a byte value into a string.
    * @param b byte value
    * @return result string
    * @see #toHexString(byte, StringBuffer)
    */
    public static String toHexString(byte b)
    {
        return StringTools.toHexString(b,null).toString();
    }
    
    /**
    * Converts an specified part of an array of bytes into a string value representing a hex value.
    * @param b byte array
    * @param ofs offset
    * @param len length, -1 for the entire array
    * @param sb String buffer to write into
    * @return StringBuffer
    */
    public static StringBuffer toHexString(byte b[], int ofs, int len, StringBuffer sb)
    {
        if (sb == null) { sb = new StringBuffer(); }
        if (b != null) {
            int bstrt = (ofs < 0)? 0 : ofs;
            int bstop = (len < 0)? b.length : Math.min(b.length,(ofs + len));
            for (int i = bstrt; i < bstop; i++) { StringTools.toHexString(b[i], sb); }
        }
        return sb;
    }
    
    /**
    * Converts entire array of bytes into a string value representing a hex value.
    * @param b byte array
    * @param sb a string buffer to write into
    * @return StringBuffer value
    */
    public static StringBuffer toHexString(byte b[], StringBuffer sb)
    {
        return StringTools.toHexString(b,0,-1,sb);
    }
    
    /**
    * Converts an entire array of bytes into a string value representing a hex value.
    * @param b byte array
    * @return string value
    */
    public static String toHexString(byte b[])
    {
        return StringTools.toHexString(b,0,-1,null).toString();
    }
    
    /**
    * Converts an specified part of an array of bytes into a string value representing a hex value.
    * @param b byte array
    * @param ofs offset
    * @param len length, -1 for the entire array
    * @return string vlaue
    */
    public static String toHexString(byte b[], int ofs, int len)
    {
        return StringTools.toHexString(b,ofs,len,null).toString();
    }
    
    /**
    * Converts a long hex value into a string prefixed with zeroes.
    * @param val long value
    * @param bitLen the length of the string in bits.
    * @return the result string
    */
    public static String toHexString(long val, int bitLen)
    {
        byte b[] = StringTools.getBytes(val, bitLen);
        return StringTools.toHexString(b);
    }
    
    /**
    * Converts a long hex value into a string with bitLen = 64.
    * @param val the long value
    * @return result string
    */
    public static String toHexString(long val)
    {
        return StringTools.toHexString(val, 64);
    }
    
    /**
    * Converts an int hex value into a string with bitLen = 32.
    * @param val the int value
    * @return result string
    */
    public static String toHexString(int val)
    {
        return StringTools.toHexString((long)val & 0xFFFFFFFF, 32);
    }
    
    /**
    * Converts a short hex value into a string with bitLen = 16.
    * @param val the short value
    * @return result string
    */
    public static String toHexString(short val)
    {
        return StringTools.toHexString((long)val & 0xFFFF, 16);
    }
    
    // ------------------------------------------------------------------------

    /**
    * Tests if one string has another as a prefix, ignoring the case.
    * @param t a string
    * @param m a prefix
    * @return true if t starts with m, false otherwise
    */
    public static boolean startsWithIgnoreCase(String t, String m)
    {
        if ((t != null) && (m != null)) {
            return t.toLowerCase().startsWith(m.toLowerCase());
        } else {
            return false;
        }
    }

    /**
    * Returns the index within this string of the first occurrence of the specified substring
    * ignoring case.
    * @param t string
    * @param m substring
    * @return if the string argument occurs as a substring within this object, then the index of the
    *         first character of the first such substring is returned; if it does not occur as a
    *         substring, -1 is returned.
    */
    public static int indexOfIgnoreCase(String t, String m)
    {
        if ((t != null) && (m != null)) {
            return t.toLowerCase().indexOf(m.toLowerCase());
        } else {
            return -1;
        }
    }
    
    // ------------------------------------------------------------------------
    // 'parseString' does not take quoted values into account

    /**
    * Provides a tokenizer from a String.
    */
    private static class Tokenizer
    {
        private String value = null;
        private char delimiter = 0;
        private int index = 0;
        private int length = 0;
        /**
        * Initiates the Tokenizer.
        * @param value The String to be tokenized.
        * @param delim Delimeter.
        */
        public Tokenizer(String value, char delim) {
            this.value = value;
            this.delimiter = delim;
            this.index = 0;
            this.length = this.value.length();
        }
        /**
        * Checks if the end of the String.
        * @return True if the end of the String, or false otherwise.
        */
        public boolean hasMoreTokens() {
            return (this.index < this.length);
        }
        /**
        * Returns the next token from the String.
        * @return The next token from the String.
        */
        public String nextToken() {
            if (this.index < this.length) {
                int p = this.index;
                this.index = this.scanToken(p);
                return this.value.substring(p, this.index);
            } else {
                return null;
            }
        }
        /**
        * Returns the next position of the delimiter in the String from the indeicted starting point.
        * @param startPos The starting point.
        * @return The next position of the delimiter.
        */
        private int scanToken(int startPos) {
            int p = startPos;
            if ((p < this.length) && (this.value.charAt(p) == this.delimiter)) {
                // return delimiter
                return p + 1;
            } else {
                // return non-delimiter
                for (p++; p < this.length; p++) {
                    if (this.value.charAt(p) == this.delimiter) {
                        break;
                    }
                }
                return p;
            }
        }
    }
    
    /**
    * Parse a String into a String array with the indicated delimiter. Guaranteed not to be null.
    * @param value The String to be parsed.
    * @param delim The delimiter.
    * @return A String array parsed from the String.
    */
    public static String[] parseString(String value, char delim)
    {
        if (value != null) {
            
            /* parse */
            Tokenizer st = new Tokenizer(value, delim);
            Vector v1 = new Vector();
            for (;st.hasMoreTokens();) {
                v1.addElement(st.nextToken()); 
            }

            /* examine all tokens to make sure we include blank items */
            int dupDelim = 1; // assume we've started with a delimiter
            Vector v2 = new Vector();
            for (Enumeration i = v1.elements(); i.hasMoreElements();) {
                String s = (String)i.nextElement();
                if ((s.length() == 1) && (s.charAt(0) == delim)) {
                    if (dupDelim > 0) { v2.addElement(""); } // blank item
                    dupDelim++;
                } else {
                    v2.addElement(s.trim());
                    dupDelim = 0;
                }
            }
            
            /* return parsed array */
            int sx = 0;
            String sa[] = new String[v2.size()];
            for (Enumeration i = v2.elements(); i.hasMoreElements();) {
                sa[sx++] = (String)i.nextElement();
            }
            return sa;
            
        } else {
            
            /* nothing parsed */
            return new String[0];
            
        }
    }

    // ------------------------------------------------------------------------
    
    /**
    * Returns the 10^d, where d is the parameter.
    * @param d The exponential index.
    * @return 10^d.
    */
    private static double DIV(int d) { double div=1.; for (;d>0;d--) div*=10.; return div; }
    private static String ZEROES = "0000000000";
    
    /**
    * Format a double value with indicated length of decimal part.
    * @param val The double value.
    * @param dec The length of decimal part.
    * @return A String representing the double value.
    */
    public static String formatDouble(double val, int dec)
    {
        // a simple double value formatter.
        
        /* check range */
        if (dec > 10) { dec = 10; }
        if (dec <  0) { dec =  0; }
        
        /* round */
        double div = DIV(dec);
        double rval = val * div;
        if (rval >= 0.0) { rval += 0.5; } else { rval -= 0.5; }
        rval = (double)((long)rval) / div;
        
        /* no decimal places? */
        if (dec == 0) {
            // return with no decimal point
            return String.valueOf((long)rval);
        }
        
        /* to string */
        boolean exp = false;
        String v = String.valueOf(rval);
        if ((v.indexOf("e") > 0) || v.indexOf("E") > 0) {
            exp = true;
            //Log.debug(LOG_NAME, "Exp notation: " + rval);
            if ((rval >= 0.0) && (rval < 1.0)) {
                v = String.valueOf(rval + 1.0);
            } else
            if ((rval <= 0.0) && (rval > -1.0)) {
                v = String.valueOf(rval - 1.0);
            } else {
                // we don't handle this, return as-is
                return v;
            }
        }

        /* format */
        int n = v.length(), p = v.indexOf(".");
        if (p < 0) {
            // no decimal points
            v = v + "." + ZEROES.substring(0, dec);
        } else
        if ((p + dec + 1) <= n) {
            // more decimal places than needed
            v = v.substring(0, p + dec + 1);
        } else {
            // not enough trailing zeroes
            v = v + ZEROES.substring(0, (p + dec + 1) - n);
        }
        
        /* was exponent? */
        if (exp) {
            if (v.startsWith("1.")) { // (rval >= 0.0)
                v = "0" + v.substring(1);
            } else
            if (v.startsWith("-1.")) { // (rval < 0.0)
                v = "-0" + v.substring(2);
            } else {
                Log.error(LOG_NAME, "Unexpected value: " + v);
            }
        }
        
        return v;
        
    }
    
    // ------------------------------------------------------------------------
    // Probably should be in a module called 'ClassTools'

    /**
    * Gets the name of the class of an object.
    * @param c The object.
    * @return The class name of the object.
    */
    public static String className(Object c)
    {
        if (c == null) {
            return "null";
        } else
        if (c instanceof Class) {
            return ((Class)c).getName();
        } else {
            return c.getClass().getName();
        }
    }

    // ------------------------------------------------------------------------

}
