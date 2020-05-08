 package tsa2035.game.engine.scene;
 
 import java.util.Iterator;
 
 import org.lwjgl.input.Keyboard;
 
 import tsa2035.game.engine.bounding.Side;
 import tsa2035.game.engine.texture.Texture;
 
 public class Player extends Sprite {
 
 	boolean handleGravity;
 	float fallRate = 0.00005f;
 	
 	float jumpHeight = 0;
 	float jumpRate = 0;
 	boolean isJumping = false;
 	boolean jumpingDisabled = false;
 	float currentJumpOffset = 0f;
 	float lastJumpOffset = 0;
 	
 	protected boolean isWalking = false;
 	protected boolean leftWalking = false;
 	
 	SinglePressKeyboard interactKey = new SinglePressKeyboard(Keyboard.KEY_E);
 	
 	public Player(float x, float y, Texture t, boolean handleGravity) {
 		super(x, y, t);
 		this.handleGravity = handleGravity;
 	}
 	
 	public Player(float x, float y, Texture t, boolean handleGravity, float jumpHeight, float jumpRate)
 	{
 		this(x,y,t, handleGravity);
 		this.jumpHeight = jumpHeight;
 		this.jumpRate = jumpRate;
 	}
 	
 	public void setGravity(boolean state)
 	{
 		handleGravity = state;
 	}
 	
 	public boolean isHandlingGravity()
 	{
 		return handleGravity;
 	}
 	
 	public void setJumpingDisabled(boolean state)
 	{
 		jumpingDisabled = state;
 	}
 	
 	public boolean isJumpingDisabled()
 	{
 		return jumpingDisabled;
 	}
 	
 	public void render(Scene scene)
 	{
 		Iterator<Sprite> sceneObjects = scene.iterator();
 		boolean hitSides[] = new boolean[4];
 		boolean freefall = false;
 		boolean allowJumping = false;
 		boolean interactPressed = interactKey.check();
 		while ( sceneObjects.hasNext() )
 		{
 			Sprite thisObj = sceneObjects.next();
  
 			if ( !this.equals(thisObj) && !thisObj.isHidden() )
 			{
 				if ( interactPressed )
 					thisObj.interact(this);
 					
 				if ( thisObj.isSolid() )
 				{
 					Side hitSide = sideOfContact(thisObj);
 
 					if ( hitSide == Side.BOTTOM )
 						isJumping = false;
 					if ( hitSide == Side.TOP )
 						allowJumping = true;
 					
 					if ( thisObj.isPushable() )
 					{
 						if ( hitSide == Side.LEFT )
 						{
 							thisObj.setX(getX()+(getWidth()*2));
 						}
 						else if ( hitSide == Side.RIGHT )
 						{
 							thisObj.setX(getX()-(getWidth()*2));
 						}
 					}
 					else if ( hitSide != Side.NONE )
 						hitSides[hitSide.ordinal()] = true;
 				}
 			}
 		}
 		
 		if ( jumpingDisabled )
 			allowJumping = false;
 		
 		if ( !isJumping )
 			isJumping = ( allowJumping && Keyboard.isKeyDown(Keyboard.KEY_SPACE) );
 		
 		freefall = (!hitSides[Side.TOP.ordinal()] && handleGravity);
 		
 		if ( freefall )
 			fallRate += (float) Math.sqrt(fallRate)/300;
 		else
 			fallRate = 0.00005f;
 		
 		if ( fallRate > 0.01 )
 			fallRate = 0.01f;
 		
 		if ( freefall && !isJumping )
 		{
 			setY(getY()-fallRate);
 		}
 		else if ( isJumping && jumpHeight > 0 )
 		{
 			currentJumpOffset += jumpRate;
 			setY((getY()-lastJumpOffset)+currentJumpOffset);
 			lastJumpOffset = currentJumpOffset;
 
 			if ( currentJumpOffset >= jumpHeight )
 			{
 				isJumping = false;
 				currentJumpOffset = 0;
 				lastJumpOffset = 0;
 			}
 		}
 		
 		isWalking = false;
		if ( !hitSides[Side.RIGHT.ordinal()] && Keyboard.isKeyDown(Keyboard.KEY_A) )
 		{
 			setX(getX()-0.005f);
 			isWalking = true;
 			leftWalking = true;
 		}
 		
 		if ( !hitSides[Side.TOP.ordinal()] && Keyboard.isKeyDown(Keyboard.KEY_S) && !freefall )
 		{
 			setY(getY()-0.005f);
 		}
 		
 		if (  Keyboard.isKeyDown(Keyboard.KEY_W) && !handleGravity )
 		{
 			setY(getY()+0.005f);
 		}
 
		if ( !hitSides[Side.LEFT.ordinal()] && Keyboard.isKeyDown(Keyboard.KEY_D) )
 		{
 			setX(getX()+0.005f);
 			isWalking = true;
 			leftWalking = false;
 		}
 		
 		super.render(scene);
 	}
 	
 }
