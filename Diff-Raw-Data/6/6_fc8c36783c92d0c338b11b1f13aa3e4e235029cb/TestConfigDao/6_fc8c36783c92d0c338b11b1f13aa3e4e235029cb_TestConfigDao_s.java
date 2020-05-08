 package com.cbt.ws.dao;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import static com.cbt.ws.jooq.tables.Testconfig.TESTCONFIG;
 import org.apache.log4j.Logger;
 import org.jooq.Record;
 import org.jooq.Result;
 import org.jooq.SQLDialect;
 import org.jooq.impl.Executor;
 
 import com.cbt.ws.entity.TestConfig;
 import com.cbt.ws.mysql.Db;
 
 /**
  * Test configuration DAO
  * 
  * @author SauliusALisauskas 2013-03-03 Initial version
  *
  */
 public class TestConfigDao {
 
 	private final Logger mLogger = Logger.getLogger(TestConfigDao.class);
 
 	/**
 	 * Get test configurations
 	 * 
 	 * @return
 	 */
 	public TestConfig[] getAll() {
 		List<TestConfig> testExecutions = new ArrayList<TestConfig>();
 		Executor sqexec = new Executor(Db.getConnection(), SQLDialect.MYSQL);
 		Result<Record> result = sqexec.select().from(TESTCONFIG).fetch();
 		for (Record r : result) {
 			TestConfig tc = new TestConfig();
 			tc.setId(r.getValue(TESTCONFIG.TESTCONFIG_ID));
 			testExecutions.add(tc);
 			mLogger.debug(tc);
 		}
 		return testExecutions.toArray(new TestConfig[testExecutions.size()]);
 	}
 
 	/**
 	 * Add new test configuration
 	 * 
 	 * @param userid
 	 * @return
 	 */
 	public Long add(TestConfig testConfig) {
 		Executor sqexec = new Executor(Db.getConnection(), SQLDialect.MYSQL);
 		mLogger.trace("Adding new test configuration");
 		Long testConfigID = sqexec
 				.insertInto(TESTCONFIG, 
 						TESTCONFIG.USER_ID, 
 						TESTCONFIG.TESTPACKAGE_ID, 
 						TESTCONFIG.TESTTARGET_ID,
 						TESTCONFIG.TESTPROFILE_ID, 
 						TESTCONFIG.METADATA)
 				.values(testConfig.getUserId(), 
 						testConfig.getTestPackageId(), 
 						testConfig.getTestTargetId(),
 						testConfig.getTestProfileId(), 
 						testConfig.getMetadata())
 				.returning(TESTCONFIG.TESTCONFIG_ID).fetchOne().getTestconfigId();
 		mLogger.trace("Added test configuration, enw id:" + testConfigID);		
 		return testConfigID;
 	}
 }
