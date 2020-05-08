 /*
  * Quill and Parchment, a WYSIWYN document editor and rendering engine. 
  *
  * Copyright © 2009 Operational Dynamics Consulting, Pty Ltd
  *
  * The code in this file, and the program it is a part of, is made available
  * to you by its authors as open source software: you can redistribute it
  * and/or modify it under the terms of the GNU General Public License version
  * 2 ("GPL") as published by the Free Software Foundation.
  *
  * This program is distributed in the hope that it will be useful, but WITHOUT
  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
  * FITNESS FOR A PARTICULAR PURPOSE. See the GPL for more details.
  *
  * You should have received a copy of the GPL along with this program. If not,
  * see http://www.gnu.org/licenses/. The authors of this program may be
  * contacted through http://research.operationaldynamics.com/projects/quill/.
  */
 package quill.textbase;
 
 /**
  * A list of Spans.
  * 
  * <p>
  * To access the Spans in this tree, use the visitor pattern via visitAll() or
  * visitRange().
  * 
  * <p>
  * This is implemented as a b-tree, with Node left, Span center, Node right.
  * Most of the useful operations on trees are expressed on the
  * {@link TextChain} class, which wraps one of these to back a Segment and its
  * Editor.
  * 
  * 
  * @author Andrew Cowie
  */
 /*
  * If this becomes an abstract base class, then create a new public class
  * called Tree and replace public usage of Node with it.
  */
 class Node extends Extract
 {
     /**
      * Height of the tree.
      */
     private final int height;
 
     /**
      * The distance off the beginning of the backing Span tree this Node
      * begins, in characters.
      */
     /*
      * TODO Do we need this?
      */
 
     /**
      * Width of this tree, in characters.
      */
     private final int width;
 
     /**
      * The binary tree below and preceeding this Node.
      */
     private final Node left;
 
     /**
      * This Node's content.
      */
     private final Span data;
 
     /**
      * The binary tree below and following this Node.
      */
     private final Node right;
 
     private Node(Span span) {
         height = 1;
         width = span.getWidth();
         data = span;
         left = null;
         right = null;
     }
 
     private Node(Node alpha, Span span, Node omega) {
         final int heightLeft, heightRight;
         final int widthLeft, widthRight, widthCenter;
 
         if (alpha == null) {
             heightLeft = 0;
             widthLeft = 0;
         } else {
             heightLeft = alpha.getHeight();
             widthLeft = alpha.getWidth();
         }
 
         if (omega == null) {
             heightRight = 0;
             widthRight = 0;
         } else {
             heightRight = omega.getHeight();
             widthRight = omega.getWidth();
         }
 
         height = Math.max(heightLeft, heightRight) + 1;
 
         left = alpha;
         right = omega;
 
         if (span == null) {
             data = null;
             widthCenter = 0;
         } else {
             data = span;
             widthCenter = span.getWidth();
         }
 
         width = widthLeft + widthCenter + widthRight;
     }
 
     /**
      * Given a single Span, create a tree. Used in testing, and for the
      * clipboard cases.
      */
     /*
      * Called from Extract.create().
      */
     static Node createNode(Span span) {
         if (span == null) {
             throw new IllegalArgumentException();
         }
         return new Node(span);
     }
 
     /**
      * Create a new tree with the given binary trees below before and after
      * this Node, but with no content of its own. Used for adjoining two
      * trees.
      */
     static Node createNode(Node right, Node left) {
         if (right == null) {
             return left;
         } else if (left == null) {
             return right;
         }
         return new Node(right, null, left);
     }
 
     /**
      * Create a new Node with the given Span as content and the given binary
      * trees below before and after this Node. Used when inserting.
      */
     static Node createNode(Node left, Span span, Node right) {
         if ((left == null) && (right == null)) {
             return new Node(span);
         }
         if (span == null) {
             if (right == null) {
                 return left;
             }
             if (left == null) {
                 return right;
             }
         }
         return new Node(left, span, right);
     }
 
     /**
      * Get the width of this node (and its descendents), in characters.
      */
     public int getWidth() {
         return width;
     }
 
     /**
      * How many levels to the base of the tree? Leaves are 1, and emtpy is 0.
      */
     int getHeight() {
         return height;
     }
 
     Span getSpan() {
         return data;
     }
 
     /**
      * Get a String of the characters in this Node and its descendants. Only
      * use this for minor cases (ie copy to clipboard). Regular usage should
      * visit() across the characters or Spans.
      */
     /*
      * Same inefficient implementation as toString(), without the extra
      * debugging delimiters.
      */
     public String getText() {
         final StringBuilder str;
         final CharacterVisitor tourist;
 
         str = new StringBuilder();
 
         tourist = new CharacterVisitor() {
             public boolean visit(int character, Markup markup) {
                 str.appendCodePoint(character);
                 return false;
             }
         };
 
         if (left != null) {
             left.visit(tourist);
         }
 
         if (data != null) {
             str.append(data.getText());
         }
 
         if (right != null) {
             right.visit(tourist);
         }
 
         return str.toString();
 
     }
 
     Node append(final Span addition) {
         if (addition == null) {
             throw new IllegalArgumentException();
         }
         if (data == null) {
             return new Node(addition);
         } else {
             return new Node(this, addition, null);
         }
     }
 
     /**
      * Invoke tourist's visit() method for each Span in the tree, in-order
      * traversal.
      */
     private boolean visitAll(final SpanVisitor tourist) {
         if (left != null) {
             if (left.visitAll(tourist)) {
                 return true;
             }
         }
         if (data != null) {
             if (tourist.visit(data)) {
                 return true;
             }
         }
         if (right != null) {
             if (right.visitAll(tourist)) {
                 return true;
             }
         }
         return false;
     }
 
     public void visit(final SpanVisitor tourist) {
         visitAll(tourist);
     }
 
     /**
      * Invoke tourist's visit() method for each character in the tree,
      * in-order traversal.
      */
     private boolean visitAll(final CharacterVisitor tourist) {
         final int I;
         int i, ch;
         Markup m;
 
         if (left != null) {
             if (left.visitAll(tourist)) {
                 return true;
             }
         }
         if (data != null) {
             I = data.getWidth();
             m = data.getMarkup();
             for (i = 0; i < I; i++) {
                 ch = data.getChar(i);
                 if (tourist.visit(ch, m)) {
                     return true;
                 }
             }
         }
         if (right != null) {
             return right.visitAll(tourist);
         }
 
         return false;
     }
 
     public void visit(final CharacterVisitor tourist) {
         visitAll(tourist);
     }
 
     /**
      * Call tourist's visit() method for each character in the tree from start
      * for wide characters, traversing the tree in-order.
      */
     private boolean visitRange(final CharacterVisitor tourist, final int offset, final int wide) {
         int i, start, across, consumed;
         final int widthLeft, widthCenter, widthRight;
         int ch;
         Markup m;
 
         consumed = 0;
 
         if (left != null) {
             widthLeft = left.getWidth();
             if ((offset == 0) && (wide >= widthLeft)) {
                 if (left.visitAll(tourist)) {
                     return true;
                 }
                 consumed = widthLeft;
             } else if (offset < widthLeft) {
                 start = offset;
                 if (offset + wide > widthLeft) {
                     across = widthLeft - offset;
                 } else {
                     across = wide;
                 }
 
                 if (left.visitRange(tourist, start, across)) {
                     return true;
                 }
                 consumed = across;
             }
         } else {
             widthLeft = 0;
         }
         if ((data != null) && (consumed != wide)) {
             widthCenter = data.getWidth();
             m = data.getMarkup();
 
             if (offset < widthLeft + widthCenter) {
                 if (offset > widthLeft) {
                     start = offset - widthLeft;
                 } else {
                     start = 0;
                 }
                 across = wide - consumed;
 
                 if (start + across > widthCenter) {
                     across = widthCenter - start;
                 }
                 for (i = start; i < start + across; i++) {
                     ch = data.getChar(i);
                     if (tourist.visit(ch, m)) {
                         return true;
                     }
                 }
                 consumed += across;
             }
         } else {
             widthCenter = 0;
         }
         if ((right != null) && (consumed != wide)) {
             widthRight = right.getWidth();
             if ((offset == widthLeft + widthCenter) && (wide == widthRight)) {
                 if (right.visitAll(tourist)) {
                     return true;
                 }
             } else {
                 start = offset - (widthLeft + widthCenter);
                 across = wide - consumed;
                 if (right.visitRange(tourist, start, across)) {
                     return true;
                 }
             }
         }
         return false;
     }
 
     public void visit(final CharacterVisitor tourist, final int offset, final int wide) {
         visitRange(tourist, offset, wide);
     }
 
     /**
      * Get a representation of this Node showing Node left, Span center and
      * Node right each delimited by «». Use for debugging purposes only!
      */
     /*
      * This is ineffecient!
      */
     public String toString() {
         final StringBuilder str;
         final CharacterVisitor tourist;
 
         str = new StringBuilder();
 
         tourist = new CharacterVisitor() {
             public boolean visit(int character, Markup markup) {
                 str.appendCodePoint(character);
                 return false;
             }
         };
 
         str.append("«");
         if (left != null) {
             left.visit(tourist);
         }
         str.append("»\n");
 
         str.append("«");
         if (data != null) {
             str.append(data.getText());
         }
         str.append("»\n");
 
         str.append("«");
         if (right != null) {
             right.visit(tourist);
         }
         str.append("»\n");
 
         return str.toString();
     }
 
     Span getSpanAt(final int offset) {
         final int widthLeft, widthCenter;
 
         if (offset == width) {
             return null;
         }
         if (offset > width) {
             throw new IndexOutOfBoundsException();
         }
 
         if (left == null) {
             widthLeft = 0;
         } else {
             widthLeft = left.getWidth();
         }
 
         if (offset < widthLeft) {
             return left.getSpanAt(offset);
         }
 
        widthCenter = data.getWidth();
         if (offset - widthLeft < widthCenter) {
             return data;
         } else {
             return right.getSpanAt(offset - widthCenter - widthLeft);
         }
     }
 
     /*
      * FUTURE It may be that nothing in the production code actually uses
      * this! Given how single Span creation is the common case for the user
      * typing, if we fix up InsertTextChange to persist just Spans (rather
      * than automatically putting them into an Extract) or alternately stop
      * using Changes then we can probably use this for real.
      */
     Node insertSpanAt(final int offset, final Span addition) {
         final int widthLeft, widthCenter;
         final Span before, after;
         int point;
         final Node gauche, droit;
 
         if (offset == width) {
             return new Node(this, addition, null);
         }
         if (offset > width) {
             throw new IndexOutOfBoundsException();
         }
 
         if (left == null) {
             widthLeft = 0;
         } else {
             widthLeft = left.getWidth();
         }
 
         if (offset < widthLeft) {
             gauche = left.insertSpanAt(offset, addition);
             return new Node(gauche, data, right);
         }
 
         point = offset - widthLeft;
         if (data == null) {
             widthCenter = 0;
         } else {
             widthCenter = data.getWidth();
         }
         if (point > widthCenter) {
             droit = right.insertSpanAt(point - widthCenter, addition);
             return new Node(left, data, droit);
         }
 
         if (point == 0) {
             droit = new Node(null, data, right);
 
             return new Node(left, addition, droit);
         } else if (point == widthCenter) {
             gauche = new Node(left, data, null);
 
             return new Node(gauche, addition, right);
         } else {
             before = data.split(0, point);
             after = data.split(point);
 
             gauche = new Node(left, before, null);
             droit = new Node(null, after, right);
 
             return new Node(gauche, addition, droit);
         }
     }
 
     /*
      * Logic cloned from insertSpanAt() above.
      */
     Node insertTreeAt(int offset, Node tree) {
         final int widthLeft, widthCenter;
         int point;
         final Node gauche, droit;
         final Span before, after;
 
         if (offset == 0) {
             return new Node(tree, null, this);
         }
         if (offset == width) {
             return new Node(this, null, tree);
         }
         if (offset > width) {
             throw new IndexOutOfBoundsException();
         }
 
         if (left == null) {
             widthLeft = 0;
         } else {
             widthLeft = left.getWidth();
         }
 
         if (offset < widthLeft) {
             gauche = left.insertTreeAt(offset, tree);
             return new Node(gauche, data, right);
         }
 
         point = offset - widthLeft;
         if (data == null) {
             widthCenter = 0;
         } else {
             widthCenter = data.getWidth();
         }
         if (point > widthCenter) {
             droit = right.insertTreeAt(point - widthCenter, tree);
             return new Node(left, data, droit);
         }
 
         if (point == 0) {
             gauche = new Node(left, null, tree);
             droit = new Node(null, data, right);
 
             return new Node(gauche, null, droit);
         } else if (point == widthCenter) {
             gauche = new Node(left, data, null);
             droit = new Node(tree, null, right);
 
             return new Node(gauche, null, droit);
         } else {
             before = data.split(0, point);
             after = data.split(point);
 
             gauche = new Node(left, before, tree);
             droit = new Node(null, after, right);
 
             return new Node(gauche, null, droit);
         }
     }
 
     /**
      * Get a subset of this tree from offset, wide characters in width.
      */
     Node subset(int offset, int wide) {
         final int widthLeft, widthCenter;
         int amount, begin, end;
         Node gauche, droit;
         Span span;
 
         /*
          * Boundary checks
          */
 
         if (offset < 0) {
             throw new IndexOutOfBoundsException("negative offset illegal");
         }
         if (width < 0) {
             throw new IndexOutOfBoundsException("can't subset a negative number of characters");
         }
         if (offset > width) {
             throw new IndexOutOfBoundsException("offset too high");
         }
         if (offset + wide > width) {
             throw new IndexOutOfBoundsException(
                     "requested number of characters greater than available text");
         }
 
         /*
          * Handle corner cases. If we ever adapt this into something that is
          * subclassed rather than all-in-one, then some of this logic will
          * move.
          */
 
         if ((offset == 0) && (wide == width)) {
             return this;
         }
 
         if (wide == 0) {
             return null;
         }
 
         /*
          * Ok, we have a valid range. Is that range entirely left or entirely
          * right?
          */
 
         if (left == null) {
             widthLeft = 0;
         } else {
             widthLeft = left.getWidth();
             if (offset + wide < widthLeft) {
                 return left.subset(offset, wide);
             }
         }
 
         if (data == null) {
             widthCenter = 0;
         } else {
             widthCenter = data.getWidth();
         }
 
         if (right != null) {
             if (offset > widthLeft + widthCenter) {
                 return right.subset(width - offset, wide);
             }
         }
 
         /*
          * Entirely center?
          */
 
         if (wide <= widthCenter) {
             begin = offset - widthLeft;
             end = begin + wide;
             span = data.split(begin, end);
             return new Node(span);
         }
 
         /*
          * So now we know it's at least partially overlapping either left or
          * right.
          */
 
         if (offset < widthLeft) {
             // how many characters?
             amount = widthLeft - offset;
             gauche = left.subset(offset, amount);
             // how many characters remain?
             amount = wide - amount;
         } else {
             gauche = null;
             amount = wide;
         }
 
         if (amount == 0) {
             span = null;
             droit = null;
         } else if (amount == widthCenter) {
             span = data;
             droit = null;
         } else if (amount < widthCenter) {
             span = data.split(0, amount);
             droit = null;
         } else {
             span = data;
             droit = right.subset(0, amount - widthCenter);
         }
 
         return new Node(gauche, span, droit);
     }
 
     int getWordBoundaryBefore(final int offset) {
         final int widthLeft, widthCenter;
         final int result;
         int i, ch, previous;
 
         if (offset == 0) {
             return 0;
         }
 
         if (offset > width) {
             throw new IndexOutOfBoundsException();
         }
 
         if (left == null) {
             widthLeft = 0;
         } else {
             widthLeft = left.getWidth();
         }
 
         /*
          * If it's on the left, then pass the search down, and we're done
          * here.
          */
 
         if (offset < widthLeft) {
             return left.getWordBoundaryBefore(offset);
         }
 
         /*
          * If the offset is on the right, pass it down. But if we don't find
          * it in the right sub node, we need to search here after all.
          */
 
         if (data == null) {
             widthCenter = 0;
         } else {
             widthCenter = data.getWidth();
         }
 
         if (offset > widthLeft + widthCenter) {
             result = right.getWordBoundaryBefore(offset - (widthCenter + widthLeft));
             if (result > 0) {
                 return widthLeft + widthCenter + result;
             }
             i = widthCenter;
         } else {
             i = offset - widthLeft;
         }
 
         /*
          * Search here. We have to handle the corner cases of starting on a
          * space and starting at the end. The previous variable handles the i
          * + 1 case while allowing for the initial loop not needing this.
          */
 
         if (data != null) {
             previous = i;
             if (i == widthCenter) {
                 i--;
             }
 
             while (i >= 0) {
                 ch = data.getChar(i);
 
                 if (isWhitespace(ch)) {
                     return widthLeft + previous;
                 }
 
                 previous = i;
                 i--;
             }
         }
 
         if (left != null) {
             return left.getWordBoundaryBefore(widthLeft);
         }
 
         return 0;
     }
 
     int getWordBoundaryAfter(final int offset) {
         final int widthLeft, widthCenter;
         final int result;
         int i, ch;
 
         if (offset == width) {
             return width;
         }
 
         if (offset > width) {
             throw new IndexOutOfBoundsException();
         }
 
         if (left == null) {
             widthLeft = 0;
         } else {
             widthLeft = left.getWidth();
         }
 
         /*
          * If it's on the left, then pass the search down.
          */
 
         if (offset < widthLeft) {
             result = left.getWordBoundaryAfter(offset);
             if (result != widthLeft) {
                 return result;
             }
         }
 
         /*
          * Otherwise run through this node
          */
 
         if (data == null) {
             widthCenter = 0;
         } else {
             widthCenter = data.getWidth();
         }
 
         if (offset > widthLeft + widthCenter) {
             return right.getWordBoundaryAfter(offset);
         }
 
         if (offset > widthLeft) {
             i = offset - widthLeft;
         } else {
             i = 0;
         }
         while (i < widthCenter) {
             ch = data.getChar(i);
             if (isWhitespace(ch)) {
                 return widthLeft + i;
             }
             i++;
         }
 
         /*
          * Still here? Ok, try going down the right side.
          */
 
         if (right != null) {
             if (i == widthCenter) {
                 return widthLeft + widthCenter + right.getWordBoundaryAfter(0);
             } else {
                 return widthLeft + widthCenter
                         + right.getWordBoundaryAfter(offset - (widthLeft + widthCenter));
             }
         }
 
         return width;
     }
 }
