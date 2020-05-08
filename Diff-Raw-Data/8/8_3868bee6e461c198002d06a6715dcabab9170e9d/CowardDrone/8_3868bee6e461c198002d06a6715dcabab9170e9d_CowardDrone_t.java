 import info.gridworld.actor.Actor;
 import info.gridworld.grid.Grid;
 import info.gridworld.grid.Location;
 
 import java.awt.Color;
 
 public class CowardDrone extends Drone
 {
	//private int pointValue;
 	
 	public CowardDrone(ActorWorld w)
 	{
		super(w, 150);
 		setColor(Color.GREEN);
		//pointValue = 150;
 	}
 	
 	public void checkLasers()
 	{
 		Grid<Actor> gr = getGrid();
 		Location loc = getLocation();
 		Location right = loc.getAdjacentLocation(90);
 		Location left = loc.getAdjacentLocation(270);
 		Location up = loc.getAdjacentLocation(0);
 		Location down = loc.getAdjacentLocation(180);
 		Actor u = new Actor(), d = new Actor(), l = new Actor(), r = new Actor();
 		
 		if(gr.isValid(right))
 			r = gr.get(right);
 		if(gr.isValid(left))
 			l = gr.get(left);
 		
 		if(gr.isValid(up))
 			u = gr.get(up);
 		
 		if(gr.isValid(down))
 			d = gr.get(down);
 		
 		runAway(loc, u, d, l, r);
 	}
 	
 	public void runAway(Location loc, Actor up, Actor down, Actor left, Actor right)
 	{
 		Grid<Actor> gr = getGrid();
 		int rand = (int)(Math.random()*2+1);
 		//System.out.println(rand);
 		if(up instanceof Laser || down instanceof Laser)
 		{
 			//if(up.getDirection() == up.getLocation().getDirectionToward(this.getLocation()))
 			//{
 				Location move = loc.getAdjacentLocation(90);
 				Location move2 = loc.getAdjacentLocation(270);
 				if(rand == 1 && gr.isValid(move))
 				{	
 					if(gr.get(move) instanceof Laser)
 						die();
 					else moveTo(move);
 				}
 				else
 				{
 					if(gr.get(move2) instanceof Laser)
 						die();
 					else moveTo(move2);
 				}
 			//}
 		}
 		else if(right instanceof Laser || left instanceof Laser)
 		{
 			Location move = loc.getAdjacentLocation(0);
 			Location move2 = loc.getAdjacentLocation(180);
 			if(gr.isValid(move))
 				moveTo(move);
 			else moveTo(move2);
 		}
 	}
 	
 	public void act()
 	{
 		checkLasers();
 	}
 }
