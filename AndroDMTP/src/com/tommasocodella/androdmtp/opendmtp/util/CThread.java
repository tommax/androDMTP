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
//  Thread management.
// ----------------------------------------------------------------------------
// Change History:
//  2006/03/26  Martin D. Flynn
//      Initial release
//  2006/11/03  Robert Puckett
//      Include JavaDocs
// ----------------------------------------------------------------------------
package com.tommasocodella.androdmtp.opendmtp.util;

import java.util.Enumeration;
import java.util.Hashtable;

/**
* Provides methods for creating, tracking, and managing threads.
*/
public class CThread
{

    // ------------------------------------------------------------------------

    private static final String LOG_NAME  = "THREAD";

    // ------------------------------------------------------------------------

    private static  Hashtable   threadMap = new Hashtable();

    /**
    * Starts all threads.
    */
    public static void startThreads()
    {
        for (Enumeration i = threadMap.keys(); i.hasMoreElements();) {
            String name = (String)i.nextElement();
            CThread thread = (CThread)threadMap.get(name);
            thread.startThread(true);
        }
    }

    /**
    * Stops all threads.
    */
    public static void stopThreads()
    {
        
        /* stop all threads */
        Log.info(LOG_NAME, "Messaging stop ...");
        for (Enumeration i = threadMap.keys(); i.hasMoreElements();) {
            String name = (String)i.nextElement();
            CThread thread = (CThread)threadMap.get(name);
            thread.stopThread(0L); // do not wait for thread to stop
        }
        
        /* join */
        Log.debug(LOG_NAME, "Waiting for all-stop ...");
        for (Enumeration i = threadMap.keys(); i.hasMoreElements();) {
            String name = (String)i.nextElement();
            CThread thread = (CThread)threadMap.get(name);
            if (thread.hasStarted()) {
                Log.debug(LOG_NAME, "Waiting for stop: " + name);
                if (thread.stopThread(5000L)) {
                    Log.debug(LOG_NAME, "Has stopped: " + name);
                } else {
                    Log.error(LOG_NAME, "Did not stop: " + name);
                }
            }
        }
        
    }

    // ------------------------------------------------------------------------

    private String      name = null;

    private Object      threadLock = new Object();
    private Thread      thread = null;
    private Runnable    runnable = null;

    private boolean     threadStarted = false;
    private boolean     threadStopped = false;

    private boolean     shouldStop = false;

    /**
    * Creates a CThread instance with the passed parameters.
    * @param name Name of the thread.
    * @param runnable Runnable variable needed for threads.
    */
    public CThread(String name, Runnable runnable)
    {
        super();
        try {
            this.name = (name != null)? name : ("Thread_" + threadMap.size());
            this.runnable = (runnable != null)? runnable : ((this instanceof Runnable)? (Runnable)this : null);
            this.thread = null;
            this.threadStarted = false;
            this.threadStopped = false;
            this.shouldStop = false;
            threadMap.put(this.name, this); 
        } catch (Throwable t) {
            Log.error(LOG_NAME, "Init error", t);
        }
    }
    
    /**
    * Obtains the name for the Cthread.
    * @return Name corresponding to this Cthread.
    */
    public String getName()
    {
        return this.name;
    }
    
    // ------------------------------------------------------------------------

    /**
    * Extends the thread class to provide child threads.
    */
    private class ChildThread
        extends Thread
    {
        /**
        * Creates an instance of ChildThread.
        */
        public ChildThread() {
            super(new Runnable() {
                public void run() {
                    CThread.this.thread_run();
                }
            });
        }
        /**
        * Contains a boolean flag indicating whether or not the child thread should stop.
        * @return True if the thread should stop, false otherwise.
        */
        public boolean shouldStop() {
            return CThread.this.shouldStop();
        }
    }

    /**
    * Obtains the thread in scope and may creates a child thread to the thread in scope depending on
    * the boolean parameter.
    * @param create Boolean flag signifies whether a child thread should be created when the parent
    *        thread is null. If true, then a child thread will be created when the parent thread is
    *        null.
    * @return The thread in the CThread being referenced, or a child thread thereof.
    */
    public Thread getThread(boolean create)
    {
        Thread t = null;
        synchronized (this.threadLock) {
            if ((this.thread == null) && create) {
                this.thread = new ChildThread();
            }
            t = this.thread;
        }
        return t;
    }
    
