 package cs444;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertNull;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.StringReader;
 
 import cs444.lexer.Lexer;
 import cs444.lexer.Token;
 import cs444.lexer.Token.Type;
 
 public class TestHelper {
 	public static void assertTokenFor(String string, Type tokenType) throws Exception {
 		Lexer scanner = getScannerFor(string);
 
 		Token token = scanner.getNextToken();
 		assertToken(tokenType, string, token);
 		assertNull(scanner.getNextToken());
 	}
 
 	// Used mainly to test the scanner throws exceptions for lexically invalid strings
 	public static void scanString(String string) throws Exception {
 		Lexer scanner = getScannerFor(string);
 		while (null != scanner.getNextToken()){}
 	}
 
 	public static Lexer getScannerFor(String input) throws IOException {
 		BufferedReader reader = new BufferedReader(new StringReader(input));
 
 		return new Lexer(reader);
 	}
 
 	public static void assertToken(Token.Type type, String lexeme, Token token){
 		String failMessage = "Unexpected token: " + token;
		assertEquals(failMessage, type, token.getType());
		assertEquals(failMessage, lexeme, token.getLexeme());
 	}
 
 	// useful for debugging
 	public static void printAllTokensFor(String string) throws Exception{
 		Lexer scanner = getScannerFor(string);
 		Token token;
 		while (null != (token = scanner.getNextToken())) {
 			System.out.println(token.toString());
 		}
 	}
 }
