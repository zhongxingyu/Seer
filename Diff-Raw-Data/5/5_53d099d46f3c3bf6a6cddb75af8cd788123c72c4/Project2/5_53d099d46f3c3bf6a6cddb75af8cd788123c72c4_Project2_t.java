 package ch3;
 
 import java.util.Arrays;
 
 /**
  * <pre>
  * Project2 for CSCI313 Data Structures
  * 
  * Task 1: sortWords(String, String)
  * Task 2: reverseStrings(String)
  * Task 3: palindrome
  * 
  * Output:
  * Line1: A new order began a more Roman age bred Rowena
  * Soreted array of line1: [A, a, age, bred, began, Roman, more, order, new, Rowena]
  * Line2: A toyota Race fast safe car A Toyota
  * Soreted array of line2: [A, A, car, fast, Race, safe, toyota, Toyota]
  * new array: [A, a, age, bred, began, Roman, more, order, new, Rowena, A, A, car, 
  * fast, Race, safe, toyota, Toyota]
  * Input string: A new order began a more Roman age bred Rowena
  * Reversed string: anewoR derb ega namoR erom a nageb redro wen A
  * 'A dog, a plan, a canal: pagoda.' is a palindrome.
  * 'A man, a plan, a canal: panama.' is a palindrome.
  * 'A new order began, a more Roman age bred Rowena.' is a palindrome.
  * 'A tin mug for a jar of gum, Nita.' is a palindrome.
  * 'A toyota. Race fast, safe car. A Toyota.' is a palindrome.
  * 'Able was I ere I Saw Elba.' is a palindrome.
  * 'Animal loots foliated detail of stool lamina.' is a palindrome.
  * 'Anne, I vote more cars race Rome to Vienna.' is a palindrome.
  * 'Are we not drawn onward, we few, drawn onward to new era?' is a palindrome.
  * 'Are we not pure? "No sir!" Panama's moody Noriega brags. "It is garbage!" Irony 
  * dooms a man; a prisoner up to new era.' is a palindrome.
  * </pre>
  * 
  * @author Haijun Su Created date: 2013-9-28
  */
 public class Project2 {
 
 	private final static String LINE_1 = "A new order began a more Roman age bred Rowena";
 
 	private final static String LINE_2 = "A toyota Race fast safe car A Toyota";
 
 	/**
 	 * Read in two lines of strings, in which for each, the words in the string
 	 * are in alphabetic order. Each word in each string should be stored in a
 	 * separate array (of Strings). Output the two lines. Merge the words into
 	 * one array and output the result.
 	 */
 	private void sortWords(String line1, String line2) {
 		String[] array1 = line1.split(" ");
 		String[] array2 = line2.split(" ");
 
 		sortArray(array1);
 		sortArray(array2);
 
 		System.out.println("Line1: " + line1);
 		System.out
 				.println("Soreted array of line1: " + Arrays.toString(array1));
 
 		System.out.println("Line2: " + line2);
 		System.out
 				.println("Soreted array of line2: " + Arrays.toString(array2));
 
 		// Merge the words into one array.
 		String[] array3 = new String[array1.length + array2.length];
 
 		for (int i = 0; i < array1.length; i++) {
 			array3[i] = array1[i];
 		}
 		for (int i = 0; i < array2.length; i++) {
 			array3[array1.length + i] = array2[i];
 		}
 		System.out.println("new array: " + Arrays.toString(array3));
 	}
 
 	/**
 	 * Sort a string array in alphbetic order
 	 * 
 	 * @param array
 	 */
 	private void sortArray(String[] array) {
 		for (int i = 0; i < array.length; i++) {
 			String cur = array[i];
 			int j = i - 1;
 			while ((j >= 0) && isGreater(array[j], cur)) {
 				array[j + 1] = array[j--];
 			}
 			array[j + 1] = cur;
 		}
 	}
 
 	/**
 	 * Compare to string words. If word1 is greater than word2, return ture.
 	 * otherwise return false.
 	 * 
 	 * @param word1
 	 * @param word2
 	 * @return
 	 */
 	private boolean isGreater(String word1, String word2) {
 		char[] char1 = word1.toCharArray();
 		char[] char2 = word2.toCharArray();
 		int loopSize = char1.length;
 		if (char2.length < char1.length)
 			loopSize = char2.length;
 		for (int i = 0; i < loopSize; i++) {
 			// ignore case
 			if ((char1[i] > 96) && (char1[i] < 123)) {
 				char1[i] -= 32;
 			}
 			if ((char2[i] > 96) && (char2[i] < 123)) {
 				char2[i] -= 32;
 			}
 			if (char1[i] > char2[i]) {
 				return true;
 			}
 		}
 		return false;
 	}
 
 	/**
 	 * Store into another array, the reverse order of the first string and
 	 * output
 	 * 
 	 * @param str
 	 */
 	private void reverseStrings(String str) {
 		System.out.println("Input string: " + str);
 		//String[] array = str.split(" ");
 		char[] array = str.toCharArray();
 		reverseArray(array);
 		System.out.print("Reversed string: ");
 		for (int i = 0; i < array.length; i++) {
 			System.out.print(array[i]);
 		}
 		System.out.println();
 	}
 
 	/**
	 * Reverse a char array
 	 * 
 	 * @param array
 	 */
 	private void reverseArray(char[] array) {
 		int i = 0;
 		int j = array.length - 1;
 		while (i < j) {
 			char tmp = array[i];
 			array[i] = array[j];
 			array[j] = tmp;
 			i++;
 			j--;
 		}
 	}
 
 	/**
	 * Reverse a string array
 	 * 
 	 * @param array
 	 */
 	private void reverseArray(String[] array) {
 		int i = 0;
 		int j = array.length - 1;
 		while (i < j) {
 			String tmp = array[i];
 			array[i] = array[j];
 			array[j] = tmp;
 			i++;
 			j--;
 		}
 	}
 
 	/**
 	 * Check if a sentence is a palindrome (ignoring spaces, capitals and
 	 * non-alphabetic characters) using an array of characters. A few that are:
 	 * 
 	 * <pre>
 	 * 1. A dog, a plan, a canal: pagoda.
 	 * 2. A man, a plan, a canal: panama.
 	 * 3. A new order began, a more Roman age bred Rowena.
 	 * 4. A tin mug for a jar of gum, Nita.
 	 * 5. A toyota. Race fast, safe car. A Toyota.
 	 * 6. Able was I ere I Saw Elba.
 	 * 7. Animal loots foliated detail of stool lamina.
 	 * 8. Anne, I vote more cars race Rome to Vienna.
 	 * 9. Are we not drawn onward, we few, drawn onward to new era?
 	 * 10. Are we not pure? "No sir!" Panama's moody Noriega brags.
 	 * "It is garbage!" Irony dooms a man; a prisoner up to new era.
 	 * </pre>
 	 */
 	private void palindrome() {
 		String[] sentences = new String[] {
 				"A dog, a plan, a canal: pagoda.",
 				"A man, a plan, a canal: panama.",
 				"A new order began, a more Roman age bred Rowena.",
 				"A tin mug for a jar of gum, Nita.",
 				"A toyota. Race fast, safe car. A Toyota.",
 				"Able was I ere I Saw Elba.",
 				"Animal loots foliated detail of stool lamina.",
 				"Anne, I vote more cars race Rome to Vienna.",
 				"Are we not drawn onward, we few, drawn onward to new era?",
 				"Are we not pure? \"No sir!\" Panama's moody Noriega brags. \"It is "
 						+ "garbage!\" Irony dooms a man; a prisoner up to new era." };
 		for (int i = 0; i < sentences.length; i++) {
 			if (checkPalindrome(sentences[i])) {
 				System.out.println("'" + sentences[i] + "' is a palindrome.");
 			} else {
 				System.out.println("'" + sentences[i]
 						+ "' is NOT a palindrome.");
 			}
 		}
 	}
 
 	/**
 	 * Check if a sentense is a palindrome
 	 * 
 	 * @param sentence
 	 * @return
 	 */
 	private boolean checkPalindrome(String sentence) {
 		char[] chars = sentence.toCharArray();
 		char[] resveredChars = new char[chars.length];
 		int offset = 0;
 		boolean findAlphabetic = false;
 		for (int i = 0; (i + offset) < resveredChars.length; i++) {
 			if (!isAlphabetic(chars[i + offset])) {
 				chars[i + offset] = '\0'; // set current element as null;
 				// find next alphabetic
 				findAlphabetic = false;
 				offset++;
 				while ((i + offset) < chars.length) {
 					if (isAlphabetic(chars[i + offset])) {
 						findAlphabetic = true;
 						break;
 					}
 					chars[i + offset] = '\0';
 					offset++;
 				}
 			} else {
 				findAlphabetic = true;
 			}
 			if (findAlphabetic && offset > 0) {
 				chars[i] = chars[i + offset];
 				chars[i + offset] = '\0';
 				findAlphabetic = false;
 			}
 			if (chars[i] > 96) {
 				chars[i] -= 32; // to upper case
 			}
 		}
 
 		int alphabeticLength = chars.length - offset - 1;
 		for (int i = 0; i <= alphabeticLength; i++) {
 			resveredChars[i] = chars[alphabeticLength - i];
 		}
 
 		// System.out.println(Arrays.toString(chars));
 		// System.out.println(Arrays.toString(resveredChars));
 
 		return Arrays.equals(chars, resveredChars);
 	}
 
 	/**
 	 * Check whether the char is an alphabetic letter.
 	 * 
 	 * @param c
 	 * @return
 	 */
 	private boolean isAlphabetic(char c) {
 		return (c > 64 && c < 91) || (c > 96 && c < 123);
 	}
 
 	/**
 	 * main method
 	 * 
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		Project2 proj = new Project2();
 		proj.sortWords(LINE_1, LINE_2);
 
 		proj.reverseStrings(LINE_1);
 
 		proj.palindrome();
 	}
 
 }
