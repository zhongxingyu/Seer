 /* 2.4 You have two numbers represented by a linked list, where each node
  * contains a single digit. The digits are stored in reserve order, such that
  * the 1's digit is at the head of the list. Write a function that adds the two
  * numbers and returns the sum as a linked list.
  *
  * EXAMPLE:
  * Input: (3->1->5) + (5->9->2)
  * Output: 8->0->8
  * */
 
 import java.util.NoSuchElementException;
 
 public class P0204 {
    private class Node {
      private int digit;
      private Node next;
    }
 
    // Lessons:
    // Pay attention to the movement of the pointers, for example,
    // rr starts from result, and we need to initialize a new node for rr.next
    //
    // Analysis:
    // The space complexity of this solution is O(N), where N is the length of
    // the longer list. Note that the lists are mutable here, we could reduce 
    // the complexity to O(1) by modifying the input directly.
    public Node addWithBuffer(Node a, Node b) {
       if (a.next == null && b.next != null) return b;
       if (a.next != null && b.next == null) return a;
       if (a.next == null && b.next == null) return null;
 
       Node result = new Node();
       result.digit = Integer.MIN_VALUE;
       result.next = null;
       // runner for a
       Node ra = a.next;
       // runner for b
       Node rb = b.next;
       // runner for result
       Node rr = result;
 
       int carrier = 0;
       int tmp = 0;
       while (ra != null && rb != null) {
          Node node = new Node();
          tmp = ra.digit + rb.digit + carrier;
          node.digit = tmp % 10;
          carrier = tmp / 10;
 
          rr.next = node;
          ra = ra.next;
          rb = rb.next;
          rr = rr.next;
       }
 
       if (ra == null && rb != null) {
          System.out.println("ra == null && rb != null");
          Node node = new Node();
          node.digit = rb.digit + carrier;
          rr.next = node;
          node.next = rb.next;
       }
       else if (ra != null && rb == null) {
          Node node = new Node();
          node.digit = ra.digit + carrier;
          rr.next = node;
          node.next = ra.next;
       }
  
       return result;
    }
 
    public Node addWithoutBuffer(Node a, Node b) {
       if (a.next == null && b.next != null) return b;
       if (a.next != null && b.next == null) return a;
       if (a.next == null && b.next == null) return null;
 
       // Use a to record the result
       Node ra = a.next;
       Node rb = b.next;
 
       int carrier = 0;
       int tmp = 0;
       while (ra != null && rb != null) {
          tmp = ra.digit + rb.digit + carrier;
          rb.digit = ra.digit = tmp % 10;
          carrier = tmp / 10;
 
          ra = ra.next;
          rb = rb.next;
       }
 
       if (ra == null && rb != null) {
          rb.digit += carrier;
          return b;
       }
       /* The cases that (ra == null && rb == null) and
        * (ra != null && rb == null) can be merged, since when ra and rb
        * are both null, we could return either of them. (they were modified
        * simultaneously).
        */
       else {
         if (ra != null)
            ra.digit += carrier;

          return a;
       }
    }
 
    public Node initialize(int[] digit) {
       Node head = new Node();
       head.digit = Integer.MIN_VALUE;
       head.next = null;
 
       if (digit == null) {
          throw new NoSuchElementException("Empty inputs");
       }
 
       Node current = head;
       int len = digit.length;
       for (int i = 0; i < len; i++) {
          Node tmp = new Node();
          tmp.digit = digit[i];
          tmp.next = null;
          current.next = tmp;
          current = tmp;
       }
       return head;
    }
 
    public String toString(Node list) {
       StringBuilder s = new StringBuilder();
       Node current = list;
       while (current != null) {
          s.append(current.digit + " ");
          current = current.next;
       }
 
       return s.toString();
    }
 
    public static void main(String[] args) {
       int[] n1 = {3, 1, 5};
       //int[] n1 = {3};
      int[] n2 = {7, 5, 2};
       P0204 p0204 = new P0204();
       Node a = p0204.initialize(n1);
       Node b = p0204.initialize(n2);
       //Node result1 = p0204.addWithBuffer(a, b);
       Node result2 = p0204.addWithoutBuffer(a, b);
       //System.out.println(p0204.toString(result1));
       System.out.println(p0204.toString(result2));
    }
 
 }
