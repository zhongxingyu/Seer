 package dungeon.game.messages;
 
 import dungeon.messages.Message;
 
 public class PlayerLeaveCommand implements Message {
   private final int playerId;
 
   public PlayerLeaveCommand (int playerId) {
     this.playerId = playerId;
   }
 
   public int getPlayerId () {
    return playerId;
   }
 }
