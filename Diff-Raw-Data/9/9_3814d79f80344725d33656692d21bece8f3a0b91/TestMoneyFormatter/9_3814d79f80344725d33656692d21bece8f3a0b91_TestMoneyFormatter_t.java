 package com.acmetelecom;
 
 import java.math.BigDecimal;
import java.text.DecimalFormatSymbols;
 
 import junit.framework.TestCase;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 
 public class TestMoneyFormatter extends TestCase {
 
 	@Before
 	public void setUp() throws Exception {
 	}
 
 	@After
 	public void tearDown() throws Exception {
 	}
 
 	@Test
 	public void testPenceToPounds() {
//      This test needs to cater for different decimal symbols used in various locales
        DecimalFormatSymbols formatSymbols = new DecimalFormatSymbols();
        char separator = formatSymbols.getMonetaryDecimalSeparator();

        String expected = "0" + separator + "10";
		assertEquals(expected, MoneyFormatter.penceToPounds(new BigDecimal(10.1)));
 	}
 
 }
