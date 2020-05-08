 import javax.microedition.lcdui.Graphics;
 
 /**
  * User Interface element.
  *
  * UI Element is a box that will get paint requests
  * and should update part of the screen using
  * UIElement.Parent interface.
  */
 interface UIElement {
 	/**
 	 * Interface that class aggregating UIElements must
 	 * provide.
 	 */
 	interface Parent {
 		public int getXDiv();
 		public int getYDiv();
		public int getYDivOn();
 		public int getYSize();
		public int getXSize();
 		public boolean isActive();
 	}
 
 	/**
 	 * Repaint element.
 	 *
 	 * Mark element non-dirty after repaint.
 	 */
 	public void paint(Graphics g);
 
 	/**
 	 * Mark element dirty.
 	 *
 	 * Non-dirty elements may not be repainted on paint().
 	 */
 	public void markDirty();
 }
