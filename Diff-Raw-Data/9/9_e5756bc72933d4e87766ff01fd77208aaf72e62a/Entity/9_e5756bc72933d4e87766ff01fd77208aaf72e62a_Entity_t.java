 package com.vgdc.merge.entities;
 
 import java.util.ArrayList;
 
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import com.badlogic.gdx.math.Vector2;
 import com.vgdc.merge.entities.abilities.Ability;
 import com.vgdc.merge.entities.audio.SoundComponent;
 import com.vgdc.merge.entities.controllers.Controller;
 import com.vgdc.merge.entities.physics.MovingBody;
 import com.vgdc.merge.entities.physics.PhysicsBody;
 import com.vgdc.merge.entities.rendering.Renderer;
 import com.vgdc.merge.world.World;
 
 public class Entity extends BaseEntity{
 	
 	private static final float HALF_JUMP_DIR = 0.2f;
 	
 	private final EntityData data;
 	private ArrayList<Ability> abilities = new ArrayList<Ability>();
 	private int health;
 	private Controller controller;
 	private int team;
 	private float halfJumpDir;
 	private boolean currentlyWalking,currentlyJumping;
 	private boolean isFacingLeft;
 	
 	public Entity(EntityData data, World world)
 	{
 		this(data, world, new Renderer(), new MovingBody(), new SoundComponent());
 	}
 	
 	public Entity(EntityData data, World world, Renderer renderer, PhysicsBody body, SoundComponent sound)
 	{
 		this.data = data;
 		this.team = data.defaultTeam;
 		setWorld(world);
 		if(data.defaultAbilities!=null)
 		{
 			if(!data.mergeable)
 				for(Ability a : data.defaultAbilities)
 					abilities.add(a);
 			else
 				for(Ability a : data.defaultAbilities)
 					abilities.add(null);
 		}
 		this.controller = data.controller.copy();
 		controller.setEntity(this);
 		this.health = data.maxHealth;
 		setRenderer(renderer);
 		setPhysicsBody(body);
 		setSoundComponent(sound);
 	}
 	
 	protected PhysicsBody createPhysicsBody(){
 		return new MovingBody();
 	}
 	
 	public Controller getController(){
 		return controller;
 	}
 	
 	public boolean isDead()
 	{
 		return health<=0;
 	}
 	
 	public EntityData getData()
 	{
 		return data;
 	}
 	
 	public boolean canMerge()
 	{
 		return data.mergeable;
 	}
 	
 	public boolean frictionIsApplied(){
 		return !currentlyWalking;
 	}
 	
 	public EntityType getEntityType(){
 		return EntityType.Entity;
 	}
 	
 	public void setFacingLeft(boolean ifr){isFacingLeft = ifr;}
 	
 	public boolean facingLeft(){return isFacingLeft;}
 	
 	public int getDamage()
 	{
 		return data.damage;
 	}
 	
 	public float getMoveSpeed(){
 		return data.moveSpeed;
 	}
 	
 	public int getTeam()
 	{
 		return team;
 	}
 	
 	public void setTeam(int team)
 	{
 		this.team = team;
 	}
 	
 	public void setRenderer(Renderer nRenderer)
 	{
 		super.setRenderer(nRenderer);
 		nRenderer.setAnimations(data.animations);
 		if(data.animations!=null)
 			nRenderer.setState(0);
 	}
 	
 	public void setSoundComponent(SoundComponent sound)
 	{
 		super.setSoundComponent(sound);
 		sound.setSounds(data.sounds);
 	}
 	
 	public void setPhysicsBody(PhysicsBody body)
 	{
 		super.setPhysicsBody(body);
 		if(data.dimensions!=null)
 			body.setSize(data.dimensions);
 	}
 	
 	@Override
 	public void onUpdate(float delta) {
 		currentlyWalking = false;
 		currentlyJumping = false;
 		controller.onUpdate(delta);
 		getPhysicsBody().onUpdate(delta);
 		if(halfJumpDir>0){
 			if(!currentlyJumping)
 				halfJumpDir = 0;
 			else
 				halfJumpDir-=delta;
 		}
 	}
 	
 	@Override
 	public void onRender(SpriteBatch batch, float delta){
 		getRenderer().onRender(batch, delta);
 	}
 	
 	public int getHealth(){
 		return health;
 	}
 	
 	public void damage(int amount)
 	{
 		health-=amount;
 	}
 	
 	public void heal(int amount)
 	{
 		health = Math.min(health+amount, data.maxHealth);
 	}
 	
 	public ArrayList<Ability> getAbilities()
 	{
 		return abilities;
 	}
 	
 	public void tryJump(float delta){
 		currentlyJumping = true;
 		if(getMovingBody().checkTouchingGround() && halfJumpDir <= 0)//Jumping up
 		{
 			//Vector2 velocity = getMovingBody().getVelocity();
 			//getMovingBody().setVelocity(new Vector2((getMovingBody().getPosition().x-getMovingBody().getLastPosition().x)/delta*60/*velocity.x*/, data.jumpHeight));
 			getMovingBody().setVelocity(new Vector2(getMovingBody().getVelocity().x,data.jumpHeight));
 			System.out.println("jump velocity x: "+getMovingBody().getVelocity().x);
 			halfJumpDir = HALF_JUMP_DIR;
 		}
 		else if(halfJumpDir>0)//Staying up
 		{
 			getMovingBody().setVelocity(new Vector2(getMovingBody().getVelocity().x, data.jumpHeight));
 		}
 	}
 	
 	public MovingBody getMovingBody()
 	{
 		return (MovingBody)getPhysicsBody();
 	}
 	
 	static final float JUMPCONTROL = .5f;
 	static final float WALKFORCE = .2f;
 	public void moveLeft(float delta)
 	{
 		currentlyWalking = true;
 		float walkspeed = -data.moveSpeed;
 		if(!getMovingBody().checkTouchingGround())
 			walkspeed *= JUMPCONTROL;
 		if(getMovingBody().getVelocity().x > walkspeed)
 			getMovingBody().setVelocity(new Vector2(walkspeed*WALKFORCE+getMovingBody().getVelocity().x*(1-WALKFORCE),getMovingBody().getVelocity().y));
 
		setFacingLeft(true);
 		getRenderer().flip(true);
 	}
 	
 	public void moveRight(float delta)
 	{
 		currentlyWalking = true;
 
 		float walkspeed = data.moveSpeed;
 		if(!getMovingBody().checkTouchingGround())
 			walkspeed *= JUMPCONTROL;
 		if(getMovingBody().getVelocity().x < walkspeed)
 			getMovingBody().setVelocity(new Vector2(walkspeed*WALKFORCE+getMovingBody().getVelocity().x*(1-WALKFORCE),getMovingBody().getVelocity().y));
 
		setFacingLeft(false);
 		getRenderer().flip(false);
 	}
 
 	public void onEntityCollision(Entity other){
 		controller.onEntityCollision(other);
 	}
 	public void onPlatformCollision(Platform other){
 		controller.onPlatformCollision(other);
 	}
 	
 	public void setState(int state){
 		getRenderer().setState(state);
 		getSoundComponent().playSound(state);
 	}
 	
 	public int getState(){
 		return getRenderer().getState();
 	}
 
 	public void delete() {
 		getWorld().getEntityManager().removeEntity(this);
 		health = 0;	
 	}
 
 }
