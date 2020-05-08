 package org.zwobble.shed.parser.tokeniser;
 
 import lombok.Data;
 
 @Data
 public class Token {
     private static final Token END = new Token(TokenType.END, null);
     
     public static Token keyword(Keyword keyword) {
         return new Token(TokenType.KEYWORD, keyword.keywordName());
     }
     
     public static Token symbol(String value) {
         return new Token(TokenType.SYMBOL, value);
     }
     
     public static Token identifier(String value) {
         return new Token(TokenType.IDENTIFIER, value);
     }
     
     public static Token whitespace(String value) {
         return new Token(TokenType.WHITESPACE, value);
     }
     
     public static Token number(String value) {
         return new Token(TokenType.NUMBER, value);
     }
     
     public static Token end() {
         return END;
     }
     
     private final TokenType type;
     private final String value;
     
     public String describe() {
         return String.format("%s \"%s\"", type.name().toLowerCase(), value);
     }
 }
