 package com.cffreedom.utils.db;
 
 
 import java.sql.*;
 import java.util.*;
 
 import com.cffreedom.utils.LoggerUtil;
 
 /**
  * @author markjacobsen.net (http://mjg2.net/code)
  * Copyright: Communication Freedom, LLC - http://www.communicationfreedom.com
  * 
  * Free to use, modify, redistribute.  Must keep full class header including 
  * copyright and note your modifications.
  * 
  * If this helped you out or saved you time, please consider...
  * 1) Donating: http://www.communicationfreedom.com/go/donate/
  * 2) Shoutout on twitter: @MarkJacobsen or @cffreedom
  * 3) Linking to: http://visit.markjacobsen.net
  */
 class ConnectionReaper extends Thread
 {
 	private final LoggerUtil logger = new LoggerUtil(LoggerUtil.FAMILY_UTIL, this.getClass().getPackage().getName() + "." + this.getClass().getSimpleName());
 		
     private ConnectionPool 	pool;
     private final long 		delaySeconds = 2*60;
     private boolean 		shutdown = false;
 
     ConnectionReaper(ConnectionPool pool)
     {
         this.pool=pool;
     }
 
     public void run()
     {
     	final String METHOD = "run";
     	
         while(this.shutdown == false)
         {
            try
            {
               sleep(this.delaySeconds * 1000);
            }
            catch( InterruptedException e) { }
            
            logger.logDebug(METHOD, "Calling reapConnections");
            this.pool.reapConnections();
         }
         
         logger.logDebug(METHOD, "Exiting");
     }
     
     protected void shutdown()
     {
     	this.shutdown = true;
     	this.interrupt();
     }
 }
 
 public class ConnectionPool
 {
 	private final LoggerUtil logger = new LoggerUtil(LoggerUtil.FAMILY_UTIL, this.getClass().getPackage().getName() + "." + this.getClass().getSimpleName());
 	
 	private String 					poolName;
 	private Vector<DbConnection> 	connections;
 	private String 					driver;
 	private String 					url;
 	private String 					user;
 	private String 					password;
 	final private long 				secondsUntilStale = 60;
 	private ConnectionReaper 		reaper;
 	final private int 				poolsize = 10;
 
 	public ConnectionPool(String poolName, String driver, String url, String user, String password)
 	{
 		this.poolName = poolName;
 		this.driver = driver;
 		this.url = url;
 		this.user = user;
 		this.password = password;
 		this.connections = new Vector<DbConnection>(poolsize);
 		logger.logDebug("constructor", "Creating pool " + this.getDetailName());
 		
 		this.reaper = new ConnectionReaper(this);
 		this.reaper.start();
 	}
 
 	public synchronized void reapConnections()
 	{
 		final String METHOD = "reapConnections";
 		
 		long stale = System.currentTimeMillis() - (secondsUntilStale * 1000);
 		
 		if ( (this.connections != null) && (this.connections.size() > 0) )
 		{
 			Enumeration<DbConnection> connlist = this.connections.elements();
 	    
 			while((connlist != null) && (connlist.hasMoreElements()))
 			{
 				DbConnection conn = (DbConnection)connlist.nextElement();
 	
 				//logger.logDebug(METHOD, "In Use: " + conn.inUse());
 				//logger.logDebug(METHOD, "Last Use: " + conn.getLastUse());
 				//logger.logDebug(METHOD, "Stale: " + stale);
 				
 				if  (
 					(conn.inUse() == false) && 
 					(stale > conn.getLastUse())
 	        		)
 				{
 					//logger.logDebug(METHOD, "Removing stale connection");
 					removeConnection(conn);
 				}
 			}
 			
 			//logger.logDebug(METHOD, "Current connections: " + this.connections.size());
 		}
 		
 		if ((this.connections == null) || (this.connections.size() == 0))
 		{
 			logger.logInfo(METHOD, "No connections to reap. Closing pool: " + this.getPoolName());
 			this.close();
 		}
 	}
 
 	public synchronized void close()
 	{        
 		final String METHOD = "close";
 		
 		try
 		{
 			if ( (this.connections != null) && (this.connections.size() > 0) )
 			{
 				logger.logDebug(METHOD, "Closing all connections in pool: " + this.getPoolName());
 				Enumeration<DbConnection> connlist = this.connections.elements();
 		
 				while((connlist != null) && (connlist.hasMoreElements()))
 				{
 					DbConnection conn = (DbConnection)connlist.nextElement();
 					removeConnection(conn);
 				}
 			}
 			else
 			{
 				logger.logDebug(METHOD, "No connections to close in pool: " + this.getPoolName());
 			}
 		}
 		catch (Exception e)
 		{
 			logger.logError(METHOD, e.getMessage(), e);
 		}
 		finally
 		{
 			try { this.reaper.shutdown(); } catch (Exception e) {}
 			try { this.connections = null; } catch (Exception e) {}
 		}
 	}
 
 	private synchronized void removeConnection(DbConnection conn) {
 		this.connections.removeElement(conn);
 	}
 
 
 	public synchronized DbConnection getConnection() throws SQLException, ClassNotFoundException
 	{
 		final String METHOD = "getConnection";
 		DbConnection c;
 		
 		logger.logDebug(METHOD, "Getting connection from pool: " + this.getPoolName());
		
		if (this.connections == null)
		{
			logger.logDebug(METHOD, "Reinitializing pool connections: " + this.getPoolName());
			this.connections = new Vector<DbConnection>(poolsize);
		}
		
 		for(int i = 0; i < this.connections.size(); i++)
 		{
 			c = (DbConnection)this.connections.elementAt(i);
 			if (c.lease() == true)
 			{
 				logger.logDebug(METHOD, "Returning cached Connection from pool: " + this.getPoolName());
 				return c;
 			}
 		}
 
 		// A connection was not obtained from the pool so create a new one
 		logger.logDebug(METHOD, "Creating new connection in pool: " + this.getPoolName());
 		Class.forName(this.driver);
 		Connection conn = DriverManager.getConnection(this.url, this.user, this.password);
 		c = new DbConnection(conn, this);
 		logger.logDebug(METHOD, "New Connection created in pool: " + this.getPoolName());
 		c.lease();
 		this.connections.addElement(c);
 		logger.logDebug(METHOD, "Returning new Connection from pool: " + this.getPoolName());
 		return c;
 	} 
 
    public synchronized void returnConnection(DbConnection conn) {
 	   conn.expireLease();
    }
    
    public String getPoolName()
    {
 	   return this.poolName;
    }
    
    private String getDetailName()
    {
 	   return "** " + this.getPoolName() + " ** " + this.driver + " ** " + this.url + " ** " + this.user + " **";
    }
 }