    /**
    * Starts, or resets and starts, the thread.
    * @param reset Boolean flag indicates if the thread should be reset.
    * @return True if the thread is started, false otherwise.
    */
    public boolean startThread(boolean reset)
    {
        
        /* already running? */
        if (this.isAlive()) {
            Log.error(LOG_NAME, "Already running: " + this.getName());
            return false;
        } else 
        if (this.threadStarted && !reset) {
            Log.error(LOG_NAME, "Previously started: " + this.getName());
            return false;
        }
        
        /* thread listener */
        Runnable r = this.getRunnable();
        if (r instanceof ThreadListener) {
            ((ThreadListener)r).threadWillStart();
        }

        /* reset/start */
        try {
            this.threadStarted = true;
            this.shouldStop    = false;
            this.threadStopped = false;
            Thread t = this.getThread(true);
            t.start();
            return true;
        } catch (Throwable t) {
            Log.error(LOG_NAME, "Unable to start: " + this.getName(), t);
            return false;
        }
        
    }

    /**
    * Checks to see if the thread has started.
    * @return True if the thread has started, false otherwise.
    */
    public boolean hasStarted()
    {
        return this.threadStarted;
    }

    /**
    * Checks to see if the thread is alive.
    * @return True if the thread is alive, false otherwise.
    */
    public boolean isAlive()
    {
        Thread t = this.getThread(false);
        return ((t != null) && t.isAlive());
    }

    /**
    * Stops the thread.
    * @param joinTimeoutMS An amount of time in ms representing when the thread should stop.
    * @return True if the thread is stopped, false otherwise.
    */
    public boolean stopThread(long joinTimeoutMS)
    {
        this.shouldStop = true;
        Thread t = this.getThread(false);
        if (t != null) {
            Runnable r = this.getRunnable();
            if (r instanceof ThreadListener) {
                ((ThreadListener)r).threadWillStop();
            }
            t.interrupt();
            while (t.isAlive() && (joinTimeoutMS > 0L)) {
                //t.join(); // "<Thread>.join(<timeout>)" is not available
                t.interrupt();
                try { Thread.sleep(400L); } catch (Throwable th) {}
                joinTimeoutMS -= 400L;
            }
            return !t.isAlive();
        }
        return true; // thread is not alive
    }

    // ------------------------------------------------------------------------
    
    /**
    * Checks if the thread should stop.
    * @return True if the Cthread should stop, false otherwise.
    */
    public boolean shouldStop()
    {
        return this.shouldStop;
    }

    /**
    * Checks if the child thread should stop.
    * @return True if the current child thread should stop, false otherwise or if referenced thread
    *         is not a ChildThread.
    */
    public static boolean threadShouldStop()
    {
        Thread t = Thread.currentThread();
        if (t instanceof ChildThread) {
            return ((ChildThread)t).shouldStop();
        } else {
            Log.error(LOG_NAME, "Created without using 'CThread'!");
            return false;
        }
    }

    // ------------------------------------------------------------------------

    /**
    * Interrupts the thread.
    */
    public void interrupt()
    {
        Thread t = this.getThread(false);
        if (t != null) {
            t.interrupt();
        }
    }
    
    // ------------------------------------------------------------------------

    /**
    * Sets the runnable variable for this CThread.
    * @param runnable A runnable variable.
    */
    public void setRunnable(Runnable runnable)
    {
        this.runnable = runnable;
    }

    /**
    * Obtains the runnable variable for this CThread.
    * @return The runnable variable for this CThread.
    */
    public Runnable getRunnable()
    {
        return this.runnable;
    }
    
    /**
    * Runs the thread.
    */
    private void thread_run() 
    {
        this.threadStarted = true;
        Log.debug(LOG_NAME, "Started: " + this.getName());
        try {
            Runnable r = this.getRunnable();
            if (r != null) {
                r.run();
            } else {
                Log.error(LOG_NAME, "Runnable is null!");
            }
        } catch (SecurityException se) {
            Log.error(LOG_NAME, "Access denied: " + this.getName(), se);
        } catch (Throwable th) {
            if (th instanceof InterruptedException) {
                Log.warn(LOG_NAME, "Interrupted: " + this.getName());
            } else {
                Log.error(LOG_NAME, "'run' error: " + this.getName(), th);
                th.printStackTrace();
            }
        }
        synchronized (this.threadLock) {
            this.threadStopped = true;
            this.thread = null;
        }
        Log.debug(LOG_NAME, "Stopped: " + this.getName());
    }
     
    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
    * Provides the interface for ThreadListener to allow initialization and finalization routines for threads.
    */
    public interface ThreadListener
    {
        /**
        * Provides a hook for initialization when the thread starts.
        */
        public void threadWillStart();
        /**
        * Provides a hook for initialization when the thread stops.
        */
        public void threadWillStop();
    }
    
}
