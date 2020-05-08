 //////////////////////////////////////////////////////////////////////////////////
 // Class: 	FFDot
 //
 // Purpose: This class encapsulates the functionality of the small particles used
 //			in our simulations, based on drawing an ellipse at (x,y) coordinates
 //			within a rectangular boundary.
 //
 //////////////////////////////////////////////////////////////////////////////////
 
 import java.awt.Rectangle;
 import java.awt.geom.Ellipse2D;
 import java.util.Random;
 
 public class FFDot
 {
 	private int x;
 	private int y;
 	private int diameter;
 	private Random rando = new Random();
 	
 	//Constructs a new dot w/given diameter at random coordinates within a rectangle
 	public FFDot(Rectangle boundary, int diameter)
 	{	
 		this.diameter = diameter;
 		int height = (int) boundary.getHeight();
 		int width = (int) boundary.getWidth();
		y = rando.nextInt((int)(height - diameter )) ;
		x = rando.nextInt((int)(width - diameter )) ;	
 	}
 	
 	//returns the ellipse
 	public Ellipse2D.Double getEllipse()
 	{
 		return new Ellipse2D.Double(x, y, diameter, diameter);
 	}
 	
 	public void setDiameter(int diameter)
 	{
 		this.diameter = diameter;
 	}	
 }
