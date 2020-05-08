 package org.mdissjava.commonutils.sql.db;
 
 import java.sql.SQLException;
 
 import org.junit.Test;
 import org.mdissjava.commonutils.sql.db.SqlDBConnection;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public class SqlDBConnectionTest {
 	
 	private SqlDBConnection msqldbc;
 	final Logger logger = LoggerFactory.getLogger(this.getClass());
 	
 	final String DRIVER = "com.mysql.jdbc.Driver";
 	final String CONSTR = "jdbc:mysql://localhost/test?user=test&password=test";
 	
 	@Test
 	public void testConnection() throws SQLException, ClassNotFoundException 
 	{
 		this.logger.info("TEST(SQLDB) testConnection");
 		
 		this.msqldbc = SqlDBConnection.getInstance();
 		this.msqldbc.connect(DRIVER,CONSTR);
 	}
 	
 	@Test(expected=SQLException.class)
 	public void testConnectionWrongConnString() throws SQLException, ClassNotFoundException
 	{
 		this.logger.info("TEST(SQLDB) testConnectionWrongConnString");
 		
 		this.msqldbc = SqlDBConnection.getInstance();
 		this.msqldbc.connect(DRIVER, "jdbc:mysql://localhost/test?user=fake&password=fake");
 		
 	}
 
 	@Test(expected=ClassNotFoundException.class)
 	public void testConnectionWrongDriver() throws SQLException, ClassNotFoundException
 	{
 		this.logger.info("TEST(SQLDB) testConnectionWrongDriver");
 		
 		this.msqldbc = SqlDBConnection.getInstance();
 		this.msqldbc.connect("com.mysql.jdbc.FakeDriver", CONSTR);
 		
 	}
 }
