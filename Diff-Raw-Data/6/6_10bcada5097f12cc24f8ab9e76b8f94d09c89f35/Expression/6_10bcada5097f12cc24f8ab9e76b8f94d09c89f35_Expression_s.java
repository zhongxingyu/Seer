 // Copyright (c) 2011, Christopher Pavlina. All rights reserved.
 
 package me.pavlina.alco.ast;
 import me.pavlina.alco.compiler.Env;
 import me.pavlina.alco.compiler.errors.*;
 import me.pavlina.alco.lex.TokenStream;
 import me.pavlina.alco.lex.Token;
 import me.pavlina.alco.language.Type;
 import me.pavlina.alco.language.HasType;
 import me.pavlina.alco.language.Resolver;
 import me.pavlina.alco.language.Keywords;
 import me.pavlina.alco.llvm.LLVMEmitter;
 import me.pavlina.alco.llvm.Function;
 import java.util.Map;
 import java.util.HashMap;
 import java.util.Stack;
 import java.util.List;
 
 /**
  * Expression base class and parser. All actual Expression objects should be
  * a subclass of one of the specialised Expression subclasses.
  * When adding operators, look at the static initialiser for most (only a
  * select few, like function calls, need special wiring). */
 public abstract class Expression extends AST implements HasType
 {
 
     /**
      * Expression parser. This is a modified shunting-yard parser. It reads
      * tokens up to an end marker, and returns the result.
      * @param end End markers. This is a string of single-character
      * Token.OPERs which end the expression. The parser is smart enough not to
      * stop on these while nested.
      * @return Expression, or null if empty
      * @throws CError on syntax error
      */
     public static Expression parse (Env env, TokenStream stream, String end)
             throws CError
     {
         Stack<Operator> stack = new Stack<Operator> ();
         Stack<Expression> output = new Stack<Expression> ();
         Token token;
         Token firstToken = null; // Used for errors when we don't know where
                                  // the hell the error came from
 
         int nest = 0;
         boolean callPossible = false;
         boolean unaryPossible = true;
 
         // This loop only uses peek() at the top for convenience - just about
         // every action we could take on a token requires leaving it in the
         // stream. The bottom of the loop contains a stream.next() to move on.
         while (!(token = stream.peek ()).is (Token.NO_MORE)) {
             if (firstToken == null) firstToken = token;
 
             // Check for end characters. They only mark the end if we are not
             // nested.
             if (token.is (Token.OPER) && token.value.length () == 1) {
                 if (end.indexOf (token.value) >= 0 && nest == 0)
                     break;
             }
 
             // Is this a single value?
             if (token.is (Token.INT)) {
                 output.push (new IntValue (env, stream));
                 callPossible = unaryPossible = false;
 
             } else if (token.is (Token.REAL)) {
                 output.push (new RealValue (env, stream));
                 callPossible = unaryPossible = false;
 
             } else if (token.is (Token.WORD, "null")) {
                 output.push (new NullValue (env, stream));
                 callPossible = unaryPossible = false;
 
             
             } else if (token.is (Token.EXTRA, "$$name")) {
                 output.push (new NameValue (env, stream));
                 callPossible = true;
                 unaryPossible = false;
 
             } else if (token.is (Token.WORD) &&
                        !Keywords.isKeyword (token.value)) {
                 output.push (new NameValue (env, stream));
                 callPossible = true;
                 unaryPossible = false;
             }
 
             // Opening paren?
             else if (token.is (Token.OPER, "(")) {
                 // There are two things that can happen here.
                 // 1. If callPossible is true, then this paren opens a function
                 // call. The function name itself was just pushed to the output
                 // stack; replace it with an OpCall on the operator stack.
                 // 2. If callPossible is false, then this paren is just for
                 // grouping. Push a special OpeningParen object to the
                 // operator stack.
 
                 if (callPossible) {
                     Expression expr = output.pop ();
                     stack.push (new OpCall (expr.getToken (), expr));
                     stack.push (new Expression.OpeningParen (token));
                     // If the next token is ), we have an empty function call.
                     // This confuses shuntOper(), which sees a call as a
                     // unary operator, so we push a placeholder.
                     stream.next ();
                     Token peek = stream.peek ();
                     if (peek.is (Token.OPER, ")"))
                         output.push (new OpComma (peek));
                 } else {
                     stack.push (new Expression.OpeningParen (token));
                     stream.next ();
                 }
                 callPossible = false;
                 unaryPossible = true;
             }
 
             // Closing paren?
             else if (token.is (Token.OPER, ")")) {
                 boolean foundOpen = false;
                 while (! stack.empty ()) {
                     Operator oper = stack.pop ();
                     if (Expression.OpeningParen.class.isInstance (oper)) {
                         foundOpen = true;
                         break;
                     } else {
                         moveOper (oper, output);
                     }
                 }
                 if (!foundOpen) {
                     throw CError.at ("mismatched parentheses", firstToken);
                 } else {
                     if (!stack.empty () &&
                         OpCall.class.isInstance (stack.peek ())) {
                         moveOper (stack.pop (), output);
                     }
                 }
                 callPossible = true;
                 unaryPossible = false;
                 stream.next ();
             }
 
             // Opening square bracket?
             else if (token.is (Token.OPER, "[")) {
                 // This signifies an index, which is like a function call. For
                 // this to show up, we had better have "callPossible".
                 if (!callPossible)
                     throw Unexpected.at ("indexable before [", token);
 
                 Expression expr = output.pop ();
                 stack.push (new OpIndex (expr.getToken (), expr));
                 stack.push (new Expression.OpeningSquare (token));
                 callPossible = false;
                 unaryPossible = true;
                 stream.next ();
             }
 
             // Closing square bracket?
             else if (token.is (Token.OPER, "]")) {
                 boolean foundOpen = false;
                 while (! stack.empty ()) {
                     Operator oper = stack.pop ();
                     if (Expression.OpeningSquare.class.isInstance (oper)) {
                         foundOpen = true;
                         break;
                     } else {
                         moveOper (oper, output);
                     }
                 }
                 if (!foundOpen) {
                     throw CError.at ("mismatched brackets", firstToken);
                 } else {
                     if (OpIndex.class.isInstance (stack.peek ())) {
                         moveOper (stack.pop (), output);
                     } else {
                         throw new RuntimeException ("allowed opening bracket "
                                 + "without indexable");
                     }
                 }
                 callPossible = true;
                 unaryPossible = false;
                 stream.next ();
             }
 
             // Is this an operator?
             else if (token.is (Token.OPER) ||
                      (token.is (Token.WORD) &&
                       Keywords.isKeyword (token.value))) {
                 OperatorCreator creator;
                 String message;
                 if (unaryPossible) {
                     creator = UNOPS.get (token.value);
                     message = "unary operator";
                 } else {
                     creator = BINOPS.get (token.value);
                     message = "binary operator";
                 }
                 callPossible = false;
                 unaryPossible = true;
 
                 if (creator == null)
                     throw Unexpected.at (message, token);
 
                 Operator oper = creator.create (env, stream);
                 shuntOper (oper, stack, output);
             }
 
             // Anything else? It's invalid.
             else {
                 throw CError.at ("invalid in expression", token);
             }
         }
 
         // Empty the stack
         while (!stack.empty ()) {
             Operator oper = stack.pop ();
             if (OpeningParen.class.isInstance (oper))
                 throw CError.at ("mismatched parentheses", oper.getToken ());
             else if (OpeningSquare.class.isInstance (oper))
                 throw CError.at ("mismatched brackets", oper.getToken ());
             moveOper (oper, output);
         }
 
         // Get the item
         if (output.empty ())
             return null;
         Expression expr = output.pop ();
         if (!output.empty ()) {
             // We have a split expression. Try to find the leftmost item to
             // whine at. This may not always be accurate, but we want to give
             // the best error messages possible.
             List<AST> children = expr.getChildren ();
             AST pos = expr;
             while (children != null && children.size () > 0) {
                 pos = children.get (0);
                 children = pos.getChildren ();
             }
             throw CError.at ("split expression", pos.getToken ());
         }
         return expr;
     }
 
     /**
      * Move an operator onto the output, giving it any operands it needs. */
     private static void moveOper (Operator oper, Stack<Expression> output)
         throws CError {
 
         if (oper.getArity () == Arity.UNARY) {
             // Give it one operand.
             if (output.empty ())
                 throw CError.at ("requires one operand", oper.getToken ());
             Expression operand = output.pop ();
             oper.setOperands (operand, null);
         } else {
             // Give it two operands.
             if (output.empty ())
                 throw CError.at ("requires two operands", oper.getToken ());
             Expression right = output.pop ();
             if (output.empty ())
                 throw CError.at ("requires two operands", oper.getToken ());
             Expression left = output.pop ();
             oper.setOperands (left, right);
         }
         output.push (oper);
     }
 
     /**
      * "Shunt" the operator through the shunting yard. This is the most
      * important part of the algorithm, even if it's very small. This is what
      * makes associativity and precedence work. */
     private static void shuntOper (Operator oper, Stack<Operator> stack,
                                    Stack<Expression> output) throws CError {
         while (! stack.empty ()) {
             Operator top = stack.peek ();
             // This is a big conditional. Break it up for readability
             boolean p = oper.getAssociativity () == Associativity.LEFT;
             boolean q = oper.getPrecedence () <= top.getPrecedence ();
             boolean r = oper.getPrecedence () < top.getPrecedence ();
             if ((p && q) || (!p && r)) {
                 top = stack.pop ();
                 moveOper (top, output);
             } else
                 break;
         }
         stack.push (oper);
     }
 
     /**
      * Get the LLVM value string. This is any string which is a valid value
      * in the LLVM code. It is assumed that AST#genLLVM() has been run
      * before calling this.
      */
     public abstract String getValueString ();
 
     /**
      * Get the type of the expression. */
     public abstract Type getType ();
 
     /**
      * Check whether getting a pointer to the value is possible. This should
      * throw a proper exception if not.
      * @param write Whether write access to the pointer is required
      * @param token Token to throw an exception on if not possible
      */
     public abstract void checkPointer (boolean write, Token token)
         throws CError;
 
     /**
      * Get a pointer to the value, if possible. This is used IN PLACE OF
      * genLLVM() and getValueString(), and the behavior when calling both
      * is undefined. This can write code if it needs to (for example, to access
      * an array element). The behavior when calling this on an object for which
      * checkPointer() throws is undefined (though if you're a good programmer,
      * you'll make this function empty in that case...)
      */
     // I just know that after writing "if you're a good programmer", I'll think
     // of some reason to break my own rule :-)
     // -- Yep, I did. See OpCast.
     public abstract String getPointer (Env env, LLVMEmitter emitter,
                                        Function function);
 
     public enum Associativity {LEFT, RIGHT}
     public enum Arity {UNARY, BINARY}
 
     /**
      * Operator. All operators must extend this. */
     public static abstract class Operator extends Expression
     {
         /**
          * Return the operator precedence. The semantics of this number are
          * unspecified, other than that operators which return higher numbers
          * have higher precedence. */
         public abstract int getPrecedence ();
 
         /**
          * Return the associativity. Left-associative operators are as if read
          * from left to right, and vice versa. */
         public abstract Associativity getAssociativity ();
 
         /**
          * Return the arity. */
         public abstract Arity getArity ();
 
         /**
          * Set the operands. For binary operators, the semantics are obvious;
          * for unary operators, the operands will be in 'left', and 'right'
          * will be null. */
         public abstract void setOperands (Expression left, Expression right);
     }
 
     /**
      * Private class representing an opening parenthesis. This is a sentinel
      * value, and very un-Operator-like. Bad. Very bad. :-) */
     private static class OpeningParen extends Operator {
         private Token token;
         public OpeningParen (Token token) { this.token = token; }
         public int getPrecedence () { return 0; }
         public Associativity getAssociativity () { return Associativity.LEFT; }
         public Arity getArity () { return Arity.UNARY; }
         public void setOperands (Expression left, Expression right) {}
         public String getValueString () { return ""; }
         public Token getToken () { return token; }
         public List<AST> getChildren () { return null; }
         public void checkTypes (Env env, Resolver r) throws CError {}
         public Type getType () { return null; }
         public void genLLVM (Env env, LLVMEmitter emitter, Function f) {}
         public void checkPointer (boolean w, Token t) throws CError {}
         public String getPointer (Env e, LLVMEmitter em, Function f) {
             return null; }
         public void print (java.io.PrintStream out) {}
     }
         
     /**
      * Private class representing an opening square bracket. This is a sentinel
      * value, and very un-Operator-like. Bad. Very bad. :-) */
     private static class OpeningSquare extends Operator {
         private Token token;
         public OpeningSquare (Token token) { this.token = token; }
         public int getPrecedence () { return 0; }
         public Associativity getAssociativity () { return Associativity.LEFT; }
         public Arity getArity () { return Arity.UNARY; }
         public void setOperands (Expression left, Expression right) {}
         public String getValueString () { return ""; }
         public Token getToken () { return token; }
         public List<AST> getChildren () { return null; }
         public void checkTypes (Env env, Resolver r) throws CError {}
         public Type getType () { return null; }
         public void genLLVM (Env env, LLVMEmitter emitter, Function f) {}
         public void checkPointer (boolean w, Token t) throws CError {}
         public String getPointer (Env e, LLVMEmitter em, Function f) {
             return null; }
         public void print (java.io.PrintStream out) {}
     }
 
     /**
      * Operator creator interface */
     protected static interface OperatorCreator {
         /**
          * Create and return an operator, given Env and TokenStream. */
         public Operator create (Env env, TokenStream stream) throws CError;
     }
 
     /**
      * Map of all binary operators. This maps the operator text to an
      * OperatorCreator. */
     private static Map<String, OperatorCreator> BINOPS;
 
     /**
      * Map of all unary operators. This maps the operator text to an
      * OperatorCreator. */
     private static Map<String, OperatorCreator> UNOPS;
 
     /**
      * Operator-loading static initialiser. This loads all operators into the
      * map. */
     static {
         BINOPS = new HashMap<String, OperatorCreator> ();
         UNOPS = new HashMap<String, OperatorCreator> ();
 
         BINOPS.put ("=", OpAssign.CREATOR);
         BINOPS.put (",", OpComma.CREATOR);
         // Yes, it's unary, but it is placed like a binary
         BINOPS.put ("as", OpCast.CREATOR);
         //BINOPS.put (".", OpMember.CREATOR);
         
         UNOPS.put ("*", OpDeref.CREATOR);
     }
 }
