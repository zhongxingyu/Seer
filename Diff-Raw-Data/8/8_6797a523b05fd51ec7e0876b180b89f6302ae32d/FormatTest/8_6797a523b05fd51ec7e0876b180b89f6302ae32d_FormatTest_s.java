 package com.cffreedom.utils;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertNull;
 
 import java.math.BigDecimal;
 
 import org.junit.Assert;
 import org.junit.Test;
 
 public class FormatTest
 {
 	@Test
 	public void numberTest()
 	{
 		int val = 5;
 		assertEquals("5", Format.number(val, 0));
 		assertEquals("5.00", Format.number(val, 2));
 		val = 55000;
 		assertEquals("55,000", Format.number(val, 0));
 		
 		double doubleVal = 6000;
 		assertEquals("6,000", Format.number(doubleVal, 0));
 		assertEquals("6,000.0", Format.number(doubleVal, 1));
 	}
 	
 	@Test
 	public void currencyTest()
 	{
 		int amount = 6000;
 		assertEquals("$6,000", Format.currency(amount, false));
 		assertEquals("$6,000.00", Format.currency(amount, true));
 		
 		BigDecimal bdAmount = Convert.toBigDecimal(7123.45);
 		assertEquals("$7,123", Format.currency(bdAmount, false));
 		assertEquals("$7,123.45", Format.currency(bdAmount, true));
 	}
 	
 	@Test
 	public void stripNonNumericTest()
 	{
 		assertEquals("3000", Format.stripNonNumeric("$3,000"));
 		assertEquals("3000000", Format.stripNonNumeric("3,000,000"));
 	}
 	
 	@Test
 	public void upperCaseFirstCharTest()
 	{
 		assertEquals("Mark", Format.upperCaseFirstChar("mark"));
 	}
 	
 	@Test
 	public void stripExtraSpacesTest()
 	{
 		assertEquals("Mark Jacobsen", Format.stripExtraSpaces("Mark  Jacobsen"));
 	}
 	
 	@Test
 	public void maxLenStringTest()
 	{
 		String test = "hi there";
 		assertEquals(test, Format.maxLenString(test, 5000));
 		assertEquals("hi", Format.maxLenString(test, 2));
 		Assert.assertNull(Format.maxLenString(null, 20));
 	}
 	
 	@Test
 	public void formatPhone()
 	{
 		String expected = "517-803-2254";
 		assertEquals(expected, Format.phoneNumber(Format.PHONE_DASH, "517-803-2254"));
 		assertEquals(expected, Format.phoneNumber(Format.PHONE_DASH, "5178032254"));
 		assertEquals(expected, Format.phoneNumber(Format.PHONE_DASH, "(517) 803-2254"));
 		assertEquals(expected, Format.phoneNumber(Format.PHONE_DASH, "+15178032254"));
 		assertEquals(expected, Format.phoneNumber(Format.PHONE_DASH, "1-517-803-2254"));
 		
 		expected = "517.803.2254";
 		assertEquals(expected, Format.phoneNumber(Format.PHONE_DOT, "517-803-2254"));
 		assertEquals(expected, Format.phoneNumber(Format.PHONE_DOT, "5178032254"));
 		assertEquals(expected, Format.phoneNumber(Format.PHONE_DOT, "(517) 803-2254"));
 		assertEquals(expected, Format.phoneNumber(Format.PHONE_DOT, "+15178032254"));
 		assertEquals(expected, Format.phoneNumber(Format.PHONE_DOT, "1-517-803-2254"));
 	}
 	
 	@Test
 	public void upperCaseFirstCharAllWords()
 	{
 		assertNull(Format.upperCaseFirstCharAllWords(null));
 		assertEquals("", Format.upperCaseFirstCharAllWords(""));
 		assertEquals("", Format.upperCaseFirstCharAllWords(" "));
 		assertEquals("The", Format.upperCaseFirstCharAllWords("the"));
 		assertEquals("The", Format.upperCaseFirstCharAllWords(" the "));
 		assertEquals("The Blue Goose Flies At Dawn", Format.upperCaseFirstCharAllWords("the Blue goose flies at dawn"));
 		assertEquals("Hi There", Format.upperCaseFirstCharAllWords("hi  there "));
 		assertEquals("I Live At 123 Main St.", Format.upperCaseFirstCharAllWords("i live at 123 main st."));
 	}
 }
