 package core;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Random;
 
 import org.newdawn.slick.AppGameContainer;
 import org.newdawn.slick.Color;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.state.transition.FadeInTransition;
 import org.newdawn.slick.state.transition.FadeOutTransition;
 import org.newdawn.slick.state.transition.RotateTransition;
 import org.newdawn.slick.state.transition.Transition;
 
 import scene.*;
 import scene.encounter.*;
 import scene.test.*;
 
 import model.*;
 
 /**
  * Directs the logical functionality of the game. Sets everything in motion.
  */
 
 public class GameDirector implements SceneListener {
 	private static GameDirector sharedDirector;
 	
 	private FontStore fontManager;
 	
 	private SceneDirector sceneDirector;
 	private AppGameContainer container;
 	
 	private Game game;
 	private WorldMap worldMap;
 	/**
 	 * Constructs a game director object.
 	 */
 	private GameDirector() {
 		 sharedDirector = this;
 		 
 		 //fontManager = new FontManager();
 
 		 sceneDirector = new SceneDirector("Oregon Trail");
 		 worldMap = new WorldMap();
 		 game = new Game(worldMap);
		 game.getStoreInventory().addRandomItems();
 	}
 	
 	/**
 	 * Singleton Design pattern. Returns the single instance of game director object that drives the game, for the scenes to interact with.
 	 * @return handle of game director for scene to interact with
 	 */
 	public static SceneListener sharedSceneListener() {
 		return sharedDirector;
 	}
 
 	/**
 	 * Launches window that contains game.
 	 */
 	public void start() {
 		try {
 			container = new AppGameContainer(sceneDirector);
 			container.setDisplayMode(1024, 576, false);
 			container.setTargetFrameRate(60);
 			container.setAlwaysRender(true);
 			container.setShowFPS(false);
 			container.start();
 		} catch (SlickException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	/**
 	 * Returns the game object.
 	 * @return the game
 	 */
 	public Game getGame() {
 		return game;
 	}
 	
 	/**
 	 * Returns the container that holds the gui element of the game.
 	 * @return the game container
 	 */
 	public AppGameContainer getContainer() {
 		return container;
 	}
 	
 	/**
 	 * Determines the appropriate game scene to return based on a passed id.
 	 * @param id the {@code SceneID} desired
 	 * @return the handle to the newly created scene
 	 */
 	private Scene sceneForSceneID(SceneID id) {
 		switch (id) {
 		case MAINMENU:
 			return new MainMenuScene();
 		case PARTYCREATION:
 			return new PartyCreationScene(game.getPlayer());
 		case TOWN:
 			game.resetStoreInventory(worldMap.getCurrLocationNode());
 			return new TownScene(game.getPlayer().getParty(), worldMap.getCurrLocationNode());
 		case STORE:
 			return new StoreScene(game.getPlayer().getParty(), game.getStoreInventory(), (1 + worldMap.getCurrLocationNode().getRank()/10));
 		case PARTYINVENTORY:
 			return new PartyInventoryScene(game.getPlayer().getParty());
 		case SCENESELECTOR:
 			return new SceneSelectorScene(game);
 		case HUNT:
 			return new HuntScene(game.getPlayer().getParty());
 		case TRAIL:
 			return new TrailScene(game.getPlayer().getParty(), new RandomEncounterTable(getEncounterList()));
 		case PARTYMANAGEMENTSCENE:
 			return new PartyManagementScene(game.getPlayer().getParty());
 		case GAMEOVER:
 			return new GameOverScene();
 		case VICTORY:
 			return new VictoryScene();
 		case COMPONENTTEST:
 			return new ComponentTestScene();
 		case TRAILTEST:
 			return new TrailTestScene(game.getPlayer().getParty());
 		case CAMP:
 			return new CampScene();
 		case MAP:
 			return new MapScene(game.getWorldMap());
 		case RIVER:
 			return new RiverScene(game.getPlayer().getParty());
 		}
 		
 		return null;
 	}
 
 	/*----------------------
 	  SceneDelegate
 	  ----------------------*/
 	@Override
 	public void requestScene(SceneID id, Scene lastScene, boolean popLastScene) {
 		
 		Transition outTransition = null;
 		Transition inTransition = null;
 		
 		if(game.getPlayer().getParty() != null && game.getPlayer().getParty().getLocation() != null) {
 			worldMap.setCurrLocationNode(game.getPlayer().getParty().getLocation());
 		}
 		if (game.getPlayer().getParty() != null && game.getPlayer().getParty().getLocation() != null) {
 			worldMap.setCurrTrail(game.getPlayer().getParty().getTrail());
 		}
 		
 		Scene newScene = id != null ? sceneForSceneID(id): null;
 		
 		if(worldMap.getCurrLocationNode().getRank() == worldMap.getMaxRank()) {
 			newScene = new VictoryScene();
 		}
 		if (newScene instanceof VictoryScene || newScene instanceof GameOverScene) {
 			inTransition = new RotateTransition(Color.black);
 		} else if (newScene instanceof PartyInventoryScene && lastScene instanceof StoreScene) {
 			// Requested Party Inventory Scene
 			newScene = new PartyInventoryScene(game.getPlayer().getParty(), ((StoreScene) lastScene).getInventory());
 		}
 		if (lastScene instanceof PartyCreationScene) {
 			// Last scene was Party Creation Scene
 			game.getPlayer().getParty().setLocation(game.getWorldMap().getMapHead());
 		}  
 		
 		/*
 		if (id == SceneID.SCENESELECTOR) {
 			newScene = sceneForSceneID(id);
 		} else if(worldMap.getCurrLocationNode().getRank() == worldMap.getMaxRank()) {
 			newScene = new VictoryScene();
 			inTransition = new RotateTransition(Color.black);
 		} else if (id == SceneID.GAMEOVER) {
 			newScene = sceneForSceneID(id);
 			inTransition = new RotateTransition(Color.black);
 		} else if (lastScene instanceof MainMenuScene) {
 			// Last scene was Main Menu Scene
 			if (id == SceneID.PARTYCREATION) {
 				// Requested Party Creation Scene
 				newScene = new PartyCreationScene(game.getPlayer());
 			}
 		} else if (lastScene instanceof PartyCreationScene) {
 			// Last scene was Party Creation Scene
 			game.getPlayer().getParty().setLocation(game.getWorldMap().getMapHead());
 			newScene = new TownScene(game.getPlayer().getParty(), worldMap.getMapHead());
 		} else if (lastScene instanceof TownScene) {
 			// Last scene was Town Scene
 			if (id == SceneID.STORE) {
 				// Requested Store Scene
 				newScene = new StoreScene(game.getPlayer().getParty(), game.getStoreInventory(), (1 + worldMap.getCurrLocationNode().getRank()/10));
 			}
 			if (id == SceneID.TRAIL) {
 				//Requested Trail scene
 				newScene = new TrailScene(game.getPlayer().getParty(), new RandomEncounterTable(getEncounterList()));
 			}
 		} else if (lastScene instanceof StoreScene) {
 			// Last scene was Store Scene
 			if (id == SceneID.PARTYINVENTORY) {
 				// Requested Party Inventory Scene
 				newScene = new PartyInventoryScene(game.getPlayer().getParty(), ((StoreScene) lastScene).getInventory());
 			}
 		} else if (lastScene instanceof SceneSelectorScene) {
 			// Last scene was Scene Selector Scene
 			newScene =  sceneForSceneID(id);
 		} else if (id == SceneID.PARTYINVENTORY) {
 			// Requested Party Inventory Scene
 			newScene = new PartyInventoryScene(game.getPlayer().getParty());
 		} else if (id == SceneID.HUNT) {
 			//Requested Hunt scene
 			newScene = new HuntScene(game.getPlayer().getParty());
 		} else if (id == SceneID.CAMP) {
 			//Requested Trail scene
 			newScene = new CampScene();
 		} else if (id == SceneID.PARTYMANAGEMENTSCENE) {
 			//Requested Party Management Scene scene
 			newScene = new PartyManagementScene(game.getPlayer().getParty());
 		} else if (id == SceneID.TOWN) {
 			game.resetStoreInventory(worldMap.getCurrLocationNode());
 			newScene = new TownScene(game.getPlayer().getParty(), worldMap.getCurrLocationNode());
 		}
 		*/
 		if (newScene != null) {
 			lastScene.disable();
 			
 			if (outTransition == null) {
 				outTransition = new FadeOutTransition(Color.black);
 			}
 			if (inTransition == null) {
 				inTransition = new FadeInTransition(Color.black);
 			}
 			
 			sceneDirector.pushScene(newScene, popLastScene, true, outTransition, inTransition);
 		}
 	}
 	
 	@Override
 	public void sceneDidEnd(Scene scene) {
 		scene.disable();
 		sceneDirector.popScene(true);
 	}
 	
 	@Override
 	public FontStore getFontManager() {
 		return fontManager;
 	}
 	
 	@Override
 	public void showSceneSelector() {
 		sceneDirector.replaceStackWithScene(sceneForSceneID(SceneID.SCENESELECTOR));
 	}
 	
 	/**
 	 * Gets the encounter list to populate the trail scenes random encounter table
 	 * @return The mapping of scene types to probabilities
 	 */
 	private List<Encounter> getEncounterList() {
 		Random rand = new Random();
 		List<Encounter> encounterList = new ArrayList<Encounter>();
 		
 		for ( EncounterID encounter : EncounterID.values() ) {
 			encounterList.add(EncounterID.getEncounter(game.getPlayer().getParty(), encounter, 5 * encounter.getFrequency() * (rand.nextInt(10)+1)));
 		}
 		return encounterList;
 	}
 
 	/*
 	@Override
 	public void playMusic(Music music) {
 		this.music = music;
 		this.music.loop();
 	}
 	
 	public void stopMusic() {
 		if (this.music != null) {
 			this.music.fade(1000, 0, true);
 		}
 	}
 	*/
 	
 	/*----------------------
 	  Main
 	  ----------------------*/
 	/**
 	 * The game main method
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		Logger.log("-----------------NEW GAME STARTED-----------------", Logger.Level.INFO);
 		GameDirector gameDirector = new GameDirector();
 		gameDirector.start();
 	}
 }
