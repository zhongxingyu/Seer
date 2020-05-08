 /* 
 	JCavernApplet.java
 
 	Title:			JCavern And Glen
 	Author:			Bill Walker
 	Description:	
 */
 
 package jcavern;
 
 import java.awt.event.*;
 import java.awt.*;
 import java.util.Enumeration;
 
 public class KeyboardCommandListener extends KeyAdapter
 {
 	/** * A model of the game world */
 	private World				mWorld;
 	
 	private WorldView			mWorldView;
 	
 	private MissionView			mMissionView;
 	
 	/** * The representation of the player */
 	private Player				mPlayer;
 	
 	private int					mCurrentMode;
 	
 	/** * In normal command mode (movement, etc) */
 	private static final int	NORMAL_MODE = 1;
 	
 	/** * In normal command mode (movement, etc) */
 	private static final int	SWORD_MODE = 2;
 	
 	/** * In normal command mode (movement, etc) */
 	private static final int	RANGED_ATTACK_MODE = 3;
 	
 	/** * In normal command mode (movement, etc) */
 	private static final int	CASTLE_MODE = 4;
 
 
 	public KeyboardCommandListener(World aWorld, WorldView aWorldView, Player aPlayer, MissionView aMissionView)
 	{
 		mWorld = aWorld;
 		mWorldView = aWorldView;
 		mPlayer = aPlayer;
 		mMissionView = aMissionView;
 		mCurrentMode = NORMAL_MODE;
 	}
 	
 	private int parseDirectionKey(KeyEvent e)
 	{
 	    switch (e.getKeyChar())
 		{
 			case 'q' : return Location.NORTHWEST;
 			case 'w' : return Location.NORTH;
 			case 'e' : return Location.NORTHEAST;
 			case 'a' : return Location.WEST;
 			case 'd' : return Location.EAST;
 			case 'z' : return Location.SOUTHWEST;
 			case 'x' : return Location.SOUTH;
 			case 'c' : return Location.SOUTHEAST;
 		}
 		
 		throw new IllegalArgumentException("not a movement key!");
 	}
 
 	/**
 	 * Handles keyboard commands.
 	 */
 	public void keyTyped(KeyEvent e)
 	{
 	    try
 		{
 			switch (mCurrentMode)
 			{
 				case NORMAL_MODE: switch (e.getKeyChar())
 						{
 							// movement commands
 							case 'q' :
 							case 'w' :
 							case 'e' :
 							case 'a' :
 							case 'd' :
 							case 'z' :
 							case 'x' :
 							case 'c' : doMove(parseDirectionKey(e)); break;
							case 's' : mCurrentMode = SWORD_MODE; JCavernApplet.log("Sword attack, direction?"); break;
 							case 'b' : mCurrentMode = RANGED_ATTACK_MODE; JCavernApplet.log("Ranged attack, direction?"); break;
 							case 'v' : mCurrentMode = CASTLE_MODE; JCavernApplet.log("Visiting Castle, command?"); break;
 							case '.' : JCavernApplet.log("Sit"); break;
 							case 'o' : doOpen(); break;
 							default  : JCavernApplet.log("Unknown command");
 						} break;
 				case CASTLE_MODE: switch (e.getKeyChar())
 						{
 							// movement commands
 							case 'q' : doEndMission(); mCurrentMode = NORMAL_MODE; break;
 							default  : JCavernApplet.log("Unknown command");
 						} break;
 				case SWORD_MODE: switch (e.getKeyChar())
 						{
 							// movement commands
 							case 'q' :
 							case 'w' :
 							case 'e' :
 							case 'a' :
 							case 'd' :
 							case 'z' :
 							case 'x' :
 							case 'c' : doAttack(parseDirectionKey(e)); break;
 							default  : JCavernApplet.log("Unknown command");
 						} break;
 				case RANGED_ATTACK_MODE: switch (e.getKeyChar())
 						{
 							// movement commands
 							case 'q' :
 							case 'w' :
 							case 'e' :
 							case 'a' :
 							case 'd' :
 							case 'z' :
 							case 'x' :
 							case 'c' : doRangedAttack(parseDirectionKey(e)); break;
 							default  : JCavernApplet.log("Unknown command");
 						} break;
 			}
 	
 			// and now, the monsters get a turn
 			if (mCurrentMode == NORMAL_MODE)
 			{
 				mWorld.doTurn();
 			}
 			
 			if (mPlayer.isDead())
 			{
 				JCavernApplet.log("Sorry, " + mPlayer.getName() + ", your game is over.");
 			}
 		}
 		catch(JCavernInternalError jcie)
 		{
 			System.out.println("internal error " + jcie);
 		}
 	}
 
 	private void doEndMission() throws JCavernInternalError
 	{
 		if (mPlayer.getMission().getCompleted())
 		{
 			JCavernApplet.log("Congratulations, " + mPlayer.getName() + ".");
 			
 			// let's do it again!
 			mPlayer.setMission(MonsterFactory.createMission(mPlayer));
 			mMissionView.setModel(mPlayer.getMission());
 	
 			// Create a world  and a view of the world
 			mWorld.populateFor(mPlayer);
 		}
 		else
 		{
 			JCavernApplet.log("Sorry, " + mPlayer.getName() + ", you have not completed your mission");
 		}		
 	}
 	
 	private void doRangedAttack(int direction) throws JCavernInternalError
 	{
 		if (mPlayer.getArrows() > 0)
 		{
 			try
 			{
 				mPlayer.rangedAttack(mWorld, direction);
 			}
 			catch(NonCombatantException nce)
 			{
 				JCavernApplet.log(mPlayer.getName() + " can't attack that!");
 			}
 			catch(IllegalLocationException ile)
 			{
 				JCavernApplet.log(mPlayer.getName() + " shot arrow of the edge of the world!"); 
 			}
 		}
 		else
 		{
 			JCavernApplet.log(mPlayer.getName() + " has no more arrows!"); 
 		}
 				
 		mCurrentMode = NORMAL_MODE;
 	}	
 
 	private void doAttack(int direction) throws JCavernInternalError
 	{
 		try
 		{
 			mPlayer.attack(mWorld, direction);
 		}
 		catch(IllegalLocationException nce)
 		{
 			JCavernApplet.log(mPlayer.getName() + " can't attack off the edge of the world!");
 		}
 		catch(EmptyLocationException nce)
 		{
 			JCavernApplet.log(mPlayer.getName() + " has nothing to attack!");
 		}
 		catch(NonCombatantException nce)
 		{
 			JCavernApplet.log(mPlayer.getName() + " can't attack that!");
 		}
 
 		mCurrentMode = NORMAL_MODE;
 	}
 	
 	private void doOpen() throws JCavernInternalError
 	{
 		TreasureChest aChest = (TreasureChest) mWorld.getNeighboring(mWorld.getLocation(mPlayer), new TreasureChest(null, 0));
 
 		mWorld.remove(aChest);
 		JCavernApplet.log(mPlayer.getName() + " found " + aChest);
 		
 		if (aChest.getGold() > 0)
 		{
 			mPlayer.incrementGold(aChest.getGold());
 		}
 		
 		if (aChest.getContents() != null)
 		{
 			mPlayer.receiveItem(aChest.getContents());
 		}
 	}
 	
 	private void doMove(int direction) throws JCavernInternalError
 	{
 		try
 		{
 			Location oldLocation = mWorld.getLocation(mPlayer);
 			
 			mWorld.move(mPlayer, direction);
 			
 			if (mPlayer.getCastle() != null)
 			{
 				mWorld.place(oldLocation, mPlayer.getCastle());
 				mPlayer.setCastle(null);
 			}
 		}
 		catch (ThingCollisionException tce)
 		{
 			if (tce.getMovee() instanceof Castle)
 			{
 				Castle	theCastle = (Castle) tce.getMovee();
 				
 				mWorld.remove(theCastle);
 				doMove(direction);
 				mPlayer.setCastle(theCastle);
				JCavernApplet.log(mPlayer.getName() + " collected " + tce.getMovee().getName());
 			}
 			else
 			{
 				JCavernApplet.log(mPlayer.getName() + " collided with " + tce.getMovee().getName());
 			}
 		}
 		catch (IllegalLocationException tce)
 		{
 			JCavernApplet.log(mPlayer.getName() + " can't move off the edge of the world!");
 		}
 	}
 }
