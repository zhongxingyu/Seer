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
 
 import java.util.Vector;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.io.DataInputStream;
 import java.io.DataOutputStream;
 
 /** <p>Manages global distributed bus state.</p>
  *
  * <p>The single <code>SystemBus</code> instance, obtained via
  * <code>getSystemBus()</code>, contains the global state of the
  * distributed bus system. It provides methods for sending and
  * receiving UDP messages, and registering and removing connections
  * from being managed by the distributed bus.</p>
  *
  * <p>The bus system provides a low-level packet multiplexing service
  * that allows multiple "services" to transparently share the same
  * connections. In order to do this, it runs one thread per
  * <code>BusConnection</code> which polls for incoming messages and
  * dispatches them to the appropriate
  * <code>UDPMessageListener</code>.</p>
  *
  * @see #getSystemBus()
  * @see BusConnection
  * @see UDPMessageListener
  */
 public class SystemBus implements UDPMessageListener {
 
     /** A list of UDP port bindings */
     private Vector portBindings;
 
     /** Bind a UDP <code>service</code> to a particular UDP
      * <code>port</code>.
      *
      * @param service The service to be notified if a message arrives
      *                for <code>port</code>.
      * @param port    The port number that service wishes to listen on.
      *
      * @throws UDPBindException if the <code>port</code> has already
      *                          been bound.
      */
     public void addUDPService(UDPMessageListener service, int port) throws UDPBindException {
         if (service == null) {
             throw new IllegalArgumentException ("Invalid service");
         }
         /* Check that the port is not already bound */
         synchronized (portBindings) {
             for (int i = 0; i < portBindings.size(); i++) {
                 UDPBinding b = (UDPBinding) portBindings.elementAt(i);
                 if (port == b.port) {
                     throw new UDPBindException("Port " + Integer.toString(port) +
                                                " in use");
                 }
             }
             portBindings.addElement(new UDPBinding(service, port));
         }
     }
 
     /** Unbind a UDP service from a particular UDP port. After this
      * method is called, <code>service</code> will no longer be
      * notified of messages arriving for <code>port</code>. If
      * <code>port</code> is -1, unbinds the service from <em>all</em>
      * ports.
      *
      * @param service The service to be unbound.
      * @param port    The port to be unbound, or -1 to match all ports.
      */
     public void removeUDPService(UDPMessageListener service, int port) {
         synchronized (portBindings) {
             for (int i = 0; i < portBindings.size(); i++) {
                 UDPBinding b = (UDPBinding) portBindings.elementAt(i);
                 if (service == b.service) {
                     /* If the port was specified, only remove that
                      * particular binding */
                     if (port == b.port) {
                         portBindings.removeElement(b);
                         break;
                     }
                     /* If the port was not specified, remove every
                      * binding of this service */
                     if (port < 0) {
                         portBindings.removeElement(b);
                         continue;
                     }
                 }
             }
         }
     }
 
     /** Unbind a UDP service from all UDP ports. This is equivalent to
      * calling:
      *
      * <code>removeUDPService(service, -1).</code>
      *
      * @param service The service to be unbound.
      *
      * @see #removeUDPService(UDPMessageListener, int) */
     public void removeUDPService(UDPMessageListener service) {
         removeUDPService(service, -1);
     }
 
     /** Send a UDP message. Transmits <code>msg</code> on
      * <code>connection</code>.  If <code>connection</code> is
      * <code>null</code>, delivers the message locally.
      *
      * @param connection The connection to transmit on.
      * @param msg        The message to send.
      *
      * @throws IOException if an error occurs while transmitting the
      *                     message.
      */
     public void sendUDPMessage(BusConnection connection, UDPMessage msg)
         throws IOException {
         if (connection == null) {
             recvUDPMessage(connection, msg);
             return;
         }
 
         /* Pass message into connection */
         try {
             OutputStream out = connection.getOutputStream();
             synchronized (out) {
                 msg.send(new DataOutputStream(out));
             }
         } catch (IOException e) {
             connection.disconnect();
             throw e;
         }
     }
 
     /** Deliver a UDP message. Examines the destination port of
      * <code>msg</code>, and if there has been a service bound to that
      * port, delivers it to the service. If no service has been bound,
      * drops the <code>msg</code> silently.
      *
      * @param connection Connection the <code>msg</code> arrived from.
      * @param msg        Message to deliver.
      *
      * @see UDPMessage#getToPort()
      */
     public void recvUDPMessage(BusConnection connection, UDPMessage msg) {
         synchronized (portBindings) {
             for (int i = 0; i < portBindings.size(); i++) {
                 UDPBinding b = (UDPBinding) portBindings.elementAt(i);
                 if (msg.getToPort() == b.port) {
                     b.service.recvUDPMessage(connection, msg);
                     return;
                 }
             }
             /* No port binding was present, so silently drop the packet */
         }
     }
 
     /** Binding of UDP service to UDP port */
     private class UDPBinding {
         UDPMessageListener service;
         int port;
         UDPBinding(UDPMessageListener service, int port) {
             this.service = service;
             this.port = port;
         }
     }
 
     /* ************************************************** */
 
     /** A list of active connections. */
     private Vector connections;
     /** A list of UDPMonitors */
     private Vector connectionMonitors;
     /** A list of BusConnectionChangeListener. */
     private Vector connectionListeners;
 
     /** Add a a listener for connection change events. The
      * <code>BusConnectionChangeListener</code>'s
      * <code>connectionChanged()</code> method is called whenever a
      * connection is added or removed from the list of active
      * connections.
      *
      * @param l Event handler to add.
      *
      * @see BusConnectionChangeListener#connectionChanged(BusConnection, int)
      */
     public void addConnectionChangeListener(BusConnectionChangeListener l) {
         synchronized (connectionListeners) {
             if (connectionListeners.indexOf(l) == -1) {
                 connectionListeners.addElement(l);
             }
         }
     }
 
     /** Remove a listener for connection change events. If
      * <code>l</code> was not previously added with
      * <code>addConnectionChangeListener()</code>, does nothing.
      *
      * @param l Event handler to remove.
      */
     public void removeConnectionChangeListener(BusConnectionChangeListener l) {
         synchronized (connectionListeners) {
             connectionListeners.removeElement(l);
         }
     }
 
     /** Dispatch a connection change event to listeners. */
     private void connectionChangeDispatch(BusConnection c, int s) {
         synchronized (connectionListeners) {
             for (int i = 0; i < connectionListeners.size(); i++) {
                 BusConnectionChangeListener l = (BusConnectionChangeListener)
                     connectionListeners.elementAt(i);
                 l.connectionChanged(c, s);
             }
         }
     }
 
     /** <p>Get a list of active connections.</p>
      *
      * <p><b>Warning:</b> the <code>Vector</code> returned by this
      * call is owned by the <code>SystemBus<code> instance, and should
      * not be modified.</p>
      *
      * @return a <code>Vector</code> of
      *         <code>BusConnection</code>s.
      */
     public Vector getConnections() {
         return connections;
     }
 
     /** Add a connection to the active connections. Starts a thread
      * which monitors <code>connection</code>'s
      * <code>InputStream</code> for incoming messages, and dispatches
      * them when they arrive, until the connection is removed.
      *
      * @param connection Connection to add.
      */
     public void addConnection(BusConnection connection) {
         if (connection == null)
             throw new NullPointerException();
 
         synchronized (connections) {
             for (int i = 0; i < connections.size(); i++) {
                 if (connection == connections.elementAt(i)) {
                     return;
                 }
             }
             connections.addElement(connection);
         }
 
         synchronized (connectionMonitors) {
             UDPMonitor m = new UDPMonitor(connection);
             (new Thread(m)).start();
             connectionMonitors.addElement(m);
         }
 
         connectionChangeDispatch(connection,
                                  BusConnectionChangeListener.CONNECTION_ADDED);
     }
 
     /** Remove a connection from the active connections. Halts the
      * monitoring thread.
      *
      * Note that this does <em>not</em> call
      * <code>connection.disconnect()</code>.
      *
      * @param connection Connection to remove.
      *
      * @see BusConnection#disconnect()
      */
     public void removeConnection(BusConnection connection) {
         if (connection == null)
             throw new NullPointerException();
 
         /* Remove the connection from the list of active
          * connections. */
         synchronized (connections) {
             connections.removeElement(connection);
         }
 
         synchronized (connectionMonitors) {
             for (int i = 0; i < connectionMonitors.size(); i++) {
                 UDPMonitor m = (UDPMonitor) connectionMonitors.elementAt(i);
                 if (m.conn == connection) {
                     m.shutdown();
                     connectionMonitors.removeElement(m);
                     break;
                 }
             }
         }
 
         connectionChangeDispatch(connection,
                                  BusConnectionChangeListener.CONNECTION_REMOVED);
     }
 
     /** Monitors a BusConnection for incoming messages */
     private class UDPMonitor implements Runnable {
         BusConnection conn;
         private boolean enabled;
         private Object lock;
 
         UDPMonitor(BusConnection connection) {
             super();
             conn = connection;
             lock = new Object();
             enabled = true;
         }
 
         public void run() {
             try {
                 InputStream in = conn.getInputStream();
                 DataInputStream din = new DataInputStream(in);
                 while (true) {
                     synchronized (in) {
                         UDPMessage msg = UDPMessage.recv(din);
                         SystemBus.this.recvUDPMessage(conn, msg);
                     }
                     synchronized (lock) {
                         /* Note that shutdown() is only called from
                          * removeConnection(), so bypass calling it
                          * again. */
                         if (!enabled) return;
                     }
                     Thread.yield();
                 }
             } catch (IOException e) {
                /* Die */
             } finally {
                 removeConnection(conn);
             }
         }
 
         void shutdown() {
             synchronized (lock) {
                 enabled = false;
             }
         }
     }
 
     /* ************************************************** */
 
     /** Create a new system bus. Do not call this directly: use
      * getSystemBus().
      *
      * @see #getSystemBus()
      */
     protected SystemBus() {
         connections = new Vector();
         connectionMonitors = new Vector();
         connectionListeners = new Vector();
         portBindings = new Vector();
     }
 
     /** The singleton instance of the system bus */
     private static SystemBus instance = null;
 
     /** Get the system bus.
      *
      * @return the global <code>SystemBus</code>.
      */
     public static synchronized SystemBus getSystemBus() {
         if (instance == null) {
             instance = new SystemBus();
         }
         return instance;
     }
 }
