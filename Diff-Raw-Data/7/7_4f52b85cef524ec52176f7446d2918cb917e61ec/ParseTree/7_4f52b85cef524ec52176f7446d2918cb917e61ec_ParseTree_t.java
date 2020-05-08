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
 
 import edu.osu.cse.meisam.interpreter.tokens.Token;
 
 /**
  * @author Meisam Fathi Salmi <fathi@cse.ohio-state.edu>
  * 
  */
 public class ParseTree {
 
     /**
      * 
      */
     private final ParseTree leftTree;
 
     /**
      * 
      */
     private final ParseTree rightTree;
 
     /**
      * 
      */
     private final Token token;
 
     /**
      * @param token
      * @param leftTree
      * @param rightTree
      */
     public ParseTree(final Token token, final ParseTree leftTree,
             final ParseTree rightTree) {
         this.token = token;
         this.leftTree = leftTree;
         this.rightTree = rightTree;
     }
 
     /**
      * @param token
      */
     public ParseTree(final Token token) {
         this(token, null, null);
     }
 
     /**
     * @return the token
     */
    public Token getToken() {
        return token;
    }

    /**
      * @return the leftTree
      */
     public ParseTree getLeftTree() {
         return this.leftTree;
     }
 
     /**
      * @return the rightTree
      */
     public ParseTree getRightTree() {
         return this.rightTree;
     }
 
     /**
      * @return tru leftTree is NOT null
      */
     public boolean hasLeftTree() {
         return this.leftTree != null;
     }
 
     /**
      * @return true if rightTree is NOT null
      */
     public boolean hasRightTree() {
         return this.rightTree != null;
     }
 
 }
