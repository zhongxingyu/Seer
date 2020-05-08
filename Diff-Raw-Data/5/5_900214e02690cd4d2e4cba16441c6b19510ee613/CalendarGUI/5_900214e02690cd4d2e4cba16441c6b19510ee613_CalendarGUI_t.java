 package dealwithcalendar;
 
 import java.io.*;
 import java.awt.*;
 import javax.imageio.ImageIO;
 import javax.swing.*;
 
 /**
  * A JFrame extension implementing the user interface
  * 
  * @author Deal With It Productions
  * @version 0.1
  */
 public class CalendarGUI extends JFrame
 {
	private input.Mouse mouse;
 	/**
 	 * Constructor, sets the window up, centers it and changes the icon
 	 * 
 	 * @param x The width of the window
 	 * @param y The height of the window
 	 */
 	public CalendarGUI(int x, int y)
 	{
 		this.setDefaultCloseOperation(EXIT_ON_CLOSE); //Close down from X
 		this.setBounds(new Rectangle(x, y)); //Set size based on parameters
 		JPanel temp = (JPanel) this.getContentPane(); //Get the content pane..
 		temp.setPreferredSize(this.getSize()); //..and set its preferred size
 		this.setLocationRelativeTo(null); //Center the window
 		
		mouse = new input.Mouse(); //create a mouse listener
		this.addMouseListener(mouse); //add the mouse listener to the window
		
 		try
 		{	//set the logo as icon
 			this.setIconImage(ImageIO.read(new File("icon.jpg")));
 		} catch (IOException ex)
 		{	//advanced error handling
 			System.err.println("No icon found, deal with it");
 		}
 		
 		this.pack(); //Pack everything up..
 		this.setVisible(true); //..and show the interface window
 	}
 }
