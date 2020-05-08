 
 import com.google.gson.Gson;
 import com.google.gson.GsonBuilder;
 import com.google.gson.reflect.TypeToken;
 
 import java.lang.reflect.Type;
 
 import com.mongodb.MongoClient;
 import com.mongodb.MongoException;
 import com.mongodb.WriteConcern;
 import com.mongodb.DB;
 import com.mongodb.DBCollection;
 import com.mongodb.BasicDBList;
 import com.mongodb.BasicDBObject;
 import com.mongodb.DBObject;
 import com.mongodb.DBCursor;
 import com.mongodb.ServerAddress;
 
 import java.util.ArrayList;
 
 public class GameController {
     /**
      * Main method of the MULE game
      * Initializes the environment and starts the game
      *
      * Currently a stub method 
      *
      * @param args command line arguments - currently don't do anything
      */
     public static void main(String[] args) {
         System.out.println("Main Method!");
         GameController game = new GameController();
     }
 
     private int difficulty = 1;
     private int roundNumber = 1;
     private Renderer renderer;
     private int currPlayer;
     private int numPlayers;
     private Map map;
     private String state;
     private ArrayList<Player> players;
     private long startTime;
     private long stopTime;
     private Integer elapsedTime;
     private Store store;
     private MongoClient mongoClient;
     private DB db;
     private String output;
 
     /**
      * GameController handles all input related actions for game.
      */
     public GameController() {
         renderer = new Renderer();
         currPlayer = 0;
         numPlayers = 1;
         state = "";
         output = "";
         players = new ArrayList<Player>();
         try {
             mongoClient = new MongoClient();
         }
         catch (Exception e){
         }
         db = mongoClient.getDB( "mule" ); 
         playGame();
     }
 
     /**
      * playGame initializes main game
      */
     private void playGame() {
         startGame();
         mainGame();
     }
 
     /**
      * startGame handles and appropriates intial game data
      */
     private void startGame() {
         state = "intro";
         boolean initializing = true;
 
         // variables we are collecting
         difficulty = 1;
         map = null;
         ArrayList<String> takenColors = new ArrayList<String>();
 
         while(initializing) {
             System.out.println("State: " + state);
             // Introduction Screen
             if (state.equals("intro")) {
                 String action = renderer.drawIntroScreen()[0];
                 if (action.equals("quit")) {
                     state = "quit";
                 }
                 else if (action.equals("load")) {
                     state = "load";
                 }
                 else {
                     state = "setup";
                 }
             }
             
             //Load
             else if(state.equals("load")){
                 String[] results = renderer.drawLoadScreen();
                 String action = results[0];
                 if (action.equals("back")) {
                    state = "intro";
                }
                else {
                     state = "intro";
                     if (loadGame(results[1]))
                     {
                         initializing = false;
                     }
                     else
                     {
                         output = "Failed to load game!";
                     }
                }
             }
 
             // Setup Screen
             else if (state.equals("setup")) {
                String[] results = renderer.drawSetupScreen(numPlayers, difficulty);
                String action = results[0];
                difficulty = Integer.parseInt(results[1]);
                store = new Store(difficulty);
                numPlayers = Integer.parseInt(results[2]);
                if (action.equals("okay")) {
                    state = "map";
                }
                else {
                    state = "intro";
                }
             }
 
             // Map Screen
             else if (state.equals("map")) {
                 String[] results = renderer.drawMapScreen(numPlayers, difficulty);
                 String action = results[0];
                 map = new Map(Integer.parseInt(results[1]));
                 if (action.equals("okay")) {
                     state = "player";
                 }
                 else {
                     state = "setup";
                 }
             }
 
             // Character selection screen
             else if (state.equals("player")) {
                 String[] results = renderer.drawCharacterScreen(players, difficulty, map, numPlayers);
                 String action = results[0];
                 int counter = 5;
                 if (action.equals("back")) {
                     state = "map";
                 }
                 else {
                     if (takenColors.contains(results[3])) {
                         output = "That color is taken!";
                         continue;
                     }
                     try {
                         players.add(new Player(results[2], results[1], results[3], difficulty));
                         takenColors.add(results[3]);
                     }
                     catch (Exception exception) {
                         System.out.println(exception);
                         continue;
                     }
 
                     // only move on if we have all the players
                     if (--numPlayers == 0) {
                         state = "game";
                     }   
                 }
             }
 
             else if (state.equals("game")) {
                 initializing = false;
             }
 
             // quit state
             else {
                 System.exit(0);
                 initializing = false;
             }
         }
         System.out.println("Game initialization complete with paramaters:\n");
         numPlayers = players.size();
         System.out.println("Difficulty: " + difficulty);
         System.out.println("NumPlayers: " + numPlayers);
         System.out.println("Map: " + map.getMapNum());
         for (Player p : players) {
             System.out.println(p);
         }
         System.out.println("Starting game...\n\n");
         }
 
     /**
      * mainGame main framework for the game.
      */
     private void mainGame() {
         String[] quantities = {"0", "0", "0", "0"}; // quantities to be bought/sold
         boolean initializing = true;
         renderer.startTimer(getTime());
         startTime = System.currentTimeMillis();
 
         while(initializing) {
             if (state.equals("game")){
                 String[] results = renderer.drawMainGameScreen(map, players, currPlayer, store, numPlayers, roundNumber, output);
                 output = "";
 
                 if (results[0].equals("time")) {
                     System.out.println("Time's up, switching player");
                     switchPlayer();
                 }
 
                 else if(results[0].equals("stop")) {
                     results = renderer.drawSaveScreen(players, currPlayer, store, numPlayers, roundNumber);
                     if(results[0].equals("save")) {
                         if (!saveGame(results[1]))
                         {
                             output = "Failed to save game!";
                             state = "game";
                         }
                         else
                         {
                             System.exit(0);
                             initializing = false;
                         }
                     }
                 }
                 else if(results[0].equals("pause")) {
                     renderer.pauseTimer();
                     output = "Game Paused.";
                     results = renderer.drawMenuScreen(players, currPlayer, store, numPlayers, roundNumber, map, output);
                     if(results[0].equals("resume")) {
                         output = "Game Resumed.";
                         state = "game";
                         renderer.unpauseTimer();
                     }
                     else if(results[0].equals("save")) {
                         if (!saveGame(results[1]))
                         {
                             output = "Failed to save game!";
                             state = "game";
                         }
                         else
                         {
                             System.exit(0);
                             initializing = false;
                         }
                     }
                     else if(results[0].equals("skip")) {
                     output = "Player skipped turn.";
                     switchPlayer();
                     }
                 }
                 else if(results[0].equals("skip")) {
                     output = "Player skipped turn.";
                     switchPlayer();
                 }
 
                 else {
                     int tileSelection = Integer.parseInt(results[0]);
                     if (!(map.getTiles()[tileSelection].getType().equals("town"))) {
                         System.out.println("Map");
                     }
                     else {
                         state = "town";
                     }
                 }
             }
 
             else if (state.equals("town")) {
                 String[] results = renderer.drawTownScreen(players, currPlayer, store, numPlayers, roundNumber);
 
                 if (results[0].equals("time")) {
                     output = "Time's up, switching player";
                     switchPlayer();
                 }
                 else if (results[0].equals("pub")){
                     pub();
                 }
                 else if (results[0].equals("store")){
                     state = "storeBuy";
                 }
                 else if (results[0].equals("land office")){
                     results = renderer.drawLandOfficeScreen(players, currPlayer, store, numPlayers, roundNumber);
                     if(results[0].equals("buy")) {
                         state = "game";
                         results = renderer.drawMainGameScreen(map, players, currPlayer, store, numPlayers, roundNumber, null);
                         int tileSelection = Integer.parseInt(results[0]);
                         if (!(map.getTiles()[tileSelection].getType().equals("town"))) {
                             landSelection(tileSelection, map, "buy");
                         }
                     }
                     else if(results[0].equals("sell")) {
                         state = "game";
                         results = renderer.drawMainGameScreen(map, players, currPlayer, store, numPlayers, roundNumber, null);
                         int tileSelection = Integer.parseInt(results[0]);
                         if (!(map.getTiles()[tileSelection].getType().equals("town"))) {
                             landSelection(tileSelection, map, "sell");
                         }
                     }
                     else if (results[0].equals("back")){
                         state = "town";
                     }  
                 }
                 else if (results[0].equals("assay")){
                     System.out.println("Assay Office");
                 }
                 else if (results[0].equals("back")){
                     state = "game";
                 }
                 else if(results[0].equals("stop")) {
                     results = renderer.drawSaveScreen(players, currPlayer, store, numPlayers, roundNumber);
                     if(results[0].equals("save")) {
                         if (!saveGame(results[1]))
                         {
                             output = "Failed to save game!";
                             state = "game";
                         }
                         else
                         {
                             System.exit(0);
                             initializing = false;
                         }
                     }
                 }
                 else if(results[0].equals("pause")) {
                     renderer.pauseTimer();
                     output = "Game Paused.";
                     results = renderer.drawMenuScreen(players, currPlayer, store, numPlayers, roundNumber, map, output);
                     if(results[0].equals("resume")) {
                         output = "Game Resumed.";
                         state = "game";
                         renderer.unpauseTimer();
                     }
                     else if(results[0].equals("save")) {
                         if (!saveGame(results[1]))
                         {
                             output = "Failed to save game!";
                             state = "game";
                         }
                         else
                         {
                             System.exit(0);
                             initializing = false;
                         }
                     }
                     else if(results[0].equals("skip")) {
                     output = "Player skipped turn.";
                     switchPlayer();
                     }
                 }
                 else if(results[0].equals("skip")) {
                     output = "Player skipped turn.";
                     switchPlayer();
                 }
             }
 
             else if (state.equals("storeBuy")) {
                 String[] results = renderer.drawStoreScreen(players, currPlayer, "buy", quantities, store, numPlayers, roundNumber, output);
                 quantities[0] = results[1];
                 quantities[1] = results[2];
                 quantities[2] = results[3];
                 quantities[3] = results[4];
 
                 if (results[0].equals("time")) {
                     output = "Time's up, switching player";
                     switchPlayer();
                 }
 
                 else if (results[0].equals("quit")) {
                     state = "town";
                     quantities[0] = "0";
                     quantities[1] = "0";
                     quantities[2] = "0";
                     quantities[3] = "0";
                 }
                 else if (results[0].equals("switchScreen")){
                     state = "storeSell";
                 }
                 else if (results[0].equals("food")) {
                     store("buyFood", Integer.parseInt(quantities[0]));
                     quantities[0] = "0";
                 }
                 else if (results[0].equals("energy")) {
                     store("buyEnergy", Integer.parseInt(quantities[1]));
                     quantities[1] = "0";
                 }
                 else if (results[0].equals("smithore")) {
                     store("buySmithore", Integer.parseInt(quantities[2]));
                     quantities[2] = "0";
                 }
                 else if (results[0].equals("crystite")) {
                     store("buyCrystite", Integer.parseInt(quantities[3]));
                     quantities[3] = "0";
                 }
                 else if (results[0].equals("foodMule")) {
                     state = "game";
                     results = renderer.drawMainGameScreen(map, players, currPlayer, store, numPlayers, roundNumber, null);
                     int tileSelection = Integer.parseInt(results[0]);
                     if (!(map.getTiles()[tileSelection].getType().equals("town"))) {
                         boolean wrongTile = mulePlacement(tileSelection, map, "buyFoodMule");
                         if(wrongTile){
                             output = "Lost Mule. Player Mule on wrong tile.";
                             state = "game";
                         }
                     }
                     else {
                         state = "town";
                     }
                 }
                 else if (results[0].equals("energyMule")) {
                     state = "game";
                     results = renderer.drawMainGameScreen(map, players, currPlayer, store, numPlayers, roundNumber, null);
                     int tileSelection = Integer.parseInt(results[0]);
                     if (!(map.getTiles()[tileSelection].getType().equals("town"))) {
                         boolean wrongTile = mulePlacement(tileSelection, map, "buyEnergyMule");
                         if(wrongTile){
                             output = "Lost Mule. Player Mule on wrong tile.";
                             state = "game";
                         }
                     }
                     else {
                         state = "town";
                     }
                 }
                 else if (results[0].equals("smithoreMule")) {
                     state = "game";
                     results = renderer.drawMainGameScreen(map, players, currPlayer, store, numPlayers, roundNumber, null);
                     int tileSelection = Integer.parseInt(results[0]);
                     if (!(map.getTiles()[tileSelection].getType().equals("town"))) {
                         boolean wrongTile = mulePlacement(tileSelection, map, "buySmithoreMule");
                         if(wrongTile){
                             output = "Lost Mule. Player Mule on wrong tile";
                             state = "game";
                         }
                     }
                     else {
                         state = "town";
                     }
                 }
                 else if (results[0].equals("crystiteMule")) {
                     state = "game";
                     results = renderer.drawMainGameScreen(map, players, currPlayer, store, numPlayers, roundNumber, null);
                     int tileSelection = Integer.parseInt(results[0]);
                     if (!(map.getTiles()[tileSelection].getType().equals("town"))) {
                         boolean wrongTile = mulePlacement(tileSelection, map, "buyCrystiteMule");
                         if(wrongTile){
                             output = "Lost Mule. Player Mule on wrong tile";
                             state = "game";
                         }
                     }
                 }
                 else if(results[0].equals("stop")) {
                     results = renderer.drawSaveScreen(players, currPlayer, store, numPlayers, roundNumber);
                     if(results[0].equals("save")) {
                         if (!saveGame(results[1]))
                         {
                             output = "Failed to save game!";
                             state = "game";
                         }
                         else
                         {
                             System.exit(0);
                             initializing = false;
                         }
                     }
                 }
                 else if(results[0].equals("pause")) {
                     renderer.pauseTimer();
                     output = "Game Paused.";
                     results = renderer.drawMenuScreen(players, currPlayer, store, numPlayers, roundNumber, map, output);
                     if(results[0].equals("resume")) {
                         output = "Game Resumed.";
                         state = "game";
                         renderer.unpauseTimer();
                     }
                     else if(results[0].equals("save")) {
                         if (!saveGame(results[1]))
                         {
                             output = "Failed to save game!";
                             state = "game";
                         }
                         else
                         {
                             System.exit(0);
                             initializing = false;
                         }
                     }
                     else if(results[0].equals("skip")) {
                     output = "Player skipped turn.";
                     switchPlayer();
                     }
                 }
                 else if(results[0].equals("skip")) {
                     output = "Player skipped turn.";
                     switchPlayer();
                 }
                 else {
                     state = "town";
                 }
             }
 
             else if (state.equals("storeSell")) {
                 String[] results = renderer.drawStoreScreen(players, currPlayer, "sell", quantities, store, numPlayers, roundNumber, output);
                 quantities[0] = results[1];
                 quantities[1] = results[2];
                 quantities[2] = results[3];
                 quantities[3] = results[4];
 
                 if (results[0].equals("time")) {
                     output = "Time's up, switching player";
                     switchPlayer();
                 }
 
                 else if (results[0].equals("quit")) {
                     state = "town";
                     quantities[0] = "0";
                     quantities[1] = "0";
                     quantities[2] = "0";
                     quantities[3] = "0";
                 }
                 else if (results[0].equals("switchScreen")) {
                     state = "storeBuy";
                 }
                 else if (results[0].equals("food")) {
                     store("sellFood", Integer.parseInt(quantities[0]));
                     quantities[0] = "0";
                 }
                 else if (results[0].equals("energy")) {
                     store("sellEnergy", Integer.parseInt(quantities[1]));
                     quantities[1] = "0";
                 }
                 else if (results[0].equals("smithore")) {
                     store("sellSmithore", Integer.parseInt(quantities[2]));
                     quantities[2] = "0";
                 }
                 else if (results[0].equals("crystite")) {
                     store("sellCrystite", Integer.parseInt(quantities[3]));
                     quantities[3] = "0";
                 }
                 else if (results[0].equals("foodMule")) {
                     state = "game";
                     results = renderer.drawMainGameScreen(map, players, currPlayer, store, numPlayers, roundNumber, null);
                     int tileSelection = Integer.parseInt(results[0]);
                     if (!(map.getTiles()[tileSelection].getType().equals("town"))) {
                         muleRemoval(tileSelection, map, "sellFoodMule");
                     }
                     else {
                         state = "town";
                     }
                 }
                 else if (results[0].equals("energyMule")) {
                     state = "game";
                     results = renderer.drawMainGameScreen(map, players, currPlayer, store, numPlayers, roundNumber, null);
                     int tileSelection = Integer.parseInt(results[0]);
                     if (!(map.getTiles()[tileSelection].getType().equals("town"))) {
                         muleRemoval(tileSelection, map, "sellEnergyMule");
                     }
                     else {
                         state = "town";
                     }
                 }
                 else if (results[0].equals("smithoreMule")) {
                     state = "game";
                     results = renderer.drawMainGameScreen(map, players, currPlayer, store, numPlayers, roundNumber, null);
                     int tileSelection = Integer.parseInt(results[0]);
                     if (!(map.getTiles()[tileSelection].getType().equals("town"))) {
                         muleRemoval(tileSelection, map, "sellSmithoreMule");
                     }
                     else {
                         state = "town";
                     }
                 }
                 else if (results[0].equals("crystiteMule")) {
                     state = "game";
                     results = renderer.drawMainGameScreen(map, players, currPlayer, store, numPlayers, roundNumber, null);
                     int tileSelection = Integer.parseInt(results[0]);
                     if (!(map.getTiles()[tileSelection].getType().equals("town"))) {
                         muleRemoval(tileSelection, map, "sellCrystiteMule");
                     }
                     else {
                         state = "town";
                     }
                 }
                 else if(results[0].equals("stop")) {
                     results = renderer.drawSaveScreen(players, currPlayer, store, numPlayers, roundNumber);
                     if(results[0].equals("save")) {
                         if (!saveGame(results[1]))
                         {
                             output = "Failed to save game!";
                             state = "game";
                         }
                         else
                         {
                             System.exit(0);
                             initializing = false;
                         }
                     }
                 }
                 else if(results[0].equals("pause")) {
                     renderer.pauseTimer();
                     output = "Game Paused.";
                     results = renderer.drawMenuScreen(players, currPlayer, store, numPlayers, roundNumber, map, output);
                     if(results[0].equals("resume")) {
                         output = "Game Resumed.";
                         state = "game";
                         renderer.unpauseTimer();
                     }
                     else if(results[0].equals("save")) {
                         if (!saveGame(results[1]))
                         {
                             output = "Failed to save game!";
                             state = "game";
                         }
                         else
                         {
                             System.exit(0);
                             initializing = false;
                         }
                     }
                     else if(results[0].equals("skip")) {
                     output = "Player skipped turn.";
                     switchPlayer();
                     }
                 }
                 else if(results[0].equals("skip")) {
                     output = "Player skipped turn.";
                     switchPlayer();
                 }
             }
 
             else {
                 System.out.println("done");
                 initializing = false;
             }
         }
     }
 
     /**
      * switchPlayer switches player to determine currPlayer
      */
     private void switchPlayer() {
         double chance = Math.random() * 100;
         if(currPlayer == (numPlayers-1)){
             this.roundNumber++;
             gatherResources();
             reorderPlayers();
             checkForEnd();
         }
 
         if((chance -= 27) < 0){
             RandomEvents randomEvent = new RandomEvents(roundNumber);
             if(numPlayers == 1){
                 output = randomEvent.generate(players, currPlayer, 6);
             }
             else if(players.get(currPlayer).equals(players.get(0))){
                 output = randomEvent.generate(players, currPlayer, 3);
             }
             else{
                 output = randomEvent.generate(players, currPlayer, 6);
             }
         }
         currPlayer = (currPlayer + 1) % numPlayers;
         renderer.restartTimer(getTime());
         startTime = System.currentTimeMillis();
         state = "game";
     }
 
     /**
      * gatherResources gathers resource players made
      *                 from mules after each round
      */
     private void gatherResources() {
         for (Tile t : map.getTiles()) {
             int ownerIndex = getPlayerIndex(t.getOwner());
             if (ownerIndex == -1) 
                 continue;
             Player player = players.get(ownerIndex);
             if (player.getEnergy() > 0) { 
                 t.collectResources();
                 player.setEnergy(player.getEnergy() - 1);
             }
         }
     }
 
     /**
      * getPlayerIndex gets player index
      */
     private int getPlayerIndex(Player player) {
         for (int i = 0; i < numPlayers; i++) {
             if (players.get(i) == player) {
                 return i;
             }
         }
         return -1;
     }
 
     /**
      * getTime gets current round time and sets round time
      *         based on how much food the currPlayer has
      */
     private int getTime() {
         int[] foodReqs = {3, 3, 3, 3, 4, 4, 4, 4, 5, 5, 5, 5};
         Player player = players.get(currPlayer);
         if (player.getFood() >= foodReqs[roundNumber - 1] && player.getEnergy() >= player.getMulesPlaced()) {
             System.out.println("Timer set to: " + 50000);
             return 50000;
         }
         else if (player.getFood() > 0) {
             System.out.println("Timer set to: " + 30000);
             return 30000;
         }
         System.out.println("Timer set to: " + 5000);
         return 5000;
     }
 
     /**
      * reorderPlayers reorders players based on who has the lowest amount of money
      */
     private void reorderPlayers() {
         // insertion sort is the fastest sort for < 30
         for (int i = 0; i < players.size(); i++) {
             Player playerToAdd = players.get(i);
             int index = i;
             while (index > 0 && (playerToAdd.getMoney() < players.get(index - 1).getMoney())) {
                 players.set(index, players.get(index - 1));
                 index--;
             }
             players.set(index, playerToAdd);
         }
     }
 
     /**
      * checkForEnd checks to see if roundNumber is equal to 12
      */
     private void checkForEnd() {
         if (roundNumber <= 12) {
             return;
         }
         String[] results = renderer.drawWinScreen(players, currPlayer, store, numPlayers, roundNumber);
         if(results[0].equals("okay")) {
             String winningPlayer = players.get(numPlayers - 1).getName();
             System.out.println(winningPlayer + " is the winner!");
             System.exit(0);
         }
     }
 
     /**
      * landSelection takes land clicked and assigns it to currPlayer
      * @param tileSelection The tile current player selected
      * @param map Used to set owner of tile to currPlayer
      */
     private void landSelection(int tileSelection, Map map, String choice) {
         if(choice.equals("buy")){
             if(map.getOwnerOfTile(tileSelection) != null){
                 output = "You cannot purchase land that is already owned by another player.";
             }
             int propertyOwned = (int)players.get(currPlayer).getPropertyOwned();
             LandOffice landOffice = new LandOffice(propertyOwned, roundNumber);
             boolean bought = landOffice.buyProperty(tileSelection, players, currPlayer, map);
             if(bought){
                 output = "Successfully purchased land!";
             }
             else{
                 output = "Land not purchased. Player does not have sufficient funds.";
             }
         }
         else if(choice.equals("sell")){
             if(map.getOwnerOfTile(tileSelection) == null || !map.getOwnerOfTile(tileSelection).getName().equals(players.get(currPlayer).getName())){
                 output = "You cannot sell land that you do not own.";
                 return;
             }
             int propertyOwned = (int)players.get(currPlayer).getPropertyOwned();
             LandOffice landOffice = new LandOffice(propertyOwned, roundNumber);
             boolean sold = landOffice.sellingProperty(tileSelection, players, currPlayer, map);
             if(sold){
                 output = "Successfully sold land!";
                 map.getTiles()[tileSelection].sellProperty();
             }
             else{
                 output = "Land not sold. You cannot sell land that you do not own.";
             }
         }
     }
 
     /**
      * mulePlacement takes land clicked and places appropriate mule on it
      * @param tileSelection The tile current player selected
      * @param map Used to set owner of tile to currPlayer
      * @param choice The mule type the player wishes to place on the map
      */
     private boolean mulePlacement(int tileSelection, Map map, String choice) {
         String type = choice.substring(3);
         //Subtract money from player if placed on wrong tile and lose mulse
        if(!map.getOwnerOfTile(tileSelection).equals(players.get(currPlayer))){
             if(store(choice, 1)){
                 output = "Player does not own tile.";
                 return true;
             }
         }
         else if(map.getTiles()[tileSelection].muleIsValid(type)) {
             if(!store(choice, 1)){
                 Tile tile = map.getTiles()[tileSelection];
                 tile.addMule();
                 tile.setMuleType(type);
             }
             else{
                 return false;
             }
         }
         else
         {
         }
         return false;
     }
 
     /**
      * muleRemoval takes land clicked and removes the mule placed on it
      * @param tileSelection The tile current player selected
      * @param map Used to set owner of tile to currPlayer
      * @param choice The mule type the player wishes to remove from the map
      */
     private void muleRemoval(int tileSelection, Map map, String choice) {
         if(!map.getOwnerOfTile(tileSelection).equals(players.get(currPlayer))){
             output = "You do not own this plot. Try again.";
         }
         else{
             String type = choice.substring(4);
             if(map.getTiles()[tileSelection].muleIsValid(type)){
                 if (map.getTiles()[tileSelection].removeMule()) {
                     store(choice, 1);
                 }
             }
             else{
                 output = "Player should have selected a: " + type + " instead of a(n): " + map.getOwnerOfTile(tileSelection).getMuleType();
             }
         }
     }
 
     /**
      * pub allows currPlayer to gamble and make more $$$$.
      */
     private void pub(){
         stopTime = System.currentTimeMillis();
         Integer elapsedTime = ((int)(long)(stopTime - startTime))/1000;
         int timeRemaining = 50 - elapsedTime;
 
         Pub pub = new Pub(roundNumber, timeRemaining);
         output = pub.gamble(players, currPlayer);
         switchPlayer();
     }
 
     /**
      * store allows currPlayer to sell and buy resources.
      * @param choice The resources the player wishes to buy/sell from store
      * @param quantities The amount of resources the player wishes to buy/sell
      */
     private boolean store(String choice, int quantities){
         //BUY
         if(choice.equals("buyFood")){
             if(store.buyItem(players, currPlayer, "food", quantities).equals("noBuy")){
                 output = "Buying more than what store owns.";
             }
             else if(store.buyItem(players, currPlayer, "food", quantities).equals("noFunds")){
                 output = "Player does not have sufficient funds";
             }
             else{
                 output = "Successfully purchased " + quantities + " Food.";
             }
         }
         else if(choice.equals("buyEnergy")){
             if(store.buyItem(players, currPlayer, "energy", quantities).equals("noBuy")){
                 output = "Buying more than what store owns.";
             }
             else if(store.buyItem(players, currPlayer, "energy", quantities).equals("noFunds")){
                 output = "Player does not have sufficient funds";
             }
             else{
                 output = "Successfully purchased " + quantities + " Energy.";
             }
         }
         else if(choice.equals("buySmithore")){
             if(store.buyItem(players, currPlayer, "smithore", quantities).equals("noBuy")){
                 output = "Buying more than what store owns.";
             }
             else if(store.buyItem(players, currPlayer, "smithore", quantities).equals("noFunds")){
                 output = "Player does not have sufficient funds";
             }
             else{
                 output = "Successfully purchased " + quantities + " Smithore.";
             }
         }
         else if(choice.equals("buyCrystite")){
             if(store.buyItem(players, currPlayer, "crystite", quantities).equals("noBuy")){
                 output = "Buying more than what store owns.";
             }
             else if(store.buyItem(players, currPlayer, "crystite", quantities).equals("noFunds")){
                 output = "Player does not have sufficient funds.";
             }
             else{
                 output = "Successfully purchased " + quantities + " Crysite.";
             }
         }
         else if(choice.equals("buyFoodMule")){
             if(store.buyItem(players, currPlayer, "foodMule", quantities).equals("noBuy")){
                 output = "Buying more than what store owns.";
                 return true;
             }
             else{
                 output = "Player successfully bought a Food Mule.";
             }
         }
         else if(choice.equals("buyEnergyMule")){
             if(store.buyItem(players, currPlayer, "energyMule", quantities).equals("noBuy")){
                 output = "Buying more than what store owns.";
                 return true;
             }
             else{
                 output = "Player successfully bought an Energy Mule.";
             }
         }
         else if(choice.equals("buySmithoreMule")){
             if(store.buyItem(players, currPlayer, "smithoreMule", quantities).equals("noBuy")){
                 output = "Buying more than what store owns.";
                 return true;
             }
             else{
                 output = "Player successfully bought a Smithore Mule.";
             }
         }
         else if(choice.equals("buyCrystiteMule")){
             if(store.buyItem(players, currPlayer, "crystiteMule", quantities).equals("noBuy")){
                 output = "Buying more than what store owns.";
                 return true;
             }
             else{
                 output = "Player successfully bought a Crysite Mule.";
             }
         }
 
         //SELL
         if(choice.equals("sellFood")){
             output = store.sellItem(players, currPlayer, "food", quantities);
         }
         else if(choice.equals("sellEnergy")){
             output = store.sellItem(players, currPlayer, "energy", quantities);
         }
         else if(choice.equals("sellSmithore")){
             output = output = store.sellItem(players, currPlayer, "smithore", quantities);
         }
         else if(choice.equals("sellCrystite")){
             output = store.sellItem(players, currPlayer, "crystite", quantities);
         }
         else if(choice.equals("sellFoodMule")){
             output = store.sellItem(players, currPlayer, "foodMule", quantities);
         }
         else if(choice.equals("sellEnergyMule")){
             output = store.sellItem(players, currPlayer, "energyMule", quantities);
         }
         else if(choice.equals("sellSmithoreMule")){
             output = store.sellItem(players, currPlayer, "smithoreMule", quantities);
         }
         else if(choice.equals("sellCrystiteMule")){
             store.sellItem(players, currPlayer, "crystiteMule", quantities);
         }
         return false;
     }
 
     /**
      * ---Work In Progress----
      * showLoadGameSavePartial
      * @param savedGame 
      */
     private void showLoadGameSavePartial(Save savedGame){
 
     }
         
     /**
      * ---Work In Progress----
      * showLoadScreen shows load screen for game
      */
     private void showLoadScreen(){
         Save[] savedGames = getSavedGames();
         LoadScreenModel model = new LoadScreenModel();
         model.setSavedGames(savedGames);
         renderer.drawLoadScreen();
     }
 
     /**
      * Getter method for the game save
      *
      * @return null
      */
     private Save[] getSavedGames() {
         return null;
         //Query database for saved games
     }
 
     /**
      * Getter method for the game's difficulty setting
      *
      * @return Game's difficulty setting
      */
     public int getDifficulty() {
         return difficulty;
     }
 
     /**
      * Getter method for current round
      *
      * @return Game's current round
      */
     public int getRoundNumber() {
         return roundNumber;
     }
 
     private boolean saveGame(String gameName) {
         DBCollection coll = db.getCollection(gameName);
         BasicDBObject doc = new BasicDBObject("name", gameName);
 
         // check if the name already exists
         try {
             BasicDBObject query = new BasicDBObject("name", gameName);
             DBCursor cursor = coll.find(query);
             if (cursor.hasNext()) {
                 System.out.println("That save game already exists!");
                 return false;
             }
         } catch (Exception e) {
             System.out.println("Failed to connect to database!");
             return false;
         }
         Gson gson = new GsonBuilder().create();
         String difficultyJson = gson.toJson(difficulty);
         String roundNumberJson = gson.toJson(roundNumber);
         String currPlayerJson = gson.toJson(currPlayer);
         String numPlayersJson = gson.toJson(numPlayers);
         String mapJson = gson.toJson(map);
         String stateJson = gson.toJson(state);
         String playerJson = gson.toJson(players);
         String startTimeJson = gson.toJson(startTime);
         String stopTimeJson = gson.toJson(stopTime);
         String elapsedTimeJson = gson.toJson(elapsedTime);
         String storeJson = gson.toJson(store);
 
         System.out.println("JSON");
         System.out.println(difficultyJson);
         System.out.println(roundNumberJson);
         System.out.println(currPlayerJson);
         System.out.println(numPlayersJson);
         System.out.println(mapJson);
         System.out.println(stateJson);
         System.out.println(playerJson);
         System.out.println(startTimeJson);
         System.out.println(stopTimeJson);
         System.out.println(elapsedTimeJson);
         System.out.println(storeJson);
 
         doc.append("difficulty", difficultyJson);
         doc.append("roundNumber", roundNumberJson);
         doc.append("currPlayer", currPlayerJson);
         doc.append("numPlayers", numPlayersJson);
         doc.append("map", mapJson);
         doc.append("state", stateJson);
         doc.append("player", playerJson);
         doc.append("startTime", startTimeJson);
         doc.append("stopTime", stopTimeJson);
         doc.append("elapsedTime", elapsedTimeJson);
         doc.append("store", storeJson);
 
         try {
             coll.insert(doc);
             System.out.println("Game successfully saved!");
         }
         catch (Exception e){
             System.out.println("Failed to write to database!");
             return false;
         }
         return true;
     }
 
     private boolean loadGame(String gameName) {
         DBCollection coll = db.getCollection(gameName);
 
         // check if the name already exists
         try {
             BasicDBObject query = new BasicDBObject("name", gameName);
             DBCursor cursor = coll.find(query);
             if (!cursor.hasNext()) {
                 System.out.println("That save game doesn't exists!");
                 return false;
             }
         } catch (Exception e) {
             System.out.println("Failed to connect to database!");
             return false;
         }
 
         DBObject game = coll.findOne();
 
         Type playerList = new TypeToken<ArrayList<Player>>() {}.getType();
 
         Gson gson = new GsonBuilder().create();
         try {
             difficulty = gson.fromJson(game.get("difficulty").toString(), int.class);
             roundNumber = gson.fromJson(game.get("roundNumber").toString(), int.class);
             currPlayer = gson.fromJson(game.get("currPlayer").toString(), int.class);
             numPlayers = gson.fromJson(game.get("numPlayers").toString(), int.class);
             map = gson.fromJson(game.get("map").toString(), Map.class);
             state = gson.fromJson(game.get("state").toString(), String.class);
             players = gson.fromJson(game.get("player").toString(), playerList);
             startTime = gson.fromJson(game.get("startTime").toString(), long.class);
             stopTime = gson.fromJson(game.get("stopTime").toString(), long.class);
             elapsedTime = gson.fromJson(game.get("elapsedTime").toString(), Integer.class);
             store = gson.fromJson(game.get("store").toString(), Store.class);
 
             mainGame();
         } catch (Exception e) {
             System.out.println("Exception in loadGame: " + e);
             System.out.println("Error loading game! Starting new game instead");
             playGame();
         }
         return true;
     }
 }
