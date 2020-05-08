 package fi.dy.esav.GameEngine;
 
 import java.awt.Point;
 import java.awt.event.KeyEvent;
 import java.awt.event.MouseEvent;
 import java.util.ArrayList;
 
 public class InputState {
 	
 	ArrayList<Integer> keysDown = new ArrayList<Integer>();
 	
 	Point mousePos = new Point();
 	ArrayList<Integer> mouseButtons = new ArrayList<Integer>();
 	
 	/**
 	 * Return the state of a key
 	 * @param ADDME The key to be checked
 	 * @return Is the key down?
 	 */
	public boolean isKeyDown(KeyEvent key) {
 		return keysDown.contains(key);
 	}
 	
 	/**
 	 * Return true if key is up
 	 * @param ADDME The key to be checked
 	 * @return Is the key up?
 	 */
	public boolean isKeyUp(KeyEvent key) {
 		return !isKeyDown(key);
 	}
 
 	/**
 	 * @return the mouseX
 	 */
 	public int getMouseX() {
 		return mousePos.x;
 	}
 
 	/**
 	 * @return the mouseY
 	 */
 	public int getMouseY() {
 		return mousePos.y;
 	}
 	
 	/**
 	 * Get the x and y of the mouse
 	 * @return Mouse cursor coordinates
 	 */
 	public Point getMousePos() {
 		return mousePos;
 	}
 
 	/**
 	 * @return the state of the specified mouse button
 	 */
 	public boolean isMouseKey(int key) {
 		return mouseButtons.contains(key);
 	}
 	
 	/**
 	 * @return the state of the left mouse button
 	 */
 	public boolean isMouseLeft() {
 		return mouseButtons.contains(MouseEvent.BUTTON1);
 	}
 
 	/**
 	 * @return the state of the right mouse button
 	 */
 	public boolean isMouseRight() {
 		return mouseButtons.contains(MouseEvent.BUTTON2);
 	}
 	
 	/**
 	 * @return A copy of the keyboard state
 	 */
 	@SuppressWarnings("unchecked")
 	public ArrayList<Integer> getKeyboardState() {
 		return (ArrayList<Integer>) keysDown.clone();
 	}
 	
 	/**
 	 * @return A copy of the mouse buttons state
 	 */
 	@SuppressWarnings("unchecked")
 	public ArrayList<Integer> getMouseState() {
 		return (ArrayList<Integer>) mouseButtons.clone();
 	}
 	
 	
 }
