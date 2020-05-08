 /*
  * Copyright (c) 2013, Kyle Mulleady
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions are met:
  *     * Redistributions of source code must retain the above copyright
  *       notice, this list of conditions and the following disclaimer.
  *     * Redistributions in binary form must reproduce the above copyright
  *       notice, this list of conditions and the following disclaimer in the
  *       documentation and/or other materials provided with the distribution.
  *     * Neither the name of the organization nor the
  *       names of its contributors may be used to endorse or promote products
  *       derived from this software without specific prior written permission.
  *
  * THIS SOFTWARE IS PROVIDED BY KYLE MULLEADY ''AS IS'' AND ANY
  * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
  * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
  * DISCLAIMED. IN NO EVENT SHALL KYLE MULLEADY BE LIABLE FOR ANY
  * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
  * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
  * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
  * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
  * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
  * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 
 package ics311km;
 
 /** 
  * BST is an abtract class that defines and implements methods for searching, 
  * inserting, and deleting nodes from a binary search tree.
  *
  * @author Kyle Mulleady
  * @version 1.0
  */
 public abstract class BST implements DynamicSet {
 
     protected int size;
     protected Node root;
 
     public int size() {
         return this.size;
     }
 
     public abstract void insert(KeyType k, Object e);
                                         
     public abstract void delete(KeyType k);
 
     public Object search(KeyType k) {
         Node n = this.nodeSearch(k);
         return n != null ? n.getKey() : null;
     }
                                                                    
     public Object minimum() {
         Node n = this.nodeMinimum(this.root);
         return n != null ? n.getKey() : null;
     }
                                                                                          
     public Object maximum() {
         Node n = this.nodeMaximum(this.root);
         return n != null ? n.getKey() : null;
     }
 
     public Object successor(KeyType k) {
         Node n = this.nodeSuccessor(k);
         return n != null ? n.getKey() : null;
     }
 
     public Object predecessor(KeyType k) {
         Node n = this.nodePredecessor(k);
         return n != null ? n.getKey() : null;
     }
 
     /**
      * Traverses the tree, searching for the node with key k.
      *
      * @param k  the key to be inserted
      * @return   the node with key k, or null if not found
      */
     protected Node nodeSearch(KeyType k) {
         if (this.size == 0)
             return null;
         // Start at the root. 
         Node currentNode = this.root;
         // Make comparisons and move down the tree as necessary.
         while (currentNode != null) {
             if (currentNode.getKey().compareTo(k) == 0) {
                 return currentNode;
             }
             else if (currentNode.getKey().compareTo(k) > 0) {
                 currentNode = currentNode.getLeft();
             }
             else {
                 currentNode = currentNode.getRight();
             }
         }
         // If we made it here, the search was unsuccessful.
         return null;
     }
 
     /**
      * Traverses the tree, finds the appropriate insertion point,
      * then inserts a new node with key k.
      *
     * @param k  the key of the node to be inserted
     * @return   the node inserted
      */
     protected Node nodeInsert(KeyType k, boolean isRBNode) {
         // If the tree is empty, insert at the root.
         if (this.size == 0) {
             this.root = isRBNode ? new RBNode(k) : new BSTNode(k);
             this.size++;
             return this.root;
         }
         // Move down the tree until we find a null node to be our insertion point.
         Node currentNode = this.root;
         Node previousNode = this.root;
         while (currentNode != null) {
             // Keep track of parent node as we move down.
             previousNode = currentNode;
             // If the current node's key is larger than k, move left.
             if (currentNode.getKey().compareTo(k) > 0) {
                 currentNode = currentNode.getLeft();
             }
             // Otherwise, move right.
             else {
                 currentNode = currentNode.getRight();
             }
         }
         // Insert new node.
         currentNode = isRBNode ? new RBNode(k) : new BSTNode(k);
         currentNode.setP(previousNode);
         // Determine whether currentNode will be the left or right child of its parent.
         if (previousNode.getKey().compareTo(k) > 0)
             previousNode.setLeft(currentNode);
         else
             previousNode.setRight(currentNode);
         this.size++;
         return currentNode;
     }
 
     /**
      * Upon successful search for the node n with key k, deletes n.
      *
      * @param k  the key of the node to be deleted
      */
     protected void nodeDelete(KeyType k) {
         Node n = this.nodeSearch(k);
         // If the search was unsuccessful, we're done.
         if (n == null)
             return;
         // If n has no left child, replace it with its right child, which may be null.
         if (n.getLeft() == null) {
             this.transplant(n, n.getRight());
         }
         // If n has no right child, replace it with its left child.
         else if (n.getRight() == null) {
             this.transplant(n, n.getLeft());
         }
         // Otherwise, move n's successor's right child into n's successor's spot, and
         // n's successor into n's spot, then rearrange pointers.
         else {
             Node y = this.nodeMinimum(n.getRight());
             if (y.getP() != n) {
                 this.transplant(y, y.getRight());
                 y.setRight(n.getRight());
                 y.getRight().setP(y);
             }
             this.transplant(n, y);
             y.setLeft(n.getLeft());
             y.getLeft().setP(y);
         }
         this.size--;
     }
 
     // Straight from the book.
     private void transplant(Node oldNode, Node newNode) {
         if (oldNode.getP() == null)
             this.root = newNode;
         else if (oldNode == oldNode.getP().getLeft())
             oldNode.getP().setLeft(newNode);
         else 
             oldNode.getP().setRight(newNode);
         if (newNode != null)
             newNode.setP(oldNode.getP());
     }
 
     protected Node nodeMinimum(Node n) {
         if (this.size == 0)
             return null;
         // Move to the very bottom left of n's subtree.
         while (n.getLeft() != null) {
             n = n.getLeft();
         }
         return n;
     }
 
     protected Node nodeMaximum(Node n) {
         if (this.size == 0)
             return null;
         // Move to the very bottom right of n's subtree.
         while (n.getRight() != null) {
             n = n.getRight();
         }
         return n;
     }
 
     protected Node nodePredecessor(KeyType k) {
         Node n = this.nodeSearch(k);
         if (n == null)
             return null;
         if (n.getLeft() != null) {
             Node min = this.nodeMaximum(n.getLeft());
             return min != null ? min : null;
         }
         Node y = n.getP();
         while (y != null && n == y.getLeft()) {
             n = y;
             y = y.getP();
         }
         return y;
     }
 
     protected Node nodeSuccessor(KeyType k) {
         Node n = this.nodeSearch(k);
         if (n == null)
             return null;
         if (n.getRight() != null) {
             Node max = this.nodeMinimum(n.getRight());
             return max != null ? max : null;
         }
         Node y = n.getP();
         while (y != null && n == y.getRight()) {
             n = y;
             y = y.getP();
         }
         return y;
     }
 
     public void inOrder(Node n) {
         if (n != null) {
              this.inOrder(n.getLeft());
              log(n.getKey().getValue() + " ");
              this.inOrder(n.getRight());
         }
     }
 
     protected void log(Object o) {
         System.out.print(String.valueOf(o));
     }
 
     public String toString() {
         this.inOrder(this.root);
         return "";
     }
 }
