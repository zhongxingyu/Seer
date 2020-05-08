 package controllers;
 
 import javax.swing.ImageIcon;
 
 /**
  * <p>This class contains helper functions for images used throughout the program.
  * 
  * @author Adam Childs
  * @version 1.00
  */
 public class Image_Controller
 {
 	
 	public Image_Controller()
 	{
 		// Don't need
 	}
 
 	/**
 	 * <p>Loads the specified image, given the location i
 	 * @param i the location of the image
 	 * @return a new ImageIcon
 	 */
 	public ImageIcon loadImage(String i)
 	{
 		try {
			return new ImageIcon(this.getClass().getClassLoader().getResource(i));
 		} catch (NullPointerException e) {
			System.out.println("NullPointerException: Image \"" + i + "\" could not be found.");
			e.printStackTrace();
 		}
 		return null;
 	}
 }
