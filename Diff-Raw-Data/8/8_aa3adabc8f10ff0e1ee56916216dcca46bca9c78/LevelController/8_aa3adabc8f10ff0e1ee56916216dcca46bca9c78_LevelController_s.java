 package com.dat255.Wood.controller;
 
 import java.util.HashMap;
 
 import com.badlogic.gdx.math.Vector2;
 import com.dat255.Wood.WoodGame;
 import com.dat255.Wood.model.Block;
 import com.dat255.Wood.model.GameTimer;
 import com.dat255.Wood.model.HighScore;
 import com.dat255.Wood.model.Level;
 import com.dat255.Wood.model.Player;
 import com.dat255.Wood.model.Player.State;
 import com.dat255.Wood.model.SoundHandler;
 import com.dat255.Wood.screens.HighScoreScreen;
 import com.dat255.Wood.screens.LevelSelect;
 
 
 /**
  * This class modifies the model classes based on input from a user, and implements most of the game's logic.
  */
 public class LevelController {
 
 
 	public boolean isPaused;
 	public boolean levelWon;
 	public boolean gameOver;
 
 	public LevelSelect levelSelect;
 
 
 	enum Keys
 	{
 		LEFT, RIGHT, UP, DOWN
 	}
 
 	private Level level;
 	private Player player;
 	private WoodGame game;
 	private float startXpos, startYpos, actionBlockStartXpos, actionBlockStartYpos;
 	private Block actionBlock = null;
 	private Block oldActionBlockGround = null;
 
 	static HashMap<LevelController.Keys, Boolean> keys = new HashMap<LevelController.Keys, Boolean>();
 
 	static
 	{
 		keys.put(Keys.LEFT, false);
 		keys.put(Keys.RIGHT, false);
 		keys.put(Keys.UP, false);
 		keys.put(Keys.DOWN, false);
 	};
 
 	/**
 	 * Constructor for LevelController.
 	 * It loads a level and a player and sets all starting
 	 * booleans to proper values
 	 * @param level The level that will be loaded
 	 * @param game used for advancing between screens.
 	 *
 	 */
 	public LevelController(Level level,WoodGame game)
 	{
 		this.level = level;
 		this.player = level.getPlayer();
 		this.game = game;
 		isPaused = false;
 		levelWon = false;
 		gameOver = false;
 	}
 
 	//Input
 
 	public void leftPressed()
 	{
 		keys.put(Keys.LEFT, true);
 	}
 
 	public void leftReleased()
 	{
 		keys.put(Keys.LEFT, false);
 	}
 
 	public void rightPressed()
 	{
 		keys.put(Keys.RIGHT, true);
 	}
 
 	public void rightReleased()
 	{
 		keys.put(Keys.RIGHT, false);
 	}
 
 	public void upPressed()
 	{
 		keys.put(Keys.UP, true);
 	}
 
 	public void upReleased()
 	{
 		keys.put(Keys.UP, false);
 	}
 
 	public void downPressed()
 	{
 		keys.put(Keys.DOWN, true);
 	}
 
 	public void downReleased()
 	{
 		keys.put(Keys.DOWN, false);
 	}
 
 	/**
 	 * This method is run repeatedly. It increases the timer as long
 	 * as the game is not paused or gameover has been activated.
 	 * The timer will also only tick if a second has passed. It also
 	 * checks for input from the user. It also looks for interactions with
 	 * different kinds of blocks.
 	 * @param delta Seconds since last frame
 	 */
 	public void update(float delta)
 	{
 		if(!isPaused && !gameOver){
 			GameTimer.updateFps();
 
 			processInput();
 			finishPlayerMovement();
 			finishActionBlockMovement();
 			gameOver();
 			player.update(delta);
 			if(actionBlock != null)
 			{
 				actionBlock.update(delta);
 			}
 			
 		}	
 
 
 	}
 
 	/**
 	 * This method moves the player. (Only one of the input parameters should be allowed to be non-zero and the non zero-value must be +1/-1 but not more.)
 	 * @param dirX direction in x-plane.(for example -1 moves the player to the left and +1 moves the player to the right.)
 	 * @param dirY direction in y-plane.(for example -1 moves the player to the down and +1 moves the player to the up.)
 	 */
 
 	private void movePlayer(int dirX, int dirY)
 	{
 		player.setState(State.WALKING);
 
 		//Checks if
 		if(level.getGroundLayer()[(int) (player.getPosition().x + dirX)][(int) (player.getPosition().y + dirY)].isSlippery())
 		{
 			player.setState(State.SLIDING);
 		}
 		//Sets the players velocity to go in the direction in which he will be moving.
 		player.getVelocity().x = dirX * Player.SPEED;
 		player.getVelocity().y = dirY * Player.SPEED;
 		//Saves the players position before he starts to move, so that we later on can know when he has moved 1 unit from his starting position.
 		startXpos = player.getPosition().x;
 		startYpos = player.getPosition().y;
 	}
 
 	/**
 	 * This method stops the player
 	 * @param incX the direction the players position should be incremented with when done moving.
 	 * @param incY the direction the players position should be incremented with when done moving.
 	 */
 
 	private void stopPlayer(int incX, int incY)
 	{
 		//Checks if the right circumstances are met for the player to be allowed so stop.
 		if(!(player.getState() == State.SLIDING && level.getGroundLayer()[(int) (startXpos + incX)][(int) (startYpos + incY)].isSlippery() && !level.getCollisionLayer()[(int) (startXpos + (2 * incX))][(int) (startYpos + (2 * incY))].isSolid()))	
 		{
 			player.setState(State.IDLE);
 			player.getAcceleration().x = 0;
 			player.getVelocity().x = 0;
 			player.getAcceleration().y = 0;
 			player.getVelocity().y = 0;
 		}
 		player.getPosition().set(startXpos + incX, startYpos + incY);
 		startXpos = startXpos + incX;
 		startYpos = startYpos + incY;	
 	}
 
 	/**
 	 *Determines if the player can move in a specific direction (char d).
 	 *And if there is a actionBlock in front of him interact with it.
 	 * @param dX direction the player should move in the x-plane.
 	 * @param dY direction the player should move in the y-plane.
 	 */
 	private boolean canMoveTo(int dX, int dY)
 	{
 		int deltaX = dX;
 		int deltaY = dY;
 
 		unlockDoor(dX,dY);
 
 		//If the adjacent block in the direction the player is moving to is an actionBlock save it in ActionBlock variable and apply velocity as well as save its starting position. 
 		if((level.getCollisionLayer()[(int) (player.getPosition().x + deltaX)][(int) player.getPosition().y + deltaY].isMoveable()) && 
 				(level.getCollisionLayer()[(int) (player.getPosition().x + (2 * deltaX))][(int) player.getPosition().y + (2 * deltaY)].isSolid() == false))
 		{
 			actionBlock = level.getCollisionLayer()[(int) (player.getPosition().x + deltaX)][(int) player.getPosition().y + deltaY];
 			actionBlock.getVelocity().x = deltaX * Block.SPEED;
 			actionBlock.getVelocity().y = deltaY * Block.SPEED;
 			actionBlockStartXpos = actionBlock.getPosition().x;
 			actionBlockStartYpos = actionBlock.getPosition().y;
 			return true;
 		}
 		//Move if the adjacent block is not solid.
 		return (!(level.getCollisionLayer()[(int) (player.getPosition().x + deltaX)][(int) player.getPosition().y + deltaY].isSolid()));
 	}
 
 	//If a block is on a liquid block,  replaces them both with ground blocks
 	private boolean pushBlockToLiquid(int x, int y){
 		if(level.getGroundLayer()[(int)actionBlockStartXpos+x][(int) actionBlockStartYpos+y].isLiquid()){
 			if(level.getGroundLayer()[(int)actionBlockStartXpos+x][(int) actionBlockStartYpos+y].getBlockId() == '3'){
 				SoundHandler.playWater();	
 			}
 			else if(level.getGroundLayer()[(int)actionBlockStartXpos+x][(int) actionBlockStartYpos+y].getBlockId() == '4'){
 				SoundHandler.playFire();	
 			}
 			level.getCollisionLayer()[(int)actionBlockStartXpos][(int) actionBlockStartYpos] = new Block(new Vector2(actionBlockStartXpos, actionBlockStartYpos), '0', false, false,false,false); //Kan vara fel hr
 			level.getGroundLayer()[(int)actionBlockStartXpos+x][(int) actionBlockStartYpos+y] = new Block(new Vector2(actionBlockStartXpos+x, actionBlockStartYpos+y), '0', false, false,false,false);	
 			return true;
 		}
 		return false;
 	}
 	/**
 	 * This method teleports the player between 2 twin teleportation blocks.
 	 */
 	public void teleportPlayer(){
 
 		char tpBlockId = (char) level.getGroundLayer()[(int) player.getPosition().x][(int) player.getPosition().y].getBlockId();
 
 		if(tpBlockId!='T' && tpBlockId!='t')
 			return;
 
 		SoundHandler.playTeleport();
 
 		for(int x=0;x<16;x++){						
 			for(int y=0;y<16;y++){
 				if(level.getGroundLayer()[x][y].getBlockId()==tpBlockId && !(new Vector2((float)x,(float)y).equals(level.getPlayer().getPosition())) && !(level.getCollisionLayer()[x][y].isSolid())){
 					level.getPlayer().getPosition().set(new Vector2((float)x,(float)y));
 					return;
 				}
 			}
 		}		
 	}
 
 	/**
 	 * This method picks up a key for the player if he does not have one.
 	 */
 	public void isOnKey(){
 		if(level.getCollisionLayer()[(int) player.getPosition().x][(int) player.getPosition().y].getBlockId()=='K'){
 			player.increaseKey();
 			SoundHandler.playPick();
 			level.getCollisionLayer()[(int) player.getPosition().x][(int) player.getPosition().y] =new Block(new Vector2(player.getPosition().x,player.getPosition().y), '0', false, false,false,false);
 		}
 	}
 
 	/**
 	 * This method removes the key from a player and opens a door. And then replaces the door with
 	 * a ground block
 	 * @param dx The x coordinate of the door
 	 * @param dy the y coordinate of the door
 	 */
 	public void unlockDoor(int dx,int dy){
 		if((level.getCollisionLayer()[(int) player.getPosition().x+dx][(int) player.getPosition().y+dy].getBlockId()) == 'H' && level.getPlayer().hasKey()){
 			level.getPlayer().decreaseKey();
 			SoundHandler.playUnlock();
 			level.getCollisionLayer()[(int) player.getPosition().x+dx][(int) player.getPosition().y+dy] =new Block(new Vector2(player.getPosition().x+dx,player.getPosition().y+dy), '0', false, false,false,false);
 		}
 
 	}
 
 	/**
 	 * Checks if the player is standing on a block that incorporates special logic.
 	 */
 	public void doBlockLogic(){
 		if(!(((level.getGroundLayer()[(int) player.getPosition().x][(int) player.getPosition().y].getBlockId()) == '0')) || !(((level.getCollisionLayer()[(int) player.getPosition().x][(int) player.getPosition().y].getBlockId()) == '0'))){
 			teleportPlayer();
 			isOnKey();
 			isOnFatalBlock();
 			isOnGoalBlock();
 
 		}
 	}
 
 	/**
 	 * Switches place of 2 collisionblocks with each other.
 	 * @param x1 x position of 1st block.
 	 * @param y1 y position of 1st block.
 	 * @param x2 x position of 2nd block.
 	 * @param y2 y position of 2nd block.
 	 */
 	public void switchCollisionBlocks(int x1, int y1, int x2, int y2)
 	{
 		Block[][] collisionLayer = level.getCollisionLayer();
 
 		if(oldActionBlockGround == null)
 		{
 			oldActionBlockGround = new Block(new Vector2(x1,y1), '0', false, false,false,false);
 		}
 
 		Block temp = collisionLayer[x1][y1];
 		Block temp2 = collisionLayer[x2][y2];
 
 		temp.getPosition().set(x2, y2);
 
 		collisionLayer[x1][y1] = oldActionBlockGround;
 		collisionLayer[x2][y2] = temp;
 
 		oldActionBlockGround = temp2;
 	}
 
 	/**
 	 * Checks if the player is standing on an fatal block, if so set his state to dead.
 	 */
 	public void isOnFatalBlock(){
 		if(level.getGroundLayer()[(int) player.getPosition().x][(int) player.getPosition().y].isLiquid()){
 			player.setState(State.DEAD);
 			SoundHandler.stopAllSound();
 		}
 	}
 	
 	public void gameOver()
 	{
 		//If the players state is dead then set gameOver to true and go back to the LevelSelect screen.
 		if(player.getState()==State.DEAD){
 			gameOver=true;
 			game.setScreen(new LevelSelect(game));
 		}
 	}
 
 	/**
 	 * Checks if the player is standing on a goal block, if so set boolean levelWon to true which will finish the level.
 	 */
 	public void isOnGoalBlock(){
 		if(level.getGroundLayer()[(int) player.getPosition().x][(int) player.getPosition().y].getBlockId()=='G'){
 			levelWon = true;
 			SoundHandler.stopAllSound();
 			SoundHandler.playApplause();
 
 			new HighScore(game.getName(), GameTimer.getTime()).updateScorelist();
 			game.setScreen(new HighScoreScreen(game));
 		}
 	}
 
 	/**
 	 * Processes the input, and makes the player move. Also handles some movement and actionBlock logic aswell as checking if the game is over. (Will be reworked when time is available).
 	 */
 	private void processInput()
 	{
 		//Check if any of the keys are pressed down and if so check if the player can move in that direction and if thats true start moveing the player in the desired direction.
 		if(player.getState() == Player.State.IDLE)
 		{
 			if(keys.get(Keys.LEFT))
 			{
 				player.setFacingDirection(Player.FacingDirection.LEFT);
 				if(canMoveTo(-1,0))
 				{
 					movePlayer(-1,0);
 				}
 			}
 
 			else if(keys.get(Keys.RIGHT))
 			{
 				player.setFacingDirection(Player.FacingDirection.RIGHT);
 				if(canMoveTo(1,0))
 				{
 					movePlayer(1,0);
 				}
 			}
 
 			else if(keys.get(Keys.UP))
 			{
 				player.setFacingDirection(Player.FacingDirection.UP);
 				if(canMoveTo(0,1))
 				{
 					movePlayer(0,1);
 				}
 			}
 
 			else if(keys.get(Keys.DOWN))
 			{
 				player.setFacingDirection(Player.FacingDirection.DOWN);
 				if(canMoveTo(0,-1))
 				{
 					movePlayer(0,-1);
 				}
 			}
 		}
 
 
 		
 	}
 
 	/**
 	 * If there is an active actionBlock check if it has moved more than 1 unit and if so finish its movement logic.
 	 */
 	public void finishActionBlockMovement()
 	{
 
 		if(actionBlock != null)
 		{
 
 
 			if ((actionBlock.getPosition().x - actionBlockStartXpos) > 1)
 			{
 				if(pushBlockToLiquid(1,0)==false){
 					actionBlock.getVelocity().x = 0;
 					switchCollisionBlocks((int) actionBlockStartXpos,(int) actionBlockStartYpos,(int) (actionBlockStartXpos + 1),(int) actionBlockStartYpos);
 					actionBlock.getPosition().set(actionBlockStartXpos + 1, actionBlockStartYpos);
 				}
 				actionBlock = null;
 
 			}
 			else if ((actionBlock.getPosition().y - actionBlockStartYpos) > 1)
 			{
 				if(pushBlockToLiquid(0,1)==false){
 					actionBlock.getVelocity().y = 0;
 					switchCollisionBlocks((int) actionBlockStartXpos,(int) actionBlockStartYpos,(int) (actionBlockStartXpos ),(int) actionBlockStartYpos + 1);
 					actionBlock.getPosition().set(actionBlockStartXpos , actionBlockStartYpos + 1);
 				}
 				actionBlock = null;
 
 			}
 			else if (Math.abs((actionBlock.getPosition().y - actionBlockStartYpos)) > 1)
 			{
 				if(pushBlockToLiquid(0,-1)==false){
 					actionBlock.getVelocity().y = 0;
 					switchCollisionBlocks((int) actionBlockStartXpos,(int) actionBlockStartYpos,(int) (actionBlockStartXpos ),(int) actionBlockStartYpos - 1);
 					actionBlock.getPosition().set(actionBlockStartXpos , actionBlockStartYpos - 1);
 				}
 				actionBlock = null;
 
 			}
 			else if (Math.abs((actionBlock.getPosition().x - actionBlockStartXpos)) > 1)
 			{
 				if(pushBlockToLiquid(-1,0)==false){
 					actionBlock.getVelocity().x = 0;
 					switchCollisionBlocks((int) actionBlockStartXpos,(int) actionBlockStartYpos,(int) (actionBlockStartXpos - 1),(int) actionBlockStartYpos);
 					actionBlock.getPosition().set(actionBlockStartXpos - 1 , actionBlockStartYpos);
 				}
 				actionBlock = null;
 
 			}
 		}
 	}
 	
 	public void finishPlayerMovement()
 	{
 		//If the player is moving and have moved more than 1 unit from its startPosition, then stop the Player and do some blockLogic checks.		
 				if(player.getState() != State.IDLE)
 				{
 					if ((player.getPosition().x - startXpos) > 1)
 					{
 						stopPlayer(1,0);
 						doBlockLogic();
 					}
 					else if ((player.getPosition().y - startYpos) > 1)
 					{
 						stopPlayer(0,1);
 						doBlockLogic();
 					}
 					else if (Math.abs((player.getPosition().x - startXpos)) > 1)
 					{
 						stopPlayer(-1,0);
 						doBlockLogic();
 					}
 					else if (Math.abs((player.getPosition().y - startYpos)) > 1)
 					{
 						stopPlayer(0,-1);
 						doBlockLogic();
 					}
 				}
 	}
 
 }
