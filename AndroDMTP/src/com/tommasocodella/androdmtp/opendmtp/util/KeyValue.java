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
//  This class is used by the property manager for representing key/value pairs.
// ----------------------------------------------------------------------------
// Change History:
//  2006/03/26  Martin D. Flynn
//     -Initial release
//  2006/11/03  Guanghong Yang
//     -Include JavaDocs
//  2006/12/21  Martin D. Flynn
//     -Corrected the order of GPS formatted properties to comply with the
//      protocol when parsed or printed.  The corrected format is now:
//         <fixtime>,<latitude>,<longitude>
//      Previously, the format was "<latitude>/<longitude>" (see GeoPoint.java)
//  2006/??/??  Martin D. Flynn
//     -'getString' now returns proper String value for byte arrays.
// ----------------------------------------------------------------------------
package com.tommasocodella.androdmtp.opendmtp.util;


/**
* Provides key-value pair units that support different types of data. Supported types include:
* String, byte[], Boolean, Long, Double and GeoPoint.
*/
public class KeyValue
{
    
    // ------------------------------------------------------------------------

    private static final String LOG_NAME = "KV";
    
    // ------------------------------------------------------------------------
    // Supported types:
    //      GeoPoint.class
    //      String.class
    //      Byte[].class
    //      Boolean.class
    //      Long.class      [Number]
    //      Double.class    [Number]
    
    /** Type for <tt>CommandHandler</tt>. */
    public static Type COMMAND  = new Type(  0, false, false, 0);
    /** Type for <tt>GeoPoint</tt>. */
    public static Type GPS      = new Type(  8, false, false, 0);
    /** Type for <tt>String</tt>. */
    public static Type STRING   = new Type(255, false, false, 0);
    /** Type for <tt>byte[]</tt>. */
    public static Type BINARY   = new Type(255, false, false, 0);
    /** Type for <tt>Boolean</tt>. */
    public static Type BOOLEAN  = new Type(  1, false, false, 0);
    /** Type for 32-bit Unsigned Integer (Long of 4 bytes). */
    public static Type UINT32   = new Type(  4, true , false, 0);
    /** Type for 16-bit Unsigned Integer (Long of 2 bytes). */
    public static Type UINT16   = new Type(  2, true , false, 0);
    /** Type for 16-bit Signed Integer (Long of 2 bytes). */
    public static Type INT16    = new Type(  2, true , true , 0);
    /** Type for 8-bit Unsigned Integer (Long of 1 byte). */
    public static Type UINT8    = new Type(  1, true , false, 0);
    /** Type for 32-bit Unsigned Decimal (Double of 4 bytes). */
    public static Type UDEC32   = new Type(  4, true , false, 1);
    /** Type for 32-bit Signed Decimal (Double of 4 bytes). */
    public static Type DEC32    = new Type(  4, true , true , 1);
    /** Type for 16-bit Unsigned Decimal (Double of 2 bytes). */
    public static Type UDEC16   = new Type(  2, true , false, 1);
    /** Type for 16-bit Signed Decimal (Double of 2 byte). */
    public static Type DEC16    = new Type(  2, true , true , 1);

