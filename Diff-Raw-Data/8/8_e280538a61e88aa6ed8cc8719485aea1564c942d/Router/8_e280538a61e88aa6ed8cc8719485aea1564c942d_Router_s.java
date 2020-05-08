 package com.steamedpears.comp3004.routing;
 
 import com.google.gson.Gson;
 import com.google.gson.JsonElement;
 import com.google.gson.JsonObject;
 import com.steamedpears.comp3004.SevenWonders;
 import com.steamedpears.comp3004.models.Changeable;
 import com.steamedpears.comp3004.models.players.Player;
 import com.steamedpears.comp3004.models.PlayerCommand;
 import com.steamedpears.comp3004.models.SevenWondersGame;
 import org.apache.log4j.Logger;
 
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ChangeListener;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Set;
 
 public abstract class Router extends Changeable implements ChangeListener {
 
     /**
      * Gets a new HostRouter instance
      * @param port The port to have it listen to
      * @param totalPlayers the total players the Router should make the game with
      * @return a new HostRouter
      */
     public static Router getHostRouter(int port, int totalPlayers){
         log.debug(String.format("Creating Host Router(port:%d,players:%d)",port,totalPlayers));
         return new HostRouter(port,
                 Math.max(Math.min(totalPlayers, SevenWonders.MAX_PLAYERS), SevenWonders.MIN_PLAYERS));
     }
 
     /**
      * Gets a new ClientRouter instance
      * @param ipAddress the ip to connect to
      * @param port the port to connect to
      * @return a new ClientRouter
      */
     public static Router getClientRouter(String ipAddress, int port){
         log.debug("Creating Client Router");
         return new ClientRouter(ipAddress, port);
     }
 
     public static final int HOST_PORT = 1567;
 
     public static final String PROP_ROUTE_CARDS = "cards";
     public static final String PROP_ROUTE_WONDERS = "wonders";
     public static final String PROP_ROUTE_DECK = "deck";
     public static final String PROP_ROUTE_PLAYERS = "players";
     public static final String PROP_ROUTE_YOU_ARE = "ur";
 
     public static final String COMMAND_ROUTE_TAKE_TURN = "takeUrTurn";
     public static final String COMMAND_ROUTE_OK = "k";
 
     private static Logger log = Logger.getLogger(Router.class);
 
 
     //localGame#applyCommands should be called when every player has decided on a command
     private SevenWondersGame localGame;
     private boolean playing;
     private boolean valid;
     private int localPlayerId;
 
     /**
      * Creates a new Router
      */
     protected Router() {
         this.localGame = new SevenWondersGame(this);
         localGame.addChangeListener(this);
         this.playing = false;
         this.valid = true;
     }
 
     /**
      * Registers a given command on the given player.
      * @param player The given player.
      * @param command The given command.
      */
     public abstract void registerMove(Player player, PlayerCommand command);
 
     /**
      * Begins the game associated with this router.
      */
     public abstract void beginGame();
 
     /**
      * Get the number of human players in the game associated with this router.
      * @return The number of human players in the game associated with this router.
      */
     public abstract int getTotalHumanPlayers();
 
     /**
      * Set the state of the game.
      * @param playing True if the game state is "playing."
      */
     protected void setPlaying(boolean playing){
         this.playing = playing;
     }
 
     /**
      * Set the localPlayerId of the Router
      * @param localPlayerId the localPlayerId
      */
     protected void setLocalPlayerId(int localPlayerId) {
         this.localPlayerId = localPlayerId;
     }
 
     /**
      * Get the game associated with this router.
      * @return The game associated with this router.
      */
     public SevenWondersGame getLocalGame(){
         return localGame;
     }
 
     /**
      * Get the state of the game.
      * @return True if the game is "playing."
      */
     public boolean isPlaying(){
         return playing;
     }
 
     /**
      * Get the ID of the local player.
      * @return The ID of the local player.
      */
     public int getLocalPlayerId(){
         return localPlayerId;
     }
 
     /**
      * Serialize the player commands to JSON format.
      * @param commands The Map from Players to PlayerCommands to be serialized.
      * @return The JSON representation of the player commands.
      */
     protected JsonObject playerCommandsToJson(Map<Player, PlayerCommand> commands){
         Gson gson = new Gson();
         JsonObject result = new JsonObject();
         for(Player p: commands.keySet()){
             result.add(""+p.getPlayerId(), gson.toJsonTree(commands.get(p)));
         }
 
         return result;
     }
 
     /**
      * Deserialize the player commands from JSON.
      * @param obj The JSON representation of the player commands.
      * @return A map from each Player to their PlayerCommand for this turn.
      */
     protected Map<Player, PlayerCommand> jsonToPlayerCommands(JsonObject obj){
         Gson gson = new Gson();
 
         Map<Player, PlayerCommand> result = new HashMap<Player, PlayerCommand>();
         Set<Map.Entry<String, JsonElement>> entries = obj.entrySet();
         SevenWondersGame local = getLocalGame();
         for(Map.Entry<String, JsonElement> entry: entries){
             result.put(local.getPlayerById(Integer.parseInt(entry.getKey())),
                     gson.fromJson(entry.getValue(), PlayerCommand.class));
         }
 
         return result;
     }
 
     /**
      * Cleans up router after game is finished.
      */
     public void cleanup() {
         this.valid = false;
         Player.cleanup();
     }
 
     /**
      * Check if this router is still valid
      * @return true iff this router is valid
      */
     public boolean isValid() { return this.valid; }
 
     @Override
     public void stateChanged(ChangeEvent event){
         announceChange(event.getSource());
     }
 
     /**
      * Set local wonder, only implemented in host.
      * @param wonder the wonder to set the user to
      */
     public void setLocalWonder(String wonder) {}
 
     /**
      * Set local side, only implemented in host.
      * @param side the side to set the user to
      */
     public void setLocalSide(String side) {}
 }
