 package cse.buffalo.edu.algorithms.search;
 
 import cse.buffalo.edu.algorithms.stdlib.StdIn;
 import cse.buffalo.edu.algorithms.stdlib.StdOut;
 import cse.buffalo.edu.algorithms.datastructure.queue.Queue;
 import java.util.NoSuchElementException;
 
 public class BST<Key extends Comparable<Key>, Value> {
 
   private Node root;
 
   private class Node {
     private Key key;
     private Value value;
     private Node left, right;
     private int N;
 
     public Node(Key key, Value value) {
       this.key   = key;
       this.value = value;
       this.N = 1;
     }
   }
 
   // This is a very concise, but tricky code.
   public void put(Key key, Value value) {
     root = put(root, key, value);
   }
 
   private Node put(Node x, Key key, Value value) {
     if (x == null) return new Node(key, value);
 
     int cmp = key.compareTo(x.key);
 
     if (cmp == 0)     x.value = value;
     else if (cmp < 0) x.left  = put(x.left, key, value);
     else              x.right = put(x.right, key, value);
 
     x.N = 1 + size(x.left) + size(x.right);
     return x;
   }
 
   // Why we use recusive in put() but not get()
   // because this operation often falls within the
   // inner loop in typical applications.
   public Value get(Key key) {
     Node tmp = root;
     while (tmp != null) {
       int cmp = key.compareTo(tmp.key);
       if (cmp == 0)     return tmp.value;
       else if (cmp < 0) tmp = tmp.left;
       else              tmp = tmp.right;
     }
     return null;
   }
 
   public void deleteMin() {
     if (isEmpty()) throw new NoSuchElementException("Symbol table underflow");
     root = deleteMin(root);
   }
 
   // Need a little time to understand this.
   private Node deleteMin(Node x) {
     if (x.left == null) return x.right;
     x.left = deleteMin(x.left);
     x.N = 1 + size(x.left) + size(x.right);
     // Trciky part. If x != null, return x itself.
     return x;
   }
 
   public void deleteMax() {
     if (isEmpty()) throw new NoSuchElementException("Symbol table underflow");
     root = deleteMax(root);
   }
 
   private Node deleteMax(Node x) {
     if (x.right == null) return x.left;
     x.right = deleteMax(x.right);
     x.N = 1 + size(x.left) + size(x.right);
     return x;
   }
 
   public void delete(Key key) {
     if (!contains(key)) {
       System.err.println("Symbol table does not contian " + key);
       return;
     }
     root = delete(root, key);
   }
 
   private Node delete(Node x, Key key) {
     if (x == null) return null;
     int cmp = key.compareTo(x.key);
     // Search for the key.
    if (cmp < 0)      delete(x.left, key);
    else if (cmp > 0) delete(x.right, key);
     else {
       // Case 1: no right child.
       // You can do the same thing when there is no left child
       // but because no left child can be handle by the case 2,
       // you don't need to do it here
       if (x.right == null) return x.left;
 
       Node t = x;
       // Replace this node with the min node of the right subtree.
       x = min(t.right);
       x.right = deleteMin(t.right);
       x.left = t.left;
     }
     x.N = 1 + size(x.left) + size(x.right);
     return x;
   }
 
   public Key min() {
     if (isEmpty()) return null;
     return min(root).key;
   }
 
   private Node min(Node x) {
     if (x.left == null) return x;
     else               return min(x.left);
   }
 
   public Key max() {
     if (isEmpty()) return null;
     return max(root).key;
   }
 
   private Node max(Node x) {
     if (x.right == null) return x;
     else               return max(x.right);
   }
 
   public Value floor(Key key) {
     return floor(root, key).value;
   }
 
   private Node floor(Node x, Key key) {
     if (x == null) return null;
     int cmp = key.compareTo(x.key);
 
     if (cmp == 0)     return x;
     else if (cmp < 0) return floor(x.left, key);
     else {
       // Find the smallest one in the right section.
       Node t = floor(x.right, key);
       if (t != null)  return t;
       else            return x;
     }
   }
 
   public Value ceiling(Key key) {
     return ceiling(root, key).value;
   }
 
   private Node ceiling(Node x, Key key) {
     if (x == null) return null;
     int cmp = key.compareTo(x.key);
 
     if (cmp == 0)     return x;
     else if (cmp > 0) return ceiling(x.right, key);
     else {
       Node t = floor(x.left, key);
       if (t != null)  return t;
       else            return x;
     }
   }
 
   // Number of elements smaller than the key.
   // It is also a little bit tricky.
   public int rank(Key key) {
     return rank(root, key);
   }
 
   private int rank(Node x, Key key) {
     // Stop condition for the recursion.
     if (x == null) return 0;
     int cmp = key.compareTo(x.key);
 
     if (cmp == 0)     return size(x.left);
     else if (cmp < 0) return rank(x.left, key);
     else              return 1 + size(x.left) + rank(x.right, key);
   }
 
   // Key of given rank.
   public Key select(int k) {
     if (k < 0) return null;
     if (k >= size()) return null;
     Node x = select(root, k);
     return x.key;
   }
 
   private Node select(Node x, int k) {
     if (x == null) return null;
 
     int t = size(x.left);
 
     if (t == k)     return x;
     else if (t < k) return select(x.left, k);
     // The following is the tricky part.
     else            return select(x.right, k-t-1);
   }
 
   public int size() {
     return size(root);
   }
 
   private int size(Node x) {
     if (x == null) return 0;
     else           return x.N;
   }
 
   public boolean isEmpty() {
     return size() == 0;
   }
 
   public boolean contains(Key key) {
     return get(key) != null;
   }
 
   public Iterable<Key> keys() {
     Queue<Key> q = new Queue<Key>();
     inorder(root, q);
     return q;
   }
 
   // Ascending order. A concise and important part.
   private void inorder(Node x, Queue<Key> q) {
     if (x == null) return;
     inorder(x.left, q);
     q.enqueue(x.key);
     inorder(x.right, q);
   }
 
   public static void main(String[] args) {
     BST<String, Integer> st = new BST<String, Integer>();
     for (int i = 0; !StdIn.isEmpty(); i++) {
       String key = StdIn.readString();
       st.put(key, i);
     }
 
     for (String s : st.keys()) {
       StdOut.println(s + " " + st.get(s));
     }
   }
 }