    /**
    * Inner class for type attributes.
    */
    public static class Type 
    {
        private int     maxLen     = 0;
        private boolean isNumeric  = false;
        private boolean isSigned   = false;
        private int     decimalLen = 0;
        /**
        * Sets attibutes for the type instance.
        * @param maxLen Number of bytes for the value.
        * @param numeric True if a numeric type, or false otherwise.
        * @param signed True if a signed numeric type, or false otherwise.
        * @param decLen Length of decimal part, 0 if not numeric or integer.
        */
        public Type(int maxLen, boolean numeric, boolean signed, int decLen) {
            this.maxLen     = maxLen;
            this.isNumeric  = numeric;
            this.isSigned   = signed;
            this.decimalLen = decLen;
        }
        /**
        * Returns the maximum length of the value.
        * @return The maximum length of the value.
        */
        public int getMaxLength() {
            return this.maxLen;
        }
        /**
        * Returns true if a numeric type, or false otherwise.
        * @return True if a numeric type, or false otherwise.
        */
        public boolean isNumeric() {
            return this.isNumeric;
        }
        /**
        * Returns true if a signed numeric type, or false otherwise.
        * @return True if a signed numeric type, or false otherwise.
        */
        public boolean isSigned() {
            return this.isSigned;
        }
        /**
        * Returns length of decimal part, 0 if not numeric or integer.
        * @return Length of decimal part, 0 if not numeric or integer.
        */
        public int getDecLength() {
            return this.decimalLen;
        }
        /**
        * Checks whether the object is of this type.
        * @param obj The object to be checked.
        * @return True if the object is of this type, or false otherwise.
        */
        public boolean isType(Object obj) {
            if (obj == null) {
                return false;
            } else
            if ((obj instanceof String) && (this == STRING)) {
                return true;
            } else
            if ((obj instanceof Boolean) && (this == BOOLEAN)) {
                return true;
            } else
            if ((obj instanceof GeoPoint) && (this == GPS)) {
                return true;
            } else
            if ((obj instanceof byte[]) && (this == BINARY)) {
                return true;
            } else
            if ((obj instanceof Long) && this.isNumeric()) {
                return true;
            } else
            if ((obj instanceof Double) && this.isNumeric()) {
                return true; // weak typing
            }
            return false;
        }
        /**
        * Returns multiplier based on the length of decimal part. Basically, it is 10^x where x is the
        * length of decimal part.
        * @return The multiplier.
        */
        public double getDecMultiplier() {
            switch (this.getDecLength()) {
                case  0: return   1.0;
                case  1: return  10.0;
                case  2: return 100.0;
                default: return   0.0;
            }
        }
        /**
        * Returns a long value representing the object. Returns 0 if the object is not numeric or null.
        * @param n The object to be converted.
        * @return A long value representing the object.
        */
        public long toLong(Object n) {
            if (!this.isNumeric() || (n == null)) {
                return 0L;
            } else
            if (this.getDecLength() == 0) {
                if (n instanceof Long) {
                    return ((Long)n).longValue();
                } else
                if (n instanceof Double) {
                    return ((Double)n).longValue();
                }
            } else { 
                if (n instanceof Long) {
                    return (long)((((Long)n).doubleValue() * this.getDecMultiplier()) + 0.5);
                } else
                if (n instanceof Double) {
                    return (long)((((Double)n).doubleValue() * this.getDecMultiplier()) + 0.5);
                }
            }
            return 0L;
        }
        /**
        * Returns the numberic value representing the long value. Returns null if this type is not
        * numeric; Returns a double value dividing the long value with the multiplier; Returns the long
        * value itself if the length of decimal is 0.
        * @param v The long value to be converted.
        * @return The numberic value representing the long value.
        */
        public Object toNumber(long v) {
            if (!this.isNumeric()) {
                return null;
            } else 
            if (this.getDecLength() == 0) {
                return new Long(v);
            } else {
                double d = (double)v / this.getDecMultiplier();
                return new Double(d);
            }
        }
    }
    
    // ------------------------------------------------------------------------

    public static final int SAVE                    = 0x8000;  // save to auxiliary storage
    public static final int READONLY                = 0x2000;  // read only
    public static final int WRITEONLY               = 0x1000;  // write only (ie. command)
   
    // ------------------------------------------------------------------------
    
    private int keyCode     = 0x0000;
    private String keyName  = null;
    private Type valType    = null;
    private int valAttr     = 0x0000;
    private int ndxSize     = 1;
    private String dftVal   = "";

    private CommandHandler cmd = null;
    
    private Object values[] = null;
    private boolean changed = false;
    private ChangeListener changeListener = null;

    // ------------------------------------------------------------------------

    /**
    * Constructs a pair of key/values with customized type, attribute, and size.
    * @param code The mapping key.
    * @param name The name of the key/values pair.
    * @param type The type of the values. Possible types: String, byte[], Boolean, Long, Double and
    *        GeoPoint.
    * @param attr The access attribute of the pair. Possible modes: Save, ReadOnly, WriteOnly.
    * @param ndxSize The number of values in the pair. It is limited to 1~5.
    * @param dft The value(s) of the pair.
    */
    public KeyValue(int code, String name, Type type, int attr, int ndxSize, String dft)
    {
        this.keyCode = code;
        this.keyName = (name != null)? name : "";
        this.valType = type;
        this.valAttr = attr;
        this.ndxSize = (ndxSize <= 0)? 1 : ((ndxSize > 5)? 5 : ndxSize);
        this.setDefaultValue(dft);
        this.resetToDefault();
    }

