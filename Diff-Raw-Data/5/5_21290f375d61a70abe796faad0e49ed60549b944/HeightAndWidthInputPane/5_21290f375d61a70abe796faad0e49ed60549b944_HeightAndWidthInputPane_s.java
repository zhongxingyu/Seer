 import javax.swing.*;
 import java.awt.*;
 
 /**
  * This class will create a JOptionPane to prompt a user for a width and height.  There are methods to check for input validity and retrieve the width and height entered.
  * @author Logan Gore
  * TODO: Create a cleaner panel with sliders and better input validation
  */
 public class HeightAndWidthInputPane
 {
 	//User input values of width and height
 	private int width;
 	private int height;
 	
 	//Given values of maximum width and height
 	private int maxWidth;
 	private int maxHeight;
 
 	//GUI elements
 	private JPanel mainPanel;
 	private JTextField widthField;
 	private JTextField heightField;
 	
 	/**
 	 * Constructor to create a JOptionPane that has the user input width and height dimensions
 	 * @param maxWidth the maximum width to be accepted
 	 * @param maxHeight the maximum height to be accepted
 	 */
 	public HeightAndWidthInputPane(int maxWidth, int maxHeight)
 	{
 		this.maxWidth = maxWidth;
 		this.maxHeight = maxHeight;
 		
 		//Create the JPanel
 		createPanel();
 		
 		//Get the user's desired dimensions using a JOptionPane
 		getDimensions();
 	}
 	
 	/**
 	 * Creates the JPanel which serves as the main input for the JOptionPane
 	 */
 	private void createPanel()
 	{
 		//Create the main panel
 		mainPanel = new JPanel(new GridLayout(4, 2));		
 		
 		//Create the width and height text fields
 		widthField = new JTextField();
 		heightField = new JTextField();
 		
 		//Add some basic instructions to the main panel
 		mainPanel.add(new JLabel("Enter dimensions for your new picture."));
 		mainPanel.add(new JLabel("Max dimensions: Width = " + maxWidth + ", Height = " + maxHeight));
 		
 		//Add the width label and text field to the main panel
 		mainPanel.add(new JLabel("New Width:"));
 		mainPanel.add(widthField);
 		
 		//Add the height label and text field to the main panel
 		mainPanel.add(new JLabel("New Height"));
 		mainPanel.add(heightField);
 	}
 	
 	/**
 	 * Use a JOptionPane to get a user's desired width and height dimensions for the new picture to be created
 	 */
 	public void getDimensions()
 	{
 		//Show the JOptionPane to get the user's desired width and height
 		int result = JOptionPane.showConfirmDialog(null, mainPanel, "New Picture Dimensions", JOptionPane.OK_CANCEL_OPTION);
 		
 		//If the user clicked OK, validate the input
 		if (result == JOptionPane.OK_OPTION)
 			validateInput();
 	}
 	
 	/**
 	 * Validate the input that was given in the JOptionPanel
 	 */
 	private void validateInput()
 	{
 		//Validate the width field
 		try
 		{
 			//Parse the width given by the user
 			int widthFieldVal = Integer.parseInt(widthField.getText());
 			
 			//If the given value is valid, set it as the width
 			if (widthFieldVal < maxWidth && widthFieldVal > 0)
 				width = widthFieldVal;
 			//Otherwise, throw an IllegalArgumentException
 			else
				throw new IllegalArgumentException("Value in width field is larger than max width.");
 		}
 		catch(NumberFormatException e)
 		{
 			//Set the width to the default value and warn the user
 			width = maxWidth;
 			JOptionPane.showMessageDialog(null, "Warning: width is not an integer.  Using image's default width instead.");
 		}
 		catch(IllegalArgumentException e)
 		{
 			//Set the width to the default value and warn the user
 			width = maxWidth;
 			JOptionPane.showMessageDialog(null, "Warning: " + e.getMessage());		
 		}
 		
 		//Validate the height field
 		try
 		{
 			//Parse the height given by the user
 			int heightFieldVal = Integer.parseInt(heightField.getText());
 			
 			//If the given value is valid, set it as the height
 			if (heightFieldVal < maxHeight)
 				height = heightFieldVal;
 			//Otherwise, set the height as the maxHeight and warn the user
 			else
				throw new IllegalArgumentException("Value in height field in larger than max height");
 		}
 		catch(NumberFormatException e)
 		{
 			//Set the width to the default value and warn the user
 			height = maxHeight - 1;
 			JOptionPane.showMessageDialog(null, "Warning: height is not an integer.  Using image's default height instead.");
 		}
 		catch(IllegalArgumentException e)
 		{
 			//Set the width to the default value and warn the user
 			width = maxWidth - 1;
 			JOptionPane.showMessageDialog(null, "Warning: " + e.getMessage());	
 		}
 	}
 	
 	/**
 	 * Getter method to return the width the user input.
 	 * NOTE: Will return maxWidth if the user's input was not valid.
 	 */
 	public int getWidth()
 	{
 		return width;
 	}
 	
 	/**
 	 * Getter method to return the height the user input.
 	 * NOTE: Will return maxHeight if the user's input was not valid.
 	 */
 	public int getHeight()
 	{
 		return height;
 	}
 }
