 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package etc.aloe.filters;
 
 import org.junit.After;
 import org.junit.AfterClass;
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Test;
 import static org.junit.Assert.*;
 import weka.core.Instances;
 
 /**
  *
  * @author michael
  */
 public class StringToDictionaryVectorTest {
 
     public StringToDictionaryVectorTest() {
     }
 
     @BeforeClass
     public static void setUpClass() {
     }
 
     @AfterClass
     public static void tearDownClass() {
     }
 
     @Before
     public void setUp() {
     }
 
     @After
     public void tearDown() {
     }
 
     /**
      * Test of determineOutputFormat method, of class StringToDictionaryVector.
      */
     @Test
     public void testDetermineOutputFormat() throws Exception {
         System.out.println("determineOutputFormat");
         Instances inputFormat = null;
         StringToDictionaryVector instance = new StringToDictionaryVector();
         Instances expResult = null;
        Instances result = instance.determineOutputFormat(inputFormat);
        assertEquals(expResult, result);
         // TODO review the generated test code and remove the default call to fail.
         fail("The test case is a prototype.");
     }
 
     /**
      * Test of process method, of class StringToDictionaryVector.
      */
     @Test
     public void testProcess() throws Exception {
         System.out.println("process");
         Instances instances = null;
         StringToDictionaryVector instance = new StringToDictionaryVector();
         Instances expResult = null;
        Instances result = instance.process(instances);
        assertEquals(expResult, result);
         // TODO review the generated test code and remove the default call to fail.
         fail("The test case is a prototype.");
     }
 
 }
