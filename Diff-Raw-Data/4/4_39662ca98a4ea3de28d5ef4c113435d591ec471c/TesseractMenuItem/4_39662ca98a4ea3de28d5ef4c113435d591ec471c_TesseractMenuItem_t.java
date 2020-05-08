 package tesseract.menuitems;
 
 import java.awt.event.ActionListener;
 
 import javax.swing.JMenuItem;
 import javax.vecmath.Vector3f;
 
 import tesseract.World;
 
 /**
  * Abstract class for menu items.
  * 
  * @author Jesse Morgan
  */
 public abstract class TesseractMenuItem 
 	extends JMenuItem implements ActionListener {
 
 	/**
 	 * Serial ID.
 	 */
 	private static final long serialVersionUID = 1839955501629795920L;
 	
 	/**
 	 * The reference to the world.
 	 */
 	protected World myWorld;
 	
 	/**
 	 * Constructor.
 	 * 
 	 * @param theWorld The world in which to add.
 	 * @param theLabel The label for the menu item.
 	 */
 	public TesseractMenuItem(final World theWorld, final String theLabel) {
 		super(theLabel);
 		
 		myWorld = theWorld;
 		
 		addActionListener(this);
 	}
 	
 	/**
 	 * Utility method to parse a string formatted as x,y,z into a vector3f.
 	 * 
 	 * @param input A string to parse.
 	 * @return A vector3f.
 	 */
 	protected Vector3f parseVector(final String input)  {
 		String[] split = input.split(",");
 		
 		float x = Float.parseFloat(split[0]);
		float y = Float.parseFloat(split[1]);
		float z = Float.parseFloat(split[2]);
 
 		return new Vector3f(x, y, z);
 	}
 }
