 import java.awt.Color;
 import java.awt.geom.Ellipse2D;
 
 
 /**
  * This class is the small picture showing the color and number of armies that a territory shows.
  * It's supposed to show itself with display() but that needs to be fixed. (See Gfx.draw().)
  * It also requires a set of coordinates to display at, we don't have those in territory data yet.
  * That needs the mouse click thing, probably for Michael to do.
  * @author Ben Huchley
  *
  */
 public class TerritoryGraphics
 {
 	public Territory parent; //the territory it is showing
 	public int xCoord; //coords
 	public int yCoord;
 	public static final int SIDE_LENGTH = 20; //The length of the sides of the rectangle enclosing the oval.
<<<<<<< HEAD
	public TerritoryGraphics(int xCoord, int yCoord, Territory parent)
=======
 	public Ellipse2D.Double icon;
 	public TerritoryGraphics(int xCoord,int yCoord,Territory parent)
>>>>>>> 0e11ed25431e5463b68109c06d59778b4275f04f
 	{
 		this.parent = parent;
 		this.xCoord = xCoord;
 		this.yCoord = yCoord;
 		icon = new Ellipse2D.Double(xCoord-SIDE_LENGTH/2, yCoord-SIDE_LENGTH/2, SIDE_LENGTH, SIDE_LENGTH);
 	}
 	public TerritoryGraphics(Territory parent)
 	{
 		this.parent = parent;
 	}
 	public void setCoords(int xCoord,int yCoord)
 	{
 		this.xCoord = xCoord;
 		this.yCoord = yCoord;
 		icon.setFrame(xCoord-SIDE_LENGTH/2, yCoord-SIDE_LENGTH/2, SIDE_LENGTH, SIDE_LENGTH);
 	}
 	
 	public Color getColor()
 	{
 		if (parent.getOwner() == null) return Color.WHITE;
 		return parent.getOwner().getColor();
 	}
 }
