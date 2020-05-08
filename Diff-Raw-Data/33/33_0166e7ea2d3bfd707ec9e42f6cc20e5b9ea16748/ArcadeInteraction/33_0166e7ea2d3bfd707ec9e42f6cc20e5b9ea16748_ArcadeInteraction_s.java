 package arcade.games;
 
 /**
  * Arcade Model will implement ArcadeDataSource
  * 
  * Represents all the information and functionality that a Game should need 
  * from the Arcade.
  * 
  * @author ArcadeTeam
  */
 public interface ArcadeInteraction {
    
         
    
 
     /**
      * Closes the window containing the game
      */
     void killGame();
     
     /**
      * Get data associated with a specific game and a user
      * @return UserGameData
      */
    public UserGameData getUserGameData(String gameName);
     
     public GameData getGameData(String gameName);
 
     String getCurrentGame ();
 
         
 }
