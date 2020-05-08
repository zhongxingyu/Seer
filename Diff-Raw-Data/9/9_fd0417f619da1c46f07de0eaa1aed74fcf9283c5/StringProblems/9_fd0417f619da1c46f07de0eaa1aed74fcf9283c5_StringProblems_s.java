 import java.util.HashMap;
 import java.util.HashSet;
 /**
  * Practice problems related to Strings.
  */
 public class StringProblems {
 
 	/**
 	 * Problem:
 	 * Implement an algorithm to determine if a string has all unique characters.
 	 * @param string the input string.
 	 * 
 	 * First attempt, use a hash map.
 	 */
 	public static boolean hasAllUniqueCharacters1(String string) {
 		HashMap<Character, Integer> map = new HashMap<Character, Integer>();
 		char character;
 		for (int i = 0; i < string.length(); i++) {
 			character = string.charAt(i);
 			if (map.containsKey(character)) {
 				return false;
 			} else {
 				map.put(string.charAt(i), 0);
 			}
 		}
 		return true;
 	}
 
 	/**
 	 * Problem:
 	 * Implement an algorithm to determine if a string has all unique characters.
 	 * @param string the input string.
 	 * 
 	 * Second attempt, we don't really need a hash map. Just an array will do.
 	 */
 	public static boolean hasAllUniqueCharacters2(String string) {
 		// Assuming ASCII characters (256 possible values)
 		// otherwise, if 16-bit Unicode, we need a bigger array (2^16 = 65536)
 		boolean table[] = new boolean[256];
 		// No need to initialize since the default value for boolean (primitive) is 
 		// false
 		int value;
 		for (int i = 0; i < string.length(); i++) {
 			value = string.charAt(i);
 			if (table[value]) {
 				return false;
 			} else {
 				table[value] = true;
 			}
 		}
 		return true;
 	}
 
 	/**
 	 * Problem:
 	 * Implement an algorithm to reverse a string.
 	 * @param string the input string.
 	 */
 	public static String reverseString(String string) {
 		StringBuilder stringBuilder = new StringBuilder();
 		for (int i = string.length() - 1; i > 0; i--) {
 			stringBuilder.append(string.charAt(i));
 		}
 		return stringBuilder.toString();
 	}
 
 	/**
 	 * Given a string print all possible permutations.
 	 */
 	public static void printPermutations(String s) {
 		perm("", s);
 	}
 
 	/**
 	 * Recursive function.
 	 */
 	private static void perm(String prefix, String s) {
 		if (s.length() == 1) {
 			System.out.println(prefix + s);
 			return;
 		}
 		for (int i = 0; i < s.length(); i++) {
 			perm(prefix + s.charAt(i), s.substring(0, i) + s.substring(i+1));
 		}
 	}
 
 	/**
 	 * Shift a string to the left with rotation.
 	 * @param s the string to be shifted
 	 * @param shift the number of positions to shift
 	 * @return the shifted string
 	 */
 	static String rotationShift(String s, int shift) {
 		StringBuilder sb = new StringBuilder();
 		for (int i = 0; i < s.length() - shift; i++) {
 			sb.append(s.charAt(i + shift));
 		}
 		for (int i = 0; i < shift; i++) {
 			sb.append(s.charAt(i));
 		}
 		return sb.toString();
 	}
 
 	/**
 	 * Write a function f(a, b) which takes two string arguments and returns a string containing
 	 * only the characters found in both strings in the order of a. Write a version which is order
 	 * N-squared.
 	 * 
 	 * @param a First string
 	 * @param b Second string
 	 * @return String that contains common characters in the order they appear in a
 	 */
 	static String commonCharacters(String a, String b) {
 		char tmpChar;
 		StringBuilder sb = new StringBuilder();		
 		for (int i = 0; i < a.length(); i++) {
 			tmpChar = a.charAt(i);
			// TODO: You also have to check whether the character already appears in the buffer.
 			for (int j = 0; j < b.length(); j++) {
 				if (tmpChar == b.charAt(j)) {
 					sb.append(tmpChar);
 					break;
 				}
 			}
 		}
 		return sb.toString();
 	}
 	
 	/**
 	 * Write a function f(a, b) which takes two string arguments and returns a string containing
 	 * only the characters found in both strings in the order of a. Write a version which is order
 	 * N.
 	 * 
 	 * @param a First string
 	 * @param b Second string
 	 * @return String that contains common characters in the order they appear in a
 	 */
 	static String commonCharactersLinear(String a, String b) {
 		StringBuilder sb = new StringBuilder();
 		HashSet<Character> charSet = new HashSet<Character>();
 		for (int i = 0; i < b.length(); i++) {
 			charSet.add(b.charAt(i));
 		}
		// TODO: You also have to check whether the character already appears in the buffer.
 		for (int i = 0; i < a.length(); i++) {
 			if (charSet.contains(a.charAt(i))) {
 				sb.append(a.charAt(i));
 			}
 		}
 		
 		return sb.toString();
 	}
 	
 	/**
 	 * Return a new string for which all duplicate characters from the input string have been
 	 * deleted. The order is maintained. Linear complexity.
 	 * @param s
 	 * @return
 	 */
 	static String getUnique(String s) {
 		HashSet<Character> charSet = new HashSet<Character>();
 		StringBuilder sb = new StringBuilder();
 		// For every character in the input string
 		for (int i = 0; i < s.length(); i++) {
 			// Add him to the output string if we have not seen it before
 			if (!charSet.contains(s.charAt(i))) {
 				charSet.add(s.charAt(i));
 				sb.append(s.charAt(i));
 			}
 		}
 		return sb.toString();
 	}
 }
