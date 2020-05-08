 package at.bxm.dbtools.schemacopy.table;
 
 import static at.bxm.dbtools.schemacopy.Oracle.*;
 import static org.junit.Assert.*;
 
 import at.bxm.dbtools.schemacopy.DatabaseUtils;
 import at.bxm.dbtools.schemacopy.TestBase;
 import org.junit.Test;
 
 /** Test the {@link TableCopier} class for an Oracle database */
 public class TableCopierOracleTest extends TestBase {
 
 	//	create user targettest identified by test;
 	@Test
 	public void copy_create() {
 		// GIVEN: a non-empty source table with LOBs and an empty target database
 		final int datasets = 111;
 		sourceDb = createTableWithData(USERNAME_SOURCE, datasets);
 		targetDb = connect(USERNAME_TARGET);
 
 		// WHEN: copying
 		TableCopier tc = new TableCopier(sourceDb, targetDb);
 		tc.copy(TABLE_NAME, null, null, null, CopyTargetMode.CREATE);
 
 		// THEN: target table contains equal dataset count
 		assertEquals(datasets, targetDb.queryForLong(TABLE_COUNTQUERY));
 	}
 
 	@Test
 	public void copy_reuse() {
 		// GIVEN: a non-empty source table with LOBs and an empty target table
 		final int datasets = 111;
 		sourceDb = createTableWithData(USERNAME_SOURCE, datasets);
 		targetDb = createTable(USERNAME_TARGET);
 
 		// WHEN: copying
 		TableCopier tc = new TableCopier(sourceDb, targetDb);
 		tc.copy(TABLE_NAME, null, null, null, CopyTargetMode.REUSE);
 
 		// THEN: target table contains equal dataset count
 		assertEquals(datasets, targetDb.queryForLong(TABLE_COUNTQUERY));
 	}
 
 	@Test
 	public void copy_withQuery() {
 		// GIVEN: a non-empty source table and an empty target database
 		final int datasets = 210;
 		sourceDb = createTableWithData(USERNAME_SOURCE, datasets);
		targetDb = connect(USERNAME_TARGET);
 
 		// WHEN: copying only selected rows and columns
 		final int datasetsToCopy = 14;
 		TableCopier tc = new TableCopier(sourceDb, targetDb);
 		int datasetsCopied = tc.copyFromQuery("select c_decimal, c_date from " + TABLE_NAME + " where c_int<="
 			+ datasetsToCopy, TABLE_NAME, null, CopyTargetMode.CREATE);
 
 		// THEN: target table contains only the selected rows and columns
 		assertEquals(datasetsToCopy, datasetsCopied);
 		assertEquals(datasetsToCopy, targetDb.queryForLong(TABLE_COUNTQUERY));
 		assertEquals(2, DatabaseUtils.getColumnCount(targetDb, TABLE_NAME));
 	}
 
 }
