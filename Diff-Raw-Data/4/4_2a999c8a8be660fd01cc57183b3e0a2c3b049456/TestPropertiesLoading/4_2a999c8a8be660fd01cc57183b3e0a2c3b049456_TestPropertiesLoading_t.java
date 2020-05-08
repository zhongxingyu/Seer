 package org.wwald;
 
 import java.io.InputStream;
 import java.util.Properties;
 
 import junit.framework.Assert;
 
 import org.junit.Test;
 import org.wwald.model.ConnectionPool;
 
 public class TestPropertiesLoading {
 	
 	@Test
 	public void testLoadingDBProperties() throws Exception {
 		InputStream propStream = 
 			ConnectionPool.class.
 				getClassLoader().
					getResourceAsStream("db.properties");
 		Properties dbProps = new Properties();
 		dbProps.load(propStream);
 		Assert.assertNotNull(dbProps.getProperty("db.url"));
 		Assert.assertNotNull(dbProps.getProperty("db.user"));
 		Assert.assertNotNull(dbProps.getProperty("db.password"));
 	}
 }
