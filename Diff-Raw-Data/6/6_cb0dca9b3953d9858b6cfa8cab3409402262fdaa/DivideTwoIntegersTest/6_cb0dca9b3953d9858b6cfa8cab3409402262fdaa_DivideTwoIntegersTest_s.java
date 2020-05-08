package lcoj;
 
import static org.junit.Assert.*;
 
 import org.junit.Test;
 
 public class DivideTwoIntegersTest {
 
 	@Test
 	public void test() {
 		assertEquals(DivideTwoIntegers.divide(10, 3), 3);
 		assertEquals(DivideTwoIntegers.divide(100, 3), 33);
 		assertEquals(DivideTwoIntegers.divide(15, 3), 5);
 		assertEquals(DivideTwoIntegers.divide(100, 0), -1);
 		assertEquals(DivideTwoIntegers.divide(100, 200), 0);
 		assertEquals(DivideTwoIntegers.divide(-1, 1), -1);
 		assertEquals(DivideTwoIntegers.divide(Integer.MAX_VALUE, 1), Integer.MAX_VALUE);
 		assertEquals(DivideTwoIntegers.divide(Integer.MIN_VALUE, 1), Integer.MIN_VALUE);
 	}
 
 }
