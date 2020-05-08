 package fr.xebia.usiquizz.core.sort;
 
 
 
 /**
  * Inspired from Java TreeMap with refinement to maintain MIN key, Max Key
  * and allow to navigate the tree using NodeSet in both orders.
  */
 
 import java.util.HashMap;
 import java.util.Map;
 
 
 public class LocalBTree<K extends Comparable<K>> {
 
 
     static final int BLACK = 0;
     static final int RED = 1;
 
 
     Node<K> root;
     Node<K> min;
     Node<K> max;
 
 
     public LocalBTree() {
 
     }
 
 
 
     public NodeSet<K> getMaxSet() {
         return new DefaultNodeSet(max);
     }
 
 
     public NodeSet<K> getMinSet() {
         return new DefaultNodeSet(min);
     }
 
 
     public NodeSet<K> getSet(K info) {
         return new DefaultNodeSet(lookupNode(info), true);
     }
 
 
     private int nodeColor(Node<?> n) {
         return n == null ? BLACK : n.color;
     }
 
 
 
     private Node<K> lookupNode(K key) {
         Node<K> n = root;
         while (n != null) {
             int compResult = key.compareTo(n.key);
             if (compResult == 0) {
                 return n;
             } else if (compResult < 0) {
                 n = n.left;
             } else {
                 
                 n = n.right;
             }
         }
         return n;
     }
 
 
     private void rotateLeft(Node<K> n) {
         Node<K> r = n.right;
         replaceNode(n, r);
         n.right = r.left;
         if (r.left != null) {
             Node<K> left = r.left;
             left.parent = n;
         }
         r.left = n;
         n.parent = r;
     }
 
     private void rotateRight(Node<K> n) {
         Node<K> l = n.left;
         replaceNode(n, l);
         n.left = l.right;
         if (l.right != null) {
             Node<K> right = l.right;
             right.parent = n;
         }
         l.right = n;
         n.parent = l;
     }
 
     private void replaceNode(Node<K> oldn, Node<K> newn) {
         if (oldn.parent == null) {
             updateRoot(newn);
         } else {
             Node<K> parent = oldn.parent;
             if (oldn.isLeftNode())
                 parent.left = (newn != null ? newn : null);
             else
                 parent.right = (newn != null ? newn : null);
         }
         if (newn != null) {
             newn.parent = oldn.parent;
         }
     }
 
     public void insert(K key) {
 
         Node<K> insertedNode = new Node<K>(key, RED, null, null);
         Node<K> root = getRoot();
         if (root == null) {
             updateMin(insertedNode);
             updateMax(insertedNode);
             updateRoot(insertedNode);

         } else {
             Node<K> n = root;
             while (true) {
                 int compResult = key.compareTo(n.key);
                 if (compResult == 0) {
                     return;
                 } else if (compResult < 0) {
                     if (n.left == null) {
                         n.left = insertedNode;
                         break;
                     } else {
                         n = n.left;
                     }
                 } else {
                     assert compResult > 0;
                     if (n.right == null) {
                         n.right = insertedNode;
                         break;
                     } else {
                         n = n.right;
                     }
                 }
             }
             insertedNode.parent = n;
         }
         fixAfterInsertion(insertedNode);
 
         // Update min and max if necessary
         if (insertedNode.key.compareTo(getMaxKey()) > 0) {
             updateMax(insertedNode);
         }
 
         if (insertedNode.key.compareTo(getMinKey()) < 0) {
             updateMin(insertedNode);
         }
 
     }
 
     private K getMaxKey(){
 
         return (max == null ? null : max.key);
     }
 
     private K getMinKey(){
 
         return (min == null ? null : min.key);
     }
 
     private K getRootKey(){
         Node<K> root = getRoot();
 
         return (root == null ? null : root.key);
     }
 
 
     /**
      * Gets the root node and maintain a local cache
      * instance to reduce store access
      * @return
      */
     private Node<K> getRoot(){
 
         return root;
     }
 
     /**
      * Stores a new root node in both local instance
      */
     private void updateRoot(Node<K> newRoot){
         root = newRoot;
     }
 
 
     /**
      * Stores a new min node in both local instance
      */
     private void updateMin(Node<K> newRoot){
         min = newRoot;
     }
 
     /**
      * Stores a new min node in both local instance and NodeStore
      */
     private void updateMax(Node<K> newRoot){
         max = newRoot;
     }
 
 
      /** From CLR **/
     private void fixAfterInsertion(Node<K> x) {
         x.color = RED;
 
         while (x != null && (!x.key.equals(getRoot().key) ) && parentOf(x).color == RED) {
             Node<K> parentOfX = parentOf(x);
             Node<K> grandParentOfX = parentOf(parentOfX);
             if (parentOfX.key.equals((grandParentOfX != null ? grandParentOfX.left : null ))) {
                 Node<K> y = rightOf(grandParentOfX);
                 if (colorOf(y) == RED) {
                     setColor(parentOfX, BLACK);
                     setColor(y, BLACK);
                     setColor(grandParentOfX, RED);
                     x = grandParentOfX;
                 } else {
                     if (x.key.equals(parentOfX.right)) {
                         x = parentOfX;
                         rotateLeft(x);
                     }
                     setColor(parentOfX, BLACK);
                     setColor(grandParentOfX, RED);
                     if (grandParentOfX != null)
                         rotateRight(grandParentOfX);
                 }
             } else {
                 Node<K> y = leftOf(grandParentOfX);
                 if (colorOf(y) == RED) {
                     setColor(parentOfX, BLACK);
                     setColor(y, BLACK);
                     setColor(grandParentOfX, RED);
                     x = grandParentOfX;
                 } else {
                     if (x.key.equals(parentOfX.left)) {
                         x = parentOfX;
                         rotateRight(x);
                     }
                     setColor(parentOfX, BLACK);
                     setColor(grandParentOfX, RED);
                     if (grandParentOfX != null)
                         rotateLeft(grandParentOfX);
                 }
             }
         }
         root.color = BLACK;
     }
 
 
     /**
      * Clear the complete Tree 
      */
     public void clear(){
         root = null;
         min = null;
         max = null;
     }
 
 
     public void delete(K key) {
         Node<K> n = lookupNode(key);
         if (n == null)
             return;  // Key not found, do nothing
 
         // Fix Min and Max if necessary
         if (n.key.equals(getMaxKey())) {
             max = max.parent;
         }
         if (n.key.equals(getMinKey())) {
             min = min.parent;
         }
 
         deleteEntry(n);
     }
 
 
     /**
      * From TreeMap
      */
     private void deleteEntry(Node<K> p) {
         // decrementSize();
          K key = p.key;
         // If strictly internal, copy successor's element to p and then make p
         // point to successor.
         if (p.left != null && p.right != null) {
             Node<K> s = successor(p);
             updateNodeKey(p, s.key);
             p = s;
         } // p has 2 children
 
         // Start fixup at replacement node, if it exists.
         Node<K> replacement = (p.right == null) ? p.left : p.right;
 
         if (replacement != null) {
             // Link replacement to p parent and reverse
             updateParent(p, replacement);
 
             if (p.parent == null) {
                 updateRoot(replacement);
             }
             // Null out links so they are OK to use by fixAfterDeletion.
             p.left = p.right = p.parent = null;
 
             // Fix replacement
             if (p.color == BLACK)
                 fixAfterDeletion(replacement);
         } else if (p.parent == null) { // return if we are the only node.
             updateRoot(null);
             updateMin(null);
             updateMax(null);
         } else { // No children. Use self as phantom replacement and unlink.
             if (p.color == BLACK)
                 fixAfterDeletion(p);
 
             if (p.parent != null) {
                 if (p.isLeftNode()) {
                     p.parent.left = null;
 
                 } else {
                     p.parent.right = null;
                 }
                 p.parent = null;
             }
         }
     }
 
 
     /**
      * From CLR *
      */
     private void fixAfterDeletion(Node<K> x) {
 
 
         while (!x.equals(getRoot()) && colorOf(x) == BLACK) {
             Node<K> parentOfX = parentOf(x);
             if (x.equals(parentOfX.left)) {
                 Node<K> sib = rightOf(parentOfX);
 
                 if (colorOf(sib) == RED) {
                     setColor(sib, BLACK);
                     setColor(parentOfX, RED);
                     rotateLeft(parentOfX);
                     sib = rightOf(parentOfX);
                 }
 
                 if (colorOf(leftOf(sib)) == BLACK &&
                         colorOf(rightOf(sib)) == BLACK) {
                     setColor(sib, RED);
                     x = parentOfX;
                 } else {
                     if (colorOf(rightOf(sib)) == BLACK) {
                         setColor(leftOf(sib), BLACK);
                         setColor(sib, RED);
                         rotateRight(sib);
                         sib = rightOf(parentOfX);
                     }
                     setColor(sib, colorOf(parentOfX));
                     setColor(parentOfX, BLACK);
                     setColor(rightOf(sib), BLACK);
                     rotateLeft(parentOfX);
                     x = getRoot();
                 }
             } else { // symmetric
                 Node<K> sib = leftOf(parentOfX);
 
                 if (colorOf(sib) == RED) {
                     setColor(sib, BLACK);
                     setColor(parentOfX, RED);
                     rotateRight(parentOfX);
                     sib = leftOf(parentOfX);
                 }
 
                 if (colorOf(rightOf(sib)) == BLACK &&
                         colorOf(leftOf(sib)) == BLACK) {
                     setColor(sib, RED);
                     x = parentOf(x);
                 } else {
                     if (colorOf(leftOf(sib)) == BLACK) {
                         setColor(rightOf(sib), BLACK);
                         setColor(sib, RED);
                         rotateLeft(sib);
                         sib = leftOf(parentOfX);
                     }
                     setColor(sib, colorOf(parentOfX));
                     setColor(parentOfX, BLACK);
                     setColor(leftOf(sib), BLACK);
                     rotateRight(parentOfX);
                     x = root;
                 }
             }
         }
 
         setColor(x, BLACK);
     }
 
     private int colorOf(Node<K> n) {
         return nodeColor(n);
     }
 
     private void setColor(Node<K> node, int color) {
         if (node !=null){
         node.color = color;
         }
     }
 
 
 
     private Node<K> parentOf(Node<K> node) {
         if (node.parent == null){
             return null;
         }
         return node.parent;
     }
 
 
     private Node<K> leftOf(Node<K> node) {
         if (node == null )
             return null;
         return node.left;
     }
 
     private Node<K> rightOf(Node<K> node) {
         if (node == null)
             return null;
         return node.right;
     }
 
     /**
      * @param node
      * @param newKey
      */
     private void updateNodeKey(Node<K> node, K newKey) {
 /*        Node<K> parent = parentOf(node);
         if (parent != null) {
             if (node.equals(parent.right)) {
                 parent.right = newKey;
             } else if (node.key.equals(parent.left)) {
                 parent.left = newKey;
             }
             store.update(parent);
         }
 
         if (node.left != null) {
             parent = leftOf(node);
             parent.parent = newKey;
             store.update(parent);
         }
         if (node.right != null) {
             parent = rightOf(node);
             parent.parent = newKey;
             store.update(parent);
         }*/
         node.key = newKey;
     }
 
 
     private void updateParent(Node<K> old, Node<K> newn) {
         newn.parent = old.parent;
         Node<K> parent = parentOf(old);
         if (parent != null) {
             if (old.isRightNode()) {
                 parent.right = newn;
             } else {
                 parent.left = newn;
             }
 
         } else {
             newn.parent = null;
         }
     }
 
 
     /* Tree set visitors */
 
     /**
      * Retrieves the next value in tree
      *
      * @param ptr
      * @return
      */
     private Node<K> successor(Node<K> ptr) {
 
         if (ptr == null) {
             return null;
         } else if (ptr.right != null) {
             Node<K> node = ptr.right;
             while (node.left != null) {
                 node = node.left;
             }
             return node;
         } else {
             Node<K> node = ptr.parent;
             Node<K> nch = ptr;
             while (node != null && nch.equals(node.right)) {
                 nch = node;
                 node = node.parent;
             }
             return node;
         }
     }
 
 
     /**
      * Retrieves the previous value in tree
      *
      * @param ptr
      * @return
      */
     private Node<K> predecessor(Node<K> ptr) {
 
         if (ptr == null) {
             return null;
         } else if (ptr.left != null) {
             Node<K> node = ptr.left;
             while (node.right != null) {
                 node = node.right;
             }
             return node;
         } else {
             Node<K> node = ptr.parent;
             Node<K> nch = ptr;
            while (node != null && nch.key.equals(node.left)) {
                 nch = node;
                 node = node.parent;
             }
             return node;
         }
     }
 
 
     private class DefaultNodeSet implements NodeSet<K> {
 
         private Node<K> node;
         private boolean visited = false;
 
 
         public DefaultNodeSet(Node<K> node) {
             this.node = node;
         }
 
         public DefaultNodeSet(Node<K> node, boolean visited) {
             this.node = node;
             this.visited = visited;
         }
 
         @Override
         public K next() {
             if (!visited) {
                 visited = true;
                 return node.key;
             }
             node = successor(node);
             return node != null ? node.key : null;
         }
 
         @Override
         public K prev() {
             if (!visited) {
                 visited = true;
                 return node.key;
             }
             node = predecessor(node);
             return node != null ? node.key : null;
         }
     }
 
 
 /**
  * Created by IntelliJ IDEA.
  * User: slm
  * Date: 27/03/11
  * Time: 17:05
  * To change this template use File | Settings | File Templates.
  */
  class Node<T extends Comparable<T>> {
 
     public T key;
 
     Node<T> parent;
 
     Node<T> left;
 
     Node<T> right;
 
     public int color;
 
     public Node(T information, Node<T> parent) {
         this.key = information;
         this.parent = parent;
         this.left = null;
         this.right = null;
     }
 
     public Node(T key, int nodeColor, Node<T> left, Node<T> right) {
         this.key = key;
         this.color = nodeColor;
         this.left = left;
         this.right = right;
         this.parent = null;
     }
 
 
     boolean isLeaf() {
         return ((left == null) && (right == null));
     }
 
     boolean isNode() {
         return !isLeaf();
     }
 
     boolean hasLeftNode() {
         return (null != left);
     }
 
     boolean hasRightNode() {
         return (right != null);
     }
 
     boolean isLeftNode() {
         return (this.equals(parent.left));
     }
 
     boolean isRightNode() {
         return this.equals(parent.right);
     }
 
 
     public Node<T> grandparent() {
         assert parent != null; // Not the root node
         assert parent.parent != null; // Not child of root
         return parent.parent;
     }
 
     public Node<T> sibling() {
         assert parent != null; // Root node has no sibling
         
         if (isLeftNode())
             return parent.right;
         else
             return parent.left;
     }
 
     public Node<T> uncle() {
         assert parent != null; // Root node has no uncle
 
         assert parent.parent != null; // Children of root have no uncle
         return parent.sibling();
     }
 
     @Override
     public boolean equals(Object o) {
         if (this == o) return true;
         if (o == null || getClass() != o.getClass()) return false;
 
         Node node = (Node) o;
 
         if (key != null ? !key.equals(node.key) : node.key != null) return false;
 
         return true;
     }
 
     @Override
     public int hashCode() {
         return key != null ? key.hashCode() : 0;
     }
 }
 
 }
 
