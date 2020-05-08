 package edu.depaul.se433;
 
 import org.junit.*;
 import static org.junit.Assert.*;
 
import static edu.depaul.se433.BinarySearch.*;

 /**
  *
  * @author Tomislav S. Mitic
  */
 public class BinarySearchTest {
 
   /**
    * Test of binarySearch method, of class BinarySearch.
    */
   @Test
   public void testBinarySearch_1() {
     try {
       System.out.println("binarySearch case 1");
       Integer[] a = new Integer[] {1, 3, 5, 7};
       int expResult1 = 1, expResult2 = -1;
       assertTrue(binarySearch(a, 3) == expResult1);
       assertTrue(binarySearch(a, 2) == expResult2);
     } catch(Exception e) {
       // TODO review the generated test code and remove the default call to fail.
       fail("The test has failed: " + e.toString());
     }
   }
 
   /**
    * Test of binarySearch method, of class BinarySearch.
    */
   @Test(expected = NullPointerException.class)
   public void testBinarySearch_2() {
     System.out.println("binarySearch case 1");
     Integer[] a = new Integer[] {1, 3, 5, 7};
     int expResult1 = 1, expResult2 = -1;
     assertTrue(binarySearch(a, 3) == expResult1);
     assertTrue(binarySearch(a, null) == expResult2);
     // TODO review the generated test code and remove the default call to fail.
    fail("Null Pointer has been encounter.");
   }
 
   /**
    * Test of binarySearch2 method, of class BinarySearch.
    */
   @Test(expected = IllegalArgumentException.class)
   public void testBinarySearch2_1() {
     System.out.println("binarySearch case 1");
     Integer[] a = new Integer[] {1, 3, 5, 7};
     int expResult1 = 1, expResult2 = -1;
     assertTrue(binarySearch2(a, 3) == expResult1);
     assertTrue(binarySearch2(a, null) == expResult2);
     fail("Illegal Argument has been encounter");
   }
 }
