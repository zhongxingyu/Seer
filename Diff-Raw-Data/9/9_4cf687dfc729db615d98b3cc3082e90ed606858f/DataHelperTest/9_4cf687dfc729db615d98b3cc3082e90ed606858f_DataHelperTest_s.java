 package ch.hsr.wolski.omc.services.helper;
 
 import static org.junit.Assert.assertTrue;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 
 import ch.hsr.wolski.omc.helper.DatabaseHelper;
 
 /**
  * User: Michael Wolski
  * Date: 12/11/13
  * Time: 11:13 PM
  */
 public class DataHelperTest {
 
     DatabaseHelper databaseHelper;
 
     @Before
     public void setUp() throws Exception {
         databaseHelper = DatabaseHelper.getInstance();
     }
 
     @After
     public void tearDown() throws Exception {
 
     }
 
     @Test
     public void testIsAuthorizationValid() {
       assertTrue(databaseHelper.isAuthorizationValid("michaelwolaski2010@gmail.com", "hallo"));
     }
 
 }
