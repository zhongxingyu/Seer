 package gamedev.objects;
 
 import gamedev.game.Direction;
 import gamedev.game.ResourcesManager;
 
 import java.util.Random;
 
 import org.andengine.engine.handler.physics.PhysicsHandler;
 import org.andengine.entity.primitive.Rectangle;
 import org.andengine.entity.sprite.AnimatedSprite;
 import org.andengine.extension.physics.box2d.PhysicsConnector;
 import org.andengine.extension.physics.box2d.PhysicsFactory;
 import org.andengine.extension.physics.box2d.util.Vector2Pool;
 
 import com.badlogic.gdx.math.Vector2;
 import com.badlogic.gdx.physics.box2d.Body;
 import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
 
 public class Dinosaur extends AnimatedSprite {
 	
 	private static int nDinosaurs = 0;
 	public final static long[] ANIMATION_DURATION = { 120, 120, 120, 120, 120, 120, 120, 120, 120, 120, 120, 120, 120};
 	public final static int FRAMES_PER_ANIMATION = 13;
 	public final static int TILES_PER_LINE = 26;
 	
 	public Body body;
 	public PhysicsHandler physicsHandler;
 	
 	protected int id;
 	protected ResourcesManager resourcesManager;
 	protected DinosaurState currentState;
 	protected float animationElapsedTime = 0;
 	protected float animationTime = 10;
 	protected float attackElapsedTime = 0;
 	protected float attackBlockTime = 2;
 	protected boolean firstAttack = false;
 	
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
 		DEAD,
 	}
 	
 	public Dinosaur(float pX, float pY) {
 		super(pX, pY, ResourcesManager.getInstance().dinosaurGreenRegion, ResourcesManager.getInstance().vbom);
 		this.resourcesManager = ResourcesManager.getInstance();
 		this.createPhysic();
 		this.direction = Direction.getRandomDirection();
 		this.setState(DinosaurState.LOOKING);
 		// Scale it up, so it has normal size.
 		this.mScaleX = this.mScaleX * 2;
 		this.mScaleY = this.mScaleY * 2;
 		this.id = nDinosaurs++;
 		this.attachChild(new Rectangle(this.body.getPosition().x, this.body.getPosition().y, 10, 10, this.resourcesManager.vbom));
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
 			rowIndex = 28;
 			break;
 		case TIPPING_OVER:
 			rowIndex = 24;
 			this.body.setLinearVelocity(0, 0);
 			animate = false;
 			break;
 		case RUNNING:
 		case CHASE_PLAYER:
 			rowIndex = 20;
 			break;
 		case ROARING:
 			rowIndex = 16;
 			this.body.setLinearVelocity(0, 0);
 			break;
 		case PAUSED:
 			rowIndex = 12;
 			this.body.setLinearVelocity(0, 0);
 			break;
 		case LOOKING:
 			rowIndex = 8;
 			this.body.setLinearVelocity(0, 0);
 			break;
 		case BEEN_HIT:
 			rowIndex = 4;
 			this.body.setLinearVelocity(0, 0);
 			animate = false;
 			break;
 		case ATTACK:
 			rowIndex = 0;
 			this.body.setLinearVelocity(0, 0);
 			break;
 		default:
 			break;
 		}
 		int startTile = rowIndex*TILES_PER_LINE + this.direction*FRAMES_PER_ANIMATION;
 		this.animate(ANIMATION_DURATION, startTile, startTile+FRAMES_PER_ANIMATION-1, animate);			
 
 	}
 	
 	/**
 	 * Set the position where the dinosaur should move
 	 * @param x
 	 * @param y
 	 * @param state WALKING|RUNNING|CHASE_PLAYER
 	 */
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
 		
 		if (this.currentState == DinosaurState.CHASE_PLAYER && state == DinosaurState.CHASE_PLAYER) {
 			if (this.direction == direction) {
 				// No need to update the animation, still same State + same Direction
 				return;
 			}
 		} 
 		this.setDirection(direction);
 		this.setState(state);			
 	}
 	
 	
 	public void underAttack(int damage) {
 		this.life -= damage;
 		if (this.life <= 0) {
 			this.setState(DinosaurState.TIPPING_OVER);
 			this.resourcesManager.player.getAttackers().remove(this);
 			this.currentState = DinosaurState.DEAD;
 			this.detachSelf();
 			this.dispose();
 		} else {
 			this.setState(DinosaurState.BEEN_HIT);			
 		}
 	}
 	
 	public String toString() {
 		return "[Dinosaur "+this.id+", currentState="+this.currentState+", pos="+this.body.getPosition()+"]";
 	}
 	
 	@Override
     protected void onManagedUpdate(float pSecondsElapsed) {
 		super.onManagedUpdate(pSecondsElapsed);
         
 		if (this.currentState == DinosaurState.DEAD) return;
 		
 		Vector2 bodyPos = this.body.getPosition();
//		if (bodyPos.x < 0 || bodyPos.y < 0) {
//			// Temporary fix! Need boundaries in level...
//			this.body.setTransform(Math.max(bodyPos.x, 0), Math.max(bodyPos.y,0), this.body.getAngle());
//			this.setState(DinosaurState.LOOKING);
//			return;
//		}
 
 		// Check if the dino should chase our player
         Vector2 playerPos = this.resourcesManager.player.body.getPosition();
         // TODO Calculation of distance does not always work and then the player is under attack always... why!
         float distance = Math.abs(bodyPos.dst(playerPos)); 
         if (distance < 0.5) {
         	if (this.currentState != DinosaurState.ATTACK) this.setState(DinosaurState.ATTACK);
         	// TODO Damage should be based on distance...
         	if (!this.firstAttack) {
         		this.resourcesManager.player.underAttack(10, this);
         		this.firstAttack = true;
         	} else {
             	this.attackElapsedTime += pSecondsElapsed;
         		if (this.attackElapsedTime > this.attackBlockTime) {
         			this.resourcesManager.player.underAttack(10, this);
         			this.attackElapsedTime = 0;
             	}        		
         	}
         	return;
         } else if (distance < this.radius) {
         	this.moveTo(playerPos.x, playerPos.y, DinosaurState.CHASE_PLAYER);
         	this.resourcesManager.player.removeAttacker(this);
         	return;
         } else {
         	if (this.currentState == DinosaurState.CHASE_PLAYER || this.currentState == DinosaurState.ATTACK) {
         		this.resourcesManager.player.removeAttacker(this);
         		// Force calculation  of new state
         		this.animationTime = 0;
         	}
         }
         
         // If walking or running, check if we reached our goal
         if (this.currentState == DinosaurState.WALKING || this.currentState == DinosaurState.RUNNING) {
        	if (Math.abs(this.body.getPosition().dst(this.moveTo)) < 1) {
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
