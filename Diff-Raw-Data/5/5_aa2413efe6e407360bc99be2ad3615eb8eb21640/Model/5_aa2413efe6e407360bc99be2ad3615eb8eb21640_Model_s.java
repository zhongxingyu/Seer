 package arcade.model;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.MissingResourceException;
 import java.util.ResourceBundle;
 import arcade.database.Database;
 import arcade.exceptions.CorruptedDatabaseException;
 import arcade.exceptions.InvalidPaymentException;
 import arcade.exceptions.LoginErrorException;
 import arcade.exceptions.UsernameTakenException;
 import arcade.games.ArcadeInteraction;
 import arcade.games.Game;
 import arcade.games.GameData;
 import arcade.games.GameInfo;
 import arcade.games.Score;
 import arcade.games.MultiplayerGame;
 import arcade.games.User;
 import arcade.games.UserGameData;
 import arcade.model.payment.DukePaymentManager;
 import arcade.model.payment.PaymentManager;
 import arcade.view.MainView;
 import arcade.view.forms.LoginView;
 
 
 public class Model implements ArcadeInteraction {
 	private static final String RESOURCE_LOCATION = "arcade.resources.";
     private static final String PAYMENT_MANAGER_LOCATION = "arcade.model.payment.";
     public static final String DEFAULT_LOGIN_MESSAGE = "";
     private static final String LOGIN_FAILURE_MESSAGE =
             "The username or password you entered is incorrect";
     private static final String REGISTER_FAILURE_MESSAGE = "That username is already taken.";
     private ResourceBundle myResources;
     private LoginView myLoginView;
     private String myLanguage;
     private Database myDb;
     private Map<String, GameInfo> myGameInfos;
     private List<GameInfo> mySnapshots;
     private String myUser;
     private PaymentManager myPaymentManager;
 
     // These will be null until you try to play a game
     Game myCurrentGame = null;
     MultiplayerGame myCurrentMultiplayerGame = null;
 
     
     public Model (String language) {
     	myResources = ResourceBundle.getBundle(RESOURCE_LOCATION + language);
     	myDb = new Database();
     	myGameInfos = new HashMap<String, GameInfo>();
     	myLoginView = new LoginView(this,myResources);
     }
     
    /*
     public Model (ResourceBundle rb, String language) {
         myResources = rb;
         myLanguage = language;
     }
    */
 
     public void setLoginView (LoginView login) {
         myLoginView = login;
     }
 
     /**
      * 
      * @param directoryPath
      */
     public void publishGame (String directoryPath) {
         return;
     }
 
     /**
      * This should be called after a developer enters the information about
      * his / her game. The method will add the game entry to the database and
      * create a new GameInfo to display in the gamecenter.
      * 
      * This sanitizes all the input so we guarantee that all names an genres are
      * lowercase on the backend.
      * 
      * @param gameName
      * @param genre
      */
     public void publish (String name,
                          String genre,
                          String author,
                          double price,
                          String extendsGame,
                          String extendsMultiplayerGame,
                          int ageRating,
                          boolean singlePlayer,
                          boolean multiplayer,
                          String thumbnailPath,
                          String adScreenPath,
                          String description) {
         System.out.println(extendsGame);
         System.out.println(extendsMultiplayerGame);
         myDb.createGame(name.toLowerCase(), 
                         genre.toLowerCase(), 
                         author, 
                         price,
                         formatClassFilePath(extendsGame),
                         formatClassFilePath(extendsMultiplayerGame), 
                         ageRating, 
                         singlePlayer,
                         multiplayer, 
                         thumbnailPath, 
                         adScreenPath, 
                         description);
         addGameInfo(newGameInfo(name));
     }
 
     /**
      * Tedious Java string manipulation to change something like:
      * C://blah/blah/blah/src/games/rts/ageOfEmpires/game.java
      * to games.rts.ageOfEmpires.game
      * so replace slashes with periods and remove the file extension
      */
     private String formatClassFilePath (String path) {
         if (path == null) return null;
         // split on file extension
         String[] split = path.split(".");
         // take everything before file extension and after src to get java relative filepath.
         List<String> list = Arrays.asList(split);
         if (list.contains("src")) {
             // this means you got the absolute file path, so you need to
             // get java relative file path (i.e. after src/ )
             path = split[0].split("src")[1];
         }
         split = path.split("/");
         String ret = "";
         for (String str : split) {
             ret += str;
             ret += ".";
         }
         // remove the hanging period
         ret = ret.substring(0, ret.length() - 1);
         System.out.println("this is ret" + ret);
         return ret;
     }
 
     private GameInfo newGameInfo (String name) throws MissingResourceException {
         return new GameInfo(myDb, name);
     }
 
     private void addGameInfo (GameInfo game) {
         myGameInfos.put(game.getName(), game);
     }
 
     public void authenticate (String username, String password) throws LoginErrorException {
         if (!myDb.authenticateUsernameAndPassword(username, password)) {
             throw new LoginErrorException();
         }
         myLoginView.dispose();
         organizeSnapshots();
         new MainView(this, myResources);
     }
 
     /**
      * Create a new user profile by entering user-specific information.
      * This information is eventually stored in the database.
      * @throws UsernameTakenException 
      */
     public void createNewUserProfile (String username,
                                       String pw,
                                       String firstname,
                                       String lastname,
                                       String dataOfBirth) throws UsernameTakenException {
         if (myDb.usernameExists(username)) {
             throw new UsernameTakenException();
         }
         myDb.createUser(username, pw, firstname, lastname, dataOfBirth);
         try {
             authenticate(username, pw);
         }
         catch (LoginErrorException e) {
             // this can't happen because just added to db.
             throw new CorruptedDatabaseException();
         }
 
     }
 
     public void createNewUserProfile (String username,
                                       String pw,
                                       String firstname,
                                       String lastname,
                                       String dataOfBirth,
                                       String filepath) throws UsernameTakenException {
         
         if (myDb.usernameExists(username)) {
             throw new UsernameTakenException();
         }
         myDb.createUser(username, pw, firstname, lastname, dataOfBirth, filepath);
         try {
             authenticate(username, pw);
         }
         catch (LoginErrorException e) {
             // this can't happen because just added to db.
             throw new CorruptedDatabaseException();
         }
     }
 
     public void deleteUser (String username) {
         myDb.deleteUser(username);
     }
     
     
     /**
      * First creates the appropriate PaymentManager for the transactionType
      * if the transactionType is Duke, then the DukePaymentManager is created.
      * 
      * Then tries to complete the transaction with the paymentInfo.  If the 
      * transaction is unsuccessful, the InvalidPaymentExecption is thrown.
      * 
      * @param transactionType
      * @param paymentInfo
      * @throws InvalidPaymentException
      */
     public void performTransaction(GameInfo game, String transactionType, String[] paymentInfo) throws InvalidPaymentException {
         try {
             Class<?> paymentManagerClass = Class.forName(PAYMENT_MANAGER_LOCATION + transactionType);
             myPaymentManager = (PaymentManager) paymentManagerClass.newInstance();
         }
         catch (ClassNotFoundException e) {
             throw new InvalidPaymentException();
         }
         catch (InstantiationException e) {
             throw new InvalidPaymentException();
         }
         catch (IllegalAccessException e) {
             throw new InvalidPaymentException();
         }
         
         myPaymentManager.doTransaction(paymentInfo);
         // TODO: write code here for moving game from Store to GameCenter
     }
     
 
     /**
      * Rate a specific game, store in user-game database
      */
     public void rateGame (double rating, String gameName) {
         myDb.updateRating(myUser, gameName, rating);
     }
 
     public void playGame (GameInfo gameinfo) {
         myCurrentGame = gameinfo.getGame(this);
         myCurrentGame.run();
     }
 
     public void playMultiplayerGame (GameInfo gameinfo) {
         MultiplayerGame game = gameinfo.getMultiplayerGame(this);
         game.run();
     }
 
     /**
      * TODO:
      * Get the list of games from the database.
      * 
      * @return
      */
     public Collection<GameInfo> getGameList () {
         return myGameInfos.values();
     }
 
     private void organizeSnapshots () {
         List<String> gameNames = myDb.retrieveListOfGames();
         for (String name : gameNames) {
             try {
                 addGameInfo(newGameInfo(name));
             }
             catch (MissingResourceException e) {
                 continue;
             }
 
         }
     }
 
     /**
      * GameDetailPanel must call this method to get game-specific info.
      * 
      * @param gameName: name of the chosen game (String)
      * @return
      */
     public GameInfo getGameDetail (String gameName) {
         return myGameInfos.get(gameName);
     }
 
     /**
      * TODO: Must add user-game specific detail
      * 
      * @param user ,game (whatever that identifies the user and the game)
      * @return
      */
     public UserGameData getUserGameData (String user, String game) {
         // Query database to get info specific to the user and the game (e.g. scores)
         return null;
     }
 
     @Override
     public User getUser () {
         // TODO get the user's avatar, figure out how we are implementing user info for games
         return null;
     }
 
     public double getAverageRating (String gameName) {
         return myDb.getAverageRating(gameName);
     }
 
     @Override
     public Score getHighScores (int n) {
         // TODO I wish I understood how we are planning on implementing high scores . . .
         // nonetheless: do database stuff here
         return null;
     }
 
     @Override
     public void killGame () {
         // save the usergamedata and game data if applicable, and return to detail screen
 
     }
 
     @Override
     public UserGameData getUserGameData (String gameName) {
         UserGameData ugd = myDb.getUserGameData(gameName, myUser);
         if (ugd == null) {
             // use reflection to find the game class and call the generate user profile method
         }
         return ugd;
     }
 
     @Override
     public GameData getGameData (String gameName) {
         GameData gd = myDb.getGameData(gameName);
         if (gd == null) {
             // use reflection to find the game class and call the generate game method
         }
         return gd;
     }
 }
