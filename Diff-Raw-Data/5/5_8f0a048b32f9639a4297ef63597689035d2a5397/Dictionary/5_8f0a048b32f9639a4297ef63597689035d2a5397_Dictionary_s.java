 package com.mntnorv.wrdl_holo.dict;
 
 import java.io.BufferedInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.List;
 import android.util.Log;
 
 public class Dictionary
 {
 	private List<Long> wordList = new ArrayList<Long>();
 	
 	/**
 	 * Load a dictionary from an InputStream.
 	 * @param dicInputStream - a specially encoded dictionary
 	 * file InputStream
 	 * @throws IOException
 	 */
 	public Dictionary (InputStream dicInputStream) throws IOException {
 		BufferedInputStream dictFile = new BufferedInputStream(dicInputStream);
 		
 		long shift = 0L;
 		long shiftIncrement = 8L;
 		long current = 0L;
 		int read;
 		
 		while ((read = dictFile.read()) != -1) {
 			if (read != 0) {
 				if (read == 0xFF) {
 					dictFile.mark(1);
 					
 					if (dictFile.read() == 0) {
 						read = 0;
 					} else {
 						dictFile.reset();
 					}
 				}
 				
 				current |= (long)read << shift;
 				shift += shiftIncrement;
 			} else {
 				wordList.add(current);
 				
 				shift = 0;
 				current = 0;
 			}
 		}
 		
 		dicInputStream.close();
 	}
 	
 	/**
 	 * Comparator needed for binary word search and prefix search.
 	 * The words in the dictionary (and in the dictionary file) are
 	 * sorted using this method.
 	 */
 	private Comparator<Long> dictComparator = new Comparator<Long> () {
 		@Override
 		public int compare(Long lhs, Long rhs) {
 			if (lhs == rhs) {
 				return 0;
 			}
 			
 			long mask = 31;
 			long shift = 0;
 			long shiftIncrement = 5;
 			long fChar = 0, sChar = 0;
 			
 			while (shift <= 64) {
 				fChar = (lhs >> shift) & mask;
 				sChar = (rhs >> shift) & mask;
 				
 				if (fChar != sChar) {
 					return (int)(fChar - sChar);
 				}
 
 				shift += shiftIncrement;
 			}
 
 			return (int)(fChar - sChar);
 		}
 	};
 	
 	/**
 	 * Encode a String to a long. The word must be in uppercase latin
 	 * and 12 letters or shorter.
 	 * @param word - the word to be encoded
 	 * @return the encoded long value
 	 */
 	private long encodeWord (String word) {
 		long encoded = 0L;
 		long shift = 0L;
 		long shiftIncrement = 5L;
 		
 		for (int i = 0; i < word.length(); i++) {
 			encoded |= (long)(word.charAt(i) - 64) << shift;
 			shift += shiftIncrement;
 		}
 		
 		return encoded;
 	}
 	
 	/**
 	 * Decodes a String from a long. The long must be encoded using the
 	 * same method as in {@link #encodeWord(String)}.
 	 * @param encodedWord - word encoded in a long
 	 * @return the decoded String
 	 */
 	private String decodeWord (long encodedWord) {
 		String decoded = "";
 		long mask = 31L;
 		long shift = 0L;
 		long shiftIncrement = 5L;
 		long currentChar;
 		boolean error = false;
 		
 		while ((currentChar = ((encodedWord >> shift) & mask)) != 0) {
 			decoded += (char)(currentChar + 64);
 			shift += shiftIncrement;
 			
 			if (currentChar > 26) {
 				error = true;
 			}
 		}
 		
 		if (error) {
 			Log.d("wrdl", decoded);
 		}
 		
 		return decoded;
 	}
 	
 	/**
 	 * Checks if a String is in the dictionary
 	 * @param word - the String to check
 	 * @return {@code true} if {@code word} is in the dictionary,
 	 * {@code false} otherwise
 	 */
 	public boolean isAWord (String pWord) {
		String word = pWord.toUpperCase();
 		return (Collections.binarySearch(wordList, encodeWord (word), dictComparator) >= 0)?true:false;
 	}
 	
 	/**
 	 * Checks if words with the given prefix exist in the dictionary.
 	 * @param prefix - prefix to search for
 	 * @return {@code true} if words starting with {@code prefix} exist in
 	 * the dictionary, {@code false} otherwise
 	 */
 	public boolean searchPrefix (String prefix) {
 		int result = Collections.binarySearch(wordList, encodeWord (prefix), dictComparator);
 		
 		if (result >= 0) {
 			return true;
 		} else if ((-result-1) < wordList.size()) {
 			return decodeWord(wordList.get(-result-1)).startsWith(prefix);
 		} else {
 			return false;
 		}
 	}
 }
