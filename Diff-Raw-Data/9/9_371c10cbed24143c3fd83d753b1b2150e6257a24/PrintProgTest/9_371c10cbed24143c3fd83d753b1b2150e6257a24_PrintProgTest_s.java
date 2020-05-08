 /**
  * 
  */
 package swp_compiler_ss13.fuc.lexer.milestone.m2;
 
 import swp_compiler_ss13.common.lexer.BoolToken;
 import swp_compiler_ss13.common.lexer.NumToken;
 import swp_compiler_ss13.common.lexer.RealToken;
 import swp_compiler_ss13.common.lexer.Token;
 import swp_compiler_ss13.common.lexer.TokenType;
 import swp_compiler_ss13.fuc.lexer.LexerImpl;
 import swp_compiler_ss13.fuc.lexer.token.BoolTokenImpl;
 import swp_compiler_ss13.fuc.lexer.token.NumTokenImpl;
 import swp_compiler_ss13.fuc.lexer.token.RealTokenImpl;
 import swp_compiler_ss13.fuc.lexer.token.TokenImpl;
 import static org.junit.Assert.*;
 
 import java.io.ByteArrayInputStream;
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.Arrays;
 import org.junit.Before;
 import org.junit.Ignore;
 import org.junit.Test;
 
 /**
  * @author Tay, Ho Phuong
  * 
  */
 public class PrintProgTest {
 	private String prog =
 		"# return 0\n" +
 		"# prints:\n" +
 		"# true\n" +
 		"# 18121313223\n" +
 		"# -2.323e-99\n" +
 		"# jagÄrEttString\"\n" +
 		"\n" +
 		"long l;\n" +
 		"double d;\n" +
 		"string s;\n" +
 		"bool b;\n" +
 		"\n" +
 		"string linebreak;\n" +
 		"linebreak = \"\\n\"\n" +
 		"		\n" +
 		"b = true;\n" +
 		"l = 18121313223;\n" +
 		"d = -23.23e-100;\n" +
 		"s = \"jagÄrEttString\\\"\\n\";  # c-like escaping in strings\n" +
 		"\n" +
 		"print b; print linebreak;\n" +
 		"print l; print linebreak;       # print one digit left of the radix point\n" +
 		"print d; print linebreak;\n" +
 		"print s;\n" +
 		"\n" +
 		"return;                    # equivalent to return EXIT_SUCCESS";
 	private InputStream stream;
 	private LexerImpl lexer;
 	private ArrayList<Token> list;
 
 	@Before
 	public void setUp() throws Exception {
 		this.stream = new ByteArrayInputStream(prog.getBytes());
 		this.lexer = new swp_compiler_ss13.fuc.lexer.LexerImpl();
 		this.lexer.setSourceStream(this.stream);
 		this.list = new ArrayList<Token>(Arrays.asList(
 			new TokenImpl("# return 0", TokenType.COMMENT, 1, 1),
 			new TokenImpl("# prints:", TokenType.COMMENT, 2, 1),
 			new TokenImpl("# true", TokenType.COMMENT, 3, 1),
 			new TokenImpl("# 18121313223", TokenType.COMMENT, 4, 1),
 			new TokenImpl("# -2.323e-99", TokenType.COMMENT, 5, 1),
 			new TokenImpl("# jagÄrEttString\"", TokenType.COMMENT, 6, 1),
 			new TokenImpl("long", TokenType.LONG_SYMBOL, 8, 1),
 			new TokenImpl("l", TokenType.ID, 8, 6),
 			new TokenImpl(";", TokenType.SEMICOLON, 8, 7),
 			new TokenImpl("double", TokenType.DOUBLE_SYMBOL, 9, 1),
 			new TokenImpl("d", TokenType.ID, 9, 8),
 			new TokenImpl(";", TokenType.SEMICOLON, 9, 9),
 			new TokenImpl("string", TokenType.STRING_SYMBOL, 10, 1),
 			new TokenImpl("s", TokenType.ID, 10, 8),
 			new TokenImpl(";", TokenType.SEMICOLON, 10, 9),
 			new TokenImpl("bool", TokenType.BOOL_SYMBOL, 11, 1),
 			new TokenImpl("b", TokenType.ID, 11, 6),
 			new TokenImpl(";", TokenType.SEMICOLON, 11, 7),
 			new TokenImpl("string", TokenType.STRING_SYMBOL, 13, 1),
 			new TokenImpl("linebreak", TokenType.ID, 13, 8),
 			new TokenImpl(";", TokenType.SEMICOLON, 13, 17),
 			new TokenImpl("linebreak", TokenType.ID, 14, 1),
 			new TokenImpl("=", TokenType.ASSIGNOP, 14, 11),
 			new TokenImpl("\"\\n\"", TokenType.STRING, 14, 13),
 			new TokenImpl("b", TokenType.ID, 16, 1),
 			new TokenImpl("=", TokenType.ASSIGNOP, 16, 3),
 			new TokenImpl("true", TokenType.TRUE, 16, 5),
 			new TokenImpl(";", TokenType.SEMICOLON, 16, 9),
 			new TokenImpl("l", TokenType.ID, 17, 1),
 			new TokenImpl("=", TokenType.ASSIGNOP, 17, 3),
 			new TokenImpl("18121313223", TokenType.NUM, 17, 5),
 			new TokenImpl(";", TokenType.SEMICOLON, 17, 16),
 			new TokenImpl("d", TokenType.ID, 18, 1),
 			new TokenImpl("=", TokenType.ASSIGNOP, 18, 3),
 			new TokenImpl("-23.23e-100", TokenType.REAL, 18, 5),
 			new TokenImpl(";", TokenType.SEMICOLON, 18, 16),
 			new TokenImpl("s", TokenType.ID, 19, 1),
 			new TokenImpl("=", TokenType.ASSIGNOP, 19, 3),
 			new TokenImpl("\"jagÄrEttString\\\"\\n\"", TokenType.STRING, 19, 5),
 			new TokenImpl(";", TokenType.SEMICOLON, 19, 25),
 			new TokenImpl("# c-like escaping in strings", TokenType.COMMENT, 19, 28),
 			new TokenImpl("print", TokenType.PRINT, 21, 1),
 			new TokenImpl("b", TokenType.ID, 21, 7),
 			new TokenImpl(";", TokenType.SEMICOLON, 21, 8),
 			new TokenImpl("print", TokenType.PRINT, 21, 10),
 			new TokenImpl("linebreak", TokenType.ID, 21, 16),
 			new TokenImpl(";", TokenType.SEMICOLON, 21, 25),
 			new TokenImpl("print", TokenType.PRINT, 22, 1),
 			new TokenImpl("l", TokenType.ID, 22, 7),
 			new TokenImpl(";", TokenType.SEMICOLON, 22, 8),
 			new TokenImpl("print", TokenType.PRINT, 22, 10),
 			new TokenImpl("linebreak", TokenType.ID, 22, 16),
 			new TokenImpl(";", TokenType.SEMICOLON, 22, 25),
 			new TokenImpl("# print one digit left of the radix point", TokenType.COMMENT, 22, 33),
 			new TokenImpl("print", TokenType.PRINT, 23, 1),
 			new TokenImpl("d", TokenType.ID, 23, 7),
 			new TokenImpl(";", TokenType.SEMICOLON, 23, 8),
 			new TokenImpl("print", TokenType.PRINT, 23, 10),
 			new TokenImpl("linebreak", TokenType.ID, 23, 16),
 			new TokenImpl(";", TokenType.SEMICOLON, 23, 25),
 			new TokenImpl("print", TokenType.PRINT, 24, 1),
 			new TokenImpl("s", TokenType.ID, 24, 7),
 			new TokenImpl(";", TokenType.SEMICOLON, 24, 8),
 			new TokenImpl("return", TokenType.RETURN, 26, 1),
 			new TokenImpl(";", TokenType.SEMICOLON, 26, 7),
 			new TokenImpl("# equivalent to return EXIT_SUCCESS", TokenType.COMMENT, 26, 28),
 			new TokenImpl("$", TokenType.EOF, 27, 1)
 		));
 	}
 
	@Ignore
 	@Test
 	public void testgetNextToken() {
 		Token token = null;
 		Token comparisonToken = null;
 
 		do {
 			comparisonToken = list.remove(0);
 			token = this.lexer.getNextToken();
 
 			assertTrue(token != null);
 			assertEquals(comparisonToken.getValue(), token.getValue());		
 			assertEquals(comparisonToken.getTokenType(), token.getTokenType());
 			
 			if (token.getTokenType().equals(TokenType.NUM)) {
 
 				NumToken comparisonNumToken = new NumTokenImpl(
 						comparisonToken.getValue(), null, null, null);
 				NumToken numToken = new NumTokenImpl(token.getValue(), null,
 						null, null);
 				assertEquals(comparisonNumToken.getLongValue(),
 						numToken.getLongValue());
 
 			} else if (token.getTokenType().equals(TokenType.REAL)) {
 
 				RealToken comparisonRealToken = new RealTokenImpl(
 						comparisonToken.getValue(), null, null, null);
 				RealToken realToken = new RealTokenImpl(token.getValue(), null,
 						null, null);
 				assertEquals(comparisonRealToken.getDoubleValue(),
 						realToken.getDoubleValue());
 
 			} else if (token.getTokenType().equals(TokenType.TRUE) 
 					|| token.getTokenType().equals(TokenType.FALSE)) {
 
 				BoolToken comparisonBoolToken = new BoolTokenImpl(
 						comparisonToken.getValue(), null, null, null);
 				BoolToken boolToken = new BoolTokenImpl(token.getValue(), null,
 						null, null);
 				assertEquals(comparisonBoolToken.getBooleanValue(),
 						boolToken.getBooleanValue());
 
 			}
 
 		} while (token.getTokenType() != TokenType.EOF);
 	}
 
 }
