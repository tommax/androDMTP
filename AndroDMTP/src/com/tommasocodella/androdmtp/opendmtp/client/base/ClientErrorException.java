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
//  This class contains client-side protocol and property exceptions.
// ----------------------------------------------------------------------------
// Change History:
//  2006/03/26  Martin D. Flynn
//      Initial release
//  2006/11/03  Joshua Stupplebeen
//      Include JavaDocs
// ----------------------------------------------------------------------------
package com.tommasocodella.androdmtp.opendmtp.client.base;

import com.tommasocodella.androdmtp.opendmtp.codes.ClientErrors;
import com.tommasocodella.androdmtp.opendmtp.util.StringTools;

/**
* Exception which gets thrown when errors occur during client activities.
*/
public class ClientErrorException
    extends Exception
    implements ClientErrors
{
    
    // ------------------------------------------------------------------------

    private int     clientError    = 0x0000;
    private int     commandError   = 0x0000;

    // ------------------------------------------------------------------------

    /**
    * Exception constructor taking an integer error code as a parameter.
    * @param clientError integer error code.
    */
    public ClientErrorException(int clientError) 
    {
        super();
        this.clientError = clientError;
    }

    /**
    * Exception constructor taking an integer error code and integer
    * command error code as parameters.
    * @param clientError integer error code.
    * @param commandError integer command error code.
    */
    public ClientErrorException(int clientError, int commandError) 
    {
        super();
        this.clientError  = clientError;
        this.commandError = commandError;
    }

    // ------------------------------------------------------------------------

    /**
    * Returns the client error code.
    * @return int client error code.
    */
    public int getClientError()
    {
        return this.clientError;
    }
    
    /**
    * Returns the command error code.
    * @return int command error code.
    */
    public int getCommandError()
    {
        return this.commandError;
    }

    // ------------------------------------------------------------------------

    /**
    * Returns a String representation of this instance
    * @return String "ClientError ['clientError']";
    */
    public String toString() 
    {
        int errCode = this.getClientError();
        StringBuffer sb = new StringBuffer();
        sb.append("ClientError [");
        sb.append(StringTools.toHexString(errCode,16));
        sb.append("] ");
        return sb.toString();
    }
    
    // ------------------------------------------------------------------------

}
