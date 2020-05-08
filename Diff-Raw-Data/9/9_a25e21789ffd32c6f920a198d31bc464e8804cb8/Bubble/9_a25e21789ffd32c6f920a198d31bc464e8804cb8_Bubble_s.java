 package com.mel.wallpaper.starWars.entity;
 
 import org.andengine.entity.modifier.IEntityModifier;
 import org.andengine.entity.modifier.MoveModifier;
 import org.andengine.entity.modifier.PathModifier;
 import org.andengine.entity.sprite.Sprite;
 import org.andengine.util.debug.Debug;
 import org.andengine.util.modifier.IModifier;
 
 import com.mel.entityframework.IEntity;
 import com.mel.entityframework.IMovable;
 import com.mel.util.Point;
 import com.mel.wallpaper.starWars.sound.SoundLibrary;
 import com.mel.wallpaper.starWars.sound.SoundLibrary.Sample;
 import com.mel.wallpaper.starWars.view.Position;
 import com.mel.wallpaper.starWars.view.SpriteFactory;
 import com.mel.wallpaper.starWars.view.WalkerAnimator;
 
 public class Bubble implements IEntity, IMovable
 {
 	public enum BubbleType {
 		BUBBLE_OLA_K_ASE("bubble1",Sample.LASER),
 		BUBBLE_TU_PADRE("bubble1",Sample.CHEWAKA);
 		
 		String spriteName;
 		Sample sound;
 		
 		private BubbleType(String spriteName, Sample sound)
 		{
 			this.spriteName = spriteName;
 			this.sound = sound;
 		}
 		
 		public Sprite getNewSprite()
 		{
 			Sprite sprite = SpriteFactory.getMe().newSprite(spriteName,
 					   							   BUBBLE_SIZE*SpriteFactory.PLAYERS_SPRITE_SCALEFACTOR,
 					   							   BUBBLE_SIZE*SpriteFactory.PLAYERS_SPRITE_SCALEFACTOR);
 			
 			sprite.setRotationCenter(BUBBLE_SIZE*SpriteFactory.PLAYERS_SPRITE_SCALEFACTOR/2,
 									 BUBBLE_SIZE*SpriteFactory.PLAYERS_SPRITE_SCALEFACTOR/2);
 			
 			return sprite;
 		}
 		
 		public String toString() {
 			return spriteName;
 		}
 	}
 	
 	BubbleType type;
 	
 	public static final float DEFAULT_SPEED = 10f;
 	public float speed;
 	
 	public static final float BUBBLE_SIZE = 75f;
 	
 	public Position position;
 	public Sprite sprite;
 	
 	public Point origin;
 	public Point destination;
 	
 	public boolean isFinished = false;
 	
 	/* Constructor */
 	public Bubble(BubbleType type, float x, float y){
 		this(type, new Position(x,y));
 	}
 	public Bubble(BubbleType type, Position p){
 		this.type = type;
 		this.position = (Position) p.clone();
 		this.speed = Bubble.DEFAULT_SPEED;
 		this.sprite = type.getNewSprite();
 	}
 	
 	public void playSound()
 	{
 		SoundLibrary.playSample(type.sound);
 	}
 	
 	public Point getSpriteOffset(){
 		Point spriteCenter = new Point(getSpriteOffsetX(), getSpriteOffsetY());
 		return spriteCenter;
 	}
 	
 	public float getSpriteOffsetX(){
 		return this.sprite.getWidth()/2;
 	}
 	public float getSpriteOffsetY(){
		return (WalkerAnimator.SPRITE_HEIGHT);
 	}
 	
 	public float getTotalDistance(){
 		return this.origin.distance(this.destination);
 	}
 
 	public float getTraveledDistance(){
 		return this.position.distance(this.origin);
 	}
 	
 	public boolean isBusy(){
 		return false;
 	}
 	
 	public boolean isMoving(){
 		return this.destination != null;
 	}
 
 	public void recycle(){
 		this.speed = Bubble.DEFAULT_SPEED;
 
 		this.sprite.clearEntityModifiers();
 		this.sprite.clearUpdateHandlers();
 		
 		this.position.setLocation(0, 0);
 		this.position.clearEntityModifiers();
 		this.position.clearUpdateHandlers();
 
 		this.sprite.detachSelf();
 	}
 	
 	/*
 	 * IEntity methods
 	 */
 	
 	public Sprite getSprite() {
 		return sprite;
 	}
 	
 	/* 
 	 * IMovable methods
 	 */
 	
 	public Position getPosition() {
 		return this.position;
 	}
 	
 	public float getSpeed() {
 		return this.speed;
 	}
 	
 	public void animateMoveAndStartCooldowns(Point destination) {
 		this.origin = position.toPoint();
 		this.destination = destination;
 	}
 	
 	public void movementEnd(){
 		this.destination = null;
 	}
 	
 	public void animateStopAndStartCooldowns(){
 		//testing code
 		if(this.position.getEntityModifierCount() > 1){
 			Debug.d("ball","ALERT: paramos animacion jugador y tenemos MODIFIERS acumulados: "+this.position.getEntityModifierCount());
 		}// testing code
 		
 		this.destination = null;
 		//animateStop(); //no aplica
 	}
 	
 	public void forceStopMovement(){
 		removeOldMovementOrders();
 	}
 	
 	public void removeOldMovementOrders(){
 		this.destination = null;
 		this.position.unregisterEntityModifiers(new IEntityModifier.IEntityModifierMatcher()
 		{
 			public boolean matches(IModifier<org.andengine.entity.IEntity> pObject) {
 				boolean matches = pObject.getClass().equals(PathModifier.class) || pObject.getClass().equals(MoveModifier.class);
 				return matches;
 			}
 		});
 	}
 }
