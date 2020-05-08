 import java.awt.Color;
 import info.gridworld.actor.Actor;
 import info.gridworld.grid.Grid;
 import info.gridworld.grid.Location;
 
 public class Drone extends Actor
 {
  	private int pointValue;
 	public ActorWorld world;
 
 	public Drone(ActorWorld w)
 	{
 		setColor(Color.CYAN);
 		pointValue = 50;
 		world = w;
 	}
 	
 	public Drone(ActorWorld w, int p)
 	{
 		pointValue = p;
 		world = w;
 	}
 
 	public void act()
 	{
 		Grid<Actor> gr = getGrid();
 		Location loc = getLocation();
 		Location next = loc.getAdjacentLocation(loc.getDirectionToward(trackPlayer()));
 		setDirection(loc.getDirectionToward(next));
 		if(gr.get(next) instanceof PacifistShip)
 		{
 			if(((PacifistShip)(gr.get(next))).getInvinc() == false)
 				((PacifistShip)(gr.get(next))).die();
 			else removeSelfFromGrid();
 		}
 		else if(gr.get(next) instanceof Laser)
 		{
 			gr.get(next).removeSelfFromGrid();
			die(/*getPointValue()*/);
 		}
 		else if(canMove())
 			move();
 	}
 
	public void die(/*int p*/)
 	{
 		Grid<Actor> gr = getGrid();
		world.setScore(world.getScore()+/*p*/(getPointValue()*
 				((PacifistShip)(gr.get(trackPlayer()))).getScoreMultiplier()));
 		this.removeSelfFromGrid();
 	}
 
 	public Location trackPlayer()
 	{
 		Grid<Actor> gr = getGrid();
 		for(int r = 0; r < gr.getNumRows(); r++)
 		{
 			for(int c = 0; c < gr.getNumCols(); c++)
 			{
 				Location loc = new Location(r,c);
 				if(gr.get(loc) instanceof PacifistShip 
 						|| gr.get(loc) instanceof PlayerShip)
 					return loc;
 			}
 		}
 		return null;
 	}
 
 	public void move()
 	{
 		Grid<Actor> gr = getGrid();
 		if (gr == null)
 			return;
 		Location loc = getLocation();
 		Location playerLoc = trackPlayer();
 		Location next = loc.getAdjacentLocation(loc.getDirectionToward(playerLoc));
 		if(gr.get(next) instanceof PacifistShip)
 		{
 			((PacifistShip)(gr.get(next))).die();
 		}
 		else if(gr.isValid(next) && !(gr.get(next) instanceof Drone))
 			moveTo(next);
 	}
 
 	public boolean canMove()
 	{
 		Grid<Actor> gr = getGrid();
 		if (gr == null)
 			return false;
 		Location loc = getLocation();
 		Location playerLoc = trackPlayer();
 		Location next = loc.getAdjacentLocation(loc.getDirectionToward(playerLoc));
 		if (!gr.isValid(next))
 			return false;
 		Actor neighbor = gr.get(next);
 		return (neighbor == null) && (!(neighbor instanceof Laser)
 				|| !(neighbor instanceof Drone))/*|| (neighbor instanceof PowerUp)*/;
 	}
 
 	public int getPointValue()
 	{
 		return pointValue;
 	}
 }
