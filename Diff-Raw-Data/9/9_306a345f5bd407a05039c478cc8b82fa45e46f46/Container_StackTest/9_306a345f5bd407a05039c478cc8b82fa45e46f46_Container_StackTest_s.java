 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package Storage;
 
 import Main.Container;
 import java.util.Date;
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
 public class Container_StackTest {
     
     private Container_Stack stack;
     private Container container;
     
     public Container_StackTest() 
     {
         container = new Container("Cont");
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
      * Test of getHeight method, of class Container_Stack.
      */
     @Test
     public void testGetHeight() 
     {
         try
         {
             stack = new Container_Stack(6);
         }
         catch (Exception E) { }
         
         try
         {
             stack.push(container);
             stack.push(container); 
         }        
         catch (Exception E) { }
         
         
         int result = stack.getHeight();
         int expResult = 2;
         
         assertEquals(expResult, result);
     }
 
     /**
      * Test of pop method, of class Container_Stack.
      */
     @Test
     public void testPop() 
     {
         try
         {
             stack = new Container_Stack(6);
         }
         catch (Exception E) { }
         
         try
         {
             stack.push(container);
         }
         catch (Exception E) { }
         
         try
         {
             stack.pop();
         }
         catch (Exception E) { fail("The method throws an exception when it shouldn't."); }
         
         try
         {
             stack.pop();
             fail("The method doesn't throw an exception when it should.");
         }
         catch (Exception E) { }
     }
 
     /**
      * Test of peak method, of class Container_Stack.
      */
     @Test
     public void testPeak() 
     {
         try
         {
             stack = new Container_Stack(6);
         }
         catch (Exception E) { }
         
         try
         {
             stack.push(container);
         }
         catch (Exception E) { }
         
         try
         {
            Container cont = stack.peak();
             
             if (cont.getId().matches("Cont") == false)
             {
                 fail("Doesn't return the right container");
             }
         }
         catch (Exception E) { fail("Doesn't return a container."); }
     }
 
     /**
      * Test of push method, of class Container_Stack.
      */
     @Test
     public void testPush() 
     {        
         try
         {
             stack = new Container_Stack(6);
         }
         catch (Exception E) { }
         
         boolean nullTest = false;
         
         try { stack.push(null); }
         catch (Exception E) { nullTest = true; }
         
         try { stack.push(container); }
         catch (Exception E) { fail("Push returns an exception when it shouldn't.");  }
         
         if(stack.getHeight() != 1)
         {
             fail("There's more than 1 container ");
         }
     }
     
     @Test
     public void testHeight()
     {
         try
         {
             stack = new Container_Stack(6);
         }
         catch (Exception E) { }
         
         try
         {
             for (int i = 0; i < 7; i++)
             {
                 stack.push(container);           
             }
             
             fail("No exception is thrown when it needs to be thrown.");
         }
         catch(Exception E) {  } 
     }
     
     @Test
     public void testDepth()
     {
         try
         {
             stack = new Container_Stack(6);
         }
         catch (Exception E) { }
         
         try
         {
             for (int i = 0; i < 3; i++)
             {
                 stack.pop();           
             }
             
             fail("No exception is thrown when it needs to be thrown.");
         }
         catch(Exception E) {  } 
     }
     
     @Test
     public void testAddRemove()
     {
         try
         {
             stack = new Container_Stack(6);
         }
         catch (Exception E) { }
         
         try
         {
             for (int i = 0; i < 3; i++)
             {
                 stack.push(container);
                 stack.pop();           
             }
             
             for (int i = 0; i < 3; i++)
             {
                 stack.push(container);          
             }
             
             for (int i = 0; i < 3; i++)
             {
                 stack.pop();           
             }
             
             for (int i = 0; i < 6; i++)
             {
                 stack.push(container);          
             }
             
             for (int i = 0; i < 6; i++)
             {
                 stack.pop();          
             }
         }
         catch(Exception E) { fail("Exception is thrown when none should be.");  } 
     }
     
     @Test
     public void testNull ()
     {
         try
         {
             stack = new Container_Stack(6);
         }
         catch (Exception E) { }
         
         try
         {
             stack.push(null);
             fail("No exception is thrown when it needs to be thrown.");
         }
         catch (Exception E) { }
     }
 }
