 /**
  * 
  */
 package yucatan.communication.presentation;
 
 import static org.junit.Assert.assertEquals;
 
 import java.util.ArrayList;
 
 import org.junit.Test;
 
 /**
  * Tests for class TemplateTokenizer.
  */
 public class TestTemplateTokenizer {
 
 	/**
 	 * Test method for {@link yucatan.communication.presentation.TemplateProcessor#getTokens(java.lang.Object, java.lang.String)}.
 	 */
 	@Test
 	public void testGetTokensSimplePlaceholders1() {
 
 		// test simple placholders
 		// - one placeholder at the end of string
 		// - another placeholder inbetween
 		// * call getTokens() -> general expected result
 		String template = "<html><body><h1>${@odd}</h1><ul>${@count}";
 
 		// same test string but this time have a look at the tokens
 		// * call getTokens() -> analyze tokens
 		ArrayList<TemplateToken> tokens = TemplateTokenizer.getTokens(template);
 		TemplateToken token0 = tokens.get(0);
 		TemplateToken token1 = tokens.get(1);
 		TemplateToken token2 = tokens.get(2);
 		TemplateToken token3 = tokens.get(3);
 		TemplateToken token4 = tokens.get(4);
 		TemplateToken token5 = tokens.get(5);
 
 		assertEquals(TemplateToken.TOKENTYPE_TEXT, token0.tokenType);
 		assertEquals("<html><body><h1>", token0.plainText);
 
 		assertEquals(TemplateToken.TOKENTYPE_ACTIONNAME, token1.tokenType);
 		assertEquals("odd", token1.plainText);
 
 		assertEquals(TemplateToken.TOKENTYPE_PLACEHOLDEREND, token2.tokenType);
 
 		assertEquals(TemplateToken.TOKENTYPE_TEXT, token3.tokenType);
 		assertEquals("</h1><ul>", token3.plainText);
 
 		assertEquals(TemplateToken.TOKENTYPE_ACTIONNAME, token4.tokenType);
 		assertEquals("count", token4.plainText);
 
 		assertEquals(TemplateToken.TOKENTYPE_PLACEHOLDEREND, token5.tokenType);
 
 		assertEquals(6, tokens.size());
 	}
 
 
 	/**
 	 * Test method for {@link yucatan.communication.presentation.TemplateProcessor#getTokens(java.lang.Object, java.lang.String)}.
 	 */
 	@Test
 	public void testGetTokensSimplePlaceholders2() {
 		// simple placeholders at start position
 		// - one placeholder at the beginning of string
 		// - one placeholder at the end of string
 		// * getTokens -> general expected result
 		String template = "${@odd}<html><ul>${@data}";
 
 		// same test string but this time have a look at the tokens
 		// * getTokens -> analyze tokens
 		ArrayList<TemplateToken> tokens = TemplateTokenizer.getTokens(template);
 		TemplateToken token0 = tokens.get(0);
 		TemplateToken token1 = tokens.get(1);
 		TemplateToken token2 = tokens.get(2);
 		TemplateToken token3 = tokens.get(3);
 		TemplateToken token4 = tokens.get(4);
 		TemplateToken token5 = tokens.get(5);
 
 		assertEquals(TemplateToken.TOKENTYPE_TEXT, token0.tokenType);
 		assertEquals("", token0.plainText); // empty token
 
 		assertEquals(TemplateToken.TOKENTYPE_ACTIONNAME, token1.tokenType);
 		assertEquals("odd", token1.plainText);
 
 		assertEquals(TemplateToken.TOKENTYPE_PLACEHOLDEREND, token2.tokenType);
 
 		assertEquals(TemplateToken.TOKENTYPE_TEXT, token3.tokenType);
 		assertEquals("<html><ul>", token3.plainText);
 
 		assertEquals(TemplateToken.TOKENTYPE_ACTIONNAME, token4.tokenType);
 		assertEquals("data", token4.plainText);
 
 		assertEquals(TemplateToken.TOKENTYPE_PLACEHOLDEREND, token5.tokenType);
 
 		assertEquals(6, tokens.size());
 	}
 
 	/**
 	 * Test method for {@link yucatan.communication.presentation.TemplateProcessor#getTokens(java.lang.Object, java.lang.String)}.
 	 */
 	@Test
 	public void testGetTokensSimplePlaceholders3() {
 
 		// simple placeholder after an simple placeholder
 		// - two directly following simple placeholders -
 		String template = "<html><body><h1>${@odd}${@count}</h1><ul>";
 
 		// same test string but this time have a look at the tokens
 		// * getTokens -> analyze tokens
 		ArrayList<TemplateToken> tokens = TemplateTokenizer.getTokens(template);
 		TemplateToken token0 = tokens.get(0);
 		TemplateToken token1 = tokens.get(1);
 		TemplateToken token2 = tokens.get(2);
 		TemplateToken token3 = tokens.get(3);
 		TemplateToken token4 = tokens.get(4);
 		TemplateToken token5 = tokens.get(5);
 		TemplateToken token6 = tokens.get(6);
 
 		assertEquals(TemplateToken.TOKENTYPE_TEXT, token0.tokenType);
 		assertEquals("<html><body><h1>", token0.plainText);
 
 		assertEquals(TemplateToken.TOKENTYPE_ACTIONNAME, token1.tokenType);
 		assertEquals("odd", token1.plainText);
 
 		assertEquals(TemplateToken.TOKENTYPE_PLACEHOLDEREND, token2.tokenType);
 
 		assertEquals(TemplateToken.TOKENTYPE_TEXT, token3.tokenType);
 		assertEquals("", token3.plainText);
 
 		assertEquals(TemplateToken.TOKENTYPE_ACTIONNAME, token4.tokenType);
 		assertEquals("count", token4.plainText);
 
 		assertEquals(TemplateToken.TOKENTYPE_PLACEHOLDEREND, token5.tokenType);
 
 		assertEquals(TemplateToken.TOKENTYPE_TEXT, token6.tokenType);
 		assertEquals("</h1><ul>", token6.plainText);
 
 		assertEquals(7, tokens.size());
 	}
 
 	/**
 	 * Test method for {@link yucatan.communication.presentation.TemplateProcessor#getTokens(java.lang.Object, java.lang.String)}.
 	 */
 	@Test
 	public void testGetTokensSimplePlaceholders4() {
 		// simple placeholder after an simple placeholder
 		// - seperated by one character
 		String template = "<h1>${@odd}-${@count}</h1>";
 
 		// same test string but this time have a look at the tokens
 		// * getTokens -> analyze tokens
 		ArrayList<TemplateToken> tokens = TemplateTokenizer.getTokens(template);
 		TemplateToken token0 = tokens.get(0);
 		TemplateToken token1 = tokens.get(1);
 		TemplateToken token2 = tokens.get(2);
 		TemplateToken token3 = tokens.get(3);
 		TemplateToken token4 = tokens.get(4);
 		TemplateToken token5 = tokens.get(5);
 		TemplateToken token6 = tokens.get(6);
 
 		assertEquals(TemplateToken.TOKENTYPE_TEXT, token0.tokenType);
 		assertEquals("<h1>", token0.plainText);
 
 		assertEquals(TemplateToken.TOKENTYPE_ACTIONNAME, token1.tokenType);
 		assertEquals("odd", token1.plainText);
 
 		assertEquals(TemplateToken.TOKENTYPE_PLACEHOLDEREND, token2.tokenType);
 
 		assertEquals(TemplateToken.TOKENTYPE_TEXT, token3.tokenType);
 		assertEquals("-", token3.plainText);
 
 		assertEquals(TemplateToken.TOKENTYPE_ACTIONNAME, token4.tokenType);
 		assertEquals("count", token4.plainText);
 
 		assertEquals(TemplateToken.TOKENTYPE_PLACEHOLDEREND, token5.tokenType);
 
 		assertEquals(TemplateToken.TOKENTYPE_TEXT, token6.tokenType);
 		assertEquals("</h1>", token6.plainText);
 
 		assertEquals(7, tokens.size());
 	}
 
 	/**
 	 * Test method for {@link yucatan.communication.presentation.TemplateProcessor#getTokens(java.lang.Object, java.lang.String)}.
 	 */
 	@Test
 	public void testGetTokensSimplePlaceholders5() {
 		// invalid placeholder format
 		String template = "<h1>${other-placeholder}</h1>";
 
 		// same test string but this time have a look at the tokens
 		// * getTokens -> analyze tokens
 		ArrayList<TemplateToken> tokens = TemplateTokenizer.getTokens(template);
 		TemplateToken token0 = tokens.get(0);
 		TemplateToken token1 = tokens.get(1);
 		TemplateToken token2 = tokens.get(2);
 
 		assertEquals(TemplateToken.TOKENTYPE_TEXT, token0.tokenType);
 		assertEquals("<h1>", token0.plainText);
 
 		assertEquals(TemplateToken.TOKENTYPE_TEXT, token1.tokenType);
 		assertEquals("$", token1.plainText);
 
 		assertEquals(TemplateToken.TOKENTYPE_TEXT, token2.tokenType);
 		assertEquals("{other-placeholder}</h1>", token2.plainText);
 
 		assertEquals(3, tokens.size());
 	}
 
 	/**
 	 * Test method for {@link yucatan.communication.presentation.TemplateProcessor#getTokens(java.lang.Object, java.lang.String)}.
 	 */
 	@Test
 	public void testGetTokensSimplePlaceholders6() {
 		// invalid placeholder followed by another invalid placeholder
 		String template = "<h1>${other-placeholder}${other-placeholder}</h1>";
 
 		// same test string but this time have a look at the tokens
 		// * getTokens -> analyze tokens
 		ArrayList<TemplateToken> tokens = TemplateTokenizer.getTokens(template);
 		TemplateToken token0 = tokens.get(0);
 		TemplateToken token1 = tokens.get(1);
 		TemplateToken token2 = tokens.get(2);
 		TemplateToken token3 = tokens.get(3);
 		TemplateToken token4 = tokens.get(4);
 
 		assertEquals(TemplateToken.TOKENTYPE_TEXT, token0.tokenType);
 		assertEquals("<h1>", token0.plainText);
 
 		assertEquals(TemplateToken.TOKENTYPE_TEXT, token1.tokenType );
 		assertEquals("$", token1.plainText);
 
 		assertEquals(TemplateToken.TOKENTYPE_TEXT, token2.tokenType );
 		assertEquals("{other-placeholder}", token2.plainText);
 
 		assertEquals(TemplateToken.TOKENTYPE_TEXT, token3.tokenType);
 		assertEquals("$", token3.plainText);
 
 		assertEquals(TemplateToken.TOKENTYPE_TEXT, token4.tokenType );
 		assertEquals("{other-placeholder}</h1>", token4.plainText);
 
 		assertEquals(5, tokens.size());
 	}
 
 	/**
 	 * Test method for {@link yucatan.communication.presentation.TemplateProcessor#getTokens(java.lang.Object, java.lang.String)}.
 	 */
 	@Test
 	public void testGetTokensSimplePlaceholders7() {
 		// invalid placeholder followed by a valid placeholder
 		String template = "<h1>${other-placeholder}${@odd}</h1>";
 
 		// same test string but this time have a look at the tokens
 		// * getTokens -> analyze tokens
 		ArrayList<TemplateToken> tokens = TemplateTokenizer.getTokens(template);
 		TemplateToken token0 = tokens.get(0);
 		TemplateToken token1 = tokens.get(1);
 		TemplateToken token2 = tokens.get(2);
 		TemplateToken token3 = tokens.get(3);
 		TemplateToken token4 = tokens.get(4);
 		TemplateToken token5 = tokens.get(5);
 
 		assertEquals(TemplateToken.TOKENTYPE_TEXT, token0.tokenType);
 		assertEquals("<h1>", token0.plainText);
 
 		assertEquals(TemplateToken.TOKENTYPE_TEXT, token1.tokenType);
 		assertEquals("$", token1.plainText);
 
 		assertEquals(TemplateToken.TOKENTYPE_TEXT, token2.tokenType);
 		assertEquals("{other-placeholder}", token2.plainText);
 
 		assertEquals(TemplateToken.TOKENTYPE_ACTIONNAME, token3.tokenType);
 		assertEquals("odd", token3.plainText);
 
 		assertEquals(TemplateToken.TOKENTYPE_PLACEHOLDEREND, token4.tokenType);
 
 		assertEquals(TemplateToken.TOKENTYPE_TEXT, token5.tokenType);
 		assertEquals("</h1>", token5.plainText);
 
 		assertEquals(6, tokens.size());
 	}
 
 	/**
 	 * Test method for {@link yucatan.communication.presentation.TemplateProcessor#getTokens(java.lang.Object, java.lang.String)}.
 	 */
 	@Test
 	public void testGetTokensSimplePlaceholders8() {
 		// valid placeholder within an invalid placeholder
 		String template = "<h1>${ ${@odd} }</h1>";
 
 		// same test string but this time have a look at the tokens
 		// * getTokens -> analyze tokens
 		ArrayList<TemplateToken> tokens = TemplateTokenizer.getTokens(template);
 		TemplateToken token0 = tokens.get(0);
 		TemplateToken token1 = tokens.get(1);
 		TemplateToken token2 = tokens.get(2);
 		TemplateToken token3 = tokens.get(3);
 		TemplateToken token4 = tokens.get(4);
 		TemplateToken token5 = tokens.get(5);
 
 		assertEquals(TemplateToken.TOKENTYPE_TEXT, token0.tokenType);
 		assertEquals("<h1>", token0.plainText);
 
 		assertEquals(TemplateToken.TOKENTYPE_TEXT, token1.tokenType);
 		assertEquals("$", token1.plainText);
 
 		assertEquals(TemplateToken.TOKENTYPE_TEXT, token2.tokenType);
 		assertEquals("{ ", token2.plainText);
 
 		assertEquals(TemplateToken.TOKENTYPE_ACTIONNAME, token3.tokenType);
 		assertEquals("odd", token3.plainText);
 
 		assertEquals(TemplateToken.TOKENTYPE_PLACEHOLDEREND, token4.tokenType);
 
 		assertEquals(TemplateToken.TOKENTYPE_TEXT, token5.tokenType);
 		assertEquals(" }</h1>", token5.plainText);
 
 		assertEquals(6, tokens.size());
 	}
 
 	/**
 	 * Test method for {@link yucatan.communication.presentation.TemplateProcessor#getTokens(java.lang.Object, java.lang.String)}.
 	 */
 	@Test
 	public void testGetTokensSimplePlaceholders9() {
 		// valid placeholder within an invalid placeholder
 		String template = "<h1>${${@odd}}</h1>";
 
 		// same test string but this time have a look at the tokens
 		// * getTokens -> analyze tokens
 		ArrayList<TemplateToken> tokens = TemplateTokenizer.getTokens(template);
 		TemplateToken token0 = tokens.get(0);
 		TemplateToken token1 = tokens.get(1);
 		TemplateToken token2 = tokens.get(2);
 		TemplateToken token3 = tokens.get(3);
 		TemplateToken token4 = tokens.get(4);
 		TemplateToken token5 = tokens.get(5);
 
 		assertEquals(TemplateToken.TOKENTYPE_TEXT, token0.tokenType);
 		assertEquals("<h1>", token0.plainText);
 
 		assertEquals(TemplateToken.TOKENTYPE_TEXT, token1.tokenType);
 		assertEquals("$", token1.plainText);
 
 		assertEquals(TemplateToken.TOKENTYPE_TEXT, token2.tokenType);
 		assertEquals("{", token2.plainText);
 
 		assertEquals(TemplateToken.TOKENTYPE_ACTIONNAME, token3.tokenType);
 		assertEquals("odd", token3.plainText);
 
 		assertEquals(TemplateToken.TOKENTYPE_PLACEHOLDEREND, token4.tokenType);
 
 		assertEquals("}</h1>", token5.plainText);
 		assertEquals(TemplateToken.TOKENTYPE_TEXT, token5.tokenType);
 
 		assertEquals(6, tokens.size());
 	}
 
 	/**
 	 * Test method for {@link yucatan.communication.presentation.TemplateProcessor#getTokens(java.lang.Object, java.lang.String)}.
 	 */
 	@Test
 	public void testGetTokensSimplePlaceholders10() {
 		// valid placeholder within an invalid placeholder
 		String template = "<h1>${@data(key1)}</h1>";
 
 		// same test string but this time have a look at the tokens
 		// * getTokens -> analyze tokens
 		ArrayList<TemplateToken> tokens = TemplateTokenizer.getTokens(template);
 		TemplateToken token0 = tokens.get(0);
 		TemplateToken token1 = tokens.get(1);
 		TemplateToken token2 = tokens.get(2);
 		TemplateToken token3 = tokens.get(3);
		TemplateToken token4 = tokens.get(4);
 
 		assertEquals(TemplateToken.TOKENTYPE_TEXT, token0.tokenType);
 		assertEquals("<h1>", token0.plainText);
 
 		assertEquals(TemplateToken.TOKENTYPE_ACTIONNAME, token1.tokenType);
 		assertEquals("data", token1.plainText);
 
 		assertEquals(TemplateToken.TOKENTYPE_MEMBERQUERY, token2.tokenType);
 		assertEquals("key1", token2.plainText);
 
 		assertEquals(TemplateToken.TOKENTYPE_PLACEHOLDEREND, token3.tokenType);
 
		assertEquals("</h1>", token4.plainText);
		assertEquals(TemplateToken.TOKENTYPE_TEXT, token4.tokenType);
 
 		assertEquals(5, tokens.size());
 	}
 
 }
