 import junit.framework.Assert;
 import junit.framework.JUnit4TestAdapter;
 
 import org.junit.Test;
 import org.junit.runner.JUnitCore;
 
 public class DequeueUnitTest extends Assert {
     @Test
     public void testConstruction() {
         Dequeue<Integer> foo = new Dequeue<Integer>();
         assertTrue(foo.isEmpty());
         assertTrue(foo.size() == 0);
     }
 
     @Test(expected = java.util.NoSuchElementException.class)
     public void testRemoveFirstFromEmptyDequeue() {
         Dequeue<Integer> foo = new Dequeue<Integer>();
         foo.removeFirst();
     }
 
     @Test(expected = java.util.NoSuchElementException.class)
     public void testRemoveLastFromEmptyDequeue() {
         Dequeue<Integer> foo = new Dequeue<Integer>();
         foo.removeLast();
     }
 
     @Test(expected = java.lang.NullPointerException.class)
     public void testAddFirstNullElement() {
         Dequeue<Integer> foo = new Dequeue<Integer>();
         foo.addFirst(null);
     }
 
     @Test(expected = java.lang.NullPointerException.class)
     public void testAddLastNullElement() {
         Dequeue<Integer> foo = new Dequeue<Integer>();
         foo.addLast(null);
     }
 
     @Test
     public void testWorkflow() {
         Dequeue<Integer> foo = new Dequeue<Integer>();
 
         for (int i = 0; i < 10; ++i) {
             foo.addLast(i);
         }
 
         assertEquals(10, foo.size());
         assertTrue(!foo.isEmpty());
 
         int i = 0;
         for (int elem : foo) {
             assertEquals((int) elem, i);
             ++i;
         }
 
         assertEquals(10, foo.size());
 
         for (i = 9; i >= 0; --i) {
             assertEquals(i, (int) foo.removeLast());
         }
 
         assertTrue(foo.size() == 0);
         assertTrue(foo.isEmpty());
 
         for (i = 0; i < 100; ++i) {
             foo.addFirst(i);
         }
 
         assertTrue(foo.size() == 100);
         assertTrue(!foo.isEmpty());
 
         i = 99;
         for (int elem : foo) {
             assertEquals((int) elem, i);
             --i;
         }
 
        for (i = 99; i >=0; --i) {
             assertEquals((int) foo.removeFirst(), i);
         }
 
         assertTrue(foo.size() == 0);
         assertTrue(foo.isEmpty());
     }
 
     @Test
     public void testEmptyDequeue() {
         Dequeue<Integer> foo = new Dequeue<Integer>();
 
         for (int i : foo) {
             fail();
         }
     }
 
     public static junit.framework.Test suite() {
         return new JUnit4TestAdapter(DequeueUnitTest.class);
     }
 
     public static void main(String[] args) {
         new JUnitCore().main("DequeueUnitTest");
     }
 }
