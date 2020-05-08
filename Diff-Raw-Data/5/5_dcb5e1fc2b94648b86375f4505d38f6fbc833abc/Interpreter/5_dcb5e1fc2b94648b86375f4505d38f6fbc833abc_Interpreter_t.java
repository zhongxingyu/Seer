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
 
 import java.io.PrintStream;
 import java.util.Iterator;
 import java.util.Vector;
 
 import edu.osu.cse.meisam.interpreter.esxpression.BinaryExpression;
 import edu.osu.cse.meisam.interpreter.esxpression.BooleanAtomExpression;
 import edu.osu.cse.meisam.interpreter.esxpression.LeafExpression;
 import edu.osu.cse.meisam.interpreter.esxpression.NumericAtomExpression;
 import edu.osu.cse.meisam.interpreter.esxpression.SExpression;
 import edu.osu.cse.meisam.interpreter.tokens.Atom;
 import edu.osu.cse.meisam.interpreter.tokens.LiteralAtom;
 import edu.osu.cse.meisam.interpreter.tokens.NumericAtom;
 import edu.osu.cse.meisam.interpreter.tokens.Token;
 
 /**
  * @author Meisam Fathi Salmi <fathi@cse.ohio-state.edu>
  * 
  */
 public class Interpreter {
 
     private final InputProvider in;
     private final Lexer lexer;
     private final Parser parser;
     private final PrintStream out;
 
     public Interpreter(final InputProvider in, final PrintStream out) {
         this.out = out;
         this.in = in;
         this.lexer = new Lexer(this.in);
         this.parser = new Parser(this.lexer);
     }
 
     /**
      * @param args
      */
     public static void main(final String[] args) {
         try {
             final Interpreter interpreter = new Interpreter(
                     new InputStreamProvider(System.in), System.out);
             interpreter.interpret();
         } catch (final Exception ex) {
             System.out.println("Error: " + ex.getMessage());
         }
 
     }
 
     /**
      * 
      */
     public void interpret() {
         try {
             do {
                 final ParseTree parseTree = this.parser.parseNextSExpresion();
                 if (parseTree == null) {
                     break;
                 }
                 final SExpression evaluatedExpression = evaluate(parseTree);
                 prettyPrint(evaluatedExpression);
             } while (true);
         } catch (final Exception e) {
             this.out.append("ERROR: " + e.getMessage());
         }
     }
 
     /**
      * @param parseTree
      */
     private SExpression evaluate(final ParseTree parseTree) {
         if (parseTree instanceof LeafNode) {
             return evaluateNode((LeafNode) parseTree);
         } else if (parseTree instanceof InternalNode) {
             final InternalNode internalNode = (InternalNode) parseTree;
             final ParseTree leftTree = internalNode.getLeftTree();
             final ParseTree rightTree = internalNode.getRightTree();
             if (leftTree != null) {
                 if (leftTree instanceof LeafNode) {
                     return apply((LeafNode) leftTree, rightTree);
                 } else {
                     return raiseInterpreterException("Expecting to see an atom");
                 }
 
             } else if ((leftTree == null) && (rightTree == null)) {
                 return evaluateNil();
             } else {
                 return null;
             }
         } else {
             return raiseInterpreterException("Invalid S-Expression");
         }
     }
 
     private SExpression evaluateNil() {
         return BooleanAtomExpression.NIL;// FIXME
     }
 
     /**
      * @param leafNode
      * @param rightTree
      */
     private SExpression apply(final LeafNode leafNode, final ParseTree rightTree) {
         assertTrue("leafnode is null", leafNode != null);
         final Token token = leafNode.getToken();
         assertTrue("token in the leaf node is null", token != null);
         assertTrue(token.getLexval() + " is not a LiteralAtom",
                 token instanceof LiteralAtom);
         final Atom atom = (LiteralAtom) token;
         final String lexval = atom.getLexval();
         if (lexval.equals("T")) {
             return applyT(rightTree);
         } else if (lexval.equals("NIL")) {
             return applyNil(rightTree);
         } else if (lexval.equals("CAR")) {
             return applyCar(rightTree);
         } else if (lexval.equals("CDR")) {
             return applyCdr(rightTree);
         } else if (lexval.equals("CONS")) {
             return applyCons(rightTree);
         } else if (lexval.equals("ATOM")) {
             return applyAtom(rightTree);
         } else if (lexval.equals("EQ")) {
             return applyEq(rightTree);
         } else if (lexval.equals("NULL")) {
             return applyNull(rightTree);
         } else if (lexval.equals("INT")) {
             return applyInt(rightTree);
         } else if (lexval.equals("PLUS")) {
             return applyPlus(rightTree);
         } else if (lexval.equals("MINUS")) {
             return applyMinus(rightTree);
         } else if (lexval.equals("TIMES")) {
             return applyTimes(rightTree);
         } else if (lexval.equals("QUOTIENT")) {
            return applyQuotient(rightTree);
         } else if (lexval.equals("REMAINDER")) {
             return applyRemainder(rightTree);
         } else if (lexval.equals("LESS")) {
             return applyLess(rightTree);
         } else if (lexval.equals("GREATER")) {
             return applyGreater(rightTree);
         } else if (lexval.equals("COND")) {
             return applyCond(rightTree);
         } else if (lexval.equals("QUOTE")) {
             return applyQuote(rightTree);
         } else if (lexval.equals("DEFUN")) {
             return applyDefun(rightTree);
         }
         return null;
     }
 
     private SExpression applyT(final ParseTree params) {
         return raiseInterpreterException("cannot apply T");
     }
 
     private SExpression applyNil(final ParseTree params) {
         return raiseInterpreterException("cannot apply NIL");
     }
 
     private SExpression applyCar(final ParseTree params) {// FIXME
         final ParseTree firstParam = extractOnlyParameter(params, "CAR");
         final InternalNode firstParamTree = castAsTree(firstParam, "CAR");
         final ParseTree head = getLeftChild(firstParamTree, "CAR");
         return evaluate(head);
     }
 
     private SExpression applyCdr(final ParseTree params) { // FIXME
         final ParseTree firstParam = extractOnlyParameter(params, "CAR");
         final InternalNode firstParamTree = castAsTree(firstParam, "CAR");
         final ParseTree tail = getRightChild(firstParamTree, "CAR");
         return evaluate(tail);
     }
 
     private SExpression applyCons(final ParseTree params) {
         System.out.println("Interpreter.applyCons()");
         return evaluate(params);
     }
 
     private SExpression applyAtom(final ParseTree params) {
         final ParseTree paramTree = extractOnlyParameter(params, "ATOM");
         if (paramTree == InternalNode.NILL_LEAF) {
             return BooleanAtomExpression.T;
         } else if (paramTree instanceof LeafNode) {
             return BooleanAtomExpression.T;
         } else if (paramTree instanceof InternalNode) {
             return BooleanAtomExpression.NIL;
         } else {
             return raiseInterpreterException("Expression of front of ATOM");
         }
     }
 
     private SExpression applyEq(final ParseTree params) {
         return applyAritmeticOperation(params, "EQ");
     }
 
     private SExpression applyNull(final ParseTree params) {
         final ParseTree paramTree = extractOnlyParameter(params, "NULL");
         final SExpression result = evaluate(paramTree);
         if (result == BooleanAtomExpression.NIL) {
             return BooleanAtomExpression.T;
         } else {
             return BooleanAtomExpression.NIL;
         }
     }
 
     private SExpression applyInt(final ParseTree params) {
         final ParseTree paramTree = extractOnlyParameter(params, "INT");
         final SExpression result = evaluate(paramTree);
         if (result instanceof NumericAtomExpression) {
             return BooleanAtomExpression.T;
         } else {
             return BooleanAtomExpression.NIL;
         }
     }
 
     private SExpression applyPlus(final ParseTree params) {
         return applyAritmeticOperation(params, "PLUS");
     }
 
     private SExpression applyMinus(final ParseTree params) {
         return applyAritmeticOperation(params, "MINUS");
     }
 
     private SExpression applyTimes(final ParseTree params) {
         return applyAritmeticOperation(params, "TIMES");
     }
 
    private SExpression applyQuotient(final ParseTree params) {
         return applyAritmeticOperation(params, "QUOTIENT");
     }
 
     private SExpression applyRemainder(final ParseTree params) {
         return applyAritmeticOperation(params, "REMAINDER");
     }
 
     private SExpression applyLess(final ParseTree params) {
         return applyAritmeticOperation(params, "LESS");
     }
 
     private SExpression applyGreater(final ParseTree params) {
         return applyAritmeticOperation(params, "GREATER");
     }
 
     private SExpression applyCond(final ParseTree params) {
         final ParseTree[] allParams = getAllParams(params, "COND");
         assertParamsInPair(allParams, "COND");
         for (int i = 0; i < allParams.length; i++) {
             final ParseTree[] pairParams = getAllParams(allParams[i], "COND");
             assertTrue("Parameters for COND are malformed",
                     pairParams.length == 2);
             final ParseTree conditionParam = pairParams[0];
             final ParseTree bodyParam = pairParams[1];
 
             final SExpression conditionResult = evaluate(conditionParam);
             if (conditionResult == BooleanAtomExpression.T) {
                 return evaluate(bodyParam);
             }
         }
         return raiseInterpreterException("None of the conditions in the COND satisfied");
     }
 
     private SExpression applyQuote(final ParseTree params) {
         System.out.println("Interpreter.applyQuote()");
         return evaluate(params);
     }
 
     private SExpression applyDefun(final ParseTree params) {
         System.out.println("Interpreter.applyDefun()");
         return evaluate(params);
     }
 
     /**
      * @param leafNode
      * @return
      */
     private SExpression evaluateNode(final LeafNode leafNode) {
         final Token token = leafNode.getToken();
         if (token instanceof NumericAtom) {
             return new NumericAtomExpression(token.getLexval());
         } else if (token instanceof LiteralAtom) {
             final LiteralAtom literalAtom = (LiteralAtom) token;
             if (literalAtom.getLexval().equals("T")) {
                 return BooleanAtomExpression.T;
             } else if (literalAtom.getLexval().equals("NIL")) {
                 return BooleanAtomExpression.NIL;
             }
 
             return new NumericAtomExpression(token.getLexval());
         }
         {
             this.out.println("Evaluating some leafnode");
         }
         return null;
     }
 
     private SExpression applyAritmeticOperation(final ParseTree params,
             final String operationName) {
         assertTrue("Looking for a list of two ints in front of "
                 + operationName, params instanceof InternalNode);
 
         final InternalNode paramsTree = (InternalNode) params;
         final ParseTree firstParam = paramsTree.getLeftTree();
         assertTrue("list of parameters to " + operationName
                 + " has less that two elements",
                 firstParam != InternalNode.NILL_LEAF);
 
         final ParseTree tailParams = paramsTree.getRightTree();
         assertTrue("Parameters to " + operationName + " are invalid",
                 tailParams instanceof InternalNode);
         final InternalNode tailParamTree = (InternalNode) tailParams;
 
         final ParseTree secondParam = tailParamTree.getLeftTree();
         assertTrue("list of parameters to " + operationName
                 + " has more that two elements",
                 secondParam != InternalNode.NILL_LEAF);
 
         final ParseTree nilParamTree = tailParamTree.getRightTree();
         assertTrue("list of parameters to " + operationName
                 + " has more than two elements",
                 nilParamTree == InternalNode.NILL_LEAF);
 
         final SExpression evaluatedFirstParam = evaluate(firstParam);
         final SExpression evaluatedSecondParam = evaluate(secondParam);
 
         assertTrue("First param of " + operationName
                 + " should evaluate to a number in ",
                 evaluatedFirstParam instanceof NumericAtomExpression);
         assertTrue("Second param of " + operationName
                 + " should evaluate to a number",
                 evaluatedSecondParam instanceof NumericAtomExpression);
 
         final NumericAtomExpression firstNumber = (NumericAtomExpression) evaluatedFirstParam;
         final NumericAtomExpression secondNumber = (NumericAtomExpression) evaluatedSecondParam;
 
         if (operationName.equals("PLUS")) {
             return new NumericAtomExpression(firstNumber.getVal()
                     + secondNumber.getVal());
         } else if (operationName.equals("MINUS")) {
             return new NumericAtomExpression(firstNumber.getVal()
                     - secondNumber.getVal());
         } else if (operationName.equals("TIMES")) {
             return new NumericAtomExpression(firstNumber.getVal()
                     * secondNumber.getVal());
         } else if (operationName.equals("QUOTIENT")) {
             return new NumericAtomExpression(firstNumber.getVal()
                     / secondNumber.getVal());
         } else if (operationName.equals("REMAINDER")) {
             return new NumericAtomExpression(firstNumber.getVal()
                     % secondNumber.getVal());
         } else if (operationName.equals("EQ")) {
             return firstNumber.getVal() == secondNumber.getVal() ? BooleanAtomExpression.T
                     : BooleanAtomExpression.NIL;
         } else if (operationName.equals("LESS")) {
             return firstNumber.getVal() < secondNumber.getVal() ? BooleanAtomExpression.T
                     : BooleanAtomExpression.NIL;
         } else if (operationName.equals("GREATER")) {
             return firstNumber.getVal() > secondNumber.getVal() ? BooleanAtomExpression.T
                     : BooleanAtomExpression.NIL;
         } else {
             return raiseInterpreterException(operationName
                     + " is not a know Operation");
         }
     }
 
     /**
      * @param params
      * @param functionName
      * @return
      */
     private ParseTree extractOnlyParameter(final ParseTree params,
             final String functionName) {
         if (params == InternalNode.NILL_LEAF) {
             raiseInterpreterException("Exactly one parameter is required for "
                     + functionName);
             return null; // never happens 'cause of raising error in the
                          // previous line
         }
         assertTrue("Exactly one parameter is required for " + functionName,
                 params instanceof InternalNode);
         final InternalNode paramTree = (InternalNode) params;
         assertTrue("Exactly one parameter is required for " + functionName,
                 paramTree.getRightTree() == InternalNode.NILL_LEAF);
         assertTrue("Exactly one parameter is required for " + functionName,
                 paramTree.getLeftTree() != InternalNode.NILL_LEAF);
         return paramTree.getLeftTree();
     }
 
     /**
      * @param firstParamTree
      * @param string
      * @return
      */
     private ParseTree getLeftChild(final InternalNode firstParamTree,
             final String string) {
         assertTrue("Invalid S-Expression as parameter to " + string,
                 firstParamTree.getLeftTree() != null);
         return firstParamTree.getLeftTree();
     }
 
     /**
      * @param firstParamTree
      * @param string
      * @return
      */
     private ParseTree getRightChild(final InternalNode firstParamTree,
             final String string) {
         assertTrue("Invalid S-Expression as parameter to " + string,
                 firstParamTree.getRightTree() != null);
         return firstParamTree.getRightTree();
     }
 
     /**
      * @param paramTree
      * @param string
      * @return
      */
     private InternalNode castAsTree(final ParseTree paramTree,
             final String string) {
         assertTrue(string + " cannot be applied on a null tree",
                 paramTree != InternalNode.NILL_LEAF);
         assertTrue(string + " cannot be applied on a null tree",
                 paramTree instanceof InternalNode);
         return (InternalNode) paramTree;
     }
 
     /**
      * @param params
      * @param string
      * @return
      */
     private ParseTree[] getAllParams(final ParseTree params, final String string) {
         if ((params instanceof LeafNode) || (params == InternalNode.NILL_LEAF)) {
             raiseInterpreterException("No valid list of parameters for "
                     + string);
             return null; // never happens, raising exception in the prev. line
         }
 
         final Vector allParams = new Vector();
         ParseTree currentParam = params;
 
         while ((currentParam != InternalNode.NILL_LEAF)
                 && (currentParam instanceof InternalNode)) {
             final InternalNode paramTree = (InternalNode) currentParam;
             assertTrue("Parameters cannot be null",
                     paramTree.getLeftTree() != null);
             allParams.add(paramTree.getLeftTree());
             currentParam = paramTree.getRightTree();
         }
 
         int i = 0;
         final Iterator iterator = allParams.iterator();
         final ParseTree[] allParamsArray = new ParseTree[allParams.size()];
 
         while (iterator.hasNext()) {
             allParamsArray[i] = (ParseTree) iterator.next();
             i++;
         }
 
         return allParamsArray;
     }
 
     /**
      * @param params
      * @param string
      */
     private void assertParamsInPair(final ParseTree[] params,
             final String string) {
         for (int i = 0; i < params.length; i++) {
             assertTrue("Invalid parameters for " + string, params[i] != null);
             assertPair(string, params[i]);
         }
     }
 
     /**
      * @param string
      * @param parseTree
      */
     private void assertPair(final String functionName, final ParseTree pair) {
         if (pair == InternalNode.NILL_LEAF) {
             raiseInterpreterException("Parameters for " + functionName
                     + " should be pairs");
         }
 
         assertTrue("Parameters for " + functionName + " should be pairs",
                 pair instanceof InternalNode);
         final InternalNode paramTree = (InternalNode) pair;
 
         assertTrue("Parameters for " + functionName + " should be pairs",
                 paramTree.getLeftTree() != InternalNode.NILL_LEAF);
 
         assertTrue("Parameters for " + functionName + " should be pairs",
                 paramTree.getRightTree() != InternalNode.NILL_LEAF);
 
         assertTrue("Parameters for " + functionName + " should be pairs",
                 paramTree.getRightTree() instanceof InternalNode);
 
         final InternalNode secondParam = (InternalNode) paramTree
                 .getRightTree();
 
         assertTrue("Parameters for " + functionName + " should be pairs",
                 secondParam.getLeftTree() != InternalNode.NILL_LEAF);
 
         assertTrue("Parameters for " + functionName + " should be pairs",
                 secondParam.getRightTree() == InternalNode.NILL_LEAF);
 
         assertTrue("Parameters for " + functionName + " should be pairs",
                 secondParam.getRightTree() instanceof InternalNode);
     }
 
     /**
      * @param msg
      * @param b
      */
     private void assertTrue(final String msg, final boolean b) {
         if (!b) {
             raiseInterpreterException(msg);
         }
     }
 
     /**
      * @param msg
      * @return
      */
     private SExpression raiseInterpreterException(final String msg) {
         throw new InterPreterException(msg);
     }
 
     private void prettyPrint(final SExpression expression) {
         if (expression instanceof LeafExpression) {
             this.out.print(expression);
         } else if (expression instanceof BinaryExpression) {
             final BinaryExpression binaryExpression = (BinaryExpression) expression;
             if (binaryExpression.isList()) {
                 prettyPrintList(expression);
             } else {
                 this.out.print("(");
                 prettyPrint(binaryExpression.getHead());
                 this.out.print(" . ");
                 prettyPrint(binaryExpression.getTail());
                 this.out.print(")");
             }
         } else {
             raiseInterpreterException("Unknown structure for S-Expression. "
                     + expression
                     + " in the S-Expression is neither a a leaf nor a binary expression");
         }
     }
 
     private void prettyPrintList(final SExpression expression) {
         this.out.print("(");
         if (expression == BooleanAtomExpression.NIL) {
             this.out.print(")");
         } else if (expression instanceof BinaryExpression) {
             BinaryExpression list = null;
             do {
                 list = (BinaryExpression) expression;
                 this.out.print(list.getHead());
                 if (list.getTail() == BooleanAtomExpression.NIL) {
                     break;
                 }
                 list = (BinaryExpression) list.getTail();
             } while (true);
         }
         final BinaryExpression list = (BinaryExpression) expression;
         this.out.print(list.getHead());
 
     }
 }
