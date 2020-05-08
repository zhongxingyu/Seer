 package com.phaerical.graveblade.entities;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.graphics.Color;
 import com.badlogic.gdx.graphics.g2d.Animation;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import com.badlogic.gdx.graphics.g2d.TextureRegion;
 import com.badlogic.gdx.maps.tiled.TiledMap;
 import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
 import com.badlogic.gdx.maps.tiled.TiledMapTileLayer.Cell;
 import com.badlogic.gdx.math.Rectangle;
 import com.badlogic.gdx.math.Vector2;
 import com.badlogic.gdx.scenes.scene2d.Action;
 import com.badlogic.gdx.scenes.scene2d.Actor;
 import com.badlogic.gdx.utils.Array;
 import com.phaerical.graveblade.SoundManager;
 import com.phaerical.graveblade.entities.EntityAction.ActionType;
 import com.phaerical.graveblade.screens.GameScreen;
 
 public abstract class Entity extends Actor
 {
 	final float GRAVITY = -0.3f;
 	
 	enum EntityState
 	{
 		MOVING, JUMPING, FALLING, HURT, DYING, ATTACKING
 	}
 	
 	private EntityState state;
 	
 	protected Vector2 velocity;
 	private float speed;
 	private float jumpSpeed;
 	
 	private int maxHealth;
 	private int health;
 	
 	private int strength;
 	private int vitality;
 	private int dexterity;
 	private int luck;
 	
 	private Animation idleAnimation;
 	private Animation runAnimation;
 	private Animation jumpAnimation;
 	private Animation attackAnimation;
 	private Animation hurtAnimation;
 	private Animation deathAnimation;
 	
 	private boolean facingRight = true;
 	
 	protected float stateTime;
 	
 	private TiledMap map;
 	private Array<Rectangle> tiles;
 	
 	private float spriteScale;
 	
 	
 	public Entity (TiledMap map)
 	{
 		this.velocity = new Vector2 ();
 		this.map = map;
 		this.tiles = new Array<Rectangle> ();
 		
 		this.state = EntityState.MOVING;
 		this.stateTime = 0f;
 		this.spriteScale = 1;
 		this.speed = 1f;
 	}
 	
 	
 	/**************************************
 	 * ATTRIBUTES METHODS
 	 **************************************/
 	
 	public int getStrength ()
 	{
 		return strength;
 	}
 	
 	public void setStrength (int str)
 	{
 		strength = str;
 	}
 	
 	public int getVitality ()
 	{
 		return vitality;
 	}
 	
 	public void setVitality (int vit)
 	{
 		vitality = vit;
 	}
 	
 	public int getDexterity ()
 	{
 		return dexterity;
 	}
 	
 	public void setDexterity (int dex)
 	{
 		dexterity = dex;
 	}
 	
 	public int getLuck ()
 	{
 		return luck;
 	}
 	
 	public void setLuck (int lck)
 	{
 		luck = lck;
 	}
 	
 	public float getSpeed ()
 	{
 		return speed;
 	}
 	
 	public void setSpeed (float s)
 	{
 		speed = s;
 	}
 	
 	public float getJumpSpeed ()
 	{
 		return jumpSpeed;
 	}
 	
 	public void setJumpSpeed (float s)
 	{
 		jumpSpeed = s;
 	}
 	
 	public void setVelocityX (float x)
 	{
 		velocity.x = x;
 	}
 	
 	public void setVelocityY (float y)
 	{
 		velocity.y = y;
 	}
 	
 	
 	/**************************************
 	 * STATE METHODS
 	 **************************************/
 	
 	public EntityState getState ()
 	{
 		return state;
 	}
 	
 	public void setState (EntityState s)
 	{
 		state = s;
 		if (s != EntityState.MOVING)
 		{
 			stateTime = 0f;
 		}
 	}
 	
 	public boolean isFacingRight ()
 	{
 		return facingRight;
 	}
 	
 	public void setFacingRight (boolean right)
 	{
 		if (state != EntityState.ATTACKING)
 		{
 			if (right)
 			{
 				facingRight = true;
 			}
 			else
 			{
 				facingRight = false;
 			}
 		}
 	}
 	
 	public void addEntityAction (ActionType type)
 	{
 		addAction (new EntityAction (type));
 	}
 	
 	public void addEntityAction (ActionType type, float duration)
 	{
 		addAction (new EntityAction (type, duration));
 	}
 	
 	public void removeEntityAction (ActionType type)
 	{
 		for (Action a : getActions())
 		{
 			if (a.getClass() == EntityAction.class && ((EntityAction) a).getType() == type)
 			{
 				removeAction (a);
 			}
 		}
 	}
 	
 	
 	/**************************************
 	 * ANIMATION METHODS
 	 **************************************/
 	
 	public Animation getIdleAnimation ()
 	{
 		return idleAnimation;
 	}
 	
 	public Animation getRunAnimation ()
 	{
 		return runAnimation;
 	}
 	
 	public Animation getJumpAnimation ()
 	{
 		return jumpAnimation;
 	}
 	
 	public Animation getAttackAnimation ()
 	{
 		return attackAnimation;
 	}
 	
 	public Animation getHurtAnimation ()
 	{
 		return hurtAnimation;
 	}
 	
 	public Animation getDeathAnimation ()
 	{
 		return deathAnimation;
 	}
 	
 	public void setIdleAnimation (Animation a)
 	{
 		idleAnimation = a;
 	}
 	
 	public void setRunAnimation (Animation a)
 	{
 		runAnimation = a;
 	}
 	
 	public void setJumpAnimation (Animation a)
 	{
 		jumpAnimation = a;
 	}
 	
 	public void setAttackAnimation (Animation a)
 	{
 		attackAnimation = a;
 	}
 	
 	public void setHurtAnimation (Animation a)
 	{
 		hurtAnimation = a;
 	}
 	
 	public void setDeathAnimation (Animation a)
 	{
 		deathAnimation = a;
 	}
 	
 	
 	/**************************************
 	 * HEALTH METHODS
 	 **************************************/
 	
 	public int getHealth ()
 	{
 		return health;
 	}
 	
 	public int getMaxHealth ()
 	{
 		return maxHealth;
 	}
 	
 	public void setHealth (int amount)
 	{
		if (amount > getMaxHealth ())
 		{
			health = getMaxHealth ();
 		}
 		else if (amount <= 0)
 		{
 			health = 0;
 			die ();
 		}
 		else
 		{
 			health = amount;
 		}
 	}
 	
 	public void setMaxHealth (int amount)
 	{
 		maxHealth = amount;
 	}
 	
 	public void die ()
 	{
 		SoundManager.play (SoundManager.DEATH);
 		setState (EntityState.DYING);
 	}
 	
 	public boolean isAlive ()
 	{
 		return (state != EntityState.DYING);
 	}
 	
 	public void setSpriteScale (float scale)
 	{
 		spriteScale = scale;
 	}
 	
 
 	/**************************************
 	 * ACT METHOD
 	 **************************************/
 	
 	@Override
 	public void act (float delta)
 	{
 		if (isAlive ())
 		{
 			super.act (delta);
 			
 			if (state != EntityState.HURT)
 			{
 				entityCollisionCheck ();
 			}
 			
 			if (state == EntityState.ATTACKING)
 			{
 				velocity.x = 0;
 			}
 			
 			float newX = getX () + velocity.x * delta;
 			float newY = getY () + velocity.y * delta;
 			
 			getCollisionTiles (tiles, newX, newY);
 			
 			boolean contactX, contactBottom, contactTop;
 			contactX = contactBottom = contactTop = true;
 
 			Vector2 leftUpperBound = new Vector2 (newX, newY + getHeight() * 2 / 3);
 			Vector2 leftLowerBound = new Vector2 (newX, newY + getHeight() / 3);
 			Vector2 rightUpperBound = new Vector2 (newX + getWidth(), newY + getHeight() * 2 / 3);
 			Vector2 rightLowerBound = new Vector2 (newX + getWidth(), newY + getHeight() / 3);
 			Vector2 upperLeftBound = new Vector2 (newX + getWidth() / 3, newY + getHeight());
 			Vector2 upperRightBound = new Vector2 (newX + getWidth() * 2 / 3, newY + getHeight());
 			Vector2 bottomLeftBound = new Vector2 (newX + getWidth() / 3, newY);
 			Vector2 bottomRightBound = new Vector2 (newX + getWidth() * 2 / 3, newY);
 			
 			for (int i = 0; i < 2 && (contactTop || contactBottom || contactX); i++)
 			{
 				contactTop = contactBottom = contactX = false;
 				
 				for (Rectangle r : tiles)
 				{
 					while ((r.contains (leftUpperBound) || r.contains (leftLowerBound)) && velocity.x < 0)
 					{
 						setX (getX() + 0.01f);
 						leftUpperBound.x = getX() * velocity.x;
 						leftLowerBound.x = getX() * velocity.x;
 						contactX = true;
 					}
 					
 					while ((r.contains (upperLeftBound) || r.contains (upperRightBound)) && velocity.y > 0)
 					{
 						setY (getY() - 0.01f);
 						upperLeftBound.y = getY() + velocity.y * delta + getHeight();
 						upperRightBound.y = getY() + velocity.y * delta + getHeight();
 						contactTop = true;
 					}
 					
 					while ((r.contains (bottomLeftBound) || r.contains (bottomRightBound)) && velocity.y < 0)
 					{
 						setY (getY() + 0.01f);
 						bottomLeftBound.y = getY () + velocity.y * delta;
 						bottomRightBound.y = getY () + velocity.y * delta;
 						contactBottom = true;
 					}
 					
 					while ((r.contains (rightUpperBound) || r.contains(rightLowerBound)) && velocity.x > 0)
 					{
 						setX (getX() - 0.01f);
 						rightUpperBound.x = getX() * velocity.x + getWidth();
 						rightLowerBound.x = getX() * velocity.x + getWidth();
 						contactX = true;
 					}
 				}
 				
 				if (contactTop)
 				{
 					velocity.y = 0;
 				}
 				
 				if (contactBottom)
 				{
 					velocity.y = 0;
 					
 					if (state == EntityState.FALLING || state == EntityState.JUMPING)
 					{
 						setState (EntityState.MOVING);
 					}
 				}
 				
 				if (contactX)
 				{
 					velocity.x = 0;
 				}
 			}
 			
 			if (velocity.y < 0 && state != EntityState.HURT && state != EntityState.DYING)
 			{
 				setState (EntityState.FALLING);
 			}
 			
 			velocity.add (0, GRAVITY);
 			
 			setPosition (getX () + velocity.x, getY () + velocity.y);
 			
 		}
 		
 		if (getState() == EntityState.HURT && hurtAnimation.isAnimationFinished (stateTime))
 		{
 			setState (EntityState.MOVING);
 		}
 		
 		if (getState() == EntityState.ATTACKING && attackAnimation.isAnimationFinished (stateTime))
 		{
 			setState (EntityState.MOVING);
 		}
 		
 		if (getState() == EntityState.DYING && deathAnimation.isAnimationFinished (stateTime))
 		{
 			remove ();
 		}
 	}
 	
 	public void attack ()
 	{
 		if (state == EntityState.MOVING)
 		{
 			SoundManager.play (SoundManager.SWING);
 			setState (EntityState.ATTACKING);
 		}
 	}
 	
 	public void hurt (int damage)
 	{
 		if (state != EntityState.HURT && state != EntityState.DYING)
 		{
 			addEntityAction (ActionType.KNOCKBACK_LEFT);
 			GameScreen.ft.show (String.valueOf (damage), Color.WHITE, getX(), getY() + getHeight() + 10);
 			setHealth (getHealth() - damage);
 			
 		}
 	}
 	
 	public Rectangle getBounds ()
 	{
 		return new Rectangle (getX (), getY (), getWidth (), getHeight ());
 	}
 	
 	
 	public Rectangle getAttackBounds ()
 	{
 		if (state == EntityState.ATTACKING)
 		{
 			if (facingRight)
 			{
 				return new Rectangle (getX()+getWidth(), getY(), 40, 50);
 			}
 			else
 			{
 				return new Rectangle (getX()-getWidth(), getY(), 60, 50);
 			}
 		}
 		
 		return null;
 	}
 	
 	
 	@Override
 	public void draw (SpriteBatch batch, float parentAlpha)
 	{	
 		stateTime += Gdx.graphics.getDeltaTime ();
 		TextureRegion frame = null;
 		
 		if (state == EntityState.MOVING)
 		{
 			if (velocity.x != 0)
 			{
 				frame = runAnimation.getKeyFrame (stateTime, true);
 			}
 			else
 			{
 				frame = idleAnimation.getKeyFrame (stateTime, true);
 			}
 		}
 		else if (state == EntityState.JUMPING)
 		{
 			frame = jumpAnimation.getKeyFrame (stateTime, false);
 		}
 		else if (state == EntityState.FALLING)
 		{
 			frame = jumpAnimation.getKeyFrame (1, false);
 		}
 		else if (state == EntityState.ATTACKING)
 		{
 			frame = attackAnimation.getKeyFrame (stateTime, false);
 		}
 		else if (state == EntityState.HURT)
 		{
 			frame = hurtAnimation.getKeyFrame (stateTime, false);
 		}
 		else if (state == EntityState.DYING)
 		{
 			frame = deathAnimation.getKeyFrame (stateTime, false);
 		}
 		
 		setColor (Color.WHITE);
 		setSpriteColor ();
 		batch.setColor (getColor ());
 		
 		if (facingRight)
 		{
 			batch.draw (frame, getX(), getY(), frame.getRegionWidth() * spriteScale, frame.getRegionHeight() * spriteScale);
 		}
 		else
 		{
 			batch.draw (frame, getX() + getWidth(), getY(), -frame.getRegionWidth() * spriteScale, frame.getRegionHeight() * spriteScale);
 		}
 	}
 	
 
 	public void setSpriteColor ()
 	{
 		if (!isAlive ())
 		{
 			setColor (1, 0, 0, Math.max (0, 1 - stateTime / deathAnimation.animationDuration));
 		}
 		else if (state == EntityState.HURT)
 		{
 			setColor (0.9f, 0.2f, 0, 1);
 		}
 	}
 	
 	
 	protected abstract void entityCollisionCheck ();
 	
 	
 	private void getCollisionTiles (Array<Rectangle> tiles, float startX, float startY)
 	{
 		TiledMapTileLayer layer = (TiledMapTileLayer) map.getLayers().get(0);
 		
 		tiles.clear ();
 		
 		for (int x = (int) (startX / 32) - 4; x < (int) (startX / 32) + 4; x++)
 		{
 			for (int y = (int) (startY / 32) - 4; y < (int) (startY / 32) + 4; y++)
 			{
 				Cell cell = layer.getCell (x, y);
 				
 				if (cell != null && cell.getTile().getProperties().containsKey("blocked") ||
 						x < 0 || y < 0 || x > layer.getWidth() - 1 || y > layer.getHeight())
 				{
 					tiles.add (new Rectangle (x * map.getProperties().get("tilewidth", Integer.class),
 							y * map.getProperties().get("tileheight", Integer.class),
 							map.getProperties().get("tilewidth", Integer.class),
 							map.getProperties().get("tileheight", Integer.class)));
 				}
 			}
 		}
 	}
 }
