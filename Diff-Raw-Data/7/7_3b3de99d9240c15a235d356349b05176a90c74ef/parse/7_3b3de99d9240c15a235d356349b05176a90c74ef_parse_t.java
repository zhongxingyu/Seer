 /* Tests date parsing with SimpleDateFormat
    Copyright (C) 2004 Noa Resare <noa@resare.com>
 
    This file is part of mauve.
 */
 
 // Tags: JDK1.1
 
 package gnu.testlet.java.text.SimpleDateFormat;
 
 import gnu.testlet.Testlet;
 import gnu.testlet.TestHarness;
 import java.text.*;
 import java.util.*;
 
 public class parse implements Testlet
 {
   public void test(TestHarness harness)
   {
     SimpleDateFormat sdf = new SimpleDateFormat("MMMM yyyy", Locale.US);
     sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
 
     Map toTest = new HashMap();
     toTest.put("august 1978", new Date(270777600000L));
     toTest.put("August 1978", new Date(270777600000L));
     toTest.put("December 1978", new Date(281318400000L));
     doParse(harness, sdf, toTest);
 
     sdf.applyPattern("EEEE MMMM yyyy");
     toTest.clear();
     toTest.put("Saturday November 2004", new Date(1099699200000L));
     doParse(harness, sdf, toTest);
 
     sdf.applyPattern("yyyy-MM-dd HH:mm z");
     toTest.clear();
     toTest.put("2004-08-11 10:42 GMT", new Date(1092220920000L));
     toTest.put("2004-08-11 10:42 GMT+00:00", new Date(1092220920000L));
     toTest.put("2004-08-11 10:42 GMT-00:00", new Date(1092220920000L));
     toTest.put("2004-08-11 12:42 CEST", new Date(1092220920000L));
     toTest.put("2004-08-11 12:42 GMT+02:00", new Date(1092220920000L));
     toTest.put("2004-08-11 12:42 +0200", new Date(1092220920000L));
     doParse(harness, sdf, toTest);
 
     // Z should work exactly as z when parsing
     sdf.applyPattern("yyyy-MM-dd HH:mm Z");
     doParse(harness, sdf, toTest);
 
     // long and short names should both work.
     sdf.applyPattern("EEE MMM");
     toTest.clear();
     toTest.put("Sat Jun", new Date(13478400000L));
     toTest.put("Saturday June", new Date(13478400000L));
     doParse(harness, sdf, toTest);
     sdf.applyPattern("EEEE MMMM");
     doParse(harness, sdf, toTest);

    /* Test case from bug #11583 */
    SimpleDateFormat sdf1 = new SimpleDateFormat("MMM dd, yyyy");
    toTest = new HashMap();
    toTest.put("dec 31, 2004", new Date(1104451200000L));
    doParse(harness, sdf1, toTest);
   }
 
   /**
    * Test if the date strings in toTest equals to the Date values when parsed
    * with sdf.
    */
   private static void doParse(TestHarness h, SimpleDateFormat sdf, Map toTest)
   {
     h.checkPoint("parse pattern " + sdf.toPattern());
     Iterator cases = toTest.keySet().iterator();
     while (cases.hasNext())
       {
 	String dateString = (String)cases.next();
         try
 	  {
 	    h.check(sdf.parse(dateString), toTest.get(dateString));
           }
         catch(Exception e)
 	  {
 	    h.check(false, e.getClass().getName() + ": ");
 	    h.debug(e);
 	  }
       }
   }
 }
