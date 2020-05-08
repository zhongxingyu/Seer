 /*
  * Distributed bus system for robotic applications
  * Copyright (C) 2009 University of Cambridge
  *
  * This program is free software; you can redistribute it and/or
  * modify it under the terms of the GNU General Public License as
  * published by the Free Software Foundation; either version 2 of the
  * License, or (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful, but
  * WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  * General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
  * USA
  */
 
 package uk.ac.cam.dbs;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.FilterInputStream;
 import java.io.OutputStream;
 import java.io.FilterOutputStream;
 
 import java.net.InetAddress;
 import java.net.ServerSocket;
 import java.net.Socket;
 import java.net.SocketTimeoutException;
 import java.net.SocketException;
 import java.net.NetworkInterface;
 
 import java.util.Enumeration;
 
 /** <p>Manages bus connections tunnelled over TCP/IP.</p>
  *
  * <p>The single <code>TCPIPConnectionManager</code> instance, obtained
  * via <code>getConnectionManager()</code>, provides a way of
  * tunnelling the distributed bus over TCP/IP links (e.g. the
  * Internet).</p>
  *
  * <p>It provides ways of initiating connections to remote devices as
  * well as a service for listening for incoming connections (the
  * default TCP port is 51992).</p>
  *
  * @see SystemBus
  * @see #getConnectionManager()
  */
 public class TCPIPConnectionManager implements BusConnectionServer {
     private int tcp_port = 51992;
     private InterfaceAddress localAddress = null;
 
     /* ************************************************** */
 
     /** <p>Connect to a remote host over TCP/IP. Equivalent to
      * calling:</p>
      *
      * <p><code>connectHost(hostname, getTCPPort())</code></p>
      *
      * @param hostname The hostname of the remote device.
      *
      * @return a newly-established <code>BusConnection</code>.
      *
      * @see #connectHost(String, int)
      */
     public BusConnection connectHost(String hostname)
         throws IOException {
 
         return connectHost(hostname, tcp_port);
     }
 
     /** <p>Connect to a remote host over TCP/IP. The remote device,
      * identified by <code>hostname</code>, must be listening for
      * incoming connections on the given <code>port</code>.
      *
      * <p>The resulting <code>BusConnection</code> is automatically
      * registered with the <code>SystemBus</code>.</p>
      *
      * @param hostname The hostname of the remote device.
      * @param port     The TCP port to connect to.
      *
      * @return a newly-established <code>BusConnection</code>.
      */
     public BusConnection connectHost(String hostname, int port)
         throws IOException {
 
         InetAddress hostAddr = InetAddress.getByName(hostname);
         Socket sock = new Socket(hostAddr, port);
 
         return this.new TCPIPConnection(sock, getLocalAddress());
     }
 
     /** BusConnection implementation for TCP/IP tunnels. */
     private class TCPIPConnection implements BusConnection {
         private Socket socket;
         private InputStream inStream;
         private OutputStream outStream;
         private InterfaceAddress localAddress;
         private InterfaceAddress remoteAddress;
 
         TCPIPConnection(Socket sock, InterfaceAddress localAddress)
             throws IOException {
             socket = sock;
 
             inStream = new FilterInputStream(socket.getInputStream()) {
                 public void close() throws IOException {
                     TCPIPConnection.this.disconnect();
                 }
             };
             outStream = new FilterOutputStream(socket.getOutputStream()) {
                 public void close() throws IOException {
                     TCPIPConnection.this.disconnect();
                 }
             };
 
             this.localAddress = localAddress;
 
             /* Exchange interface addresses (should be the first 16
              * bytes sent over connection) */
             outStream.write(getLocalAddress().getBytes());
             byte[] buf = new byte[16];
             int i = 0;
             while (i < buf.length) {
                 i += inStream.read(buf, i, buf.length - i);
             }
             remoteAddress = new InterfaceAddress(buf);
 
             SystemBus.getSystemBus().addConnection(this);
         }
 
         public InputStream getInputStream()
             throws IOException {
 
             return inStream;
         }
 
         public OutputStream getOutputStream()
             throws IOException {
 
             return outStream;
         }
 
         public synchronized void disconnect() throws IOException {
             if (socket.isClosed()) {
                 return;
             }
 
             /* Unregister with SystemBus */
             SystemBus.getSystemBus().removeConnection(this);
 
             socket.close();
         }
 
         public boolean isConnected() {
             return socket.isConnected();
         }
 
         public InterfaceAddress getLocalAddress() {
             return localAddress;
         }
 
         public InterfaceAddress getRemoteAddress() {
             return remoteAddress;
         }
     }
 
     /* ************************************************** */
 
     protected int ACCEPT_TIMEOUT = 100; /* ms */
 
     private Thread serverThread;
 
     /** <p>Set the default TCP port. The
      * <code>TCPIPConnectionManager</code> will listen on this port,
      * and it will be used as the default port when connecting to
      * remote hosts.</p>
      *
      * <p>Calling this method has no effect on existing connections.</p>
      *
      * @param port New default TCP port.
      *
      * @see #setListenEnabled(boolean)
      * @see #connectHost(String)
      */
     public synchronized void setTCPPort(int port) {
         if (tcp_port == port) return;
         tcp_port = port;
         localAddress = null; /* Reset this! */
 
         /* If necessary, restart the server thread. */
         if (isListenEnabled()) {
             setListenEnabled(false);
             setListenEnabled(true);
         }
     }
 
     /** Get the default TCP port.
      *
      * @return the default TCP port for new TCP/IP tunnel connections.
      */
     public int getTCPPort() {
         return tcp_port;
     }
 
     /** <p>Set whether connections are accepted from other devices.<p>
      *
      * <p>If <code>enabled</code> is <code>true</code>, accept
      * incoming connections on the current default TCP port.</p>
      *
      * @param enabled If <code>true</code>, listen for incoming
      *                connections.
      *
      * @see #setTCPPort(int)
      */
     public synchronized void setListenEnabled(boolean enabled) {
 
        if (isListenEnabled()) {
             serverThread.interrupt();
             try {
                 serverThread.join();
             } catch (InterruptedException e) {
                 Thread.currentThread().interrupt();
             }
             serverThread = null;
        } else {
             serverThread = new Thread (new TCPIPServer());
 
             /* Don't let server thread keep the program running. */
             serverThread.setDaemon(true);
             serverThread.start();
         }
         /* FIXME should we check the server actually has started
          * successfully, and throw an Exception if it doesn't? */
     }
 
     /** <p>Test whether connections are accepted from other devices.</p>
      *
      * @return <code>true</code> if listening for incoming connections.
      */
     public boolean isListenEnabled() {
         if (serverThread != null) {
             return serverThread.isAlive();
         } else {
             return false;
         }
     }
 
     private class TCPIPServer implements Runnable {
 
         public void run() {
             ServerSocket listen_sock = null;
             try {
                 listen_sock = new ServerSocket(tcp_port);
                 listen_sock.setSoTimeout(ACCEPT_TIMEOUT);
                 while (!Thread.interrupted()) {
                     try {
                         Socket client_sock = listen_sock.accept();
 
                         /* This *isn't* just being thrown away! */
                         InterfaceAddress localAddr =
                             TCPIPConnectionManager.this.getLocalAddress();
                         TCPIPConnectionManager.this.new
                             TCPIPConnection(client_sock, localAddr);
                     } catch (SocketTimeoutException e) {
                         continue;
                     }
                 }
             } catch (IOException e) {
                 System.err.printf("SPP server error: %1$s", e.getMessage());
             } finally {
                 if (listen_sock != null) {
                     try {
                         listen_sock.close();
                     } catch (IOException e) { }
                 }
             }
         }
     }
 
     /* ************************************************** */
 
     /** Get the local interface address.
      *
      * @return the interface address used by TCP/IP
      * <code>BusConnection</code>s created by this connection manager.
      *
      * @see BusConnection#getLocalAddress()
      */
     public InterfaceAddress getLocalAddress() {
         if (localAddress != null) return localAddress;
 
         /* Try and find an EUI-64 to latch onto. */
         byte[] mac = null;
         try {
             Enumeration<NetworkInterface> ifaces =
                 NetworkInterface.getNetworkInterfaces();
             while (ifaces.hasMoreElements()) {
                 NetworkInterface iface = ifaces.nextElement();
                 mac = iface.getHardwareAddress();
                 if (mac != null) break;
             }
         } catch (SocketException e) {
             throw new RuntimeException(e);
         }
 
         localAddress = new Rfc4193InterfaceAddress(mac);
         return localAddress;
     }
 
     /** Create a new <code>TCPIPConnectionManager</code>. Do not call
      * this directly: use <code>getConnectionManager()</code>.
      *
      * @see #getConnectionManager()
      */
     protected TCPIPConnectionManager() {
         serverThread = null;
     }
 
     /** The singleton instance of the connection manager. */
     private static TCPIPConnectionManager instance = null;
 
     /** Get the TCP/IP connection manager
      *
      * @return the global <code>TCPIPConnectionManager</code>.
      */
     public static synchronized TCPIPConnectionManager getConnectionManager() {
         if (instance == null) {
             instance = new TCPIPConnectionManager();
         }
         return instance;
     }
 }
