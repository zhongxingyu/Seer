 package at.bxm.dbtools.schemacopy;
 
 import org.junit.After;
 
 public class TestBase {
 
 	protected Database sourceDb;
 	protected Database targetDb;
 
 	/**
 	 * Make sure the next test uses an empty database
	 * FIXME if a test fails, @After is not executed (and then the follwing tests also fail)
 	 */
 	@After
 	public void cleanup() {
 		cleanup(sourceDb);
 		cleanup(targetDb);
 	}
 
 	private void cleanup(Database database) {
 		if (database != null) {
 			switch (database.getDialect()) {
 				case H2:
 					database.execute("drop all objects");
 					break;
 				case ORACLE:
 					Oracle.dropTable(database, Oracle.TABLE_NAME);
 					break;
 			}
 		}
 	}
 
 }
