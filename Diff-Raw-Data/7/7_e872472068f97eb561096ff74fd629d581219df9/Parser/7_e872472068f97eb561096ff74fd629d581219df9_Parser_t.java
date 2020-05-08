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
 
 import edu.osu.cse.meisam.interpreter.tokens.Atom;
 import edu.osu.cse.meisam.interpreter.tokens.CloseParentheses;
 import edu.osu.cse.meisam.interpreter.tokens.Dot;
 import edu.osu.cse.meisam.interpreter.tokens.OpenParentheses;
 import edu.osu.cse.meisam.interpreter.tokens.Token;
 
 // <S> ::= <E>
 // <E> ::= atom
 // <E> ::= ( <X>
 // <X> ::= <E> <Y>
 // <X> ::= ) 
 // <Y> ::= . <E> )
 // <Y> ::= <R> )
 // <R> ::= NIL
 // <R> ::= <E> <R>
 
 /**
  * @author Meisam Fathi Salmi <fathi@cse.ohio-state.edu>
  * 
  */
 public class Parser {
 
     private static final String DEFAULT_ERROR_MESSAGE = "Input is not a valid lisp program";
 
     /**
      * 
      */
     private final Lexer lexer;
 
     /**
      * Current token
      */
     private Token token;
 
     private ParseTree parseTree;
 
     /**
      * @param lexer
      */
     public Parser(final Lexer lexer) {
         this.lexer = lexer;
        this.parseTree = null;
     }
 
     /**
      * @return the parseTree
      */
     public ParseTree getParseTree() {
         return this.parseTree;
     }
 
     private void move() {
         this.token = this.lexer.nextToken();
     }
 
     public boolean hasMoreSExpression() {
         return this.lexer.hasMoreTokens();
     }
 
     public void parseNextSExpresion() {
        if (this.parseTree == null) {
            this.token = this.lexer.nextToken();
        }
         this.parseTree = parseE();
     }
 
     private ParseTree parseE() {
         if (this.token instanceof Atom) {
             return parseAtom();
         } else if (this.token instanceof OpenParentheses) {
             parseOpenParentheses();
             final InternalNode x = parseX();
             final ParseTree leftParseTree = x.getLeftTree();
             final ParseTree rightParseTree = x.getRightTree();
 
             final boolean shouldbeDoted = ((rightParseTree != null) && rightParseTree
                     .hasDotedParent());
             return new InternalNode(leftParseTree, rightParseTree,
                     shouldbeDoted);
         } else { // error
             return raiseParserError("an Atom or an OpenParentheses", this.token
                     .getClass().getSimpleName());
         }
     }
 
     private InternalNode parseX() {
         if (this.token instanceof CloseParentheses) {
             parseCloseParentheses();
             return InternalNode.NILL_LEAF;
         } else if ((this.token instanceof OpenParentheses)
                 || (this.token instanceof Atom)) { // head of E
             final ParseTree leftParseTree = parseE();
             final ParseTree rightParseTree = parseY();
             final boolean shouldbeDoted = (rightParseTree != null)
                     && (rightParseTree.hasDotedParent());
             return new InternalNode(leftParseTree, rightParseTree,
                     shouldbeDoted);
         } else { // error
             return raiseParserError(
                     "a CloseParentheses or an OpenParentheses or an Atom",
                     this.token.getClass().getSimpleName());
         }
     }
 
     private ParseTree parseY() {
         if (this.token instanceof Dot) {
             parseDot();
             final ParseTree parseTree = parseE();
             parseTree.setParentDoted(); // vitally important
             parseCloseParentheses();
             return parseTree;
         } else if ((this.token instanceof OpenParentheses)
                 || (this.token instanceof CloseParentheses)
                 || (this.token instanceof Atom)) {
             final InternalNode parseTree = parseR();
             parseCloseParentheses();
             return parseTree;
         } else {
             return raiseParserError(
                     "a Dot or a CloseParentheses or an OpenParentheses or an Atom",
                     this.token.getClass().getSimpleName());
         }
     }
 
     private InternalNode parseR() {
         if ((this.token instanceof Atom)
                 || (this.token instanceof OpenParentheses)) {
             final ParseTree leftParseTree = parseE();
             final ParseTree rightParseTree = parseR();
             return new InternalNode(leftParseTree, rightParseTree,
                     rightParseTree.hasDotedParent());
         } else if (this.token instanceof CloseParentheses) {
             return InternalNode.NILL_LEAF;
         } else {
             return raiseParserError(Parser.DEFAULT_ERROR_MESSAGE, this.token
                     .getClass().getSimpleName());
         }
     }
 
     private ParseTree parseAtom() {
         if (this.token instanceof Atom) {
             final Token currentToken = this.token;
             move();
             return new LeafNode(currentToken);
         } else {
             return raiseParserError("an Atom", this.token.getClass()
                     .getSimpleName());
         }
     }
 
     private ParseTree parseOpenParentheses() {
         if (this.token instanceof OpenParentheses) {
             final Token currentToken = this.token;
             move();
             return new LeafNode(currentToken);
         } else {
             return raiseParserError("an OpenParentheses", this.token.getClass()
                     .getSimpleName());
         }
     }
 
     private ParseTree parseCloseParentheses() {
         if (this.token instanceof CloseParentheses) {
             final Token currentToken = this.token;
             move();
             return new LeafNode(currentToken);
         } else {
             return raiseParserError("a CloseParentheses", this.token.getClass()
                     .getSimpleName());
         }
     }
 
     private ParseTree parseDot() {
         if (this.token instanceof Dot) {
             final Token currentToken = this.token;
             move();
             return new LeafNode(currentToken);
         } else {
             return raiseParserError("a Dot", this.token.getClass()
                     .getSimpleName());
         }
     }
 
     private InternalNode raiseParserError(final String expected,
             final String found) {
         throw new ParserException("Looking for " + expected + ", but found "
                 + found);
     }
 
 }
