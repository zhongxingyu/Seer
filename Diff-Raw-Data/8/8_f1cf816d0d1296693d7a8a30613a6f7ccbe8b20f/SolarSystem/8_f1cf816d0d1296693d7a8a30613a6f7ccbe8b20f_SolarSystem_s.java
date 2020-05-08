 package com.evervoid.state;
 
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.LinkedHashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.SortedSet;
 import java.util.TreeSet;
 
 import com.evervoid.json.Json;
 import com.evervoid.json.Jsonable;
 import com.evervoid.state.data.PlanetData;
 import com.evervoid.state.data.RaceData;
 import com.evervoid.state.geometry.Dimension;
 import com.evervoid.state.geometry.GridLocation;
 import com.evervoid.state.geometry.Point;
 import com.evervoid.state.geometry.Point3D;
 import com.evervoid.state.observers.ShipObserver;
 import com.evervoid.state.observers.SolarObserver;
 import com.evervoid.state.player.Player;
 import com.evervoid.state.prop.Planet;
 import com.evervoid.state.prop.Portal;
 import com.evervoid.state.prop.Prop;
 import com.evervoid.state.prop.Ship;
 import com.evervoid.state.prop.ShipPath;
 import com.evervoid.state.prop.Star;
 import com.evervoid.utils.MathUtils;
 
 public class SolarSystem implements EVContainer<Prop>, Jsonable, ShipObserver
 {
 	private final Dimension aDimension;
 	private final Map<Point, Prop> aGrid = new HashMap<Point, Prop>();
 	private final int aID;
 	private final String aName;
 	private final Set<SolarObserver> aObservableSet;
 	private final Point3D aPoint;
 	private final SortedSet<Prop> aProps = new TreeSet<Prop>();
 	private Star aStar;
 	private final EVGameState aState;
 
 	/**
 	 * Default constructor.
 	 * 
 	 * @param size
 	 *            Dimension of the solar system to use.
 	 * @param state
 	 *            Reference to the game state
 	 */
 	SolarSystem(final int id, final Dimension size, final Point3D point, final Star star, final EVGameState state)
 	{
 		aState = state;
 		aObservableSet = new HashSet<SolarObserver>();
 		aID = id;
 		aDimension = size;
 		aPoint = point;
 		aStar = star;
 		state.registerProp(star, this);
 		aName = EVGameState.getRandomSolarSystemName();
 	}
 
 	SolarSystem(final Json j, final EVGameState state)
 	{
 		aState = state;
 		aObservableSet = new HashSet<SolarObserver>();
 		aDimension = new Dimension(j.getAttribute("dimension"));
 		aPoint = Point3D.fromJson(j.getAttribute("point"));
 		aID = j.getIntAttribute("id");
 		aStar = null;
 		aName = j.getStringAttribute("name");
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public boolean addElem(final Prop prop)
 	{
 		final GridLocation loc = prop.getLocation();
 		if (aProps.contains(prop) || !loc.fitsIn(aDimension)) {
 			return false;
 		}
 		aProps.add(prop);
 		for (final Point p : prop.getLocation().getPoints()) {
 			aGrid.put(p, prop);
 		}
 		prop.enterContainer(this);
 		if (prop instanceof Ship) {
 			for (final SolarObserver observer : aObservableSet) {
 				observer.shipEntered((Ship) prop);
 			}
 		}
 		return true;
 	}
 
 	@Override
 	public boolean containsElem(final Prop p)
 	{
 		return aProps.contains(p);
 	}
 
 	public void deregisterObserver(final SolarObserver sObserver)
 	{
 		aObservableSet.remove(sObserver);
 	}
 
 	@Override
 	public Iterable<Prop> elemIterator()
 	{
 		return aProps;
 	}
 
 	@Override
 	public boolean equals(final Object other)
 	{
 		if (other == null || other.getClass() != this.getClass()) {
 			return false;
 		}
 		final SolarSystem o = (SolarSystem) other;
 		return getID() == o.getID();
 	}
 
 	/**
 	 * @return The dimension for of the solar system.
 	 */
 	public Dimension getDimension()
 	{
 		return aDimension;
 	}
 
 	private Set<Point> getEdges()
 	{
 		final Set<Point> aSet = new HashSet<Point>();
 		// bottom and top rows
 		for (int i = 0; i < getWidth(); i++) {
 			aSet.add(new Point(i, 0));
 			aSet.add(new Point(i, getHeight() - 1));
 		}
 		// left and right column
 		for (int i = 0; i < getHeight(); i++) {
 			aSet.add(new Point(0, i));
 			aSet.add(new Point(getWidth() - 1, i));
 		}
 		return aSet;
 	}
 
 	/**
 	 * Finds at least one prop (if there is one) at the given GridLocation. Here for convenience; use getPropsAt for
 	 * completeness
 	 * 
 	 * @param location
 	 *            The location to search at
 	 * @return One prop at the given GridLocation, if any
 	 */
 	public Prop getFirstPropAt(final GridLocation location)
 	{
 		if (location == null) {
 			return null;
 		}
 		for (final Point p : location.getPoints()) {
 			final Prop match = getPropAt(p);
 			if (match != null) {
 				return match;
 			}
 		}
 		return null;
 	}
 
 	/**
 	 * @return The height of the solar system.
 	 */
 	public int getHeight()
 	{
 		return aDimension.getHeight();
 	}
 
 	/**
 	 * @return The "home owner" of this solar system, or the NullPlayer if no regular player owns this solar system
 	 */
 	public Player getHomePlayer()
 	{
 		for (final Player p : aState.getPlayers()) {
 			if (equals(p.getHomeSolarSystem())) {
 				return p;
 			}
 		}
 		return aState.getNullPlayer();
 	}
 
 	@Override
 	public int getID()
 	{
 		return aID;
 	}
 
 	public String getName()
 	{
 		return aName;
 	}
 
 	/**
 	 * Return all direct neighbors of the given gridPoint in which props of dimension size could fit.
 	 * 
 	 * @param gridLocation
 	 *            The location around which we are finding the neighbors
 	 * @param ofSize
 	 *            The dimension of the neighbor locations we should be returning.
 	 * @return a set containing all gridLocation's direct, unoccupied neighbors of dimension "size"
 	 */
 	public Set<GridLocation> getNeighbours(final GridLocation gridLocation, final Dimension size)
 	{
 		final HashSet<GridLocation> neighbourSet = new LinkedHashSet<GridLocation>();
 		final int x = gridLocation.getX();
 		final int y = gridLocation.getY();
 		for (int i = x - size.width; i < x + gridLocation.getWidth() + 1; i++) {
 			for (int j = y + gridLocation.getHeight(); j > y - gridLocation.getHeight() - size.width + 1; j--) {
 				if (i < 0 || j - size.height < 0 || i + size.width >= getWidth() || j >= getHeight()) {
 					continue;
 				}
 				final GridLocation tempLoc = new GridLocation(i, j, size);
 				if (!isOccupied(tempLoc)) {
 					neighbourSet.add(tempLoc);
 				}
 			}
 		}
 		return neighbourSet;
 	}
 
 	public Set<GridLocation> getNeighbours(final Prop prop, final Dimension size)
 	{
 		return getNeighbours(prop.getLocation(), size);
 	}
 
 	/**
 	 * @return The location of this solar system in 3D space
 	 */
 	public Point3D getPoint3D()
 	{
 		return aPoint;
 	}
 
 	/**
 	 * @return The set of all portal props in this solar system
 	 */
 	public Set<Portal> getPortals()
 	{
 		final Set<Portal> portals = new HashSet<Portal>();
 		for (final Prop p : aProps) {
 			if (p instanceof Portal) {
 				portals.add((Portal) p);
 			}
 		}
 		return portals;
 	}
 
 	/**
 	 * Returns a reference to the Portal prop to the given solar system
 	 * 
 	 * @param ss
 	 *            The destination solar system
 	 * @return The Portal prop going to this solar system, or null if there is no such Portal.
 	 */
 	Portal getPortalTo(final SolarSystem ss)
 	{
 		for (final Prop p : aProps) {
 			if (p instanceof Portal && ((Portal) p).connects(ss)) {
 				return (Portal) p;
 			}
 		}
 		return null;
 	}
 
 	/**
 	 * Finds a prop at the given point
 	 * 
 	 * @param point
 	 *            The point to look at
 	 * @return The prop at the given point, or null if the point is free
 	 */
 	public Prop getPropAt(final Point point)
 	{
 		return aGrid.get(point);
 	}
 
 	/**
 	 * Finds the prop(s) at the given GridLocation
 	 * 
 	 * @param location
 	 *            The location to search at
 	 * @return The set of props at the given location
 	 */
 	public Set<Prop> getPropsAt(final GridLocation location)
 	{
 		final Set<Prop> props = new HashSet<Prop>();
 		for (final Point p : location.getPoints()) {
 			final Prop match = getPropAt(p);
 			if (match != null) {
 				props.add(match);
 			}
 		}
 		return props;
 	}
 
 	public int getRadius()
 	{
 		return Math.max(getHeight(), getWidth());
 	}
 
 	/**
 	 * Finds a random vacant GridLocation to put a prop in
 	 * 
 	 * @param dimension
 	 *            The dimension of the prop to fit
 	 * @return The random GridLocation
 	 */
 	public GridLocation getRandomLocation(final Dimension dimension)
 	{
 		return getRandomLocation(dimension, 0);
 	}
 
 	/**
 	 * Finds a random vacant GridLocation to put a prop in
 	 * 
 	 * @param dimension
 	 *            The dimension of the prop to fit
 	 * @param margin
 	 *            Safety margin to avoid
 	 * @return The random GridLocation
 	 */
 	public GridLocation getRandomLocation(final Dimension dimension, final int margin)
 	{
 		GridLocation loc = null;
 		while (loc == null || isOccupied(loc)) {
 			loc = new GridLocation(MathUtils.getRandomIntBetween(margin, aDimension.width - margin),
 					MathUtils.getRandomIntBetween(margin, aDimension.height - margin), dimension).constrain(aDimension);
 		}
 		return loc;
 	}
 
 	public Star getStar()
 	{
 		return aStar;
 	}
 
 	/**
 	 * @return A GridLocation where the sun is located.
 	 */
 	public GridLocation getSunLocation()
 	{
 		return aStar.getLocation();
 	}
 
 	/**
 	 * @return The shadow color of the sun
 	 */
 	public Color getSunShadowColor()
 	{
 		return aStar.getShadowColor();
 	}
 
 	/**
 	 * @return The width of the solar system.
 	 */
 	public int getWidth()
 	{
 		return aDimension.getWidth();
 	}
 
 	public GridLocation getWormholeLocation()
 	{
 		GridLocation tempLocation;
 		final Dimension vertical = new Dimension(1, 4);
 		final Dimension horizontal = new Dimension(4, 1);
 		Point p;
 		do {
 			p = (Point) MathUtils.getRandomElement(getEdges());
 			if (p.x == 0 || p.x == getWidth() - 1) {
 				tempLocation = new GridLocation(p, vertical);
 			}
 			else {
 				tempLocation = new GridLocation(p, horizontal);
 			}
 			tempLocation = tempLocation.constrain(getWidth(), getHeight());
 		}
 		while (isOccupied(tempLocation));
 		return tempLocation;
 	}
 
 	/**
 	 * Finds if there is one or more props at the given GridLocation
 	 * 
 	 * @param location
 	 *            The location to look at
 	 * @return True if there is one or more props at the given location
 	 */
 	public boolean isOccupied(final GridLocation location)
 	{
 		return getFirstPropAt(location) != null;
 	}
 
 	void populate(final Json j)
 	{
 		for (final Json p : j.getListAttribute("props")) {
 			Prop prop = null;
 			if (p.getStringAttribute("proptype").equalsIgnoreCase("planet")) {
 				prop = new Planet(p, aState.getPlanetData(p.getStringAttribute("planettype")), aState);
 			}
 			else if (p.getStringAttribute("proptype").equalsIgnoreCase("ship")) {
 				prop = new Ship(p, aState);
 			}
 			else if (p.getStringAttribute("proptype").equalsIgnoreCase("star")) {
 				prop = new Star(p, aState.getStarData(p.getStringAttribute("startype")), aState);
 				aStar = (Star) prop;
 			}
 			else if (p.getStringAttribute("proptype").equalsIgnoreCase("portal")) {
 				prop = new Portal(p, aState);
 			}
 			if (prop != null) {
 				aState.registerProp(prop, this);
 			}
 		}
 	}
 
 	/**
 	 * Randomly populates this solar system
 	 */
 	void populateRandomly()
 	{
 		final Player owner = getHomePlayer();
 		if (!owner.isNullPlayer()) {
 			// All your lolships are belong to us
 			final RaceData race = owner.getRaceData();
 			for (int i = 0; i < 8; i++) {
 				final String shipType = (String) MathUtils.getRandomElement(race.getShipTypes());
 				final Ship tempElem = new Ship(aState.getNextPropID(), owner, this, getRandomLocation(race
 						.getShipData(shipType).getDimension(), 2), shipType, aState);
 				aState.registerProp(tempElem, this);
 			}
 		}
 		// No one expects the lolplanets inquisition
 		for (int i = 0; i < 3; i++) {
 			final PlanetData randomPlanet = aState.getPlanetData((String) MathUtils.getRandomElement(aState.getPlanetTypes()));
 			final Planet tempElem = new Planet(aState.getNextPropID(), owner,
 					getRandomLocation(randomPlanet.getDimension(), 4), randomPlanet.getType(), aState);
 			tempElem.populateInitialBuildings();
 			aState.registerProp(tempElem, this);
 		}
 		// Sprinkle some extra serving of neutral planets
 		for (int i = 0; i < 10; i++) {
 			final PlanetData randomPlanet = aState.getPlanetData((String) MathUtils.getRandomElement(aState.getPlanetTypes()));
 			final Planet tempElem = new Planet(aState.getNextPropID(), aState.getNullPlayer(), getRandomLocation(
 					randomPlanet.getDimension(), 4), randomPlanet.getType(), aState);
 			aState.registerProp(tempElem, this);
 		}
 	}
 
 	public void registerObserver(final SolarObserver sObserver)
 	{
 		aObservableSet.add(sObserver);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public boolean removeElem(final Prop prop)
 	{
 		if (aProps.contains(prop)) {
 			for (final Point p : prop.getLocation().getPoints()) {
 				aGrid.remove(p);
 			}
 			aProps.remove(prop);
 			if (prop instanceof Ship) {
 				for (final SolarObserver observer : aObservableSet) {
 					observer.shipLeft((Ship) prop);
 				}
 			}
 			return true;
 		}
 		return false;
 	}
 
 	@Override
 	public void shipBombed(final Ship ship, final GridLocation bombLocation)
 	{
 		// TODO Auto-generated method stub
 	}
 
 	@Override
 	public void shipCapturedPlanet(final Ship ship, final Planet planet, final ShipPath underlyingPath)
 	{
 		// TODO Auto-generated method stub
 	}
 
 	@Override
 	public void shipDestroyed(final Ship ship)
 	{
 		for (final Point p : ship.getLocation().getPoints()) {
 			aGrid.remove(p);
 		}
 		aProps.remove(ship);
 	}
 
 	@Override
 	public void shipEnteredCargo(final Ship container, final Ship docker, final ShipPath shipPath)
 	{
 	}
 
 	@Override
 	public void shipEnteredThis(final Ship containerShip, final Ship ship)
 	{
 		// Do nothing
 	}
 
 	@Override
 	public void shipExitedCargo(final Ship container, final Ship docker)
 	{
 		addElem(docker);
 	}
 
 	@Override
 	public void shipHealthChanged(final Ship ship, final int health)
 	{
 	}
 
 	@Override
 	public void shipJumped(final Ship ship, final EVContainer<Prop> oldContainer, final ShipPath leavingMove,
 			final EVContainer<Prop> newContainer)
 	{
 	}
 
 	@Override
 	public void shipLeftContainer(final Ship ship, final EVContainer<Prop> container, final ShipPath exitPath)
 	{
 		// TODO Auto-generated method stub
 	}
 
 	@Override
 	public void shipMoved(final Ship ship, final GridLocation oldLocation, final ShipPath path)
 	{
 		for (final Point p : oldLocation.getPoints()) {
 			aGrid.remove(p);
 		}
 		final List<GridLocation> elbows = path.getPath();
 		for (final Point p : elbows.get(elbows.size() - 1).getPoints()) {
 			aGrid.put(p, ship);
 		}
 	}
 
 	@Override
 	public void shipShieldsChanged(final Ship ship, final int shields)
 	{
 		// Nothing
 	}
 
 	@Override
 	public void shipShot(final Ship ship, final GridLocation shootLocation)
 	{
 	}
 
 	@Override
 	public Json toJson()
 	{
 		return new Json().setAttribute("dimension", aDimension).setListAttribute("props", aProps).setIntAttribute("id", aID)
 				.setAttribute("point", aPoint).setStringAttribute("name", aName);
 	}
 }
