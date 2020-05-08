 package io.scan;
 
import java.io.IOException;
 import java.io.File;
 import java.io.FileNotFoundException;
 
 /**
  * The class performs line reconstruction and rearranges the input
  * character sequence into a canonical form ready for the parser.
  * <p/>
  * todo: More efficient PslScanner algorithm:
  * todo: 1) read the stream until ';' is met, or throw an Exception
  * todo:    if it is not met
  * todo: 2) replace incorrect ranges with correct using regexp-s:
  * todo:    e.g. line.replace("\\s+", " ");
  *
  * @author Anton Chepurov
  */
 public class PSLScanner {
 	private LexemeComposer lexemeComposer;
 
 	public PSLScanner(String sourceLine) {
 		lexemeComposer = new LexemeComposer(sourceLine, true);
 	}
 
 	public PSLScanner(File sourceFile) throws FileNotFoundException {
 		this.lexemeComposer = new LexemeComposer(sourceFile, true);
 	}
 
 	/**
 	 * Reads next property line from source.
 	 *
 	 * @return rearranged (whitespace-s) property line or <code>null</code> when EOF reached
 	 * @throws Exception if {@link LexemeComposer#nextLexeme()}, {@link LexemeComposer#close()}
	 * 					 or malformed (unclosed) token is read
 	 */
 	public String next() throws Exception {
 		StringBuffer token = new StringBuffer();
 		Lexeme lexeme;
 
 		while (true) {
 			/* Get NEXT LEXEME */
 			lexeme = lexemeComposer.nextLexeme();
 
 			/* Exit if EOF reached */
 			if (lexeme == null) {
 				lexemeComposer.close();
 				if (token.length() == 0) {
 					return null;
 				} else {
 					throw new Exception("Malformed token: \'" + token.toString() + "\'"); //todo: add line number to LexemeComposer and include it into the Exception message
 				}
 			}
 
 			/* Manage PRECEDING SPACE */
 			manageSpace(token, lexeme);
 
 			/* APPEND LEXEME */
 			token.append(lexeme.getValue());
 
 			/* APPEND SPACE */
 			token.append(" ");
 
 			/* Return the token when SEMICOLON is met */
 			if (lexeme.getType() == LexemeType.SEMICOLON) {
 				break;
 			}
 		}
 
 		return token.toString().trim();
 	}
 
 	public void close() throws IOException {
 		lexemeComposer.close();
 	}
 
 	private void manageSpace(StringBuffer token, Lexeme lexeme) {
 
 		/* Remove trailing space BETWEEN '<' and '-' */
 		removeSpaceBetween('<', LexemeType.OP_SUBTR, token, lexeme);
 
 		/* Remove trailing space BETWEEN '-' and '>' */
 		removeSpaceBetween('-', LexemeType.GT, token, lexeme);
 
 		/* Remove trailing space BETWEEN '/' and '=' */
 		removeSpaceBetween('/', LexemeType.OP_EQ, token, lexeme);
 
 		/* Remove space BEFORE '[' */
 		if (lexeme.getType() == LexemeType.OPEN_SQUARE_BRACKET) {
 			if (token.length() > 0 && Character.isWhitespace(token.charAt(token.length() - 1))) {
 				token.deleteCharAt(token.length() - 1);
 			}
 		}
 	}
 
 	private void removeSpaceBetween(char charBeforeSpace, LexemeType lexemeAfterSpace, StringBuffer token, Lexeme lexeme) {
 		if (lexeme.getType() == lexemeAfterSpace) {
 			/* If charBeforeSpace precedes, remove the space in between */
 			if (token.length() > 1 && token.charAt(token.length() - 2) == charBeforeSpace) {
 				if (Character.isWhitespace(token.charAt(token.length() - 1))) {
 					token.deleteCharAt(token.length() - 1);
 				}
 			}
 		}
 	}
 
 }
