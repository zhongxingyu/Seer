 package frontend;
 
 import frontend.tokens.NumberToken;
 import frontend.tokens.SpecialSymbolToken;
 import frontend.tokens.WordToken;
 
 import java.io.IOException;
 
 import static frontend.Source.EOF;
 import static frontend.Source.EOL;
 
 public class Scanner {
     private Source source;
     private Token currentToken;
 
     public Scanner(Source source) {
         this.source = source;
     }
 
     public Token extractToken() throws IOException {
         skipWhiteSpace();
         Token token = null;
         char currentChar = currentChar();
 
         // TODO: change this to extract Scheme instead
         if (currentChar == EOF) {
             token = new EofToken(source);
         }
         else if (Character.isLetter(currentChar)) {
             token = new WordToken(source);
         }
         else if (Character.isDigit(currentChar)) {
             token = new NumberToken(source);
         }
         else if (TokenType.SPECIAL_SYMBOLS
                 .containsKey(Character.toString(currentChar))) {
             token = new SpecialSymbolToken(source);
         }
 
         return token;
     }
 
     private void skipWhiteSpace() throws IOException {
         char currentChar = currentChar();
 
         while (Character.isWhitespace(currentChar) || (currentChar == ';')) {
             // Start of a comment?
             if (currentChar == ';') {
                do {
                    currentChar = nextChar();  // consume comment characters
                } while ((currentChar != EOL) && (currentChar != EOF));
                //source.readLine();
                if (currentChar == EOL) {
                    currentChar = nextChar();  // consume the EOL
                }
             }
 
             // Not a comment.
             else {
                 currentChar = nextChar();  // consume whitespace character
             }
         }
     }
 
     public Token currentToken() {
         return currentToken;
     }
 
     public Token nextToken() throws IOException {
         currentToken = extractToken();
         return currentToken;
     }
 
     public char currentChar() throws IOException {
         return source.currentChar();
     }
 
     public char nextChar() throws IOException {
         return source.nextChar();
     }
 }
