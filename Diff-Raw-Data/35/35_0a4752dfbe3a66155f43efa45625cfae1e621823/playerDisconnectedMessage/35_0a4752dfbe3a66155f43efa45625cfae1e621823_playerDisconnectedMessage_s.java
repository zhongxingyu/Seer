 package messages;
 
 import channels.messages.ChannelMessage;
 
 import java.io.Serializable;
 
public class PlayerDisconnectedMessage extends ChannelMessage implements Serializable {
     private String playerName;
 
    public PlayerDisconnectedMessage(String playerName) {
         this.playerName = playerName;
     }
 
     public String getPlayerName() {
         return playerName;
     }
 }
