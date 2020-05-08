 package me.gory_moon.hangman.keyhandler;
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import javax.swing.JTextField;
 
 import me.gory_moon.hangman.core.Screen;
 import me.gory_moon.hangman.wordprocces.Words;
 
 /**
 * Everyting about the key that is pressed by the user.
 */
 public class HangManKeyListener implements KeyListener {
 
 	static Logger logger = Logger.getLogger("Hangman");
 	private static char[] pressedK = {};
 	public static boolean detect = true;
 	
 
 	/**
 	* Triggerd by a key pressed and it does a check
 	* if the key is valid or not.
 	* @param arg0
 	*			The KeyEvent.
 	*/
 	@Override
 	public void keyPressed(KeyEvent arg0) {
 		boolean status = false;
 		detect = true;
 		if (Screen.done){
 			detect = false;
 			Screen.restart();
 		}
 		if (detect){
 			if (!alreadyPressed(arg0.getKeyChar())){
 				System.out.println("Key: " + arg0.getKeyChar() + " pressed");
 				if (KeyChecker.compareLetters(arg0.getKeyChar())) {
 					System.out.println("True");
 					status = true;
 				} else {
 					System.out.println("False");
 					Screen.triesLeft --;
 					Screen.print(KeyChecker.correctWord, false);
 					if (Screen.triesLeft == 0){
 						Screen.Lose(Words.getWord());
 						pressedK = null;
 					}
 				}
 				JTextField typingArea = Screen.getTypingArea();
 				typingArea.setText(null);
 				logger.log(Level.FINE, "Key: "+ arg0.getKeyChar() + " pressed \t"+status);
 				if (KeyChecker.checkIfAllDone(KeyChecker.correctWord)){
 					Screen.finalPrint(KeyChecker.correctWord, true);
 				}
 			}
 		}
 	}
 	
 	/**
 	 * Sets the PressedK array to the array.
 	 * @param array
 	 * 		An char[] array to replace with.
 	 */
 	public static void setPressedKeyArray(char[] array){
 		pressedK = array;
 	}
 	
 	/**
 	* Triggers when a key is released.
 	*/
 	@Override
 	public void keyReleased(KeyEvent arg0) {
 		JTextField typingArea = Screen.getTypingArea();
 		typingArea.setText(null);
 
 	}
 
 	@Override
 	public void keyTyped(KeyEvent arg0) {
 
 	}
 
 	static char[] addArray (char[] x, char y){
 	String word = "";
 	word = Screen.arrayToString(x);
	System.out.println(word);
 	word += y;
 	char[] result = word.toCharArray();
	System.out.print(result);
 	return result;
   }
 
 	public boolean alreadyPressed (char arg0){
 		boolean status = false;
 		if (pressedK.length != 0) {
 			status = KeyChecker.compareLetters(arg0,pressedK,false);
 		}
 		if (status){
 			return true;
 		}
 		pressedK = addArray(pressedK,arg0);
 		return false;
 	}
 
 }
