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
//  This class contains exceptions occurring while attempting to communication
//  with a GPS receiver.
// ----------------------------------------------------------------------------
// Change History:
//  2006/03/26  Martin D. Flynn
//      Initial release
//  2006/11/03  Robert S. Brewer
//      Include JavaDocs
// ----------------------------------------------------------------------------
package com.tommasocodella.androdmtp.opendmtp.client.gps;

/**
* This class contains exceptions occurring while attempting to communicate with
* a GPS receiver.
*/
public class GPSException
    extends Exception
{

    // ------------------------------------------------------------------------

    private Throwable exception = null;
    
    /**
    * Sets the message associated with this exception.
    * @param msg message to be set.
    */
    public GPSException(String msg)
    {
        super(msg);
    }

    // ------------------------------------------------------------------------

    /**
    * Sets the message and some related exception associated with this exception.
    * @param msg message to be set.
    * @param t a related exception
    */
    public GPSException(String msg, Throwable t) 
    {
        this(msg);
        this.exception = t;
    }

    // ------------------------------------------------------------------------

    /**
    * Accessor for related exception field.
    * @return the related exception.
    */
    public Throwable getException() 
    {
        return this.exception;
    }

    // ------------------------------------------------------------------------

}
