 /*
  * $Id: HexConverterTests.java,v 1.16 2007/03/16 10:30:41 agoubard Exp $
  *
  * Copyright 2003-2007 Orange Nederland Breedband B.V.
  * See the COPYRIGHT file for redistribution and use restrictions.
  */
 package org.xins.tests.common.text;
 
 import junit.framework.Test;
 import junit.framework.TestCase;
 import junit.framework.TestSuite;
 
 import org.xins.common.text.HexConverter;
 
 /**
  * Tests for class <code>HexConverter</code>.
  *
  * @version $Revision: 1.16 $ $Date: 2007/03/16 10:30:41 $
  * @author <a href="mailto:ernst@ernstdehaan.com">Ernst de Haan</a>
  */
 public class HexConverterTests extends TestCase {
 
    /**
     * Constructs a new <code>HexConverterTests</code> test suite with
     * the specified name. The name will be passed to the superconstructor.
     *
     * @param name
     *    the name for this test suite.
     */
    public HexConverterTests(String name) {
       super(name);
    }
 
    /**
     * Returns a test suite with all test cases defined by this class.
     *
     * @return
     *    the test suite, never <code>null</code>.
     */
    public static Test suite() {
       return new TestSuite(HexConverterTests.class);
    }
 
    public void testParseHexString_String() throws Throwable {
 
       // Pass arguments that should trigger failure
       doTestParseHexString_String(null,                           0L);
       doTestParseHexString_String("",                             0L);
       doTestParseHexString_String("1",                            0L);
       doTestParseHexString_String("0",                            0L);
       doTestParseHexString_String("123456789012345",              0L);
       doTestParseHexString_String("12345678901234567",            0L);
       doTestParseHexString_String("123456789012345g",             0L);
       doTestParseHexString_String("0000000000000000",             0L);
       doTestParseHexString_String("0000000000000001",             1L);
       doTestParseHexString_String("0000000000000002",             2L);
       doTestParseHexString_String("0000000000000003",             3L);
       doTestParseHexString_String("000000000000000f",             0xfL);
       doTestParseHexString_String("00000000000000ff",             0xffL);
       doTestParseHexString_String("1234567890123456",             0x1234567890123456L);
       doTestParseHexString_String("123456789012345a",             0x123456789012345aL);
       doTestParseHexString_String(String.valueOf(Long.MAX_VALUE), Long.MAX_VALUE);
       doTestParseHexString_String("ffffffffffffffff",             -1L);
       doTestParseHexString_String("fffffffffffffffe",             -2L);
       doTestParseHexString_String(String.valueOf(Long.MIN_VALUE), Long.MIN_VALUE);
 
       // Test other methods
       assertTrue(HexConverter.isHexDigit('6'));
       assertTrue(HexConverter.isHexDigit('b'));
       assertTrue(HexConverter.isHexDigit('F'));
 
       assertEquals("ANT", new String(HexConverter.parseHexBytes("414e54", 0, 6)));
       assertEquals(2, HexConverter.parseHexBytes("414e54", 2, 4).length);
       assertEquals((byte)78, HexConverter.parseHexBytes("414e54", 2, 2)[0]);
 
       assertEquals(0x123b56F, HexConverter.parseHexInt("Testing 0123b56F", 8));
       assertEquals(0x123b56F, HexConverter.parseHexInt("0123b56F"));
    }
 
    private void doTestParseHexString_String(String arg, long expected) {
 
       boolean illegalArg = (arg == null || arg.length() != 16);
       boolean invalidNumberFormat = false;
       for (int i = 0; !illegalArg && !invalidNumberFormat && i < 16; i++) {
          char c = arg.charAt(i);
          if (c >= '0' && c <= '9') {
             // okay
          } else if (c >= 'a' && c <= 'f') {
             // okay
          } else {
             invalidNumberFormat = true;
          }
       }
 
       String s = (arg == null) ? "null" : "\"" + arg + '"';
 
       if (illegalArg) {
          try {
             HexConverter.parseHexLong(arg);
             fail("HexConverter.parseHexLong(" + s + ") should throw an IllegalArgumentException.");
          } catch (IllegalArgumentException exception) {
             // as expected
          }
       } else if (invalidNumberFormat) {
          try {
             HexConverter.parseHexLong(arg);
             fail("HexConverter.parseHexLong(" + s + ") should throw a NumberFormatException.");
          } catch (NumberFormatException exception) {
             // as expected
          }
       } else {
          assertEquals(expected, HexConverter.parseHexLong(arg));
       }
    }
 
    public void testToHexString_byteArray() throws Throwable {
 
       // Test with null
       byte[] input1 = null;
       try {
          HexConverter.toHexString(input1);
          fail("Expected HexConverter.toHexString(null) to throw IllegalArgumentException.");
          return;
       } catch (IllegalArgumentException exception) {
          // as expected
       }
 
       // Test with zero-size byte array
       input1 = new byte[0];
      assertEquals("", HexConverter.toHexString(input1)); // XXX: Before XINS 3.0, this would have thrown an IllegalArgumentException
 
       // Test with other data
       input1 = new byte[] { (byte) 56, (byte) 10, (byte) 230};
       assertEquals("380ae6", HexConverter.toHexString(input1));
       input1 = new byte[] { (byte) 0, (byte) 10, (byte) 0};
       assertEquals("000a00", HexConverter.toHexString(input1));
    }
 
    public void testToHexString_byte() throws Throwable {
       /* TODO
       doTestToHexString((byte) 0x00, "0000000000000001");
       doTestToHexString(0x1234567890123456L, "1234567890123456");
       doTestToHexString(0x1234567890ABCDEFL, "1234567890abcdef");
       */
 
       assertEquals("00", HexConverter.toHexString((byte) 0x00));
       assertEquals("01", HexConverter.toHexString((byte) 0x01));
       assertEquals("0a", HexConverter.toHexString((byte) 0x0a));
       assertEquals("10", HexConverter.toHexString((byte) 0x10));
       assertEquals("34", HexConverter.toHexString((byte) 0x34));
       assertEquals("ff", HexConverter.toHexString((byte) 0xff));
    }
 
    public void testToHexString_char() throws Throwable {
       /* TODO
       doTestToHexString("", 1L, "0000000000000001");
       doTestToHexString("", 0x1234567890123456L, "1234567890123456");
       doTestToHexString("", 0x1234567890ABCDEFL, "1234567890abcdef");
       */
 
       assertEquals("0000", HexConverter.toHexString((char) 0x0000));
       assertEquals("0001", HexConverter.toHexString((char) 0x0001));
       assertEquals("000a", HexConverter.toHexString((char) 0x000a));
       assertEquals("1234", HexConverter.toHexString((char) 0x1234));
       assertEquals("1000", HexConverter.toHexString((char) 0x1000));
       assertEquals("ffff", HexConverter.toHexString((char) 0xffff));
    }
 
    public void testToHexString_short() throws Throwable {
       /* TODO
       doTestToHexString("", 1L, "0000000000000001");
       doTestToHexString("", 0x1234567890123456L, "1234567890123456");
       doTestToHexString("", 0x1234567890ABCDEFL, "1234567890abcdef");
       */
 
       assertEquals("0000", HexConverter.toHexString((short) 0x0000));
       assertEquals("0001", HexConverter.toHexString((short) 0x0001));
       assertEquals("000a", HexConverter.toHexString((short) 0x000a));
       assertEquals("1234", HexConverter.toHexString((short) 0x1234));
       assertEquals("1000", HexConverter.toHexString((short) 0x1000));
       assertEquals("ffff", HexConverter.toHexString((short) 0xffff));
    }
 
    public void testToHexString_int() throws Throwable {
       /* TODO
       doTestToHexString("", 1L, "0000000000000001");
       doTestToHexString("", 0x1234567890123456L, "1234567890123456");
       doTestToHexString("", 0x1234567890ABCDEFL, "1234567890abcdef");
       doTestToHexString("Testing ", 0x1234567890ABCDEFL, "Testing 1234567890abcdef");
       */
 
       assertEquals("00000000",         HexConverter.toHexString(0x00000000));
       assertEquals("00000001",         HexConverter.toHexString(0x00000001));
       assertEquals("0000000a",         HexConverter.toHexString(0x0000000a));
       assertEquals("00001234",         HexConverter.toHexString(0x00001234));
       assertEquals("00123456",         HexConverter.toHexString(0x00123456));
       assertEquals("10000000",         HexConverter.toHexString(0x10000000));
       assertEquals("12345678",         HexConverter.toHexString(0x12345678));
       assertEquals("ffffffff",         HexConverter.toHexString(0xffffffff));
    }
 
    public void testToHexString_long() throws Throwable {
       doTestToHexString(0x0000000000000001L, "0000000000000001");
       doTestToHexString(0x1234567890123456L, "1234567890123456");
       doTestToHexString(0x1234567890ABCDEFL, "1234567890abcdef");
 
       assertEquals("0000000000000000", HexConverter.toHexString(0x0000000000000000L));
       assertEquals("0000000000000001", HexConverter.toHexString(0x0000000000000001L));
       assertEquals("000000000000000a", HexConverter.toHexString(0x000000000000000aL));
       assertEquals("0000000000001234", HexConverter.toHexString(0x0000000000001234L));
       assertEquals("0000000000123456", HexConverter.toHexString(0x0000000000123456L));
       assertEquals("1000000000000000", HexConverter.toHexString(0x1000000000000000L));
       assertEquals("1234567890abcdef", HexConverter.toHexString(0x1234567890ABCDEFL));
       assertEquals("ffffffffffffffff", HexConverter.toHexString(0xffffffffffffffffL));
    }
 
    public void testParseHexBytes() throws Exception {
 
       try {
          HexConverter.parseHexBytes(null, 0, 1);
          fail("Expected parseHexBytes(null,0,1) to throw an IllegalArgumentException.");
          return;
       } catch (IllegalArgumentException exception) {
          // as expected
       }
 
       try {
          HexConverter.parseHexBytes("000000", -1, 1);
          fail("Expected parseHexBytes(<string>,-1,1) to throw an IllegalArgumentException.");
          return;
       } catch (IllegalArgumentException exception) {
          // as expected
       }
 
       try {
          HexConverter.parseHexBytes("00000", 1, -1);
          fail("Expected parseHexBytes(<string>,1,-1) to throw an IllegalArgumentException.");
          return;
       } catch (IllegalArgumentException exception) {
          // as expected
       }
 
       try {
          HexConverter.parseHexBytes("00000", 5, 1);
          fail("Expected parseHexBytes(<string>,<stringlength>,-1) to throw an IllegalArgumentException.");
          return;
       } catch (IllegalArgumentException exception) {
          // as expected
       }
 
       // TODO: byte[] bytes = HexConverter.parseHexBytes("
    }
 
    public void testParseHexInt() throws Exception {
 
       try {
          HexConverter.parseHexInt(null);
          fail("Expected parseHexInt(null) to throw an IllegalArgumentException.");
          return;
       } catch (IllegalArgumentException exception) {
          // as expected
       }
 
       String input = "";
       try {
          HexConverter.parseHexInt("");
          fail("Expected parseHexInt(\"" + input + "\") to throw an IllegalArgumentException.");
          return;
       } catch (IllegalArgumentException exception) {
          // as expected
       }
 
       input = "1234567";
       try {
          HexConverter.parseHexInt(input);
          fail("Expected parseHexInt(\"" + input + "\") to throw an IllegalArgumentException.");
          return;
       } catch (IllegalArgumentException exception) {
          // as expected
       }
 
       input = "123456789";
       try {
          HexConverter.parseHexInt(input);
          fail("Expected parseHexInt(\"" + input + "\") to throw an IllegalArgumentException.");
          return;
       } catch (IllegalArgumentException exception) {
          // as expected
       }
 
       input        = "2468abcd";
       int expected = 0x2468abcd;
       int actual   = HexConverter.parseHexInt(input);
       assertEquals(expected, actual);
 
       input    = "00000001";
       expected = 0x00000001;
       actual   = HexConverter.parseHexInt(input);
       assertEquals(expected, actual);
 
       input    = "aAaAaAa0";
       expected = 0xaaaaaaa0;
       actual   = HexConverter.parseHexInt(input);
       assertEquals(expected, actual);
    }
 
    public void testParseHexLong() throws Exception {
 
       try {
          HexConverter.parseHexLong(null);
          fail("Expected parseHexLong(null) to throw an IllegalArgumentException.");
          return;
       } catch (IllegalArgumentException exception) {
          // as expected
       }
 
       String input = "";
       try {
          HexConverter.parseHexLong("");
          fail("Expected parseHexLong(\"" + input + "\") to throw an IllegalArgumentException.");
          return;
       } catch (IllegalArgumentException exception) {
          // as expected
       }
 
       input = "12345678";
       try {
          HexConverter.parseHexLong(input);
          fail("Expected parseHexLong(\"" + input + "\") to throw an IllegalArgumentException.");
          return;
       } catch (IllegalArgumentException exception) {
          // as expected
       }
 
       input = "123456789012345";
       try {
          HexConverter.parseHexLong(input);
          fail("Expected parseHexLong(\"" + input + "\") to throw an IllegalArgumentException.");
          return;
       } catch (IllegalArgumentException exception) {
          // as expected
       }
 
       input = "12345678901234567";
       try {
          HexConverter.parseHexLong(input);
          fail("Expected parseHexLong(\"" + input + "\") to throw an IllegalArgumentException.");
          return;
       } catch (IllegalArgumentException exception) {
          // as expected
       }
 
       input         = "2468abcd12345678";
       long expected = 0x2468abcd12345678L;
       long actual   = HexConverter.parseHexLong(input);
       assertEquals(expected, actual);
 
       input    = "0000000000000001";
       expected = 0x0000000000000001L;
       actual   = HexConverter.parseHexLong(input);
       assertEquals(expected, actual);
 
       input    = "aAaAaAa0bbbbbbbb";
       expected = 0xaaaaaaa0bbbbbbbbL;
       actual   = HexConverter.parseHexLong(input);
       assertEquals(expected, actual);
    }
 
    private void doTestToHexString(long value, String expectedResult) {
       assertEquals(expectedResult, HexConverter.toHexString(value));
    }
 }
