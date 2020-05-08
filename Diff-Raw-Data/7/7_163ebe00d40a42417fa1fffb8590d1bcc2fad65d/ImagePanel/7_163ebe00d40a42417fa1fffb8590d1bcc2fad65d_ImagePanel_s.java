 import javax.swing.*;
 import java.awt.*;
 import java.awt.image.*;
 import java.io.*;
 import javax.imageio.*;
 
 /**
  * Helper class to show a given image as a JPanel
  * The benefit of this is allowing other items to be added to the JPanel.
  * This makes it easier to create the cropping frame.
  */
 public class ImagePanel extends JPanel
 {
 	//Image to load
 	private BufferedImage myImage;
 	
 	/**
 	 * Constructor to create a special JPanel that shows the given image file
 	 * @param image an image file that can be loaded as a BufferedImage
 	 */
 	public ImagePanel(File image)
 	{
 		//Try to load the image file
 		try
 		{
 			myImage = ImageIO.read(image);
 		}
 		//If an IOException was encountered, tell the user
 		catch(IOException e)
 		{
 			JOptionPane.showMessageDialog(null, "Error: " + e.getMessage());				
 		}
 		
 		//TODO: This isn't properly allowing scrolling
 		setSize(myImage.getWidth(), myImage.getHeight());
 		setAutoscrolls(true);
 		
 		//Set the layout manager to null to allow repositioning the edit panel
 		setLayout(null);
 	}
 	
 	/**
 	 * Constructor to create a special JPanel that shows the given image file
 	 * @param image the BufferedImage to load
 	 */
 	public ImagePanel(BufferedImage image)
 	{
 		myImage = image;
 	}
 	
 	@Override
 	protected void paintComponent(Graphics g)
 	{
 		super.paintComponent(g);
 		g.drawImage(myImage, 0, 0, null);
 	}
 
 }
