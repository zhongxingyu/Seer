 package com.lolcode.runtime;
 
 import junit.framework.Assert;
 import org.junit.Test;
 
 public class LolStdLibTest {
 
     TestStdLib lib = new TestStdLib();
 
     private void testPrintAssert(LolObject val, String expected) {
         lib.print(val);
         Assert.assertEquals(lib.out, expected);
     }
 
     @Test
     public void testPrint() throws Exception {
         testPrintAssert(new LolBool(true), "true");
         testPrintAssert(new LolBool(false), "false");
         testPrintAssert(new LolDouble(3.1415926535899996), "3.1415926535899996");
         testPrintAssert(new LolInt(12345), "12345");
         testPrintAssert(new LolString("string"), "string");
     }
 
     private void testReadAssert(String input, LolObject expected) {
         lib.in = input;
         Assert.assertTrue(lib.read().eq(expected).toBool());
     }
 
     @Test
     public void testRead() throws Exception {
        testReadAssert("true", new LolBool(true));
        testReadAssert("false", new LolBool(false));
         testReadAssert("123456", new LolInt(123456));
         testReadAssert("123.456", new LolDouble(123.456));
         testReadAssert("string", new LolString("string"));
     }
 }
