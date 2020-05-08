 package edu.luc.clearing;
 
 import static org.junit.Assert.assertEquals;
 
 import org.junit.Before;
 import org.junit.Test;
 
 public class CheckParserTest {
 	private CheckParser parser;
 	
 	@Before
 	public void setup(){
 		parser = new CheckParser();
 	}
 
 	@Test
 	public void shouldIgnoreCase() throws Exception {
 		assertEquals(100, parser.parseExpression("one").intValue());
 		assertEquals(200, parser.parseExpression("TWO").intValue());
 		assertEquals(300, parser.parseExpression("Three").intValue());
 		assertEquals(1000, parser.parseExpression("TEN").intValue());
 	}
 
 	@Test
 	public void shouldHandleZeroAmount() throws Exception {
 		assertEquals(0, parser.parseExpression("zero").intValue());
 	}
 
 	@Test
 	public void shouldIgnoreSpace() throws Exception {
 		assertEquals(400, parser.parseExpression("Four ").intValue());
 		assertEquals(1400, parser.parseExpression(" fourteen ").intValue());
 	}
 	
 	@Test
 	public void shouldMatchWithDollars() throws Exception {
 		assertEquals(100, parser.parseExpression("One dollar").intValue());
 		assertEquals(200, parser.parseExpression("two dollars").intValue());
 		assertEquals(300, parser.parseExpression("three  dollars").intValue());
 		assertEquals(8000, parser.parseExpression(" Eighty  dollars  ").intValue());
 		assertEquals(7600, parser.parseExpression(" SEVENTY SIX  dollars  ").intValue());
 	}
 	
 	@Test
 	public void shouldReturnNullOnlyDollar() throws Exception{
 		assertEquals(null, parser.parseExpression("dollar"));
 	}
 	
 	@Test
 	public void shouldMatchWithTwoDigits() throws Exception{
 		assertEquals(2100, parser.parseExpression("twenty one").intValue());
 		assertEquals(7200, parser.parseExpression("seventy two").intValue());
 		assertEquals(5300, parser.parseExpression("  fifty   three ").intValue());
 	}
 
 	@Test
 	public void shouldReturnNullIfTwoDigitsReverse() throws Exception{
 		assertEquals(null, parser.parseExpression("ten ten"));
 		assertEquals(null, parser.parseExpression("five twenty"));
 		assertEquals(null, parser.parseExpression("seventy eighty"));
 	}
 	
 	@Test
 	public void shouldMatchOnlyCents()  throws Exception{
 		assertEquals(87, parser.parseExpression("87/100").intValue());
 		assertEquals(0, parser.parseExpression("0/100").intValue());
 		//invalid cents
 		assertEquals(null, parser.parseExpression("10"));
 	}
 	
 	@Test
 	public void shouldMatchWithAndCents()  throws Exception{
 		assertEquals(444, parser.parseExpression("four and 44/100").intValue());
 		assertEquals(1782, parser.parseExpression("seventeenand82/100").intValue());
 		assertEquals(9099, parser.parseExpression("ninety and 99/100").intValue());
 		assertEquals(9999, parser.parseExpression("ninety nine and 99/100").intValue());
 	}
 	
 	
 
 }
