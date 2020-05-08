 
 				/*
  *  Adito
  *
  *  Copyright (C) 2003-2006 3SP LTD. All Rights Reserved
  *
  *  This program is free software; you can redistribute it and/or
  *  modify it under the terms of the GNU General Public License
  *  as published by the Free Software Foundation; either version 2 of
  *  the License, or (at your option) any later version.
  *  This program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public
  *  License along with this program; if not, write to the Free Software
  *  Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
  */
 			
 package com.adito.jdbc.hsqldb;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.io.Writer;
 import java.net.Socket;
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.Statement;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
import org.hsqldb.HsqlProperties;
 import org.hsqldb.Server;
 import org.hsqldb.ServerConstants;
 
 import com.adito.boot.SystemProperties;
 import com.adito.core.CoreServlet;
 import com.adito.jdbc.JDBCConnectionImpl;
 
 /**
  * <p>
  * Maintains the embedded HSQLDB server engine. When Adito is started up,
  * an instance is created and then {@link #start()}ed.
  * 
  * <p>
  * Plugins may then register any new databases they wish to add to the server
  * (although this should be called indirectly through
  * {@link com.adito.core.CoreServlet#addDatabase(String,File)}.
  * 
  * <p>
  * When the server is shutting down, the {@link #stop()} method should be called
  * allow the database engine to clean itself up.
  */
 public class EmbeddedHSQLDBServer {
 
     private final static Log log = LogFactory.getLog(EmbeddedHSQLDBServer.class);
 
     private HsqlProperties properties;
     private Server server;
     private boolean testedConnection;
     private boolean serverMode;
     private int dbIdx = 1;
     private List<String> databases;
     private boolean started;
 
     /**
      * Constructor
      * 
      * @param serverMode if <code>true</code> run HSQLDB in <b>Server</b>
      *        mode, which allows external TCP/IP connections.
      * @throws Exception
      */
     public EmbeddedHSQLDBServer(boolean serverMode) throws Exception {
         super();
 
         databases = new ArrayList<String>();
         this.serverMode = serverMode;
         if (serverMode) {
             properties = new HsqlProperties();
         }
     }
 
     /**
      * Stop the Database engine.
      */
     public void stop() {
 
         if (server != null) {
 
             /*
              * TODO A nasty hack. HSQLDB cannot have new databases added to it
              * while its running in TCP/IP server mode. So we have to restart
              * the server. Unfortunately, the client side of the connection does
              * not register that this has happened so is considered re-useable
              * by the pool. This results in a 'Connection is closed' error when
              * then next statement executes.
              */
             JDBCConnectionImpl.JDBCPool.getInstance().closeAll();
 
             // Get a JDBC connection
             for (Iterator i = databases.iterator(); i.hasNext();) {
                 String n = (String) i.next();
                 Connection con = null;
                 try {
                 	if (log.isInfoEnabled())
                 		log.info("Compacting database " + n);
                 	// PLUNDEN: Removing the context
                 	// con = DriverManager
                                     // .getConnection(EmbeddedHSQLDBServer.this.serverMode ? "jdbc:hsqldb:hsql://localhost/" + n
                                                     // : "jdbc:hsqldb:file:" + ContextHolder.getContext().getDBDirectory().getPath()
                                                                     // + "/" + n);
                 	con = DriverManager.getConnection(EmbeddedHSQLDBServer.this.serverMode ? "jdbc:hsqldb:hsql://localhost/" + n : "jdbc:hsqldb:file:" + new File((String)CoreServlet.getServlet().getServletContext().getAttribute("adito.directories.db")).getPath() + "/" + n);
                 	// end change
                     Statement s = con.createStatement();
                     s.execute("SHUTDOWN COMPACT");
                     if (log.isInfoEnabled())
                     	log.info("Database " + n + " compacted.");
                 } catch (Exception e) {
                     log.error("Failed to compact database.");
                 } finally {
                     if(con != null) {
                         try {
                             con.close();
                         }
                         catch(Exception e) {                            
                         }
                     }
                 }
 
             }
             server.signalCloseAllServerConnections();
             server.stop();
             waitForServerToStop();
             server = null;
             testedConnection = false;
         }
         started = false;
     }
 
     /**
      * Start the database engine.
      * 
      * @throws Exception
      */
     public void start() throws Exception {
         if (serverMode) {
             if (server == null) {
                 server = new Server();
                 server.setLogWriter(log.isDebugEnabled() ? new PrintWriter(new LoggingPrintWriter()) : new PrintWriter(
                                 new SinkPrintWiter()));
                 server.setNoSystemExit(true);
                 server.setProperties(properties);
             }
             if (server.getState() != ServerConstants.SERVER_STATE_SHUTDOWN) {
                 throw new Exception("Cannot start an HSQLDB server that is not shutdown.");
             }
             server.start();
         }
 
         waitForServer();
         started = true;
     }
 
     void waitForServer() {
         if (!testedConnection && serverMode) {
             Socket s = null;
             String addr = server.getAddress().equals("0.0.0.0") ? "127.0.0.1" : server.getAddress();
             if (log.isInfoEnabled())
             	log.info("Waiting for HSQLDB to start accepting connections on " + addr + ":" + server.getPort());
             for (int i = 0; i < 30; i++) {
                 try {
                     s = new Socket(addr, server.getPort());
                     break;
                 } catch (IOException ioe) {
                     try {
                         Thread.sleep(1000);
                     } catch (InterruptedException ie) {
                     }
                 }
             }
             if (s == null) {
                 throw new IllegalStateException("The HSQLDB server is not accepting connections after 30 seconds.");
             } else {
                 testedConnection = true;
                 if (log.isInfoEnabled())
                 	log.info("HSQLDB is now accepting connections.");
                 try {
                     s.close();
                 } catch (IOException ioe) {
                 }
             }
         }
     }
 
     void waitForServerToStop() {
         if (serverMode) {
             Socket s = null;
             String addr = server.getAddress().equals("0.0.0.0") ? "127.0.0.1" : server.getAddress();
             if (log.isInfoEnabled())
             	log.info("Waiting for HSQLDB to stop accepting connections on " + addr + ":" + server.getPort());
             int i = 0;
             for (; i < 30; i++) {
                 try {
                     s = new Socket(addr, server.getPort());
                     try {
                         s.close();
                     } catch (Exception e) {
                         e.printStackTrace();
                     }
                     s = null;
                     try {
                         Thread.sleep(1000);
                     } catch (InterruptedException ie) {
                     }
                 } catch (IOException ioe) {
                     break;
                 } finally {
                     if (s != null) {
                         try {
                             s.close();
                         } catch (Exception e) {
                         }
                         s = null;
                     }
                 }
             }
             if (i == 30) {
                 throw new IllegalStateException("The HSQLDB server has not stopped after 30 seconds.");
             } else {
             	if (log.isInfoEnabled())
             		log.info("HSQLDB is now stopped.");
             }
         }
     }
 
     /**
      * Add a new database to be maintained by this engine. If the database files
      * do not already exist they will be automatically created. If running in
      * TCP/IP server mode and the database has not been yet been started it will
      * be.
      * 
      * @param databaseName
      * @param file 
      * @throws Exception on any error
      */
     public void addDatabase(String databaseName, File file) throws Exception {
         if (!databases.contains(databaseName)) {
             if (serverMode) {
             	if (log.isInfoEnabled())
             		log.info("Adding database " + databaseName + " in TCP/IP server mode, so restarting database");
                 boolean wasStarted = started;
                 if (wasStarted) {
                     stop();
                 }
                 databases.add(databaseName);
                 dbIdx++;
                 properties.setProperty("server.database." + dbIdx, "file:" +file.getPath()+  "/" + databaseName);
                 properties.setProperty("server.dbname." + dbIdx, databaseName);
                 start();
             } else {
             	if (log.isInfoEnabled())
             		log.info("Adding database " + databaseName + " in embedded mode.");
                 databases.add(databaseName);
 
             }
         }
 
     }
 
     /*
      * Dummy {@link Write} to just sink log output.
      */
     class SinkPrintWiter extends Writer {
         public void close() throws IOException {
         }
 
         public void flush() throws IOException {
         }
 
         public void write(char[] cbuf, int off, int len) throws IOException {
         }
 
     }
 
     /*
      * {@link Writer} to just convert log output in Commons Loggin calls.
      */
     class LoggingPrintWriter extends Writer {
 
         private StringBuffer buffer = new StringBuffer();
         private char ch;
 
         /*
          * (non-Javadoc)
          * 
          * @see java.io.Writer#close()
          */
         public void close() throws IOException {
             // not implemented.
         }
 
         /*
          * (non-Javadoc)
          * 
          * @see java.io.Writer#flush()
          */
         public void flush() throws IOException {
         }
 
         /*
          * (non-Javadoc)
          * 
          * @see java.io.Writer#write(char[], int, int)
          */
         public synchronized void write(char[] cbuf, int off, int len) throws IOException {
             String s = new String(cbuf, off, len);
             for (int i = 0; i < len; i++) {
                 ch = s.charAt(i);
                 if (ch == '\n') {
                 	if (log.isInfoEnabled())
                 		log.info(buffer.toString());
                     buffer.setLength(0);
                 } else {
                     buffer.append(ch);
                 }
             }
         }
 
     }
 
}
