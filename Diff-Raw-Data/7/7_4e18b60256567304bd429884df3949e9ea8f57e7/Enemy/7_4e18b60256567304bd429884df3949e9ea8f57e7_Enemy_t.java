 package cannon.shuffle;
 
 import com.badlogic.gdx.math.Vector2;
 import com.badlogic.gdx.physics.box2d.BodyDef;
 import com.badlogic.gdx.physics.box2d.World;
 import com.badlogic.gdx.utils.TimeUtils;
 
 public abstract class Enemy extends GameEntity {
 
 	public double hp = 5;
 	
 	public boolean destroyed = false;
 	public boolean first_collision_happened = false;
 
 	double theta=0; 	//just used for spinning enemy
 	double rotation_speed=Math.PI/50;
 
 	int direction=1;
 	
 	final static int MAX_CHARGING = 700;
 	double lastFiring = 0;
 	
 	enum EnemyState{
 		ARRIVING, COMBAT_ACTION_1, COMBAT_ACTION_2;
 	}
 	private EnemyState state;
 	
 	double arrivedAt = TimeUtils.millis();
 	
 	public Enemy(World world, Vector2 pos, float angle){
 		super(BodyDef.BodyType.DynamicBody, pos, angle, world);
 		state = EnemyState.ARRIVING;
 	}
 
 	public void remove() {
 		body.destroyFixture(body.getFixtureList().get(0));
 	}
 	
 	public void move(){
 		
 		if ( state == EnemyState.COMBAT_ACTION_1 ){
 			combat_action_1();
 		}
 		else if ( state == EnemyState.COMBAT_ACTION_2 ){
 			combat_action_2();
 		}
 		else{
			if ( TimeUtils.millis() - arrivedAt > 1000 || this.getPosition().y < CannonShuffle.cannon.getPosition().y + convertToBox(Constants.ENEMY_HEIGHT)*10){
				state = EnemyState.COMBAT_ACTION_1;
			}
 			float target_angle=(float)(Math.PI/2+Math.atan2((double)this.getPosition().y-CannonShuffle.cannon.getPosition().y,(double)this.getPosition().x-CannonShuffle.cannon.getPosition().x));
 			Vector2 velocity = new Vector2(Constants.ENEMY_SPEED*2*(float)-Math.sin(target_angle), Constants.ENEMY_SPEED*2*(float)Math.cos(target_angle));
 			body.setLinearVelocity(velocity);
 		}
 		
 	}
 
 	public abstract void combat_action_1();
 	public abstract void combat_action_2();
 	public abstract void fire();
 	
 	public void update(){
 		super.update();
 		move();
 	}
 
 }
