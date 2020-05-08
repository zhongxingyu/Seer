 package com.camilolopes.readerweb.dbunit;
 
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 import org.springframework.test.context.transaction.TransactionConfiguration;
 import org.springframework.transaction.annotation.Transactional;
 @RunWith(SpringJUnit4ClassRunner.class)
 @ContextConfiguration(locations={"classpath:**/OrderPersistenceTests-context.xml"})
 @TransactionConfiguration(defaultRollback=true,transactionManager="transactionManager")
 @Transactional
 public class DBUnitConfigurationTest extends DBUnitConfiguration{
 	
 	@Before
 	public void setUp() throws Exception {
 		getSetUpOperation();
 	}
 
 	@Test
 	public void testConectionDataSet() throws Exception {
 		assertNotNull(getDataSet());
 		int rowCount = getDataSet().getTable("user").getRowCount();
		int atLeastRecord = 1;
		assertTrue(rowCount>= atLeastRecord);
 	}
 
 
 }
