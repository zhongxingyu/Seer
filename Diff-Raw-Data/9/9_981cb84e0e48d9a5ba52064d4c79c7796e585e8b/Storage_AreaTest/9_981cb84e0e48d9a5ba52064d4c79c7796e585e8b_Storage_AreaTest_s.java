 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package Storage;
 
 import Helpers.Vector3f;
 import Main.Container;
 import org.junit.After;
 import org.junit.AfterClass;
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Test;
 import static org.junit.Assert.*;
 
 /**
  *
  * @author Karel
  */
 public class Storage_AreaTest 
 {
     private Container container;
     private Storage_Area storage;
     
     public Storage_AreaTest() 
     {
         try
         {
             storage = new Storage_Area(1,1,1,new Vector3f(1,1,1));
             container = new Container("Cont");
             storage.pushContainer(container, 0, 0);
         }
         catch(Exception E) { }
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
      * Test of rowEmpty method, of class Storage_Area.
      */
     @Test
     public void testRowEmpty() throws Exception 
     {
         if (storage.rowEmpty(0) == true)
         {
             fail("Return true when it should return false.");
         }
         
         storage.popContainer(0, 0);
         
         if (storage.rowEmpty(0) == false)
         {
             fail("Return false when it should return true.");
         }
     }
 
     /**
      * Test of peakContainer method, of class Storage_Area.
      */
     @Test
     public void testPeakContainer() throws Exception 
     {
         if (storage.peekContainer(0,0).getId().matches("Cont") == false)
         {
             fail("Doesn't return the right container.");
         }
     }
 
     /**
      * Test of popContainer method, of class Storage_Area.
      */
     @Test
     public void testPopContainer() throws Exception 
     {
         storage.popContainer(0, 0);
         
         if(storage.rowEmpty(0) == false)
         {
             fail("Doesn't pop the container correctly.");
         }
     }
 
     /**
      * Test of pushContainer method, of class Storage_Area.
      */
     @Test
     public void testPushContainer() throws Exception 
     {        
         if(storage.rowEmpty(0) == true)
         {
             fail("Doesn't push the container correctly.");
         }
     }
 
     /**
      * Test of getLength method, of class Storage_Area.
      */
     @Test
     public void testGetLength() 
     {
         if (storage.getLength() != 1)
         {
             fail("Doesn't return the correct length.");
         }
     }
 
     /**
      * Test of getWidth method, of class Storage_Area.
      */
     @Test
     public void testGetWidth() 
     {
         if (storage.getWidth() != 1)
         {
             fail("Doesn't return the correct Width.");
         }
     }
 
     /**
      * Test of getHeight method, of class Storage_Area.
      */
     @Test
     public void testGetHeight() 
     {
         if (storage.getHeight() != 1)
         {
             fail("Doesn't return the correct height.");
         }
     }
 
     /**
      * Test of Count method, of class Storage_Area.
      */
     @Test
     public void testCount_0args() 
     {
         if (storage.Count() != 1)
         {
             fail("Doesn't return the correct amount of containers in the storage.");
         }
     }
 
     /**
      * Test of Count method, of class Storage_Area.
      */
     @Test
    public void testCount_int_int() 
     {
         if (storage.Count(0,0) != 1)
         {
             fail("Doesn't return the correct amount of containers in the stack.");
         }
     }
 
     /**
      * Test of isFilled method, of class Storage_Area.
      */
     @Test
     public void testIsFilled() 
     {
         if (storage.isFilled() == false)
         {
             fail("Doesn't detect that the stack is filled to it's maximum potential.");
         }
     }
 }
