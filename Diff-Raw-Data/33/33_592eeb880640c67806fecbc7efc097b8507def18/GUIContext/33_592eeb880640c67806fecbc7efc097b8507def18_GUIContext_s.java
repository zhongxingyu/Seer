 //Manages GUI elements, calling their methods when appropriate.
 //Keeps a buffer of key pressess and notifies UIElements of them if it is added to an input's keylisteners.
 //Keeps track of which element has the input focus. Only the element with input focus receives key events.
 //This shouldn't be used for static, unchanging things. It's for the UI that can be interacted with.
 
 package org.haferlib.slick.gui;
 
 import java.util.ArrayList;
 import java.util.Vector;
 import java.util.Collections;
 import org.newdawn.slick.KeyListener;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.Color;
 import org.newdawn.slick.Input;
 
 public class GUIContext implements KeyListener {
 
 	public static boolean debugMode = false; //Do we draw debug info?
 
 	//Holds a pair of a key code and a char. Used by the key buffer.
 	private static class KeyCharPair {
 
 		private int key;
 		private char c;
 
 		private KeyCharPair(int key, char c) {
 			this.key = key;
 			this.c = c;
 		}
 	}
 
 	///////////////////////
 	////INSTANCE FIELDS////
 	///////////////////////
 	private Vector<KeyCharPair> keyBuffer;								//The buffer of key presses that have occurred since the last update.
 	private ArrayList<GUIElement> elements;								//The GUIElements in this GUIContext, sorted by depth.
 	private ElementDepthComparator depthComparator;						//Compares elements by depth.
 	private GUIElement focus;											//The element in focus.
 	private int focusBoxX, focusBoxY, focusBoxWidth, focusBoxHeight;	//The box drawn around the element in focus in debug mode.
 	private ArrayList<GUIElement> addThese;								//The elements to be added on the next update.
 	private ArrayList<GUIElement> removeThese;							//The elements to be removed on the next update.
 	private boolean enabled;											//Is this GUIContext listening to key input?
 
 	//Constructors.
     public GUIContext() {
     	elements = new ArrayList<>();
     	depthComparator = new ElementDepthComparator();
     	enabled = true;
     	keyBuffer = new Vector<>();
     	addThese = new ArrayList<>();
     	removeThese = new ArrayList<>();
     	focus = null;
     }
 
 	//Run this UIManager.
 	public void update(Input input, int delta) {
 		update(input.getMouseX(), input.getMouseY(),
 			input.isMousePressed(Input.MOUSE_LEFT_BUTTON), input.isMousePressed(Input.MOUSE_MIDDLE_BUTTON), input.isMousePressed(Input.MOUSE_RIGHT_BUTTON),
 			input.isMouseButtonDown(Input.MOUSE_LEFT_BUTTON), input.isMouseButtonDown(Input.MOUSE_MIDDLE_BUTTON), input.isMouseButtonDown(Input.MOUSE_RIGHT_BUTTON),
 			delta);
 	}
 
 	public void update(int mouseX, int mouseY, boolean leftMousePressed, boolean middleMousePressed, boolean rightMousePressed, boolean leftMouseDown, boolean middleMouseDown, boolean rightMouseDown, int delta) {
 		//If we're just not running this thing
 		if (!enabled)
 			return;
 
 		//And and remove stuff as it has asked
 		addAndRemoveElements();
 
 		//If the mouse was clicked, clear the focus. This allows for unfocusing by clicking nothing.
 		if (leftMousePressed | middleMousePressed | rightMousePressed)
 			clearFocus();
 		
 		//We only want to call events on the top element of a bunch of elements if they overlap, so we keep track of if the event has been consumed yet.
 		boolean clickConsumed = false;
 		boolean mouseDownConsumed = false;
 		boolean hoverConsumed = false;
 		
 		//Loop through the elements and call their methods as they need to be called.
 		//They are looped through backwards to ensure that for overlapping elements, the one clicked is
 		//the one the player saw on top.
 		GUIElement e;
 		for (int i = elements.size() - 1; i > -1; i--) {
 			e = elements.get(i);
 			//If we're within the element's area...
 			if (e.pointIsWithin(mouseX, mouseY)) {
 				//If we are clicking...
 				if (leftMousePressed | middleMousePressed | rightMousePressed) {
 					//If the click has already been consumed, clickedElsewhere the element.
 					if (clickConsumed) {
 						clickedElementElsewhere(e, leftMousePressed, middleMousePressed, rightMousePressed);
 					}
 					//Otherwise, click the element and make it the focus.
 					else {
 						clickElement(e, mouseX, mouseY, leftMousePressed, middleMousePressed, rightMousePressed);
 						setFocus(e);
 						clickConsumed = true;
 					}
 				}
 				//If we have the mouse down...
				else if (mouseDownConsumed | leftMouseDown | middleMouseDown | rightMouseDown) {
 					//If the mouse down has already been consumed, mouseDownElsewhere the element.
 					if (mouseDownConsumed) {
						mouseDownElementElsewhere(e, leftMousePressed, middleMousePressed, rightMousePressed);
 					}
 					//Otherwise, mouseDown the element.
 					else {
						mouseDownElement(e, mouseX, mouseY, leftMousePressed, middleMousePressed, rightMousePressed);
 						mouseDownConsumed = true;
 					}
 				}
 				//If we are hovering...
 				else {
 					//If the hover has already been consumed, hoveredElsewhere the element.
 					if (hoverConsumed) {
 						e.hoveredElsewhere();
 					}
 					//Otherwise, hover the element.
 					else {
 						e.hover(mouseX, mouseY);
 						hoverConsumed = true;
 					}
 				}
 			}
 			//If we're not within the area...
 			else {
 				//If we're clicking outside of this element...
 				if (leftMousePressed | middleMousePressed | rightMousePressed)
 					clickedElementElsewhere(e, leftMousePressed, middleMousePressed, rightMousePressed);
 				//If we're mousing down outside of this element...
 				else if (leftMouseDown | middleMouseDown | rightMouseDown)
					mouseDownElementElsewhere(e, leftMousePressed, middleMousePressed, rightMousePressed);
 				//If we are hovering outside of this element...
 				else
 					e.hoveredElsewhere();
 			}
 			//Update the element after all the other stuff happens
 			e.update(delta);
 			//Check to see if the element is dead now, and if so, remove it
 			if (e.dead())
 				removeElement(e);
 		}
 		//Only the element with input focus gets key input.
 		if (focus != null) {
 			//If any keys have been pressed...
 			if (keyBuffer.size() > 0) {
 				//Tell the focus of those key presses
 				for (KeyCharPair k : keyBuffer) {
 					focus.keyPressed(k.key, k.c);
 				}
 				focus.keyInputDone();
 				//Then check again if the focus is dead because new stuff happened to it.
 				if (focus.dead())
 					removeElement(focus);
 			}
 		}
 		//Clear the key buffer now that the focus knows about it.
 		keyBuffer.clear();
 		
 		//See if we need to resort the elements and if we do, do it.
 		for (int i = 1; i < elements.size(); i++) {
 			//If the two elements are out of order, sort that shit.
 			if (elements.get(i - 1).getDepth() > elements.get(i).getDepth()) {
 				Collections.sort(elements, depthComparator);
 			}
 		}
 	}
 
 	private void setFocus(GUIElement e) {
 		focus = e;
 		focusBoxX = focus.getX() - 5;
 		focusBoxY = focus.getY() - 5;
 		focusBoxWidth = focus.getWidth() + 10;
 		focusBoxHeight = focus.getHeight() + 10;
 	}
 
 	private void clearFocus() {
 		focus = null;
 	}
 	
 	private void clickElement(GUIElement e, int mouseX, int mouseY, boolean lmbPressed, boolean mmbPressed, boolean rmbPressed) {
 		if (lmbPressed)
 			e.click(mouseX, mouseY, Input.MOUSE_LEFT_BUTTON);
 		if (mmbPressed)
 			e.click(mouseX, mouseY, Input.MOUSE_MIDDLE_BUTTON);
 		if (rmbPressed)
 			e.click(mouseX, mouseY, Input.MOUSE_RIGHT_BUTTON);
 	}
 	
 	private void clickedElementElsewhere(GUIElement e, boolean lmbPressed, boolean mmbPressed, boolean rmbPressed) {
 		if (lmbPressed)
 			e.clickedElsewhere(Input.MOUSE_LEFT_BUTTON);
 		if (mmbPressed)
 			e.clickedElsewhere(Input.MOUSE_MIDDLE_BUTTON);
 		if (rmbPressed)
 			e.clickedElsewhere(Input.MOUSE_RIGHT_BUTTON);
 	}
 	
	private void mouseDownElement(GUIElement e, int mouseX, int mouseY, boolean lmbPressed, boolean mmbPressed, boolean rmbPressed) {
		if (lmbPressed)
 			e.mouseDown(mouseX, mouseY, Input.MOUSE_LEFT_BUTTON);
		if (mmbPressed)
 			e.mouseDown(mouseX, mouseY, Input.MOUSE_MIDDLE_BUTTON);
		if (rmbPressed)
 			e.mouseDown(mouseX, mouseY, Input.MOUSE_RIGHT_BUTTON);
 	}
 	
	private void mouseDownElementElsewhere(GUIElement e, boolean lmbPressed, boolean mmbPressed, boolean rmbPressed) {
		if (lmbPressed)
 			e.mouseDownElsewhere(Input.MOUSE_LEFT_BUTTON);
		if (mmbPressed)
 			e.mouseDownElsewhere(Input.MOUSE_MIDDLE_BUTTON);
		if (rmbPressed)
 			e.mouseDownElsewhere(Input.MOUSE_RIGHT_BUTTON);
 	}
 
 	private void addAtSortedLoc(GUIElement e) {
 		for (int i = 0; i < elements.size(); i++) {
 			//If the element being added is of a lower depth than the one we are looking at, add it.
 			if (e.getDepth() < elements.get(i).getDepth()) {
 				elements.add(i, e);
 				return;
 			}
 		}
 		//If it is greater than everything in the list, add it to the end
 		elements.add(e);
 	}
 
 	//Adds an element to this manager in the right spot
 	public void addElement(GUIElement e) {
 		addThese.add(e);
 	}
 
 	//Removes an element from this manager if it contains it.
 	public void removeElement(GUIElement e) {
 		removeThese.add(e);
 	}
 
 	//Add and remove everything that has asked to be added or removed.
 	protected void addAndRemoveElements() {
 		//Remove anything that wants to be removed. Removal is done first to make adding faster.
 		if (removeThese.size() > 0) {
 			elements.removeAll(removeThese);
 			if (removeThese.contains(focus))
 				focus = null;
 			System.out.println("Removed " + removeThese);
 			removeThese.clear();
 		}
 
 		//Add anything that wants to be added
 		if (addThese.size() > 0) {
 			for (GUIElement e : addThese)
 				addAtSortedLoc(e);
 			System.out.println("Added " + addThese);
 			addThese.clear();
 		}
 	}
 
 	//Get the elements.
 	public ArrayList<GUIElement> getElements() {
 		return elements;
 	}
 
 	//Clear this GUIManager of elements.
 	public void clear() {
 		clearFocus();
 		elements.clear();
 		addThese.clear();
 		removeThese.clear();
 	}
 
 	//Render an area of this GUIManager.
 	public void render(Graphics g, int leftX, int topY, int rightX, int bottomY) {
 		//Render the elements that are in the render area.
 		g.setClip(leftX, topY, rightX - leftX, bottomY - topY);
 		for (GUIElement e : elements) {
 			if (e.getX() + e.getWidth() > leftX && e.getY() + e.getHeight() > topY && e.getX() < rightX && e.getY() < bottomY)
 				e.render(g);
 		}
 		g.clearClip();
 
 		//Debug rendering.
 		if (debugMode) {
 			//Draw the focus box.
 			if (focus != null) {
 				g.setColor(Color.red);
 				g.drawRect(focusBoxX, focusBoxY, focusBoxWidth, focusBoxHeight);
 			}
 		}
 	}
 
 	//Turn this manager's key listening on and off.
 	public void enable() {
 		enabled = true;
 	}
 
 	public void disable() {
 		enabled = false;
 	}
 
 	//Does this contain a given GUIElement?
 	public boolean contains(GUIElement e) {
 		return elements.contains(e);
 	}
 
 	//Is a point within any of the elements?
 	public boolean pointIsWithin(int x, int y) {
 		for (GUIElement e : elements) {
 			if (e.pointIsWithin(x, y))
 				return true;
 		}
 		return false;
 	}
 
 	//Translate all the elements along the x axis.
 	public void translateX(int dX) {
 		for (GUIElement e : elements) {
 			e.setX(e.getX() + dX);
 		}
 	}
 
 	//Translate all the elements along the y axis.
 	public void translateY(int dY) {
 		for (GUIElement e : elements) {
 			e.setY(e.getY() + dY);
 		}
 	}
 
 	//KeyListener methods
 	public void keyPressed(int key, char c) {
 		keyBuffer.add(new KeyCharPair(key, c));
 	}
 
 	public void keyReleased(int key, char c) {
 	}
 
 	public void inputStarted() {
 	}
 
 	public void inputEnded() {
 	}
 
 	public boolean isAcceptingInput() {
 		return enabled;
 	}
 
 	public void setInput(Input input) {
 	}
 
 	//toString
 	public String toString() {
 		return elements.toString();
 	}
 }
