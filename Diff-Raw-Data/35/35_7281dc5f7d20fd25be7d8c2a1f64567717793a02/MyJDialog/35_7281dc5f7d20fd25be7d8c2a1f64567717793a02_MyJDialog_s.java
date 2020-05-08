 /*
  * Copyright 2007-2008 Sven Strickroth <email@cs-ware.de>
  * 
  * This file is part of jBubbleBreaker.
  * 
  * jBubbleBreaker is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License version 3 as
  * published by the Free Software Foundation.
  * 
  * jBubbleBreaker is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with jBubbleBreaker. If not, see <http://www.gnu.org/licenses/>.
  */
 package org.jbubblebreaker;
 
 import java.awt.Toolkit;
 
 import javax.swing.JDialog;
 
 /**
  * JDialog Implementation which allows you to set icon, center-state and size more flexible
  * @author Sven Strickroth
  */
 @SuppressWarnings("serial")
 public abstract class MyJDialog extends JDialog {
 	/**
 	 * Creates a new JDialog
 	 */
 	public MyJDialog() {
 		super();
 	}
 
 	/**
 	 * Creates a JDialog with certain parameters
 	 * @param title the window-title
 	 * @param iconFileName the icon filename, null for no icon
 	 * @param width width of the JDialog
 	 * @param height height of the JDialog
 	 */
 	public MyJDialog(String title, String iconFileName, int width, int height) {
 		this(title, iconFileName, width, height, false, true);
 	}
 
 	/**
 	 * Creates a JDialog with certain parameters
 	 * @param title the window-title
 	 * @param iconFileName the icon filename, null for no icon
 	 * @param width width of the JDialog
 	 * @param height height of the JDialog
	 * @param resizeAble create resizable JDialog?
 	 */
	public MyJDialog(String title, String iconFileName, int width, int height, boolean resizAble) {
		this(title, iconFileName, width, height, resizAble, true);
 	}
 
 	/**
 	 * Creates a JDialog with certain parameters
 	 * @param title the window-title
 	 * @param iconFileName the icon filename, null for no icon
 	 * @param width width of the JDialog
 	 * @param height height of the JDialog
	 * @param resizeAble create resizable JDialog?
 	 * @param centered center the JDialog?
 	 */
	public MyJDialog(String title, String iconFileName, int width, int height, boolean resizeAble, boolean centered) {
 		super();
 		setTitle(title);
 		if (iconFileName != null) {
 			setIcon(iconFileName);
 		}
 		setSize(width, height);
		setResizable(resizeAble);
 		if (centered == true) {
 			setCentered();
 		}
 	}
 
 	/**
 	 * Sets the icon of the JDialog
 	 * @param filename the path and name of the image to use as the icon for the JDialog
 	 */
 	protected void setIcon(String filename) {
 		setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/images/" + filename)));
 	}
 
 	/**
 	 * Centers the window on the screen
 	 */
 	protected void setCentered() {
 		setLocation((Toolkit.getDefaultToolkit().getScreenSize().width - getWidth()) / 2, (Toolkit.getDefaultToolkit().getScreenSize().height - getHeight()) / 2);
 	}
 }
