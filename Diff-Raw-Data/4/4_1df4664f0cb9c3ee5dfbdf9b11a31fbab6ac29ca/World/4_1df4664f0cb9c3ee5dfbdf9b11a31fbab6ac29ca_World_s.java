 /* 
 	JCavernApplet.java
 
 	Title:			JCavern And Glen
 	Author:			Bill Walker
 	Description:	
 */
 
 package jcavern;
 
 import jcavern.ui.*;
 import jcavern.thing.*;
 
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
 	private static final Rectangle	kBounds = new Rectangle(20, 20);
 	
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
 	
 	/**
 	 * Populate this world in a manner appropriate to the given player.
 	 *
 	 * @param		aPlayer					the Player for whom the World is being created
 	 * @exception	JCavernInternalError	the World could not be created.
 	 */
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
 		
 			// Mark the beginning of the mission
 			eventHappened(new WorldEvent(aPlayer, WorldEvent.TURN_START, "Begin mission"));
 
 			// Put the player in the world
 			place(getRandomEmptyLocation(), aPlayer);
 
 			// Display the opening info messages			
 			eventHappened(new WorldEvent(aPlayer, WorldEvent.INFO_MESSAGE, "your mission is " + aPlayer.getMission()));
 			eventHappened(new WorldEvent(aPlayer, WorldEvent.INFO_MESSAGE, "you can seek safety in " + castles + " magic castles"));
 		}
 		catch (ThingCollisionException tce)
 		{
 			throw new JCavernInternalError("trouble creating a world " + tce);
 		}
 	}
 	
 	/**
 	 * Places random trees using the default fraction.
 	 *
 	 * @return								how many trees were placed
 	 * @exception	JCavernInternalError	the trees could not be added.
 	 */
 	public int placeRandomTrees() throws JCavernInternalError
 	{
 		return placeRandom(new Tree(), kTreeFraction);
 	}
 	
 	/**
 	 * Places random castles using the default fraction.
 	 *
 	 * @return								how many castles were placed
 	 * @exception	JCavernInternalError	the castles could not be added.
 	 */
 	public int placeRandomCastles() throws JCavernInternalError
 	{
 		return placeRandom(new Castle(), kCastleFraction);
 	}
 	
 	/**
 	 * Places random TreasureChests. The number of TreasureChests is based on the
 	 * number of monsters to be killed in the given Players mission quota.
 	 *
 	 * @param		aPlayer					a non-null Player who may find the Chests
 	 * @return								how many treasure chests were placed
 	 * @exception	JCavernInternalError	the Treasure chests could not be added.
 	 */
 	public int placeRandomTreasureChests(Player aPlayer) throws JCavernInternalError
 	{
 		int chestCount = aPlayer.getMission().getQuota() / 2;
 
 		try
 		{
 			for (int index = 0; index < chestCount; index++)
 			{
 				place(getRandomEmptyLocation(), TreasureChest.createRandom());
 			}
 		}
 		catch(ThingCollisionException tce)
 		{
 			throw new JCavernInternalError("Can't place random treasure chests " + tce);
 		}
 		
 		return chestCount;
 	}
 	
 	/**
 	 * Places Things randomly according to the fraction passed in.
 	 *
 	 * @param		aThingPrototype				a non-null Thing to be cloned and placed.
 	 * @param		fraction					what fraction of the squares in the world should be covered
 	 * @return									how many things were placed
 	 * @exception	JCavernInternalError		the Things could not be added.
 	 */
 	public int placeRandom(Thing aThingPrototype, double fraction) throws JCavernInternalError
 	{
 		int	numberOfThings = (int) (getBounds().width * getBounds().height * fraction);
 		
 		placeRandom(aThingPrototype, numberOfThings);
 		
 		return numberOfThings;
 	}
 	
 	/**
 	 * Places a given number of Things according to the fraction passed in.
 	 *
 	 * @param		aThingPrototype				a non-null Thing to be cloned and placed.
 	 * @param		numberOfThings				how many squares in the world should be covered
 	 * @exception	JCavernInternalError		the Things could not be added.
 	 */
 	public void placeRandom(Thing aThingPrototype, int numberOfThings) throws JCavernInternalError
 	{
 		try
 		{
 			for (int index = 0; index < numberOfThings; index++)
 			{
 				place(getRandomEmptyLocation(), (Thing) aThingPrototype.clone());
 			}
 		}
 		catch(ThingCollisionException tce)
 		{
 			throw new JCavernInternalError("Can't place random things, " + tce);
 		}
 	}
 	
 	/**
 	 * Places appropriate opponents on the board, based on the prowess of the given player.
 	 *
 	 * @param		aPlayer						for whom the opponents should be worthy
 	 * @param		numberOfMonsters			how many squares in the world should be covered
 	 * @exception	JCavernInternalError		the Monsters could not be added.
 	 */
 	public void placeWorthyOpponents(Player aPlayer, int numberOfMonsters) throws JCavernInternalError
 	{
 		try
 		{		
 			for (int index = 0; index < numberOfMonsters; index++)
 			{
 				place(getRandomEmptyLocation(), MonsterFactory.getWorthyOpponent(aPlayer));
 			}
 		}
 		catch(ThingCollisionException tce)
 		{
 			throw new JCavernInternalError("Can't place random things, " + tce);
 		}
 	}
 	
 	/**
 	 * Returns a random location within the bounds of this world.
 	 *
 	 * @return		a random location within the bounds of this world.
 	 */
 	public Location getRandomLocation()
 	{
 		int		x = (int) (Math.random() * kBounds.width);
 		int		y = (int) (Math.random() * kBounds.height);
 		
 		return new Location(x, y);
 	}
 	
 	/**
 	 * Returns a random, empty location within the bounds of this world.
 	 *
 	 * @return		a random, empty location within the bounds of this world.
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
 	 * Informs the World that an event happened.
 	 *
 	 * @param	anEvent		a non-null WorldEvent describing what happened.
 	 */
 	public void eventHappened(WorldEvent anEvent)
 	{
 		setChanged();
 		notifyObservers(anEvent);
 	}
 
 	/**
 	 * Returns the bounds of this world.
 	 *
 	 * @return	a non-null Rectangle describing the bounds of this world.
 	 */
	private Rectangle getBounds()
 	{
 		return kBounds;
 	}
 	
 	/**
 	 * Transforms the given location into one that observes an inset from the bounds of this World
 	 *
 	 * @param	aLocation	a non-null Location
 	 * @param	inset		an inset from the bounds of this world
 	 * @return				a non-null Location that observes an inset from the bounds of this World
 	 */
 	public Location enforceMinimumInset(Location aLocation, int inset)
 	{
 		return aLocation.enforceMinimumInset(getBounds(), inset);
 	}
 	
 	/**
 	 * Returns whether the given Location is within the bounds of this world
 	 *
 	 * @param	aLocation	a non-null Location
 	 * @return				<CODE>true</CODE> if the given Location is within the bounds of this world, <CODE>false</CODE> otherwise.
 	 */
 	public boolean inBounds(Location aLocation)
 	{
 		return aLocation.inBounds(getBounds());
 	}
 	
 	/**
 	 * Returns the Thing in the given direction from the given Thing.
 	 * The seearch proceeds outward from the given Thing, until it encounters another Thing,
 	 * or the edge of the world.
 	 *
 	 * @param		attacker					a non-null Thing
 	 * @param		aDirection					one of the direction codes defined in class Location
 	 * @return									a non-null Thing in the given direction from the first Thing
 	 * @exception	IllegalLocationException	there's no thing in that direction
 	 * @exception	JCavernInternalError		could not retrieve the Thing
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
 			if (! inBounds(attackeeLocation))
 			{
 				throw new IllegalLocationException("Ranged attack hit nothing");
 			}
 			
 			eventHappened(new WorldEvent(attackeeLocation, attacker, WorldEvent.RANGED_ATTACK, attacker, "*"));
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
 	
 	/**
 	 * Returns the first Thing found in an adjacent Location that matches the prototype.
 	 *
 	 * @param		aLocation					a non-null Location
 	 * @param		aPrototype					an examplar of the kind of Thing to look for
 	 * @return									a matching, non-null Thing adjacent to the first Thing
 	 * @exception	JCavernInternalError		could not retrieve the Thing
 	 */
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
 	 *
 	 * @param		aThing						a non-null Thing in this world
 	 * @param		direction					one of the direction codes in class Location
 	 * @exception	ThingCollisionException		there's already in a thing in the destination location
 	 * @exception	JCavernInternalError		the given Thing is not of this world
 	 * @exception	IllegalLocationException	tried to move off the edge of the world
 	 */
 	public void move(Combatant aThing, int direction) throws ThingCollisionException, JCavernInternalError, IllegalLocationException
 	{
 		if (! mThingsToLocations.containsKey(aThing))
 		{
 			throw new JCavernInternalError("There's no " + aThing + " in this world to move");
 		}
 		
 		Location	oldLocation = (Location) mThingsToLocations.get(aThing);
 		Location	newLocation = oldLocation.getNeighbor(direction);
 
 		if (! inBounds(newLocation))
 		{
 			throw new IllegalLocationException(aThing + " moved outside the world");
 		}
 		
 		if (isEmpty(newLocation)) // no collision on move
 		{
 			privateRemove(aThing);
 			privatePlace(newLocation, aThing);
 			eventHappened(WorldContentsEvent.moved(oldLocation, aThing));		
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
 	 *
 	 * @param		aThing					an origin
 	 * @param		anotherThing			a destination
 	 * @return								a direction code pointing from the origin to the destination
 	 * @exception	JCavernInternalError	the origin or destination are not of this world
 	 */
 	public int getDirectionToward(Thing aThing, Thing anotherThing) throws JCavernInternalError
 	{
 		if (! mThingsToLocations.containsKey(aThing))
 		{
 			throw new JCavernInternalError("There's no " + aThing + " in this world");
 		}
 		
 		if (! mThingsToLocations.containsKey(anotherThing))
 		{
 			throw new JCavernInternalError("There's no " + anotherThing + " in this world");
 		}
 		
 		Location	location1 = getLocation(aThing);
 		Location	location2 = getLocation(anotherThing);
 
 		return location1.getDirectionToward(location2);
 	}
 	
 	/**
 	 * Finds the distance between two things.
 	 *
 	 * @param		aThing					an origin
 	 * @param		anotherThing			a destination
 	 * @return								a distance from the origin to the destination
 	 * @exception	JCavernInternalError	the origin or destination are not of this world
 	 */
 	public int getDistanceBetween(Thing aThing, Thing anotherThing) throws JCavernInternalError
 	{
 		if (! mThingsToLocations.containsKey(aThing))
 		{
 			throw new JCavernInternalError("There's no " + aThing + " in this world");
 		}
 		
 		if (! mThingsToLocations.containsKey(anotherThing))
 		{
 			throw new JCavernInternalError("There's no " + anotherThing + " in this world");
 		}
 		
 		Location	location1 = getLocation(aThing);
 		Location	location2 = getLocation(anotherThing);
 
 		return location1.distanceTo(location2);	
 	}
 	
 	/**
 	 * Retrieves the thing at the given location.
 	 *
 	 * @param		aLocation					a non-null Location
 	 * @return									the non-null thing at that location
 	 * @exception	EmptyLocationException		the given location is empty
 	 * @exception	IllegalLocationException	the given location is invalid
 	 */
 	public Thing getThing(Location aLocation) throws EmptyLocationException, IllegalLocationException
 	{
 		if (mLocationsToThings.containsKey(aLocation))
 		{
 			return (Thing) mLocationsToThings.get(aLocation);
 		}
 		else if (! inBounds(aLocation))
 		{
 			throw new IllegalLocationException(aLocation + " is not in bounds");
 		}
 		else
 		{
 			throw new EmptyLocationException("There's nothing at " + aLocation);
 		}
 	}
 	
 	/**
 	 * Returns the first Player found in this World.
 	 *
 	 * @return								the first Player found in this World.
 	 * @exception	JCavernInternalError	there's no Player in this World
 	 */
 	public Player getPlayer() throws JCavernInternalError
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
 		
 		throw new JCavernInternalError("Can't find Player in world");
 	}
 	
 	/**
 	 * Performs one turn by giving every Thing in the world a chance to
 	 * do something.
 	 *
 	 * @exception	JCavernInternalError	could not perform turn operations
 	 */
 	public void doTurn() throws JCavernInternalError
 	{
 		Enumeration theThings = mThingsToLocations.keys();
 
 		while (theThings.hasMoreElements())
 		{
 			((Thing) theThings.nextElement()).doTurn(this);
 		}
 	}
 	
 	/**
 	 * Returns all the Things matching the given prototype.
 	 *
 	 * @param	aPrototype		a non-null prototype of the thing to look for
 	 * @return					all the matching things
 	 */
 	public Vector getThings(Thing aPrototype)
 	{
 		Enumeration	theThings = mThingsToLocations.keys();
 		Vector		specialThings = new Vector();
 		Class		aClass = aPrototype.getClass();
 		
 		while (theThings.hasMoreElements())
 		{
 			Object aThing = theThings.nextElement();
 			
 			if (aThing.getClass() == aPrototype.getClass())
 			{
 				specialThings.addElement(aThing);
 			}
 		}
 
 		return specialThings;
 	}
 	
 	/**
 	 * Returns all the Things in the World.
 	 *
 	 * @return		all the Things
 	 */
 	public Vector getThings()
 	{
 		Enumeration	theThings = mThingsToLocations.keys();
 		Vector		allThings = new Vector();
 		
 		while (theThings.hasMoreElements())
 		{
 			allThings.addElement(theThings.nextElement());
 		}
 
 		return allThings;
 	}
 	
 	/**
 	 * Answers whether there is any thing at the given location.
 	 *
 	 * @param	aLocation	a non-null Location
 	 * @return				<CODE>true</CODE> if the location is emtpy, <CODE>false</CODE> otherwise
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
 	 * Retrieves the location of the given thing.
 	 *
 	 * @param		aThing					a non-null Thing
 	 * @return								a non-null Location for the given Thing
 	 * @exception	JCavernInternalError	could not find the Thing
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
 	 * Places a Thing at the given location and sends a WorldContentsEvent.
 	 *
 	 * @param		aLocation					a non-null Location
 	 * @param		aThing						a non-null Thing to place at that location
 	 * @exception	ThingCollisionException		there's already a Thing at that location
 	 * @exception	JCavernInternalError		could not place the Thing.
 	 */
 	public void place(Location aLocation, Thing aThing) throws ThingCollisionException, JCavernInternalError
 	{
 		privatePlace(aLocation, aThing);
 		addObserver(aThing.getGraphicalThingView());
 		eventHappened(WorldContentsEvent.placed(aLocation, aThing));
 	}
 	
 	/**
 	 * Places a Thing at the given location.
 	 *
 	 * @param		aLocation					a non-null Location
 	 * @param		aThing						a non-null Thing to place at that location
 	 * @exception	ThingCollisionException		there's already a Thing at that location
 	 * @exception	JCavernInternalError		could not place the Thing.
 	 */
 	private void privatePlace(Location aLocation, Thing aThing) throws ThingCollisionException, JCavernInternalError
 	{
 		if (mLocationsToThings.containsKey(aLocation))
 		{
 			throw new ThingCollisionException(aThing, "There's already " + aThing + " at " + aLocation);
 		}
 		
 		mLocationsToThings.put(aLocation, aThing);
 		mThingsToLocations.put(aThing, aLocation);
 		
 		aThing.thingPlaced(this, aLocation);
 	}
 	
 
 	/**
 	 * Remove the given thing from the world and send a WorldContentsEvent.
 	 *
 	 * @param		thingToRemove				a non-null Thing to remove
 	 * @exception	JCavernInternalError		could not remove the Thing.
 	 */
 	public void remove(Thing thingToRemove) throws JCavernInternalError
 	{
 		Location locationToRemove = privateRemove(thingToRemove);
 		deleteObserver(thingToRemove.getGraphicalThingView());
 		eventHappened(WorldContentsEvent.removed(locationToRemove, thingToRemove));
 	}
 
 	/**
 	 * Remove the given thing from the world. Also used for moving things.
 	 *
 	 * @param		thingToRemove				a non-null Thing to remove
 	 * @exception	JCavernInternalError		could not remove the Thing.
 	 */
 	private Location privateRemove(Thing thingToRemove) throws JCavernInternalError
 	{
 		Location	locationToRemove = (Location) mThingsToLocations.get(thingToRemove);
 		
 		if (locationToRemove == null)
 		{
 			throw new JCavernInternalError("There's no " + thingToRemove + " in this world to remove");
 		}
 		
 		mLocationsToThings.remove(locationToRemove);
 		mThingsToLocations.remove(thingToRemove);
 		
 		thingToRemove.thingRemoved(this, locationToRemove);
 		
 		return locationToRemove;
 	}
 }
