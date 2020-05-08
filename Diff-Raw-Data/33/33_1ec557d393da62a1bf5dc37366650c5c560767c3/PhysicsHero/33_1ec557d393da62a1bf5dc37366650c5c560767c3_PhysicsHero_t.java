 package com.detourgames.raw.game;
 
 import com.badlogic.gdx.math.Vector2;
 import com.badlogic.gdx.physics.box2d.BodyDef;
 import com.badlogic.gdx.physics.box2d.FixtureDef;
 import com.badlogic.gdx.physics.box2d.PolygonShape;
 import com.badlogic.gdx.physics.box2d.World;
 import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
 import com.detourgames.raw.PhysicsComponent;
 
 
 public class PhysicsHero extends PhysicsComponent{
 	
 	private Vector2 mVel = new Vector2(); // Current velocity.
 	private Vector2 mRunVel = new Vector2(); // Impulse velocity that needs to be applied to run at correct speed
 	private Vector2 mJumpVel = new Vector2(0, 10);
 	private Vector2 mDashVel = new Vector2(20, 0);
 	private Vector2 mTotalVel = new Vector2();
 	
 	private float velChange;
 	private float runImpulse;
 	
 	private static final float MAX_SPEED = 2f;
 	
 	public PhysicsHero(){
 		super();
 	}
 	
 	@Override
 	public void update() {
 		
 		run();
 		mBody.applyLinearImpulse(mTotalVel, mBody.getWorldCenter());
 		mTotalVel.set(Vector2.Zero);
 	}
 	
 	private void run(){
 		mBody.setLinearVelocity(mBody.getLinearVelocity().mul(0.9f)); // Slow down, in case we're above max speed.
 		mRunVel.set(Vector2.Zero);
 		mVel = mBody.getLinearVelocity();
 		velChange = MAX_SPEED - mVel.x;
 	    runImpulse = mBody.getMass() * velChange;
 	    if(runImpulse>0){
 	    	mRunVel.set(runImpulse, 0);
 	    }
 	    mTotalVel = mTotalVel.add(mRunVel);
 	}
 	
 	public void jump(){
 		mTotalVel = mTotalVel.add(mJumpVel);
 	}
 	
 	public void dash(){
 		mTotalVel = mTotalVel.add(mDashVel);
 	}
 	
 	public void swing(){
 		System.out.println("Swing!");
 		/*1 create chainsaw projectile
 		*2 create chain graphic
 		*3 resize chain graphic until chain hits
 		*4 when chainsaw hits terrain and sticks, create solid chain
 		*5 glue chain to RAW and chainsaw using joints
 		*
 		*OR
 		*
 		*1 create chainsaw projectile
 		*2 get distance between chainsaw and RAW and fill with appropriate number of chainlinks (small solid rectangles)
 		*3 glue chainlinks, RAW, and chainsaw together with joints
 		*3 repeat steps 2&3 (adding new links as needed) until chainsaw hits terrain and sticks
 		*/
 	}
 	
 	@Override
 	public void create(World world, float x, float y, float width, float height, boolean dynamic, int mainFixtureType) {
 
 		BodyDef bodyDef = new BodyDef();
 		if (dynamic) {
 			bodyDef.type = BodyType.DynamicBody;
 		} else {
 			bodyDef.type = BodyType.StaticBody;
 		}
 		bodyDef.position.set(x, y);// (x+(width/2f), y+(height/2f))
 		mBody = world.createBody(bodyDef);
 
 		PolygonShape dynamicBox = new PolygonShape();
 		dynamicBox.setAsBox(width / 2f, height / 2f);
 		FixtureDef fixtureDef = new FixtureDef();
 		fixtureDef.shape = dynamicBox;
 		fixtureDef.density = 1.0f;
 		fixtureDef.friction = 0.0f;
 		fixtureDef.filter.groupIndex = PhysicsComponent.GROUP_ALLY;
 		mBody.createFixture(fixtureDef).setUserData(mainFixtureType);
 		mBody.setGravityScale(1.4f);
 		//mBody.setLinearDamping(10.5f);
 		mBody.setFixedRotation(true);
 		mBody.setUserData(this);
 	}
	
	@Override
	public void create(World world, float x, float y, Vector2 vertices[], boolean dynamic, int mainFixtureType) {
		
		//create main fixture (bodies may have more than one)
		BodyDef bodyDef = new BodyDef();
		if (dynamic) {
			bodyDef.type = BodyType.DynamicBody;
		} else {
			bodyDef.type = BodyType.StaticBody;
		}
		bodyDef.position.set(x, y);
		mBody = world.createBody(bodyDef);

		PolygonShape dynamicBox = new PolygonShape();
		dynamicBox.set(vertices);
		FixtureDef fixtureDef = new FixtureDef();
		fixtureDef.shape = dynamicBox;
		fixtureDef.density = 1.0f;
		fixtureDef.friction = 0.0f;
		fixtureDef.filter.groupIndex = PhysicsComponent.GROUP_ALLY;
		mBody.createFixture(fixtureDef).setUserData(mainFixtureType);
		mBody.setGravityScale(1.4f);
		//mBody.setLinearDamping(10.5f);
		mBody.setFixedRotation(true);
		mBody.setUserData(this);
	}
 }