    /**
    * Constructs a command type key/value pair. The access attribute for commands is write-only.
    * @param code The mapping key.
    * @param name The name of the key/value pair.
    * @param cmd The command to be set as the value of the pair.
    */
    public KeyValue(int code, String name, CommandHandler cmd)
    {
        this(code, name, null/*type*/, WRITEONLY, 1/*maxLen*/, null/*dft*/);
        this.setCommandHandler(cmd);
    }

    // ------------------------------------------------------------------------

    /**
    * Returns true if the attribute of the values is read-only. 
    * @return True if the attribute of the values is read-only, or false otherwise.
    */
    public boolean isReadOnly()
    {
        return ((this.valAttr & READONLY) != 0);
    }
    
    /**
    * Sets the attribute of the values to be read-only.
    * @param readOnly True to set the pair to be read-only, or false otherwise.
    * @see org.opendmtp.j2me.util.KeyValue#isReadOnly().
    */
    public void setReadOnly(boolean readOnly)
    {
        if (readOnly) {
            this.valAttr |= READONLY;
        } else {
            this.valAttr &= ~READONLY;
        }
    }

    // ------------------------------------------------------------------------

    /**
    * Returns if the attribute of the values is write-only.
    * @return True if the attribute of the values is write-only, or false otherwise.
    */
    public boolean isWriteOnly()
    {
        return ((this.valAttr & WRITEONLY) != 0);
    }
    
    /**
    * Sets the attribute of the values to be write-only.
    * @param writeOnly True to set the pair to be write-only, or false otherwise.
    * @see org.opendmtp.j2me.util.KeyValue#isWriteOnly().
    */
    public void setWriteOnly(boolean writeOnly)
    {
        if (writeOnly) {
            this.valAttr |= WRITEONLY;
        } else {
            this.valAttr &= ~WRITEONLY;
        }
    }

    // ------------------------------------------------------------------------

    /**
    * Returns if the attribute of the values is save.
    * @return True if the attribute of the values is save, or false otherwise.
    */
    public boolean isSave()
    {
        return ((this.valAttr & SAVE) != 0);
    }
    
    /**
    * Sets the attribute of the values to be save.
    * @param save True to set the pair to be save, or false otherwise.
    * @see org.opendmtp.j2me.util.KeyValue#isSave().
    */
    public void setSave(boolean save)
    {
        if (save) {
            this.valAttr |= SAVE;
        } else {
            this.valAttr &= ~SAVE;
        }
    }
    
    // ------------------------------------------------------------------------

    /**
    * Returns if the pair has been changed since saved.
    * @return True if the pair has been changed since initiated, or false otherwise.
    */
    public boolean hasChanged()
    {
        return this.changed;
    }
    
    /**
    * Sets the 'changed' status
    */
    public void setChanged()
    {
        this.changed = true;
    }

    /**
    * Clears the 'changed' status
    */
    public void clearChanged()
    {
        this.changed = false;
    }

    // ------------------------------------------------------------------------

    /**
    * Checks if the pair still holds the default values when it was initiaized.
    * @return True if the current value and the default value are the same, or false
    *         otherwise.
    */
    public boolean isDefault()
    {
        String dft = this.getDefaultValue();
        String val = this.getValueString();
        if (dft == val) {
            // (null == null), (dft == dft), etc
            return true;
        } else
        if ((dft != null) && (val != null) && dft.equals(val)) {
            // String values of default and value are identical
            return true;
        } else {
            // value/default differ
            //Log.debug(LOG_NAME, "[val!=dft] '" + val + "' != '" + dft + "'");
            return false;
        }
    }

    /**
    * Sets the values back to the default ones, and changes the state to be un-changed.
    */
    public void resetToDefault() 
    {
        this.initFromString(this.getDefaultValue());
        this.clearChanged();
    }

    // ------------------------------------------------------------------------
    
