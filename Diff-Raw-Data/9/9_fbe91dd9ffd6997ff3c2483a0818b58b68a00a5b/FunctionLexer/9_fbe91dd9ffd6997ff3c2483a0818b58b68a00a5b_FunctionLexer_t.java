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
 
 import java.math.BigDecimal;
 import java.util.HashMap;
 import java.util.Map;
 
 import greenapi.gpi.metric.expression.Value;
 import greenapi.gpi.metric.expression.token.CommaToken;
 import greenapi.gpi.metric.expression.token.ExponentToken;
 import greenapi.gpi.metric.expression.token.IdentifierToken;
 import greenapi.gpi.metric.expression.token.LeftParenthesisToken;
 import greenapi.gpi.metric.expression.token.NumberToken;
 import greenapi.gpi.metric.expression.token.OperatorToken;
 import greenapi.gpi.metric.expression.token.RightParenthesisToken;
 import greenapi.gpi.metric.expression.token.Token;
 
 public class FunctionLexer extends AbstractLexer
 {
 
     /**
      * The tokens of this {@link greenapi.gpi.metric.expression.lexer.Lexer}.
      */
     private static final Map<Integer, Class<?>> TOKENS = new HashMap<>();
 
     static
     {
         TOKENS.put(1, IdentifierToken.class);
         TOKENS.put(2, LeftParenthesisToken.class);
         TOKENS.put(3, RightParenthesisToken.class);
         TOKENS.put(4, RightParenthesisToken.class);
         TOKENS.put(5, CommaToken.class);
         TOKENS.put(6, OperatorToken.class);
         TOKENS.put(7, NumberToken.class);
         TOKENS.put(8, ExponentToken.class);
     }
 
     /**
      * Creates a {@link FunctionLexer} with the given input.
      * 
      * @param input
      *            The input to be analyzed.
      */
     public FunctionLexer(String input)
     {
         super(input);
     }
 
     @Override
     public Token nextToken()
     {
         while (current() != EOF)
         {
             switch (current())
             {
             case ' ':
             case '\n':
             case '\t':
             case '\r':
             case '\f':
                 WS();
                 continue;
             case '+':
             case '-':
             case '*':
             case '/':
             case '%':
                 Token token = new OperatorToken<Value<BigDecimal>>(current());
                 consume();
                 return token;
             case ',':
                 // A comma in the real world can be a thousands separator or an argument separator. But here It's always an argument separator.
                 consume();
                 return new CommaToken();
             case '(':
                 consume();
                 return new LeftParenthesisToken();
             case ')':
                 consume();
                 return new RightParenthesisToken();
             default:
 
                 if (isLETTER())
                 {
                     return name();
                 }
 
                 if (isNUMBER())
                 {
                     return number();
                 }
                 throw new Error("Invalid character: " + current());
             }
         }
         return null;
     }
 
     /**
      * Consumes a name token. A name is sequence of >=1 letter. <br/>
      * name : LETTER+.
      * 
      * @return A {@link IdentifierToken} with the name of the identifier.
      */
     protected IdentifierToken name()
     {
         StringBuilder buf = new StringBuilder();
         do
         {
             buf.append(current());
             LETTER();
         } while (isLETTER());
 
         return new IdentifierToken(buf.toString());
     }
 
     /**
      * Consumes a number token. A number is a sequence of one or more digits, thousands separators, E or e and +-. Example: 5,559.25.
      * 
      * @return A {@link NumberToken} with the name of the identifier.
      */
     protected NumberToken number()
     {
         StringBuilder buf = new StringBuilder();
         do
         {
             buf.append(current());
             NUMBER();
         } while (isNUMBER());
 
         if (isEXPONENT())
         {
             Token exponent = exponent();
             buf.append(exponent.getValue());
         }
 
         return new NumberToken(buf.toString());
     }
 
     /**
      * Consumes a exponent token. A exponent is a 'E' or 'e' followed by a {+,-}? and by numbers [0..9]*
      * 
      * @return A exponent token.
      */
     protected Token exponent()
     {
         StringBuilder buf = new StringBuilder();
         do
         {
             buf.append(current());
             EXPONENT();
         } while (isEXPONENT());
 
         return new ExponentToken(buf.toString());
     }
 
     @Override
     public Token getToken(int x)
     {
         return null;
     }
 }
