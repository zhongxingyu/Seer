 /* OpenMark online assessment system
    Copyright (C) 2007 The Open University
 
    This program is free software; you can redistribute it and/or
    modify it under the terms of the GNU General Public License
    as published by the Free Software Foundation; either version 2
    of the License, or (at your option) any later version.
 
    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.
 
    You should have received a copy of the GNU General Public License
    along with this program; if not, write to the Free Software
    Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
  */
 package om.tnavigator.db;
 
 import java.sql.*;
 import java.util.*;
 
 import om.tnavigator.Log;
 
 /** Thread-safe database access code */
 public class DatabaseAccess
 {
 	/** JDBC URL for database */
 	private String sURL;
 
 	/** Maximum number of JDBC connections */
 	private int iMaxConnections=5;
 
 	/** How long an unused connection lurks around before expiring (1 hr) */
 	private final static int CONNECTIONEXPIRY=60 * 60 * 1000;
 
 	/** How often we check for expired connections (15 mins) */
 	private final static int CONNECTIONCHECKDELAY=15*60*1000;
 
 	/** Current available connections (List of ConnectionInfo) */
 	private LinkedList<ConnectionInfo> llAvailableConnections=new LinkedList<ConnectionInfo>();
 
 	/** Connections currently in-use */
 	private Set<ConnectionInfo> sInUseConnections=new HashSet<ConnectionInfo>();
 
 	/** If access has been closed */
 	private boolean bClosed=false;
 
 	/** Log for debug logging */
 	private Log l;
 
 	/** Information about one connection */
 	private static class ConnectionInfo
 	{
 		/** Actual connection */
 		Connection c;
 
 		/** Statement */
 		Statement s;
 
 		/** Last-used time */
 		long lLastUsed;
 	}
 
 	/**
 	 * A single SQL transaction that can contain multiple queries and updates.
 	 * These are <i>not</i> themselves thread-safe, a single transaction must be
 	 * used in a single thread only.
 	 */
 	public class Transaction
 	{
 		/** Connection this transaction is using */
 		private ConnectionInfo ci;
 
 		/** True if an error has occurred and we should rollback instead of committing */
 		private boolean bError=false;
 
 		/** Current result-set (if any) */
 		private ResultSet rsCurrent=null;
 
 		private long lTime=0L;
 
 		/**
 		 * Constructs internally.
 		 * @param ci Connection
 		 */
 		private Transaction(ConnectionInfo ci) { this.ci=ci; }
 
 		/**
 		 * Runs an SQL query and returns a result set.
 		 * <p>
 		 * You do not need to close the ResultSet. It is automatically closed
 		 * when finish() or any other Transaction call takes place.
 		 * @param sSQL SQL query to run
 		 * @return Results
 		 * @throws SQLException
 		 */
 		public ResultSet query(String sSQL) throws SQLException
 		{
 			if(ci==null) throw new SQLException("Already commited");
 			if(rsCurrent!=null)
 			{
 				rsCurrent.close();
 				rsCurrent=null;
 			}
 
 			try
 			{
 				logDebug("[Q] "+sSQL);
 				long lStart=System.currentTimeMillis();
 				rsCurrent=ci.s.executeQuery(sSQL);
 				long lElapsed=System.currentTimeMillis()-lStart;
 				lTime+=lElapsed;
 				logDebug("OK ("+lElapsed+"ms)");
 				return rsCurrent;
 			}
 			catch(SQLException se)
 			{
 				bError=true;
 				throw se;
 			}
 		}
 
 		/**
 		 * Runs an SQL update.
 		 * @param sSQL SQL update command
 		 * @return Number of rows
 		 * @throws SQLException
 		 */
 		public int update(String sSQL) throws SQLException
 		{
 			try
 			{
 				if(ci==null) throw new SQLException("Already commited");
 				if(rsCurrent!=null)
 				{
 					rsCurrent.close();
 					rsCurrent=null;
 				}
 				logDebug("[U] "+sSQL);
 				long lStart=System.currentTimeMillis();
 				int iRows=ci.s.executeUpdate(sSQL);
 				long lElapsed=System.currentTimeMillis()-lStart;
 				lTime+=lElapsed;
 				logDebug("OK ("+lElapsed+"ms)");
 				return iRows;
 			}
 			catch(SQLException se)
 			{
 				bError=true;
 				throw se;
 			}
 		}
 
 		/**
 		 * Must be called in try-finally, even if exceptions are thrown from other
 		 * methods. Can safely call it more than once.
 		 * @return Total time spent waiting inside all database calls
 		 */
 		public long finish()
 		{
 			if(ci!=null)
 			{
 				try
 				{
 					if(rsCurrent!=null)
 					{
 						rsCurrent.close();
 						rsCurrent=null;
 					}
 					long lStart=System.currentTimeMillis();
 					if(!bError)
 						ci.c.commit();
 					else
 					  ci.c.rollback();
 					long lElapsed=System.currentTimeMillis()-lStart;
 					lTime+=lElapsed;
 				}
 				catch(SQLException se)
 				{
 					logError("Failure to finish() [error="+bError+"]",se);
 				}
 				releaseConnection(ci,bError);
 				ci=null;
 			}
 			long lReturn=lTime;
 			lTime=0L;
 			return lReturn;
 		}
 
 		/**
 		 * Call optionally before calling finish(), if some other error (not an
 		 * exception from a Transaction method) occurred and you want to rollback.
 		 * <p>
 		 * finish() should <i>always</i> be called - you just call this first.
 		 */
 		public void rollback()
 		{
 			bError=true;
 		}
 	}
 
 	/**
 	 * Logs string, with exception.
 	 * @param s Message
 	 * @param t Exception
 	 */
 	private void logError(String s,Throwable t)
 	{
 		if(l!=null) l.logError("DatabaseAccess",s,t);
 	}
 	/**
 	 * Logs string to debug log.
 	 * @param s Message
 	 */
 	private void logDebug(String s)
 	{
 		if(l!=null) l.logDebug("DatabaseAccess",s,null);
 	}
 
 	/** @return Number of DB connections currently up */
 	private synchronized int getNumConnections()
 	{
 		return llAvailableConnections.size()+sInUseConnections.size();
 	}
 
 	/** Thread that gets rid of unused connections every now and then */
 	private class CheckThread extends Thread
 	{
 		@Override
 		public void run()
 		{
 			synchronized(DatabaseAccess.this)
 			{
 				while(!bClosed)
 				{
 					if(!llAvailableConnections.isEmpty())
 					{
 						// If least-recently-used connection hasn't been used for an hour, chuck it
 						ConnectionInfo ci=llAvailableConnections.getLast();
 						if(ci.lLastUsed + CONNECTIONEXPIRY < System.currentTimeMillis())
 						{
 							logDebug("Discarding unused DB connection " +
 								"(# before: "+getNumConnections()+")");
 							try
 							{
 								ci.s.close();
 								ci.c.close();
 							}
 							catch(SQLException e)
 							{
 								logError("Error closing DB connection",e);
 							}
 							llAvailableConnections.removeLast();
 						}
 					}
 
 					// Wait until it's time to check again
 					long lWakeTarget=System.currentTimeMillis()+CONNECTIONCHECKDELAY;
 					while(!bClosed && System.currentTimeMillis() < lWakeTarget)
 					{
 						try
 						{
 							DatabaseAccess.this.wait(CONNECTIONCHECKDELAY);
 						}
 						catch(InterruptedException e)
 						{
 						}
 					}
 				}
 			}
 		}
 	}
 
 	/**
 	 * Constructs access pool. Note that this starts a cleanup thread so you must
 	 * take care to always call close().
 	 * @param sURL JDBC URL of database including password etc.
 	 * @param l Log to use for debug and error logging (null if none required)
 	 */
 	public DatabaseAccess(String sURL,Log l)
 	{
 		this.sURL=sURL;
 		this.l=l;
 		(new CheckThread()).start();
 	}
 
 	/**
 	 * Closes access pool along with all database connections.
 	 */
 	public synchronized void close()
 	{
 		if(bClosed) return;
 
 		logDebug("Database connection closing ("+sInUseConnections.size()+" active)");
 
		for(ConnectionInfo ci : llAvailableConnections)
 		{
 			try
 			{
 				ci.s.close();
 				ci.c.close();
 			}
 			catch(SQLException e)
 			{
 				logError("Error closing DB connection",e);
 			}
 		}
 		llAvailableConnections=null;
		for(ConnectionInfo ci : sInUseConnections)
 		{
 			try
 			{
 				ci.s.close();
 				ci.c.close();
 			}
 			catch(SQLException e)
 			{
 			}
 		}
 		sInUseConnections=null;
 
 		bClosed=true;
 		notifyAll();
 	}
 
 	/**
 	 * Sets the number of JDBC connections to open (at maximum). Default is 5.
 	 * @param iMaxConnections Max. number of connections
 	 */
 	public void setMaxConnections(int iMaxConnections)
 	{
 		this.iMaxConnections=iMaxConnections;
 	}
 
 	/**
 	 * Creates a new DatabaseAccess.Transaction. You <i>must</i> call finish()
 	 * on this - immediately after this call returns, a try/finally block should
 	 * begin that calls finish() on the Transaction.
 	 * <p>
 	 * This call may block if no connection is available.
 	 * @return Transaction object to do stuff with
 	 * @throws SQLException If there's a problem creating a required database
 	 *   connection
 	 */
 	public Transaction newTransaction() throws SQLException
 	{
 		return new Transaction(getConnection());
 	}
 
 	/**
 	 * Obtains a new connection separate from the general pool. You <i>must</i>
 	 * close this connection when done with it (use try/finally).
 	 * @return New connection object
 	 * @throws SQLException If there's a problem creating the connection
 	 */
 	public Connection newUnpooledConnection() throws SQLException
 	{
 		Connection c = DriverManager.getConnection(sURL);
 		c.setAutoCommit(false);
 		return c;
 	}
 
 	/**
 	 * Obtain a database connection and lock it (must call releaseConnection).
 	 * @return Connection info
 	 * @throws SQLException Any problem getting connection
 	 */
 	private synchronized ConnectionInfo getConnection() throws SQLException
 	{
 		while(!bClosed)
 		{
 			// If a connection is available, use that
 			if(!llAvailableConnections.isEmpty())
 			{
 				ConnectionInfo ci=llAvailableConnections.removeFirst();
 				sInUseConnections.add(ci);
 				return ci;
 			}
 
 			// If we're allowed to create another connection, do so
 			if(sInUseConnections.size() < iMaxConnections)
 			{
 				logDebug("Creating new DB connection " +
 					"(# before: "+getNumConnections()+")");
 
 				ConnectionInfo ci=new ConnectionInfo();
 
 				ci.c = DriverManager.getConnection(sURL);
 				ci.c.setAutoCommit(false);
 				ci.s=ci.c.createStatement();
 
 				sInUseConnections.add(ci);
 				return ci;
 			}
 
 			// Otherwise connections must be maxed, so block
 			try
 			{
 				wait();
 			}
 			catch(InterruptedException e)
 			{
 				throw new SQLException("Interrupted while waiting for connection");
 			}
 		}
 
 		throw new SQLException("Database access closed while getting connection");
 	}
 
 	/**
 	 * Release a database connection obtained using getConnection().
 	 * @param ci Connection info
 	 * @param bDiscard If true, discards the connection rather than returning it
 	 *   to the pool
 	 */
 	private synchronized void releaseConnection(ConnectionInfo ci,boolean bDiscard)
 	{
 		if(bClosed) return;
 
 		// Remove from 'in use' and add to front of available pile
 		if(!sInUseConnections.remove(ci))
 			throw new Error("Tried to release connection that was not in use");
 		if(!bDiscard) // Don't add it back if discarding!
 			llAvailableConnections.addFirst(ci);
 
 		// Set last-used date
 		ci.lLastUsed=System.currentTimeMillis();
 
 		// Notify for anyone waiting (note: we should do this even if discarding)
 		notifyAll();
 	}
 
 	/** @return Number of connections currently up */
 	public synchronized int getConnectionCount()
 	{
 		return sInUseConnections.size() + llAvailableConnections.size();
 	}
 }
