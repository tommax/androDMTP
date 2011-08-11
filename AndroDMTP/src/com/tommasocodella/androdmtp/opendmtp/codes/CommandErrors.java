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
//  OpenDMTP protocol command error constants.
// ----------------------------------------------------------------------------
// Change History:
//  2006/03/26  Martin D. Flynn
//      Initial release
//  2006/11/03  Robert Puckett
//      Include JavaDocs
// ----------------------------------------------------------------------------
package com.tommasocodella.androdmtp.opendmtp.codes;

/**
* Contains OpenDMTP protocol command error constants.
*/
public interface CommandErrors
{
    
// ----------------------------------------------------------------------------
// Command success

    /**
    * Contains a constant signifying the command success for when command execution was successful
    * (no error return to server).
    */
    public static final int COMMAND_OK                          = 0x0000;
    // Description:
    //      Command execution was successful (no error return to server)

    /**
    * Contains a constant signifying the command success for when command execution was successful
    * (Acknowledgement returned to server).
    */
    public static final int COMMAND_OK_ACK                      = 0x0001;
    // Description:
    //      Command execution was successful (Acknowledgement returned to server)

// ----------------------------------------------------------------------------
// Command argument errors
    
    /**
    * Contains a constant signifying the command argument errors for insufficient/invalid command
    * arguments.
    */
    public static final int COMMAND_ARGUMENTS                   = 0xF011;
    // Description:
    //      Insufficient/Invalid command arguments

    /**
    * Contains a constant signifying the command argument errors for when an index specified in the
    * command arguments is invalid.
    */
    public static final int COMMAND_INDEX                       = 0xF012;
    // Description:
    //      An index specified in the command arguments is invalid

    /**
    * Contains a constant signifying the command argument errors for when a status code specified in
    * the command arguments is invalid.
    */
    public static final int COMMAND_STATUS                      = 0xF013;
    // Description:
    //      A status code specified in the command arguments is invalid

    /**
    * Contains a constant signifying the command argument errors for when a length specified in the
    * command arguments is invalid.
    */
    public static final int COMMAND_LENGTH                      = 0xF014;
    // Description:
    //      A length specified in the command arguments is invalid

    /**
    * Contains a constant signifying the command argument errors for when an ID/Filename specified in
    * the command arguments is invalid.
    */
    public static final int COMMAND_NAME                        = 0xF015;
    // Description:
    //      An ID/Filename specified in the command arguments is invalid

    /**
    * Contains a constant signifying the command argument errors for when a checksum specified in the
    * command arguments is invalid.
    */
    public static final int COMMAND_CHECKSUM                    = 0xF016;
    // Description:
    //      A checksum specified in the command arguments is invalid

    /**
    * Contains a constant signifying the command argument errors for when an offset specified in the
    * command arguments is invalid.
    */
    public static final int COMMAND_OFFSET                      = 0xF017;
    // Description:
    //      An offset specified in the command arguments is invalid
    
// ----------------------------------------------------------------------------
// Command execution errors
    
    /**
    * Contains a constant signifying the command execution error for when the client has determined
    * that the execution of the command has failed. (no specific reason)
    */
    public static final int COMMAND_EXECUTION                   = 0xF511;
    // Description:
    //      The client has determined that the execution of the command has failed
    //      (no specific reason)

    /**
    * Contains a constant signifying the command execution error for when the client has determined
    * that the execution of the command has failed due to hardware failure.
    */
    public static final int COMMAND_HARDWARE_FAILURE            = 0xF521;
    // Description:
    //      The client has determined that the execution of the command has failed
    //      due to hardware failure.
    
// ----------------------------------------------------------------------------
// Generic Command errors
// Create desired aliases for these to define specific custom errors

    /** Contains a constant signifying a generic command error #0. */
    public static final int COMMAND_ERROR_00                    = 0xFE00;
    /** Contains a constant signifying a generic command error #1. */
    public static final int COMMAND_ERROR_01                    = 0xFE01;
    /** Contains a constant signifying a generic command error #2. */
    public static final int COMMAND_ERROR_02                    = 0xFE02;
    /** Contains a constant signifying a generic command error #3. */
    public static final int COMMAND_ERROR_03                    = 0xFE03;
    /** Contains a constant signifying a generic command error #4. */
    public static final int COMMAND_ERROR_04                    = 0xFE04;
    /** Contains a constant signifying a generic command error #5. */
    public static final int COMMAND_ERROR_05                    = 0xFE05;
    /** Contains a constant signifying a generic command error #6. */
    public static final int COMMAND_ERROR_06                    = 0xFE06;
    /** Contains a constant signifying a generic command error #7. */
    public static final int COMMAND_ERROR_07                    = 0xFE07;

// ----------------------------------------------------------------------------
// Command execution errors

    /**
    * /** Contains a constant signifying a command execution error for when a requested command
    * feature is not supported.
    */
    public static final int COMMAND_FEATURE_NOT_SUPPORTED       = 0xFF01;
    // Description:
    //      A requested command feature is not supported.

// ----------------------------------------------------------------------------

}
