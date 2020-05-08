 /*
  Parser provides a simple but useful mathematical expression parser.
 
   Copyright (C) 2012 Rafael Rendón Pablo <smart.rendon@gmail.com>
 
   This program is free software: you can redistribute it and/or modify
   it under the terms of the GNU General Public License as published by
   the Free Software Foundation, either version 3 of the License, or
   (at your option) any later version.
 
   This program is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
   GNU General Public License for more details.
 
   You should have received a copy of the GNU General Public License
   along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
 
 import java.io.*;
 import java.util.regex.*;
 import java.util.HashMap;
 import java.util.TreeSet;
 
 public class Parser {
 
   private StringBuffer expression;
   private String token;
   private int tokenType;
   private int errorCode;
   private int index;
   private boolean isValidationMode;
 
   private HashMap<String, Double> variables;
   private HashMap<String, Double> constants;
 
   private TreeSet<String> functions;
   private PrintStream out;
 
   public static final double EPS                = 1e-8;
 
   public static final int DELIMITER             = 0x00000001;
   public static final int VARIABLE              = 0x00000002;
   public static final int CONSTANT              = 0x00000003;
   public static final int FUNCTION              = 0x00000004;
   public static final int NUMBER                = 0x00000005;
   public static final int UNDEFINED             = 0x00000006;
 
   public static final int SUCCESS               = 0x00000007;
   public static final int NO_EXPRESSION         = 0x00000008;
   public static final int LAST_TOKEN_NOT_NULL   = 0x00000009;
   public static final int INVALID_EXPRESSION    = 0x0000000A;
   public static final int INVALID_NUMBER        = 0x0000000B;
   public static final int INVALID_VARIABLE_NAME = 0x0000000C;
   public static final int INVALID_FUNCTION      = 0x0000000D;
   public static final int UNDEFINED_VARIABLE    = 0x0000000E;
   public static final int UNDEFINED_VALUE       = 0x0000000F;
 
   public Parser()
   {
     initialize();
 
     // Utility
     out = System.out;
   }
   public Parser(String expression)
   {
     this.expression = new StringBuffer(expression);
     initialize();
 
     // Utility
     out = System.out;
   }
 
 
   /**
    * Initialize data members, add predefined constants and functions.
    */
   private void initialize()
   {
     variables = new HashMap<String, Double>();
     constants = new HashMap<String, Double>();
     functions = new TreeSet<String>();
 
     constants.put("pi", ExtendedMath.PI);
     constants.put("e", ExtendedMath.E);
 
     String[] temp = new String[] {"sin", "cos", "tan", "log", "ln", "exp",
                                   "abs", "sqrt"};
 
     for (String function : temp)
       functions.add(function);
 
     setErrorCode(SUCCESS);
   }
 
   /**
    * Sets a variable and their corresponding value, useful when we need evaluate
    * a function with a specific parameter.
    * @param variable variable name
    * @param value variable value
    */
   public void setVariable(String variable, double value)
   {
     variables.put(variable, value);
   }
 
   /**
    * Returns the next token in the expression, number, variable, operator, etc.
    * @return String, the next token or empty string if we have finished.
    */
   private boolean nextToken()
   {
     int length = expression.length();
     token = "";
     tokenType = UNDEFINED;
 
     if (index >= length)
       return false;
     while (index < length && expression.charAt(index) == ' ')
       index++;
 
     if (index < length && isDelimiter(expression.charAt(index))) {
       tokenType = DELIMITER;
       token += expression.charAt(index);
       index++;
     } else if (index < length && Character.isLetter(expression.charAt(index))) {
       while (index < length) {
         if (isDelimiter(expression.charAt(index))) {
           break;
         } else if (!Character.isLetter(expression.charAt(index))) {
           setErrorCode(INVALID_VARIABLE_NAME);
         }
 
         token += expression.charAt(index);
         index++;
       }
 
       if (functions.contains(token))
         tokenType = FUNCTION;
       else if (constants.get(token) != null)
         tokenType = CONSTANT;
       else
         tokenType = VARIABLE;
 
     } else if (index < length && Character.isDigit(expression.charAt(index))) {
 
       while (index < length) {
         char at = expression.charAt(index);
         if (at == '-') {
           char prev = expression.charAt(index - 1);
           if (prev != 'e' && prev != 'E')
             break;
         } else {
           if (isDelimiter(at))
             break;
         }
 
         token += at;
         index++;
       }
 
       if (!token.matches("([0-9]+([eE][+-]?[0-9]+)?)|([0-9]+[.][0-9]+([eE][+-]?[0-9]+)?)")) {
         setErrorCode(INVALID_NUMBER);
         return false;
       }
 
       tokenType = NUMBER;
     }
 
     return true;
   }
 
   /**
    * Returns the error code of the previous operation.
    * @return int, 0 if all it's fine, different than zero otherwise.
    */
   public int getErrorCode() {
     return errorCode;
   }
 
   /**
    * Sets the error code for the current operation.
    * @param code the error code: SUCCESS, NO_EXPRESSION, INVALID_NUMBER,
    *             LAST_TOKEN_NOT_NULL, INVALID_EXPRESSION, INVALID_FUNCTION,
    *             UNDEFINED_VARIABLE, UNDEFINED_VALUE.
    */
   public void setErrorCode(int code) {
     errorCode = code;
   }
 
   /**
    * Returns the operation mode, evaluation or validation.
    * @return boolean, true if current mode is validation, false otherwise
    */
   public boolean isValidationMode() {
     return isValidationMode;
   }
 
   /**
    * Sets validation mode status.
    * @param validationMode boolean, true to enable validation mode,
    *                       false to disable
    */
   public void setValidationMode(boolean validationMode) {
     isValidationMode = validationMode;
   }
 
   /**
    * Determines if c is a delimiter.
    * @param c a token
    * @return true if c is a delimiter, false otherwise
    */
   private boolean isDelimiter(char c)
   {
     if ("+-/*%^!=() ".contains("" + c) ||
       c == '\t' || c == '\r')
       return true;
 
     return false;
   }
 
   /**
    * Evaluates additions and subtractions in the expression. Here begins the
    * parsing process and follows the following production:
    *  expression  -> term [+term] [-term]
    *  term        -> factor[*factor] [/factor]
    *  factor      -> number, variable, (expression)
    * @return double, the sum of all terms
    */
   private double sumAndSubtraction()
   {
     if (getErrorCode() != SUCCESS) return 0;
 
     String operator;
     double temp;
     double result = productAndDivision();
 
     while ((operator = token).equals("+") || operator.equals("-")) {
       nextToken();
       temp = productAndDivision();
 
       if (operator.equals("+"))
         result = result + temp;
       if (operator.equals("-"))
         result = result - temp;
     }
 
     return result;
   }
 
   /**
    * Returns the next term in the expression, according to the following
    * production:
    *
    *  expression  -> term [+term] [-term]
    *  term        -> factor[*factor] [/factor]
    *  factor      -> number, variable, (expression)
    * @return double, next term
    */
   private double productAndDivision()
   {
     if (getErrorCode() != SUCCESS) return 0;
 
     String operator;
     double temp, result;
 
     result = exponentAndFactorial();
 
     while ((operator = token).equals("*") || operator.equals("/")) {
       nextToken();
       temp = exponentAndFactorial();
 
       if (operator.equals("*"))
         result = result *  temp;
       if (operator.equals("/")) {
         result = result/temp;
       }
     }
 
     return result;
   }
 
   /**
    * Check for factorial or exponentiation operation for the current value and
    * returns the result, or the value itself.
    * @return double, n! or n^p or n.
    */
   private double exponentAndFactorial()
   {
     if (getErrorCode() != SUCCESS) return 0;
 
     double result = 0;
     result = sign();
     if (token.equals("^")) {
       nextToken();
       double p = exponentAndFactorial();
       result = ExtendedMath.pow(result, p);
     } else if (token.equals("!")) {
       result = ExtendedMath.factorial((int)result);
       nextToken();
     }
 
     return result;
   }
 
   /**
    * Check for plus or minus sign and returns the the result
    * after applying the sign.
    * @return double, +n or -n
    */
   private double sign()
   {
     if (getErrorCode() != SUCCESS) return 0;
 
     String operator = "";
     double result;
 
     if ((tokenType == DELIMITER) && token.equals("+")  || token.equals("-")) {
       operator = token;
       nextToken();
     }
 
     result = subExpression();
     if (operator.equals("-"))
       result = -result;
 
     return result;
   }
 
   /**
    * Evaluate a sub expression( '('expression')' ) and returns the result.
    * @return double, the result of evaluating the sub expression
    */
   private double subExpression()
   {
     if (getErrorCode() != SUCCESS) return 0;
 
     double result;
     if (token.equals("(")) {
       nextToken();
       result = sumAndSubtraction();
       if (!token.equals(")")) {
         setErrorCode(INVALID_EXPRESSION);
         return 0;
       }
 
       nextToken();
     } else {
       result = atom();
     }
     return result;
   }
 
   /**
    * Returns the value of the current factor, number, variable or function.
    * @return double, the value of the current factor
    */
   private double atom()
   {
     if (getErrorCode() != SUCCESS) return 0;
 
     double result = 0;
     if (tokenType == NUMBER) {
       result = Double.parseDouble(token);
       nextToken();
     } else if (tokenType == VARIABLE) {
       if (isValidationMode()) {
         result = 0;
       } else {
         if (!variables.containsKey(token))
           setErrorCode(UNDEFINED_VARIABLE);
         else
           result = variables.get(token);
       }
       nextToken();
     } else if (tokenType == CONSTANT) {
       result = constants.get(token);
       nextToken();
     } else if (tokenType == FUNCTION) {
       String function = token;
       nextToken();
       if (!token.equals("(")) {
         setErrorCode(INVALID_FUNCTION);
         return 0;
       }
 
       double parameter = subExpression();
 
       if (isValidationMode()) {
         if (!functions.contains(function)) {
           setErrorCode(INVALID_FUNCTION);
         }
         return 0;
       }
 
       if (function.equals("sin")) {
         double theta = parameter;
         result = ExtendedMath.sin(theta);
 
       } else if (function.equals("cos")) {
         double theta = parameter;
         result = ExtendedMath.cos(theta);
 
       } else if (function.equals("tan")) {
         result = ExtendedMath.tan(parameter);
 
       } else if (function.equals("log")) {
         if (parameter <= 0) {
           setErrorCode(UNDEFINED_VALUE);
           return 0;
         } else {
           result = ExtendedMath.log10(parameter);
         }
 
       } else if (function.equals("ln")) {
         if (parameter <= 0) {
           setErrorCode(UNDEFINED_VALUE);
           result = 0;
         } else {
           result = ExtendedMath.log(parameter);
         }
 
       } else if (function.equals("abs")) {
         result = ExtendedMath.abs(parameter);
 
       } else if (function.equals("exp")) {
         result = ExtendedMath.exp(parameter);
 
       } else if (function.equals("sqrt")) {
         if (parameter < 0) {
           setErrorCode(UNDEFINED_VALUE);
         } else {
         result = ExtendedMath.sqrt(parameter);
         }
 
       }
 
       // BUG: this call to nextToken is wrong
       //nextToken();
     } else {
       setErrorCode(INVALID_NUMBER);
       return 0;
     }
 
     return result;
   }
 
   /**
    * Evaluates the expression passed in the constructor an returns the result.
    * If there exists variables in the expression it's assumed that they have
    * been initialized, e.g. with setVariable(var, val).
    * @return double, the result of evaluating the expression
    */
   public double evaluate()
   {
     String temp = expression.toString();
     double result = evaluate(temp);
     expression = new StringBuffer(temp);
 
     return result;
   }
 
 
   /**
    * Returns the result of evaluating the expression passed as parameter.
    * If there exists variables in the expression it's assumed that they have
    * been initialized, e.g. with setVariable(var, val).
    * @param expression the expression to evaluate
    * @return double, the result of evaluating the mathematical expression
    */
   public double evaluate(String expression)
   {
     double result = 0;
     this.expression = new StringBuffer(expression);
     index = 0;
 
     setErrorCode(SUCCESS);
     nextToken();
     if (token.equals("")) {
       setErrorCode(NO_EXPRESSION);
       return result;
     }
 
     result = sumAndSubtraction();
 
     if (!token.equals("")) {
       setErrorCode(LAST_TOKEN_NOT_NULL);
       return 0;
     }
 
     return result;
   }
 
   /**
    * Tests if expression is valid, e.g. balanced parentheses, valid operations,
    * valid functions, valid constants, etc.
    * @param expression the expression to examine
    * @return boolean, true if the expression is valid, false otherwise
    */
   public boolean validate(String expression)
   {
     setValidationMode(true);
     evaluate(expression);
     setValidationMode(false);
     return getErrorCode() == SUCCESS;
   }
 
 
 }
 
 /**
 * This class is a wrapper of the java.lang.Math class that provides
  * more additional functionality the the class Parser.
  */
 
 class ExtendedMath {
   public static double PI = java.lang.Math.PI;
   public static double E  = java.lang.Math.E;
 
   public static long factorial(int n)
   {
     long ans = 1;
     
     for (long i = 2; i <= n; i++)
       ans *= i;
 
     return ans;
   }
 
   public static double sin(double theta)
   {
     return java.lang.Math.sin(theta);
   }
 
   public static double cos(double theta)
   {
     return java.lang.Math.cos(theta);
   }
 
   public static double tan(double theta)
   {
     return java.lang.Math.tan(theta);
   }
 
   public static double log10(double x)
   {
     return java.lang.Math.log10(x);
   }
 
   public static double log(double x)
   {
     return java.lang.Math.log(x);
   }
 
   public static double abs(double x)
   {
     return java.lang.Math.abs(x);
   }
 
   public static int abs(int x)
   {
     if (x < 0) x = -x;
     return x;
   }
 
   public static double exp(double x)
   {
     return java.lang.Math.exp(x);
   }
 
   public static double pow(double x, double p)
   {
     return java.lang.Math.pow(x, p);
   }
 
   public static double sqrt(double x)
   {
     return java.lang.Math.sqrt(x);
   }
 
   public static int sign(double n)
   {
     if (n > 0 ) return 1;
     if (n < 0) return -1;
 
     return 0;
   }
 
   public static double round(double value, int precision)
   {
     double p = pow(10, precision);
     return java.lang.Math.floor(value * p)/p;
   }
 
 }
 
