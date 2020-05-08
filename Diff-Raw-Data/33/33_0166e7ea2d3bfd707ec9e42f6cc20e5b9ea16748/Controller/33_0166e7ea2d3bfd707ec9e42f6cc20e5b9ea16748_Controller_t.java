 
 package arcade.controller;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.MissingResourceException;
 import java.util.ResourceBundle;
 import twitter4j.TwitterException;
 import util.FilePathFormatter;
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
 import arcade.games.UserGameData;
 import arcade.model.payment.DukePaymentManager;
 import arcade.model.payment.PaymentManager;
 import arcade.model.social.TwitterConnection;
 import arcade.view.MainView;
 import arcade.view.forms.LoginView;
 
 
 public class Controller implements ArcadeInteraction {
     // Locations
     private static final String RESOURCE_LOCATION = "arcade.resources.";
     private static final String PAYMENT_MANAGER_LOCATION = "arcade.model.payment.";
 
     // Messages
     public static final String DEFAULT_LOGIN_MESSAGE = "";
 
     // Status parameterrs
     private static final String NO_USER_IMAGE = "";
 
     // View
     private LoginView myLoginView;
     private MainView myView;
 
     // Resource
     private ResourceBundle myResources;
 
     // Models
     private Database myDb;
     private Map<String, GameInfo> myGameInfos;
     private PaymentManager myPaymentManager;
     private TwitterConnection myTwitter;
     private FilePathFormatter myFilePathFormatter = new FilePathFormatter();
 
     private Game myCurrentGame;
     private GameInfo myCurrentGameInfo;
 
     public GameInfo getCurrentGameInfo () {
         return myCurrentGameInfo;
     }
 
 
     // Parameters
     private String myLanguage;
     private String currentUser;
     private UserGameData myCurrentUserGameData;
    private GameData myCurrentGameData;
 
     public Controller (String language) {
         myLanguage = language;
         myResources = ResourceBundle.getBundle(RESOURCE_LOCATION + language);
         myDb = new Database();
         myGameInfos = new HashMap<String, GameInfo>();
         myLoginView = new LoginView(this, myResources);
     }
 
     /*
      * public Controller(ResourceBundle rb, String language) {
      * myResources = rb;
      * myLanguage = language;
      * }
      */
 
     public void setLoginView (LoginView login) {
         myLoginView = login;
     }
 
     public void authenticate (String username, String password) throws LoginErrorException {
         if (loginCreteriaNotSatisfied(username, password)) { throw new LoginErrorException(); }
         myLoginView.dispose();
         currentUser = username;
         organizeSnapshots();
         myView = new MainView(this, myResources);
     }
 
     private boolean loginCreteriaNotSatisfied (String username, String password) {
         return !myDb.authenticateUsernameAndPassword(username, password);
     }
 
     private void organizeSnapshots () {
         List<String> gameNames = myDb.retrieveListOfGames();
         for (String name : gameNames) {
             try {
                 addNewGameInfoToList(name);
             }
             catch (MissingResourceException e) {
                 continue;
             }
 
         }
     }
 
     private void addNewGameInfoToList (String gameName) throws MissingResourceException {
         GameInfo g = new GameInfo(myDb, gameName);
         myGameInfos.put(g.getName(), g);
     }
 
     /**
      * Create a new user profile by entering user-specific information. This
      * information is eventually stored in the database.
      * 
      * @throws UsernameTakenException
      */
     public void createNewUserProfile (UserSpecificData data) throws UsernameTakenException {
         if (usernameInDatabase(data.getUsername())) throw new UsernameTakenException();
         myDb.createUser(data);
         try {
             authenticate(data.getUsername(), data.getPassword());
         }
         catch (LoginErrorException e) {
             throw new CorruptedDatabaseException();
         }
     }
 
     /*
      * public void createNewUserProfile(String username, String pw, String firstname, String
      * lastname, String dateOfBirth)
      * throws UsernameTakenException {
      * createNewUserProfile(username, pw, firstname, lastname, dateOfBirth, NO_USER_IMAGE);
      * }
      * 
      * public void createNewUserProfile(String username, String pw, String firstname, String
      * lastname, String dateOfBirth,
      * String imageFilePath) throws UsernameTakenException {
      * if (usernameInDatabase(username)) {
      * throw new UsernameTakenException();
      * }
      * myDb.createUser(username, pw, firstname, lastname, dateOfBirth, imageFilePath);
      * try {
      * authenticate(username, pw);
      * } catch (LoginErrorException e) {
      * // this can't happen because just added to db.
      * throw new CorruptedDatabaseException();
      * }
      * }
      */
 
     private boolean usernameInDatabase (String username) {
         return myDb.usernameExists(username);
     }
 
     public void deleteUser (String username) {
         myDb.deleteUser(username);
     }
 
     /**
      * This should be called after a developer enters the information about his
      * / her game. The method will add the game entry to the database and create
      * a new GameInfo to display in the gamecenter.
      * 
      * This sanitizes all the input so we guarantee that all names an genres are
      * lowercase on the backend.
      * 
      * @param gameName
      * @param genre
      */
     /*
      * public void publish(GameSpecificData data){
      * myDb.createGame(data);
      * addNewGameInfoToList(data.getName());
      * }
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
         // print
         System.out.println(extendsGame);
         System.out.println(extendsMultiplayerGame);
 
         // Put info in DB
         myDb.createGame(name.toLowerCase(), genre.toLowerCase(), author, price,
                         myFilePathFormatter.formatClassFilePath(extendsGame),
                         myFilePathFormatter.formatClassFilePath(extendsMultiplayerGame), ageRating,
                         singlePlayer, multiplayer, thumbnailPath, adScreenPath,
                         description);
 
         // Put game info in map
         addNewGameInfoToList(name);
     }
 
     /**
      * First creates the appropriate PaymentManager for the transactionType if
      * the transactionType is Duke, then the DukePaymentManager is created.
      * 
      * Then tries to complete the transaction with the paymentInfo. If the
      * transaction is unsuccessful, the InvalidPaymentExecption is thrown.
      * 
      * @param transactionType
      * @param paymentInfo
      * @throws InvalidPaymentException
      */
     public void performTransaction (GameInfo game, String transactionType, String[] paymentInfo)
                                                                                                 throws InvalidPaymentException {
         try {
             Class<?> paymentManagerClass =
                     Class.forName(PAYMENT_MANAGER_LOCATION + transactionType);
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
      * Sets up a new twitter request to get access to a user's account.
      * Returns a URL that a user can access to authorize.
      * 
      * @return
      * @throws TwitterException
      */
     public String setUpTwitterRequest () throws TwitterException {
         myTwitter = new TwitterConnection();
         return myTwitter.newRequest();
     }
 
     /**
      * After the user authorizes the twitter request, s/he will have a pin.
      * This gets access using the provided pin, and sends a tweet containing
      * text.
      * 
      * @param pin
      * @param text
      * @throws TwitterException
      */
     public void sendTweet (String pin, String text) throws TwitterException {
         myTwitter.sendTweet(pin, text);
     }
 
     /**
      * Rate a specific game, store in user-game database
      */
     public void commentAndRateGame (String comment, double rating, String gameName) {
         myDb.insertCommentAndRating(currentUser, gameName, comment, rating);
     }
 
     public void playGame (GameInfo gameinfo) {
         myCurrentGame = gameinfo.getGame(this);
         myCurrentGameInfo = gameinfo;
         myCurrentGame.run();
     }
 
 
 
     /**
      * TODO: Get the list of games from the database.
      * 
      * @return
      */
     public Collection<GameInfo> getGameList () {
         return myGameInfos.values();
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
      * UserProfile must call this method to retrieve User-specific information
      */

 
     /**
      * TODO: Must add user-game specific detail
      * 
      * @param user, game (whatever that identifies the user and the game)
      * @return
      */
     public UserGameData getUserGameData (String user, String game) {
         if (myCurrentUserGameData == null ){
             myCurrentUserGameData =  myCurrentGameInfo.getUserGameData(myCurrentGame , user);
         }
         return myCurrentUserGameData;
     }
 
 
 
     public double getAverageRating (String gameName) {
         return myDb.getAverageRating(gameName);
     }
 
  
 
     @Override
     public void killGame () {
         int score = getCurrentUserGameData().getLastScore();
         myDb.addNewHighScore(currentUser, myCurrentGameInfo.getName(),  score);
         myView.showEndGameView(score);
         myCurrentGame = null;
         myCurrentGameInfo = null;
     }
 
     
     private UserGameData getCurrentUserGameData(){
         return myCurrentGameInfo.getUserGameData(myCurrentGame, currentUser);
     }
     
 
     @Override
     public String getCurrentGame () {
         return myCurrentGameInfo.getName();
     }

    @Override
    public GameData getGameData (String gameName) {
      
        if (myCurrentGameData == null ){
            myCurrentGameData =  myCurrentGameInfo.getGameData(myCurrentGame );
        }
        return myCurrentGameData;
        
        
        
        
    }




 }
 
