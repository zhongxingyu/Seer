 package flatsql.test;
 
 import flatsql.util.JdbcUtil;
 import org.testng.Assert;
 import org.testng.annotations.AfterClass;
 import org.testng.annotations.BeforeClass;
 
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 
 
 public abstract class MySqlBasedTest {
 
     protected Connection conn = null;
 
     @BeforeClass
     public void setup() {
         try {
             TestHelper.createTestDatabase();
             conn = TestHelper.getConnection();
         } catch (Exception ex) {
             ex.printStackTrace();
             Assert.fail("Unable to setup tests");
         }
     }
 
     @AfterClass
     public void tearDown() {
         if (conn != null) {
             try {
                 conn.close();
             } catch (Exception ex) {
                 ex.printStackTrace();
             }
         }
         try {
             TestHelper.dropTestDatabase();
         } catch (Exception ex) {
         }
     }
 
     /**
      * Assert that a table exists
      * @param tableName
      */
     protected void assertTableExists(String tableName) {
 
         try {
             PreparedStatement stmt = conn.prepareStatement("SHOW TABLES LIKE ?");
             stmt.setString(1, tableName);
 
             ResultSet rs = stmt.executeQuery();
             boolean exists = rs.next();
 
             JdbcUtil.close(rs, stmt);
 
             if (!exists) {
                 Assert.fail(String.format("Table '%s' does not exist", tableName));
             }
 
         } catch (SQLException ex) {
             ex.printStackTrace();
             Assert.fail();
         }
     }
 
     /**
      * Assert that a record exists in the table
      * @param tableName Table name
      * @param id Record ID
      * @param entityName Entity name
      */
     protected void assertRecordExists(String tableName, String id, String entityName) {
         String query = String.format(
         		"SELECT COUNT(id) FROM %s WHERE id=? and name=?", tableName);
         
         PreparedStatement stmt = null;
         ResultSet rs = null;
         
         
         	try {
 				stmt = conn.prepareStatement(query);
 				stmt.setString(1, id);
 	        	stmt.setString(2, entityName);
 			} catch (SQLException e) { 
 				e.printStackTrace();
 				Assert.fail(e.getMessage());
 			} finally {
 				JdbcUtil.close(rs, stmt);
 			}
         	
         
     }
 
     /**
      * Assert that an attribute exists
      * @param tableName The actual table name, for example UserAttr, UserLargeAttr
      * @param id Entity ID
      * @param key The key
      * @param value The value
      * @param meta The meta data
      */
     private void internalAssertAttrExists(String tableName, String id, String key, String value, String meta) {
         String query = String.format(
                 "SELECT COUNT(entity_id) FROM %s WHERE entity_id=? and attr_key=? and attr_value=? and attr_meta=?",
                 tableName);
 
         PreparedStatement stmt = null;
         ResultSet rs = null;
 
         try {
             stmt = conn.prepareStatement(query);
             stmt.setString(1, id);
             stmt.setString(2, key);
             stmt.setString(3, value);
             stmt.setString(4, meta);
 
             rs = stmt.executeQuery();
            if (!rs.next() || rs.getInt(1) != 1) {
                 Assert.fail("There must only be 1 row");
             }
 
 
         } catch (SQLException e) {
             e.printStackTrace();
             Assert.fail(e.getMessage());
         } finally {
             JdbcUtil.close(rs, stmt);
         }
     }
 
     /**
      * Assert that a normal attribute exists
      * @param tableName The (entity) table name (without the Attr part)
      * @param id Entity ID
      * @param key The key
      * @param value The value
      * @param meta The meta data
      */
     protected void assertAttrExists(String tableName, String id, String key, String value, String meta) {
         this.internalAssertAttrExists(tableName + "Attr", id, key, value, meta);
     }
 
     /**
      * Assert that a normal attribute exists
     * @param tableName The (entity) table name (without the LargeAttr part)
      * @param id Entity ID
      * @param key The key
      * @param value The value
      * @param meta The meta data
      */
     protected void assertLargeAttrExists(String tableName, String id, String key, String value, String meta) {
         this.internalAssertAttrExists(tableName + "LargeAttr", id, key, value, meta);
     }
 }