    /**
    * Sets values by parsing the parameter String, and sets them as the default values if parameter
    * <tt>setAsDefault</tt> is True.
    * @param val A String representing the values to be set.
    * @param setAsDefault If true, set the new values to be the default values.
    * @return True if succeed, or false otherwise.
    */
    public boolean initFromString(String val, boolean setAsDefault) 
    {
        if (this.initFromString(val)) {
            if (setAsDefault) {
                this.setDefaultValue(val);
            }
            return true;
        } else {
            return false;
        }
    }

    /**
    * Sets values by parsing the parameter String. 
    * @param val A String representing the values to be set.
    * @return True if succeed, or false if failed or the type is command (which can not be set from
    *         String).
    */
    public boolean initFromString(String val) 
    {
        // does not invoke change listener
        this.values = null; // reset
        Object v[] = this.getValues();
        Type type = this.getValueType();

        /* first check for non-array types */
        if (type == COMMAND) {
            // command
            return false;
        } else
        if (type == GPS) {
            String g[] = StringTools.parseString(val, ',');
            if (g.length >= 3) {
                long fixtime = (g.length > 0)? StringTools.parseLong  (g[0], 0L) :  0L;
                double lat   = (g.length > 1)? StringTools.parseDouble(g[1],0.0) : 0.0;
                double lon   = (g.length > 2)? StringTools.parseDouble(g[2],0.0) : 0.0;
                v[0] = new GeoPoint(lat, lon, fixtime);
            } else {
                v[0] = new GeoPoint(); // invalid point
            }
            return true;
        } else
        if (type == STRING) {
            v[0] = val;
            return true;
        } else
        if (type == BINARY) {
            byte b[] = StringTools.parseHex(val, new byte[0]);
            v[0] = b;
            return true;
        }
        
        /* handle array types */
        String d[] = StringTools.parseString(val, ',');
        if (type == BOOLEAN) {
            for (int i = 0; (i < d.length) && (i < this.ndxSize); i++) {
                v[i] = new Boolean(StringTools.parseBoolean(d[i],false));
            }
            return true;
        } else
        if (type == UINT32) {
            for (int i = 0; (i < d.length) && (i < this.ndxSize); i++) {
                v[i] = new Long(StringTools.parseLong(d[i],0L));
            }
            return true;
        } else
        if (type == UINT16) {
            for (int i = 0; (i < d.length) && (i < this.ndxSize); i++) {
                v[i] = new Long(StringTools.parseLong(d[i],0L));
            }
            return true;
        } else
        if (type == UINT8) {
            for (int i = 0; (i < d.length) && (i < this.ndxSize); i++) {
                v[i] = new Long(StringTools.parseLong(d[i],0L));
            }
            return true;
        } else
        if (type == UDEC32) {
            for (int i = 0; (i < d.length) && (i < this.ndxSize); i++) {
                v[i] = new Double(StringTools.parseDouble(d[i],0.0));
            }
            return true;
        } else
        if (type == UDEC16) {
            for (int i = 0; (i < d.length) && (i < this.ndxSize); i++) {
                v[i] = new Double(StringTools.parseDouble(d[i],0.0));
            }
            return true;
        } else {
            // invalid type 
            return false;
        }
    }

    // ------------------------------------------------------------------------

    /**
    * Returns the key code.
    * @return The key code.
    */
    public int getKeyCode() 
    {
        return this.keyCode;
    }

    /**
    * Returns a String containing the key code in Hex.
    * @return A String containing the key code in Hex.
    */
    public String getKeyCodeString()
    {
        // return code as String (ie. return 0x1234 as "0x1234")
        return StringTools.toHexString((long)this.getKeyCode(), 16);
    }
    
    /**
    * Returns the name.
    * @return The name of the key/values pair.
    */
    public String getKeyName() 
    {
        return this.keyName;
    }

    /**
    * Returns the name.
    * @return The name of the key/values pair.
    */
    public String getName()
    {
        return this.getKeyName();
    }

    // ------------------------------------------------------------------------

