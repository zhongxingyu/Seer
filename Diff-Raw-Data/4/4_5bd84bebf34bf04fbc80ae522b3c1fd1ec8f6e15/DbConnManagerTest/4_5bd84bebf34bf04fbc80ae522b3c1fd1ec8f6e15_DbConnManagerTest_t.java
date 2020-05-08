 package com.cffreedom.apps;
 
 import static org.junit.Assert.*;
 
 import org.junit.Before;
 import org.junit.Test;
 
 import com.cffreedom.beans.DbConn;
import com.cffreedom.beans.DbDriver;
 import com.cffreedom.beans.DbType;
 import com.cffreedom.utils.db.DbUtils;
 
 public class DbConnManagerTest 
 {
 	private static final String GOOD_KEY = "odbc";
 	private static final String GOOD_DB = "OdbcDb";
	private static final String GOOD_DRIVER = DbDriver.ODBC.value;
 	DbConnManager dbcm = null;
 	
 	@Before
 	public void setup()
 	{
 		try
 		{
 			String url = DbUtils.getUrl(DbType.ODBC, null, "OdbcDb");
 			dbcm = new DbConnManager(null);
 			DbConn dbconn = new DbConn(GOOD_DRIVER, url, DbType.ODBC, null, GOOD_DB, 0);
 			dbcm.addConnection(GOOD_KEY, dbconn);
 		}
 		catch (Exception e)
 		{
 			fail("Should not get an exception");
 		}
 	}
 	
 	@Test
 	public void testGoodValues()
 	{
 		DbConn actual = dbcm.getDbConn(GOOD_KEY);
 		assertNotNull(actual);
 		assertEquals(actual.getDriver(), GOOD_DRIVER);
 		assertEquals(actual.getDb(), GOOD_DB);
 	}
 	
 	@Test
 	public void testBadValues()
 	{
 		DbConn actual = dbcm.getDbConn("junk");
 		assertNull(actual);
 	}
 
 }
