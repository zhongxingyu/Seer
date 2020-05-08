 
 package edu.wustl.cab2b.server.util;
 
 import java.sql.Connection;
 import java.sql.SQLException;
 
 import javax.naming.Context;
 import javax.naming.InitialContext;
 import javax.naming.NamingException;
 import javax.sql.DataSource;
 
 import edu.wustl.cab2b.common.errorcodes.ErrorCodeConstants;
 import edu.wustl.cab2b.common.exception.RuntimeException;
 
 /**
  * Utility which generates and closes connection.
  * @author Chandrakant Talele
  */
 public class ConnectionUtil
 {
 
 	/**
 	 * Creates a connection from data-source and returns it.
 	 * @return Returns the Connection object
 	 * @throws SQLException 
 	 */
 	public static Connection getConnection()
 	{
 		DataSource dataSource = null;
 		String dsName = new StringBuilder().append("java:/").append(
 				ServerProperties.getDatasourceName()).toString();
 		try
 		{
 			Context ctx = new InitialContext();
 
 			dataSource = (DataSource) ctx.lookup(dsName);
 		}
 		catch (NamingException e)
 		{
 			throw new RuntimeException("Unable to look up Datasource from JNDI", e,
 					ErrorCodeConstants.JN_0001);
 		}
 
 		Connection connection = null;
 		if (dataSource != null)
 		{
 			try
 			{
 				connection = dataSource.getConnection();
 			}
 			catch (SQLException e)
 			{
 				throw new RuntimeException("Unable to create a connection from datasource.", e,
 						ErrorCodeConstants.DB_0002);
 			}
 		}
 		else
 		{
 			throw new RuntimeException("Datasource lookup failed, got null datasource",
 					new RuntimeException(), ErrorCodeConstants.JN_0001);
 		}
 		return connection;
 	}
 
 	/**
 	 * Closes the connection.
 	 * @param connection to be closed.
 	 */
 	public static void close(Connection connection)
 	{
 
 		if (connection != null)
 		{
 			try
 			{
 				connection.close();
 			}
 			catch (SQLException e)
 			{
 				//DO NOTHING
				e.printStackTrace();
 			}
 		}
 	}
 }
