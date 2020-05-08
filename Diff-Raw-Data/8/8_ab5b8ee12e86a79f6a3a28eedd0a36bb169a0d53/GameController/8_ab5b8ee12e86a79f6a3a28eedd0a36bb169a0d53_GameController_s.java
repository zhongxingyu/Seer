 
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
 	
 	public static final int EASY_DIFFICULTY = 1;
 	public static final int MEDIUM_DIFFICULTY = 2;
 	public static final int HARD_DIFFICULTY = 3;
 	public static final int DIFFICULTY_NOT_SET = 0;
 
 	private int difficulty;
 	private Renderer renderer;
 
 
 	public GameController() {
 		renderer = new Renderer();
         startGame();
 	}
 
 	private void startGame() {
 
         String state = "intro";
         boolean initializing = true;
 
         // variables we are collecting
         String difficulty = "";
         int numPlayers = 1;
         int map = 1;
         ArrayList<Player> players = new ArrayList<Player>();
 
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
 
             // Setup Screen
             else if (state.equals("setup")) {
                String[] results = renderer.drawSetupScreen();
                String action = results[0];
                difficulty = results[1];
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
                 String[] results = renderer.drawMapScreen();
                 String action = results[0];
                 map = Integer.parseInt(results[1]);
                 if (action.equals("okay")) {
                     state = "player";
                 }
                 else {
                     state = "setup";
                 }
             }
 
             // Character selection screen
             else if (state.equals("player")) {
                 String[] results = renderer.drawCharacterScreen();
                 String action = results[0];
                 if (action.equals("back")) {
                     state = "map";
                 }
                 else {
                    players.add(new Player(results[2], results[1], results[3]));
 
                     // only move on if we have all the players
                     if (--numPlayers == 0) {
                         state = "done";
                     }
                 }
             }
 
 
             // quit state
             else {
                 System.out.println("State: " + state);
                 break;
             }
         }
         numPlayers = players.size();
         System.out.println("Difficulty: " + difficulty);
         System.out.println("NumPlayers: " + numPlayers);
         System.out.println("Map: " + map);
         for (Player p : players) {
             System.out.println(p);
         }
         System.exit(0);
 	}
 	
 	private void showLoadGameSavePartial(Save savedGame){
 		
 	}
 	
 	private void showLoadScreen(){
 		Save[] savedGames = getSavedGames();
 		LoadScreenModel model = new LoadScreenModel();
 		model.setSavedGames(savedGames);
 		renderer.drawLoadScreen(model);
 	}
 
 	private Save[] getSavedGames() {
 		return null;
 		//Query database for saved games
 	}
 
 	public int getDifficulty() {
 		return difficulty;
 	}
 }
