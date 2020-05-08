 package ru.madzi.o2.lexer;
 
 /**
 * @author Dmitry Eliseev
  */
 public class O2Lexer {
 
     private O2CharStream stream;
 
     private long intVal;
 
     private double realVal;
 
     private String strVal;
 
     public O2Lexer(O2CharStream stream) {
         this.stream = stream;
     }
 
     private char getChar() {
         return stream.read();
     }
 
     private void undoChar() {
         stream.undoChar(1);
     }
 
     public O2Token getToken() {
         char ch = getChar();
         if (ch == stream.EOF) {
             return O2Token.EOF;
         } else if (Character.isWhitespace(ch)) {
             return O2Token.WHITESPACE;
         } else if (Character.isDigit(ch)) {
             undoChar();
             return number();
         } else if (Character.isJavaIdentifierStart(ch)) {
             undoChar();
             return identifier();
         } else {
             switch (ch) {
                 case '\'':
                 case '"':
                     return string(ch);
 
                 case '#':
                     return O2Token.NEQ;
 
                 case '&':
                     return O2Token.AND;
 
                 case '(':
                     ch = getChar();
                     if (ch == '*') {
                         return comment();
                     }
                     undoChar();
                     return O2Token.LPAREN;
 
                 case ')':
                     return O2Token.RPAREN;
 
                 case '*':
                     return O2Token.TIMES;
 
                 case '+':
                     return O2Token.PLUS;
 
                 case ',':
                     return O2Token.COMMA;
 
                 case '-':
                     return O2Token.MINUS;
 
                 case '.':
                     ch = getChar();
                     if (ch == '.') {
                         return O2Token.UPTO;
                     }
                     undoChar();
                     return O2Token.LPAREN;
 
                 case '/':
                     return O2Token.SLASH;
 
                 case ':':
                     ch = getChar();
                     if (ch == '=') {
                         return O2Token.BECOMES;
                     }
                     undoChar();
                     return O2Token.COLON;
 
                 case ';':
                     return O2Token.SEMICOLON;
 
                 case '<':
                     ch = getChar();
                     if (ch == '=') {
                         return O2Token.LEQ;
                     }
                     undoChar();
                     return O2Token.LSS;
 
                 case '=':
                     return O2Token.EQL;
 
                 case '>':
                     ch = getChar();
                     if (ch == '=') {
                         return O2Token.GEQ;
                     }
                     undoChar();
                     return O2Token.GTR;
 
                 case '[':
                     return O2Token.LBRAK;
 
                 case ']':
                     return O2Token.RBRAK;
 
                 case '^':
                     return O2Token.ARROW;
 
                 case '{':
                     return O2Token.LBRACE;
 
                 case '|':
                     return O2Token.BAR;
 
                 case '}':
                     return O2Token.RBRACE;
 
                 case '~':
                     return O2Token.NOT;
 
                 default: return O2Token.ERROR;
             }
         }
     }
 
     private static double ten(int e) {
         double x = 1;
         double p = 10;
         while (e > 0) {
             if (e % 2 == 1) { // ODD(e)
                 x *= p;
             }
             e = e / 2;
             if (e > 0) {
                 p *= p;
             }
         }
         return x;
     }
 
     private int ord(char ch, boolean hex) {
         switch (ch) {
             case '0':
                 return 0;
 
             case '1':
                 return 1;
 
             case '2':
                 return 2;
 
             case '3':
                 return 3;
 
             case '4':
                 return 4;
 
             case '5':
                 return 5;
 
             case '6':
                 return 6;
 
             case '7':
                 return 7;
 
             case '8':
                 return 8;
 
             case '9':
                 return 9;
 
             case 'A':
                 return hex ? 10 : -1;
 
             case 'B':
                 return hex ? 11 : -1;
 
             case 'C':
                 return hex ? 12 : -1;
 
             case 'D':
                 return hex ? 13 : -1;
 
             case 'E':
                 return hex ? 14 : -1;
 
             case 'F':
                 return hex ? 15 : -1;
         }
         return -1;
     }
 
     private int parseInt(String str, char end) {
         return 0;
         //TODO: Write parse int;
     }
 
     private O2Token number() {
         boolean wasdot = false;
         boolean real = false;
         char prev = stream.EOF;
         char ch = getChar();
         StringBuilder sb = new StringBuilder();
         while (ch != stream.EOF && isNumberChar(ch)) {
             if (ch == 'H') {
                 intVal = parseInt(sb.toString(), ch);
                 return real ? O2Token.ERROR : O2Token.NUMBER;
             } else if (ch == 'X') {
                 intVal = parseInt(sb.toString(), ch);
                 return real ? O2Token.ERROR : O2Token.NUMBER;
             } else if (ch == '.') {
                 if (ch == prev) { // 123..321
                     undoChar();
                     undoChar();
                     intVal = parseInt(sb.toString(), 'D');
                     return real ? O2Token.ERROR : O2Token.NUMBER;
                 } else if (wasdot) {
                     return O2Token.ERROR;
                 } else {
                     wasdot = true;
                 }
             } else if (ch == '-') {
                 if (prev != stream.EOF && prev != 'E') {
                     undoChar();
                     return O2Token.NUMBER;
                 }
             } else if (ch == '+') {
                 if (prev != stream.EOF && prev != 'E') {
                     undoChar();
                     return O2Token.NUMBER;
                 }
             }
             prev = ch;
             sb.append(ch);
             ch = getChar();
         }
         undoChar();
         return O2Token.NUMBER;
     }
 
     private O2Token identifier() {
         char ch = getChar();
         StringBuilder sb = new StringBuilder();
         while (ch != stream.EOF && Character.isJavaIdentifierPart(ch)) {
             sb.append(ch);
             ch = getChar();
         }
         undoChar();
         strVal = sb.toString();
         for (O2Token token : O2Token.values()) {
             if (strVal.equals(token.getName())) {
                 return token;
             }
         }
         return O2Token.IDENTIFIER;
     }
 
     private O2Token string(char end) {
         char ch = getChar();
         StringBuilder str = new StringBuilder();
         while (ch != stream.EOF && ch != end) {
             str.append(ch);
             ch = getChar();
         }
         strVal = str.toString();
         if (ch == stream.EOF) {
             undoChar();
             return O2Token.ERROR;
         }
         return O2Token.STRING;
     }
 
     private O2Token comment() {
         int level = 1;
         CommentState state = CommentState.STAR;
         char ch = getChar();
         while (ch != stream.EOF && level > 0) {
             if (ch == '(') {
                 state = CommentState.LPAREN;
             } else if (ch == '*') {
                 if (state == CommentState.LPAREN) {
                     level++;
                 }
                 state = CommentState.STAR;
             } else if (ch == ')') {
                 if (state == CommentState.STAR) {
                     level--;
                 }
                 state = CommentState.RPAREN;
             } else {
                 state = CommentState.TEXT;
             }
             ch = getChar();
         }
         if (ch == stream.EOF) {
             undoChar();
             return O2Token.ERROR;
         }
         return O2Token.COMMENT;
     }
 
     private static boolean isNumberChar(char ch) {
         switch (ch) {
             case '0':
             case '1':
             case '2':
             case '3':
             case '4':
             case '5':
             case '6':
             case '7':
             case '8':
             case '9':
             case 'A':
             case 'B':
             case 'C':
             case 'D':
             case 'E':
             case 'F':
             case 'H':
             case 'X':
             case '.':
             case '-':
             case '+':
                 return true;
         }
         return false;
     }
 
     private enum CommentState {
         TEXT,
         LPAREN,
         STAR,
         RPAREN
     }
 
 }
