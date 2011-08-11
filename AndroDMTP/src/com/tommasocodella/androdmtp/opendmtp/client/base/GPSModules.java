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
//  This class is a container for GPS based event modules.  During initialization
//  modules witl be added to this class via 'addModule', and will later be
//  invoked periodically via 'checkGPS'.
// ----------------------------------------------------------------------------
// Change History:
//  2006/03/26  Martin D. Flynn
//      Initial release
//  2006/11/03  Joshua Stupplebeen
//      Include JavaDocs
// ----------------------------------------------------------------------------
package com.tommasocodella.androdmtp.opendmtp.client.base;

import java.util.Enumeration;
import java.util.Vector;

import com.tommasocodella.androdmtp.opendmtp.util.GeoEvent;

/**
* Implements modules for the handling of GPS event data.
*/
public class GPSModules
{
    
    // ------------------------------------------------------------------------
    
    private Vector gpsModules = null;
    
    /**
    * default constructor
    */
    public GPSModules()
    {
        this.gpsModules = new Vector();
    }
   
    // ------------------------------------------------------------------------

    /**
    * Adds a module to the list of modules.
    * @param mod GPSModule.Module new gps module
    */
    public void addModule(GPSModules.Module mod)
    {
        if ((mod != null) && !this.gpsModules.contains(mod)) {
            this.gpsModules.addElement(mod);
        }
    }
        
    // ------------------------------------------------------------------------

    /**
    * Iterates through the contained list recursively calling 'checkGPS' for
    * each of the added modules.
    * @param oldFix old/prior gps fix.
    * @param newFix new gps fix.
    */
    public void checkGPS(GeoEvent oldFix, GeoEvent newFix)
    {
        for (Enumeration i = this.gpsModules.elements(); i.hasMoreElements();) {
            GPSModules.Module m = (GPSModules.Module)i.nextElement();
            m.checkGPS(oldFix, newFix);
        }
    }
    
    // ------------------------------------------------------------------------

    /**
    * Module interface
    */
    public interface Module
    {
        /**
        * Allows the called module to act on the new gps fix
        * @param oldFix old/prior gps fix.
        * @param newFix new gps fix.
        */
        public void checkGPS(GeoEvent oldFix, GeoEvent newFix);
    }

}
