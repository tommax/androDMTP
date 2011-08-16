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
//  This class manages the transport layer for the OpenDMTP protocol.
// ----------------------------------------------------------------------------
// Change History:
//  2007/??/??  Martin D. Flynn
//     -Initial release
//     -Initial JavaDocs provided by Robert S. Brewer
// ----------------------------------------------------------------------------
package com.tommasocodella.androdmtp.services;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import com.tommasocodella.androdmtp.opendmtp.client.base.Packet;
import com.tommasocodella.androdmtp.opendmtp.client.base.Props;
import com.tommasocodella.androdmtp.opendmtp.client.base.Protocol;
import com.tommasocodella.androdmtp.opendmtp.client.base.Transport;
import com.tommasocodella.androdmtp.opendmtp.codes.Encoding;
import com.tommasocodella.androdmtp.opendmtp.util.Log;

/**
* Manages the transport layer for the OpenDMTP protocol, handling both simplex and duplex
* communication with a common interface. It also performs caching of data until it is
* ready to be sent.
*/
public class TransportImpl
    implements Transport
{

    // ------------------------------------------------------------------------

    private static final String LOG_NAME = "XPORT";

    // ----------------------------------------------------------------------------

    /**
    * Wraps a simplex datagram (UDP) data structure, providing standard data communication
    * methods, including caching data until it is ready to be sent.
    */
    private static class DatagramWrapper
    {
        private String          host = "";
        private int             port = 0;
        private ByteArrayOutputStream cache = null;
        /**
        * Sets host and port fields after quick validation. If hostname is null, then hostname
        * is set to the empty String.
        * @param host hostname to send data to
        * @param port port to connect to on host
        */
        public DatagramWrapper(String host, int port) {
            this.host = (host != null)? host.trim() : "";
            this.port = port;
        }
        /**
        * Opens the datagram connection. Basically a no-op provided for symmetry with
        * the duplex communication class SocketWrapper.
        * @return true always, since datagrams are connectionless
        */
        public boolean open() {
            return true;
        }
        /**
        * Appends data to the cache, creating the cache object if it has not been initialized
        * already.
        * @param data bytes to be added to the cache.
        */
        public void appendData(byte data[]) {
            if (this.cache == null) { this.cache = new ByteArrayOutputStream(); }
            this.cache.write(data, 0, data.length);
        }
        /**
        * Attempts to send the datagram to the previously specified hostname and port. Logs
        * errors if there are problems sending the datagram.
        * @return true if the datagram was successfully sent, and false for failure.
        */
        public boolean send() {
            if (this.cache == null) {
                return false; // nothing to send
            }
            if (this.host.equals("") || (port <= 0)) {
                Log.error(LOG_NAME, "Invalid 'host:port': " + this.host + ":" + this.port);
                return false;
            }
            boolean rtn = true;
            byte data[] = this.cache.toByteArray();
            /*J2ME
            UDPDatagramConnection socket = null;
            try {
                String uri = "datagram://" + this.host + ":" + this.port;
                Log.debug(LOG_NAME, "UDP Connect: " + uri);
                socket = (UDPDatagramConnection)Connector.open(uri, Connector.WRITE, true);
                Datagram packet = socket.newDatagram(data, data.length);
                socket.send(packet);
            } catch (ConnectionNotFoundException e) {
                Log.error(LOG_NAME, "Unable to connect to UDP host: " + this.host + ":" + this.port);
                return false;
            } catch (InterruptedIOException iioe) { // timeout
                Log.error(LOG_NAME, "Timeout sending UDP");
                rtn = false;
            } catch (Throwable t) {
                Log.error(LOG_NAME, "Unable to send UDP", t);
                rtn = false;
            } finally {
                if (socket != null) { try { socket.close(); } catch (Throwable t) {} }
            }
            */
            /*J2SE*/
            DatagramSocket socket = null;
            try {
                socket = new DatagramSocket();
                Log.info(LOG_NAME, "Sending to: " + this.host + ":" + this.port);
                InetAddress hostAddr = InetAddress.getByName(this.host);
                DatagramPacket packet = new DatagramPacket(data, data.length, hostAddr, port);
                //packet.setData(data, 0, data.length);
                socket.send(packet);
            } catch (Throwable t) {
                Log.error(LOG_NAME, "Unable to send UDP", t);
                rtn = false;
            } finally {
                if (socket != null) { socket.close(); }
            }
            /**/
            this.cache = null;
            return rtn;
        }
        /**
        * Closes the datagram "connection", which boils down to just clearing the cache.
        */
        public void close() {
            this.cache = null;
        }
    }
    
    /**
    * Wraps a duplex socket (TCP) data structure, providing standard data communication
    * methods.
    */
    private static class SocketWrapper
    {
        private String          host = "";
        private int             port = 0;
        private InputStream     input = null;
        private OutputStream    output = null;
        /*J2ME*
        private SocketConnection socket = null;
        /**/
        /*J2SE*/
        private Socket          socket = null;
        /**/
        /**
        * Sets host and port fields after quick validation. If hostname is null, then hostname
        * is set to the empty String.
        * @param host hostname to send data to
        * @param port port to connect to on host
        */
        public SocketWrapper(String host, int port) {
            this.host = (host != null)? host.trim() : "";
            this.port = port;
        }
        /**
        * Opens the socket connection to the previously specified hostname and port, logging
        * any errors encountered.
        * @return true if the socket could be opened, false otherwise.
        */
        public boolean open() {
            if (this.host.equals("") || (port <= 0)) {
                Log.error(LOG_NAME, "Invalid 'host:port': " + this.host + ":" + this.port);
                return false;
            }
            /*J2ME*
            try {
                String uri = "socket://" + this.host + ":" + this.port;
                Log.debug(LOG_NAME, "TCP Connect: " + uri);
                this.socket = (SocketConnection)Connector.open(uri, Connector.READ_WRITE, true);
                this.input  = this.socket.openDataInputStream();
                this.output = this.socket.openDataOutputStream();
                Log.debug(LOG_NAME, "TCP Connected ...");
            } catch (ConnectionNotFoundException e) {
                Log.error(LOG_NAME, "Unable to connect to TCP host: " + this.host + ":" + this.port);
                return false;
            } catch (InterruptedIOException iioe) { // timeout
                Log.error(LOG_NAME, "Timeout sending to TCP host: " + this.host + ":" + this.port);
                return false;
            } catch (IOException ioe) {
                Log.error(LOG_NAME, "Exception", ioe);
                return false;
            }
            /**/
            /*J2SE*/
            try {
            	Log.info(LOG_NAME, "Sending to: " + this.host + ":" + this.port);
                this.socket = new Socket(this.host, this.port);
                this.input  = this.socket.getInputStream();
                this.output = this.socket.getOutputStream();
                this.socket.setSoTimeout(3000);
            } catch (UnknownHostException uhe) {
                Log.error(LOG_NAME, "Unable to find host: " + this.host + ":" + this.port);
                return false;
            } catch (IOException ioe) {
                Log.error(LOG_NAME, "Exception", ioe);
                return false;
            }
            /**/
            return true;
        }
        /**
        * Accessor for InputStream field.
        * @return the InputStream
        * @throws IOException if unexpected IO error occurs.
        */
        public InputStream getInputStream() throws IOException {
            return this.input;
        }
        /**
        * Accessor for OutputStream field.
        * @return the OutputStream
        * @throws IOException if unexpected IO error occurs.
        */
        public OutputStream getOutputStream() throws IOException {
            return this.output;
        }
        /**
        * Closes socket, InputStream, and OutputStream if they are non-null.
        */
        public void close() {
            try { if (this.input  != null) { this.input .close(); } } catch (Throwable t) {}
            try { if (this.output != null) { this.output.close(); } } catch (Throwable t) {}
            try { if (this.socket != null) { this.socket.close(); } } catch (Throwable t) {}
        }
    }
    
    // ----------------------------------------------------------------------------

    private String                  host = null;
    private int                     port = 0;
    private int                     xportType = Protocol.TRANSPORT_NONE;
    
    private SocketWrapper           socket = null;
    private DatagramWrapper         datagram = null;
    
    /**
    * Default constructor
    * Initializes transport type to Protocol.TRANSPORT_NONE.
    */
    public TransportImpl()
    {
        this.xportType = Protocol.TRANSPORT_NONE;
    }

    // ----------------------------------------------------------------------------

    /**
    * Indicates whether the transport is open or not.
    * @return false if transport type is uninitialized, otherwise true.
    */
    public boolean isOpen()
    {
        return (this.xportType != Protocol.TRANSPORT_NONE);
    }
    
    /**
    * Opens a connection of the specified transport type to the hostname and port
    * specified by the user in the client properties. Logs errors on failure.
    * @param xportType type of transport to be opened, see Protocol constants.
    * @return true if connection could be established, false otherwise.
    * @see org.opendmtp.j2me.client.base.Protocol
    */
    public boolean open(int xportType)
    {
        // Protocol.TRANSPORT_SIMPLEX
        // Protocol.TRANSPORT_DUPLEX
        
        /* aready open? */
        if (this.isOpen()) {
            Log.warn(LOG_NAME, "Transport seems to still be open!");
            this.close(false);
        }

        /* get host:port */
        this.host = Props.getString(Props.PROP_COMM_DMTP_HOST, "");
        this.port = (int)Props.getLong(Props.PROP_COMM_DMTP_PORT, 0, 0L);
        Log.info(LOG_NAME, "Connecting to: " + this.host + ":" + this.port);
        if ((this.host == null) || this.host.equals("") || (this.port <= 0)) {
            Log.warn(LOG_NAME, "TransportImpl.open: host/port not specified ...");
            // If we don't have a valid host:port, this problem will likely not be fixed 
            // soon, so go ahead and sleep awhile.
            try { Thread.sleep(30000L); } catch (Throwable t) {}
            return false;
        }
        Log.info(LOG_NAME, "TransportImpl.open: host=" + this.host + " port=" + this.port);

        /* open */
        switch (xportType) {
            case Protocol.TRANSPORT_SIMPLEX:
                this.datagram = new DatagramWrapper(this.host, this.port);
                if (!this.datagram.open()) {
                    return false;
                }
                break;
            case Protocol.TRANSPORT_DUPLEX:
                this.socket = new SocketWrapper(this.host, this.port);
                if (!this.socket.open()) {
                    return false;
                }
                break;
            default:
                Log.error(LOG_NAME, "Invalid Transport type: " + xportType);
                return false;
        }
        this.xportType = xportType;

        /* reset buffers and return success */
        Log.debug(LOG_NAME, "Openned Transport ...");
        return this.isOpen();
        
    }

    // ----------------------------------------------------------------------------

    /**
    * Closes the connection, optionally sending the UDP datagram if one exists.
    * @param sendUDP if true, then attempt to send cached data to remote end.
    * @return true if connection was successfully closed, false if it could not be closed
    * or the UDP datagram could not be sent.
    * @see org.opendmtp.j2me.client.base.Transport#close(boolean)
    */
    public boolean close(boolean sendUDP)
    {
        boolean rtn = true;
        
        /* send UDP Datagram? */
        if (sendUDP && (this.xportType == Protocol.TRANSPORT_SIMPLEX)) {
            if (this.datagram != null) {
                rtn = this.datagram.send();
            } else {
                Log.error(LOG_NAME, "Datagram not defined");
                rtn = false;
            }
        }
        if (this.datagram != null) {
            this.datagram.close();
            this.datagram = null;
        }

        /* make sure duplex is closed */
        if (this.socket != null) {
            this.socket.close();
            this.socket = null;
        }
        
        /* clear vars */
        this.xportType = Protocol.TRANSPORT_NONE;
        
        return rtn;
    }

    // ----------------------------------------------------------------------------

    /**
    * Reads the payload from an OpenDMTP packet on a duplex connection. Not applicable
    * to simplex connections or closed connections.
    * @return the payload of the packet, or null if there was an error receiving or decoding
    * it.
    * @see org.opendmtp.j2me.client.base.Transport#readPacket()
    */
    public byte[] readPacket()
    {
        
        /* open? */
        if (!this.isOpen()) {
            Log.warn(LOG_NAME, "Transport not open!");
            return null;
        }

        /* cannot read when connecting via Simplex */
        if (this.xportType == Protocol.TRANSPORT_SIMPLEX) {
            Log.error(LOG_NAME, "Cannot read from Simplex transport");
            return null;
        }
        
        /* read packet */
        try {
            
            /* get input stream */
            InputStream input = this.socket.getInputStream();
            byte pkt[] = new byte[600];
            int rlen = 0;
    
            /* read encoding indicator */
            rlen = input.read(pkt, 0, 1);
            if (rlen != 1) {
                Log.error(LOG_NAME, "Unable to read encoding character");
                return null;
            }
        
            /* ASCII packet? */
            if (pkt[0] == Encoding.AsciiEncodingChar) {
                int b = 1;
                for (; b < pkt.length;) {
                    int ch = input.read();
                    if (ch < 0) {
                        return null; // end-of-input
                    } else
                    if (ch == Encoding.AsciiEndOfLineChar) {
                        break;
                    }
                    pkt[b++] = (byte)ch;
                }
                byte p[] = new byte[b];
                System.arraycopy(pkt, 0, p, 0, p.length);
                return p;
            }

            /* read remainder of header */
            rlen = input.read(pkt, 1, Packet.MIN_HEADER_LENGTH - 1);
            if (rlen != (Packet.MIN_HEADER_LENGTH - 1)) {
                Log.error(LOG_NAME, "Unable to read header");
                return null;
            }

            /* read payload */
            int payloadLen = (int)pkt[2] & 0xFF;
            if (payloadLen > 0) {
                int len = input.read(pkt, Packet.MIN_HEADER_LENGTH, payloadLen);
                if (len != payloadLen) {
                    Log.error(LOG_NAME, "Unable to read payload");
                    return null;
                }
            }

            /* return packet */
            byte p[] = new byte[Packet.MIN_HEADER_LENGTH + payloadLen];
            System.arraycopy(pkt, 0, p, 0, p.length);
            return p;
            
        } catch (InterruptedIOException ee) { // SocketTimeoutException ee)
            Log.error(LOG_NAME, "Timeout");
            return null;
        } catch (Throwable t) {
            Log.error(LOG_NAME, "Exception", t);
            return null;
        }

    }
    
    // ----------------------------------------------------------------------------
    
    /**
    * Attempts to send bytes to the remote side. Bytes are sent on duplex connections
    * immediately, while bytes on simplex connections are cached for later transmission.
    * @param b the bytes to be sent as the payload.
    * @return the number of bytes sent, or -1 if there was a problem sending.
    * @see org.opendmtp.j2me.client.base.Transport#writePacket(byte[])
    */
    public int writePacket(byte b[])
    {
        
        /* open? */
        if (!this.isOpen()) {
            Log.warn(LOG_NAME, "Transport not open!");
            return -1;
        }

        /* simplex */
        if (this.xportType == Protocol.TRANSPORT_SIMPLEX) {
            if (this.datagram != null) {
                this.datagram.appendData(b);
                return b.length;
            } else {
                Log.error(LOG_NAME, "Datagram not initialized");
                return -1;
            }
        }

        /* duplex */
        if (this.xportType == Protocol.TRANSPORT_DUPLEX) {
            if ((this.socket != null) && (b != null)) {
                try {
                    OutputStream output = this.socket.getOutputStream();
                    output.write(b);
                    output.flush();
                    return b.length;
                } catch (IOException ioe) {
                    Log.error(LOG_NAME, "Unable to write packet");
                    return -1;
                }
            } else {
                Log.error(LOG_NAME, "Null socket/data");
                return -1;
            }
        }
        
        /* error */
        Log.error(LOG_NAME, "Undefined transport: " + this.xportType);
        return -1;
        
    }
    
}
