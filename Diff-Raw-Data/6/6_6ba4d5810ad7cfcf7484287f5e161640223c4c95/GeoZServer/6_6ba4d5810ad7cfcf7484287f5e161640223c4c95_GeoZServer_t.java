 /* Copyright (c) 2001, 2003 TOPP - www.openplans.org.  All rights reserved.
  * This code is licensed under the GPL 2.0 license, availible at the root
  * application directory.
  */
 
 // This program is free software; you can redistribute it and/or
 // modify it under the terms of the GNU Lesser General Public License
 // as published by the Free Software Foundation; either version 2.1 of
 // the license, or (at your option) any later version.
 //
 // This program is distributed in the hope that it will be useful,
 // but WITHOUT ANY WARRANTY; without even the implied warranty of
 // MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 // GNU Lesser General Public License for more details.
 //
 // You should have received a copy of the GNU Lesser General Public License
 // along with this program; if not, write to the Free Software
 // Foundation, Inc., 59 Temple Place - Suite
 // 330, Boston, MA  02111-1307, USA.
 //
 package org.vfny.geoserver.zserver;
 
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.ServerSocket;
 import java.net.Socket;
 import java.util.Properties;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import com.k_int.codec.util.OIDRegister;
 
 
 /**
  * GeoZServer : Controller class for a Z39.50 Server
  *
  * @author Chris Holmes,
  * @author Ian Ibbotson  This class listens on an identified socket. On new
  *         connections,  a new instance of ZServerAssociation is created with
  *         a parameter  that identifies a concrete realisation of the IR
  *         Searchable class.  The ZServerAssociation talks with the abstract
  *         Searchable interface  and uses that service to resolve Z39.50
  *         services.
  * @version $Id: GeoZServer.java,v 1.10 2004/02/09 23:29:45 dmzwiers Exp $ Made by slightly modifying ZServer.java to start up with a properties file, so it can be invoked by another class.  Most code by:
  */
 public class GeoZServer extends Thread {
     /* Initializes the logger. */
 
     /** Standard logging instance for class */
     private static final Logger LOGGER = Logger.getLogger(
             "org.vfny.geoserver.zserver");
 
     /**
      * sets the logging level.
      */
     static {
         //LOGGER.setLevel(Level.parse("FINER"));
     }
 
     private int socket_timeout = 300000; // 300 second default timeout
     private boolean running = true;
     private ServerSocket server_socket = null;
     private Properties server_properties = null;
 
     /**
      * Constructor, with properties to use in props file.
      *
      * @param props DOCUMENT ME!
      *
      * @throws java.io.IOException DOCUMENT ME!
      */
     public GeoZServer(Properties props) throws java.io.IOException {
         server_properties = props;
 
         String port_str = server_properties.getProperty("port");
         String timeout_str = server_properties.getProperty("timeout");
 
         if (timeout_str != null) {
             socket_timeout = Integer.parseInt(timeout_str);
         }
 
         LOGGER.info("Creating ZServer on port: " + port_str);
 
         //+" (timeout="+socket_timeout+")");
         String attrMapFile = props.getProperty("fieldmap");
 
         if (attrMapFile != null) {
             GeoProfile.setUseAttrMap(attrMapFile);
         }
 
         server_socket = new ServerSocket(Integer.parseInt(port_str));
 
         GeoIndexer indexer = new GeoIndexer(server_properties);
         int numIndexed = indexer.update();
         LOGGER.finer(numIndexed + " documents indexed");
     }
 
     /**
      * To start up server from command line, call with a props file with the
      * appropriate properties.
      *
      * @param args DOCUMENT ME!
      *
      * @throws IOException DOCUMENT ME!
      */
     public static void main(String[] args) throws IOException {
         Properties defaults = new Properties();
         Properties props = new Properties(defaults);
         OIDRegister reg = OIDRegister.getRegister();
         Class thisClass = reg.getClass();
 
         // Load default properties
         try {
             // read from top of any classpath entry
             InputStream is = reg.getClass().getResourceAsStream("/com/k_int/z3950/server/default.props");
             defaults.load(is);
         } catch (Exception e) {
             e.printStackTrace();
         }
 
         if (args.length >= 1) {
             // Parameter should be name of a properties file
             try {
                 String props_file_name = args[0];
                 LOGGER.finer("Attempting to load local properties file "
                     + props_file_name);
 
                 InputStream is = new FileInputStream(props_file_name);
                 props = new Properties(defaults);
                 props.load(is);
 
                 LOGGER.finer("Done loading local props");
             } catch (Exception e) {
                 e.printStackTrace();
                 System.err.println("No system properties parameter given");
                 System.err.println("Usage: java org.vfny.geoserver.GeoZServer"
                     + "<<propsfile>>");
                 System.exit(0);
             }
         }
 
         GeoZServer server = new GeoZServer(props);
         server.start();
     }
 
     public void run() {
         try {
             while (running) {
                 LOGGER.finer("Waiting for connection...");
 
                 Socket socket = (Socket) server_socket.accept();
                 socket.setSoTimeout(socket_timeout);
 
                 GeoZServerAssociation za = new GeoZServerAssociation(socket,
                         server_properties);
             }
 
             server_socket.close();
         } catch (java.io.IOException e) {
             LOGGER.finer("problem with zserver " + e);
         }
     }
 
     public void shutdown(int shutdown_type) {
         this.running = false;
 
         switch (shutdown_type) {
         default:
 
             // Currently no special processing to join 
             //with or shutdown active associations.
             try {
                 server_socket.close();
             } catch (java.io.IOException ioe) {
                 // No special action
             }
 
             break;
         }
 
         try {
             this.join();
         } catch (java.lang.InterruptedException ie) {
             // No action
         }
     }
 }
