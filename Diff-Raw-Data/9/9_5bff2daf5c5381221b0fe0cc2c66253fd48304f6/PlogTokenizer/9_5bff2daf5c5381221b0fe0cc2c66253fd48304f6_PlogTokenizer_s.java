 package parser.plog;
 
 import java.io.IOException;
 
 import parser.Source;
 import parser.Tokenizer;
 import token.EofToken;
 import token.ErrorToken;
 import token.Token;
 import token.plog.ErrorCode;
 import token.plog.NumberToken;
 import token.plog.SpecialToken;
 import token.plog.StringToken;
 import token.plog.TokenType;
 import token.plog.VarToken;
 
 public class PlogTokenizer extends Tokenizer {
 
 	public PlogTokenizer(Source source) {
 		super(source, '#');
 	}
 
 	@Override
 	public Token extract() throws IOException {
 		skipWhitespace();
 		Token token = null;
 		char currentChar = source().current();
 		if (currentChar == Source.EOF) {
 			token = new EofToken(source());
		} else if (Character.isLetter(currentChar)) {
 			token = new VarToken(source());
 		} else if (TokenType.isSpecial(String.valueOf(currentChar))) {
 			token = new SpecialToken(source());
 		} else if (Character.isDigit(currentChar)) {
 			token = new NumberToken(source());
 		} else if (currentChar == StringToken.STRING) {
 			token = new StringToken(source());
 		} else {
 			token = new ErrorToken(source(), ErrorCode.INVALID_CHARACTER,
 					String.valueOf(currentChar));
 		}
 
 		token.extract();
 		return token;
 	}
 }
