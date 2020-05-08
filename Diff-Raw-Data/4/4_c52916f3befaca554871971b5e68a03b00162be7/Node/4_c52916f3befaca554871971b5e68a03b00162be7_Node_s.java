 /**
  * Created with IntelliJ IDEA.
  * User: michelle
  * Date: 11/1/12
  * Time: 8:22 PM
  * To change this template use File | Settings | File Templates.
  */
 
 
// Basic node stored in a linked list
// Note that this class is not accessible outside
// of package weiss.nonstandard

 class Node {
 
     public int val;
     public Node next;
 
     // Constructors
     public Node(int value) {
         this(value, null);
     }
 
     public Node(int value, Node n) {
         val = value;
         next = n;
     }
 
 
 }
