 package calc;
 
 import static org.junit.Assert.*;
 import java.util.*;
 
import org.junit.After;
import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.AfterClass;
 import org.junit.Test;
 
 
 
 public class CurrencyXMLParserTest {
 
	private static TreeMap<String, Long> currencyhash;
 	private static CurrencyXMLParser parser;
 
 	@BeforeClass 
 	public static void setUpBeforeClass() throws Exception {
 		parser = new CurrencyXMLParser();
 		currencyhash = parser.getCurrencyList();
 	}
 
 	@AfterClass
 	public static void tearDownAfterClass() throws Exception {
 	}
 
 	@Test
 	public void testGetCurrencyList() {
 		assertTrue(currencyhash.containsKey("CNY"));
 	}
 
 	@Test
 	public void testConvertCurrency() {
		assertTrue(parser.convertCurrency(1.0,"EUR","CNY") == (Math.round(currencyhash.get("CNY") * 100)/100));
 	}
 
 }
