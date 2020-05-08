 package ai;
 
 import java.util.Arrays;
 import java.util.HashSet;
 import java.util.Set;
 import board.Board;
 
 public class WordGetter {
 	public static Set<int[][]> getPlays(char[][] letterBoard, HashSet<String> dict) {
 	    System.out.println("[WordGetter] Number of words in dictionary: " + dict.size());
 		HashSet<int[][]> plays = new HashSet<int[][]>();
 		
 		// Iterate through the dictionary, add all the ways to make all the words in it
 		for (String word : dict) {
 			plays.addAll(canMake(letterBoard, word, 0,
 					new int[word.length()][2]));
 		}
 		return plays;
 	}
 
 	// index is the letter this iteration needs to fill in
 	// play[][] will eventually be the word we play, after it gets filled in at index
 	private static Set<int[][]> canMake(char[][] letterBoard, String word,
 			int index, int[][] play) {
 		// If we've already filled
 		if (index == word.length()) {
			return new HashSet<int[][]>();
 		}
 
 		// Which character are we looking for a copy of in the letterboard?
 		char[] wordarray = word.toCharArray();
 		char next = wordarray[index];
 		char[][] copyBoard;
 		int[][] updatedPlay = play;
 		Set<int[][]> plays = new HashSet<int[][]>();
 
 		// Look through letterboard for the letter we want
 		for (int i = 0; i < 5; ++i) {
 			for (int j = 0; j < 5; ++j) {
 				
 				// every time we find it, recursively call this method again.
 				if (letterBoard[i][j] == next) {
 					copyBoard = letterBoard;
 					copyBoard[i][j] = ' ';
 					updatedPlay[index][0] = i;
 
 					updatedPlay[index][1] = j;
 					// add everything our submethods find to the set of plays
 					plays.addAll(canMake(Board.deepCopy5x5Array(copyBoard), word, index + 1,
 							updatedPlay));
 					copyBoard[i][j] = letterBoard[i][j];
 				}
 
 			}
 		}
 		// Return the result, as summed up abovee
 		return plays;
 	}
 }
