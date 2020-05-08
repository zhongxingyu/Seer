 package org.zwobble.shed.parser.tokeniser;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 import static com.google.common.collect.Lists.charactersOf;
 
 public class Tokeniser {
     private static final String operatorCharacters = ".;";
     
     public List<TokenPosition> tokenise(String inputString) {
         int lineNumber = 1;
         int startCharacterNumber = 1;
         int currentCharacterNumber = 1;
         if (inputString.isEmpty()) {
             return Collections.singletonList(new TokenPosition(lineNumber, currentCharacterNumber, Token.end()));
         }
         List<TokenPosition> tokens = new ArrayList<TokenPosition>();
         TokenType currentTokenType = null;
         TokenType previousTokenType = null;
         StringBuilder currentTokenValue = new StringBuilder();
         
         for (char c : charactersOf(inputString)) {
             previousTokenType = currentTokenType;
             if (Character.isWhitespace(c)) {
                 currentTokenType = TokenType.WHITESPACE;
             } else if (isSymbolCharacter(c)) {
                 currentTokenType = TokenType.SYMBOL;
             } else if (Character.isDigit(c) && (previousTokenType == TokenType.NUMBER || currentTokenValue.length() == 0)) {
                 currentTokenType = TokenType.NUMBER;
            } else if (previousTokenType == TokenType.ERROR) {
                currentTokenType = TokenType.ERROR;
            } else if (previousTokenType == TokenType.NUMBER) {
                currentTokenType = TokenType.ERROR;
             } else {
                 currentTokenType = TokenType.IDENTIFIER;
             }
             if (previousTokenType != currentTokenType && previousTokenType != null) {
                 tokens.add(new TokenPosition(lineNumber, startCharacterNumber, toToken(currentTokenValue.toString(), previousTokenType)));
                 currentTokenValue = new StringBuilder();
                 startCharacterNumber = currentCharacterNumber;
             }
             currentTokenValue.append(c);
             if (c == '\n') {
                 lineNumber += 1;
                 currentCharacterNumber = 1;
             } else {
                 currentCharacterNumber += 1;
             }
         }
         tokens.add(new TokenPosition(lineNumber, startCharacterNumber, toToken(currentTokenValue.toString(), currentTokenType)));
         tokens.add(new TokenPosition(lineNumber, currentCharacterNumber, Token.end()));
         return tokens;
     }
     
     private boolean isSymbolCharacter(char c) {
         return operatorCharacters.contains(Character.toString(c));
     }
 
     private Token toToken(String value, TokenType currentTokenType) {
         if (isKeyword(value)) {
             return new Token(TokenType.KEYWORD, value);
         }
         return new Token(currentTokenType, value);
     }
 
     private boolean isKeyword(String value) {
         for (Keyword keyword : Keyword.values()) {
             if (value.equals(keyword.keywordName())) {
                 return true;
             }
         }
         return false;
     }
 }
