 package com.evervoid.state;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import com.evervoid.json.Json;
 import com.evervoid.json.Jsonable;
 import com.evervoid.state.action.Action;
 import com.evervoid.state.action.Turn;
 import com.evervoid.state.building.Building;
 import com.evervoid.state.data.BadJsonInitialization;
 import com.evervoid.state.data.BuildingData;
 import com.evervoid.state.data.GameData;
 import com.evervoid.state.data.PlanetData;
 import com.evervoid.state.data.RaceData;
 import com.evervoid.state.data.ResourceData;
 import com.evervoid.state.data.StarData;
 import com.evervoid.state.geometry.Dimension;
 import com.evervoid.state.player.Player;
 import com.evervoid.state.prop.Planet;
 import com.evervoid.state.prop.Prop;
 import com.evervoid.state.prop.Ship;
 import com.evervoid.state.prop.Star;
 import com.evervoid.utils.MathUtils;
 
 public class EVGameState implements Jsonable
 {
 	private static final String sNeutralPlayerName = "NullPlayer";
 	private final Map<Integer, Building> aAllBuildings = new HashMap<Integer, Building>();
 	private final Map<Integer, Prop> aAllProps = new HashMap<Integer, Prop>();
 	protected Galaxy aGalaxy;
 	private final GameData aGameData;
 	private boolean aGameStarted = false;
 	private final Player aNullPlayer;
 	private final List<Player> aPlayerList;
 
 	/**
 	 * Restore a game state from a serialized state
 	 * 
 	 * @param json
 	 *            The Json representation of the game state
 	 * @throws BadJsonInitialization
 	 */
 	public EVGameState(final Json json) throws BadJsonInitialization
 	{
 		aGameStarted = json.getBooleanAttribute("gamestarted");
 		aGameData = new GameData(json.getAttribute("gamedata"));
 		final Json players = json.getAttribute("players");
 		aPlayerList = new ArrayList<Player>(players.size());
 		for (final Json p : players) {
 			aPlayerList.add(new Player(p, this));
 		}
 		if (getPlayerByName(sNeutralPlayerName) != null) {
 			aNullPlayer = getPlayerByName(sNeutralPlayerName);
 		}
 		else {
 			aNullPlayer = new Player(sNeutralPlayerName, "red", "round", this);
 			aPlayerList.add(aNullPlayer);
 		}
 		// It is necessary to create an empty galaxy first and then to populate it.
 		// This is because certain stuff (props) need to look up stuff from the game state when constructing themselves
 		// Thus aGalaxy must be defined in the game state before they can look that up
 		aGalaxy = new Galaxy(json.getAttribute("galaxy"), this);
 	}
 
 	/**
 	 * Default constructor, creates a fully randomized galaxy with solar systems and planets in it.
 	 */
 	public EVGameState(final List<Player> playerList, final GameData data)
 	{
 		// TODO - call up
 		aGameData = data;
 		aPlayerList = playerList;
 		aNullPlayer = new Player(sNeutralPlayerName, "round", "red", this);
 		aPlayerList.add(aNullPlayer);
 		aGalaxy = new Galaxy(this);
 		aGalaxy.populateGalaxy();
 		final List<SolarSystem> solarSystems = aGalaxy.getSolarSystems();
 		for (final Player p : aPlayerList) {
 			final SolarSystem home = (SolarSystem) MathUtils.getRandomElement(solarSystems);
 			solarSystems.remove(home);
 			p.setState(this);
 			p.setHomeSolarSystem(home);
 		}
 		aGalaxy.populateSolarSystems();
 	}
 
 	public void addProp(final Prop prop, final SolarSystem ss)
 	{
 		registerProp(prop);
 		ss.addElem(prop);
 	}
 
 	@Override
 	public EVGameState clone()
 	{
 		try {
 			return new EVGameState(toJson());
 		}
 		catch (final BadJsonInitialization e) {
 			// this should never happen
 			// if it does, this mean the toJson() is not in sync with the constructor
 			// this is bad news all around
 			Logger.getAnonymousLogger().log(Level.SEVERE,
 					"Error caught in State cloning. This is bad news, it very likely means the toJson() is having trouble");
 			return null;
 		}
 	}
 
 	public boolean commitAction(final Action action)
 	{
 		return action.execute();
 	}
 
 	public Turn commitTurn(final Turn turn)
 	{
 		final Turn newTurn = new Turn();
 		for (final Action action : turn.getActions()) {
 			if (commitAction(action)) {
 				newTurn.addAction(action);
 			}
 		}
		return turn;
 	}
 
 	public void deregisterBuilding(final int buildingID)
 	{
 		aAllBuildings.remove(buildingID);
 	}
 
 	public void deregisterProp(final int propID)
 	{
 		aAllProps.remove(propID);
 	}
 
 	public List<Ship> getAllShips()
 	{
 		final List<Ship> ships = new ArrayList<Ship>();
 		for (final Prop p : aAllProps.values()) {
 			if (p instanceof Ship) {
 				ships.add((Ship) p);
 			}
 		}
 		return ships;
 	}
 
 	public BuildingData getBuildingData(final String race, final String building)
 	{
 		return aGameData.getBuildingData(race, building);
 	}
 
 	public Building getBuildingFromID(final int id)
 	{
 		return aAllBuildings.get(id);
 	}
 
 	public EVContainer<Prop> getContainer(final int intAttribute)
 	{
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	public GameData getData()
 	{
 		return aGameData;
 	}
 
 	public Galaxy getGalaxy()
 	{
 		return aGalaxy;
 	}
 
 	public int getJumpCost()
 	{
 		return aGameData.getJumpCost();
 	}
 
 	public int getNextPlanetID()
 	{
 		if (aAllBuildings.isEmpty()) {
 			return 0;
 		}
 		int maxID = Integer.MIN_VALUE;
 		for (final int id : aAllBuildings.keySet()) {
 			maxID = Math.max(maxID, id);
 		}
 		return maxID + 1;
 	}
 
 	/**
 	 * @return A new, unused prop ID
 	 */
 	public int getNextPropID()
 	{
 		// If we have no prop, then ID 0 is not taken
 		if (aAllProps.isEmpty()) {
 			return 0;
 		}
 		// If we have props, iterate over them, get the max, and return max+1
 		// because that ID is certainly not taken
 		int maxId = Integer.MIN_VALUE;
 		for (final Integer id : aAllProps.keySet()) {
 			maxId = Math.max(maxId, id);
 		}
 		return maxId + 1;
 	}
 
 	/**
 	 * @return A new, unused solar system ID
 	 */
 	public int getNextSolarID()
 	{
 		return aGalaxy.getNextSolarID();
 	}
 
 	public int getNextWormholeID()
 	{
 		return aGalaxy.getNextWormholeID();
 	}
 
 	/**
 	 * @return The neutral (null) player
 	 */
 	public Player getNullPlayer()
 	{
 		return aNullPlayer;
 	}
 
 	/**
 	 * @return The number of players in the game
 	 */
 	public int getNumOfPlayers()
 	{
 		// -1 for nullplayer
 		return aPlayerList.size() - 1;
 	}
 
 	public Set<Planet> getPlanetByPlayer(final Player player)
 	{
 		final Set<Planet> planetSet = new HashSet<Planet>();
 		for (final Prop p : aAllProps.values()) {
 			if (p instanceof Planet && p.getPlayer().equals(player)) {
 				planetSet.add((Planet) p);
 			}
 		}
 		return planetSet;
 	}
 
 	/**
 	 * @param planetType
 	 *            The type of planet
 	 * @return The PlanetData object corresponding to that planet type
 	 */
 	public PlanetData getPlanetData(final String planetType)
 	{
 		return aGameData.getPlanetData(planetType);
 	}
 
 	/**
 	 * @return Available planet types
 	 */
 	public Set<String> getPlanetTypes()
 	{
 		return aGameData.getPlanetTypes();
 	}
 
 	/**
 	 * Returns a Player by his/her name
 	 * 
 	 * @param name
 	 *            The name to search for
 	 * @return The player object
 	 */
 	public Player getPlayerByName(final String name)
 	{
 		for (final Player p : aPlayerList) {
 			if (p.getName().equalsIgnoreCase(name)) {
 				return p;
 			}
 		}
 		return null;
 	}
 
 	/**
 	 * @param colorName
 	 *            The name of the player's color
 	 * @return The color of that name
 	 */
 	public Color getPlayerColor(final String colorName)
 	{
 		return aGameData.getPlayerColor(colorName);
 	}
 
 	/**
 	 * @return The list of players
 	 */
 	public List<Player> getPlayers()
 	{
 		return aPlayerList;
 	}
 
 	/**
 	 * Look up a Prop in the state.
 	 * 
 	 * @param id
 	 *            The prop ID.
 	 * @return The Prop object, or null if there is no such prop.
 	 */
 	public Prop getPropFromID(final int id)
 	{
 		return aAllProps.get(id);
 	}
 
 	/**
 	 * @param raceType
 	 *            The type of race
 	 * @return The RaceData object corresponding to that race type
 	 */
 	public RaceData getRaceData(final String raceType)
 	{
 		return aGameData.getRaceData(raceType);
 	}
 
 	/**
 	 * @return A randomly-selected player
 	 */
 	Player getRandomPlayer()
 	{
 		Player rand = (Player) MathUtils.getRandomElement(aPlayerList);
 		while (rand.equals(aNullPlayer)) {
 			rand = (Player) MathUtils.getRandomElement(aPlayerList);
 		}
 		return rand;
 	}
 
 	public Star getRandomStar(final Dimension dim)
 	{
 		return Star.randomStar(dim, this);
 	}
 
 	public ResourceData getResourceData(final String resourceType)
 	{
 		return aGameData.getResourceData(resourceType);
 	}
 
 	public Set<String> getResourceNames()
 	{
 		return aGameData.getResources();
 	}
 
 	/**
 	 * @param id
 	 *            The ID of the solar system
 	 * @return The solar system of the specified ID.
 	 */
 	public SolarSystem getSolarSystem(final int id)
 	{
 		return aGalaxy.getSolarSystem(id);
 	}
 
 	/**
 	 * @return The list of solar systems in the galaxy
 	 */
 	public Collection<SolarSystem> getSolarSystems()
 	{
 		return aGalaxy.getSolarSystems();
 	}
 
 	/**
 	 * @param starType
 	 *            The type of star
 	 * @return The StarData object corresponding to that star type
 	 */
 	public StarData getStarData(final String starType)
 	{
 		return aGameData.getStarData(starType);
 	}
 
 	/**
 	 * @return Available star types
 	 */
 	public Set<String> getStarTypes()
 	{
 		return aGameData.getStarTypes();
 	}
 
 	public Wormhole getWormhole(final int id)
 	{
 		return aGalaxy.getWormhole(id);
 	}
 
 	/**
 	 * @return Whether the game has started or not (in lobby)
 	 */
 	public boolean isStarted()
 	{
 		return aGameStarted;
 	}
 
 	/**
 	 * @return Whether the game is ready to be started (all players ready, all slots filled)
 	 */
 	public boolean readyToStart()
 	{
 		// TODO: Check if all players are ready
 		return true;
 	}
 
 	public void registerBuilding(final Building building)
 	{
 		aAllBuildings.put(building.getID(), building);
 	}
 
 	/**
 	 * Adds a prop to the game state's list of props
 	 * 
 	 * @param prop
 	 *            The prop to add
 	 */
 	public void registerProp(final Prop prop)
 	{
 		aAllProps.put(prop.getID(), prop);
 	}
 
 	@Override
 	public Json toJson()
 	{
 		final Json j = new Json();
 		j.setBooleanAttribute("gamestarted", aGameStarted);
 		j.setAttribute("gamedata", aGameData);
 		j.setAttribute("galaxy", aGalaxy);
 		j.setListAttribute("players", aPlayerList);
 		return j;
 	}
 }
