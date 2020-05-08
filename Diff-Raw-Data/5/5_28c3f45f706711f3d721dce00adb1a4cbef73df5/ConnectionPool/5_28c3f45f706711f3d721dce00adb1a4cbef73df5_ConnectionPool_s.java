 /*
  * Copyright (c) 2003, Rafael Steil
  * All rights reserved.
 
  * Redistribution and use in source and binary forms, 
  * with or without modification, are permitted provided 
  * that the following conditions are met:
 
  * 1) Redistributions of source code must retain the above 
  * copyright notice, this list of conditions and the 
  * following  disclaimer.
  * 2)  Redistributions in binary form must reproduce the 
  * above copyright notice, this list of conditions and 
  * the following disclaimer in the documentation and/or 
  * other materials provided with the distribution.
  * 3) Neither the name of "Rafael Steil" nor 
  * the names of its contributors may be used to endorse 
  * or promote products derived from this software without 
  * specific prior written permission.
  * 
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT 
  * HOLDERS AND CONTRIBUTORS "AS IS" AND ANY 
  * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, 
  * BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF 
  * MERCHANTABILITY AND FITNESS FOR A PARTICULAR 
  * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL 
  * THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE 
  * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, 
  * EXEMPLARY, OR CONSEQUENTIAL DAMAGES 
  * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF 
  * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, 
  * OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER 
  * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER 
  * IN CONTRACT, STRICT LIABILITY, OR TORT 
  * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN 
  * ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF 
  * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE
  * 
  * This file creation date: Mar 3, 2003 / 14:43:35 AM
  * The JForum Project
  * http://www.jforum.net
  */
 
 package net.jforum;
 
 import java.io.IOException;
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.SQLException;
 import java.util.Date;
 import java.util.Iterator;
 import java.util.LinkedList;
 
 import net.jforum.util.preferences.SystemGlobals;
 
 /** 
  * Every query and connection come from here.
  * Before you can get some connection from the pool, you must
  * call init() to initialize the connections. After that, 
  * to get a conneciton, simple use
  *
  * <blockquote><pre>
  * 		Connection con = ConnectionPool.getPool().getConnection();
  * </pre></blockquote>
  *
  * The name of the query is associated with a Prepared Statement string, which is inside 
  * of a properties file, called [dbtype].sql, where the type is specified inside of 
  * database.properties.<br>
  * <br>
  * Also manages the connection to the database. The configuration is a config file which
  * is read at the first <code>init()</code> call. You must init it before using the pool.<p>
  *
  * <code>ConnectionPool</code> is for now a singleton.
  *
  * @author Paulo Silveira
  * @author Rafael Steil
 * @version $Id: ConnectionPool.java,v 1.6 2004/06/02 15:17:33 pieter2 Exp $
  * */
 
 public class ConnectionPool 
 {
 	private static ConnectionPool pool;
 	private static boolean isDatabaseUp;
 	
 	private int minConnections, maxConnections, timeout;
 	private String connectionString;
 
 	/**
 	* It is the connection pool
 	*/
 	private LinkedList connections = new LinkedList();
     
 	/**
 	 * It has all the connections, even the ones in use.
 	 * This way, the garbage collector does not get them.
 	 */
 	private LinkedList allConnections = new LinkedList();
 
 	// for sinalizing a release
 	private Object releaseSignal = new Object();
 	
 	private static final boolean debug = false;
 	
 	/**
 	 * Private constructor that loads the driver and set the configuration from
 	 * the properties file. It will also initialize the Database driver.
 	 * 
 	 * @param dbConfigFile The full path plus the filename to the file which contains database specifc parameters
 	 * @throws IOException
 	 * @throws Exception
 	*/
 	private ConnectionPool() throws IOException, SQLException
 	{
 		SystemGlobals.loadAdditionalDefaults(SystemGlobals.getValue("database.driver.config"));
 		String driver = SystemGlobals.getValue("database.connection.driver");
 		
 		try {
 			Class.forName(driver);
 			
 			this.minConnections = SystemGlobals.getIntValue("database.connection.pool.min");
 			this.maxConnections = SystemGlobals.getIntValue("database.connection.pool.max");
 			this.timeout = SystemGlobals.getIntValue("database.connection.pool.timeout");
 
 			this.connectionString = SystemGlobals.getValue("database.connection.string");
 			
			System.err.println("database.connection.string=" + this.connectionString);
			
 			if (debug) {
 				System.err.println("*********************************************");
 				System.err.println("******** STARTING CONNECTION POOL ***********");
 				System.err.println("*********************************************");
 				System.err.println("database.connection.driver = "+ driver);
 				System.err.println("minConnections = "+ this.minConnections);
 				System.err.println("maxConnections = "+ this.maxConnections);
 				System.err.println("timeout = "+ this.timeout);
 				System.err.println("*********************************************");
 			}
 
 			for (int i = 0; i < this.minConnections; i++) {
 				Connection conn = DriverManager.getConnection(this.connectionString);
 				this.connections.addLast(conn);
 				this.allConnections.add(conn);
 				
 				if (debug) {
 					Date now = new Date();
 					System.err.println(now.toString() + " opening connection "+ (i + 1));
 				}
 			}
 			
 			isDatabaseUp = true;
 		}
 		catch (ClassNotFoundException e) {
 			System.err.println("Ouch... Cannot find database driver: "+ driver);
 		}
     }
 
     /**
      * Inits ConnectionPool. 
      * If the pool was already initialized, this action will take no effect.
 	 *
 	 * @throws SQLException
 	 * @throws IOException
      */
     public static void init() throws SQLException, IOException
     {
         if (pool == null || !isDatabaseUp) {
             pool = new ConnectionPool();
         }
     }
     
     public static boolean isDatabaseUp()
     {
     	return isDatabaseUp;
     }
     
 	/**
 	 *  Gets the ConnectionPool singleton instance. 
 	 * 
 	 * @return <code>ConnectionPool</code> object
 	 * @throws java.sql.SQLException
 	 **/
 	public static ConnectionPool getPool() throws SQLException
 	{
 		if (pool == null) {
 			throw new SQLException("ConnectionPool was not initialized yet. You need to call init() first.");
 		}
 		
 		return pool;
 	}
 
 	/**
 	 * Gets a connection to the database.<p> 
 	 * 
 	 * So you need to release it, after use. It will not be a huge problem if you do not
 	 * release it, but this way you will get a better performance.<p>
 	 * Thread safe.
 	 *
 	 * @return <code>Connection</code> object
 	 * @throws java.sql.SQLException	 
 	 */
 	public synchronized Connection getConnection() throws SQLException 
 	{
 		Connection conn = null;
 
 		// if there is enought Connections
 		if (this.connections.size() != 0) {
 			synchronized (this.connections) {
 				conn = (Connection) this.connections.removeFirst();
 			}
 
 			// take a look if the connection has died!
 			try {
 				if (conn.isClosed()) {
 					synchronized (this.allConnections) {
 						this.allConnections.remove(conn);
 						conn = DriverManager.getConnection(this.connectionString);
 						this.allConnections.add(conn);
 					}
 				}
 			}
 			catch (SQLException e) {
 				if (debug) {
 					System.err.println("Cannot reconnect a closed connection:" + this.connectionString + e);
 				}
 				
 				throw e;
 			}
 
 			return conn;
 		}
         // Otherwise, create a new one if the Pool is now full
 		else {
 			if (this.allConnections.size() < this.maxConnections) {
 				try {
 					conn = DriverManager.getConnection(this.connectionString);
 				}
 				catch (SQLException e) {
 					if (debug) {
 						System.err.println("Cannot stabilish a NEW connection to the database:" + this.connectionString + e);
 					}
 					
                     throw e;
 				}
 				
 				// registering the new connection
 				synchronized (this.allConnections) {
 					this.allConnections.add(conn);
 				}
 				
 				return conn;
 			}
 		}
 
         /*
          * Trying to get some Connections stuck inside some Queries.
          * The Query.finalize method will release them.
          * We need to wait sometime, so the GC will get the Connections for us
          */
 		System.gc();
 
 		synchronized (this.releaseSignal) {
 			/*
 			 * Not inside a while, since we are giving it a maximum timeout, 
 			 * and this method is already SYNC, there is no way that we will loose
 			 * the state if we receive a signal
 			 */
 			if (this.connections.size() == 0) {
 				try {
 					this.releaseSignal.wait(this.timeout);
 				}
 				catch (InterruptedException e) {
 					if (debug)
 						System.err.println("Problems while waiting for connection. "+ e);
 				}
 			}
 
 			if (this.connections.size() == 0) {
 				// TIMED OUT!!!!
 				if (debug) {
 					System.err.println( "Pool is empty, and th waiting for one timed out!"
 						+ "If this is happening too much, your code is probably not releasing the Connections."
 						+ "If you cant solve this, set your 'database.connection.pool.timeout' to a bigger number.");
 				}
 			}
 			else {
 				synchronized (this.connections) {
 					conn = (Connection) this.connections.removeFirst();
 				}
 				
 				return conn;
 			}
 		}
 		
 		return conn;
 	}
 
 	/**
 	 * Releases a connection, making it available to the pool once more.
 	 *
 	 * @param conn <code>Connection</code> object to release
 	 * @throws java.sql.SQLException
 	 */
 	public void releaseConnection(Connection conn) throws SQLException 
 	{
 		if (conn == null) {
 			if (debug)
 			System.err.println("Cannot release a NULL connection!");
 				
 			return;
 		}
 
 		/*
 		 * Sync because collection.contains() uses the fail fast iterator!
 		 */
 		synchronized (this.allConnections) {
 			if (!this.allConnections.contains(conn) && debug) {
 				System.err.println("Cannot release a connection that is not from this pool!");
 				
 				return;
 			}
 			
 			try {
 				if (conn.isClosed()) {
 					this.allConnections.remove(conn);
 					
 					return;
 				}
 			}
 			catch (SQLException e) {
 				if (debug) {
 					System.err.println("Cannot get info about the conn: "+ e);
 				}
 			}
 		}
 		
 		synchronized (this.releaseSignal) {
 			synchronized (this.connections) {
 				this.connections.addLast(conn);
 			}
 			
 			this.releaseSignal.notify();
 		}
 		
 		if (debug) {
 			System.err.println("Releasing connection...");
 		}
 	}
 
 	/**
 	 * Returns the status
 	 * 
 	 * @return The status
 	 */
 	public synchronized String getStatus() 
 	{
 		StringBuffer status = new StringBuffer();
 		int i = 0;
 
 		Iterator it = this.allConnections.iterator();
 		while (it.hasNext()) {
 			i++;
 			status.append("Connection " + i + ": ");
 
 			Connection c = (Connection) it.next();
 			if (c != null) {
 				try {
                     status.append(c + " closed: " + c.isClosed());
 				}
 				catch (SQLException e) {
                     status.append(e);
 				}
 			}
 			else {
 				status.append("NULL!!!");
 			}
 
 			status.append("\n");
 		}
 
 		status.append("\nPOOL:\n\n");
 		i = 0;
 
 		it = this.connections.iterator();
 		while (it.hasNext()) {
 			i++;
 			status.append("Connection " + i + ": ");
 
 			Connection c = (Connection) it.next();
 			if (c != null) {
 				try {
 					status.append(c + " closed: " + c.isClosed());
 				}
 				catch (SQLException e) {
 					status.append(e);
 				}
 			}
 			else
 				status.append("NULL!!!");
 
 			status.append("\n");
 		}
         
 		return status.toString();
 	}
 }
