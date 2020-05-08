 package com.sf.checkinactivity;
 
 import org.junit.Test;
 import org.xml.sax.SAXParseException;
 
 import java.io.FileNotFoundException;
 import java.util.Arrays;
 import java.util.List;
 
 import static org.junit.Assert.*;
 
 /**
  * Unit test for simple Checkins.
  */
 public class CheckinsTest {
     @Test
     public void testApp() {
         Checkins checkins = new Checkins();
         assertNotNull(checkins);
     }
     @Test
     public void checkDetailedCheckins() throws Exception {
         Checkins checkins = new Checkins();
         checkins.parse("src/test/resources/depot.xml");
         assertTrue(checkins.getDetailedCheckinPerDevPerDayOfWeek().size() > 0);
     }
 
     @Test(expected = FileNotFoundException.class)
     public void testFileNotFound() throws Exception {
         Checkins checkins = new Checkins();
         checkins.parse("fubar.txt");
     }
 
 
     @Test(expected = SAXParseException.class)
     public void testBadFile() throws Exception {
         Checkins checkins = new Checkins();
         checkins.parse("src/test/resources/baddepot.xml");
     }
 
     @Test
     public void totalPerDay() throws Exception {
         Checkins checkins = new Checkins();
         checkins.parse("src/test/resources/smalldepot.xml");
         assertEquals(checkins.totalForDay(Checkins.DAYS_OF_WEEK.SUNDAY), 0);
         assertEquals(checkins.totalForDay(Checkins.DAYS_OF_WEEK.MONDAY), 1);
         assertEquals(checkins.totalForDay(Checkins.DAYS_OF_WEEK.TUESDAY), 2);
         assertEquals(checkins.totalForDay(Checkins.DAYS_OF_WEEK.WEDNESDAY), 2);
         assertEquals(checkins.totalForDay(Checkins.DAYS_OF_WEEK.THURSDAY), 0);
         assertEquals(checkins.totalForDay(Checkins.DAYS_OF_WEEK.FRIDAY), 2);
         assertEquals(checkins.totalForDay(Checkins.DAYS_OF_WEEK.SATURDAY), 0);
     }
 
     @Test
     public void totalPerDayLargerDataSet() throws Exception {
         Checkins checkins = new Checkins();
         checkins.parse("src/test/resources/depot.xml");
         assertEquals(checkins.totalForDay(Checkins.DAYS_OF_WEEK.SUNDAY), 4);
         assertEquals(checkins.totalForDay(Checkins.DAYS_OF_WEEK.MONDAY), 13);
         assertEquals(checkins.totalForDay(Checkins.DAYS_OF_WEEK.TUESDAY), 19);
         assertEquals(checkins.totalForDay(Checkins.DAYS_OF_WEEK.WEDNESDAY), 19);
         assertEquals(checkins.totalForDay(Checkins.DAYS_OF_WEEK.THURSDAY), 30);
         assertEquals(checkins.totalForDay(Checkins.DAYS_OF_WEEK.FRIDAY), 14);
         assertEquals(checkins.totalForDay(Checkins.DAYS_OF_WEEK.SATURDAY), 3);
     }
     @Test
     public void devsWithFewest() throws Exception {
         Checkins checkins = new Checkins();
         checkins.parse("src/test/resources/depot.xml");
         List<String> devs =  checkins.devWithFewestForDay(Checkins.DAYS_OF_WEEK.THURSDAY);
         assertEquals(1, devs.size());
         assertTrue(devs.contains("dev4"));
     }
 
     @Test
     public void devsWithMost() throws Exception {
         Checkins checkins = new Checkins();
         checkins.parse("src/test/resources/depot.xml");
         List<String> devs =  checkins.devWithMostForDay(Checkins.DAYS_OF_WEEK.THURSDAY);
         assertEquals(1, devs.size());
         assertTrue(devs.contains("dev3"));
     }
 
 
     @Test
     public void devsWithMostMultiplesPerDay() throws Exception {
         Checkins checkins = new Checkins();
         checkins.parse("src/test/resources/minmaxdepot.xml");
         List<String> devs =  checkins.devWithMostForDay(Checkins.DAYS_OF_WEEK.MONDAY);
         assertEquals(2, devs.size());
         assertTrue(devs.contains("dev1"));
         assertTrue(devs.contains("dev2"));
     }
 
     @Test
     public void devsWithFewestMultiplesPerDay() throws Exception {
         Checkins checkins = new Checkins();
         checkins.parse("src/test/resources/minmaxdepot.xml");
         List<String> devs =  checkins.devWithFewestForDay(Checkins.DAYS_OF_WEEK.MONDAY);
         assertEquals(3, devs.size());
         assertTrue(devs.contains("dev3"));
         assertTrue(devs.contains("dev4"));
         assertTrue(devs.contains("dev5"));
     }
 
     @Test
     public void devs() throws Exception {
         Checkins checkins = new Checkins();
         checkins.parse("src/test/resources/depot.xml");
         String[] devs =  checkins.getDevelopers();
         assertEquals(4, devs.length);
         assertArrayEquals(new String[]{"dev1", "dev2", "dev3", "dev4"}, devs);
     }
 
     @Test
     public void checkDayConversions() throws Exception {
         Checkins checkins = new Checkins();
         checkins.parse("src/test/resources/one_seven_depot.xml");
         assertEquals(1, checkins.totalForDay(Checkins.DAYS_OF_WEEK.SUNDAY));
         assertEquals(2, checkins.totalForDay(Checkins.DAYS_OF_WEEK.MONDAY));
         assertEquals(3, checkins.totalForDay(Checkins.DAYS_OF_WEEK.TUESDAY));
         assertEquals(4, checkins.totalForDay(Checkins.DAYS_OF_WEEK.WEDNESDAY));
         assertEquals(5, checkins.totalForDay(Checkins.DAYS_OF_WEEK.THURSDAY));
         assertEquals(6, checkins.totalForDay(Checkins.DAYS_OF_WEEK.FRIDAY));
         assertEquals(7, checkins.totalForDay(Checkins.DAYS_OF_WEEK.SATURDAY));
     }
 
 
     @Test
     public void checkDatesOnAndAfter() throws Exception {
         Checkins checkins = new Checkins();
         checkins.parse("src/test/resources/one_seven_depot.xml", "10/8/2012", null);
         assertEquals(27, checkins.totalCheckins());
     }
 
     @Test
     public void checkDatesBefore() throws Exception {
         Checkins checkins = new Checkins();
         checkins.parse("src/test/resources/one_seven_depot.xml", null, "10/8/2012");
         assertEquals(1, checkins.totalCheckins());
     }
 
     @Test
     public void checkDatesBetween() throws Exception {
         Checkins checkins = new Checkins();
         checkins.parse("src/test/resources/one_seven_depot.xml", "10/8/2012", "10/10/2012");
         assertEquals(5, checkins.totalCheckins());
     }
 
 
     @Test
     public void compareXmlToP4Direct() throws Exception {
         if (getClass().getResourceAsStream(P4Checkins.PROPERTIES_FILE) != null) {
             Checkins checkins = new Checkins();
             checkins.parse("src/test/resources/nl_depot.xml", "10/1/2011", "10/10/2012");
             Checkins p4 = new Checkins();
            p4.p4(Arrays.asList("nlipke"), "10/1/2011", "10/10/2012");
             System.err.println(checkins.totalCheckins());
             assertEquals(checkins.totalCheckins(), p4.totalCheckins());
             assertEquals(checkins.totalForDay(Checkins.DAYS_OF_WEEK.MONDAY), p4.totalForDay(Checkins.DAYS_OF_WEEK.MONDAY));
         } else {
             assertTrue(true);
         }
     }
 }
