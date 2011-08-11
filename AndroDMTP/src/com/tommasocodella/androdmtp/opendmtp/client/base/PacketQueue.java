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
//  This class is a container for Packets that are waiting to be sent to the
//  server on the next connection.
// ----------------------------------------------------------------------------
// Change History:
//  2006/03/26  Martin D. Flynn
//     -Initial release
//  2006/11/03  Kiet Huynh
//     -Include JavaDocs
//  2007/??/??  Martin D. Flynn
//     -Added method 'getQueueSize()'
// ----------------------------------------------------------------------------
package com.tommasocodella.androdmtp.opendmtp.client.base;

import java.util.Enumeration;
import java.util.Vector;

import com.tommasocodella.androdmtp.opendmtp.util.GeoEvent;

/**
* A Queue that contains Packets to be sent to the server.
*/
public class PacketQueue
{
    
    // ------------------------------------------------------------------------
    
    private Vector  queue = null;
    
    /**
    * Creates a new PacketQueue instance that contains packets to be sent to the server.
    */
    public PacketQueue()
    {
        this.queue = new Vector();
    }
        
    // ------------------------------------------------------------------------

    /**
    * Removes packets that are currently in this queue.
    */
    public void emptyQueue()
    {
        synchronized (this.queue) {
            this.queue.removeAllElements();
        }
    }
        
    // ------------------------------------------------------------------------

    /**
    * Checks if this queue is empty.
    * @return True if this queue is empty. Otherwise, returns false.
    */
    public boolean isEmpty()
    {
        boolean empty;
        synchronized (this.queue) {
            empty = this.queue.isEmpty();
        }
        return empty;
    }
    
    /** 
    * Returns the number of packets in the queue
    * @return The number of packets in the queue
    */
    public long getQueueSize()
    {
        long size = 0L;
        synchronized (this.queue) {
            size = this.queue.size();
        }
        return size;
    }
    
    /**
    * Checks whether this queue has unsent packets
    * @return True if this queue has any packets that have not been sent to the server, false otherwise
    */
    public boolean hasUnsentPackets()
    {
        // We only need to check the very last packet in the queue.  If the last
        // packet was sent, then all preceding packets have been sent.
        boolean hasUnsent;
        synchronized (this.queue) {
            int len = this.queue.size();
            hasUnsent = (len > 0)? !((Packet)this.queue.elementAt(len-1)).isSent() : false;
        }
        return hasUnsent;
    }
    
    /**
    * Clears all contained packets to 'unsent' state.
    */
    public void resetSent()
    {
        synchronized (this.queue) {
            for (Enumeration i = this.queue.elements(); i.hasMoreElements();) {
                Packet pkt = (Packet)i.nextElement();
                if (pkt.isSent()) {
                    pkt.setSent(false);
                } else {
                    // we can stop at the first unsent packet
                    break;
                }
            }
        }
    }
    
    // ------------------------------------------------------------------------

    /**
    * Adds a new Packet to this queue.
    * @param pkt The new Packet.
    */
    public void addPacket(Packet pkt)
    {
        // TODO: Limit the number of packets added to this queue?
        synchronized (this.queue) {
            this.queue.addElement(pkt);
        }
    }
        
    // ------------------------------------------------------------------------

    /**
    * Creates a Packet that contains GeoEvent and adds this packet the this queue.
    * @param priority The packet priority (currently unused)
    * @param event The GeoEvent that will be added to the packet.
    */
    public void addEvent(int priority, GeoEvent event)
    {
        Packet evPkt = Packet.createClientEventPacket(event, null);
        this.addPacket(evPkt);
    }
        
    // ------------------------------------------------------------------------
    
    /**
    * Returns the packet at the specified index in the PacketQueue.
    * @param ndx The index of the packet.
    * @return The Packet at the specified index. If the index is invalid, returns null.
    */
    public Packet getPackatAt(int ndx)
    {
        if (ndx >= 0) {
            Packet pkt = null;
            synchronized (this.queue) {
                if (ndx < this.queue.size()) {
                    pkt = (Packet)this.queue.elementAt(ndx);
                }
            }
            return pkt;
        } else {
            return null;
        }
    }
    
    // ------------------------------------------------------------------------

    /**
    * Deletes all unsent packets from up to, and including, the specified sequence number
    * @param seq The specified index.
    * @return The number of packets deleted.
    */
    public int deleteToSequence(long seq)
    {
        boolean delAll = (seq == Packet.SEQUENCE_ALL) || (seq < 0);
        int deleteCount = 0;
        synchronized (this.queue) {
            while (this.queue.size() > 0) {
                
                /* get next packet */
                Packet pkt = (Packet)this.queue.elementAt(0);
                
                /* not sent */
                if (!pkt.isSent()) {
                    // stop at the first un-sent packet
                    break;
                }
                
                /* remove */
                this.queue.removeElementAt(0);
                deleteCount++;
                
                /* check matching sequence */
                if (!delAll && (pkt.getEventSequence() == seq)) {
                    // stop at the first matching sequence number
                    break;
                }

            }
        }
        return deleteCount;
    }
    
    /**
    * Removes the first Packet in the PacketQueue if it has been sent.
    * @return True if the Packet was deleted, false otherwise.
    */
    public boolean deleteFirstSent()
    {
        boolean didDelete = false;
        synchronized (this.queue) {
            if (this.queue.size() > 0) {
                Packet pkt = (Packet)this.queue.elementAt(0);
                if (pkt.isSent()) {
                    this.queue.removeElementAt(0);
                    didDelete = true;
                }
            }
        }
        return didDelete;
    }

    // ------------------------------------------------------------------------

    /**
    * Gets the highest priority number of the Packets in the Queue.
    * @return The highest priority value.
    */
    public int getHighestPriority()
    {
        int priority = Packet.PRIORITY_NONE;
        synchronized (this.queue) {
            for (Enumeration i = this.queue.elements(); i.hasMoreElements() && (priority < Packet.PRIORITY_HIGH);) {
                Packet pkt = (Packet)i.nextElement();
                int pri = pkt.getPriority();
                if (pri > priority) {
                    priority = pri;
                }
            }
        }
        return priority;
    }
    
}
