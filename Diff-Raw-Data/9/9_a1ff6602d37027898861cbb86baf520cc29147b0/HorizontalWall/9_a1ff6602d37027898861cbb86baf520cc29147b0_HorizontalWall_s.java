 package chalmers.TDA367.B17.terrain;
 
 import org.newdawn.slick.geom.Vector2f;
 
 import chalmers.TDA367.B17.controller.GameController;
 import chalmers.TDA367.B17.model.*;
 
 public class HorizontalWall extends AbstractObstacle {
 
 	private static Vector2f HORIZONTAL_WALL_SIZE = new Vector2f(150, 15);
 	
 	/**
 	 * Create a new BlackWall.
 	 * @param id The id
 	 * @param position The position
 	 */
 	public HorizontalWall(int id, Vector2f position) {
 		super(id, HORIZONTAL_WALL_SIZE, position);
 		spriteID = "horizontal_wall";
 		GameController.getInstance().getWorld().addEntity(this);
 	}
 	
 	@Override
 	public void didCollideWith(Entity entity){
 		if(entity instanceof AbstractProjectile){
 
 		}
 	}
 }
