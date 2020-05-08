 package net.todd.biblestudy;
 
 import static org.junit.Assert.assertEquals;
 
 import java.util.List;
 
 import org.apache.commons.io.IOUtils;
 import org.junit.Test;
 
 public class SqlBatchCreatorTest {
 	@Test
 	public void testCreationOfBatchSqlScripts() throws Exception {
 		String content = IOUtils.toString(getClass().getResourceAsStream(
				"/db.script"));
 		SqlBatchCreator sqlBatchCreator = new SqlBatchCreator();
 		List<String> createBatchQueries = sqlBatchCreator
 				.createBatchQueries(content);
 		assertEquals(3, createBatchQueries.size());
 		assertEquals("CREATE SCHEMA PUBLIC AUTHORIZATION DBA",
 				createBatchQueries.get(0));
 		assertEquals(
 				"CREATE MEMORY TABLE BIBLE (BIB_ID INT NOT NULL, BIB_VERSION VARCHAR(10) NOT NULL, BIB_BOOK VARCHAR(30) NOT NULL, BIB_CHAPTER INT NOT NULL, BIB_VERSE INT NOT NULL, BIB_TEXT VARCHAR(4000) NOT NULL, BIB_SEQUENCE_ID INT NOT NULL)",
 				createBatchQueries.get(1));
 		assertEquals(
 				"CREATE USER SA PASSWORD \"\" GRANT DBA TO SA SET WRITE_DELAY 20 SET SCHEMA PUBLIC",
 				createBatchQueries.get(2));
 	}
 }
