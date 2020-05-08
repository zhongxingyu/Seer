 import static org.junit.Assert.*;
 
 import java.util.ArrayList;
 
 import org.junit.Test;
 
 
 public class PrimeFactorsTest {
 
 	@Test
 	public void testGeneratingFactors1() {
 		assertEquals(new ArrayList<Integer>(), PrimeFactors.generateFactors(1));
 	}
 
 	@Test
 	public void testGeneratingFactors2() {
 		assertEquals(list(2), PrimeFactors.generateFactors(2));
 	}
 	
 	@Test
 	public void testGeneratingFactors3() {
 		assertEquals(list(3), PrimeFactors.generateFactors(3));
 	}
 	
 	@Test
 	public void testGeneratingFactors6() {
 		assertEquals(list(2, 3), PrimeFactors.generateFactors(6));
 	}
 	
 	@Test
 	public void testGeneratingFactors8() {
 		assertEquals(list(2, 2, 2), PrimeFactors.generateFactors(8));
 	}
 	
 	@Test
 	public void testGeneratingFactors9() {
 		assertEquals(list(3, 3), PrimeFactors.generateFactors(9));
 	}
 	
 	@Test
 	public void testGeneratingFactorsLast() {
 		assertEquals(list(5, 7, 7, 1327), PrimeFactors.generateFactors(325115));
 	}
 	
 	@Test
 	public void testLesserPrimes1() {
 		assertEquals(new ArrayList<Integer>(), PrimeFactors.getLesserPrimes(1));
 	}
 	
 	@Test
 	public void testLesserPrimes2() {
		assertEquals(list(1), PrimeFactors.getLesserPrimes(2));
 	}
 	
 	@Test
 	public void testLesserPrimes3() {
		assertEquals(list(1, 2), PrimeFactors.getLesserPrimes(3));
 	}
 	
 	@Test
 	public void testLesserPrimes4() {
		assertEquals(list(1, 2, 3), PrimeFactors.getLesserPrimes(4));
 	}
 	
 	@Test
 	public void testLesserPrimes5() {
		assertEquals(list(1, 2, 3), PrimeFactors.getLesserPrimes(5));
 	}
 	
 	@Test
 	public void testLesserPrimes6() {
		assertEquals(list(1, 2, 3, 5), PrimeFactors.getLesserPrimes(6));
 	}
 	
 	@Test
 	public void testLesserPrimes7() {
		assertEquals(list(1, 2, 3, 5), PrimeFactors.getLesserPrimes(7));
 	}
 	
 	@Test
 	public void testLesserPrimes8() {
		assertEquals(list(1, 2, 3, 5, 7), PrimeFactors.getLesserPrimes(8));
 	}
 	
 	@Test
 	public void testLesserPrimes9() {
		assertEquals(list(1, 2, 3, 5, 7), PrimeFactors.getLesserPrimes(9));
 	}
 	
 	@Test
 	public void testLesserPrimes10() {
		assertEquals(list(1, 2, 3, 5, 7), PrimeFactors.getLesserPrimes(10));
 	}
 	private ArrayList<Integer> list(int... ints){
 		ArrayList<Integer> ret = new ArrayList<Integer>();
 		for (int i : ints){
 			ret.add(i);
 		}
 		return ret;
 	}
 }
