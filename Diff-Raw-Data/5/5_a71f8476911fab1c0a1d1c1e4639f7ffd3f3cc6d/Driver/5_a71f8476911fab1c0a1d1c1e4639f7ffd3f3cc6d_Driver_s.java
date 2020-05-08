 package m4rk4l.LinkedList;
 
 import java.lang.Integer;
 
 /**
  * Class that contains the entry point for this application.
  * Could have used JUnit testing.
  *
  * @author Marco Anton.
  */
 public class Driver {
 
     public static int ELEM[] = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9};
 
     /**
      * Entry point for this application.
      * @param args not used.
      */
     public static void main(String[] args) {
 
         LinkedList<Integer> list = new LinkedList<Integer>();
 
         System.out.println("Testing Lists");
         System.out.println("======================");
 
         test_add(list);
         test_remove(list);
         test_add(list);
         System.out.println("\nremove: " + list.remove());
         System.out.println(list.print());
         System.out.println("\nremove: " + list.remove());
         System.out.println(list.print());
         System.out.println("\nremove: " + ELEM[0]);
         list.remove(ELEM[0]);
         System.out.println(list.print());
     }
 
     /**
      * Tests the adding functionality of a linked list.
      * @param l is a linked list to test.
      */
     public static void test_add(MyList<Integer> l) {
         System.out.println("\nAdd method:");
 
 
         for (int i = 0; i < ELEM.length; i++) {
             System.out.println("adding:\t" + ELEM[i]);
             System.out.println("peek:\t" + l.peek());
             System.out.println("before:\t" + l.print());
             l.add(ELEM[i]);
             System.out.println("after:\t" + l.print());
         }
 
     }
 
     /**
      * Test the removing functionality of a linked list.
      * @param l is a linked list to be tested.
      */
     public static void test_remove(MyList<Integer> l) {
         System.out.println("\nRemove method:");
 
         System.out.println("\nRemoving from begining to end");
         for (int i = ELEM.length - 1; i >= 0; i--) {
             System.out.println("removing:\t" + ELEM[i]);
             System.out.println("before:\t" + l.print());
             l.remove(ELEM[i]);
             System.out.println("after:\t" + l.print());
         }
 
         test_add(l);
 
        System.out.println("\nRemoving from end to begining")res. To avoid
        potential overrides, use at least one period character (.) in custom
        tag names.
        ;
         for (int i = ELEM.length - 1; i >= 0; i--) {
             System.out.println("removing:\t" + ELEM[i]);
             System.out.println("before:\t" + l.print());
             l.remove(ELEM[i]);
             System.out.println("after:\t" + l.print());
         }
 
     }
 }
