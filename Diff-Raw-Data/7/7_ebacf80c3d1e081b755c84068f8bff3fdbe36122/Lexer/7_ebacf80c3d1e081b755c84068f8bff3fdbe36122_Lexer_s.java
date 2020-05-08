 package com.github.dokky.gherkin.lexer;
 
 import lombok.Data;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.List;
 
 @Data
 public final class Lexer {
     protected CharSequence buffer;
     protected int          startOffset;
     protected int          endOffset;
     private   int          currentPosition;
     private   int          currentTokenStartPosition;
 
     private int       state;
     private TokenType currentTokenType;
 
     private static final int STATE_DEFAULT            = 0;
     private static final int STATE_AFTER_KEYWORD      = 1;
     private static final int STATE_AFTER_STEP_KEYWORD = 3;
     private static final int STATE_TABLE              = 2;
 
    private static final String PYSTRING = "\"\"\"";
 
     private static final List<String> KEYWORDS = buildKeywords();
 
 
     public Lexer(CharSequence buffer) {
         init(buffer, 0, buffer.length(), STATE_DEFAULT);
     }
 
     public void init(CharSequence buffer, int startOffset, int endOffset, int initialState) {
         this.buffer = buffer;
         this.startOffset = startOffset;
         this.endOffset = endOffset;
         currentPosition = startOffset;
         state = initialState;
     }
 
     public String getCurrentTokenValue() {
         return buffer.subSequence(currentTokenStartPosition, currentPosition).toString();
     }
 
     public void parseNextToken() {
         if (currentPosition >= endOffset) {
             currentTokenType = null;
             return;
         }
         currentTokenStartPosition = currentPosition;
         char c = buffer.charAt(currentPosition);
 
         if (Character.isWhitespace(c)) {
             parseWhitespaces();
             currentTokenType = TokenType.WHITESPACE;
             while (currentPosition < endOffset && Character.isWhitespace(buffer.charAt(currentPosition))) {
                 parseWhitespaces();
             }
             return;
         } else if (c == '#') { // todo check steps args
             currentTokenType = TokenType.COMMENT;
             parseToEol();
             return;
         } else if (c == '@') {
             currentTokenType = TokenType.TAG;
             currentPosition++;
             while (currentPosition < endOffset && isValidTagChar(buffer.charAt(currentPosition))) {
                 currentPosition++;
             }
             return;
         } else if (c == ':') {
             currentTokenType = TokenType.COLON;
             currentPosition++;
             return;
         } else if (c == '|') {
             currentTokenType = TokenType.PIPE;
             currentPosition++;
             state = STATE_TABLE;
             return;
         } else if (state == STATE_TABLE) {
             currentTokenType = TokenType.TABLE_CELL;
             while (currentPosition < endOffset && buffer.charAt(currentPosition) != '|' && buffer.charAt(currentPosition) != '\n' && buffer.charAt(currentPosition) != '#') {
                 currentPosition++;
             }
             while (currentPosition > 0 && Character.isWhitespace(buffer.charAt(currentPosition - 1))) {
                 currentPosition--;
             }
             return;
         } else if (isStringAtPosition(PYSTRING)) {
             currentTokenType = TokenType.PYSTRING;
             currentPosition += 3;
             while (currentPosition < endOffset && !isStringAtPosition(PYSTRING)) {
                 currentPosition++;
             }
             currentPosition += 3;
             return;
         } else if (state == STATE_DEFAULT) {
 
             for (String keyword : KEYWORDS) {
 
                 if (isStringAtPosition(keyword)) {
 
                     int length = keyword.length();
 
                     if (endOffset - currentPosition > length && !Character.isLetterOrDigit(buffer.charAt(currentPosition + length))) {
                         currentPosition += length;
                         currentTokenType = TokenType.KEYWORDS.get(keyword);
 
                         if (currentTokenType == TokenType.STEP_KEYWORD) {
                             state = STATE_AFTER_STEP_KEYWORD;
                         } else {
                             state = STATE_AFTER_KEYWORD;
                         }
                         return;
                     }
                 }
             }
         }
 
         currentTokenType = TokenType.TEXT;
         parseToEolOrComment();
     }
 
     private static boolean isValidTagChar(char c) {
         return !Character.isWhitespace(c) && c != '@';
     }
 
     private boolean isStringAtPosition(String keyword) {
         int length = keyword.length();
         return endOffset - currentPosition >= length && buffer.subSequence(currentPosition, currentPosition + length).toString().equals(keyword);
     }
 
     private void parseWhitespaces() {
         if (buffer.charAt(currentPosition) == '\n') {
             state = STATE_DEFAULT;
         }
         currentPosition++;
     }
 
     private void parseToEol() {
         currentPosition++;
         while (currentPosition < endOffset && buffer.charAt(currentPosition) != '\n') {
             currentPosition++;
         }
        state = STATE_DEFAULT;
     }
 
     private void parseToEolOrComment() {
         currentPosition++;
         while (currentPosition < endOffset && buffer.charAt(currentPosition) != '\n' && buffer.charAt(currentPosition) != '#') {
             currentPosition++;
         }
         state = STATE_DEFAULT;
     }
 
 
     private static List<String> buildKeywords() {
         List<String> keywords = new ArrayList<String>(TokenType.KEYWORDS.keySet());
         Collections.sort(keywords, new Comparator<String>() {
             public int compare(String x0, String x1) {
                 return x1.length() - x0.length();
             }
         });
         return keywords;
     }
 
     public boolean hasNewLine(int start, int end) {
         while (start < end && start < endOffset) {
             if (buffer.charAt(start++) == '\n') {
                 return true;
             }
         }
         return false;
     }
 }