    /**
    * Provides an interface for the value of type Command. 
    * Note that this interface is never used in the Java reference implementation
    */
    public interface CommandHandler
    {
        /**
        * Sets the key/value pair with type command.
        * @param key The mapping key.
        * @param ndx The number of values (commands).
        * @param value The values.
        * @return 0 if succeeds, a positive number if failed.
        */
        public int command(int key, int ndx, Object value);
    }

    /**
    * Returns the command if the type is command or null otherwise.
    * @return The command if the type is command or null otherwise.
    */
    public CommandHandler getCommandHandler()
    {
        return (this.getValueType() == COMMAND)? this.cmd : null;
    }

    /**
    * Sets the command value if the type is command and returns true is succeeds.
    * @param cmd The command to be set.
    * @return True if the type is command and <tt>cmd</tt> is set, or false otherwise.
    */
    public boolean setCommandHandler(CommandHandler cmd)
    {
        if (this.getValueType() == COMMAND) {
            this.cmd = cmd;
            return true;
        } else {
            return false;
        }
    }

    /**
    * Returns if the type is command and the command is set.
    * @return True if the type is command and the command is set, or false otherwise.
    */
    public boolean isCommand()
    {
        if (this.cmd != null) {
            return true;
        } else {
            return false;
        }
    }

    // ------------------------------------------------------------------------

    /**
    * Provides an interface for listening property change events.
    */
    public interface ChangeListener
    {
        /**
        * Invoked when reading the value in a <tt>KeyValue</tt> object.
        * @param kv The <tt>KeyValue</tt> object being read.
        * @param ndx The index of value in the value array being read.
        */
        public void updateProperty(KeyValue kv, int ndx);
        /**
        * Invoked when changing the value in a <tt>KeyValue</tt> object.
        * @param kv The <tt>KeyValue</tt> object being changed.
        * @param ndx The index of value in the value array being changed.
        */
        public void propertyChanged(KeyValue kv, int ndx);
    }

    /**
    * Returns the <tt>ChangeListener</tt> associated.
    * @return The <tt>ChangeListener</tt> associated.
    */
    protected ChangeListener getChangeListener()
    {
        return this.changeListener;
    }

    /**
    * Sets the <tt>ChangeListener</tt> associated.
    * @param listener The <tt>ChangeListener</tt> to be associated.
    */
    public void setChangeListener(ChangeListener listener)
    {
        this.changeListener = listener;
    }
    
    /**
    * Invokes reading event for the <tt>ChangeListener</tt>.
    * @param ndx The index of the value in the array being read.
    */
    protected void fireUpdateProperty(int ndx)
    {
        ChangeListener cl = this.getChangeListener();
        if (cl != null) {
            cl.updateProperty(this, ndx);
        }
    }
    
    /**
    * Invokes writing event for the <tt>ChangeListener</tt>.
    * @param ndx The index of the value in the array being changed.
    */
    protected void firePropertyChanged(int ndx)
    {
        ChangeListener cl = this.getChangeListener();
        if (cl != null) {
            cl.propertyChanged(this, ndx);
        }
    }

    // ------------------------------------------------------------------------

    /**
    * Returns the type of the value.
    * @return The type of the value.
    */
    public Type getValueType() 
    {
        return this.valType;
    }
    
    /**
    * Returns the attribute of the value.
    * @return The attribute of the value.
    */
    public int getAttributes()
    {
        return this.valAttr;
    }

    // ------------------------------------------------------------------------

    /**
    * Returns the size of the value array.
    * @return The size of the value array.
    */
    public int getIndexSize()
    {
        return this.ndxSize;
    }
    
    /**
    * Pre-processes the requested index of the value. Changes it to 0 if the size is not set, to the
    * last one if the index if larger than the size.
    * @param ndx The requested index of the value.
    * @return The legal index for accessing issues.
    */
    private int index(int ndx)
    {
        return (ndx < 0)? 0 : ((ndx >= this.ndxSize)? (this.ndxSize - 1) : ndx);
    }

    // ------------------------------------------------------------------------

    /**
    * Sets the default value String. If the parameter null, sets the String to be empty.
    * @param dft The value String to be set.
    */
    public void setDefaultValue(String dft) 
    {
        this.dftVal = (dft != null)? dft : "";
    }

    /**
    * Gets the default value String.
    * @return The default value String.
    */
    public String getDefaultValue() 
    {
        return this.dftVal;
    }
    
