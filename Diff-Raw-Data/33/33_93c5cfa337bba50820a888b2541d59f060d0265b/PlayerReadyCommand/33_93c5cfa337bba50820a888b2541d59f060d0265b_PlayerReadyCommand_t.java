 package dungeon.game.messages;
 
 import dungeon.messages.Message;
 
 import java.io.Serializable;
 
 /**
  * Signals to the server that the player is ready.
  */
 public class PlayerReadyCommand implements Message, Serializable {
   private final int playerId;
 
   public PlayerReadyCommand (int playerId) {
     this.playerId = playerId;
   }
 
   public int getPlayerId () {
    return this.playerId;
   }
 }
