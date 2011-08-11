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
//  This class handles the debug/info/warning/error logs for all other classes
//  in the J2ME application.
// ----------------------------------------------------------------------------
// Change History:
//  2006/03/26  Martin D. Flynn
//      Initial release
//  2006/11/03  Guanghong Yang
//      Include JavaDocs
// ----------------------------------------------------------------------------
package com.tommasocodella.androdmtp.opendmtp.util;

import java.io.File;
import java.io.PrintStream;
import java.lang.System;

/**
* Handles the debug/info/warning/error logs for all other classes in the J2ME application.
*/
public class Log
{
    
    // ----------------------------------------------------------------------------

    public static final int LOG_NONE    = 0;
    public static final int LOG_ERROR   = 1;
    public static final int LOG_WARN    = 3;
    public static final int LOG_INFO    = 5;
    public static final int LOG_DEBUG   = 7;
    
    private static int currentLogLevel  = LOG_DEBUG;
    
    /**
    * Sets the log level of details.
    * @param level The log level of details.
    */
    public static void setLogLevel(int level)
    {
        Log.currentLogLevel = level;
    }
    
    /**
    * Returns the log level of details.
    * @return The log level of details.
    */
    public static int getLogLevel()
    {
        return Log.currentLogLevel;
    }

    // ----------------------------------------------------------------------------

    /** The output target. */
    private static PrintStream logOutput = new PrintStream(System.out);
    
    /**
    * Outputs the log.
    * @param type The tag of the log. Outputs it if not null.
    * @param name The name of the log.
    * @param msg The message of the log.
    */
    private static void _println(String type, String name, String msg)
    {
        if (Log.logOutput != null) {
            StringBuffer sb = new StringBuffer();
            if ((type != null) || (name != null)) {
                sb.append("[");
                if (type != null) {
                    sb.append(type).append(":");
                }
                if (name != null) {
                    sb.append(name);
                } else {
                    sb.append("?");
                }
                sb.append("] ");
            }
            sb.append((msg != null)? msg : "");
            System.out.println(sb.toString());
            Log.logOutput.println(sb.toString());
        }else{
        	System.err.println("Log config error");
        }
    }
    
    // ----------------------------------------------------------------------------

    /**
    * Logs the message with no tag.
    * @param name The name of the message.
    * @param msg The content of the message.
    */
    public static void println(String name, String msg)
    {
        Log._println(null, name, msg);
    }
    
    // ----------------------------------------------------------------------------

    /**
    * If log level is equal to or above DEBUG level, logs the message with tag "Debug", otherwise
    * logs nothing.
    * @param name The name of the message.
    * @param msg The content of the message.
    */
    public static void debug(String name, String msg)
    {
        if (Log.getLogLevel() >= LOG_DEBUG) {
            Log._println("Debug", name, msg);
        }
    }
    
    // ----------------------------------------------------------------------------

    /**
    * If log level is equal to or above INFO level, logs the message with tag "Info", otherwise logs
    * nothing.
    * @param name The name of the message.
    * @param msg The content of the message.
    */
    public static void info(String name, String msg)
    {
        if (Log.getLogLevel() >= LOG_INFO) {
            Log._println("Info", name, msg);
        }
    }
    
    // ----------------------------------------------------------------------------

    /**
    * If log level is equal to or above WARN level, logs the message with tag "Warn", otherwise logs
    * nothing.
    * @param name The name of the message.
    * @param msg The content of the message.
    */
    public static void warn(String name, String msg)
    {
        if (Log.getLogLevel() >= LOG_WARN) {
            Log._println("Warn", name, msg);
        }
    }
    
    // ----------------------------------------------------------------------------

    /**
    * If log level is equal to or above ERROR level, logs the message with tag "Error", otherwise
    * logs nothing.
    * @param name The name of the message.
    * @param msg The content of the message.
    */
    public static void error(String name, String msg)
    {
        if (Log.getLogLevel() >= LOG_ERROR) {
            Log._println("ERROR", name, msg);
        }
    }
    
    // ----------------------------------------------------------------------------

    /**
    * If log level is equal to or above ERROR level, logs the message appended with the content of
    * the excetpion with tag "Error", otherwise logs nothing.
    * @param name The name of the message.
    * @param msg The content of the message.
    * @param t The exception to be logged.
    */
    public static void error(String name, String msg, Throwable t)
    {
        if (Log.getLogLevel() >= LOG_ERROR) {
            StringBuffer sb = new StringBuffer();
            sb.append(msg);
            while (t != null) {
                sb.append(" ==> ");
                sb.append(t.toString());
                String errMsg = t.getMessage();
                if ((errMsg != null) && !errMsg.equals("")) {
                    sb.append("[").append(t.getMessage()).append("]");
                }
                //t = t.getCause();
                break; // continue;
            }
            Log.error(name, sb.toString());
            t.printStackTrace(); // logOutput);
        }
    }
    
    // ----------------------------------------------------------------------------

    private static LogMessageHandler logMsgHandler = null;
    
    /**
    * Sets the <tt>LogMessageHandler</tt>. This LogMessageHandler will performs the actual logging
    * behavior.
    * @param msgHandler The <tt>LogMessageHandler</tt>.
    */
    public static void setMessageHandler(LogMessageHandler msgHandler)
    {
        logMsgHandler = msgHandler;
    }
    
    /**
    * Logs the message using the bounded <tt>LogMessageHandler</tt>.
    * @param ndx Index of the message. Usually a sequence number.
    * @param msg The message to be logged.
    */
    public static void setMessage(int ndx, String msg)
    {
        if (logMsgHandler != null) {
            logMsgHandler.setMessage(ndx, msg);
        }
    }
    
    /**
    * Performs the actual logging operation for <tt>Log</tt>.
    */
    public interface LogMessageHandler
    {
        /**
        * Performs the actual logging operation for <tt>Log</tt>.
        * @param ndx Index of the message.
        * @param msg Content of the message.
        */
        public void setMessage(int ndx, String msg);
    }

}
