 package trollhoehle.gamejam.magnets;
 
 import java.util.ArrayList;
 
 import org.newdawn.slick.Image;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.geom.Circle;
 
 /**
  * The core is the .. core! .. in the middle of the game. This class has extra
  * private methods for awesome special effects like a pulsing-effect or the
  * spawning of Obstacles.
  * 
  * @author Cakemix
  * 
  */
 public class Core extends Entity {
 
     private static final float OBSTACLE_SPAWNER_SPAWN_PROBABILITY = 20000f;
     private ArrayList<ObstacleSpawner> spawners;
 
     public Core(float screenWidth, float screenHeight) throws SlickException {
 	super(screenWidth / 2, screenHeight / 2, new Circle(screenWidth / 2 - 25, screenHeight / 2 - 25, 50),
 		new Image("res/images/core.png"), 0);
 	this.spawners = new ArrayList<ObstacleSpawner>();
	this.spawners.add(new ObstacleSpawner(this.getCenterX() + this.getRadius(), this.getCenterY(), 0.05f, 3000f));
     }
 
     public ArrayList<ObstacleSpawner> getObstacleSpawner() {
 	return this.spawners;
     }
 
     @Override
     public Obstacle[] update(float timePerFrame, float toCenterX, float toCenterY, float attract) {
 	this.pulse(timePerFrame);
	// TODO: Right random-math-magic!
	if (Math.random() * (timePerFrame / 1000) >= 0.5) {
 	    try {
		System.out.println("new spawner spawned");
 		this.spawners.add(new ObstacleSpawner(this.getCenterX() + this.getRadius(), this.getCenterY(), 0.05f,
			(float) (Math.random() * 3000f)));
 	    } catch (SlickException e) {
 		// TODO Auto-generated catch block
 		e.printStackTrace();
 	    }
 	}
 	return null;
     }
 
     private void pulse(float timePerFrame) {
 	// TODO maaaaake iiiiit puuuuuuuulse! :O
 
     }
 
     @Override
     public Obstacle[] collision(Entity collider) {
 	// TODO Auto-generated method stub
 	return null;
     }
 
 }
