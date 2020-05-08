 /**
  * Lisp Subinterpreter, an interpreter for a sublanguage of Lisp
  * Copyright (C) 2011  Meisam Fathi Salmi <fathi@cse.ohio-state.edu>
  * 
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package edu.osu.cse.meisam.interpreter;
 
 import edu.osu.cse.meisam.interpreter.tokens.CloseParentheses;
 import edu.osu.cse.meisam.interpreter.tokens.Dot;
 import edu.osu.cse.meisam.interpreter.tokens.EOF;
 import edu.osu.cse.meisam.interpreter.tokens.LiteralAtom;
 import edu.osu.cse.meisam.interpreter.tokens.NumericAtom;
 import edu.osu.cse.meisam.interpreter.tokens.OpenParentheses;
 import edu.osu.cse.meisam.interpreter.tokens.Token;
 
 /**
  * @author Meisam Fathi Salmi <fathi@cse.ohio-state.edu>
  * 
  */
 public class Lexer {
 
     private static final int MAX_INPUT_SIZE = 1000;
 
     private int tokenCounter;
 
     private final InputProvider in;
 
     /**
      * 
      */
     public Lexer(final InputProvider inputProvider) {
         this.in = inputProvider;
         this.tokenCounter = 0;
     }
 
    public boolean hasMoreTokens() {
        removeWhitespace();
        return this.in.hasMore();
    }

     public Token nextToken() {
         if (this.tokenCounter++ > Lexer.MAX_INPUT_SIZE) {
             throw new LexerExeption(
                     "Input is too large to be processed, use a smaller input");
         }
         try {
             String buffer = "";
 
             removeWhitespace();
 
             if (!this.in.hasMore()) {
                 return new EOF();
             }
 
             final char lookaheadChar = this.in.lookaheadChar();
 
             if (isDigit(lookaheadChar) || isSign(lookaheadChar)) {
                 buffer = readNumber();
                 return new NumericAtom(buffer);
             }
 
             if (isLetter(lookaheadChar)) {
                 buffer = readSymbol();
                 return new LiteralAtom(buffer);
             }
 
             if (isWhiteSpace(lookaheadChar)) {
                 // buffer = readWhiteSpace()
             }
 
             if (isOpenParentheses(lookaheadChar)) {
                 buffer = readOpenParentheses();
                 return new OpenParentheses();
             }
 
             if (isCloseParentheses(lookaheadChar)) {
                 buffer = readCloseParentheses();
                 return new CloseParentheses();
             }
 
             if (isDot(lookaheadChar)) {
                 buffer = readDot();
                 return new Dot();
             }
 
             throw new LexerExeption("Unknown Symbol in the input:"
                     + lookaheadChar);
 
         } catch (final LexerExeption ex) {
             throw ex;
         } catch (final Exception ex) {
             throw new LexerExeption("Cannot read the input");
         }
     }
 
     private void removeWhitespace() {
         while (this.in.hasMore() && isWhiteSpace(this.in.lookaheadChar())) {
             this.in.nextChar();
         }
     }
 
     private final boolean isDigit(final char ch) {
         if (('0' <= ch) && (ch <= '9')) {
             return true;
         }
         return false;
     }
 
     private final boolean isSign(final char ch) {
         if ((ch == '+') || (ch == '-')) {
             return true;
         }
         return false;
     }
 
     private final boolean isMinusSign(final char ch) {
         if (ch == '-') {
             return true;
         }
         return false;
     }
 
     private final boolean isLowercaseLetter(final char ch) {
         if (('a' <= ch) && (ch <= 'z')) {
             return true;
         }
         return false;
     }
 
     private final boolean isUppercaseLetter(final char ch) {
         if (('A' <= ch) && (ch <= 'Z')) {
             return true;
         }
         return false;
     }
 
     private final boolean isLetter(final char ch) {
         if (isUppercaseLetter(ch) || isLowercaseLetter(ch)) {
             return true;
         }
         return false;
     }
 
     private final boolean isWhiteSpace(final char ch) {
         if ((ch == ' ') || (ch == '\t') || (ch == '\r') || (ch == '\n')) {
             return true;
         }
         return false;
     }
 
     private final boolean isOpenParentheses(final char ch) {
         if (ch == '(') {
             return true;
         }
         return false;
     }
 
     private final boolean isCloseParentheses(final char ch) {
         if (ch == ')') {
             return true;
         }
         return false;
     }
 
     private final boolean isDot(final char ch) {
         if (ch == '.') {
             return true;
         }
         return false;
     }
 
     private final boolean isDelimiter(final char ch) {
         return isOpenParentheses(ch) || isCloseParentheses(ch)
                 || isWhiteSpace(ch);
     }
 
     private final String readNumber() {
         final StringBuffer buffer = new StringBuffer(100);
 
         // read the sign if there is any
         if (this.in.hasMore()) {
             if (isSign(this.in.lookaheadChar())) {
                 final char nextChar = this.in.nextChar();
                 if (isMinusSign(nextChar)) {
                     buffer.append(nextChar);
                 }
             }
         }
 
         boolean atLeastOneDigit = false;
         // read the rest of the number
         while (this.in.hasMore() && isDigit(this.in.lookaheadChar())) {
             final char nextChar = this.in.nextChar();
             if (isDigit(nextChar)) {
                 buffer.append(nextChar);
                 atLeastOneDigit = true;
             } else if (isDelimiter(nextChar)) {
                 break;
             } else {
                 throw new LexerExeption("Lexing Error: '" + buffer.toString()
                         + nextChar + "'is not a valid number");
             }
         }
 
         if (!atLeastOneDigit) {
             throw new LexerExeption("Expecting to see a number here:"
                     + buffer.toString());
         }
 
         return buffer.toString();
     }
 
     private final String readSymbol() {
         final StringBuffer buffer = new StringBuffer(100);
         while (this.in.hasMore()) {
             if (isLetter(this.in.lookaheadChar())
                     || isDigit(this.in.lookaheadChar())) {
                 final char nextChar = this.in.nextChar();
                 buffer.append(nextChar);
             } else if (isDelimiter((this.in.lookaheadChar()))) {
                 break;
             } else {
                 throw new LexerExeption("Lexing Error: '" + buffer.toString()
                         + this.in.lookaheadChar()
                         + "'is not a valid identifier");
             }
         }
         return buffer.toString();
     }
 
     private final String readOpenParentheses() {
         final StringBuffer buffer = new StringBuffer(100);
         final char nextChar = this.in.nextChar();
         if (isOpenParentheses(nextChar)) {
             buffer.append(nextChar);
             return buffer.toString();
         } else {
             throw new LexerExeption("Lexing Error: '" + buffer.toString()
                     + this.in.lookaheadChar() + "'is not a valid identifier");
         }
     }
 
     private final String readCloseParentheses() {
         final StringBuffer buffer = new StringBuffer(100);
         final char nextChar = this.in.nextChar();
         if (isCloseParentheses(nextChar)) {
             buffer.append(nextChar);
             return buffer.toString();
         } else {
             throw new LexerExeption("Lexing Error: '" + buffer.toString()
                     + this.in.lookaheadChar() + "'is not a valid identifier");
         }
     }
 
     private final String readDot() {
         final StringBuffer buffer = new StringBuffer(100);
         final char nextChar = this.in.nextChar();
         if (isDot(nextChar)) {
             if (!this.in.hasMore()
                     || (this.in.hasMore() && isDelimiter(this.in
                             .lookaheadChar()))) {
                 buffer.append(nextChar);
                 return buffer.toString();
             } else {
                 throw new LexerExeption(
                         "Lexing Error: DOT should be followed by whitespace or parentheses");
             }
         } else {
             throw new LexerExeption("Lexing Error: '" + buffer.toString()
                     + this.in.lookaheadChar() + "'is not a valid identifier");
         }
     }
 
 }
