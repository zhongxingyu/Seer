 import static org.junit.Assert.*;
 
 import java.util.ArrayList;
 
 import org.junit.Test;
 
 
 
 public class PrimeFactorTest {
 	@Test
 	public void testJUnit()
 	{
 		assertTrue("Hope this does not Minge.", true);
 	}
 	
 	@Test
 	public void testPrimeFactorForOne()
 	{
 		assertNull(PrimeFactor.prime(1));
 	}
 	
 	@Test
 	public void testPrimeFactorForTwo()
 	{
 		ArrayList<Integer> list = new ArrayList<Integer>();
 		list.add(2);
 		assertEquals(PrimeFactor.prime(2), list);
 	}
 	
 	@Test
 	public void testPrimeFactorForThree()
 	{
 		ArrayList<Integer> list = new ArrayList<Integer>();
 		list.add(3);
 		assertEquals(PrimeFactor.prime(3), list);
 	}
 	
	@Test
	public void testPrimeFactorForFour()
	{
		ArrayList<Integer> list = new ArrayList<Integer>();
		list.add(2);
		list.add(2);
		assertEquals(PrimeFactor.prime(4), list);
	}
 }
