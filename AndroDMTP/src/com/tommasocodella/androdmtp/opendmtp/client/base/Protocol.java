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
//  This class handles the DMTP protocol communication between the server and
//  the client.
// ----------------------------------------------------------------------------
// Change History:
//  2006/03/26  Martin D. Flynn
//     -Initial release
//  2006/04/02  Martin D. Flynn
//     -Clear 'sendIdentification' after sending UniqueID
//     -Changed '_hasMoreDataToSend()' to check for 'unsent' event packets.
//  2006/11/03  Elayne Man
//     -Include JavaDocs
//  2007/??/??  Martin D. Flynn
//     -Added method "getTotalEventsPending()".
// ----------------------------------------------------------------------------
package com.tommasocodella.androdmtp.opendmtp.client.base;

import com.tommasocodella.androdmtp.opendmtp.client.custom.Constants;
import com.tommasocodella.androdmtp.opendmtp.client.gps.GPSUtils;
import com.tommasocodella.androdmtp.opendmtp.codes.ClientErrors;
import com.tommasocodella.androdmtp.opendmtp.codes.Encoding;
import com.tommasocodella.androdmtp.opendmtp.codes.ServerErrors;
import com.tommasocodella.androdmtp.opendmtp.util.CThread;
import com.tommasocodella.androdmtp.opendmtp.util.DateTime;
import com.tommasocodella.androdmtp.opendmtp.util.FletcherChecksum;
import com.tommasocodella.androdmtp.opendmtp.util.Log;
import com.tommasocodella.androdmtp.opendmtp.util.Payload;
import com.tommasocodella.androdmtp.opendmtp.util.TimeoutException;

