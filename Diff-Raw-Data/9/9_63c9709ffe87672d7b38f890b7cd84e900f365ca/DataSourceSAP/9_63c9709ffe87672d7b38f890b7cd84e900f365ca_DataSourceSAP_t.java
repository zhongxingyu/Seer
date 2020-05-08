 package dsa.colourmylife.rest.util;
 
 import javax.naming.Context;
 import javax.naming.InitialContext;
 import javax.naming.NamingException;
 import javax.sql.DataSource;
 
 public class DataSourceSAP {
 	private static DataSourceSAP instance = null;
 	private DataSource dataSource = null;
 
 	private DataSourceSAP() {
 		try {
 			// Get DataSource
 			Context initContext = new InitialContext();
 			Context envContext = (Context) initContext.lookup("java:/comp/env");
			dataSource = (DataSource) envContext.lookup("jdbc/music");
 
 		} catch (NamingException e) {
 			e.printStackTrace();
 		}
 	}
 
 	public final static DataSourceSAP getInstance() {
 		if (instance == null)
 			instance = new DataSourceSAP();
 		return instance;
 	}
 
 	public DataSource getDataSource() {
 		return dataSource;
 	}
 }
