 /* 
 	JCavernApplet.java
 
 	Title:			JCavern And Glen
 	Author:			Bill Walker
 	Description:	
 */
 
 package jcavern;
 
 import java.util.*;
 import java.awt.Rectangle;
 
 /**
  * a World is a rectangular grid of Locations, some of which contain Things.
  *
  * @author	Bill Walker
  * @version	$Id$
  */
 public class World extends Observable
 {
 	/** * The usual fraction of trees in the world. */
 	private static final double		kTreeFraction = 17.0 / 100.0;
 	
 	/** * The usual fraction of castles in the world. */
 	private static final double		kCastleFraction = 0.4 / 100.0;
 	
 	/** * The bounds of the world */
 	private static final Rectangle	kBounds = new Rectangle(21, 21);
 	
 	/** * A dictionary mapping from locations to things */
 	private Hashtable			mLocationsToThings;
 	
 	/** * A dictionary mapping from things to their locations */
 	private Hashtable			mThingsToLocations;
 	
 	/**
 	 * Creates a new, empty world.
 	 */
 	public World()
 	{
 		removeAll();
 	}
 	
 	/**
 	 * Empties out the dictionaries that map things to locations and locations to things.
 	 */
 	public void removeAll()
 	{
 		mLocationsToThings = new Hashtable();
 		mThingsToLocations = new Hashtable();
 	}
 	
 	public void populateFor(Player aPlayer) throws JCavernInternalError
 	{
 		try
 		{
 			// Remove all the stuff from the World
 			removeAll();
 			
 			// Get rid of that pesky castle the player is probably carrying
 			aPlayer.setCastle(null);
 			
 			// Place trees
 			placeRandomTrees();
 			
 			// Place castles
 			int castles = placeRandomCastles();
 			
 			// Place treasure chests
 			placeRandomTreasureChests(aPlayer);
 			
 			// Place monsters
 			// orignally: Num_Monster := 3*Mis_quota + 2*Random(Mis_Quota);
 			int quota = aPlayer.getMission().getQuota();			
 			int desiredPopulation = (int) (3 * quota + 2 * Math.random() * quota);
 			
 			placeRandom(aPlayer.getMission().getTarget(), quota);
 			placeWorthyOpponents(aPlayer, desiredPopulation - quota);
 		
 			// Put the player in the world
 			place(getRandomEmptyLocation(), aPlayer);
 			
 			JCavernApplet.log("You will have " + castles + " magic castles to help you");
 		}
 		catch (ThingCollisionException tce)
 		{
 			throw new JCavernInternalError("trouble creating a world " + tce);
 		}
 	}
 	
 	/**
 	 * Places random trees using the default fraction.
 	 */
 	public int placeRandomTrees() throws ThingCollisionException
 	{
 		return placeRandom(new Tree(), kTreeFraction);
 	}
 	
 	/**
 	 * Places random trees using the default fraction.
 	 */
 	public int placeRandomCastles() throws ThingCollisionException
 	{
 		return placeRandom(new Castle(), kCastleFraction);
 	}
 	
 	/**
 	 * Places random TreasureChests. The number of TreasureChests is based on the
 	 * number of monsters to be killed in the given Players mission quota.
 	 */
 	public int placeRandomTreasureChests(Player aPlayer) throws ThingCollisionException
 	{
 		int chestCount = aPlayer.getMission().getQuota() / 2;
 
 		System.out.println("Place " + chestCount + " Random Treasure Chests");
 		
 		for (int index = 0; index < chestCount; index++)
 		{
 			place(getRandomEmptyLocation(), TreasureChest.createRandom());
 		}
 		
 		return chestCount;
 	}
 	
 	/**
 	 * Places random trees according to the fraction passed in.
 	 */
 	public int placeRandom(Thing aThingPrototype, double fraction) throws ThingCollisionException
 	{
 		int	numberOfThings = (int) (getBounds().width * getBounds().height * fraction);
 		
 		System.out.println("Place fraction " + fraction + " Random " + aThingPrototype);
 		
 		placeRandom(aThingPrototype, numberOfThings);
 		
 		return numberOfThings;
 	}
 	
 	/**
 	 * Places random trees according to the fraction passed in.
 	 */
 	public void placeRandom(Thing aThingPrototype, int numberOfThings) throws ThingCollisionException
 	{
 		System.out.println("Place " + numberOfThings + " Random " + aThingPrototype);
 		
 		for (int index = 0; index < numberOfThings; index++)
 		{
 			place(getRandomEmptyLocation(), (Thing) aThingPrototype.clone());
 		}
 	}
 	
 	/**
 	 * Places appropriate opponents on the board, based on the prowess of the given player.
 	 */
 	public void placeWorthyOpponents(Player aPlayer, int numberOfMonsters) throws ThingCollisionException
 	{
		System.out.println("Place " + numberOfMonsters + " Worthy Opponents");
 		
 		for (int index = 0; index < numberOfMonsters; index++)
 		{
 			place(getRandomEmptyLocation(), MonsterFactory.getWorthyOpponent(aPlayer));
 		}
 	}
 	
 	/**
 	 * Returns a random location within the bounds of this world.
 	 */
 	public Location getRandomLocation()
 	{
 		int		x = (int) (Math.random() * kBounds.width);
 		int		y = (int) (Math.random() * kBounds.height);
 		
 		return new Location(x, y);
 	}
 	
 	/**
 	 * Returns a random, empty location within the bounds of this world.
 	 */
 	public Location getRandomEmptyLocation()
 	{
 		Location emptyLocation = getRandomLocation();
 		
 		while (! isEmpty(emptyLocation))
 		{
 			emptyLocation = getRandomLocation();
 		}
 		
 		return emptyLocation;
 	}
 
 	/**
 	 * Returns the bounds of this world.
 	 */
 	public Rectangle getBounds()
 	{
 		return kBounds;
 	}
 	
 	/**
 	 * Returns the Thing in the given direction from the given Thing.
 	 * The seearch proceeds outward from the given Thing, until it encounters another Thing,
 	 * or the edge of the world.
 	 */
 	public Thing getThingToward(Thing attacker, int aDirection) throws JCavernInternalError, IllegalLocationException
 	{
 		//System.out.println("attack(" + attacker + ", " + Location.directionToString(aDirection) + ")");
 		
 		if (! mThingsToLocations.containsKey(attacker))
 		{
 			throw new JCavernInternalError("There's no " + attacker + " to attack");
 		}
 		
 		Location	attackerLocation = (Location) mThingsToLocations.get(attacker);
 		Location	attackeeLocation = attackerLocation.getNeighbor(aDirection);
 		
 		while (isEmpty(attackeeLocation))
 		{
 			if (! attackeeLocation.inBounds(kBounds))
 			{
 				throw new IllegalLocationException("Ranged attack hit nothing");
 			}
 			
 			JCavernApplet.log(attackeeLocation.toString());
 			attackeeLocation = attackeeLocation.getNeighbor(aDirection);
 		}
 		
 		try
 		{
 			return getThing(attackeeLocation);
 		}
 		catch (EmptyLocationException ele)
 		{
 			throw new JCavernInternalError("World says location isn't empty, but throws EmptyLocationException");
 		}
 	}
 	
 	public Thing getNeighboring(Location aLocation, Thing aPrototype) throws JCavernInternalError
 	{
 		try
 		{
 			Vector neighbors = aLocation.getNeighbors();
 			
 			for (int index = 0; index < neighbors.size(); index++)
 			{
 				try
 				{
 					Location neighbor = (Location) neighbors.elementAt(index);
 				
 					if ((! isEmpty(neighbor)) && (getThing(neighbor).getClass().equals(aPrototype.getClass())))
 					{
 						return getThing(neighbor);
 					}
 				}
 				catch (IllegalLocationException ile)
 				{
 				}
 			}
 		}
 		catch (EmptyLocationException ele)
 		{
 			throw new JCavernInternalError("tried to find neighbors, got unexpected empty location exception " + ele);
 		}
 		
 		throw new IllegalArgumentException("Can't find neighboring " + aPrototype);
 	}
 	
 	/**
 	 * Moves the given thing in the given direction.
 	 */
 	public void move(Combatant aThing, int direction) throws ThingCollisionException, JCavernInternalError, IllegalLocationException
 	{
 		if (! mThingsToLocations.containsKey(aThing))
 		{
 			throw new JCavernInternalError("There's no " + aThing + " in this world to move");
 		}
 		
 		Location	oldLocation = (Location) mThingsToLocations.get(aThing);
 		Location	newLocation = oldLocation.getNeighbor(direction);
 
 		if (! newLocation.inBounds(kBounds))
 		{
 			throw new IllegalLocationException(aThing + " moved outside the world");
 		}
 		
 		if (isEmpty(newLocation)) // no collision on move
 		{
 			remove(oldLocation);
 			place(newLocation, aThing);
 		}
 		else // collision on move, find out what's currently there
 		{
 			try
 			{
 				Thing	currentOccupant = getThing(newLocation);
 		
 				throw new ThingCollisionException(aThing, currentOccupant, aThing.getName() + " collided with " + currentOccupant.getName());
 			}
 			catch (EmptyLocationException ele)
 			{
 				throw new JCavernInternalError("World says location isn't empty, but throws EmptyLocationException!");
 			}
 		}	
 	}
 		
 	/**
 	 * Finds the direction between two things.
 	 */
 	public int directionToward(Thing aThing, Thing anotherThing) throws JCavernInternalError
 	{
 		if (! mThingsToLocations.containsKey(aThing))
 		{
 			throw new JCavernInternalError("There's no " + aThing + " in this world to move");
 		}
 		
 		if (! mThingsToLocations.containsKey(anotherThing))
 		{
 			throw new JCavernInternalError("There's no " + anotherThing + " in this world to move toward");
 		}
 		
 		Location	oldLocation1 = (Location) mThingsToLocations.get(aThing);
 		Location	oldLocation2 = (Location) mThingsToLocations.get(anotherThing);
 
 		return oldLocation1.getDirectionToward(oldLocation2);
 	}
 	
 	/**
 	 * Retrieves the thing at the given location.
 	 */
 	public Thing getThing(Location aLocation) throws EmptyLocationException, IllegalLocationException
 	{
 		if (mLocationsToThings.containsKey(aLocation))
 		{
 			return (Thing) mLocationsToThings.get(aLocation);
 		}
 		else if (! aLocation.inBounds(getBounds()))
 		{
 			throw new IllegalLocationException(aLocation + " is not in bounds");
 		}
 		else
 		{
 			throw new EmptyLocationException("There's nothing at " + aLocation);
 		}
 	}
 	
 	public Player getPlayer()
 	{
 		Enumeration theThings = mThingsToLocations.keys();
 		
 		while (theThings.hasMoreElements())
 		{
 			Thing aThing = (Thing) theThings.nextElement();
 			
 			if (aThing instanceof Player)
 			{
 				return (Player) aThing;
 			}
 		}
 		
 		return null;
 	}
 	
 	public void doTurn() throws JCavernInternalError
 	{
 		Enumeration theThings = mThingsToLocations.keys();
 
 		while (theThings.hasMoreElements())
 		{
 			((Thing) theThings.nextElement()).doTurn(this);
 		}
 	}
 	
 	/**
 	 * Answers whether there is any thing at the given location.
 	 */
 	public boolean isEmpty(Location aLocation)
 	{
 		if (mLocationsToThings.containsKey(aLocation))
 		{
 			return false;
 		}
 		else
 		{
 			return true;
 		}
 	}
 	
 	/**
 	 * Places a Thing at the given location.
 	 */
 	public void place(Location aLocation, Thing aThing) throws ThingCollisionException
 	{
 		if (mLocationsToThings.containsKey(aLocation))
 		{
 			throw new ThingCollisionException(aThing, "There's already " + aThing + " at " + aLocation);
 		}
 		
 		mLocationsToThings.put(aLocation, aThing);
 		mThingsToLocations.put(aThing, aLocation);
 		
 		setChanged();
 		notifyObservers();
 	}
 	
 	/**
 	 * Retrieves the location of the given thing.
 	 */
 	public Location getLocation(Thing aThing) throws JCavernInternalError
 	{
 		if (! mThingsToLocations.containsKey(aThing))
 		{
 			throw new JCavernInternalError("There's no " + aThing + " in this world to locate");
 		}
 		
 		return (Location) mThingsToLocations.get(aThing);
 	}
 	
 	/**
 	 * Remove the thing at the given location from the world.
 	 */
 	public void remove(Location locationToRemove) throws JCavernInternalError
 	{
 		Thing	thingToRemove = (Thing) mLocationsToThings.get(locationToRemove);
 		
 		if (thingToRemove == null)
 		{
 			throw new JCavernInternalError("There's no " + thingToRemove + " in this world to remove");
 		}
 		
 		mLocationsToThings.remove(locationToRemove);
 		mThingsToLocations.remove(thingToRemove);
 
 		setChanged();
 		notifyObservers();
 	}
 	
 	/**
 	 * Remove the given thing from the world.
 	 */
 	public void remove(Thing thingToRemove) throws JCavernInternalError
 	{
 		Location	locationToRemove = (Location) mThingsToLocations.get(thingToRemove);
 		
 		if (locationToRemove == null)
 		{
 			throw new JCavernInternalError("There's no " + thingToRemove + " in this world to remove");
 		}
 		
 		mLocationsToThings.remove(locationToRemove);
 		mThingsToLocations.remove(thingToRemove);
 
 		setChanged();
 		notifyObservers();
 	}
 }
