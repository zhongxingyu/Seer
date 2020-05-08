 package edu.msergey.jalg.exercises.ch4.ex19;
 
 import edu.msergey.jalg.exercises.ch4.ex18.IPushdownStack;
 import edu.msergey.jalg.exercises.ch4.ex18.PushdownStackTest;
 import org.junit.Test;
 
 public class StrictArrayBasedStackTest extends PushdownStackTest {
     public final static int MAX_COUNT = 3;
 
     @Override
     protected IPushdownStack<Integer> constructStack() {
         return new StrictArrayBasedStack<Integer>(MAX_COUNT);
     }
 
     @Test (expected = IllegalStateException.class)
     public void test_pop_empty_stack() {
         IPushdownStack<Integer> stack = constructStack();
 
         stack.pop();
     }
 
     @Test (expected = IllegalStateException.class)
     public void test_push_four_items() {
         IPushdownStack<Integer> stack = constructStack();
 
         stack.push(1);
         stack.push(2);
         stack.push(3);
         stack.push(4);
     }
 
     @Test
     public void test_constructor_max_count_is_small_positive() {
         new StrictArrayBasedStack<Integer>(MAX_COUNT);
     }
 
     @Test (expected = IllegalArgumentException.class)
     public void test_constructor_max_count_is_zero() {
         new StrictArrayBasedStack<Integer>(0);
     }
 
     @Test (expected = IllegalArgumentException.class)
     public void test_constructor_max_count_is_negative() {
         new StrictArrayBasedStack<Integer>(-100);
     }
 
     @Test (expected = IllegalArgumentException.class)
     public void test_constructor_max_count_is_max_int() {
        // it will be funny when workstation will have the memory for Integer.MAX_VALUE elements :)
        // but it's only exercise
         new StrictArrayBasedStack<Integer>(Integer.MAX_VALUE);
     }
 
     @Test (expected = IllegalArgumentException.class)
     public void test_push_null() {
         IPushdownStack<Integer> stack = constructStack();
 
         stack.push(null);
     }
 }
