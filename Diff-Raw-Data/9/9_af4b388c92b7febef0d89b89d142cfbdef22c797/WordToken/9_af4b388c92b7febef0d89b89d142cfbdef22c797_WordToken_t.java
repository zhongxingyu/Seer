 package frontend.tokens;
 
 import frontend.Source;
 import frontend.Token;
 import frontend.TokenType;
 
 import java.io.IOException;
 
 import static frontend.TokenType.IDENTIFIER;
 import static frontend.TokenType.RESERVED_WORDS;
 
 public class WordToken extends Token {
     public WordToken(Source source) throws IOException {
         super(source);
     }
 
     protected void extract() throws IOException {
         StringBuilder textBuffer = new StringBuilder();
         char currentChar = currentChar();
 
         // Get the word characters (letter or digit).  The scanner has
         // already determined that the first character is a letter.
         while (Character.isLetterOrDigit(currentChar)
                 || currentChar == '-' || currentChar == '?')
         {
             textBuffer.append(currentChar);
             currentChar = nextChar();  // consume character
         }
 
         text = textBuffer.toString();
 
         // Is it a reserved word or an identifier?
        type = (RESERVED_WORDS.contains(text))
                 ? TokenType.valueOf(text.toUpperCase())  // reserved word
                 : IDENTIFIER;                                  // identifier
     }
 }
