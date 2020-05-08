 package net.maltera.daranable.edinet;
 
 import static org.junit.Assert.*;
 
 import java.sql.ResultSet;
 import java.sql.SQLException;
 
 import net.maltera.daranable.edinet.model.Database;
 
 import org.junit.AfterClass;
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Test;
 
 public class TestDatabaseInitialization {
 	
 	private static DatabaseHelper dbHelper;
 	
 	@BeforeClass
 	public static void setupClass() {
 		dbHelper = new DatabaseHelper();
 	}
 	
 	@Before
 	public void setup() {
 		dbHelper.prepare();
 	}
 	
 	@AfterClass
 	public static void cleanupClass() {
 		dbHelper.cleanup();
 	}
 	
 	/** Tests creation of the database when the file does not exist.
 	 * 
 	 */
 	@Test
 	public void testCreate()
 	throws Exception {
 		Database database = new Database();
 		database.shutdown();
 	}
 	
 	@Test
 	public void testPersistence()
 	throws Exception {
 		Database database = new Database();
 		
 		database.getConnection().createStatement().execute(
 				"INSERT INTO properties (name, value) VALUES ('test', 'foo')" );
 		
 		database.shutdown();
 		database = new Database();

 		ResultSet result =
 				database.getConnection().createStatement().executeQuery(
 						"SELECT value FROM properties WHERE name='test'" );
		if (!result.next()) fail( "no results returned" );
 		assertEquals( "saved value", "foo", result.getString( "value" ) );
		
		database.shutdown();
 	}
 }
