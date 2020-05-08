 package token.plog;
 
 import java.io.IOException;
 
 import parser.Source;
 import token.Token;
 
 public class VarToken extends Token {
 
 	public VarToken(Source source) {
 		super(source);
 	}
 
 	@Override
 	public void extract() throws IOException {
 		StringBuilder textBuilder = new StringBuilder();
 
 		char current = source().current();
		while (Character.isLetterOrDigit(current) || current == '_') {
 			textBuilder.append(current);
 			current = source().next();
 		}
 
 		text = textBuilder.toString();
 		value = text;
 		type = TokenType.isReserved(text) ? TokenType.valueOf(text
 				.toUpperCase()) : TokenType.IDENTIFIER;
 	}
 }
