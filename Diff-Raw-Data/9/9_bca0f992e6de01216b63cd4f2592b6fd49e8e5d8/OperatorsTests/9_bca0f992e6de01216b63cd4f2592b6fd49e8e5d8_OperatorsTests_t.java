 package scanner;
 
 import htmlscanner.Token;
 import htmlscanner.TokenType;
 
 import java.util.Arrays;
 import java.util.List;
 
 public class OperatorsTests extends AutomatonTests {
 
 	public void testOperatorsOnly() {
 
 		ScanResult result = scan("++ += + - = / * ^ && || ( ) [ ] { } %");
 		List<Token> tokens = Arrays.asList(new Token(TokenType.POSTINCREMENT,
 				null),  new Token(
 				TokenType.SUMASSIGN, null), 
 				new Token(TokenType.SUM, null), 
				new Token(TokenType.DIFFERENCE, null), new Token(TokenType.ASSIGNATION, null),  new Token(TokenType.DIVISION,
 						null),  new Token(
 						TokenType.MULTIPLICATION, null), new Token(TokenType.POWER, null),
 				 new Token(TokenType.LOGIC_AND,
 						null),  new Token(
 						TokenType.LOGIC_OR, null),  new Token(TokenType.O_P, null), new Token(TokenType.C_P, null),
 				 new Token(TokenType.O_BRACKET,
 						null),  new Token(
 						TokenType.C_BRACKET, null), new Token(TokenType.O_BRACE, null),
 				new Token(TokenType.C_BRACE, null), new Token(TokenType.PCT, null));
 		assertTrue(String.format("tokens esperado: %s\ntokens resultado: %s",
 				tokens, result.tokens), isEqual(result.getTokens(), tokens));
 		System.out.println(result.tokens);
 	}
 
 	public void testOperatorsWithErrors() {
 		setLog(true);
 
 		ScanResult result = scan(" & | ");
 		List<Token> tokens = Arrays.asList();
 		System.out.println(result.tokens);
 		System.out.println(result.errors);
 		assertTrue(String.format("tokens esperado: %s\ntokens resultado: %s",
 				tokens, result.tokens), isEqual(result.getTokens(), tokens));
 		assertEquals(result.errors.size(), 4);
 
 	}
 }
