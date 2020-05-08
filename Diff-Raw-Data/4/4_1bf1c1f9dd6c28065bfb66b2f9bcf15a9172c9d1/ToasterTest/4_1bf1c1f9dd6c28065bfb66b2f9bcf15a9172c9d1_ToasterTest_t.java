 package com.jpii.navalbattle.util.toaster;
 
 import java.awt.*;
 import javax.swing.*;
 
 /**
  * Class to show toasters in multi-platform
  * 
  * @author daniele piras
  * 
  */
 public class ToasterTest {
 	// Width of the toaster
 	private int toasterWidth = 300;
 
 	// Height of the toaster
 	private int toasterHeight = 80;
 
 	// Step for the toaster
 	private int step = 20;
 
 	// Step time
 	private int stepTime = 20;
 
 	// Show time
 	private int displayTime = 3000;
 
 	// Current number of toaster...
 	public int currentNumberOfToaster = 0;
 
 	// Last opened toaster
 	public int maxToaster = 0;
 
 	// Max number of toasters for the screen
 	public int maxToasterInSceen;
 
 	// Background image
 	private Image backgroundImage;
 
 	// Font used to display message
 	private Font font;
 
 	// Color for border
 	private Color borderColor;
 
 	// Color for toaster
 	private Color toasterColor;
 
 	// Set message color
 	private Color messageColor;
 
 	// Set the margin
 	int margin;
 
 	// Flag that indicate if use alwaysOnTop or not.
 	// method always on top start only SINCE JDK 5 !
 	boolean useAlwaysOnTop = true;
 
 	/**
 	 * Constructor to initialized toaster component...
 	 * 
 	 * @author daniele piras
 	 * @wbp.parser.entryPoint
 	 * 
 	 */
 	public ToasterTest() {
 		try{
 			
 		}catch(Exception e) {}
 		
 		// Set default font...
 		font = new Font("Arial", Font.BOLD, 12);
 		// Border color
 		borderColor = Color.GRAY;
 		toasterColor = Color.BLACK;
		messageColor = Color.WHITE;
 		useAlwaysOnTop = true;
 		// Verify AlwaysOnTop Flag...
 		try {
 			JWindow.class.getMethod("setAlwaysOnTop",
 					new Class[] { Boolean.class });
 		} catch (Exception e) {
 			useAlwaysOnTop = false;
 		}
 	}
 	/**
 	 * Show a toaster with the specified message and the associated icon.
 	 * @wbp.parser.entryPoint
 	 */
 	public void showToaster(Icon icon, String msg) {
 		SingleToaster singleToaster = new SingleToaster(this);
 		if (icon != null) {
 			singleToaster.iconLabel.setIcon(icon);
 		}
 		singleToaster.message.setText(msg);
		singleToaster.message.setBorder(null);
 		singleToaster.animate();
 	}
 
 	/**
 	 * Show a toaster with the specified message.
 	 */
 	public void showToaster(String msg) {
 		showToaster(null, msg);
 	}
 
 	/**
 	 * @return Returns the font
 	 */
 	public Font getToasterMessageFont() {
 		return font;
 	}
 
 	/**
 	 * Set the font for the message
 	 */
 	public void setToasterMessageFont(Font f) {
 		font = f;
 	}
 
 	/**
 	 * @return Returns the borderColor.
 	 */
 	public Color getBorderColor() {
 		return borderColor;
 	}
 
 	/**
 	 * @param borderColor
 	 *            The borderColor to set.
 	 */
 	public void setBorderColor(Color borderColor) {
 		this.borderColor = borderColor;
 	}
 
 	/**
 	 * @return Returns the displayTime.
 	 */
 	public int getDisplayTime() {
 		return displayTime;
 	}
 
 	/**
 	 * @param displayTime
 	 *            The displayTime to set.
 	 */
 	public void setDisplayTime(int displayTime) {
 		this.displayTime = displayTime;
 	}
 
 	/**
 	 * @return Returns the margin.
 	 */
 	public int getMargin() {
 		return margin;
 	}
 
 	/**
 	 * @param margin
 	 *            The margin to set.
 	 */
 	public void setMargin(int margin) {
 		this.margin = margin;
 	}
 
 	/**
 	 * @return Returns the messageColor.
 	 */
 	public Color getMessageColor() {
 		return messageColor;
 	}
 
 	/**
 	 * @param messageColor
 	 *            The messageColor to set.
 	 */
 	public void setMessageColor(Color messageColor) {
 		this.messageColor = messageColor;
 	}
 
 	/**
 	 * @return Returns the step.
 	 */
 	public int getStep() {
 		return step;
 	}
 
 	/**
 	 * @param step
 	 *            The step to set.
 	 */
 	public void setStep(int step) {
 		this.step = step;
 	}
 
 	/**
 	 * @return Returns the stepTime.
 	 */
 	public int getStepTime() {
 		return stepTime;
 	}
 
 	/**
 	 * @param stepTime
 	 *            The stepTime to set.
 	 */
 	public void setStepTime(int stepTime) {
 		this.stepTime = stepTime;
 	}
 
 	/**
 	 * @return Returns the toasterColor.
 	 */
 	public Color getToasterColor() {
 		return toasterColor;
 	}
 
 	/**
 	 * @param toasterColor
 	 *            The toasterColor to set.
 	 */
 	public void setToasterColor(Color toasterColor) {
 		this.toasterColor = toasterColor;
 	}
 
 	/**
 	 * @return Returns the toasterHeight.
 	 */
 	public int getToasterHeight() {
 		return toasterHeight;
 	}
 
 	/**
 	 * @param toasterHeight
 	 *            The toasterHeight to set.
 	 */
 	public void setToasterHeight(int toasterHeight) {
 		this.toasterHeight = toasterHeight;
 	}
 
 	/**
 	 * @return Returns the toasterWidth.
 	 */
 	public int getToasterWidth() {
 		return toasterWidth;
 	}
 
 	/**
 	 * @param toasterWidth
 	 *            The toasterWidth to set.
 	 */
 	public void setToasterWidth(int toasterWidth) {
 		this.toasterWidth = toasterWidth;
 	}
 
 	public Image getBackgroundImage() {
 		return backgroundImage;
 	}
 
 	public void setBackgroundImage(Image backgroundImage) {
 		this.backgroundImage = backgroundImage;
 	}
 }
 
 
 
