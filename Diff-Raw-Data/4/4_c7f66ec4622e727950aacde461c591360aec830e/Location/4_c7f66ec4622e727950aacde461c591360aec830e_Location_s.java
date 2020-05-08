 /* 
 	JCavernApplet.java
 
 	Title:			JCavern And Glen
 	Author:			Bill Walker
 	Description:	
 */
 
 package jcavern;
 
 import java.awt.Rectangle;
 import java.util.Vector;
 
 /**
  * Represents a location of a Thing within a World.
  *
  * @author	Bill Walker
  * @version	$Id$
  */
 public class Location
 {
 	/** * Maximum value for location coordinates. Used for hash value computations. */
 	private static final	int		gMaximumCoordinateValue = 1000;
 	
 	// Constants used for specifying directions
 	public static final	int		NORTHWEST = 7;
 	public static final	int		NORTH = 8;
 	public static final	int		NORTHEAST = 9;
 	
 	public static final	int		WEST = 4;
 	public static final	int		EAST = 6;
 
 	public static final	int		SOUTHWEST = 1;
 	public static final	int		SOUTH = 2;
 	public static final	int		SOUTHEAST = 3;
 
 	/** * X (east/west or horiziontal) coordinate. Larger values are east and/or right. */
 	private int		mX;
 
 	/** * Y (north/south or vertical) coordinate. Larger values are south and/or down. */
 	private int		mY;
 	
 	public static String directionToString(int aDirection)
 	{
 		switch (aDirection)
 		{
 			case NORTH: return "north";
 			case NORTHEAST: return "northeast";
 			case EAST: return "east";
 			case SOUTHEAST: return "southeast";
 			case SOUTH: return "south";
 			case SOUTHWEST: return "southwest";
 			case WEST: return "west";
 			case NORTHWEST: return "northwest";
 		}
 		
 		throw new IllegalArgumentException("no such direction");
 	}
 	
 	public boolean inBounds(Rectangle bounds)
 	{
 		return bounds.contains(mX, mY);
 	}
 	
 	/**
 	 * Returns a Location no closer to the bounds than the supplied inset.
 	 */
 	public Location enforceMinimumInset(Rectangle bounds, int inset)
 	{
 		// make sure newX,Y are no larger than the largest acceptable value
		int newX = Math.min(mX, bounds.x + bounds.width - inset);
		int newY = Math.min(mY, bounds.y + bounds.height - inset);
 		
 		// make sure newX, Y are no smaller than the smallest acceptable value
 		newX = Math.max(newX, bounds.x + inset);
 		newY = Math.max(newY, bounds.y + inset);
 		
 		//System.out.println(this + "enforceMinimumInset(" + bounds + ", " + inset + ") = " + newX + ", " + newY);
 		return new Location(newX, newY);
 	}
 	
 	public int getX()
 	{
 		return mX;
 	}
 	
 	public int getY()
 	{
 		return mY;
 	}
 	
 	/**
 	 * Computes the number of moves between Locations.
 	 * We use a modified Manhattan distance metric, with diagonal
 	 * moves allowed.
 	 */ 
 	public int distanceTo(Location aLocation)
 	{
 		return Math.max(Math.abs(aLocation.getX() - mX), Math.abs(aLocation.getY() - mY));
 	}
 	
 	public String toString()
 	{
 		return "[" + mX + ", " + mY + "]";
 	}
 	
 	public int hashCode()
 	{
 		return mX * gMaximumCoordinateValue + mY;
 	}
 	
 	public boolean equals(Object aLocation)
 	{
 		return (((Location) aLocation).mX == mX) && (((Location) aLocation).mY == mY);
 	}
 	
 	public Location(int x, int y)
 	{
 		mX = x;
 		mY = y;
 	}
 
 	public Location getNeighbor(int direction)
 	{
 		int	newX = mX;
 		int newY = mY;
 		
 		switch (direction)
 		{
 			case NORTH: newY--; break;
 			case NORTHEAST: newX++; newY--; break;
 			case EAST: newX++; break;
 			case SOUTHEAST: newX++; newY++; break;
 			case SOUTH: newY++; break;
 			case SOUTHWEST: newX--; newY++; break;
 			case WEST: newX--; break;
 			case NORTHWEST: newX--; newY--; break;
 			default: throw new IllegalArgumentException("No such direction");
 		}
 		
 		return new Location(newX, newY);
 	}
 	
 	public Vector getNeighbors()
 	{
 		Vector neighbors = new Vector(8);
 		
 		neighbors.addElement(getNeighbor(NORTH));
 		neighbors.addElement(getNeighbor(NORTHEAST));
 		neighbors.addElement(getNeighbor(EAST));
 		neighbors.addElement(getNeighbor(SOUTHEAST));
 		neighbors.addElement(getNeighbor(SOUTH));
 		neighbors.addElement(getNeighbor(SOUTHWEST));
 		neighbors.addElement(getNeighbor(WEST));
 		neighbors.addElement(getNeighbor(NORTHWEST));
 
 		return neighbors;
 	}
 
 	public int getDirectionToward(Location anotherLocation)
 	{
 		double	deltaX = anotherLocation.getX() - mX;
 		double	deltaY = anotherLocation.getY() - mY;
 		double	degrees = (360.0 / (2 * Math.PI)) * Math.atan(deltaY / deltaX);
 		
 		if (Math.abs(degrees) > 67.5) // either north or south
 		{
 			return deltaY > 0 ? SOUTH : NORTH;
 		}
 		else if (Math.abs(degrees) > 22.5) // one of the four diagonals
 		{
 			if (deltaY < 0)
 			{
 				return deltaX > 0 ? NORTHEAST : NORTHWEST;
 			}
 			else
 			{
 				return deltaX > 0 ? SOUTHEAST : SOUTHWEST;
 			}
 		}
 		else // either east or west
 		{
 			return deltaX > 0 ? EAST : WEST;
 		}
 	}
 
 	public Location getNeighborToward(Location anotherLocation)
 	{
 		return getNeighbor(getDirectionToward(anotherLocation));
 	}
 }
