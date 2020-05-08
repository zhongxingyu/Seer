 package edu.wustl.cab2b.server.ejb.sqlquery;
 
 import java.rmi.RemoteException;
 import java.sql.SQLException;
 
 import junit.framework.TestCase;
 import edu.wustl.cab2b.common.ejb.EjbNamesConstants;
 import edu.wustl.cab2b.common.ejb.sqlquery.SQLQueryBusinessInterface;
 import edu.wustl.cab2b.common.ejb.sqlquery.SQLQueryHomeInterface;
 import edu.wustl.cab2b.common.locator.Locator;
 import edu.wustl.common.util.logger.Logger;
 
 /**
  * Test class to test {@link SQLQueryUtil}
  * To Run this test case a started server even though it is JUnit
  * @author Chandrakant Talele
  */
 public class SQLQueryBeanTest extends TestCase {
     SQLQueryBusinessInterface sqlQueryBean;
 
     /**
      * @see junit.framework.TestCase#setUp()
      */
     @Override
     protected void setUp() throws Exception {
         Logger.configure();
         String createTableSQL = "create table TEST_TABLE (ID BIGINT(38) NOT NULL, NAME VARCHAR(10) NULL,PRIMARY KEY (ID))";// insert into TEST_TABLE (ID,NAME) values (1,'ABC'),(2,'GAAL'),(3,'CLASS'),(4,'FOO')";
         sqlQueryBean = (SQLQueryBusinessInterface) Locator.getInstance().locate(EjbNamesConstants.SQL_QUERY_BEAN,SQLQueryHomeInterface.class);
         sqlQueryBean.executeUpdate(createTableSQL);
     }
 
     /**
      * This method tests funtionality provided by {@link SQLQueryUtil}
      * @throws RemoteException 
      */
     public void testSQLQueryUtil() throws RemoteException {
         String insertDataSQL = "insert into TEST_TABLE (ID,NAME) values (1,'ABC'),(2,'GAAL'),(3,'CLASS'),(4,'FOO')";
         int res=-1;
         try {
             res = sqlQueryBean.executeUpdate(insertDataSQL);
         } catch (RemoteException e1) {
             e1.printStackTrace();
             fail("Remote Exception");
         } catch (SQLException e1) {
             e1.printStackTrace();
             fail("SQLException");
         }
         assertEquals(4, res);
 
         String selectSQL = "SELECT id,name FROM test_table WHERE name like ?";
         int recordCount = 0;
        Object[][] rs = null;
         try {
             rs = sqlQueryBean.executeQuery(selectSQL, "%A%");
         } catch (SQLException e) {
             e.printStackTrace();
             fail("SQLQueryUtil.executeQuery() failed ");
         }
 
         for (int i = 0; i < rs.length; i++) {
 
             recordCount++;
            Long id = (Long) rs[i][0];
             String name = (String) rs[i][1];
             assertTrue(id != 4);
             assertTrue(name.indexOf((char) 'A') != -1);
         }
 
         assertTrue(recordCount == 3);
     }
 
     /**
      * @see junit.framework.TestCase#tearDown()
      */
     @Override
     protected void tearDown() throws Exception {
         sqlQueryBean.executeUpdate("DROP table TEST_TABLE");
     }
 
 }
