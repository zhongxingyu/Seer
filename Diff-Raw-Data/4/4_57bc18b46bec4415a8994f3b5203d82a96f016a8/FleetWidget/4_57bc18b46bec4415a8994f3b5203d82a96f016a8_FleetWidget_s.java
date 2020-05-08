 package ui;
 
 import graphic.Camera;
 import graphic.Render;
 
 import java.util.HashMap;
 import java.util.Map.Entry;
 
 import org.newdawn.slick.Color;
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.Image;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.geom.Vector2f;
 
 import state.Fleet;
 import state.Unit;
 import state.UnitStack;
 
 public class FleetWidget extends IndexedDialog
 {
 	/**
 	 * An internal class in order to keep count of unit selections inside a fleet.
 	 */
 	private class StackSelection
 	{
 		float selected;
 		int max;
 		Unit design;
 	}
 	
 // Internals ==========================================================================================================	
 	private Fleet fleet;
 	private Image[] backgrounds;
 	private int[][] bckDeltas;
 	private StackSelection[] cache;
 	private int numSteps;
 
 // Public Methods =====================================================================================================
 	public FleetWidget() throws SlickException
 	{
 		this.fleet = null;
 		this.numSteps = 6;
 		backgrounds = new Image[] 
 			{
 				new Image("resources/fleetBase.png"),
 				new Image("resources/fleetExt1.png"),
 				new Image("resources/fleetExt2.png"),
 				new Image("resources/fleetExt3.png"),
 				new Image("resources/fleetExt4.png")
 			};
 		
 		bckDeltas = new int[][]
 			{
 				{	-74,	-60,	-121, -168,	-108 },
 				{	-119,	-121,	-105,	-129,	-169 }
 			};
 	}
 
 	/**
 	 * Sets the task fleet to be displayed by this
 	 * @param fleet
 	 */
 	public void showFleet(Fleet fleet)
 	{
 		this.fleet = fleet;
 		
 		// Reset the selected values of the ships for this fleet to their maximum value.
 		if(fleet != null)
 		{
 			cache = new StackSelection[fleet.stacks().size()];
 			int i=0;
 			for(Entry<Unit, UnitStack> entry : fleet.stacks().entrySet())
 			{
 				cache[i] = new StackSelection();
 				cache[i].design = entry.getKey();
 				cache[i].max = entry.getValue().quantity();
 				cache[i].selected = cache[i].max;
 				i++;
 			}
 		}
 	}
 	
 	public Fleet selectedfleet()
 	{
 		return fleet;
 	}
 
 	public void render(GameContainer gc, Graphics g)
 	{
 		// If no star is being displayed, do nothing.
 		if(fleet == null)
 			return;
 		
 		// Make it so drawing stars is always done in local coordinates.
 		Camera.instance().pushLocalTransformation(g, fleet.position());
 
 		// Decide how many segments to show.
 		int numStacks = fleet.stacks().size();
 		for(int i=0; i<=(numStacks-1)/4 && i<5; i++)
 			backgrounds[i].draw(bckDeltas[0][i], bckDeltas[1][i]);
 
 		// Paint the icons and numbers.
 		for(int i=0; i<cache.length; i++)
 		{
 			// Draw the icon.
 			Vector2f pos = indexToCoord(i);
 			cache[i].design.image().draw(pos.x-15, pos.y-15);
 			
 			// Calculate location and draw the count for the stack.
 			g.setColor(Color.orange);
 			float length = pos.length();
 			String number = Integer.toString((int)cache[i].selected);
 			pos.normalise().scale(length + 10.0f);
 			g.fillRect(
 						pos.x - Render.normal.getWidth(number)/2,
 						pos.y - Render.normal.getHeight()/2,
 						Render.normal.getWidth(number),
 						Render.normal.getHeight());
 			Render.normal.drawString(
 						pos.x - Render.normal.getWidth(number)/2,
 						pos.y - Render.normal.getHeight()/2,
 						number, Color.black);
 			
 			// Check if we also display the local information.
 			if(hoverIndex == i)
 			{
 				Render.titles.drawString(120, -100, cache[i].design.name());
 			}
 		}
 		
 		g.popTransform();
 	}
 
 	public Fleet getFleetFromSelection()
 	{
 		// Check if a split can be done.
 		if(fleet.orbiting() == null)
 			return null;	// Can't split in transit.
 		
 		// Collect a map of selections.
 		HashMap<Unit, Integer> split = new HashMap<Unit, Integer>();
 		boolean everything = true;
 		for(StackSelection s : cache)
 		{
 			if(s.selected > 0)
 				split.put(s.design, (int)s.selected);
 			
 			if(s.selected != s.max)
 				everything = false;
 		}
 		
 		return everything ? fleet : fleet.split(split);
 	}
 	
 	/* (non-Javadoc)
 	 * @see ui.IndexedDialog#indexToCoord(int)
 	 */
 	@Override
 	protected Vector2f indexToCoord(int index)
 	{
 		// Determine first 12 segments.
 		if(index < 12)
 		{
 			float angle = index*15.0f;
 			if(index%2 == 0)
 				angle = -angle - 15.0f;
 			
 			return new Vector2f(angle).scale(98.5f);
 		}
 		else
 		{
 			float angle = -(index-12)*10.0f - 180.0f;
 			if(index%2 == 1)
 				angle = -angle + 10.0f;
 
 			return new Vector2f(angle).scale(145.5f);
 		}
 	}
 	
 
 	/* (non-Javadoc)
 	 * @see ui.IndexedDialog#location()
 	 */
 	@Override
 	public Vector2f location()
 	{
 		return fleet == null ? null : fleet.location().getPos();
 	}
 	
 
 	/* (non-Javadoc)
 	 * @see ui.IndexedDialog#coordToIndex(org.newdawn.slick.geom.Vector2f)
 	 */
 	@Override
 	protected int coordToIndex(Vector2f vector)
 	{
 		double angle = vector.getTheta();
 		double radius = vector.length();
 		
 		if(54 < radius && radius < 74 )
 		{
 			// Buttons
 			if(angle >= 310)
 				return -1 - (int)((angle - 310.0) / 20.0);
 			
 			if(angle <= 50)
 				return -1 - (int)((angle + 50) / 20.0);
 		}
 		if(76 < radius && radius < 121 )
 		{
 			// First circle, all of it works
 			int aux = (int)(360 - angle) / 30;
 			if(aux < 6)
 				return aux*2;
 			else
 				return 23 - aux*2; 
 		}
 		else if(123 < radius && radius < 168 )
 		{
 			// Second circle
 			int aux = (int)(angle - 10) / 20;
 			if(aux > 2)
 			{
 				if(aux < 9)
 					return 28 - aux*2;
 				else if(aux < 14)
 					return aux*2 - 5;
 			}
 		}

 		return NO_INDEX;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see ui.IndexedDialog#mouseClick(int, int)
 	 */
 	@Override 
 	public void mouseClick(int button, int delta)
 	{
 		// Check if visible.
		if(fleet == null || hoverIndex <= NO_INDEX || delta != 0)
 			return;
 		
 		// Process if it's a button.
 		if(hoverIndex < 0)
 		{
 			// TODO
 		}
 		
 		// Process if its a stack.
 		else
 		{
 			// This calculation may seem rather convoluted and the % operator may sound like a better idea, but this behavior is rather rare. 
 			// If we are close to the maximum, we want to go to 12 before going pass 12. Example to avoid: 0, 3, 6, 9, 12, 2, 5, 8, 11...
 			float step = Math.max(1.0f * cache[hoverIndex].max / numSteps, 1.0f);
 			if(button == 0)
 			{
 				if(cache[hoverIndex].selected == cache[hoverIndex].max)
 					cache[hoverIndex].selected = 0;
 				else
 					cache[hoverIndex].selected = Math.min(cache[hoverIndex].selected + step, cache[hoverIndex].max);
 			}
 			else if(button == 1)
 			{
 				if(cache[hoverIndex].selected < 1.0f)
 					cache[hoverIndex].selected = cache[hoverIndex].max;
 				else
 					cache[hoverIndex].selected = Math.max(cache[hoverIndex].selected - step, 0.0f);
 			}
 			System.out.println(cache[hoverIndex].selected);
 		}
 		
 		return;
 	}
 
 }
