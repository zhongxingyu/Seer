 package de.weltraumschaf.caythe.frontend.caythe;
 
 import de.weltraumschaf.caythe.frontend.EofToken;
 import de.weltraumschaf.caythe.frontend.Scanner;
 import de.weltraumschaf.caythe.frontend.Source;
 import static de.weltraumschaf.caythe.frontend.Source.EOF;
 import de.weltraumschaf.caythe.frontend.Token;
 import static de.weltraumschaf.caythe.frontend.caythe.CayTheErrorCode.INVALID_CHARACTER;
 import de.weltraumschaf.caythe.frontend.caythe.tokens.*;
 
 /**
  * Scans Cay-The source code.
  *
  * @author Sven Strittmatter <weltraumschaf@googlemail.com>
  */
 public class CayTheScanner extends Scanner {
 
     /**
      * Initialize scanner with source.
      *
      * @param source
      */
     public CayTheScanner(Source source) {
         super(source);
     }
 
     @Override
     protected Token extractToken() throws Exception {
         nextChar();
         skipWhitespace();
 
         Token token;
         char currentChar = currentChar();
 
         if (EOF == currentChar) {
             token = new EofToken(getSource());
         } else if (Character.isLetter(currentChar)) {
             token = new CayTheWordToken(getSource());
         } else if (Character.isDigit(currentChar)) {
             token = new CayTheNumberToken(getSource());
         } else if ('\'' == currentChar) {
             token = new CayTheCharacterToken(getSource());
         } else if ('"' == currentChar) {
             token = new CayTheStringToken(getSource());
         } else if (CayTheTokenType.SPECIAL_SYMBOLS.containsKey(Character.toString(currentChar))) {
             token = new CayTheSpecialSymbolToken(getSource());
         } else {
             token = new CayTheErrorToken(getSource(), INVALID_CHARACTER, Character.toString(currentChar));
         }
 
         token.extract();
         return token;
     }
 
     /**
      * Skips white spaces and comments.
      *
      * @throws Exception
      */
     private void skipWhitespace() throws Exception {
         char currentChar = currentChar();
 
         while (Character.isWhitespace(currentChar) || ( '/' == currentChar )) {
             // Start of a comment?
             if ('/' == currentChar) {
                 char nextChar = peakChar();
 
                 if ('/' == nextChar) { // single line comment
                     skipToNextLine();
                 }
                 else if ('*' == nextChar) { // multi line comment
                     do {
                         nextChar(); // Consume comment chars.
                         currentChar = currentChar();
                    } while (('*' != currentChar) && ('/' != peakChar()) && (EOF != currentChar));
 
                     // Found closing '*/'?
                     if ('*' == currentChar) {
                         nextChar(); // Consumes the '*'.
                         currentChar = currentChar();
 
                         if ('/' == currentChar) {
                             nextChar(); // Consumes the '/'.
                             currentChar = currentChar();
                         }
                     }
                 }
             } // Not a comment.
             else {
                 nextChar();
                 currentChar = currentChar();
             }
         }
     }
 }
