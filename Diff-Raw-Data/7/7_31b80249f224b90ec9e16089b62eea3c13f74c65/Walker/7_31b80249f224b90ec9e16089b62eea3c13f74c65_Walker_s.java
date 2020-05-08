 package com.mel.wallpaper.starWars.entity;
 
 
 import java.util.ArrayList;
 
 import org.andengine.engine.handler.timer.ITimerCallback;
 import org.andengine.engine.handler.timer.TimerHandler;
 import org.andengine.entity.modifier.IEntityModifier;
 import org.andengine.entity.modifier.MoveModifier;
 import org.andengine.entity.modifier.PathModifier;
 import org.andengine.entity.modifier.PathModifier.Path;
 import org.andengine.entity.sprite.AnimatedSprite;
 import org.andengine.entity.sprite.Sprite;
 import org.andengine.util.debug.Debug;
 import org.andengine.util.modifier.IModifier;
 
 import com.mel.entityframework.IEntity;
 import com.mel.entityframework.IMovable;
 import com.mel.util.Point;
 import com.mel.wallpaper.starWars.entity.commands.Command;
 import com.mel.wallpaper.starWars.settings.GameSettings;
 import com.mel.wallpaper.starWars.timer.TimerHelper;
 import com.mel.wallpaper.starWars.view.AnimationFactory;
 import com.mel.wallpaper.starWars.view.IWalkerAnimator;
 import com.mel.wallpaper.starWars.view.Animation;
 import com.mel.wallpaper.starWars.view.Position;
 import com.mel.wallpaper.starWars.view.SpriteFactory;
 
 
 public class Walker implements IEntity, IMovable
 {
 	
 	
 	public static final float DEFAULT_SPEED = 50;
 	
 	public AnimatedSprite sprite;
 	public Position position;
 
 	public Point destination;
 	private float speed;
 	public Rol rol;
 	public String textureId;
 	
 	protected IWalkerAnimator animator;
 	
 	
 	protected boolean isOnRunningCooldown = false;
 	protected boolean isOnAplastadoCooldown = false;
 	protected boolean isOnWaitingCooldown = false;
 	
 	private static final float APLASTADO_DURATION = 3f;
 	
 	public ArrayList<Command> pendingCommands = new ArrayList<Command>();
 	
 	public enum Rol {
         JEDI("JEDI"),
         CHUWAKA("CHUWAKA"),
         STORM_TROOPER("STORM_TROOPER");
         
         private final String rolId;
 
         Rol(String valor) {
             this.rolId = valor;
         }
 
         public String toString() {
             return this.rolId;
         }
     }	
 	
 	/* Getters/Setters */
 	public Sprite getSprite() {
 		return sprite;
 	}
 	
 	public Position getPosition(){
 		return this.position;
 	}
 	
 	
 	public float getSpeed(){
 		return this.speed;
 	}
 	
 	public void setSpeed(float s){
 		this.speed = s;
 		if(this.animator != null){
 			this.animator.setSpeed(s);
 		}
 	}
 	
 	
 	protected void setRunningEnd(){
 		this.isOnRunningCooldown = false;
 	}
 
 	protected void setWaitEnd(){
 		this.isOnWaitingCooldown = false;
 	}
 	
 	protected void setAplastadoEnd(){
 		this.isOnAplastadoCooldown = false;
 	}
 	
 	public boolean isPerformingAnimation(){
 		return isOnRunningCooldown || isOnAplastadoCooldown;
 	}
 	
 	public boolean canWalk(){
 		return !hasDestination() && !isWaiting() && !isPerformingAnimation();
 	}
 	
 	public boolean isIdle(){
 		return !hasDestination() && !isWaiting() && !isPerformingAnimation();
 	}
 	
 //	public boolean canWait(){
 //		return !hasDestination() && !isWaiting() && !isBusy();
 //	}
 	
 	public boolean isAplastado(){
 		return this.isOnAplastadoCooldown;
 	}
 	
 	public void addCommand(Command c){
 		this.pendingCommands.add(c);
 	}
 	
 	public void clearPendingCommands(){
 		this.pendingCommands.clear();
 	}
 	
 	public float getSpriteOffsetX(){
 		return this.animator.getSpriteOffsetX();
 	}
 	
 	public float getSpriteOffsetY(){
 		return this.animator.getSpriteOffsetY();
 	}
 	
 	/*
 	public Point getSpriteOffset(){
 		Point spriteCenter = new Point(getSpriteOffsetX(), getSpriteOffsetY());
 		return spriteCenter;
 	}
 	*/
 	
 	public boolean hasDestination()
 	{
 		return (destination!=null);
 	}
 	
 	public boolean isWaiting(){
 		return isOnWaitingCooldown;
 	}
 	
 	
 	/* Constructor */
 	public Walker(float x, float y, float speed, String textureId, Animation initialAnimation, Rol r) {
 		this(new Point(x,y), speed, textureId, initialAnimation, r);
 	}
 
 	public Walker(float x, float y, String textureId, Animation initialAnimation, Rol r){
 		this(new Point(x,y), DEFAULT_SPEED, textureId, initialAnimation, r);
 	}
 	
 	public Walker(Point p, String textureId, Animation initialAnimation, Rol r){
 		this(p, DEFAULT_SPEED, textureId, initialAnimation, r);
 	}
 	public Walker(Point p, float speed, String textureId, Animation initialAnimation, Rol r){
 		this.textureId = textureId;
 		this.position = new Position(p);
 		this.speed = speed;
 		this.rol = r;
 		
 		this.animator = (IWalkerAnimator) AnimationFactory.newAnimation(textureId, this.speed);
 		this.sprite = animator.getSprite();
 	}
 	
 	
 	public Point calcInitialPosition(Point pos){
 		Point ini = pos.clone();
 		ini.setX((ini.getX()+InvisibleWalls.leftWall)/2f); //a mitad de recorrido
 		ini.setX(ini.getX()+50f);
 		
 		//que no este en campo contrario, y que no este dentro de circulo de saque
 		while(ini.getX()>-20 || ini.distance(new Point(0,0))<110){
 			ini.setX(ini.getX()-10);
 		}
 		
 		return ini;
 	}
 	
 	public Point getRotationCenter(){
 		return new Point(25,30);
 	}
 	
 	
 	/* METHODS */
 	public void animateMoveAndStartCooldowns(Point destination){
 		//Debug.d("player.goTo(): "+(int)destination.getX()+","+(int)destination.getY());
 		this.destination = destination;
 		
 		this.animator.animateWalk(position, destination);
 		
 		float cooldown = 0.01f * position.distance(destination);
 		
 		this.isOnRunningCooldown = true;
 		TimerHelper.startTimer(this.position, Math.min(cooldown, 0.5f),  new ITimerCallback() {                      
             
             public void onTimePassed(final TimerHandler pTimerHandler)
             {
             	setRunningEnd();
             }
         });
 	}
 	
 	public void movementEnd(){
 		this.destination = null;
 		this.animator.animateStop();
 	}
 	
 	
 	
 	public void animateWaitAndStartCooldowns(float seconds){
 
 		this.animator.animateStop();
 		
 		this.isOnWaitingCooldown = true;
 		TimerHelper.startTimer(this.position, seconds,  new ITimerCallback() {                      
             
             public void onTimePassed(final TimerHandler pTimerHandler)
             {
             	setWaitEnd();
             }
         });
 	}
 	
 	public void forceStopMovement(){
 		removeOldMovementOrders();
		this.animator.animateInitialAnimation();
 	}
 	
 	public void animateStopAndStartCooldowns(){
 
 		//sanity check code
 		if(this.position.getEntityModifierCount() > 1){
 			Debug.d("ball","ALERT: paramos animacion jugador y tenemos MODIFIERS acumulados: "+this.position.getEntityModifierCount());
 		}//sanity check code
 		
 		this.destination = null;
 		
 		this.animator.animateStop();
 	}
 	
 	
 	public void animateAplastarAndStartCooldowns(){
 		if(!GameSettings.getInstance().godsFingerEnabled){
 			return;
 		}
 		
 		if(this.isOnAplastadoCooldown){
 			return;
 		}
 		
 		//Debug.d("aplastando jugador!");
 		this.isOnAplastadoCooldown = true;
 		
 		forceStopMovement();
 		
 		animator.animateAplastado();
 		
 		TimerHelper.startTimer(this.position, APLASTADO_DURATION,  new ITimerCallback() 
 		{                      
             public void onTimePassed(final TimerHandler pTimerHandler) {
             	animator.animateInitialAnimation();
             	
             	TimerHelper.startTimer(position, 1f,  new ITimerCallback() {                      
                     
                     public void onTimePassed(final TimerHandler pTimerHandler){
                     	setAplastadoEnd();
                     }
                 });
             }
         });
 		
 		TimerHelper.startTimer(this.position, APLASTADO_DURATION-0.5f,  new ITimerCallback()
 		{                      
             public void onTimePassed(final TimerHandler pTimerHandler) {
             	int offset = 3;
             	final Path path = new Path(10).to(position.getX()+offset, position.getY())
             								.to(position.getX()-offset, position.getY())
             								.to(position.getX()+offset, position.getY())
             								.to(position.getX()-offset, position.getY())
             								.to(position.getX()+offset, position.getY())
             								.to(position.getX()-offset, position.getY())
             								.to(position.getX()+offset, position.getY())
             								.to(position.getX()-offset, position.getY())
             								.to(position.getX()+offset, position.getY())
             								.to(position.getX(), position.getY());
             	
             	PathModifier moveModifier = new PathModifier(0.5f, path);
         		position.registerEntityModifier(moveModifier);
             }
         });
 	}
 	
 	
 	
 	/* HELPERS */
 	public void recycle(){
 		//this.textureId = null; //vamos a reusar esto no?
 		//this.animator = null; //vamos a reusar esto no?
 
 		this.position.detachSelf();
 		this.position.unregisterEntityModifiers(null);
 		this.position.unregisterUpdateHandlers(null);
 		this.position.setPosition(0, 0);
 		
 		this.sprite.detachSelf();
 		this.sprite.unregisterEntityModifiers(null);
 		this.sprite.unregisterUpdateHandlers(null);
 		this.sprite.setPosition(0,0);
 		
 		this.rol = null;
 		this.destination = null;
 		this.setSpeed(DEFAULT_SPEED);
 		
 		this.isOnRunningCooldown = false;
 		this.isOnAplastadoCooldown = false;
 		
 	}
 	
 	//this method is overwritten by many of Walker sub-classes (Shooter, Jedi)
 	public void removeOldMovementOrders(){
 		this.destination = null;
 		this.isOnRunningCooldown = false;
 		
 		this.position.unregisterEntityModifiers(new IEntityModifier.IEntityModifierMatcher()
 		{
 			
 			public boolean matches(IModifier<org.andengine.entity.IEntity> pObject) {
 				boolean matches = pObject.getClass().equals(PathModifier.class) || pObject.getClass().equals(MoveModifier.class);
 				return matches;
 			}
 		});
 	}
 
 	
 }
