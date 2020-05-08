 package me.gory_moon.hangman.keyhandler;
 
 import me.gory_moon.hangman.core.Screen;
 import me.gory_moon.hangman.score.ScoreHandler;
 import me.gory_moon.hangman.wordprocces.Words;
 
 /**
  * Handels the key checking methods.
  */
 public class KeyChecker {
 	
 	public static char[] correctWord = null;
 
 	public KeyChecker(int lenght){
 		int i = 1;
 		String string = "";
 		while(i <= lenght){
 			string += "_";
 			i++;
 		}
 		correctWord = Words.splitWordChar(string);
 		
 	}
 	
 	/**
 	* Get a character and compare it to the random
 	* word that is generated in {@link Words}.
 	* @param word
 	* 			A char that is going to be cheked if it's match.
 	* @param oWord
 	*			The word to compare to.
 	* @param print
 	*			If it's  to print and compare the input.
 	* @return True if it's a match, else false
 	*/
 	public static boolean compareLetters(char word, char[] cWord, boolean print) {
 		char[] words = null;
 		if (print){
 			words = Words.getChars();
 		}else{
 			words = cWord;
 		}
 		String wordS = "";
 		String wordsS = "";
 		int o = 0;
 		for (int i = 0; i <= words.length -1; i++) {
 			wordsS = Character.toString(words[i]);
 			wordS = Character.toString(word);
 			if (wordS.equalsIgnoreCase(wordsS)) {
 				if (print){
 					correctWord[i] = word;
 					ScoreHandler.setScore(10,true);
 					Screen.print(correctWord,false);
 				}
 				o = 1;
 		 	}
 		}
 		if(o == 1){
 			return true;
 		}
 		return false;
 	}
 	/**
 	* Get a character and compare it to the random
 	* word that is generated in {@link Words}.
 	* Lower amount of params and is used to
 	* print and compare the input.
 	* @param word
 	*			The word to compare to.
 	*/
 	public static boolean compareLetters(char word) {
 		boolean status = compareLetters(word, correctWord,true);
 		return status;
 	}
 
 	
 	/**
 	* Takes a char[] array to check if it's any 
 	* underlines left in the array.
 	* @param words
 	*			The char[] array to check.
 	* @return True if not underscore, else false.
 	*/
 	public static boolean checkIfAllDone(char[] words) {
 		String letter = "";
 		int correct = 0;
 		for (int i = 0; i <= words.length -1; i++){
 			letter = Character.toString(words[i]);
 			if (!letter.equals("_")){
 				correct++;
 			}
 		}
 		if (correct == words.length){
 			return true;
 		}
 		return false;
 		
 	}
 }
