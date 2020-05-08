 package edu.gatech.CS2340.GrandTheftPoke;
 
 import com.badlogic.gdx.Game;
 import com.badlogic.gdx.Screen;
 
 import edu.gatech.CS2340.GrandTheftPoke.backend.Items.GlobalItemReference;
 import edu.gatech.CS2340.GrandTheftPoke.backend.Towns.Town;
 import edu.gatech.CS2340.GrandTheftPoke.backend.Towns.TownFactoryImplementation;
 import edu.gatech.CS2340.GrandTheftPoke.screens.*;
 
 public class GTPoke extends Game {
 	private String playerName = "";
 	private Player thePlayer;
 	private GlobalItemReference theReference;
 	private static final int INITIAL_RANGE = 50;
 	private static final int INITIAL_CARRY = 30;
 	private static final int INITIAL_HEALTH = 500;
 
 	public Screen getSplashScreen(){
         return new SplashScreen( this );
     }
 	
 	public Screen getMainMenuScreen(){
         return new MainMenu(this);
     }
 	
 	public Screen getNameScreen(){
         return new Name(this);
     }
 	
 	public Screen getMarketPlaceDemoScreen(){
         return new MarketPlaceItemDemo(this);
     }
 	
 	public Screen getSkillPointsScreen(){
         return new SkillPoints(this);
     }
 	
 	public Screen getStarterPokemonScreen(){
         return new StarterPokemon(this);
     }
 	
     @Override
     public void create(){
    	items = new GlobalItemReference();
         setScreen(getSplashScreen());
     }
 
 	@Override
 	public void dispose() {
 		super.dispose();
 	}
 
 	@Override
 	public void render() {		
 		super.render();
 		
 	}
 
 	@Override
 	public void resize(int width, int height) {
 		super.resize(width, height);
 	}
 
 	@Override
 	public void pause() {
 		super.pause();
 	}
 
 	@Override
 	public void resume() {
 		super.resume();
 	}
 
 	public void setPlayerName(String playerName) {
 		this.playerName = playerName;
 		
 	}
 
 	public void createPlayer(Integer strength, Integer trade, Integer agility,
 			Integer stamina) {
 		theReference = new GlobalItemReference();
 		GameMap theMap = GTPoke.makeMap(theReference);
 		thePlayer = new Player(playerName, strength, trade, agility, stamina, INITIAL_HEALTH, INITIAL_RANGE, INITIAL_CARRY, theMap);
 	}
 	
 	public static GameMap makeMap(GlobalItemReference theReference) {
 		TownFactoryImplementation townGenerator = new TownFactoryImplementation(
 				theReference);
 
 		Town palletTown = townGenerator.makePalletTown();
 		Town viridianCity = townGenerator.makeViridianCity();
 		Town pewterCity = townGenerator.makePewterCity();
 		Town ceruleanCity = townGenerator.makeCeruleanCity();
 		Town vermillionCity = townGenerator.makeVermillionCity();
 		Town lavenderTown = townGenerator.makeLavenderTown();
 		Town celadonCity = townGenerator.makeCeladonCity();
 		Town fuchsiaCity = townGenerator.makeFuchsiaCity();
 		Town saffronCity = townGenerator.makeSaffronCity();
 		Town cinnabarIsland = townGenerator.makeCinnabarIsland();
 		Town powerPlant = townGenerator.makePowerPlant();
 
 		palletTown.addConnection(new Path(cinnabarIsland, 100));
 		palletTown.addConnection(new Path(viridianCity, 10));
 
 		viridianCity.addConnection(new Path(palletTown, 10));
 		viridianCity.addConnection(new Path(pewterCity, 20));
 
 		pewterCity.addConnection(new Path(viridianCity, 20));
 		pewterCity.addConnection(new Path(ceruleanCity, 50));
 
 		ceruleanCity.addConnection(new Path(pewterCity, 50));
 		ceruleanCity.addConnection(new Path(powerPlant, 30));
 		ceruleanCity.addConnection(new Path(saffronCity, 20));
 
 		powerPlant.addConnection(new Path(ceruleanCity, 30));
 
 		saffronCity.addConnection(new Path(ceruleanCity, 20));
 		saffronCity.addConnection(new Path(celadonCity, 10));
 		saffronCity.addConnection(new Path(lavenderTown, 20));
 		saffronCity.addConnection(new Path(vermillionCity, 20));
 
 		celadonCity.addConnection(new Path(saffronCity, 10));
 		celadonCity.addConnection(new Path(fuchsiaCity, 50));
 
 		lavenderTown.addConnection(new Path(saffronCity, 20));
 		lavenderTown.addConnection(new Path(vermillionCity, 40));
 		lavenderTown.addConnection(new Path(fuchsiaCity, 120));
 
 		vermillionCity.addConnection(new Path(saffronCity, 20));
 		vermillionCity.addConnection(new Path(lavenderTown, 40));
 		vermillionCity.addConnection(new Path(fuchsiaCity, 80));
 
 		fuchsiaCity.addConnection(new Path(celadonCity, 50));
 		fuchsiaCity.addConnection(new Path(lavenderTown, 120));
 		fuchsiaCity.addConnection(new Path(vermillionCity, 80));
 		fuchsiaCity.addConnection(new Path(cinnabarIsland, 100));
 
 		cinnabarIsland.addConnection(new Path(fuchsiaCity, 100));
 		cinnabarIsland.addConnection(new Path(palletTown, 100));
 
 		GameMap map = new GameMap(palletTown);
 		map.addTown(viridianCity);
 		map.addTown(pewterCity);
 		map.addTown(ceruleanCity);
 		map.addTown(powerPlant);
 		map.addTown(saffronCity);
 		map.addTown(celadonCity);
 		map.addTown(lavenderTown);
 		map.addTown(vermillionCity);
 		map.addTown(fuchsiaCity);
 		map.addTown(cinnabarIsland);
 
 		return map;
 	}
 }