    /**
    * Gets the value array. If the array is null, creates a new array of the attributed size and
    * returns it.
    * @return The value array.
    */
    public Object[] getValues()
    {
        if (this.values == null) {
            this.values = new Object[this.ndxSize];
        }
        return this.values;
    }
    
    /**
    * Gets the value at the desired index from the value array.
    * @param ndx The desired index of the value.
    * @return The value at the desired index from the value array.
    */
    public Object getValue(int ndx)
    {
        if (this.isCommand()) {
            return null;
        } else {
            int n = this.index(ndx);
            this.fireUpdateProperty(n);
            return this.getValues()[n];
        }
    }
    
    /**
    * Sets the value at the desired index in the value array.
    * @param val The value to be set.
    * @param ndx The desired index.
    * @return True if succeeds, or false otherwise.
    */
    public boolean setValue(Object val, int ndx)
    {
        int n = this.index(ndx);
        if (this.isCommand()) {
            CommandHandler ch = this.getCommandHandler();
            ch.command(this.getKeyCode(), n, val);
            return true;
        } else
        if ((val == null) || this.getValueType().isType(val)) {
            this.getValues()[n] = val;
            this.setChanged();
            this.firePropertyChanged(n);
            return true;
        } else {
            // invalid type 
            return false;
        }
    }

    // ------------------------------------------------------------------------

    /**
    * Returns a String containing the values delimetered by ",".
    * @return A String containing the values delimetered by ",".
    */
    public String getValueString()
    {
        return this.getValueString(new StringBuffer());
    }

    /**
    * Returns a String containing the values delimetered by ",". Note that the parameter
    * <tt>StringBuffer</tt> is also set with the same content.
    * @param sb Used for processing. Also set with the same content as the return String.
    * @return A String containing the values delimetered by ",".
    */
    public String getValueString(StringBuffer sb)
    {
        if (sb == null) { sb = new StringBuffer(); }
        Object v[] = this.getValues();
        for (int i = 0; i < v.length; i++) {
            if (i > 0) { 
                sb.append(",");
            }
            if (v[i] != null) {
                sb.append(v[i].toString());
            }
        }
        return sb.toString();
    }

    // ------------------------------------------------------------------------

    /**
    * Returns the boolean value at the desired index. Returns the boolean parameter as default value
    * when the type of the obtained value is not boolean.
    * @param ndx The desired index.
    * @param dft The default value.
    * @return The boolean value at the desired index, or the parameter dft as default value when the
    *         type of the obtained value is not boolean.
    */
    public boolean getBoolean(int ndx, boolean dft)
    {
        Object obj = this.getValue(ndx);
        if (obj instanceof Boolean) {
            return ((Boolean)obj).booleanValue();
        }
        return dft;
    }

    /**
    * Sets the boolean value at the desired index.
    * @param val The desired boolean value.
    * @param ndx The desired index.
    * @return True if succeeds, or false if type mismatch or other errors occur.
    */
    public boolean setBoolean(boolean val, int ndx)
    {
        return this.setValue(new Boolean(val), ndx);
    }

    // ------------------------------------------------------------------------

    /**
    * Returns the long value at the desired index. Returns the long parameter as default value when
    * the type of the obtained value is not numeric.
    * @param ndx The desired index.
    * @param dft The default value.
    * @return The long value at the desired index, or the parameter dft as default value when the
    *         type of the obtained value is not numeric.
    */
    public long getLong(int ndx, long dft) 
    {
        Object obj = this.getValue(ndx);
        if (obj instanceof Long) {
            return ((Long)obj).longValue();
        } else
        if (obj instanceof Double) {
            return ((Double)obj).longValue();
        }
        return dft;
    }

    /**
    * Sets the long value at the desired index.
    * @param val The desired long value.
    * @param ndx The desired index.
    * @return True if succeeds, or false if type mismatch or other errors occur.
    */
    public boolean setLong(long val, int ndx)
    {
        return this.setValue(new Long(val), ndx);
    }

    // ------------------------------------------------------------------------

