 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package gov.usgs.webservices.ibatis;
 
 import org.junit.AfterClass;
 import org.junit.BeforeClass;
 import org.junit.Test;
 import static org.junit.Assert.*;
 
 /**
  *
  * @author lkranendonk
  */
 public class StateCodeTypeHandlerTest {
 
     public StateCodeTypeHandlerTest() {
     }
 
 
 
     @Test
     public void testGetPostalNameFromSQL() {
 
         String result = StateCodeTypeHandler.getPostalNameFromSQL("01");
         assertEquals("AL", result);
         result = StateCodeTypeHandler.getPostalNameFromSQL("55");
         assertEquals("WI", result);
        result = StateCodeTypeHandler.getPostalNameFromSQL("12");
        assertEquals("FL", result);
     }
 
     @Test
     public void testGetStateNameFromSQL() {
 
         String result = StateCodeTypeHandler.getStateNameFromSQL("01");
         assertEquals("Alabama", result);
         result = StateCodeTypeHandler.getStateNameFromSQL("55");
         assertEquals("Wisconsin", result);
        result = StateCodeTypeHandler.getStateNameFromSQL("12");
        assertEquals("Flordia", result);
     }
 
 }
