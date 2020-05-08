 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Set;
 
 public class Huffman {
 
 	/**
 	 * Builds a frequency map of characters for the given string.
 	 * 
 	 * This should just be the count of each character.
 	 * 
 	 * @param s
 	 * @return
 	 */
 	public static Map<Character, Integer> buildFrequencyMap(String s) {
 		Map<Character, Integer> map = new HashMap<Character, Integer>();
 		for(int i = 0; i < s.length(); i++){
 			Integer frequency = map.get(s.charAt(i));
             map.put(s.charAt(i), (frequency == null) ? 1 : frequency + 1);
 		}
 		return map;
 	}
 	
 	/**
 	 * Build the Huffman tree using the frequencies given.
 	 * 
 	 * The frequency map will not necessarily come from the buildFrequencyMap() method.
 	 * 
 	 * @param freq
 	 * @return
 	 */
 	public static Node buildHuffmanTree(Map<Character, Integer> freq) {
 		ArrayList<Node> list = new ArrayList<Node>();
 		Set<Character> set = freq.keySet();
 		for (Character i: set){
 			list.add(new Node(i, freq.get(i)));
 		}
 		Collections.sort(list);
 		return list.get(0);
 	}
 	
  	/**
  	 * Traverse the tree and extract the encoding for each character in the tree
  	 * 
  	 * The tree provided will be a valid huffman tree but may not come from the buildHuffmanTree() method.
  	 * 
  	 * @param tree
  	 * @return
  	 */
  	public static Map<Character, EncodedString> buildEncodingMap(Node tree) {
  		return null;
  	}
 	
 	/**
 	 * Encode each character in the string using the map provided.
 	 * 
 	 * If a character in the string doesn't exist in the map ignore it.
 	 * 
 	 * The encoding map may not necessarily come from the buildEncodingMap() method, but will be correct
 	 * for the tree given to decode() when decoding this method's output.
 	 * 
 	 * @param encodingMap
 	 * @param s
 	 * @return
 	 */
 	public static EncodedString encode(Map<Character, EncodedString> encodingMap, String s) {
 		return null;
 	}
 	
 	/**
 	 * Decode the encoded string using the tree provided.
 	 * 
 	 * The encoded string may not necessarily come from encode, but will be a valid string for the given tree.
 	 * 
 	 * (tip: use StringBuilder to make this method faster -- concatenating strings is SLOW)
 	 * 
 	 * @param tree
 	 * @param es
 	 * @return
 	 */
 	public static String decode(Node tree, EncodedString es) {
 		return null;
 	}
 }
