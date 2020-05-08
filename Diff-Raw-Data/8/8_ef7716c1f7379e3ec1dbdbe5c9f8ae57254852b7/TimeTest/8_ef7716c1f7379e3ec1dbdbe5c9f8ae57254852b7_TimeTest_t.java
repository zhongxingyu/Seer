 package library.tests;
 
 import static org.junit.Assert.*;
 import library.Time;
 import org.junit.Before;
 import org.junit.Test;
 
 /**
  * // -------------------------------------------------------------------------
 /**
  *  This class is for testing the Time class and its methods
  *
  *  @author cmbuck
  *  @version Apr 15, 2012
  */
 public class TimeTest
 {
     private Time astart, aend, bstart, bend, cstart, cend, dstart, dend;
 
     /**
      * This method sets up the testing environment
      */
     @Before
     public void setUp()
     {
         astart = new Time(2, 30);
         aend = new Time(3, 45);
         bstart = new Time(3, 30);
         bend = new Time(4, 45);
         cstart = new Time(2, 30);
         cend = new Time(3, 45);
         dstart = new Time(4, 0);
         dend = new Time (5, 15);
 
     }
 
     /**
      * This method tests the isOverlap() method
      */
     @Test
     public void testIsOverlap()
     {
         assertTrue(Time.isOverlap(astart, aend, bstart, bend));
         assertTrue(Time.isOverlap(bstart, bend, astart, aend));
         assertFalse(Time.isOverlap(cstart, cend, dstart, dend));
        assertTrue(Time.isOverlap(astart, aend, cstart, cend));
        assertTrue(Time.isOverlap(dstart, dend, bstart, bend));
        assertFalse(Time.isOverlap(dstart, dend, astart, aend));
        assertFalse(Time.isOverlap(astart, aend, dstart, dend));
 
 
         //tests Chris added
         assertFalse(Time.isOverlap(new Time(8,00), new Time(8,50),
             new Time(9,05), new Time(9,55)));
         assertFalse(Time.isOverlap(new Time(9,05), new Time(9,55),
             new Time(8,00), new Time(8,50))); //swap above
         assertTrue(Time.isOverlap(new Time(8,00), new Time(9,30),
             new Time(9,05), new Time(9,55)));
         assertTrue(Time.isOverlap(new Time(9,05), new Time(9,55),
             new Time(8,00), new Time(9,30))); //swap above
         assertTrue(Time.isOverlap(new Time(8,00), new Time(11,00),
             new Time(9,05), new Time(9,55)));
         assertTrue(Time.isOverlap(new Time(9,05), new Time(9,55),
             new Time(8,00), new Time(11,00))); //swap above
 
     }
 }
