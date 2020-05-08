 package org.apache.catalina.util;
 
 import static org.junit.Assert.assertEquals;
 
 import java.text.SimpleDateFormat;
 
 import org.junit.Test;
 
 public class TestStrftime {
 
     @Test
     public void parseText() throws Exception {
         long t = 1358675873;
        String pat = "%Y-%m-%d %X";
         Strftime strftime = new Strftime(pat);
         String converted = strftime.convertDateFormat(pat);
         System.out.println(converted);
         SimpleDateFormat f = new SimpleDateFormat(converted);
        assertEquals(t, f.parse("2013-01-20 18:57:53").getTime() / 1000);
     }
 }
