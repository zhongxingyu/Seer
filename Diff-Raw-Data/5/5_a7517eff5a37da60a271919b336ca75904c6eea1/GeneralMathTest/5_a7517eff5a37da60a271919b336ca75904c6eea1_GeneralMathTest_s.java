 package com.osiris.math.shared;
 
 import static org.junit.Assert.*;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.junit.Test;
 
 public class GeneralMathTest {
 	
 	/*****************************************************************************
 	 * Factorial
 	 *****************************************************************************/
 	/**
 	 * Expect exception with bad input
 	 * @throws MathException
 	 */
 	@Test(expected = MathException.class)
 	public void factorialNegative() throws MathException{
 		GeneralMath.factorial(-1);
 	}
 	
 	/**
 	 * Test case: 1 : 1
 	 * @throws MathException
 	 */
 	@Test
 	public void factorialOne() throws MathException{
 		double answer = GeneralMath.factorial(1);
 		double expected = 1;
 		
 		assertTrue("Expected " + expected + ", got " + answer, answer == expected);
 	}
 	
 	/**
 	 * Test case: 2 : 2
 	 * @throws MathException
 	 */
 	@Test
 	public void factorialTwo() throws MathException{
 		double answer = GeneralMath.factorial(2);
 		double expected = 2;
 		
 		assertTrue("Expected " + expected + ", got " + answer, answer == expected);
 	}
 	
 	/**
 	 * Test case: 3 : 6
 	 * @throws MathException
 	 */
 	@Test
 	public void factorialThree() throws MathException{
 		double answer = GeneralMath.factorial(3);
 		double expected = 6;
 		
 		assertTrue("Expected " + expected + ", got " + answer, answer == expected);
 	}
 	
 	/**
 	 * Test case: 4 : 4
 	 * @throws MathException
 	 */
 	@Test
 	public void factorialFour() throws MathException{
 		double answer = GeneralMath.factorial(4);
 		double expected = 24;
 		
 		assertTrue("Expected " + expected + ", got " + answer, answer == expected);
 	}
 	
 	/**
 	 * Test case: 5 : 0
 	 * @throws MathException
 	 */
	@Test
 	public void factorialZero() throws MathException{
		double answer = GeneralMath.factorial(4);
 		double expected = 0;
 		
 		assertTrue("Expected " + expected + ", got " + answer, answer == expected);
 	}
 	
 	/*****************************************************************************
 	 * Sum of squares
 	 *****************************************************************************/
 	
 	/**
 	 * Should fail due negative input
 	 * @throws MathException 
 	 */
 	@Test(expected = MathException.class)
 	public void testsquareOfSumsNegativeInput() throws MathException {
 		GeneralMath.squareOfSums(-1);
 	}
 	
 	/**
 	 * Should pass with input zero
 	 * @throws MathException 
 	 */
 	@Test
 	public void testsquareOfSumsInputZero() throws MathException {
 		GeneralMath.squareOfSums(0);
 	}
 	
 	/**
 	 * Return 0 for input 0
 	 * @throws MathException 
 	 */
 	@Test
 	public void testsquareOfSumsZero() throws MathException {
 		assertTrue("Was not 0", GeneralMath.squareOfSums(0) == 0);
 	}
 	
 	/**
 	 * Return 1 for input 1
 	 * @throws MathException 
 	 */
 	@Test
 	public void testsquareOfSumsOne() throws MathException {
 		assertTrue("Was not 1", GeneralMath.squareOfSums(1) == 1);
 	}
 	
 	/**
 	 * Return 9 for input 2
 	 * @throws MathException 
 	 */
 	@Test
 	public void testsquareOfSumsTwo() throws MathException {
 		assertTrue("Was not 9", GeneralMath.squareOfSums(2) == 9);
 	}
 	
 	/**
 	 * Return 36 for input 3
 	 * @throws MathException 
 	 */
 	@Test
 	public void testsquareOfSumsThree() throws MathException {
 		assertTrue("Was not 36", GeneralMath.squareOfSums(3) == 36);
 	}
 	
 	/**
 	 * Return 3025 for input 10
 	 * @throws MathException 
 	 */
 	@Test
 	public void testsquareOfSums10() throws MathException {
 		assertTrue("Was not 3025, got" + GeneralMath.squareOfSums(10) , GeneralMath.squareOfSums(10) == 3025);
 	}
 
 	
 	/*****************************************************************************
 	 * Sum of squares
 	 *****************************************************************************/
 	
 	/**
 	 * Should fail due negative input
 	 * @throws MathException 
 	 */
 	@Test(expected = MathException.class)
 	public void testSumOfSquaresNegativeInput() throws MathException {
 		GeneralMath.sumOfSquares(-1);
 	}
 	
 	/**
 	 * Should pass with input zero
 	 * @throws MathException 
 	 */
 	@Test
 	public void testSumOfSquaresInputZero() throws MathException {
 		GeneralMath.sumOfSquares(0);
 	}
 	
 	/**
 	 * Return 0 for input 0
 	 * @throws MathException 
 	 */
 	@Test
 	public void testSumOfSquaresZero() throws MathException {
 		assertTrue("Was not 0", GeneralMath.sumOfSquares(0) == 0);
 	}
 	
 	/**
 	 * Return 1 for input 1
 	 * @throws MathException 
 	 */
 	@Test
 	public void testSumOfSquaresOne() throws MathException {
 		assertTrue("Was not 1", GeneralMath.sumOfSquares(1) == 1);
 	}
 	
 	/**
 	 * Return 5 for input 2
 	 * @throws MathException 
 	 */
 	@Test
 	public void testSumOfSquaresTwo() throws MathException {
 		assertTrue("Was not 5", GeneralMath.sumOfSquares(2) == 5);
 	}
 	
 	/**
 	 * Return 14 for input 3
 	 * @throws MathException 
 	 */
 	@Test
 	public void testSumOfSquaresThree() throws MathException {
 		assertTrue("Was not 14", GeneralMath.sumOfSquares(3) == 14);
 	}
 	
 	
 	
 	
 	
 	/*****************************************************************************
 	 * Divisible By
 	 *****************************************************************************/
 	
 	
 	/**
 	 * Should fail due to limits wrong way round
 	 * @throws MathException 
 	 */
 	@Test(expected = MathException.class)
 	public void testDivisibleWrongLimit() throws MathException {
 		GeneralMath.divisiblyBy(3, 2, 1);
 	}
 	
 	/**
 	 * Should fail due to negative divisor
 	 * @throws MathException 
 	 */
 	@Test(expected = MathException.class)
 	public void testDivisibleNegativeDivisor() throws MathException {
 		GeneralMath.divisiblyBy(2, 4, -1);
 	}
 	
 	/**
 	 * Should fail due to zero divisor
 	 * @throws MathException 
 	 */
 	@Test(expected = MathException.class)
 	public void testDivisibleZeroDivisor() throws MathException {
 		GeneralMath.divisiblyBy(2, 4, 0);
 	}
 	
 	
 	/**
 	* Should pass since limits are equal
 	 * @throws MathException 
 	 */
 	@Test
 	public void testDivisibleGoodLimit() throws MathException {
 		GeneralMath.divisiblyBy(2, 2, 1);
 	}
 	
 	/**
 	 * Return  all numbers upto 10
 	 * @throws MathException 
 	 */
 	@Test
 	public void testDivisibleReturnAll() throws MathException {
 		ArrayList<Double> numbers = GeneralMath.divisiblyBy(1, 10, 1);
 		
 		for(int i = 1; i < numbers.size(); i++){
 			assertTrue(numbers.get(i) + " : " + i, numbers.get(i) == (i + 1));
 		}
 	}
 	
 	/**
 	 * Return  all numbers up to and including 10
 	 * @throws MathException 
 	 */
 	@Test
 	public void testDivisibleReturnTwos() throws MathException {
 		ArrayList<Double> numbers = GeneralMath.divisiblyBy(0, 10, 2);
 		
 		for(int i = 0; i < numbers.size(); i++){
 			assertTrue(numbers.get(i) + " : " + i * 2, numbers.get(i) == (i * 2));
 		}
 	}
 	
 	/**
 	 * Return numbers divisible by 3 or 5
 	 * @throws MathException 
 	 */
 	@Test
 	public void testDivisibleReturnThreeFive() throws MathException {
 		ArrayList<Double> numbers = GeneralMath.divisiblyBy(1, 10, 3, 5);
 		
 		assertTrue(numbers.get(0) == 3);
 		assertTrue(numbers.get(1) == 5);
 		assertTrue(numbers.get(2) == 6);
 		assertTrue(numbers.get(3) == 9);
 	}
 	
 	/*****************************************************************************
 	 * Fibonacci
 	 *****************************************************************************/
 	
 	/**
 	 * Check negative numbers aren't accepted
 	 * @throws MathException
 	 */
 	@Test(expected = MathException.class)
 	public void testFibonacciNegativeLimit() throws MathException {
 		ArrayList<Double> numbers = GeneralMath.fibonacci(-1);
 	}
 	
 	/**
 	 * Check zero isn't accepted
 	 * @throws MathException
 	 */
 	@Test(expected = MathException.class)
 	public void testFibonacciZeroLimit() throws MathException {
 		ArrayList<Double> numbers = GeneralMath.fibonacci(0);
 	}
 	
 	/**
 	 * Check 1 is accepted
 	 * @throws MathException
 	 */
 	@Test
 	public void testFibonacciOneLimit() throws MathException {
 		ArrayList<Double> numbers = GeneralMath.fibonacci(1);
 	}
 	
 	/**
 	 * Check initial values, when 1 is entered as limit, is 1
 	 * @throws MathException
 	 */
 	@Test
 	public void testFibonacciInitial() throws MathException {
 		ArrayList<Double> numbers = GeneralMath.fibonacci(1);
 		
 		assertTrue("Incorrect size", numbers.size() == 1);
 		
 		assertTrue("1st: " + numbers.get(0), numbers.get(0) == 1);
 	}
 	
 	@Test
 	public void testFibonacciUpToTen() throws MathException {
 		ArrayList<Double> numbers = GeneralMath.fibonacci(10);
 		
 		assertTrue(numbers.size() == 5);
 		
 		assertTrue("1st: " + numbers.get(0), numbers.get(0) == 1);
 		assertTrue("2nd: " + numbers.get(1), numbers.get(1) == 2);
 		assertTrue("3rd: " + numbers.get(2), numbers.get(2) == 3);
 		assertTrue("4th: " + numbers.get(3), numbers.get(3) == 5);
 		assertTrue("5th: " + numbers.get(4), numbers.get(4) == 8);
 	}
 	
 	
 	/*****************************************************************************
 	 * Sum
 	 *****************************************************************************/
 	
 	/**
 	 * Check exception thrown with null input
 	 * @throws MathException
 	 */
 	@Test(expected = MathException.class)
 	public void testSumNull() throws MathException {
 		GeneralMath.sum(null);
 	}
 	
 	/**
 	 * Check exception thrown empty input
 	 * @throws MathException
 	 */
 	@Test(expected = MathException.class)
 	public void testSumEmpty() throws MathException {
 		GeneralMath.sum(new ArrayList<Double>());
 	}
 	
 	/**
 	 * Check adds numbers correctly
 	 * @throws MathException
 	 */
 	@Test
 	public void testSumNormal() throws MathException {
 		ArrayList<Double> numbers = new ArrayList<Double>();
 		numbers.add(1.0);
 		numbers.add(10.0);
 		numbers.add(100.0);
 		
 		assertTrue("Not 111", GeneralMath.sum(numbers) == 111);
 	}
 	
 	/**
 	 * Check exception thrown with null input
 	 * @throws MathException
 	 */
 	@Test(expected = MathException.class)
 	public void testSumEvenNull() throws MathException {
 		GeneralMath.sumEven(null);
 	}
 	
 	/**
 	 * Check exception thrown empty input
 	 * @throws MathException
 	 */
 	@Test(expected = MathException.class)
 	public void testSumEvenEmpty() throws MathException {
 		GeneralMath.sumEven(new ArrayList<Double>());
 	}
 	
 	/**
 	 * Check adds numbers correctly
 	 * @throws MathException
 	 */
 	@Test
 	public void testSumEvenNormal() throws MathException {
 		ArrayList<Double> numbers = new ArrayList<Double>();
 		numbers.add(2.0);
 		numbers.add(4.0);
 		numbers.add(6.0);
 		
 		assertTrue("Not 12", GeneralMath.sumEven(numbers) == 12);
 	}
 	
 	/**
 	 * Check adds even numbers correctly, with odd numbers in between
 	 * @throws MathException
 	 */
 	@Test
 	public void testSumEvenNormalWithODD() throws MathException {
 		ArrayList<Double> numbers = new ArrayList<Double>();
 		numbers.add(2.0);
 		numbers.add(4.0);
 		numbers.add(5.0);
 		numbers.add(6.0);
 		numbers.add(11.0);
 		
 		assertTrue("Not 12", GeneralMath.sumEven(numbers) == 12);
 	}
 	
 	/*****************************************************************************
 	 * Primes
 	 *****************************************************************************/
 
 	/**
 	 * Enter negative input and expect and error
 	 * @throws MathException
 	 */
 	@Test(expected = MathException.class)
 	public void testFirsteNegative() throws MathException {
 		GeneralMath.firstXPrimeNumbers(-1);
 	}
 	
 	/**
 	 * Enter zero input and expect and error
 	 * @throws MathException
 	 */
 	@Test(expected = MathException.class)
 	public void testFirsteZero() throws MathException {
 		GeneralMath.firstXPrimeNumbers(0);
 	}
 	
 	/**
 	 * Get only 1 prime number
 	 * @throws MathException
 	 */
 	@Test
 	public void testFirsteSinglePrime() throws MathException {
 		ArrayList<Double> primes = GeneralMath.firstXPrimeNumbers(1.0);
 		
 		assertTrue(primes.size() == 1);
 		assertTrue(primes.get(0) == 2);
 	
 	}
 	
 	/**
 	 * Get two primes, check second is 2
 	 * @throws MathException
 	 */
 	@Test
 	public void testFirsteCheckFor2() throws MathException {
 		ArrayList<Double> primes = GeneralMath.firstXPrimeNumbers(2.0);
 		
 		assertTrue("Incorrect size. Expected 2, got: " + primes.size(), primes.size() == 2);
 		assertTrue("Expected 3, got: " + primes.get(1), primes.get(1) == 3);
 	
 	}
 	
 	/**
 	 * Get primes under 10
 	 * @throws MathException
 	 */
 	@Test
 	public void testFirstPrimesUnder10() throws MathException {
 		ArrayList<Double> primes = GeneralMath.firstXPrimeNumbers(5);
 		
 		assertTrue("Incorrect size. Expected 5, got: " + primes.size(), primes.size() == 5);
 		
 		assertTrue("Expected 2, got: " + primes.get(0), primes.get(0) == 2.0);
 		assertTrue("Expected 3, got: " + primes.get(1), primes.get(1) == 3.0);
 		assertTrue("Expected 5, got: " + primes.get(2), primes.get(2) == 5.0);
 		assertTrue("Expected 7, got: " + primes.get(3), primes.get(3) == 7.0);
 		assertTrue("Expected 11, got: " + primes.get(4), primes.get(4) == 11.0);
 	
 	}
 	
 	/******/
 	
 	
 	
 	/**
 	 * Enter negative input and expect and error
 	 * @throws MathException
 	 */
 	@Test(expected = MathException.class)
 	public void testPrimeNegative() throws MathException {
 		GeneralMath.PrimeNumbersBelow(-1);
 	}
 	
 	/**
 	 * Enter zero input and expect and error
 	 * @throws MathException
 	 */
 	@Test(expected = MathException.class)
 	public void testPrimeZero() throws MathException {
 		GeneralMath.PrimeNumbersBelow(0);
 	}
 	
 	/**
 	 * Get only 1 prime number
 	 * @throws MathException
 	 */
 	@Test
 	public void testPrimeSinglePrime() throws MathException {
 		ArrayList<Double> primes = GeneralMath.PrimeNumbersBelow(3.0);
 		
 		assertTrue(primes.size() == 1);
 		assertTrue(primes.get(0) == 2);
 	
 	}
 	
 	/**
 	 * Get two primes, check second is 2
 	 * @throws MathException
 	 */
 	@Test
 	public void testPrimeCheckFor2() throws MathException {
 		ArrayList<Double> primes = GeneralMath.PrimeNumbersBelow(4.0);
 		
 		assertTrue("Incorrect size. Expected 2, got: " + primes.size(), primes.size() == 2);
 		assertTrue("Expected 2, got: " + primes.get(1), primes.get(1) == 3);
 	
 	}
 	
 	/**
 	 * Get primes under 10
 	 * @throws MathException
 	 */
 	@Test
 	public void testPrimPrimesUnder10() throws MathException {
 		ArrayList<Double> primes = GeneralMath.PrimeNumbersBelow(10);
 		
 		assertTrue("Incorrect size. Expected 4, got: " + primes.size(), primes.size() == 4);
 		
 		assertTrue("Expected 2, got: " + primes.get(0), primes.get(0) == 2.0);
 		assertTrue("Expected 3, got: " + primes.get(1), primes.get(1) == 3.0);
 		assertTrue("Expected 5, got: " + primes.get(2), primes.get(2) == 5.0);
 		assertTrue("Expected 7, got: " + primes.get(3), primes.get(3) == 7.0);
 	
 	}
 	
 	/**
 	 * Check exception is thrown when getting numbers on negative number
 	 * @throws MathException
 	 */
 	@Test(expected = MathException.class)
 	public void testPrimeFactorNegative() throws MathException {
 		GeneralMath.primeFactors(-1);
 	}
 	
 	/**
 	 * Check exception is thrown when getting numbers on zero number
 	 * @throws MathException
 	 */
 	@Test(expected = MathException.class)
 	public void testPrimeFactorZero() throws MathException {
 		GeneralMath.primeFactors(0);
 	}
 	
 	/**
 	 * Test a normal case
 	 * @throws MathException
 	 */
 	@Test
 	public void testPrimeFactorNormalCase1() throws MathException {
 		ArrayList<Double> primes = GeneralMath.primeFactors(13195);
 		
 		assertTrue("Incorrect size. Expected 4, got: " + primes.size(), primes.size() == 4);
 		
 		assertTrue("Expected 5, got: " + primes.get(0), primes.get(0) == 5.0);
 		assertTrue("Expected 7, got: " + primes.get(1), primes.get(1) == 7.0);
 		assertTrue("Expected 13, got: " + primes.get(2), primes.get(2) == 13.0);
 		assertTrue("Expected 29, got: " + primes.get(3), primes.get(3) == 29.0);
 		
 	}
 	
 	
 	/**********************************************************************************************************************************************************
 	 * Get Max
 	 **********************************************************************************************************************************************************/
 	
 	/**
 	 * Check exception thrown with null input
 	 * @throws MathException
 	 */
 	@Test(expected = MathException.class)
 	public void testMaxNull() throws MathException {
 		GeneralMath.max(null);
 	}
 	
 	/**
 	 * Check exception thrown empty input
 	 * @throws MathException
 	 */
 	@Test(expected = MathException.class)
 	public void testMaxEmpty() throws MathException {
 		GeneralMath.max(new ArrayList<Double>());
 	}
 	
 	/**
 	 * Test normal case, numbers in order
 	 * @throws MathException
 	 */
 	@Test
 	public void testMaxNormalCase1() throws MathException {
 		double max;
 		ArrayList<Double> numbers = new ArrayList<Double>();
 		
 		numbers.add(1.0);
 		numbers.add(2.0);
 		numbers.add(3.0);
 		
 		max = GeneralMath.max(numbers);
 		
 		assertTrue("Incorrect Max. Exptected 3, got: " + max, max == 3.0);
 	}
 	
 	/**
 	 * Test normal case, numbers reverse order
 	 * @throws MathException
 	 */
 	@Test
 	public void testMaxNormalCase2() throws MathException {
 		double max;
 		ArrayList<Double> numbers = new ArrayList<Double>();
 		
 		numbers.add(5.0);
 		numbers.add(2.0);
 		numbers.add(1.0);
 		
 		max = GeneralMath.max(numbers);
 		
 		assertTrue("Incorrect Max. Exptected 5, got: " + max, max == 5.0);
 	}
 	
 	/**
 	 * Test normal case, numbers no order
 	 * @throws MathException
 	 */
 	@Test
 	public void testMaxNormalCase3() throws MathException {
 		double max;
 		ArrayList<Double> numbers = new ArrayList<Double>();
 		
 		numbers.add(58.0);
 		numbers.add(129.0);
 		numbers.add(12.0);
 		
 		max = GeneralMath.max(numbers);
 		
 		assertTrue("Incorrect Max. Exptected 129, got: " + max, max == 129.0);
 	}
 	
 	/**
 	 * Test normal case, numbers double up
 	 * @throws MathException
 	 */
 	@Test
 	public void testMaxNormalCase4() throws MathException {
 		double max;
 		ArrayList<Double> numbers = new ArrayList<Double>();
 		
 		numbers.add(58.0);
 		numbers.add(12.0);
 		numbers.add(58.0);
 		
 		max = GeneralMath.max(numbers);
 		
 		assertTrue("Incorrect Max. Exptected 58, got: " + max, max == 58.0);
 	}
 	
 	
 	
 }
