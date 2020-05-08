 package com.mel.wallpaper.starWars.entity;
 
 import org.andengine.engine.handler.timer.ITimerCallback;
 import org.andengine.engine.handler.timer.TimerHandler;
 import org.andengine.entity.modifier.IEntityModifier;
 import org.andengine.entity.modifier.MoveModifier;
 import org.andengine.entity.modifier.PathModifier;
 import org.andengine.entity.sprite.Sprite;
 import org.andengine.util.debug.Debug;
 import org.andengine.util.modifier.IModifier;
 
 import com.mel.entityframework.IEntity;
 import com.mel.entityframework.IMovable;
 import com.mel.util.MathUtil;
 import com.mel.util.Point;
 import com.mel.wallpaper.starWars.timer.TimerHelper;
 import com.mel.wallpaper.starWars.view.Position;
 import com.mel.wallpaper.starWars.view.ShooterAnimator;
 import com.mel.wallpaper.starWars.view.SpriteFactory;
 
 public class LaserBeam implements IEntity, IMovable
 {
 	public static final float	HIT_DISTANCE			= 20f;
 	public static final float	JUMP_DISTANCE			= 60f;
 	public static final float	MAX_REACH_MATE_DISTANCE	= 220f;
 	public static final float	MAX_REACH_DISTANCE		= 350f;
 	public static final float	MIN_REACH_DISTANCE		= 70f;
 	
 	public static final float	DEFAULT_SPEED			= 500f;
 	
 	public static final float	BEAM_SIZE				= 50f * SpriteFactory.PLAYERS_SPRITE_SCALEFACTOR;
 	
 	public Position				position;
 	public float				speed;
 	public Sprite				sprite;
 	
 	public Point				origin;
 	public Point				destination;
 	
 	public boolean				isOnExlplosionCooldown	= false;
 	public boolean				hasExploded				= false;
 	
 	public JediKnight			lastParringJedi = null;
 	
 	public int jumps;
 	
 	/* Constructor */
 	public LaserBeam(float x, float y, int jumps) {
 		this(new Position(x, y), jumps);
 	}
 	
 	public LaserBeam(Position p, int jumps) {
 		this.position = (Position) p.clone();
 		this.speed = LaserBeam.DEFAULT_SPEED;
 		this.sprite = (Sprite) SpriteFactory.getMe().newSprite(SpriteFactory.LASER, BEAM_SIZE, BEAM_SIZE);
 		this.sprite.setRotationCenter(BEAM_SIZE * 0.5f, BEAM_SIZE * 0.5f);
 		this.jumps = jumps;
 	}
 	
 	/* Getters/Setters */
 	public Position getPosition() {
 		return this.position;
 	}
 	
 	public float getSpeed() {
 		return this.speed;
 	}
 	
 	public Point getSpriteOffset() {
 		Point spriteCenter = new Point(getSpriteOffsetX(), getSpriteOffsetY());
 		return spriteCenter;
 	}
 	
 	public float getSpriteOffsetX() {
 		return BEAM_SIZE / 2;
 	}
 	
 	public float getSpriteOffsetY() {
 		return (ShooterAnimator.SPRITE_HEIGHT * 0.45f);
 	}
 	
 	public float getTotalDistance() {
 		return this.origin.distance(this.destination);
 	}
 	
 	public float getTraveledDistance() {
 		return this.position.distance(this.origin);
 	}
 	
 	public boolean isBusy() {
 		return false;
 	}
 	
 	public boolean isMoving() {
 		return this.destination != null;
 	}
 	
 	/* Methods */
 	public void animateMoveAndStartCooldowns(Point destination) {
 		this.origin = position.toPoint();
 		this.destination = destination;
 		
 		// this.sprite.setRotation(MathUtil.PI_HALF);
 		this.sprite.setRotation(-1 * MathUtil.RAD_TO_DEG * MathUtil.getAngulo(this.origin, this.destination));
 		
 	}
 	
 	public void movementEnd(){
 		this.destination = null;
 		//sin forceStopMovement!! pq no queremos quitar todos los MovementModifiers que pueda tener el personaje.
 	}
 	
 	
 	public void animateStopAndStartCooldowns() {
 		// testing code
 		if (this.position.getEntityModifierCount() > 1) {
 			Debug.d("ball", "ALERT: paramos animacion jugador y tenemos MODIFIERS acumulados: " + this.position.getEntityModifierCount());
 		}// testing code
 		
 		this.destination = null;
 		// animateStop(); //no aplica
 	}
 	
 	public void forceStopMovement() {
 		removeOldMovementOrders();
 	}
 	
 	public void explode() {
 		// testing code
 		if (this.position.getEntityModifierCount() > 1) {
 			Debug.d("laser", "ALERT: paramos animacion jugador y tenemos MODIFIERS acumulados: " + this.position.getEntityModifierCount());
 		}// testing code
 		
 		this.destination = null;
 		animateExplosion();
 	}
 	
 	public void animateExplosion() {
 		
 		// TODO: configurar animacion de explosin. Dejo comentado un codigo de
 		// ejemplo
 		/*
 		 * long tileDuration = 300; sprite.animate(new long[]{tileDuration,
 		 * tileDuration}, 1, 3, true);
 		 */
 		isOnExlplosionCooldown = true;
 		
 		TimerHelper.startTimer(this.position, 300 * 2, new ITimerCallback()
 		{
 			public void onTimePassed(final TimerHandler pTimerHandler) {
 				if (destination != null) {
 					explosionEnd();
 				}
 			}
 		});
 	}
 	
 	private void explosionEnd() {
 		this.isOnExlplosionCooldown = false;
 		this.hasExploded = true;
 	}
 	
 	public void recycle() {
 		this.speed = LaserBeam.DEFAULT_SPEED;
 		this.lastParringJedi = null;
 		
 		this.sprite.setPosition(0, 0);
 		this.sprite.clearEntityModifiers();
 		this.sprite.clearUpdateHandlers();
 		this.sprite.detachSelf();
 		
 		this.position.setPosition(0, 0);
 		this.position.clearEntityModifiers();
 		this.position.clearUpdateHandlers();
 		this.position.detachSelf();
 		
 	}
 	
 	/* HELPERS */
 	public void removeOldMovementOrders() {
 		this.destination = null;
 		this.position.unregisterEntityModifiers(new IEntityModifier.IEntityModifierMatcher()
 		{
 			public boolean matches(IModifier<org.andengine.entity.IEntity> pObject) {
 				boolean matches = pObject.getClass().equals(PathModifier.class) || pObject.getClass().equals(MoveModifier.class);
 				return matches;
 			}
 		});
 	}
 	
 	public Sprite getSprite() {
 		return sprite;
 	}
 	
 }
