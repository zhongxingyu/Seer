 /* 
 	Treasure.java
 
 	Title:			JCavern And Glen
 	Author:			Bill Walker
 	Description:	
 */
 
 package jcavern;
 
 import jcavern.ui.*;
 import jcavern.thing.*;
 import java.util.*;
 import java.net.*;
 import java.io.*;
 
 /**
  * Treasures (including Swords, Armour, and MagicItems) are found in TreasureChests, and are
  * picked up and used by Players.
  *
  * @author	Bill Walker
  * @version	$Id$
  */
 public abstract class Treasure implements Cloneable, Serializable
 {
 	/** * The global list of all the possible Treasures. */
 	private static Vector	gTreasures;
 	
 	/** * The name of this Treasure. */
 	private String			mName;
 	
 	/**
 	 * Creates a new treasure item.
 	 *
 	 * @param	inName		non-null String naming the treasure item
 	 */
 	public Treasure(String inName)
 	{
 		mName = inName;	
 	}
 	
 	/**
 	 * Records the fact that this item is now in use by a particular player.
 	 *
 	 * @param		inPlayer				a non-null Player using the item
 	 * @param		inWorld					a non-null World in which the item is being used
 	 * @exception	JCavernInternalError	Could not locate the Player in the World
 	 */
 	public void startUseBy(Player inPlayer, World inWorld) throws JCavernInternalError
 	{
 		inPlayer.startUsing(this);
 		inWorld.eventHappened(new WorldEvent(inPlayer, WorldEvent.USE_INVENTORY, inPlayer.getName() + " begins using " + getName()));
 	}
 	
 	/**
 	 * Records the fact that this item is not in use by a particular player.
 	 *
 	 * @param	inPlayer	a non-null Player no longer using the item
 	 * @param	inWorld		a non-null World in which the item is not being used
 	 */
 	public void stopUseBy(Player inPlayer, World inWorld)
 	{
 		inPlayer.stopUsing(this);
 		inWorld.eventHappened(new WorldEvent(inPlayer, WorldEvent.USE_INVENTORY, inPlayer.getName() + " stops using " + getName()));
 	}
 	
 	/**
 	 * Implements the cloning interface.
 	 *
 	 * @return	a clone of this treasure item.
 	 */
 	public abstract Object clone();
 	
 	/**
 	 * Returns the name of this treasure item.
 	 *
 	 * @return		the non-null String name of this item.
 	 */
 	public String getName()
 	{
 		return mName;
 	}
 	
 	/**
 	 * Returns a random Treasure.
 	 *
 	 * @return	a random Treasure, one of the items specified in <CODE>treasure.dat</CODE>.
 	 */
 	public static Treasure getRandom()
 	{
 		int randomIndex = (int) (Math.random() * gTreasures.size());
 		
 		return (Treasure) ((Treasure) gTreasures.elementAt(randomIndex)).clone();
 	}
 
 	/**
 	 * Load the prototypical treasure items from the treasures.dat file.
 	 *
 	 * @param	aURL	a non-null URL from which to load <CODE>treasures.dat</CODE>
 	 */
 	public static void loadPrototypes(URL aURL)
 	{
		System.out.println("loadPrototypes(" + aURL + ")");
 		
 		gTreasures = new Vector();
 		
 		try
 		{
 			//InputStream		aStream = MonsterFactory.class.getResourceAsStream("/treasure.dat");
 			InputStream		aStream = aURL.openStream();
 			BufferedReader	aReader = new BufferedReader(new InputStreamReader(aStream));
 			String			aMessageLine;
 
 			while ((aMessageLine = aReader.readLine()) != null)
 			{
 				String				aType = aMessageLine.substring(0,1).trim();
 				String				aName = aMessageLine.substring(2,16).trim();
 				Integer				aValue = Integer.valueOf(aMessageLine.substring(16).trim());
 				
 				if (aType.equals("s"))
 				{
 					gTreasures.addElement(new Sword(aName, aValue.intValue()));
 				}
 				else if (aType.equals("a"))
 				{
 					gTreasures.addElement(new Armour(aName, aValue.intValue()));
 				}
 				else
 				{
 					gTreasures.addElement(new MagicItem(aName, aValue.intValue()));
 				}
 			}
 		}
 		catch (MalformedURLException mue)
 		{
			System.out.println("IO Error reading monsters " + mue);
 		}
 		catch (IOException ioe)
 		{
			System.out.println("IO Error reading monsters " + ioe);
 		}
 
 		System.out.println("STOP " + gTreasures);
 	}
 }
