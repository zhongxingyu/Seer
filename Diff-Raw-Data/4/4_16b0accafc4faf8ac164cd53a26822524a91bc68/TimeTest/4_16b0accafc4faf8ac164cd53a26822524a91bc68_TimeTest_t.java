 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package model;
 
 import java.util.Calendar;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import org.junit.After;
 import org.junit.AfterClass;
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Ignore;
 import org.junit.Test;
 import static org.junit.Assert.*;
 
 /**
  *
  * @author liufeng
  */
 public class TimeTest {
 
     public TimeTest() {
     }
 
     @BeforeClass
     public static void setUpClass() throws Exception {
     }
 
     @AfterClass
     public static void tearDownClass() throws Exception {
     }
 
     @Before
     public void setUp() {
     }
 
     @After
     public void tearDown() {
     }
 
     /**
      * Test of getYear method, of class Time.
      */
     @Test
     public void testGetYear() {
         System.out.println("getYear");
         Time instance = new Time();
         int expResult = 2009;
         int result = instance.getYear();
         assertEquals(expResult, result);
     }
 
     /**
      * Test of getMonth method, of class Time.
      */
     @Test
     public void testGetMonth() {
         System.out.println("getMonth");
         Time instance = new Time();
         int expResult = 5;
         int result = instance.getMonth();
         assertEquals(expResult, result);
 
         instance = new Time(2009, 5, 19, 20, 29, 30);
         expResult = 5;
         result = instance.getMonth();
         assertEquals(expResult, result);
     }
 
     /**
      * Test of getDay method, of class Time.
      */
     @Test
     public void testGetDay() {
         System.out.println("getDay");
 
         assertEquals(19, new Time(2009, 5, 19, 20, 20, 20).getDay());
         assertEquals(1, new Time(2009, 5, 1, 20, 20, 20).getDay());
     }
 
     /**
      * Test of getHour method, of class Time.
      */
     @Test
     public void testGetHour() {
         System.out.println("getHour");
         Time instance = new Time(2014, 12, 25, 19, 28, 30);
         int expResult = 19;
         int result = instance.getHour();
         assertEquals(expResult, result);
     }
 
     /**
      * Test of getMinute method, of class Time.
      */
     @Test
     public void testGetMinute() {
         System.out.println("getMinute");
         Time instance = new Time(1987, 2, 8, 14, 10, 29);
         int expResult = 10;
         int result = instance.getMinute();
         assertEquals(expResult, result);
     }
 
     /**
      * Test of getSecond method, of class Time.
      */
     @Test
     public void testGetSecond() {
         System.out.println("getSecond");
         Time instance = new Time(1997, 12, 25, 12, 29, 36);
         int expResult = 36;
         int result = instance.getSecond();
         assertEquals(expResult, result);
     }
 
     /**
      * Test of add method, of class Time.
      */
     @Test
     public void testAdd() {
         System.out.println("add");
         Time time = new Time(2, 0, 0, 0, 48, 0);
         Time instance = new Time(2009, 5, 19, 20, 0, 0);
         instance.add(time);
         String expResult = "2011-5-19 20:48:0";
         String result = instance.toString();
         assertEquals(expResult, result);
     }
 
     /**
      * Test of minus method, of class Time.
      */
     @Test
     public void testMinus() {
         System.out.println("minus");
         Time time = new Time(2009, 5, 19, 20, 18, 20);
         Time instance = new Time(2009, 5, 1, 22, 29, 30);
         int expResult = 18;
         int result = instance.minus(time);
         assertEquals(19, time.getDay());
         assertEquals(1, instance.getDay());
         assertEquals(expResult, result);
     }
 
     /**
      * Test of getTimeInMillis method, of class Time.
      */
     @Ignore
     @Test
     public void testGetTimeInMillis() {
         System.out.println("getTimeInMillis");
         Time instance = new Time(1987, 2, 8, 14, 10, 29);
         long expResult = 53981342909907L;
         long result = instance.getTimeInMillis();
         assertEquals(expResult, result);
     }
 
     /**
      * Test of compareTo method, of class Time.
      */
     @Test
     public void testCompareTo() {
         System.out.println("compareTo");
         Time time = new Time();
         try {
             Thread.sleep(1000);
         } catch (InterruptedException ex) {
             Logger.getLogger(TimeTest.class.getName()).log(Level.SEVERE, null, ex);
         }
         Time instance = new Time();
         // time is later than instance.
         int expResult = 1;
         int result = instance.compareTo(time);
         assertEquals(expResult, result);
 
         expResult = -1;
         result = time.compareTo(instance);
         assertEquals(expResult, result);
     }
 
     /**
      * Test of getCalendar method, of class Time.
      */
     @Ignore
     @Test
     public void testGetCalendar() {
         System.out.println("getCalendar");
         Time instance = new Time();
         Calendar expResult = null;
         Calendar result = instance.getCalendar();
         assertEquals(expResult, result);
     }
 
     /**
      * Test of after method, of class Time.
      */
     @Test
     public void testAfter() {
         System.out.println("after");
         Time time = new Time();
         try {
             Thread.sleep(1000);
         } catch (InterruptedException ex) {
             Logger.getLogger(TimeTest.class.getName()).log(Level.SEVERE, null, ex);
         }
         Time instance = new Time();
         boolean expResult = true;
         boolean result = instance.after(time);
         assertEquals(expResult, result);
     }
 
     /**
      * Test of before method, of class Time.
      */
     @Test
     public void testBefore() {
         System.out.println("before");
         Time time = new Time();
         try {
             Thread.sleep(1000);
         } catch (InterruptedException ex) {
             Logger.getLogger(TimeTest.class.getName()).log(Level.SEVERE, null, ex);
         }
         Time instance = new Time();
         boolean expResult = false;
         boolean result = instance.before(time);
         assertEquals(expResult, result);
     }
 
     /**
      * Test of getCurrentTime method, of class Time.
      */
     @Ignore
     @Test
     public void testGetCurrentTime() {
         System.out.println("getCurrentTime");
         Time instance = new Time();
         Time expResult = null;
         Time result = instance.getCurrentTime();
         assertEquals(expResult, result);
     }
 
     /**
      * Test of toString method, of class Time.
      */
     @Test
     public void testToString() {
         System.out.println("toString");
         Time instance = new Time(2009, 5, 19, 20, 6, 0);
         String expResult = "2009-5-19 20:6:0";
         String result = instance.toString();
         assertEquals(expResult, result);
     }
 
 }
