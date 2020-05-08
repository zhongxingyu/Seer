 package net.minekingdom.Sepia.environment;
 
 import java.text.ParseException;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Stack;
 
 import org.spout.api.command.CommandSource;
 
 import net.minekingdom.Sepia.environment.heap.Bucket;
 import net.minekingdom.Sepia.environment.heap.Heap;
 import net.minekingdom.Sepia.script.Token;
 import net.minekingdom.Sepia.script.Variable;
 import net.minekingdom.Sepia.script.operator.ArrayBuilder;
 import net.minekingdom.Sepia.script.operator.Grouping;
 import net.minekingdom.Sepia.script.operator.Operator;
 import net.minekingdom.Sepia.script.operator.arithmetic.Addition;
 import net.minekingdom.Sepia.script.operator.arithmetic.Subtraction;
 import net.minekingdom.Sepia.script.value.Value;
 import net.minekingdom.Sepia.script.value.primitive.DecimalValue;
 import net.minekingdom.Sepia.script.value.primitive.IntegerValue;
 import net.minekingdom.Sepia.script.value.primitive.StringValue;
 import net.minekingdom.Sepia.script.value.structure.PlayerValue;
 import net.minekingdom.Sepia.script.value.structure.VectorValue;
 import net.minekingdom.Sepia.script.value.structure.WorldValue;
 
 public class Expression {
     
     private List<Token> execution;
     private Map<String, Variable> variables;
     
     private Expression(List<Token> execution) {
         this.execution = execution;
         this.variables = new HashMap<String, Variable>();
         
         for (Token t : execution) {
             if (t instanceof Variable) {
                 variables.put(t.toString(), (Variable) t);
             }
         }
     }
     
     public boolean hasVariable(String name) {
         return variables.containsKey(name);
     }
     
     public void setVariable(String name, Object value) {
         Variable var = variables.get(name);
         if (var == null) {
             throw new IllegalArgumentException("Variable " + name + " does not exist in expression");
         }
         setVariable(var, value);
     }
     
     public void populate(Heap heap) {
         for (Map.Entry<String, Variable> entry : this.variables.entrySet()) {
             Bucket bucket = heap.get(entry.getKey());
             if (bucket != null) {
                 setVariable(entry.getValue(), bucket.getValue());
             }
         }
     }
     
     public void clearVariables() {
         for (Variable var : this.variables.values()) {
             var.setValue(null);
         }
     }
     
     public Value evaluate(CommandSource source) {
         List<Token> list = new ArrayList<Token>(execution);
         
         for (int i = 0; i < list.size(); i++) {
             Token t = list.get(i);
             if (t instanceof Operator) {
                 Operator o = (Operator) t;
                 LinkedList<Token> l = new LinkedList<Token>();
                 for (int j = 0; j < o.getNumberOfArguments(); j++) {
                     l.addFirst(list.get(i - j - 1));
                 }
                 Token[] values = new Token[l.size()];
                 l.toArray(values);
                 
                 Value result;
                 try {
                     result = o.invoke(source, values);
                 } catch (ClassCastException ex) {
                     List<Token> tmp = new ArrayList<Token>();
                     Collections.addAll(tmp, values);
                     throw new RuntimeException("Cannot apply operation " + t + " to " + tmp);
                 }
                 
                 for (int j = 0; j < o.getNumberOfArguments() + 1; j++) {
                     list.remove(i - o.getNumberOfArguments());
                 }
                 i -= o.getNumberOfArguments();
                 list.add(i, result);
             }
         }
         
         if (list.size() != 1) {
             throw new RuntimeException("Invalid expression");
         }
         
         return (Value) list.get(0);
     }
     
     private void setVariable(Variable var, Object value) {
         var.setValue(Value.getValue(value));
     }
 
     public static Expression build(String expr) throws ParseException {
         return new Expression(getPostfixOrder(expr));
     }
     
     private static List<Token> getPostfixOrder(String expr) throws ParseException {
         
         ShuntingYard sy = new ShuntingYard();
         
         int lastLeftParenthesis = 0;
         main:
         for ( int i = 0; i < expr.length(); i++ ) {
            System.out.println(expr.charAt(i));
            
             if (Character.isDigit(expr.charAt(i))) {
                 int size = 0;
                 while (i + ++size < expr.length() && (isAlphaNumeric(expr.charAt(i + size)) || expr.charAt(i + size) == '.'));
                 String val = expr.substring(i, i + size);
                 
                 try {
                     sy.processValue(new IntegerValue(Long.parseLong(val)));
                 } catch (NumberFormatException ex){
                     try {
                         sy.processValue(new DecimalValue(Double.parseDouble(val)));
                     } catch (NumberFormatException ex2){
                         throw new ParseException("Invalid number format", i);
                     }
                 }
                 i += size - 1;
             } else if (Character.isAlphabetic(expr.charAt(i))) {
                 int size = 0;
                 while (i + ++size < expr.length() && isAlphaNumeric(expr.charAt(i + size)));
                 String val = expr.substring(i, i + size);
                 
                 sy.processVariable(new Variable(val));
                 i += size - 1;
             }  else if (expr.charAt(i) == '"' || expr.charAt(i) == '\'') {
                 char delim = expr.charAt(i);
                 
                 int size = 0;
                 while (i + ++size < expr.length() && (expr.charAt(i + size) != delim || expr.charAt(i + size - 1) == '\\'));
                 if (expr.charAt(i + size) != delim) {
                     throw new ParseException("Mismatching quotation marks", i);
                 }
                 
                 sy.processValue(new StringValue(expr.substring(i + 1, i + size)));
                 i += size;
             } else if (expr.charAt(i) == '(') {
                 lastLeftParenthesis = i;
                 sy.processOperator(Grouping.LEFT_PARENTHESIS, i);
             } else if (expr.charAt(i) == ')') {
                 sy.processRightParenthesis(i);
             } else if (expr.charAt(i) == '<') {
                 int size = 0;
                 while (i + ++size < expr.length() && expr.charAt(i + size) != '>');
                 if (i + size == expr.length()) {
                     throw new ParseException("Mismatching brackets in vector definition", i);
                 }
                 String val = expr.substring(i + 1, i + size);
                 
                 sy.processValue(VectorValue.parse(val));
                 i += size;
             } else if (expr.charAt(i) == '[') {
                 int size = 0;
                 while (i + ++size < expr.length() && expr.charAt(i + size) != ']');
                 if (i + size == expr.length()) {
                     throw new ParseException("Mismatching brackets in vector definition", i);
                 }
                 String val = expr.substring(i + 1, i + size);
                 
                 int n = 0;
                 if (val.length() != 0) {
                     LinkedList<Token> list = new LinkedList<Token>();
                     
                     ++n;
                     int start = val.length();
                     int end = start;
                     while ((start = val.lastIndexOf(',', start - 1)) != -1) {
                         list.addAll(0, getPostfixOrder(val.substring(start + 1, end)));
                         end = start;
                         ++n;
                     }
                     list.addAll(0, getPostfixOrder(val.substring(0, end)));
                     
                     sy.addAll(list);
                 }
                 
                 sy.processArrayBuilder(n, i);
                 i += size;
             } else if (expr.charAt(i) == '@') {
                 int size = 0;
                 while (i + ++size < expr.length() && isAlphaNumeric(expr.charAt(i + size)));
                 String val = expr.substring(i + 1, i + size);
                 
                 sy.processValue(new PlayerValue(val));
                 i += size - 1;
             } else if (expr.charAt(i) == '#') {
                 int size = 0;
                 while (i + ++size < expr.length() && isAlphaNumeric(expr.charAt(i + size)));
                 String val = expr.substring(i + 1, i + size);
 
                 sy.processValue(new WorldValue(val));
                 i += size - 1;
             } else {
                 // Operator handling
                 for (Operator o : Operator.OPERATORS) {
                     if (expr.startsWith(o.toString(), i)) {
                         o.process(sy, i);
                         i += o.toString().length() - 1;
                         continue main;
                     }
                 }
                 
                 if (!Character.isWhitespace(expr.charAt(i))) {
                     throw new ParseException("Syntax error on token \"" + expr.charAt(i) + "\", delete this token", i);
                 }
             }
         }
         
         sy.end(lastLeftParenthesis);
         return new ArrayList<Token>(sy.result);
     }
     
     private static boolean isAlphaNumeric(char c) {
         return c >= 'a' && c <= 'z' 
              || c >= 'A' && c <= 'Z'
              || c >= '0' && c <= '9'
              || c == '_';
     }
 
     public static class ShuntingYard {
         
         private Stack<Operator> operators;
         private List<Token> result;
         
         private Token last;
         
         public ShuntingYard() {
             this.operators = new Stack<Operator>();
             this.result    = new LinkedList<Token>();
         }
 
         public void addAll(List<Token> postfixTokens) {
             this.result.addAll(postfixTokens);
             this.last = this.result.get(this.result.size() - 1);
         }
         
         public void processArrayBuilder(int n, int index) throws ParseException {
             processOperator(new ArrayBuilder(n), index);
             // we have to trick the system because the ArrayBuilder needs to stand as a value
             last = new Value("");
         }
 
         public void processVariable(Variable variable) {
             this.result.add(last = variable);
         }
 
         public void processValue(Value value) {
             this.result.add(last = value);
         }
 
         public void processOperator(Operator o, int index) throws ParseException {
             if ( (result.isEmpty() || last instanceof Operator) 
                     && !(o instanceof Addition.Unary) 
                     && !(o instanceof Subtraction.Unary)
                    && !(o instanceof ArrayBuilder)) {
                 throw new ParseException("Syntax error on token \"" + o + "\", delete this token", index);
             }
             
             while (!operators.isEmpty() && !(operators.peek() instanceof Grouping.LeftParenthesis) && o.getPredecence() >= operators.peek().getPredecence()) {
                 result.add(operators.pop());
             }
             
             operators.push(o);
             last = o;
         }
         
         public void processRightParenthesis(int index) throws ParseException {
             while (!operators.isEmpty() && !(operators.peek() instanceof Grouping.LeftParenthesis)) {
                 result.add(operators.pop());
             }
             if (operators.isEmpty()) {
                 throw new ParseException("Mismatching parenthesis", index);
             }
             operators.pop();
         }
         
         public void end(int index) throws ParseException {
             while (!operators.isEmpty() && !(operators.peek() instanceof Grouping.LeftParenthesis)) {
                 result.add(operators.pop());
             }
             if (!operators.isEmpty()) {
                 throw new ParseException("Mismatching parenthesis", index);
             }
         }
         
         public Token getLastToken() {
             return last;
         }
     }
 }
