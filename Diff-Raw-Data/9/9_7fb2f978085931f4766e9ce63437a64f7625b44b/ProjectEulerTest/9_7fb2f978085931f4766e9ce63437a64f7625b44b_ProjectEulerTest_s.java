 package com.ssparrow.projecteuler;
 
 import static org.junit.Assert.*;
 
 import java.math.BigInteger;
 
 import org.junit.Test;
 
import com.ssparrow.algorithm.array.Triplet;

 public class ProjectEulerTest {
 
 	@Test
 	public void testP001Find3or5MultipleSum() {
 		assertEquals(0, ProjectEuler.p001Find3or5MultipleSum(0));
 
 		assertEquals(0, ProjectEuler.p001Find3or5MultipleSum(1));
 
 		assertEquals(0, ProjectEuler.p001Find3or5MultipleSum(2));
 
 		assertEquals(0, ProjectEuler.p001Find3or5MultipleSum(3));
 
 		assertEquals(3, ProjectEuler.p001Find3or5MultipleSum(4));
 
 		assertEquals(8, ProjectEuler.p001Find3or5MultipleSum(6));
 
 		assertEquals(23, ProjectEuler.p001Find3or5MultipleSum(10));
 		
 		//3 5 6 9 10 12 15 18
 		assertEquals(78, ProjectEuler.p001Find3or5MultipleSum(20));
 		
 		assertEquals(233168, ProjectEuler.p001Find3or5MultipleSum(1000));
 	}
 	
 	
 	@Test
 	public void testP002FindEvenFibonacciNumberSum(){
 		assertEquals(0, ProjectEuler.p002FindEvenFibonacciNumberSum(0));
 
 		assertEquals(0, ProjectEuler.p002FindEvenFibonacciNumberSum(1));
 
 		assertEquals(2, ProjectEuler.p002FindEvenFibonacciNumberSum(2));
 
 		assertEquals(2, ProjectEuler.p002FindEvenFibonacciNumberSum(3));
 
 		assertEquals(2, ProjectEuler.p002FindEvenFibonacciNumberSum(5));
 
 		assertEquals(10, ProjectEuler.p002FindEvenFibonacciNumberSum(10));
 
 		assertEquals(44, ProjectEuler.p002FindEvenFibonacciNumberSum(100));
 
 		assertEquals(4613732, ProjectEuler.p002FindEvenFibonacciNumberSum(4000000));
 	}
 	
 	
 	@Test
 	public void testP003FindLargestPrimeFactor(){
 		assertEquals(BigInteger.valueOf(1), ProjectEuler.p003FindLargestPrimeFactor(BigInteger.valueOf(1)));
 
 		assertEquals(BigInteger.valueOf(2), ProjectEuler.p003FindLargestPrimeFactor(BigInteger.valueOf(2)));
 
 		assertEquals(BigInteger.valueOf(3), ProjectEuler.p003FindLargestPrimeFactor(BigInteger.valueOf(3)));
 
 		assertEquals(BigInteger.valueOf(2), ProjectEuler.p003FindLargestPrimeFactor(BigInteger.valueOf(4)));
 
 		assertEquals(BigInteger.valueOf(5), ProjectEuler.p003FindLargestPrimeFactor(BigInteger.valueOf(5)));
 
 		assertEquals(BigInteger.valueOf(5), ProjectEuler.p003FindLargestPrimeFactor(BigInteger.valueOf(10)));
 
 		assertEquals(BigInteger.valueOf(5), ProjectEuler.p003FindLargestPrimeFactor(BigInteger.valueOf(15)));
 
 		assertEquals(BigInteger.valueOf(2), ProjectEuler.p003FindLargestPrimeFactor(BigInteger.valueOf(16)));
 
 		assertEquals(BigInteger.valueOf(17), ProjectEuler.p003FindLargestPrimeFactor(BigInteger.valueOf(17)));
 
 		assertEquals(BigInteger.valueOf(5), ProjectEuler.p003FindLargestPrimeFactor(BigInteger.valueOf(20)));
 		
 		assertEquals(BigInteger.valueOf(11), ProjectEuler.p003FindLargestPrimeFactor(BigInteger.valueOf(22)));
 
 		assertEquals(BigInteger.valueOf(6857), ProjectEuler.p003FindLargestPrimeFactor(new BigInteger("600851475143",10)));
 	}
 
 	@Test
 	public void testP004FindLargestPalindromicNumber(){
 		assertTrue(ProjectEuler.isPalindrome(101));
 
 		assertTrue(ProjectEuler.isPalindrome(111));
 
 		assertTrue(ProjectEuler.isPalindrome(1001));
 
 		assertFalse(ProjectEuler.isPalindrome(1011));
 		
 		assertTrue(ProjectEuler.isPalindrome(11211));
 		
 		assertEquals(906609, ProjectEuler.p004FindLargestPalindromicNumber());
 	}
 	
 	@Test
 	public void testP005FindSmallestDivisibleBy1ToN(){
 		
 		assertEquals(1, ProjectEuler.p005FindSmallestDivisibleBy1ToN(1));
 		
 		assertEquals(2, ProjectEuler.p005FindSmallestDivisibleBy1ToN(2));
 
 		assertEquals(6, ProjectEuler.p005FindSmallestDivisibleBy1ToN(3));
 		
 		assertEquals(2520, ProjectEuler.p005FindSmallestDivisibleBy1ToN(10));
 		
 		assertEquals(232792560, ProjectEuler.p005FindSmallestDivisibleBy1ToN(20));
 	}
 	
 	@Test
 	public void testP006FindDiffSumSquareAndSquareSum(){
 		assertEquals(0, ProjectEuler.p006FindDiffSumSquareAndSquareSum(1));
 		
 		assertEquals(4, ProjectEuler.p006FindDiffSumSquareAndSquareSum(2));
 
 		assertEquals(2640, ProjectEuler.p006FindDiffSumSquareAndSquareSum(10));
 
 		assertEquals(25164150, ProjectEuler.p006FindDiffSumSquareAndSquareSum(100));
 	}
 	
 	@Test
 	public void testP007FindNthPrimeNumber(){
 		assertEquals(2, ProjectEuler.p007FindNthPrimeNumber(1));
 		
 		assertEquals(3, ProjectEuler.p007FindNthPrimeNumber(2));
 		
 		assertEquals(5, ProjectEuler.p007FindNthPrimeNumber(3));
 		
 		assertEquals(7, ProjectEuler.p007FindNthPrimeNumber(4));
 		
 		assertEquals(11, ProjectEuler.p007FindNthPrimeNumber(5));
 		
 		assertEquals(104743, ProjectEuler.p007FindNthPrimeNumber(10001));
 	}
 	
 	@Test
 	public void testP008FindLargest5DigitProduct(){
 		String input="73167176531330624919225119674426574742355349194934"+
 				"96983520312774506326239578318016984801869478851843"+
 				"85861560789112949495459501737958331952853208805511"+
 				"12540698747158523863050715693290963295227443043557"+
 				"66896648950445244523161731856403098711121722383113"+
 				"62229893423380308135336276614282806444486645238749"+
 				"30358907296290491560440772390713810515859307960866"+
 				"70172427121883998797908792274921901699720888093776"+
 				"65727333001053367881220235421809751254540594752243"+
 				"52584907711670556013604839586446706324415722155397"+
 				"53697817977846174064955149290862569321978468622482"+
 				"83972241375657056057490261407972968652414535100474"+
 				"82166370484403199890008895243450658541227588666881"+
 				"16427171479924442928230863465674813919123162824586"+
 				"17866458359124566529476545682848912883142607690042"+
 				"24219022671055626321111109370544217506941658960408"+
 				"07198403850962455444362981230987879927244284909188"+
 				"84580156166097919133875499200524063689912560717606"+
 				"05886116467109405077541002256983155200055935729725"+
 				"71636269561882670428252483600823257530420752963450";
 		assertEquals(40824, ProjectEuler.p008FindLargest5DigitProduct(input));
 	}
 	
 	@Test
 	public void testP009FindTheLargestPythagorean(){
 		assertEquals(new Triplet(3,4,5), ProjectEuler.p009FindTheLargestPythagorean(12));
 		
 
 		assertEquals(new Triplet(200,375,425), ProjectEuler.p009FindTheLargestPythagorean(1000));
 	}
 	
 	@Test
 	public void testP010FindProductOfPrimeBelowN(){
 		assertEquals(BigInteger.valueOf(2), ProjectEuler.p010FindSumOfPrimeBelowN(2));
 
 		assertEquals(BigInteger.valueOf(5), ProjectEuler.p010FindSumOfPrimeBelowN(3));
 		
 		assertEquals(BigInteger.valueOf(5), ProjectEuler.p010FindSumOfPrimeBelowN(4));
 
 		assertEquals(BigInteger.valueOf(10), ProjectEuler.p010FindSumOfPrimeBelowN(5));
 
 		assertEquals(BigInteger.valueOf(77), ProjectEuler.p010FindSumOfPrimeBelowN(20));
 		
 		assertEquals(BigInteger.valueOf(21171191), ProjectEuler.p010FindSumOfPrimeBelowN(20000));
 
 		assertEquals(new BigInteger("142913828922",10), ProjectEuler.p010FindSumOfPrimeBelowN(2000000));
 	}
 	
 	@Test
 	public void testP011FindLargestAdjacentProduct(){
 		int [][] matrix=new int[][]{{8,2,22,97,38,15,0,40,0,75,4,5,7,78,52,12,50,77,91,8},
 				{49,49,99,40,17,81,18,57,60,87,17,40,98,43,69,48,4,56,62,0},
 				{81,49,31,73,55,79,14,29,93,71,40,67,53,88,30,3,49,13,36,65},
 				{52,70,95,23,4,60,11,42,69,24,68,56,1,32,56,71,37,2,36,91},
 				{22,31,16,71,51,67,63,89,41,92,36,54,22,40,40,28,66,33,13,80},
 				{24,47,32,60,99,3,45,2,44,75,33,53,78,36,84,20,35,17,12,50},
 				{32,98,81,28,64,23,67,10,26,38,40,67,59,54,70,66,18,38,64,70},
 				{67,26,20,68,2,62,12,20,95,63,94,39,63,8,40,91,66,49,94,21},
 				{24,55,58,5,66,73,99,26,97,17,78,78,96,83,14,88,34,89,63,72},
 				{21,36,23,9,75,0,76,44,20,45,35,14,0,61,33,97,34,31,33,95},
 				{78,17,53,28,22,75,31,67,15,94,3,80,4,62,16,14,9,53,56,92},
 				{16,39,5,42,96,35,31,47,55,58,88,24,0,17,54,24,36,29,85,57},
 				{86,56,0,48,35,71,89,7,5,44,44,37,44,60,21,58,51,54,17,58},
 				{19,80,81,68,5,94,47,69,28,73,92,13,86,52,17,77,4,89,55,40},
 				{4,52,8,83,97,35,99,16,7,97,57,32,16,26,26,79,33,27,98,66},
 				{88,36,68,87,57,62,20,72,3,46,33,67,46,55,12,32,63,93,53,69},
 				{4,42,16,73,38,25,39,11,24,94,72,18,8,46,29,32,40,62,76,36},
 				{20,69,36,41,72,30,23,88,34,62,99,69,82,67,59,85,74,4,36,16},
 				{20,73,35,29,78,31,90,1,74,31,49,71,48,86,81,16,23,57,5,54},
 				{1,70,54,71,83,51,54,69,16,92,33,48,61,43,52,1,89,19,67,48}};
 		
 		assertEquals(70600674, ProjectEuler.p011FindLargestAdjacentProduct(matrix));
 	}
 }
