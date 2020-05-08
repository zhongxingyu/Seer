 /**
  * Copyright (c) 2012 GreenI2R
  *
  * Permission is hereby granted, free of charge, to any person obtaining
  * a copy of this software and associated documentation files (the
  * "Software"), to deal in the Software without restriction, including
  * without limitation the rights to use, copy, modify, merge, publish,
  * distribute, sublicense, and/or sell copies of the Software, and to
  * permit persons to whom the Software is furnished to do so, subject to
  * the following conditions:
  *
  * The above copyright notice and this permission notice shall be
  * included in all copies or substantial portions of the Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
  * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
  * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
  * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
  * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
  * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
  * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
  */
 package greenapi.gpi.metric.expression.lexer;
 
 public abstract class AbstractLexer extends Lexer
 {
 
     /**
      * Create an instance of an {@link AbstractLexer} with the given input.
      * 
      * @param input
      *            The input to be analyzed.
      */
     public AbstractLexer(String input)
     {
         super(input);
     }
 
     /**
      * Determines if a character is a space or white space (' '|'\t'|'\n'|'\r').
      * 
      * @param character
      *            The character being evaluated.
      * 
      * @return True if the character is a space or white space and false if not.
      */
     protected boolean isWhitespace(char character)
     {
         return character == ' ' || character == '\t' || character == '\n' || character == '\r' || character == '\f';
     }
 
     /**
      * Ignore any whitespace onto the input.
      */
     protected void WS()
     {
        while (current() == ' ' || current() == '\t' || current() == '\n' || current() == '\r')
         {
             advance();
         }
     }
 
     /**
      * Returns <code>true</code> only if the current character is a letter [a..z,A..Z] or <code>false</code> otherwise.
      * 
      * @return <code>true</code> only if the current character is a letter [a..z,A..Z] or <code>false</code> otherwise.
      */
     protected boolean isLETTER()
     {
         return current() >= 'a' && current() <= 'z' || current() >= 'A' && current() <= 'Z';
     }
 
     /**
      * Returns <code>true</code> if the current character is a {@link Number}.
      * 
      * @return <code>true</code> if the current character is a {@link Number}.
      */
     protected boolean isNUMBER()
     {
         return isNUMBER(current());
     }
 
     /**
      * Returns <code>true</code> if the given character is a {@link Number}.
      * 
      * @param value
      *            The value to be checked.
      * @return <code>true</code> if the given character is a {@link Number}.
      */
     protected boolean isNUMBER(char value)
     {
         return value >= 48 && value <= 57 || value == '.';
     }
 
     /**
      * Returns <code>true</code> if the {@link #current()} character is an EXPONENT.
      * 
      * @return <code>true</code> if the {@link #current()} character is an EXPONENT.
      * @see #isEXPONENT(char)
      */
     protected boolean isEXPONENT()
     {
         return this.isEXPONENT(current());
     }
 
     /**
      * Returns <code>true</code> if a given character is an EXPONENT. <br />
      * 
      * @param value
      *            The value to be checked.
      * @return <code>true</code> if a given character is an EXPONENT.
      */
     protected boolean isEXPONENT(char value)
     {
         int ni = index() + 1;
 
         if (ni >= this.length())
 
         {
             return false;
         }
 
         // final char np = nextWithoutConsume();
         // && (isNUMBER(last()) && (np == '-' || np == '+' || isNUMBER(np)));
         return (current() == 'E' || current() == 'e') || (current() == '+' || current() == '-') || (current() >= 48 && current() <= 57);
     }
 
     /**
      * Define what a letter is (\w). LETTER : 'a'..'z'|'A'..'Z'.
      * 
      * @throws Error
      *             if the current character is not a letter.
      * @see #isLETTER()
      * @see #current()
      */
     protected void LETTER()
     {
         if (isLETTER())
         {
             consume();
         }
         else
         {
             throw new Error("expecting LETTER; found " + current());
         }
     }
 
     /**
      * Defines what a number is. NUMBER: '0'..'9'.
      */
     protected void NUMBER()
     {
         if (isNUMBER())
         {
             consume();
         }
         else
         {
             throw new Error("expecting NUMBER; found " + current());
         }
     }
 
     /**
      * Defines what a exponent is. EXPONENT : ('e'|'E') ('+'|'-')?('0'..'9')+.
      */
     protected void EXPONENT()
     {
         if (isEXPONENT())
         {
             consume();
         }
         else
         {
             throw new Error("expecting EXPONENT; found " + current());
         }
     }
 }
