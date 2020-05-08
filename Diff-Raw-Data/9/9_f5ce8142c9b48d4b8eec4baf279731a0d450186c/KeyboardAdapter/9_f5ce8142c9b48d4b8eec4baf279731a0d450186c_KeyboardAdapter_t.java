 /**
  * 
  * @author Stephen Conway
  * 
  * Created for:		M5		9/30/13
 * Last modified:	M5		10/2/13			Stephen Conway
  * 
  * 
  * 
  * Purpose: Data input class. Takes in keyboard input and channels it to
  * 			active KeyboardReceiver. Handles top level key inputs (like esc
  * 			to pull up main menu) itself.
  */
 public class KeyboardAdapter implements Runnable{
 	private InputReceiver currentFocused;
 	
 	/**
 	 * @author Stephen Conway #M5
 	 * Primary constructor
 	 */
 	public KeyboardAdapter() {
 		currentFocused = null;
 	}
 	
 	/**
 	 * @author #M5
 	 * Keyboard input should be interpretted parallel to the application.
 	 * This is used to initiate operation as a seperate thread.
 	 * The method should listen for keyboard events and when appropriate
 	 * pass them to the currentFocused object by a call to the receiveInput
 	 * method
 	 */
 	public void run() {
 		
 	}
 	
 	/**
 	 * @author Stephen Conway #M5
 	 * Method to set the current receiver of the keyboard input
 	 * @param newFocused New item to focus on, or null
 	 */
 	public void setReceiver(InputReceiver newFocused) {
 		if (currentFocused != null) {
 			currentFocused.loseFocus();
 		}
 		currentFocused = newFocused;
 		if (currentFocused != null) {
 			currentFocused.gainFocus();
 		}
 	}
 }
