 package demo.client.shared.lobby;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 
 import org.jboss.errai.common.client.api.annotations.Portable;
 
 import demo.client.shared.meta.GameRoom;
 import demo.client.shared.meta.Player;
 
 /**
  * A portable bean for transmitting lists of players in lobby and games in progress.
  */
 @Portable
 public class LobbyUpdate {
 
  /** A map player ids to Player objects (of players in lobby). */
   private Map<Integer, Player> playerMap = null;
  /** A map of game ids to Game objects (of games in progress). */
   private Map<Integer, GameRoom> gameMap = null;
 
   /**
   * A default no-arg constructor for proxying.
    */
   public LobbyUpdate() {
   }
 
   /**
    * Construct a LobbyUpdate instance.
    * 
    * @param playerMap
    *          A map of player ids to players, for all the players in the lobby.
    * 
    * @param gameMap
    *          A map of game ids to games, for all the currently in progress games.
    */
   public LobbyUpdate(Map<Integer, Player> players, Map<Integer, GameRoom> games) {
     this.playerMap = players;
     this.gameMap = games;
   }
 
   /**
    * Get a map of the current players in the lobby.
    * 
    * @return Return a map of player ids to players for the players currently in the lobby.
    */
   public Map<Integer, Player> getPlayerMap() {
     return playerMap;
   }
 
   /**
    * Set the map of players currently in the lobby.
    * 
    * @param playerMap
    *          A map of player ids to playerMap for the playerMap currently in the lobby.
    */
   public void setPlayerMap(Map<Integer, Player> players) {
     this.playerMap = players;
   }
 
   /**
    * Set the map of games currently in progress.
    * 
    * @param gameMap
    *          A map of game ids to games of the games currently in progress.
    */
   public void setGameMap(Map<Integer, GameRoom> games) {
     this.gameMap = games;
   }
 
   /**
    * Get the map of games currently in progress.
    * 
    * @return Return a map of game ids to games of the games currently in progress.
    */
   public Map<Integer, GameRoom> getGameMap() {
     return gameMap;
   }
 
   /**
    * Get a list of players currently in the lobby.
    * 
    * @return A list of players currently in the lobby.
    */
   public List<Player> getPlayers() {
     return getterHelper(playerMap);
   }
 
   /**
    * Get a list of currently available games.
    * 
    * @return A list of currently available games.
    */
   public List<GameRoom> getGames() {
     return getterHelper(gameMap);
   }
 
   private <T> List<T> getterHelper(Map<Integer, T> map) {
     List<T> retVal = new ArrayList<T>();
 
     for (T p : map.values()) {
       retVal.add(p);
     }
 
     return retVal;
   }
 }
