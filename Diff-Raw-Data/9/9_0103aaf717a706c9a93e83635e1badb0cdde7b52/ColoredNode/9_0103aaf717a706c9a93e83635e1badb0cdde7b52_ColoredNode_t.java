 package linewars.display;
 
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Graphics;
 
 import linewars.gamestate.Position;
 import linewars.gamestate.mapItems.Node;
 
 /**
  * This class handles drawing a Nodes. It will draw the Node in the color of the
  * Player that controls it. A "neutral" or "contested" Node is drawn white.
  * 
  * @author Ryan Tew
  * 
  */
 public class ColoredNode
 {
 	private Node node;
 
 	/**
 	 * Constructs a ColoredNode
 	 * 
 	 * @param n
 	 *            The Node that this ColoredNode will color.
 	 */
 	public ColoredNode(Node n)
 	{
 		node = n;
 	}
 	
 	public void draw(Graphics g)
 	{
 		if(node.isContested() || node.getOwner() == null)
 		{
 			g.setColor(Color.white);
 		}
 		else
 		{
 			g.setColor(node.getOwner().getPlayerColor());
 		}
 		
 		Position pos = node.getCommandCenter().getPosition();
 		Dimension size = node.getSize();
 		
		g.fillOval((int)(pos.getX() - size.width / 2), (int)(pos.getY() - size.height / 2), size.width, size.height);
 	}
 }