    /**
    * Returns the double value at the desired index. Returns the double parameter as default value
    * when the type of the obtained value is not numeric.
    * @param ndx The desired index.
    * @param dft The default value.
    * @return The double value at the desired index, or the parameter dft as default value when the
    *         type of the obtained value is not numeric.
    */
    public double getDouble(int ndx, double dft) 
    {
        Object obj = this.getValue(ndx);
        if (obj instanceof Double) {
            return ((Double)obj).doubleValue();
        } else
        if (obj instanceof Long) {
            return ((Long)obj).doubleValue();
        }
        return dft;
    }

    /**
    * Sets the double value at the desired index.
    * @param val The desired double value.
    * @param ndx The desired index.
    * @return True if succeeds, or false if type mismatch or other errors occur.
    */
    public boolean setDouble(double val, int ndx)
    {
        return this.setValue(new Double(val), ndx);
    }

    // ------------------------------------------------------------------------

    /**
    * Returns a String containing the first value. Returns the String parameter as default value when
    * the first value is null.
    * @param dft The default String.
    * @return A String containing the first value, or the parameter dft as default value when the
    *         first value is null.
    */
    public String getString(String dft)
    {
        Object obj = this.getValue(0);
        if (obj == null) {
            return dft;
        } else
        if (obj instanceof byte[]) {
            byte b[] = (byte[])obj;
            if (b.length > 0) {
                return "0x" + StringTools.toHexString((byte[])obj);
            } else {
                return "";
            }
        } else {
            return obj.toString();
        }
    }

    /**
    * Sets the first value to be the desired String. If the parameter is null, creates an empty
    * String and sets it to be the first value.
    * @param val The desired String.
    * @return True if succeeds, or false if type mismatch or other errors occur.
    */
    public boolean setString(String val)
    {
        return this.setValue(((val != null)? val : ""), 0);
    }

    // ------------------------------------------------------------------------

    /**
    * Generates a <tt>Payload</tt> instance containing the data of the first value.
    * @param maxLen The maximum length of the Payload data.
    * @return A <tt>Payload</tt> instance containing the data of the first value.
    */
    public Payload getPayload(int maxLen)
    {
        Payload p = new Payload();
        int ndxSize = this.getIndexSize();
        Type type = this.getValueType();
        
        /* non array types */
        if (type == BINARY) {
            byte b[] = (byte[])this.getValue(0);
            int len = ((maxLen < 0) || (b.length < maxLen))? b.length : maxLen;
            p.writeBytes(b, len);
            return p;
        } else
        if (type == STRING) {
            String s = (String)this.getValue(0);
            int len = ((maxLen < 0) || (s.length() < maxLen))? s.length() : maxLen;
            p.writeString(s, len);
            return p;
        } else
        if (type == GPS) {
            if ((maxLen >= 0) && (maxLen < 10)) {
                // not enough room
                return null;
            } else {
                // (maxLen < 0) || (maxLen >= 10)
                GeoPoint g = (GeoPoint)this.getValue(0);
                p.writeLong(g.getFixtime(), 4);
                p.writeGPS(g, (maxLen > 0)? (maxLen - 4) : 6);
                return p;
            }
        }
        
        /* bytes per index (applicable to array types only) */
        int maxBpi = type.getMaxLength();
        int bpi = maxBpi; // bytes per index
        if (maxLen >= 0) {
            bpi = maxLen / ndxSize;
            if (bpi > maxBpi) { 
                bpi = maxBpi; 
            } else
            if (bpi <= 0) {
                // not enough room
                return null;
            }
        }

        /* array types */
        if (type == BOOLEAN) {
            for (int i = 0; i < ndxSize; i++) {
                Boolean b = (Boolean)this.getValue(i);
                p.writeLong(b.booleanValue()?1:0, 1);
            }
            return p;
        } else
        if (type.isNumeric()) {
            for (int i = 0; i < ndxSize; i++) {
                Object n = this.getValue(i);
                long v = type.toLong(n);
                p.writeLong(v, bpi);
            }
            return p;
        }
        
        /* invalid type */
        return null;
        
    }

