 /* 
 	MagicItem.java
 
 	Title:			JCavern And Glen
 	Author:			Bill Walker
 	Description:	
 */
 
 package jcavern;
 
 import jcavern.thing.*;
 import jcavern.ui.*;
 
 import java.util.Vector;
 
 /**
  * MagicItems are Things which can perform some magical operation.
  *
  * @author	Bill Walker
  * @version	$Id$
  */
 public class MagicItem extends Treasure
 {
 	/** * Which magic power does this particular item have? */
 	private int					mPower;
 	
 	/** * Indicates the power to do nothing.*/
 	public static final int		MAGIC_NOTHING = 0;
 	
 	/** * Indicates the power to permanently reveal invisiblet Things. */
 	public static final int		MAGIC_REVEAL_INVISIBILITY = 1;
 
 	/** * Indicates the power to teleport the player randomly within the world. */
 	public static final int		MAGIC_TELEPORTATION = 2;
 
 	/** * Indicates the power to locate all trasure chests, regardless of location. */
 	public static final int		MAGIC_DETECT_TREASURE = 3;
 
 	/** * Indicates the poiwer to location all magic castles, reguardless of location */
 	public static final int		MAGIC_DETECT_MAGIC_CASTLE = 4;
 
 	/** * Indicates the poiwer to location all magic castles, reguardless of location */
 	public static final int		MAGIC_DETECT_QUEST_MONSTER = 5;
 	
 	/**
 	 * Creates a new MagicItem with the given power.
 	 *
 	 * @param	aName		the name of this magic item
 	 * @param	power		the integer thhat represents this item's magic power.
 	 */ 
 	public MagicItem(String aName, int power)
 	{
 		super(aName);
 		mPower = power;
 	}
 	
 	/**
 	 * Returns a string-based representation of this item.
 	 *
 	 * @return		a string-based representation of this item.
 	 */
 	public String toString()
 	{
 		return getName();
 	}
 	
 	/**
 	 * Informs this magic item that a Player is starting to use it.
 	 *
 	 * @param		aPlayer					a non-null player using this item
 	 * @param		aWorld					a world in which the use takes places
 	 * @exception	JCavernInternalError	could not use the item.
 	 */
 	public void startUseBy(Player aPlayer, World aWorld) throws JCavernInternalError
 	{
		aWorld.eventHappened(new WorldEvent(aPlayer, WorldEvent.INFO_MESSAGE, "you start using " + getName()));
 		
 		switch (mPower)
 		{
 			case MAGIC_NOTHING:
 					aWorld.eventHappened(new WorldEvent(aPlayer, WorldEvent.INFO_MESSAGE, getName() + ": nothing happens")); break;
 					
 			case MAGIC_REVEAL_INVISIBILITY:
 					aWorld.eventHappened(new WorldEvent(aPlayer, WorldEvent.INFO_MESSAGE, getName() + ": reveal invisibility"));
 					doRevealInvisibility(aPlayer, aWorld);
 					break;
 					
 			case MAGIC_TELEPORTATION:
 					aWorld.eventHappened(new WorldEvent(aPlayer, WorldEvent.INFO_MESSAGE, getName() + ": teleportation")); 
 					doTeleportation(aPlayer, aWorld); break;
 					
 			case MAGIC_DETECT_TREASURE:
 					aWorld.eventHappened(new WorldEvent(aPlayer, WorldEvent.INFO_MESSAGE, getName() + ": detect treasure")); 
 					doDetectTreasure(aPlayer, aWorld); break;
 					
 			case MAGIC_DETECT_MAGIC_CASTLE:
 					aWorld.eventHappened(new WorldEvent(aPlayer, WorldEvent.INFO_MESSAGE, getName() + ": detect magic castle")); 
 					doDetectMagicCastle(aPlayer, aWorld); break;
 					
 			case MAGIC_DETECT_QUEST_MONSTER:
 					aWorld.eventHappened(new WorldEvent(aPlayer, WorldEvent.INFO_MESSAGE, getName() + ": detect quest monster")); 
 					doDetectQuestMonster(aPlayer, aWorld); break;
 			
 			default:
 					throw new JCavernInternalError("MagicItem.startUseBy(), known power");
 		}
 	}
 	
 	/**
 	 * Informs the magic item that the Player is stopping use of the item.
 	 *
 	 * @param	aPlayer 	a non-null Player who is ceasing use of this item
 	 */
 	public void stopUseBy(Player aPlayer)
 	{
 	}
 
 	/**
 	 * Returns a clone of this magic item.
 	 *
 	 * @return		a clone of this magic item.
 	 */
 	public Object clone()
 	{
 		return new MagicItem(new String(getName()), mPower);
 	}
 		
 	/**
 	 * Returns the magic power code for this Item.
 	 *
 	 * @return		the magic power code for this Item.
 	 */
 	public int getPower()
 	{
 		return mPower;
 	}
 	
 	/**
 	 * Removes the Thing from the world and places it randomly.
 	 *
 	 * @param		aThing					a non-null Thing to teleport
 	 * @param		aWorld					the non-null World in which the teleportation will happen
 	 * @exception	JCavernInternalError	could not teleport
 	 */
 	private void doTeleportation(Thing aThing, World aWorld) throws JCavernInternalError
 	{
 		try
 		{
 			aWorld.remove(aThing);
 			aWorld.place(aWorld.getRandomEmptyLocation(), aThing);
 		}
 		catch (ThingCollisionException tce)
 		{
 			throw new JCavernInternalError("can't teleport");
 		}
 	}
 	
 	/**
 	 * Reveals invisible Things
 	 *
 	 * @param		aPlayer					the non-null Player using the item
 	 * @param		aWorld					the non-null World in which magic castles may be located
 	 */
 	private void doRevealInvisibility(Player aPlayer, World aWorld)
 	{
 		Vector	theThings = aWorld.getThings();
 		
 		for (int index = 0; index < theThings.size(); index++)
 		{
 			Thing	detectee = (Thing) theThings.elementAt(index);
 
 			if (detectee.getInvisible())
 			{
 				aWorld.eventHappened(new WorldEvent(aPlayer, WorldEvent.INFO_MESSAGE, detectee, "There is an invisible " + detectee.getName()));
 				detectee.setInvisible(false);
 				aWorld.eventHappened(WorldContentsEvent.revealed(detectee, aPlayer));
 			}
 		}
 	}
 	
 	/**
 	 * Detects the given kind of Thing anywhere in the World.
 	 *
 	 * @param		seeker					the non-null Player using the item
 	 * @param		aWorld					the non-null World in which magic castles may be located
 	 * @param		aPrototype				a non-null Thing representing the kind of Thing to detect
 	 * @exception	JCavernInternalError	could not detect magic castles
 	 */
 	private void doDetectThings(Thing seeker, World aWorld, Thing aPrototype) throws JCavernInternalError
 	{
 		Vector	theThings = aWorld.getThings(aPrototype);
 		
 		for (int index = 0; index < theThings.size(); index++)
 		{
 			Thing	detectee = (Thing) theThings.elementAt(index);
 			int		direction = aWorld.getDirectionToward(seeker, detectee);
 			
 			aWorld.eventHappened(new WorldEvent(
 					seeker,
 					WorldEvent.INFO_MESSAGE,
 					detectee,
 					"There is a " +
					detectee.getName() + " " +
 					aWorld.getDistanceBetween(seeker, detectee) + " moves " + 
 					Location.directionToString(direction) + " of " + seeker.getName()));
 		}
 	}
 	
 	/**
 	 * Detects treasure anywhere in the World.
 	 *
 	 * @param		seeker					the non-null Player using the item
 	 * @param		aWorld					the non-null World in which treasure may be located
 	 * @exception	JCavernInternalError	could not detect treasure
 	 */
 	private void doDetectTreasure(Thing seeker, World aWorld) throws JCavernInternalError
 	{
 		doDetectThings(seeker, aWorld, new TreasureChest(null, 0));
 	}
 	
 	/**
 	 * Detects magic castles anywhere in the World.
 	 *
 	 * @param		seeker					the non-null Player using the item
 	 * @param		aWorld					the non-null World in which magic castles may be located
 	 * @exception	JCavernInternalError	could not detect magic castles
 	 */
 	private void doDetectMagicCastle(Thing seeker, World aWorld) throws JCavernInternalError
 	{
 		doDetectThings(seeker, aWorld, new Castle());
 	}
 
 	/**
 	 * Detects quest monsters anywhere in the World.
 	 *
 	 * @param		seeker					the non-null Player using the item
 	 * @param		aWorld					the non-null World in which magic castles may be located
 	 * @exception	JCavernInternalError	could not detect magic castles
 	 */
 	private void doDetectQuestMonster(Player seeker, World aWorld) throws JCavernInternalError
 	{
 		doDetectThings(seeker, aWorld, seeker.getMission().getTarget());
 	}
 }