/**
* DMTP Protocol handler
*/
public class Protocol
    implements Runnable, ClientErrors
{

    // ------------------------------------------------------------------------

    private static final String LOG_NAME                = "PROTO";

    // ------------------------------------------------------------------------
    
    public  static final int TRANSPORT_NONE             = 0;
    public  static final int TRANSPORT_SIMPLEX          = 1;
    public  static final int TRANSPORT_DUPLEX           = 2;
    
    private static final int SEND_ID_NONE               = 0; // identification not necessary, or already sent
    private static final int SEND_ID_UNIQUE             = 1; // sending Unique ID (if available)
    private static final int SEND_ID_ACCOUNT            = 2; // send Account/Device ID

    private static final int MAX_SEVERE_ERRORS          = 10;
    private static final int EXCESSIVE_SEVERE_ERRORS    = 15;
    private static final int MAX_DUPLEX_EVENTS          = 128;
    private static final int MAX_SIMPLEX_EVENTS         = 8;
    private static final int GPS_EVENT_INTERVAL         = 10;

    // ------------------------------------------------------------------------

    /**
    * Singleton instance of the Protocol handler
    */
    public static Protocol   DMTP_Protocol = null;
    
    /**
    * Creates an instance of a Protocol given a transport.
    * @param transport The transport value
    * @return The protocol
    */
    public static Protocol createInstance(Transport transport)
    {
        // To be called only once, during initialization
        DMTP_Protocol = new Protocol(transport);
        return DMTP_Protocol;
    }

    /**
    * Returns the DMTP protocol singleton instance.
    * @return The protocol
    */
    public static Protocol getInstance()
    {
        return DMTP_Protocol;
    }

    // ------------------------------------------------------------------------
    
    private FletcherChecksum fletcher           = null;
    
    private PacketQueue  eventQueue             = null;
    private long         totalEventsSent        = 0L;
    
    private PacketQueue  pendingQueue           = null;
    private PacketQueue  volatileQueue          = null;

    private CThread      protocolThread         = null;
    private Object       transportLock          = new Object();
    private int          currentTransport       = TRANSPORT_NONE;
    
    private Transport    transport              = null;
    private long         lastSimplexErrorTimer  = 0L;
    private long         lastDuplexErrorTimer   = 0L;

    private int          severeErrorCount       = 0;
    private int          totalSevereErrorCount  = 0;
    private int          checkSumErrorCount     = 0;
    private int          invalidAcctErrorCount  = 0;
    
    private boolean      speakFreely            = false;
    private boolean      relinquishSpeakFreely  = false;
    private boolean      speakBrief             = false;

    private int          sendIdentification     = SEND_ID_UNIQUE;
    private long         totalReadBytes         = 0L;
    private long         totalWriteBytes        = 0L;
    private long         sessionReadBytes       = 0L;
    private long         sessionWriteBytes      = 0L;
    
    private int          sessionFirstEncoding   = Encoding.ENCODING_BINARY;
    private int          sessionEncoding        = Encoding.ENCODING_BINARY;
    private boolean      sessionEncodingChanged = false;

    /**
    * Constructor for Protocol.
    * @param xport The transport value
    */
    @SuppressWarnings("static-access")
	private Protocol(Transport xport)
    {
        try {
            this.fletcher = new FletcherChecksum();
            this.eventQueue = new PacketQueue();
            this.pendingQueue = new PacketQueue();
            this.volatileQueue = new PacketQueue();
            this.transport = xport; 
            this.protocolThread = new CThread("Protocol", this);
            //this.protocolThread.startThreads();
        } catch (Throwable t) {
        	
        	t.printStackTrace();
            Log.error(LOG_NAME, "Init error", t);
        }
    }
        
    // ------------------------------------------------------------------------

    /**
    * Notifies the transport thread that data is ready
    */
    public void transport()
    {
    	
        synchronized (this.transportLock) {
            if (!this.protocolThread.isAlive()) {
                Log.setMessage(1, "No Valid Connection");
            } else
            if (this.currentTransport == TRANSPORT_NONE) {
                int xportType = this._getTransportType();
                Log.info(LOG_NAME, "xporttype: " + xportType);
                if (xportType != TRANSPORT_NONE) {
                    this.currentTransport = xportType;
                    this.transportLock.notify();
                }
            }
        }
    }

    // ------------------------------------------------------------------------

    /**
    * Returns the event queue.
    * @return The event PacketQueue
    */
    public PacketQueue getEventQueue()
    {
        return this.eventQueue;
    }

    /** 
    * Returns the number of Events waiting to be sent
    * @return The number of events to be sent
    */
    public long getTotalEventsPending()
    {
        return this.getEventQueue().getQueueSize();
    }

    /** 
    * Returns the total number of Events successfully sent
    * @return The number of events sent
    */
    public long getTotalEventsSent()
    {
        return this.totalEventsSent;
    }

    /**
    * Returns the pending queue.
    * @return The pending PacketQueue
    */
    public PacketQueue getPendingQueue()
    {
        return this.pendingQueue;
    }

    /**
    * Returns the volatile queue.
    * @return The volatile PacketQueue
    */
    public PacketQueue getVolatileQueue()
    {
        return this.volatileQueue;
    }

    // ------------------------------------------------------------------------
    
    /**
    * Adds a packet to the queue. By default packets are created with 'Packet.PRIORITY_NORMAL', and will be
    * placed into the volatile queue which is reset at the start of each session. Thus, the volatile
    * queue should only be used within a server connected session. If a packet is important enough to
    * be retained until it is transmitted to the server, then its priority should be set to
    * 'Packet.PRIORITY_HIGH'. It will then be added to the pending queue and will be retained until
    * it is successfully transmitted to the server. This is also true for important packets that are
    * queued while NOT within a server connected session.
    * @param pkt the incoming packet
    */
    public void queuePacket(Packet pkt)
    {
        // Notes:
        // - By default packets are created with 'Packet.PRIORITY_NORMAL', and will be placed
        // into the volatile queue which is reset at the start of each session.  Thus,
        // the volatile queue should only be used within a server connected session. 
        // - If a packet is important enough to be retained until it is transmitted to
        // the server, then its priority should be set to 'Packet.PRIORITY_HIGH'. It will then
        // be added to the pending queue and will be retained until it is successfully 
        // transmitted to the server.  This is also true for important packets that are
        // queued while NOT within a server connected session.
        if (pkt.getPriority() >= Packet.PRIORITY_HIGH) {
            // Place high priority packets in the pending queue
            // This queue persists across sessions
            this.getPendingQueue().addPacket(pkt);
        } else {
            // place normal/low priority packets in the volitile queue
            // This queue is clear before/after each session
            this.getVolatileQueue().addPacket(pkt);
        }
    }

    /**
    * Adds an error packet to the queue.
    * @param payload The error payload
    */
    public void queueError(Payload payload)
    {
        Packet pkt = Packet.createClientPacket(Packet.PKT_CLIENT_ERROR, payload);
        this.queuePacket(pkt);
    }

    // ------------------------------------------------------------------------

    /**
    * Checks to see if it is time to make a connection to the DMT service provider, and what type of
    * connection to make. The time of the last connection and the number of connections made in the
    * last hour are considered.
    * @return the transport type (TRANSPORT_NONE, TRANSPORT_SIMPLEX, TRANSPORT_DUPLEX)
    */
    private int _getTransportType()
    {
    
        /* first check absolute minimum delay between connections */
        if (!Accounting.absoluteDelayExpired()) {
            // absolute minimum delay interval has not expired
            //if (this.getEventQueue().getHighestPriority() != Packet.PRIORITY_NONE) {
            //    Log.info(LOG_NAME, "Abs min delay interval not expired");
            //}
        	Log.warn(LOG_NAME, "absoluteDelayExpired");
            return TRANSPORT_NONE;
        }
        
        /* check specific event priority */
        int xportType = TRANSPORT_NONE;
        int evPri = this.getEventQueue().getHighestPriority();
        //Log.info(LOG_NAME, "Highest event priority: " + evPri);
        switch (evPri) {
    
            // no events, time for 'checkup'?
            case Packet.PRIORITY_NONE: 
                if (!Accounting.isUnderTotalQuota()) {
                    // Over Total quota
                    //Log.debug(LOG_NAME, "Over Total quota");
                    xportType = TRANSPORT_NONE;
                } else
                if (!Accounting.maxIntervalExpired()) {
                    // MAX interval has not expired
                    //Log.debug(LOG_NAME, "MAX interval has not expired");
                    xportType = TRANSPORT_NONE;
                } else
                if (Accounting.isUnderDuplexQuota()) {
                    // Under Total/Duplex quota and MAX interval expired, time for Duplex checkup
                    //Log.info(LOG_NAME, "Duplex checkup");
                    xportType = TRANSPORT_DUPLEX;
                } else {
                    // Over Duplex quota
                    //Log.debug(LOG_NAME, "Over Duplex quota");
                    xportType = TRANSPORT_NONE;
                }
                break;
    
            // low priority events
            case Packet.PRIORITY_LOW: 
                if (!Accounting.isUnderTotalQuota()) {
                    // Over Total quota, no sending
                    //Log.debug(LOG_NAME, "Over total quota");
                    xportType = TRANSPORT_NONE;
                } else
                if (!Accounting.minIntervalExpired()) {
                    // Min interval has not expired, no sending
                    //Log.debug(LOG_NAME, "MIN interval has not expired");
                    xportType = TRANSPORT_NONE;
                } else
                if (Accounting.supportsSimplex()) {
                    // Under Total quota, min interval expired, send Simplex
                    //Log.info(LOG_NAME, "Send Simplex");
                    xportType = TRANSPORT_SIMPLEX;
                } else
                if (Accounting.isUnderDuplexQuota()) {
                    // under Total/Duplex quota and min interval expired, Simplex not supported, send Duplex
                    //Log.info(LOG_NAME, "Send Duplex");
                    xportType = TRANSPORT_DUPLEX;
                } else {
                    if (!Accounting.supportsDuplex()) {
                        Log.error(LOG_NAME, "Transport does not support Simplex or Duplex!!!");
                    }
                    // Over Duplex quota (or Duplex not supported), no sending
                    //Log.debug(LOG_NAME, "Over Duplex quota");
                    xportType = TRANSPORT_NONE;
                }
                break;
    
            // normal priority events
            case Packet.PRIORITY_NORMAL:
                if (!Accounting.isUnderTotalQuota()) {
                    // Over Total quota, no sending
                    //Log.debug(LOG_NAME, "Over Total quota");
                    xportType = TRANSPORT_NONE;
                } else
                if (!Accounting.minIntervalExpired()) {
                    // Min interval has not expired, no sending
                    //Log.debug(LOG_NAME, "MIN interval has not expired");
                    xportType = TRANSPORT_NONE;
                } else
                if (Accounting.isUnderDuplexQuota()) {
                    // Under Total/Duplex quota and min interval expired, send Duplex
                    //Log.info(LOG_NAME, "Send Duplex");
                    xportType = TRANSPORT_DUPLEX;
                } else
                if (!Accounting.supportsDuplex()) {
                    // under Total quota, but the client doesn't support Duplex connections, send Simplex
                    //Log.info(LOG_NAME, "Send Simplex");
                    xportType = TRANSPORT_SIMPLEX;
                } else {
                    // Over Duplex quota, no sending
                    //Log.debug(LOG_NAME, "Over Duplex quota");
                    xportType = TRANSPORT_NONE;
                }
                break;
    
            // high priority events
            case Packet.PRIORITY_HIGH:
            default: // catch-all
                if (Accounting.isUnderDuplexQuota()) { // (disregard timer interval and total quota)
                    // Under Duplex quota and critical event, send Duplex
                    //Log.info(LOG_NAME, "Send Duplex");
                    xportType = TRANSPORT_DUPLEX;
                } else
                if (!Accounting.supportsDuplex()) {
                    // critical event, but the client does not support duplex connections, send Simplex
                    //Log.info(LOG_NAME, "Send Simplex");
                    xportType = TRANSPORT_SIMPLEX;
                } else {
                    // over Duplex quota, no sending
                    //Log.debug(LOG_NAME, "Over Duplex quota");
                    xportType = TRANSPORT_NONE;
                }
                break;
    
        }
        
        return xportType;
        
    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
    * Thread run
    */
    public void run()
        // throws SecurityException
    {
        //Log.debug(LOG_NAME, "Starting thread ...");
        while (!this.protocolThread.shouldStop()) {
            int xportType = TRANSPORT_NONE;

            /* wait for a transport request */
            synchronized (this.transportLock) {
                if (this.currentTransport == TRANSPORT_NONE) {
                    try { this.transportLock.wait(); } catch (InterruptedException ie) {}
                }
                xportType = this.currentTransport;
            }
            
            /* stop thread? */
            if (this.protocolThread.shouldStop()) {
                break;
            }

            /* continue now if it is still TRANSPORT_NONE */
            if (xportType == TRANSPORT_NONE) {
                continue;
            }

            /* establish connections */
            // (this section may 
            boolean xportOK = false;
            try {
                if (xportType == TRANSPORT_SIMPLEX) {
                    // establish Simplex communication here
                    xportOK = this.run_protocolSimplexTransport();
                } else 
                if (xportType == TRANSPORT_DUPLEX) {
                    // establish Duplex communication here
                    xportOK = this.run_protocolDuplexTransport();
                }
            } catch (SecurityException se) {
                // We could get a security exception if the user has denied us access
                // to the network.  In which case, there is nothing we can do but to
                // exit the thread.
                Log.error(LOG_NAME, "Access denied", se);
                break;
            }
            
            /* transport failed? */
            if (!xportOK) {
                // if transport fails, wait a few seconds before trying again
                try { Thread.sleep(15000L); } catch (Throwable t) {}
            }

            /* reset transport type */
            // even though the above may have failed, reset the transport type anyway.
            // it will be set again by the main thread.
            synchronized (this.transportLock) {
                this.currentTransport = TRANSPORT_NONE;
            }
            
        }
        //Log.debug(LOG_NAME, "Stopping thread ...");
    }
    
    /**
    * Simplex transport
    * @return true, if simplex transport was successful
    */
    private boolean run_protocolSimplexTransport()
    {
        //Log.debug(LOG_NAME, "run_protocolSimplexTransport ...");
    
        /* open transport */
        //Log.setMessage(1, "Openning UDP connection ...");
        if (!this._transportOpen(TRANSPORT_SIMPLEX)) {
            if (DateTime.isTimerExpired(this.lastSimplexErrorTimer,60L)) {
                this.lastSimplexErrorTimer = DateTime.getTimerSec();
                Log.warn(LOG_NAME, "Unable to open Simplex transport");
                Log.setMessage(1, "UDP failed");
            }
            return false;
        }
        Log.setMessage(1, "TX UDP ...");

        /* check for GPS Fix expiration ("stale") */
        if (GPSUtils.isGpsStale()) {
            // queue GPS error message
            long lastSampleTime = GPSUtils.getInstance().getLastSampleTime();
            if (DateTime.getCurrentTimeSec() > (lastSampleTime + GPS_EVENT_INTERVAL)) {
                // Likely serious GPS problem.
                // We haven't received ANYTHING from the GPS reciver in the last GPS_EVENT_INTERVAL seconds
                // The GPS receiver is no longer working!
                Payload p = new Payload();
                p.writeULong(ClientErrors.ERROR_GPS_FAILURE, 2);
                p.writeULong(lastSampleTime                , 4);
                this.queueError(p);
            } else {
                // The GPS receiver still appears to be working, the fix is just expired
                long lastValidTime = GPSUtils.getInstance().getLastValidTime();
                Payload p = new Payload();
                p.writeULong(ClientErrors.ERROR_GPS_EXPIRED, 2);
                p.writeULong(lastValidTime                 , 4);
                this.queueError(p);
            }
        }

        /* send queued packets/events */
        if (!this._sendAllPackets(TRANSPORT_SIMPLEX, false)) {
            this._transportClose(TRANSPORT_SIMPLEX, false);
            this.getEventQueue().resetSent(); // set all to 'unsent'
            Log.setMessage(1, "UDP TX Err");
            return false;
        }
    
        /* acknowledge sent events */
        Log.setMessage(1, "");
        if (this._transportClose(TRANSPORT_SIMPLEX, true)) {
            // - Data doesn't get transmitted until the close for Simplex connections.
            // So events should not be auto-acknowledged until the close has occured
            // and didn't get any errors. (This still doesn't guarantee that the server
            // received the data).
            // - Since many/most wireless data services will be placing the device behind
            // a NAT'ed router, there is no way for the server to send back a UDP
            // acknowledgement to the device.  As such, no attempt is made to read an
            // acknowledgement from the server.
            this.getPendingQueue().emptyQueue(); // remove all pending messages
            int delCnt = this.getEventQueue().deleteToSequence(Packet.SEQUENCE_ALL); // del all 'sent'
            if (delCnt > 0) {
                // add to total events sent/ack'ed
                this.totalEventsSent += delCnt;
            }
            Accounting.markSimplexConnection();
            return true;
        } else {
            return false;
        }

    }
    
    /**
    * Duplex transport.
    * @return true, if duplex transport was successful
    */
    private boolean run_protocolDuplexTransport()
    {
        //Log.debug(LOG_NAME, "run_protocolDuplexTransport ...");
    
        /* open transport */
        //Log.setMessage(1, "Openning TCP connection ...");
        if (!this._transportOpen(TRANSPORT_DUPLEX)) {
            if (DateTime.isTimerExpired(this.lastDuplexErrorTimer,60L)) {
                this.lastDuplexErrorTimer = DateTime.getTimerSec();
                Log.warn(LOG_NAME, "Unable to open Duplex transport");
                Log.setMessage(1, "TCP failed");
            } else {
                //Log.setMessage(1, "");
            }
            return false;
        }
        Log.setMessage(1, "TX TCP ...");

        /* check for GPS Fix expiration ("stale") */
        if (GPSUtils.isGpsStale()) {
            // queue GPS error message
            long lastSampleTime = GPSUtils.getInstance().getLastSampleTime();
            if (DateTime.getCurrentTimeSec() > (lastSampleTime + GPS_EVENT_INTERVAL)) {
                // Likely serious GPS problem.
                // We haven't received ANYTHING from the GPS reciver in the last GPS_EVENT_INTERVAL seconds
                // The GPS receiver is no longer working!
                Payload p = new Payload();
                p.writeULong(ClientErrors.ERROR_GPS_FAILURE, 2);
                p.writeULong(lastSampleTime                , 4);
                this.queueError(p);
            } else {
                // The GPS receiver still appears to be working, the fix is just expired
                long lastValidTime = GPSUtils.getInstance().getLastValidTime();
                Payload p = new Payload();
                p.writeULong(ClientErrors.ERROR_GPS_EXPIRED, 2);
                p.writeULong(lastValidTime                 , 4);
                this.queueError(p);
            }
        }

        /* default speak freely permission on new connections */
        this.speakFreely = false;
        this.relinquishSpeakFreely = false;
    
        /* default speak-brief on new connection */
        this.speakBrief = Props.getBoolean(Props.PROP_COMM_FIRST_BRIEF, 0, false);

        /* packet handling loop */
        boolean rtnOK       = true;
        boolean keepLooping = true;
        boolean speakFirst  = Props.getBoolean(Props.PROP_COMM_SPEAK_FIRST, 0, true);
        boolean firstPass   = true;
        for (;keepLooping;) {
            
            /* send queued packets */
            if (firstPass) {
                firstPass = false;
                if (speakFirst) {
                    // client initiates conversation
                    // send identification and first block of events
                    // 'speakFreely' is always false here
                    if (!this._sendAllPackets(TRANSPORT_DUPLEX, this.speakBrief)) {
                        rtnOK = false; // write error
                        break;
                    }
                    this.speakBrief = false;
                }
            } else
            if (this.speakFreely) {
                // send any pending packets
                // During 'speak-freely' wait until we have something to send.
                if (this._hasMoreDataToSend()) {
                    // The thread may decide whether, or not, to relinquish 'speakFreely' permission
                    if (this.relinquishSpeakFreely) {
                        this.speakFreely = false; // relinquish speak-freely permission
                    }
                    if (!this._sendAllPackets(TRANSPORT_DUPLEX, false)) {
                        rtnOK = false; // write error
                        break;
                    }
                }
            }
            
            /* read packet */
            Packet pkt = null;
            try {
                pkt = this._readServerPacket(); // <-- timeout is specified by transport
                if (pkt == null) {
                    // read/parse error
                    rtnOK = false;
                    break;
                }
            } catch (TimeoutException toe) {
                // read timeout
                if (this.speakFreely) {
                    // read timeouts are allowed in 'speak-freely' mode
                    continue;
                } else {
                    Log.info(LOG_NAME, "Duplex server read timeout");
                    // this is an error when not in 'speak-freely' mode, or not in a thread
                    // otherwise we'll be blocking the mainloop for too long.
                    rtnOK = false;
                    break;
                }
            }
            
            /* handle received packet */
            keepLooping = this._handleServerPacket(pkt);
    
        }
        
        /* close transport */
        if (rtnOK) {
            Log.setMessage(1, "");
        } else {
            Log.setMessage(1, "TCP TX Err");
        }
        this._transportClose(TRANSPORT_DUPLEX, false);
        this.getEventQueue().resetSent(); // set any remaining 'sent' events to 'unsent'
        Accounting.markDuplexConnection();
        return rtnOK;

    }

    /**
    * Determines if there is more data to send.
    * @return True if there is more data to send, false otherwise
    */
    private boolean _hasMoreDataToSend()
    {
        if (this.sendIdentification != SEND_ID_NONE) {
            // identification has been requested
            return true;
        } else
        if (!this.getPendingQueue().isEmpty()) {
            // has pending (unsent) important packets
            return true;
        } else
        if (!this.getVolatileQueue().isEmpty()) {
            // has miscellaneous (unsent) volatile packets
            return true;
        } else
        if (this.getEventQueue().hasUnsentPackets()) {
            // has unsent event packets
            return true;
        }
        return false;
    }

    /**
    * Sends all packets.
    * @param xportType The transport type
    * @param brief true to send only ID packets, false to send all packets
    * @return true, if successful, false otherwise
    */
    private boolean _sendAllPackets(int xportType, boolean brief)
    {
    
        /* reset checksum before we start transmitting */
        this.fletcher.reset();
    
        /* transmit identification packets */
        if (!this._sendIdentification()) {
            return false; // write error
        }
        
        /* 'brief' means send only the identification and EOB packets */
        // If the ID packets aren't sent (don't need to be sent), and 'speakFreekly' is true,
        // then its possible that nothing will be sent.
        boolean hasMoreEvents = false;
        if (brief) {
            // on this pass we want to send just the identification packets (above)
            
            /* have events? */
            hasMoreEvents = this._hasMoreDataToSend();
            
        } else {
        
            /* transmit pending packets */
            if (!this._sendQueue(this.getPendingQueue(), Packet.PRIORITY_HIGH, -1)) {
                return false; // write error: close socket
            }
    
            /* transmit volatile packets */
            if (!this._sendQueue(this.getVolatileQueue(), Packet.PRIORITY_HIGH, -1)) {
                return false; // write error: close socket
            }
            
            /* clear pending queue */
            this.getVolatileQueue().emptyQueue(); // reset volatile queue
            
            /* clear pending queue */
            // at this point the assme pending messages have been received
            this.getPendingQueue().emptyQueue();

            /* do we have a sent, but unacknowledged, event in the queue? */
            // if so, relinquish 'speakFreely'
            if (this.speakFreely) {
                if (!this.getEventQueue().isEmpty()) {
                    // Optional implementation:
                    // If we have any events at all, relinquish speak-freely
                    // This will allow the server to acknowledge these events and let the client
                    // know that the server is listening.
                    this.speakFreely = false;
                }
                //if (pqueHasSentPacketWithSequence(evGetEventQueue(), Packet.SEQUENCE_ALL)) {
                //    // Relinquish speak-freely if we've already sent these events once and the
                //    // server hasn't yet acknowledged them.
                //    this.speakFreely = false;
                //}
            }

            /* max events to send during this block */
            int maxEvents = 8;
            switch (xportType) {
                case TRANSPORT_SIMPLEX:
                    maxEvents = (int)Props.getLong(Props.PROP_COMM_MAX_SIM_EVENTS, 0, 4L);
                    if (maxEvents > MAX_SIMPLEX_EVENTS) { maxEvents = MAX_SIMPLEX_EVENTS; }
                    break;
                case TRANSPORT_DUPLEX:
                    maxEvents = (int)Props.getLong(Props.PROP_COMM_MAX_DUP_EVENTS, 0, 8L);
                    if (maxEvents > MAX_DUPLEX_EVENTS) { maxEvents = MAX_DUPLEX_EVENTS; }
                    break;
            }
    
            /* max priority events to send */
            // - This function is getting called because "_getTransportType()" returned a transport
            // type based on what it found in the event queue.  If it chose a Simplex connection
            // based on 'Low' priority events found in the queue, then we should make sure that
            // only low priority events will get sent via Simplex.  This prevents Normal and High
            // priority events from getting sent that may have entered the queue while we are
            // still trying to set up the connection (which could take a while).
            // - If it is deisirable to go ahead and send all (including High priority) events found
            // in the queue, then this restriction should be relaxed, however it would then be 
            // necessary to insure that these event don't get purged from the queue until they were 
            // successfully later sent via Duplex.
            int maxPri = ((xportType == TRANSPORT_DUPLEX) || !Accounting.supportsDuplex())? 
                Packet.PRIORITY_HIGH :     // all priority events will be sent
                Packet.PRIORITY_LOW;       // only low priority events will be sent
    
            /* transmit unacknowledged event packets */
            if (!this._sendQueue(this.getEventQueue(), maxPri, maxEvents)) {
                return false; // write error: close socket
            }
            hasMoreEvents = this.getEventQueue().hasUnsentPackets(); // this._hasMoreDataToSend();

        }
    
        /* send end-of-block packet */
        if (xportType == TRANSPORT_DUPLEX) {
            // This also relinquishes any 'speakFreely' permission
            if (!_protocolSendEOB(hasMoreEvents)) {
                return false;
            }
        }

        return true;
    }

    // ----------------------------------------------------------------------------

    /**
    * Send identification packets
    * @return true, if successful
    */
    private boolean _sendIdentification()
    {
        if (this.sendIdentification != SEND_ID_NONE) {
    
            /* first try our UniqueID */
            boolean okUniqueId = (this.sendIdentification == SEND_ID_UNIQUE)? true : false;
            if (okUniqueId) {
                byte id[] = Props.getByteArray(Props.PROP_STATE_UNIQUE_ID, null);
                if ((id != null) && (id.length == 6)) { // length must be '6'
                    int b;
                    for (b = 0; (b < id.length) && (id[b] == (byte)0); b++);
                    if (b < 6) { // at least one field must be non-zero
                        Log.debug(LOG_NAME, "_sendIdentification: UniqueID ...");
                        Packet idPkt = Packet.createClientPacket(Packet.PKT_CLIENT_UNIQUE_ID, id);
                        if (this._transportWritePacket(idPkt) < 0) {
                            Log.error(LOG_NAME, "Error writing UniqueID packet");
                            return false; // write error
                        }
                        this.sendIdentification = SEND_ID_NONE;
                        return true;
                    }
                }
            }
        
            // AccountID
            String acctId = Props.getString(Props.PROP_STATE_ACCOUNT_ID, "");
            if ((acctId != null) && !acctId.equals("")) {
                Log.debug(LOG_NAME, "_sendIdentification: AccountID ...");
                if (acctId.length() > Props.MAX_ID_SIZE) { acctId = acctId.substring(0, Props.MAX_ID_SIZE); }
                Packet idPkt = Packet.createClientPacket(Packet.PKT_CLIENT_ACCOUNT_ID, acctId);
                if (this._transportWritePacket(idPkt) < 0) {
                    Log.error(LOG_NAME, "Error writing AccountID packet");
                    return false; // write error
                }
            }
        
            // DeviceID
            String devId = Props.getString(Props.PROP_STATE_DEVICE_ID, "");
            if ((devId != null) && !devId.equals("")) {
                Log.debug(LOG_NAME, "_sendIdentification: DeviceID ...");
                if (devId.length() > Props.MAX_ID_SIZE) { devId = devId.substring(0, Props.MAX_ID_SIZE); }
                Packet idPkt = Packet.createClientPacket(Packet.PKT_CLIENT_DEVICE_ID, devId);
                if (this._transportWritePacket(idPkt) < 0) {
                    Log.error(LOG_NAME, "Error writing DeviceID packet");
                    return false; // write error
                }
            }
        
            /* ID successfully sent */
            this.sendIdentification = SEND_ID_NONE;
            
        }
        
        return true;
    
    }

    // ----------------------------------------------------------------------------

    /**
    * Read a server packet
    * @return The parsed packet
    * @throws TimeoutException If a timeout occurs
    */
    private Packet _readServerPacket()
        throws TimeoutException
    {
        byte b[] = this.transport.readPacket();
        if (b != null) {
            this.sessionReadBytes += b.length;
            this.totalReadBytes   += b.length;
            try {
                Packet p = new Packet(false, b);
                return p;
            } catch (PacketParseException ppe) {
                return null;
            }
        } else {
            return null;
        }
    }

    // ----------------------------------------------------------------------------

    /**
    * Handle a server packet
    * @param srvPkt Incoming server packet
    * @return true, if successful
    */
    private boolean _handleServerPacket(Packet srvPkt)
    {
    
        /* check header */
        if (srvPkt.getPacketHeader() != Packet.HEADER_BASIC) {
            // unsupported header
            Payload p = new Payload();
            p.writeULong(ClientErrors.ERROR_PACKET_HEADER, 2);
            p.writeULong(srvPkt.getPacketType()          , 2);
            this.queueError(p);
            return true; // continue communication
        }
        
        /* handle packet */
        Payload payload = srvPkt.getPayload(true); // payload is data soure
        switch (srvPkt.getPacketType()) {
            case Packet.PKT_SERVER_EOB_DONE     : {    // End of transmission, query response
                // Arguments: none
                this.speakFreely = false; // relinquish speak-freely permission
                if (!this._sendAllPackets(TRANSPORT_DUPLEX, this.speakBrief)) {
                    return false; // write error
                }
                this.speakBrief = false;
                return true;
            }
            case Packet.PKT_SERVER_EOB_SPEAK_FREELY: { // End of transmission, speak freely
                // Arguments: none
                this.speakFreely = true; // 'speak-freely' permission granted
                // we will be sending data shortly (in the outer loop)
                return true;
            }
            case Packet.PKT_SERVER_ACK          : { // Acknowledge [optional sequence]
                // Arguments: sequence[optional]
                long sequence = payload.readULong(4, Packet.SEQUENCE_ALL);
                // remove sent/acknowledged events from queue up to specified sequence #
                int delCount = this.getEventQueue().deleteToSequence(sequence);
                if (delCount <= 0) {
                    Payload p = new Payload();
                    p.writeULong(ClientErrors.ERROR_PACKET_ACK, 2);
                    p.writeULong(srvPkt.getPacketType()       , 2);
                    this.queueError(p);
                } else {
                    // add to total events sent/ack'ed
                    this.totalEventsSent += delCount;
                }
                this.getEventQueue().resetSent(); // set all events to 'unsent'
                return true;
            }
            case Packet.PKT_SERVER_GET_PROPERTY : { // Get property
                // Arguments: propertyKey
                while (payload.getAvail() >= 2) {
                    int propKey = (int)payload.readULong(2, 0L);
                    if (propKey > 0) {
                        // queue property value packet
                        try {
                            Payload p = Props.getPayload(propKey);
                            Packet propPkt = Packet.createClientPacket(Packet.PKT_CLIENT_PROPERTY_VALUE, p);
                            this.queuePacket(propPkt);
                        } catch (ClientErrorException ce) {
                            int clientError = ce.getClientError();
                            Payload p = new Payload();
                            p.writeULong(clientError, 2);
                            p.writeULong(propKey    , 2);
                            this.queueError(p);
                        }
                    } else {
                        // no property specified
                        Payload p = new Payload();
                        p.writeULong(ClientErrors.ERROR_PACKET_PAYLOAD, 2);
                        p.writeULong(srvPkt.getPacketType()           , 2);
                        this.queueError(p);
                    }
                }
                return true;
            }
            case Packet.PKT_SERVER_SET_PROPERTY : { // Set property
                // Arguments: propertyKey, propertyValue[optional]
                int propKey = (int)payload.readULong(2, 0L);
                if (propKey > 0) {
                    // set property value
                    try {
                        // this could be a command
                        Props.setPayload(propKey, payload);
                    } catch (ClientErrorException ce) {
                        int clientError  = ce.getClientError();
                        int commandError = ce.getCommandError();
                        Payload p = new Payload();
                        p.writeULong(clientError        , 2);
                        p.writeULong(propKey            , 2);
                        if (commandError > 0) {
                            p.writeULong(commandError   , 2);
                        }
                        this.queueError(p);
                    }
                } else {
                    // no property specified
                    Payload p = new Payload();
                    p.writeULong(ClientErrors.ERROR_PACKET_PAYLOAD, 2);
                    p.writeULong(srvPkt.getPacketType()           , 2);
                    this.queueError(p);
                }
                return true;
            }
            case Packet.PKT_SERVER_FILE_UPLOAD  : { // File upload
                //uploadProcessRecord(srvPkt->data, (int)srvPkt->dataLen);
                // this already sends error/ack packets
                return true;
            }
            case Packet.PKT_SERVER_ERROR        : { // NAK/Error codes
                // Arguments: errorCode, packetHeader, packetType, extraData
                int errCode    = (int)payload.readULong(2, 0L);
                int pktHdrType = (int)payload.readULong(2, 0L);
                // remaining data in payload is for error diagnostics
                if (errCode > 0) {
                    boolean ok = this._handleErrorCode(errCode, pktHdrType, payload);
                    if (!ok) {
                        return false; // critical error determined by error code
                    }
                } else {
                    // no error specified
                    Payload p = new Payload();
                    p.writeULong(ClientErrors.ERROR_PACKET_PAYLOAD, 2);
                    p.writeULong(srvPkt.getPacketType()           , 2);
                    this.queueError(p);
                }
                return true;
            }
            case Packet.PKT_SERVER_EOT          : { // End transmission (socket will be closed)
                // Arguments: none
                // return false to close communications
                return false; // server is closing the connection
            }
            default: {
                // unsupported type
                Payload p = new Payload();
                p.writeULong(ClientErrors.ERROR_PACKET_TYPE, 2);
                p.writeULong(srvPkt.getPacketType()        , 2);
                this.queueError(p);
                return true;
            }
        }
        
    }
    
    // ----------------------------------------------------------------------------

    /**
    * Handles error codes.
    * @param errCode The error code
    * @param hdrType The header type
    * @param payload The payload value
    * @return true, if successful
    */
    private boolean _handleErrorCode(int errCode, int hdrType, Payload payload)
    {
        
        switch (errCode) {
            
            case ServerErrors.NAK_ID_INVALID             : { // Invalid unique id
                // The DMT server doesn't recognize our unique-id. 
                // We should try to send our account and device id.
                this.sendIdentification = SEND_ID_ACCOUNT;
                return true;
            }
                
            case ServerErrors.NAK_ACCOUNT_INVALID        : // Invalid/missing account id
            case ServerErrors.NAK_DEVICE_INVALID         : { // Invalid/missing device id
                // The DMT server doesn't know who we are
                this.severeErrorCount++;
                if (++this.invalidAcctErrorCount >= 2) { // fail on 2nd error
                    return false;
                } else {
                    // try one more time (if the server will let us)
                    return true;
                }
            }
                
            case ServerErrors.NAK_ACCOUNT_INACTIVE       : // Account has expired, or has become inactive
            case ServerErrors.NAK_DEVICE_INACTIVE        : { // Device has expired, or has become inactive
                this.severeErrorCount++;
                return false;
            }
                
            case ServerErrors.NAK_EXCESSIVE_CONNECTIONS  : { // Excessive connections
                // The DMT server may mark (or has marked) us as an abuser.
                // No alternative, but to quit
                // Slow down minimum connection interval
                Props.addLong(Props.PROP_COMM_MIN_XMIT_RATE , 0, 300L);
                Props.addLong(Props.PROP_COMM_MIN_XMIT_DELAY, 0, 300L);
                return false;
            }
                
            case ServerErrors.NAK_PACKET_HEADER          :   // Invalid/Unsupported packet header
            case ServerErrors.NAK_PACKET_TYPE            : { // Invalid/Unsupported packet type
                // The DMT server does not support our custom extensions
                // Ignore the error and continue.
                return true;
            }
            
            case ServerErrors.NAK_PACKET_LENGTH          :   // Invalid packet length
            case ServerErrors.NAK_PACKET_PAYLOAD         : { // Invalid packet payload
                // This indicates a protocol compliance issue in the client
                this.severeErrorCount++;
                return false;
            }
            
            case ServerErrors.NAK_PACKET_ENCODING        : { // Encoding not supported
                // DMT servers are required to support Binary, ASCII-Base64, and ASCII-Hex encoding.
                // This should only occur if we are encoding packets in ASCII CSV format and the
                // server doesn't support this encoding.  We shoud try again with HEX or Base64
                // encoding for the remainder of the session.
                // We need to handle getting several of these errors during a transmission block.
                if (!this.sessionEncodingChanged) {
                    this.sessionEncodingChanged = true; // mark changed
                    if (Encoding.IsServerRequired(this.sessionEncoding))  {
                        // We're already encoding in a server supported encoding
                        // This is likely some protocol compliance issue with the client
                        // (of course it can't be the server! :-)
                        return false;
                    }
                    long encodingMask = Props.getLong(Props.PROP_COMM_ENCODINGS, 0, 0L) & Encoding.ENCODING_MASK;
                    encodingMask &= ~Encoding.SUPPORTED_ENCODING_CSV;
                    Props.setLong(Props.PROP_COMM_ENCODINGS, 0, encodingMask);
                    this.sessionEncoding &= ~Encoding.ENCODING_MASK; // save checksum
                    this.sessionEncoding |= Encoding.SUPPORTED_ENCODING_BASE64; // new encoding
                    if ((hdrType == Packet.PKT_CLIENT_UNIQUE_ID)  ||
                        (hdrType == Packet.PKT_CLIENT_ACCOUNT_ID) ||
                        (hdrType == Packet.PKT_CLIENT_DEVICE_ID)    ) {
                        // error occured on identification packets, resend id
                        this.sendIdentification = SEND_ID_UNIQUE;
                    }
                }
                return true;
            }
            
            case ServerErrors.NAK_PACKET_CHECKSUM        : // Invalid packet checksum (ASCII encoding only)
            case ServerErrors.NAK_BLOCK_CHECKSUM         : { // Invalid block checksum
                // increment checksum failure indicator, if it gets too large, quit
                if (++this.checkSumErrorCount >= 3) { // fail on 3rd error (per session)
                    this.severeErrorCount++;
                    return false;
                } else {
                    return true;
                }
            }
            
            case ServerErrors.NAK_PROTOCOL_ERROR         : { // Protocol error
                // This indicates a protocol compliance issue in the client
                this.severeErrorCount++;
                return false;
            }
                
            case ServerErrors.NAK_FORMAT_DEFINITION_INVALID: { // Custom format type is invalid
                // The custom type we've specified isn't within the supported custom format packet types
                // This indicates a protocol compliance issue in the client
                Props.setBoolean(Props.PROP_COMM_CUSTOM_FORMATS, 0, false);
                this.severeErrorCount++;
                return false;
            }
    
            case ServerErrors.NAK_FORMAT_NOT_SUPPORTED   : { // Custom formats not supported
                // The DMT server does not support custom formats (at least not for our
                // current level of service).  
                // We should acknowledge all sent events, and set a flag indicating that
                // we should not send custom formats to this server in the future.
                this.getEventQueue().deleteToSequence(Packet.SEQUENCE_ALL);
                // these records are not counted
                Props.setBoolean(Props.PROP_COMM_CUSTOM_FORMATS, 0, false);
                return true;
            }
                
            case ServerErrors.NAK_FORMAT_NOT_RECOGNIZED  : { // Custom format not recognized
                // The DMT does support custom formats, but it doesn't recognize the 
                // format we've used in an event packet.  We should send the custom
                // format template(s), then resend the events.
                PayloadTemplate template = Packet.GetClientPayloadTemplate(hdrType);
                if (template != null) {
                    Payload p = template.getPayload();
                    Packet custPkt = Packet.createClientPacket(Packet.PKT_CLIENT_FORMAT_DEF_24, p);
                    custPkt.setPriority(Packet.PRIORITY_HIGH);
                    this.queuePacket(custPkt);
                    return true;
                } else {
                    // One of the following has occured:
                    // - The server just told us it doesn't support a custom format that we didn't 
                    //   send it (unlikely).
                    // - An internal buffer overflow has ocurred.
                    // - We were unable to add the packet to the queue
                    this.severeErrorCount++;
                    return false;
                }
            }
    
            case ServerErrors.NAK_EXCESSIVE_EVENTS       : { // Excessive events
                // The DMT server may mark (or has marked) us as an abuser.
                // If present, the next (first) event will never be accepted, purge it from the queue.
                this.getEventQueue().deleteFirstSent(); // first "sent" event
                // Slow down periodic messages to prevent this from occurring in the future
                long inMotionInterval = Props.getLong(Props.PROP_MOTION_IN_MOTION, 0, 0L);
                if (inMotionInterval > 0L) {
                    Props.setLong(Props.PROP_MOTION_IN_MOTION, 0, (inMotionInterval + DateTime.MinuteSeconds(2)));
                }
                long dormantInterval = Props.getLong(Props.PROP_MOTION_DORMANT_INTRVL, 0, 0L);
                if (dormantInterval > 0L) {
                    Props.setLong(Props.PROP_MOTION_DORMANT_INTRVL, 0, (dormantInterval + DateTime.MinuteSeconds(10)));
                }
                // continue with session
                return true;
            }
    
            case ServerErrors.NAK_DUPLICATE_EVENT        : { // Duplicate event found
                // ignore error
                return true;
            }
    
            case ServerErrors.NAK_EVENT_ERROR            : { // Server had an error when processing this event
                // ignore error
                return true;
            }
    
        }
        
        /* unhandled error - ignore */
        return true;

    }

    // ----------------------------------------------------------------------------

    /**
    * Sends the packets in the queue to the server
    * @param pq The acket queue
    * @param maxPri The maximum priority packets
    * @param maxEvents The maximum events (if >0)
    * @return true, if successful.
    */
    private boolean _sendQueue(PacketQueue pq, int maxPri, int maxEvents)
    {
        int rtnWriteLen = 0; // rtnVal
    
        /* adjust arguments */
        if (maxPri < Packet.PRIORITY_LOW) { maxPri = Packet.PRIORITY_LOW; } // at least low priority packets
        if (maxEvents == 0) { maxEvents = 1; } // at least 1 packet
        // a 'maxEvent' < 0 means there is no maximum number of events to send

        /* iterate through queue */
        // This loop stops as soon as one of the following has occured:
        //  - We've sent the specified 'maxEvents'.
        //  - All events in the queue have been sent.
        //  - We've run into a packet that exceeds our maximum allowable priority.
        for (int pi = 0; maxEvents != 0; pi++) {

            /* get next packet */
            Packet quePkt = pq.getPackatAt(pi);
            if (quePkt == null) {
                break;
            }

            /* priority */
            if (quePkt.getPriority() > maxPri) {
                break;
            }

            /* write packet */
            rtnWriteLen = this._transportWritePacket(quePkt);
            if (rtnWriteLen < 0) {
                // error
                break;
            }
            
            /* mark this packet as sent */
            quePkt.setSent(true); // mark it as sent
            
            /* decrement counter */
            if (maxEvents > 0) { maxEvents--; }
            
        }
    
        /* check for errors */
        if (rtnWriteLen < 0) {
            return false; // write error: close socket
        } else {
            return true;
        }
    
    }

    /**
    * Sends the End-Of-Block packet. Do not use for simplex transport.
    * @param hasMoreEvents True if there are more events.
    * @return true, if successful.
    */
    private boolean _protocolSendEOB(boolean hasMoreEvents)
    {
        // Don't call this for Simplex Transport!!!!
        if (!this.speakFreely) {
            int eobType = hasMoreEvents? Packet.PKT_CLIENT_EOB_MORE : Packet.PKT_CLIENT_EOB_DONE;
            Packet eob = Packet.createClientPacket(eobType);
            byte eobEnc[] = null;

            /* Add Fletcher checksum if encoding is binary */
            if (Encoding.IsEncodingBinary(this.sessionFirstEncoding)) {
                
                /* encode packet with a placeholder for the checksum */
                eob.getPayload().writeBytes(new byte[] { (byte)0, (byte)0 }, 2);
                eobEnc = eob.encode(Encoding.ENCODING_BINARY); // length should be 5
                this.fletcher.runningChecksum(eobEnc); 
                
                /* calculate the checksum and insert it into the packet */
                this.fletcher.getChecksum(eobEnc, 3);
            
            } else {
                
                /* encode packet without checksum */
                eobEnc = eob.encode(this.sessionFirstEncoding);
                
            }
    
            /* write EOB packet */
            int rtnWriteLen = this._transportWrite(eobEnc, false);
            if (rtnWriteLen < 0) {
                return false; // write error: close socket
            }
            this.speakFreely = false; // relinquish any granted "speak freely" permission on EOB
            this.sessionFirstEncoding = this.sessionEncoding;
            
        }
        
        return true;
    }

    // ----------------------------------------------------------------------------
    // The following are wrappers to the 'transport.c' module function calls
 
    /**
    * Opens the transport communication.
    * @param type The type of transport (TRANSPORT_SIMPLEX, TRANSPORT_DUPLEX)
    * @return true, if successful
    */
    private boolean _transportOpen(int type)
    {
        boolean didOpen = this.transport.open(type);
        if (didOpen) {
            /* openned, reset session */
            this.getVolatileQueue().emptyQueue();
            //evEnableOverwrite(false); // disable overwrites while connected
            this.severeErrorCount        = 0;
            this.checkSumErrorCount      = 0;
            this.invalidAcctErrorCount   = 0;
            this.sendIdentification      = SEND_ID_UNIQUE;
            this.totalReadBytes          = Props.getLong(Props.PROP_COMM_BYTES_READ   , 0, 0L);
            this.totalWriteBytes         = Props.getLong(Props.PROP_COMM_BYTES_WRITTEN, 0, 0L);
            this.sessionReadBytes        = 0L;
            this.sessionWriteBytes       = 0L;
        }
        return didOpen;
    }
    
    /**
    * Closes the transpot communication.
    * @param xportType The type of transport (TRANSPORT_SIMPLEX, TRANSPORT_DUPLEX)
    * @param sendUDP True if sending UDP packets
    * @return true, if successful.
    */
    private boolean _transportClose(int xportType, boolean sendUDP)
    {
        
        /* close transport */
        // If the connection is via Simplex, the data will be sent now.
        boolean didClose = this.transport.close((xportType == TRANSPORT_SIMPLEX)? sendUDP : false);
        if (didClose) {
            // save read/write byte counts if 'close' was successful
            Props.setLong(Props.PROP_COMM_BYTES_READ   , 0, this.totalReadBytes );
            Props.setLong(Props.PROP_COMM_BYTES_WRITTEN, 0, this.totalWriteBytes);
        }
        
        /* clear volitile queue */
        this.getVolatileQueue().emptyQueue();

        /* re-enable event queue overwrites while not connected */
        //evEnableOverwrite(EVENT_QUEUE_OVERWRITE); // enabled only while not connected
        
        /* check for severe errors */
        if (xportType == TRANSPORT_DUPLEX) {
            if (this.severeErrorCount > 0) {
                // this helps prevent runnaway clients from abusing the server
                this.totalSevereErrorCount += this.severeErrorCount;
                Log.warn(LOG_NAME, "Severe errors encountered --> " + this.totalSevereErrorCount);
                if (this.totalSevereErrorCount >= MAX_SEVERE_ERRORS) {
                    // Slow down minimum connection interval
                    long minXmitRate = Props.getLong(Props.PROP_COMM_MIN_XMIT_RATE, 0, 0L);
                    if (minXmitRate < DateTime.HourSeconds(12)) {
                        //if (minXmitRate < 1) { minXmitRate = 1; }
                        if (minXmitRate < Constants.MIN_XMIT_RATE) { minXmitRate = Constants.MIN_XMIT_RATE; }
                        Props.addLong(Props.PROP_COMM_MIN_XMIT_RATE, 0, minXmitRate); // doubles rate
                    }
                    long minXmitDelay = Props.getLong(Props.PROP_COMM_MIN_XMIT_DELAY, 0, 0L);
                    if (minXmitDelay < DateTime.HourSeconds(12)) {
                        //if (minXmitDelay < 1) { minXmitDelay = 1; }
                        if (minXmitDelay < Constants.MIN_XMIT_DELAY) { minXmitDelay = Constants.MIN_XMIT_DELAY; }
                        Props.addLong(Props.PROP_COMM_MIN_XMIT_DELAY, 0, minXmitDelay); // doubles delay
                    }
                }
                if (this.totalSevereErrorCount >= EXCESSIVE_SEVERE_ERRORS) {
                    // Turn off periodic messaging
                    Props.setLong(Props.PROP_MOTION_START,          0, 0L);
                    Props.setLong(Props.PROP_MOTION_IN_MOTION,      0, 0L);
                    Props.setLong(Props.PROP_MOTION_DORMANT_INTRVL, 0, 0L);
                }
            } else
            if (this.totalSevereErrorCount > 0) {
                // a session without any severe errors will reduce this count
                this.totalSevereErrorCount--;
            }
        }
        
        return didClose;
    }
    
    // ----------------------------------------------------------------------------

    /**
    * Writes the specified byte array to the transport.
    * @param buf The data to be sent
    * @param calcChksum true to calculate checksums
    * @return The length of the data written.
    */
    private int _transportWrite(byte buf[], boolean calcChksum)
    {
        
        /* write */
        int len = this.transport.writePacket(buf);
        if (len >= 0) {
            if (calcChksum) { 
                this.fletcher.runningChecksum(buf); 
            }
            this.sessionWriteBytes += len;
            this.totalWriteBytes   += len;
        }
        return len;
        
    }
    
    /**
    * Writes the specified packet to the transport.
    * @param pkt The packet to be written
    * @return the length written
    */
    private int _transportWritePacket(Packet pkt)
    {
        byte pb[] = pkt.encode(this.sessionFirstEncoding);
        int rtnWriteLen = _transportWrite(pb, true);
        this.sessionFirstEncoding = this.sessionEncoding;
        return rtnWriteLen;
    }
    
    // ----------------------------------------------------------------------------

}
