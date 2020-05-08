 package hms.util;
 public class Encryptor {
 	/**
 	 * Encodes the given string.
 	 * @param word the string to be encoded
 	 * @return an encoded version of the given string
 	 */
 	public static String encode(String word) {
 		if(word == null || word.equals("null")) {
 			return "null"; 
 		}
		
		word = word.trim();
 		char[] value = word.toCharArray();
		
 		for (int i = 0; i < word.length(); i++) {
 			if (value[i] >= 'a' && value[i] <= 'z') {
 				value[i] -= 'a';
 				value[i] = (char)((value[i] + 13) % 26);
 				value[i] += 'a';
 			}
 			if (value[i] >= 'A' && value[i] <= 'Z') {
 				value[i] -= 'A';
 				value[i] = (char)((value[i] + 13) % 26);
 				value[i] += 'A';
 			}
 		}
		
 		return new String(value);
 	}
 	
 	/**
 	 * Decodes the given string.
 	 * @param word an already encoded string
 	 * @return a decoded version of the given string
 	 */
 	public static String decode(String word) {
 		return encode(word);		
 	}
 }
