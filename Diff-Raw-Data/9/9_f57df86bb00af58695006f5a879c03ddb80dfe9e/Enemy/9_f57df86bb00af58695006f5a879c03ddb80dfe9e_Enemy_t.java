 import java.util.Random;
 
 import org.newdawn.slick.Image;
 import org.newdawn.slick.Input;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.geom.Rectangle;
 
 
 public class Enemy extends Entity
 {
 	protected final int VARIANCE = 0;
 	protected final int LOWER_RANDOM_BOUND = 380;
 	
 	public Enemy(Image[] images) throws SlickException
 	{
 		super(images);
 		this.init();
 	}
 	
 	private void init()
 	{		
 		Random rand = new Random();
 		int randomYValue = rand.nextInt(LOWER_RANDOM_BOUND) + VARIANCE;
		this.position.x = GLOBAL.SCREEN_WIDTH + 100;
 		this.position.y = randomYValue;
 		this.setVelocity(0.15f);
 		this.boundingShape = new Rectangle(this.getPosition().x, 
 					this.getPosition().y, 
 					this.getAnimationFrame().getWidth(),
 					this.getAnimationFrame().getHeight());
 		 this.colidable = true;
 	}
 	
 	public static Enemy getRandomlyPlacedEnemy() throws SlickException
 	{
 		Image[] iEnemy = {
 	    		new Image(GLOBAL.BIRD_BROWN_1, GLOBAL.chromakey), 
 				new Image(GLOBAL.BIRD_BROWN_2, GLOBAL.chromakey), 
 				new Image(GLOBAL.BIRD_BROWN_3, GLOBAL.chromakey), 
 				new Image(GLOBAL.BIRD_BROWN_4, GLOBAL.chromakey), 
 				new Image(GLOBAL.BIRD_BROWN_5, GLOBAL.chromakey)
 	    		};
 		Enemy returnEnemy = new Enemy(iEnemy);
 		
 		return returnEnemy;
 	}
 	
 	public void update(Input input, int delta)
 	{
 		this.getPosition().x -=  this.getVelocity() * delta;
 		this.updateBoundingRect();	
 	}
 	
 	private void updateBoundingRect()
 	{
 		boundingShape.setX(this.position.x);
 		boundingShape.setY(this.position.y);
 	}
 	
 	public void handleCollision(Entity entity)
 	{
 		Random rand = new Random();
 		
 		this.colidable = false;
 		this.getAnimation().stop();
 		
 		if(rand.nextBoolean()){
 			this.getAnimationFrame().rotate(90);
 		}else{
 			this.getAnimationFrame().rotate(-90);
 		}
 		this.setVelocity(.1f);
 	}
 	
 	public void draw()
 	{
 		this.getAnimation().draw(this.getPosition().x, this.getPosition().y);
 	}
 }
