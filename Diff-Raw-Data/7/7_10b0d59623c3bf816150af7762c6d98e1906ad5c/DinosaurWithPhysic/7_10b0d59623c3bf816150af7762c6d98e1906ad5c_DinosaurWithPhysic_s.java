 package gamedev.objects;
 
 import gamedev.game.Direction;
 import gamedev.game.ResourcesManager;
 
 import java.util.Random;
 
 import org.andengine.engine.handler.physics.PhysicsHandler;
 import org.andengine.entity.sprite.AnimatedSprite;
 import org.andengine.extension.physics.box2d.PhysicsConnector;
 import org.andengine.extension.physics.box2d.PhysicsFactory;
 import org.andengine.extension.physics.box2d.util.Vector2Pool;
 
 import com.badlogic.gdx.math.Vector2;
 import com.badlogic.gdx.physics.box2d.Body;
 import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
 
 public class DinosaurWithPhysic extends AnimatedSprite {
 	
 	public final static long[] ANIMATION_DURATION = { 120, 120, 120, 120, 120, 120, 120, 120, 120, 120, 120, 120, 120};
 	public final static int FRAMES_PER_ANIMATION = 13;
 	public final static int TILES_PER_LINE = 26;
 	
 	public Body body;
 	public PhysicsHandler physicsHandler;
 
 	protected ResourcesManager resourcesManager;
 	protected DinosaurState currentState;
 	protected float animationElapsedTime = 0;
 	protected float animationTime = 10;
 	
 	protected int direction = Direction.WEST;
 	protected int life = 100;
 	protected float velocity = 2f;
 	protected float factorRunning = 2f;
 	
 	// Current vector to move to
 	protected Vector2 moveTo;
 	protected float radius = 5;
 	
 	public enum DinosaurState {
 		WALKING,
 		RUNNING,
 		BEEN_HIT,
 		TIPPING_OVER,
 		ATTACK,
 		ROARING,
 		PAUSED,
 		LOOKING,
 		CHASE_PLAYER,
 	}
 	
 	public DinosaurWithPhysic(float pX, float pY) {
 		super(pX, pY, ResourcesManager.getInstance().dinosaurGreenRegion, ResourcesManager.getInstance().vbom);
 		this.resourcesManager = ResourcesManager.getInstance();
 		this.direction = Direction.getRandomDirection();
 		this.setState(DinosaurState.LOOKING);
		this.createPhysic();
 		// Scale it up, so it has normal size.
 		this.mScaleX = this.mScaleX * 2;
 		this.mScaleY = this.mScaleY * 2;
 
 	}
 
 	public void setDirection(int direction) {
 		this.direction = direction;
 	}
 	
 	public void setState(DinosaurState state) {
 		this.currentState = state;
 		// Display the correct animation based on the state and direction
 		int rowIndex = 0;
 		boolean animate = true;
 		
 		switch (state) {
 		case WALKING:
 			rowIndex = 0;
 			break;
 		case TIPPING_OVER:
 			rowIndex = 4;
 			this.body.setLinearVelocity(0, 0);
 			animate = false;
 			break;
 		case RUNNING:
 		case CHASE_PLAYER:
 			rowIndex = 8;
 			break;
 		case ROARING:
 			rowIndex = 12;
 			this.body.setLinearVelocity(0, 0);
 			break;
 		case PAUSED:
 			rowIndex = 16;
 			this.body.setLinearVelocity(0, 0);
 			break;
 		case LOOKING:
 			rowIndex = 20;
 			this.body.setLinearVelocity(0, 0);
 			break;
 		case BEEN_HIT:
 			rowIndex = 24;
 			this.body.setLinearVelocity(0, 0);
 			animate = false;
 			break;
 		case ATTACK:
 			rowIndex = 28;
 			this.body.setLinearVelocity(0, 0);
 			break;
 		}
 		int startTile = rowIndex*TILES_PER_LINE + this.direction*FRAMES_PER_ANIMATION;
 		this.animate(ANIMATION_DURATION, startTile, startTile+12, animate);			
 
 	}
 	
 	
 	public void moveTo(float x, float y, DinosaurState state) {
 		// Store the point where to go
 		this.moveTo = new Vector2(x, y);
 		// Calculate the direction for the sprite animation
 		int direction = Direction.getDirectionFromVectors(this.body.getPosition(), this.moveTo);
 		// Calculate the slope between source/destination
 		Vector2 v = Vector2Pool.obtain(x - this.body.getPosition().x, y - this.body.getPosition().y);
 		v.nor();
 		if (state == DinosaurState.WALKING) {
 			this.body.setLinearVelocity(v.x * this.velocity, v.y * this.velocity);			
 		} else {
 			this.body.setLinearVelocity(v.x * this.velocity * this.factorRunning, v.y * this.velocity * this.factorRunning);						
 		}
 		Vector2Pool.recycle(v);
 		if (state == DinosaurState.CHASE_PLAYER && direction == this.direction) {
 			
 		} else {
 			this.setDirection(direction);
 			this.setState(state);			
 		}
 	}
 	
 	@Override
     protected void onManagedUpdate(float pSecondsElapsed) {
             super.onManagedUpdate(pSecondsElapsed);
             
             // Check if the dino should chase our player
             Vector2 playerPos = this.resourcesManager.player.body.getPosition();
             float distance = this.body.getPosition().dst(playerPos); 
             if (distance < 0.5) {
            	this.body.setLinearVelocity(0, 0);
            	this.setState(DinosaurState.ATTACK);
             	return;
             } else if (distance < this.radius) {
             	this.moveTo(playerPos.x, playerPos.y, DinosaurState.CHASE_PLAYER);
             	return;
             } else {
             	if (this.currentState == DinosaurState.CHASE_PLAYER) {
             		// Force calculation  of new state
             		this.animationTime = 0;
             	}
             }
             
             // If walking or running, check if we reached our goal
             if (this.currentState == DinosaurState.WALKING || this.currentState == DinosaurState.RUNNING) {
             	if (Math.abs(this.body.getPosition().dst(this.moveTo)) < 5) {
             		// Stop dino and force to calculate a new state
             		this.body.setLinearVelocity(0, 0);
             		this.animationTime = 0;
             	}
             }
             
             // Set a random state after a random time. If the state is walking or running, set a random position where the dino walks.
             this.animationElapsedTime += pSecondsElapsed;
             if (this.animationElapsedTime > this.animationTime) {
             	this.animationElapsedTime = 0;
             	Random r = new Random();
             	// Set a random animation time [10...20] for the next animation seconds
             	this.animationTime = 10 + (r.nextFloat() * 10 + 1);
             	// Pick a random state, exclude some states
             	DinosaurState randomState = this.getRandomState();
             	// If the state is walking, calculate a new random position
             	if (randomState == DinosaurState.WALKING || randomState == DinosaurState.RUNNING) {
             		// The new Position should be in Range [-1000...1000] from the current position
             		float rX = this.body.getPosition().x + (-1000 + (r.nextFloat() * 2000 + 1));
             		float rY = this.body.getPosition().y + (-1000 + (r.nextFloat() * 2000 + 1));            		            		
             		this.moveTo(rX, rY, randomState);
             	} else {
                 	this.setState(randomState);            		
             	}
             }
     }
 	
 	protected void createPhysic() {
 		this.body = PhysicsFactory.createBoxBody(this.resourcesManager.physicsWorld, this, BodyType.KinematicBody, PhysicsFactory.createFixtureDef(0, 0, 0));
 //		this.physicsHandler = new PhysicsHandler(this);
 //		this.registerUpdateHandler(this.physicsHandler);
 		this.resourcesManager.physicsWorld.registerPhysicsConnector(new PhysicsConnector(this, this.body, true, true));		
 	}
 	
 	private DinosaurState getRandomState() {
     	Random r = new Random();
 		DinosaurState randomState = DinosaurState.values()[r.nextInt(7)];
     	while (randomState == DinosaurState.ATTACK || randomState == DinosaurState.BEEN_HIT || randomState == DinosaurState.TIPPING_OVER || randomState == DinosaurState.CHASE_PLAYER) {
     		randomState = DinosaurState.values()[r.nextInt(7)];
     	}
 		return randomState;
 	}
 	
 	
 }
