 package ar.capacitacion.jdbc;
 
 import java.sql.ResultSet;
 import java.sql.SQLException;
 
 import junit.framework.Assert;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 
 /**
  * @author dan
  * 
  */
 public class H2JDBCTest {
 
 	private H2Acces h2Acces;
 
 	@Before
 	public void createConnection() {
 		h2Acces = new H2Acces();
 		h2Acces.createConnection();
 	}
 
 	@After
 	public void closeConnection() {
 		try {
 			h2Acces.getConn().close();
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 	}
 
 	@Test
 	public void testConnection() {
 		Assert.assertNotNull(h2Acces.getConn());
 	}
 
 	@Test
 	public void testResultSetStatment() {
 		try {
 			ResultSet resultSet = h2Acces
 					.executeStatment("select count(*) total from TEST");
 			Assert.assertNotNull(resultSet.findColumn("total"));
 			
 			//obtengo la primera tupla
 			resultSet.next();
 			//obtengo la primer columna y la tipo a int.
 			Assert.assertEquals(1, resultSet.getInt(1));
 			resultSet.close();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 	
 	@Test
 	public void testPreparedStatementInsert() {
 		try {
 			h2Acces.executePreparedStatementInsertTest("daniel", "danhorris@gmail.com", "primer insert");
 			
 			ResultSet resultSet =  h2Acces.executeStatment("Select count(*) from TEST");
 			resultSet.next();
			
 			Assert.assertEquals(2,resultSet.getInt(1));
 			
 		} catch (SQLException e) {
			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 }
