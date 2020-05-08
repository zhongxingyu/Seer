 import java.math.BigInteger;
 import java.util.ArrayList;
 import java.util.List;
 
 public class PrimeSum {
 
 	private List<BigInteger> primes = new ArrayList<BigInteger>();
 	private BigInteger maxNumber = BigInteger.valueOf(2000000);
 	
 	public PrimeSum()
 	{
 		BigInteger thousand = BigInteger.valueOf(10000);
 		System.out.println("Calculating sum of primes below " + maxNumber);
 		for (BigInteger i = BigInteger.valueOf(3); i.compareTo(maxNumber) < 0; i = i.add(BigInteger.valueOf(2)))
 		{
 			if (isPrime(i)) {
 				primes.add(i);
 			}
 			if (i.mod(thousand).compareTo(BigInteger.ONE) == 0) {
 				System.out.println("Current number: " + i);
 			}
 		}
 		
 		BigInteger primeSum = BigInteger.ZERO;
 		for (BigInteger prime : primes)
 		{
 			primeSum = primeSum.add(prime);
 		}
 		System.out.println("Sum of primes: " + primeSum);
 	}
 	
 	private boolean isPrime(BigInteger number)
 	{
 		for (BigInteger prime : primes)
 		{
 			if (number.mod(prime) == BigInteger.ZERO) {
 				return false;
 			}
 		}
 		return true;
 	}
 	
 	public static void main(String[] args)
 	{
 		new PrimeSum();
 	}
 }
