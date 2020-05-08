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
 
 /**
  * @author Meisam Fathi Salmi <fathi@cse.ohio-state.edu>
  * 
  */
 public class InternalNode extends ParseTree {
 
     /**
      * 
      */
     public final static InternalNode NILL_LEAF = new InternalNode(null, null,
             false);
 
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
     private final boolean isDoted;
 
     /**
      * @param leftTree
      * @param rightTree
      * @param isDoted
      * @param token
      */
     public InternalNode(final ParseTree leftTree, final ParseTree rightTree,
             final boolean isDot) {
         this.leftTree = leftTree;
         this.rightTree = rightTree;
         this.isDoted = isDot;
     }
 
     /**
      * @return the isDoted
      */
     public boolean isDoted() {
         return this.isDoted;
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
 
     /*
      * (non-Javadoc)
      * 
      * @see java.lang.Object#toString()
      */
     public String toString() {
         if (this.isDoted) {
             return "<.>";
         } else {
             return "< >";
         }
     }
 
     public int hashCode() {
         final int leftHash = getLeftTree() == null ? 0 : getLeftTree()
                 .hashCode();
         final int rightHash = getRightTree() == null ? 0 : getRightTree()
                 .hashCode();
         if (this.isDoted) {
             return leftHash + rightHash;
         } else {
             return ~(leftHash + rightHash); // bitwise NOT
         }
     }
 
     public boolean equals(final Object obj) {
         if (obj == null) {
             return false;
         }
         if (obj instanceof InternalNode) {
             final InternalNode node = (InternalNode) obj;
             if (this.isDoted != node.isDoted()) {
                 return false;
             }
 
             boolean leftTreesEqual;
             if ((getLeftTree() == null) || (node.getRightTree() == null)) {
                 leftTreesEqual = true;
             } else {
                 leftTreesEqual = ((getLeftTree() != null) && (getLeftTree()
                         .equals(node.getLeftTree())));
             }
 
             boolean rightTreesEqual;
             if ((getLeftTree() == null) || (node.getRightTree() == null)) {
                 rightTreesEqual = true;
             } else {
                 rightTreesEqual = ((getLeftTree() != null) && (getLeftTree()
                         .equals(node.getLeftTree())));
             }
 
             return leftTreesEqual && rightTreesEqual;
 
         }
         return false;
     }
 
 }
