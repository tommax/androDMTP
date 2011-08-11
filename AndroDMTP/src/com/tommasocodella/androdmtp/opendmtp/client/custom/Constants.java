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
//  Custom constants.
// ----------------------------------------------------------------------------
// Change History:
//  2006/03/26  Martin D. Flynn
//      Initial release
//  2006/11/03  Elayne Man
//      Include JavaDocs
// ----------------------------------------------------------------------------
package com.tommasocodella.androdmtp.opendmtp.client.custom;

/**
* Constants interface class.
*/
public interface Constants
{
    
    /** Minimum transmit rate in seconds. */
    public static final long MIN_XMIT_RATE              = 5L;     // seconds
    
    /** Absolute minimum transmit delay in seconds. */
    public static final long MIN_XMIT_DELAY             = 5L;      // seconds
    
    /** Minimum in-motion interval in seconds. */
    public static final long MIN_IN_MOTION_INTERVAL     = 60L;      // seconds
    
    /** Minimum dormant interval in seconds. */
    public static final long MIN_DORMANT_INTERVAL       = 300L;     // seconds

}

