 package ch.inftec.ju.testing.db.data;
 
 import ch.inftec.ju.db.JuDbException;
 import ch.inftec.ju.testing.db.data.TestDbUtils.AbstractTestDb;
 
 /**
  * Test database using an in-memory derby DB.
  * @author tgdmemae
  *
  */
 class DerbyTestDb extends AbstractTestDb {
 	@Override
 	protected void createTables() throws JuDbException {
 		this.jdbcTemplate.update("CREATE TABLE TEST_A ("
 				+ "  Aid INTEGER not null primary key,"
 				+ "  text VARCHAR(64),"
 				+ "  b_fk INTEGER"
 				+ ")");
 		
 		this.jdbcTemplate.update("CREATE TABLE TEST_B ("
 				+ "  Bid INTEGER not null primary key,"
 				+ "  text VARCHAR(64)"
 				+ ")");
 		
 		this.jdbcTemplate.update("CREATE TABLE TEST_C ("
 				+ "  Cid INTEGER not null primary key,"
 				+ "  text VARCHAR(64),"
 				+ "  a_fk INTEGER,"
 				+ "  d_fk INTEGER"
 				+ ")");
 		
 		this.jdbcTemplate.update("CREATE TABLE TEST_D ("
 				+ "  Did INTEGER not null primary key,"
 				+ "  text VARCHAR(64)"
 				+ ")");
 		
 		this.jdbcTemplate.update("CREATE TABLE TEST_DATATYPES ("
 				+ "  id INTEGER not null primary key,"
 				+ "  integerNumber INTEGER,"
 				+ "  varcharText VARCHAR(64),"
 				+ "  clobText CLOB,"
 				+ "  dateField DATE"
 				+ ")");
 	}
 
 	@Override
 	protected void cleanup() throws JuDbException {
 		this.jdbcTemplate.update("DROP TABLE TEST_DATATYPES");
 		
 		this.jdbcTemplate.update("DROP TABLE TEST_A");
 	}
 	
 	@Override
 	protected void resetPlatformSpecificData() throws JuDbException {
//						
//			this.jdbcTemplate.update("DELETE FROM TEST_DATATYPES");
//			this.jdbcTemplate.update("INSERT INTO TEST_DATATYPES (ID, INTEGERNUMBER, VARCHARTEXT, CLOBTEXT, DATEFIELD) VALUES (1, 1, 'one', 'oneClob', '1980-12-03')");
//			this.jdbcTemplate.update("INSERT INTO TEST_DATATYPES (ID, INTEGERNUMBER, VARCHARTEXT, CLOBTEXT, DATEFIELD) VALUES (2, null, null, null, null)");

		// Reset sequence to guarantee predictable primary key values
		this.jdbcTemplate.update("UPDATE SEQUENCE SET SEQ_COUNT=? WHERE SEQ_NAME=?", 9, "SEQ_GEN");
 	}
 }
