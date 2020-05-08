 package org.jessies.calc;
 
 /*
  * This file is part of LittleHelper.
  * Copyright (C) 2009 Elliott Hughes <enh@jessies.org>.
  * 
  * LittleHelper is free software; you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation; either version 3 of the License, or
  * (at your option) any later version.
  * 
  * Talc is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 import java.math.*;
 import java.util.*;
 import org.jessies.test.*;
 
 // FIXME: Mac OS' calculator offers -d variants of all the trig functions for degrees. that, or offer constants to multiply by to convert to degrees/radians?
 // FIXME: higher-order built-in functions like http://www.vitanuova.com/inferno/man/1/calc.html (sum, product, integral, differential, solve).
 // FIXME: integer division (//).
 // FIXME: factorial (postfix !).
 // FIXME: logical not (prefix !).
 public class Calculator {
     private final CalculatorLexer lexer;
     private final Map<String, CalculatorAstNode> constants;
     private final Map<String, CalculatorFunction> functions;
     private final Map<String, BigDecimal> variables;
     
     public Calculator(String expression) {
         this.lexer = new CalculatorLexer(expression);
         this.constants = new HashMap<String, CalculatorAstNode>();
         this.functions = new HashMap<String, CalculatorFunction>();
         this.variables = new HashMap<String, BigDecimal>();
         
         initBuiltInConstants();
         initBuiltInFunctions();
     }
     
     private void initBuiltInConstants() {
         // FIXME: use higher-precision string forms?
         constants.put("e", new CalculatorNumberNode(new BigDecimal(Math.E)));
         
         final CalculatorAstNode pi = new CalculatorNumberNode(new BigDecimal(Math.PI));
         constants.put("pi", pi);
        constants.put("π", pi);
     }
     
     private void initBuiltInFunctions() {
         // FIXME: acosh, asinh, atanh, chop, clip, sign(um), int(eger_part), frac(tional_part)
         functions.put("abs",       new CalculatorFunctions.Abs());
         functions.put("acos",      new CalculatorFunctions.Acos());
         functions.put("asin",      new CalculatorFunctions.Asin());
         functions.put("atan",      new CalculatorFunctions.Atan());
         functions.put("atan2",     new CalculatorFunctions.Atan2());
         functions.put("cbrt",      new CalculatorFunctions.Cbrt());
         final CalculatorFunction ceiling = new CalculatorFunctions.Ceiling();
         functions.put("ceil",      ceiling);
         functions.put("ceiling",   ceiling);
         functions.put("cos",       new CalculatorFunctions.Cos());
         functions.put("cosh",      new CalculatorFunctions.Cosh());
         functions.put("exp",       new CalculatorFunctions.Exp());
         functions.put("factorial", new CalculatorFunctions.Factorial());
         functions.put("floor",     new CalculatorFunctions.Floor());
         functions.put("hypot",     new CalculatorFunctions.Hypot());
         functions.put("log",       new CalculatorFunctions.Log());
         functions.put("log10",     new CalculatorFunctions.Log10());
         functions.put("log2",      new CalculatorFunctions.Log2());
         functions.put("logE",      new CalculatorFunctions.LogE());
         final CalculatorFunction random = new CalculatorFunctions.Random();
         functions.put("rand",      random);
         functions.put("random",    random);
         functions.put("round",     new CalculatorFunctions.Round());
         functions.put("sin",       new CalculatorFunctions.Sin());
         functions.put("sinh",      new CalculatorFunctions.Sinh());
         functions.put("sqrt",      new CalculatorFunctions.Sqrt());
         functions.put("tan",       new CalculatorFunctions.Tan());
         functions.put("tanh",      new CalculatorFunctions.Tanh());
         
         final CalculatorFunction sum = new CalculatorFunctions.Sum();
         functions.put("sum",       sum);
         functions.put("\u03a3",    sum); // Unicode Greek capital letter sigma.
         functions.put("\u2211",    sum); // Unicode summation sign.
         
         final CalculatorFunction product = new CalculatorFunctions.Product();
         functions.put("product",   product);
         functions.put("\u03a0",    product); // Unicode Greek capital letter pi.
         functions.put("\u220f",    product); // Unicode product sign.
     }
     
     public String evaluate() throws CalculatorError {
         CalculatorAstNode ast = parseExpr();
         expect(CalculatorToken.END_OF_INPUT);
         
         //System.err.println(ast);
         BigDecimal value = ast.value(this);
         return value.toString();
     }
     
     public BigDecimal getVariable(String name) {
         return variables.get(name);
     }
     
     public void setVariable(String name, BigDecimal newValue) {
         variables.put(name, newValue);
     }
     
     private CalculatorAstNode parseExpr() {
         return parseOrExpression();
     }
     
     // Mathematica operator precedence: http://reference.wolfram.com/mathematica/tutorial/OperatorInputForms.html
     
     // |
     private CalculatorAstNode parseOrExpression() {
         CalculatorAstNode result = parseXorExpression();
         while (lexer.token() == CalculatorToken.B_OR) {
             CalculatorToken op = lexer.token();
             lexer.nextToken();
             result = new CalculatorOpNode(op, result, parseXorExpression());
         }
         return result;
     }
     
     // ^
     private CalculatorAstNode parseXorExpression() {
         CalculatorAstNode result = parseAndExpression();
         while (lexer.token() == CalculatorToken.B_XOR) {
             CalculatorToken op = lexer.token();
             lexer.nextToken();
             result = new CalculatorOpNode(op, result, parseAndExpression());
         }
         return result;
     }
     
     // &
     private CalculatorAstNode parseAndExpression() {
         CalculatorAstNode result = parseRelationalExpression();
         while (lexer.token() == CalculatorToken.B_AND) {
             CalculatorToken op = lexer.token();
             lexer.nextToken();
             result = new CalculatorOpNode(op, result, parseRelationalExpression());
         }
         return result;
     }
     
     // == >= > <= < !=
     private CalculatorAstNode parseRelationalExpression() {
         CalculatorAstNode result = parseShiftExpression();
         while (lexer.token() == CalculatorToken.EQ || lexer.token() == CalculatorToken.GE || lexer.token() == CalculatorToken.GT || lexer.token() == CalculatorToken.LE || lexer.token() == CalculatorToken.LT || lexer.token() == CalculatorToken.NE) {
             CalculatorToken op = lexer.token();
             lexer.nextToken();
             result = new CalculatorOpNode(op, result, parseShiftExpression());
         }
         return result;
     }
     
     // << >>
     private CalculatorAstNode parseShiftExpression() {
         CalculatorAstNode result = parseAdditiveExpression();
         while (lexer.token() == CalculatorToken.SHL || lexer.token() == CalculatorToken.SHR) {
             CalculatorToken op = lexer.token();
             lexer.nextToken();
             result = new CalculatorOpNode(op, result, parseAdditiveExpression());
         }
         return result;
     }
     
     // + -
     private CalculatorAstNode parseAdditiveExpression() {
         CalculatorAstNode result = parseMultiplicativeExpression();
         while (lexer.token() == CalculatorToken.PLUS || lexer.token() == CalculatorToken.MINUS) {
             CalculatorToken op = lexer.token();
             lexer.nextToken();
             result = new CalculatorOpNode(op, result, parseMultiplicativeExpression());
         }
         return result;
     }
     
     // * / %
     private CalculatorAstNode parseMultiplicativeExpression() {
         CalculatorAstNode result = parseUnaryExpression();
         while (lexer.token() == CalculatorToken.MUL || lexer.token() == CalculatorToken.DIV || lexer.token() == CalculatorToken.MOD) {
             CalculatorToken op = lexer.token();
             lexer.nextToken();
             result = new CalculatorOpNode(op, result, parseUnaryExpression());
         }
         return result;
     }
     
     // ~ -
     private CalculatorAstNode parseUnaryExpression() {
         if (lexer.token() == CalculatorToken.MINUS) {
             lexer.nextToken();
             // Convert (-f) to (0-f) for simplicity.
             return new CalculatorOpNode(CalculatorToken.MINUS, new CalculatorNumberNode(BigDecimal.ZERO), parseUnaryExpression());
         } else if (lexer.token() == CalculatorToken.B_NOT) {
             lexer.nextToken();
             return new CalculatorOpNode(CalculatorToken.B_NOT, parseUnaryExpression(), null);
         }
         return parseExponentiationExpression();
     }
     
     // sqrt
     
     // **
     private CalculatorAstNode parseExponentiationExpression() {
         CalculatorAstNode result = parseFactorialExpression();
         if (lexer.token() == CalculatorToken.POW) {
             CalculatorToken op = lexer.token();
             lexer.nextToken();
             result = new CalculatorOpNode(op, result, parseExponentiationExpression());
         }
         return result;
     }
     
     // postfix-!
     private CalculatorAstNode parseFactorialExpression() {
         CalculatorAstNode result = parseFactor();
         if (lexer.token() == CalculatorToken.PLING) {
             expect(CalculatorToken.PLING);
             result = new CalculatorOpNode(CalculatorToken.FACTORIAL, result, null);
         }
         return result;
     }
     
     private CalculatorAstNode parseFactor() {
         if (lexer.token() == CalculatorToken.OPEN_PARENTHESIS) {
             expect(CalculatorToken.OPEN_PARENTHESIS);
             CalculatorAstNode result = parseExpr();
             expect(CalculatorToken.CLOSE_PARENTHESIS);
             return result;
         } else if (lexer.token() == CalculatorToken.NUMBER) {
             CalculatorAstNode result = new CalculatorNumberNode(lexer.number());
             expect(CalculatorToken.NUMBER);
             return result;
         } else if (lexer.token() == CalculatorToken.IDENTIFIER) {
             final String identifier = lexer.identifier();
             expect(CalculatorToken.IDENTIFIER);
             CalculatorAstNode result = constants.get(identifier);
             if (result == null) {
                 final CalculatorFunction fn = functions.get(identifier);
                 if (fn != null) {
                     result = new CalculatorFunctionApplicationNode(fn, parseArgs());
                 } else {
                     result = new CalculatorVariableNode(identifier);
                 }
             }
             return result;
         } else {
             throw new CalculatorError("unexpected '" + lexer.token() + "'");
         }
     }
     
     // '(' expr [ ',' expr ] ')'
     private List<CalculatorAstNode> parseArgs() {
         final List<CalculatorAstNode> result = new LinkedList<CalculatorAstNode>();
         expect(CalculatorToken.OPEN_PARENTHESIS);
         while (lexer.token() != CalculatorToken.CLOSE_PARENTHESIS) {
             result.add(parseExpr());
             if (lexer.token() == CalculatorToken.COMMA) {
                 expect(CalculatorToken.COMMA);
                 continue;
             }
         }
         expect(CalculatorToken.CLOSE_PARENTHESIS);
         return result;
     }
     
     private void expect(CalculatorToken what) {
         if (lexer.token() != what) {
             throw new CalculatorError("expected " + what + ", got " + lexer.token() + " instead");
         }
         lexer.nextToken();
     }
     
     @Test private static void testArithmetic() {
         Assert.equals(new Calculator("0").evaluate(), "0");
         Assert.equals(new Calculator("1").evaluate(), "1");
         Assert.equals(new Calculator("-1").evaluate(), "-1");
         Assert.equals(new Calculator("--1").evaluate(), "1");
         Assert.equals(new Calculator("1.00").evaluate(), "1.00");
         
         Assert.equals(new Calculator("1+2+3").evaluate(), "6");
         Assert.equals(new Calculator("1+-2").evaluate(), "-1");
         Assert.equals(new Calculator("3-2-1").evaluate(), "0");
         Assert.equals(new Calculator("10000+0.001").evaluate(), "10000.001");
         Assert.equals(new Calculator("0.001+10000").evaluate(), "10000.001");
         Assert.equals(new Calculator("10000-0.001").evaluate(), "9999.999");
         Assert.equals(new Calculator("0.001-10000").evaluate(), "-9999.999");
         
         Assert.equals(new Calculator("3*4").evaluate(), "12");
         Assert.equals(new Calculator("-3*4").evaluate(), "-12");
         Assert.equals(new Calculator("3*-4").evaluate(), "-12");
         Assert.equals(new Calculator("-3*-4").evaluate(), "12");
         
         Assert.equals(new Calculator("1+2*3").evaluate(), "7");
         Assert.equals(new Calculator("(1+2)*3").evaluate(), "9");
         
         Assert.equals(new Calculator("1/2").evaluate(), "0.5");
         
         Assert.equals(new Calculator("3%4").evaluate(), "3");
         Assert.equals(new Calculator("4%4").evaluate(), "0");
         Assert.equals(new Calculator("5%4").evaluate(), "1");
     }
     
     @Test private static void testRelationalOperations() {
         Assert.equals(new Calculator("1<2").evaluate(), "1");
         Assert.equals(new Calculator("2<2").evaluate(), "0");
         Assert.equals(new Calculator("2<1").evaluate(), "0");
         Assert.equals(new Calculator("1<=2").evaluate(), "1");
         Assert.equals(new Calculator("2<=2").evaluate(), "1");
         Assert.equals(new Calculator("2<=1").evaluate(), "0");
         Assert.equals(new Calculator("1>2").evaluate(), "0");
         Assert.equals(new Calculator("2>2").evaluate(), "0");
         Assert.equals(new Calculator("2>1").evaluate(), "1");
         Assert.equals(new Calculator("1>=2").evaluate(), "0");
         Assert.equals(new Calculator("2>=2").evaluate(), "1");
         Assert.equals(new Calculator("2>=1").evaluate(), "1");
         Assert.equals(new Calculator("1==2").evaluate(), "0");
         Assert.equals(new Calculator("2==2").evaluate(), "1");
         Assert.equals(new Calculator("2==1").evaluate(), "0");
         Assert.equals(new Calculator("1!=2").evaluate(), "1");
         Assert.equals(new Calculator("2!=2").evaluate(), "0");
         Assert.equals(new Calculator("2!=1").evaluate(), "1");
     }
     
     @Test private static void testShifts() {
         Assert.equals(new Calculator("1<<4").evaluate(), "16");
         Assert.equals(new Calculator("(12<<3)>>3").evaluate(), "12");
     }
     
     @Test private static void testBitOperations() {
         Assert.equals(new Calculator("(0x1234 & 0xff0) == 0x230").evaluate(), "1");
         Assert.equals(new Calculator("(0x1200 | 0x34) == 0x1234").evaluate(), "1");
         Assert.equals(new Calculator("5 ^ 3").evaluate(), "6");
         Assert.equals(new Calculator("((0x1234 & ~0xff) | 0x56) == 0x1256").evaluate(), "1");
         Assert.equals(new Calculator("~3").evaluate(), "-4");
         Assert.equals(new Calculator("~~3").evaluate(), "3");
     }
     
     @Test private static void testExponentiation() {
         Assert.equals(new Calculator("2**3").evaluate(), "8");
         Assert.equals(new Calculator("2**3**4").evaluate(), "2417851639229258349412352");
         Assert.equals(new Calculator("4**0.5").evaluate(), "2");
         Assert.equals(new Calculator("-10**2").evaluate(), "-100");
         Assert.equals(new Calculator("(-10)**2").evaluate(), "100");
     }
     
     @Test private static void testConstants() {
         Assert.equals(Double.valueOf(new Calculator("e").evaluate()), Math.E, 0.000001);
         Assert.equals(Double.valueOf(new Calculator("pi").evaluate()), Math.PI, 0.000001);
        Assert.equals(new Calculator("pi == π").evaluate(), "1");
     }
     
     @Test private static void testFunctions() {
         // FIXME: better tests?
         Assert.equals(new Calculator("abs(2)").evaluate(), "2");
         Assert.equals(new Calculator("abs(-2)").evaluate(), "2");
         Assert.equals(new Calculator("acos(1)").evaluate(), "0");
         Assert.equals(new Calculator("asin(0)").evaluate(), "0");
         Assert.equals(new Calculator("acos(0) == asin(1)").evaluate(), "1");
         Assert.equals(new Calculator("atan(0)").evaluate(), "0");
         Assert.equals(new Calculator("cbrt(27)").evaluate(), "3");
         Assert.equals(new Calculator("ceil(1.2)").evaluate(), "2");
         Assert.equals(new Calculator("cos(0)").evaluate(), "1");
         Assert.equals(new Calculator("cos(pi)").evaluate(), "-1");
         Assert.equals(new Calculator("cosh(0)").evaluate(), "1");
         Assert.equals(Double.valueOf(new Calculator("exp(1)/e").evaluate()), 1.0, 0.000001);
         Assert.equals(new Calculator("factorial(5)").evaluate(), "120");
         Assert.equals(new Calculator("factorial(5) == 5!").evaluate(), "1");
         Assert.equals(new Calculator("floor(1.2)").evaluate(), "1");
         Assert.equals(new Calculator("hypot(3, 4)").evaluate(), "5");
         Assert.equals(new Calculator("log(2, 1024)").evaluate(), "10");
         Assert.equals(new Calculator("log2(1024)").evaluate(), "10");
         Assert.equals(new Calculator("logE(exp(4))").evaluate(), "4");
         Assert.equals(new Calculator("log10(1000)").evaluate(), "3");
         Assert.equals(new Calculator("round(1.2)").evaluate(), "1");
         Assert.equals(new Calculator("round(1.8)").evaluate(), "2");
         Assert.equals(new Calculator("sin(0)").evaluate(), "0");
         Assert.equals(new Calculator("sin(pi/2)").evaluate(), "1");
         Assert.equals(new Calculator("sinh(0)").evaluate(), "0");
         Assert.equals(new Calculator("sqrt(81)").evaluate(), "9");
         Assert.equals(new Calculator("tan(0)").evaluate(), "0");
         Assert.equals(new Calculator("tanh(0)").evaluate(), "0");
     }
     
     @Test private static void testSum() {
         Assert.equals(new Calculator("sum(0, 10, i)").evaluate(), "55");
         Assert.equals(new Calculator("sum(0, 10.2, i)").evaluate(), "55");
         Assert.equals(new Calculator("sum(0, 10, i**2)").evaluate(), "385");
         Assert.equals(Double.valueOf(new Calculator("sum(0,30,1/i!)-e").evaluate()), 0.0, 0.000001);
         // FIXME: failure test for min > max.
     }
     
     @Test private static void testProduct() {
         Assert.equals(new Calculator("product(1, 10, i)").evaluate(), "3628800");
         Assert.equals(new Calculator("product(1, 10.2, i)").evaluate(), "3628800");
         Assert.equals(new Calculator("product(1, 6, i**2)").evaluate(), "518400");
         // FIXME: failure test for min > max.
     }
    
 }
