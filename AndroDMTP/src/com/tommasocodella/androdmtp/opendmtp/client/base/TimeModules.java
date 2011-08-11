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
//  This class is a container for time based event modules. During initialization
//  modules witl be added to this class via 'addModule', and will later be
//  invoked periodically via 'checkTime'.
// ----------------------------------------------------------------------------
// Change History:
//  2006/03/26  Martin D. Flynn
//      Initial release
//  2006/11/03  Elayne Man
//      Include JavaDocs
// ----------------------------------------------------------------------------
package com.tommasocodella.androdmtp.opendmtp.client.base;

import java.util.Enumeration;
import java.util.Vector;

/**
* Represents Time Modules.
*/
public class TimeModules
{
    
    // ------------------------------------------------------------------------
    
    private Vector timeModules = null;
    
    /**
    * Default constructor for Time Modules.
    */
    public TimeModules()
    {
        this.timeModules = new Vector();
    }
   
    // ------------------------------------------------------------------------

    /**
    * Adds a time module.
    * @param mod The time module to add.
    */
    public void addModule(TimeModules.Module mod)
    {
        if ((mod != null) && !this.timeModules.contains(mod)) {
            this.timeModules.addElement(mod);
        }
    }
        
    // ------------------------------------------------------------------------

    /**
    * Calls 'checkTime' on all contained Modules
    * @param time The time value
    */
    public void checkTime(long time)
    {
        for (Enumeration i = this.timeModules.elements(); i.hasMoreElements();) {
            TimeModules.Module m = (TimeModules.Module)i.nextElement();
            m.checkTime(time);
        }
    }
    
    // ------------------------------------------------------------------------

    /**
    * Interface TimeModules.Module.
    */
    public interface Module
    {
        /**
        * Check time expiration
        * @param time time.
        */
        public void checkTime(long time);
    }

}
