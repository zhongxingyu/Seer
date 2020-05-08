 /**
  * 
  */
 package edu.gatech.cs2340.test;
 
 import static org.junit.Assert.*;
 import static org.mockito.Mockito.*;
 
 import java.awt.Color;
 import java.util.ArrayList;
 import java.util.HashMap;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.mockito.Mock;
 import org.mockito.Mockito;
 import org.mockito.MockitoAnnotations;
 import org.mockito.runners.MockitoJUnitRunner;
 
 import edu.gatech.cs2340.data.GameData;
 import edu.gatech.cs2340.data.HillTile;
 import edu.gatech.cs2340.data.Map;
 import edu.gatech.cs2340.data.MapResponsibilities;
 import edu.gatech.cs2340.data.MountainTile;
 import edu.gatech.cs2340.data.Mule;
 import edu.gatech.cs2340.data.PeakTile;
 import edu.gatech.cs2340.data.PlainsTile;
 import edu.gatech.cs2340.data.Player;
 import edu.gatech.cs2340.data.ResourceAmount;
 import edu.gatech.cs2340.data.ResourceAmount.ResourceType;
 import edu.gatech.cs2340.data.RiverTile;
 import edu.gatech.cs2340.data.Tile;
 import edu.gatech.cs2340.engine.ResourceProducer;
 
 /**
  * @author Dan
  * 
  */
 @RunWith(MockitoJUnitRunner.class)
 public class M10ResourceProducerTest {
 
 	/**
 	 * Array of colors to choose from
 	 */
 	private static Color[] colors = { Color.BLUE, Color.YELLOW, Color.GREEN,
 			Color.RED };
 
 	/**
 	 * There are 4 colors to choose from
 	 */
 	private static int numColors = 4;
 
 	private static ResourceType[] resourceTypes = {
 			ResourceAmount.ResourceType.FOOD,
 			ResourceAmount.ResourceType.ENERGY,
 			ResourceAmount.ResourceType.SMITHORE,
 			ResourceAmount.ResourceType.CRYSTITE, };
 
 	private enum TestType {
 		food_t(0), energy_t(1), ore_t(2), crystite_t(3), spare_t(4), insufficientEnergy_t(
 				5);
 		private int value;
 
 		TestType(int value) {
 			this.value = value;
 		}
 	}
 	private static final int FOOD_INDEX = 0;
 	private static final int ENERGY_INDEX = 1;
 	private static final int ORE_INDEX = 2;
 	private static final int CRYSTITE_INDEX = 3;
	private static final int SPARE_INDEX = 3;
	private static final int INSUFFICIENT_ENERGY_INDEX = 3;
 
 	/**
 	 * The class to be tested
 	 */
 	ResourceProducer resProd;
 
 	/**
 	 * Mocked object of GameData to return the map
 	 */
 	@Mock
 	GameData gameData;
 
 	/**
 	 * Mocked object of Map to return tiles
 	 */
 	@Mock
 	Map map;
 
 	/**
 	 * Holds the tiles to test the resource producer
 	 */
 	ArrayList<Tile> tiles;
 
 	/**
 	 * Holds the tiles to test the resource producer
 	 */
 	ArrayList<Player> players;
 
 	/**
 	 * The amount of food that should come out of production for a given test
 	 */
 	HashMap<Integer, Integer> resIncreases;
 
 	/**
 	 * Number of tiles in the array
 	 */
 	private final static int numTiles = 6;
 
 	/**
 	 * The number of resources that are able to be produced
 	 */
 	private final static int numResources = 4;
 	/**
 	 * Number of players in the array
 	 */
 	private final static int numPlayers = 5;
 
 	private static final int standardValues[] = {8, 4, 0, 0};
 	private static final int STANDARD_FOOD = 8;
 	private static final int STANDARD_ENERGY = 4;
 	private static final int STANDARD_ORE = 0;
 	private static final int STANDARD_CRYSTITE = 0;
 
 	/**
 	 * Number of energy units used to produce resources
 	 */
 	private static final int ENERGY_AMOUNT_USED = 1;
 
 	/**
 	 * Maximum possible crystite production
 	 */
 	private static final int MAX_CRYSTITE = 25;
 
 	/**
 	 * @throws java.lang.Exception
 	 */
 	@Before
 	public void setUp() throws Exception {
 		// MockitoAnnotations.initMocks(this); // initialize mocked classes
 		gameData = Mockito.mock(GameData.class);
 		map = Mockito.mock(Map.class);
 		when(gameData.getMap()).thenReturn((Map) map); // return the map
 		// initialize players
 		players = new ArrayList<Player>();
 		for (int i = 0; i < numPlayers; i++) {
 			players.add(new Player("Player" + i, "Race" + i, colors[i
 					% numColors]));
 		}
 		tiles = new ArrayList<Tile>();
 		resIncreases = new HashMap<Integer, Integer>();
 		resProd = new ResourceProducer(gameData);
 	}
 
 	/**
 	 * @throws java.lang.Exception
 	 */
 	@After
 	public void tearDown() throws Exception {
 		gameData = null;
 		map = null;
 		players = null;
 		tiles = null;
 		resIncreases = null;
 		resProd = null;
 	}
 
 	@Test
 	public void runRiverTestSufficientEnergy() {
 		runRiverTest(true);
 	}
 
 	@Test
 	public void runRiverTestInsufficientEnergy() {
 		drainEnergy();
 		runRiverTest(false);
 	}
 	
 	@Test
 	public void runPlainsTestSufficientEnergy() {
 		runPlainsTest(true);
 	}
 
 	@Test
 	public void runPlainsTestInsufficientEnergy() {
 		drainEnergy();
 		runPlainsTest(false);
 	}
 	
 	@Test
 	public void runHillTestSufficientEnergy() {
 		runHillTest(true);
 	}
 
 	@Test
 	public void runHillTestInsufficientEnergy() {
 		drainEnergy();
 		runHillTest(false);
 	}
 	
 	@Test
 	public void runMountainTestSufficientEnergy() {
 		runMountainTest(true);
 	}
 
 	@Test
 	public void runMountainTestInsufficientEnergy() {
 		drainEnergy();
 		runMountainTest(false);
 	}
 	
 	@Test
 	public void runPeakTestSufficientEnergy() {
 		runPeakTest(true);
 	}
 
 	@Test
 	public void runPeakTestInsufficientEnergy() {
 		drainEnergy();
 		runPeakTest(false);
 	}
 
 	/**
 	 * Remove all energy from all players
 	 */
 	private void drainEnergy() {
 		for (int i = 0; i < numPlayers; i++) {
 			Player curPlayer = players.get(i);
 			// take away all energy
 			curPlayer.addResources(new ResourceAmount(0, 0, -1
 					* curPlayer.getResourceAmount(ResourceType.ENERGY), 0));
 		}
 	}
 
 	/**
 	 * Test the river tile
 	 * 
 	 * @param hasEnergy True if the players have energy to produce
 	 */
 	public void runRiverTest(boolean hasEnergy) {
 		resIncreases.put(FOOD_INDEX, 4);
 		resIncreases.put(ENERGY_INDEX, 2);
 		resIncreases.put(ORE_INDEX, 0);
 		resIncreases.put(CRYSTITE_INDEX, 0);
 		resIncreases.put(SPARE_INDEX, 0);
 		resIncreases.put(INSUFFICIENT_ENERGY_INDEX, 0);
 
 		for (int i = 0; i < numTiles; i++) {
 			Tile curTile = new RiverTile("Tile" + i, null);
 			setUpTile(curTile, i);
 			tiles.add(curTile);
 		}
 		// Set up map functionality
 		setUpMapMock();
 		resProd.runSynchronous();
 		checkPlayerProduction("River", hasEnergy);
 	}
 	
 	/**
 	 * Test the plains tile
 	 * 
 	 * @param hasEnergy True if the players have energy to produce
 	 */
 	public void runPlainsTest(boolean hasEnergy) {
 		resIncreases.put(FOOD_INDEX, 2);
 		resIncreases.put(ENERGY_INDEX, 3);
 		resIncreases.put(ORE_INDEX, 1);
 		resIncreases.put(CRYSTITE_INDEX, 0);
 		resIncreases.put(SPARE_INDEX, 0);
 		resIncreases.put(INSUFFICIENT_ENERGY_INDEX, 0);
 
 		for (int i = 0; i < numTiles; i++) {
 			Tile curTile = new PlainsTile("Tile" + i, null);
 			setUpTile(curTile, i);
 			tiles.add(curTile);
 		}
 		// Set up map functionality
 		setUpMapMock();
 		resProd.runSynchronous();
 		checkPlayerProduction("Plains", hasEnergy);
 	}
 	
 	/**
 	 * Test the hill tile
 	 * 
 	 * @param hasEnergy True if the players have energy to produce
 	 */
 	public void runHillTest(boolean hasEnergy) {
 		resIncreases.put(FOOD_INDEX, 1);
 		resIncreases.put(ENERGY_INDEX, 1);
 		resIncreases.put(ORE_INDEX, 2);
 		resIncreases.put(CRYSTITE_INDEX, 2);
 		resIncreases.put(SPARE_INDEX, 0);
 		resIncreases.put(INSUFFICIENT_ENERGY_INDEX, 0);
 
 		for (int i = 0; i < numTiles; i++) {
 			Tile curTile = new HillTile("Tile" + i, null);
 			setUpTile(curTile, i);
 			tiles.add(curTile);
 		}
 		// Set up map functionality
 		setUpMapMock();
 		resProd.runSynchronous();
 		checkPlayerProduction("Hill", hasEnergy);
 	}
 	
 	/**
 	 * Test the mountain tile
 	 * 
 	 * @param hasEnergy True if the players have energy to produce
 	 */
 	public void runMountainTest(boolean hasEnergy) {
 		resIncreases.put(FOOD_INDEX, 1);
 		resIncreases.put(ENERGY_INDEX, 1);
 		resIncreases.put(ORE_INDEX, 3);
 		resIncreases.put(CRYSTITE_INDEX, 3);
 		resIncreases.put(SPARE_INDEX, 0);
 		resIncreases.put(INSUFFICIENT_ENERGY_INDEX, 0);
 
 		for (int i = 0; i < numTiles; i++) {
 			Tile curTile = new MountainTile("Tile" + i, null);
 			setUpTile(curTile, i);
 			tiles.add(curTile);
 		}
 		// Set up map functionality
 		setUpMapMock();
 		resProd.runSynchronous();
 		checkPlayerProduction("Mountain", hasEnergy);
 	}
 
 	/**
 	 * Test the peak tile
 	 * 
 	 * @param hasEnergy True if the players have energy to produce
 	 */
 	public void runPeakTest(boolean hasEnergy) {
 		resIncreases.put(FOOD_INDEX, 1);
 		resIncreases.put(ENERGY_INDEX, 1);
 		resIncreases.put(ORE_INDEX, 4);
 		resIncreases.put(CRYSTITE_INDEX, 4);
 		resIncreases.put(SPARE_INDEX, 0);
 		resIncreases.put(INSUFFICIENT_ENERGY_INDEX, 0);
 
 		for (int i = 0; i < numTiles; i++) {
 			Tile curTile = new PeakTile("Tile" + i, null);
 			setUpTile(curTile, i);
 			tiles.add(curTile);
 		}
 		// Set up map functionality
 		setUpMapMock();
 		resProd.runSynchronous();
 		checkPlayerProduction("Peak", hasEnergy);
 	}
 	
 	/**
 	 * Verify that player production is what it should be
 	 * 
 	 * @param tileType
 	 *            The type of tile for which the method tests
 	 * @param hasEnergy Whether the player has energy to produce
 	 */
 	private void checkPlayerProduction(String tileType, boolean hasEnergy) {
 		// for each player
 		for (int i = 0; i < numPlayers; i++) {
 			// Check that the intended resource was produced
 			Player curPlayer = players.get(i);
 			// for each resource that player owns
 			for (int resIndex = 0; resIndex < numResources; resIndex++) {
 				ResourceType curResource = resourceTypes[resIndex];
 				String message = curResource + " production wrong for player " + i
 						+ " in Tile type " + tileType;
 				if (!hasEnergy){
 					message += "; player didn't have energy";
 				}
 				int standardValue = standardValues[resIndex];
 				int resAmountActual = (int) curPlayer
 						.getResourceAmount(curResource);
 				int resAmountExpected = standardValue;
 				// get the value if the player has energy and if the mule was the same as the resource type
 				if (hasEnergy && (i == resIndex)) {
 					resAmountExpected = (int) resIncreases.get(resIndex)
 							+ standardValue;
 				}
 				// use energy in production if player has a mule to produce with
 				if (hasEnergy && (i < numResources) && (resIndex == ENERGY_INDEX)){
 					resAmountExpected -= ENERGY_AMOUNT_USED;
 				} else if (!hasEnergy && (resIndex == ENERGY_INDEX)){
 					resAmountExpected = 0; // energy is drained
 				}
 				if (resIndex == CRYSTITE_INDEX){
 					if (resAmountActual > MAX_CRYSTITE || resAmountActual < 0){
 						message += ". Expected less than: "+MAX_CRYSTITE+" && >= 0 but actual was: "+resAmountActual;
 						fail(message);
 					}
 				} else {
 					// test for the resource that actually increased
 					assertEquals(message, resAmountActual, resAmountExpected);
 				}
 			}
 		}
 	}
 
 	/**
 	 * Set up iterator through tiles of the Map
 	 */
 	private void setUpMapMock() {
 		when(map.getNextTile()).thenReturn(tiles.get(0))
 				.thenReturn(tiles.get(1)).thenReturn(tiles.get(2))
 				.thenReturn(tiles.get(3)).thenReturn(tiles.get(4))
 				.thenReturn(tiles.get(5)).thenReturn(null);
 	}
 
 	/**
 	 * Set up the tile with the configuration set by the parameters
 	 * 
 	 * @param curTile
 	 *            Tile to be set
 	 * @param index
 	 *            Index from which the tile was taken
 	 * @param resourceType
 	 *            Type of resource the Mule on it will produce
 	 */
 	private void setUpTile(Tile curTile, int index) {
 		if (index < numPlayers) { // assign one tile per player (one tile will
 									// not have an owner)
 			curTile.setOwner(players.get(index));
 			if (index < numResources) { // allow for one player without a MULE
 				curTile.setMule(new Mule(resourceTypes[index]));
 			}
 		}
 	}
 
 }
