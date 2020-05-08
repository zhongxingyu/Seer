 /**
  * Created with IntelliJ IDEA.
  * User: michelle
  * Date: 11/1/12
  * Time: 8:22 PM
  * To change this template use File | Settings | File Templates.
  */
 
 
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