    /**
    * Sets the value using the data of the <tt>Payload</tt>.
    * @param p The Payload containing the desired data.
    * @return True if succeeds, or false otherwise.
    */
    public boolean setPayload(Payload p)
    {
        int ndxSize = this.getIndexSize();
        Type type = this.getValueType();
        
        /* non array types */
        if (type == BINARY) {
            byte b[] = p.readBytes(p.getAvail());
            this.setByteArray(b);
            return true;
        } else
        if (type == STRING) {
            String s = p.readString(p.getAvail());
            this.setString(s);
            return true;
        } else
        if (type == GPS) {
            long n = p.readULong(4, 0L); // fixtime
            GeoPoint g = p.readGPS(p.getAvail());
            g.setFixtime(n);
            this.setGeoPoint(g);
            return true;
        }
        
        /* bytes per index (applicable to array types only) */
        int maxBpi = type.getMaxLength();
        int bpi = p.getAvail() / ndxSize;
        if (bpi > maxBpi) { 
            bpi = maxBpi; 
        } else
        if (bpi <= 0) {
            // not enough room
            return false;
        }
        
        /* array types */
        if (type == BOOLEAN) {
            for (int i = 0; i < ndxSize; i++) {
                boolean b = (p.readULong(1, 0L) != 0L)? true : false;
                this.setBoolean(b, i);
            }
            return true;
        } else
        if (type.isNumeric()) {
            for (int i = 0; i < ndxSize; i++) {
                long n = p.readULong(bpi, 0L);
                this.setValue(type.toNumber(n), i);
            }
            return true;
        }
        
        /* invalid type */
        return false;

    }

    // ------------------------------------------------------------------------

    /**
    * Returns the first value as byte[]. Returns the byte[] parameter as default value when the first
    * value is not byte[] type.
    * @param dft The default byte[].
    * @return The first byte[] value, or the parameter dft as default value when the first value is
    *         byte[] type.
    */
    public byte[] getByteArray(byte dft[])
    {
        Object obj = this.getValue(0);
        if (obj instanceof byte[]) {
            return (byte[])obj;
        }
        return dft;
    }

    /**
    * Sets the first value to be the desired byte[]. If the parameter is null, creates a new empty
    * byte[] and sets it to the first value.
    * @param val The desired byte[].
    * @return True if succeeds, or false if type mismatch or other errors occur.
    */
    public boolean setByteArray(byte val[])
    {
        return this.setValue(((val != null)? val : new byte[0]), 0);
    }

    // ------------------------------------------------------------------------

    /**
    * Returns the first value as GeoPoint. Returns the GeoPoint parameter as default value when the
    * first value is not GeoPoint type.
    * @param dft The default GeoPoint.
    * @return The first GeoPoint value, or the parameter dft as default value when the first value is
    *         GeoPoint type.
    */
    public GeoPoint getGeoPoint(GeoPoint dft)
    {
        Object obj = this.getValue(0);
        if (obj instanceof GeoPoint) {
            return (GeoPoint)obj;
        }
        return dft;
    }

    /**
    * Sets the first value to be the desired GeoPoint. If the parameter is null, creates a new
    * GeoPoint and sets it to the first value.
    * @param val The desired GeoPoint.
    * @return True if succeeds, or false if type mismatch or other errors occur.
    */
    public boolean setGeoPoint(GeoPoint val)
    {
        return this.setValue(((val != null)? val : new GeoPoint()), 0);
    }

    // ------------------------------------------------------------------------
    
    /**
    * Returns a String in the format "Key=Value".
    * @return The String in the format "Key=Value".
    */
    public String toString()
    {
        StringBuffer sb = new StringBuffer();
        sb.append(this.getKeyName()).append("=");
        return this.getValueString(sb);
    }
   
    // ------------------------------------------------------------------------

    /**
    * Parses the String in the format "key=value" to a String array as {key, value}. If the String is
    * not in the correct format, returns a String array as {String, ""}.
    * @param kv The String in the format "key=value".
    * @return A String array as {key, value}.
    */
    public static String[] parseKeyValue(String kv)
    {
        if (kv != null) {
            int p = kv.indexOf('=');
            if (p >= 0) {
                String key = kv.substring(0,p);
                String val = kv.substring(p+1);
                return new String[] { key, val };
            } else {
                return new String[] { kv, "" };
            }
        } else {
            return null;
        }
    }
    
    // ------------------------------------------------------------------------

}
