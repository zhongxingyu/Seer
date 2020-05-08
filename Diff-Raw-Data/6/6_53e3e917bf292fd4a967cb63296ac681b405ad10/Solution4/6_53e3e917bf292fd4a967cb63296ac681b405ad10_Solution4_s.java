 import static org.hamcrest.MatcherAssert.*;
 import static org.hamcrest.Matchers.*;
 
 import java.util.Arrays;
 import java.util.NoSuchElementException;
 
 import org.junit.Test;
 
 public class Solution4 {
 
 	// WARNING : assumed an even sized solution exist, this is not the case when
 	// digits == 1
	private int largestEvenPalindromFromDigits(int digits) throws Exception {
 		if (digits == 1) {
 			throw new NoSuchElementException("there is no even sized solution when digits is 1");
 		}
		int[] palindromDigitValues = buildPalindromeRepresentation(digits);
 		for(int evenPalindrome : new EvenPalindromeFromLargestIterator(digits*2)) {
 			if (hasProductOfDigitSize(evenPalindrome, digits)) {
 				return evenPalindrome;
 			}
 		}
 	}
 
 	private int[] buildPalindromeRepresentation(int digits) {
 		int[] palindromDigitValues = new int[digits];
 		Arrays.fill(palindromDigitValues, 9);
 		return palindromDigitValues;
 	}
 
 	private void decreaseToNextPalindromRepresentation(int[] palindromDigitValue) {
 		for (int i = 0; i < palindromDigitValue.length; i++) {
 			if (palindromDigitValue[i] == 0) {
 				palindromDigitValue[i] = 9;
 			} else {
 				palindromDigitValue[i] = palindromDigitValue[i] - 1;
 				return;
 			}
 		}
 	}
 
 	private int buildPalindrom(int[] palindromDigitValue) {
 		int palindrome = 0;
 		for (int i = 0; i < palindromDigitValue.length; i++) {
 			palindrome += palindromDigitValue[i] * Math.pow(10, palindromDigitValue.length - 1 - i);
 			palindrome += palindromDigitValue[i] * Math.pow(10, palindromDigitValue.length + i);
 		}
 		return palindrome;
 	}
 
 	private boolean hasProductOfDigitSize(int proposedPalindrome, int digits) {
 		for (int divisor = maxDivisor(digits); divisor >= minDivisor(digits); divisor--) {
 			if (isDivisibleWithAndHasDigits(proposedPalindrome, divisor, digits)) {
 				return true;
 			}
 		}
 		return false;
 	}
 
 	private double minDivisor(int digits) {
 		return Math.pow(10, digits - 1);
 	}
 
 	private int maxDivisor(int digits) {
 		return (int) Math.pow(10, digits) - 1;
 	}
 
 	private boolean isDivisibleWithAndHasDigits(int proposedPalindrome, int divisor, int digits) {
 		return proposedPalindrome % divisor == 0 && proposedPalindrome / divisor < Math.pow(10, digits) && proposedPalindrome / divisor >= minDivisor(digits);
 	}
 
 	public static void main(String... args) throws Exception {
 		System.out.println(new Solution4().largestEvenPalindromFromDigits(3));
 		System.out.println("expected Answer:906609");
 	}
 
 	@Test
 	public void test_that_we_determine_the_largest_even_palindrom_of_2_digit_numbers() throws Exception {
 		assertThat(largestEvenPalindromFromDigits(2), equalTo(9009)); // WARNING : number originates from easily findable online sources
 	}
 
 	@Test
 	public void test_that_we_determine_the_largest_even_palindrom_of_larger_digit_numbers() throws Exception {
 		assertThat(largestEvenPalindromFromDigits(3), equalTo(906609)); // WARNING : number originates from easily findable online sources
 	}
 
 	@Test(expected = NoSuchElementException.class)
 	public void test_that_we_cannot_determine_the_largest_even_palindrom_of_1_digit_numbers() throws Exception {
 		largestEvenPalindromFromDigits(1);
 	}
 }
