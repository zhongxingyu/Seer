 package java.util;
 import java.awt.Rectangle;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 
 import lejos.nxt.Button;
 import lejos.nxt.LCD;
 
 public class RectGroup extends ArrayList<Rectangle> implements List<Rectangle>
 {
 	@SuppressWarnings("deprecation")
     public RectGroup(Rectangle... rects)
 	{
 		super(rects);
 	}
 
 	public RectGroup()
 	{
 		super();
 	}
 
 	public RectGroup(Collection<? extends Rectangle> c)
 	{
 		super(c);
 	}
 
 	public RectGroup(int initialCapacity)
 	{
 		super(initialCapacity);
 	}
 
 	public boolean intersects(Rectangle rect)
 	{
 		for(Rectangle r : this)
 		{
 			if(rect.intersects(r))
 			{
 				return true;
 			}
 		}
 		return false;
 	}
 
 	public Rectangle getBoundingBox()
 	{
 		if(this.size() == 0)
 			return null;
 		
 		Rectangle first = this.get(0);
 		
 		int minY = first.y, maxY = first.y + first.height,
 		minX = first.x, maxX = first.x + first.width;
 		
 		for(Rectangle rect : this)
 		{
 			minY = Math.min(rect.y,minY);
 			maxY = Math.max(rect.y + rect.height, maxY);
 			minX = Math.min(rect.x, minX);
 			maxX = Math.max(rect.x + rect.width, maxX);
 		}
 		
 		return new Rectangle(minX, minY, maxX-minX, maxY-minY);
 	}
 
 	public List<RectGroup> getDistinctGroups()
 	{
 		List<RectGroup> groups = new ArrayList<RectGroup>();
 		// Create a list of groups to hold grouped rectangles.
 
 		for(Rectangle newRect : this)
 		{
 			List<RectGroup> newGroupings = new ArrayList<RectGroup>();
 
 			for(RectGroup group : groups)
			{
 				if(group.intersects(newRect))
				{
 					newGroupings.add(group);
					break;
				}
			}
 
 			RectGroup newGroup = new RectGroup(newRect);
 
 			for(List<Rectangle> oldGroup : newGroupings)
 			{
 				groups.remove(oldGroup);
 				newGroup.addAll(oldGroup);
 			}
 			groups.add(newGroup);
 		}
 		return groups;
 	}
 	
 	public Rectangle getLargest()
 	{
 		return getLargest(-1);
 	}
 	
 	public Rectangle getLargest(int minArea)
 	{
 		Rectangle largest = null;
 		for(Rectangle r : this)
 		{
 			int area = r.width*r.height;
 			if(area > minArea)
 			{
 				largest = r;
 				minArea = area;
 			}
 		}
 		return largest;
 	}
 	public static void main(String... args)
 	{
 		RectGroup rects = new RectGroup(new Rectangle(60, 60, 50, 50),
 		                                new Rectangle(70, 55, 50, 50),
 		                                new Rectangle(80, 52, 50, 50),
 		                                new Rectangle(25, 25, 50, 50),
 		                                new Rectangle(60, 60, 50, 50),
 		                                new Rectangle(70, 55, 50, 50),
 		                                new Rectangle(80, 52, 50, 50),
 		                                new Rectangle(0, 0, 50, 50));
 
 		LCD.drawString("Length = " + rects.getDistinctGroups().size(), 0, 0);
 		Button.ENTER.waitForPressAndRelease();
 	}
 }
