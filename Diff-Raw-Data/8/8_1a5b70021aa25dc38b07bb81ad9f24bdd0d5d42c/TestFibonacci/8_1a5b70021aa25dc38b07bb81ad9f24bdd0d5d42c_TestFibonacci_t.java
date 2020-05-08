 package recursion;
 
 import static org.junit.Assert.assertEquals;
 import org.junit.Test;
 
 /**
 * Test Fibonacci program.
  * @author David Lin
  */
 public class TestFibonacci {
 
   /**
   * JUnit test on Fibonacci.
    */
   @Test
  public void testFibonacci() {
     Fibonacci test = new Fibonacci();
     int a = test.fibonacci(0);
     int b = test.fibonacci(1);
     int c = test.fibonacci(2);
     int d = test.fibonacci(3);
     int e = test.fibonacci(4);
     int f = test.fibonacci(6);
     int g = test.fibonacci(12);
 
     assertEquals("Fibonacci failed on n=0", a, 0);
     assertEquals("Fibonacci failed on n=1", b, 1);
     assertEquals("Fibonacci failed on n=2", c, 1);
     assertEquals("Fibonacci failed on n=3", d, 2);
     assertEquals("Fibonacci failed on n=4", e, 3);
     assertEquals("Fibonacci failed on n=6", f, 8);
     assertEquals("Fibonacci failed on n=12", g, 144);
   }
 
 }
