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
 
     /**
      * GameController handles all input related actions for game.
      */
     public GameController() {
         renderer = new Renderer();
         currPlayer = 0;
         numPlayers = 1;
         state = "";
         players = new ArrayList<Player>();
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
             /*
             //Load
             else if(state.equals("load")){
             }
             */
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
                 System.out.println("Map created with num " + Integer.parseInt(results[1]));
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
                         System.out.println("That color is taken!");
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
                 System.out.println("State: " + state);
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
                 String[] results = renderer.drawMainGameScreen(map, players, currPlayer, store, numPlayers, roundNumber);
 
                 if (results[0].equals("time")) {
                     System.out.println("Time's up, switching player");
                     switchPlayer();
                 }
 
                 else if(results[0].equals("stop")) {
                     System.out.println("Stop.");
                 }
 
                 else if(results[0].equals("pause")) {
                     //pause timer
                     results = renderer.drawMenuScreen(players, currPlayer, store, numPlayers, roundNumber);
                     if(results[0].equals("resume")) {
                         state = "game";
                     }
                     else if(results[0].equals("save")) {
                         System.out.println("Save");
                     }
                     else if(results[0].equals("load")) {
                         System.out.println("Load");
                     }
                     else if(results[0].equals("quit")) {
                         System.exit(0);
                         initializing = false;
                     }
                 }
 
                 else if(results[0].equals("skip")) {
                     System.out.println("Skip Turn.");
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
                     System.out.println("Time's up, switching player");
                     switchPlayer();
                 }
                 else if (results[0].equals("pub")){
                     pub();
                 }
                 else if (results[0].equals("store")){
                     state = "storeBuy";
                 }
                 else if (results[0].equals("land office")){
                     state = "game";
                     results = renderer.drawMainGameScreen(map, players, currPlayer, store, numPlayers, roundNumber);
                     int tileSelection = Integer.parseInt(results[0]);
                     if (!(map.getTiles()[tileSelection].getType().equals("town"))) {
                         landSelection(tileSelection, map);
                     }                }
                 else if (results[0].equals("assay")){
                     System.out.println("Assay Office");
                 }
                 else if (results[0].equals("back")){
                     state = "game";
                 }
                 else if(results[0].equals("stop")) {
                     System.out.println("Stop.");
                 }
 
                 else if(results[0].equals("pause")) {
                     //pause timer
                     results = renderer.drawMenuScreen(players, currPlayer, store, numPlayers, roundNumber);
                     if(results[0].equals("resume")) {
                         state = "town";
                     }
                     else if(results[0].equals("save")) {
                         System.out.println("Save");
                     }
                     else if(results[0].equals("load")) {
                         System.out.println("Load");
                     }
                     else if(results[0].equals("quit")) {
                         System.exit(0);
                         initializing = false;
                     }
                 }
                 else if(results[0].equals("skip")) {
                     System.out.println("Skip Turn.");
                     switchPlayer();
                 }
             }
 
             else if (state.equals("storeBuy")) {
                 String[] results = renderer.drawStoreScreen(players, currPlayer, "buy", quantities, store, numPlayers, roundNumber);
                 quantities[0] = results[1];
                 quantities[1] = results[2];
                 quantities[2] = results[3];
                 quantities[3] = results[4];
 
                 if (results[0].equals("time")) {
                     System.out.println("Time's up, switching player");
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
                     results = renderer.drawMainGameScreen(map, players, currPlayer, store, numPlayers, roundNumber);
                     int tileSelection = Integer.parseInt(results[0]);
                     if (!(map.getTiles()[tileSelection].getType().equals("town"))) {
                         boolean wrongTile = mulePlacement(tileSelection, map, "buyFoodMule");
                         if(wrongTile){
                             System.out.println("Lost Mule. Should've placed on right tile, bitch.");
                             state = "game";
                         }
                     }
                     else {
                         state = "town";
                     }
                 }
                 else if (results[0].equals("energyMule")) {
                     state = "game";
                     results = renderer.drawMainGameScreen(map, players, currPlayer, store, numPlayers, roundNumber);
                     int tileSelection = Integer.parseInt(results[0]);
                     if (!(map.getTiles()[tileSelection].getType().equals("town"))) {
                         boolean wrongTile = mulePlacement(tileSelection, map, "buyEnergyMule");
                         if(wrongTile){
                             System.out.println("Lost Mule. Should've placed on right tile, bitch.");
                             state = "game";
                         }
                     }
                     else {
                         state = "town";
                     }
                 }
                 else if (results[0].equals("smithoreMule")) {
                     state = "game";
                     results = renderer.drawMainGameScreen(map, players, currPlayer, store, numPlayers, roundNumber);
                     int tileSelection = Integer.parseInt(results[0]);
                     if (!(map.getTiles()[tileSelection].getType().equals("town"))) {
                         boolean wrongTile = mulePlacement(tileSelection, map, "buySmithoreMule");
                         if(wrongTile){
                             System.out.println("Lost Mule. Should've placed on right tile, bitch.");
                             state = "game";
                         }
                     }
                     else {
                         state = "town";
                     }
                 }
                 else if (results[0].equals("crystiteMule")) {
                     state = "game";
                     results = renderer.drawMainGameScreen(map, players, currPlayer, store, numPlayers, roundNumber);
                     int tileSelection = Integer.parseInt(results[0]);
                     if (!(map.getTiles()[tileSelection].getType().equals("town"))) {
                         boolean wrongTile = mulePlacement(tileSelection, map, "buyCrystiteMule");
                         if(wrongTile){
                             System.out.println("Lost Mule. Should've placed on right tile, bitch.");
                             state = "game";
                         }
                     }
                 }
                 else if(results[0].equals("stop")) {
                         System.out.println("Stop.");
                 }
                 else if(results[0].equals("pause")) {
                     //pause timer
                     results = renderer.drawMenuScreen(players, currPlayer, store, numPlayers, roundNumber);
                     if(results[0].equals("resume")) {
                         state = "storeBuy";
                     }
                     else if(results[0].equals("save")) {
                         System.out.println("Save");
                     }
                     else if(results[0].equals("load")) {
                         System.out.println("Load");
                     }
                     else if(results[0].equals("quit")) {
                         System.exit(0);
                         initializing = false;
                     }
                 }
                 else if(results[0].equals("skip")) {
                     System.out.println("Skip Turn.");
                     switchPlayer();
                 }
                 else {
                     state = "town";
                 }
             }
 
             else if (state.equals("storeSell")) {
                 String[] results = renderer.drawStoreScreen(players, currPlayer, "sell", quantities, store, numPlayers, roundNumber);
                 quantities[0] = results[1];
                 quantities[1] = results[2];
                 quantities[2] = results[3];
                 quantities[3] = results[4];
 
                 if (results[0].equals("time")) {
                     System.out.println("Time's up, switching player");
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
                     results = renderer.drawMainGameScreen(map, players, currPlayer, store, numPlayers, roundNumber);
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
                     results = renderer.drawMainGameScreen(map, players, currPlayer, store, numPlayers, roundNumber);
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
                     results = renderer.drawMainGameScreen(map, players, currPlayer, store, numPlayers, roundNumber);
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
                     results = renderer.drawMainGameScreen(map, players, currPlayer, store, numPlayers, roundNumber);
                     int tileSelection = Integer.parseInt(results[0]);
                     if (!(map.getTiles()[tileSelection].getType().equals("town"))) {
                         muleRemoval(tileSelection, map, "sellCrystiteMule");
                     }
                     else {
                         state = "town";
                     }
                 }
                 else if(results[0].equals("stop")) {
                     System.out.println("Stop.");
                 }
                 else if(results[0].equals("pause")) {
                     //pause timer
                     results = renderer.drawMenuScreen(players, currPlayer, store, numPlayers, roundNumber);
                     if(results[0].equals("resume")) {
                         state = "storeSell";
                     }
                     else if(results[0].equals("save")) {
                         System.out.println("Save");
                     }
                     else if(results[0].equals("load")) {
                         System.out.println("Load");
                     }
                     else if(results[0].equals("quit")) {
                         System.exit(0);
                         initializing = false;
                     }
                 }
                 else if(results[0].equals("skip")) {
                     System.out.println("Skip Turn.");
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
         double chance = 25;//Math.random() * 100;
         if(currPlayer == (numPlayers-1)){
             this.roundNumber++;
             gatherResources();
             reorderPlayers();
             checkForEnd();
         }
 
         if((chance -= 27) < 0){
             RandomEvents randomEvent = new RandomEvents(roundNumber);
             if(numPlayers == 1){
                 randomEvent.generate(players, currPlayer, 6);
             }
             else if(players.get(currPlayer).equals(players.get(0))){
                 randomEvent.generate(players, currPlayer, 3);
             }
             else{
                 randomEvent.generate(players, currPlayer, 6);
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
         // make sure player has enough energy
         int[] playerEnergy = new int[numPlayers];
         for (int i = 0; i < numPlayers; i++) {
             playerEnergy[i] = players.get(i).getEnergy();
         }
 
         for (Tile t : map.getTiles()) {
             int ownerIndex = getPlayerIndex(t.getOwner());
             if (ownerIndex == -1) 
                 continue;
             Player player = players.get(ownerIndex);
             if (playerEnergy[ownerIndex] > 0) { 
                 t.collectResources();
                 playerEnergy[ownerIndex]--;
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
         System.out.println("Reordering players");
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
         String winningPlayer = players.get(numPlayers - 1).getName();
         System.out.println(winningPlayer + " is the winner!");
         System.exit(0);
     }
 
     /**
      * landSelection takes land clicked and assigns it to currPlayer
      * @param tileSelection The tile current player selected
      * @param map Used to set owner of tile to currPlayer
      */
     private void landSelection(int tileSelection, Map map) {
         if(map.getOwnerOfTile(tileSelection) != null){
             System.out.println("Sorry, tile already owned by " + map.getOwnerOfTile(tileSelection));
         }
         else{
             int propertyOwned = (int)players.get(currPlayer).getPropertyOwned();
             System.out.println("propertyOwned: " + propertyOwned);
             LandOffice landOffice = new LandOffice(propertyOwned, roundNumber);
             boolean bought = landOffice.buyProperty(tileSelection, players, currPlayer, map);
 
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
         if(map.getOwnerOfTile(tileSelection) != players.get(currPlayer)){
            store(choice, 1);
            System.out.println("Player does not own tile.");
            return true;
         }
         //else {
         else if(map.getTiles()[tileSelection].muleIsValid(type)) {
             if(store(choice, 1)){
                 Tile tile = map.getTiles()[tileSelection];
                 tile.addMule();
                 tile.setMuleType(type); // just get the type
             }
             else{
                 return false;
             }
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
         if(map.getOwnerOfTile(tileSelection) != players.get(currPlayer)){
             System.out.println("You do not own this mule. Try again.");
         }
         else{
             String type = choice.substring(4);
             if(map.getTiles()[tileSelection].muleIsValid(type)){
                 store(choice, 1);
                 map.getTiles()[tileSelection].removeMule();
             }
             else{
                 System.out.println("Player should have selected a: " + type);
                 System.out.println("Player selected a: " + map.getOwnerOfTile(tileSelection).getMuleType());
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
         System.out.println("Elapsed time was " + elapsedTime + " seconds.");
         System.out.println("Player had " + timeRemaining + " seconds remaining.");
 
         Pub pub = new Pub(roundNumber, timeRemaining);
         pub.gamble(players, currPlayer);
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
             System.out.println("Buy " + quantities + " food");
             store.buyItem(players, currPlayer, "food", quantities);
         }
         else if(choice.equals("buyEnergy")){
             System.out.println("Buy " + quantities + " energy");
             store.buyItem(players, currPlayer, "energy", quantities);
         }
         else if(choice.equals("buySmithore")){
             System.out.println("Buy " + quantities + " smithore");
             store.buyItem(players, currPlayer, "smithore", quantities);
         }
         else if(choice.equals("buyCrystite")){
             System.out.println("Buy " + quantities + " crystite");
             store.buyItem(players, currPlayer, "crystite", quantities);
         }
         else if(choice.equals("buyFoodMule")){
             System.out.println("Buy food mule");
             if(store.buyItem(players, currPlayer, "foodMule", quantities)){
                 return true;
             }
         }
         else if(choice.equals("buyEnergyMule")){
             System.out.println("Buy energy mule");
             if(store.buyItem(players, currPlayer, "energyMule", quantities)){
                 return true;
             }
         }
         else if(choice.equals("buySmithoreMule")){
             System.out.println("Buy smithore mule");
             if(store.buyItem(players, currPlayer, "smithoreMule", quantities)){
                 return true;
             }
         }
         else if(choice.equals("buyCrystiteMule")){
             System.out.println("Buy crystite mule");
             if(store.buyItem(players, currPlayer, "crystiteMule", quantities)){
                 return true;
             }
         }
 
         //SELL
         if(choice.equals("sellFood")){
             System.out.println("Sell " + quantities + " food");
             store.sellItem(players, currPlayer, "food", quantities);
         }
         else if(choice.equals("sellEnergy")){
             System.out.println("Sell " + quantities + " energy");
             store.sellItem(players, currPlayer, "energy", quantities);
         }
         else if(choice.equals("sellSmithore")){
             System.out.println("Sell " + quantities + " smithore");
             store.sellItem(players, currPlayer, "smithore", quantities);
         }
         else if(choice.equals("sellCrystite")){
             System.out.println("Sell " + quantities + " crystite");
             store.sellItem(players, currPlayer, "crystite", quantities);
         }
         else if(choice.equals("sellFoodMule")){
             System.out.println("Sell food mule");
             store.sellItem(players, currPlayer, "foodMule", quantities);
         }
         else if(choice.equals("sellEnergyMule")){
             System.out.println("Sell energy mule");
             store.sellItem(players, currPlayer, "energyMule", quantities);
         }
         else if(choice.equals("sellSmithoreMule")){
             System.out.println("Sell smithore mule");
             store.sellItem(players, currPlayer, "smithoreMule", quantities);
         }
         else if(choice.equals("sellCrystiteMule")){
             System.out.println("Sell crystite mule");
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
         renderer.drawLoadScreen(model);
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
 }
