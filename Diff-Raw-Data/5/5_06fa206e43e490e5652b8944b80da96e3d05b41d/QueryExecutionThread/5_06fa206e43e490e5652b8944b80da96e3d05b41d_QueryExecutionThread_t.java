 
 package edu.wustl.query.queryexecutionmanager;
 
 import java.sql.ResultSet;
 import java.sql.SQLException;
 
 import edu.wustl.common.query.AbstractQuery;
 import edu.wustl.common.query.itablemanager.ITableManager;
 import edu.wustl.common.util.dbManager.DAOException;
 
 /**
  * This class represents the Thread to execute the query
  * This class is responsible for
  *   a.   Execute the query, read one record at time from the result set, 
  *   	  update the UPI cache and populate the iTable
  *   b.   Provide an interface to kill the thread which will cleanup the JDBC ResultSet and the UPI cache
  * 
  * @author ravindra_jain
  * @version 1.0
  * @since January 5, 2009
  */
 
 public class QueryExecutionThread implements Runnable
 {
 
	protected AbstractQuery abstractQueryObj;
 	// JDBC result set for this query
	protected ResultSet results;
 
 	/**
 	 * External Condition which controls cancel
 	 * operation of a Thread
 	 */
 	protected boolean cancelThread = false;
 
 	/**
 	 * PARAMETERIZED CONSTRUCTOR
 	 * @param abstractQueryObj 
 	 */
 	QueryExecutionThread(AbstractQuery abstractQueryObj)
 	{
 		this.abstractQueryObj = abstractQueryObj;
 	}
 
 	/**
 	 * Logic to calculate execution Id for each query
 	 * @param abstractQueryObj 
 	 * @return Query Execution Id
 	 * @throws DAOException 
 	 * @throws SQLException 
 	 */
 	public static int getQueryExecutionId(AbstractQuery abstractQueryObj) throws DAOException,
 			SQLException
 	{
 		ITableManager manager = ITableManager.getInstance();
 
 		return manager.insertNewQuery(-1L, abstractQueryObj.getUserId(), abstractQueryObj
 				.getQuery().getId());
 	}
 
 	/**
 	 * Thread's RUN method
 	 */
 	public void run()
 	{
 
 	}
 
 	/**
 	 *  To ABORT / CANCEL the THREAD (Query)
 	 */
 	public void cancel()
 	{
 
 	}
 
 }
