 /**
  * Contains all the high-level game functions and implements the actual gameplay.
  */
 package kaninator.game;
 import kaninator.graphics.*;
 import kaninator.mechanics.*;
 import java.util.ArrayList;
 
 /**
  * Uses a model object internally to represent the pieces of gore in the game.
  * @see kaninator.game.Model
  */
 public class Gore
 {
 	private static final double GORE_SPEED = 6.0;
 	private static final double GORE_JUMP_SPEED = 15.0;
 	private static final double RADIUS_CONSTANT = 1.0 / 4.0;
 	
 	private Model model;
 	private int ttyl;
 	
 	public Gore(ArrayList<Animation> animations, Map _map, DynamicObject victim, double vel_x, double vel_y, int _ttyl) throws ModelException
 	{
 		ttyl = _ttyl;
 		
 		model = new Model(animations, _map, victim.get_x(), victim.get_y(), RADIUS_CONSTANT, GORE_SPEED);
 		model.setVelX(vel_x);
 		model.setVelY(vel_y);
 		model.setVelHeight(GORE_JUMP_SPEED);
 	}
 	
 	public DynamicObject getMainObject()
 	{
 		return model.getModel();
 	}
 	
 	public boolean update()
 	{
 		if(model.onGround())
 		{
 			model.setVelX(0);
 			model.setVelY(0);
 		}
 		
		model.update();
 		ttyl--;
		
 		return ttyl <= 0;
 	}
 }
