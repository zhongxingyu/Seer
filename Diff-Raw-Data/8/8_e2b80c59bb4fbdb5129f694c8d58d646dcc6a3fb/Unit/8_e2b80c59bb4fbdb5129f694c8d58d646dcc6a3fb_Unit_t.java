 package game;
 
 
 import java.util.ArrayList;
 
 import collision.CollisionSystem;
 import collision.Move;
 
 import util.Vector2D;
 
 import engine.GlobalInfo;
 
 
 public class Unit extends Entity {
 
 	public ArrayList<Vector2D> path;
 	public Vector2D direction = new Vector2D(0,0);
 	double speed = 4;
 	public boolean actionmode = true;
 	//Unit health, 0-100
 	int health = 0;
 	
 	public Unit(Vector2D position, Game game) {
 		super(position,game);
 		this.game = game;
 		health = 100;
 	}
 	
 	public void moveTo(Vector2D dest) {
 		path = game.pathPlanner.plan(pos, dest);
 	}
 	
 	public void setHealth(int h) {
 		if(h > 100) {
 			h = 100;
 		}
 		else if(h < 0) {
 			h = 0;
 		}
 		
 		health = h;
 	}
 	
 	public int getHealth() {
 		return health;
 	}
 	
 	public Vector2D velocityDt() {
 		return velocity.scalar(game.dt);
 	}
 	
 	public void process(double dt) {
 		
 		CollisionSystem cs = game.collisionSystem;
 		
 		
 		if(path != null && path.size() > 0) {
 			if(pos.distance(path.get(0)) < GlobalInfo.accuracy) {
 				path.remove(0);
 				velocity = new Vector2D(0,0);
 			}
 			if(path.size() > 0) {
 				Vector2D dest = path.get(0);
 				velocity = dest.subtract(pos).normalize().scalar(speed);
 			}
 			
 		}
 		
 		Move move = cs.collideAndSlide(this);
 		move.apply();
 		
 	}
 
 	@Override
 	public double getRadius() {
 		return 0.49;
 	}
 
 }
